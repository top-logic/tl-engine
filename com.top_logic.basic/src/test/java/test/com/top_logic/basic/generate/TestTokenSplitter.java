/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package test.com.top_logic.basic.generate;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.generate.TokenSplitter;

/**
 * Test case for {@link TokenSplitter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTokenSplitter extends TestCase {

	public void testSplit() {
		Map<String, List<String>> glossary = new HashMap<>();
		glossary.put("A", Collections.emptyList());
		glossary.put("ABK", Collections.singletonList("ABKUERZUNG"));
		glossary.put("ID", Collections.singletonList("IDENTIFIER"));
		glossary.put("USER", null);
		glossary.put("PASSWD", Collections.singletonList("PASSWORD"));
		TokenSplitter splitter = new TokenSplitter(glossary);

		assertEquals(list("FOOBAR"), splitter.split("FOOBAR"));
		assertEquals(list("IDENTIFIER"), splitter.split("AID"));
		assertEquals(list("A"), splitter.split("A"));
		assertEquals(list("USER", "IDENTIFIER"), splitter.split("USERID"));
		assertEquals(list("USER", "ABKUERZUNG"), splitter.split("AUSERABK"));
		assertEquals(list("USER", "42", "IDENTIFIER"), splitter.split("AUSER42ID"));
	}

}
