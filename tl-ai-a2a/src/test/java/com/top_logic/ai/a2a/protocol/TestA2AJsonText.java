/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test case for {@link A2AJson#text(Object, java.util.function.Function)}: what an agent reads of a
 * message whose data parts an application handles.
 */
public class TestA2AJsonText extends TestCase {

	/**
	 * A data part the application handles is read as the handler's text; one it leaves alone stays
	 * JSON, in the order of the parts.
	 */
	public void testAHandledDataPartIsReadAsTheHandlersText() {
		Map<String, Object> message = A2AJson.message(A2AJson.ROLE_USER, null, null, List.of(
			A2AJson.textPart("Bitte prüfen."),
			A2AJson.dataPart(Map.of("order", Map.of("title", "x"))),
			A2AJson.dataPart(Map.of("other", 1))));

		String text = A2AJson.text(message,
			data -> data instanceof Map<?, ?> map && map.containsKey("order") ? "[order 1 stored]" : null);

		assertEquals("Bitte prüfen.\n\n[order 1 stored]\n\n```json\n{\"other\":1}\n```", text);
		assertTrue(A2AJson.text(message).contains("\"order\""));
	}
}
