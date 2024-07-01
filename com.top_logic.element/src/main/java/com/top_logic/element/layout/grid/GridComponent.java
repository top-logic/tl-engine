/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.webfolder.ui.clipboard.ShowClipboardCommandHandler;
import com.top_logic.element.layout.grid.GridBuilder.GridHandler;
import com.top_logic.element.layout.meta.DefaultFormContextModificator;
import com.top_logic.element.layout.meta.FormContextModificator;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.UpdateFactory;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyCode;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.KeySelector;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.Command.CommandChain;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.SingletonCheckScope;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.ComponentChannel.ChannelValueFilter;
import com.top_logic.layout.channel.RowsChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareAlgorithmHolder;
import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.component.model.NoSelectionModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.ChangeStateListener;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.WarningsDialog;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableModelUtils;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.layout.table.component.ColumnsChannel;
import com.top_logic.layout.table.component.ComponentRowSource;
import com.top_logic.layout.table.component.ComponentTableConfigProvider;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.layout.table.display.ColumnAnchor;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableModelColumnsEvent;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.tree.component.StructureModelBuilder;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.component.WithSelectionPath;
import com.top_logic.layout.tree.model.TreeViewConfig;
import com.top_logic.mig.html.AbstractRestrainedSelectionModel;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelConfig;
import com.top_logic.mig.html.SelectionModelFactory;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InEditModeExecutable;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.TL5Types;

/**
 * Grid component that allows editing of a single row of a table.
 * 
 * @see GridComponent.Config
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GridComponent extends EditComponent implements
		SelectableWithSelectionModel,
		ControlRepresentable, SelectionVetoListener, CompareAlgorithmHolder,
		ComponentRowSource, WithSelectionPath {

	/**
	 * Configuration options for {@link GridComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends EditComponent.Config, ColumnsChannel.Config, TreeViewConfig, SelectionModelConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "grid";

		@Override
		@ClassDefault(GridComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Name(GridComponent.XML_CONFIG_MODIFIER_CLASS)
		PolymorphicConfiguration<FormContextModificator> getModifier();

		@Name(GridComponent.XML_CONFIG_INNER_APPLY_HANDLER)
		PolymorphicConfiguration<GridApplyHandler> getGridApplyHandlerClass();

		@Name(GridComponent.XML_CONFIG_ROW_SECURITY_PROVIDER)
		PolymorphicConfiguration<GridRowSecurityObjectProvider> getRowSecurityProviderClass();

		@Name(GridComponent.XML_CONFIG_EXCLUDE_COLUMNS)
		@StringDefault("")
		String getExcludeColumns();

		@Name(GridComponent.XML_CONFIG_OPEN_IN_EDIT)
		@BooleanDefault(DEFAULT_OPEN_IN_EDIT)
		boolean getOpenInEdit();

		@Name(GridComponent.XML_CONFIG_EDIT_COMPONENT_NAME)
		ComponentName getEditComponentName();

		/** Flag, whether to create marker fields. */
		@Name(GridComponent.XML_CONFIG_SHOW_MARKER_FIELDS)
		@BooleanDefault(DEFAULT_SHOW_MARKER_FIELDS)
		boolean getShowMarkerFields();

		/**
		 * Configuration option whether detail dialog opener should be displayed.
		 */
		@Name(GridComponent.XML_CONFIG_SHOW_DETAIL_OPENER)
		@BooleanDefault(true)
		boolean getShowDetailOpener();

		/** flag indicating whether to show clipboard commands or not */
		@Name(GridComponent.XML_CONFIG_SHOW_CLIPBOARD_COMMANDS)
		@BooleanDefault(true)
		Boolean getShowClipboardCommands();

		@Name(XML_CONFIG_STRUCTURE_NAME)
		String getStructureName();

		@Name(XML_CONFIG_ELEMENT_TYPES)
		String[] getElementTypes();

		/**
		 * Returns the factory to create programmatic {@link TableConfigurationProvider} for the
		 * configured {@link GridComponent}.
		 * 
		 * @see com.top_logic.layout.table.component.TableComponent.Config#getComponentTableConfigProvider()
		 */
		@InstanceFormat
		@InstanceDefault(GridComponentTableConfigProvider.class)
		@Name(TableComponent.Config.COMPONENT_TABLE_CONFIG_PROVIDER_NAME)
		ComponentTableConfigProvider getComponentTableConfigProvider();

		@Override
		@BooleanDefault(false)
		boolean getSaveClosesDialog();

		@Override
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();

		/** flag indicating whether to show technical column or not */
		@Name(GridComponent.XML_CONFIG_SHOW_TECHNICAL_COLUMN)
		@BooleanDefault(true)
		Boolean getAddTechnicalColumn();

		@Override
		Map<String, ? extends GridTableConfig> getTables();

		/**
		 * This property is only relevant when {@link #getModelBuilder()} is a
		 * {@link TreeModelBuilder}.
		 * 
		 * @see TreeViewConfig#isRootVisible()
		 */
		@Override
		@BooleanDefault(false)
		boolean isRootVisible();

		/**
		 * This property is only relevant when {@link #getModelBuilder()} is a
		 * {@link TreeModelBuilder}.
		 * 
		 * @see TreeViewConfig#getExpandRoot()
		 */
		@Override
		boolean getExpandRoot();

		/**
		 * This property is only relevant when {@link #getModelBuilder()} is a
		 * {@link TreeModelBuilder}.
		 * 
		 * @see TreeViewConfig#getExpandSelected()
		 */
		@Override
		boolean getExpandSelected();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditComponent.Config.super.modifyIntrinsicCommands(registry);

			if (getShowMarkerFields() && getAddTechnicalColumn()) {
				registry.registerButton(ClearSelectedCheckboxes.COMMAND_ID);
				if (getShowClipboardCommands()) {
					registry.registerButton(AddSelectedToClipboard.COMMAND_ID);
					registry.registerButton(ShowClipboardCommandHandler.COMMAND_ID);
				}
				registry.registerButton(SelectAllCheckboxes.COMMAND_ID);
			}
		}
	}

    /** A class for modifying the form members during creation of an input row. */
    public static final String XML_CONFIG_MODIFIER_CLASS = "modifier";

    /** Apply handler configuration key which is used to store changes in a row. */
    public static final String XML_CONFIG_INNER_APPLY_HANDLER = "gridApplyHandlerClass";

    /** Row Security object provider configuration key which is used to get object to check security for. */
    public static final String XML_CONFIG_ROW_SECURITY_PROVIDER = "rowSecurityProviderClass";

    /** The columns to be displayed (mandatory). */
    public static final String XML_CONFIG_EXCLUDE_COLUMNS = "excludeColumns";
    
    /** Name of the KO element types supported by this grid (mandatory). */
    public static final String XML_CONFIG_ELEMENT_TYPES = "elementTypes";
    
    /** Name of the structure to be supported by this grid (mandatory). */
    public static final String XML_CONFIG_STRUCTURE_NAME = "structureName";

    /** Configuration flag, if opened dialog has to be in edit mode. */
    public static final String XML_CONFIG_OPEN_IN_EDIT  = "openInEdit";
    
    /** Configuration name for the class to use as a table filter provider */
    public static final String XML_CONFIG_NO_FILTER = "noFilter";

    /** Configuration name for the flag, if attributes of sub elements should be selectable too. */
    public static final String XML_CONFIG_INCLUDE_SUB_METAELEMENTS = "includeSubMetaElements";

    /**
	 * @see Config#getEditComponentName()
	 */
    public static final String XML_CONFIG_EDIT_COMPONENT_NAME = "editComponentName";
    
    /** Configuration flag indicating whether the clipboard buttons should be shown in the grid */
    public static final String XML_CONFIG_SHOW_CLIPBOARD_COMMANDS = "showClipboardCommands";

	/** Configuration flag indicating whether the technical column should be shown in the grid */
	public static final String XML_CONFIG_SHOW_TECHNICAL_COLUMN = "showTechnicalColumn";

    /** Configuration name for the flag, whether marker check boxes should be generated. */
    public static final String XML_CONFIG_SHOW_MARKER_FIELDS = "showMarkerFields";

	/** Configuration name for the flag, whether detail opener buttons should be generated. */
	public static final String XML_CONFIG_SHOW_DETAIL_OPENER = "showDetailOpener";

    /** The name in the form context the table field can be accessed with. */
    public static final String FIELD_TABLE = "fieldTable";

    /** This Column will contain all the commands shown in the first column of the Grid */ 
    public static final String COLUMN_TECHNICAL = "technical";

    /** The row object represented by a row form group. */
	public static final Property<Object> PROP_ATTRIBUTED = TypedAnnotatable.property(Object.class, "attributed");

    /** Default value for {@link #XML_CONFIG_OPEN_IN_EDIT}. */
    private static final boolean DEFAULT_OPEN_IN_EDIT = false;

    /** Default value for {@link #XML_CONFIG_SHOW_MARKER_FIELDS}. */
    private static final boolean DEFAULT_SHOW_MARKER_FIELDS = true;

    private static final String NEW_OBJECT_MARKER_FIELD = "_newObject";

	/**
	 * @see #channels()
	 */
	@SuppressWarnings("hiding")
	protected static final Map<String, ChannelSPI> CHANNELS =
		channels(EditComponent.CHANNELS, SelectionChannel.INSTANCE, RowsChannel.INSTANCE, ColumnsChannel.INSTANCE,
			WithSelectionPath.SELECTION_PATH_SPI);

	private static final ComponentChannel.ChannelListener COLUMNS_LISTENER = new ComponentChannel.ChannelListener() {

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			GridComponent grid = (GridComponent) sender.getComponent();
			if (grid.hasFormContext()) {
				TableModelUtils.setKnownColumns(grid.getViewModel(), unsafeCast(newValue));
			}
		}
	};

	private static final String ROW_DOMAIN = null;

	GridHandler<FormGroup> _handler;

	private String _focusColumn;

    /** The columns to be excluded from view. */
	Set<String> excludeColumns;

	private Object tokenBase;

	boolean _isSelectionValid;

	private boolean _rowsValid;

	private TableControl renderingControl;

	/** Unique ID for new created rows (when inserting a new row). */
    private long counter = 0;

    /** @see #getElementNames() */
	private String[] nodeTypes;

    /** Modifying instance for the form members during creation of an input row. */
    private FormContextModificator modifier;

    /** Handler which is used to apply changes in a row. */
    private GridApplyHandler applyHandler;

    /** Gets the object which shall be used to check security for editing a row. */
    private GridRowSecurityObjectProvider rowSecurityProvider;

    /** Local cache for getting the form group representing an object. */
    private Map<Object, FormGroup> groupMap = new HashMap<>();

    /** Flag, if opened dialog has to be in edit mode. */
    private boolean openInEdit;

	/**
	 * Flag, whether to create opener for detail dialogs.
	 * 
	 * @see Config#getShowDetailOpener()
	 */
	private boolean _showDetailOpener;

	/** Stores the multi selection model (row marking model). */
	private final SelectionModel _selectionModel;

    /**
     * Name of the dialog component to open when execute the goto. 
     */
	private final ComponentName _editComponentName;

	/**
	 * Listener which switches the active image of the apply command (
	 * {@link #getApplyCommandHandler()}) from enabled to disabled an vice versa, depending on the
	 * {@link FormGroup#isChanged() changed state} of the {@link FormGroup} representing the
	 * currently selected object.
	 * 
	 * Not <code>null</code> after
	 * {@link #modelForCommand(CommandHandler, LayoutComponent)}
	 */
	private ApplyComandImageChange _applyListener;
	
	private ComponentTableConfigProvider _componentTableConfigProvider;

	private CompareAlgorithm _compareAlgorithm;

	private Collection<?> _expansionUserModel;

	private boolean _addTechnicalColumn;

	/**
	 * Create a new GridComponent from XML.
	 */
    public GridComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
		this.modifier = createModifier(context, someAttrs.getModifier());
		this.applyHandler = (someAttrs.getGridApplyHandlerClass() == null) ? DefaultGridApplyHandler.INSTANCE
			: context.getInstance(someAttrs.getGridApplyHandlerClass());
		this.rowSecurityProvider =
			(someAttrs.getRowSecurityProviderClass() == null) ? DefaultGridRowSecurityObjectProvider.INSTANCE
				: context.getInstance(someAttrs.getRowSecurityProviderClass());
        this.excludeColumns        = Collections.unmodifiableSet(CollectionUtil.toSet(StringServices.toArray(someAttrs.getExcludeColumns())));
        this.openInEdit            = someAttrs.getOpenInEdit();
		this._editComponentName = someAttrs.getEditComponentName();
		_showDetailOpener = someAttrs.getShowDetailOpener();
		this.nodeTypes = someAttrs.getElementTypes();
		_selectionModel = initSelectionModel(someAttrs);
		_componentTableConfigProvider = someAttrs.getComponentTableConfigProvider();
		_addTechnicalColumn = someAttrs.getAddTechnicalColumn();
	}

	private FormContextModificator createModifier(InstantiationContext context,
			PolymorphicConfiguration<FormContextModificator> modifierConfig) {
		FormContextModificator result = context.getInstance(modifierConfig);
		return result != null ? result : DefaultFormContextModificator.INSTANCE;
	}

	@Override
	public synchronized AttributeFormContext getFormContext() {
		return (AttributeFormContext) super.getFormContext();
	}

	@Override
	protected ModelBuilder createBuilder(InstantiationContext context, BuilderComponent.Config attr)
			throws ConfigurationException {
		ModelBuilder builder = super.createBuilder(context, attr);

		ModelBuilder gridBuilder = createGridBuilder(builder);

		if (gridBuilder instanceof AbstractTreeGridBuilder) {
			initTreeGridBuilder((Config) attr, (AbstractTreeGridBuilder<?>) gridBuilder);
		}

		return gridBuilder;
	}

	private void initTreeGridBuilder(Config config, AbstractTreeGridBuilder<?> gridBuilder) {
		gridBuilder.setRootVisible(config.isRootVisible());
		gridBuilder.setExpandRoot(config.getExpandRoot());
		gridBuilder.setExpandSelected(config.getExpandSelected());
		gridBuilder.adjustSelectionWhenCollapsing(config.adjustSelectionWhenCollapsing());
	}

	private ModelBuilder createGridBuilder(ModelBuilder builder) {
		if (builder == null) {
			return null;
		}
		if (builder instanceof GridBuilder) {
			@SuppressWarnings("unchecked")
			GridBuilder<FormGroup> result = (GridBuilder<FormGroup>) builder;
			return result;
		}
		if (builder instanceof ListModelBuilder) {
			return new TableGridBuilder<FormGroup>((ListModelBuilder) builder);
		}
		if (builder instanceof TreeModelBuilder<?>) {
			return new TreeGridBuilder<>(unsafeCast(builder));
		}
		if (builder instanceof StructureModelBuilder<?>) {
			return new StructureGridBuilder<FormGroup>(unsafeCast(builder));
		}
		
		throw new ConfigurationError("Only " + ListModelBuilder.class.getName()
			+ ", " + StructureModelBuilder.class.getName() + ", or "
			+ GridBuilder.class.getName()
			+ " instances can be configured as model builders in a grid, seen: "
			+ builder.getClass().getName());
	}
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void writeBody(ServletContext context, HttpServletRequest request,
    		HttpServletResponse response, TagWriter out) throws IOException, ServletException {

    	getRenderingControl().write(DefaultDisplayContext.getDisplayContext(request), out);
    }
    
	@Override
	public CompareAlgorithm getCompareAlgorithm() {
		return _compareAlgorithm;
	}

	@Override
	public void setCompareAlgorithm(CompareAlgorithm algorithm) {
		_compareAlgorithm = algorithm;
	}

	/**
	 * Retrieves the table renderer from table configuration, or creates an instance on demand.
	 *
	 * @return {@link TableRenderer} of this grid
	 */
	protected ITableRenderer getTableRenderer(TableData tableData) {
		return tableData.getTableModel().getTableConfiguration().getTableRenderer();
	}

    /**
     * The form context will contain the table field ({@link #FIELD_TABLE}).
     */
    @Override
	public FormContext createFormContext() {
		AttributeFormContext theContext = new AttributeFormContext("context", this.getResPrefix());

        theContext.addMember(this.createTableField(theContext));

		if (_isSelectionValid && isInEditMode()) {
			createFields(getSelected(), theContext);
		}

        return theContext;
    }

    @Override
	public void removeFormContext() {
		if (isVisible() && _handler != null) {
			_expansionUserModel = _handler.getExpansionState();
		}

		if (getLockHandler().hasLock()) {
			getLockHandler().releaseLock();
        }

		ToolBar toolBar = getToolBar();
		if (toolBar != null) {
			if (hasFormContext()) {
				TableField tableField = getTableField(getFormContext());
				TableUtil.removeTableButtons(toolBar, tableField);
				/* Reset the selection model of the *old* table field to ensure, that all listeners
				 * added internally by the table field are removed from our model. */
				tableField.setSelectionModel(NoSelectionModel.INSTANCE);
			}
		}

		this.resetRenderingControl();

		super.removeFormContext();
    }

    @Override
    public void invalidate() {
		invalidateSelection();

        super.invalidate();
    }

	/**
	 * Sets the flag to validate the selection.
	 */
	public void invalidateSelection() {
		_isSelectionValid = false;
	}

    @Override
    public boolean isModelValid() {
		return super.isModelValid()
			&& _isSelectionValid
			&& _rowsValid
			&& _focusColumn == null
			&& hasFormContext();
    }

    @Override
    public boolean validateModel(DisplayContext aContext) {
		boolean superSentEvents = super.validateModel(aContext);
		boolean eventsSent = false;

		if (hasFormContext()) {
			if (!_isSelectionValid) {
				_isSelectionValid = true;

				setUISelectionPaths(getSelectedPathsCollection());
			}
		} else {
			getFormContext();
			/* When the rows are invalid and the FormContext is recreated, the stored UI selection
			 * still contains the *old* FormGroups for the rows. Creating the FormContext create new
			 * FormGroup, so that the groups in the selection model can not be found. Therefore the
			 * selection must be updated. */
			setUISelectionPaths(getSelectedPathsCollection());
		}

		_focusColumn = null;
		if (!_rowsValid) {
			_rowsValid = true;
			fireRowsChanged();
		}

		return eventsSent || superSentEvents;
    }

	/**
	 * Returns the first selectable displayed row if the {@link #getDefaultSelection()} is enabled,
	 * otherwise <code>null</code>.
	 * 
	 * <p>
	 * If the table is empty (has no rows) then <code>null</code> is returned.
	 * </p>
	 */
	public Object getDefaultSelection() {
		if (((Config) getConfig()).getDefaultSelection()) {
			TableViewModel tableViewModel = getViewModel();
			TableModel tableModel = getTableField(getFormContext()).getTableModel();

			for (int rowIndex = 0; rowIndex < tableViewModel.getRowCount(); rowIndex++) {
				FormGroup group = getFormGroup(tableViewModel, rowIndex);

				Object rowObject = getRowObject(group);
				Object internalRow = _handler.getFirstTableRow(group);

				if (_selectionModel.isSelectable(internalRow) && tableModel.containsRowObject(internalRow)) {
					return rowObject;
				}
			}
		}
		
		return null;
	}

	/**
	 * Initializes the multi selection model (row marking model).
	 * 
	 * @param config
	 *        The configuration of this {@link GridComponent}.
	 */
	protected SelectionModel initSelectionModel(Config config) {
		AbstractRestrainedSelectionModel selectionModel =
			(AbstractRestrainedSelectionModel) config.getSelectionModelFactory().newSelectionModel(this);
		selectionModel.addSelectionListener(_selectionListener);
		selectionModel
			.setSelectionFilter(FilterFactory.or(selectionModel.getSelectionFilter(), GridComponent::isTransient));
		return selectionModel;
	}

	/**
	 * The rows currently being marked.
	 */
	@Override
	public final SelectionModel getSelectionModel() {
		return _selectionModel;
	}

    @Override
	public Control getRenderingControl() {
    	if (this.renderingControl == null) {
    		TableField   tableField   = this.getTableField(getFormContext());
			ITableRenderer renderer = getTableRenderer(tableField);
			TableControl theControl = createTableControl(tableField, renderer, null);
            this.renderingControl = theControl;
        }
        return this.renderingControl;
    }

    /** 
     * Allow subclasses to change the way the control is created.
     */
	protected TableControl createTableControl(TableField tableField, ITableRenderer aRenderer, String theConfigKey) {
		TableControl result = TableTag.createTableControl(tableField, aRenderer, true);
		return result;
    }

	/**
	 * Removes the fields of the currently selected objects, if exist.
	 */
	private void dropRowFields(Collection<?> models) {
		if (!isInEditMode()) {
			return;
		}

		if (hasFormContext()) {
			AttributeFormContext theFormContext = getFormContext();
			AttributeUpdateContainer theContainer = theFormContext.getAttributeUpdateContainer();

			for (Object object : models) {
				FormGroup row = getRowGroup(object);
				if (row != null) {
					removeFields(object, row, theContainer);
					updateTableRow(row, false);
				}
			}
		}

	}

    /**
	 * Name of the component used as detail view for a row.
	 * 
	 * @return may be <code>null</code> when there is no such edit component.
	 */
	public ComponentName getEditComponentName() {
		return _editComponentName;
	}

    @Override
	protected CommandModel modelForCommand(CommandHandler command, Map<String, Object> arguments, LayoutComponent aComponent) {
		CommandModel theModel = super.modelForCommand(command, arguments, aComponent);

		if (command.equals(this.getApplyCommandHandler())) {
			this.setButtonImages(theModel, Icons.SAVE_BUTTON_ICON, Icons.SAVE_BUTTON_ICON_DISABLED);
			/*
			 * set ImageButtonRenderer explicitly because ButtonRenderer is
			 * default which replaces the whole DOM element when image changes.
			 * If such an replacement occurs the command may not be executed,
			 * e.g. an input field was changed and the button is clicked
			 * directly (without previously leaving the field). In this case (if
			 * the server is fast enough) the DOM element of the button is
			 * replaced before the onClick-event occurs
			 */
			theModel.set(ButtonControl.BUTTON_RENDERER, ImageButtonRenderer.INSTANCE);
			this._applyListener = new ApplyComandImageChange(theModel, Icons.SAVE_BUTTON_MODIFIED, Icons.SAVE_BUTTON_ICON);
            // initially the button looks like not executable
            this._applyListener.updateImage(false);

        }
        else if (command instanceof OpenModalDialogCommandHandler) {
			ComponentName theDialogName = ((OpenModalDialogCommandHandler) command).getOpenToDialogName();
            LayoutComponent theDialog     = this.getDialog(theDialogName);
			DialogInfo theInfo = theDialog.getDialogInfo();

			this.setButtonImages(theModel, theInfo.getImage(), theInfo.getDisabledImage());
        }

        return theModel;
    }

    /** 
     * Set the given images to the given command model.
     * 
     * @param    aModel             The model to set the images for, must not be <code>null</code>.
     * @param    anImage            The image for the command, may be <code>null</code>.
     * @param    anDisabledImage    The image for the disabled state, may be <code>null</code>.
     */
	protected void setButtonImages(CommandModel aModel, ThemeImage anImage, ThemeImage anDisabledImage) {
        if (anImage != null) { 
			aModel.setImage(anImage);
        }

        if (anDisabledImage != null) { 
			aModel.setNotExecutableImage(anDisabledImage);
        }
    }

    @Override
	protected void registerAdditionalNonConfigurableCommands() {
        super.registerAdditionalNonConfigurableCommands();

        for (CommandHandler command : gridBuilder().getCommands()) {
			registerButtonCommand(command);
		}
    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		getLockHandler().releaseLock();
		invalidate();
	}

	@Override
	protected Set<? extends TLStructuredType> getTypesToObserve() {
		Set<TLStructuredType> result = new HashSet<>(getConfiguredTypes());
		if (getBuilder() instanceof TreeGridBuilder) {
			result.addAll(((TreeGridBuilder<?>) getBuilder()).getTypesToObserve());
		}
		return result;
	}

	/** The types of the {@link TLObject}s that can be displayed. */
	protected final Set<TLClass> getConfiguredTypes() {
		String[] elementNames = getElementNames();
		int count = elementNames.length;
		if (count == 0) {
			return Collections.emptySet();
		}
		Set<TLClass> nodeClasses = CollectionUtil.newSet(count);
		String structureName = ((Config) getConfig()).getStructureName();
		if (!StringServices.isEmpty(structureName)) {
			for (String elementName : elementNames) {
				String typeSpec = TL5Types.nodeTypeSpec(structureName, elementName);
				TLType contentType = TLModelUtil.findType(typeSpec);
				nodeClasses.add((TLClass) contentType);
			}

		} else {
			for (String nodeType : elementNames) {
				TLType contentType = TLModelUtil.findType(nodeType);
				nodeClasses.add((TLClass) contentType);
			}
		}
		return nodeClasses;
	}

	@Override
	protected void handleTLObjectCreations(Stream<? extends TLObject> created) {
		if (!hasFormContext()) {
			// No adaption of list, because it still has to be created.
			return;
		}
		boolean changes = gridBuilder().handleTLObjectCreations(this, created);
		if (changes) {
			invalidateSelection();
		}
	}

	@Override
	@Deprecated
	protected boolean receiveModelCreatedEvent(Object aModel, Object changedBy) {
		// Ignore. Only creations of TLObjects are relevant. And they use the new "handleTLObjectCreations" method.
		return false;
    }

	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object someChangedBy) {
		if (!hasFormContext()) {
			// No adaption of list, because it still has to be created.
			return false;
		}
		gridBuilder().receiveModelChangedEvent(this, aModel);
		invalidateSelection();
		if (!supportsRow(aModel)) {
			return false;
		}

		FormGroup formGroup = getRowGroup(aModel);
		if (formGroup == null && this.isRelevant(aModel, someChangedBy) && isVisible()) {
			this.invalidate();
			formGroup = getRowGroup(aModel);
		}

		if (formGroup != null) {
			if (isVisible()) {
				if ((getSelectionModel().isSelected(_handler.getFirstTableRow(formGroup)))
					&& getLockHandler().hasLock()) {
					AttributeFormContext theContext = getFormContext();
					AttributeUpdateContainer theContainer = theContext.getAttributeUpdateContainer();
					Object object = getRowObject(formGroup);
					removeFields(object, formGroup, theContainer);
					createFields(aModel);
				}
				updateRowStructure(formGroup);
			} else {
				invalidate();
			}
		}

        return false;
    }

    @Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		Collection<?> selected = this.getSelectedCollection();

		if (!Collections.disjoint(selected, models)) {
			dropRowFields(models);
			invalidateSelection();
		}

		if (models.contains(getModel())) {
			this.invalidate();
			super.receiveModelDeletedEvent(models, changedBy);
			return true;
		}

        if (this.hasFormContext()) {
			Set<FormGroup> rowGroups = getRowGroups(models);
			if (!rowGroups.isEmpty()) {
				if (isVisible()) {
					dropRows(rowGroups);
				} else {
					invalidate();
				}
			}
        }

		return super.receiveModelDeletedEvent(models, changedBy);
    }

	@Override
	public boolean receiveDialogEvent(Object aDialog, Object anOwner, boolean isOpen) {
		boolean receiveDialogEvent = super.receiveDialogEvent(aDialog, anOwner, isOpen);
		if (isOpen) {
			dropRowFields(getSelectedCollection());
		} else {
			Set<LayoutComponent> dialogComponents = getDialogSupport().getOpenedDialogs().keySet();
			if (!openedAsDialog()) {
				if (dialogComponents.isEmpty()) {
					reactivateSelection();
				}
			} else {
				LayoutComponent dialog = getDialogTopLayout();
				assert dialog != null : "We are opened as dialog, so we have a outermost dialog layout.";
				Iterator<LayoutComponent> dialogs = dialogComponents.iterator();
				while (dialogs.hasNext()) {
					// Select top leven dialog in opened dialogs
					LayoutComponent openedDialog = dialogs.next();
					if (dialog == openedDialog) {
						if (!dialogs.hasNext()) {
							// GridComponent becomes top level dialog
							reactivateSelection();
						} else {
							// there is still a dialog opened.
						}
					}
					break;
				}
			}
		}
		return receiveDialogEvent;
	}

    /**
     * The token context of this component is not its model but the attributed
     * represented by the currently selected row in the {@link TableField}.
     *
     * @return the object that is currently used as a base for the token
     *         handling if {@link #tokenBase} is set, super otherwise.
     */
    @Override
	public Object getTokenContextBase() {
        if(this.tokenBase != null) {
            return this.tokenBase;
        }

        return super.getTokenContextBase();
    }

    /**
     * Return the columns defined as default for this component.
     *
     * @return    The configured default columns.
     */
    public List<String> getDefaultColumnNames() {
		return getTableField(getFormContext()).getTableModel().getTableConfiguration().getDefaultColumns();
    }

    /** 
     * Shall the opened dialog be in edit mode.
     * 
     * @return    <code>true</code> if dialog has to be in edit mode.
     */
    public boolean openInEdit() {
        return this.openInEdit;
    }

    /**
     * Shall marker fields be created.
     */
    public boolean showMarkerFields() {
		return ((Config) getConfig()).getShowMarkerFields();
    }

    /**
	 * Shall detail dialog opener be displayed.
	 */
	public boolean showDetailOpener() {
		return _showDetailOpener;
	}

	/**
     * Returns all FormGroups.
     */
    public List<FormGroup> getAllVisibleFormGroups() {
		return addVisibleFormGroups(new ArrayList<>());
    }

	private <T extends Collection<? super FormGroup>> T addVisibleFormGroups(T result) {
		if (this.hasFormContext()) {
			TableViewModel viewModel = getViewModel();
            int rows = viewModel.getRowCount();
            for (int i = 0; i < rows; i++) {
            	result.add(getFormGroup(viewModel, viewModel.getApplicationModelRow(i)));
            }
        }
		return result;
	}

    /**
	 * The sorted list of {@link FormGroup}s for the rows that are both visible and selected.
	 * 
	 * @return The requested list, never <code>null</code>. May be immutable.
	 * 
	 * @see #getDisplayedSelection()
	 */
	public List<FormGroup> getVisibleMarkedFormGroups() {
		if (this.hasFormContext()) {
			return _handler.sortGridRows(getDisplayedSelection());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * The {@link FormGroup}s for the rows that are both visible and selected.
	 * 
	 * @return A new, mutable and resizable {@link Set}. Never null. Never contains null.
	 * 
	 * @see #getVisibleMarkedFormGroups()
	 */
	public Set<FormGroup> getDisplayedSelection() {
		Set<FormGroup> displayedRows = getDisplayedGridRows();
		Set<FormGroup> selectedRows = getSelectedFormGroupsUnsorted();
		return CollectionFactory.set(CollectionUtil.intersection(displayedRows, selectedRows));
	}

	/**
	 * The {@link FormGroup}s for the visible rows.
	 * 
	 * @return A new, mutable and resizable {@link Set}. Never null. Never contains null.
	 */
	public Set<FormGroup> getDisplayedGridRows() {
		HashSet<FormGroup> displayedGridRows = new HashSet<>();
		for (Object tableRow : getDisplayedInternalGridRows()) {
			displayedGridRows.add(_handler.getGridRow(tableRow));
		}
		return displayedGridRows;
	}

	private List<?> getDisplayedInternalGridRows() {
		return getViewModel().getDisplayedRows();
	}

	/**
	 * Return an unmodifiable list containing the form groups which have a marker set, regardless of
	 * their visibility state.
	 * 
	 * @return The requested list, never <code>null</code>.
	 */
    public List<FormGroup> getMarkedFormGroups() {
    	if (this.hasFormContext()) {
			Set<FormGroup> selection = getSelectedFormGroupsUnsorted();
			List<FormGroup> sortedSelection = _handler.sortGridRows(selection);
			return sortedSelection;
    	} else {
    		return Collections.emptyList(); 
    	}
    }

	/**
	 * Set all marked check boxes to unselected, regardless of their current visibility.
	 */
	public void clearAllMarkedCheckboxes() {
		getSelectionModel().clear();
	}

	/**
	 * Set all marked and visible check boxes to unselected.
	 */
	public void clearAllVisibleMarkedCheckboxes() {
		Set<?> newSelection = new HashSet<>(getSelectionModel().getSelection());
		newSelection.removeAll(getDisplayedInternalGridRows());
		getSelectionModel().setSelection(newSelection);
	}

    /**
     * Set all visible check boxes to selected.
     */
    public void selectAllVisibleCheckboxes() {
		setUISelectionPaths(addDisplayedPaths(new HashSet<>()));
    }

    /** 
     * Return the form group representing the given model.
     * @param    aModel    The model to get the form group for, must not be <code>null</code>.
     *
     * @return   The requested form group or <code>null</code> if no group found.
     */
    public final FormGroup getRowGroup(Object aModel) {
        return this.groupMap.get(aModel);
    }

	/**
	 * Get {@link FormGroup}s from row objects.
	 * 
	 * @param rowObjects
	 *        the row objects
	 * @return all {@link FormGroup}s in a Set
	 */
	public final Set<FormGroup> getRowGroups(Collection<?> rowObjects) {
		if (rowObjects == null) {
			return Collections.emptySet();
		}
		switch (rowObjects.size()) {
			case 0:
				return Collections.emptySet();
			case 1:
				return CollectionUtil.singletonOrEmptySet(getRowGroup(rowObjects.iterator().next()));
			default:
				Set<FormGroup> rows = new HashSet<>();
				for (Object object : rowObjects) {
					FormGroup formGroup = getRowGroup(object);
					if (formGroup == null) {
						continue;
					}
					rows.add(formGroup);
				}
				return rows;
		}

	}

    /** 
     * Return the names of the elements supported by this grid.
     * 
     * This information can be used in the {@link ListModelBuilder} for a generic
     * {@link ListModelBuilder#getModel(Object, LayoutComponent)} approach.
     * 
     * @return   The requested names of the supported elements, be <code>null</code>.
     */
    public String[] getElementNames() {
        return this.nodeTypes;
    }

    /**
	 * Update the form context and set the given object as new selection in it.
	 * 
	 * Therefore the old selected form group must be changed (remove form fields if there are any)
	 * and the new selected form group must be activated (form field have to be created if it is
	 * allowed).
	 * 
	 * @param warningsDisabled
	 *        Whether to disable warnings before save.
	 * 
	 * @return <code>true</code> if storing the old values and changing to the new form context
	 *         succeeds.
	 */
    public HandlerResult updateFormContext(boolean warningsDisabled) {
		HandlerResult result = store(warningsDisabled);
        if (!result.isSuccess()) {
        	return result;
        }
        
        // Note: Storing values may change the selection in case of a newly created object.
		createFields();

        return HandlerResult.DEFAULT_RESULT;
    }

	final HandlerResult store(boolean warningsDisabled) {
		Collection<?> oldSelection = getSelectedCollection();
		if (oldSelection.isEmpty()) {
        	return HandlerResult.DEFAULT_RESULT;
        }
        
		for (Object object : oldSelection) {
			FormGroup group = getRowGroup(object);
			if (group == null || !group.isChanged()) {
				removeFields(object, group, getFormContext().getAttributeUpdateContainer());
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		return this.storeRowValues(oldSelection, warningsDisabled);
	}

    /**
	 * Store changes made to the currently selected row.
	 * 
	 * @param rowObjects
	 *        The {@link TLObject}s to be stored.
	 * @param warningsDisabled
	 *        Whether to disable warnings before save.
	 */
	public HandlerResult storeRowValues(Collection<?> rowObjects, boolean warningsDisabled) {
		SelectionModel selectionModel = getSelectionModel();

		for (Object object : rowObjects) {
			FormGroup formGroup = getRowGroup(object);
			if (formGroup == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			AttributeFormContext theContext = getFormContext();
			AttributeUpdateContainer theContainer = theContext.getAttributeUpdateContainer();
			if (!isValid(object)) {
				removeFields(object, formGroup, theContainer);

				return HandlerResult.DEFAULT_RESULT;
			}

			try {
				boolean needsCommit = theContext.isChanged() || isTransient(object);
				if (needsCommit) {
					if (!theContext.checkAll()) {
						HandlerResult error = new HandlerResult();
						AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, error);
						error.setErrorTitle(I18NConstants.ERROR_CANNOT_STORE_VALUES);
						return error;
					}

					boolean checkWarnings = !warningsDisabled && checkWarnings();
					if (checkWarnings && theContext.hasWarnings()) {
						final HandlerResult suspended = HandlerResult.suspended();
						WarningsDialog.openWarningsDialog(getWindowScope(),
							com.top_logic.layout.form.component.I18NConstants.APPLY,
							theContext, AbstractApplyCommandHandler.resumeContinuation(suspended));
						return suspended;
					}

					try (Transaction tx = getKnowledgeBase(object).beginTransaction()) {
						if (isTransient(object)) {
							Object createdObject = ((NewObject) object).create(this, formGroup);
							tx.commit();

							// Exchange transient with persistent object.
							this.groupMap.remove(object);
							setRowModel(formGroup, createdObject);
							this.groupMap.put(createdObject, formGroup);

							// Exchange row objects.
							if (selectionModel.isSelected(_handler.getFirstTableRow(formGroup))) {
								boolean before = _isSelectionValid;

								Set<List<? extends Object>> newSelection = getSelectedPathsCollection()
									.stream()
									.map(path -> {
										int idx = path.indexOf(object);
										if (idx < 0) {
											return path;
										}
										List<Object> newPath = new ArrayList<>(path.size());
										newPath.addAll(path.subList(0, idx));
										newPath.add(createdObject);
										newPath.addAll(path.subList(idx + 1, path.size()));
										return newPath;
									})
									.collect(Collectors.toSet());
								updateSelectionPathsChannel(newSelection);

								// Note: When a selection change is pending, a mode switch is does
								// not update the form context. Since this is not a real selection
								// change, the selection must stay valid to drop fields, if a save
								// operation is pending.
								_isSelectionValid = before;
							}
						} else {
							getApplyHandler().storeChanges(this, object, formGroup);
							tx.commit();

							this.fireModelModifiedEvent(object, this);
						}
					}
				}

				removeFields(object, formGroup, theContainer);

				// Note: If the row was saved, a change event will occur later on
				// that notifies about the structure change.
				updateTableRow(formGroup, false);
			} catch (RuntimeException ex) {
				return error(ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;

    }
    
	@Override
	public void checkVeto(SelectionModel selectionModel, Object newSelectedRow, SelectionType selectionType)
			throws VetoException {
		if (!hasFormContext()) {
			return;
		}
		FormContext formContext = this.getFormContext();


		Collection<?> currentSelection = getSelectedCollection();
		if (currentSelection.isEmpty()) {
			return;
		}
		if (selectionRemainsUnchanged(currentSelection, newSelectedRow, selectionType)) {
			return;
		}
		for (Object oldSelected : currentSelection) {
			if (oldSelected == null) {
				continue;
			}
			
			final FormGroup group = getRowGroup(oldSelected);
			if (group == null || !group.isChanged()) {
				continue;
			}

			boolean needsCommit = formContext.isChanged() || isTransient(oldSelected);
			if (needsCommit) {
				if (!formContext.checkAll()) {
					VetoException veto = new VetoException() {
						@Override
						public void process(WindowScope window) {
							HandlerResult error = new HandlerResult();
							AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, error);
							error.setErrorTitle(I18NConstants.ERROR_CANNOT_STORE_VALUES);
							GridComponent.this.openErrorDialog(error);
						}
					};

					throw veto;
				}

				boolean checkWarnings = checkWarnings() && !AbstractApplyCommandHandler.warningsDisabledTemporarily();
				if (checkWarnings && formContext.hasWarnings()) {
					VetoException veto = new VetoException() {
						@Override
						public void process(WindowScope window) {
							WarningsDialog.openWarningsDialog(window,
								com.top_logic.layout.form.component.I18NConstants.APPLY,
								formContext, AbstractApplyCommandHandler.confirmContinuation(getContinuationCommand()));
						}
					};

					throw veto;
				}
			}
		}
	}

	private boolean selectionRemainsUnchanged(Collection<?> currentSelection, Object selectedRow,
			SelectionType selectionType) {
		switch (selectionType) {
			case SINGLE:
				if (currentSelection.contains(getBusinessObjectFromInternalRow(selectedRow))) {
					// Click on the currently selected row, actually no change.
					return  currentSelection.contains(getBusinessObjectFromInternalRow(selectedRow));
				}
				return false;
			case TOGGLE_SINGLE:
				// Selection changes
				return false;
			case AREA:
			case TOGGLE_AREA:
				// The currently selected element remains selected. But it may be that new elements
				// are selected, therefore it is not safe to say "selection remains untouched".
				return false;
		}
		throw new UnreachableAssertion("Unhandled " + SelectionType.class.getName() + ": " + selectionType);
	}

    /**
	 * Initializes the creation of a new object.
	 * 
	 * @param typeName
	 *        Type name compatible with {@link ResourceProvider#getType(Object)} of the new object
	 *        being created.
	 * @param type
	 *        The type of the object to be created.
	 * @param createHandler
	 *        The handler that processes the creation.
	 * @param position
	 *        The location where the creation should happen.
	 * @param container
	 *        The container of the created object.
	 * @param model
	 *        The target model of the command, see
	 *        {@link AbstractCreateCommandHandler#createObject(LayoutComponent, Object, FormContainer, Map)}.
	 */
	public void startCreation(String typeName, TLClass type, CreateFunction createHandler, ContextPosition position,
			TLObject container, Object model) {
		if (type.isAbstract()) {
			Logger.warn("Starting creation of abstract type '" + type
				+ "'. This is only possible with legacy create function that allocates an object of a different type that was originally requested.",
				new StackTrace(), GridComponent.class);
		}
		setEditMode();
		NewObject creation = new NewObject(typeName, type, createHandler, container, model);
    	
    	Object newTableRow = _handler.createRow(container, position, creation);
		setUISelectionPaths(Collections.singleton(getRowObjectPath(newTableRow)));
		focusFirstElementOfSelectedRow();
    }
    
	private void focusFirstElementOfSelectedRow() {
		Collection<?> selection = getSelectedCollection();
		switch (selection.size()) {
			case 0:
				// Nothing selected
				break;
			case 1:
				focus(firstVisibleAttribute(getViewModel(), getMetaElement(selection.iterator().next())));
				break;
			default:
				// Not a unique selection
				break;
		}
	}

	/**
	 * First visible column name that represents an attribute in the given row type.
	 */
	private String firstVisibleAttribute(TableViewModel aViewModel, TLClass rowType) {
		String result = null;
		for (String column : aViewModel.getHeader().getColumnNames()) {
			if (MetaElementUtil.hasMetaAttribute(rowType, column)) {
				result = column;
				break;
			}
		}
		return result;
	}

	/**
	 * Stores a focus request for the given column.
	 */
	void focus(String columnName) {
		_focusColumn = columnName;
	}

	/** 
     * Removes all form fields in the current selected row and creates them new.  
     */
    void renewSelectedRowGroup() {
		if (!isInEditMode()) {
			return;
		}
		Collection<?> selectedObjects = getSelectedCollection();
		if (selectedObjects.isEmpty()) {
    		return;
    	}
    
        // remove all fields
		AttributeFormContext formContext = getFormContext();
        AttributeUpdateContainer updateContainer = formContext.getAttributeUpdateContainer();

		for (Object object : selectedObjects) {
			removeFields(object, getRowGroup(object), updateContainer);
		}
		createFields();
    }

	/**
	 * Add {@link FormField}s for the new selected objects and store the attribute values for the
	 * old objects that are no longer selected.
	 * 
	 * @return {@link HandlerResult} eventually holding a problem during storing the values.
	 */
	public HandlerResult storeAttributeValuesAndAddFields(Set<?> oldSelection, Set<?> newSelection) {
		HandlerResult result;
		if (!CollectionUtils.isEqualCollection(oldSelection, newSelection)) {
			result = storeRowValues(getRowObjects(oldSelection), true);

			if (result.isSuccess()) {
				createFields(getRowObjects(newSelection));

				// Manually trigger nested validation run, to gain possibility of handling
				// events
				// (e.g. object creation, object change), which were generated by storing
				// the attributed values.
				getMainLayout().processGlobalEvents();
			}
		} else {
			result = HandlerResult.DEFAULT_RESULT;
		}
		return result;
	}

	private HandlerResult error(Throwable ex) {
		HandlerResult error = new HandlerResult();
		error.setException(ex instanceof TopLogicException ? (TopLogicException) ex
			: new TopLogicException(com.top_logic.util.I18NConstants.INTERNAL_ERROR, ex));
		return error;
	}

	public final void removeFields(Object theSelectedObject, FormGroup theGroup, AttributeUpdateContainer theContainer) {
		this.removeFormFieldsFromGroup(theSelectedObject, theGroup, theContainer);
	}

    /**
	 * Remove the form fields from the given group and container.
	 *
	 * This is needed when the row selection or column selection has changed.
	 *
	 * @param rowObject
	 *        The attributed to remove the fields for, must not be <code>null</code>.
	 * @param formGroup
	 *        The form group to remove the values in, may be <code>null</code>.
	 * @param updateContainer
	 *        The form container to remove the values in, must not be <code>null</code>.
	 */
	protected void removeFormFieldsFromGroup(Object rowObject, FormGroup formGroup,
			AttributeUpdateContainer updateContainer) {
        if ((formGroup != null) && isTransient(rowObject)) {
        	formGroup.removeMember(NEW_OBJECT_MARKER_FIELD);
        }

		TLObject object = toAttributed(rowObject);
		TLFormObject overlay = updateContainer.getOverlay(object, null);
		if (overlay != null) {
			TLClass type = (TLClass) overlay.getType();
			modifier.clear(this, type, object, updateContainer, formGroup);
		}
		updateContainer.clear();

		if (formGroup != null) {
			TableViewModel viewModel = getViewModel();
			for (String columnName : viewModel.getColumnNames()) {
				formGroup.removeMember(columnName);
			}
		}
    
		getLockHandler().releaseLock();
    }

    /**
     * Return the table field held by this component.
     *
     * @return    The requested table field or <code>null</code> if no form context initialized.
     */
    protected TableField getTableField(FormContext aContext) {
        return (TableField) aContext.getMember(GridComponent.FIELD_TABLE);
    }

    /** 
     * Try to reactivate the former selection.
     * 
     * This method will be called, when an edit dialog has been closed and the former
     * selection must be reactivated now.
     */
    protected void reactivateSelection() {
		if (!isInEditMode()) {
			return;
		}

		if (isVisible()) {
			createFieldsForValidSelection();
		}
	}

	/**
	 * If {@link FormContext} is available, this method creates the fields for the currently
	 * selected object.
	 * 
	 * This must not be done if selection is currently invalid, because then validation happens in
	 * {@link #validateModel(DisplayContext)}.
	 */
	private void createFieldsForValidSelection() {
		if (_isSelectionValid && this.hasFormContext()) {
			AttributeFormContext theFormContext = getFormContext();

            // Release tokenContext as selection may have changed.
			if (getLockHandler().getLock() != null) {
                AttributeUpdateContainer theContainer = theFormContext.getAttributeUpdateContainer();

				for (Object object : getSelectedCollection()) {
					FormGroup theGroup = this.getRowGroup(object);
					removeFields(object, theGroup, theContainer);
				}
            }
			createFields();
        }
    }

    /**
     * Check, if the given model is relevant for this component.
     *
     * Currently this will only be asked in {@link #receiveModelChangedEvent(Object, Object)}
     * if the model is supported but cannot be found in the form group.
     *
     * @param    aModel       The model to be inspected, must not be <code>null</code>.
     * @param    changedBy    The changing component (may be MainLayout).
     * @return   <code>true</code> if it has to be displayed in this grid.
     */
    protected boolean isRelevant(Object aModel, Object changedBy) {
        return false;
    }

    /**
	 * Set the given model paths as new selection.
	 * 
	 * @param selectionPaths
	 *        The object paths to select. Must not be <code>null</code>.
	 */
	protected void setUISelectionPaths(Collection<? extends List<?>> selectionPaths) {
		_handler.setUISelectionPaths(selectionPaths);
	}

	/**
	 * Return the form group for the given row number.
	 * 
	 * @param aViewModel
	 *        The view model to get the form group from, must not be <code>null</code>.
	 * @param aApplModelRow
	 *        The row in the application model.
	 * @return The requested form group or <code>null</code> when no matching form group found.
	 */
    protected FormGroup getFormGroup(TableViewModel aViewModel, int aApplModelRow) {
        return _handler.getGridRow(getTableRow(aViewModel, aApplModelRow));
    }

	private Object getTableRow(TableViewModel aViewModel, int aApplModelRow) {
		return aViewModel.getApplicationModel().getRowObject(aApplModelRow);
	}

    /** 
     * Retrieve the business object displayed in a given view row.
     * 
     * @param    aViewModel       The view model to get the attributed from, must not be <code>null</code>.
     * @param    aViewModelRow    The row of the view model to get the model for.
     * @return   The attributed displayed in the row, or <code>null</code> if the row does not exist.
     */
    protected Object getRowObject(TableViewModel aViewModel, int aViewModelRow) {
        FormGroup theGroup = this.getFormGroup(aViewModel, aViewModel.getApplicationModelRow(aViewModelRow));

		return getRowObject(theGroup);
    }

	/**
	 * Retrieves the business object from the {@link FormGroup}.
	 * 
	 * @param formGroup
	 *        Is allowed to be null.
	 * @return The business object, usually a {@link TLObject}. Null, if the parameter is null.
	 */
	public static final Object getRowObject(FormGroup formGroup) {
		return ((formGroup != null) ? formGroup.get(PROP_ATTRIBUTED) : null);
	}

	/**
	 * Retrieves the business object from the {@link FormGroup}.
	 * 
	 * 
	 * @param formGroups
	 *        Is not allowed to be or contain null.
	 * @return The business objects, usually {@link TLObject}s. A new, mutable and resizable
	 *         {@link LinkedHashSet}. Never null.
	 */
	public static final LinkedHashSet<Object> getBusinessObjectsFromFormGroups(
			Collection<? extends FormGroup> formGroups) {
		LinkedHashSet<Object> result = new LinkedHashSet<>();
		for (FormGroup formGroup : formGroups) {
			result.add(getRowObject(formGroup));
		}
		return result;
	}

	/**
	 * Converts from the grid internal row object to the corresponding business object.
	 * 
	 * @param row
	 *        A grid internal row object: Either a {@link FormGroup} or a {@link GridTreeTableNode}.
	 *        Is not allowed to be null.
	 * @return The business object, usually a {@link TLObject}. Null, if that is the business
	 *         object.
	 * 
	 * @see GridUtil#getBusinessObjectFromInternalRow(Object)
	 */
	public Object getBusinessObjectFromInternalRow(Object row) {
		return getRowObject(getFormGroupFromInternalRow(row));
	}

	/**
	 * Retrieves the grid internal row objects from the {@link FormGroup}.
	 * 
	 * 
	 * @param groups
	 *        Is not allowed to be or contain null.
	 * @return The grid internal row objects: Either {@link FormGroup}s or
	 *         {@link GridTreeTableNode}. A new, mutable and resizable {@link LinkedHashSet}. Never
	 *         null.
	 */
	public LinkedHashSet<Object> getInternalRowsFromFormGroups(Collection<? extends FormGroup> groups) {
		LinkedHashSet<Object> result = new LinkedHashSet<>();
		for (FormGroup group : groups) {
			for (Object row : getInternalRowsFromFormGroup(group)) {
				result.add(row);
			}
		}
		return result;
	}

	/**
	 * Converts from the {@link FormGroup} to the corresponding grid internal row object.
	 * 
	 * @param row
	 *        Is not allowed to be null.
	 * @return A grid internal row object: Either a {@link FormGroup} or a
	 *         {@link GridTreeTableNode}.
	 */
	public Collection<?> getInternalRowsFromFormGroup(FormGroup row) {
		return getHandler().getTableRows(row);
	}

	/**
	 * The {@link FormGroup}s for the selected rows.
	 * 
	 * @return A new, mutable and resizable {@link Set}. Never null.
	 */
	public Set<FormGroup> getSelectedFormGroupsUnsorted() {
		return getFormGroupsFromInternalRows(getSelectionModel().getSelection());
	}

	/**
	 * Converts from the grid internal row objects to the corresponding {@link FormGroup}s.
	 * 
	 * @param rows
	 *        Grid internal row objects: Either {@link FormGroup}s or {@link GridTreeTableNode}s. Is
	 *        not allowed to be or contain null.
	 * @return A new, mutable and resizable {@link Set}. Never null. Never contains null.
	 * 
	 * @see GridUtil#getFormGroupsFromInternalRows(Collection)
	 */
	public Set<FormGroup> getFormGroupsFromInternalRows(Collection<?> rows) {
		Set<FormGroup> formGroups = new HashSet<>();
		for (Object row : rows) {
			formGroups.add(getFormGroupFromInternalRow(row));
		}
		return formGroups;
	}

	/**
	 * Converts from the grid internal row object to the corresponding {@link FormGroup}.
	 * 
	 * @param row
	 *        A grid internal row object: Either a {@link FormGroup} or s {@link GridTreeTableNode}.
	 *        Is not allowed to be.
	 * @return Never null.
	 * 
	 * @see GridUtil#getFormGroupFromInternalRow(Object)
	 */
	final FormGroup getFormGroupFromInternalRow(Object row) {
		return getHandler().getGridRow(row);
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);
		if (hasFormContext()) {
			TableField table = getTableField(getFormContext());
			table.setToolBar(newValue);
		}
	}

    /**
     * Create a new table field for the form context.
     *
     * @return   The requested table field, never <code>null</code>.
     */
    protected TableField createTableField(FormContext aContext) {
		this.initGridHandler(aContext);
		
		if (_expansionUserModel != null) {
			_handler.setExpansionState(_expansionUserModel);
			_expansionUserModel = null;
		}

		TableField theTable = _handler.getTableField();

		if (getToolBar() != null) {
			theTable.setToolBar(getToolBar());
			theTable.getTableModel().getTableConfiguration().setShowTitle(false);
		}

		initViewModel(theTable.getViewModel());
		initSelection(theTable);

        theTable.setSelectable(true);
        theTable.setCheckScope(new SingletonCheckScope(this));
		theTable.addSelectionVetoListener(this);

        return theTable;
    }

    /**
     * Return the table model used for display.
     *
     * @param     aContext    The form context used by this grid component, must not be <code>null</code>.
     * @return    The requested table model, never <code>null</code>.
     * @see       #getTableField(FormContext)
     */
    protected TableViewModel getTableModel(FormContext aContext) {
        return this.getTableField(aContext).getViewModel();
    }

	/**
	 * The table model of the current {@link #getFormContext()}.
	 * 
	 * @implSpec Calls {@link #getTableModel(FormContext)} with {@link #getFormContext()}.
	 * 
	 * @return The requested table model, never <code>null</code>.
	 * @see #getTableModel(FormContext)
	 */
	TableViewModel getViewModel() {
		return getTableModel(getFormContext());
	}

    /** 
     * Return the resource view to be used in the grid header.
     * 
     * @return    The requested resource view or <code>null</code> when no meta element defined.
     */
    protected ResourceView getResourceView() {
		return this.getResPrefix();
    }

	protected void initViewModel(final TableViewModel viewModel) {

		viewModel.setColumnChangeVetoListener(new GridColumnChangeVetoListener(this));

		viewModel.addTableModelListener(new TableModelListener() {
			
			@Override
			public void handleTableModelEvent(TableModelEvent event) {
				switch (event.getType()) {
					case TableModelEvent.COLUMNS_UPDATE: {
						TableModelColumnsEvent columnsEvent = (TableModelColumnsEvent) event;
						if (renewSelectedGroupOnColumnChange(columnsEvent.oldColumns(), columnsEvent.newColumns())) {
							renewSelectedRowGroup();
						}
						columnsChannel().set(viewModel.getColumnNames());
						break;
					}
					case TableModelEvent.INVALIDATE: {
						// Table is invalidated. This happens e.g. if filter are applied or removed
						if (getSelectedCollection().isEmpty()) {
							/* Ensure that an object is selected. It may be that nothing is selected
							 * because all rows are removed by filter. */
							invalidateSelection();
						}
						markRowsInvalid();
						break;
					}
					case TableModelEvent.INSERT:
					case TableModelEvent.DELETE:
						markRowsInvalid();
						break;
					case TableModelEvent.UPDATE:
						markRowsInvalid();
						break;
					case TableModelEvent.COLUMN_FILTER_UPDATE:
						TableModelUtils.scrollToSelectedRow(getTableField(getFormContext()).getTableData());
						break;
				}
			}
		});
		// set initial value
		columnsChannel().set(viewModel.getColumnNames());
	}

	/**
	 * Whether the selected row group is renewed when the displayed columns change.
	 * 
	 * @implNote The group must be renewed when there are either new columns (in this case new
	 *           fields must be created) or columns disappear (in this case the value for the
	 *           removed columns must be saved). Only re-ordering need no change in the form group.
	 * 
	 * @param oldColumns
	 *        Former displayed columns.
	 * @param newColumns
	 *        New displayed columns.
	 */
	protected boolean renewSelectedGroupOnColumnChange(List<String> oldColumns, List<String> newColumns) {
		return !CollectionUtil.containsSame(oldColumns, newColumns);
	}

	private void markRowsInvalid() {
		_rowsValid = false;
	}

	/**
	 * Initializes the {@link SelectionModel}
	 * 
	 * @param tableData
	 *        the {@link TableData}
	 */
	protected void initSelection(TableData tableData) {
		tableData.setSelectionModel(getSelectionModel());
	}

	private boolean isInSingleSelectionMode() {
		return !isInMultiSelectionMode();
	}

	private boolean isInMultiSelectionMode() {
		return getSelectionModel().isMultiSelectionSupported();
	}

	void fireRowsChanged() {
		rowsChannel().set(getDisplayedRows());
	}

	@Override
	public List<Wrapper> getDisplayedRows() {
		List<Wrapper> rows = new ArrayList<>();
		for (FormGroup group : getAllVisibleFormGroups()) {
			Object rowObject = group.get(GridComponent.PROP_ATTRIBUTED);

			// beware: rowObject may be a new, transient object
			if (rowObject instanceof Wrapper) {
				rows.add((Wrapper) rowObject);
			}
		}
		return rows;
	}

	private <T extends Collection<? super List<Object>>> T addDisplayedPaths(T out) {
		GridHandler<FormGroup> gridHandler = getHandler();
		for (FormGroup group : getAllVisibleFormGroups()) {
			addRowObjectPaths(gridHandler.getTableRows(group), out);
		}
		return out;
	}

	/**
	 * List of all column names that are available for display.
	 * 
	 * <p>
	 * The order is that in which the columns would be displayed by default.
	 * </p>
	 * 
	 * @param tableConfiguration
	 *        The current {@link TableConfiguration}.
	 */
	protected String[] getAvailableColumns(TableConfiguration tableConfiguration) {
		List<String> configuredDefaultColumns = tableConfiguration.getDefaultColumns();
		LinkedHashSet<String> declaredColumns = new LinkedHashSet<>(tableConfiguration.getElementaryColumnNames());
		declaredColumns.removeAll(configuredDefaultColumns);

		// Select column will be added at table model creation, if it is not part of configured
		// default columns.
		declaredColumns.remove(TableControl.SELECT_COLUMN_NAME);
		
		String[] result = new String[configuredDefaultColumns.size() + declaredColumns.size()];
		int index = 0;
		index = addAll(result, index, configuredDefaultColumns);
		index = addAll(result, index, declaredColumns);
		return result;
	}

	private <T> int addAll(T[] result, int index, Collection<T> values) {
		for (T configuredColumn : values) {
			result[index++] = configuredColumn;
		}
		return index;
	}

    private Mapping<Object, FormGroup> getRowMapping(
			final TableConfiguration tableConfiguration,
			final FormContext formContext) {
		final ResPrefix thePrefix = this.getResPrefix();
        Mapping<Object, FormGroup> mapping = new Mapping<>() {
			@Override
			public FormGroup map(Object rowObject) {
				FormGroup group = getRowGroup(rowObject);
				if (group == null) {
					return addAttributedRow(tableConfiguration, thePrefix, rowObject, formContext);
				} else {
					return group;
				}
			}
		};
		return mapping;
	}

    /**
     * Create a new form group with the given parameters and append this to the form context.
     * @param    resPrefix         The resource prefix for the form group, must not be <code>null</code>.
     * @param    rowObject    The attributed represented by the new row, must not be <code>null</code>.
     * @param    aContext        The form context to append the group to, must not be <code>null</code>.
     *
     * @return   The new created form group, never <code>null</code>.
     */
    protected FormGroup addAttributedRow(TableConfiguration tableConfiguration, ResPrefix resPrefix, Object rowObject, FormContext aContext) {
        FormGroup theGroup = new FormGroup("row" + this.counter++, resPrefix);

        theGroup.setInheritDeactivation(false);
        setRowModel(theGroup, rowObject);
        aContext.addMember(theGroup);
        
        if(_applyListener != null) {
			theGroup.addListener(FormMember.IS_CHANGED_PROPERTY, _applyListener);
        }

        this.groupMap.put(rowObject, theGroup);

        return theGroup;
    }

	private void setRowModel(FormGroup row, Object rowModel) {
		row.set(PROP_ATTRIBUTED, rowModel);
        row.setStableIdSpecialCaseMarker(rowModel);
	}

	/**
	 * {@link Mapping} that maps an underlying row group to the represented business object.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	final class ModelMapping implements Mapping<FormGroup, Object> {

		@Override
		public Object map(FormGroup input) {
			Object result = input.get(PROP_ATTRIBUTED);
			if (isTransient(result)) {
				return null;
			} else {
				return result;
			}
		}

	}

    /**
	 * {@link Accessor} that retrieves the underlying row group (depending the
	 * the {@link GridBuilder}).
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
    final class RowAccessor extends ReadOnlyAccessor<Object> {
		@Override
		public Object getValue(Object object, String property) {
			return _handler.getGridRow(object);
		}
	}

	/**
	 * Stop inner actions, if the current row is a newly created row that was
	 * dropped e.g. in a check changed dialog.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
    class CancelIfObjectCreationWasCanceled extends CommandChain {

		private final FormContainer _row;

		public CancelIfObjectCreationWasCanceled(FormContainer row, Command... parts) {
			super(parts);
			
			_row = row;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
    		if (_row.getParent() == null) {
				// Row no longer part of the grid. This happens, if a detail
				// dialog is opened for a newly created row, and the check
				// changed dialog is answered with "drop changes". In this
				// case, the open operation must be stopped.
    			return HandlerResult.DEFAULT_RESULT;
    		}
    		
			return super.executeCommand(context);
		}
    }

	@Override
	protected void updateLock(boolean editMode) {
		// Token context handling is handled during field creation. The mode can be changed, even if
		// the current row is locked, since another row could be selected afterwards
		//
		// super.updateLock(editMode);
	}

	@Override
	protected void handleComponentModeChange(boolean editMode) {
		// Do not drop the form context. The existing context is incrementally updated.
		//
		// super.handleComponentModeChange(oldMode, newMode);

		if (!_isSelectionValid) {
			// Note: When a selection change is pending, field creation is handled by validateModel.
			return;
		}
		if (isInEditMode()) {
			createFields();
		} else {
			if (hasFormContext()) {
				AttributeFormContext formContext = getFormContext();
				AttributeUpdateContainer updateContainer = formContext.getAttributeUpdateContainer();
				for (Object selected : getSelectedCollection()) {
					FormGroup rowGroup = getRowGroup(selected);
					removeFields(selected, rowGroup, updateContainer);
					if (isTransient(selected)) {
						dropRow(rowGroup);
					} else {
						updateTableRow(rowGroup, false);
					}
				}
			}
		}
    }

    /**
	 * Create form fields for the given attributed.
	 *
	 * @param rowObject
	 *        The attributed to add the fields for, must not be <code>null</code>.
	 * @param row
	 *        The form group to add the values in, must not be <code>null</code>.
	 * @param aViewModel
	 *        The table view model to send notifications , must not be <code>null</code>.
	 * @param formContext
	 *        The form context to add the values in, must not be <code>null</code>.
	 */
	protected void createFieldsInGroup(Object rowObject, FormGroup row, TableViewModel aViewModel,
			AttributeFormContext formContext) {
		AttributeUpdateContainer theContainer = formContext.getAttributeUpdateContainer();

        if (this.allowEdit(rowObject)) {
        	if (!isTransient(rowObject)) {
        		try {
        			this.acquireTokenContext();
        		} catch (TopLogicException ex) {
        			// Locked in another context.
        			return;
        		}
        	}
            
            if (!isValid(rowObject)) {
            	// Object deletion was refetched during lock acquisition, see Ticket #7527.
            	throw LayoutUtils.createErrorSelectedObjectDeleted();
            }

			TLClass type = getMetaElement(rowObject);
			
			if (isTransient(rowObject)) {
				HiddenField changeMarker = FormFactory.newHiddenField(NEW_OBJECT_MARKER_FIELD, 1);
				changeMarker.setDefaultValue(0);
				changeMarker.setLabel(Resources.getInstance().getString(I18NConstants.NEW_OBJECT_MARKER));
				row.addMember(changeMarker);
			}
            
			final TLObject editedObject = toAttributed(rowObject);
			if (this.modifier.preModify(this, type, editedObject)) {
				AttributeFormFactory formFactory = AttributeFormFactory.getInstance();

				KeyEventListener onKeyPress = new OnKeyPress();

				UpdateFactory overlay =
					editedObject == null
						? formContext.createObject(type, ROW_DOMAIN, ((NewObject) rowObject).getContainer())
						: formContext.editObject(editedObject);

				for (String columnName : aViewModel.getColumnNames()) {
					ColumnConfiguration column = aViewModel.getColumnDescription(columnName);

					TLStructuredTypePart attribute = type.getPart(columnName);
					if (attribute != null) {
						// A direct attribute of the edited row is handled directly by the
						// GridComponent for legacy compatibility.

						AttributeUpdate update;
						if (editedObject == null) {
							// currently no object -> creating a new object
							if (isHiddenInCreate(attribute)) {
								// Should not be visible.
								update = null;
							} else {
								update = overlay.newCreateUpdate(attribute);
							}
						} else {
							boolean disabled = this.isReadOnly(attribute, columnName);
							if (isHidden(attribute)) {
								update = null;
							} else {
								update = overlay.newEditUpdateDefault(attribute, disabled);
							}
						}

						if (update != null) {
							update.setInTableContext(true);
							update = this.modifyUpdateForAdd(columnName, attribute, editedObject, update);
						}

						if (update != null) {
							String attributeId = MetaAttributeGUIHelper.getAttributeID(update);
							if (!row.hasMember(attributeId)) {
								FormMember member =
									this.createFormMember(formFactory, theContainer, update, attributeId, attribute);

								if (member != null) {
									row.addMember(member);

									if (member instanceof FormField) {
										((FormField) member).addKeyListener(onKeyPress);
									}

									this.modifier.modify(this, columnName, member, attribute, type, editedObject,
										update, formContext, row);
                                }
							} else {
								Logger.warn("Reuse field for " + attribute, this);
                            }
                        }
					} else {
						// Custom columns can handle field creation through field providers.
						FieldProvider fieldProvider = column.getFieldProvider();
						if (fieldProvider != null) {
							FormMember field =
								fieldProvider.createField(rowObject, column.getAccessor(), columnName);
							if (field != null) {
								row.addMember(field);
							}
						}
					}
                }

				this.modifier.postModify(this, type, editedObject, formContext, row);
				updateFocus(row, formContext, type);
            }
    	}

        updateTableRow(row, false);

    }

	/** Whether the attribute is declared as "hidden during creation" in the model. */
	protected boolean isHiddenInCreate(TLStructuredTypePart attribute) {
		return DisplayAnnotations.isHiddenInCreate(attribute);
	}

	/** Whether the attribute is declared as "hidden" in the model. */
	protected boolean isHidden(TLStructuredTypePart attribute) {
		return DisplayAnnotations.isHidden(attribute);
	}

	private void updateFocus(FormGroup row, AttributeFormContext theContext, TLClass rowType) {
		String focusColumn = _focusColumn;
		if (focusColumn == null) {
			focusColumn = firstVisibleAttribute(getTableModel(theContext), rowType);
		}
		if (focusColumn != null) {
			focusCellFormMember(row, focusColumn);
		}
	}

	private void focusCellFormMember(FormGroup row, String column) {
		Object cellValue =
			getTableField(getFormContext()).getViewModel().getValueAt(_handler.getFirstTableRow(row), column);
		if (cellValue instanceof FormMember) {
			((FormMember) cellValue).focus();
		}
	}

	private void updateRowStructure(FormGroup formGroup) {
		updateTableRow(formGroup, true);
	}

    /**
     * @see GridHandler#updateTableRow(Object, boolean)
     */
	private void updateTableRow(FormGroup group, boolean structureChange) {
		_handler.updateTableRow(group, structureChange);
	}

    /** 
     * Create a new form member for the given meta attribute.
     * 
     * @param    aFactory      The factory to create the fields, must not be <code>null</code>.
     * @param    aContainer    The container holding the attribute updates, must not be <code>null</code>.
     * @param    anUpdate      The new created attribute update, must not be <code>null</code>.
     * @param    anID          The unique ID of the new form member to be created, must not be <code>null</code>.
     * @param    aMA           The meta attribute, the form field has to be created for, must not be <code>null</code>.
     * @return   The requested form member, never <code>null</code>.
     */
    protected FormMember createFormMember(AttributeFormFactory aFactory, AttributeUpdateContainer aContainer, AttributeUpdate anUpdate, String anID, TLStructuredTypePart aMA) {
		if (AttributeOperations.isWebFolderAttribute(aMA)) {
			// No inline display of webfolder. Rely on corresponding label provider for this column.
			return null;
        }
		return aFactory.toFormMember(anUpdate, aContainer, anID);
    }

    /**
	 * Check if a meta attribute is read only in this component.
	 *
	 * @param aMA
	 *        The meta attribute to be checked, must not be <code>null</code>.
	 * @param aName
	 *        The name of the given meta attribute, must not be <code>null</code>.
	 * @return <code>true</code> if the attribute value has to be read only.
	 * @see #createFieldsInGroup(Object, FormGroup, TableViewModel, AttributeFormContext)
	 */
    protected boolean isReadOnly(TLStructuredTypePart aMA, String aName) {
		return false;
    }

    /**
     * Hook for sub classes to modify the update object when created.
     *
     * This can be used to disable the field for the form context or remove it from that.
     *
     * @param    aName           The name of the meta attribute, must not be <code>null</code>.
     * @param    aMA             The meta attribute to get the value for, must not be <code>null</code>.
     * @param    anAttributed    The matching attributed, must not be <code>null</code>.
     * @param    anUpdate        The update to be added, must not be <code>null</code>.
     * @return   The update to be added, may be <code>null</code>, which will not add the constraint to the context.
     */
    protected AttributeUpdate modifyUpdateForAdd(String aName, TLStructuredTypePart aMA, TLObject anAttributed, AttributeUpdate anUpdate) {
        return anUpdate;
    }

	/**
	 * Creates fields in the current row.
	 */
	final void createFields() {
		createFields(getSelected());
	}

    /**
	 * Create some form fields for a new selected attributed.
	 * 
	 * <p>
	 * When more than one row objects is given (i.e. a collection of more than element), no form
	 * groups are created.
	 * </p>
	 * 
	 * @param rowObject
	 *        The business object (or collection of) to create form fields for, must not be
	 *        <code>null</code>.
	 * @return The {@link FormGroup} in which fields have been created.
	 * 
	 * @see #createFieldsInGroup(Object, FormGroup, TableViewModel, AttributeFormContext)
	 */
	protected final FormGroup createFields(Object rowObject) {
		return createFields(rowObject, null);
	}

	/**
	 * Creates the fields for the given row object in the given {@link FormContext}.
	 * 
	 * <p>
	 * When more than one row objects is given (i.e. a collection of more than element), no form
	 * groups are created.
	 * </p>
	 * 
	 * 
	 * @param rowObject
	 *        A row object or collection of row objects.
	 * 
	 * @param formContext
	 *        the {@link FormContext} to create the fields in. May be <code>null</code>. In that
	 *        case the {@link FormContext} returned by {@link #getFormContext()} is used, i.e. it is
	 *        possible that the context is created when this method is called.
	 * @return The {@link FormGroup} in which fields have been created.
	 */
	private FormGroup createFields(Object rowObject, AttributeFormContext formContext) {
		if (rowObject == null) {
			return null;
        }
		if (rowObject instanceof Collection) {
			if (((Collection<?>) rowObject).size() != 1) {
				return null;
			}
			rowObject = ((Collection<?>) rowObject).iterator().next();
		}

		if (!isValid(rowObject)) {
			return null;
		}
		this.setTokenContextBase(rowObject);

		FormGroup row = getRowGroup(rowObject);
		if (row != null) {
			if (formContext == null) {
				formContext = getFormContext();
			}
			TableViewModel viewModel = getTableModel(formContext);
			createFieldsInGroup(rowObject, row, viewModel, formContext);
		}

		return row;
    }

	/**
     * Check if the given attributed can be edited by the current user.
     *
     * @param    rowObject    The attributed to be checked, must not be <code>null</code>.
     * @return   <code>true</code> if editing is allowed or object is no {@link BoundObject}.
     */
    protected boolean allowEdit(Object rowObject) {
    	// New objects must be allowed to get edited. If not, the create button must be disabled, instead.
    	if (isTransient(rowObject)) return true;

    	boolean executable = getApplyHandler().allowEdit(this, rowObject);
		BoundObject securityObject = getRowSecurityProvider().getSecurityObject(this, rowObject);
        if (executable && securityObject != null) {
			BoundChecker theChecker = BoundHelper.getInstance().getDefaultChecker(getMainLayout(), securityObject);
            return theChecker != null ? theChecker.allow(SimpleBoundCommandGroup.WRITE, securityObject) : true;
        }
        else {
        	return executable;
	    }
    }

	/**
	 * The {@link GridApplyHandler handler} used to apply made changes.
	 */
	protected final GridApplyHandler getApplyHandler() {
		return applyHandler;
	}

	/**
	 * The {@link GridRowSecurityObjectProvider} of this component.
	 */
	protected final GridRowSecurityObjectProvider getRowSecurityProvider() {
		return rowSecurityProvider;
	}

    public ConfigKey getConfigKey() {
		return ConfigKey.part(this, FIELD_TABLE);
	}

    /**
	 * Return the currently selected business object out of the selected form group.
	 * 
	 * @param tableData
	 *        The data containing the selection model, must not be <code>null</code>.
	 * @return The requested business object or <code>null</code> if nothing selected.
	 */
	Object getUISelection(TableData tableData) {
		int theSelectedRow = TableUtil.getSingleSelectedRow(tableData);

		return (theSelectedRow < 0) ? null : this.getRowObject(tableData.getViewModel(), theSelectedRow);
    }

	/**
	 * Builder for the table model.
	 */
	final GridBuilder<FormGroup> gridBuilder() {
		return unsafeCast(super.getBuilder());
	}

    private void initGridHandler(FormContext aContext) {
        this.groupMap.clear();

        TableConfiguration tableConfiguration = createTableConfiguration();
        String[] availableColumns = this.getAvailableColumns(tableConfiguration);
		Mapping<Object, FormGroup> toRow = this.getRowMapping(tableConfiguration, aContext);
		Mapping<FormGroup, ?> toModel = new ModelMapping();
	
		this._handler = gridBuilder().createHandler(this, tableConfiguration, availableColumns, toRow, toModel);
    }

    private TableConfiguration createTableConfiguration() {
		List<TableConfigurationProvider> providers = new ArrayList<>();
		addTableConfigs(providers);
		providers.add(new TableConfigurationProvider() {

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				GridComponent.this.adaptDefaultColumn(defaultColumn);
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				GridComponent.this.adaptTableConfiguration(table, _addTechnicalColumn);
			}
		});
		providers.add(new EnsureGridAccessor());
		if (_addTechnicalColumn && showDetailOpener()) {
			providers.add(new NoDefaultColumnAdaption() {

				@Override
				public void adaptConfigurationTo(TableConfiguration table) {
					List<String> defaultColumns = table.getDefaultColumns();
					if (!defaultColumns.contains(COLUMN_TECHNICAL)) {
						List<String> adaptedDefaultColumns = new ArrayList<>();
						adaptedDefaultColumns.add(COLUMN_TECHNICAL);
						adaptedDefaultColumns.addAll(defaultColumns);
						table.setDefaultColumns(adaptedDefaultColumns);
					}
				}
			});
		}
		providers.add(GenericTableConfigurationProvider.showDefaultColumns());
		
		return TableConfigurationFactory.build(providers);
    }

	void addTableConfigs(List<TableConfigurationProvider> providers) {
		if (_componentTableConfigProvider != null) {
			providers.add(_componentTableConfigProvider.getTableConfigProvider(this, GridComponent.FIELD_TABLE));
		}
		
		TableConfigurationProvider tableConfiguration = lookupTableConfigurationBuilder(GridComponent.FIELD_TABLE);
		if (tableConfiguration != null) {
			providers.add(tableConfiguration);
		}
    }

	/**
	 * Since the application could have adjusted the columns with new custom accessors, it must be
	 * ensured that each column accessor is wrapped with a grid accessor that hides the
	 * implementation details of the grid table model.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private class EnsureGridAccessor implements TableConfigurationProvider {

		EnsureGridAccessor() {
		}

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			for (ColumnConfiguration column : table.getElementaryColumns()) {
				if (isIgnoredColumn(column)) {
					continue;
				}
				ensureGridAccessor(column);
			}
		}

		private boolean isIgnoredColumn(ColumnConfiguration column) {
			return TableControl.SELECT_COLUMN_NAME.equals(column.getName())
				|| COLUMN_TECHNICAL.equals(column.getName());
		}

		@Override
		public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
			ensureGridAccessor(defaultColumn);
		}

	}

	void ensureGridAccessor(ColumnConfiguration column) {
		Accessor columnAccessor = column.getAccessor();
		if (columnAccessor instanceof GridAccessor) {
			return;
		}
		
		column.setAccessor(new GridAccessor(columnAccessor));
	}

	private void applyGridCellTester(ColumnConfiguration column) {
		CellExistenceTester cellExistenceTester = column.getCellExistenceTester();
		if (!(cellExistenceTester instanceof GridCellTester)) {
			column.setCellExistenceTester(new GridCellTester(cellExistenceTester));
		}
	}

	protected void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		// No adaption by default
	}

	protected void adaptTableConfiguration(TableConfiguration table, boolean addTechnicalColumn) {
		table.setRowObjectResourceProvider(new GridRowResourceProvider(this));
		if (addTechnicalColumn && showDetailOpener()) {
			adaptTechnicalColumn(table.declareColumn(COLUMN_TECHNICAL));
		}
		for (ColumnConfiguration column : table.getElementaryColumns()) {
			String columnName = column.getName();
			if (isGridDefaultColumn(columnName)) {
				continue;
			}
        	adaptColumnConfiguration(column);
        }
		Mapping<Object, TLObject> mapping = new Mapping<>() {

			Mapping<FormGroup, Object> _modelMapping = new ModelMapping();

			@Override
			public TLObject map(Object input) {
				FormGroup underlyingRowGroup = _handler.getGridRow(input);
				Object rowObject = _modelMapping.map(underlyingRowGroup);
				if (rowObject instanceof TLObject) {
					return (TLObject) rowObject;
				}
				/* In tree case the root row object is the model of the component. This may not be a
				 * TLObject. */
				return null;
			}

		};
		table.setModelMapping(mapping);
	}

	private boolean isGridDefaultColumn(String columnName) {
		return TableControl.SELECT_COLUMN_NAME.equals(columnName) || COLUMN_TECHNICAL.equals(columnName);
	}

    protected void adaptColumnConfiguration(ColumnConfiguration column) {
		TableUtil.setBusinessModelMapping(column);

		applyGridCellTester(column);

		Renderer<Object> customRenderer = column.finalRenderer();
		ControlProvider optionalControlProvider = column.getEditControlProvider();
		column.setRenderer(GridContentRenderer.createContentRenderer(customRenderer, optionalControlProvider));
	}

	/**
     * Return the column description for the edit object column.
	 * @param column The technical marker column. 
     */
    protected void adaptTechnicalColumn(ColumnConfiguration column) {
    	column.setAccessor(new RowAccessor());
		column.setRenderer(createTechnicalColumnRenderer());

    	column.setColumnLabelKey(I18NConstants.TECHNICAL_COLUMN);
		column.setShowHeader(false);

		int columnWidth = computeTechnicalColumnWidth();
		if (columnWidth > 0) {
			column.setDefaultColumnWidth(columnWidth + "px");
		}

		column.setSortable(false);
		column.setCellStyle("align: left;");
		column.setMandatory(true);
		column.setFilterProvider(null);
		column.setFullTextProvider(null);
		column.setClassifiers(Collections.singletonList(ColumnConfig.CLASSIFIER_NO_EXPORT));
    }

	/**
	 * Computation of the width of the technical column.
	 * 
	 * @return Number of "px" to use for the size of the technical column.
	 */
	protected int computeTechnicalColumnWidth() {
		return ThemeFactory.getTheme().getValue(Icons.GRID_EDIT_WIDTH);
	}

	/**
	 * Creates the {@link Renderer} for the "technical" column of this grid displaying the
	 * multi-selection marker and the detail dialog opener button by default.
	 */
	protected TechnicalColumnRenderer createTechnicalColumnRenderer() {
		return new TechnicalColumnRenderer(this);
	}

	@Override
	protected String tableName(TableConfig tableConfig) {
		String tableName = tableConfig.getTableName();
		if (tableName.isEmpty()) {
			return FIELD_TABLE;
		}
		return super.tableName(tableConfig);
	}
	
	/** 
     * Reset the rendering control an invalidate the filters.
     */
    private void resetRenderingControl() {
        this.renderingControl = null;
    }

    /**
     * Set the given Object as the new TokenContextBase.
     *
     * @param    aTokenContext    An object to be used as the TokenContextBase
     */
    private void setTokenContextBase(Object aTokenContext) {
        this.tokenBase = aTokenContext;
    }

	private void dropRow(FormGroup theGroup) {
		FormContext    theContext = this.getFormContext();
		Collection<?> tableRows = _handler.getTableRows(theGroup);
		_handler.removeRow(theGroup);
		theContext.removeMember(theGroup);
		groupMap.remove(theGroup.get(PROP_ATTRIBUTED));
		getSelectionModel().removeFromSelection(tableRows);
	}

	private void dropRows(Set<FormGroup> groups) {
		FormContext theContext = this.getFormContext();
		for (FormGroup group : groups) {
			_handler.removeRow(group);
			theContext.removeMember(group);
			groupMap.remove(group.get(PROP_ATTRIBUTED));
		}
		invalidateSelection();
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return CHANNELS;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		ChannelLinking channelLinking = getChannelLinking(((Config) getConfig()).getColumns());
		columnsChannel().linkChannel(log, this, channelLinking);
		columnsChannel().addListener(COLUMNS_LISTENER);

		selectionChannel().addListener(GridComponent::handleNewSelectionChannelValue);

		selectionPathChannel().addListener(GridComponent::handleNewSelectionPathChannelValue);
		selectionPathChannel().addVetoListener(GridComponent::isValidSelectionPathChannelChange);

		Object channelValue = selectionPathChannel().get();
		if (isInMultiSelectionMode()) {
			if (!((Collection<?>) channelValue).isEmpty()) {
				handleNewSelectionPathChannelValue(selectionPathChannel(), Collections.emptySet(), channelValue);
			} else {
				Object currentSelection = selectionChannel().get();
				if (!((Collection<?>) currentSelection).isEmpty()) {
					handleNewSelectionChannelValue(selectionChannel(), Collections.emptySet(), currentSelection);
				}
			}
		} else {
			if (channelValue != null) {
				handleNewSelectionPathChannelValue(selectionPathChannel(), null, channelValue);
			} else {
				Object currentSelection = selectionChannel().get();
				if (currentSelection != null) {
					handleNewSelectionChannelValue(selectionChannel(), null, currentSelection);
				}
			}
		}
	}

	/**
	 * {@link ChannelListener} for {@link #selectionChannel()}.
	 *
	 * @param sender
	 *        The changed channel.
	 * @param oldValue
	 *        Old value of the channel.
	 * @param newValue
	 *        New value for the channel.
	 */
	private static void handleNewSelectionChannelValue(ComponentChannel sender, Object oldValue, Object newValue) {
		GridComponent grid = (GridComponent) sender.getComponent();

		Object selectionPath = grid.getSelectionPath();
		if (grid.isInSingleSelectionMode()) {
			if (newValue == null) {
				if (selectionPath == null) {
					return;
				} else {
					grid.setSelectionPath(null);
				}
			} else {
				if (selectionPath == null) {
					grid.setSelectionPath(buildRandomPathForObject(grid, newValue));
				} else {
					List<?> path = unsafeCast(selectionPath);
					if (newValue.equals(getLast(path))) {
						return;
					} else {
						grid.setSelectionPath(buildRandomPathForObject(grid, newValue));
					}
				}
			}
		} else {
			Collection<?> newSetValue = (Collection<?>) newValue;
			Collection<List<?>> selectionPaths = unsafeCast(selectionPath);
			switch (newSetValue.size()) {
				case 0: {
					grid.setSelectionPath(Collections.emptySet());
					break;
				}
				default: {
					Set<Object> newSelectionPaths = new HashSet<>();
					Set<Object> newSelection = new HashSet<>();
					boolean withDeselection = false;
					for (List<?> currentPath : selectionPaths) {
						Object selected = getLast(currentPath);
						if (newSetValue.contains(selected)) {
							newSelectionPaths.add(currentPath);
							newSelection.add(selected);
						} else {
							// Element was deselected
							withDeselection = true;
						}
					}
					if (!withDeselection && newSelection.containsAll(newSetValue)) {
						// same selected objects
						return;
					} else {
						List<?> tmp = new ArrayList<>(newSetValue);
						tmp.removeAll(newSelection);
						for (Object newlySelected : tmp) {
							newSelectionPaths.add(buildRandomPathForObject(grid, newlySelected));
						}
						grid.setSelectionPath(newSelectionPaths);
					}
				}
			}
		}
	}

	static List<Object> buildRandomPathForObject(GridComponent grid, Object bo) {
		Set<Object> alreadySeen = new HashSet<>();
		List<Object> path = new ArrayList<>();
		pathStep:
		while (bo != null) {
			alreadySeen.add(bo);
			FormGroup row = grid.getRowGroup(bo);
			if (row != null) {
				Collection<?> tableRows = grid.getHandler().getTableRows(row);
				if (!tableRows.isEmpty()) {
					List<Object> boPathToRoot = grid.getRowObjectPath(tableRows.iterator().next());
					Collections.reverse(boPathToRoot);
					path.addAll(boPathToRoot);
					break;
				}
			}
			path.add(bo);
			Collection<? extends Object> parentObjects = grid.gridBuilder().getParentsForRow(grid, bo);
			if (parentObjects.isEmpty()) {
				// reached root
				break;
			}
			for (Object parent : parentObjects) {
				if (!alreadySeen.contains(parent)) {
					bo = parent;
					continue pathStep;
				}
			}
			throw new IllegalArgumentException("Can not create path. All parents of " + bo
					+ " already contained in path " + path + ". Parents: " + parentObjects);
		}
		Collections.reverse(path);
		return path;

	}


	/**
	 * {@link ChannelListener} for {@link #selectionPathChannel()}.
	 *
	 * @param sender
	 *        The changed channel.
	 * @param oldValue
	 *        Old value of the channel.
	 * @param newValue
	 *        New value for the channel.
	 */
	private static void handleNewSelectionPathChannelValue(ComponentChannel sender, Object oldValue, Object newValue) {
		GridComponent grid = (GridComponent) sender.getComponent();

		Collection<? extends List<?>> selectionPaths;
		Object selectionChannelValue;
		if (grid.isInSingleSelectionMode()) {
			if (newValue == null) {
				selectionChannelValue = null;
				selectionPaths = Collections.emptySet();
			} else {
				List<?> selectedPath = (List<?>) newValue;
				selectionChannelValue = getLast(selectedPath);
				grid.setModel(retrieveModelFromPath(grid, selectedPath));
			}
		} else {
			selectionPaths = unsafeCast(newValue);
			if (!selectionPaths.isEmpty()) {
				Set<Object> lastElements = new HashSet<>();
				for (List<?> path : selectionPaths) {
					lastElements.add(getLast(path));
				}
				selectionChannelValue = lastElements;
				// all have the same model
				grid.setModel(retrieveModelFromPath(grid, getFirst(selectionPaths)));
			} else {
				selectionChannelValue = Collections.emptySet();
			}
		}
		grid.setSelected(selectionChannelValue);

		grid.invalidateSelection();
	}

	private static Object retrieveModelFromPath(GridComponent grid, List<?> path) {
		Object actualSelected = getLast(path);
		if (GridComponent.isTransient(actualSelected)) {
			// Unable to retrieve model from transient object.
			return grid.getModel();
		}
		if (!GridComponent.isValid(actualSelected)) {
			return grid.getModel();
		}
		return grid.gridBuilder().retrieveModelFromRow(grid, actualSelected);
	}

	/**
	 * Casts the given value to anything you want.
	 * 
	 * @return The given value.
	 */
	@SuppressWarnings("unchecked")
	static <T> T unsafeCast(Object value) {
		return (T) value;
	}

	/**
	 * {@link ChannelValueFilter} for {@link #selectionPathChannel()}.
	 *
	 * @param sender
	 *        Channel about to change.
	 * @param oldValue
	 *        Current value of the channel.
	 * @param newValue
	 *        Potential new value for the channel.
	 * @return Whether the value is a valid channel value.
	 */
	private static boolean isValidSelectionPathChannelChange(ComponentChannel sender, Object oldValue,
			Object newValue) {
		GridComponent grid = (GridComponent) sender.getComponent();

		if (!grid.isInMultiSelectionMode()) {
			if (newValue == null) {
				return true;
			}
			return isValidPath(grid, newValue);
		} else {
			if (!(newValue instanceof Collection<?>)) {
				return false;
			}
			Collection<?> potentialPaths = (Collection<?>) newValue;
			switch (potentialPaths.size()) {
				case 0:
					return true;
				case 1:
					return isValidPath(grid, potentialPaths.iterator().next());
				default:
					Iterator<?> it = potentialPaths.iterator();
					Object firstPath = it.next();
					if (!isValidPath(grid, firstPath)) {
						return false;
					}
					Object newComponentModel = retrieveModelFromPath(grid, (List<?>) firstPath);
					do {
						Object nextPath = it.next();
						if (!isValidPath(grid, nextPath)) {
							return false;
						}
						if (!Utils.equals(newComponentModel, retrieveModelFromPath(grid, (List<?>) nextPath))) {
							// different models
							return false;
						}
					} while (it.hasNext());
					return true;
			}
		}
	}

	private static boolean isValidPath(GridComponent grid, Object path) {
		if (path instanceof List<?>) {
			List<?> l = (List<?>) path;
			int pathLength = l.size();
			if (pathLength == 0) {
				return false;
			}
			GridBuilder<FormGroup> gridBuilder = grid.gridBuilder();
			Object lastNode = getLast(l);
			if (!GridComponent.isTransient(lastNode)) {
				if (!GridComponent.isValid(lastNode)) {
					// Last node may be the transient.
					return false;
				}
				if (!gridBuilder.supportsRow(grid, lastNode)) {
					return false;
				}
			} else {
				// The last node may be transient.
			}
			if (pathLength == 1) {
				return true;
			}
			for (int i = pathLength - 2; i > 0; i--) {
				Object node = l.get(i);
				if (GridComponent.isTransient(node) || !GridComponent.isValid(node)) {
					return false;
				}
				Collection<?> parents = gridBuilder.getParentsForRow(grid, node);
				if (!parents.contains(l.get(i - 1))) {
					return false;
				}
			}
			Object firstNode = l.get(0);
			return !GridComponent.isTransient(firstNode) && GridComponent.isValid(firstNode);
		}
		return false;
	}


	ComponentChannel columnsChannel() {
		return getChannel(ColumnsChannel.NAME);
	}

	/**
	 * Determines the uniquely selected element.
	 * 
	 * If exactly one element is selected, the selected element is returned, when nothing or more
	 * than one element is selected (only in multiple selection mode) then <code>null</code> is
	 * returned.
	 */
	public Object getSelectedSingletonOrNull() {
		Object selected = getSelected();
		if (selected instanceof Collection<?>) {
			Collection<?> selectedCollection = (Collection<?>) selected;
			if (selectedCollection.size() == 1) {
				return selectedCollection.iterator().next();
			}
			return null;
		}
		return selected;
	}

	/**
	 * Access to {@link #getSelected()} potentially wrapped into a {@link Collection}.
	 */
	public Collection<?> getSelectedCollection() {
		return asCollection(getSelected());
	}

	Collection<?> asCollection(Object value) {
		if (value instanceof Collection<?>) {
			return (Collection<?>) value;
		}
		return CollectionUtilShared.singletonOrEmptySet(value);
	}

	/**
	 * Access to {@link #getSelectionPath()} potentially wrapped into a {@link Collection}.
	 */
	public Collection<? extends List<?>> getSelectedPathsCollection() {
		if (isInSingleSelectionMode()) {
			return CollectionUtilShared.singletonOrEmptySet((List<?>) getSelectionPath());
		} else {
			return unsafeCast(getSelectionPath());
		}
	}

	private Set<List<Object>> getRowObjectPaths(Collection<?> tableRows) {
		return addRowObjectPaths(tableRows, new HashSet<>());
	}

	private <T extends Collection<? super List<Object>>> T addRowObjectPaths(Collection<?> tableRows, T paths) {
		for (Object row : tableRows) {
			paths.add(getRowObjectPath(row));
		}
		return paths;
	}

	private List<Object> getRowObjectPath(Object tableRow) {
		GridHandler<FormGroup> handler = getHandler();

		List<Object> path = new ArrayList<>();
		do {
			path.add(getRowObject(handler.getGridRow(tableRow)));
			tableRow = handler.getParentRow(tableRow);
		} while (tableRow != null);
		Collections.reverse(path);
		return path;
	}

	private Set<Object> getRowObjects(Set<?> rows) {
		Set<Object> rowObjects = new HashSet<>();

		for (Object row : rows) {
			rowObjects.add(getRowObject(getFormGroupFromInternalRow(row)));
		}

		return rowObjects;
	}

	private boolean updateSelectionPathsChannel(Set<?> newSelectedPaths) {
		boolean updated;
		if (isInMultiSelectionMode()) {
			updated = setSelectionPath(newSelectedPaths);
		} else {
			updated = setSelectionPath(CollectionUtil.getSingleValueFromCollection(newSelectedPaths));
		}
		return updated;
	}

	private boolean updateSelectionPathChannel(Object newSelectedPath) {
		boolean updated;
		if (isInMultiSelectionMode()) {
			updated = setSelectionPath(CollectionUtil.singletonOrEmptySet(newSelectedPath));
		} else {
			updated = setSelectionPath(newSelectedPath);
		}
		return updated;
	}

    /**
     * Accessor for a table row in the grid.
     *
     * A grid row must be represented as form group. This will contain some form members
     * and an {@link GridComponent#PROP_ATTRIBUTED}. When there is a form member named
     * like a column in the table, the accessor will return this, otherwise the
     * {@link GridComponent#PROP_ATTRIBUTED} (which is an {@link Wrapper}) will be asked
     * for the value.
     *
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
	public class GridAccessor implements Accessor<Object> {

        /** Business accessor to be used, when value doesn't appear in form group. */
        private Accessor<Object> inner;

        /**
		 * Creates a {@link GridAccessor}.
		 */
        public GridAccessor(Accessor<Object> anAccessor) {
            this.inner = anAccessor;
        }

        @Override
		public Object getValue(Object anObject, String aKey) {
            FormGroup theGroup = _handler.getGridRow(anObject);

            if (theGroup.hasMember(aKey)) {
                return theGroup.getMember(aKey);
            }
            else {
				Object rowObject = theGroup.get(PROP_ATTRIBUTED);

                if (isValid(rowObject)) {
                    TLClass theME = getMetaElement(rowObject);
                    if (theME == null) {
                    	// Deleted object, stop access to prevent errors, see Ticket #7527.
                    	return null;
                    }

                    if (MetaElementUtil.hasMetaAttribute(theME, aKey)) {
                        try {
							TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, aKey);
							TLObject attributed = toAttributed(rowObject);
							String        theID = MetaAttributeGUIHelper.getAttributeID(theMA, attributed);

                            if (theGroup.hasMember(theID)) {
                                return theGroup.getMember(theID);
                            }
                        }
                        catch (Exception ex) {
                        }
                    }
                    
                    return this.inner.getValue(rowObject, aKey);
                }

                return null;
            }
        }

		@Override
		public void setValue(Object object, String property, Object value) {
			inner.setValue(object, property, value);
		}
    }

	private class GridCellTester implements CellExistenceTester {

		private CellExistenceTester _wrappedTester;

		GridCellTester(CellExistenceTester wrappedTester) {
			_wrappedTester = wrappedTester;
		}

		@Override
		public boolean isCellExistent(Object gridRowObject, String columnName) {
			FormGroup theGroup = _handler.getGridRow(gridRowObject);
			Object rowObject = theGroup.get(PROP_ATTRIBUTED);
			return _wrappedTester.isCellExistent(rowObject, columnName);
		}
	}

	/**
	 * Selection listener to the displayed grid.
	 *
	 * This will be called when the user changes a row in the grid.
	 *
	 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
	 */
	private final SelectionListener _selectionListener = new SelectionListener() {

		private boolean _ignoreChanges = false;

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
			if (_ignoreChanges) {
				return;
			}
			focusColumn(getTableField(getFormContext()).getViewModel());

			Set<List<Object>> newSelectedPaths = getRowObjectPaths(newSelection);

			Collection<?> currentlySelectedPathsCollection = getSelectedPathsCollection();
			boolean channelUpdated = updateSelectionPathsChannel(newSelectedPaths);
			if (!channelUpdated) {
				/* There are two possible reasons why the selection has not changed: The channel may
				 * reject the selection. In this case the selection must be reverted. The second is
				 * that the event is triggered by the selection channel itself. In this case the
				 * selection must not be reverted. */
				Set<Object> newSelectedObjects = getRowObjects(newSelection);
				if (!CollectionUtil.equals(getSelectedCollection(), newSelectedObjects)) {
					channelUpdated = false;
					revertSelection(oldSelection);
				}
			}

			if (isInEditMode()) {
				HandlerResult result = storeAttributeValuesAndAddFields(oldSelection, newSelection);
				if (!result.isSuccess()) {
					revertSelection(oldSelection);
					if (channelUpdated) {
						// Channel value was updated; Revert changes.
						updateSelectionPathsChannel(CollectionUtil.toSet(currentlySelectedPathsCollection));
					}
					openErrorDialog(result);
				}
			}
		}

		private void revertSelection(Set<?> oldSelection) {
			boolean before = _ignoreChanges;
			/* Ignore changes temporarily, because setting the old value will trigger this listener
			 * again, which may lead to endless loop. */
			_ignoreChanges = true;
			try {
				_selectionModel.setSelection(oldSelection);
			} finally {
				_ignoreChanges = before;
			}
		}

		private void focusColumn(TableViewModel viewModel) {
			if (StringServices.isEmpty(_focusColumn)) {
				ColumnAnchor lastClickedColumn = getLastClickedColumn(viewModel);
				if (lastClickedColumn != ColumnAnchor.NONE) {
					focus(lastClickedColumn.getColumnName());
				}
			}
		}

		private ColumnAnchor getLastClickedColumn(TableViewModel viewModel) {
			return viewModel.getClientDisplayData().getViewportState().getLastClickedColumn();
		}
	};

    /**
     * Handler to store the data of the currently selected row.
     *
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
	public static class GridApplyCommandHandler extends AbstractApplyCommandHandler {

		public interface Config extends AbstractApplyCommandHandler.Config {
			// No additional properties.
		}

		/**
		 * Creates a {@link GridComponent.GridApplyCommandHandler} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
    	public GridApplyCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

    	// Overridden methods from AbstractApplyCommandHandler

    	@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			GridComponent grid = (GridComponent) aComponent;
			return grid.updateFormContext(AbstractApplyCommandHandler.warningsDisabledTemporarily());
    	}

    	@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(HasTokenContextRule.INSTANCE, InEditModeExecutable.INSTANCE);
		}

		@Override
		protected boolean storeChanges(LayoutComponent component, FormContext context, Object model) {
			return false;
		}
    }

    /**
	 * Allow execution only, when at least one visible row in {@link GridComponent} has been marked.
	 */
    protected static class HasVisibleMarkedGroupsRule implements ExecutabilityRule {

        public static final ExecutabilityRule INSTANCE = new HasVisibleMarkedGroupsRule();

        /** Disabled executable state, when no row has been marked in the component. */
		private static final ExecutableState EXECUTABLE_NONE =
			ExecutableState.createDisabledState(I18NConstants.NOT_MARKED);

		@SuppressWarnings({ "synthetic-access" })
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
            if (aComponent instanceof GridComponent) {
                GridComponent theGrid = (GridComponent) aComponent;
                if (theGrid.hasFormContext()) {
					if (theGrid.isInSingleSelectionMode()) {
						return ExecutableState.NOT_EXEC_HIDDEN;
					} else {
						return !theGrid.getDisplayedSelection().isEmpty() ? ExecutableState.EXECUTABLE
							: EXECUTABLE_NONE;
					}
                }
            }
            return ExecutableState.NOT_EXEC_HIDDEN;
        }
    }

	/**
	 * Allow execution only, when at least one row in {@link GridComponent} has been marked.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	protected static class HasMarkedGroupsRule implements ExecutabilityRule {

		public static final ExecutabilityRule INSTANCE = new HasMarkedGroupsRule();

		/** Disabled executable state, when no row has been marked in the component. */
		private static final ExecutableState EXECUTABLE_NONE =
			ExecutableState.createDisabledState(I18NConstants.NOT_MARKED);

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (aComponent instanceof GridComponent) {
				GridComponent theGrid = (GridComponent) aComponent;
				if (theGrid.hasFormContext()) {
					return theGrid.getSelectionModel().getSelection().isEmpty()
						? EXECUTABLE_NONE
						: ExecutableState.EXECUTABLE;
				}
			}
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

    /**
	 * Allow execution only, when at least one row in {@link GridComponent} has not been marked and
	 * multi selection is allowed.
	 *
	 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
	 */
    protected static class HasNotVisibleMarkedGroupsRule implements ExecutabilityRule {
    	
    	public static final ExecutabilityRule INSTANCE = new HasNotVisibleMarkedGroupsRule();
    	
		private static final ExecutableState EXECUTABLE_NONE =
			ExecutableState.createDisabledState(I18NConstants.ALL_MARKED);
    	
		@SuppressWarnings("synthetic-access")
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map someValues) {
			if (aComponent instanceof GridComponent) {
				GridComponent theGrid = (GridComponent) aComponent;
				if (theGrid.hasFormContext()) {
					if (!theGrid.isInMultiSelectionMode()) {
						return ExecutableState.NOT_EXEC_HIDDEN;
					} else {
						return theGrid.getDisplayedSelection().size() < theGrid.getDisplayedGridRows().size()
							? ExecutableState.EXECUTABLE : EXECUTABLE_NONE;
					}
				}
			}
    		return ExecutableState.NOT_EXEC_HIDDEN;
    	}
    }

	/**
	 * {@link GenericPropertyListener} attached to all form groups which represent a
	 * row. If the FormGroup is {@link FormGroup#isChanged() changed} a
	 * different image is displayed.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private final class ApplyComandImageChange implements ChangeStateListener {
    	
    	private final CommandModel _applyCommand;

		private final ThemeImage _changedImage;

		private final ThemeImage _unchangedImage;

		public ApplyComandImageChange(CommandModel applyCommand, ThemeImage changedImage, ThemeImage unchangedImage) {
			this._applyCommand = applyCommand;
			this._changedImage = changedImage;
			this._unchangedImage = unchangedImage;
		}
		
		@Override
		public Bubble handleChangeStateChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			if (sender instanceof FormGroup) {
				Object correspondingAttributed = ((FormGroup) sender).get(PROP_ATTRIBUTED);
				if (correspondingAttributed == null) {
					/* may happen when the form group does not represent a row or currently nothing
					 * is selected */
					return Bubble.BUBBLE;
				}

				if (!GridComponent.this.getSelectedCollection().contains(correspondingAttributed)) {
					/* may happen when selection changed: in this case all member of the old form
					 * group are removed and if they were changed before the changed state of the
					 * group may be switched from changed to not changed */
					return Bubble.BUBBLE;
				}
				updateImage(newValue);
			}
			return Bubble.BUBBLE;
		}

		/**
		 * Updates the apply button image due to the new state
		 * 
		 * @param isChanged
		 *        if <code>true</code> the enabled image is set, the disabled
		 *        image otherwise.
		 */
		void updateImage(boolean isChanged) {
			if (isChanged) {
				// form group is changed
				GridComponent.this.setButtonImages(_applyCommand, _changedImage, null);
			} else {
				GridComponent.this.setButtonImages(_applyCommand, _unchangedImage, null);
			}
		}
    }
    	

    /**
     * Allow execution only, when {@link GridComponent} owns the token context. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public static class HasTokenContextRule implements ExecutabilityRule {

		public static final HasTokenContextRule INSTANCE = new HasTokenContextRule();

        /** Disabled executable state, when no token context owned by the component. */
		private static final ExecutableState NOT_LOCKED = ExecutableState.createDisabledState(I18NConstants.NONE);

		/** Disabled executable state, when no Nothing is selected. */
		private static final ExecutableState NOTHING_SELECTED =
			ExecutableState.createDisabledState(I18NConstants.NO_SELECTION);

        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
            return canExecute(aComponent);
        }

		/**
		 * Whether the command is executable on the given component.
		 */
		public ExecutableState canExecute(LayoutComponent aComponent) {
			if (aComponent instanceof GridComponent) {
                GridComponent grid = (GridComponent) aComponent;
				Collection<?> selectedCollection = grid.getSelectedCollection();
				if (selectedCollection.isEmpty()) {
					return NOTHING_SELECTED;
				}
				if (allTransient(selectedCollection)) {
                	return ExecutableState.EXECUTABLE;
                }
                
				return grid.getLockHandler().hasLock() ? ExecutableState.EXECUTABLE : NOT_LOCKED;
            }

            return ExecutableState.NOT_EXEC_HIDDEN;
		}

		private static boolean allTransient(Collection<?> objects) {
			return objects.stream().allMatch(GridComponent::isTransient);
		}
        
    }

	/**
	 * Simple handler for adding all rows marked to the clipboard.
	 *
	 * @author <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
	 */
    public static class AddSelectedToClipboard extends AJAXCommandHandler {

        // Constants

        public static final String COMMAND_ID = "addSelectedToClipboard";

        /**
		 * Creates a {@link AddSelectedToClipboard}.
		 */
        public AddSelectedToClipboard(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            GridComponent theComp = (GridComponent) aComponent;

			{
                boolean     isAdded = false;
                Clipboard   theClip = Clipboard.getInstance();
				Transaction theTX = PersistencyLayer.getKnowledgeBase().beginTransaction();
                
                for (Iterator<FormGroup> theIt = theComp.getMarkedFormGroups().iterator(); theIt.hasNext(); ) {
                    FormGroup theGroup   = theIt.next();
					Wrapper theWrapper = (Wrapper) theGroup.get(PROP_ATTRIBUTED);

                    if (!theWrapper.tValid()) {
                        throw LayoutUtils.createErrorSelectedObjectDeleted();
                    }

                    isAdded |= theClip.add(theWrapper);
                }

                if (isAdded) {
                    try {
                        theTX.commit();
                    }
                    catch (KnowledgeBaseException ex) {
                        HandlerResult theResult = new HandlerResult();
						theResult.addErrorMessage(aComponent.getResPrefix().key("clipboard.failed"), ex.getLocalizedMessage());
                        return theResult;
                    }
                    // if everything did go well clear the check boxes
                    theComp.clearAllMarkedCheckboxes();
                }
            }

            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
			return HasMarkedGroupsRule.INSTANCE;
        }
    }

    /**
	 * Clear all selected check boxes in the grid.
	 * 
	 * @see SelectAllCheckboxes
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public static class ClearSelectedCheckboxes extends AJAXCommandHandler {

		public static final String COMMAND_ID = "clearSelectedCheckboxes";

		/**
		 * Configuration for {@link ClearSelectedCheckboxes}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AJAXCommandHandler.Config {

			@Override
			@FormattedDefault("theme:ICONS_REMOVE_CHECKBOX")
			ThemeImage getImage();

			@Override
			@FormattedDefault("theme:ICONS_REMOVE_CHECKBOX_DISABLED")
			ThemeImage getDisabledImage();

		}

        /**
		 * Creates a new {@link ClearSelectedCheckboxes}.
		 */
        public ClearSelectedCheckboxes(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			((GridComponent) aComponent).clearAllVisibleMarkedCheckboxes();

            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return HasVisibleMarkedGroupsRule.INSTANCE;
        }
    }

    /**
     * Selects all check boxes in the marker column of the grid.
     *
     * @see ClearSelectedCheckboxes
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    public static class SelectAllCheckboxes extends AJAXCommandHandler {

    	public static final String COMMAND_ID = "selectAllCheckboxes";

		/**
		 * Configuration for {@link SelectAllCheckboxes}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AJAXCommandHandler.Config {

			@Override
			@FormattedDefault("theme:ICONS_SELECT_ALL_CHECKBOXES")
			ThemeImage getImage();

			@Override
			@FormattedDefault("theme:ICONS_SELECT_ALL_CHECKBOXES_DISABLED")
			ThemeImage getDisabledImage();

		}

    	/**
    	 * Creates a new instance of this class.
    	 */
    	public SelectAllCheckboxes(InstantiationContext context, Config config) {
    		super(context, config);
    	}

    	@Override
    	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
    		((GridComponent) aComponent).selectAllVisibleCheckboxes();
    		return HandlerResult.DEFAULT_RESULT;
    	}

    	@Override
		@Deprecated
    	public ExecutabilityRule createExecutabilityRule() {
    		return HasNotVisibleMarkedGroupsRule.INSTANCE;
    	}
    }

    /**
     * Write the grid based content to the screen.
     * 
     * If the given value is a {@link FormMember}, this class will use the
     * configured {@link ControlProvider}, otherwise the configured inner {@link Renderer}.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public static class GridContentRenderer implements Renderer<Object> {

		public static final Renderer<Object> DEFAULT_GRID_CELL_RENDERER = ResourceRenderer.NO_LINK_INSTANCE;

		/**
		 * Singleton {@link GridComponent.GridContentRenderer} instance.
		 */
		public static final GridContentRenderer INSTANCE = new GridContentRenderer(DEFAULT_GRID_CELL_RENDERER);

        // Attributes

        /** Control provider for a value of kind {@link FormMember}. */
        private final ControlProvider controlProvider;

        /** Renderer for a value of all kinds but {@link FormMember}. */
		private final Renderer<Object> defaultRenderer;

        // Constructors

		/**
		 * Creates a {@link GridContentRenderer}.
		 * 
		 * @param aRenderer
		 *        See {@link #GridContentRenderer(ControlProvider, Renderer)}.
		 */
		public GridContentRenderer(Renderer<Object> aRenderer) {
			this(DefaultFormFieldControlProvider.INSTANCE, aRenderer);
        }

        /** 
         * Creates a {@link GridContentRenderer}.
         * 
         * @param    aProvider    The control provide to be used for {@link FormMember}, must not be <code>null</code>.
         * @param    aRenderer    The normal renderer to be used, must not be <code>null</code>.
         */
		public GridContentRenderer(ControlProvider aProvider, Renderer<Object> aRenderer) {
            this.controlProvider = aProvider;
            this.defaultRenderer = aRenderer;
        }

		@Override
		public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
			if (value instanceof FormMember) {
				this.controlProvider.createControl(value).write(context, out);
				if (value instanceof FormField) {
					new ErrorControl((FormField) value, true).write(context, out);
				}
			} else {
				this.defaultRenderer.write(context, out, value);
            }
        }

		static GridContentRenderer createContentRenderer(Renderer<Object> customRenderer,
				ControlProvider optionalControlProvider) {
			if (customRenderer == null) {
				if (optionalControlProvider != null) {
					return new GridComponent.GridContentRenderer(optionalControlProvider, DEFAULT_GRID_CELL_RENDERER);
				} else {
					return INSTANCE;
				}
			}
			
			if (customRenderer instanceof GridComponent.GridContentRenderer) {
				return (GridComponent.GridContentRenderer) customRenderer;
			}
			
			ControlProvider controlProvider = optionalControlProvider != null ?
				optionalControlProvider : DefaultFormFieldControlProvider.INSTANCE;
			return new GridComponent.GridContentRenderer(controlProvider, customRenderer);
		}
    }

	/**
	 * {@link SelectionModelFactory} creating {@link DefaultMultiSelectionModel}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class DefaultMultiSelectionModelFactory extends SelectionModelFactory {

		/** Singleton {@link GridComponent.DefaultMultiSelectionModelFactory} instance. */
		public static final DefaultMultiSelectionModelFactory INSTANCE = new DefaultMultiSelectionModelFactory();

		@Override
		public SelectionModel newSelectionModel(SelectionModelOwner owner) {
			return new DefaultMultiSelectionModel(owner);
		}

	}

    /**
     * Whether the given row model is a (still transient) object being created.
     */
	public static boolean isTransient(Object rowObject) {
		return rowObject instanceof NewObject;
	}

    /**
     * Whether the given row model is valid for access.
     */
	public static boolean isValid(Object rowObject) {
		return ((TLObject) rowObject).tValid();
	}

	private boolean supportsRow(Object element) {
		return gridBuilder().supportsRow(this, element);
	}

    /**
     * The {@link KnowledgeBase} of the given row model.
     */
	public static KnowledgeBase getKnowledgeBase(Object rowObject) {
		return ((TLObject) rowObject).tKnowledgeBase();
	}

    /**
     * The {@link TLClass} type of the given row model.
     */
	public static TLClass getMetaElement(Object rowObject) {
		return (TLClass) ((TLObject) rowObject).tType();
	}
    
    /**
     * The type name consistent with {@link MetaResourceProvider} of the given row model.
     */
    public static String getTypeName(Object rowObject) {
    	if (rowObject instanceof NewObject) {
    		return ((NewObject) rowObject).getTypeName();
    	} else {
    		return MetaResourceProvider.INSTANCE.getType(rowObject);
    	}
    }

    /**
	 * Returns the handler.
	 */
	public GridHandler<FormGroup> getHandler() {
		return (_handler);
	}

	/**
	 * The given row model as {@link TLObject} object, or <code>null</code>, if it is
	 * {@link #isTransient(Object)}.
	 */
	public static TLObject toAttributed(Object rowObject) {
		if (isTransient(rowObject)) {
			return null;
		} else {
			return (TLObject) rowObject;
		}
	}

	private final class OnKeyPress extends KeyEventListener {

		public OnKeyPress() {
			super(new KeySelector[] {
				new KeySelector(KeyCode.RETURN, 0),
				new KeySelector(KeyCode.ESCAPE, 0),
				new KeySelector(KeyCode.RETURN, KeySelector.SHIFT_MASK) });
		}

		@Override
		public HandlerResult handleKeyEvent(DisplayContext commandContext, KeyEvent event) {
			switch (event.getKeyCode()) {
				case RETURN:
					return onReturn(event);
				case ESCAPE:
					return onEscape(commandContext);
				default:
					return HandlerResult.DEFAULT_RESULT;
			}
		}

		private HandlerResult onReturn(KeyEvent event) {
			TableViewModel tableModel = getViewModel();
			int currentRowNumber = currentSelectedRowNumber(tableModel);
			FormMember source = (FormMember) event.getSource();
			AttributeUpdate sourceUpdate = DefaultAttributeFormFactory.getAttributeUpdate(source);
			String columnName = sourceUpdate.getAttribute().getName();

			HandlerResult result = store(AbstractApplyCommandHandler.warningsDisabledTemporarily());
			if (result.isSuccess()) {
				int nextRowNumber = calcNextRowNumber(tableModel, currentRowNumber, event);
				Object nextTableRow = getTableRow(tableModel, tableModel.getApplicationModelRow(nextRowNumber));
				updateSelectionPathChannel(getRowObjectPath(nextTableRow));
				focus(columnName);

				FormGroup nextSelectedGroup = _handler.getGridRow(nextTableRow);
				if (ScriptingRecorder.isRecordingActive()) {
					// Explicitly record the selection change, since keystrokes are not recorded by
					// default.
					ScriptingRecorder.recordSelection(
						getTableField(nextSelectedGroup.getFormContext()),
						nextSelectedGroup, true, SelectionChangeKind.ABSOLUTE);
				}
			}

			return result;
		}

		private int currentSelectedRowNumber(TableViewModel tableModel) {
			Collection<?> selectedCollection = getSelectedCollection();
			switch(selectedCollection.size()) {
				case 0:
					/* Key event listener is only triggered when a form group is present; a
					 * FormGroup is only created for a one-sized selection. */
					Logger.warn(
						"Unexpected empty collection in Grid '" + GridComponent.this + "'.", GridComponent.this);
					return -1;
				case 1:
					FormGroup rowGroup = getRowGroup(selectedCollection.iterator().next());
					Object tableRow = getHandler().getFirstTableRow(rowGroup);
					return tableModel.getRowOfObject(tableRow);
				default:
					/* Key event listener is only triggered when a form group is present; a
					 * FormGroup is only created for a one-sized selection. */
					Logger.warn(
						"Unexpected multiple collection in Grid '" + GridComponent.this + "': " + selectedCollection,
						GridComponent.this);
					return -1;
			}
		}

		private int calcNextRowNumber(TableViewModel tableModel, int currentRowNumber, KeyEvent event) {
			if (event.getModifiers() == 0) {
				int nextRowNumber = currentRowNumber + 1;
				if (nextRowNumber >= tableModel.getRowCount()) {
					return 0;
				}
				return nextRowNumber;
			}
			if (event.getModifiers() == KeySelector.SHIFT_MASK) {
				int nextRowNumber = currentRowNumber - 1;
				if (nextRowNumber < 0) {
					return tableModel.getRowCount() - 1;
				}
				return nextRowNumber;
			}
			return currentRowNumber;
		}

		private HandlerResult onEscape(DisplayContext commandContext) {
			CommandHandler cancel = getCancelCommand();
			return CommandHandlerUtil.handleCommand(cancel, commandContext, GridComponent.this, Collections.<String, Object> emptyMap());
		}
	}

}
