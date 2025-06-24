/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg.event;

/**
 * A handler registration.
 */
public interface Registration {

	/**
	 * A dummy registration that does nothing on {@link #cancel()}.
	 */
	Registration NONE = () -> {
		// Ignore.
	};

	/**
	 * Cancels the registration and frees resources.
	 */
	void cancel();

}
