/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import static com.top_logic.tl.ai.index.config.IndexTestModel.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.AiIndex;
import com.top_logic.tl.ai.index.lucene.IndexFixture;
import com.top_logic.util.model.ModelService;

/**
 * A search is bounded by its page, not by the data: the work it does is the same for 200 and for
 * 2,000 objects the reader may not read.
 *
 * <p>
 * Scope A holds N objects that contain the query word, scope B holds 15. The reader may read only
 * scope B. The proof is an invariant over two sizes of A: the number of objects the search resolves
 * and the number of chunks it reads do not change when A grows tenfold.
 * </p>
 */
public class TestBoundedSearch extends BasicTestCase {

	private static final String SCOPED = """
		<index readable-scopes="all(`test.aiindex:Project`)">
			<types>
		""" + STATEMENT_CONFIG + """
			</types>
		</index>
		""";

	private static final String UNSCOPED = """
		<index max-candidates="60">
			<types>
				<type name="test.aiindex:Statement">
					<text attribute="text"/>
				</type>
			</types>
		</index>
		""";

	private static Person reader;

	/** What one search did. */
	private record Work(List<String> hits, boolean more, int resolved, int chunksRead) {
		// A plain carrier.
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (reader == null) {
			reader = TestPerson.createPerson("bounded-reader");
		}
	}

	/**
	 * With the scope inside the query: a full page from B, at most a page of objects resolved, and
	 * the same work for both sizes of A.
	 */
	public void testScopedSearchIsBounded() throws IOException {
		Data small = data(200);
		Work smallWork = search(SCOPED, small);
		Data large = data(2_000);
		Work largeWork = search(SCOPED, large);

		assertEquals(10, smallWork.hits().size());
		assertTrue("Every hit is from scope B.", large.b().containsAll(largeWork.hits()));
		assertTrue(small.b().containsAll(smallWork.hits()));
		assertEquals(10, smallWork.resolved());
		assertEquals("Objects resolved do not grow with the data.", smallWork.resolved(), largeWork.resolved());
		assertEquals("Chunks read do not grow with the data.", smallWork.chunksRead(), largeWork.chunksRead());
		assertTrue("B has more than a page.", smallWork.more());
	}

	/**
	 * Without a scope rule the search checks hit by hit: it stays at or below its cap of candidates
	 * for both sizes, shows only what the reader may read, and says that there may be more.
	 */
	public void testUnscopedSearchStaysBelowItsCap() throws IOException {
		Data small = data(200);
		Work smallWork = search(UNSCOPED, small);
		Data large = data(2_000);
		Work largeWork = search(UNSCOPED, large);

		for (Work work : List.of(smallWork, largeWork)) {
			assertTrue("Resolved " + work.resolved(), work.resolved() <= 60);
			assertTrue(work.more());
		}
		assertTrue(small.b().containsAll(smallWork.hits()));
		assertTrue(large.b().containsAll(largeWork.hits()));
	}

	/** The objects of one size: the word, scope A's objects and scope B's object ids. */
	private record Data(String word, List<TLObject> readable, List<String> b) {
		// A plain carrier.
	}

	private static int runs;

	private Data data(int n) {
		String word = "omicron" + (++runs) + "x" + n;
		return committed(() -> {
			TLObject a = create(PROJECT, Map.of("name", "A" + n));
			TLObject b = create(PROJECT, Map.of("name", "B" + n));
			for (int i = 0; i < n; i++) {
				create(STATEMENT, Map.of("text", "The " + word + " bearing note " + i + ".", "project", a));
			}
			List<TLObject> readable = new ArrayList<>(List.of(b));
			List<String> ids = new ArrayList<>();
			for (int i = 0; i < 15; i++) {
				TLObject statement = create(STATEMENT, Map.of("text", "A " + word + " bearing remark " + i + ".",
					"project", b));
				readable.add(statement);
				ids.add(ReadFilter.idOf(statement));
			}
			return new Data(word, readable, ids);
		});
	}

	private Work search(String config, Data data) throws IOException {
		Path dir = IndexFixture.tempDir("tl-ai-index-bounded-search");
		AiIndex index = SearchFixture.start(config, dir);
		try {
			AtomicInteger resolved = new AtomicInteger();
			index.searcher().setResolver((id, security) -> {
				resolved.incrementAndGet();
				return IndexSearch.CHECKED.resolve(id, security);
			});
			SearchFixture.Rights rights = new SearchFixture.Rights().read(data.readable());
			Hits hits = SearchFixture.asUser(reader,
				() -> index.search(SearchRequest.of(data.word()).withTypes(Set.of(STATEMENT)), rights));
			return new Work(SearchFixture.ids(hits), hits.moreAvailable(), resolved.get(),
				index.searcher().lastChunksRead());
		} finally {
			index.stop();
			IndexFixture.deleteRecursively(dir);
		}
	}

	/** The knowledge base with the test model and accounts. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestBoundedSearch.class), ModelService.Module.INSTANCE));
	}

}
