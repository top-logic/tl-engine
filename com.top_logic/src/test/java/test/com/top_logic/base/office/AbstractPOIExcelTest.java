/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.POIUtil;

/**
 * Super class for POI excle tests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractPOIExcelTest extends BasicTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		POIUtil.setUserTimeZoneAndLocale();
	}

	@Override
	protected void tearDown() throws Exception {
		POIUtil.resetUserTimeZoneAndLocale();
		super.tearDown();
	}

}

