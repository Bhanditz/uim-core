package org.theeuropeanlibrary.uim.gui.gwt.client.content;

import java.util.Comparator;
import java.util.Set;

import org.theeuropeanlibrary.uim.gui.gwt.client.IngestionCockpitWidget;
import org.theeuropeanlibrary.uim.gui.gwt.client.OrchestrationServiceAsync;
import org.theeuropeanlibrary.uim.gui.gwt.client.content.BrowserTreeViewModel.BrowserObject;
import org.theeuropeanlibrary.uim.gui.gwt.shared.CollectionDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.ProviderDTO;
import org.theeuropeanlibrary.uim.gui.gwt.shared.WorkflowDTO;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SimpleKeyProvider;

/**
 * Triggers execution.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Apr 27, 2011
 */
public class ExecutionTriggerWidget extends IngestionCockpitWidget {
    /**
     * The UiBinder interface used by this example.
     */
    interface Binder extends UiBinder<Widget, ExecutionTriggerWidget> {
    }

    private final OrchestrationServiceAsync orchestrationService;

    @UiField
    DecoratorPanel                          decPanel;

    @UiField
    SplitLayoutPanel                        splitPanel;

    @UiField
    LayoutPanel                             leftPanel;

    @UiField
    LayoutPanel                             rightPanel;

// @UiField(provided = true)
    CellBrowser                             cellBrowser;

// @UiField(provided = true)
    CellTable<String>                       cellTable;

    @UiField(provided = true)
    ExecutionForm                           executionForm;

// @UiField
// Button startButton;

    /**
     * Creates a new instance of this class.
     * 
     * @param orchestrationService
     */
    public ExecutionTriggerWidget(OrchestrationServiceAsync orchestrationService) {
        super(
                "Start Execution",
                "This view allows to select provider, collection and workflow and optional the resources to start a new execution!");
        this.orchestrationService = orchestrationService;
    }

    /**
     * Initialize this example.
     */
    @Override
    public Widget onInitialize() {
        executionForm = new ExecutionForm(orchestrationService, new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                for (int i = 0; i < cellBrowser.getRootTreeNode().getChildCount(); i++) {
                    cellBrowser.getRootTreeNode().setChildOpen(i, false);
                }
            }
        });
        
        Binder uiBinder = GWT.create(Binder.class);
        Widget widget = uiBinder.createAndBindUi(this);

        final MultiSelectionModel<BrowserObject> selectionModelBrowser = new MultiSelectionModel<BrowserObject>(
                BrowserTreeViewModel.KEY_PROVIDER);
        selectionModelBrowser.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<BrowserObject> vals = selectionModelBrowser.getSelectedSet();
                for (BrowserObject val : vals) {
                    if (val.getWrappedObject() instanceof ProviderDTO) {
                        executionForm.setProvider((ProviderDTO) val.getWrappedObject());
                        executionForm.setCollection(null); 
                        executionForm.setWorkflow(null); 
                    } else if (val.getWrappedObject() instanceof CollectionDTO) {
                        executionForm.setCollection((CollectionDTO) val.getWrappedObject()); 
                        executionForm.setWorkflow(null); 
                    } else if (val.getWrappedObject() instanceof WorkflowDTO) {
                        executionForm.setWorkflow((WorkflowDTO) val.getWrappedObject());
                    } 
                }
            }
        });

        BrowserTreeViewModel browserTreeViewModel = new BrowserTreeViewModel(orchestrationService, selectionModelBrowser); 
        cellBrowser = new CellBrowser(browserTreeViewModel, null);
        cellBrowser.setAnimationEnabled(true);
        cellBrowser.setSize("100%", "100%");

        leftPanel.add(cellBrowser);

        cellTable = new CellTable<String>(new SimpleKeyProvider<String>());
        cellTable.setWidth("100%", true);
        cellTable.setHeight("30px");
//        cellTable.setPageSize(15);
//        cellTable.setRowCount(10);

        final ListDataProvider<String> dataProvider = new ListDataProvider<String>();
        dataProvider.addDataDisplay(cellTable);

        ListHandler<String> sortHandler = new ListHandler<String>(
                new ListDataProvider<String>().getList());
        cellTable.addColumnSortHandler(sortHandler);

        final MultiSelectionModel<String> selectionModelTable = new MultiSelectionModel<String>(
                new SimpleKeyProvider<String>());
        cellTable.setSelectionModel(selectionModelTable,
                DefaultSelectionEventManager.<String> createCheckboxManager());

        initTableColumns(selectionModelTable, sortHandler);

        rightPanel.add(cellTable);

        return widget;
    }

    @Override
    protected void asyncOnInitialize(final AsyncCallback<Widget> callback) {
        GWT.runAsync(ExecutionTriggerWidget.class, new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess() {
                callback.onSuccess(onInitialize());
            }
        });
    }

    /**
     * Add the columns to the table.
     */
    private void initTableColumns(final SelectionModel<String> selectionModel,
            ListHandler<String> sortHandler) {
        // Key
        Column<String, String> keyColumn = new Column<String, String>(new TextCell()) {
            @Override
            public String getValue(String object) {
                return object;
            }
        };
        keyColumn.setSortable(true);
        sortHandler.setComparator(keyColumn, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        cellTable.addColumn(keyColumn, "Resource Name");
        cellTable.setColumnWidth(keyColumn, 20, Unit.PCT);

        // Value
        Column<String, String> valueColumn = new Column<String, String>(new TextCell()) {
            @Override
            public String getValue(String object) {
                return object;
            }
        };
        valueColumn.setSortable(true);
        sortHandler.setComparator(valueColumn, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        cellTable.addColumn(valueColumn, "Resource Value");
        cellTable.setColumnWidth(valueColumn, 20, Unit.PCT);

        // Update Button
        Column<String, String> updateColumn = new Column<String, String>(new ActionCell<String>(
                "Update", new ActionCell.Delegate<String>() {
                    @Override
                    public void execute(String contact) {
                        Window.alert("You clicked " + contact);
                    }
                })) {
            @Override
            public String getValue(String object) {
                return object;
            }
        };
        cellTable.addColumn(updateColumn, "Update");
        cellTable.setColumnWidth(updateColumn, 5, Unit.PCT);
    }
}
