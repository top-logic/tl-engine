/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.form.model;

/**
 * Algorithm to attach to an {@link FormContext} to apply additional store operations.
 * 
 * @see FormContext#addStoreAlgorithm(FormContextStoreAlgorithm)
 * @see FormContext#store()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FormContextStoreAlgorithm {

	/**
	 * Performs the actual store operation.
	 * 
	 * @param context
	 *        The {@link FormContext} which is {@link FormContext#store() stored}.
	 */
	void store(FormContext context);

}
