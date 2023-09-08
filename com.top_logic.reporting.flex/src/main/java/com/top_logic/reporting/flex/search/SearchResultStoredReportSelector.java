/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.layout.meta.expression.ExpressionSelectorComponent;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.element.layout.meta.search.SearchCommandHandler;
import com.top_logic.element.layout.meta.search.SearchFieldSupport;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.search.handler.DeleteReportCommand;
import com.top_logic.reporting.flex.search.handler.ResetReportCommand;
import com.top_logic.reporting.flex.search.handler.SaveReportCommand;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Selector-component for new search-reporting. The current search-result and existing
 * stored-querries can be used as datasource. According to the type of the currently displayed
 * objects a SelectField with existing stored-reports is displayed.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SearchResultStoredReportSelector extends ExpressionSelectorComponent<FlexReport>
		implements ValueListener {

	/**
	 * Config-interface for {@link SearchResultStoredReportSelector}.
	 */
	public interface Config extends ExpressionSelectorComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * the name of the search-result-table to get the current objects from
		 */
		@Mandatory
		ComponentName getResultTable();

		/**
		 * the name of the search-input-component to execute search-queries
		 */
		@Mandatory
		ComponentName getSearchInputComponent();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			ExpressionSelectorComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(DeleteReportCommand.COMMAND_ID);
			registry.registerButton(ResetReportCommand.COMMAND_ID);
			registry.registerButton(SaveReportCommand.COMMAND_ID);
		}

	}

	@CalledFromJSP
	public static final String REPORT_SELECTION_FIELD = "reportSelection";

	private ComponentName _resultTable;

	private ComponentName _inputComponent;

	/**
	 * Config-constructor for {@link SearchResultStoredReportSelector}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public SearchResultStoredReportSelector(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_resultTable = config.getResultTable();
		_inputComponent = config.getSearchInputComponent();
	}

	@Override
	public SearchResultChartConfigComponent getTarget() {
		return (SearchResultChartConfigComponent) super.getTarget();
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = super.createFormContext();

		SelectField queryField = FormFactory.newSelectField(SearchFieldSupport.STORED_QUERY, getStoredQueries());
		final Resources resources = Resources.getInstance();
		queryField.setEmptyLabel(resources.getString(I18NConstants.SEARCH_RESULT));
		queryField.setOptionLabelProvider(new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				StoredQuery query = (StoredQuery) object;
				TLClass me = query.getQueryMetaElement();
				String name = query.getName();
				String type = MetaResourceProvider.INSTANCE.getLabel(me);
				return resources.getMessage(I18NConstants.STORED_QUERY, name, type);
			}
		});
		queryField.addValueListener(this);

		result.addMember(queryField);
		return result;
	}

	private List<StoredQuery> getStoredQueries() {
		Person user = TLContext.getContext().getCurrentPersonWrapper();
		return CollectionUtil.dynamicCastView(StoredQuery.class, StoredQuery.getStoredQueries(null, user, true));
	}

	@Override
	protected List<FlexReport> getStoredExpressions() {
		return getStoredReports(getTypes());
	}

	private List<FlexReport> getStoredReports(Set<? extends TLClass> types) {
		if (types.isEmpty()) {
			return Collections.emptyList();
		}
		Person user = TLContext.getContext().getCurrentPersonWrapper();
		return CollectionUtil.dynamicCastView(FlexReport.class,
			FlexReport.getStoredReports(user, true, types, NewStoredConfigChartReportComponent.STORED_REPORT_VERSION));
	}

	private Set<? extends TLClass> getTypes() {
		if (getModel() instanceof AttributedSearchResultSet) {
			return ((AttributedSearchResultSet) getModel()).getTypes();
		}
		return Collections.emptySet();
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof AttributedSearchResultSet;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		if (metaElementChange(oldModel, newModel)) {
			if (hasFormContext() && newModel != null) {
				updateSelectorField(((AttributedSearchResultSet) newModel).getTypes());
				updateQueryField();
			}
		}
		super.afterModelSet(oldModel, newModel);
	}

	private void updateQueryField() {
		if (isActive()) {
			SelectField field = getQueryField();
			field.setAsSingleSelection(null);
		}
	}

	private void updateSelectorField(Set<? extends TLClass> types) {
		SelectField field = getSelectorField();
		field.setAsSingleSelection(null);
		field.setOptions(getStoredReports(types));
	}

	private boolean metaElementChange(Object oldModel, Object newModel) {
		return !types(oldModel).equals(types(newModel));
	}

	private Set<? extends TLClass> types(Object model) {
		if (model instanceof AttributedSearchResultSet) {
			return ((AttributedSearchResultSet) model).getTypes();
		}
		return Collections.emptySet();
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (SearchFieldSupport.STORED_QUERY.equals(field.getName())) {
			StoredQuery query = (StoredQuery) CollectionUtil.getFirst(newValue);
			AttributedSearchResultSet set = null;
			if (query == null) {
				set = getCurrentSearchResult();
			}
			else {
				set = executeQuery(query);
			}
			setActive(false);
			setModel(set);
			setActive(true);
			getTarget().setModel(set);
		}
	}

	/**
	 * Executes the given {@link StoredQuery}.
	 */
	protected AttributedSearchResultSet executeQuery(StoredQuery query) {
		AttributedSearchComponent searchComponent = getSearchInputComponent();
		SearchCommandHandler theSearchHandler = (SearchCommandHandler) CommandHandlerFactory.getInstance().getHandler(SearchCommandHandler.COMMAND_ID);
		searchComponent.setSearchMetaElement(query.getQueryMetaElement());
		searchComponent.setModel(query);
		return (AttributedSearchResultSet) theSearchHandler.search(searchComponent, searchComponent.getFormContext(), Collections.EMPTY_MAP, new DefaultProgressInfo());
	}

	/**
	 * Returns the configured {@link AttributedSearchComponent} to execute search queries.
	 */
	protected final AttributedSearchComponent getSearchInputComponent() {
		return (AttributedSearchComponent) getMainLayout().getComponentByName(_inputComponent);
	}

	private AttributedSearchResultSet getCurrentSearchResult() {
		AttributedSearchResultSet result = (AttributedSearchResultSet) getResultTable().getModel();
		if (result == null) {
			return new AttributedSearchResultSet(Collections.<TLObject> emptyList(), Collections.emptySet(),
				Collections.<String> emptyList(), Collections.emptyList());
		}
		return result;
	}

	private LayoutComponent getResultTable() {
		return getMainLayout().getComponentByName(_resultTable);
	}

	/**
	 * Compatibility redirect to {@link #loadExpression(FlexReport)}.
	 */
	public void loadReport(FlexReport report) {
		loadExpression(report);
	}

	@Override
	public void loadExpression(FlexReport report) {
		if (report != null) {
			String string = (String) report.getValue(FlexReport.ATTRIBUTE_REPORT);
			getTarget().loadConfig(string);
		}
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		if (!hasFormContext()) {
			return false;
		}
		boolean result = super.receiveModelCreatedEvent(model, changedBy);
		if (model instanceof StoredQuery) {
			SelectField field = getQueryField();
			Object currentSelection = field.getSingleSelection();
			setActive(false);
			field.setOptions(getStoredQueries());
			setActive(true);
		}
		return result;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		if (!hasFormContext()) {
			return false;
		}
		boolean result = super.receiveModelDeletedEvent(models, changedBy);
		if (models.stream().anyMatch(StoredQuery.class::isInstance)) {
			SelectField field = getQueryField();
			handleDeletion(models, field);
		}
		return result;
	}

	@Override
	public String getExpressionSelectorName() {
		return REPORT_SELECTION_FIELD;
	}

	private SelectField getQueryField() {
		if (!hasFormContext()) {
			return null;
		}
		return (SelectField) getFormContext().getMember(SearchFieldSupport.STORED_QUERY);
	}

	public FlexReport getCurrentSelection() {
		SelectField field = getSelectorField();
		if (field == null) {
			return null;
		}
		return (FlexReport) field.getSingleSelection();
	}

	@Override
	protected boolean isSupportedVersion(FlexReport model) {
		return model.getFormatVersion() == NewStoredConfigChartReportComponent.STORED_REPORT_VERSION;
	}

	@Override
	protected Class<FlexReport> getWrapperClass() {
		return FlexReport.class;
	}

}
