/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * Common parameters for component templates creating layouts.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface LayoutTemplateParameters extends ConfigurationItem {

	/**
	 * @see #getOrientation()
	 */
	String ORIENTATION = "orientation";

	/**
	 * The {@link Orientation} of the created layout.
	 */
	@Name(ORIENTATION)
	Orientation getOrientation();

	/**
	 * Whether the {@link #getOrientation()} is {@link Orientation#HORIZONTAL}.
	 * 
	 * @see com.top_logic.mig.html.layout.Layout.Config#getHorizontal() Required for type
	 *      compatibility with legacy configuration.
	 */
	@Hidden
	@Derived(fun = Orientation.IsHorizontal.class, args = @Ref(ORIENTATION))
	boolean getHorizontal();

	/**
	 * Whether the Layout can be resized.
	 * 
	 * @see com.top_logic.mig.html.layout.Layout.Config#getResizable()
	 */
	@BooleanDefault(true)
	boolean getResizable();

}
