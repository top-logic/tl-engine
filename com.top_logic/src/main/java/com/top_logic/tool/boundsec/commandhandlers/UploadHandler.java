/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.knowledge.gui.layout.upload.MultiUploadAware;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.error.TopLogicException;

/**
 * Handler for a simple upload of a file to the server.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class UploadHandler extends AbstractUploadHandler {

	public interface Config extends AbstractUploadHandler.Config {

		/**
		 * @see #getAllowEmptyFiles()
		 */
		String ALLOW_EMPTY_FILES_PROPERTY = "allow-empty-files";

		@Name(ALLOW_EMPTY_FILES_PROPERTY)
		boolean getAllowEmptyFiles();

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/** The command provided by this instance. */
    public static final String COMMAND = "upload";

    /** Flag indicating whether it is allowed to upload empty files (leave upload file field empty). */
	protected final boolean allowEmptyFiles;

    public UploadHandler(InstantiationContext context, Config config) {
		super(context, config);

		allowEmptyFiles = config.getAllowEmptyFiles();
    }

	@Override
	protected HandlerResult handleUpload(DisplayContext context, LayoutComponent aComponent, final DataField dataField) throws IOException {
		HandlerResult theResult = new HandlerResult();
		try {
			((MultiUploadAware) aComponent).receiveFiles(dataField.getDataItems());
		} catch (Exception ex) {
			// is not really IO exception but a non-upload-complaint component
			TopLogicException tlEx = new TopLogicException(this.getClass(), "notSupported", new Object[] { aComponent.getName() }, ex);
			theResult.setException(tlEx);
		} 
		theResult.setCloseDialog(theResult.isSuccess());
		
		// Need this to clean up form field which holds the uploaded file
		dataField.initializeField(null);
		
		return theResult;
	}

	public static <C extends Config> C updateAllowEmptyFiles(C config, boolean value) {
		return update(config, Config.ALLOW_EMPTY_FILES_PROPERTY, value);
	}

}
