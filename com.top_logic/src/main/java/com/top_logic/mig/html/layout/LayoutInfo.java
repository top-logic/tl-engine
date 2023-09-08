/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.values.edit.DefaultLayoutInfoOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Component-local information to the layout algorithm.
 * 
 * @see LayoutComponent.Config#getLayoutInfo()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Options(fun = DefaultLayoutInfoOptions.class)
public interface LayoutInfo extends ConfigurationItem {

	/**
	 * @see #getSize()
	 */
	String SIZE = "size";

	/**
	 * @see #getMinSize()
	 */
	String MIN_SIZE = "minSize";

	/**
	 * @see #isScrolleable()
	 */
	String SCROLLEABLE = "scrolleable";

	/**
	 * The displayed size of the component in the direction of its surrounding layout.
	 */
	@Name(SIZE)
	DisplayDimension getSize();

	/**
	 * The minimum size the component can be resized to.
	 */
	@Name(MIN_SIZE)
	@IntDefault(0)
	@Hidden
	int getMinSize();

	/**
	 * Whether the component's content are should get scroll bars, if the content does not fit into
	 * the available display area.
	 */
	@Name(SCROLLEABLE)
	@BooleanDefault(true)
	boolean isScrolleable();

}
