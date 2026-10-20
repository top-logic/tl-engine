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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.util.ResKey;
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
 * Tests what {@link ToolChangeSet#pending(KnowledgeBase)} reads from a real transaction for an
 * attribute whose values the engine keeps outside the row of the object holding it: a reference
 * stored as links, in a table of their own (Gitea #631), the translations of an internationalized
 * attribute, and a composition that keeps the container in the row of each part (Gitea #638).
 *
 * <p>
 * A row that stores such a value is no object of its own. Creating, changing or dropping one is a
 * write of the attribute whose value it stores, booked on the object holding that attribute - unless
 * the row goes with the creation or the deletion of an object, which the guard checks as that. Every
 * change here is made in a transaction that is rolled back right after it was read, so nothing a test
 * writes outlives it.
 * </p>
 */
public class TestToolChangeSet extends BasicTestCase {

	private static final String MEMORY = "tl.ai.execution:UserMemory";

	private static final String SESSION = "tl.ai.execution:AgentSession";

	private static final String FOLDER = "test.objectedit:Folder";

	private static final String LETTER = "test.objectedit:Letter";

	private static final String COVER = "test.objectedit:Cover";

	private static final String SHELF = "test.objectedit:Shelf";

	private static final String BOX = "test.objectedit:Box";

	private static final String BOOK = "test.objectedit:Book";

	private static final String PAMPHLET = "test.objectedit:Pamphlet";

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

	/**
	 * Changing a translation of an internationalized attribute is a write of that attribute, by the
	 * object holding it: the engine keeps each translation in a row of a table of its own and books
	 * no change on the object (Gitea #638).
	 */
	public void testChangingATranslationIsAWriteOfItsHolder() {
		TLObject cover = cover(text("one", "eins"));

		ToolChangeSet changes = changesOf(() -> cover.tUpdate(part(cover, "caption"), text("two", "zwei")));

		assertEquals(Map.of(cover, Set.of(part(cover, "caption"))), changes.updated());
		assertEquals(List.of(), changes.deleted());
	}

	/** Adding a translation is one as well: the engine creates a row for it. */
	public void testAddingATranslationIsAWriteOfItsHolder() {
		TLObject cover = cover(text("one", null));

		ToolChangeSet changes = changesOf(() -> cover.tUpdate(part(cover, "caption"), text("one", "eins")));

		assertEquals(Map.of(cover, Set.of(part(cover, "caption"))), changes.updated());
		assertEquals(List.of(), changes.created());
	}

	/**
	 * Dropping a translation is one as well: the engine deletes its row, which is no object of its
	 * own and no deletion the guard is to ask about.
	 */
	public void testDroppingATranslationIsAWriteOfItsHolder() {
		TLObject cover = cover(text("one", "eins"));

		ToolChangeSet changes = changesOf(() -> cover.tUpdate(part(cover, "caption"), text("two", null)));

		assertEquals(Map.of(cover, Set.of(part(cover, "caption"))), changes.updated());
		assertEquals("The dropped translation is no object of its own.", List.of(), changes.deleted());
	}

	/** An object created with a translated attribute is checked as its creation alone. */
	public void testCreatingAnObjectWithATranslationIsTheCreationAlone() {
		ToolChangeSet changes = changesOf(() -> newCover(text("one", "eins")));

		assertEquals(1, changes.created().size());
		assertEquals(Map.of(), changes.updated());
	}

	/**
	 * Deleting an object drops the translations of its attributes with it. That is no write of the
	 * attribute: it is the deletion, which is checked as that.
	 */
	public void testDeletingAnObjectWithATranslationIsTheDeletionAlone() {
		TLObject cover = cover(text("one", "eins"));

		ToolChangeSet changes = changesOf(cover::tDelete);

		assertEquals(List.of(cover), changes.deleted());
		assertEquals(Map.of(), changes.updated());
	}

	/**
	 * Adding a part to a composition that keeps the container in the part's own row - as tl.doc keeps
	 * the children of a page - and removing one are writes of the container's composition. The rows
	 * that change are the parts' own; the guard still asks for the right to write them, as before.
	 */
	public void testAddingAndRemovingAPartKeptInItsOwnRowIsAWriteOfTheContainer() {
		TLObject shelf = shelf("First", "Second");
		TLObject loose = committed(() -> book("Loose"));
		TLStructuredTypePart books = part(shelf, "books");
		TLObject second = books(shelf).get(1);

		ToolChangeSet added = changesOf(() -> shelf.tUpdate(books, List.of(books(shelf).get(0), second, loose)));
		ToolChangeSet removed = changesOf(() -> shelf.tUpdate(books, List.of(books(shelf).get(0))));

		assertWrite(added, shelf, books);
		assertTrue("The row of the part changed as well: " + added.updated(), added.updated().containsKey(loose));
		assertWrite(removed, shelf, books);
		assertEquals("A part removed from its container lives on.", List.of(), removed.deleted());
	}

	/**
	 * Reordering a list kept in the rows of its parts is a write of it, though it changes only their
	 * order numbers.
	 */
	public void testReorderingPartsKeptInTheirOwnRowsIsAWriteOfTheContainer() {
		TLObject shelf = shelf("First", "Second");
		TLStructuredTypePart books = part(shelf, "books");

		ToolChangeSet changes = changesOf(() -> shelf.tUpdate(books, reversed(books(shelf))));

		assertWrite(changes, shelf, books);
	}

	/** Moving a part from one container to another is a write of both. */
	public void testMovingAPartKeptInItsOwnRowIsAWriteOfBothContainers() {
		TLObject from = shelf("First");
		TLObject to = shelf();
		TLStructuredTypePart books = part(from, "books");
		TLObject first = books(from).get(0);

		ToolChangeSet changes = changesOf(() -> {
			from.tUpdate(books, List.of());
			to.tUpdate(books, List.of(first));
		});

		assertWrite(changes, from, books);
		assertWrite(changes, to, books);
	}

	/**
	 * Changing an attribute of a part kept in its own row is a write of that attribute, and no write
	 * of the container: the part did not move. The engine reports the container column, a key column
	 * of the table as tl.doc's is, with every change of the row, though its value did not change.
	 */
	public void testChangingAPartKeptInItsOwnRowIsNoWriteOfTheContainer() {
		TLObject shelf = shelf("First");
		TLObject first = books(shelf).get(0);

		ToolChangeSet changes = changesOf(() -> first.tUpdate(part(first, "title"), "Renamed"));

		assertEquals(Map.of(first, Set.of(part(first, "title"))), changes.updated());
	}

	/** The same holds for a set of parts kept in their own rows. */
	public void testAddingAPartToASetKeptInTheRowsOfItsPartsIsAWriteOfTheContainer() {
		TLObject box = committed(() -> newObject(BOX));
		TLObject loose = committed(() -> newObject(PAMPHLET));
		TLStructuredTypePart pamphlets = part(box, "pamphlets");

		ToolChangeSet changes = changesOf(() -> box.tUpdate(pamphlets, List.of(loose)));

		assertWrite(changes, box, pamphlets);
	}

	/**
	 * Creating a part in a composition kept in the rows of its parts is a write of that composition
	 * as well as a creation, as for one the engine keeps as links.
	 */
	public void testCreatingAPartKeptInItsOwnRowIsAlsoAWriteOfTheContainer() {
		TLObject shelf = shelf("First");
		TLStructuredTypePart books = part(shelf, "books");

		ToolChangeSet changes = changesOf(() -> shelf.tUpdate(books, List.of(books(shelf).get(0), book("Second"))));

		assertEquals(1, changes.created().size());
		Creation creation = changes.created().get(0);
		assertSame(shelf, creation.container());
		assertEquals(books, creation.containerReference());
		assertEquals(Map.of(shelf, Set.of(books)), changes.updated());
	}

	/** Deleting a part kept in its own row is checked as the deletion alone. */
	public void testDeletingAPartKeptInItsOwnRowIsTheDeletionAlone() {
		TLObject shelf = shelf("First", "Second");
		TLObject first = books(shelf).get(0);

		ToolChangeSet changes = changesOf(first::tDelete);

		assertEquals(List.of(first), changes.deleted());
		assertNull("The container lost a part as a consequence of its deletion.", changes.updated().get(shelf));
	}

	/** Asserts that the given changes book exactly the given attribute as written on the given object. */
	private static void assertWrite(ToolChangeSet changes, TLObject holder, TLStructuredTypePart attribute) {
		assertEquals("Booked: " + changes.updated(), Set.of(attribute),
			changes.updated().getOrDefault(holder, Set.of()));
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

	private static TLObject shelf(String... titles) {
		return committed(() -> {
			TLObject shelf = newObject(SHELF);
			List<TLObject> books = new ArrayList<>();
			for (String title : titles) {
				books.add(book(title));
			}
			shelf.tUpdate(part(shelf, "books"), books);
			return shelf;
		});
	}

	private static TLObject book(String title) {
		TLObject book = newObject(BOOK);
		book.tUpdate(part(book, "title"), title);
		return book;
	}

	@SuppressWarnings("unchecked")
	private static List<TLObject> books(TLObject shelf) {
		return new ArrayList<>((Collection<TLObject>) shelf.tValue(part(shelf, "books")));
	}

	private static TLObject committed(Supplier<TLObject> create) {
		try (Transaction tx = kb().beginTransaction()) {
			TLObject result = create.get();
			tx.commit();
			return result;
		}
	}

	private static TLObject cover(ResKey caption) {
		try (Transaction tx = kb().beginTransaction()) {
			TLObject cover = newCover(caption);
			tx.commit();
			return cover;
		}
	}

	private static TLObject newCover(ResKey caption) {
		TLObject cover = newObject(COVER);
		cover.tUpdate(part(cover, "caption"), caption);
		return cover;
	}

	/** A text in English and, unless <code>null</code>, in German. */
	static ResKey text(String english, String german) {
		ResKey.Builder text = ResKey.builder().add(Locale.ENGLISH, english);
		if (german != null) {
			text.add(Locale.GERMAN, german);
		}
		return text.build();
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
