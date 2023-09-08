/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

/**
 * Default {@link AnnotationCustomizations} using only the statically declared annotations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NoCustomizations extends DefaultCustomizations {

	/**
	 * Singleton {@link NoCustomizations} instance.
	 */
	public static final NoCustomizations INSTANCE = new NoCustomizations();

	private NoCustomizations() {
		// Singleton constructor.
	}

}
