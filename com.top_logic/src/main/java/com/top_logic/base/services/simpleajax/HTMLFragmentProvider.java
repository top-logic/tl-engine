/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.FragmentControlProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Creates {@link HTMLFragment}s for the given model and style.
 * 
 * @see FragmentControlProvider
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface HTMLFragmentProvider {

	/**
	 * @param model
	 *        The model is usually passed in the constructor of the {@link HTMLFragment}. If the
	 *        {@link HTMLFragment} does not have such a parameter, it is ignored. Is allowed to be
	 *        <code>null</code>, if the model of the {@link HTMLFragment} is allowed to be
	 *        <code>null</code>.
	 * @param style
	 *        An unspecified style instruction for the {@link Control}. The meaning is defined by
	 *        the {@link ControlProvider} or {@link Control}. {@link ControlProvider}s for
	 *        {@link FormField}s use this style to write only the value or only the label of the
	 *        field, for example. In most areas, the style parameter is ignored. Is allowed to be
	 *        <code>null</code>.
	 * @return Is allowed to be <code>null</code>.
	 */
	HTMLFragment createFragment(Object model, String style);

	/**
	 * Creates an {@link HTMLFragment} with <code>null</code> style.
	 * 
	 * @param model
	 *        The model is usually passed in the constructor of the {@link HTMLFragment}. If the
	 *        {@link HTMLFragment} does not have such a parameter, it is ignored. Is allowed to be
	 *        <code>null</code>, if the model of the {@link HTMLFragment} is allowed to be
	 *        <code>null</code>.
	 * 
	 * @return Is allowed to be <code>null</code>.
	 * 
	 * @see #createFragment(Object, String)
	 * 
	 * @implSpec Calls {@link #createFragment(Object, String)} and should not be overridden. Handle
	 *           case <code>style == null</code> in {@link #createFragment(Object, String)} instead.
	 */
	default HTMLFragment createFragment(Object model) {
		return createFragment(model, null);
	}

}
