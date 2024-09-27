/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import static com.top_logic.layout.DisplayDimension.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;

import com.top_logic.base.locking.LockService;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandBuilder;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommand;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareAlgorithmHolder;
import com.top_logic.layout.component.model.NoSelectionModel;
import com.top_logic.layout.component.model.NoSingleSelectionModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormGroupAccessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutableCommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.ValueVetoListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.ValueWithErrorControlProvider;
import com.top_logic.layout.form.treetable.DefaultTreeTableImageProvider;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.control.IndexViewportState;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.MutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.renderer.ColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.TableDeclaration;
import com.top_logic.layout.tree.renderer.TreeTableRenderer;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExcelExportHandler;
import com.top_logic.tool.export.ExcelExportSupport;
import com.top_logic.tool.export.ExportAware;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * This is a EditComponent to edit a complete structure at once, including adding, moving and
 * removing elements in structure and editing attributes of the elements.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class StructureEditComponent<N extends MutableTLTreeNode<N>> extends EditComponent implements
		StructureEditComponentConstants, SelectionListener, TreeModelListener, ControlRepresentable, ExportAware,
		SelectionModelOwner, CompareAlgorithmHolder, ScrollPositionModel {

	public interface Config extends EditComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		@StringDefault(EDIT_STRUCTURE_OPERATION)
		String getLockOperation();

		@Name(XML_ATTRIBUTE_COLUMNS)
		@StringDefault(DefaultTableDeclaration.DEFAULT_COLUMN_NAME)
		String getColumns();

		@Name(XML_ATTRIBUTE_HEADER_RENDERER)
		@InstanceFormat
		Renderer<?> getHeaderRenderer();

		@Name(XML_ATTRIBUTE_NODE_RESOURCE_PROVIDER)
		@InstanceFormat
		ResourceProvider getNodeResourceProvider();

		@Name(XML_ATTRIBUTE_NODE_ACCESSOR)
		@InstanceFormat
		Accessor getNodeAccessor();

		@Name(XML_ATTRIBUTE_CONTROL_PROVIDER)
		@InstanceFormat
		ControlProvider getControlProvider();

		@Name(XML_ATTRIBUTE_ROOT_VISIBLE)
		@BooleanDefault(true)
		boolean getRootVisible();

		@Name(XML_ATTRIBUTE_EXPAND_ALL)
		@BooleanDefault(false)
		boolean getExpandAll();

		/** Command handler name of the export command. */
		@Name(XML_ATTRIBUTE_EXPORT_COMMAND)
		@Nullable
		@StringDefault(ExcelExportHandler.COMMAND)
		String getExportCommand();

		@Name(XML_CONF_KEY_DOWNLOAD_NAME_KEY)
		@InstanceFormat
		ResKey getDownloadNameKey();

		@Name(XML_CONF_KEY_TEMPLATE_NAME)
		@StringDefault("defaultTemplate.xls")
		String getTemplateName();

		@Name(XML_ATTRIBUTE_FIXED_COLUMNS)
		@IntDefault(DEFAULT_FIXED_COLUMN_COUNT)
		int getFixedColumns();

		/**
		 * Is not supported by {@link StructureEditComponent} and its subclasses, and is therefore
		 * not allowed to be set.
		 */
		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		public String getPage();

		/** Name of the attribute {@link #isMultiSelect()} */
		String ATTRIBUTE_MULTI_SELECT = "multiSelect";

		/**
		 * Whether the component should support multi selection.
		 */
		@Name(ATTRIBUTE_MULTI_SELECT)
		boolean isMultiSelect();

		/** Name of the attribute {@link #isMultiSelect()} */
		String ATTRIBUTE_SHOW_SELECTION_COLUMN = "showSelectionColumn";

		/**
		 * Whether a column should be displayed which shows selection of tree explicit.
		 */
		@Name(ATTRIBUTE_SHOW_SELECTION_COLUMN)
		boolean isShowSelectionColumn();

		/** Name of the attribute {@link #getViewModeColumns()} */
		String ATTRIBUTE_VIEW_MODE_COLUMNS = "viewModeColumns";

		/**
		 * Returns the columns that must be shown in view mode.
		 * 
		 * If none are given {@link #getColumns()} are used.
		 */
		@Name(ATTRIBUTE_VIEW_MODE_COLUMNS)
		String getViewModeColumns();

		/** Name of the attribute {@link #getViewModeColumns()} */
		String ATTRIBUTE_VIEW_MODE_FIXED_COLUMN_COUNT = "viewModeFixedColumns";

		/**
		 * Returns the number of columns that must be fixed in view mode.
		 * 
		 * If none is given {@link #getFixedColumns()} is used.
		 */
		@Name(ATTRIBUTE_VIEW_MODE_FIXED_COLUMN_COUNT)
		@IntDefault(-1)
		int getViewModeFixedColumnsCount();

		/** Name of attribute {@link #getOnlyOneLevelModify()}. */
		String ATTRIBUTE_ONLY_ONE_LEVEL_MODIFY = "onlyOneLevelModify";

		/**
		 * Whether commands are just allowed if selection contains of nodes that all have the same
		 * parent.
		 */
		@Name(ATTRIBUTE_ONLY_ONE_LEVEL_MODIFY)
		boolean getOnlyOneLevelModify();

		/** Name of attribute {@link #getCompareModelBuilder()}. */
		String ATTRIBUTE_COMPARE_MODEL_BUILDER = "compareModelBuilder";

		/**
		 * Returns the {@link TreeModelBuilder} that creates the displayed structure.
		 */
		@Name(ATTRIBUTE_COMPARE_MODEL_BUILDER)
		@InstanceFormat
		TreeModelBuilder<?> getCompareModelBuilder();

		/**
		 * Type of the displayed objects. This type is used to define generic table configuration.
		 * 
		 * @see com.top_logic.layout.table.component.TableComponent.Config#getObjectType()
		 */
		@Name(TableComponent.XML_CONF_KEY_OBJECT_TYPE)
		String getObjectType();

		@Override
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditComponent.Config.super.modifyIntrinsicCommands(registry);
			if (getEditCommand() != null) {
				registry.registerButton(getExportCommand());
			}
			registry.registerCommand(OpenStructureEditCompareViewCommand.COMMAND_NAME);
		}

	}

	/** Configuration name for the download file name i18n key. */
	public static final String XML_CONF_KEY_DOWNLOAD_NAME_KEY = "downloadNameKey";

	/** Configuration name for the location of the export template file. */
	public static final String XML_CONF_KEY_TEMPLATE_NAME = "templateName";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe("<div class='body'"
	+   " xmlns='" + HTMLConstants.XHTML_NS + "'"
	+   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
	+   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
	+   LayoutControlRenderer.getLayoutConstraintInformation(100, DisplayUnit.PERCENT)
		+ LayoutControlRenderer.getLayoutInformation(Orientation.VERTICAL, 100)
	+   ">"
	+"	<p:field name='" + FIELD_NAME_TREE + "' />"
	+"</div>");
    
	public static final String XML_ATTRIBUTE_COLUMNS = "columns";


    public static final String XML_ATTRIBUTE_HEADER_RENDERER = "headerRenderer";
    public static final String XML_ATTRIBUTE_NODE_RESOURCE_PROVIDER = "nodeResourceProvider";
    public static final String XML_ATTRIBUTE_NODE_ACCESSOR = "nodeAccessor";
    public static final String XML_ATTRIBUTE_CONTROL_PROVIDER = "controlProvider";
    public static final String XML_ATTRIBUTE_ROOT_VISIBLE = "rootVisible";


    public static final String XML_ATTRIBUTE_EXPAND_ALL = "expandAll";

    public static final String XML_ATTRIBUTE_EXPORT_COMMAND = "exportCommand";
    
	public static final String XML_ATTRIBUTE_FIXED_COLUMNS = "fixedColumns";

	public static final String XML_ATTRIBUTE_VIEW_MODE_FIXED_COLUMNS = "viewModeFixedColumns";

    /**
	 * Lock operation name for editing the structure of a (sub-) tree.
	 * 
	 * @see LockService#createLock(String, Object...)
	 */
	public static final String EDIT_STRUCTURE_OPERATION = "editStructure";

	/**
	 * number of fixed columns if no number is configured
	 */
	private static final int DEFAULT_FIXED_COLUMN_COUNT = 2;

	public static final ExecutableState NO_EXEC_ROOT_SELECTED = 
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_ROOT_SELECTED);

	public static final ExecutableState NO_EXEC_NODES_WITH_DIFFERENT_PARENTS_SELECTED =
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_NODES_WITH_DIFFERENT_PARENTS_SELECTED);

	public static final ExecutableState NO_EXEC_EMPTY_SELECTION =
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_EMPTY_SELECTION);

	public static final ExecutableState NO_EXEC_ALREADY_ALL_NODES_SELECTED =
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_ALREADY_ALL_NODES_SELECTED);

	public static final ExecutableState NOT_EXEC_ALL_SIBBLINGS_ALREADY_SELECTED =
		ExecutableState.createDisabledState(I18NConstants.NOT_EXEC_ALL_SIBLINGS_ALREADY_SELECTED);

	public static final ExecutableState NO_EXEC_NO_MOVES_POSSIBLE =
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_NO_MOVES_POSSIBLE);

	public static final ExecutableState NO_EXEC_NO_SELECTED_NODE_REMOVABLE =
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_NO_SELECTED_NODE_REMOVABLE);

	public static final ExecutableState NO_EXEC_MORE_THAN_ONE_NODE_SELECTED =
		ExecutableState.createDisabledState(I18NConstants.NO_EXEC_MORE_THAN_ONE_NODE_SELECTED);

	private static final Property<String> CLIQUE = TypedAnnotatable.property(String.class, "clique");

	private final Command EXECUTABLE_EXPAND_ALL = new ExpandTreeCommand(true);
    private final Command EXECUTABLE_COLLAPSE_ALL = new ExpandTreeCommand(false);

	private final Command EXECUTABLE_SELECT_ALL = new SelectAllCommand();

	private final Command EXECUTABLE_DESELECT_ALL = new DeselectAllCommand();
    private final Command EXECUTABLE_SELECT_SIBLINGS = new SelectAllSiblings();
    private final Command EXECUTABLE_INVERT_SELECTION = new InvertSelection();
    private final Command EXECUTABLE_MOVE_UP = new MoveUpCommand();
    private final Command EXECUTABLE_MOVE_DOWN = new MoveDownCommand();
    private final Command EXECUTABLE_MOVE_LEFT = new MoveLeftCommand();
    private final Command EXECUTABLE_MOVE_RIGHT = new MoveRightCommand();
    private final Command EXECUTABLE_REMOVE = new RemoveCommand();
    private final Command EXECUTABLE_CREATE = new CreateCommand();

    /** Holds the tree. */
    private FormTree formTree;

    /**
     * Holds the expansion states in case of edit mode switch.
     * The expansion model is a tuple(model, set(expanded user objects))
     **/
    protected Tuple expansionModel;

    /** Holds the tree. */
    protected TableDeclaration tableDeclaration;

    /** The list of all buttons registered here. */
	protected List<? extends CommandModel> buttons = Collections.emptyList();

    /** The configured columns for the tree table. */
	protected final String columns;

	protected final String viewModeColumns;

    /* Renderer options */
	protected final Renderer<?> headerRenderer;
    protected final ResourceProvider nodeResourceProvider;
    protected final Accessor nodeAccessor;
    protected final ControlProvider controlProvider;

    /** Flag whether the root node shall be visible. */
    protected final boolean rootVisible;

	/** Flag whether multi selection is allowed. */
	protected final boolean multiSelect;

    /** Flag whether all nodes shall be expanded initially. */
    protected final boolean expandAll;
    
	/** the control which is used to render this component */
	private Control renderingControl;

	/**
	 * number of fixed columns in this component
	 */
	protected final int fixedColumnCount;

	/** Number of fixed columns in this component in view mode. */
	protected final int viewModeFixedColumnCount;

	/** Configuration name for the download file name i18n key. */
	private final ResKey downloadKey;

	/** Location of the export template file. */
	private final String templateName;

	/** Value of {@link Config#getOnlyOneLevelModify()} */
	final boolean onlyOneLevelModify;

	private CompareAlgorithm _compareAlgorithm;

	TableConfigurationProvider _tableConfiguration;

	TreeModelBuilder<?> _compareModelBuilder;

	private ViewportState _tableViewportState = new ViewportState();

    /**
     * Creates a new instance of this class.
     */
    public StructureEditComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		String configuredColumns = atts.getColumns();
		boolean selectionColumnAdded = false;
		if (atts.isShowSelectionColumn() && !configuredColumns.contains(COLUMN_SELECTION)) {
			configuredColumns = COLUMN_SELECTION + ',' + configuredColumns;
			selectionColumnAdded = true;
		}
		this.columns = configuredColumns;
		String configuredViewColumns = atts.getViewModeColumns();
		if (StringServices.isEmpty(configuredViewColumns)) {
			configuredViewColumns = atts.getColumns();
		}
		this.viewModeColumns = configuredViewColumns;
        this.headerRenderer = (atts.getHeaderRenderer() == null) ? ResourceRenderer.newResourceRenderer(new I18NResourceProvider(getResPrefix().append(PREFIX_COLUMN))) : atts.getHeaderRenderer();
        this.nodeResourceProvider = (atts.getNodeResourceProvider() == null) ? TLTreeNodeResourceProvider.INSTANCE : atts.getNodeResourceProvider();
        this.nodeAccessor = (atts.getNodeAccessor() == null) ? FormGroupAccessor.INSTANCE : atts.getNodeAccessor();
		this.controlProvider = (atts.getControlProvider() == null)
			? getDefaultControlProvider() : atts.getControlProvider();
		this.multiSelect = atts.isMultiSelect();
        this.rootVisible = atts.getRootVisible();
        this.expandAll = atts.getExpandAll();
		this.downloadKey = atts.getDownloadNameKey();
		this.templateName = atts.getTemplateName();
		int configuredFixedColumns = atts.getFixedColumns();
		if (configuredFixedColumns < 0) {
			throw new ConfigurationException("Configuration value for parameter '" + XML_ATTRIBUTE_FIXED_COLUMNS
				+ "' must be geq 0 but is '" + configuredFixedColumns + "' in " + StructureEditComponent.class);
		}
		if (selectionColumnAdded) {
			configuredFixedColumns++;
		}
		this.fixedColumnCount = configuredFixedColumns;
		int configuredViewModeFixedColumns = atts.getViewModeFixedColumnsCount();
		if (configuredViewModeFixedColumns < 0) {
			configuredViewModeFixedColumns = atts.getFixedColumns();
		}
		this.viewModeFixedColumnCount = configuredViewModeFixedColumns;
		this.onlyOneLevelModify = atts.getOnlyOneLevelModify();

		_tableConfiguration = TableConfigurationFactory.combine(createTableConfigurations(atts));
		_compareModelBuilder = atts.getCompareModelBuilder();
    }

	private List<TableConfigurationProvider> createTableConfigurations(Config atts) {
		List<TableConfigurationProvider> providers = new ArrayList<>();
		addTableConfigurations(providers);
		String contentTypeDescription = atts.getObjectType();
		if (!StringServices.isEmpty(contentTypeDescription)) {
			TableConfigurationProvider dynamicConfiguration =
				GenericTableConfigurationProvider.getTableConfigurationProvider(contentTypeDescription);
			providers.add(dynamicConfiguration);
		}
		return providers;
	}

	/**
	 * Adds {@link TableConfigurationProvider} defining the displayed table.
	 * 
	 * @param providers
	 *        {@link List} to add necessary {@link TableConfigurationProvider} to.
	 */
	protected void addTableConfigurations(List<TableConfigurationProvider> providers) {
		// hook for subclasses to add programmatic table configurations.
	}

	@Override
	public void setCompareAlgorithm(CompareAlgorithm algorithm) {
		_compareAlgorithm = algorithm;
	}

	@Override
	public CompareAlgorithm getCompareAlgorithm() {
		return _compareAlgorithm;
	}

    /**
     * This method gets called when the form Context gets removed and does cleanup work.
     * Subclasses may extend this method. In addition, the expansion model of tree gets
     * stored.
     */
	protected void cleanUpFormContext(Object oldModel) {
		if (formTree != null && oldModel != null) {
			expansionModel = TupleFactory.newTuple(oldModel, TreeUIModelUtil.getExpansionUserModel(treeUIModel()));
        }
        formTree = null;
        ToolBar toolbar = getToolBar();
		if (toolbar != null) {
			clearToolBarButtons(toolbar, buttons);
		}
		buttons = Collections.emptyList();
        tableDeclaration = null;
        if (renderingControl != null) {
        	renderingControl.detach();
        	renderingControl = null;
        }
        
        removeFormContext();
    }

    @Override
    public FormContext createFormContext() {
        FormContext context = new FormContext(this);
        initFormContext(context);
        return context;
    }

	/**
	 * Initializes the given empty {@link FormContext}.
	 */
	protected void initFormContext(FormContext context) {
		Object theModel = getModel();
        if (!supportsInternalModel(theModel)) {
			return;
        }

        // create/get the tree model we're going to display.
		TLTreeModel<N> applicationModel = createTreeModel();

        // wrap the model into a UITreeModel to manage expansion state for nodes
		DefaultStructureTreeUIModel<N> treeUIModel =
			new DefaultStructureTreeUIModel<>(applicationModel, this.rootVisible);
        treeUIModel.addTreeModelListener(this);

        // create form tree
		NodeGroupInitializer nodeGroupInitializer = new StructureEditNodeGroupInitializer(getNodeGroupInitializer());
		formTree = new FormTree(FIELD_NAME_TREE, getResPrefix(), treeUIModel, nodeGroupInitializer);
		SelectionModel selectionModel;
		if (multiSelect) {
			if (isSelectable()) {
				selectionModel = new DefaultMultiSelectionModel(this);
				selectionModel.addSelectionListener(this);
			} else {
				selectionModel = NoSelectionModel.INSTANCE;
			}
		} else {
			if (isSelectable()) {
				selectionModel = new DefaultSingleSelectionModel(this);
				selectionModel.addSelectionListener(this);
			} else {
				selectionModel = NoSingleSelectionModel.SINGLE_SELECTION_INSTANCE;
			}
		}
        formTree.setSelectionModel(selectionModel);

        // restore expansion states
        if (expansionModel != null && expansionModel.get(0) == theModel) {
            TreeUIModelUtil.setExpansionUserModel((Set)expansionModel.get(1), treeUIModel());
            expansionModel = null;
        }
        else {
            if (expandAll) treeUIModel.setExpandAll(true);
            else treeUIModel.setExpanded(treeUIModel.getRoot(), true);
        }

        // create the renderer using the table declaration.
        TreeTableRenderer renderer = createTreeTableRenderer(getTableDeclaration());
        formTree.setTreeRenderer(renderer);
        context.addMember(formTree);

        // field to trigger changed flag for changes in the form tree
        context.addMember(FormFactory.newHiddenField(FIELD_NAME_HIDDEN));

        // create the structure edit buttons
        FormGroup buttonsGroup = new FormGroup(TREE_BUTTONS_GROUP, getResPrefix());
        buttons = createButtons(formTree);
        for (int i = 0, length = buttons.size(); i < length; i++) {
            buttonsGroup.addMember((CommandField)buttons.get(i));
        }
        context.addMember(buttonsGroup);
		initToolBarButtons(getToolBar());
        handleButtonExecutability();
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && hasFormContext();
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);

		// Ensure that toolbar buttons are created before rendering.
		getFormContext();

		return result;
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);

		if (oldValue != null) {
			clearToolBarButtons(oldValue, buttons);
		}
		
		if (hasFormContext()) {
			initToolBarButtons(newValue);
		}
	}

	private void initToolBarButtons(ToolBar toolBar) {
		if (toolBar == null) {
			return;
		}

		installToolBarButtons(toolBar, buttons);
	}

	/**
	 * Installs the given buttons into a component button toolbar group of the given {@link ToolBar}
	 * .
	 */
	public static void installToolBarButtons(ToolBar toolBar, List<? extends CommandModel> buttons) {
		clearToolBarButtons(toolBar, buttons);

		if (!buttons.isEmpty()) {
			CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
			for (CommandModel command : buttons) {
				String cliqueGroup = cliqueGroup(factory, command);
				ToolBarGroup group = toolBar.defineGroup(cliqueGroup);
				group.addButton(command);
			}
		}
	}

	private static String cliqueGroup(CommandHandlerFactory factory, CommandModel command) {
		String clique = command.get(CLIQUE);
		if (clique == null) {
			clique = factory.getDefaultClique();
		}
		String cliqueGroup = factory.getCliqueGroup(clique);
		return cliqueGroup;
	}

	private static void clearToolBarButtons(ToolBar toolBar, List<? extends CommandModel> buttons) {
		CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		for (CommandModel command : buttons) {
			String cliqueGroup = cliqueGroup(factory, command);
			toolBar.removeGroup(cliqueGroup);
		}
	}

    /**
     * Creates the tree table buttons. This may be overridden by sub classes to add their
     * own buttons or modify the existing ones.
     *
     * @param aTree
     *        the FormTree to create buttons for.
     * @return Returns a list of command fields to be rendered as buttons above the
     *         tree-table.
     */
    protected List<CommandField> createButtons(FormTree aTree) {
        List<CommandField> commands = new ArrayList<>();
		commands.add(createButton(aTree, COMMAND_EXPAND_ALL, EXECUTABLE_EXPAND_ALL, CommandHandlerFactory.EXPAND_CLIQUE,
			com.top_logic.layout.basic.Icons.EXPAND_ALL, com.top_logic.layout.basic.Icons.EXPAND_ALL_DISABLED));
		commands.add(createButton(aTree, COMMAND_COLLAPSE_ALL, EXECUTABLE_COLLAPSE_ALL,
			CommandHandlerFactory.COLLAPSE_CLIQUE, com.top_logic.layout.basic.Icons.COLLAPSE_ALL,
			com.top_logic.layout.basic.Icons.COLLAPSE_ALL_DISABLED));
        if (isSelectable()) {
			if (multiSelect) {
				commands.add(createButton(aTree, COMMAND_SELECT_ALL, EXECUTABLE_SELECT_ALL,
					CommandHandlerFactory.SELECT_GROUP, Icons.SELECT_ALL_CHECKBOXES, Icons.SELECT_ALL_CHECKBOXES_DISABLED));
				commands.add(createButton(aTree, COMMAND_DESELECT_ALL, EXECUTABLE_DESELECT_ALL,
					CommandHandlerFactory.SELECT_GROUP, Icons.REMOVE_CHECKBOX,
					Icons.REMOVE_CHECKBOX_DISABLED));
				commands.add(createButton(aTree, COMMAND_SELECT_SIBLINGS, EXECUTABLE_SELECT_SIBLINGS,
					CommandHandlerFactory.SELECT_GROUP,
					Icons.SELECT_SIBLINGS, Icons.SELECT_SIBLINGS_DISABLED));
				commands.add(createButton(aTree, COMMAND_INVERT_SELECTION, EXECUTABLE_INVERT_SELECTION,
					CommandHandlerFactory.SELECT_GROUP,
					Icons.INVERT_SELECTION, Icons.INVERT_SELECTION_DISABLED));
			}
			commands.add(createButton(aTree, COMMAND_CREATE, EXECUTABLE_CREATE, CommandHandlerFactory.CREATE_CLIQUE,
				com.top_logic.layout.table.control.Icons.ADD_ROW,
				com.top_logic.layout.table.control.Icons.ADD_ROW_DISABLED));
			commands.add(createButton(aTree, COMMAND_MOVE_UP, EXECUTABLE_MOVE_UP, "moveUp",
				com.top_logic.layout.table.control.Icons.MOVE_ROW_UP,
				com.top_logic.layout.table.control.Icons.MOVE_ROW_UP_DISABLED));
			commands.add(createButton(aTree, COMMAND_MOVE_DOWN, EXECUTABLE_MOVE_DOWN, "moveDown",
				com.top_logic.layout.table.control.Icons.MOVE_ROW_DOWN,
				com.top_logic.layout.table.control.Icons.MOVE_ROW_DOWN_DISABLED));
            
            commands.add(createButton(aTree, COMMAND_MOVE_LEFT, EXECUTABLE_MOVE_LEFT, "moveLeft", Icons.MOVE_ROW_LEFT, Icons.MOVE_ROW_LEFT_DISABLED));
            commands.add(createButton(aTree, COMMAND_MOVE_RIGHT, EXECUTABLE_MOVE_RIGHT, "moveRight", Icons.MOVE_ROW_RIGHT, Icons.MOVE_ROW_RIGHT_DISABLED));
			commands.add(createButton(aTree, COMMAND_REMOVE, EXECUTABLE_REMOVE, CommandHandlerFactory.DELETE_CLIQUE,
				Icons.DELETE_MENU, Icons.DELETE_MENU_DISABLED));
        }
		commands.add(createCompareViewButton(aTree));
        return commands;
    }

	/**
	 * Creates a button in the default clique.
	 * 
	 * @see CommandHandlerFactory#getDefaultClique()
	 * 
	 * @see #createButton(FormTree, String, Command, String, ThemeImage, ThemeImage)
	 */
	protected final CommandField createButton(FormTree aTree, String aName, Command aExecutable,
			ThemeImage aImage, ThemeImage aDisabledImage) {
		String defaultClique = CommandHandlerFactory.getInstance().getDefaultClique();
		return createButton(aTree, aName, aExecutable, defaultClique, aImage, aDisabledImage);
	}

	/**
	 * Creates a button in the default clique.
	 * 
	 * @see CommandHandlerFactory#getDefaultClique()
	 * 
	 * @see #createButton(FormTree, String, CommandBuilder, String, ThemeImage, ThemeImage)
	 */
	protected final CommandField createButton(FormTree aTree, String aName, CommandBuilder aExecutable,
			ThemeImage aImage, ThemeImage aDisabledImage) {
		String defaultClique = CommandHandlerFactory.getInstance().getDefaultClique();
		return createButton(aTree, aName, aExecutable, defaultClique, aImage, aDisabledImage);
	}

    /**
     * Creates a button to open the comparison dialog.
     */
	protected final CommandField createCompareViewButton(FormTree aTree) {
		String commandId = OpenStructureEditCompareViewCommand.COMMAND_NAME;
		CommandHandler compareViewOpener = getCommandById(commandId);
		ThemeImage image = compareViewOpener.getImage(this);
		ThemeImage notExecutableImage = compareViewOpener.getNotExecutableImage(this);
		CommandBuilder compareViewExecutable = ComponentCommand.newInstance(compareViewOpener, this);
		CommandField result = createButton(aTree, commandId, compareViewExecutable, image, notExecutableImage);
		return result;
	}

    /**
	 * Creates a command field representing a button.
	 * 
	 * @param aTree
	 *        the FormTree to create the button for
	 * @param aName
	 *        the name of the command field
	 * @param aExecutable
	 *        the Executable to execute when the button is triggered
	 * @param clique
	 *        The the command clique defining the group to add the command to.
	 * @param aImage
	 *        the image of the button
	 * @param aDisabledImage
	 *        the disabled image of the button
	 * @return the created command field representing the button
	 */
	protected CommandField createButton(FormTree aTree, String aName, Command aExecutable, String clique,
			ThemeImage aImage, ThemeImage aDisabledImage) {
        CommandField commandField = FormFactory.newCommandField(aName, aExecutable);
		return init(aName, clique, aImage, aDisabledImage, commandField);
	}

	/**
	 * Creates a command field representing a button.
	 * 
	 * @param aTree
	 *        the FormTree to create the button for
	 * @param aName
	 *        the name of the command field
	 * @param builder
	 *        Builder for the {@link Command} to execute when the button is triggered.
	 * @param clique
	 *        The the command clique defining the group to add the command to.
	 * @param aImage
	 *        the image of the button
	 * @param aDisabledImage
	 *        the disabled image of the button
	 * @return the created command field representing the button
	 */
	protected CommandField createButton(FormTree aTree, String aName, CommandBuilder builder, String clique,
			ThemeImage aImage, ThemeImage aDisabledImage) {
		CommandField commandField = FormFactory.newCommandField(aName, builder);
		return init(aName, clique, aImage, aDisabledImage, commandField);
	}

	private CommandField init(String aName, String clique, ThemeImage aImage, ThemeImage aDisabledImage,
			CommandField commandField) {
		commandField.setImage(aImage);
		ResKey label = I18NConstants.STRUCTURE_EDIT_BUTTONS.key(aName);
        commandField.setLabel(label);
        commandField.setNotExecutableImage(aDisabledImage);
        commandField.setInheritDeactivation(false);
		commandField.set(CLIQUE, clique);
        return commandField;
	}

    /**
     * Defines, whether the nodes should be selectable (required to change the structure).
     * If not, the change buttons won't get created.
     */
    protected boolean isSelectable() {
        return isInEditMode();
    }


    /**
     * Creates the TreeTableRenderer to use to render the tree table.
     */
    protected TreeTableRenderer createTreeTableRenderer(TableDeclaration aTableDeclaration) {
        final DefaultTreeTableImageProvider imageProvider = DefaultTreeTableImageProvider.INSTANCE;
		int fixedColumns;
		if (isInEditMode()) {
			fixedColumns = fixedColumnCount;
		} else {
			fixedColumns = viewModeFixedColumnCount;
		}
		return new FixedTreeTableRenderer(imageProvider, aTableDeclaration, fixedColumns);
    }

    /**
     * Gets the table declaration.
     */
    public TableDeclaration getTableDeclaration() {
        if (tableDeclaration == null) {
            tableDeclaration = getTableDeclarationProvider().getTableDeclaration();
        }
        return tableDeclaration;
    }

    /**
     * Gets the table declaration provider.
     */
    protected TableDeclarationProvider getTableDeclarationProvider() {
        return new StructureEditTableDeclarationProvider();
    }

	@SuppressWarnings({ "hiding", "unchecked" })
	@Override
	public void setScrollPosition(IndexViewportState indexViewportState) {
		int fixedColumnCount;
		if (isInEditMode()) {
			fixedColumnCount = this.fixedColumnCount;
		} else {
			fixedColumnCount = viewModeFixedColumnCount;
		}
		_tableViewportState = ViewportStateConverter.getViewportState(indexViewportState,
			getTableDeclarationProvider().getTableDeclaration().getColumnNames(), fixedColumnCount);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IndexViewportState getScrollPosition() {
		return ViewportStateConverter.getIndexViewportState(_tableViewportState,
			getTableDeclarationProvider().getTableDeclaration().getColumnNames());
	}

	/**
	 * Creates the tree application model for the tree from the model of this
	 * {@link StructureEditComponent}. Is only called when the component supports the current model.
	 * 
	 * @return must not be <code>null</code>
	 */
	protected abstract TLTreeModel<N> createTreeModel();

	/**
	 * Returns a {@link NodeGroupInitializer} to initialize the {@link FormGroup} which represents
	 * the single rows, i.e. the returned {@link NodeGroupInitializer} is called to fill the 'row'-
	 * {@link FormGroup} with {@link FormMember} which are displayed in the cells of the row.
	 * 
	 * @return must not be <code>null</code>
	 */
	protected abstract NodeGroupInitializer getNodeGroupInitializer();

    /**
     * Checks the executability of the buttons and enables or disables them.
     */
    protected void handleButtonExecutability() {
        for (int i = 0, length = buttons.size(); i < length; i++) {
			ExecutableCommandField field = (ExecutableCommandField) buttons.get(i);
            Command command = field.getExecutable();
			if (command != Command.DO_NOTHING) {
				if (command instanceof StructureEditComponent.AbstractButtonCommand) {
					// can not use StructureEditComponent<?>.AbstractButtonCommand because Java6 can not compile this.
					AbstractButtonCommand cmd = (StructureEditComponent.AbstractButtonCommand) command;
					ExecutableState executable = cmd.isExecutable(this, null, Collections.<String, Object> emptyMap());
					if (executable.isExecutable()) {
						field.setVisible(true);
						field.setExecutable();
					} else if (executable.isHidden()) {
						field.setVisible(false);
					} else {
						field.setVisible(true);
						field.setNotExecutable(executable.getI18NReasonKey());
					}
				} else {
					field.setExecutable();
				}
			}
        }
    }

    @Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
        handleButtonExecutability();
    }

    /**
     * If a node gets collapsed and a child of this node is selected, the collapsed node
     * gets selected.
     *
     * @see com.top_logic.layout.tree.model.TreeModelListener#handleTreeUIModelEvent(com.top_logic.layout.tree.model.TreeModelEvent)
     */
    @Override
	public void handleTreeUIModelEvent(TreeModelEvent event) {
		if (event.getType() == TreeModelEvent.BEFORE_COLLAPSE) {
			TreeUIModel<N> treeModel = treeUIModel();
			N nodeToCollapse = (N) event.getNode();
			Set<? extends N> selection = getSelection();
			boolean found = false;
			if (!selection.isEmpty()) {
				// copy to avoid concurrent modification
				Object[] selectionCopy = selection.toArray();
				for (Object selectedObject : selectionCopy) {
					if (nodeToCollapse.equals(selectedObject)) {
						continue;
					}
					N selectedNode = (N) selectedObject;
					if (treeModel.createPathToRoot(selectedNode).contains(nodeToCollapse)) {
						removeFromSelection(selectedNode);
						found = true;
					}
				}
				if (found && !multiSelect) {
					setSingleSelection(nodeToCollapse);
				}

			}
        }
    }

	/**
	 * {@link TreeUIModel} of the {@link FormTree}.
	 */
	public final TreeUIModel<N> treeUIModel() {
		return formTree.getTreeModel();
	}

    /**
	 * Gets the name of the export template file.
	 */
	protected String getTemplateName() {
		return templateName;
	}

	/**
	 * Gets the download filename of the export
	 */
	protected String getDownloadName() {
		ResKey nameKey = downloadKey;
		if (nameKey == null) {
			nameKey = ResKey.fallback(getResPrefix().key("exportDownloadName"), I18NConstants.EXPORT_NAME);
		}
		return Resources.getInstance().getString(nameKey);
	}

	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map someArguments) {
		return new OfficeExportValueHolder(getTemplateName(), getDownloadName(), getExportValues(progressInfo), true);
    }

	/**
	 * Gets the export data to add to the export.
	 */
	protected Object getExportValues(DefaultProgressInfo progressInfo) {
		return ExcelExportSupport.newInstance().excelValuesFromTree(getFormTree().getTreeModel(), getTableDeclaration());
	}



    // node processing methods:

	/**
     * Checks whether the given node is allowed to get moved within the tree.
     *
     * @param aNode
     *            the node to get removed
     * @return <code>true</code>, if the node is allowed to get moved,
     *         <code>false</code> otherwise
     */
    protected boolean allowMoveNode(Object aNode) {
        return true;
    }

    /**
     * Checks whether the given node is allowed to get removed from the tree.
     *
     * @param aNode
     *            the node to get removed
     * @return <code>true</code>, if the node is allowed to get removed,
     *         <code>false</code> otherwise
     */
    protected boolean allowRemoveNode(Object aNode) {
        return true;
    }

    /**
     * Checks whether the given node is allowed to create a new child node.
     *
     * @param aParent
     *            the parent node of the new node to be created.
     * @return <code>true</code>, if the node is allowed to create a new child node,
     *         <code>false</code> otherwise
     */
	protected ExecutableState allowCreateChild(Object aParent) {
		return ExecutableState.EXECUTABLE;
    }

    /**
     * Checks whether the given child object can be added as child to the given
     * parent object. Subclasses should override this method to cover their own
     * business logic. The default is "always permitted".
     *
     * @param aPotentialParent
     *            the user object to be the parent
     * @param aPotentialChild
     *            the user object to add as child to the given parent node
     * @return <code>true</code> if the given child object can be added to the
     *         given parent object as child
     */
    protected boolean acceptChild(Object aPotentialParent, Object aPotentialChild) {
        return true;
    }


    /**
     * Moves the given node and adds it the given new parent as child at the given index.
     *
     * @param aNode
     *        the node to move
     * @param aNewParent
     *        the new parent of the node
     * @param aIndex
     *        the index in the new parents child list to add the node
     */
	protected void moveNode(N aNode, N aNewParent, int aIndex) {
        aNode.moveTo(aNewParent, aIndex);
    }

    /**
     * Removes the given node from its parents child list.
     *
     * @param node
     *        the node to remove
     */
	protected void removeNode(N node) {
		N parent = node.getParent();
		int index = parent.getIndex(node);
		parent.removeChild(index);
    }

	/**
	 * Create a new user object for the tree. This method should encapsulate all details of
	 * collecting the necessary data (e.g. handling dialogs etc.) and creating the actual object.
	 * Subclasses must override and implement the business logic. Note that this object should be a
	 * transient object, and the apply command handler should make this object persistent.
	 * 
	 * @param aParent
	 *        the parent user object that shall get a new child object.
	 * 
	 * @return a user object that has been populated with all necessary data, or <code>null</code>
	 *         if creation was canceled.
	 */
    protected Object createNewObject(Object aParent) {
        return null;
    }

    /**
     * Gets the Form tree.
     */
	public final FormTree getFormTree() {
        return formTree;
    }


	/**
	 * Gets the current visible nodes in the tree.
	 * 
	 * @return the current visible nodes in the tree as new modifiable Set. The iteration order is
	 *         the order the nodes are listened in the table.
	 */
	protected Set<N> getVisibleNodes() {
		TreeUIModel<N> treeModel = treeUIModel();
		N root = treeModel.getRoot();
		Set<N> result = new LinkedHashSet<>();
		if (rootVisible) {
			result.add(root);
		}
		findVisibleNodesRecursively(result, treeModel, root);
		return result;
	}

	private void findVisibleNodesRecursively(Set<N> result, TreeUIModel<N> treeModel, N node) {
		if (treeModel.isExpanded(node)) {
			for (N child : node.getChildren()) {
				result.add(child);
				findVisibleNodesRecursively(result, treeModel, child);
			}
		}
	}


    /**
	 * Gets the current selected node in the tree.
	 * 
	 * @return the current selected node in the tree; may be <code>null</code> if no node is
	 *         selected.
	 * @throws IllegalStateException
	 *         if component is in multi selection mode
	 */
	protected N getSingleSelection() {
    	if (multiSelect) {
			throw new IllegalStateException("Component is configured as multi select.");
    	}
    	return CollectionUtil.getFirst(getSelection());
    }

    /**
	 * Gets the current selected nodes in the tree.
	 * 
	 * @return the current selected nodes in the tree as unmodifiable Set view
	 */
	protected Set<N> getSelection() {
    	if (formTree == null) return Collections.emptySet();
		return (Set<N>) formTree.getSelectionModel().getSelection();
    }

    /**
	 * Gets the selected nodes in the tree combined to nodes with common parents mapped by their
	 * parents.
	 * 
	 * @return A map "node --&gt; set of selected children".
	 */
	protected Map<N, Set<N>> getSelectionParentMap() {
		return getSelectionParentMap(getSelection());
    }

    /**
	 * Combines the given nodes to nodes with common parents mapped by their parents.
	 * 
	 * @param elements
	 *        The elements to combine.
	 * @return A map "node --&gt; set of children".
	 */
	private Map<N, Set<N>> getSelectionParentMap(Collection<? extends N> elements) {
		Map<N, Set<N>> parentMap = new HashMap<>();
		for (N node : elements) {
			N parent = node.getParent();
    		// root cannot be moved
			if (parent != null) {
				MapUtil.addObjectToSet(parentMap, parent, node);
			}
    	}
    	return parentMap;
    }

    /**
	 * Sets the current selected node in the tree. Deselects all other nodes.
	 */
	protected void setSingleSelection(N selected) {
    	if (multiSelect) {
			throw new IllegalStateException("Component is configured as multi select.");
    	}
		if (formTree == null) {
			return;
		}
        SelectionModel selectionModel = formTree.getSelectionModel();
		selectionModel.clear();
		if (selected != null) {
			selectionModel.setSelected(selected, true);
            setDisplayed(selected);
        }
    }

    /**
     * Sets the current selected nodes in the tree. This selection replaces the current selection.
     */
	protected void setSelection(Set<N> selectedObjects) {
		if (formTree == null) {
			return;
		}
    	SelectionModel selectionModel = formTree.getSelectionModel();
		selectionModel.setSelection(selectedObjects);
		setDisplayed(selectedObjects);
    }

	/**
	 * Appends the given node to the selected elements.
	 * 
	 * @param node
	 *        the node to add to selection.
	 */
	protected void addToSelection(N node) {
		formTree.getSelectionModel().setSelected(node, true);
		setDisplayed(node);
	}

	/**
	 * Removes the given node from the selected elements.
	 * 
	 * @param node
	 *        the node to remove from selection.
	 */
	protected void removeFromSelection(N node) {
		formTree.getSelectionModel().setSelected(node, false);
	}


    /**
     * Marks the form context as changed.
     */
    protected void setTreeChangedFlag() {
        if (formTree == null) return;
        formTree.getParent().getField(FIELD_NAME_HIDDEN).setValue(CHANGED_VALUE);
    }

    /**
     * Expands all parents of the given node so that the given node is visible in the tree.
     *
     * @param node
     *        the node to set visible
     */
    protected void setDisplayed(N node) {
        if (node == null) return;
		TreeUIModel<N> treeModel = treeUIModel();
		N parent = node.getParent();
        while (parent != null) {
            treeModel.setExpanded(parent, true);
            parent = parent.getParent();
        }
    }

    /**
     * Expands all parents of the given nodes so that the given nodes are visible in the tree.
     *
     * @param nodes
     *        the nodes to set visible
     */
	protected void setDisplayed(Set<? extends N> nodes) {
		for (N node : nodes) {
    		setDisplayed(node);
    	}
    }

    /**
     * Compute the index of the given node in the child list of it's parent.
     *
     * @param node
     *        the node to determine the index for.
     * @return the index of the given node or -1, if the node or its parent is
     *         <code>null</code>.
     */
    protected int getChildIndex(N node) {
        if (node == null) return -1;
		N parent = node.getParent();
        return parent == null ? -1 : parent.getIndex(node);
    }





    // tree table commands:

    /**
     * Abstract command which can decide whether a button is executable or not.
     */
	public abstract class AbstractButtonCommand implements Command, ExecutabilityRule {

        @Override
		public HandlerResult executeCommand(DisplayContext aContext) {
            doExecute(aContext);
            setDisplayed(getSelection());
            handleButtonExecutability();
            return DefaultHandlerResult.DEFAULT_RESULT;
        }

        /**
		 * Executes the command. This method may assume valid input for its operation. Any checks on
		 * validity of input and whether this operation is allowed in this context should be done in
		 * {@link #isExecutable(LayoutComponent, Object, Map)} which will cause inapplicable
		 * commands to be de-activated.
		 *
		 * @see Command#executeCommand(DisplayContext)
		 */
        public abstract void doExecute(DisplayContext aContext);

    }

	/**
	 * Super class of commands that modifies the structure based on the current selection.
	 * 
	 * @since 5.7.4
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public abstract class StructureChangeCommand extends AbstractButtonCommand {

		@Override
		public final ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			ExecutableState result = acceptSelection();
			if (!result.isExecutable()) {
				return result;
			}
			if (getSelection().isEmpty()) {
				return NO_EXEC_EMPTY_SELECTION;
			}
			return isExecutable2(aComponent, someValues);
		}

		protected abstract ExecutableState isExecutable2(LayoutComponent aComponent, Map<String, Object> someValues);

		/**
		 * Checks whether the current selection is valid to execute this command.
		 * 
		 * @return Returns <code>true</code> if the selection was accepted, <code>false</code>
		 *         otherwise.
		 */
		public ExecutableState acceptSelection() {
			if (onlyOneLevelModify) {
				return checkSelectionForSameParents();
			}
			return ExecutableState.EXECUTABLE;
		}

		/**
		 * Checks whether each selected node has the same parent (that only elements in one level
		 * are selected).
		 */
		protected ExecutableState checkSelectionForSameParents() {
			N lastParent = null;
			for (N node : getSelection()) {
				N parent = node.getParent();
				if (parent == null) {
					// don't allow command for root node
					return NO_EXEC_ROOT_SELECTED;
				}
				if (lastParent == null) {
					lastParent = parent;
				} else if (!lastParent.equals(parent)) {
					return NO_EXEC_NODES_WITH_DIFFERENT_PARENTS_SELECTED;
				}
			}
			return ExecutableState.EXECUTABLE;
		}

	}

	/**
	 * Command to deselect all nodes in the tree.
	 */
	public class DeselectAllCommand extends AbstractButtonCommand {

		@Override
		public void doExecute(DisplayContext aContext) {
			getFormTree().getSelectionModel().clear();
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (getSelection().isEmpty()) {
				return NO_EXEC_EMPTY_SELECTION;
			} else {
				return ExecutableState.EXECUTABLE;
			}
		}

	}

    /**
	 * Command to select all visible nodes in the tree.
	 */
    public class SelectAllCommand extends AbstractButtonCommand {

    	/**
		 * Creates a new {@link SelectAllCommand}.
		 */
		public SelectAllCommand() {
    	}

    	@Override
    	public void doExecute(DisplayContext aContext) {
			setSelection(getVisibleNodes());
    	}

    	@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (!multiSelect) {
				// select more than one node only in multi select case.
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			if (getSelection().equals(getVisibleNodes())) {
				return NO_EXEC_ALREADY_ALL_NODES_SELECTED;
			} else {
				return ExecutableState.EXECUTABLE;
			}
    	}

    }

    /**
     * Command to select all unselected and deselect all selected elements.
     */
    public class InvertSelection extends AbstractButtonCommand {

    	@Override
    	public void doExecute(DisplayContext aContext) {
			Set<N> newSelection = getVisibleNodes();
			newSelection.removeAll(getSelection());
			setSelection(newSelection);
    	}

    	@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			return multiSelect ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
    	}

    }


    /**
     * Command to select all siblings of the selected elements.
     */
    public class SelectAllSiblings extends AbstractButtonCommand {

    	@Override
    	public void doExecute(DisplayContext aContext) {
			Set<N> newSelection = new HashSet<>(getSelection());
			for (N node : getSelectionParentMap().keySet()) {
        		newSelection.addAll(node.getChildren());
        	}
        	setSelection(newSelection);
    	}

    	@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (!multiSelect) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			Set<Entry<N, Set<N>>> entrySet = getSelectionParentMap().entrySet();
			if (entrySet.isEmpty()) {
				return NO_EXEC_EMPTY_SELECTION;
			}
			for (Entry<N, Set<N>> entry : entrySet) {
				N parent = entry.getKey();
				Set<N> selected = entry.getValue();
				List<? extends N> children = parent.getChildren();
				if (selected.size() != children.size()) {
					return ExecutableState.EXECUTABLE;
				}
        	}
			return NOT_EXEC_ALL_SIBBLINGS_ALREADY_SELECTED;
    	}

    }

    /**
     * Command to expand / collapse all nodes in the tree.
     */
    public class ExpandTreeCommand extends AbstractButtonCommand {

        private boolean expand;

        /**
         * Creates a new instance of this class.
         *
         * @param expand
         *        Indicator whether to expand (<code>true</code>) or collapse (
         *        <code>false</code>) all
         */
        public ExpandTreeCommand(boolean expand) {
            this.expand = expand;
        }

        @Override
        public void doExecute(DisplayContext aContext) {
			DefaultStructureTreeUIModel<N> treeModel =
				(DefaultStructureTreeUIModel<N>) formTree.getTreeApplicationModel();
            treeModel.setExpandAll(expand);
        }

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			return ExecutableState.EXECUTABLE;
		}

    }

    /**
     * Command to move a node up.
     */
	public class MoveUpCommand extends StructureChangeCommand {

        @Override
        public void doExecute(DisplayContext aContext) {
        	// handle each level for its own
			for (Entry<N, Set<N>> entry : getSelectionParentMap().entrySet()) {
				N parent = entry.getKey();
				Set<N> levelSelection = entry.getValue();
				List<? extends N> children = parent.getChildren();

        		boolean unselectedMoveableSeen = false;
        		for (int i = 0, length = children.size(); i < length; i++) {
					N child = children.get(i);
        			if (!canMove(child)) {
        				unselectedMoveableSeen = false;
					} else if (levelSelection.contains(child)) {
        				if (unselectedMoveableSeen) {
							moveNode(child, parent, i - 1);
        				}
					} else {
        				unselectedMoveableSeen = true;
        			}
        		}
        	}
        	setTreeChangedFlag();
        }

        /**
		 * Checks whether the given node can be moved up or down.
		 */
		protected boolean canMove(N node) {
			return allowMoveNode(node.getBusinessObject());
		}

		@Override
		public ExecutableState isExecutable2(LayoutComponent aComponent, Map<String, Object> someValues) {
			// handle each level for its own
			for (Entry<N, Set<N>> entry : getSelectionParentMap().entrySet()) {
				N parent = entry.getKey();
				Set<N> levelSelection = entry.getValue();
				List<? extends N> children = parent.getChildren();

        		boolean unselectedMoveableSeen = false;
        		for (int i = 0, length = children.size(); i < length; i++) {
					N child = children.get(i);
        			if (!canMove(child)) {
        				unselectedMoveableSeen = false;
					} else if (levelSelection.contains(child)) {
        				if (unselectedMoveableSeen) {
							return ExecutableState.EXECUTABLE;
        				}
					} else {
        				unselectedMoveableSeen = true;
        			}
        		}
        	}
			return NO_EXEC_NO_MOVES_POSSIBLE;
        }

    }

    /**
     * Command to move a node down.
     */
	public class MoveDownCommand extends StructureChangeCommand {

        @Override
        public void doExecute(DisplayContext aContext) {
        	// handle each level for its own
			for (Entry<N, Set<N>> entry : getSelectionParentMap().entrySet()) {
				N parent = entry.getKey();
				Set<N> levelSelection = entry.getValue();
				List<? extends N> children = parent.getChildren();

        		boolean unselectedMoveableSeen = false;
        		for (int i = children.size() - 1; i >= 0; i--) {
					N child = children.get(i);
        			if (!canMove(child)) {
        				unselectedMoveableSeen = false;
					} else if (levelSelection.contains(child)) {
        				if (unselectedMoveableSeen) {
							moveNode(child, parent, i + 1);
        				}
					} else {
        				unselectedMoveableSeen = true;
        			}
        		}
        	}
        	setTreeChangedFlag();
        }

        /**
		 * Checks whether the given node can be moved up or down.
		 */
		protected boolean canMove(N child) {
			return allowMoveNode(child.getBusinessObject());
		}

		@Override
		public ExecutableState isExecutable2(LayoutComponent aComponent, Map<String, Object> someValues) {
			// handle each level for its own
			for (Entry<N, Set<N>> entry : getSelectionParentMap().entrySet()) {
				N parent = entry.getKey();
				Set<N> levelSelection = entry.getValue();
				List<? extends N> children = parent.getChildren();

        		boolean unselectedMoveableSeen = false;
        		for (int i = children.size() - 1; i >= 0; i--) {
					N child = children.get(i);
        			if (!canMove(child)) {
        				unselectedMoveableSeen = false;
					} else if (levelSelection.contains(child)) {
        				if (unselectedMoveableSeen) {
							return ExecutableState.EXECUTABLE;
        				}
					} else {
        				unselectedMoveableSeen = true;
        			}
        		}
        	}
			return NO_EXEC_NO_MOVES_POSSIBLE;
        }


    }

    /**
     * Command to move a node one level lower in the tree.
     */
	public class MoveLeftCommand extends StructureChangeCommand {

        @Override
        public void doExecute(DisplayContext aContext) {
			TreeUIModel<N> treeModel = treeUIModel();
			Map<N, Set<N>> selectionParentMap = getSelectionParentMap();

        	// sort selectionParentMap by depth of the parents
			Map<Integer, Set<N>> parentDepthMap = new TreeMap<>();
			for (N node : selectionParentMap.keySet()) {
        		int depth = treeModel.createPathToRoot(node).size();
        		MapUtil.addObjectToSet(parentDepthMap, Integer.valueOf(depth), node);
        	}

        	// handle each level for its own, but parents before their children
			for (Set<N> entry : parentDepthMap.values()) {
				for (N parent : entry) {
					N grandParent = parent.getParent();
					if (grandParent == null) {
						/* First level node (parent is root). Move left would create a second root
						 * node. */
						continue;
					}
					Set<N> selected = selectionParentMap.get(parent);
					List<? extends N> children = parent.getChildren();
            		
            		for (int i = children.size() - 1; i >= 0; i--) {
						N child = children.get(i);
            			if (selected.contains(child)) {
							if (canMove(child, grandParent)) {
            					moveNode(child, grandParent, grandParent.getIndex(parent) + 1);
            				}
            			}
            		}
        		}
        	}
            setTreeChangedFlag();
        }

        /**
		 * Checks whether the given node can be moved left.
		 */
		protected boolean canMove(N node, N newParent) {
			Object businessObject = node.getBusinessObject();
			return allowMoveNode(businessObject) && acceptChild(newParent.getBusinessObject(), businessObject);
		}

		@Override
		public ExecutableState isExecutable2(LayoutComponent aComponent, Map<String, Object> someValues) {
			for (N node : getSelection()) {
				N parent = node.getParent();
				if (parent == null) {
					continue;
				}
				N grandParent = parent.getParent();
				if (grandParent == null) {
					continue;
				}
				if (canMove(node, grandParent)) {
					return ExecutableState.EXECUTABLE;
				}
			}
			return NO_EXEC_NO_MOVES_POSSIBLE;
        }

    }

    /**
     * Command to move a node one level higher in the tree.
     */
	public class MoveRightCommand extends StructureChangeCommand {

        @Override
        public void doExecute(DisplayContext aContext) {
        	// handle each level for its own
			for (Entry<N, Set<N>> entry : getSelectionParentMap().entrySet()) {
				N parent = entry.getKey();
				Set<N> selected = entry.getValue();
				List<? extends N> children = parent.getChildren();

				N sibling = null;
        		for (int i = 0, length = children.size(); i < length; i++) {
					N child = children.get(i);
        			if (selected.contains(child)) {
        				if (sibling != null) {
        					if (canMove(child, sibling)) {
        						moveNode(child, sibling, sibling.getChildCount());
								length--;
								i--;
							} else {
        						sibling = null;
        					}
        				}
					} else {
        				sibling = child;
        			}
        		}
        	}
            setTreeChangedFlag();
        }

        /**
		 * Checks whether the given node can be moved right.
		 */
		protected boolean canMove(N node, N newParent) {
			Object businessObject = node.getBusinessObject();
			return allowMoveNode(businessObject) && acceptChild(newParent.getBusinessObject(), businessObject);
		}

		@Override
		protected ExecutableState isExecutable2(LayoutComponent aComponent, Map<String, Object> someValues) {
			// handle each level for its own
			for (Entry<N, Set<N>> entry : getSelectionParentMap().entrySet()) {
				N parent = entry.getKey();
				Set<N> selected = entry.getValue();
				List<? extends N> children = parent.getChildren();

				N sibling = null;
        		for (int i = 0, length = children.size(); i < length; i++) {
					N child = children.get(i);
        			if (selected.contains(child)) {
        				if (sibling != null) {
        					if (canMove(child, sibling)) {
								return ExecutableState.EXECUTABLE;
							} else {
        						sibling = null;
        					}
        				}
					} else {
        				sibling = child;
        			}
        		}
        	}
			return NO_EXEC_NO_MOVES_POSSIBLE;
        }
    }

    /**
     * Removes the node.
     */
	public class RemoveCommand extends StructureChangeCommand {

        @Override
        public void doExecute(DisplayContext aContext) {
			final Object[] selectionCopy = getSelection().toArray();

			List<N> notRemovableObjects = null;
			for (Object nodeObject : selectionCopy) {
				/* nodeObject is taken from selection which is Set<N> */
				@SuppressWarnings("unchecked")
				N node = (N) nodeObject;
				if (!canRemove(node)) {
					if (notRemovableObjects == null) {
						notRemovableObjects = new ArrayList<>();
					}
					notRemovableObjects.add(node);
				}
			}
			if (notRemovableObjects != null) {
				Command continuation = new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						internalRemove(selectionCopy);
						return HandlerResult.DEFAULT_RESULT;
					}
				};
				openConfirmDialog(aContext, notRemovableObjects, continuation);
			} else {
				internalRemove(selectionCopy);
			}
        }

		/**
		 * Opens the confirm dialog to inform the user that not all elements can be removed.
		 * 
		 * @param context
		 *        The {@link DisplayContext} to open dialog in.
		 * @param notRemovableObjects
		 *        The elements that can not be removed.
		 * @param continuation
		 *        The {@link Command} to execute in case other objects must be removed.
		 */
		protected void openConfirmDialog(DisplayContext context, final List<N> notRemovableObjects, Command continuation) {
			CommandModel yes = MessageBox.button(ButtonType.YES, continuation);
			CommandModel no = MessageBox.button(ButtonType.NO);
			DisplayValue title = new ResourceText(MessageType.CONFIRM.getTitleKey());
			HTMLFragment message = new HTMLFragment() {

				@Override
				public void write(DisplayContext innerContext, TagWriter out) throws IOException {
					out.writeText(innerContext.getResources().getString(I18NConstants.CAN_NOT_REMOVE_ELEMENTS_1));
					out.beginTag(HTMLConstants.UL);
					for (N node : notRemovableObjects) {
						out.beginTag(HTMLConstants.LI);
						ResourceRenderer.NO_LINK_INSTANCE.write(innerContext, out, node.getBusinessObject());
						out.endTag(HTMLConstants.LI);
					}
					out.endTag(HTMLConstants.UL);
					out.writeText(innerContext.getResources().getString(I18NConstants.CAN_NOT_REMOVE_ELEMENTS_2));
				}
			};
			LayoutData layout =
				DefaultLayoutData.newLayoutData(dim(300, DisplayUnit.PIXEL), dim(200, DisplayUnit.PIXEL));
			MessageBox.confirm(context.getWindowScope(), layout, true, title, message, yes, no);
		}

		void internalRemove(Object[] selectionCopy) {
			for (Object nodeObject : selectionCopy) {
				/* nodeObject is taken from selection which is Set<N> */
				@SuppressWarnings("unchecked")
				N node = (N) nodeObject;
                if (canRemove(node)) {
					N parent = node.getParent();
					int index = parent.getIndex(node);
                	removeNode(node);
					// select new element in single select mode
					if (!multiSelect) {
						setNewSingleSelection(parent, index);
					}
                }
        	}
        	setTreeChangedFlag();
        }

		/**
		 * Sets a new single selection in single selection mode after deleted a node.
		 * 
		 * @param parent
		 *        The parent node of the deleted node
		 * @param index
		 *        The index of the deleted node before deletion.
		 */
		protected void setNewSingleSelection(N parent, int index) {
			if (parent.isLeaf()) {
				// don't select root if it is not visible
				if (rootVisible || parent.getParent() != null) {
					setSingleSelection(parent);
				}
			} else {
				int selectionIndex;
				int numberChildren = parent.getChildCount();
				if (index < numberChildren) {
					selectionIndex = index;
				} else {
					selectionIndex = numberChildren - 1;

				}
				N newSelection = parent.getChildAt(selectionIndex);
				setSingleSelection(newSelection);
			}
		}

        /**
		 * Checks whether the given node can be removed.
		 */
		protected boolean canRemove(N node) {
            // can't remove root
			return node.getParent() != null && allowRemoveNode(node.getBusinessObject());
		}

		@Override
		protected ExecutableState isExecutable2(LayoutComponent aComponent, Map<String, Object> someValues) {
			for (N node : getSelection()) {
				if (canRemove(node)) {
					return ExecutableState.EXECUTABLE;
				}
			}
			return NO_EXEC_NO_SELECTED_NODE_REMOVABLE;
        }
    }

    /**
     * Creates a new node in the tree.
     */
    public class CreateCommand extends AbstractButtonCommand {

        @Override
        public void doExecute(DisplayContext aContext) {
			N theSelected = CollectionUtil.getFirst(getSelection());
			N theNewNode = createNewNode(theSelected);
			if (theNewNode != null) {
                setDisplayed(theNewNode);
                setTreeChangedFlag();
            }
        }

		/**
		 * Creates a new node containing a new user object for the tree.
		 *
		 * @param selected
		 *        the parent node that shall get a new child node.
		 *
		 * @return a new node, or <code>null</code> if creation was canceled.
		 */
		protected N createNewNode(N selected) {
        	Object newObject = createNewObject(selected.getBusinessObject());
        	return newObject != null ? selected.createChild(newObject) : null;
        }

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Set<N> selection = getSelection();
			int size = selection.size();
			if (size == 0) {
				return NO_EXEC_EMPTY_SELECTION;
			}
			if (size > 1) {
				return NO_EXEC_MORE_THAN_ONE_NODE_SELECTED;
			}
			return allowCreateChild(CollectionUtil.getFirst(selection).getBusinessObject());
        }
    }





    // other inner classes

     /**
     * Adds technical column field to form tree.
     */
    public class StructureEditNodeGroupInitializer implements NodeGroupInitializer {

    	private final NodeGroupInitializer innerInitializer;

    	/**
    	 * Creates a new {@link StructureEditNodeGroupInitializer}.
    	 */
    	public StructureEditNodeGroupInitializer(NodeGroupInitializer innerInitializer) {
    		this.innerInitializer = innerInitializer;
    	}

		@Override
		public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
			innerInitializer.createNodeGroup(formTree, nodeGroup, node);
			nodeGroup.set(StructureEditComponentConstants.NODE_PROPERTY, node);
		}

    }

    /**
     * Listener for selection fields to update selection
     */
	public static class StructureEditSelectionValueListener implements ValueListener, ValueVetoListener {
    	
		private final Object node;
    	private final SelectionModel selectionModel;
    	
    	/**
    	 * Creates a new {@link StructureEditSelectionValueListener}.
    	 */
		public StructureEditSelectionValueListener(Object node, SelectionModel selectionModel) {
    		this.node = node;
    		this.selectionModel = selectionModel;
    	}

		@Override
		public void checkVeto(FormField field, Object newValue) throws VetoException {
			if (!selectionModel.isSelectable(node))
				throw new SilentVetoException();
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			selectionModel.setSelected(node, Utils.getbooleanValue(newValue));
		}

    }
	
	/**
	 * Renders the given object as part of a {@link SelectionModel}.
	 * 
	 * @since 5.7.4
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class SelectionPartRenderer implements Renderer<Object> {

		private final SelectionModel _selectionModel;

		/**
		 * Creates a new {@link SelectionPartRenderer}.
		 * 
		 * @param selectionModel
		 *        The {@link SelectionModel} where the rendered values are part of.
		 */
		public SelectionPartRenderer(SelectionModel selectionModel) {
			_selectionModel = selectionModel;
		}

		@Override
		public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
			new SelectionPartControl(_selectionModel, value).write(context, out);
		}
	}

	private static final class TreeNodeAccessor implements Accessor<FormGroup> {

		private final Accessor<FormGroup> _innerAccessor;

		private final String _nodeProperty;

		public TreeNodeAccessor(Accessor<FormGroup> innerAccessor, String nodeProperty) {
			_innerAccessor = innerAccessor;
			_nodeProperty = nodeProperty;
		}

		@Override
		public Object getValue(FormGroup object, String property) {
			if (_nodeProperty.equals(property)) {
				return object.get(StructureEditComponentConstants.NODE_PROPERTY);
			}
			return _innerAccessor.getValue(object, property);
		}

		@Override
		public void setValue(FormGroup object, String property, Object value) {
			if (_nodeProperty.equals(property)) {
				throw new UnsupportedOperationException("Can not set node.");
			}
			_innerAccessor.setValue(object, property, value);

		}
	}
    /**
     * Default TableDeclarationProvider for this component. Subclasses may extend from this class.
     */
    public class StructureEditTableDeclarationProvider implements TableDeclarationProvider {

        @Override
		public TableDeclaration getTableDeclaration() {
			Renderer<?> theHeaderRenderer = getHeaderRenderer();
            ResourceProvider theNodeResourceProvider = getNodeResourceProvider();
			Accessor theNodeAccessor = getNodeAccessor();
			if (ArrayUtil.contains(getColumns(), COLUMN_SELECTION)) {
				theNodeAccessor = new TreeNodeAccessor(theNodeAccessor, COLUMN_SELECTION);
            }

            // columns
            DefaultTableDeclaration tableDec = new DefaultTableDeclaration(getResPrefix(), MetaResourceProvider.INSTANCE);

            for (String column : getColumns()) {
                DefaultColumnDeclaration columnDeclaration = null;
                if (DefaultTableDeclaration.DEFAULT_COLUMN_NAME.equals(column)) {
                    columnDeclaration = new DefaultColumnDeclaration(ColumnDeclaration.DEFAULT_COLUMN);
					columnDeclaration.setHeaderType(DefaultColumnDeclaration.RENDERED_HEADER);
					columnDeclaration.setHeaderRenderer(theHeaderRenderer);
				} else if (COLUMN_SELECTION.equals(column)) {
					columnDeclaration = new DefaultColumnDeclaration(ColumnDeclaration.RENDERED_COLUMN);
					columnDeclaration.setRenderer(new SelectionPartRenderer(getSelectionModel()));
					columnDeclaration.setSelectable(false);
					columnDeclaration.setHeaderType(DefaultColumnDeclaration.NO_HEADER);
				} else {
                    columnDeclaration = new DefaultColumnDeclaration(DefaultColumnDeclaration.CONTROL_COLUMN);
                    columnDeclaration.setControlProvider(getControlProvider(column));
					columnDeclaration.setHeaderType(DefaultColumnDeclaration.RENDERED_HEADER);
					columnDeclaration.setHeaderRenderer(theHeaderRenderer);
                }
				setStyle(columnDeclaration, getHeaderColumnStyle(column), getContentColumnStyle(column), getColumnSize(column));
                tableDec.addColumnDeclaration(column, columnDeclaration);
            }

            tableDec.setHasHeader(theHeaderRenderer != null);
            tableDec.setResourceProvider(theNodeResourceProvider);
            tableDec.setAccessor(theNodeAccessor);
            return tableDec;
        }

        /**
		 * The header style for this column, may be <code>null</code>.
		 */
		protected String getHeaderColumnStyle(String aColumn) {
			return null;
		}

		/**
		 * The content style for this column, may be <code>null</code>.
		 */
		protected String getContentColumnStyle(String aColumn) {
			return null;
		}

		/** 
		 * Adapt the different information to the given column description.
		 * 
		 * @param aCD       The column description to be adapted, must not be <code>null</code>.
		 * @param aStyle    The special content style for that column, may be <code>null</code>.
		 * @param aWidth    The width to be set, may be <code>null</code>.
		 */
		protected final void setStyle(DefaultColumnDeclaration aCD, String aStyle, String aWidth) {
		    this.setStyle(aCD, aStyle, aStyle, aWidth);
		}

		/** 
		 * Adapt the different information to the given column description.
		 * 
		 * @param aCD              The column description to be adapted, must not be <code>null</code>.
		 * @param aHeaderStyle     The special header style for that column, may be <code>null</code>.
		 * @param aContentStyle    The special content style for that column, may be <code>null</code>.
		 * @param aWidth           The width to be set, may be <code>null</code>.
		 */
		protected final void setStyle(DefaultColumnDeclaration aCD, String aHeaderStyle, String aContentStyle, String aWidth) {
        	aCD.setHeaderStyle(aHeaderStyle);
        	aCD.setStyle(aContentStyle);
			aCD.setWidth(aWidth);
		}

		/**
         * Creates the default renderer for table headers; may be <code>null</code>.
         */
		protected Renderer<?> getHeaderRenderer() {
            return headerRenderer;
        }

        /**
         * Creates the resource provider for the elements in the default (tree) column.
         */
        protected ResourceProvider getNodeResourceProvider(){
            return nodeResourceProvider;
        }

        /**
         * Creates the default accessor to get the cell values for the tree table.
         * The Accessor must be able to handle FormGroups, which have form members
         * named like the columns.
         */
        protected Accessor getNodeAccessor() {
            return nodeAccessor;
        }

        /**
         * Gets the default control provider for the given column.
         */
        protected ControlProvider getControlProvider(String column) {
            return controlProvider;
        }

        /**
		 * Gets the columns for the given tree table. The special column named
		 * {@link DefaultTableDeclaration#DEFAULT_COLUMN_NAME} will be used for the nodes of the
		 * tree.
		 */
        public String[] getColumns() {
			String columns;
			if (isInEditMode()) {
				columns = StructureEditComponent.this.columns;
			} else {
				columns = StructureEditComponent.this.viewModeColumns;
			}
			return StringServices.toArray(columns, ',');
        }

        /**
         * Gets the fixed column size for the given column in pixel.
         */
        public String getColumnSize(String column) {
			if (COLUMN_SELECTION.equals(column)) {
				return "35px;";
			}
            if (DefaultTableDeclaration.DEFAULT_COLUMN_NAME.equals(column)) {
                return "250px;";
            }
            return "150px;";
        }
    }

	/**
	 * Wraps the {@link #newTableDeclarationControlProvider()} with an {@link ControlProvider}
	 * drawing also {@link ErrorControl}s to all fields.
	 */
	protected ControlProvider getDefaultControlProvider() {
		return ValueWithErrorControlProvider.newInstance(newTableDeclarationControlProvider());
	}

	/**
	 * The {@link ControlProvider} used by this component.
	 */
	protected ControlProvider newTableDeclarationControlProvider() {
		return new StructureEditControlProvider();
	}

    /**
	 * The {@link ControlProvider} used by this component. It uses {@link SelectionControl}s instead
	 * of {@link SelectControl}s for {@link SelectField}s.
	 */
	public class StructureEditControlProvider extends DefaultFormFieldControlProvider {
    	
        @Override
		public Control visitSelectField(SelectField member, Void arg) {
            SelectionControl control = new SelectionControl(member);
            if (isInEditMode()) {
                control.setOptionRenderer(ResourceRenderer.NO_LINK_INSTANCE);
            }
            return control;
        }
    }


    /**
     * @see com.top_logic.layout.basic.component.ControlComponent#invalidate()
     */
    @Override
	public void invalidate() {
        super.invalidate();
        this.renderingControl=null;
    }
    
    @Override
	protected void becomingVisible() {
		super.becomingVisible();
		resetScrollState();
	}

	/**
	 * Resets the view port state.
	 */
	protected final void resetScrollState() {
		_tableViewportState = new ViewportState();
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		cleanUpFormContext(oldModel);
		resetScrollState();
	}

	@Override
	public Control getRenderingControl() {
		if (!supportsInternalModel(getModel())) {
			return new ConstantControl<DisplayValue>(new ResourceText(getResPrefix().key("noModel"))) {

				@Override
				protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(SPAN);
					writeControlAttributes(context, out);
					out.endBeginTag();
					getModel().append(context, out);
					out.endTag(SPAN);
				}
			};
    	} else {
    		if (this.renderingControl == null) {
    			this.renderingControl = new FormGroupControl(getFormContext(), getControlProvider(), getTemplate(), getResPrefix());
    		}
    		return renderingControl;
    	}
    }

	/**
	 * Returns the {@link ControlProvider} which is used to produce controls for
	 * the {@link FormMember member} of the {@link #getFormContext() form
	 * context}.
	 */
    protected ControlProvider getControlProvider() {
    	return new DefaultFormFieldControlProvider() {
    		
    		@Override
			public Control visitFormTree(FormTree member, Void arg) {
				OnVisibleControl result = createOnVisibleControl(member);
				final StructureEditTreeControl tree =
					new StructureEditTreeControl(member, StructureEditComponent.this);
				result.addChild(tree);
				result.setRenderer(new DefaultSimpleCompositeControlRenderer(HTMLConstants.DIV) {
					@Override
					protected void writeControlTagAttributes(DisplayContext context, TagWriter out,
							CompositeControl control) throws IOException {
						super.writeControlTagAttributes(context, out, control);
						LayoutControlRenderer.writeLayoutConstraintInformation(100, DisplayUnit.PERCENT, out);
						LayoutControlRenderer.writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
					}
				});
				return result;
    		}
    	};
    }

	/**
	 * Returns a template which specifies the rendering structure of the
	 * {@link FormContext}.
	 */
	protected Document getTemplate() {
		return TEMPLATE;
	}

	@Override
	public SelectionModel getSelectionModel() {
		return formTree.getSelectionModel();
	}

}
