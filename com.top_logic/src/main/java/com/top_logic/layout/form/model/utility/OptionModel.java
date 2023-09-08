/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.List;

import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Wrapper for either a {@link List} of options or a {@link TLTreeModel} of structured options.
 * 
 * @author <a href="mailto:STS@top-logic.com">STS</a>
 */
public interface OptionModel<T> extends Iterable<T> {
	
	/** Constant that is used by {@link #getOptionCount()}, if there are infinite many options. */
	int INFINITE = Integer.MAX_VALUE;

	/**
	 * This method returns the options of a select field
	 * 
	 * @return options of a select field
	 */
	public Object getBaseModel();
	
	/**
	 * Determines and returns the amount of options of an {@link OptionModel}.
	 * 
	 * @return The amount of options of this {@link OptionModel}. If there are infinite options,
	 *         {@link #INFINITE} is returned.
	 * 
	 * @see #INFINITE
	 */
	int getOptionCount();

	/**
	 * The option to choose by default for a mandatory property that is automatically filled in the
	 * UI.
	 */
	default T getDefaultValue() {
		return null;
	}

	/**
	 * Adds the given {@link OptionModelListener} to this {@link OptionModel}.
	 * 
	 * @return Whether the given listener was added.
	 */
	public boolean addOptionListener(OptionModelListener listener);

	/**
	 * Removes the given {@link OptionModelListener} from this {@link OptionModel}.
	 * 
	 * @return Whether the given listener was removed.
	 */
	public boolean removeOptionListener(OptionModelListener listener);
}
