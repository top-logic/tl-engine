/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.ApplicationUrl;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.tl.ai.core.screenshots.model.TlAiScreenshotsFactory;
import com.top_logic.tl.ai.tool.api.AICategory;
import com.top_logic.util.model.ModelService;

/**
 * Service that persists {@link Screenshot} business objects (a captured PNG plus its metadata).
 *
 * <p>
 * A screenshot is a part of the {@link AgentSession} it was taken in
 * ({@link AgentSession#getScreenshots()}, a composition): it lives as long as that conversation and
 * is deleted with it. The {@code browserScreenshot} agent verb stores its capture through
 * {@link com.top_logic.tl.ai.core.execution.AgentExecutionStore#createScreenshot}, which resolves the
 * run's session on the store's writer thread and calls {@link #createScreenshot(AgentSession, String,
 * byte[], String)} there. This service does not open a {@code Transaction} of its own; programmatic
 * callers (and tests) supply their own transaction around the screenshot-creating call.
 * </p>
 *
 * @see #createScreenshot(AgentSession, String, byte[], String)
 */
@ServiceDependencies({
	ModelService.Module.class,
})
@ServiceCategory.Of(AICategory.class)
public class ScreenshotService extends ConfiguredManagedClass<ScreenshotService.Config<?>> {

	/** Content type of the stored image. Screenshots are always captured as PNG. */
	public static final String PNG_CONTENT_TYPE = "image/png";

	/** The path {@link ScreenshotServlet} is mapped to, including its trailing slash. */
	public static final String URL_PREFIX = "/screenshot/";

	/** Bytes of randomness for ids; 16 bytes encodes to 22 base64url characters without padding. */
	private static final int ID_BYTE_LENGTH = 16;

	/**
	 * What identifies a stored screenshot, as plain data.
	 *
	 * @param id
	 *        The {@link Screenshot#getId() id} its viewing URL carries.
	 * @param objectId
	 *        The external form of the object's identifier.
	 */
	public record Saved(String id, String objectId) {

		/**
		 * The URL at which {@link ScreenshotServlet} serves the screenshot.
		 *
		 * @see ScreenshotService#viewUrl(String)
		 */
		public String viewUrl() {
			return ScreenshotService.viewUrl(id);
		}
	}

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
	 * Creates a new {@link Screenshot} as a part of the given session, in the <em>ambient</em>
	 * transaction.
	 *
	 * <p>
	 * The caller must have an active KB transaction; this method does not begin or commit one, so
	 * the created object is committed with the caller's transaction. The screenshot's
	 * {@link Screenshot#getOwner() owner} is the session's owner, which is what
	 * {@link #mayRead(Screenshot, Person)} and the shipped read policy decide by; a screenshot of a
	 * session without an owner (an unattended run) is readable by administrators only.
	 * </p>
	 *
	 * @param session
	 *        The conversation the screenshot was taken in; must not be {@code null}.
	 * @param name
	 *        Human-readable filename/label; must not be {@code null} or blank.
	 * @param image
	 *        The PNG payload to store; must not be {@code null} or empty.
	 * @param sourceUrl
	 *        The URL the page was captured from. May be {@code null}.
	 * @return The freshly created (not-yet-committed) screenshot.
	 */
	public Screenshot createScreenshot(AgentSession session, String name, byte[] image, String sourceUrl) {
		Objects.requireNonNull(session, "A screenshot is a part of a session.");
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
		Person owner = session.getOwner();
		if (owner != null) {
			screenshot.setOwner(owner);
		}
		session.addScreenshot(screenshot);
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
	 * A screenshot captured by an unattended run belongs to a session without an owner and so has
	 * no owner itself. It is readable by an administrator only - the safe direction, not an
	 * oversight.
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
	 *        The screenshot to address. May be <code>null</code>.
	 * @return The absolute or root-relative URL, or <code>null</code> for no screenshot.
	 * @see ApplicationUrl
	 */
	public static String viewUrl(Screenshot screenshot) {
		if (screenshot == null) {
			return null;
		}
		return viewUrl(screenshot.getId());
	}

	/**
	 * The URL at which {@link ScreenshotServlet} serves the screenshot with the given id.
	 *
	 * @param id
	 *        The value of {@link Screenshot#getId()}. May be <code>null</code>.
	 * @return The absolute or root-relative URL, or <code>null</code> for no id.
	 * @see ApplicationUrl
	 */
	public static String viewUrl(String id) {
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
