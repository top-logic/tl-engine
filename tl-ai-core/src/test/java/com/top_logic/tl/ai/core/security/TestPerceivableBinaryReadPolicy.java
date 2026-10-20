/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.cookiepool.CookiePoolService;
import com.top_logic.tl.ai.core.cookiepool.model.CookiePool;
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

	/** A plausible {@code storageState} payload; its content does not matter here. */
	private static final byte[] STATE_JSON = "{\"cookies\":[],\"origins\":[]}".getBytes(StandardCharsets.UTF_8);

	/** The three accounts and the four objects, made once: no test here changes them. */
	private static Person owner;

	private static Person stranger;

	private static Person administrator;

	private static Screenshot ownedShot;

	private static Screenshot orphanShot;

	private static CookiePool ownedPool;

	private static CookiePool orphanPool;

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
			ownedShot = screenshots.createScreenshot("owned.png", PNG_BYTES, "https://example.com/", owner);
			// The unattended capture: no Person could be resolved on the executing thread.
			orphanShot = screenshots.createScreenshot("unattended.png", PNG_BYTES, "https://example.com/", null);

			CookiePoolService pools = CookiePoolService.getInstance();
			ownedPool = pools.upsertPool("owned-login", STATE_JSON, owner, false);
			// The only pool an unattended run can save: shared, and with nobody to own it.
			orphanPool = pools.upsertPool("unattended-login", STATE_JSON, null, true);
			tx.commit();
		}
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

	/** The owner of a cookie pool may read it, and a colleague may not. */
	public void testCookiePoolFollowsTheSameRule() {
		assertTrue(readAccessOf(owner).canRead(ownedPool));
		assertFalse("A cookie pool is a login somebody saved; it is not readable material.",
			readAccessOf(stranger).canRead(ownedPool));
		assertTrue(readAccessOf(administrator).canRead(ownedPool));
	}

	/**
	 * A shared pool saved by an unattended run is readable by administrators and the system only.
	 *
	 * <p>
	 * Sharing a login is not the same as letting a model <em>read</em> the stored session state:
	 * {@code CookiePoolService} lets every account <em>select</em> a shared pool to drive a browser
	 * with, and that path never asks model security. What this rule governs is reading the bytes.
	 * </p>
	 */
	public void testOwnerlessCookiePoolIsAdministratorAndSystemOnly() {
		assertFalse(readAccessOf(owner).canRead(orphanPool));
		assertFalse(readAccessOf(stranger).canRead(orphanPool));
		assertTrue(readAccessOf(administrator).canRead(orphanPool));
		assertTrue(readsInSystemContext(orphanPool));
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
		List<TLObject> all = List.of(ownedShot, orphanShot, ownedPool, orphanPool);

		assertEquals("The owner keeps their screenshot and their pool.", List.of(ownedShot, ownedPool),
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
		assertFalse(readAccessOf(owner).canWrite(ownedPool));
		assertFalse(readAccessOf(owner).canDelete(ownedPool));
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
	 * Asked on a thread of its own, because that is the only place the question exists:
	 * {@link ThreadContext#inSystemContext} establishes a system context only where no user
	 * interaction is active, and a test method runs inside one. A thread with no context is also
	 * what the code this covers actually is - the stores' writer thread and the background jobs.
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
	 * that derives the roles, the rights service that grants them, and the two stores whose objects
	 * are under test.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPerceivableBinaryReadPolicy.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				EncryptionService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE,
				CookiePoolService.Module.INSTANCE));
	}
}
