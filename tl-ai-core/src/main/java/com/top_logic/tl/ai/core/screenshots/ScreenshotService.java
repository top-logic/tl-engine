/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.util.Date;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.screenshots.Screenshot;
import com.top_logic.tl.ai.screenshots.TlAiScreenshotsFactory;
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

	/**
	 * Configuration interface for {@link ScreenshotService}.
	 */
	public interface Config<I extends ScreenshotService> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties for v1.
	}

	/**
	 * Creates a {@link ScreenshotService}.
	 */
	public ScreenshotService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance.
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
