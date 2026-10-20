/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.screenshots.ScreenshotService;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.util.model.ModelService;

/**
 * That {@code browserScreenshot} hands the model a URL it can show, not only a TID (issue #216).
 *
 * <p>
 * Before this, the answer named the persisted object and nothing else, so the picture was
 * write-only from the user's point of view: a TID renders as nothing. The answer now carries the
 * viewing URL and the exact Markdown the model is to write, and both are pinned here - the second
 * because a URL the model does not know what to do with is the same gap again.
 * </p>
 */
public class TestBrowserScreenshotAnswer extends BasicTestCase {

	/** A minimal but valid 1x1 PNG. */
	private static final byte[] PNG_BYTES = {
		(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
		0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
		0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
		0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
		(byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
		0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
		0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
		0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
		0x42, 0x60, (byte) 0x82
	};

	/** The answer names the viewing URL, and the markdown image the model is to write with it. */
	public void testAnswerCarriesTheViewableUrlAsAMarkdownImage() throws Exception {
		Screenshot saved;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			saved = ScreenshotService.getInstance()
				.createScreenshot("example.com.png", PNG_BYTES, "https://example.com/", null);
			tx.commit();
		}

		String answer = BrowserTool.screenshotAnswer("example.com.png", PNG_BYTES.length,
			"https://example.com/", saved);

		String url = ScreenshotService.viewUrl(saved);
		assertTrue("the route is in the answer: " + answer,
			answer.contains(ScreenshotService.URL_PREFIX + saved.getId()));
		assertTrue("as a markdown image the chat can render: " + answer,
			answer.contains("![example.com.png](" + url + ")"));
		assertTrue("and the persisted object is still named: " + answer,
			answer.contains(saved.tId().asString()));
	}

	/** The tool's own description tells the model to render the URL, not to repeat it. */
	public void testDescriptionAsksForTheMarkdownImage() throws Exception {
		Method verb = null;
		for (Method method : BrowserTool.class.getMethods()) {
			if (method.getName().equals("browserScreenshot")) {
				verb = method;
			}
		}
		assertNotNull("browserScreenshot exists", verb);
		ToolMethod annotation = verb.getAnnotation(ToolMethod.class);
		assertNotNull("browserScreenshot is a @ToolMethod", annotation);
		String description = String.join("", annotation.description());
		assertTrue("the description asks for a markdown image: " + description,
			description.contains("![name](url)"));
	}

	/** Creates the test suite with the KB, the model and the screenshot store. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestBrowserScreenshotAnswer.class),
				ModelService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE));
	}
}
