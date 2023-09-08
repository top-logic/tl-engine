/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

/**
 * An {@link ObjectTranslator} implementation is capable of translating given
 * objects into an external form.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectTranslator {
	
	/**
	 * Translate the given model object into an external form that can be used
	 * by other components.
	 * 
	 * Events that refer to the given model object use its external form instead
	 * of the model object itself.
	 * 
	 * @param object
	 *     The model object.
	 * @return a translated vesion of the given object that can be used by other
	 *     components.
	 */
	public Object resolve(Object object);

}
