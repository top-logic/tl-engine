/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;


import com.top_logic.layout.ControlScope;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.UpdateListener;

/**
 * {@link ControlScope} for test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DummyControlScope implements ControlScope {

	private FrameScope _frameScope = new DummyFrameScope();

	@Override
	public boolean removeUpdateListener(UpdateListener aListener) {
		return false;
	}

	@Override
	public boolean isScopeDisabled() {
		return false;
	}

	@Override
	public FrameScope getFrameScope() {
		return _frameScope;
	}

	@Override
	public void disableScope(boolean disable) {
		// Ignore.
	}

	@Override
	public void addUpdateListener(UpdateListener aListener) {
		// Ignore.
	}
}