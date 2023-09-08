/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.progress;

import junit.framework.Test;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.layout.scripting.action.AwaitProgressAction;

/**
 * Test case for {@link AJAXProgressComponent} and {@link AwaitProgressAction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAJAXProgressComponentLive {

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
        return DemoSetup.createDemoSetup(XmlScriptedTestUtil.suite(TestAJAXProgressComponentLive.class, "testAwait"));
    }

}
