/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.screenshots.Screenshot;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScreenshotService}: a {@link Screenshot} is persisted with the right binary,
 * name, source URL and content type.
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
	 * Creates the test suite with required service dependencies (KB + ModelService +
	 * ScreenshotService).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScreenshotService.class),
				ModelService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE));
	}
}
