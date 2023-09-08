/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * {@link TableFilterProvider} for {@link Boolean} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanTableFilterProvider implements TableFilterProvider {

	static final EqualsFilter TRUE_INSTANCE = new EqualsFilter(Boolean.TRUE);

	static final EqualsFilter FALSE_INSTANCE = new EqualsFilter(Boolean.FALSE);

	private static final Filter<Object> FALSE_OR_NULL_INSTANCE = new Filter<>() {
		@Override
		public boolean accept(Object anObject) {
			return anObject == null || Boolean.FALSE.equals(anObject);
		}
	};

	static final List<Class<?>> TYPES = Collections.singletonList(Boolean.class);

	private boolean _tristate;

	/**
	 * Creates a {@link BooleanTableFilterProvider}.
	 * 
	 * @param tristate
	 *        Whether the column value cannot be empty.
	 */
	protected BooleanTableFilterProvider(boolean tristate) {
		super();
		_tristate = tristate;
	}

	@Override
	public TableFilter createTableFilter(TableViewModel aTableModel, String filterPosition) {
		List<ConfiguredFilter> valueFilters = new ArrayList<>(3);

		valueFilters.add(new StaticFilterWrapper(
			TRUE_INSTANCE, new ResourceText(I18NConstants.TRUE_LABEL),
			TYPES, new StaticFilterWrapperViewBuilder()));

		Filter<Object> falseFilter = _tristate ? FALSE_INSTANCE : FALSE_OR_NULL_INSTANCE;
		valueFilters.add(new StaticFilterWrapper(
			falseFilter, new ResourceText(I18NConstants.FALSE_LABEL),
			TYPES, new StaticFilterWrapperViewBuilder()));

		PopupDialogModel dialogModel =
			new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
				new DefaultLayoutData(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH),
					DisplayUnit.PIXEL, 100, 0, DisplayUnit.PIXEL, 100,
					Scrolling.NO),
				1);
		FilterDialogBuilder manager = new PopupFilterDialogBuilder(dialogModel);

		TableFilter booleanFilter = new TableFilter(manager);
		booleanFilter.addSubFilterGroup(valueFilters);
		if (_tristate) {
			booleanFilter.addSubFilter(TableFilterProviderUtil.createNoValueFilter(), true);
		}

		return booleanFilter;
	}

}
