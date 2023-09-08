/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.List;

import com.top_logic.layout.component.model.ModelChangeListener;

/**
 * The class {@link ObservableList} is a {@link List} which allows {@link ModelChangeListener} to
 * inform about changes of the content of the list.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ObservableList extends List {

	/**
	 * This method adds the given {@link ModelChangeListener} as listener to be informed about a
	 * change of this {@link ObservableList}.
	 * <p>
	 * The {@link ModelChangeListener} will be called after a change of this {@link ObservableList}
	 * with <code>this</code> as model, the list of added objects before the change, and the list
	 * of added objects after the change takes place.
	 * </p>
	 */
	public void addModelChangedListener(ModelChangeListener listener);

	/**
	 * This method removes the given listener as listener for this {@link ObservableList}. The
	 * given listener will not longer be informed about a change of the models.
	 */
	public void removeModelChangedListener(ModelChangeListener listener);

}
