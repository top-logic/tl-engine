/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.workItem.favorites.ModifyFavoritesAccessor.WrapperFavoriteState;
import com.top_logic.element.workItem.favorites.ModifyFavoritesExecutable.State;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.SimpleDisplayValue;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.util.Resources;

/**
 * {@link TableFilterProvider} for {@link WrapperFavoriteState}s
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class FavoritesFilterProvider extends AbstractTableFilterProvider {

	private static Filter<Object> IS_FAVORITE =
		new Filter<>() {
			@Override
			public boolean accept(Object object) {
				return object != null && ((WrapperFavoriteState) object)._state == State.IS_FAVORITE;
			}
		};

	private static Filter<Object> IS_NOT_FAVORITE =
		new Filter<>() {
			@Override
			public boolean accept(Object object) {
				return object == null || ((WrapperFavoriteState) object)._state != State.IS_FAVORITE;
			}
		};

	private static DisplayValue IS_FAVORITE_DISPLAY = new SimpleDisplayValue() {
		@Override
		public String get(DisplayContext context) {
			return Resources.getInstance().getString(I18NConstants.IS_FAVORITE);
		}
	};

	private static DisplayValue IS_NOT_FAVORITE_DISPLAY = new SimpleDisplayValue() {
		@Override
		public String get(DisplayContext context) {
			return Resources.getInstance().getString(I18NConstants.IS_NOT_FAVORITE);
		}
	};

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel, String filterPosition) {
		List<ConfiguredFilter> filters = new ArrayList<>(2);

		filters.add(new StaticFilterWrapper(IS_FAVORITE, IS_FAVORITE_DISPLAY));
		filters.add(new StaticFilterWrapper(IS_NOT_FAVORITE, IS_NOT_FAVORITE_DISPLAY));

		return filters;
	}
}
