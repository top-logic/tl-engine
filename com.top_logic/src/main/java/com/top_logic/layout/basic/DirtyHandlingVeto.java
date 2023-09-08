/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Collection;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.form.FormHandler;

/**
 * {@link VetoException} which opens a {@link DirtyHandling} dialog.
 * 
 * <p>
 * This {@link VetoException} is designed to have a veto against any operation due to changed
 * {@link FormHandler}s. Typical usage is fetching a certain collection of {@link FormHandler},
 * checking them for modifications, and, if any is changed, throwing an {@link DirtyHandlingVeto}.
 * In such case the typical "U have unsaved changes" dialog is displayed.
 * </p>
 * 
 * @see DirtyHandling#checkVeto(com.top_logic.layout.basic.check.CheckScope)
 * @see DirtyHandling#checkVeto(Collection)
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DirtyHandlingVeto extends VetoException {

	private final Collection<? extends ChangeHandler> _dirtyHandlers;

	/**
	 * Creates a new {@link DirtyHandlingVeto}.
	 * 
	 * @param dirtyHandlers
	 *        The {@link FormHandler} which are "dirty". The opened confirm dialog bases on these
	 *        handlers.
	 */
	public DirtyHandlingVeto(Collection<? extends ChangeHandler> dirtyHandlers) {
		_dirtyHandlers = dirtyHandlers;
	}

	@Override
	public void process(WindowScope window) {
		DirtyHandling.getInstance().openConfirmDialog(getContinuationCommand(), _dirtyHandlers, window);
	}

}
