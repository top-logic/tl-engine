/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.display;

import com.top_logic.layout.DisplayContext;

/**
 * Callback function for {@link BPMNDisplay#storeDiagram(StoreCallback)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface StoreCallback {

	/**
	 * Called when the BPML data is fetched from the UI.
	 *
	 * @param context
	 *        The current {@link DisplayContext} of the running command.
	 * @param xml
	 *        The BPML source code.
	 */
	void handleDiagramData(DisplayContext context, String xml);

}
