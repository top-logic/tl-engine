/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.table;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScopeFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.model.TransientObject;

/**
 * {@link ModelNamingScheme} identifying a row in a {@link TableData} by its label in the
 * <code>name</code> or <code>_self</code> column.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabeledTableRowNaming extends ModelNamingScheme<TableData, Object, LabeledTableRowNaming.Name> {

	/**
	 * {@link ModelName} created by a {@link LabeledTableRowNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The label of the row in its table context.
		 */
		String getName();

		/**
		 * @see #getName()
		 */
		void setName(String value);

	}

	@Override
	public Class<? extends Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Class<TableData> getContextClass() {
		return TableData.class;
	}

	@Override
	public Object locateModel(ActionContext context, TableData valueContext, Name name) {
		TableModel tableModel = valueContext.getTableModel();

		LabelProvider provider = ValueScopeFactory.getLabelProvider(tableModel);
		if (provider == null) {
			throw ApplicationAssertions.fail(name,
				"No label provider can be retrieved for the context table.");
		}

		String rowName = name.getName();
		Object result = null;
		Collection<?> rows = tableModel.getAllRows();
		for (Object candidate : rows) {
			if (rowName.equals(provider.getLabel(candidate))) {
				if (result == null) {
					result = candidate;
				} else {
					throw ApplicationAssertions.fail(name,
						"Non-unique match for table row with label '" + rowName + "', candidates: '" + result
							+ "', '" + candidate + "'.");
				}
			}
		}

		if (result == null) {
			throw ApplicationAssertions.fail(name,
				"Table row with label '" + rowName + "' not found, available rows are " + rowLabels(provider, rows)
					+ ".");
		}

		return result;
	}

	private StringBuilder rowLabels(LabelProvider provider, Collection<?> rows) {
		int maxLabels = 10;
		StringBuilder result = new StringBuilder();
		int index = 0;
		for (Object row : rows) {
			if (index > maxLabels) {
				result.append("... (" + (rows.size() - maxLabels) + " more)");
			}
			if (index > 0) {
				result.append(", ");
			}
			result.append("'");
			result.append(provider.getLabel(row));
			result.append("'");

			index++;
		}
		return result;
	}

	@Override
	protected Maybe<Name> buildName(TableData valueContext, Object model) {
		if (model instanceof Collection) {
			/* Avoid exceptions: If a multi selection is recorded in a table, the parameter "model"
			 * is a collection. But the LabelProviders in tables usually don't support Collections
			 * of row objects, which causes ClassCastExceptions. To avoid these exceptions, reject
			 * all models that are Collections. The exceptions are thrown in the LabelProvider built
			 * in the ValueScopeFactory, when the accessor is applied to the model. */
			return Maybe.none();
		}
		if (model instanceof TransientObject) {
			// Most probably, a transient object cannot be identified in a stable way.
			return Maybe.none();
		}
		TableModel tableModel = valueContext.getTableModel();

		LabelProvider provider = ValueScopeFactory.getLabelProvider(tableModel);
		if (provider == null) {
			return Maybe.none();
		}

		String rowName = provider.getLabel(model);
		if (StringServices.isEmpty(rowName)) {
			return Maybe.none();
		}

		Collection<?> rows = tableModel.getAllRows();
		for (Object candidate : rows) {
			if (rowName.equals(provider.getLabel(candidate))) {
				if (candidate != model) {
					// Match not unique.
					return Maybe.none();
				}
			}
		}

		Name result = createName();
		result.setName(rowName);
		return Maybe.some(result);
	}

}
