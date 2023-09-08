/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.text.ParseException;

import junit.framework.Test;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.ExtIDFormat;

/**
 * Test for {@link ExtID}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestExtID extends BasicTestCase {

	public void testEqualHash() {
		ExtID expected = new ExtID(15, 654);
		assertEquals(expected, expected);
		assertEquals(expected, new ExtID(15, 654));
		assertNotEquals(expected, null);
		assertNotEquals(expected, new ExtID(98416, 654));
		assertNotEquals(expected, new ExtID(15, -6541651));
		assertEquals(expected.hashCode(), expected.hashCode());
		assertEquals(expected.hashCode(), new ExtID(15, 654).hashCode());
	}

	public void testFormat() throws ParseException {
		ExtID expected = new ExtID(15, 654);
		String formatted = ExtIDFormat.INSTANCE.format(expected);
		assertEquals(expected, ExtIDFormat.INSTANCE.parseObject(formatted));
		assertEquals(expected, ExtIDFormat.INSTANCE.parseExtID(formatted));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestExtID}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestExtID.class);
	}

}
