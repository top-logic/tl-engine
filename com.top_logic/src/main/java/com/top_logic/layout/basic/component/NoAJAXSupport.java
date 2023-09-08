/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;



import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;

/**
 * @author    <a href=mailto:kha@top-logic.com>bhu</a>
 */
public final class NoAJAXSupport implements AJAXSupport {

	public static final NoAJAXSupport INSTANCE = new NoAJAXSupport(); 
	
	private NoAJAXSupport() {
	}
	
	@Override
	public void startRendering() {
		// Ignored.
	}

	@Override
	public boolean isRevalidateRequested() {
		return false;
	}

	@Override
	public void revalidate(DisplayContext context, UpdateQueue actions) {
		// Ignored.
	}

	@Override
	public void invalidate() {
	}

}
