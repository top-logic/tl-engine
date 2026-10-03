/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ToolChangeSet.Creation;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Tests what {@link ToolChangeSet#pending(KnowledgeBase)} reads from a real transaction for a
 * reference whose values the engine stores as links, in a table of their own, rather than in the row
 * of the object holding the reference.
 *
 * <p>
 * A link is no object of its own. Creating, reordering or dropping one is a write of the reference it
 * stores, booked on the object holding that reference - unless the link goes with the creation or the
 * deletion of an object, which the guard checks as that (Gitea #631). Every change here is made in a
 * transaction that is rolled back right after it was read, so nothing a test writes outlives it.
 * </p>
 */
public class TestToolChangeSet extends BasicTestCase {

	private static final String MEMORY = "tl.ai.execution:UserMemory";

	private static final String SESSION = "tl.ai.execution:AgentSession";

	private static final String FOLDER = "test.objectedit:Folder";

	private static final String LETTER = "test.objectedit:Letter";

	/** Setting a reference stored as a link is a write of that reference, by the object holding it. */
	public void testSettingALinkStoredReferenceIsAWriteOfItsHolder() {
		TLObject memory = memory("FACT", null);

		ToolChangeSet changes = changesOf(() -> memory.tUpdate(part(memory, "kind"), kind("PREFERENCE")));

		assertEquals(Map.of(memory, Set.of(part(memory, "kind"))), changes.updated());
		assertEquals("The dropped link is no object of its own.", List.of(), changes.deleted());
		assertEquals(List.of(), changes.created());
	}

	/** Clearing it is one as well: the link is dropped while the object it pointed to lives on. */
	public void testClearingALinkStoredReferenceIsAWriteOfItsHolder() {
		TLObject memory = memory("FACT", session());

		ToolChangeSet changes = changesOf(() -> memory.tUpdate(part(memory, "session"), null));

		assertEquals(Map.of(memory, Set.of(part(memory, "session"))), changes.updated());
		assertEquals(List.of(), changes.deleted());
	}

	/**
	 * Adding a value to a reference of many values and removing one from it are writes of it - here
	 * in a table that holds the links of this one reference only, which is how the engine stores the
	 * members of a group.
	 */
	public void testAddingAndRemovingAGroupMemberIsAWriteOfTheGroup() {
		Group group = group();
		Person member = TestPerson.createPerson("change-set-" + System.nanoTime());
		TLStructuredTypePart members = part(group, "members");

		ToolChangeSet added = changesOf(() -> group.addMember(member));
		inTransaction(() -> group.addMember(member));
		ToolChangeSet removed = changesOf(() -> group.removeMember(member));

		assertEquals(Map.of(group, Set.of(members)), added.updated());
		assertEquals(Map.of(group, Set.of(members)), removed.updated());
		assertEquals(List.of(), removed.deleted());
	}

	/**
	 * Reordering a list stored as links is a write of it - when the list is set anew, and when only
	 * the order numbers of the existing links change.
	 */
	public void testReorderingAListIsAWriteOfIt() {
		TLObject folder = folder("First", "Second");
		TLStructuredTypePart documents = part(folder, "documents");

		ToolChangeSet replaced = changesOf(() -> folder.tUpdate(documents, reversed(documents(folder))));
		ToolChangeSet sorted =
			changesOf(() -> live(folder).sort(Comparator.comparing(TestToolChangeSet::title).reversed()));

		assertEquals(Map.of(folder, Set.of(documents)), replaced.updated());
		assertEquals(Map.of(folder, Set.of(documents)), sorted.updated());
	}

	/**
	 * Creating a part in a composition the engine stores as links is a write of that composition as
	 * well as a creation - the write the engine's own create check includes.
	 */
	public void testCreatingAPartIsAlsoAWriteOfTheComposition() {
		TLObject folder = folder("First");
		TLStructuredTypePart documents = part(folder, "documents");

		ToolChangeSet changes = changesOf(() -> {
			List<TLObject> more = new ArrayList<>(documents(folder));
			more.add(letter("Second"));
			folder.tUpdate(documents, more);
		});

		assertEquals(1, changes.created().size());
		Creation creation = changes.created().get(0);
		assertSame(folder, creation.container());
		assertEquals(documents, creation.containerReference());
		assertEquals(Map.of(folder, Set.of(documents)), changes.updated());
	}

	/**
	 * An object created with its references set is checked as its creation alone: its links are part
	 * of it, and an object that does not exist yet has no rights to ask about.
	 */
	public void testCreatingTheHolderIsTheCreationAlone() {
		ToolChangeSet changes = changesOf(() -> newMemory("FACT", null));

		assertEquals(1, changes.created().size());
		assertEquals(Map.of(), changes.updated());
	}

	/**
	 * Deleting an object drops the links that pointed to it. That is no write of the objects holding
	 * them: it is the consequence of the deletion, which is checked as that.
	 */
	public void testDeletingAReferencedObjectIsNoWriteOfTheReferrers() {
		TLObject session = session();
		memory("FACT", session);

		ToolChangeSet changes = changesOf(session::tDelete);

		assertEquals(List.of(session), changes.deleted());
		assertEquals(Map.of(), changes.updated());
	}

	/** Deleting the object holding the links drops them too, and is checked as the deletion alone. */
	public void testDeletingTheHolderIsTheDeletionAlone() {
		TLObject memory = memory("FACT", session());

		ToolChangeSet changes = changesOf(memory::tDelete);

		assertEquals(List.of(memory), changes.deleted());
		assertEquals(Map.of(), changes.updated());
	}

	/** The changes the given code makes, read before its transaction is rolled back again. */
	private static ToolChangeSet changesOf(Runnable change) {
		KnowledgeBase kb = kb();
		try (Transaction tx = kb.beginTransaction()) {
			change.run();
			return ToolChangeSet.pending(kb);
		}
	}

	private static void inTransaction(Runnable change) {
		try (Transaction tx = kb().beginTransaction()) {
			change.run();
			tx.commit();
		}
	}

	private static TLObject memory(String kind, TLObject session) {
		try (Transaction tx = kb().beginTransaction()) {
			TLObject memory = newMemory(kind, session);
			tx.commit();
			return memory;
		}
	}

	private static TLObject newMemory(String kind, TLObject session) {
		TLObject memory = newObject(MEMORY);
		memory.tUpdate(part(memory, "owner"), PersonManager.getManager().getRoot());
		memory.tUpdate(part(memory, "kind"), kind(kind));
		memory.tUpdate(part(memory, "text"), "Prefers short answers.");
		memory.tUpdate(part(memory, "createdAt"), new Date());
		memory.tUpdate(part(memory, "session"), session);
		return memory;
	}

	private static TLObject session() {
		try (Transaction tx = kb().beginTransaction()) {
			TLObject session = newObject(SESSION);
			session.tUpdate(part(session, "kind"), TLModelUtil.findPart("tl.ai.execution:SessionKind#CHAT"));
			session.tUpdate(part(session, "createdAt"), new Date());
			tx.commit();
			return session;
		}
	}

	private static Group group() {
		try (Transaction tx = kb().beginTransaction()) {
			Group group = Group.createGroup("change-set-" + System.nanoTime());
			tx.commit();
			return group;
		}
	}

	private static TLObject folder(String... titles) {
		try (Transaction tx = kb().beginTransaction()) {
			TLObject folder = newObject(FOLDER);
			folder.tUpdate(part(folder, "name"), "change-set-" + System.nanoTime());
			List<TLObject> letters = new ArrayList<>();
			for (String title : titles) {
				letters.add(letter(title));
			}
			folder.tUpdate(part(folder, "documents"), letters);
			tx.commit();
			return folder;
		}
	}

	private static TLObject letter(String title) {
		TLObject letter = newObject(LETTER);
		letter.tUpdate(part(letter, "title"), title);
		return letter;
	}

	@SuppressWarnings("unchecked")
	private static List<TLObject> documents(TLObject folder) {
		return new ArrayList<>((Collection<TLObject>) folder.tValue(part(folder, "documents")));
	}

	/** The folder's documents as the engine's live list, which writes through to the links. */
	@SuppressWarnings("unchecked")
	private static List<TLObject> live(TLObject folder) {
		return (List<TLObject>) WrapperMetaAttributeUtil.getLiveCollection(folder, part(folder, "documents"));
	}

	private static List<TLObject> reversed(List<TLObject> list) {
		List<TLObject> result = new ArrayList<>(list);
		Collections.reverse(result);
		return result;
	}

	private static String title(TLObject document) {
		return (String) document.tValue(part(document, "title"));
	}

	private static Object kind(String name) {
		return TLModelUtil.findPart("tl.ai.execution:MemoryKind#" + name);
	}

	private static TLObject newObject(String type) {
		return ModelService.getInstance().getFactory().createObject((TLClass) TLModelUtil.findType(type), null);
	}

	private static TLStructuredTypePart part(TLObject object, String name) {
		return object.tType().getPart(name);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/** The knowledge base, the model and real accounts: these tests work on real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestToolChangeSet.class), ModelService.Module.INSTANCE));
	}
}
