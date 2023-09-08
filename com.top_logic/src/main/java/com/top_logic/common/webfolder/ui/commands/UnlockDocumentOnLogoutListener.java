/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import static com.top_logic.basic.util.Utils.*;
import static java.util.Objects.requireNonNull;

import com.top_logic.base.context.SubSessionListener;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.Document;

/**
 * A {@link SubSessionListener} that unlocks the given document.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UnlockDocumentOnLogoutListener implements SubSessionListener {

	private static final Class<?> THIS_CLASS = UnlockDocumentOnLogoutListener.class;

	private final Document _document;

	/**
	 * Creates an UnlockDocumentOnLogoutListener.
	 * 
	 * @param document
	 *        Is not allowed to be null.
	 */
	public UnlockDocumentOnLogoutListener(Document document) {
		_document = requireNonNull(document);
	}

	@Override
	public void notifySubSessionUnbound(TLSubSessionContext context) {
		boolean success = unlock();
		if (!success) {
			logUnlockFailed();
		}
	}

	private boolean unlock() {
		if (!_document.tValid()) {
			return true;
		}
		return _document.getDAP().unlock();
	}

	private void logUnlockFailed() {
		logError("Failed to unlock the document when the user was logged out. Document: " + debug(_document));
	}

	private void logError(String message) {
		Logger.error(message, THIS_CLASS);
	}

}
