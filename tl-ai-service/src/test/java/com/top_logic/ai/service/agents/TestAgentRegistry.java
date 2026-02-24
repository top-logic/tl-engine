/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.time.Duration;

import junit.framework.TestCase;

import dev.langchain4j.model.chat.request.ResponseFormat;

public class TestAgentRegistry extends TestCase {

	public void testMapResponseFormatJson() {
		assertEquals(ResponseFormat.JSON,
			AgentRegistry.mapResponseFormat(AgentConfig.ResponseFormatOption.JSON));
	}

	public void testMapResponseFormatText() {
		assertEquals(ResponseFormat.TEXT,
			AgentRegistry.mapResponseFormat(AgentConfig.ResponseFormatOption.TEXT));
	}

	public void testMapResponseFormatDefault() {
		assertNull(AgentRegistry.mapResponseFormat(AgentConfig.ResponseFormatOption.DEFAULT));
		assertNull(AgentRegistry.mapResponseFormat(null));
	}

	public void testToTimeoutDuration() {
		assertEquals(Duration.ofSeconds(60), AgentRegistry.toTimeoutDuration(60));
		assertNull(AgentRegistry.toTimeoutDuration(null));
	}
}
