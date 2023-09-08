/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

/**
 * Holder for information about table display at client side (e.g. {@link ViewportState} and
 * {@link VisiblePaneRequest}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ClientDisplayData {

	private ViewportState viewportState;
	private VisiblePaneRequest visiblePaneRequest;

	public ClientDisplayData() {
		viewportState = new ViewportState();
		visiblePaneRequest = new VisiblePaneRequest(IndexRange.undefined(), IndexRange.undefined());
	}

	public ViewportState getViewportState() {
		return viewportState;
	}

	public VisiblePaneRequest getVisiblePaneRequest() {
		return visiblePaneRequest;
	}

}
