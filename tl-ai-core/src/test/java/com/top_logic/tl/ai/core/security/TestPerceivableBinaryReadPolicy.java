/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.screenshots.ScreenshotFixtures;
import com.top_logic.tl.ai.core.screenshots.ScreenshotService;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * The shipped read policy for the perceivable binaries of this module (issue #227): <b>the owner,
 * plus administrators</b>, and administrators and the system alone for a binary whose owner could
 * not be resolved.
 *
 * <p>
 * Asserted through {@link ModelSecurity#canRead(TLObject)}, the seam {@code ViewDocumentTool} asks
 * before it loads a stored binary into a model's context. Nothing here is stubbed: the roles are
 * derived by the role rules of {@code tl-ai-core.binarySecurity.config.xml} from the objects' own
 * {@code owner}, and the grants that make them count are that file's {@code <security-config>}. A
 * test that passes here is therefore a statement about the configuration this module ships, not
 * about a hand-built policy object - which is the only kind of statement worth making about a
 * default.
 * </p>
 *
 * <p>
 * {@link TestModelAccessRightsSecurity} is the other half and asks a different question: that the
 * façade maps each of its methods onto the right question of the engine. This one asks what the
 * engine answers once the shipped configuration is in front of it.
 * </p>
 */
public class TestPerceivableBinaryReadPolicy extends BasicTestCase {

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

	/** The three accounts and the two objects, made once: no test here changes them. */
	private static Person owner;

	private static Person stranger;

	private static Person administrator;

	private static Screenshot ownedShot;

	private static Screenshot orphanShot;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("binary-policy-owner");
		stranger = TestPerson.createPerson("binary-policy-stranger");
		administrator = TestPerson.createPerson("binary-policy-admin");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			administrator.setAdmin(true);
			tx.commit();
		}

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ScreenshotService screenshots = ScreenshotService.getInstance();
			ownedShot = screenshots.createScreenshot(ScreenshotFixtures.newSession(owner), "owned.png",
				PNG_BYTES, "https://example.com/");
			// The unattended capture: taken in a background session, which has no owner.
			orphanShot = screenshots.createScreenshot(ScreenshotFixtures.newSession(null), "unattended.png",
				PNG_BYTES, "https://example.com/");
			tx.commit();
		}
	}

	/**
	 * Runs each test with the security checks on, so that the read rights are decided for the
	 * account each question names, not bypassed for the system context the test runs in.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> withSecurityChecks(true, () -> {
			super.runTest();
			return null;
		});
	}

	/** The account that captured a screenshot may look at it. */
	public void testOwnerReadsOwnScreenshot() {
		assertTrue("The owner holds 'AiBinaryOwner' on their own screenshot, which Read is granted to.",
			readAccessOf(owner).canRead(ownedShot));
	}

	/** A colleague may not - the role is derived per object, never held globally. */
	public void testForeignScreenshotIsNotReadable() {
		assertFalse("A screenshot is the capturing account's own material.",
			readAccessOf(stranger).canRead(ownedShot));
	}

	/** An administrator may, by bypassing the check rather than by a grant naming them. */
	public void testAdministratorReadsAnyScreenshot() {
		assertTrue(readAccessOf(administrator).canRead(ownedShot));
		assertTrue(readAccessOf(administrator).canRead(orphanShot));
	}

	/**
	 * A screenshot of an unattended run is readable by administrators and the system, and by
	 * nobody else.
	 *
	 * <p>
	 * Not even by the account that happens to be reading: an object with no owner has no role
	 * holder at all, which is how the null-owner rule is written - as the absence of a holder
	 * rather than as a denial somebody has to remember to configure.
	 * </p>
	 */
	public void testOwnerlessScreenshotIsAdministratorAndSystemOnly() {
		assertFalse("An unattended capture belongs to nobody, so no ordinary account reads it.",
			readAccessOf(owner).canRead(orphanShot));
		assertFalse(readAccessOf(stranger).canRead(orphanShot));
		assertTrue(readAccessOf(administrator).canRead(orphanShot));
		assertTrue("Code that deliberately acts as the system reads it.", readsInSystemContext(orphanShot));
	}

	/**
	 * A listing tool sees its caller's own binaries and nothing else.
	 *
	 * <p>
	 * The same decision as {@code canRead}, asked the way a tool that reports a list has to ask it -
	 * before paginating and before reporting a count.
	 * </p>
	 */
	public void testFilterReadableKeepsOnlyTheCallersOwn() {
		List<TLObject> all = List.of(ownedShot, orphanShot);

		assertEquals("The owner keeps their screenshot.", List.of(ownedShot),
			readAccessOf(owner).filterReadable(all));
		assertEquals("A colleague keeps nothing of it.", List.of(), readAccessOf(stranger).filterReadable(all));
		assertEquals("An administrator keeps everything.", all, readAccessOf(administrator).filterReadable(all));
	}

	/**
	 * The read policy grants reading and nothing else, for everyone but an administrator.
	 *
	 * <p>
	 * What an owner may do with their own screenshot beyond looking at it is a question #227 did not
	 * answer, so the shipped configuration grants {@code Read} alone and this pins that it does.
	 * </p>
	 */
	public void testOwningIsNotWriting() {
		assertFalse("Read is the only shipped grant.", readAccessOf(owner).canWrite(ownedShot));
		assertFalse(readAccessOf(owner).canDelete(ownedShot));
	}

	/**
	 * The model rights of a run acting as the given account, built as
	 * {@code TopLogicSecurityContext.forRun} builds them.
	 */
	private static ModelSecurity readAccessOf(Person account) {
		return new ModelAccessRightsSecurity(account);
	}

	/**
	 * Whether code that deliberately acts as the system - no user identity - may read the given
	 * object.
	 *
	 * <p>
	 * Asked on a thread of its own, because a thread with no context is what the code this covers
	 * actually is - the stores' writer thread and the background jobs.
	 * </p>
	 */
	private static boolean readsInSystemContext(TLObject object) {
		AtomicBoolean allowed = new AtomicBoolean();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread worker = new Thread(() -> {
			try {
				ThreadContext.inSystemContext(TestPerceivableBinaryReadPolicy.class,
					(InContext) () -> allowed.set(new ModelAccessRightsSecurity(null).canRead(object)));
			} catch (Throwable ex) {
				failure.set(ex);
			}
		}, "system-context-read");
		worker.start();
		try {
			worker.join();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(ex);
		}
		if (failure.get() != null) {
			throw new RuntimeException("The read in a system context failed.", failure.get());
		}
		return allowed.get();
	}

	/**
	 * Creates the test suite with the services the policy is made of: the model, the access manager
	 * that derives the roles, the rights service that grants them, and the store whose objects are
	 * under test.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPerceivableBinaryReadPolicy.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE));
	}
}
