/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.ApplicationUrl;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.tl.ai.core.screenshots.model.TlAiScreenshotsFactory;
import com.top_logic.util.model.ModelService;

/**
 * Service that persists {@link Screenshot} business objects (a captured PNG plus its metadata).
 *
 * <p>
 * Mirrors {@code GeneratedHtmlPageService} in shape, with one deliberate difference: it does
 * <b>not</b> open its own {@code Transaction}. Screenshots are created by the {@code browserScreenshot}
 * agent verb, a {@link com.top_logic.tl.ai.tool.api.ToolMode#DATA_WRITE DATA_WRITE} tool whose
 * transaction is opened and committed by the agent loop's
 * {@link com.top_logic.tl.ai.core.agent.TransactionScope} (the "tool transaction seam"). Creating the
 * object in that ambient transaction keeps the screenshot atomic with the rest of the turn - it is
 * committed when the turn commits and rolled back if the turn is rolled back. Programmatic callers
 * (and tests) therefore supply their own transaction around the screenshot-creating call.
 * </p>
 *
 * @see #createScreenshot(String, byte[], String, Person)
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class ScreenshotService extends ConfiguredManagedClass<ScreenshotService.Config<?>> {

	/** Content type of the stored image. Screenshots are always captured as PNG. */
	public static final String PNG_CONTENT_TYPE = "image/png";

	/** The path {@link ScreenshotServlet} is mapped to, including its trailing slash. */
	public static final String URL_PREFIX = "/screenshot/";

	/** Bytes of randomness for ids; 16 bytes encodes to 22 base64url characters without padding. */
	private static final int ID_BYTE_LENGTH = 16;

	/**
	 * Configuration interface for {@link ScreenshotService}.
	 */
	public interface Config<I extends ScreenshotService> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties for v1.
	}

	private final SecureRandom _random = new SecureRandom();

	/**
	 * Creates a {@link ScreenshotService}.
	 */
	public ScreenshotService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance.
	 *
	 * @throws IllegalStateException
	 *         If the service is not started. Callers that have to <em>report</em> that case rather than
	 *         fail on it ask {@code Module.INSTANCE.isActive()} first.
	 */
	public static ScreenshotService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Creates a new {@link Screenshot} in the <em>ambient</em> transaction.
	 *
	 * <p>
	 * The caller must have an active KB transaction (the agent loop opens one for the
	 * {@code DATA_WRITE} {@code browserScreenshot} tool; tests open their own). This method does not
	 * begin or commit a transaction so the created object participates in - and is committed with -
	 * the caller's transaction.
	 * </p>
	 *
	 * @param name
	 *        Human-readable filename/label; must not be {@code null} or blank.
	 * @param image
	 *        The PNG payload to store; must not be {@code null} or empty.
	 * @param sourceUrl
	 *        The URL the page was captured from. May be {@code null}.
	 * @param owner
	 *        The capturing user (audit metadata). May be {@code null} when it cannot be resolved
	 *        safely on the execution thread.
	 * @return The freshly created (not-yet-committed) screenshot.
	 */
	public Screenshot createScreenshot(String name, byte[] image, String sourceUrl, Person owner) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (image == null || image.length == 0) {
			throw new IllegalArgumentException("image must not be empty");
		}

		Screenshot screenshot = TlAiScreenshotsFactory.getInstance().createScreenshot();
		screenshot.setId(generateId());
		screenshot.setName(name);
		screenshot.setImage(BinaryDataFactory.createBinaryData(image, PNG_CONTENT_TYPE, name));
		screenshot.setSourceUrl(sourceUrl);
		screenshot.setCapturedAt(new Date());
		screenshot.setContentType(PNG_CONTENT_TYPE);
		if (owner != null) {
			screenshot.setOwner(owner);
		}
		return screenshot;
	}

	/**
	 * Looks up a screenshot by the id its viewing URL carries.
	 *
	 * <p>
	 * The lookup is a scan over the type's instances, as {@code GeneratedHtmlPageService#findById}
	 * is: the id lives in generic (flex) storage, so there is no column to index it by. Screenshots
	 * are few and the scan reads only their metadata - the PNG payload stays unread until
	 * {@link Screenshot#getImage()} is called.
	 * </p>
	 *
	 * @param id
	 *        The value of {@code Screenshot#getId()}. May be <code>null</code>.
	 * @return The screenshot, or {@link Optional#empty()} when no screenshot carries that id.
	 */
	public Optional<Screenshot> findById(String id) {
		if (id == null || id.isEmpty()) {
			return Optional.empty();
		}
		TLClass type = TlAiScreenshotsFactory.getScreenshotType();
		List<TLObject> all = MetaElementUtil.getAllInstancesOf(type, TLObject.class);
		for (TLObject obj : all) {
			Screenshot screenshot = (Screenshot) obj;
			if (id.equals(screenshot.getId())) {
				return Optional.of(screenshot);
			}
		}
		return Optional.empty();
	}

	/**
	 * Whether the given account may look at the given screenshot.
	 *
	 * <p>
	 * <b>The owner, or an administrator.</b> A screenshot is a picture of a page somebody drove a
	 * browser to while logged in somewhere; it can show another system's inbox, another system's
	 * customer record, or a login form with a name in it. That is the capturing user's own
	 * material, so nobody else reads it, and an installation that wants a wider rule states one
	 * rather than inherits it.
	 * </p>
	 *
	 * <p>
	 * A screenshot captured by an unattended run has no owner. It is readable by an administrator
	 * only - the safe direction, not an oversight.
	 * </p>
	 *
	 * <p>
	 * <b>The same rule is the shipped model policy</b> since #227: the {@code <security-config>} of
	 * {@code tl-ai-core.binarySecurity.config.xml} grants Read on a screenshot to the account its
	 * {@code owner} names, which is what {@code viewDocument} asks about through
	 * {@code ModelSecurity.canRead}. This method is the same statement for the {@code /screenshot/}
	 * route, which serves bytes to a browser rather than to a model and is therefore not asked
	 * through a run's security context. The two agree by construction; an installation that
	 * <em>widens</em> the model grant does not widen this route, which is the conservative
	 * direction of the two.
	 * </p>
	 *
	 * @param screenshot
	 *        The screenshot asked for. May be <code>null</code>, which nobody may read.
	 * @param account
	 *        The account asking. May be <code>null</code>, which may read nothing.
	 */
	public static boolean mayRead(Screenshot screenshot, Person account) {
		if (screenshot == null || account == null) {
			return false;
		}
		if (Person.isAdmin(account)) {
			return true;
		}
		return account.equals(screenshot.getOwner());
	}

	/**
	 * The URL at which {@link ScreenshotServlet} serves the given screenshot.
	 *
	 * @param screenshot
	 *        The screenshot to address.
	 * @return The absolute or root-relative URL, or <code>null</code> for a screenshot that carries
	 *         no id (one captured before the viewing route existed).
	 * @see ApplicationUrl
	 */
	public static String viewUrl(Screenshot screenshot) {
		if (screenshot == null) {
			return null;
		}
		String id = screenshot.getId();
		if (id == null || id.isEmpty()) {
			return null;
		}
		return ApplicationUrl.of(URL_PREFIX + id);
	}

	private String generateId() {
		byte[] buf = new byte[ID_BYTE_LENGTH];
		_random.nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}

	/**
	 * Module for {@link ScreenshotService}.
	 */
	public static final class Module extends TypedRuntimeModule<ScreenshotService> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ScreenshotService> getImplementation() {
			return ScreenshotService.class;
		}
	}
}
