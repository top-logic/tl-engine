/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.vector.index.VectorIndex;
import com.top_logic.ai.vector.index.VectorReadFilter;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.junit.VectorIndexE2e;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

/**
 * Integration test for semantic search utilizing the real Mistral model and JVector.
 *
 * <p>
 * Behind {@link VectorIndexE2e}: it embeds with a real Mistral model, so a build without an
 * embedding key cannot run it and reports it as skipped instead.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class VectorIndexIntegrationTest extends BasicTestCase {

	/**
	 * Asks the gate before anything else, including the thread context {@link BasicTestCase} enters
	 * around a test.
	 *
	 * <p>
	 * With the gate off the suite below is the bare class, so the modules that context needs were
	 * never started - and the assumption has to be thrown before the attempt, or it never gets to be
	 * thrown at all.
	 * </p>
	 */
	@Override
	public void runBare() throws Throwable {
		VectorIndexE2e.assumeEnabled();
		super.runBare();
	}

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Ensure JVector index is completely clean before each test run
        File jvectorDir = new File("tmp/vector-store");
        if (jvectorDir.exists()) {
            for (File child : jvectorDir.listFiles()) {
                child.delete();
            }
            jvectorDir.delete();
        }
    }

	/** */
    public void testSemanticSearchEvent() throws Exception {
        withTestObjects(objects -> {
             VectorIndex index = VectorIndex.getInstance();

            for (TLObject obj : objects.getObjects()) {
                if (obj.tType().getName().equals("Event")) {
					KnowledgeObject ko = (KnowledgeObject) obj.tHandle();

                    if (ko != null) {
                        index.addContent(new ContentObject() {
                            @Override
                            public KnowledgeObject getKnowledgeObject() {
                                return ko;
                            }

                            @Override
                            public String getContent() throws IndexException {
                                String label = LabelProviderService.getLabel(obj);
                                if ("KI im Mittelstand".equals(label)) {
                                    return "KI im Mittelstand, a conference on AI agents and data models for medium-sized companies";
                                } else if ("Hamburg Data Summit".equals(label)) {
                                    return "Hamburg Data Summit, a conference on data platforms and data quality";
                                } else if ("Open Source Sommerfest".equals(label)) {
                                    return "Open Source Sommerfest, a summer party with lightning talks and a barbecue";
                                }
                                return label;
                            }

                            @Override
                            public String getDescription() throws IndexException {
                                return null;
                            }
                        });
                    }
                }
            }

            // Wait for background indexing thread to process
            for (int i = 0; i < 20; i++) {
                if (index.isIdle()) {
                    break;
                }
                Thread.sleep(1000);
            }

            String query = "artificial intelligence agents for small businesses";
            List<EmbeddingMatch<TextSegment>> results = search(index, query);

            assertNotNull("Results list should not be null", results);
            assertFalse("Results should not be empty. Results: " + results, results.isEmpty());

            boolean foundEvent = false;
            for (EmbeddingMatch<TextSegment> r : results) {
                String pl = r.embedded().text();
                // Match the label or part of the JSON text
                if (pl != null && (pl.contains("Mittelstand") || pl.contains("AI agents"))) {
                    foundEvent = true;
                }
            }

            assertTrue("Should find KI im Mittelstand in the semantic search results. Results: " + results,
                foundEvent);
        });
    }

	/** */
    public void testSemanticSearchDelete() throws Exception {
        withTestObjects(objects -> {
            VectorIndex index = VectorIndex.getInstance();

            // 1. Manually add to vector index
            TLObject eventObj = null;
            for (TLObject obj : objects.getObjects()) {
                if (obj.tType().getName().equals("Event")) {
						KnowledgeObject ko = (KnowledgeObject) obj.tHandle();
                    if (ko != null) {
                        if ("KI im Mittelstand".equals(LabelProviderService.getLabel(obj))) {
                            eventObj = obj;
                        }

                        index.addContent(new ContentObject() {
                            @Override
                            public KnowledgeObject getKnowledgeObject() {
                                return ko;
                            }

                            @Override
                            public String getContent() throws IndexException {
                                return LabelProviderService.getLabel(obj);
                            }

                            @Override
                            public String getDescription() throws IndexException {
                                return null;
                            }
                        });
                    }
                }
            }

            if (eventObj == null) {
				fail("KI im Mittelstand should have been created");
				return;
			}

            // Wait for background indexing thread to process
            for (int i = 0; i < 20; i++) {
                if (index.isIdle()) {
                    break;
                }
                Thread.sleep(1000);
            }

            // 2. Pre-condition Assert
            String query = "artificial intelligence agents for small businesses";
            List<EmbeddingMatch<TextSegment>> resultsBefore = search(index, query);

            assertNotNull("Results list should not be null", resultsBefore);
            assertFalse("Results should not be empty", resultsBefore.isEmpty());

            boolean foundEventBefore = false;
            for (EmbeddingMatch<TextSegment> r : resultsBefore) {
                String pl = r.embedded().text();
                if (pl != null && pl.contains("Mittelstand")) {
                    foundEventBefore = true;
                    break;
                }
            }
            assertTrue("Should find KI im Mittelstand in the semantic search results before deletion",
                foundEventBefore);

            // 3. Delete Action
			KnowledgeObject koToDelete = (KnowledgeObject) eventObj.tHandle();
            index.deleteContent(koToDelete.tId());

            try (Transaction tx = kb().beginTransaction(ResKey.text("Delete test object"))) {
                eventObj.tDelete();
                tx.commit();
            }

            // Wait for background deletion thread to process
            for (int i = 0; i < 20; i++) {
                if (index.isIdle()) {
                    break;
                }
                Thread.sleep(1000);
            }

            // 4. Post-condition Assert
            List<EmbeddingMatch<TextSegment>> resultsAfter = search(index, query);

            boolean foundEventAfter = false;
            for (EmbeddingMatch<TextSegment> r : resultsAfter) {
                String pl = r.embedded().text();
                if (pl != null && pl.contains("Mittelstand")) {
                    foundEventAfter = true;
                    break;
                }
            }
            assertFalse("Should NOT find KI im Mittelstand in the semantic search results after deletion",
                foundEventAfter);
        });
    }

	/** */
    public void testSemanticSearchUpdate() throws Exception {
        withTestObjects(objects -> {
            VectorIndex index = VectorIndex.getInstance();

            // 1. Setup: Index a specific event
            TLObject eventObj = null;
            for (TLObject obj : objects.getObjects()) {
                if ("KI im Mittelstand".equals(LabelProviderService.getLabel(obj))) {
                    eventObj = obj;
                    break;
                }
            }
            if (eventObj == null) {
				fail("KI im Mittelstand should exist");
				return;
			}

			KnowledgeObject ko = (KnowledgeObject) eventObj.tHandle();
            index.addContent(new ContentObject() {
                @Override
                public KnowledgeObject getKnowledgeObject() {
                    return ko;
                }

                @Override
                public String getContent() throws IndexException {
                    return "KI im Mittelstand, a conference on AI agents";
                }

                @Override
                public String getDescription() throws IndexException {
                    return null;
                }
            });

            // Wait for indexing
            for (int i = 0; i < 20; i++) {
                if (index.isIdle())
                    break;
                Thread.sleep(1000);
            }

            // 2. Verify it's found by original name
            List<EmbeddingMatch<TextSegment>> initialResults = search(index, "KI im Mittelstand conference");
            assertTrue("Should find the original event",
                initialResults.stream().anyMatch(m -> m.embedded().text().contains("Mittelstand")));

            index.addContent(new ContentObject() {
                @Override
                public KnowledgeObject getKnowledgeObject() {
                    return ko;
                }

                @Override
                public String getContent() throws IndexException {
                    return "Gardening Workshop about roses, compost and raised vegetable beds";
                }

                @Override
                public String getDescription() throws IndexException {
                    return null;
                }
            });

            // Wait for re-indexing (upsert)
            for (int i = 0; i < 20; i++) {
                if (index.isIdle())
                    break;
                Thread.sleep(1000);
            }

            // 4. Verify Post-Update:
            // Searching for "Gardening" should find it
            List<EmbeddingMatch<TextSegment>> resultsNew = search(index, "gardening workshop roses");
            assertTrue("Should find the updated content 'Gardening'. Results: " + resultsNew,
                resultsNew.stream().anyMatch(m -> m.embedded().text().contains("Gardening")));

            // Searching for "Mittelstand" should NOT find it in the core text content
            List<EmbeddingMatch<TextSegment>> resultsOld = search(index, "KI im Mittelstand conference");
            boolean textStillFoundOld = false;
            for (EmbeddingMatch<TextSegment> r : resultsOld) {
                String fullJson = r.embedded().text();

                // Only consider it a failure if "Mittelstand" is in the actual indexed text, not the metadata name
                try {
                    org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                    org.json.simple.JSONObject jo = (org.json.simple.JSONObject) parser.parse(fullJson);
                    org.json.simple.JSONObject contentJo = (org.json.simple.JSONObject) jo.get("content");
                    if (contentJo != null) {
                        String text = (String) contentJo.get("_text");
                        if (text != null && text.contains("Mittelstand")) {
                            textStillFoundOld = true;
                        }
                    }
                } catch (Exception e) {
                    // Fallback to simple check if JSON parsing fails, but be aware of metadata
                    if (fullJson.contains("Mittelstand")) {
                        textStillFoundOld = true;
                    }
                }
            }

            assertFalse(
                "Should NOT find 'Mittelstand' in the text content anymore after update. Results: " + resultsOld,
                textStillFoundOld);
        });
    }

    private void withTestObjects(TestConsumer test) throws Exception {
        List<TLObject> createdObjects = new ArrayList<>();

        // 1. Transaction: Create rooms and commit
        try (Transaction tx = kb().beginTransaction(ResKey.text("Create test rooms"))) {
            TLObject room1 = createObject(
                "new(`demo.events:Room`)" +
                    "..set(`demo.events:Named#name`, 'Saal Rhein')" +
                    "..set(`demo.events:Room#capacity`, 800)");
            createdObjects.add(room1);

            TLObject room2 = createObject(
                "new(`demo.events:Room`)" +
                    "..set(`demo.events:Named#name`, 'Raum Dom')" +
                    "..set(`demo.events:Room#capacity`, 120)");
            createdObjects.add(room2);

            tx.commit();
        }

        // Second transaction: Create events whose sessions reference the rooms
        try (Transaction tx = kb().beginTransaction(ResKey.text("Create test events"))) {
            // Event 1
            TLObject event1 = createObject(
                "{ " +
                    "  room = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Saal Rhein').firstElement(); " +
                    "  new(`demo.events:Event`)" +
                    "    ..set(`demo.events:Named#name`, 'KI im Mittelstand')" +
                    "    ..set(`demo.events:Event#status`, `demo.events:EventStatus#OPEN_FOR_REGISTRATION`)" +
                    "    ..set(`demo.events:Event#startDate`, today())" +
                    "    ..set(`demo.events:Event#maxParticipants`, 100)" +
                    "    ..set(`demo.events:Event#ticketPrice`, 500.0)" +
                    "    ..set(`demo.events:Event#sessions`, list(" +
                    "      new(`demo.events:Session`)" +
                    "        ..set(`demo.events:Named#name`, 'Agenten statt Assistenten')" +
                    "        ..set(`demo.events:Session#room`, $room)));" +
                    "}");
            createdObjects.add(event1);

            // Event 2
            TLObject event2 = createObject(
                "{ " +
                    "  room = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Raum Dom').firstElement(); " +
                    "  new(`demo.events:Event`)" +
                    "    ..set(`demo.events:Named#name`, 'Hamburg Data Summit')" +
                    "    ..set(`demo.events:Event#status`, `demo.events:EventStatus#PLANNED`)" +
                    "    ..set(`demo.events:Event#startDate`, today())" +
                    "    ..set(`demo.events:Event#maxParticipants`, 80)" +
                    "    ..set(`demo.events:Event#ticketPrice`, 600.0)" +
                    "    ..set(`demo.events:Event#sessions`, list(" +
                    "      new(`demo.events:Session`)" +
                    "        ..set(`demo.events:Named#name`, 'Datenqualität in der Praxis')" +
                    "        ..set(`demo.events:Session#room`, $room)));" +
                    "}");
            createdObjects.add(event2);

            // Event 3
            TLObject event3 = createObject(
                "{ " +
                    "  room = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Saal Rhein').firstElement(); " +
                    "  new(`demo.events:Event`)" +
                    "    ..set(`demo.events:Named#name`, 'Open Source Sommerfest')" +
                    "    ..set(`demo.events:Event#status`, `demo.events:EventStatus#COMPLETED`)" +
                    "    ..set(`demo.events:Event#startDate`, today())" +
                    "    ..set(`demo.events:Event#maxParticipants`, 60)" +
                    "    ..set(`demo.events:Event#ticketPrice`, 450.0)" +
                    "    ..set(`demo.events:Event#sessions`, list(" +
                    "      new(`demo.events:Session`)" +
                    "        ..set(`demo.events:Named#name`, 'Lightning Talks')" +
                    "        ..set(`demo.events:Session#room`, $room)));" +
                    "}");
            createdObjects.add(event3);

            tx.commit();
        }

        try {
            TestObjects objects = new TestObjects(createdObjects);
            test.accept(objects);
            // Roll back any implicitly started transactions
			try (Transaction tx = kb().beginTransaction(ResKey.text("Roll back implicit changes"))) {
				tx.rollback(ResKey.text("Roll back implicit changes"));
			}
        } finally {
            // Clean up created objects
            try (Transaction tx = kb().beginTransaction(ResKey.text("Clean up test objects"))) {
                for (TLObject obj : createdObjects) {
                    if (obj.tValid()) {
                        try {
							KnowledgeObject ko = (KnowledgeObject) obj.tHandle();
                            if (ko != null) {
                                VectorIndex.getInstance().deleteContent(ko.tId());
                            }
                        } catch (Exception e) {
                            // Ignored
                        }
                        obj.tDelete();
                    }
                }
                tx.commit();
            }
            // Wait for VectorIndex to process deletions
            try {
                for (int i = 0; i < 20; i++) {
                    if (VectorIndex.getInstance().isIdle()) {
                        break;
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    private TLObject createObject(String script) throws ParseException {
        SearchExpression expr = search(script);
        Object result = execute(expr);
        return (TLObject) result;
    }

    private static SearchExpression search(String expr) throws ParseException {
        SearchExpressionParser parser = new SearchExpressionParser(new StringReader(expr));
        Expr result = parser.expr();
        return SearchBuilder.toSearchExpression(model(), result);
    }

    private static Object execute(SearchExpression search, Object... args) {
        QueryExecutor executor = compile(kb(), model(), search);
        return executor.executeWith(null, null, Args.some(args));
    }

    private static KnowledgeBase kb() {
        return PersistencyLayer.getKnowledgeBase();
    }

    private static TLModel model() {
        return ModelService.getApplicationModel();
    }

    @FunctionalInterface
    private interface TestConsumer {
        void accept(TestObjects objects) throws Exception;
    }

    private static class TestObjects {
        private final List<TLObject> objects;

        TestObjects(List<TLObject> objects) {
            this.objects = objects;
        }

        public List<TLObject> getObjects() {
            return objects;
        }
    }

	/** */
    public static Test suite() {
		if (!VectorIndexE2e.enabled()) {
			// The setup below boots the embedding service, which is the very thing this build has
			// no key for. Handing back the bare class lets every method reach its assumption and be
			// reported as skipped, instead of the suite failing before any test starts.
			return new TestSuite(VectorIndexIntegrationTest.class);
		}
        return suite(VectorIndexIntegrationTest.class);
    }

	/**
	 * The store's hits for the given query, as the caller sees them.
	 *
	 * <p>
	 * {@link ModelSecurity#UNCHECKED}, because what is under test here is indexing and retrieval, not
	 * who may see what - that is {@code SecDemoVectorIndexTest}. There is no unfiltered search to call
	 * instead: since issue #295 a search names the principal it serves or does not happen.
	 * </p>
	 */
	private static List<EmbeddingMatch<TextSegment>> search(VectorIndex index, String query) {
		VectorReadFilter.Hits hits =
			index.searchVectorIndex(query, ReadFilter.forSecurity(ModelSecurity.UNCHECKED));
		List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>(hits.hits().size());
		for (VectorReadFilter.Hit hit : hits.hits()) {
			matches.add(hit.match());
		}
		return matches;
	}

	/** */
    protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {

        BasicRuntimeModule<?>[] modules = getModules(additionalModules);

        return KBSetup.getSingleKBTest(
            ServiceTestSetup.createSetup(
                new TestSuite(test),
                modules));
    }

    private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
        ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

        modules.add(SearchBuilder.Module.INSTANCE);
        modules.add(ModelService.Module.INSTANCE);
        modules.add(LabelProviderService.Module.INSTANCE);
        modules.add(com.top_logic.ai.vector.index.VectorIndex.Module.INSTANCE);
        modules.add(com.top_logic.ai.vector.index.VectorIndexingService.Module.INSTANCE);

        modules.addAll(Arrays.asList(additionalModules));

        return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
    }
}
