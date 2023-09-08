/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.SelectField;

/**
 * Special version of the {@link DefaultFormFieldControlProvider} which
 * uses a selection control instead of a select control for select fields.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SelectionControlProvider extends DefaultFormFieldControlProvider {

	/**
	 * Singleton {@link SelectionControlProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final SelectionControlProvider INSTANCE = new SelectionControlProvider(true);

	/**
	 * Singleton {@link SelectionControlProvider} instance that does not offer to clear the current
	 * selection.
	 */
	public static final SelectionControlProvider INSTANCE_WITHOUT_CLEAR = new SelectionControlProvider(false);

	private final boolean _offerClear;

	/**
	 * Creates a default {@link SelectionControlProvider}.
	 */
	protected SelectionControlProvider() {
		this(true);
	}

	/**
	 * Creates a {@link SelectionControlProvider}.
	 *
	 * @param offerClear
	 *        Whether to offer a clear button for non-mandatory select fields.
	 */
	protected SelectionControlProvider(boolean offerClear) {
		_offerClear = offerClear;
	}

	/**
	 * Same as {@link #INSTANCE}.
	 * 
	 * @deprecated Use {@link #INSTANCE}
	 */
	@Deprecated
	public static final ControlProvider SELECTION_INSTANCE = INSTANCE;

    @Override
	public Control visitSelectField(SelectField aMember, Void arg) {
        SelectionControl result = new SelectionControl(aMember);
		result.setClearButton(_offerClear && !aMember.isMandatory());
		return result;
    }
}

