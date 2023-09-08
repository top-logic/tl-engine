/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * Upload Handler which allows uploading empty files.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class EmptyFileUploadHandler extends UploadHandler {

	/** Config interface for {@link EmptyFileUploadHandler}. */
	public interface Config extends UploadHandler.Config {

		@Override
		@BooleanDefault(true)
		boolean getAllowEmptyFiles();

	}

    public static final String COMMAND = "emptyFileUpload";

    public EmptyFileUploadHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

}
