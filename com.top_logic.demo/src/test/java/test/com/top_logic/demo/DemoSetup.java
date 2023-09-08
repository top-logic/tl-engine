/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo;

import junit.framework.Test;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;

/**
 * @author    <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public class DemoSetup {

    public static Test createDemoSetup(Test wrapped) {
		return ApplicationTestSetup.setupApplication(wrapped);
	}
    
    public static Test createDemoSetup(Class<? extends Test> wrapped) {
		return ApplicationTestSetup.setupApplication(wrapped);
	}

}
