/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import static com.top_logic.layout.form.values.Fields.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Component displaying a {@link AttributedSearchResultSet search result}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SearchResultTable extends FormComponent {

	/**
	 * Better name for the empty {@link String} array when working with
	 * {@link ObjectTableModel#ObjectTableModel(String[], TableConfiguration, List)} and related
	 * methods.
	 */
	public static final String[] NO_COLUMNS = {};

	/**
	 * Better name for the empty {@link List} when working with
	 * {@link ObjectTableModel#ObjectTableModel(String[], TableConfiguration, List)} and related
	 * methods.
	 */
	public static final List<?> NO_ROWS = Collections.emptyList();

	/** The {@link FormField} name of the {@link TableField} that displays the search results. */
	private static final String NAME_TABLE_FIELD_RESULT = "result";

	/** The name in the component configuration of the table that shows the search results. */
	static final String NAME_TABLE_CONFIG_RESULT = "result";

	private static final String RESULT_TABLE_CONFIG_KEY_PREFIX = "ResultTable_for_types__";

	private static final Set<TLClass> NO_TYPES = Collections.emptySet();

	/**
	 * Creates a {@link SearchResultTable} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SearchResultTable(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = new FormContext(this);

		installSearchResult(result);

		return result;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof AttributedSearchResultSet;
	}

	@Override
	protected void handleNewModel(Object newModel) {
		installSearchResult(getFormContext());
	}

	private void installSearchResult(FormContext result) {
		AttributedSearchResultSet resultSet = (AttributedSearchResultSet) getModel();
		if (resultSet == null) {
			installSearchResult(result, NO_TYPES, NO_ROWS);
		} else {
			installSearchResult(result, resultSet.getTypes(), resultSet.getResultObjects());
		}
	}

	/**
	 * Create a {@link TableField} and set it.
	 * <p>
	 * None of the parameters is allowed to be null.
	 * </p>
	 */
	private void installSearchResult(FormContext formContext, Set<? extends TLClass> types, List<?> rows) {
		if (formContext.hasMember(NAME_TABLE_FIELD_RESULT)) {
			formContext.removeMember(NAME_TABLE_FIELD_RESULT);
		}
		ConfigKey configKey = getResultTableConfigKey(types);

		String[] columns = NO_COLUMNS;
		TableConfiguration config = createTableConfig(types);
		TableModel tableModel = tableModel(config, NAME_TABLE_CONFIG_RESULT, columns, rows);

		table(formContext, NAME_TABLE_FIELD_RESULT, tableModel, configKey);
	}

	/**
	 * Get the {@link ConfigKey} for the result table, depending on what types the user searched
	 * for.
	 * 
	 * @param searchedTypes
	 *        The types that the user searched for. Is not allowed to be or contain null. It is not
	 *        accessed after this method returned. It is not changed.
	 * @return Never null.
	 */
	private ConfigKey getResultTableConfigKey(Collection<? extends TLType> searchedTypes) {
		if (searchedTypes.isEmpty()) {
			return ConfigKey.none();
		}
		return ConfigKey.part(this, RESULT_TABLE_CONFIG_KEY_PREFIX + toConfigKeyString(searchedTypes));
	}

	/**
	 * Converts the given {@link List} of {@link TLType}s into a {@link String} that can be used in
	 * a {@link ConfigKey}.
	 * 
	 * @param types
	 *        The order of the elements in the collection does not matter in the base
	 *        implementation. But subclasses can change that. Is not allowed to be or contain null.
	 *        It is not accessed after this method returned. It is not changed.
	 * @return Never null.
	 */
	private String toConfigKeyString(Collection<? extends TLType> types) {
		List<String> typeNames = toQualifiedNames(types);
		Collections.sort(typeNames);
		return StringServices.join(typeNames, "__");
	}

	/**
	 * @param types
	 *        Is not allowed to be or contain null. It is not accessed after this method returned.
	 *        It is not changed.
	 * @return Never null. Never contains null.
	 */
	private static List<String> toQualifiedNames(Collection<? extends TLType> types) {
		List<String> typeNames = new ArrayList<>();
		for (TLType type : types) {
			typeNames.add(TLModelUtil.qualifiedName(type));
		}
		return typeNames;
	}

	/**
	 * @param tableName
	 *        The name of the table in the configuration of this component.
	 */
	private ObjectTableModel tableModel(TableConfiguration initialConfig, String tableName, String[] columns,
			List<?> rows) {
		return new ObjectTableModel(columns, initialConfig, rows);
	}

	/**
	 * Create the {@link TableConfiguration} for the result table.
	 * 
	 * @param searchedTypes
	 *        The types that the user searched for. Is not allowed to be or contain null. It is not
	 *        accessed after this method returned. It is not changed.
	 */
	private TableConfiguration createTableConfig(Set<? extends TLClass> searchedTypes) {
		TableConfiguration config = TableConfigurationFactory.table();
		if (!searchedTypes.isEmpty()) {
			createTableConfigProvider(searchedTypes).adaptConfigurationTo(config);
			config.setDefaultSortOrder(singletonList(SortConfigFactory.ascending(TLClass.NAME_ATTRIBUTE)));
		}
		adaptTableConfiguration(NAME_TABLE_CONFIG_RESULT, config);
		return config;
	}

	/**
	 * Create the {@link TableConfigurationProvider} for the result table.
	 * 
	 * @param types
	 *        The types that the user searched for. Is not allowed to be or contain null. It is not
	 *        accessed after this method returned. It is not changed.
	 * @return Never null.
	 */
	private TableConfigurationProvider createTableConfigProvider(Set<? extends TLClass> types) {
		return new GenericTableConfigurationProvider(types);
	}

}
