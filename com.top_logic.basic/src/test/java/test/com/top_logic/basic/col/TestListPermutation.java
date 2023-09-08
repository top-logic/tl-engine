/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.IdentityPermutation;
import com.top_logic.basic.col.ListPermutation;

/**
 * Test case for {@link ListPermutation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestListPermutation extends TestCase {
	
	public void testIdentity() {
		assertEquals(list("A", "B", "C"), new ListPermutation<>(new IdentityPermutation(3), list("A", "B", "C")));
	}

	public void testReverse() {
		assertEquals(reverse(list("A", "B", "C")), new ListPermutation<>(reverse(new IdentityPermutation(3)), list("A", "B", "C")));
	}

	public void testChoose() {
		assertEquals(list("C", "A"), new ListPermutation<>(list(2, 0), list("A", "B", "C")));
	}
	
	public void testEnlarge() {
		assertEquals(list("A", "B", "B", "C"), new ListPermutation<>(list(0, 1, 1, 2), list("A", "B", "C")));
	}
	
	private <T> List<T> reverse(List<T> list) {
		ArrayList<T> result = new ArrayList<>(list);
		Collections.reverse(result);
		return result;
	}
	
}
