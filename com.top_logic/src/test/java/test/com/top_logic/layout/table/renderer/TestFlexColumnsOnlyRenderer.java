/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.renderer;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

/**
 * Test case for {@link TestFlexColumnsOnlyRenderer}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestFlexColumnsOnlyRenderer extends TestColumnTableRenderer {

	@Override
	protected int getFrozenColumnCount() {
		return 0;
	}

	public static Test suite() {
		return TestControl.suite(TestFlexColumnsOnlyRenderer.class);
	}
}
