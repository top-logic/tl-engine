/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import static java.util.Objects.*;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Exception that aborts the
 * {@link Constraint#check(Object) constraint checking process} of a
 * {@link FormField}, if an error is detected.
 * 
 * <p>
 * The error message (see {@link #CheckException(String)}) of this exception is
 * supposed to be internationalized and end-user-readable. The error message of
 * instances of this class are made visible at the GUI in response to problems
 * that occur during {@link FormField#check() value checking} of a field.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckException extends Exception {

	/** A new {@link CheckException} with an end-user-readable error message. */
	public CheckException(ResKey explanation) {
		this(Resources.getInstance().getString(requireNonNull(explanation)));
	}

	/**
	 * Constructs a new check exception with an end-user-readable
	 * internationalized error message.
	 * 
	 * @param errorMessage
	 *     An internationalized end-user-readable error description. 
	 */
	public CheckException(String errorMessage) {
		super(errorMessage);
	}

}
