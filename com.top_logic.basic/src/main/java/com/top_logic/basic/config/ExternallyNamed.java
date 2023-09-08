/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Interface to be implemented by {@link Enum enums} which have an external
 * name, e.g. if they are potentially configured. <br />
 * 
 * <b>It must be ensured that the external names are pairwise different</b>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExternallyNamed {

	/**
	 * Returns the external name of this {@link Enum}. <br />
	 * 
	 * <b>It must be ensured that the external names are pairwise different
	 * within the enum type</b>
	 */
	String getExternalName();

}
