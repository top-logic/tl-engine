/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.util.ResKey;

/**
 * Base interface for all implementation parts of an import specification.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ImportPart {

	/**
	 * Description where this {@link ImportPart} was defined.
	 * 
	 * <p>
	 * Used for error reporting. If an import step fails, it is interesting what part in the import
	 * specification caused the failure.
	 * </p>
	 */
	ResKey location();

}
