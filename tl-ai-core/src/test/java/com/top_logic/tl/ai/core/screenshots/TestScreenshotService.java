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
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScreenshotService}: a {@link Screenshot} is persisted with the right binary,
 * name, source URL and content type, carries the unguessable id its viewing URL is built from, is
 * readable by its owner only, and is a part of the session it was taken in - deleted with it.
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

	private AgentExecutionStore _store;

	/** A session without an owner, as an unattended run has. */
	private AgentSession _background;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = ScreenshotService.Module.INSTANCE.getImplementationInstance();
		_store = AgentExecutionStore.getInstance();
		_background = session(SessionKind.BACKGROUND, null);
	}

	@Override
	protected void tearDown() throws Exception {
		_store.deleteSession(_background);
		advance();
		super.tearDown();
	}

	public void testCreatePersistsBinaryAndMetadata() throws Exception {
		Screenshot shot;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			shot = _service.createScreenshot(_background, "example.com-20260724-101500.png", PNG_BYTES,
				"https://example.com/");
			tx.commit();
		}

		assertNotNull(shot);
		assertEquals("example.com-20260724-101500.png", shot.getName());
		assertEquals("https://example.com/", shot.getSourceUrl());
		assertEquals(ScreenshotService.PNG_CONTENT_TYPE, shot.getContentType());
		assertNotNull("capturedAt is set", shot.getCapturedAt());
		assertNull("a session without an owner gives none", shot.getOwner());
		assertEquals("a part of its session", _background, shot.tContainer());

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
			shot = _service.createScreenshot(_background, "addressable.png", PNG_BYTES, "https://example.com/");
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
			first = _service.createScreenshot(_background, "a.png", PNG_BYTES, "https://example.com/a");
			second = _service.createScreenshot(_background, "b.png", PNG_BYTES, "https://example.com/b");
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

		AgentSession chat = session(SessionKind.CHAT, owner);
		Screenshot owned;
		Screenshot unattended;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			owned = _service.createScreenshot(chat, "owned.png", PNG_BYTES, "https://example.com/");
			unattended = _service.createScreenshot(_background, "unattended.png", PNG_BYTES, "https://example.com/");
			tx.commit();
		}

		assertTrue("the owner reads their own capture", ScreenshotService.mayRead(owned, owner));
		assertTrue("an administrator reads any capture", ScreenshotService.mayRead(owned, admin));
		assertFalse("a colleague does not", ScreenshotService.mayRead(owned, stranger));
		assertFalse("and nobody is not a reader", ScreenshotService.mayRead(owned, null));

		assertFalse("an owner-less capture is not everybody's", ScreenshotService.mayRead(unattended, owner));
		assertTrue("an administrator still reads it", ScreenshotService.mayRead(unattended, admin));

		assertEquals("the owner of a capture is the owner of its session", owner, owned.getOwner());
		_store.deleteSession(chat);
		advance();
	}

	/** Deleting a session deletes the screenshots taken in it, and only those. */
	public void testDeletingTheSessionDeletesItsScreenshots() throws Exception {
		Person owner = TestPerson.createPerson("screenshot-chat-owner");
		AgentSession chat = session(SessionKind.CHAT, owner);
		Screenshot inChat;
		Screenshot elsewhere;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			inChat = _service.createScreenshot(chat, "in-chat.png", PNG_BYTES, "https://example.com/");
			elsewhere = _service.createScreenshot(_background, "elsewhere.png", PNG_BYTES, "https://example.com/");
			tx.commit();
		}
		String id = inChat.getId();
		assertEquals("the chat holds its screenshot", 1, chat.getScreenshots().size());

		_store.deleteSession(chat);
		advance();

		assertFalse("the chat's screenshot is gone with it", inChat.tValid());
		assertTrue("and no longer found by its id", _service.findById(id).isEmpty());
		assertTrue("a screenshot of another session stays", elsewhere.tValid());
	}

	/**
	 * A screenshot a run takes is stored in the session of that run - a chat as well as an
	 * unattended background run - and is committed by the store, without dereferencing the run on
	 * the calling thread.
	 */
	public void testTheStoreKeepsTheScreenshotInTheSessionOfTheRun() throws Exception {
		Person owner = TestPerson.createPerson("screenshot-run-owner");
		AgentSession chat = session(SessionKind.CHAT, owner);
		AgentExecution chatRun = _store.beginExecution(chat, "chat-run", "test-agent", "test-model", null, null);
		AgentExecution backgroundRun =
			_store.beginExecution(_background, "background-run", "test-agent", "test-model", null, null);

		// The session revision is deliberately not advanced between creating the runs and storing
		// the screenshots: the calling thread does not see the rows, and must not need to.
		ScreenshotService.Saved inChat =
			_store.createScreenshot(chatRun, "chat.png", PNG_BYTES, "https://example.com/chat");
		ScreenshotService.Saved inBackground =
			_store.createScreenshot(backgroundRun, "background.png", PNG_BYTES, "https://example.com/bg");
		advance();

		Screenshot chatShot = _service.findById(inChat.id()).get();
		assertEquals("a part of the chat", chat, chatShot.tContainer());
		assertEquals("owned by the chat's owner", owner, chatShot.getOwner());
		assertEquals(chatShot.tId().asString(), inChat.objectId());
		assertTrue(inChat.viewUrl().endsWith(ScreenshotService.URL_PREFIX + inChat.id()));

		Screenshot backgroundShot = _service.findById(inBackground.id()).get();
		assertEquals("a part of the background session", _background, backgroundShot.tContainer());
		assertNull("owned by nobody, like its session", backgroundShot.getOwner());

		_store.deleteSession(chat);
		advance();
		assertFalse("deleted with the chat", chatShot.tValid());
	}

	public void testRejectsBlankNameAndEmptyImage() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			try {
				_service.createScreenshot(_background, "  ", PNG_BYTES, "https://example.com/");
				fail("blank name must be rejected");
			} catch (IllegalArgumentException expected) {
				// expected
			}
			try {
				_service.createScreenshot(_background, "x.png", new byte[0], "https://example.com/");
				fail("empty image must be rejected");
			} catch (IllegalArgumentException expected) {
				// expected
			}
		}
	}

	private AgentSession session(SessionKind kind, Person owner) {
		AgentSession session = _store.createSession(kind, owner, null);
		advance();
		return session;
	}

	/** Advances the session revision so the store's writer-thread commits become visible. */
	private static void advance() {
		HistoryUtils.updateSessionRevision();
	}

	private static boolean bytesEqual(BinaryDataSource source, byte[] expected) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		source.deliverTo(buffer);
		return Arrays.equals(buffer.toByteArray(), expected);
	}

	/**
	 * Creates the test suite with required service dependencies (KB + accounts + ModelService +
	 * ScreenshotService + AgentExecutionStore).
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScreenshotService.class),
				ModelService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
