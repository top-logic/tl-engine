/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import static com.top_logic.tl.ai.index.config.IndexTestModel.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.AiIndex;
import com.top_logic.tl.ai.index.lucene.IndexFixture;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.util.model.ModelService;

/**
 * What a search shows is what the reader may read: only readable scopes, no chunk of an attribute
 * the reader may not read, no search in a system context, readable scopes computed as the reader,
 * and every hit on the reading list with the text that was shown.
 */
public class TestIndexSearchRights extends BasicTestCase {

	private static final String CONFIG = """
		<index readable-scopes="all(`test.aiindex:Project`).filter(p -> $p.get(`test.aiindex:Project#name`) == currentUser().get(`tl.accounts:Person#name`) || $p.get(`test.aiindex:Project#name`) == 'shared')">
			<types>
		""" + STATEMENT_CONFIG + """
			</types>
		</index>
		""";

	private static Person alice;

	private static Person bob;

	private static TLObject aliceProject;

	private static TLObject bobProject;

	private static TLObject shared;

	private static TLObject alicePump;

	private static TLObject aliceTwin;

	private static TLObject bobPump;

	private static TLObject sharedPump;

	private static List<TLObject> all;

	private Path _dir;

	private AiIndex _index;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (alice == null) {
			alice = TestPerson.createPerson("rights-alice");
			bob = TestPerson.createPerson("rights-bob");
			committed(() -> {
				aliceProject = create(PROJECT, Map.of("name", "rights-alice"));
				bobProject = create(PROJECT, Map.of("name", "rights-bob"));
				shared = create(PROJECT, Map.of("name", "shared"));
				alicePump = create(STATEMENT, Map.of("title", "Rho pump", "text",
					"The rho pump needs a pressure relief valve.", "secret", "Omega budget of the rho pump.",
					"project", aliceProject));
				aliceTwin = create(STATEMENT, Map.of("text", "A rho pump without a pressure relief valve fails.",
					"project", aliceProject));
				bobPump = create(STATEMENT, Map.of("text", "The rho pump of bob needs a pressure relief valve.",
					"project", bobProject));
				sharedPump = create(STATEMENT, Map.of("text", "Shared rho pump notes.", "project", shared));
				return null;
			});
			all = List.of(aliceProject, bobProject, shared, alicePump, aliceTwin, bobPump, sharedPump);
		}
		_dir = IndexFixture.tempDir("tl-ai-index-rights");
		_index = SearchFixture.start(CONFIG, _dir);
	}

	@Override
	protected void tearDown() throws Exception {
		_index.stop();
		IndexFixture.deleteRecursively(_dir);
		super.tearDown();
	}

	/**
	 * Each account finds only the scopes its readable-scopes script answers when run as that
	 * account - here the project of its name and the shared one - even with rights to read
	 * everything.
	 */
	public void testReadableScopesAreComputedAsTheReader() {
		SearchFixture.Rights everything = new SearchFixture.Rights().read(all);
		Set<String> forAlice = Set.copyOf(SearchFixture.ids(SearchFixture.asUser(alice,
			() -> _index.search(SearchRequest.of("rho pump"), everything))));
		Set<String> forBob = Set.copyOf(SearchFixture.ids(SearchFixture.asUser(bob,
			() -> _index.search(SearchRequest.of("rho pump"), everything))));

		assertEquals(Set.of(id(alicePump), id(aliceTwin), id(sharedPump)), forAlice);
		assertEquals(Set.of(id(bobPump), id(sharedPump)), forBob);
	}

	/** A query without a word finds nothing, by meaning neither. */
	public void testBlankQueryFindsNothing() {
		Hits hits = SearchFixture.asUser(alice,
			() -> _index.search(SearchRequest.of("  ").withMode(SearchMode.MEANING), new SearchFixture.Rights().read(all)));
		assertEquals(List.of(), hits.hits());
		assertEquals("The query has no word to search for.", hits.notice());
	}

	/**
	 * A filter by a keyword shows an object only to a reader of the attribute the keyword is computed
	 * from: otherwise the filter tells the value of an attribute the reader may not read.
	 */
	public void testKeywordFilterNeedsItsAttribute() {
		TLObject requirement = committed(() -> create(STATEMENT, Map.of("text", "The tau pump needs oil.", "kind",
			kind("REQUIREMENT"), "project", aliceProject)));
		SearchFixture.await(_index);
		SearchRequest requirements =
			SearchRequest.of("tau pump").withFilters(Map.of("kind", Set.of("REQUIREMENT")));
		SearchFixture.Rights withKind = new SearchFixture.Rights().read(all).read(List.of(requirement));
		SearchFixture.Rights withoutKind =
			new SearchFixture.Rights().read(all).read(List.of(requirement)).hide("kind");

		assertEquals(List.of(id(requirement)),
			SearchFixture.ids(SearchFixture.asUser(alice, () -> _index.search(requirements, withKind))));
		assertEquals(List.of(),
			SearchFixture.ids(SearchFixture.asUser(alice, () -> _index.search(requirements, withoutKind))));
	}

	/** The rights of the run decide too: a scope the script answers but the reader may not read is out. */
	public void testOnlyReadableScopes() {
		SearchFixture.Rights noShared =
			new SearchFixture.Rights().read(List.of(aliceProject, alicePump, aliceTwin, sharedPump));
		List<String> hits = SearchFixture.ids(SearchFixture.asUser(alice,
			() -> _index.search(SearchRequest.of("rho pump"), noShared)));
		assertEquals(Set.of(id(alicePump), id(aliceTwin)), Set.copyOf(hits));
	}

	/**
	 * A chunk of an attribute the reader may not read is never shown: the object found only by its
	 * secret is no hit, while its readable text still finds it.
	 */
	public void testHiddenAttributeIsNotShown() {
		SearchFixture.Rights noSecret = new SearchFixture.Rights().read(all).hide("secret");
		SearchFixture.Rights withSecret = new SearchFixture.Rights().read(all);

		Hits hidden = SearchFixture.asUser(alice, () -> _index.search(SearchRequest.of("omega budget"), noSecret));
		for (Hit hit : hidden.hits()) {
			assertFalse("The secret is never shown: " + hit.text(), hit.text().contains("Omega"));
		}
		Hits shown = SearchFixture.asUser(alice, () -> _index.search(SearchRequest.of("omega budget"), withSecret));
		assertEquals(id(alicePump), shown.hits().get(0).id());
		assertEquals("secret", shown.hits().get(0).part());

		Hits byText = SearchFixture.asUser(alice,
			() -> _index.search(SearchRequest.of("pressure relief valve").withMode(SearchMode.WORDS), noSecret));
		assertTrue(SearchFixture.ids(byText).contains(id(alicePump)));
	}

	/** A text derived by a script is shown only to a reader of the attributes it declares it reads. */
	public void testScriptTextNeedsItsAttributes() {
		SearchFixture.Rights noTitle = new SearchFixture.Rights().read(all).hide("title");
		Hits hits = SearchFixture.asUser(alice,
			() -> _index.search(SearchRequest.of("Rho pump").withMode(SearchMode.WORDS), noTitle));
		for (Hit hit : hits.hits()) {
			assertFalse(hit.part().equals("context"));
		}
	}

	/**
	 * In a system context the engine allows every read, so a search there is refused - even with
	 * rights that would allow everything.
	 */
	public void testSystemContextIsRefused() throws InterruptedException {
		SearchFixture.Rights everything = new SearchFixture.Rights().read(all);
		assertTrue("A search in a system context must be refused.",
			inSystemContext(() -> _index.search(SearchRequest.of("rho pump"), everything)));
		assertTrue("A similarity search in a system context must be refused.",
			inSystemContext(() -> _index.similar(alicePump, SearchRequest.of(null), everything)));
	}

	/** Whether the search, run on a thread of its own in a system interaction, is refused. */
	private static boolean inSystemContext(java.util.function.Supplier<Hits> search) throws InterruptedException {
		java.util.concurrent.atomic.AtomicBoolean refused = new java.util.concurrent.atomic.AtomicBoolean();
		Thread thread = new Thread(() -> ThreadContextManager.inSystemInteraction(TestIndexSearchRights.class, () -> {
			try {
				search.get();
			} catch (SearchRefusedException ex) {
				refused.set(true);
			}
		}));
		thread.start();
		thread.join(60_000);
		return refused.get();
	}

	/**
	 * "Similar to this object" finds its twin in a readable scope, not itself, and nothing the reader
	 * may not read; an object the reader may not read is not found at all.
	 */
	public void testSimilar() {
		SearchFixture.Rights alicesOwn =
			new SearchFixture.Rights().read(List.of(aliceProject, alicePump, aliceTwin, shared, sharedPump));
		Hits hits = SearchFixture.asUser(alice, () -> _index.similar(alicePump, SearchRequest.of(null), alicesOwn));
		List<String> ids = SearchFixture.ids(hits);
		assertEquals(id(aliceTwin), ids.get(0));
		assertFalse(ids.contains(id(alicePump)));
		assertFalse(ids.contains(id(bobPump)));

		try {
			SearchFixture.asUser(alice, () -> _index.similar(bobPump, SearchRequest.of(null), alicesOwn));
			fail("An object the reader may not read cannot be the start of a search.");
		} catch (IllegalArgumentException ex) {
			// Expected: reported like an object that does not exist.
		}
	}

	/** Each hit lands on the reading list with the text shown; a second chunk of it is kept as well. */
	public void testHitsAreOnTheReadingList() {
		SearchFixture.Rights everything = new SearchFixture.Rights().read(all);
		ReadingList list = new ReadingList();
		SearchFixture.asUser(alice, () -> {
			try (ReadingListBinding.Binding binding = ReadingListBinding.bind(list)) {
				_index.search(SearchRequest.of("pressure relief valve").withMode(SearchMode.WORDS), everything);
				_index.search(SearchRequest.of("omega budget").withMode(SearchMode.WORDS), everything);
			}
			return null;
		});
		ReadSource source = list.find(id(alicePump));
		assertNotNull(source);
		assertTrue(source.text(), source.text().contains("The rho pump needs a pressure relief valve."));
		assertTrue(source.text(), source.text().contains("Omega budget of the rho pump."));
	}

	private static String id(TLObject object) {
		return ReadFilter.idOf(object);
	}

	/** The knowledge base with the test model and accounts. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestIndexSearchRights.class), ModelService.Module.INSTANCE));
	}

}
