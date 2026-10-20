/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ToolChangeSet;
import com.top_logic.util.model.ModelService;

/**
 * Tests what {@link ToolChangeSet#pending(KnowledgeBase)} reads from a real transaction on the pages
 * of {@code tl.doc}, which keep values outside their own rows in two of the engine's storages: the
 * children of a page in the rows of the children, and the translations of a page's content in rows
 * of their own, one per language (Gitea #638).
 *
 * <p>
 * The storages are the ones the application ships, which is the point: {@code tl-ai-core} tests the
 * same reading on a model of its own. Every change here is made in a transaction that is rolled back
 * right after it was read.
 * </p>
 */
public class TestToolChangeSetOnDocPages extends BasicTestCase {

	/** Moving a page from one parent to another is a write of the children of both. */
	public void testMovingAPageIsAWriteOfBothParents() {
		TLObject from = page("From", "Moved");
		TLObject to = page("To");
		TLStructuredTypePart children = part(from, "children");
		TLObject moved = children(from).get(0);

		ToolChangeSet changes = changesOf(() -> {
			from.tUpdate(children, List.of());
			to.tUpdate(children, List.of(moved));
		});

		assertEquals(Set.of(children), changes.updated().getOrDefault(from, Set.of()));
		assertEquals(Set.of(children), changes.updated().getOrDefault(to, Set.of()));
	}

	/**
	 * Renaming a page is a write of its name and no write of its parent: the page did not move,
	 * although the engine reports the parent column, a key column of the table, with the change.
	 */
	public void testRenamingAPageIsNoWriteOfItsParent() {
		TLObject parent = page("Parent", "Child");
		TLObject child = children(parent).get(0);

		ToolChangeSet changes = changesOf(() -> child.tUpdate(part(child, "name"), "Renamed"));

		assertEquals(Map.of(child, Set.of(part(child, "name"))), changes.updated());
	}

	/** Changing the content of a page is a write of it, though only rows of other tables change. */
	public void testChangingTheContentOfAPageIsAWriteOfIt() {
		TLObject page = page("Page");
		TLStructuredTypePart content = part(page, "content");
		committed(() -> {
			page.tUpdate(content, text("<p>one</p>", "<p>eins</p>"));
			return page;
		});

		ToolChangeSet changes = changesOf(() -> page.tUpdate(content, text("<p>two</p>", null)));

		assertEquals(Map.of(page, Set.of(content)), changes.updated());
		assertEquals("A dropped translation is no object of its own.", List.of(), changes.deleted());
	}

	/**
	 * Deleting a page with content is the deletion alone: neither its parent nor the rows of the
	 * translations are a write to check besides it.
	 */
	public void testDeletingAPageWithContentIsTheDeletionAlone() {
		TLObject parent = page("Parent", "Child");
		TLObject child = children(parent).get(0);
		committed(() -> {
			child.tUpdate(part(child, "content"), text("<p>one</p>", "<p>eins</p>"));
			return child;
		});

		ToolChangeSet changes = changesOf(child::tDelete);

		assertEquals(List.of(child), changes.deleted());
		assertEquals(Map.of(), changes.updated());
	}

	/** The changes the given code makes, read before its transaction is rolled back again. */
	private static ToolChangeSet changesOf(Runnable change) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			change.run();
			return ToolChangeSet.pending(kb);
		}
	}

	private static TLObject committed(Supplier<TLObject> create) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLObject result = create.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * A committed page with the given name, holding committed pages with the given names.
	 *
	 * <p>
	 * The children in a transaction of their own: the engine's commit-time analysis of a page created
	 * together with its children reads the new parent where it does not exist yet, and fails.
	 * </p>
	 */
	private static TLObject page(String name, String... children) {
		TLObject page = committed(() -> newPage(name));
		if (children.length > 0) {
			committed(() -> {
				List<TLObject> pages = new ArrayList<>();
				for (String child : children) {
					pages.add(newPage(child));
				}
				page.tUpdate(part(page, "children"), pages);
				return page;
			});
		}
		return page;
	}

	private static TLObject newPage(String name) {
		TLObject page = ModelService.getInstance().getFactory()
			.createObject((TLClass) TLModelUtil.findType("tl.doc:Page"), null);
		page.tUpdate(part(page, "name"), name + "-" + System.nanoTime());
		return page;
	}

	@SuppressWarnings("unchecked")
	private static List<TLObject> children(TLObject page) {
		return new ArrayList<>((Collection<TLObject>) page.tValue(part(page, "children")));
	}

	/** A content in English and, unless <code>null</code>, in German. */
	private static I18NStructuredText text(String english, String german) {
		Map<Locale, StructuredText> content = new HashMap<>();
		content.put(Locale.ENGLISH, new StructuredText(english));
		if (german != null) {
			content.put(Locale.GERMAN, new StructuredText(german));
		}
		return new I18NStructuredText(content);
	}

	private static TLStructuredTypePart part(TLObject object, String name) {
		return object.tType().getPart(name);
	}

	/** The application's knowledge base and model, tl.doc among them. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(new TestSuite(TestToolChangeSetOnDocPages.class),
			ModelService.Module.INSTANCE));
	}
}
