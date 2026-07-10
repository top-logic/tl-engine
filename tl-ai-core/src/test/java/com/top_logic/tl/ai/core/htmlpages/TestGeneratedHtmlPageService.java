/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.htmlpages.GeneratedHtmlPage;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link GeneratedHtmlPageService}.
 */
public class TestGeneratedHtmlPageService extends BasicTestCase {

	private GeneratedHtmlPageService _service;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = GeneratedHtmlPageService.Module.INSTANCE.getImplementationInstance();
	}

	public void testSaveAndFind() throws Exception {
		byte[] html = "<!doctype html><html><body>hi</body></html>".getBytes(StandardCharsets.UTF_8);
		GeneratedHtmlPage page;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			page = _service.savePage("Hello", html, null);
			tx.commit();
		}
		assertNotNull(page);
		assertNotNull("id must be assigned", page.getId());
		assertFalse("id must not be empty", page.getId().isEmpty());
		assertEquals("Hello", page.getTitle());

		Optional<GeneratedHtmlPage> found = _service.findById(page.getId());
		assertTrue("page must be findable by id", found.isPresent());
		assertEquals(page.getId(), found.get().getId());
	}

	public void testFindMissingReturnsEmpty() {
		Optional<GeneratedHtmlPage> found = _service.findById("does-not-exist-token");
		assertFalse(found.isPresent());
	}

	public void testDelete() throws Exception {
		byte[] html = "<!doctype html><html></html>".getBytes(StandardCharsets.UTF_8);
		GeneratedHtmlPage page;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			page = _service.savePage("Delete-me", html, null);
			tx.commit();
		}
		String id = page.getId();
		assertTrue(_service.findById(id).isPresent());

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			_service.delete(page);
			tx.commit();
		}
		assertFalse("deleted page must no longer be findable", _service.findById(id).isPresent());
	}

	public void testIdsAreUnique() throws Exception {
		byte[] html = "<!doctype html><html></html>".getBytes(StandardCharsets.UTF_8);
		Set<String> ids = new HashSet<>();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			for (int i = 0; i < 200; i++) {
				GeneratedHtmlPage page = _service.savePage("p" + i, html, null);
				assertTrue("duplicate id generated: " + page.getId(), ids.add(page.getId()));
			}
			tx.commit();
		}
		assertEquals(200, ids.size());
	}

	public void testIdShapeIsBase64Url() throws Exception {
		byte[] html = "<!doctype html><html></html>".getBytes(StandardCharsets.UTF_8);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			GeneratedHtmlPage page = _service.savePage("x", html, null);
			tx.commit();
			String id = page.getId();
			assertEquals("id length", 22, id.length());
			assertTrue("base64url charset (A-Za-z0-9_-): " + id, id.matches("[A-Za-z0-9_-]+"));
		}
	}

	/**
	 * Creates the test suite with required service dependencies (KB + ModelService +
	 * GeneratedHtmlPageService).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestGeneratedHtmlPageService.class),
				ModelService.Module.INSTANCE,
				GeneratedHtmlPageService.Module.INSTANCE));
	}
}
