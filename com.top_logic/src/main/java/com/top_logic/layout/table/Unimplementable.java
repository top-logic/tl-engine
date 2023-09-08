/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;


/**
 * Marker to prevent other interfaces being implemented outside this package by referencing this
 * class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface Unimplementable {

	/**
	 * Restricts using interfaces to package-local implementations.
	 */
	Unimplementable unimplementable();

}
