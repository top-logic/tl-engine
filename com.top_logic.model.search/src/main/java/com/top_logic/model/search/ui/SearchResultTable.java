/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;

/**
 * Component displaying a {@link AttributedSearchResultSet search result}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SearchResultTable extends TableComponent {

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
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);

		// Build new table configuration, since types may have changed.
		invalidate();
	}

	@Override
	protected Set<? extends TLType> getTypes() {
		AttributedSearchResultSet resultSet = getResultSet(getModel());
		if (resultSet == null) {
			return NO_TYPES;
		} else {
			return resultSet.getTypes();
		}
	}

	private static AttributedSearchResultSet getResultSet(Object businessModel) {
		return (AttributedSearchResultSet) businessModel;
	}

	/**
	 * Table builder for {@link SearchResultTable}.
	 */
	public static class Builder implements ListModelBuilder {
		/**
		 * Singleton {@link SearchResultTable.Builder} instance.
		 */
		public static final SearchResultTable.Builder INSTANCE = new SearchResultTable.Builder();

		private Builder() {
			// Singleton constructor.
		}

		@Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return aModel instanceof AttributedSearchResultSet;
		}

		@Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
			AttributedSearchResultSet resultSet = getResultSet(businessModel);
			if (resultSet == null) {
				return NO_ROWS;
			} else {
				return resultSet.getResultObjects();
			}
		}

		@Override
		public boolean supportsListElement(LayoutComponent component, Object candidate) {
			return true;
		}

		@Override
		public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
			return component.getModel();
		}
	}

}
