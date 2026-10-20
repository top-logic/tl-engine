/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.htmlpages.model.GeneratedHtmlPage;
import com.top_logic.tl.ai.core.htmlpages.model.TlAiHtmlpagesFactory;
import com.top_logic.util.model.ModelService;

/**
 * Service that persists and retrieves {@link GeneratedHtmlPage} entities.
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class GeneratedHtmlPageService extends ConfiguredManagedClass<GeneratedHtmlPageService.Config<?>> {

	/** Bytes of randomness for ids; 16 bytes encodes to 22 base64url characters without padding. */
	private static final int ID_BYTE_LENGTH = 16;

	/** Content type used when wrapping persisted HTML as {@link com.top_logic.basic.io.binary.BinaryData}. */
	private static final String HTML_CONTENT_TYPE = "text/html";

	/**
	 * Configuration interface for {@link GeneratedHtmlPageService}.
	 */
	public interface Config<I extends GeneratedHtmlPageService> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties for v1.
	}

	private final SecureRandom _random = new SecureRandom();

	/**
	 * Creates a {@link GeneratedHtmlPageService}.
	 */
	public GeneratedHtmlPageService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Persists a new {@link GeneratedHtmlPage}.
	 *
	 * <p>
	 * Wraps the write in its own transaction; callers don't need to start one.
	 * </p>
	 *
	 * @param title
	 *        Human readable title; must not be {@code null} or blank.
	 * @param html
	 *        Raw HTML payload to store; must not be {@code null} or empty.
	 * @param owner
	 *        The page owner (audit metadata). May be {@code null}.
	 * @return The persisted page with its generated id populated.
	 */
	public GeneratedHtmlPage savePage(String title, byte[] html, Person owner) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title must not be blank");
		}
		if (html == null || html.length == 0) {
			throw new IllegalArgumentException("html must not be empty");
		}

		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Save generated HTML page: " + title))) {
			GeneratedHtmlPage page = TlAiHtmlpagesFactory.getInstance().createGeneratedHtmlPage();
			page.setId(generateId());
			page.setTitle(title);
			page.setHtmlContent(BinaryDataFactory.createBinaryData(html, HTML_CONTENT_TYPE, title));
			if (owner != null) {
				page.setOwner(owner);
			}
			tx.commit();
			return page;
		}
	}

	/**
	 * Looks up a page by its public id.
	 *
	 * @param id
	 *        The id previously returned via {@link GeneratedHtmlPage#getId()}.
	 * @return The page, or {@link Optional#empty()} if no page with that id exists.
	 */
	public Optional<GeneratedHtmlPage> findById(String id) {
		if (id == null || id.isEmpty()) {
			return Optional.empty();
		}
		TLClass type = TlAiHtmlpagesFactory.getGeneratedHtmlPageType();
		List<TLObject> all = MetaElementUtil.getAllInstancesOf(type, TLObject.class);
		for (TLObject obj : all) {
			GeneratedHtmlPage page = (GeneratedHtmlPage) obj;
			if (id.equals(page.getId())) {
				return Optional.of(page);
			}
		}
		return Optional.empty();
	}

	/**
	 * Deletes the given page.
	 *
	 * <p>
	 * No-op if {@code page} is {@code null}. Wraps the delete in its own transaction.
	 * </p>
	 */
	public void delete(GeneratedHtmlPage page) {
		if (page == null) {
			return;
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Delete generated HTML page: " + page.getId()))) {
			page.tDelete();
			tx.commit();
		}
	}

	private String generateId() {
		byte[] buf = new byte[ID_BYTE_LENGTH];
		_random.nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}

	/**
	 * Module for {@link GeneratedHtmlPageService}.
	 */
	public static final class Module extends TypedRuntimeModule<GeneratedHtmlPageService> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<GeneratedHtmlPageService> getImplementation() {
			return GeneratedHtmlPageService.class;
		}
	}
}
