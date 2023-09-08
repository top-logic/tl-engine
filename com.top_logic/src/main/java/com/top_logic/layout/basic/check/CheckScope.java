/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.layout.form.FormHandler;

/**
 * {@link CheckScope}s are used in dirty handling on client side. They return a
 * collection of {@link FormHandler}s which will be checked whether they have
 * changes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CheckScope {

	/** A {@link CheckScope} with no affected Handlers */
	public static final CheckScope NO_CHECK = new CheckScope() {

		/**
		 * Constantly returns an empty {@link Collection}.
		 * 
		 * @see CheckScope#getAffectedFormHandlers()
		 */
		@Override
		public final Collection<? extends ChangeHandler> getAffectedFormHandlers() {
			return Collections.emptyList();
		}
	};

	/**
	 * Returns a collection of {@link FormHandler}s. All {@link FormHandler} are
	 * potentially affected by the action this {@link CheckScope} is added to.
	 * 
	 * 
	 * @return a {@link Collection} of {@link FormHandler}s. never
	 *         <code>null</code>
	 */
	public Collection<? extends ChangeHandler> getAffectedFormHandlers();

}
