/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary.scan;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;

/**
 * Strategy to inspect the content of an uploaded file before it is accepted.
 *
 * <p>
 * This is the content-level analogue of the file-name check
 * {@link com.top_logic.knowledge.gui.layout.upload.FileNameStrategy}: while the latter only sees
 * the file name, an {@link UploadContentChecker} receives the uploaded bytes and may reject them,
 * e.g. because a virus scanner classified them as malicious.
 * </p>
 *
 * <p>
 * Implementations are configured on the {@link UploadSecurityService} and invoked from every
 * upload-accepting control. A virus scanner talking to a scanning daemon (see {@link ClamAvScanner})
 * is one implementation; applications may contribute their own (e.g. an ICAP or command-line
 * backend).
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UploadContentChecker {

	/**
	 * Checks whether the given uploaded content is allowed.
	 *
	 * @param data
	 *        The uploaded content to inspect. Must not be <code>null</code>.
	 *
	 * @return The error message key describing why the upload is rejected, or <code>null</code> if
	 *         the content is accepted (or this checker is not responsible for it).
	 */
	ResKey check(BinaryData data);

}
