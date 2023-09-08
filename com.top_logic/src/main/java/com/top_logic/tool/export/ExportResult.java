/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.OutputStream;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.export.Export.State;

/**
 * The {@link ExportResult} will be filled by {@link ExportHandler}s with all information
 * needed by an {@link Export}.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public interface ExportResult {

    /**
     * Get an {@link OutputStream} where the {@link ExportHandler} can its result.
     */
    OutputStream getOutputStream();

	/**
	 * Set a resource key describing a failure.
	 * 
	 * Setting this property will result in {@link State#FAILED} of the {@link Export}.
	 * 
	 * @see Export#getFailureKey()
	 */
	void setFailureKey(ResKey aResKey);

    /**
	 * Set a resource key.
	 * <p>
	 * This key will be used for the file name of a {@link Export#getDocument()}.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> Due to proper display handling of documents all {@link ExportHandler}s are
	 * expected to set this property to not <code>null</code>.
	 * </p>
	 * 
	 * @see Export#getDisplayNameKey()
	 */
	void setFileDisplaynameKey(ResKey aResKey);

    /**
     * Set the file extension the result document will have.
     * 
     * <p>
     * <b>Note:</b> Due to proper display handling of documents all {@link ExportHandler}s
     * are expected to set this property to not <code>null</code>.
     * </p>
     * 
     * @see Export#getFileExtension()
     */
    void setFileExtension(String aFileextension);

    /**
     * Return the I18nkey that describes the occured failure. May be <code>null</code> if
     * the export was successful.
     */
    ResKey getFailureKey();

    /**
     * Return the I18nkey that defines the name of the result of the export.
	 */
    ResKey getFileDisplaynameKey();

    /**
     * Return the extension of the file (aka file type). May be <code>null</code> if the
     * export ended with an error.
     */
    String getFileExtension();
}
