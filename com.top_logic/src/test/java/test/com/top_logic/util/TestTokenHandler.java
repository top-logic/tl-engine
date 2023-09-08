/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.TokenHandler;

/**
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class TestTokenHandler extends BasicTestCase {
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestTokenHandler.class);
	}

	public static void main(String[] args) {
		new TestRunner().doRun(suite());
	}
	
	public void testReplaceTokens() throws Exception {

		// positive test
		Map<String, String> theTokens = new HashMap<>();
		theTokens.put("%abc%", "abc");
		theTokens.put("%ABC%", "ABC");
		theTokens.put("%.bc%", ".BC");
		theTokens.put("[.bc]", ".bc");
		theTokens.put("$null$", null);
		
		String theString = " %ABC% ABC [.bc] %.bc% $null$ ";
		TokenHandler theHandler = new TokenHandler(theTokens, ResKey.forTest("noValue"));
		String theResult = theHandler.replaceTokens(theString);
		assertEquals(" ABC ABC .bc .BC (noValue) ", theResult);
		
		// basic failure tests
		// test null access
		theHandler.replaceTokens(null);
		theHandler.replaceTokens("");
		theTokens.put(null, "nix");
	}
	
}
