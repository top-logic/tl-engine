/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import java.util.Comparator;

import com.top_logic.element.workItem.favorites.ModifyFavoritesAccessor.WrapperFavoriteState;

/**
 * Compare CommandFields containing {@link ModifyFavoritesExecutable} by the State of the
 * executable.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ModifiyFavoritesCommandComparator implements Comparator<WrapperFavoriteState> {

	/**
	 * Singleton instance of {@link ModifiyFavoritesCommandComparator}
	 */
	public static final ModifiyFavoritesCommandComparator INSTANCE = new ModifiyFavoritesCommandComparator();

	@Override
	public int compare(WrapperFavoriteState o1, WrapperFavoriteState o2) {
		return o1._state.compareTo(o2._state);
	}

}
