/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.mig.html.layout.DialogComponent;

/**
 * Hook for adding code before opening a dialog.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DialogEnhancer {

	/**
	 * Call-back for processing dialogs before opening them.
	 */
	void enhanceDialog(DialogComponent dialog);

}
