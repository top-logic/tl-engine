/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.tabbar.TabBarModel;

/**
 * {@link Card}s are used as selectable objects for {@link DefaultDeckPaneModel}s and for
 * {@link TabBarModel}s
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Card {

	/**
	 * The technical name of this {@link Card}.
	 */
	public String getName();

	/**
	 * The object this {@link Card} represents.
	 * 
	 * @return The object this {@link Card} displays, never <code>null</code>.
	 */
	public Object getContent();

	/**
	 * Optional additional information related to this {@link Card}.
	 * 
	 * <p>
	 * This object can e.g. be used to render a tab.
	 * </p>
	 * 
	 * @return An object containing additional information about the {@link Card}, may be
	 *         <code>null</code>.
	 */
	public Object getCardInfo();

	/**
	 * Writes some information to identify this {@link Card} in the deck it belongs to. It is
	 * possible that two different {@link Card}s in a deck produce the same output.
	 */
	public void writeCardInfo(DisplayContext context, Appendable out) throws IOException;

}
