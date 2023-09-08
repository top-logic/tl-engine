/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;

/**
 * TestCase for {@link JsonUtilities}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestJsonUtilities extends TestCase {

	/**
	 * Test {@link JsonUtilities#atLocationString(com.top_logic.common.json.gstream.JsonReader)}.
	 */
	public void testAtLocation() throws IOException {
		String simpleData = "{\"a\": true,\n\"b\":\n   false\n\n}";
		JsonReader reader = new JsonReader(new StringR(simpleData));
		assertEquals(" at line 1 column 1 path $", JsonUtilities.atLocationString(reader));
		reader.beginObject();
		assertEquals(" at line 1 column 2 path $.", JsonUtilities.atLocationString(reader));
		assertEquals("a", reader.nextName());
		assertEquals(" at line 1 column 5 path $.a", JsonUtilities.atLocationString(reader));
		assertEquals(true, reader.nextBoolean());
		assertEquals(" at line 1 column 11 path $.a", JsonUtilities.atLocationString(reader));
		assertEquals("b", reader.nextName());
		assertEquals(" at line 2 column 4 path $.b", JsonUtilities.atLocationString(reader));
		assertEquals(false, reader.nextBoolean());
		assertEquals(" at line 3 column 9 path $.b", JsonUtilities.atLocationString(reader));
		reader.endObject();
		assertEquals(" at line 5 column 2 path $", JsonUtilities.atLocationString(reader));
	}

	/**
	 * Tests {@link JsonUtilities#parse(String)}.
	 */
	public void testParse() {
		String simpleData = "{\"a\": true,\n\"b\":\n   false\n\n,\"c\":[15,true,\"text\",null]}";
		Map<Object, Object> expectedJSON = new MapBuilder<>()
			.put("a", Boolean.TRUE)
			.put("b", Boolean.FALSE)
			.put("c", list(15L, true, "text", null)).toMap();
		assertEquals(expectedJSON, JsonUtilities.parse(simpleData));
	}

	/**
	 * Tests parsing and formatting.
	 */
	public void testParseFormat() {
		Map<Object, Object> json = new MapBuilder<>()
			.put("a", Boolean.TRUE)
			.put("b", Boolean.FALSE)
			.put("c", list(15L, true, "text", null))
			.toMap();
		assertEquals(json, JsonUtilities.parse(JsonUtilities.format(json)));
	}

}

