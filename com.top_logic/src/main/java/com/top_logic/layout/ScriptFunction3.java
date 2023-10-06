/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.func.IFunction3;
import com.top_logic.layout.form.values.edit.annotation.CssClass;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;

/**
 * Configuration of an {@link IFunction3} that executes a TL-Script function.
 * 
 * @implNote The implementation of a {@link ScriptFunction3} bases on a TL-Script expression.
 * 
 * @see ScriptFunction0
 * @see ScriptFunction1
 * @see ScriptFunction2
 * @see ScriptFunction4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ItemDisplay(ItemDisplayType.VALUE)
@CssClass("tlscript")
@RenderWholeLine
@Label("Script function")
public interface ScriptFunction3<R, A1, A2, A3> extends PolymorphicConfiguration<IFunction3<R, A1, A2, A3>> {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Instantiates this {@link ScriptFunction3} to an {@link IFunction3}.
	 */
	default IFunction3<R, A1, A2, A3> impl() {
		return TypedConfigUtil.createInstance(this);
	}
}

