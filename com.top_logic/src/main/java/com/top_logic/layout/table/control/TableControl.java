/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bouncycastle.util.Strings;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.LocalScope;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachListener;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlCommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DefaultPopupHandler;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.FocusHandling;
import com.top_logic.layout.basic.Focusable;
import com.top_logic.layout.basic.Focusable.FocusRequestedListener;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.dnd.DnD;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DragSourceSPI;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractIntegerConstraint;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.popupmenu.MenuButtonRenderer;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.DefaultTableData.NoTableDataOwner;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataListener;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableModelUtils;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableUpdateAccumulator.UpdateRequest;
import com.top_logic.layout.table.control.access.CellRef;
import com.top_logic.layout.table.control.access.ColumsCollectionRef;
import com.top_logic.layout.table.control.access.RowDisplay;
import com.top_logic.layout.table.control.access.RowsCollectionRef;
import com.top_logic.layout.table.display.ColumnAnchor;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.dnd.TableDropEvent;
import com.top_logic.layout.table.dnd.TableDropEvent.Position;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.PageCountListener;
import com.top_logic.layout.table.model.PageListener;
import com.top_logic.layout.table.model.PageSizeListener;
import com.top_logic.layout.table.model.PageSizeOptionsListener;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.renderer.Icons;
import com.top_logic.layout.table.renderer.TableButtons;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link com.top_logic.layout.Control} that displays a {@link TableModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableControl extends AbstractControl implements TableModelListener,
		SelectionListener, TableDataListener, PageCountListener, PageListener, PageSizeOptionsListener,
		PageSizeListener, DragSourceSPI, ContextMenuOwner {

	/** Name of the technical select column */
	public static final String SELECT_COLUMN_NAME = "_select";

	private static final String DND_TABLE_POS_PARAM = "pos";

	private static final String DND_TABLE_REF_ID_PARAM = "id";

	public static final Map<String, ControlCommand> TABLE_COMMANDS = createCommandMap(
		new ControlCommand[] {
			FirstPageAction.INSTANCE,
			PreviousPageAction.INSTANCE,
			NextPageAction.INSTANCE,
			LastPageAction.INSTANCE,
			SelectAction.INSTANCE,
			SortAction.INSTANCE,
			ReplaceAction.INSTANCE,
			OpenFilterDialogAction.INSTANCE,
			OpenSortDialogAction.INSTANCE,
			UpdateRowsAction.INSTANCE,
			UpdateScrollPositionAction.INSTANCE,
			UpdateColumnWidthAction.INSTANCE,
			UpdateFixedColumnAmountAction.INSTANCE,
			DndTableDropAction.INSTANCE,
			TableInspector.INSTANCE,
			ContextMenuOpener.INSTANCE,
			DnDTableDragOverAction.INSTANCE
		});
	
	public static final String FIRST_PAGE_COMMAND    = "tableFirstPage";
	public static final String NEXT_PAGE_COMMAND     = "tableNextPage";
	public static final String PREVIOUS_PAGE_COMMAND = "tablePreviousPage";
	public static final String LAST_PAGE_COMMAND     = "tableLastPage";

	public static final String DND_DROP = "dndDrop";

	public static final String TABLE_SELECT_COMMAND  = "tableSelect";
	public static final String TABLE_SORT_COMMAND    = "tableSort";
	public static final String TABLE_REPLACE_COMMAND = "tableReplace";
	public static final String TABLE_FILTER_COMMAND  = "tableFilter";
	public static final String OPEN_TABLE_FILTER_DIALOG_COMMAND  = "openTableFilterDialog";

	/** Command to open a {@link PopupDialogControl} with {@link SortCommand}s */
	public static final String OPEN_TABLE_SORT_DIALOG_COMMAND = "openTableSortDialog";

	public static final String DISMISS_VIEWPORT_SLICE_COMMAND = "dismissSlice";
	public static final String REQUEST_VIEWPORT_SLICE_COMMAND  = "requestSlice";
	public static final String UPDATE_COLUMN_WIDTH_COMMAND  = "updateColumnWidth";

	public static final String UPDATE_SCROLL_POSITION_COMMAND = "updateScrollPosition";
	public static final String UPDATE_FIXED_COLUMN_AMOUNT_COMMAND  = "updateFixedColumnAmount";

	private final static String SORT_STATE = "sortState";

	private static final String PAGE_OPTION_SELECT_FIELD_NAME = "pso";
	private static final String PAGE_INPUT_FIELD_NAME = "cpi";
	
	/** Reference key for {@link #GLOBAL_TABLE_PROPERTIES} */
	private static final String GLOBAL_TABLE_PROPERTIES_KEY = "GlobalTableProperties";

	/** Reference to global table properties, defined in application xml */
	public static final Properties GLOBAL_TABLE_PROPERTIES =
		Configuration.getConfigurationByName(GLOBAL_TABLE_PROPERTIES_KEY).getProperties();

	@SuppressWarnings({ "unchecked" })
	private static final Property<Pair<TableControl, Focusable>> REQUESTED_FOCUS =
		TypedAnnotatable.propertyRaw(Pair.class, "focusRequested");

	private Map<Object, Control> innerControls;
	
	private Map<Integer, LocalScope> rowScopes;
	private boolean rowScopesInstalled;
	
	/**
	 * The current renderer of this {@link TableControl}. May not be
	 * <code>null</code>.
	 */
	private ITableRenderer renderer;
	
	/** Flag, if the selecting the table is enabled (default <code>true</code>). */
	/* package protected */boolean isSelectable;
	
	/**
	 * @deprecated Title bar commands are handled by {@link TableData#getToolBar()}.
	 */
	@Deprecated
	private List<HTMLFragment> lazyTitleBarButtons;

	private boolean visible = true;
    
	private TableData tableData;

	private TableUpdateAccumulator updateAccumulator;
	
	private int controlWriteCounter;

	private SelectionStrategy _selectionStrategy;

	/** Translation key for title of table */
	public static final String RES_TITLE = "title";

	/**
	 * Property to transfer the table control ID to {@link TableButtons}.
	 */
	public static final Property<String> CONTROL_ID_PROPERTY = TypedAnnotatable.property(String.class, "controlID");

	public TableControl(TableData tableData, ITableRenderer tableRenderer) {
		this(tableData, TABLE_COMMANDS, tableRenderer);
	}

	protected TableControl(TableData tableData, Map<String, ControlCommand> tableCommands,
			ITableRenderer tableRenderer) {
		super(tableCommands);
		
		this.renderer = tableRenderer != null ? tableRenderer : DefaultTableRenderer.newInstance();
		
		this.innerControls = new HashMap<>();
		this.rowScopes = new HashMap<>();
		this.rowScopesInstalled = false;
		
		initControlWriteCounter();
		this.tableData = tableData;
		this.updateAccumulator = new TableUpdateAccumulator();
		_selectionStrategy = DefaultSelectionStrategy.INSTANCE;

		setSelectable(true);
	}
	
	@Override
	protected void internalAttach() {
    	super.internalAttach();
    	
    	// Note: Inner controls must not be explicitly
    	// attached. Those controls attach themself if 
    	// they are written to the UI. The attach/detach
    	// of composite controls is inherently asymmetric.

		/* Must first attach to the TableData before attaching to internal things, as in
		 * hypothetical case, it will construct internal things after a listener is attached. */
		tableData.addListener(TableData.VIEW_MODEL_PROPERTY, this);
		TableViewModel viewModel = getViewModel();
		if (viewModel == null) {
			return;
		}
		/* Must not render an invalid TableViewModel as access to it may cause firing TableEvents.
		 * The control would react on that events and forces redraw (during rendering) */
		assert viewModel.isValid() : "Model to render is invalid.";

		tableData.addListener(TableData.SELECTION_MODEL_PROPERTY, this);
		addAsListener(viewModel);
	}

	/**
	 * The tables {@link ToolBar}.
	 */
	@TemplateVariable("toolbar")
	public ToolBar toolbar() {
		return getTableData().getToolBar();
	}

	/**
	 * Access to the table's columns.
	 */
	@TemplateVariable("cols")
	public Collection<CellRef> cols() {
		return new ColumsCollectionRef().init(this, null);
	}

	/**
	 * Access to the table's rows.
	 */
	@TemplateVariable("rows")
	public Collection<RowDisplay> rows() {
		return new RowsCollectionRef(this);
	}

	/**
	 * The table's title.
	 */
	@TemplateVariable("title")
	public String title() {
		return getTitle();
	}

	private void addAsListener(TableViewModel viewModel) {
		viewModel.addTableModelListener(this);
		SelectionModel selectionModel = getSelectionModel();
		selectionModel.addSelectionListener(this);
		PagingModel pagingModel = viewModel.getPagingModel();
		pagingModel.addListener(PagingModel.PAGE_COUNT_EVENT, this);
		pagingModel.addListener(PagingModel.PAGE_EVENT, this);
		pagingModel.addListener(PagingModel.PAGE_SIZE_EVENT, this);
		pagingModel.addListener(PagingModel.PAGE_SIZE_OPTIONS_EVENT, this);

		// Initialize.
		notifySelectionChanged(selectionModel, Collections.emptySet(), selectionModel.getSelection());
	}
	
	@Override
	protected void internalDetach() {
    	super.internalDetach();

        for (Iterator<Control> theIter = this.innerControls.values().iterator(); theIter.hasNext();) {
            Control theControl = theIter.next();
            if (theControl.isAttached()) {
                theControl.detach();
            }
        }
    	
		/* Must first detach from internal things of the TableData before detaching from the
		 * TableData, as in hypothetical case, TableData could destroy internal things after the
		 * last listener was detached. */
		TableViewModel viewModel = getViewModel();
		if (viewModel == null) {
			return;
		}

		removeAsListener(viewModel);
		tableData.removeListener(TableData.SELECTION_MODEL_PROPERTY, this);
		tableData.removeListener(TableData.VIEW_MODEL_PROPERTY, this);
	}

	private void removeAsListener(TableViewModel viewModel) {
		PagingModel pagingModel = viewModel.getPagingModel();
		pagingModel.removeListener(PagingModel.PAGE_SIZE_OPTIONS_EVENT, this);
		pagingModel.removeListener(PagingModel.PAGE_SIZE_EVENT, this);
		pagingModel.removeListener(PagingModel.PAGE_EVENT, this);
		pagingModel.removeListener(PagingModel.PAGE_COUNT_EVENT, this);

		getSelectionModel().removeSelectionListener(this);
		viewModel.removeTableModelListener(this);
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();

		// Attach update accumulator
		updateAccumulator.attach();
	}
	
	@Override
	protected void detachInvalidated() {
    	super.detachInvalidated();

    	// Clear all row scopes
		clearAllRowScopes();
    	
		// Attach update accumulator
		updateAccumulator.detach();

    	if (lazyTitleBarButtons != null) {
    		for (int i = 0, cnt = lazyTitleBarButtons.size(); i < cnt; i++) {
				HTMLFragment button = lazyTitleBarButtons.get(i);
				if (button instanceof Control) {
					((Control) button).detach();
    			}
    		}
    	}
    }
    
	@Override
	public final TableData getModel() {
		return tableData;
	}

	/**
	 * {@link ContextMenuProvider} that produces an optional context menu for table rows.
	 */
	public ContextMenuProvider getContextMenuProvider() {
		return tableData.getContextMenu();
	}

	/**
	 * Whether a concrete row provides a context menu.
	 */
	public boolean hasContextMenu(Object rowObject) {
		return getContextMenuProvider().hasContextMenu(rowObject);
	}

	@Override
	public Menu createContextMenu(String contextInfo) {
		int rowIndex = getRowIndex(contextInfo);
		Object directTarget = getTableData().getTableModel().getRowObject(rowIndex);
		Set<?> selection = getTableData().getSelectionModel().getSelection();
		Object extendedTarget = ContextMenuProvider.getContextMenuTarget(directTarget, selection);
		return getContextMenuProvider().getContextMenu(extendedTarget);
	}

	@Override
	public Object getDragSourceModel() {
		return getModel();
	}

	@Override
	public Object getDragData(String ref) {
		return getTableData().getDragSource().getDragObject(getTableData(), getRowIndex(ref));
	}

	@Override
	public Maybe<? extends ModelName> getDragDataName(Object dragSource, String ref) {
		return getTableData().getDragSource().getDragDataName(dragSource, getTableData(), getRowIndex(ref));
	}

	final int getRowIndex(String rowId) {
		return this.renderer.getRow(rowId);
	}

    public final TableModel getApplicationModel() {
		return getModel().getTableModel();
	}

	public final TableViewModel getViewModel() {
		return getModel().getViewModel();
	}

	/**
	 * The {@link SelectionModel} of the {@link #getTableData()}.
	 */
	public final SelectionModel getSelectionModel() {
		return getModel().getSelectionModel();
	}

	public ResourceView getResources() {
		return getApplicationModel().getTableConfiguration().getResPrefix();
	}

	public ITableRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Sets a new table renderer and requests a repaint.
	 * 
	 * @param newRenderer
	 *     The new renderer to use.
	 * @return
	 *     The old renderer.
	 */
	public ITableRenderer setRenderer(ITableRenderer newRenderer) {
		ITableRenderer oldRenderer = internalSetRenderer(newRenderer);
		requestRepaint();
		return oldRenderer;
	}

	protected final ITableRenderer internalSetRenderer(ITableRenderer newRenderer) {
		ITableRenderer oldRenderer = this.renderer;
		this.renderer = newRenderer;
		return oldRenderer;
	}

	/**
	 * @see #getSelectionStrategy()
	 */
	public void setSelectionStrategy(SelectionStrategy selectionStrategy) {
		_selectionStrategy = selectionStrategy;
	}

	/**
	 * {@link SelectionStrategy}, that is used by this {@link TableControl}, when
	 *         client-side selection occurred (e.g. row click).
	 */
	public SelectionStrategy getSelectionStrategy() {
		return _selectionStrategy;
	}

	/**
	 * Return the text for the title area.
	 *
	 * @return The text to be used, may be <code>null</code>.
	 */
	public String getTitle() {
		ResKey titleKey = getApplicationModel().getTableConfiguration().getTitleKey();
		if (titleKey != null) {
			return Resources.getInstance().getString(titleKey);
		}
		return getResources().getStringResource(TableControl.RES_TITLE, "");
	}

	/**
	 * @deprecated Use {@link TableData#getToolBar()}.
	 */
	@Deprecated
	public List<HTMLFragment> getTitleBarButtons() {
		if (this.lazyTitleBarButtons == null) {
			return Collections.emptyList();
		}
		
		return Collections.unmodifiableList(lazyTitleBarButtons);
	}

	/**
	 * @deprecated Add commands to the {@link TableData#getToolBar()}.
	 */
	@Deprecated
	public void addTitleBarControl(HTMLFragment control) {
		this.addTitleBarControl(getTitleBarControlCount(), control);
	}
	
	/**
	 * @deprecated Add commands to the {@link TableData#getToolBar()}.
	 */
	@Deprecated
    public void addTitleBarControl(int index, HTMLFragment control) {
    	// Make sure to detach controls before modifying the list.
    	this.requestRepaint();
    	
    	if (this.lazyTitleBarButtons == null) {
			this.lazyTitleBarButtons = new ArrayList<>();
    	}
    	
    	this.lazyTitleBarButtons.add(index, control);
    }
	
	/**
	 * Add Controls to be shown in the titeBar, last.
	 * 
	 * @deprecated Add commands to the {@link TableData#getToolBar()}.
	 */
	@Deprecated
	public void addTitleBarControls(List<? extends Control> controls) {
		this.addTitleBarControls(getTitleBarControlCount(), controls);
	}

	/**
	 * Add Controls to be shown in the titeBar.
	 * 
	 * @deprecated Add commands to the {@link TableData#getToolBar()}.
	 */
	@Deprecated
	public void addTitleBarControls(int index, List<? extends HTMLFragment> controls) {
		// Make sure to detach controls before modifying the list.
		this.requestRepaint();

		if (this.lazyTitleBarButtons == null) {
			this.lazyTitleBarButtons = new ArrayList<>(controls);
		} else {
			this.lazyTitleBarButtons.addAll(index, controls);
		}
	}

	private int getTitleBarControlCount() {
		return this.lazyTitleBarButtons == null ? 0 : lazyTitleBarButtons.size();
	}

	/**
	 * @deprecated Add commands to the {@link TableData#getToolBar()}.
	 */
	@Deprecated
	public void setTitleBarControls(List<? extends HTMLFragment> controls) {
		assert CollectionUtil.containsOnly(Control.class, controls) : "All elements must be controls.";
		
    	// Make sure to detach controls before modifying the list.
		this.requestRepaint();
		
		if (controls.isEmpty()) {
			this.lazyTitleBarButtons = null;
		} else {
			this.lazyTitleBarButtons = new ArrayList<>(controls);
		}
	}
	
	/**
	 * @deprecated Use {@link TableData#getToolBar()}.
	 */
	@Deprecated
	public void clearTitleBar() {
    	// Make sure to detach controls before modifying the list.
		this.requestRepaint();
		
		this.lazyTitleBarButtons = null;
	}
	
	private void createPageSizeControl() {
		int[] someOptions = getPagingModel().getPageSizeOptions();
		if (someOptions.length > 1) {
			FormContext context = new FormContext(getID() + "-pageSize", getResources());
			SelectField theField = createPageSizeSelectField(PAGE_OPTION_SELECT_FIELD_NAME, someOptions);
			context.addMember(theField);
	        
			DropDownControl theControl = getPageSizeControl();
	        if (theControl != null) {
	            this.removeInnerConrol(PAGE_OPTION_SELECT_FIELD_NAME);
	        }

			/* prevent resetting options */
			final DropDownControl select = new DropDownControl(theField, true);
			this.addInnerControl(PAGE_OPTION_SELECT_FIELD_NAME, select);
	    }
	}
	
	private SelectField createPageSizeSelectField(String aFieldID, int[] pageSizeOptions) {
		Integer currentPageSize = Integer.valueOf(getPagingModel().getPageSizeSpec());
		List<Integer> options = new ArrayList<>(pageSizeOptions.length);
		for (int n = 0, cnt = pageSizeOptions.length; n < cnt; n++) {
			options.add(Integer.valueOf(pageSizeOptions[n]));
	    }
		Collections.sort(options, PageSizeOptionComparator.INSTANCE);
	    
		SelectField selectField = FormFactory.newSelectField(aFieldID, options);
		selectField.setTransient(true);
		selectField.initializeField(Collections.singletonList(currentPageSize));
		selectField.setCustomOrder(true);
		selectField.setOptionLabelProvider(PageSizeOptionLabelProvider.INSTANCE);
		selectField.addValueListener(new ChangePageSize());

	    return selectField;
	}

	private void createPageInputControl() {
		FormContainer theContainer = new FormContext(getID() + "-pageInput", getResources());
		FormField theField = this.createPageInputField(PAGE_INPUT_FIELD_NAME);
        theContainer.addMember(theField);

        TextInputControl theControl = this.getPageInputControl();
        if (theControl != null) {
            this.removeInnerConrol(PAGE_INPUT_FIELD_NAME);
        }
        theControl = new TextInputControl(theField);
        theControl.setColumns(1);
        theControl.setStyle("background-color:#FF00EE");
        this.addInnerControl(PAGE_INPUT_FIELD_NAME, theControl);
	}

	private FormField createPageInputField(String aFieldID) {
		AbstractFormField theField =
			FormFactory.newIntField(aFieldID, Integer.valueOf(currentPage() + 1), false, false,
				new PageCountConstraint());
	    theField.setTransient(true);
	    theField.addValueListener(new ChangePage());
	    return theField;
	}
	
	public DropDownControl getPageSizeControl() {
		return (DropDownControl) this.getInnerControl(PAGE_OPTION_SELECT_FIELD_NAME);
    }

	public TextInputControl getPageInputControl() {
	    return (TextInputControl) this.getInnerControl(PAGE_INPUT_FIELD_NAME);
    }
	
    /** Get the number of the currently displayed page */
    public int getCurrentPage() {
		return currentPage();
    }

	private int currentPage() {
		return getPagingModel().getPage();
	}

	public PagingModel getPagingModel() {
		return getViewModel().getPagingModel();
	}

	/**
	 * Return the currently selected object.
	 * 
	 * If this table has no selected object, the method will return <code>null</code>.
	 * 
	 * @return The currently selected object or <code>null</code>.
	 */
    public int getSelectedRow() {
    	if (! this.isSelectable()) return -1;
    	
		return TableUtil.getSingleSelectedRow(getModel());
    }

    public boolean setSelectedRow(int newSelectedRow) {
		try {
			return internalSetSelectedRow(newSelectedRow, true, SelectionType.SINGLE);
		} catch (VetoException ex) {
			ex.process(getWindowScope());
			return false;
		}
    }
    
	/**
	 * true, if the row at given index is currently selected, false otherwise.
	 */
	public boolean isSelectedRow(int displayedRow) {
		if (!this.isSelectable()) {
			return false;
		}
		Object rowObject = getRowObject(displayedRow);
		return getSelectionModel().isSelected(rowObject);
	}

	public void setSelectedRowVeto(int newSelectedRow, boolean programaticUpdate, SelectionType selectionType)
			throws VetoException {
		internalSetSelectedRow(newSelectedRow, programaticUpdate, selectionType);
	}

	private boolean internalSetSelectedRow(int newSelectedRow, boolean programaticUpdate, SelectionType selectionType)
			throws VetoException {
		if (newSelectedRow < 0) {
			throw new IllegalArgumentException("Can not set negative selected row " + newSelectedRow + ".");
		}
		if (!isRowSelectable(newSelectedRow)) {
			return false;
		}

		if (!programaticUpdate) {
			Collection<SelectionVetoListener> vetoListeners = getTableData().getSelectionVetoListeners();
			if (!vetoListeners.isEmpty()) {
				TableViewModel viewModel = getViewModel();
				SelectionModel selectionModel = getSelectionModel();
				TableModel applicationModel = viewModel.getApplicationModel();
				int applicationRow = viewModel.getApplicationModelRow(newSelectedRow);
				Object rowObject = applicationModel.getRowObject(applicationRow);
				for (SelectionVetoListener vetoListener : vetoListeners) {
					vetoListener.checkVeto(selectionModel, rowObject, selectionType);
				}
			}
		}
		
		return acceptUISelection(newSelectedRow, selectionType);
	}

    protected boolean acceptUISelection(int newSelectedRow, SelectionType selectionType) {
		storeGlobalSelection(newSelectedRow, getLastClickedRow(), selectionType);
		return true;
	}

	private int getLastClickedRow() {
		int lastClickedRow;
		if (getSelectionModel() instanceof SingleSelectionModel) {
			lastClickedRow = -1;
		} else {
			Object lastSelectedRowObject = ((DefaultMultiSelectionModel) getSelectionModel()).getLastSelected();
			lastClickedRow = getViewModel().getRowOfObject(lastSelectedRowObject);
		}
		return lastClickedRow;
	}
    
	private void storeGlobalSelection(int newSelectedRow, int lastSelectedRow, SelectionType selectionType) {
		SelectionModel globalSelectionModel = getSelectionModel();
		switch (selectionType) {
			case SINGLE: {
				Object rowObject = getRowObject(newSelectedRow);
				Set<Object> newSelection = Collections.singleton(rowObject);
				recordAbsoluteSelection(newSelection);
				if (globalSelectionModel instanceof DefaultMultiSelectionModel) {
					DefaultMultiSelectionModel multiSelectionModel = (DefaultMultiSelectionModel) globalSelectionModel;
					multiSelectionModel.setSelection(newSelection, rowObject);
				} else {
					globalSelectionModel.setSelection(newSelection);
				}
				break;
			}
			case TOGGLE_SINGLE: {
				Object rowObject = getRowObject(newSelectedRow);
				boolean select = !globalSelectionModel.isSelected(rowObject);
				recordIncrementalSelection(rowObject, select);
				globalSelectionModel.setSelected(rowObject, select);
				break;
			}
			case AREA: {
				Object rowObject = getRowObject(newSelectedRow);
				Set<Object> selection = new HashSet<>(globalSelectionModel.getSelection());
				boolean doSelect = !selection.contains(rowObject);
				selectArea(newSelectedRow, lastSelectedRow, globalSelectionModel, selection, doSelect);
				break;
			}
			case TOGGLE_AREA: {
				selectArea(newSelectedRow, lastSelectedRow, globalSelectionModel, new HashSet<>(), true);
				break;
			}
		}
	}

	private void selectArea(int newSelectedRow, int lastSelectedRow, SelectionModel selectionModel,
			Set<Object> selection, boolean doSelect) {
		if (newSelectedRow > lastSelectedRow) {
			int lowerBound = Math.max(lastSelectedRow, 0);
			for (int i = lowerBound; i <= newSelectedRow; i++) {
				setSelected(selection, i, doSelect);
			}
		} else {
			for (int i = lastSelectedRow; i >= newSelectedRow; i--) {
				setSelected(selection, i, doSelect);
			}
		}
		recordAbsoluteSelection(selection);
		((DefaultMultiSelectionModel) selectionModel).setSelection(selection,
			getViewModel().getRowObject(newSelectedRow));
	}

	private void setSelected(Set<Object> selection, int row, boolean doSelect) {
		Object rowObject = getRowObject(row);
		if (doSelect) {
			selection.add(rowObject);
		} else {
			selection.remove(rowObject);
		}
	}

	private Object getRowObject(int newSelectedRow) {
		int applicationRowIndex = getViewModel().getApplicationModelRow(newSelectedRow);
		Object rowObject = getApplicationModel().getRowObject(applicationRowIndex);
		return rowObject;
	}

	private void recordIncrementalSelection(Object rowObject, boolean select) {
		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordSelection(getTableData(), rowObject, select, SelectionChangeKind.INCREMENTAL);
		}
	}

	private void recordAbsoluteSelection(Set<?> selection) {
		if (ScriptingRecorder.isRecordingActive()) {
			if (!selection.isEmpty()) {
				ScriptingRecorder.recordSelection(getTableData(), selection, true, SelectionChangeKind.ABSOLUTE);
			} else {
				ScriptingRecorder.recordSelection(getTableData(), ScriptingRecorder.NO_SELECTION, false,
					SelectionChangeKind.ABSOLUTE);
			}
		}
	}

	/**
	 * Return the state of the flag {@link #isSelectable}.
	 */
    public boolean isSelectable() {
        return this.isSelectable;
    }

    /** 
     * Return <code>true</code>, when at least one filter is defined on the table view model.
     * 
     * @return    <code>true</code>, when at least one filter is defined on the table view model.
     */
    public boolean hasFilters() {
		return this.getViewModel().hasFilters();
    }
	/**
	 * Decides whether the given row can be selected
	 * 
	 * @param row
	 *        the row under test
	 */
	public boolean isRowSelectable(int row) {
		Object rowObject = getViewModel().getRowObject(row);
		return isSelectable() && getSelectionModel().isSelectable(rowObject);
	}

    /**
     * Defines this table as a selectable one.
     *
     * @param isSelect
     *            <code>true</code> for a selectable table.
     */
    public void setSelectable(boolean isSelect) {
		TableViewModel model = getViewModel();
		Column selectColumn = model.getHeader().getColumn(SELECT_COLUMN_NAME);
		if (selectColumn != null) {
			if (selectColumn.getConfig().getVisibility() != DisplayMode.excluded) {
				selectColumn.setExcluded(!isSelect);
			}
		}

        this.isSelectable = isSelect;
    }

	/**
	 * Returns an array that contains the following information about the table:
	 *
	 * <ol>
	 * <li>the index of the first item </li>
	 * <li>the index of the last item </li>
	 * <li>the overall count of items</li>
	 * <li>the current page</li>
	 * <li>the last page</li>
	 * </ol>
	 * 
	 * @return an array of table data
	 */
	public Integer[] computeTableInfo() {
		PagingModel pagingModel = getPagingModel();

		int firstRowDisplay = pagingModel.getFirstRowOnCurrentPage() + 1;
		int lastRowDisplay = pagingModel.getLastRowOnCurrentPage() + 1;
		int rowCount = getViewModel().getRowCount();
		int currentPageDisplay = pagingModel.getPage() + 1;
		int lastPageDisplay = pagingModel.getPageCount();

		return new Integer[] {
			Integer.valueOf(firstRowDisplay),
			Integer.valueOf(lastRowDisplay),
			Integer.valueOf(rowCount),
			Integer.valueOf(currentPageDisplay),
			Integer.valueOf(lastPageDisplay) };
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean newVisible) {
		if (newVisible == this.visible) return;
		
		this.visible = newVisible;
		
		requestRepaint();
	}
	
	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		updateControlWriteCounter();
		rowScopesInstalled = false;
		ensureClearUpdateQueue();

		boolean hasModel = getViewModel() != null;
		if (hasModel) {
			// Create paging controls
			createPageInputControl();
			createPageSizeControl();
		}

		this.tableData.set(CONTROL_ID_PROPERTY, getID());

		getRenderer().write(context, out, this);
		
		Pair<TableControl, Focusable> requestedFocus = context.get(REQUESTED_FOCUS);
		if (requestedFocus != null
			// Check that the focused Control has not been changed in the meanwhile.
			&& requestedFocus.getFirst() == this
			// Check that the focused element has not been changed in the meanwhile.
			&& FocusHandling.shouldFocus(context, requestedFocus.getSecond())) {
			FocusHandling.writeFocusRequest(out, getID());
		}

		if (!hasModel) {
			return;
		} else {
			getViewModel().getClientDisplayData().getVisiblePaneRequest().clearTransientRanges();
		}
		assertRowScopesInstalled();
	}

	private void assertRowScopesInstalled() {
		assert (!this.isVisible()
			|| getViewModel().getRowCount() == 0
			|| getViewModel().getColumnCount() == 0
			|| rowScopesInstalled) : "No row control scopes were installed. Make sure, that the table " +
				"renderer uses the method addRow(...) of the table control to render rows.";
	}

	private void updateControlWriteCounter() {
		if (controlWriteCounter < Integer.MAX_VALUE) {
			controlWriteCounter++;
		} else {
			initControlWriteCounter();
		}
	}

	private void initControlWriteCounter() {
		// 0 is minimum counter, due to -1 means undefined display version at client side.		// 0 is minimum counter, due to -1 means undefined display version at client side.
		controlWriteCounter = 0;
	}

	private void ensureClearUpdateQueue() {

		assert logIfAccumulatorHasUpdates();
		updateAccumulator.clearUpdateQueue();
	}

	private boolean logIfAccumulatorHasUpdates() {
		boolean isNotEmptyQueue = updateAccumulator.hasUpdates();
		if (isNotEmptyQueue) {
			Logger.warn("Incremental updates found in rendering phase! Most likely reason is " +
						"event propagation within this phase, which is not allowed. Ensure event " +
						"propagation is done during command dispatching phase only! The illegal " +
						"updates will be dropped. The internal state has been corrected.", TableControl.class);
		}
		return true;
	}
	
	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		getRenderer().appendControlCSSClasses(out, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {

		super.internalRevalidate(context, actions);

		// Retrieve update requests from update accumulator
		List<UpdateRequest> updateRequests = updateAccumulator.getUpdates();
		produceUpdates(updateRequests, actions);
		updateAccumulator.clearUpdateQueue();
		
		// Get updates from row controls
		for (LocalScope scope : rowScopes.values()) {
			scope.revalidate(context, actions);
		}
	}

	private void produceUpdates(List<UpdateRequest> updateRequests, UpdateQueue actions) {
		if (!updateRequests.isEmpty()) {
			rowScopesInstalled = false;

			for (UpdateRequest updateRequest : updateRequests) {
				int firstRow = updateRequest.getFirstAffectedRow();
				int lastRow = updateRequest.getLastAffectedRow();

				// Clear row scopes for updated rows
				clearRowScopes(firstRow, lastRow);
			}

			renderer.updateRows(this, updateRequests, actions);

			assertRowScopesInstalled();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasUpdates() {
		boolean hasUpdates = updateAccumulator.hasUpdates() || super.hasUpdates();
		
		// Check, if controls of the row scopes have updates
		if (!hasUpdates) {
			for (LocalScope scope : rowScopes.values()) {
				hasUpdates = scope.hasUpdates();
				if (hasUpdates) {
					break;
				}
			}
		}
		
		return hasUpdates;
	}
	
	/**
	 * This method installs a control scope for a table row
	 * 
	 * @param renderer - the renderer, which will be called in context of the new table row scope
	 * @param context - the display context
	 * @param out - the tag writer
	 * @param row - the row, for which the control scope will be installed
	 */
	public void addRowScope(final Renderer<Object> renderer, DisplayContext context, TagWriter out, int row)
			throws IOException {
		LocalScope rowScope = rowScopes.get(row);
		if(rowScope == null) {
			rowScope = newLocalScope();
			rowScopes.put(row, rowScope);
		}
		rowScopesInstalled = true;
		context.renderScoped(rowScope, renderer, out, null);
	}
	
	@Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		updateRows(formerlySelectedObjects);
		updateRows(selectedObjects);

		if (!model.getSelection().isEmpty()) {
			setVisibleRange(model);
		} else {
			setUndefinedRange();
		}
	}

	private void setVisibleRange(Object selectionModel) {
		TableViewModel viewModel = getViewModel();
		int row;
		if (selectionModel instanceof DefaultSingleSelectionModel) {
			row = viewModel.getRowOfObject(((DefaultSingleSelectionModel) selectionModel).getSingleSelection());
		} else {
			Object lastSelectedRow = ((DefaultMultiSelectionModel) selectionModel).getLastSelected();
			row = viewModel.getRowOfObject(lastSelectedRow);
		}
		if (row != TableViewModel.NO_ROW) {
			getVisiblePaneRequest().setPersistentRowRange(IndexRange.singleIndex(row));
			viewModel.getPagingModel().showRow(row);
		} else {
			setUndefinedRange();
		}
	}

	private void setUndefinedRange() {
		getVisiblePaneRequest().setPersistentRowRange(IndexRange.undefined());
	}

	private VisiblePaneRequest getVisiblePaneRequest() {
		return getViewModel().getClientDisplayData().getVisiblePaneRequest();
	}

	private void updateRows(Collection<?> rowObjects) {
		for (Object rowObject : rowObjects) {
			int row = getViewModel().getRowOfObject(rowObject);
			handleUpdate(row, row);
		}
	}

	/**
	 * Implemented to translate the table model event into a modification of the
	 * user agent's DOM tree, which is the client-side view of this table.
	 * 
	 * @see TableModelListener#handleTableModelEvent(TableModelEvent)
	 */
	@Override
	public void handleTableModelEvent(TableModelEvent event) {
		switch (event.getType()) {
		case TableModelEvent.UPDATE: {
			handleUpdate(event.getFirstRow(), event.getLastRow());
			break;
		}
		
		case TableModelEvent.INVALIDATE: {
			requestRepaint();
			break;
		}
		
		case TableModelEvent.INSERT:
		case TableModelEvent.DELETE: {
			// If a different behaviour than a simple repaint shall be used, then
			// the handling of the table row scopes must be changed!
			requestRepaint();
			break;
			
		}
		
		default: {
			// Could potentially be optimized.
			requestRepaint();
			break;
		}
		}
	}

	@Override
	public void handlePageChanged(PagingModel sender, Integer oldValue, Integer newValue) {
		int newPage = newValue;

		TextInputControl theControl = this.getPageInputControl();
		if (theControl != null) {
			theControl.getFieldModel().setValue(newPage + 1);
		}
		getViewModel().getClientDisplayData().getViewportState().reset();
		requestRepaint();
	}

	@Override
	public void handlePageCountChanged(PagingModel sender, Integer oldValue, Integer newValue) {
		requestRepaint();
	}

	@Override
	public void handlePageSizeChanged(PagingModel sender, Integer oldValue, Integer newValue) {
		getViewModel().getClientDisplayData().getViewportState().reset();
		requestRepaint();
	}

	@Override
	public void handlePageSizeOptionsChanged(PagingModel sender, int[] oldValue, int[] newValue) {
		requestRepaint();
	}

	void handleUpdate(int eventFirstRow, int eventLastRow) {
		if (isRepaintRequested()) return;
		if (! this.visible) return;

		// Note: Rows in events must not necessarily match the number of
		// currently displayed rows. Especially, row numbers in selection events
		// must not necessarily be in synch with the actual number of rows.
		int rowCount = getViewModel().getRowCount();
		if (rowCount == 0) {
			return;
		}
		final int firstRow = Math.max(getViewModel().getFirstDisplayedRow(), eventFirstRow);
		final int lastRow = Math.min(getViewModel().getLastDisplayedRow(), eventLastRow);
		
		if (firstRow > lastRow) {
			// The event did not touch any visible row.
			return;
		}

		// Only update the rows that are currently displayed and touched by
		// the event.
		updateAccumulator.addUpdate(firstRow, lastRow);
	}

	/**
	 * This method returns the client side ID of the element which is responsible to open the filter
	 * or sort dialog.
	 * 
	 * @param columnName
	 *        whether unique column name or {@link TableViewModel#GLOBAL_TABLE_FILTER_ID}
	 */
	public String getColumnActivateElementID(String columnName) {
		return getID() + "_sortFilterButton_" + columnName;
	}
	
	@Override
	public TableControl self() {
		return this;
	}

	/**
	 * {@link ControlCommandModel} for this {@link TableControl} and given {@link ControlCommand}.
	 */
	protected ControlCommandModel newTableCommandModel(ControlCommand command) {
		ControlCommandModel result = new ControlCommandModel(command, this);
		TableData table = getTableData();
		if (table.getOwner() != NoTableDataOwner.INSTANCE) {
			result.set(LabeledButtonNaming.BUSINESS_OBJECT, table);
		}
		return result;
	}

	public static abstract class TableCommand extends ControlCommand {

		private static final String DISPLAY_VERSION_PARAM = "displayVersion";

	    public TableCommand(String aCommand) {
            super(aCommand);
        }

		@Override
		public final HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			TableControl table = (TableControl) control;
			int receivedDisplayVersion = getDisplayVersionFromRequestOrCurrent(table, arguments);
			if (table.getDisplayVersion() == receivedDisplayVersion) {
				return execute(context, table, arguments);
			} else {
				return getNOOPResponse(receivedDisplayVersion, table);
			}
		}

		private int getDisplayVersionFromRequestOrCurrent(TableControl table, Map<String, Object> arguments) {
			Number receivedVersion = (Number) arguments.get(DISPLAY_VERSION_PARAM);
			if(receivedVersion != null) {
				return receivedVersion.intValue();
			} else {
				return table.getDisplayVersion();
			}
		}

		protected abstract HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments);

		private HandlerResult getNOOPResponse(int receivedDisplayVersion, TableControl table) {
			if (Logger.isDebugEnabled(TableControl.class)) {
				Logger.debug("Request from outdated client-side control display version dropped." +
					"Request was targeted to display version '" + receivedDisplayVersion +
					"', but current display version is '" + table.getDisplayVersion() + "'.", TableViewModel.class);
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	public static abstract class CheckedTableCommand extends TableCommand {

		public CheckedTableCommand(String aCommand) {
			super(aCommand);
		}

		@Override
		protected final HandlerResult execute(DisplayContext context, final TableControl table,
				final Map<String, Object> arguments) {

			CheckScope checkScope = table.getTableData().getCheckScope();
			Collection<? extends ChangeHandler> affectedFormHandlers = checkScope.getAffectedFormHandlers();
			DirtyHandling dirtyHandling = DirtyHandling.getInstance();
			if (dirtyHandling.checkDirty(affectedFormHandlers)) {
				Command continuation = new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext continuationContext) {
						return executeChecked(continuationContext, table, arguments);
					}
				};
				dirtyHandling.openConfirmDialog(continuation, affectedFormHandlers, context.getWindowScope());
				return HandlerResult.DEFAULT_RESULT;
			} else {
				return executeChecked(context, table, arguments);
			}
		}

		protected abstract HandlerResult executeChecked(DisplayContext context, TableControl table,
				Map<String, Object> arguments);
	}

	public static class SortAction extends CheckedTableCommand {

		private static final String COLUMN_PARAM = "column";
		private static final String ASCENDING_PARAM = "ascending";

		public static final TableControl.SortAction INSTANCE = new TableControl.SortAction();

		protected SortAction() {
			super(TABLE_SORT_COMMAND);
		}

		@Override
		protected HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			int newSortedColumn =
				TableUtil.getServerColumnIndex(table.getViewModel(), ((Number) arguments.get(COLUMN_PARAM)).intValue());

			TableData tableData = table.getTableData();
			TableViewModel viewModel = tableData.getViewModel();
			boolean columnIsSortable = viewModel.isSortable(newSortedColumn);
			/**
			 * Don't use any methods that will call attach() or init().
			 */
			if(columnIsSortable){
				boolean ascending;
				boolean isSortedColumn = viewModel.isSorted(newSortedColumn);
				if (isSortedColumn) {
					ascending = !viewModel.getAscending(newSortedColumn);
				} else {
					ascending = true;
				}
				if (newSortedColumn < 0) {
					return HandlerResult.DEFAULT_RESULT;
				}
				String columnName = viewModel.getColumnName(newSortedColumn);
				SortConfig sortConfig = SortConfigFactory.sortConfig(columnName, ascending);
				List<SortConfig> sortConfigs = Collections.singletonList(sortConfig);
			
				sortTable(tableData, sortConfigs);

				viewModel.getClientDisplayData().getVisiblePaneRequest()
					.setColumnRange(IndexRange.singleIndex(newSortedColumn));
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public void appendInvokeExpression(Appendable out, JSObject argumentObject) throws IOException {
			// Make sure, the invoker passed the target row as argument.
			assert argumentObject.hasProperty(COLUMN_PARAM);

			super.appendInvokeExpression(out, argumentObject);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SORT_TABLE_ROWS;
		}
	}
	
	/**
	 * Command to open a dialog containing sort options for the table column.
	 * 
	 * <p>
	 * If the column is also filterable use {@link OpenFilterDialogAction} to create a dialog
	 * containing filter and sort options.
	 * </p>
	 *
	 * @author <a href=mailto:dpa@top-logic.com>dpa</a>
	 */
	public static class OpenSortDialogAction extends CheckedTableCommand {

		private final String SORTED_COLUMN = "column";

		private final String SORT_BUTTON = "sortFilterButtonID";

		/** Creates a {@link OpenSortDialogAction} */
		public static final TableControl.OpenSortDialogAction INSTANCE =
			new TableControl.OpenSortDialogAction();

		/**
		 * Creates {@link Command} to open a {@link PopupDialogControl} with {@link SortCommand}s.
		 */
		protected OpenSortDialogAction() {
			super(OPEN_TABLE_SORT_DIALOG_COMMAND);
		}

		@Override
		protected HandlerResult executeChecked(DisplayContext context, TableControl table,
				Map<String, Object> arguments) {
			String columnName = ((String) arguments.get(SORTED_COLUMN));
			final String placementID = (String) arguments.get(SORT_BUTTON);


			TableData tableData = table.getTableData();
			final FrameScope targetDocument = table.getFrameScope();
			return openTableSort(new DefaultPopupHandler(targetDocument, placementID), tableData, columnName);
		}

		/**
		 * Opens a {@link PopupDialogControl} to sort the table by the given column.
		 */
		public static HandlerResult openTableSort(PopupHandler handler, final TableData tableData, String columnName) {

			PopupDialogModel dialogModel = new DefaultPopupDialogModel(new ResourceText(
				I18NConstants.SORT_DIALOG_TITLE),
				new DefaultLayoutData(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH),
					DisplayUnit.PIXEL, 100, 0, DisplayUnit.PIXEL, 100, Scrolling.AUTO));
			BlockControl sortControl = createSortControl(tableData, columnName, dialogModel);
			PopupDialogControl popup = handler.createPopup(dialogModel);
			popup.setContent(sortControl);
			handler.openPopup(popup);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_SORT_POPUP_DIALOG;
		}

	}

	/**
	 * Command to open a dialog containing filter (and sort) options for the table column.
	 * 
	 * <p>
	 * Opens a filter dialog with the {@link PopupFilterDialogBuilder}. If the column is sortable a
	 * {@link BlockControl} with sort options will be added to the dialog. If the column is sortable
	 * but not filterable use {@link OpenSortDialogAction}.
	 * </p>
	 */
	public static class OpenFilterDialogAction extends CheckedTableCommand {

		/**
		 * The id for the {@link FormContext} of the currently open filter dialog.
		 * <p>
		 * The value is the empty string to be compatible with old scripted tests and to avoid
		 * clashes with the ids that are used in the filter sidebar.
		 * </p>
		 * <p>
		 * This id is used to annotate the {@link FilterFormOwner} to the {@link TableData}.
		 * </p>
		 */
		private static final String FILTER_DIALOG_FORM_CONTEXT_ID = "";

		private final String FILTERED_COLUMN = "column";
		private final String FILTER_BUTTON = "sortFilterButtonID";

		public static final TableControl.OpenFilterDialogAction INSTANCE =
			new TableControl.OpenFilterDialogAction();

		protected OpenFilterDialogAction() {
			super(OPEN_TABLE_FILTER_DIALOG_COMMAND);
		}
		
		@Override
		protected HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			String columnName     = ((String) arguments.get(FILTERED_COLUMN));
			final String placementID = (String) arguments.get(FILTER_BUTTON);

			TableData tableData = table.getTableData();
			final FrameScope targetDocument = table.getFrameScope();

			return openTableFilter(context, targetDocument, tableData, columnName, placementID);
		}

		public static HandlerResult openTableFilter(DisplayContext context, final FrameScope targetDocument,
				TableData tableData, String columnName, final String placementID) {
			return openTableFilter(context, new DefaultPopupHandler(targetDocument, placementID), tableData, columnName);
		}

		/**
		 * Opens a {@link PopupDialogControl} to filter (and sort) the table by the given column.
		 */
		public static HandlerResult openTableFilter(DisplayContext context, PopupHandler handler,
				final TableData tableData, String filterPosition) {
			TableViewModel tableModel = tableData.getViewModel();
			tableModel.addToOpenFilters(Collections.singletonList(filterPosition));
			
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordTableFilter(tableData, filterPosition);
			}
			TableFilter filter = tableModel.getFilter(filterPosition);
			if(filter != null ) {
				final PopupDialogModel dialogModel = filter.getDialogManager().getDialogModel();
				BlockControl sortControl = createSortControl(tableData, filterPosition, dialogModel);
				if (sortControl != null) {
					dialogModel.setDialogTitle(new ResourceText(
						I18NConstants.FILTER_SORT_DIALOG_TITLE));
				}
				FormContext filterForm = filter.openFilterDialog(context, handler, Optional.ofNullable(sortControl));
				addGeneralDialogCloseListener(filterPosition, tableModel, dialogModel);
				if (ScriptingRecorder.isEnabled()) {
					final FilterFormOwner filterFormOwner =
						new FilterFormOwner(tableData, filterForm, FILTER_DIALOG_FORM_CONTEXT_ID);
					filterForm.setOwningModel(filterFormOwner);
					filterFormOwner.registerToTableData();

					dialogModel.addListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, new DialogClosedListener() {

						@Override
						public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
							filterFormOwner.deregisterFromTableData();
							dialogModel.removeListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
						}
					});
				}
			}
			else {
				Logger.error("Cannot open filter dialog, due to no table filter is registered in model " +
							 "at column '" + filterPosition + "'!", TableControl.class);
			}
            return HandlerResult.DEFAULT_RESULT;
		}

		private static void addGeneralDialogCloseListener(final String filterPosition, final TableViewModel tableModel,
				final PopupDialogModel dialogModel) {
			dialogModel.addListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, new DialogClosedListener() {

				@Override
				public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
					tableModel.removeFromOpenFilters(Collections.singletonList(filterPosition));
					dialogModel.removeListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
				}
			});
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_FILTER_POPUP_DIALOG;
		}
	}

	/**
	 * Sorts a {@link TableControl} by the column with a given index.
	 * 
	 * @param table
	 *        The table to sort.
	 * @param sortOrder
	 *        The {@link SortConfig} defining the sort order.
	 */
	public static HandlerResult sortTable(TableData table, List<SortConfig> sortOrder) {
		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordSortTableColumn(table, sortOrder);
		}
		TableViewModel viewModel = table.getViewModel();
		// Actually perform the sort.
		viewModel.setSortOrder(sortOrder);
		TableModelUtils.scrollToSelectedRow(table);
		viewModel.saveSortOrder();

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Create {@link BlockControl} with {@link SortCommand}s.
	 * 
	 * @param tableData
	 *        Sortable {@link TableData}
	 * @param columnName
	 *        Name of the Column of the filter dialog.
	 * @param dialogModel
	 *        The {@link PopupDialogModel} of the {@link PopupDialogModel} containing the
	 *        {@link SortCommand}.
	 * 
	 */
	static BlockControl createSortControl(TableData tableData,
			String columnName,
			PopupDialogModel dialogModel) {

		TableViewModel viewModel = tableData.getViewModel();
		int columnIndex = viewModel.getColumnIndex(columnName);
		if (!viewModel.isSortable(columnIndex)) {
			return null;
		}

		BlockControl sortContainer = new BlockControl();
		sortContainer.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
		BlockControl groupControl = new BlockControl();
		Map<String, String> attributes = new HashMap<>();
		attributes.put(HTMLConstants.CLASS_ATTR, "popupMenu");
		groupControl.setRenderer(new DefaultSimpleCompositeControlRenderer(HTMLConstants.TABLE, attributes));

		createSortCommands(tableData, columnName, groupControl, dialogModel);

		sortContainer.addChild(groupControl);
		return sortContainer;
	}

	/**
	 * Create {@link SortCommand}s for ascending and descending table sorts.
	 * 
	 * @param tableData
	 *        Affected {@link TableData}.
	 * @param columnName
	 *        Name of the column based on which to sort.
	 * @param groupControl
	 *        Control containing the command buttons.
	 * @param dialogModel
	 *        The {@link PopupDialogModel} of the {@link PopupDialogModel} containing the
	 *        {@link SortCommand}.
	 */
	private static void createSortCommands(TableData tableData, String columnName, BlockControl groupControl,
			PopupDialogModel dialogModel) {
		TableViewModel viewModel = tableData.getViewModel();
		Command sortAscCommand = new SortCommand(tableData, columnName, true, dialogModel);
		Command sortDescCommand = new SortCommand(tableData, columnName, false, dialogModel);
		CommandModel descCommandModel = CommandModelFactory.commandModel(sortDescCommand);
		descCommandModel.setLabel(Resources.getInstance().getString(I18NConstants.SORT_DESC_BUTTON));
		CommandModel ascCommandModel = CommandModelFactory.commandModel(sortAscCommand);
		ascCommandModel.setLabel(Resources.getInstance().getString(I18NConstants.SORT_ASC_BUTTON));
		ScriptingRecorder.annotateAsDontRecord(ascCommandModel);
		ScriptingRecorder.annotateAsDontRecord(descCommandModel);

		int column = viewModel.getColumnIndex(columnName);
		boolean isSorted = viewModel.isSorted(column);
		if (isSorted) {
			if (viewModel.getAscending(column)) {
				ascCommandModel.setCssClasses(SORT_STATE);
			} else {
				descCommandModel.setCssClasses(SORT_STATE);
			}
		}

		ButtonControl descButton = new ButtonControl(descCommandModel, MenuButtonRenderer.INSTANCE);
		ButtonControl ascButton = new ButtonControl(ascCommandModel, MenuButtonRenderer.INSTANCE);
		ThemeImage sortAsc = Icons.SORT_ASC;
		ascButton.setImage(sortAsc);
		descButton.setImage(Icons.SORT_DESC);

		groupControl.addChild(ascButton);
		groupControl.addChild(descButton);
	}

	/**
	 * Command to sort a table.
	 *
	 * @author    <a href=mailto:dpa@top-logic.com>dpa</a>
	 */
	public static class SortCommand implements Command {

		private TableData _tableData;

		private String _columnName;

		private boolean _ascending;

		private PopupDialogModel _dialogModel;

		/**
		 * Creates a new {@link SortCommand} with the given properties.
		 * 
		 * @param tableData
		 *        Affected {@link TableData}.
		 * @param columnName
		 * 		  Name of sortable column.
		 * @param ascending
		 *        If the data will be sorted ascending or descending.
		 * @param dialogModel
		 *        The {@link PopupDialogModel} of the {@link PopupDialogModel} containing the
		 *        {@link SortCommand}.
		 */
		public SortCommand(TableData tableData, String columnName, boolean ascending, PopupDialogModel dialogModel) {
			_tableData = tableData;
			_columnName = columnName;
			_ascending = ascending;
			_dialogModel = dialogModel;

		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			TableViewModel viewModel = _tableData.getViewModel();
			int columnIndex = viewModel.getColumnIndex(_columnName);
			if (!viewModel.isSortable(columnIndex)) {
				return null;
			}

			SortConfig sortConfig = SortConfigFactory.sortConfig(_columnName, _ascending);
			List<SortConfig> sortConfigs = Collections.singletonList(sortConfig);
			sortTable(_tableData, sortConfigs);

			_dialogModel.setClosed();
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	public static class Slice {

		private final int _firstRow;

		private final int _lastRow;

		/**
		 * Creates a {@link Slice}.
		 * 
		 * @param firstRow
		 *        See {@link #getFirstRow()}.
		 * @param lastRow
		 *        See {@link #getLastRow()}.
		 */
		public Slice(int firstRow, int lastRow) {
			_firstRow = firstRow;
			_lastRow = lastRow;
		}

		/**
		 * The index of the first row of this {@link Slice}.
		 */
		public int getFirstRow() {
			return _firstRow;
		}

		/**
		 * The index of the last row of this {@link Slice} (inclusive).
		 */
		public int getLastRow() {
			return _lastRow;
		}

		@Override
		public String toString() {
			return "range: (" + _firstRow + ", " + _lastRow + ")]";
		}
	}

	public static class UpdateColumnWidthAction extends TableCommand {

		private static final String COLUMN_PARAM = "columnID";
		private static final String COLUMN_WIDTH_PARAM = "newColumnWidth";

		public static final TableControl.UpdateColumnWidthAction INSTANCE =
			new TableControl.UpdateColumnWidthAction();
		
		public UpdateColumnWidthAction() {
			super(UPDATE_COLUMN_WIDTH_COMMAND);
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		protected HandlerResult execute(DisplayContext context,
				TableControl table, Map<String, Object> arguments) {
			
			int viewColumn =
				TableUtil.getServerColumnIndex(table.getViewModel(), ((Number) arguments.get(COLUMN_PARAM)).intValue());
			int columnWidth = ((Number) arguments.get(COLUMN_WIDTH_PARAM)).intValue();
			
			// Update column widths
			table.getViewModel().saveColumnWidth(viewColumn, columnWidth);
			
			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Consistent column widths of th-tag and enclosed div-tag will be checked and eventually
		 * adjusted at initial client-side rendering of the table. Because the table can be in
		 * background (e.g. dialog is open) during this process, the command must executable, even
		 * if the associated view is disabled.
		 */
		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_COLUMN_WIDTH;
		}
	}
	
	/**
	 * Updates the table body by adding and removing rows.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public static class UpdateRowsAction extends TableCommand {

		/**
		 * Command name to update table rows.
		 */
		public static final String COMMAND_NAME = "updateRows";

		/**
		 * Singleton {@link TableControl.UpdateRowsAction} instance.
		 */
		public static final TableControl.UpdateRowsAction INSTANCE = new TableControl.UpdateRowsAction();

		private UpdateRowsAction() {
			super(COMMAND_NAME);
		}

		/**
		 * After a minimum amount of table rows has been given from the server to the client for the
		 * initial rendering, the client determines if and how many more rows should be rendered.
		 * The client ensures that at least enough rows are rendered to cover the user's viewport.
		 * Because the table can be in background (e.g. dialog is open) during this process, the
		 * command must executable, even if the associated view is disabled.
		 */
		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			Object rangesOfRowsToRemove = arguments.get("removed");
			if (rangesOfRowsToRemove != null) {
				removeTableRows(table, rangesOfRowsToRemove);
			}

			Object rangesOfRowsToAdd = arguments.get("added");
			if (rangesOfRowsToAdd != null) {
				addTableRows(table, rangesOfRowsToAdd);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private void removeTableRows(TableControl table, Object rangesOfRowsToRemove) {
			executeForEachRange(rangesOfRowsToRemove, (firstRow, lastRow) -> {
				table.getViewModel().dropSlice(new Slice(firstRow, lastRow));
				table.clearRowScopes(firstRow, lastRow);
			});
		}

		private void addTableRows(TableControl table, Object rangesOfRowsToAdd) {
			executeForEachRange(rangesOfRowsToAdd, (firstRow, lastRow) -> {
				table.getViewModel().pushSliceRequest(new Slice(firstRow, lastRow));
				table.handleUpdate(firstRow, lastRow);
			});
		}

		/**
		 * Executes the given consumer on each range of the given ranges of rows.
		 * 
		 * @param ranges
		 *        Collection of row ranges separated by a comma. Each range is specified by the
		 *        first row of a range concatenated by a dash and ens with the last row of the
		 *        range. For example: 1-20,40-50 to execute the consumer on the rows 1 to 20 and 40
		 *        to 50.
		 * @param consumer
		 *        Function executes on each range of the given collection of table rows.
		 */
		private void executeForEachRange(Object ranges, BiConsumer<Integer, Integer> consumer) {
			String[] rangesOfRows = Strings.split(String.valueOf(ranges), ',');

			for (int i = 0; i < rangesOfRows.length; i++) {
				String rangeOfRows = rangesOfRows[i];

				int rangeSeparatorIndex = rangeOfRows.indexOf('-');

				int firstRow = Integer.valueOf(rangeOfRows.substring(1, rangeSeparatorIndex));
				int lastRow = Integer.valueOf(rangeOfRows.substring(rangeSeparatorIndex + 1, rangeOfRows.length() - 1));

				consumer.accept(firstRow, lastRow);
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_ROWS_ACTION;
		}

	}

	public static class UpdateScrollPositionAction extends TableCommand {

		private static final String COLUMN_ANCHOR = "columnAnchor";
		private static final String COLUMN_ANCHOR_OFFSET = "columnAnchorOffset";
		private static final String ROW_ANCHOR = "rowAnchor";
		private static final String ROW_ANCHOR_OFFSET = "rowAnchorOffset";

		public static final TableControl.UpdateScrollPositionAction INSTANCE =
			new TableControl.UpdateScrollPositionAction();

		public UpdateScrollPositionAction() {
			super(UPDATE_SCROLL_POSITION_COMMAND);
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected HandlerResult execute(DisplayContext context,
				TableControl table, Map<String, Object> arguments) {
			int columnAnchorIndex = TableUtil.getServerColumnIndex(table.getViewModel(),
				((Number) arguments.get(COLUMN_ANCHOR)).intValue());
			int columnAnchorOffset = ((Number) arguments.get(COLUMN_ANCHOR_OFFSET)).intValue();
			int rowAnchorIndex = ((Number) arguments.get(ROW_ANCHOR)).intValue();
			int rowAnchorOffset = ((Number) arguments.get(ROW_ANCHOR_OFFSET)).intValue();

			setHorizontalScrollPosition(table, columnAnchorIndex, columnAnchorOffset);
			setVerticalScrollPosition(table, rowAnchorIndex, rowAnchorOffset);

			return HandlerResult.DEFAULT_RESULT;
		}

		private void setHorizontalScrollPosition(TableControl table, int columnAnchorIndex, int columnAnchorOffset) {
			int columnAnchor = getColumnAnchorInAllowedRange(table, columnAnchorIndex);
			ColumnAnchor anchor;
			if (columnAnchor < 0) {
				anchor = ColumnAnchor.NONE;
			} else {
				String columnName = table.getViewModel().getColumnName(columnAnchor);
				anchor = ColumnAnchor.create(columnName, columnAnchorOffset);
			}
			getViewportState(table).setColumnAnchor(anchor);
		}

		private int getColumnAnchorInAllowedRange(TableControl table, int columnAnchorIndex) {
			return getAnchorInAllowedRange(columnAnchorIndex, table.getViewModel().getColumnCount());
		}

		private int getAnchorInAllowedRange(int rawAnchor, int upperBoundary) {
			return Math.min(rawAnchor, upperBoundary - 1);
		}

		private ViewportState getViewportState(TableControl table) {
			return table.getViewModel().getClientDisplayData().getViewportState();
		}

		private void setVerticalScrollPosition(TableControl table, int rowAnchorIndex, int rowAnchorOffset) {
			int rowAnchor = getRowAnchorInAllowedRange(table, rowAnchorIndex);
			RowIndexAnchor anchor;
			if (rowAnchor < 0) {
				anchor = RowIndexAnchor.NONE;
			} else {
				anchor = RowIndexAnchor.create(rowAnchor, rowAnchorOffset);
			}
			getViewportState(table).setRowAnchor(anchor);
		}

		private int getRowAnchorInAllowedRange(TableControl table, int rowAnchorIndex) {
			return getAnchorInAllowedRange(rowAnchorIndex, table.getViewModel().getRowCount());
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_SCROLL_POSITION;
		}
	}

	public static class UpdateFixedColumnAmountAction extends TableCommand {
		
		private static final String FIXED_COLUMN_AMOUNT_PARAM = "fixedColumnAmount";

		public static final TableControl.UpdateFixedColumnAmountAction INSTANCE =
			new TableControl.UpdateFixedColumnAmountAction();
		
		public UpdateFixedColumnAmountAction() {
			super(UPDATE_FIXED_COLUMN_AMOUNT_COMMAND);
		}
		
		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		protected HandlerResult execute(DisplayContext context,
				TableControl table, Map<String, Object> arguments) {
			
			int fixedColumns = ((Integer) arguments.get(FIXED_COLUMN_AMOUNT_PARAM)).intValue();
			
			updateFixedColumnAmount(table, fixedColumns);
			resetMostLeftFlexibleVisibleColumn(table);
			
			return HandlerResult.DEFAULT_RESULT;
		}

		private void updateFixedColumnAmount(TableControl table, int fixedColumns) {
			table.getViewModel().setPersonalFixedColumns(fixedColumns);
		}

		private void resetMostLeftFlexibleVisibleColumn(TableControl table) {
			table.getViewModel().getClientDisplayData().getViewportState().setColumnAnchor(ColumnAnchor.NONE);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_FIXED_COLUMN_AMOUNT;
		}
	}
	
	public static class ReplaceAction extends TableCommand {
		private static final String COLUMN_PARAM = "column";

		public static final TableControl.ReplaceAction INSTANCE = new TableControl.ReplaceAction();

		protected ReplaceAction() {
			super(TABLE_REPLACE_COMMAND);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			int firstMovedColumn = TableUtil.getServerColumnIndex(table.getViewModel(),
				((Integer) arguments.get("firstMovedColumn")).intValue());
			int lastMovedColumn = TableUtil.getServerColumnIndex(table.getViewModel(),
				((Integer) arguments.get("lastMovedColumn")).intValue());
			int insertIndex = TableUtil.getServerColumnIndex(table.getViewModel(),
				((Integer) arguments.get("insertIndex")).intValue());
			boolean fixFlexMovement = ((Boolean) arguments.get("fixFlexMovement")).booleanValue();

			TableViewModel viewModel = table.getViewModel();
			if (isFrozenTable(table)) {
				int fixedColumnCount = Math.max(0, viewModel.getFixedColumnCount());
				int movedColumnCount = lastMovedColumn - firstMovedColumn + 1;
				if (fixFlexMovement) {
					if (flexToFixMovement(firstMovedColumn, insertIndex, fixedColumnCount)) {
						viewModel.setPersonalFixedColumns(fixedColumnCount + movedColumnCount);
					} else if (fixToFlexMovement(lastMovedColumn, insertIndex, fixedColumnCount)) {
						viewModel.setPersonalFixedColumns(fixedColumnCount - movedColumnCount);
					}
				}
			}

			updateColumns(table, firstMovedColumn, lastMovedColumn, insertIndex);

            return HandlerResult.DEFAULT_RESULT;
		}

		void updateColumns(final TableControl table, final int firstMovedColumn,
				final int lastMovedColumn, final int insertIndex) {
			try {
				TableViewModel viewModel = table.getViewModel();
				viewModel.moveColumns(firstMovedColumn, lastMovedColumn, insertIndex);
				viewModel.saveColumnOrder();
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						updateColumns(table, firstMovedColumn, lastMovedColumn, insertIndex);
						return HandlerResult.DEFAULT_RESULT;
					}
				});

				ex.process(table.getWindowScope());

				/* The GUI is already up-to-date. When the user cancels the update veto, the GUI is
				 * inconsistent, therefore the table must be repaint. */
				table.requestRepaint();
			}
		}

		private boolean flexToFixMovement(int firstMovedColumn, int insertIndex, int fixedColumnCount) {
			return firstMovedColumn >= fixedColumnCount;
		}

		private boolean fixToFlexMovement(int lastMovedColumn, int insertIndex, int fixedColumnCount) {
			return lastMovedColumn < fixedColumnCount;
		}

		private boolean isFrozenTable(TableControl table) {
			return table.getViewModel().getFixedColumnCount() >= 0;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.COLUMN_MOVE;
		}
		
	}	
	
	public static class SelectAction extends TableCommand {
		public static final String ROW_PARAM = "row";

		public static final String COLUMN_PARAM = "column";

		public static final String SHIFT_KEY_PARAM = "shiftKey";

		public static final String CTRL_KEY_PARAM = "ctrlKey";

		public static final TableControl.SelectAction INSTANCE = new TableControl.SelectAction();

		protected SelectAction() {
			super(TABLE_SELECT_COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			final int newSelectedRow = ((Number) arguments.get(ROW_PARAM)).intValue();
			final int clickedColumn =
				TableUtil.getServerColumnIndex(table.getViewModel(), ((Number) arguments.get(COLUMN_PARAM)).intValue());
			final boolean shiftKey = !Utils.isFalse(((Boolean) arguments.get(SHIFT_KEY_PARAM)));
			final boolean ctrlKey = !Utils.isFalse(((Boolean) arguments.get(CTRL_KEY_PARAM)));

			SelectionType selectionType = table.getSelectionStrategy()
				.getSelectionType(table.getSelectionModel(), ctrlKey, shiftKey);

			selectRow(table, newSelectedRow, clickedColumn, selectionType);

            return HandlerResult.DEFAULT_RESULT;
		}

		private void selectRow(final TableControl table, final int newSelectedRow, final int clickedColumn,
				final SelectionType selectionType) {
			try {
				storeClickedCellIndices(table, newSelectedRow, clickedColumn);
				table.setSelectedRowVeto(newSelectedRow, false, selectionType);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						selectRow(table, newSelectedRow, clickedColumn, selectionType);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(table.getWindowScope());
			}
		}

		private void storeClickedCellIndices(final TableControl table, final int newSelectedRow,
				final int clickedColumn) {
			ViewportState viewportState = table.getViewModel().getClientDisplayData().getViewportState();
			viewportState.setLastClickedRow(RowIndexAnchor.create(newSelectedRow));
			if (clickedColumn == -1 ) {
				viewportState.setLastClickedColumn(ColumnAnchor.NONE);
			} else {
				viewportState
					.setLastClickedColumn(ColumnAnchor.create(table.getViewModel().getColumnName(clickedColumn)));
			}
		}

		/**
		 * Writes the Java-Script call to trigger this {@link SelectAction}.
		 */
		public final void writeInvokeExpression(Appendable out, Control control, int row, int column)
				throws IOException {
			out.append(FormConstants.FORM_PACKAGE);
			out.append(".TableControl.select(arguments[0], this, ");
			TagUtil.writeJsString(out, control.getID());
			out.append(", ");
			if (row < 0) {
				throw new IllegalArgumentException("Can not select negative row " + row + ".");
			}
			StringServices.append(out, row);
			out.append(", ");
			StringServices.append(out, column);
			out.append(")");
		}
		
		@Override
		public void appendInvokeExpression(Appendable out, JSObject argumentObject) throws IOException {
			// Make sure, the invoker passed the target row as argument.
			assert argumentObject.hasProperty(ROW_PARAM);

			super.appendInvokeExpression(out, argumentObject);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CELL_SELECT;
		}
	}

	public static class FirstPageAction extends CheckedTableCommand {
		public static final TableControl.TableCommand INSTANCE = new TableControl.FirstPageAction();

		public FirstPageAction() {
			super(TableControl.FIRST_PAGE_COMMAND);
		}
	
		@Override
		public HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			table.getPagingModel().showFirstPage();
            return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.JUMP_TO_FIRST_PAGE;
		}
	}

	public static class NextPageAction extends CheckedTableCommand {
		public static final TableControl.TableCommand INSTANCE = new TableControl.NextPageAction();

		public NextPageAction() {
			super(TableControl.NEXT_PAGE_COMMAND);
		}
	
		@Override
		public HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			table.getPagingModel().showNextPage();
            return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.JUMP_TO_NEXT_PAGE;
		}
	}

	public static class PreviousPageAction extends CheckedTableCommand {
		public static final TableControl.TableCommand INSTANCE =
			new TableControl.PreviousPageAction();

		public PreviousPageAction() {
			super(TableControl.PREVIOUS_PAGE_COMMAND);
		}
	
		@Override
		public HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			table.getPagingModel().showPreviousPage();
            return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.JUMP_TO_PREVIOUS_PAGE;
		}
	}

	public static class LastPageAction extends CheckedTableCommand {
		public static final TableControl.TableCommand INSTANCE = new TableControl.LastPageAction();

		public LastPageAction() {
			super(TableControl.LAST_PAGE_COMMAND);
		}
	
		@Override
		public HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			table.getPagingModel().showLastPage();
            return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.JUMP_TO_LAST_PAGE;
		}
	}
	
	/**
	 * {@link CheckedTableCommand} executed, when the element is dragged over a drop zone.
	 */
	public static class DnDTableDragOverAction extends CheckedTableCommand {

		private static final String COMMAND_NAME = "dragOver";

		/**
		 * Singleton {@link DnDTableDragOverAction} instance.
		 */
		public static final DnDTableDragOverAction INSTANCE = new DnDTableDragOverAction();

		private DnDTableDragOverAction() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult executeChecked(DisplayContext context, TableControl table,
				Map<String, Object> arguments) {
			DndData data = DnD.getDndData(context, arguments);

			if (data != null) {
				TableData tableData = table.getModel();

				String pos = (String) arguments.get(DND_TABLE_POS_PARAM);
				String refId = (String) arguments.get(DND_TABLE_REF_ID_PARAM);

				int rowNum = refId == null ? -1 : table.getRowIndex(refId);
				TableDropEvent dropEvent = new TableDropEvent(data, tableData, rowNum, Position.fromString(pos));

				List<TableDropTarget> dropTargets = table.getApplicationModel().getTableConfiguration().getDropTargets();

				for (TableDropTarget dropTarget : dropTargets) {
					if (dropTarget.canDrop(dropEvent)) {
						displayDropMarker(table, refId, pos);

						return HandlerResult.DEFAULT_RESULT;
					}
				}

				changeToNoDropCursor(table, refId);
			}


			return HandlerResult.DEFAULT_RESULT;
		}

		private void changeToNoDropCursor(TableControl control, String targetID) {
			control.getFrameScope().addClientAction(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.form.TableControl.changeToNoDropCursor(");
					TagUtil.writeJsString(out, targetID);
					out.append(");");
				}
			}));
		}

		private void displayDropMarker(TableControl control, String targetID, String position) {
			control.getFrameScope().addClientAction(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.form.TableControl.displayDropMarker(");
					TagUtil.writeJsString(out, targetID);
					if (!StringServices.isEmpty(position)) {
						out.append(",");
						TagUtil.writeJsString(out, position);
					}
					out.append(");");
				}
			}));
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DND_TABLE_DRAG_OVER_ACTION;
		}

	}

	public static class DndTableDropAction extends CheckedTableCommand {
		public static final TableControl.TableCommand INSTANCE = new TableControl.DndTableDropAction();

		public DndTableDropAction() {
			super(TableControl.DND_DROP);
		}

		@Override
		public HandlerResult executeChecked(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableData tableData = table.getModel();
			List<TableDropTarget> dropTargets = table.getApplicationModel().getTableConfiguration().getDropTargets();

			DndData data = DnD.getDndData(context, arguments);
			if (data != null) {
				String pos = (String) arguments.get(DND_TABLE_POS_PARAM);

				String refId = (String) arguments.get(DND_TABLE_REF_ID_PARAM);
				int rowNum = refId == null ? -1 : table.getRowIndex(refId);

				TableDropEvent dropEvent = new TableDropEvent(data, tableData, rowNum, Position.fromString(pos));

				for (TableDropTarget dropTarget : dropTargets) {
					if (dropTarget.canDrop(dropEvent)) {
						dropTarget.handleDrop(dropEvent);

						return HandlerResult.DEFAULT_RESULT;
					}
				}

				throw new TopLogicException(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DND_TABLE_DROP;
		}
	}

	/**
	 * Appends a JavaScript command to the given writer that selects the given row (by a click to
	 * the given column).
	 */
	public void appendSelectAction(Appendable out, int row, int column) throws IOException {
		out.append("return ");
		getSelectAction().writeInvokeExpression(out, this, row, TableUtil.getClientColumnIndex(getViewModel(), column));
		out.append(";");
	}

	/**
	 * Writes JavaScropt for opening the table filter for the column with the given name.
	 */
	public void appendOpenFilterAction(Appendable out, String columnName) throws IOException {
		out.append("return TABLE.openFilterDialog('");
		appendOpenSortFilterAction(out, columnName);
	}

	/**
	 * Writes JavaScript for opening the table sort dialog for the column with the given name.
	 */
	public void appendOpenSortAction(Appendable out, String columnName) throws IOException {
		out.append("return TABLE.openSortDialog('");
		appendOpenSortFilterAction(out, columnName);
	}

	/**
	 * Writes JavaScript for opening the table sort and/or filter dialog for the column with the
	 * given name.
	 */
	private void appendOpenSortFilterAction(Appendable out, String columnName) throws IOException {
		out.append(getColumnActivateElementID(columnName));
		out.append("', '");
		out.append(getID());
		out.append("', '");
		out.append(columnName);
		out.append("');");
	}

	/**
	 * The {@link ControlCommand} selecting a given row.
	 * 
	 * @see #appendSelectAction(Appendable, int, int)
	 */
	private SelectAction getSelectAction() {
		return (SelectAction) getCommand(TableControl.TABLE_SELECT_COMMAND);
	}

	public TableCommand getFirstPageCommand() {
		return (TableCommand) getCommand(FIRST_PAGE_COMMAND);
	}

	public TableCommand getPreviousPageCommand() {
		return (TableCommand) getCommand(PREVIOUS_PAGE_COMMAND);
	}

	public TableCommand getNextPageCommand() {
		return (TableCommand) getCommand(NEXT_PAGE_COMMAND);
	}

	public TableCommand getLastPageCommand() {
		return (TableCommand) getCommand(LAST_PAGE_COMMAND);
	}

	public SortAction getSortCommand() {
		return (SortAction) getCommand(TABLE_SORT_COMMAND);
	}
	
	public ReplaceAction getSwitchCommand() {
		return (ReplaceAction) getCommand(TABLE_REPLACE_COMMAND);
	}

	protected Control getInnerControl(Object aKey) {
        return this.innerControls.get(aKey);
    }
    
	protected void addInnerControl(Object aKey, Control aControl) {
        this.innerControls.put(aKey, aControl);
        
        // Note: Controls must not be explicitly attached. Controls
        // attach themself during writing to the UI. 
    }
    
	protected void removeInnerConrol(Object aKey) {
        Control theControl = this.getInnerControl(aKey);
        if (theControl != null) {
            if (theControl.isAttached()) {
                theControl.detach();
            }
            this.innerControls.remove(aKey);
        }
    }

    private class PageCountConstraint extends AbstractIntegerConstraint {
    	
    	PageCountConstraint() {
    		// nothing to do here
		}
        
		@Override
		protected boolean checkInteger(Integer value) throws CheckException {
			if (value == null) {
				throw new CheckException(Resources.getInstance().getString(
					com.top_logic.layout.form.I18NConstants.INVALID_INT_VALUE));
			}
			final int intValue = value.intValue();
			if (intValue > getPagingModel().getPageCount()) {
				throw new CheckException(Resources.getInstance().getString(
					com.top_logic.layout.form.I18NConstants.INVALID_INT_VALUE));
			}
			if (intValue <= 0) {
				throw new CheckException(Resources.getInstance().getString(
					com.top_logic.layout.form.I18NConstants.INVALID_INT_VALUE));
			}
			return true;
		}
    }
    
    /*package protected*/
    class ChangePage implements ValueListener {
        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
            if (aNewValue instanceof Integer) {
				int pageNumber = ((Integer) aNewValue).intValue();

				// Must check value as value listener is triggered before constraints are checked,
				// so an invalid page may be given.
				if (pageNumber <= 0 || pageNumber > getPagingModel().getPageCount()) {
                    // ignore, to avoid exceptions
                    return;
                }

				getPagingModel().setPage((pageNumber - 1));
            }
        }
    }
    
    /*package protected*/ 
    class ChangePageSize implements ValueListener {
        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
           if (aNewValue instanceof Collection<?>) {
                Collection theValues  = (Collection) aNewValue;
                if (! theValues.isEmpty()) {
                    Integer theNew = (Integer) theValues.iterator().next();
					getPagingModel().changePageSizeSpec(theNew.intValue());
                }
            }
        }
    }
    
    public static class PageSizeOptionLabelProvider implements LabelProvider {
        
        public static final LabelProvider INSTANCE = new PageSizeOptionLabelProvider();
        
        @Override
		public String getLabel(Object aObject) {
            if (aObject instanceof Integer) {
                if (((Integer) aObject).intValue() == 0) {
					return Resources.getInstance().getString(I18NConstants.SHOW_ALL_PAGES);
                }
                else {
                    return String.valueOf(aObject);
                }
            }
            else {
				return Resources.getInstance().getString(I18NConstants.DEFAULT_PAGE_SIZE);
            }
        }
    }
    
    public static class PageSizeOptionComparator implements Comparator<Number> {
        
        public static final Comparator<Number> INSTANCE = new PageSizeOptionComparator();
        
		private PageSizeOptionComparator() {
			// singleton instance
		}
        
		@Override
		public int compare(Number o1, Number o2) {
			int v1 = o1.intValue();
			int v2 = o2.intValue();
			if (v1 == v2) {
				return 0;
			} else if (v1 == PagingModel.SHOW_ALL) {
				return 1;
			} else if (v2 == PagingModel.SHOW_ALL) {
				return -1;
			} else {
				return v1 - v2;
			}
		}
    }
    
	@Override
	public void notifyTableViewModelChanged(TableData source, TableViewModel oldValue, TableViewModel newValue) {
		if (!isAttached()) {
			/* may happen in following (hypothetical) case: The table data initializes its
			 * TableViewModel if the first listener attaches and then it fires an event. As the
			 * TableControl first attaches itself at the TableData during `internalAttach()`, and
			 * then at the TableViewModel, it would attach twice if this code is executed. */
			return;
		}

		if (source != tableData) {
			return;
		}
		if (oldValue != null) {
			removeAsListener(oldValue);
		}
		if (newValue != null) {
			addAsListener(newValue);
		}
		requestRepaint();
	}

	@Override
	public void notifySelectionModelChanged(TableData source, SelectionModel oldValue, SelectionModel newValue) {
		if (!isAttached()) {
			/* may happen in following (hypothetical) case: The table data initializes its
			 * SelectionModel if the first listener attaches and then it fires an event. As the
			 * TableControl first attaches itself at the TableData during `internalAttach()`, and
			 * then at the SelectionModel, it would attach twice if this code is executed. */
			return;
		}

		if (source != tableData) {
			return;
		}
		if (oldValue != null) {
			oldValue.removeSelectionListener(this);
		}
		if (newValue != null) {
			newValue.addSelectionListener(this);
		}
		requestRepaint();
	}

	@Override
	protected void disableChildScopes(boolean disabled) {
		super.disableChildScopes(disabled);

		for (LocalScope ls : rowScopes.values()) {
			ls.disableScope(disabled);
		}
	}

	/**
	 * This method clears the control scopes, thus detaches all child controls,
	 * within the specified row range
	 * 
	 * @param firstRow - the first row of the row set, whose row control scopes shall be cleared
	 * @param lastRow - the last row of the row set, whose row control scopes shall be cleared
	 * 
	 */
	void clearRowScopes(int firstRow, int lastRow) {
		assert firstRow <= lastRow : "Start row must be less or equal than stop row";
		
		for(int i = firstRow; i <= lastRow; i++) {
			LocalScope rowScope = rowScopes.remove(i);
			if(rowScope != null) {
				rowScope.clear();
			}
		}
	}

	/**
	 * Clears all control scopes
	 */
	private void clearAllRowScopes() {
		for (LocalScope scope : rowScopes.values()) {
			scope.clear();
		}
		rowScopes.clear();
	}

	public TableData getTableData() {
		return tableData;
	}

	/**
	 * the id of the control's current version in lifecycle
	 */
	public int getDisplayVersion() {
		return controlWriteCounter;
	}

	/**
	 * Adds a {@link PropertyListener} to this control that the {@link #isVisible() visibility} is
	 * synchronous to {@link FormMember#isVisible() visibility} of the given {@link FormMember}.
	 * 
	 * <p>
	 * This method creates an {@link AttachedPropertyListener} that adds a listener to the given
	 * {@link FormField} during attach and de-registers it during detach. That listener updates
	 * {@link #setVisible(boolean)} on the {@link FormMember#VISIBLE_PROPERTY visible} event.
	 * </p>
	 * 
	 * @param member
	 *        The member to synchronise visibility.
	 * 
	 * @return The {@link AttachedPropertyListener} added as listener for property
	 *         {@link AbstractControlBase#ATTACHED_PROPERTY}
	 */
	public final AttachedPropertyListener addVisibilityListenerFor(final FormMember member) {
		VisibilityListener dispatchVisibility = new VisibilityListener() {
	
			@Override
			public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
				if (sender != member) {
					/* Do not care about events of other members, e.g. of fields displayed within
					 * given member. If such a field becomes invisible the event bubbles up to the
					 * member. */
					return Bubble.BUBBLE;
				}
				TableControl.this.setVisible(newVisibility.booleanValue());
				return Bubble.BUBBLE;
			}
		};
		AttachListener listener = new AttachListener(member, FormMember.VISIBLE_PROPERTY, dispatchVisibility) {
	
			@Override
			protected void updateObservedState(AbstractControlBase sender) {
				TableControl.this.setVisible(member.isVisible());
			}
	
		};
		addListener(AbstractControlBase.ATTACHED_PROPERTY, listener);
		return listener;
	}
	
	/**
	 * Adds a {@link PropertyListener} to this control that the table is focused, when it is
	 * requested to the given {@link Focusable}.
	 * 
	 * <p>
	 * This method creates an {@link AttachedPropertyListener} that adds a listener to the given
	 * {@link Focusable} during attach and de-registers it during detach. That listener calls
	 * {@link #focus()} on the {@link Focusable#FOCUS_PROPERTY focus} event.
	 * </p>
	 * 
	 * @return The {@link AttachedPropertyListener} added as listener for property
	 *         {@link AbstractControlBase#ATTACHED_PROPERTY}
	 */
	public final AttachedPropertyListener addFocusListener(Focusable focusable) {
		FocusRequestedListener focusDispatch = new FocusRequestedListener() {

			@Override
			public Bubble handleFocusRequested(Focusable sender) {
				if (sender != focusable) {
					/* Do not care about events of other members, e.g. of fields displayed within
					 * given member. If such a field becomes invisible the event bubbles up to the
					 * member. */
					return Bubble.BUBBLE;
				}
				TableControl.this.focus();
				return Bubble.BUBBLE;
			}
		};
		AttachListener listener = new AttachListener(focusable, Focusable.FOCUS_PROPERTY, focusDispatch) {

			@Override
			protected void updateObservedState(AbstractControlBase sender) {
				DisplayContext context = DefaultDisplayContext.getDisplayContext();
				if (FocusHandling.shouldFocus(context, focusable)) {
					context.set(REQUESTED_FOCUS, new Pair<>(TableControl.this, focusable));
				}
			}

		};
		addListener(AbstractControlBase.ATTACHED_PROPERTY, listener);
		return listener;
	}

	/**
	 * Focuses the displayed table.
	 */
	public void focus() {
		addUpdate(new JSFunctionCall(getID(), "TABLE", "focus"));
	}

	/**
	 * The way how the selection has to be changed.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public enum SelectionType {

		/**
		 * The clicked row must be selected.
		 */
		SINGLE,

		/**
		 * The selection state of the row has to be toggled, i.e. if the row is currently selected,
		 * it must be de-selected, if it is not selected, it must be selected.
		 */
		TOGGLE_SINGLE,

		/**
		 * All elements between the last clicked row and the clicked row must be selected. Other
		 * selection remains untouched.
		 */
		AREA,

		/**
		 * All elements between the last clicked row and the clicked row must be selected. Other
		 * elements are deselected.
		 */
		TOGGLE_AREA
	}

	/**
	 * Strategy, that determines an {@link SelectionType}. That means the way how the internal
	 * {@link SelectionModel} shall be altered (e.g. keeping old selection, select a single row or
	 * multiple rows, etc.).
	 */
	public static interface SelectionStrategy {

		SelectionType getSelectionType(SelectionModel selectionModel, boolean ctrlKey, boolean shiftKey);
	}

	/**
	 * Default implementation of {@link SelectionStrategy}. That means, click on a row selects this
	 * row and removes any old selection. Holding Ctrl modifier keeps the former selection. Holding
	 * Shift modifier a range of rows will be selected.
	 */
	public static class DefaultSelectionStrategy implements SelectionStrategy {

		public static final SelectionStrategy INSTANCE = new DefaultSelectionStrategy();

		@Override
		public SelectionType getSelectionType(SelectionModel selectionModel, boolean ctrlKey, boolean shiftKey) {
			SelectionType selectionType;
			if (selectionModel instanceof SingleSelectionModel) {
				if (ctrlKey) {
					selectionType = SelectionType.TOGGLE_SINGLE;
				} else {
					selectionType = SelectionType.SINGLE;
				}
			} else {
				if (shiftKey && ctrlKey) {
					selectionType = SelectionType.TOGGLE_AREA;
				} else if (shiftKey) {
					selectionType = SelectionType.AREA;
				} else if (ctrlKey) {
					selectionType = SelectionType.TOGGLE_SINGLE;
				} else {
					selectionType = SelectionType.SINGLE;
				}
			}
			return selectionType;
		}

	}
}
