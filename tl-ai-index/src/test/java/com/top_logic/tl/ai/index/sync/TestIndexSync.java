/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.sync;

import static com.top_logic.tl.ai.index.config.IndexTestModel.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.AiIndex;
import com.top_logic.tl.ai.index.AiIndexConfig;
import com.top_logic.tl.ai.index.embed.HashingEmbedder;
import com.top_logic.tl.ai.index.lucene.Generations;
import com.top_logic.tl.ai.index.lucene.HybridSearch;
import com.top_logic.tl.ai.index.lucene.IndexFilter;
import com.top_logic.tl.ai.index.lucene.IndexFixture;
import com.top_logic.tl.ai.index.lucene.IndexGeneration;
import com.top_logic.tl.ai.index.lucene.Rrf;
import com.top_logic.tl.ai.index.lucene.StoredChunk;
import com.top_logic.util.model.ModelService;

/**
 * The index follows the knowledge base by its history: creates, updates, deletes and changes of a
 * link table reach it, a stop and a crash are caught up from the watermark, a lost history and a
 * changed configuration build a new generation - without embedding unchanged texts again - and the
 * worker holds one batch of objects however many there are.
 */
public class TestIndexSync extends BasicTestCase {

	private static final long WAIT = 60_000;

	private static final String CONFIG = """
		<index batch-size="16">
			<types>
		""" + STATEMENT_CONFIG + MATERIAL_CONFIG + """
			</types>
		</index>
		""";

	private Path _dir;

	private final List<AiIndex> _open = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = IndexFixture.tempDir("tl-ai-index-sync");
	}

	@Override
	protected void tearDown() throws Exception {
		for (AiIndex index : _open) {
			index.stop();
		}
		IndexFixture.deleteRecursively(_dir);
		super.tearDown();
	}

	/** A created object is found, an update replaces its chunks, a delete removes them. */
	public void testCreateUpdateDelete() throws IOException {
		AiIndex index = start(CONFIG, HashingEmbedder.create(32));
		TLObject statement = committed(() -> create(STATEMENT, Map.of("text", "Alpha pumps are loud.")));
		await(index);
		assertEquals(List.of("Alpha pumps are loud."), texts(index, statement));

		committed(() -> {
			update(statement, Map.of("text", "Beta valves leak."));
			return null;
		});
		await(index);
		assertEquals(List.of("Beta valves leak."), texts(index, statement));

		String id = ReadFilter.idOf(statement);
		delete(List.of(statement));
		await(index);
		assertTrue(chunks(index, id).isEmpty());
	}

	/** A change only of a link table - a multiple reference - re-indexes its owner's scopes. */
	public void testLinkChangeReindexesTheOwner() throws IOException {
		AiIndex index = start(CONFIG, HashingEmbedder.create(32));
		TLObject[] made = committed(() -> {
			TLObject first = create(PROJECT, Map.of("name", "First"));
			TLObject second = create(PROJECT, Map.of("name", "Second"));
			TLObject statement = create(STATEMENT, Map.of("text", "Gamma compressor.", "related", List.of(first)));
			return new TLObject[] { first, second, statement };
		});
		await(index);
		assertEquals(Set.of(ReadFilter.idOf(made[2])), found(index, "compressor", made[0]));

		committed(() -> {
			update(made[2], Map.of("related", List.of(made[1])));
			return null;
		});
		await(index);
		assertEquals(Set.of(), found(index, "compressor", made[0]));
		assertEquals(Set.of(ReadFilter.idOf(made[2])), found(index, "compressor", made[1]));
	}

	/**
	 * Changes made while the index was stopped are read from the history at the next start - only
	 * those, without a new build.
	 */
	public void testCatchUpAfterStop() throws IOException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		TLObject kept = committed(() -> create(STATEMENT, Map.of("text", "Delta first version.")));
		TLObject gone = committed(() -> create(STATEMENT, Map.of("text", "Epsilon to be deleted.")));
		await(first);
		int generation = generations();
		stop(first);

		TLObject added = committed(() -> create(STATEMENT, Map.of("text", "Zeta added while stopped.")));
		committed(() -> {
			update(kept, Map.of("text", "Delta second version."));
			return null;
		});
		String goneId = ReadFilter.idOf(gone);
		delete(List.of(gone));

		HashingEmbedder embedder = HashingEmbedder.create(32);
		AiIndex second = start(CONFIG, embedder);
		await(second);
		assertEquals(List.of("Zeta added while stopped."), texts(second, added));
		assertEquals(List.of("Delta second version."), texts(second, kept));
		assertTrue(chunks(second, goneId).isEmpty());
		assertEquals("The same generation catches up.", generation, generations());
		assertEquals("Only the two changed texts are embedded.", 2, embedder.texts());
	}

	/**
	 * The worker fails after writing chunks and before committing them, then the node stops: the next
	 * start indexes the object from the watermark.
	 */
	public void testCrashBeforeCommit() throws IOException, InterruptedException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		await(first);
		AtomicInteger crashes = new AtomicInteger();
		first.sync().setBeforeCommit(() -> {
			crashes.incrementAndGet();
			throw new IllegalStateException("Simulated crash before commit.");
		});
		TLObject lost = committed(() -> create(STATEMENT, Map.of("text", "Eta written but never committed.")));
		long deadline = System.currentTimeMillis() + WAIT;
		while (crashes.get() == 0 && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
		}
		assertTrue("The worker reached the commit.", crashes.get() > 0);
		assertTrue("Commits go on while the worker fails.",
			committed(() -> create(STATEMENT, Map.of("text", "Theta committed meanwhile."))) != null);
		stop(first);

		AiIndex second = start(CONFIG, HashingEmbedder.create(32));
		await(second);
		assertEquals(List.of("Eta written but never committed."), texts(second, lost));
	}

	/**
	 * A watermark the history no longer reaches - after a compaction of the history - builds a new
	 * generation, which then serves with every object.
	 */
	public void testLostHistoryBuildsAgain() throws IOException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		TLObject statement = committed(() -> create(STATEMENT, Map.of("text", "Iota before the gap.")));
		await(first);
		int generation = generations();
		stop(first);

		try (IndexGeneration gen = IndexGeneration.open(new Generations(_dir).path(generation))) {
			Map<String, String> data = new HashMap<>(gen.commitData());
			data.put(IndexGeneration.WATERMARK, "-5");
			gen.commit(data);
		}

		AiIndex second = start(CONFIG, HashingEmbedder.create(32));
		awaitSwitch(second);
		assertTrue("A new generation serves.", generations() > generation);
		assertEquals(List.of("Iota before the gap."), texts(second, statement));
	}

	/**
	 * A new keyword changes the fingerprint: a new generation is built and switched to, it reuses
	 * every vector of the old one, and it knows the keyword.
	 */
	public void testChangedConfigurationBuildsWithoutEmbedding() throws IOException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		TLObject risk = committed(() -> create(STATEMENT, Map.of("text", "Kappa risk text.", "kind", kind("RISK"))));
		await(first);
		int generation = generations();
		stop(first);

		String withSecondKeyword = CONFIG.replace("<keyword name=\"kind\"",
			"<keyword name=\"title\" script=\"s -> $s.get(`test.aiindex:Statement#title`)\" reads=\"title\"/>\n<keyword name=\"kind\"");
		HashingEmbedder embedder = HashingEmbedder.create(32);
		AiIndex second = start(withSecondKeyword, embedder);
		awaitSwitch(second);

		assertTrue("A new generation serves.", generations() > generation);
		assertFalse("The old generation is deleted.", new Generations(_dir).numbers().contains(generation));
		assertEquals("No chunk is embedded again.", 0, embedder.texts());
		assertEquals(List.of("Kappa risk text."), texts(second, risk));
		IndexFilter risks = new IndexFilter(Set.of(), null, Map.of("kind", Set.of("RISK")), Set.of());
		assertTrue(search(second, "Kappa", risks).contains(ReadFilter.idOf(risk)));
	}

	/**
	 * While a generation is built for another model, the serving one follows the commits with words
	 * only: the vectors of the new model - here of another length - never go into it.
	 */
	public void testModelChangeKeepsTheServingGenerationCurrent() throws IOException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		committed(() -> create(STATEMENT, Map.of("text", "Mu before the model change.")));
		await(first);
		stop(first);

		TLObject later = committed(() -> create(STATEMENT, Map.of("text", "Nu after the model change.")));
		AiIndex second = start(CONFIG, HashingEmbedder.create("other", 16));
		awaitSwitch(second);
		assertEquals(List.of("Nu after the model change."), texts(second, later));
		assertTrue(second.status().meaning());
	}

	/**
	 * An embedder that cannot be created keeps the index of its model: it serves and follows the
	 * commits with words only, and when the embedder is back only the chunks changed meanwhile are
	 * embedded.
	 */
	public void testUnavailableEmbedderKeepsTheIndex() throws IOException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		committed(() -> create(STATEMENT, Map.of("text", "Xi while the model loads.")));
		await(first);
		int generation = generations();
		stop(first);

		String withEmbedder = CONFIG.replace("<types>",
			"<embedder class=\"" + HashingEmbedder.class.getName() + "\" dimensions=\"32\"/>\n<types>");
		AiIndex withoutModel = start(withEmbedder, null);
		TLObject changed = committed(() -> create(STATEMENT, Map.of("text", "Omicron while the model is missing.")));
		await(withoutModel);
		assertFalse("Nothing is built.", withoutModel.sync().building());
		assertEquals("The index of the model serves.", generation, generations());
		assertEquals(List.of("Omicron while the model is missing."), texts(withoutModel, changed));
		stop(withoutModel);

		HashingEmbedder embedder = HashingEmbedder.create(32);
		AiIndex back = start(CONFIG, embedder);
		awaitSwitch(back);
		assertEquals("Only the chunk written without a vector is embedded.", 1, embedder.texts());
	}

	/**
	 * An object whose content cannot be computed does not stop the index: it is left out, the others
	 * are indexed.
	 */
	public void testFailingObjectDoesNotStopTheIndex() throws IOException {
		String poisoned = CONFIG.replace("script=\"s -> $s.get(`test.aiindex:Statement#kind`)\"",
			"script=\"s -> if($s.get(`test.aiindex:Statement#text`).stringContains('Poison'), throw('Poisoned'),"
				+ " $s.get(`test.aiindex:Statement#kind`))\"");
		assertFalse(CONFIG.equals(poisoned));
		AiIndex index = start(poisoned, HashingEmbedder.create(32));
		committed(() -> create(STATEMENT, Map.of("text", "Poison pill.")));
		TLObject good = committed(() -> create(STATEMENT, Map.of("text", "Pi is indexed.")));
		await(index);
		assertEquals(List.of("Pi is indexed."), texts(index, good));
	}

	/**
	 * A built generation switches although every commit of the worker is followed by another commit
	 * of the application.
	 */
	public void testSwitchUnderSteadyCommits() throws IOException {
		AiIndex first = start(CONFIG, HashingEmbedder.create(32));
		committed(() -> create(STATEMENT, Map.of("text", "Rho before the switch.")));
		await(first);
		stop(first);

		String withKeyword = CONFIG.replace("<keyword name=\"kind\"",
			"<keyword name=\"title\" script=\"s -> $s.get(`test.aiindex:Statement#title`)\" reads=\"title\"/>\n<keyword name=\"kind\"");
		AiIndex second = new AiIndex(config(withKeyword), HashingEmbedder.create(32), _dir,
			PersistencyLayer.getKnowledgeBase());
		AtomicInteger commits = new AtomicInteger();
		second.sync().setBeforeCommit(() -> {
			if (commits.incrementAndGet() > 200) {
				return;
			}
			Thread other = new Thread(() -> ThreadContextManager.inSystemInteraction(TestIndexSync.class,
				() -> committed(() -> create(STATEMENT, Map.of("text", "Sigma meanwhile.")))));
			other.start();
			try {
				other.join();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		});
		second.start();
		_open.add(second);
		long deadline = System.currentTimeMillis() + WAIT;
		while (second.sync().building() && commits.get() <= 200 && System.currentTimeMillis() < deadline) {
			second.awaitRevision(Long.MAX_VALUE, 100);
		}
		assertFalse("The new generation took over.", second.sync().building());
		assertTrue("It took over while the application committed.", commits.get() <= 200);
	}

	/**
	 * A build holds one batch of objects at a time: the same for 200 and for 2,000 objects.
	 */
	public void testBuildIsBounded() throws IOException {
		int small = largestBatchOfBuild(200);
		int large = largestBatchOfBuild(2_000);
		assertEquals(16, small);
		assertEquals("The batch does not grow with the data.", small, large);
	}

	private int largestBatchOfBuild(int objects) throws IOException {
		committed(() -> {
			for (int i = 0; i < objects; i++) {
				create(STATEMENT, Map.of("text", "Lambda statement number " + i + "."));
			}
			return null;
		});
		Path dir = IndexFixture.tempDir("tl-ai-index-bounded");
		try {
			AiIndex index = new AiIndex(config(CONFIG), HashingEmbedder.create(32), dir,
				PersistencyLayer.getKnowledgeBase());
			index.start();
			try {
				assertTrue(index.awaitRevision(lastRevision(), WAIT));
				return index.sync().largestBatch();
			} finally {
				index.stop();
			}
		} finally {
			IndexFixture.deleteRecursively(dir);
		}
	}

	private AiIndex start(String xml, HashingEmbedder embedder) throws IOException {
		AiIndex index = new AiIndex(config(xml), embedder, _dir, PersistencyLayer.getKnowledgeBase());
		index.start();
		_open.add(index);
		return index;
	}

	private void stop(AiIndex index) {
		index.stop();
		_open.remove(index);
	}

	private static AiIndexConfig config(String xml) {
		return read(AiIndexConfig.class, "index", xml);
	}

	private static void await(AiIndex index) {
		assertTrue("The index catches up.", index.awaitRevision(lastRevision(), WAIT));
	}

	private static void awaitSwitch(AiIndex index) {
		long deadline = System.currentTimeMillis() + WAIT;
		while (index.sync().building() && System.currentTimeMillis() < deadline) {
			index.awaitRevision(Long.MAX_VALUE, 100);
		}
		assertFalse("The new generation took over.", index.sync().building());
		await(index);
	}

	private static long lastRevision() {
		return PersistencyLayer.getKnowledgeBase().getHistoryManager().getLastRevision();
	}

	private int generations() {
		List<Integer> numbers = new Generations(_dir).numbers();
		return numbers.get(numbers.size() - 1);
	}

	private static List<StoredChunk> chunks(AiIndex index, String id) throws IOException {
		return index.sync().withServing(serving -> serving.index().chunksOf(id));
	}

	private static List<String> texts(AiIndex index, TLObject object) throws IOException {
		List<String> result = new ArrayList<>();
		for (StoredChunk chunk : chunks(index, ReadFilter.idOf(object))) {
			result.add(chunk.text());
		}
		return result;
	}

	private static Set<String> found(AiIndex index, String words, TLObject scope) throws IOException {
		return Set.copyOf(search(index, words, IndexFilter.NONE.withScopes(Set.of(ReadFilter.idOf(scope)))));
	}

	private static List<String> search(AiIndex index, String words, IndexFilter filter) throws IOException {
		return index.sync().withServing(serving -> serving.index().search(searcher -> {
			HybridSearch.Result result = HybridSearch.search(searcher, new HybridSearch.Request(
				HybridSearch.words(serving.index().analyzer(), words), null, filter, 50, 4, Rrf.DEFAULT, 0f));
			return result.hits().stream().map(HybridSearch.ObjectHit::object).toList();
		}));
	}

	/** The knowledge base with the test model. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestIndexSync.class), ModelService.Module.INSTANCE));
	}

}
