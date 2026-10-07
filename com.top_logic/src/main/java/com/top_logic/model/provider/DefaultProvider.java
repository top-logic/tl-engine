/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLDefaultValue;

/**
 * Provider for the default value of an {@link TLStructuredTypePart}.
 * 
 * @see TLDefaultValue
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Default value computation")
public interface DefaultProvider {

	/**
	 * Creates the default value for the given {@link TLStructuredTypePart attribute}.
	 * 
	 * <p>
	 * For a provider that {@link #isComputedInTransaction() is computed in a transaction}, this
	 * method is only called when a persistent object is created within a transaction.
	 * </p>
	 * 
	 * @param context
	 *        The context in which the default for a new object is needed, e.g. when the default for
	 *        a new child of an structured element is needed the context is the parent. May be
	 *        <code>null</code>.
	 * @param attribute
	 *        The attribute to create default value for.
	 * @return Default value for the given attribute. May be <code>null</code>.
	 */
	Object createDefault(Object context, TLStructuredTypePart attribute);

	/**
	 * Whether the default value of this provider can only be computed in the transaction that
	 * creates the persistent object.
	 * 
	 * <p>
	 * Such a provider allocates its value from transactional state, e.g. a continuous sequence
	 * number, or creates further persistent objects. The framework does not request its default
	 * for an object created outside a transaction, i.e. a transient object or the object edited
	 * in the create form of the UI. The attribute stays empty there, and the default is computed
	 * when the persistent object is created in a transaction.
	 * </p>
	 * 
	 * @return Whether {@link #createDefault(Object, TLStructuredTypePart)} must only be called in
	 *         the transaction that creates a persistent object.
	 */
	default boolean isComputedInTransaction() {
		return false;
	}

}

