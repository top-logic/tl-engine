/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration of an element displaying a list of objects, naming the displayed object its
 * scripts are evaluated for.
 *
 * @implNote The scripts declare the object with
 *           {@link com.top_logic.layout.view.channel.Supplies} renamed by {@link #ELEMENT_AS}.
 */
public interface ElementAsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getElementAs()}. */
	String ELEMENT_AS = "element-as";

	/** Name of the variable holding the object when no other name is given. */
	String DEFAULT_ELEMENT_AS = "element";

	/**
	 * The name of the variable holding the object in the scripts evaluated for one of the displayed
	 * objects - e.g. the function attaching a new object to a list, or the function computing the
	 * column of a card.
	 *
	 * <p>
	 * Unset, the variable is named {@value #DEFAULT_ELEMENT_AS}. With
	 * <code>element-as="comment"</code>, an object is removed by <code>$comment.delete()</code>. The
	 * scripts see the object behind the values of their inputs; an input of the same name is a
	 * configuration error and is renamed with <code>as</code>.
	 * </p>
	 */
	@Name(ELEMENT_AS)
	@Nullable
	String getElementAs();

}
