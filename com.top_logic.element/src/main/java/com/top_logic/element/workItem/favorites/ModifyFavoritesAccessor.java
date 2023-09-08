/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import com.top_logic.element.workItem.favorites.ModifyFavoritesExecutable.State;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.util.Resources;

/**
 * Return {@link WrapperFavoriteState} if an object is marked as favorite.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ModifyFavoritesAccessor extends ReadOnlyAccessor<Wrapper> {

	/**
	 * Single instance of {@link ModifyFavoritesAccessor}
	 */
	public static final ModifyFavoritesAccessor INSTANCE = new ModifyFavoritesAccessor();

	@Override
	public Object getValue(Wrapper object, String property) {
		State state = ModifyFavoritesExecutable.getFavoriteState(object);
		return new WrapperFavoriteState(object, state);
	}

	/**
	 * Simple transfer object that represents a cell object.
	 */
	public static class WrapperFavoriteState {
		public final Wrapper _model;

		public final State _state;

		/**
		 * Create new {@link WrapperFavoriteState}
		 */
		public WrapperFavoriteState(Wrapper model, State state) {
			_model = model;
			_state = state;
		}
	}

	public static class WrapperFavoriteStateResourceProvider extends AbstractResourceProvider {

		@Override
		public String getLabel(Object object) {
			if (((WrapperFavoriteState) object)._state == State.IS_FAVORITE) {
				return Resources.getInstance().getString(I18NConstants.IS_FAVORITE);
			}
			return Resources.getInstance().getString(I18NConstants.IS_NOT_FAVORITE);
		}

		@Override
		public String getTooltip(Object object) {
			if (((WrapperFavoriteState) object)._state == State.IS_FAVORITE) {
				return Resources.getInstance().getString(I18NConstants.IS_FAVORITE);
			}
			return Resources.getInstance().getString(I18NConstants.IS_NOT_FAVORITE);
		}

	}

}
