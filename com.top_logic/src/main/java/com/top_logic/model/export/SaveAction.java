/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

/**
 * Algorithm that stores a certain aspect of a model instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SaveAction {

	/**
	 * Stores a certain aspect of the given model instance to the given value
	 * storage array.
	 * 
	 * <p>
	 * Which aspect of the instance is stored and the indices of the value
	 * buffer that is reserved for this {@link SaveAction} is determined
	 * externally.
	 * </p>
	 * 
	 * @param modelInstance
	 *        The model instance that is (partially) stored be this
	 *        {@link SaveAction}.
	 * @param valueBuffer
	 *        A buffer into which the model aspect is stored.
	 */
	void save(Object modelInstance, Object[] valueBuffer);

}
