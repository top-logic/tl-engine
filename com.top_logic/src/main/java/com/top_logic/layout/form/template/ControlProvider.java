/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.FragmentControlProvider;
import com.top_logic.layout.form.FormField;

/**
 * A {@link ControlProvider} creates {@link Control}s for given model objects.
 * <p>
 * Use {@link HTMLFragmentProvider} when designing new APIs. It is more general and allows
 * {@link HTMLFragment}s, not just {@link Control}s.
 * </p>
 * 
 * @see FragmentControlProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ControlProvider extends HTMLFragmentProvider {

	/**
	 * Creates a new {@link Control} for the given model object in the given style.
	 * 
	 * @param style
	 *        An unspecified style instruction for the {@link Control}. The meaning is defined by
	 *        the {@link ControlProvider} or {@link Control}. {@link ControlProvider}s for
	 *        {@link FormField}s use this style to write only the value or only the label of the
	 *        field, for example. In most areas, the style parameter is ignored. Is allowed to be
	 *        <code>null</code>.
	 * @return The new {@link Control} for the model object. Is allowed to be <code>null</code>.
	 */
	Control createControl(Object model, String style);

	/**
	 * Creates a new {@link Control} for the given model.
	 * 
	 * @return The new {@link Control} for the model object. Is allowed to be <code>null</code>.
	 * 
	 * @see #createControl(Object, String)
	 * 
	 * @implSpec Calls {@link #createControl(Object, String)} and should not be overridden. Handle
	 *           case <code>style == null</code> in {@link #createControl(Object, String)} instead.
	 */
	default Control createControl(Object model) {
		return createControl(model, null);
	}

	@Override
	default Control createFragment(Object model, String style) {
		return createControl(model, style);
	}

}
