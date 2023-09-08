/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.ocr;

import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.util.TLContext;

/**
 * Implementation of {@link PDFUploadBatch} that executes the upload in a given Context.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InContextPDFUploadBatch extends PDFUploadBatch {

	private final TLContext _context;

	public InContextPDFUploadBatch(TLContext context) {
		super("PDFUploadBatch for " + context.getCurrentUserName());
		_context = context;
	}

	@Override
	protected void runInContext() {
		ocrContext = _context;
		ThreadContextManager.inContext(ocrContext, new InContext() {
		
			@Override
			public void inContext() {
				InContextPDFUploadBatch.this.executeJob();
			}

		});
	}

}

