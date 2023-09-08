/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Check if the given value is not empty.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class NotEmptyConstraint extends AbstractConstraint {

	@SuppressWarnings("javadoc")
	public static final NotEmptyConstraint INSTANCE = new NotEmptyConstraint();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean check(Object aValue) throws CheckException {

		if (aValue instanceof Collection) {
			if (((Collection<Object>) aValue).isEmpty()) {
				throwCheckException();
			}
		}

		if (aValue instanceof Map) {
			if (((Map) aValue).isEmpty()) {
				throwCheckException();
			}
		}

		if (StringServices.isEmpty(aValue)) {
			throwCheckException();
        }
        return true;
    }

	private void throwCheckException() throws CheckException {
		throw new CheckException(Resources.getInstance().getString(I18NConstants.NOT_EMPTY));
	}

}
