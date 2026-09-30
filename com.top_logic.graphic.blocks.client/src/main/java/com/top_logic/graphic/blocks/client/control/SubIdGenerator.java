/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

/**
 * Generator of locally unique IDs based on some context ID.
 */
public class SubIdGenerator {

	private final String _baseId;

	private int _next = 1;

	/**
	 * Creates a {@link SubIdGenerator}.
	 */
	public SubIdGenerator(String baseId) {
		_baseId = baseId;
	}

	/**
	 * Creates the next ID.
	 */
	public String createId() {
		return _baseId + "-" + (_next++);
	}

}
