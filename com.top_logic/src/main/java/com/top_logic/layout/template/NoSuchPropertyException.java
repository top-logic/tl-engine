/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

/**
 * {@link Exception} thrown if an undefined property is accessed on a {@link WithProperties} object.
 */
public class NoSuchPropertyException extends Exception {

	/**
	 * Creates a {@link NoSuchPropertyException}.
	 */
	public NoSuchPropertyException(String message) {
		super(message);
	}

}
