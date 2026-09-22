/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary.scan;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Thrown by the {@link UploadGuardRequest} when an {@link UploadContentChecker} rejects the content
 * of an uploaded part.
 *
 * <p>
 * The {@link #getErrorKey()} is the message of the rejecting {@link UploadContentChecker}, already
 * filled with the details of the rejection such as the name of the uploaded file.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UploadRejectedException extends I18NRuntimeException {

	/**
	 * Creates a {@link UploadRejectedException}.
	 *
	 * @param errorKey
	 *        The message of the rejecting {@link UploadContentChecker}, see {@link #getErrorKey()}.
	 */
	public UploadRejectedException(ResKey errorKey) {
		super(errorKey);
	}

}
