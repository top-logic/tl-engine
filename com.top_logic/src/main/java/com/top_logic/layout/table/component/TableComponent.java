/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.ComponentChannel.ChannelValueFilter;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareAlgorithmHolder;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.layout.table.ColumnBuilder;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableModelUtils;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.CachedObjectTableModel;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelConfig;
import com.top_logic.mig.html.SelectionUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Component for displaying and working with tables.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableComponent extends BuilderComponent implements SelectableWithSelectionModel,
		FormHandler, TableDataOwner, ControlRepresentable, CompareAlgorithmHolder, ComponentRowSource {

	/**
	 * Configuration options for {@link TableComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config
			extends BuilderComponent.Config, ColumnsChannel.Config, Selectable.SelectableConfig, SelectionModelConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * @see #getComponentTableConfigProvider()
		 */
		String COMPONENT_TABLE_CONFIG_PROVIDER_NAME = "componentTableConfigProvider";

		/**
		 * Short-cut name of {@link TableComponent}.
		 * 
		 * <p>
		 * Note this cannot be <code>table</code> because the inner table configuration already uses
		 * this tag.
		 * </p>
		 * 
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "tableView";

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		@ClassDefault(TableComponent.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Name("useFooterForPaging")
		@BooleanDefault(false)
		boolean getUseFooterForPaging();

		@Name(XML_CONF_KEY_EXCLUDE_COLUMNS)
		@StringDefault("")
		String getExcludeColumns();

		@Name(XML_CONF_KEY_SELECTABLE)
		@BooleanDefault(true)
		boolean getSelectable();

		@Name(XML_CONF_KEY_CACHING)
		@BooleanDefault(false)
		boolean getCaching();

		@Name(XML_CONF_KEY_DEFAULT_SORTABLE)
		@BooleanDefault(true)
		boolean getDefaultSortable();

		@Name(XML_CONF_KEY_OBJECT_TYPE)
		@Format(CommaSeparatedStrings.class)
		List<String> getObjectType();

		/**
		 * Check for missing types in {@link #getObjectType()}.
		 * <p>
		 * Enable to find problems in existing tables when doing the migration for ticket #27174.
		 * They will be logged as errors when the table is used. <br />
		 * Disable it afterwards, as this check is time consuming and slows the application down.
		 * </p>
		 * 
		 * @implNote See {@link #logErrorMissingTypeConfiguration(Set)} for the exact message.
		 */
		@Name("checkMissingTypeConfiguration")
		boolean shouldCheckMissingTypeConfiguration();

		@Name(XML_CONF_KEY_FORM_MEMBER_PROVIDER)
		@InstanceFormat
		FormMemberProvider getFormMemberProvider();

		@Name(XML_CONF_KEY_COLUMN_BUILDER)
		String getColumnBuilderClass();

		@Name(XML_CONF_KEY_COLUMN_SECURITY_PROVIDER_CLASS)
		String getColumnSecurityProviderClass();

		ComponentTableConfig getTable();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		/**
		 * {@link TableConfigurationProvider} for dynamically changing the
		 * {@link TableConfiguration}.
		 */
		@InstanceFormat
		@InstanceDefault(TableComponentTableConfigProvider.class)
		@Name(COMPONENT_TABLE_CONFIG_PROVIDER_NAME)
		ComponentTableConfigProvider getComponentTableConfigProvider();

		@Override
		PolymorphicConfiguration<? extends ListModelBuilder> getModelBuilder();
	}

    /** Configuration name for excluded columns attribute. */
    public static final String XML_CONF_KEY_EXCLUDE_COLUMNS = "excludeColumns";

    /** Configuration name for the column builder class. */
    public static final String XML_CONF_KEY_COLUMN_BUILDER = "columnBuilderClass";

	/** XML layout definition attribute name with comma separated list of property-name:meta-attribute-name pairs. */
	public static final String XML_CONF_KEY_PROTECTED   = "protected";
	
	/** XML layout definition attribute name with boolean value that denotes if all attributes should be protected
	 *  If set to true the properties that are not explicitly will be interpreted as meta attribute names and be checked. */
	public static final String XML_CONF_KEY_PROTECT_ALL = "protectAll";
	
    /** SecurityProviderClass which defines on which object the security will be checked. */
    public static final String XML_CONF_KEY_COLUMN_SECURITY_PROVIDER_CLASS = "columnSecurityProviderClass";
	
    /** Configuration name for the selectable flag of the table control. */
    public static final String XML_CONF_KEY_SELECTABLE = "selectable";

    /** Configuration name for the caching flag of the table control (default is "true"). */
    public static final String XML_CONF_KEY_CACHING = "caching";

    /** Configuration name for the class to use as a form member provider. */
    public static final String XML_CONF_KEY_FORM_MEMBER_PROVIDER = "formMemberProvider";

    /** See {@link #defaultSortable}. */
    public static final String XML_CONF_KEY_DEFAULT_SORTABLE = "defaultSortable";

	@Deprecated
    public static final String XML_CONF_KEY_FIXED_COLUMNS = "fixedColumns";

	/**
	 * Configuration option to configure the qualified name of the {@link TLType} to display, e.g.
	 * "tl.accounts:Person".
	 */
	public static final String XML_CONF_KEY_OBJECT_TYPE = "objectType";

	private static final ComponentChannel.ChannelListener COLUMNS_LISTENER = new ComponentChannel.ChannelListener() {

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			TableComponent table = (TableComponent) sender.getComponent();
			if (table._tableData != null) {
				TableModelUtils.setKnownColumns(table._tableData.getViewModel(), (List<String>) newValue);
			}
		}
	};

	private static final ChannelListener ON_SELECTION_CHANGE = new ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			TableComponent table = (TableComponent) sender.getComponent();

			table.invalidateSelection();
		}
	};

	private static final ChannelValueFilter SELECTION_FILTER = new ChannelValueFilter() {
		@Override
		public boolean accept(ComponentChannel sender, Object oldValue, Object newValue) {
			TableComponent self = (TableComponent) sender.getComponent();

			if (!ComponentUtil.isValid(newValue)) {
				self.showErrorSelectedObjectDeleted();
				return false;
			}

			if (newValue != null) {
				if (newValue instanceof Collection) {
					for (Object selectedObject : (Collection<?>) newValue) {
						if (!self.getListBuilder().supportsListElement(self, selectedObject)) {
							return false;
						}
					}
				} else {
					if (!self.getListBuilder().supportsListElement(self, newValue)) {
						return false;
					}
				}
			}
			return true;
		}
	};

    /** The managed table. */
    protected TableControl tableControl;

    /** This table component's model. */
	@Inspectable
	TableData _tableData;

	private final Set<TLType> _types;

    /** The names of the displayed columns. */
    private String[] columnNames;

    /**
     * This flag indicates if the default comparators are added to the columns.
     * The individual comparators aren't affected.
     */
    protected boolean defaultSortable;

    /** The table component's column builder. */
    protected ColumnBuilder columnBuilder;

    /**
     * Indicates whether the list of row objects is valid. If the list of row objects is not
     * valid, it must be recreated.
     *
     * @see #createRowObjects() for recreating the list of row objects.
     */
    private boolean listValid;

    /**
	 * Whether the current selection is in sync with the UI.
	 * 
	 * @see #getSelected()
	 */
    private boolean _isSelectionValid;

	private FormContext _formContext;

    //private int initialPageSize;

    protected boolean useFooterForPaging;

    private boolean selectable;

    /** Flag, if the application model should be a cacheable (default is "true"). */
    private boolean cachingApplModel;

	private FormMemberProvider formMemberProvider;

	private SelectionVetoListener selectionVetoListener;

	private final SelectionListener _selectionListener = new SelectionListener() {

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
			if (!isSelectable()) {
				return;
			}

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(_tableData, newSelection, true,
					SelectionChangeKind.ABSOLUTE);
			}

			if (!CollectionUtil.equals(selectionFromChannel(), newSelection)) {
				/* Don't use given set as internal value, because it is the internal value of the
				 * SelectionModel. Therefore, changing the SelectionModel value would implicitly
				 * modify the channel value. */
				boolean selectionChannelIsUpdated = setSelectionToChannel(newSelection, true);

				if (!selectionChannelIsUpdated) {
					_selectionModel.setSelection(oldSelection);
				}
			}
		}
	};

	private CompareAlgorithm _compareAlgorithm;

	/**
	 * The {@link TableConfigurationProvider} built from {@link Config#getTable()}.
	 * 
	 * <p>
	 * The {@link TabConfig} must be translated into a {@link TableConfigurationProvider} during
	 * component construction to allow using context-aware providers within the table columns.
	 * </p>
	 */
	private TableConfigurationProvider _configuredProvider;

	private SelectionModel _selectionModel;

    /**
	 * Create a {@link TableComponent}.
	 */
	public TableComponent(final InstantiationContext context, final Config attr) throws ConfigurationException {
		super(context, attr);
		_types = resolveTypes(context, attr);
        this.useFooterForPaging = attr.getUseFooterForPaging();

        this.selectable         = attr.getSelectable();
        this.cachingApplModel   = attr.getCaching();
        this.defaultSortable    = attr.getDefaultSortable();

        // initialization of the column comparators of the newly created TableComponent is done in #componentsResolved to allow for post changes 
        // of the columnDescriptionManager.
        // initColumnComparatorsFromColumnDescriptions();
        this.columnBuilder          = this.createColumnBuilder(context, attr);
		this.formMemberProvider = attr.getFormMemberProvider();

		TableConfig table = attr.getTable();
		if (table != null) {
			_configuredProvider = TableConfigurationFactory.toProvider(context, table);
		}
		_selectionModel = createSelectionModel(attr);
	}

	private Set<TLType> resolveTypes(InstantiationContext context, Config config) {
		return Set.copyOf(TLModelUtil.findTypes(context, ModelService.getApplicationModel(), config.getObjectType()));
	}

	private SelectionModel createSelectionModel(Config config) {
		SelectionModel selectionModel = config.getSelectionModelFactory().newSelectionModel(this);

		selectionModel.addSelectionListener(_selectionListener);

		return selectionModel;
	}

	@Override
	public CompareAlgorithm getCompareAlgorithm() {
		return _compareAlgorithm;
	}

	@Override
	public void setCompareAlgorithm(CompareAlgorithm algorithm) {
		_compareAlgorithm = algorithm;
	}

	@Override
	public void createSubComponents(InstantiationContext context) {
		super.createSubComponents(context);
		
		selectionVetoListener = new ComponentSelectionVetoListener(MasterSlaveCheckProvider.INSTANCE, this);
	}

	private void initColumns(String[] someColumnNames) {
		this.columnNames = someColumnNames;
	}

    @Override
	public boolean isModelValid() {
		return this.listValid && this._isSelectionValid && super.isModelValid();
    }

    @Override
	public boolean validateModel(DisplayContext context) {
		boolean changed = super.validateModel(context);

		/*
		 * must save current selected object since the table component is a
		 * listener to the SelectionModel of its TableViewModel and therefore it
		 * is possible that the selection is killed during setting new
		 * rowObjects to the application model of the TableViewModel.
		 */
    	
		if (! listValid) {
			changed = validateRows();
		}

		if (!_isSelectionValid) {
			_isSelectionValid = true;

			validateSelection();
        }
        return changed;
    }

	private void validateSelection() {
		final TableControl   theTableControl   = getTableControl();

		if (theTableControl.isSelectable()) {
			Collection<?> selectedObjects = selectionFromChannel();

			if (selectedObjects.isEmpty()) {
				setDefaultSelection();
			} else {
				Set<Object> newSelectedObjects = getSelectableObjects(selectedObjects);

				if (newSelectedObjects.isEmpty()) {
					Set<?> oldSelectedObjects = getSelectionModel().getSelection();

					if (oldSelectedObjects.isEmpty() || !isSelectionValid(oldSelectedObjects)) {
						setDefaultSelection();
					}
				} else {
					SelectionUtil.setSelection(_selectionModel, newSelectedObjects);
				}
			}
		}
	}

	private void setDefaultSelection() {
		Object defaultSelection = getDefaultSelection();

		if (defaultSelection != null) {
			SelectionUtil.setSelection(_selectionModel, Collections.singleton(defaultSelection));
		} else {
			SelectionUtil.setSelection(_selectionModel, Collections.emptySet());
		}
	}

	private Set<Object> getSelectableObjects(Collection<?> businessObjects) {
		Set<Object> selectableObjects = new HashSet<>();

		EditableRowTableModel tableModel = getTableModel();

		for (Object businessObject : businessObjects) {
			if (isObjectSelectable(tableModel, businessObject)) {
				selectableObjects.add(businessObject);
			}
		}

		return selectableObjects;
	}

	private boolean isSelectionValid(Set<?> selection) {
		EditableRowTableModel tableModel = getTableModel();

		for (Object selectedObject : selection) {
			if (!isObjectSelectable(tableModel, selectedObject)) {
				return false;
			}
		}

		return true;
	}

	private boolean isObjectSelectable(TableModel tableModel, Object object) {
		return tableModel.containsRowObject(object) && _selectionModel.isSelectable(object)
			&& getListBuilder().supportsListElement(this, object);
	}

	private boolean validateRows() {
		this.listValid = true;

		boolean changed;
		final boolean deactivatedEvents;
		if (_tableData != null) {
			// Temporarily remove selection forwarder. This prevents sending
			// a selection event with a null selection from within calling
			// setRowObjects(...) below.
			getSelectionModel().removeSelectionListener(_selectionListener);
			deactivatedEvents = true;
		} else {
			deactivatedEvents = false;
		}
		try {
			EditableRowTableModel table;
			if (this._tableData == null) {
				table = getTableModel(); // will createRowObjects() by itself
			} else {
				table = getTableModel();
				// This call sends a selection event with a null selection,
				// if something is selected in the current view model.
				// Therefore, the event forwarder is temporarily removed,
				// see above.
				table.setRowObjects(this.createRowObjects());
			}
			fireRowsChanged();
			
			// The selection must at least be installed in the UI.
			this._isSelectionValid = false;
			
			changed = true;
		} finally {
			if (deactivatedEvents) {
				getSelectionModel().addSelectionListener(_selectionListener);
			}
		}
		if (shouldCheckMissingTypeConfiguration()) {
			checkMissingTypeConfiguration();
		}
		return changed;
	}

	/** @see Config#shouldCheckMissingTypeConfiguration() */
	protected boolean shouldCheckMissingTypeConfiguration() {
		return ((Config) getConfig()).shouldCheckMissingTypeConfiguration();
	}

	private void checkMissingTypeConfiguration() {
		Collection<?> rows = _tableData.getTableModel().getAllRows();
		Set<TLType> types = new HashSet<>();
		for (Object row : rows) {
			if (!(row instanceof TLObject)) {
				continue;
			}
			TLObject tlObject = (TLObject) row;
			if (!WrapperHistoryUtils.isCurrent(tlObject)) {
				continue;
			}
			if (tlObject.tTransient()) {
				continue;
			}
			if (tlObject.tType() == null) {
				continue;
			}
			if (getTypes().stream().anyMatch(type -> TLModelUtil.isCompatibleInstance(type, tlObject))) {
				continue;
			}
			types.add(tlObject.tType());
		}
		if (!types.isEmpty()) {
			logErrorMissingTypeConfiguration(types);
		}
	}

	private void logErrorMissingTypeConfiguration(Set<TLType> types) {
		logError("The table " + getName() + " contains persistent objects whose types are not configured at the table."
			+ " Configured types: " + qualifiedNames(getTypes()) + " Non-configured types: " + qualifiedNames(types));
	}

	private void logError(String message) {
		Logger.error(message, TableComponent.class);
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
    	this.clearViewModel();
    }

	/**
	 * Removes the current {@link TableData} and {@link FormContext}. As the {@link TableData} is
	 * removed, also the corresponding {@link TableViewModel} is killed.
	 */
    protected void clearViewModel() {
		clearTableData();
		_formContext = null;
    }

	/**
	 * Ensures that the next call to {@link #getTableData()} or {@link #getTableControl()} creates a
	 * fresh {@link TableData}.
	 */
	private void clearTableData() {
		if (_tableData != null) {
			removeToolbarButtons(_tableData);
			/* view */_tableData = null;
			this.listValid = false;

			if (tableControl != null) {
				tableControl.detach();
				tableControl = null;
			}
    	}
	}

	@Override
	public final void writeBody(ServletContext aContext, HttpServletRequest aRequest, HttpServletResponse aResponse,
			TagWriter anOut) throws IOException, ServletException {
		throw new UnsupportedOperationException("Table component are rendered by their #getRenderingControl().");
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		if (this.tableControl != null) {
			// Stop listening for events.
			this.tableControl.detach();
		}
	}

	@Override
	protected Set<? extends TLStructuredType> getTypesToObserve() {
		/* Register listeners for the types of the potential row objects. */
		return FilterUtil.filterSet(TLStructuredType.class, getTypes());
	}

	/**
	 * Checks, whether the view of this component needs to be adjusted to the
	 * changed model object.
	 * 
	 * @see LayoutComponent#receiveModelChangedEvent(Object, Object)
	 */
    @Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
        if (! this.listValid) {
        	// Already invalid, no need for further checks.
            return false;
        }

        if (supportsInternalModel(aModel)) {
			// Assume that the displayed rows are build from the changed model.
			// A complete revalidation is required.
        	
			// TODO: This component needs to have an explicit business model.
			// The check should be this.model == changedModel.
            invalidate();
            return true;
        }

        EditableRowTableModel tableModel = getTableModel();
		int row = tableModel.getRowOfObject(aModel);
        if (row < 0) {
			// The changed model is not within the displayed rows.

            if (this.getListBuilder().supportsListElement(this, aModel)) {
				// The element is now part of this table.
            	addNewRowObject(aModel);
            	return true;
            } else {
            	// The changed model cannot be part of the displayed rows. There is
            	// no need to search for the changed model within the rows.
            	return false;
            }
        } else {
            if (this.getListBuilder().supportsListElement(this, aModel)) {
            	// Forward event to the UI and request a repaint.
            	tableModel.updateRows(row, row);
            } else {
				// The element is no longer part of this table.
				removeRow(tableModel, aModel, row);
				invalidateSelection();
            }
        	
        	return true;
        }

    }

    /**
	 * Forwards events from the event bus for layout components to this
	 * components table model.
     *
     * @see LayoutComponent#receiveModelCreatedEvent(Object, Object)
     */
    @Override
	protected boolean receiveModelCreatedEvent(Object aModel, Object changedBy) {
        if (! this.listValid) {
            return false;
        }

        if (! this.getListBuilder().supportsListElement(this, aModel)) {
        	return false;
        }

        addNewRowObject(aModel);
        
        return true;
    }

	/** 
	 * Add the newly created object to this table. This method is a hook for 
	 * subclasses. 
	 * 
	 * @param newRowObject The new row object.
	 */
	protected void addNewRowObject(Object newRowObject) {
        this.getTableModel().addRowObject(newRowObject);
	}

    /**
	 * Forwards events from the event bus for layout components to this components table model.
	 *
	 * @see LayoutComponent#receiveModelDeletedEvent(Set, Object)
	 */
    @Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		if (!this.listValid) {
			return super.receiveModelDeletedEvent(models, changedBy);
        }

        // This code may cause problems in some tables because aModel is deleted
        // (maybe an invalid wrapper), and some components doesn't support invalid
        // wrapper. This method will return false further below, when the deleted
        // object was not found within the list.
        // Because there should not be as many delete events in the application,
        // the speed loss may be not such bad.
//         if (! this.supportsListElement(aModel)) {
//            return false;
//         }

		EditableRowTableModel tableModel = getTableModel();
		if (models.size() == 1) {
			TLObject removedObject = models.iterator().next();
			int removedRow = tableModel.getRowOfObject(removedObject);
			if (removedRow < 0) {
				return super.receiveModelDeletedEvent(models, changedBy);
			}
			removeRow(tableModel, removedObject, removedRow);
			super.receiveModelDeletedEvent(models, changedBy);
			return true;
		} else {
			boolean anyChanges = false;
			for (TLObject deleted : models) {
				int removedRow = tableModel.getRowOfObject(deleted);
				if (removedRow < 0) {
					continue;
				}
				// Remove the corresponding row from the table model.
				tableModel.removeRow(removedRow);
				anyChanges = true;
			}
			if (anyChanges) {
				super.receiveModelDeletedEvent(models, changedBy);
				invalidateSelection();
				return true;
			} else {
				return super.receiveModelDeletedEvent(models, changedBy);
			}

		}
	}

	private void removeRow(EditableRowTableModel tableModel, Object removedObject, int removedRow) {
		// Remove the corresponding row from the table model.
		tableModel.removeRow(removedRow);

		Set<?> selection = getSelectionModel().getSelection();
		boolean isSelected = selection != null && selection.contains(removedObject);

		if (isSelected) {
			invalidateSelection();
		}
	}

	private Object getDefaultSelection() {
		if (((Config) getConfig()).getDefaultSelection()) {
			if (this.listValid) {
				List<?> displayedRows = getTableModel().getDisplayedRows();

				if (!CollectionUtils.isEmpty(displayedRows) && getTableControl().isSelectable()) {
					return getDefaultSelection(displayedRows);
				}
			}
		}

		return null;
	}

	/**
	 * Computes the row to be selected by default from the given rows.
	 */
	protected Object getDefaultSelection(List<?> displayedRows) {
		TableModel tableModel = getTableModel();

		for (int i = 0; i < displayedRows.size(); i++) {
			Object rowObject = displayedRows.get(i);

			if (isObjectSelectable(tableModel, rowObject)) {
				return rowObject;
			}
		}

		return null;
	}

	/**
	 * Whether this {@link TableComponent} is in multi selection mode.
	 * 
	 * @return <code>true</code> if {@link #getSelected()} returns a collection of element (either
	 *         empty or not), and <code>false</code> if {@link #getSelected()} returns the selected
	 *         object or <code>null</code> if nothing is selected.
	 */
	protected boolean isInMultiSelectionMode() {
		return getSelectionModel().isMultiSelectionSupported();
	}

	/**
	 * Request updating the UI selection in response to a change of {@link #getSelected()}.
	 */
	protected void invalidateSelection() {
		this._isSelectionValid = false;
	}

	public TableViewModel getViewModel() {
		return getTableData().getViewModel();
	}

	private FormTableModel createFormTableModel(EditableRowTableModel applicationModel) {
		FormContext formContext = installFormContext();
		FormTableModel formTableModel = new FormTableModel(applicationModel, formContext);

        if (this.formMemberProvider != null) {
			this.formMemberProvider.addFormMembers(formTableModel, formContext);
        }

		return formTableModel;
	}

	/**
	 * hook for subclasses to adjust the newly created {@link TableViewModel
	 * model} of this {@link TableComponent}.
	 */
	protected void initTableViewModel(final TableViewModel newViewModel) {
		newViewModel.addTableModelListener(new TableModelListener() {

			@Override
			public void handleTableModelEvent(TableModelEvent event) {
				switch (event.getType()) {
					case TableModelEvent.INVALIDATE:
					case TableModelEvent.INSERT:
					case TableModelEvent.DELETE:
						TableComponent.this.fireRowsChanged();
						break;
					case TableModelEvent.UPDATE:
						TableComponent.this.fireRowsChanged();
						break;
					case TableModelEvent.COLUMNS_UPDATE:
						List<String> newColumnNames = newViewModel.getColumnNames();
						TableComponent.this.columnsChannel().set(newColumnNames);
						break;
				}

			}
		});
		columnsChannel().set(newViewModel.getColumnNames());
	}

	void fireRowsChanged() {
		rowsChannel().set(getDisplayedRows());
	}

	@Override
	public List<?> getDisplayedRows() {
		if (!listValid) {
			validateRows();
		}
		TableModel tableModel = getTableData().getViewModel();
		List<?> displayedRows = tableModel.getDisplayedRows();
		/* Ensure that a new list is fired. This is necessary because the list of displayed has
		 * changed internally. */
		displayedRows = Arrays.asList(displayedRows.toArray());
		return displayedRows;
	}

    /**
     * Create the application model.
     *
     * @return    The application model to be used by this component, must not be <code>null</code>.
     */
    protected ObjectTableModel createApplicationModel() {
    	TableConfiguration theConfiguration = createTableConfiguration();
    	List<String>       theColumnNames;

		if (this.columnBuilder != null) {
			theColumnNames = CollectionUtil.toList(this.columnBuilder.getColumnNames(this,
				getDefaultColumnsAsArray(theConfiguration)));
		}
		else if (columnNames != null){
			theColumnNames = CollectionUtil.toList(this.columnNames);
		} else {
			theColumnNames = new ArrayList<>(theConfiguration.getDefaultColumns());
		}

		Set<String> theCols = new HashSet<>(theColumnNames);

		for (String theKey : theConfiguration.getElementaryColumnNames()) {
			// Select column will be added at table model creation, if it is not part of configured
			// column names.
			if (isNotSelectColumn(theKey) && !theCols.contains(theKey)) {
				theColumnNames.add(theKey);
			}
		}

		int theSize = theColumnNames.size();

		String[] theColumns = theColumnNames.toArray(new String[theSize]);
		ObjectTableModel theObjectTableModel;

		List rows = this.createRowObjects();
		if (this.cachingApplModel) {
			theObjectTableModel = new CachedObjectTableModel(theColumns, theConfiguration, rows);
		}
		else { 
			theObjectTableModel = new ObjectTableModel(theColumns, theConfiguration, rows);
		}
		return theObjectTableModel;
    }

	private String[] getDefaultColumnsAsArray(TableConfiguration theConfiguration) {
		return ArrayUtil.toStringArray(theConfiguration.getDefaultColumns());
	}

	private boolean isNotSelectColumn(String theKey) {
		return !TableControl.SELECT_COLUMN_NAME.equals(theKey);
	}

	private TableConfiguration createTableConfiguration() {
		TableConfiguration configuration = TableConfigurationFactory.build(createTableConfigurationProvider());
		if (getToolBar() != null) {
			configuration.setShowTitle(false);
		}
		adaptTableConfiguration(configuration);
		return configuration;
	}

	/**
	 * Creates the {@link TableConfigurationProvider} for this {@link TableComponent}.
	 */
	protected TableConfigurationProvider createTableConfigurationProvider() {
		Config config = (Config) getConfig();

		List<TableConfigurationProvider> providers = new ArrayList<>();
		ComponentTableConfigProvider componentTableConfigProvider = config.getComponentTableConfigProvider();
		if (componentTableConfigProvider != null) {
			providers.add(componentTableConfigProvider.getTableConfigProvider(this, null));
		}
		Set<TLClass> tlClasses = getClasses();
		if (!tlClasses.isEmpty()) {
			providers.add(new GenericTableConfigurationProvider(tlClasses));
		}

		if (_configuredProvider != null) {
			providers.add(_configuredProvider);
		}

		Set<String> excludedColumns = StringServices.toSet(config.getExcludeColumns(), ',');
		if (!excludedColumns.isEmpty()) {
			providers.add(GenericTableConfigurationProvider.excludeColumns(excludedColumns));
		}
		providers.add(GenericTableConfigurationProvider.showDefaultColumns());

		return TableConfigurationFactory.combine(providers);
	}

	/**
	 * The {@link TLType}s which are being displayed.
	 * <p>
	 * If this table displays non-TLObjects, the {@link Set} is empty.
	 * </p>
	 */
	protected Set<TLType> getTypes() {
		return _types;
	}

	/** The {@link #getTypes()} filtered for {@link TLClass}. */
	protected final Set<TLClass> getClasses() {
		if (getTypes().isEmpty()) {
			return emptySet();
		}
		Set<TLClass> result = new HashSet<>();
		for (TLType type : getTypes()) {
			if (type instanceof TLClass) {
				result.add((TLClass) type);
			}
		}
		return result;
	}

	protected void adaptTableConfiguration(TableConfiguration table) {
		// Hook for subclasses.
	}

	public EditableRowTableModel getTableModel() {
		return (EditableRowTableModel) getTableData().getTableModel();
	}

	/**
	 * Service method for type safe access to {@link #getBuilder()}.
	 */
	public final ListModelBuilder getListBuilder() {
		return (ListModelBuilder) getBuilder();
	}

    /**
     * Create a list of objects to be used as row objects in the table model.
     *
     * @return    The requested list, must not be <code>null</code>.
     */
    protected List createRowObjects() {
		return (CollectionUtil.toList(getListBuilder().getModel(getModel(), this)));
    }

    protected TableControl createTableControl() {
		TableData tableData = getTableData();
		ITableRenderer renderer = tableData.getTableModel().getTableConfiguration().getTableRenderer();
		TableControl theControl = new TableControl(tableData, renderer);

		theControl.setSelectable(this.selectable);

        return theControl;
    }

	/**
	 * Removes default table buttons for the toolbar of the given table.
	 */
	protected void removeToolbarButtons(TableData table) {
		ToolBar toolBar = table.getToolBar();
		if (toolBar != null) {
			TableUtil.removeTableButtons(toolBar, table);
		}
	}

	boolean setSelectionToChannel(Collection<?> newSelection, boolean copyOriginalCollection) {
		if (isInMultiSelectionMode()) {
			if (copyOriginalCollection) {
				newSelection = new HashSet<>(newSelection);
			}
			return setSelected(newSelection);
		} else {
			switch (newSelection.size()) {
				case 0:
					return setSelected(null);
				case 1:
					return setSelected(CollectionUtils.extractSingleton(newSelection));
				default:
					throw new IllegalArgumentException(
						"Multiple selection " + newSelection + " for single selection table: " + this);
			}
		}
	}

	Collection<?> selectionFromChannel() {
		Object channelValue = getSelected();

		Collection<?> selectionCollection;
		if (channelValue instanceof Collection) {
			selectionCollection = (Collection<?>) channelValue;
		} else {
			// Table uses single selection.
			selectionCollection = CollectionUtil.singletonOrEmptySet(channelValue);
		}
		return selectionCollection;
	}

    @Override
	public void invalidate() {
    	super.invalidate();
		clearViewModel();
	}

	@Override
	public final Control getRenderingControl() {
		return getTableControl();
	}

    protected final TableControl getTableControl() {
        if (this.tableControl == null) {
            this.tableControl = this.createTableControl();
			addControl(this.getName().qualifiedName(), this.tableControl);
        }

        return this.tableControl;
    }

    /**
     * Creates the column builder from the layout description.
     *
     * Note: This method gets called from the constructor, so overriding subclasses must not
     * access instance variables.
     */
    protected ColumnBuilder createColumnBuilder(InstantiationContext context, Config attr) throws ConfigurationException {
        String className = StringServices.nonEmpty(attr.getColumnBuilderClass());
        if (StringServices.isEmpty(className)) {
            return getDefaultColumnBuilder();
        }
        try {
            final Class theBuilderClass = Class.forName(className);
            return (ColumnBuilder)theBuilderClass.newInstance();
        }
        catch (Exception ex) {
			throw new ConfigurationException("Unable to instantiate column builder '" + className + "'!", ex);
        }
    }

    protected ColumnBuilder getDefaultColumnBuilder() {
        return null;
        // If a default column builder is set here, this will break all components which
        // call manually the setColumnNames method
//        return DefaultColumnBuilder.INSTANCE;
    }

    /**
     * Return the selectable state of this component.
     *
     * This will only return the flag from this instance, not the real state
     * configured in the table control.
     *
     * @TODO BHU  Is this correct, or should we return the state of the control?
     * @return    The state of the instance flag.
     */
    public boolean getSelectable() {
        return (this.selectable);
    }

    /**
     * Switch the selectable flag for this instance.
     *
     * If there is already a table control created, change the selectable flag
     * there too.
     *
     * @param    aFlag    The new selectable mode.
     */
    protected void setSelectable(boolean aFlag) {
        this.selectable = aFlag;

        if (this.tableControl != null) {
            this.tableControl.setSelectable(aFlag);
        }
    }

    public boolean isSelectable() {
        return selectable;
    }

    /**
	 * This method return the column names.
	 */
    public String[] getColumnNames() {
		if (columnNames != null) {
			return this.columnNames;
		} else {
			return getDefaultColumnsAsArray(createTableConfiguration());
		}
    }

    /**
	 * This method sets the column names. Afterwards the initialisation of the columnComparators must be repeated.
	 * if the ViewModel is still there. the copying of the columnComparators to the view model must be repeated also.
	 *
	 * @param someColumnNames
	 *            The column names. Must NOT be <code>null</code>.
	 */
    public void setColumnNames(String[] someColumnNames) {
    	initColumns(someColumnNames);
    }

    private Group getCurrentRepresentativeGroup() {
        final TLContext theContext = TLContext.getContext();
        if (theContext == null) {
            return null;
        }
        final Person theCurrentPerson = theContext.getCurrentPersonWrapper();
        if (theCurrentPerson == null) {
            return null;
        }
        return theCurrentPerson.getRepresentativeGroup();
    }

    @Override
	public FormContext getFormContext() {
		return this._formContext;
    }

	private FormContext installFormContext() {
		FormContext formContext = createFormContext();
		_formContext = formContext;
		return formContext;
	}

    protected FormContext createFormContext() {
		FormContext context = new FormContext("form", this.getResPrefix());
		FormComponent.initFormContext(this, this, context);
		return context;
    }

    @Override
	public boolean hasFormContext() {
		return this._formContext != null;
    }

	@Override
	public Command getApplyClosure() {
		return null;
	}

	@Override
	public Command getDiscardClosure() {
		return null;
	}

	@Override
	public TableData getTableData() {
		if (_tableData == null) {
			_tableData = createTableData();
			invalidateSelection();
		}
		return _tableData;
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);

		// When in a dialog, the setter happens for each opening of the dialog. Ensure that new
		// buttons are built for each new toolbar.
		if (_tableData != null) {
			if (oldValue != null) {
				TableUtil.removeTableButtons(oldValue, _tableData);
			}

			_tableData.setToolBar(newValue);
		}
	}

	private TableData createTableData() {
		FormTableModel tableModel = createFormTableModel(createApplicationModel());
		TableData tableData =
			DefaultTableData.createTableData(this, tableModel, ConfigKey.component(this));
		if (getToolBar() != null) {
			tableData.setToolBar(getToolBar());
		}
		initTableViewModel(tableData.getViewModel());
		tableData.setSelectionModel(getSelectionModel());
		tableData.addSelectionVetoListener(new InvalidSelectionVeto(invalidObject -> showErrorSelectedObjectDeleted()));
		tableData.addSelectionVetoListener(this.selectionVetoListener);

		return tableData;
	}

    @Override
	protected Map<String, ChannelSPI> channels() {
		return ColumnsChannel.COLUMNS_MODEL_ROWS_AND_SELECTION_CHANNEL;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		Config config = (Config) getConfig();
		ChannelLinking channelLinking = getChannelLinking(config.getColumns());
		columnsChannel().linkChannel(log, this, channelLinking);
		columnsChannel().addListener(COLUMNS_LISTENER);

		selectionChannel().addListener(ON_SELECTION_CHANGE);
		selectionChannel().addVetoListener(SELECTION_FILTER);
	}

	ComponentChannel columnsChannel() {
		return getChannel(ColumnsChannel.NAME);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

}
