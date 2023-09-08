/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import com.top_logic.element.workItem.favorites.ModifyFavoritesAccessor.WrapperFavoriteState;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * Create a column control that allows to mark a {@link Wrapper} as favorite.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ModifyFavoritesControlProvider extends DefaultFormFieldControlProvider {

	/**
	 * Name of favorite column
	 */
	public static final String COLUMN_NAME = "favorites";

	@Override
	public Control createControl(Object model, String style) {
		if (!(model instanceof WrapperFavoriteState)) {
			return null;
		}

		WrapperFavoriteState state = (WrapperFavoriteState) model;

		CommandField member = ModifyFavoritesExecutable.createField(state._model, state._state, COLUMN_NAME);
		member.setInheritDeactivation(false);

		return (Control) member.visit(this, style);
	}
}
