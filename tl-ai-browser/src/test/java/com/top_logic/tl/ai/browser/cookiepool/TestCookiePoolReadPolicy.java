/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.browser.cookiepool;

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
import com.top_logic.tl.ai.browser.cookiepool.model.CookiePool;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * The shipped read policy for cookie pools: <b>the owner, plus administrators</b>, and
 * administrators and the system alone for a pool whose owner could not be resolved.
 *
 * <p>
 * Asserted through {@link ModelSecurity#canRead(TLObject)}, the seam {@code ViewDocumentTool} asks
 * before it loads a stored binary into a model's context. The roles are derived by the role rule of
 * {@code tl-ai-browser.binarySecurity.config.xml} from the pool's own {@code owner}, and the grant
 * that makes them count is that file's {@code <security-config>}, so this is a statement about the
 * configuration this module ships.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.security.TestPerceivableBinaryReadPolicy
 */
public class TestCookiePoolReadPolicy extends BasicTestCase {

	/** A plausible {@code storageState} payload; its content does not matter here. */
	private static final byte[] STATE_JSON = "{\"cookies\":[],\"origins\":[]}".getBytes(StandardCharsets.UTF_8);

	/** The three accounts and the two pools, made once: no test here changes them. */
	private static Person owner;

	private static Person stranger;

	private static Person administrator;

	private static CookiePool ownedPool;

	private static CookiePool orphanPool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("cookie-policy-owner");
		stranger = TestPerson.createPerson("cookie-policy-stranger");
		administrator = TestPerson.createPerson("cookie-policy-admin");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			administrator.setAdmin(true);
			tx.commit();
		}

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			CookiePoolService pools = CookiePoolService.getInstance();
			ownedPool = pools.upsertPool("owned-login", STATE_JSON, owner, false);
			// The only pool an unattended run can save: shared, and with nobody to own it.
			orphanPool = pools.upsertPool("unattended-login", STATE_JSON, null, true);
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

	/** The owner of a cookie pool may read it, and a colleague may not. */
	public void testOwnerReadsOwnPool() {
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
	 * {@link CookiePoolService} lets every account <em>select</em> a shared pool to drive a browser
	 * with, and that path never asks model security. What this rule governs is reading the bytes.
	 * </p>
	 */
	public void testOwnerlessCookiePoolIsAdministratorAndSystemOnly() {
		assertFalse(readAccessOf(owner).canRead(orphanPool));
		assertFalse(readAccessOf(stranger).canRead(orphanPool));
		assertTrue(readAccessOf(administrator).canRead(orphanPool));
		assertTrue(readsInSystemContext(orphanPool));
	}

	/** A listing tool sees its caller's own pools and nothing else. */
	public void testFilterReadableKeepsOnlyTheCallersOwn() {
		List<TLObject> all = List.of(ownedPool, orphanPool);

		assertEquals("The owner keeps their pool.", List.of(ownedPool), readAccessOf(owner).filterReadable(all));
		assertEquals("A colleague keeps nothing of it.", List.of(), readAccessOf(stranger).filterReadable(all));
		assertEquals("An administrator keeps everything.", all, readAccessOf(administrator).filterReadable(all));
	}

	/** The read policy grants reading and nothing else, for everyone but an administrator. */
	public void testOwningIsNotWriting() {
		assertFalse("Read is the only shipped grant.", readAccessOf(owner).canWrite(ownedPool));
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
	 * object, asked on a thread of its own, as the stores' writer thread and the background jobs do.
	 */
	private static boolean readsInSystemContext(TLObject object) {
		AtomicBoolean allowed = new AtomicBoolean();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread worker = new Thread(() -> {
			try {
				ThreadContext.inSystemContext(TestCookiePoolReadPolicy.class,
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
				new TestSuite(TestCookiePoolReadPolicy.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				EncryptionService.Module.INSTANCE,
				CookiePoolService.Module.INSTANCE));
	}
}
