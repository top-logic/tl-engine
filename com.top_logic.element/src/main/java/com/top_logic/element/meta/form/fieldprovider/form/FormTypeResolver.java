/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.form;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Function computing the type of objects to be displayed in forms in a certain context.
 * 
 * @see TLFormType
 */
public interface FormTypeResolver {

	/**
	 * The type of objects to display in a form in a certain context.
	 */
	TLStructuredType getFormType(TLObject context);

}
