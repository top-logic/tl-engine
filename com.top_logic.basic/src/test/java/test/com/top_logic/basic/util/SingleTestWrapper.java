/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import junit.extensions.TestDecorator;
import junit.framework.Test;

/**
 * {@link Test} that executes exactly one wrapped test without being a {@link TestDecorator}.
 * 
 * <p>
 * Exposing the wrapped test lets the test harness see what kind of test is executed, e.g. whether a
 * {@link ScriptedTestMarker scripted test} is wrapped.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SingleTestWrapper extends Test {

	/**
	 * The test executed by this wrapper.
	 */
	Test getWrappedTest();

}
