package eu.europeana.uim.orchestration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.orchestration.processing.TaskExecutor;
import eu.europeana.uim.orchestration.processing.TaskExecutorRegistry;
import eu.europeana.uim.orchestration.processing.TaskExecutorThread;
import eu.europeana.uim.orchestration.processing.TaskExecutorThreadFactory;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.workflow.Task;
import eu.europeana.uim.workflow.TaskCreator;
import eu.europeana.uim.workflow.TaskStatus;
import eu.europeana.uim.workflow.WorkflowStart;

/**
 * Processes a UIM workflow as a runnable.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Mar 22, 2011
 */
public class UIMWorkflowProcessor implements Runnable {
    private static Logger                                             log              = Logger.getLogger(UIMWorkflowProcessor.class.getName());

    private TaskExecutorThreadFactory                                 factory          = new TaskExecutorThreadFactory(
                                                                                               "processor");
    private TaskExecutorThread                                        dispatcherThread;

    private final Registry                                            registry;

    private boolean                                                   running          = false;

    @SuppressWarnings("unchecked")
    private static TKey<UIMWorkflowProcessor, ArrayList<TaskCreator>> SCHEDULED        = TKey.register(
                                                                                               UIMWorkflowProcessor.class,
                                                                                               "creators",
                                                                                               (Class<ArrayList<TaskCreator>>)new ArrayList<TaskCreator>().getClass());

    private List<ActiveExecution<?>>                                  executions       = new ArrayList<ActiveExecution<?>>();

    private int                                                       maxTotalProgress = 5000;

    private int                                                       maxInProgress    = 1000;

    /**
     * Creates a new instance of this class.
     * 
     * @param registry
     */
    public UIMWorkflowProcessor(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void run() {
        List<ActiveExecution<?>> active = new ArrayList<ActiveExecution<?>>();

        running = true;
        while (running) {
            boolean isbusy = false;
            int totalProgress = 0;

            active.clear();
            synchronized (executions) {
                active.addAll(executions);
            }

            for (ActiveExecution<?> execution : active) {
                totalProgress += execution.getProgressSize();
            }

            try {

                Iterator<ActiveExecution<?>> activeIterator = active.iterator();
                while (activeIterator.hasNext()) {
                    ActiveExecution<?> execution = activeIterator.next();

                    try {
                        // we ask the workflow start if we have more to do
                        WorkflowStart start = execution.getWorkflow().getStart();

                        if (execution.isInitialized() && !execution.isPaused()) {
                            boolean newtasks = false;
                            int execProgress = execution.getProgressSize();
                            if (totalProgress <= maxTotalProgress && execProgress <= maxInProgress) {
                                newtasks = ensureTasksInProgress(execution, start, execProgress,
                                        totalProgress);
                            }
                            if (execProgress == 0 && !newtasks) {
                                ArrayList<TaskCreator> creators = execution.getValue(SCHEDULED);
                                if (creators.isEmpty()) {
                                    if (execution.getMonitor().isCancelled()) {
                                        // cancelled and nothing in progress
                                        if (execution.isFinished()) {
                                            complete(execution, true);
                                        }
                                    } else if (start.isFinished(execution,
                                            execution.getStorageEngine())) {
                                        // everything done no new
                                        if (execution.isFinished()) {
                                            Thread.sleep(100);
                                            if (execution.isFinished()) {
                                                complete(execution, false);
                                            }
                                        }
                                    }
                                }
                            } else {
                                IngestionPlugin[] steps = execution.getWorkflow().getSteps().toArray(
                                        new IngestionPlugin[0]);
                                for (int i = steps.length - 1; i >= 0; i--) {
                                    IngestionPlugin thisStep = steps[i];

                                    Queue<Task> prevSuccess = i > 0
                                            ? execution.getSuccess(steps[i - 1].getIdentifier())
                                            : execution.getSuccess(start.getIdentifier());

                                    Queue<Task> thisSuccess = execution.getSuccess(thisStep.getIdentifier());
                                    Queue<Task> thisFailure = execution.getFailure(thisStep.getIdentifier());
                                    Set<Task> thisAssigned = execution.getAssigned(thisStep.getIdentifier());

                                    boolean savepoint = execution.getWorkflow().isSavepoint(
                                            thisStep.getIdentifier());
                                    boolean mandatory = execution.getWorkflow().isMandatory(
                                            thisStep.getIdentifier());

                                    // if we are the "last" step we need to handle
                                    // the last success queue here.
                                    if (i == steps.length - 1) {
                                        finishTasksLastSuccess(execution, thisSuccess);
                                    }

                                    // get successful tasks from previous step
                                    // and schedule them into the step executor
                                    // of this step
                                    TaskExecutor executor = TaskExecutorRegistry.getInstance().getExecutor(
                                            thisStep.getIdentifier());

                                    Task task = null;
                                    synchronized (prevSuccess) {
                                        task = prevSuccess.poll();
                                    }

                                    while (task != null) {
                                        isbusy |= true; // well there is something todo
                                        
                                        task.setStep(thisStep, mandatory);
                                        task.setSavepoint(savepoint);
                                        task.setOnSuccess(thisSuccess);
                                        task.setOnFailure(thisFailure);
                                        task.setAssigned(thisAssigned);
                                        // mandatory
                                        task.setStatus(TaskStatus.QUEUED);

                                        synchronized (thisAssigned) {
                                            thisAssigned.add(task);
                                        }
                                        if (!executor.isShutdown()) {
                                            executor.execute(task);
                                            
                                            // if this is the first step,
                                            // then we have just now scheduled a
                                            // newly created task from the start plugin.
                                            if (i == 0) {
                                                execution.incrementScheduled(1);
                                            }

                                            if (execution.getThrowable() != null) {
                                                //log.log(Level.WARNING, "Failed execution.", execution.getThrowable());
                                                
                                                execution.setThrowable(task.getThrowable());
                                                complete(execution, true);
                                                break;
                                            }

                                            synchronized (prevSuccess) {
                                                task = prevSuccess.poll();
                                            }
                                        }

                                    }
                                }
                            }
                        } else {
                            // this is paused or not initialized ... are we now cancelled - if yes
// stop
                            if (execution.getMonitor().isCancelled()) {
                                complete(execution, true);
                            }
                        }
                    } catch (Throwable exc) {
                        log.log(Level.WARNING, "Exception in workflow execution", exc);
                    }
                } // end synchronized execution
            } catch (Throwable exc) {
                log.log(Level.SEVERE, "Exception in workflow executor", exc);
            }

            if (!isbusy) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }

            if (executions.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void finishTasksLastSuccess(ActiveExecution<?> execution, Queue<Task> thisSuccess) {
        // save and clean final
        Task task = null;
        synchronized (thisSuccess) {
            task = thisSuccess.poll();
        }
        while (task != null) {
            execution.incrementCompleted(1);
            execution.getMonitor().worked(1);

            synchronized (thisSuccess) {
                task = thisSuccess.poll();
            }
        }
    }

    private boolean ensureTasksInProgress(ActiveExecution<?> execution, WorkflowStart start,
            int execProgress, int totalProgress) throws StorageEngineException {
        // how many creators do we have
        ArrayList<TaskCreator> creators = execution.getValue(SCHEDULED);

        // number of scheduled but not done creation tasks.
        int activeCreators = 0;
        Iterator<TaskCreator> creatorsIterator = creators.iterator();
        while (creatorsIterator.hasNext()) {
            TaskCreator next = creatorsIterator.next();
            if (next.isDone()) {
                creatorsIterator.remove();
            } else {
                activeCreators++;
            }
        }

        if (!execution.getMonitor().isCancelled() &&
            !start.isFinished(execution, execution.getStorageEngine())) {
            if (activeCreators < 3) {
                log.fine("Less than 3 outstanding batches: <" + execution.getId() +
                         ">  execution/total progress:" + execProgress + "/" + totalProgress);
                TaskCreator createLoader = start.createLoader(execution,
                        execution.getStorageEngine());
                if (createLoader != null) {
                    createLoader.setQueue(execution.getSuccess(start.getIdentifier()));
                    creators.add(createLoader);

                    TaskExecutorRegistry.getInstance().getExecutor(start.getIdentifier()).execute(
                            createLoader);
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void complete(ActiveExecution execution, boolean cancel)
            throws StorageEngineException {
        try {
            execution.getWorkflow().getStart().completed(execution);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Failed to complete:" + execution.getWorkflow().getStart(), t);
        }
        for (IngestionPlugin step : execution.getWorkflow().getSteps()) {
            try {
                step.completed(execution);
            } catch (Throwable t) {
                log.log(Level.SEVERE, "Failed to complete:" + step, t);
            }
        }

        synchronized (execution) {
            execution.setActive(false);
            execution.setEndTime(new Date());
            if (cancel) {
                execution.setCanceled(true);
            } else {
                execution.setCanceled(false);
            }
            execution.setSuccessCount(execution.getCompletedSize());
            execution.setFailureCount(execution.getFailureSize());
            execution.setProcessedCount(execution.getScheduledSize());
            execution.getStorageEngine().updateExecution(execution.getExecution());
        }

        try {
            execution.getStorageEngine().checkpoint();
            execution.cleanup();
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Failed to complete:" + execution, t);
        } finally {
            execution.getStorageEngine().completed(execution);
            if (registry.getLoggingEngine() != null)
                registry.getLoggingEngine().log("UIMOrchestrator", execution, "finish",
                        LoggingEngine.Level.INFO, "Finished:" + execution.getName());

            log.warning("Remove Execution:" + execution.toString());
            synchronized (executions) {
                executions.remove(execution);
            }
        }

    }

    /**
     * Schedules the given execution.
     * 
     * @param execution
     * @throws StorageEngineException
     */
    public void schedule(final ActiveExecution<Task> execution) throws StorageEngineException {
        if (execution.getWorkflow().getSteps().isEmpty())
            throw new IllegalStateException("Empty workflow not allowed: " +
                                            execution.getWorkflow().getClass().getName());

        // init in separate thread, so that we are not blocking here.
        new Thread(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                try {
                    synchronized (executions) {
                        executions.add(execution);
                    }

                    WorkflowStart start = execution.getWorkflow().getStart();
                    start.initialize(execution, execution.getStorageEngine());

                    TaskExecutorRegistry.getInstance().initialize(start.getIdentifier(),
                            start.getPreferredThreadCount(), start.getMaximumThreadCount());

                    HashSet<String> unique = new HashSet<String>();
                    for (IngestionPlugin step : execution.getWorkflow().getSteps()) {
                        if (unique.contains(step.getIdentifier())) {
                            throw new IllegalArgumentException(
                                    "Workflow contains duplicate plugin:" +
                                            step.getClass().getSimpleName());
                        } else {
                            unique.add(step.getIdentifier());
                        }

                        step.initialize(execution);
                        TaskExecutorRegistry.getInstance().initialize(step.getIdentifier(),
                                step.getPreferredThreadCount(), step.getMaximumThreadCount());
                    }

                    execution.putValue(SCHEDULED, new ArrayList<TaskCreator>());
                    execution.setInitialized(true);

                } catch (Throwable t) {
                    log.log(Level.SEVERE, "Failed to startup execution.", t);
                    try {

                        execution.setThrowable(t);
                        execution.setActive(false);
                        execution.setEndTime(new Date());
                        execution.getStorageEngine().updateExecution(
                                (Execution<Task>)execution.getExecution());
                    } catch (StorageEngineException e) {
                        log.log(Level.SEVERE, "Failed to persist failed execution.", e);
                    } finally {
                        synchronized (executions) {
                            executions.remove(execution);
                        }
                    }
                }
            }
        }, "Initializer" + execution.getId() + ": " + execution.getWorkflowName()).start();
    }

    /**
     * @return scheduled executions
     */
    public synchronized List<ActiveExecution<?>> getExecutions() {
        ArrayList<ActiveExecution<?>> result = new ArrayList<ActiveExecution<?>>();
        synchronized (executions) {
            result.addAll(executions);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Initializes the processor, right now only switch running flag.
     */
    public void initialize() {
        running = false;
    }

    /**
     * Starts up the thread for dispatching.
     */
    public void startup() {
        dispatcherThread = (TaskExecutorThread)factory.newThread(this);
        dispatcherThread.start();
    }

    /**
     * Shuts down the dispatching and stops running.
     */
    public void shutdown() {
        running = false;
        dispatcherThread = null;
    }

    /**
     * Halt temporarily the execution.
     */
    public void pause() {
        running = false;
        dispatcherThread = null;
    }

    /**
     * Continue running.
     */
    public void resume() {
        running = true;
        dispatcherThread = (TaskExecutorThread)factory.newThread(this);
        dispatcherThread.start();
    }

    /**
     * @return number of processes in progress
     */
    public int getMaxTotalProgress() {
        return maxTotalProgress;
    }

    /**
     * @param maxTotalProgress
     *            number of processes in progress
     */
    public void setMaxTotalProgress(int maxTotalProgress) {
        this.maxTotalProgress = maxTotalProgress;
    }

    /**
     * @param maxInProgress
     *            maximum number of processes in progress
     */
    public void setMaxInProgress(int maxInProgress) {
        this.maxInProgress = maxInProgress;
    }

    /**
     * Returns the maxInProgress.
     * 
     * @return maximum number of processes in progress
     */
    public int getMaxInProgress() {
        return maxInProgress;
    }
}
