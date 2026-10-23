/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScreenshotService}: a {@link Screenshot} is persisted with the right binary,
 * name, source URL and content type, carries the unguessable id its viewing URL is built from, and
 * is readable by its owner only.
 */
public class TestScreenshotService extends BasicTestCase {

	/** A minimal but valid 1x1 PNG (signature + IHDR/IDAT/IEND). */
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

	private ScreenshotService _service;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = ScreenshotService.Module.INSTANCE.getImplementationInstance();
	}

	public void testCreatePersistsBinaryAndMetadata() throws Exception {
		Screenshot shot;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			shot = _service.createScreenshot("example.com-20260724-101500.png", PNG_BYTES,
				"https://example.com/", null);
			tx.commit();
		}

		assertNotNull(shot);
		assertEquals("example.com-20260724-101500.png", shot.getName());
		assertEquals("https://example.com/", shot.getSourceUrl());
		assertEquals(ScreenshotService.PNG_CONTENT_TYPE, shot.getContentType());
		assertNotNull("capturedAt is set", shot.getCapturedAt());
		assertNull("no owner supplied", shot.getOwner());

		BinaryDataSource image = shot.getImage();
		assertNotNull("image binary persisted", image);
		assertEquals("image content type", ScreenshotService.PNG_CONTENT_TYPE, image.getContentType());
		assertTrue("stored bytes match the captured PNG", bytesEqual(image, PNG_BYTES));

		assertNotNull("a stable TID is assigned", shot.tId());
		assertNotNull(shot.tId().asString());
	}

	/** Every screenshot gets an unguessable handle, and it is the one its viewing URL carries. */
	public void testCaptureIsAddressableByAnUnguessableId() throws Exception {
		Screenshot shot;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			shot = _service.createScreenshot("addressable.png", PNG_BYTES, "https://example.com/", null);
			tx.commit();
		}

		String id = shot.getId();
		assertNotNull("an id is assigned on creation", id);
		// 16 random bytes, base64url without padding.
		assertEquals("the id is 128 bits of randomness", 22, id.length());
		// The URL carries the deployment's host and context path in front of the route; only the
		// route itself is this class's business.
		assertTrue("the viewing URL addresses it by that id",
			ScreenshotService.viewUrl(shot).endsWith(ScreenshotService.URL_PREFIX + id));
		assertSame("the id finds exactly that screenshot", shot, _service.findById(id).get());
	}

	/** Two captures never share a handle. */
	public void testIdsAreDistinct() throws Exception {
		Screenshot first;
		Screenshot second;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			first = _service.createScreenshot("a.png", PNG_BYTES, "https://example.com/a", null);
			second = _service.createScreenshot("b.png", PNG_BYTES, "https://example.com/b", null);
			tx.commit();
		}
		assertFalse("two captures get two ids", first.getId().equals(second.getId()));
	}

	/** An id nobody stored finds nothing - the route answers 404 off this. */
	public void testUnknownIdFindsNothing() throws Exception {
		assertTrue(_service.findById("no-such-screenshot-id").isEmpty());
		assertTrue(_service.findById(null).isEmpty());
		assertTrue(_service.findById("").isEmpty());
	}

	/**
	 * The read rule: the owner, an administrator, nobody else - and an owner-less capture only an
	 * administrator.
	 */
	public void testOnlyTheOwnerAndAnAdministratorMayRead() throws Exception {
		Person owner = TestPerson.createPerson("screenshot-owner");
		Person stranger = TestPerson.createPerson("screenshot-stranger");
		Person admin = TestPerson.createPerson("screenshot-admin");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			admin.setAdmin(true);
			tx.commit();
		}

		Screenshot owned;
		Screenshot unattended;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			owned = _service.createScreenshot("owned.png", PNG_BYTES, "https://example.com/", owner);
			unattended = _service.createScreenshot("unattended.png", PNG_BYTES, "https://example.com/", null);
			tx.commit();
		}

		assertTrue("the owner reads their own capture", ScreenshotService.mayRead(owned, owner));
		assertTrue("an administrator reads any capture", ScreenshotService.mayRead(owned, admin));
		assertFalse("a colleague does not", ScreenshotService.mayRead(owned, stranger));
		assertFalse("and nobody is not a reader", ScreenshotService.mayRead(owned, null));

		assertFalse("an owner-less capture is not everybody's", ScreenshotService.mayRead(unattended, owner));
		assertTrue("an administrator still reads it", ScreenshotService.mayRead(unattended, admin));
	}

	public void testRejectsBlankNameAndEmptyImage() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			try {
				_service.createScreenshot("  ", PNG_BYTES, "https://example.com/", null);
				fail("blank name must be rejected");
			} catch (IllegalArgumentException expected) {
				// expected
			}
			try {
				_service.createScreenshot("x.png", new byte[0], "https://example.com/", null);
				fail("empty image must be rejected");
			} catch (IllegalArgumentException expected) {
				// expected
			}
		}
	}

	private static boolean bytesEqual(BinaryDataSource source, byte[] expected) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		source.deliverTo(buffer);
		return Arrays.equals(buffer.toByteArray(), expected);
	}

	/**
	 * Creates the test suite with required service dependencies (KB + accounts + ModelService +
	 * ScreenshotService).
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScreenshotService.class),
				ModelService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE));
	}
}
