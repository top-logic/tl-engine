/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

/**
 * {@link UploadForm} used for update an existing BPML diagram.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UpdateForm extends UploadForm {

	/**
	 * Whether not only the BPML diagram shall be updated, but also the application specific BPML
	 * extensions are updated from the imported diagram.
	 */
	boolean isUpdateBPMLExtensions();

	/**
	 * Setter for {@link #isUpdateBPMLExtensions()}.
	 */
	void setUpdateBPMLExtensions(boolean value);

}
