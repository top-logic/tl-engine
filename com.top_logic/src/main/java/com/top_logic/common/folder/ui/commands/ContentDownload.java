/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.ui.commands;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ContentDownload extends AbstractDynamicCommand {

	/** The document to be downloaded. */
	private final BinaryDataSource document;

	/**
	 * Creates a {@link ContentDownload}.
	 * 
	 * @param aDocument
	 *        The document to be held, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If given document is <code>null</code>.
	 */
	public ContentDownload(BinaryDataSource aDocument) {
		if (aDocument == null) {
			throw new IllegalArgumentException("Given document is null.");
		}

		this.document = aDocument;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		aContext.getWindowScope().deliverContent(document);

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutableState calculateExecutability() {
		if (!ComponentUtil.isValid(document)) {
			return ExecutableState.NO_EXEC_INVALID;
		}
		return ExecutableState.EXECUTABLE;
	}
}