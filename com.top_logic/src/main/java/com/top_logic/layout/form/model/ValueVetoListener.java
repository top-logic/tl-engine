/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;
import com.top_logic.layout.form.FormField;

/**
 * The class {@link ValueVetoListener} is a listener which can be added to some
 * {@link FormField} to provide it from setting a new value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ValueVetoListener extends VetoListener {

	/**
	 * This method determine whether this {@link ValueVetoListener} has a veto
	 * for changing {@link FormField#getValue() the value} of the given field to
	 * the given value.
	 * 
	 * @param field
	 *        the field who wants to change its value.
	 * @param newValue
	 *        the value the fields will have if no veto is given.
	 * @throws VetoException
	 *         if this listener wants to stop setting the new value.
	 */
	public void checkVeto(FormField field, Object newValue) throws VetoException;

}
