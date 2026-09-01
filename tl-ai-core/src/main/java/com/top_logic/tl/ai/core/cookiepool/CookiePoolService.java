/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cookiepool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.encryption.SymmetricEncryption;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.cookiepool.model.CookiePool;
import com.top_logic.tl.ai.core.cookiepool.model.TlAiCookiepoolFactory;
import com.top_logic.util.model.ModelService;

/**
 * Service that persists and selects {@link CookiePool} business objects: named, per-user (or
 * explicitly shared) browser login pools whose {@code storageState} JSON is stored <b>encrypted at
 * rest</b>.
 *
 * <p>
 * A cookie pool captures a browser session's authenticated login state ({@code storageState}:
 * cookies + origin storage) so a later session - including a background/unattended agent - can
 * start already logged in. The state is as sensitive as a password, so it is always encrypted with
 * the platform installation key before it touches the {@code state} {@link CookiePool#getState()
 * Binary}, and decrypted only on read, via the platform {@link EncryptionService} (its
 * {@link SymmetricEncryption}). This service never stores or returns the state in plaintext; if no
 * encryption key is installed it refuses the operation rather than falling back to plaintext.
 * </p>
 *
 * <p>
 * <b>Scoping.</b> A pool created by a user defaults to {@code owner = that user}, {@code shared =
 * false}, and is selectable only by its owner. A pool with {@code shared = true} (an admin/explicit
 * choice, not something the agent verbs set) is selectable by any session, including an unattended
 * one - the intended shared-login use case. Selection therefore returns the caller's own pools plus
 * shared pools; this owner/shared filter is enforced explicitly here rather than left to
 * {@code SecurityContext.modelSecurity()}, which grants everything in a run whose mode does not
 * enforce model access - which is the default - and therefore cannot carry this isolation.
 * </p>
 *
 * <p>
 * <b>Every pool has an owner.</b> {@link #upsertPool(String, byte[], Person) Saving a pool} refuses
 * a {@code null} owner instead of storing an owner-less pool. Such a pool would be
 * {@code shared = false} with nobody to match, so no caller could ever select it again, while the
 * save reported success; and every owner-less caller would share one bucket per pool name, so one
 * unattended run would silently overwrite another's stored login. The alternative - defaulting
 * owner-less pools to {@code shared = true} - would turn a credential store into something every
 * session can read, which is a deliberate administrative choice, not a fallback. An unattended run
 * that needs a login therefore uses a pool an administrator marked shared; it cannot create one.
 * </p>
 *
 * <p>
 * Mirrors {@code ScreenshotService} in shape: it does <b>not</b> open its own {@code Transaction}.
 * The pool-upsert call happens from the {@code browserSaveCookies} verb, a
 * {@link com.top_logic.tl.ai.tool.api.ToolMode#DATA_WRITE DATA_WRITE} tool whose transaction is
 * opened and committed by the agent loop's {@code TransactionScope}, so the pool is committed with
 * the turn. Programmatic callers (and tests) supply their own transaction.
 * </p>
 *
 * @see #upsertPool(String, byte[], Person)
 */
@ServiceDependencies({
	ModelService.Module.class,
	EncryptionService.Module.class,
})
public class CookiePoolService extends ConfiguredManagedClass<CookiePoolService.Config<?>> {

	/**
	 * Content type of the stored {@code state} blob. It holds ciphertext (encrypted
	 * <code>storageState</code> JSON), so it is a generic binary stream, not {@code application/json}.
	 */
	public static final String STATE_CONTENT_TYPE = "application/octet-stream";

	/**
	 * Configuration interface for {@link CookiePoolService}.
	 */
	public interface Config<I extends CookiePoolService> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties: the cookie-pools master toggle lives on BrowserToolsService,
		// and encryption strength is a property of the configured EncryptionService instance.
	}

	/**
	 * Creates a {@link CookiePoolService}.
	 */
	public CookiePoolService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance.
	 *
	 * @throws IllegalStateException
	 *         If the service is not started. Callers that have to <em>report</em> that case rather than
	 *         fail on it ask {@code Module.INSTANCE.isActive()} first.
	 */
	public static CookiePoolService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Creates or updates the caller's cookie pool with the given name, storing the (encrypted)
	 * {@code storageState} in the <em>ambient</em> transaction.
	 *
	 * <p>
	 * If the caller already owns a pool with this name, its state and {@code updatedAt} are updated
	 * in place (its {@code owner}/{@code shared} are left untouched). Otherwise a new pool is created
	 * owned by {@code owner} with {@code shared = false}. A shared pool owned by someone else is
	 * never overwritten - a different, caller-owned pool is created instead. The plaintext state is
	 * encrypted here and only the ciphertext is persisted.
	 * </p>
	 *
	 * <p>
	 * The owner is mandatory: an owner-less pool could never be selected again and would collide
	 * with every other owner-less save of the same name, so this refuses rather than storing one
	 * (see the class comment). Callers that cannot resolve a {@link Person} - an unattended or
	 * background run - must report that to the user instead of saving.
	 * </p>
	 *
	 * @param name
	 *        The pool handle; must not be {@code null} or blank.
	 * @param stateJson
	 *        The raw {@code storageState} JSON bytes to store (encrypted); must not be {@code null}
	 *        or empty.
	 * @param owner
	 *        The owning user; must not be {@code null}.
	 * @return The created or updated (not-yet-committed) pool.
	 * @throws IllegalArgumentException
	 *         If {@code name} is blank, {@code stateJson} is empty, or {@code owner} is
	 *         {@code null}.
	 */
	public CookiePool upsertPool(String name, byte[] stateJson, Person owner) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (stateJson == null || stateJson.length == 0) {
			throw new IllegalArgumentException("stateJson must not be empty");
		}
		if (owner == null) {
			throw new IllegalArgumentException("A cookie pool must have an owner: an unattended run has no "
				+ "Person to own the pool, and an owner-less pool could never be selected again. Save the "
				+ "login from a session with a logged-in user, or use a pool an administrator marked shared.");
		}

		byte[] cipher = encrypt(stateJson);

		CookiePool pool = findOwnedByName(name, owner);
		if (pool == null) {
			pool = TlAiCookiepoolFactory.getInstance().createCookiePool();
			pool.setName(name);
			pool.setShared(Boolean.FALSE);
			pool.setOwner(owner);
		}
		pool.setState(BinaryDataFactory.createBinaryData(cipher, STATE_CONTENT_TYPE, name));
		pool.setUpdatedAt(new Date());
		return pool;
	}

	/**
	 * Finds the pool with the given name that is selectable by {@code caller}: the caller's own pool
	 * with that name is preferred, otherwise a {@code shared} pool with that name.
	 *
	 * @param name
	 *        The pool handle to select.
	 * @param caller
	 *        The current user, or {@code null} for an unattended session (which can then only select
	 *        shared pools).
	 * @return The selectable pool, or {@link Optional#empty()} if none matches the owner/shared
	 *         filter.
	 */
	public Optional<CookiePool> findSelectablePool(String name, Person caller) {
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		CookiePool shared = null;
		for (CookiePool pool : allPools()) {
			if (!name.equals(pool.getName())) {
				continue;
			}
			if (caller != null && caller.equals(pool.getOwner())) {
				// The caller's own pool always wins over a shared one with the same name.
				return Optional.of(pool);
			}
			if (shared == null && isShared(pool)) {
				shared = pool;
			}
		}
		return Optional.ofNullable(shared);
	}

	/**
	 * Lists the pools selectable by {@code caller}: the caller's own pools plus all shared pools.
	 *
	 * @param caller
	 *        The current user, or {@code null} for an unattended session (which then sees only shared
	 *        pools).
	 * @return The selectable pools; never {@code null}.
	 */
	public List<CookiePool> listSelectablePools(Person caller) {
		List<CookiePool> result = new ArrayList<>();
		for (CookiePool pool : allPools()) {
			if (isShared(pool) || (caller != null && caller.equals(pool.getOwner()))) {
				result.add(pool);
			}
		}
		return result;
	}

	/**
	 * Decrypts and returns the {@code storageState} JSON bytes of the given pool.
	 *
	 * @param pool
	 *        The pool whose state to decrypt; must not be {@code null}.
	 * @return The plaintext {@code storageState} JSON bytes.
	 */
	public byte[] decryptState(CookiePool pool) {
		Objects.requireNonNull(pool, "pool");
		BinaryDataSource state = pool.getState();
		if (state == null) {
			throw new IllegalStateException("Cookie pool '" + pool.getName() + "' has no stored state.");
		}
		return decrypt(readBytes(state));
	}

	/**
	 * Deletes the given pool.
	 *
	 * <p>
	 * No-op if {@code pool} is {@code null}. Operates in the ambient transaction.
	 * </p>
	 */
	public void delete(CookiePool pool) {
		if (pool == null) {
			return;
		}
		pool.tDelete();
	}

	/**
	 * The pool with that name owned by {@code owner} (never {@code null}), or {@code null} if the
	 * owner has no pool of that name yet.
	 */
	private CookiePool findOwnedByName(String name, Person owner) {
		for (CookiePool pool : allPools()) {
			if (name.equals(pool.getName()) && owner.equals(pool.getOwner())) {
				return pool;
			}
		}
		return null;
	}

	private static List<CookiePool> allPools() {
		TLClass type = TlAiCookiepoolFactory.getCookiePoolType();
		List<CookiePool> result = new ArrayList<>();
		for (TLObject obj : MetaElementUtil.getAllInstancesOf(type, TLObject.class)) {
			result.add((CookiePool) obj);
		}
		return result;
	}

	private static boolean isShared(CookiePool pool) {
		return Boolean.TRUE.equals(pool.getShared());
	}

	private static byte[] encrypt(byte[] plain) {
		return encryption().encrypt(plain);
	}

	private static byte[] decrypt(byte[] cipher) {
		return encryption().decrypt(cipher);
	}

	/**
	 * The platform's symmetric encryption, or a clear failure if no installation key is configured -
	 * cookie state is never stored or read in plaintext.
	 */
	private static SymmetricEncryption encryption() {
		EncryptionService service = EncryptionService.getInstance();
		try {
			return service.getEncryption();
		} catch (IllegalStateException ex) {
			throw new IllegalStateException("Cannot encrypt/decrypt cookie pools: the platform "
				+ "EncryptionService has no installation key installed. Configure an EncryptionService "
				+ "instance with a key (see the tl-ai-demo config) before using cookie pools; cookie "
				+ "state is never stored in plaintext.", ex);
		}
	}

	private static byte[] readBytes(BinaryDataSource source) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			source.deliverTo(buffer);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read cookie pool state.", ex);
		}
		return buffer.toByteArray();
	}

	/**
	 * Module for {@link CookiePoolService}.
	 */
	public static final class Module extends TypedRuntimeModule<CookiePoolService> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<CookiePoolService> getImplementation() {
			return CookiePoolService.class;
		}
	}
}
