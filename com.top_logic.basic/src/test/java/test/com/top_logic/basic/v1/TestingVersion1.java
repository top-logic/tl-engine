/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.v1;

import com.top_logic.basic.version.Version;

/**
 * A simple Version that depends on TLBasicVersion
 */
public class TestingVersion1 {

	public static Version getInstance() {
		return new Version(TestingVersion1.class.getResourceAsStream("version.json"));
	}
    
}