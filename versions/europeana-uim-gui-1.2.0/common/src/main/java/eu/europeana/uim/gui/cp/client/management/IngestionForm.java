package eu.europeana.uim.gui.cp.client.management;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.europeana.uim.gui.cp.client.services.ExecutionServiceAsync;
import eu.europeana.uim.gui.cp.shared.CollectionDTO;
import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
import eu.europeana.uim.gui.cp.shared.ParameterDTO;
import eu.europeana.uim.gui.cp.shared.ProviderDTO;
import eu.europeana.uim.gui.cp.shared.WorkflowDTO;

/**
 * A form used for editing contacts.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Apr 27, 2011
 */
public class IngestionForm extends Composite {
    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, IngestionForm> {
    }

    private final ExecutionServiceAsync executionService;

    @UiField
    TextBox                             nameBox;
    @UiField
    TextBox                             providerBox;
    @UiField
    TextBox                             collectionBox;
    @UiField
    TextBox                             workflowBox;
    @UiField
    TextArea                            commandLineBox;

    @UiField
    Button                              commitButton;
    @UiField
    Button                              cancelButton;

    private ProviderDTO                 provider;
    private CollectionDTO               collection;
    private WorkflowDTO                 workflow;

    private String                      autoText;

    private final Set<ParameterDTO>     changedParameters = new HashSet<ParameterDTO>();

    /**
     * Creates a new instance of this class.
     * 
     * @param executionService
     * @param handler
     */
    public IngestionForm(ExecutionServiceAsync executionService, final ClickHandler handler) {
        this.executionService = executionService;
        initWidget(uiBinder.createAndBindUi(this));

        providerBox.setReadOnly(true);
        collectionBox.setReadOnly(true);
        workflowBox.setReadOnly(true);
        commandLineBox.setReadOnly(true);
        commitButton.setEnabled(false);

        commitButton.addClickHandler(handler);
        commitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (collection.getName().equals(TriggerTreeViewModel.ALL_COLLECTIONS)) {
                    executeProvider();
                } else {
                    executeCollection();
                }
                clearForm();
            }
        });

        cancelButton.addClickHandler(handler);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearForm();
            }
        });
    }

    private void clearForm() {
        nameBox.setText("");
        setProvider(null);
        setCollection(null);
        setWorkflow(null);
        setCommandline(null);
        changedParameters.clear();
    }

    private void executeCollection() {
        executionService.startCollection(workflow.getIdentifier(), collection.getId(),
                nameBox.getText(), changedParameters, new AsyncCallback<ExecutionDTO>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        // TODO panic
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(ExecutionDTO execution) {
                    }
                });
    }

    private void executeProvider() {
        executionService.startProvider(workflow.getIdentifier(), provider.getId(),
                nameBox.getText(), changedParameters, new AsyncCallback<ExecutionDTO>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        // TODO panic
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(ExecutionDTO execution) {
// application.getOverview().addExecution(execution);
                    }
                });
    }

    /**
     * @param provider
     */
    public void setProvider(ProviderDTO provider) {
        this.provider = provider;
        providerBox.setText(provider != null ? provider.getName() : "");
        if (nameBox.getText().equals(autoText)) {
            nameBox.setText("");
        }
    }

    /**
     * @param collection
     */
    public void setCollection(CollectionDTO collection) {
        this.collection = collection;
        collectionBox.setText(collection != null ? collection.getName() : "");
        if (nameBox.getText().equals(autoText)) {
            nameBox.setText("");
        }
    }

    /**
     * @param commandLine
     */
    public void setCommandline(String commandLine) {
        commandLineBox.setText(commandLine != null ? commandLine : "");
    }

    /**
     * @param workflow
     */
    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
        workflowBox.setText(workflow != null ? workflow.getName() : "");
        if (nameBox.getText().equals(autoText)) {
            nameBox.setText("");
        }
        if (nameBox.getText().length() == 0 && workflow != null) {
            autoText = workflow.getIdentifier() +
                       "/" +
                       (collection.getName().equals(TriggerTreeViewModel.ALL_COLLECTIONS)
                               ? provider.toString() : collection.toString());
            nameBox.setText(autoText);
        }
        checkCommit();
    }

    private void checkCommit() {
        if (provider != null && collection != null && workflow != null) {
            commitButton.setEnabled(true);
        } else {
            commitButton.setEnabled(false);
        }
    }

    /**
     * @param provider
     * @param collection
     * @param workflow
     */
    public void setDatasets(ProviderDTO provider, CollectionDTO collection, WorkflowDTO workflow) {
        this.provider = provider;
        this.collection = collection;
        this.workflow = workflow;
        if (provider != null) {
            providerBox.setText(provider.getName());
        }

        if (collection != null) {
            String string = collection.getName();
            string += "@" + collection.getLanguage();
            String oai = collection.getOaiBaseUrl(true);
            if (oai != null) {
                string += " OAI:[";
                string += oai + "?verb={x}";

                string += collection.getOaiSet() != null ? "&set=" + collection.getOaiSet() : "";
                string += collection.getOaiMetadataPrefix(true) != null
                        ? "&metadataPrefix=" + collection.getOaiMetadataPrefix(true) : "";
                string += "]";
            }
            collectionBox.setText(string);
        }

        if (workflow != null) {
            workflowBox.setText(workflow.getName());
        }

        checkCommit();
    }

    /**
     * Adds a changed and therefore local parameter setting to the changed parameter set.
     * 
     * @param parameter
     */
    public void addLocalParameter(ParameterDTO parameter) {
        changedParameters.add(parameter);
    }
}
