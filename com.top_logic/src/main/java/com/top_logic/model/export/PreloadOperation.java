/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.util.Collection;

/**
 * Operation that prepares the access to a number of base objects.
 * 
 * <p>
 * A {@link PreloadOperation} is responsible for bulk-loading data from the database that is
 * required for a following access to the pre-loaded base objects.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PreloadOperation {

	/**
	 * Prepare for accessing the given base objects.
	 * 
	 * @param context
	 *        A context that is able to keep objects from being garbage collected.
	 * @param baseObjects
	 *        The objects that should be pre-loaded.
	 */
	void prepare(PreloadContext context, Collection<?> baseObjects);

}
