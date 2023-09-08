/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.col.BidiHashMap;

/**
 * Test case for {@link BidiHashMap}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBidiHashMapWithHashClash extends TestBidiHashMap {
	
	private static class HashClashWrapper {
		private final Object orig;
		private final int hashCode;

		public HashClashWrapper(Object orig, int hashCode) {
			this.orig = orig;
			this.hashCode = hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			
			if (! (obj instanceof HashClashWrapper)) {
				return false;
			}
			
			return orig.equals(((HashClashWrapper) obj).orig);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public String toString() {
			return orig.toString();
		}
		
	}
	
	@Override
	public Object[] getSampleKeys() {
		Object[] result = super.getSampleKeys();
		wrapValues(result, 2);
		return result;
	}
	
	@Override
	public Object[] getSampleValues() {
		Object[] result = super.getSampleValues();
		wrapValues(result, 2);
		return result;
	}

	private void wrapValues(Object[] result, int mod) {
		assertEquals("All values are mutually distinct.", result.length, new HashSet(Arrays.asList(result)).size()); 
		
		for (int n = 0, cnt = result.length; n < cnt; n++) {
			if (result[n] != null) {
				result[n] = new HashClashWrapper(result[n], 42 + n % mod);
			}
		}
	}
	
    public static Test suite() {
        return new TestSuite(TestBidiHashMapWithHashClash.class);
    }
	
}
