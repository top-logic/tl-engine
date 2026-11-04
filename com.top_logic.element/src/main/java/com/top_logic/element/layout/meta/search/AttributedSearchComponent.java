/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static java.util.Collections.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.layout.meta.search.SearchFieldSupport.ChangeStoredQueryListener;
import com.top_logic.element.layout.meta.search.SearchFieldSupport.InputColorListener;
import com.top_logic.element.layout.meta.search.SearchFieldSupport.InputListener;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.FulltextFilter;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.element.meta.query.kbbased.StructuredElementAttributeFilter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.component.ColumnsChannel;
import com.top_logic.layout.table.control.EditableTableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * Component for searching in a set of {@link Wrapper attributed objects}.
 *
 * This component provides mechanism to search in a defined set of attributed objects, which will be
 * provided by the {@link AttributedSearchComponent.Config#getModelBuilder()}.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedSearchComponent extends FormComponent {

	/**
	 * Configuration options for {@link AttributedSearchComponent}.
	 */
	public interface Config extends FormComponent.Config, ColumnsChannel.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * The name of the type to search for.
		 * <p>
		 * A comma separated list of type names may be given. In that case, the user can choose
		 * the type to search for, the first type is selected initially.
		 * </p>
		 */
		@Name(XML_CONFIG_META_ELEMENT)
		@Mandatory
		String getMetaElement();

		/**
		 * Whether the user can save, load, and delete search queries.
		 */
		@Name(XML_CONFIG_QUERY_SUPPORT)
		@BooleanDefault(true)
		boolean getQuerySupport();

		/**
		 * Whether the user can edit the search input.
		 * <p>
		 * If not set, only the search command is offered, the commands for resetting the search,
		 * switching the search scope, and managing search queries are not available.
		 * </p>
		 */
		@Name(XML_CONFIG_INPUT_SUPPORT)
		@BooleanDefault(true)
		boolean getInputSupport();

		/**
		 * The columns to display in the search result table.
		 * <p>
		 * This value influences only the initial result display. It is only relevant if the user
		 * has not selected a {@link StoredQuery} and the {@link SearchModelBuilder} is either no
		 * {@link ExtendedSearchModelBuilder} or
		 * {@link ExtendedSearchModelBuilder#getResultColumnsFor(TLClass) returns an empty list}.
		 * </p>
		 * <p>
		 * Note: If the value starts with {@link AttributedSearchComponent#XML_CONF_KEY_IGNORED},
		 * this configuration is ignored.
		 * </p>
		 */
		@Name(XML_CONFIG_RESULT_COLUMNS)
		String getResultColumns();

		/**
		 * Comma separated list of additional attribute paths the user can select as result
		 * columns.
		 * <p>
		 * Each entry is a dot separated path of attribute names starting at the searched type.
		 * </p>
		 */
		@Name(XML_CONFIG_ADDITIONAL_RESULT_COLUMN_OPTIONS)
		@StringDefault("")
		String getAdditionalResultColumnOptions();

		/**
		 * Provider of additional derived attributes that can be searched in.
		 */
		@Name(XML_CONFIG_DERIVED_ATTRIBUTE_DESCRIPTION_PROVIDER)
		@InstanceFormat
		DerivedAttributeDescriptionProvider getDerivedAttributeDescriptionProviderClass();

		/**
		 * Whether the full text search initially requires all entered words to match.
		 * <p>
		 * If not set, an object matches if any of the entered words matches.
		 * </p>
		 */
		@Name(XML_CONFIG_FULLTEXT_MODE_IS_AND)
		@BooleanDefault(FulltextFilter.MODE_AND)
		boolean getFulltextIsAnd();

		/**
		 * Whether the full text search initially only finds exact word matches.
		 * <p>
		 * If not set, also words containing the search text are found.
		 * </p>
		 */
		@Name(XML_CONFIG_FULLTEXT_EXACT_MATCH)
		@BooleanDefault(FulltextFilter.EXACT_MATCH)
		boolean getFulltextExactMatch();

		/**
		 * Support class that creates the search filters from the user input.
		 */
		@Name(XML_CONFIG_SEARCH_FILTER_SUPPORT)
		@InstanceDefault(SecuritySearchFilterSupport.class)
		@InstanceFormat
		SearchFilterSupport getSearchFilterSupportClass();

		/**
		 * The name of the component that displays the {@link SearchResultSet}.
		 * 
		 * @see AttributedSearchComponent#displaySearchResult(SearchResultSet)
		 */
		@Mandatory
		@Name(XML_CONFIG_SEARCH_RESULT_COMPONENT)
		ComponentName getSearchResultComponent();

		/**
		 * A search input component normally has no model, but should be displayed.
		 */
		@BooleanDefault(true)
		@Override
		boolean getDisplayWithoutModel();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			if (getInputSupport()) {
				if (getQuerySupport()) {
					registerHandlersForQuerySupport(registry);
				}

				registry.registerButton(ResetSearchCommandHandler.COMMAND_ID);
				registry.registerButton(SwitchSearchScopeCommandHandler.COMMAND_ID);
			}

			registry.registerButton(SearchCommandHandler.COMMAND_ID);
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
		}

		/**
		 * Registers the buttons for saving and deleting search queries.
		 * 
		 * <p>
		 * Only called, if {@link #getInputSupport()} and {@link #getQuerySupport()} are set.
		 * </p>
		 *
		 * @param registry
		 *        The registry to add the commands to.
		 */
		default void registerHandlersForQuerySupport(CommandRegistry registry) {
			registry.registerButton(PublishableSaveQueryCommandHandler.COMMAND_ID);
			registry.registerButton(DeleteQueryCommandHandler.COMMAND_ID);

		}

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
		}

	}

	/**
	 * Comment for <code>IGNORED</code>
	 */
	public static final String XML_CONF_KEY_IGNORED    = "IGNORED!";

	/** Configuration name of {@link Config#getMetaElement()}. */
	public static final String XML_CONFIG_META_ELEMENT = "metaElement";

	/** Name of the (no longer used) configuration attribute for the model builder class. */
    public static final String XML_CONFIG_BUILDER_CLASS = "builderClass";

	/** Configuration name of {@link Config#getQuerySupport()}. */
    public static final String XML_CONFIG_QUERY_SUPPORT = "querySupport";

	/** Configuration name of {@link Config#getInputSupport()}. */
    public static final String XML_CONFIG_INPUT_SUPPORT = "inputSupport";

	/** Configuration name of {@link Config#getResultColumns()}. */
    public static final String XML_CONFIG_RESULT_COLUMNS = "resultColumns";

	/** Configuration name of {@link Config#getFulltextIsAnd()}. */
    public static final String XML_CONFIG_FULLTEXT_MODE_IS_AND = "fulltextIsAnd";
    
	/** Configuration name of {@link Config#getFulltextExactMatch()}. */
    public static final String XML_CONFIG_FULLTEXT_EXACT_MATCH = "fulltextExactMatch";

	/** Configuration name of {@link Config#getAdditionalResultColumnOptions()}. */
    public static final String XML_CONFIG_ADDITIONAL_RESULT_COLUMN_OPTIONS = "additionalResultColumnOptions";

	/** Configuration name of {@link Config#getDerivedAttributeDescriptionProviderClass()}. */
    public static final String XML_CONFIG_DERIVED_ATTRIBUTE_DESCRIPTION_PROVIDER = "derivedAttributeDescriptionProviderClass";

	/** Configuration name of {@link Config#getSearchFilterSupportClass()}. */
    public static final String XML_CONFIG_SEARCH_FILTER_SUPPORT = "searchFilterSupportClass";

	/** Configuration name of {@link Config#getSearchResultComponent()}. */
	public static final String XML_CONFIG_SEARCH_RESULT_COMPONENT = "search-result-component";

	/**
	 * Value of {@link #getSearchScope()} for the normal search, which offers only the search
	 * fields themselves.
	 */
    public static final String NORMAL_SCOPE = "normalScope";

	/**
	 * Value of {@link #getSearchScope()} for the extended search, which additionally allows to
	 * mark search fields as relevant and to negate them.
	 */
    public static final String EXTENDED_SCOPE = "extendedScope";

	/**
	 * Name of the hidden form field holding the currently searched type.
	 */
    public static final String CURRENT_META_ELEMENT = "currentMetaElement";
    
	/**
	 * The last fallback, when no {@link #getResultColumns()} can be determined.
	 * <p>
	 * Is parsed with {@link #parseResultColumns(String)}.
	 * </p>
	 */
	public static final String RESULT_COLUMNS_FALLBACK = Wrapper.NAME_ATTRIBUTE;

	private static final ComponentChannel.ChannelListener COLUMNS_LISTENER = new ComponentChannel.ChannelListener() {

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			AttributedSearchComponent searchComponent = (AttributedSearchComponent) sender.getComponent();
			if (!searchComponent.hasFormContext()) {
				return;
			}
			SelectField tableColumnsFields = SearchFieldSupport.getTableColumnsFields(searchComponent.getFormContext());
			if (tableColumnsFields == null) {
				return;
			}
			ArrayList<String> newSelection = new ArrayList<>((List<String>) newValue);
			newSelection.retainAll(tableColumnsFields.getOptions());
			tableColumnsFields.setValue(newSelection);
		}
	};

    private TLClass metaElement;

	private String searchScope = AttributedSearchComponent.NORMAL_SCOPE;

    private List<TLClass> metaElements;

	private List<String> resultColumns = emptyList();

    private SearchFieldSupport searchFieldSupport;

    private SearchFilterSupport searchFilterSupport;
    
    private List<String> additionalResultColumnOptions;

    private DerivedAttributeDescriptionProvider derivedAttributeDescriptionProvider;

    private final boolean fullTextModeIsAndDefault;
    private final boolean fullTextExactMatchDefault;
    
    /**
     * The table provider for the result columns selection.
     */
    public final ControlProvider TABLE_PROVIDER = new ControlProvider() {
    	@Override
		public Control createControl(Object aModel, String aStyle) {
    		SelectField selectField = (SelectField) aModel;
    		TableConfiguration table = TableConfigurationFactory.table();
			table.setDefaultFilterProvider(null);
			String thisColumn = "this";
			table.getDefaultColumn().setAccessor(SimpleAccessor.INSTANCE);
			ColumnConfiguration nameColumn = table.declareColumn(thisColumn);
			nameColumn.setLabelProvider(selectField.getOptionLabelProvider());
			nameColumn.setFilterProvider(null);
			table.setShowFooter(false);
    		table.setShowColumnHeader(false);
			String[] columnNames = { thisColumn };
			ResPrefix resourceNameSpace = getResPrefix().append("attributes.columns");
			table.setResPrefix(resourceNameSpace);
            SelectionTableModel tableModel = new SelectionTableModel(selectField, columnNames, table);
            TableData tableData = selectField.getTableData();
			tableData.setTableModel(tableModel);
			EditableTableControl theCtrl =
				new EditableTableControl(tableData, table.getTableRenderer(), EditableTableControl.ROW_MOVE);
            theCtrl.setSelectable(true);
            return theCtrl;
        }
    };
    
    /**
     * Comma separated list of metaElements as configured
     */
    private String metaElementsConfigured;

	private final ComponentName _searchResultComponent;

	private final List<String> _configuredResultColumns;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link AttributedSearchComponent}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param someAttrs
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
    public AttributedSearchComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
    	super(context, someAttrs);
        this.metaElementsConfigured = someAttrs.getMetaElement();
		_configuredResultColumns = parseResultColumns(someAttrs.getResultColumns());
        this.additionalResultColumnOptions = StringServices.toList(someAttrs.getAdditionalResultColumnOptions(), ',');
		this.derivedAttributeDescriptionProvider = someAttrs.getDerivedAttributeDescriptionProviderClass();
        
        this.fullTextModeIsAndDefault  = someAttrs.getFulltextIsAnd();
        this.fullTextExactMatchDefault = someAttrs.getFulltextExactMatch();
        
        searchFilterSupport = someAttrs.getSearchFilterSupportClass();
		_searchResultComponent = someAttrs.getSearchResultComponent();

		if (someAttrs.getPage() == null && (!(getBuilder() instanceof ExtendedSearchModelBuilder))) {
			context
				.error("Either page must be given, or the configured builder must be an ExtendedSearchModelBuilder: "
					+ getLocation());
		}
    }

    /**
	 * Overriden because the search keeps its values and does not need a check
	 * change dialog.
	 * 
	 * @return always false.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent#getUseChangeHandling()
	 */
    @Override
	public boolean getUseChangeHandling() {
        return false;
    }

	/**
	 * Displays the given {@link SearchResultSet}.
	 */
	public final void displaySearchResult(SearchResultSet resultSet) {
		gotoTarget(resultSet, _searchResultComponent);
	}

	/**
	 * The {@link #getBuilder() model builder} of this component, which must be a
	 * {@link SearchModelBuilder}.
	 */
    protected final SearchModelBuilder getSearchModelBuilder() {
		return (SearchModelBuilder) getBuilder();
    }

    /**
     * This method returns <code>true</code> if this component supports more than one
     * meta element, otherwise <code>false</code>.
     */
    public boolean isMultiMetaElementSearch() {
    	return this.metaElements != null && this.metaElements.size() > 1;
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject == null || (anObject instanceof TLClass) || (anObject instanceof StoredQuery);
    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);

		removeFormContext();

		// Needed for switching search scope
		this.invalidate();
	}

    @Override
	public String getPage() {
    	// If the page attribute doesn't contains a correct path and the
    	// model builder is a multi meta element builder then is returned
    	// the page of the model builder.
		if (getBuilder() instanceof ExtendedSearchModelBuilder) {
			String jspPage = ((ExtendedSearchModelBuilder) getBuilder()).getJspFor(getSearchMetaElement());
			if (!StringServices.isEmpty(jspPage)) {
        		return jspPage;
        	}
        }

		return super.getPage();
    }
    
    @Override
	protected boolean isChangeHandlingDefault() {
    	return false;
    }

    @Override
	protected void becomingVisible() {
    	super.becomingVisible();

    	// Check if this component has another AttributedSearchComponent as
		// master. If this is true this component gets the query options from the
		// master component. It isn't necessary that the component reacts on
		// modelCreated or modelDeleted events.
    	// If the old selected query is still available it will be selected.
    	LayoutComponent masterComponent = getMaster();
    	if (masterComponent != null && masterComponent instanceof AttributedSearchComponent) {
    		AttributedSearchComponent searchMasterComponent = (AttributedSearchComponent) masterComponent;

			setStoredQuery(searchMasterComponent);
    		setSearchMetaElement(searchMasterComponent.getSearchMetaElement());
    	}
    }

	private void setStoredQuery(AttributedSearchComponent searchMasterComponent) {
		if (!searchMasterComponent.getQuerySupport() || !getQuerySupport()) {
			return;
		}
		SelectField masterQueryField = (SelectField) (searchMasterComponent).getFormContext().getField(SearchFieldSupport.STORED_QUERY);
		SelectField queryField       = (SelectField) getFormContext().getField(SearchFieldSupport.STORED_QUERY);
		Object selection = queryField.getSingleSelection();
		queryField.setOptions(masterQueryField.getOptions());

		// If the previous selection is still in the new option list
		// it is selected.
		if (masterQueryField.getOptions().contains(selection)) {
			queryField.setAsSingleSelection(selection);
		}
	}

    @Override
	public FormContext createFormContext() {
		FormContext fc = this.createFormContext(getStoredQuery());
		SelectField tableColumnsFields = SearchFieldSupport.getTableColumnsFields(fc);
		if (tableColumnsFields != null) {
			final ComponentChannel columnsChannel = columnsChannel();
			tableColumnsFields.addValueListener(new ValueListener() {

				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					columnsChannel.set(newValue);
				}
			});
			// Set initial value to channel
			columnsChannel.set(tableColumnsFields.getValue());
		}
		return fc;
    }

	/**
	 * The globally registered command for switching between {@link #NORMAL_SCOPE} and
	 * {@link #EXTENDED_SCOPE}.
	 */
    public SwitchSearchScopeCommandHandler getSwitchSearchScopeCommand() {
		return (SwitchSearchScopeCommandHandler) CommandHandlerFactory.getInstance()
			.getHandler(SwitchSearchScopeCommandHandler.COMMAND_ID);
    }

    @Override
	public String toString() {
        return this.getClass().getName() + " [" + this.toStringValues() + ']';
    }

    /**
	 * Create a {@link FormContext} from the given query.
	 *
	 * @param aQuery
	 *        The query. May be <code>null</code>. In that case an empty search form will be created
	 *        from the type.
	 * @return The requested instance of {@link AttributeFormContext}.
	 */
    public FormContext createFormContext(StoredQuery aQuery) {
        return createFormContext(aQuery, true);
    }

	/**
	 * Create a {@link FormContext} from the given query.
	 *
	 * @param aQuery
	 *        The query. May be <code>null</code>. In that case an empty search form will be created
	 *        from the type.
	 * @param skipAttributes
	 *        Currently ignored.
	 * @return The requested instance of {@link AttributeFormContext}.
	 */
    public FormContext createFormContext(StoredQuery aQuery, boolean skipAttributes) {
		initResultColumns();
        AttributeFormContext theContext = new AttributeFormContext(this.getResPrefix());
		FormComponent.initFormContext(this, this, theContext);

        List theValues  = this.addAttributesFromMetaElement(theContext, this.getSearchMetaElement(), aQuery);
        this.addMoreConstraints(theContext, aQuery, theValues);

        return theContext;
    }

    /**
     * Get the initial elements for the search.
     *
     * @return    The initial elements for the search, never <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public Collection<? extends Wrapper> getSearchElements() {
		return (Collection) getBuilder().getModel(getModel(), this);
    }

    /**
     * Return the meta element to be used for search.
     *
     * @return    The requested meta element, never <code>null</code>.
     */
    public TLClass getSearchMetaElement() {
        return this.metaElement;
    }

    /**
     * Return the filters defined in the given context.
     *
     * @param    aContext    The form context to get the filters from, must be instance of {@link AttributeFormContext}.
     * @return   The list of filters defined in the given form context, never <code>null</code>.
     */
    public List<CollectionFilter> getFilters(FormContext aContext, Map someArguments) {
        return this.getSearchFilterSupport().getFilters((AttributeFormContext) aContext, this.getRelevantAndNegate());
    }

    /**
     * Get the flag whether to use relevant and negation flags or not.
     *
     * @return    The requested flag.
     */
    public boolean getRelevantAndNegate() {
        return AttributedSearchComponent.EXTENDED_SCOPE.equals(this.getSearchScope());
    }

	/**
	 * The currently selected {@link StoredQuery}, which is the model of this component.
	 * 
	 * @return The query, or <code>null</code> if no query is selected.
	 */
    public StoredQuery getStoredQuery() {
		return (StoredQuery) getModel();
    }

    /**
     * Reset the stored query to <code>null</code>.
     */
    public void resetStoredQuery() {
		setModel(null);
    }

    /**
     * Return the set of attribute names to be excluded from search UI.
     *
     * @return    The requested set, never <code>null</code>.
     */
	public Set<String> getExcludeSet() {
		ModelBuilder builder = getBuilder();
		if (builder instanceof ExtendedSearchModelBuilder) {
			return ((ExtendedSearchModelBuilder) builder).getExcludedAttributesForSearch(getSearchMetaElement());
        }

		return Collections.emptySet();
    }

	/**
	 * Return the set of attribute names to be excluded from reporting.
	 * 
	 * @return The requested set, never <code>null</code>.
	 */
	public Set<String> getExcludeSetForReporting() {
		ModelBuilder builder = getBuilder();
		if (builder instanceof ExtendedSearchModelBuilder) {
			return ((ExtendedSearchModelBuilder) builder).getExcludedAttributesForReporting(getSearchMetaElement());
        }
		return Collections.emptySet();
	}

	/**
	 * Return the set of attribute names which can not be selected as result column.
	 * 
	 * @return The requested set, never <code>null</code>.
	 */
	public Set<String> getExcludeSetForColumns() {
		ModelBuilder builder = getBuilder();
		if (builder instanceof ExtendedSearchModelBuilder) {
			return ((ExtendedSearchModelBuilder) builder).getExcludedAttributesForColumns(getSearchMetaElement());
		}
		return Collections.emptySet();
	}

	/**
	 * Sets the type to search for.
	 * <p>
	 * If the type changes, the search form is rebuilt and the search result is reset.
	 * </p>
	 *
	 * @param aMetaElement
	 *        The new type to search for. A <code>null</code> value is ignored.
	 */
    public void setSearchMetaElement(TLClass aMetaElement) {
        if ((aMetaElement != null) && (aMetaElement != this.metaElement)) {
            this.metaElement = aMetaElement;
            fireModelEvent(aMetaElement, ModelEventListener.MODEL_MODIFIED);
			this.removeFormContext();
            this.invalidate();

			/* Resets the model of the search result table. Otherwise the columns of the search
			 * result table belongs to the old type, the columns in the search component belongs to
			 * the new type. This is inconsistent and leads to synchronisation problems. */
			LayoutComponent searchResultComponent = getMainLayout().getComponentByName(_searchResultComponent);
			searchResultComponent.setModel(null);
        }
    }

	/**
	 * The configured {@link SearchFilterSupport} creating the search filters from the input.
	 */
    public SearchFilterSupport getSearchFilterSupport() {
        return searchFilterSupport;
    }

	/**
	 * The {@link SearchFieldSupport} creating the search fields, created lazily with
	 * {@link #initSearchFieldSupport()}.
	 */
    public SearchFieldSupport getSearchFieldSupport() {
        if (this.searchFieldSupport == null) {
            this.searchFieldSupport = this.initSearchFieldSupport();
        }
        return this.searchFieldSupport;
    }

	/**
	 * Creates the {@link SearchFieldSupport} of this component.
	 * <p>
	 * Hook for subclasses that need a specialized {@link SearchFieldSupport}.
	 * </p>
	 */
    protected SearchFieldSupport initSearchFieldSupport() {
        return new SearchFieldSupport();
    }

    /**
     * Hook for Subclasses to add extra form constraints.
     *
     * @param    aContext      The form context, must not be <code>null</code>.
     * @param    aQuery        The stored query that may contain further filters to be included, may be <code>null</code>.
     * @param    someValues    The list of attribute name already added, must not be <code>null</code>.
     */
    protected void addMoreConstraints(AttributeFormContext aContext, StoredQuery aQuery, List someValues) {
        // Add selection field for stored queries, if this is enabled by this instance.
		if (getQuerySupport()) {
            aContext.addMember(this.getStoredQueryField(aQuery));
        }

        // Add selection field for meta elements if more than one meta element allowed.
        if (!CollectionUtil.isEmptyOrNull(this.metaElements)) {
            aContext.addMember(this.getMetaElementField(this.metaElements));
        }

        // Handle full text search
        this.addFullTextField(aContext, aQuery);
        
        // Handle search result columns
        FormField theColumns = this.getColumnSelectField();

        if (theColumns != null) {
            Object theSel = theColumns.getValue();
            theColumns.setDefaultValue(theSel);
            aContext.addMember(theColumns);
        }

        // Form Group for published StoredQueries
	    aContext.addMember(getPublishingFormGroup());

        // Add meta element as hidden field (needed for StoredQuery later on).
        aContext.addMember(FormFactory.newHiddenField(CURRENT_META_ELEMENT, this.getSearchMetaElement()));
    }
    
	/**
	 * Adds the full text search field to the given form context.
	 *
	 * @param context
	 *        The search form to add the field to.
	 * @param query
	 *        The query to take the initial value from, may be <code>null</code>.
	 */
    protected void addFullTextField(AttributeFormContext context, StoredQuery query) {
        SearchFieldSupport support = this.getSearchFieldSupport();
        support.addFullTextField(context, query, getRelevantAndNegate(), this.fullTextModeIsAndDefault, this.fullTextExactMatchDefault);
    }
    
	/**
	 * The full text field of this {@link AttributedSearchComponent}.
	 * 
	 * @return May be <code>null</code>, either the search component does not have a full text filed
	 *         or no {@link FormContext} at all.
	 */
	public FormField getFullTextField() {
		if (!hasFormContext()) {
			return null;
		}
		return getSearchFieldSupport().findFullTextField((AttributeFormContext) getFormContext());
	}

    /**
     * Get additional filter for the Mandator attribute
     *
     * @return additional filter for the Mandator attribute
     */
    public MetaAttributeFilter getStructureFilter(TLStructuredTypePart anAttribute, StructuredElement aWrapper, boolean isRelevant, boolean doNegate) {
    	MetaAttributeFilter theFilter;
    	try {
    		theFilter = new StructuredElementAttributeFilter(anAttribute, aWrapper, true, doNegate, isRelevant, null);
    	}
    	catch (IllegalArgumentException ex) {
    		throw new TopLogicException(this.getClass(), "Wrong Arguments for StructuredElementAttributeFilter.", ex);
    	}
    	
    	return (theFilter);
    }

    /**
     * Get additional filter for the Mandator attribute
     *
     * @return additional filter for the Mandator attribute
     */
    public MetaAttributeFilter getStructureFilter(TLStructuredTypePart anAttribute, Set<? extends StructuredElement> someWrappers, boolean isRelevant, boolean doNegate) {
        MetaAttributeFilter theFilter;
        try {
            theFilter = new StructuredElementAttributeFilter(anAttribute, someWrappers, true, doNegate, isRelevant, null, StructuredElementAttributeFilter.START_LEVEL_CONTEXT);
        }
        catch (IllegalArgumentException ex) {
            throw new TopLogicException(this.getClass(), "Wrong Arguments for StructuredElementAttributeFilter.", ex);
        }

        return (theFilter);
    }

	/**
	 * Creates the field for selecting the result columns.
	 * 
	 * @return The field, or <code>null</code> if no column selection is offered.
	 */
    protected FormField getColumnSelectField() {
		return getSearchFieldSupport().getColumnSelectField(getResultColumns(), getSearchMetaElement(),
			getAdditionalResultColumnOptions(), getExcludeSetForColumns());
    }

	/** Initializes {@link #getResultColumns()}. */
	protected void initResultColumns() {
		resultColumns = getResultColumnsFromQuery();
		if (!resultColumns.isEmpty()) {
			return;
		}
		resultColumns = getResultColumnsFromModelBuilder();
		if (!resultColumns.isEmpty()) {
			return;
		}
		resultColumns = getResultColumnsConfigured();
		if (!resultColumns.isEmpty()) {
			return;
		}
		resultColumns = parseResultColumns(RESULT_COLUMNS_FALLBACK);
		assert !resultColumns.isEmpty() : "The result-column fallback is empty.";
	}

	/**
	 * The result columns requested by the {@link #getStoredQuery()}.
	 * 
	 * @return Never null. The result might be immutable.
	 */
	protected List<String> getResultColumnsFromQuery() {
		StoredQuery query = getStoredQuery();
		if (query == null) {
			return emptyList();
		}
		return nonNull(query.getResultColumns());
	}

	/**
	 * The result columns requested by the {@link #getBuilder()}.
	 * 
	 * @return Never null. The result might be immutable.
	 */
	protected List<String> getResultColumnsFromModelBuilder() {
		ModelBuilder builder = getBuilder();
		if (!(builder instanceof ExtendedSearchModelBuilder)) {
			return emptyList();
		}
		ExtendedSearchModelBuilder searchModelBuilder = (ExtendedSearchModelBuilder) builder;
		return nonNull(searchModelBuilder.getResultColumnsFor(getSearchMetaElement()));
	}

	/**
	 * The result columns configured directly on this component ({@link Config#getResultColumns()}).
	 * 
	 * @return Never null. The result might be immutable.
	 */
	protected List<String> getResultColumnsConfigured() {
		return _configuredResultColumns;
	}

    /**
     * Return the columns to be used in the result display.
     *
     * @return    The name of the resulting columns, never <code>null</code>.
     */
    public List<String> getResultColumns() {
        return this.resultColumns;
    }

    /**
     * Return the field for representing the stored queries in this UI.
     *
     * @param    aQuery    The query currently selected, may be <code>null</code>.
     * @return   The requested form field, must not be <code>null</code>.
     * @see      #addMoreConstraints(AttributeFormContext, StoredQuery, List)
     */
    protected FormField getStoredQueryField(StoredQuery aQuery) {
        return SearchFieldSupport.getStoredQueryConstraint(this.getSearchMetaElement(), aQuery);
    }

    /**
     * Returns the {@link FormGroup} for publishing details.
     */
    protected FormGroup getPublishingFormGroup() {
    	return this.getSearchFieldSupport().getPublishingFormGroup(this, this.getResPrefix());

    }

    /**
     * Return the field for representing the meta elements in this UI.
     *
     * @param    aList    The list of meta elements, must not be <code>null</code>.
     * @return   The requested form field, must not be <code>null</code>.
     * @see      #addMoreConstraints(AttributeFormContext, StoredQuery, List)
     */
    protected FormField getMetaElementField(List aList) {
        return this.getSearchFieldSupport().getMetaElementField(aList, this.metaElement);
    }

    /**
	 * Add the search input fields for the given meta element.
	 *
	 * @param aContext
	 *        The form context, must not be <code>null</code>.
	 * @param aSearchME
	 *        The meta element to create a search UI for, must not be <code>null</code>.
	 * @param aQuery
	 *        The stored query that may contain further filters to be included, may be
	 *        <code>null</code>.
	 * @return The list of attribute names added to the form context, may be <code>null</code>.
	 * @see SearchFieldSupport#addAttributesFromMetaElement(com.top_logic.layout.basic.component.ControlComponent,
	 *      AttributeFormContext, TLClass, StoredQuery, Set, boolean, Collection)
	 */
    protected List addAttributesFromMetaElement(AttributeFormContext aContext, TLClass aSearchME, StoredQuery aQuery) {
        return this.getSearchFieldSupport().addAttributesFromMetaElement(this, aContext, aSearchME, aQuery, this.getExcludeSet(), this.getRelevantAndNegate(), this.getDerivedAttributes().values());
    }

    @Override
    protected void componentsResolved(InstantiationContext context) {
        super.componentsResolved(context);
		if (getBuilder() != null && this.metaElement == null) {
    		this.initMetaElements();
    	}
    }
    
    /**
     * Init the meta elements from xml configuration or from the model builders
     */
    private void initMetaElements() {
		if (this.metaElementsConfigured.indexOf(',') > 0) {
			List theList = StringServices.toList(this.metaElementsConfigured, ',');

            this.metaElements = new ArrayList<>(theList.size());

            for (Iterator theIt = theList.iterator(); theIt.hasNext();) {
				this.metaElements.add(getSearchModelBuilder().getMetaElement((String) theIt.next()));
            }

            this.metaElements = Collections.unmodifiableList(this.metaElements);
            this.metaElement  = this.metaElements.get(0);
		}
		else {
			this.metaElement = getSearchModelBuilder().getMetaElement(this.metaElementsConfigured);
		}
    }
    
    /**
	 * Parses and sets the {@link List} of {@link #getResultColumns()}.
	 * <p>
	 * If the value is null, empty or starts with {@link #XML_CONF_KEY_IGNORED} (or anything other
	 * resulting in an empty {@link List} of result columns), the result columns are re-initialized,
	 * to prevent them from being empty. An empty {@link List} would mean, the user would see the
	 * search results in a table without columns.
	 * </p>
	 */
	protected void setResultColumns(String columns) {
		resultColumns = parseResultColumns(columns);
		if (resultColumns.isEmpty()) {
			initResultColumns();
		}
    }

	/**
	 * Parse the given list of comma separated column names.
	 * <p>
	 * If it starts with {@link #XML_CONF_KEY_IGNORED}, the empty {@link List} is returned.
	 * </p>
	 * 
	 * @param columns
	 *        Is allowed to be null, resulting in an empty {@link List}.
	 * @return Never null. The result might be immutable.
	 */
	protected List<String> parseResultColumns(String columns) {
		if (columns == null) {
			return emptyList();
		}
		if (columns.startsWith(XML_CONF_KEY_IGNORED)) {
			return emptyList();
		}
		return StringServices.toNonNullList(columns, ',');
	}

    /**
     * Return the values to be displayed in the {@link #toString()} method.
     *
     * @return    The values representing this instance.
     */
    protected String toStringValues() {
        return "name: '" + this.getName()
                + "', searchScope: " + this.searchScope
                + "', metaElement: " + this.metaElement
			+ ", storedQuery: " + this.getStoredQuery()
			+ ", inputSupport: " + ((Config) this.getConfig()).getInputSupport()
			+ ", querySupport: " + getQuerySupport()
			+ ", builder: " + getBuilder();
    }

	/**
	 * Whether the user can save, load, and delete search queries.
	 * 
	 * @see Config#getQuerySupport()
	 */
	public final boolean getQuerySupport() {
		// Final because property is used in configuration.
		return ((Config) getConfig()).getQuerySupport();
    }

	/**
	 * Whether the {@link #getSearchScope() search scope} is {@link #EXTENDED_SCOPE}.
	 */
    public boolean isExtendedSearch() {
        return AttributedSearchComponent.EXTENDED_SCOPE.equals(this.getSearchScope());
    }

	/**
	 * The current search scope, either {@link #NORMAL_SCOPE} or {@link #EXTENDED_SCOPE}.
	 */
    public String getSearchScope() {

        return this.searchScope;
    }

	/**
	 * Sets the {@link #getSearchScope() search scope} and rebuilds the search form.
	 *
	 * @param aScope
	 *        Either {@link #NORMAL_SCOPE} or {@link #EXTENDED_SCOPE}, other values are ignored.
	 */
    public void setSearchScope(String aScope) {
        if (AttributedSearchComponent.NORMAL_SCOPE.equals(aScope) || AttributedSearchComponent.EXTENDED_SCOPE.equals(aScope)) {
            if (!aScope.equals(this.searchScope)) {
                this.searchScope = aScope;
                boolean keepValues = AttributedSearchComponent.NORMAL_SCOPE.equals(aScope) ? false : true;

                FormContext theOldContext = this.getFormContext();
				StoredQuery theQuery;
				if (getQuerySupport()) {
					SelectField theSelect = (SelectField) theOldContext.getField(SearchFieldSupport.STORED_QUERY);
					theQuery = (StoredQuery) CollectionUtil.getSingleValueFromCollection(theSelect.getSelection());
				} else {
					theQuery = null;
				}

				this.removeFormContext();

				FormContext theNewContext = this.createFormContext((StoredQuery) null);

				// Set FormContext before values are transfered. This must be done because setting
				// the form context resets the form fields to default value. This is incorrect
				// because the new form context represents the same state. Moreover it is necessary
				// because the corresponding AttributeUpdates are out of sync to the FormFields.
				installFormContext(theNewContext);
                adaptFormContext(theOldContext, theNewContext, keepValues);

                this.invalidate();

				if (theQuery != null) {
					assert getQuerySupport() : "Query only not null when queries are supported.";
					SelectField theSelect = (SelectField) theNewContext.getField(SearchFieldSupport.STORED_QUERY);
					theSelect.removeValueListener(ChangeStoredQueryListener.INSTANCE);
					theSelect.setAsSingleSelection(theQuery);
					theSelect.addValueListener(ChangeStoredQueryListener.INSTANCE);
					theSelect.setDisabled(true);
                }
                if(!keepValues && theQuery==null){
					setModel(null);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Given search scope '" + aScope + "' is invalid!");
        }
    }

    /**
	 * In case the context is reset we have to save the values of some of the
	 * <p>
	 * Form Groups. Needs to be overridden in subclasses to keep specific values
	 * in case of a {@link FormContext} reset.
	 * </p>
	 * @param theOldContext
	 *            the context before the reset request
	 * @param theNewContext
	 *            the context after the reset request
     */
    protected void adaptFormContext(FormContext theOldContext, FormContext theNewContext, boolean keepValues) {
        if(keepValues) {
        	transferMetaAttributes(theOldContext, theNewContext);
    		transferFullTextField(theOldContext, theNewContext);
        }

		transferColumnSelect(theOldContext, theNewContext);
		transferPublishingGroup(theOldContext, theNewContext);
	}

	private void transferMetaAttributes(FormContext oldContext, FormContext newContext) {
		TLClass theME = this.getSearchMetaElement();
		Collection<TLStructuredTypePart> theAttributes = TLModelUtil.getMetaAttributes(theME);
		for (TLStructuredTypePart theMA : theAttributes) {
			String theMemberName = MetaAttributeGUIHelper.getAttributeIDCreate(theMA);
			if(!oldContext.hasMember(theMemberName)) {
				continue;
			}
			FormMember theMember = oldContext.getMember(theMemberName);

		    if (theMember instanceof FormField) {
		    	FormField oldField = oldContext.getField(theMemberName);
				Object theVal = oldField.getValue();
				if (!Utils.isEmpty(theVal)) {
					FormField newField = newContext.getField(theMemberName);
					newField.setValue(theVal);
				}
		    }
		    else if (theMember instanceof FormContainer) {
		    	FormGroup theGrp =  (FormGroup) oldContext.getMember(theMemberName);
				FormField oldSearchFromField = theGrp.getField(AttributeFormFactory.SEARCH_FROM_FIELDNAME);
				Object from = oldSearchFromField.getValue();
				FormField oldSearchToField = theGrp.getField(AttributeFormFactory.SEARCH_TO_FIELDNAME);
				Object to = oldSearchToField.getValue();
				if(from != null || to != null) {
					theGrp =  (FormGroup) newContext.getMember(theMemberName);
					theGrp.getField(AttributeFormFactory.SEARCH_FROM_FIELDNAME).setValue(from);
					theGrp.getField(AttributeFormFactory.SEARCH_TO_FIELDNAME).setValue(to);
				}
		    }
		}
	}

	private void transferFullTextField(FormContext oldContext, FormContext newContext) {
		if(oldContext.hasMember(SearchFieldSupport.FULL_TEXT)) {
			FormField fullTextField = oldContext.getField(SearchFieldSupport.FULL_TEXT);

			Object theVal = fullTextField.getValue();
			if (!Utils.isEmpty(theVal)) {
				newContext.getField(SearchFieldSupport.FULL_TEXT).setValue(theVal);
				String relevantAndNegateMemberName =
					SearchFilterSupport.getRelevantAndNegateMemberName(SearchFieldSupport.FULL_TEXT);
				newContext.getField(relevantAndNegateMemberName).setValue(Boolean.TRUE);
			}
		}
	}

	private void transferColumnSelect(FormContext oldContext, FormContext newContext) {
		FormField oldColumnSelectField = SearchFieldSupport.getTableColumnsFields(oldContext);
		if (oldColumnSelectField != null) {
			Object theColumns = oldColumnSelectField.getValue();
			FormField newColumnSelectField = SearchFieldSupport.getTableColumnsFields(newContext);
			newColumnSelectField.setValue(theColumns);
		}
	}

	private void transferPublishingGroup(FormContext oldContext, FormContext newContext) {
		FormMember oldPublishingGroup = oldContext.getMember(QueryUtils.FORM_GROUP);
		oldContext.removeMember(oldPublishingGroup);
		for (Iterator<FormContainer> theIt = newContext.getGroups(); theIt.hasNext();) {
			FormGroup grp = (FormGroup) theIt.next();
			if (QueryUtils.FORM_GROUP.equals(grp.getName())) {
				newContext.removeMember(grp);
				break;
			}
		}
		newContext.addMember(oldPublishingGroup);
	}

	/**
	 * Returns a string with style information for a {@link FormField} representing the given
	 * {@link TLStructuredTypePart}. The style depends on the current value of the field and the associated
	 * checkbox.
	 */
    public String getStyleInformation(TLStructuredTypePart aMA, String aDomain) {
		String theMemberName = MetaAttributeGUIHelper.getAttributeIDCreate(aMA, aDomain);
    	String theCheckboxName = SearchFilterSupport.getRelevantAndNegateMemberName(aMA, aDomain);
    	return getStyleInformation(theMemberName, theCheckboxName);

    }

	/**
	 * Additional CSS style information for the search field with the given name.
	 *
	 * @param aMemberName
	 *        The name of the search field.
	 * @param aCheckboxName
	 *        The name of the corresponding checkbox for "relevant and negate".
	 * @return The CSS style to use, the default implementation returns the empty string.
	 */
    public String getStyleInformation(String aMemberName, String aCheckboxName) {
    	return "";
    }

	/**
	 * The result columns selected in the given search form.
	 *
	 * @param aContext
	 *        The search form.
	 * @return The names of the columns selected by the user, or {@link #getResultColumns()}, if
	 *         the form has no column selection field.
	 */
    public List<String> getResultColumns(FormContext aContext) {
		SelectField tableColumnsFields = SearchFieldSupport.getTableColumnsFields(aContext);
		if (tableColumnsFields != null) {
			return this.getSearchFieldSupport().getColumnList(tableColumnsFields, this.getSearchMetaElement());
        }
        else {
            return getResultColumns();
        }
    }

    @Override
	public CommandHandler getDefaultCommand() {
		CommandHandler configuredCommand = super.getDefaultCommand();
		if (configuredCommand != null) {
			return configuredCommand;
		}
		return getCommandById(SearchCommandHandler.COMMAND_ID);
    }

    /**
	 * a list of attribute paths that can be used in
	 *         {@link Utils#getValueByPath(String, com.top_logic.model.TLObject)}, never
	 *         <code>null</code>.
	 */
    protected List<String> getAdditionalResultColumnOptions() {
        return this.additionalResultColumnOptions;
    }

    /**
     * a map of Strings (ids) to {@link DerivedAttributeDescription}s, never <code>null</code>.
     */
    public Map getDerivedAttributes() {
        if (this.derivedAttributeDescriptionProvider == null) {
            return Collections.EMPTY_MAP;
        } else {
            return this.derivedAttributeDescriptionProvider.getDerivedAttributeDescription();
        }
    }

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

    @Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		boolean superResult = super.receiveModelCreatedEvent(model, changedBy);
    	if (model instanceof StoredQuery) {
			boolean queryFieldUpdated = updateQueryField((StoredQuery) model, false);
			return queryFieldUpdated && superResult;
    	}
		return superResult;
    }

	@Override
	protected Map<String, ChannelSPI> programmaticChannels() {
		return ColumnsChannel.COLUMNS_MODEL_AND_SELECTION_CHANNEL;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		ChannelLinking channelLinking = getChannelLinking(((Config) getConfig()).getColumns());
		columnsChannel().linkChannel(log, this, channelLinking);
		columnsChannel().addListener(COLUMNS_LISTENER);
	}

	ComponentChannel columnsChannel() {
		return getChannel(ColumnsChannel.NAME);
	}

	/**
	 * Updates the stored query field to reflect recent changes.
	 * 
	 * @param query
	 *        The query which was deleted, created, or modified.
	 * @param delete
	 *        whether the given query was deleted.
	 * 
	 * @return Whether the stored query field may be changed.
	 */
	private boolean updateQueryField(StoredQuery query, boolean delete) {
		if (!hasFormContext()) {
			// No FormContext -> no StoredQuery field -> no update
			return false;
		}
		if (!getQuerySupport()) {
			// No stored queries here.
			return false;
		}
		SelectField theSQField = (SelectField)this.getFormContext().getField(SearchFieldSupport.STORED_QUERY);
		Person      thePerson  = TLContext.getContext().getCurrentPersonWrapper();
        List        theSQs     = StoredQuery.getStoredQueries(this.metaElement, thePerson, true);
        Object      theSel     = theSQField.getSingleSelection();
        
		if (delete && theSel != null && theSel.equals(query)) {
        	theSQField.setAsSingleSelection(null);
        }
        theSQField.setOptions(theSQs);
        theSQField.setDisabled(false);
		return true;
	}
	
	/**
	 * Description of an attribute that is reached from the searched type via an access path.
	 */
	public static class DerivedAttributeDescription {

	    /** Used to fetch values from a {@link StoredQuery} and defines the domain in {@link AttributeUpdate#getDomain()} */ 
        private String         path;

        private TLStructuredTypePart  target;

		/**
		 * Creates a {@link DerivedAttributeDescription}.
		 *
		 * @param aPath
		 *        The access path as list of attribute names.
		 * @param aTargetMA
		 *        The attribute at the end of the path.
		 */
        public DerivedAttributeDescription(List<String> aPath, TLStructuredTypePart aTargetMA) {
            this.path   = StringServices.toString(aPath, ".");
            this.target = aTargetMA;
        }

		/**
		 * Creates a {@link DerivedAttributeDescription}.
		 *
		 * @param aPath
		 *        The dot separated access path.
		 * @param aTargetMA
		 *        The attribute at the end of the path.
		 */
        public DerivedAttributeDescription(String aPath, TLStructuredTypePart aTargetMA) {
            this.path   = aPath;
            this.target = aTargetMA;
        }

		/**
		 * The attribute at the end of the {@link #getAccessPath() access path}.
		 */
        public TLStructuredTypePart getTarget() {
            return (this.target);
        }
        
		/**
		 * The dot separated access path, also used as domain of the search field.
		 */
        public String getAccessPath() {
            return this.path;
        }
        
		/**
		 * The name of the "relevant and negate" checkbox of the search field.
		 */
        public String getRelAndUseName() {
            return SearchFilterSupport.getRelevantAndNegateMemberName(this.getTarget(), this.getAccessPath());
        }
        
		/**
		 * The name of the search field for this attribute.
		 */
        public String getFieldName() {
			return MetaAttributeGUIHelper.getAttributeIDCreate(this.getTarget(), this.getAccessPath());
        }
        
		/**
		 * The label of the search field for this attribute.
		 *
		 * @param aME
		 *        The searched type.
		 */
        public String getLabel(TLClass aME) {
            return TLModelI18N.getI18NName(aME, this.getAccessPath(), this.getTarget());
        }
    }

    /**
     * Suppress the {@link InputListener} and {@link InputColorListener} when set.
     * 
     * This is needed by Subclass that must update the {@link FormField}s later and
     * will be hindered by these listeners do do so.
     */
    public boolean supressListeners() {
        return false;
    }

	/**
	 * A delegate used to create {@link DerivedAttributeDescription}s.
	 */
    public static interface DerivedAttributeDescriptionProvider {
        
        /**
         * TODO KBU/TSA the key in the returned Map is not used anywhere?
         */
        public Map<String, DerivedAttributeDescription> getDerivedAttributeDescription();
    }
}
