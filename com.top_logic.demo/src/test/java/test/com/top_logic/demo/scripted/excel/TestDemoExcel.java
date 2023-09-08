/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.excel;

import junit.framework.Test;
import test.com.top_logic.layout.scripting.template.excel.TestExcels;

/**
 * Runs the excel tests of the Demo.
 * 
 * @author <a href=mailto:Jan Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public class TestDemoExcel {

	/** Returns the suite of {@link Test}s to perform. */
	public static Test suite() {
		return new TestExcels().suite();
	}

}
