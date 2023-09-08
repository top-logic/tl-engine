/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.TestCase;

import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.LocationImpl;

/**
 * Test case for {@link LocationImpl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLocationImpl extends TestCase {
	
	public void testToString() {
		assertEquals("unknown location", LocationImpl.location(null, LocationImpl.NO_LINE, LocationImpl.NO_COLUMN).toString());
		
		assertEquals("line 1 column 2", LocationImpl.location(null, 1, 2).toString());
		assertEquals("line 1", LocationImpl.location(null, 1, Location.NO_COLUMN).toString());
		assertEquals("column 2", LocationImpl.location(null, LocationImpl.NO_LINE, 2).toString());
		
		assertEquals("foo line 1 column 2", LocationImpl.location("foo", 1, 2).toString());
		assertEquals("foo line 1", LocationImpl.location("foo", 1, Location.NO_COLUMN).toString());
		assertEquals("foo column 2", LocationImpl.location("foo", LocationImpl.NO_LINE, 2).toString());
	}

}
