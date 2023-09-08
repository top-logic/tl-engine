/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;

/**
 * A label provider knows about translating arbitrary application objects into
 * user-readable internationalized string representations that are suitable for
 * being displayed at the user interface.
 * 
 * <p>
 * The sort of application objects that must be understood by an implementation
 * of this interface is defined by the class that declares a
 * {@link LabelProvider} in its interface. For an example, see
 * {@link com.top_logic.layout.form.model.SelectField#setOptionLabelProvider(LabelProvider)}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LabelProvider extends Mapping<Object, String> {

	/**
	 * Computes a string representation of the given object. The returned string
	 * string must be suitable for being displayed at the user interface.
	 * 
	 * @param object
	 *     The application object, for which a string representation
	 *     should be computed.
	 * @return The internationalized string representation of the given object.
	 */
	public String getLabel(Object object);

	@Override
	default String map(Object input) {
		return getLabel(input);
	}

}
