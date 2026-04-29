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

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.vector.index.VectorIndex;
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
import com.top_logic.util.model.ModelService;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

/**
 * Integration test for semantic search utilizing the real Mistral model and JVector.
 */
public class VectorIndexIntegrationTest extends BasicTestCase {

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
    public void testSemanticSearchPizza() throws Exception {
        withTestObjects(objects -> {
             VectorIndex index = VectorIndex.getInstance();

            for (TLObject obj : objects.getObjects()) {
                if (obj.tType().getName().equals("Product")) {
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
                                if ("Margherita Pizza".equals(label)) {
                                    return "Margherita Pizza with classic tomato sauce, fresh mozzarella cheese, and basil leaves";
                                } else if ("Pepperoni Pizza".equals(label)) {
                                    return "Pepperoni Pizza with spicy salami slices, tomato sauce, and melted cheese";
                                } else if ("Hawaiian Pizza".equals(label)) {
                                    return "Hawaiian Pizza with ham, juicy pineapple chunks, and sweet tomato sauce";
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

            String query = "classic tomato and cheese italian pizza";
            List<EmbeddingMatch<TextSegment>> results = index.searchVectorIndex(query);

            assertNotNull("Results list should not be null", results);
            assertFalse("Results should not be empty. Results: " + results, results.isEmpty());

            boolean foundMargherita = false;
            for (EmbeddingMatch<TextSegment> r : results) {
                String pl = r.embedded().text();
                // Match the label or part of the JSON text
                if (pl != null && (pl.contains("Margherita") || pl.contains("tomato and cheese"))) {
                    foundMargherita = true;
                }
            }

            assertTrue("Should find Margherita Pizza in the semantic search results. Results: " + results,
                foundMargherita);
        });
    }

	/** */
    public void testSemanticSearchDelete() throws Exception {
        withTestObjects(objects -> {
            VectorIndex index = VectorIndex.getInstance();

            // 1. Manually add to vector index
            TLObject margheritaObj = null;
            for (TLObject obj : objects.getObjects()) {
                if (obj.tType().getName().equals("Product")) {
						KnowledgeObject ko = (KnowledgeObject) obj.tHandle();
                    if (ko != null) {
                        if ("Margherita Pizza".equals(LabelProviderService.getLabel(obj))) {
                            margheritaObj = obj;
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

            if (margheritaObj == null) {
				fail("Margherita Pizza object should have been created");
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
            String query = "classic tomato and cheese italian pizza";
            List<EmbeddingMatch<TextSegment>> resultsBefore = index.searchVectorIndex(query);

            assertNotNull("Results list should not be null", resultsBefore);
            assertFalse("Results should not be empty", resultsBefore.isEmpty());

            boolean foundMargheritaBefore = false;
            for (EmbeddingMatch<TextSegment> r : resultsBefore) {
                String pl = r.embedded().text();
                if (pl != null && pl.contains("Margherita")) {
                    foundMargheritaBefore = true;
                    break;
                }
            }
            assertTrue("Should find Margherita Pizza in the semantic search results before deletion",
                foundMargheritaBefore);

            // 3. Delete Action
			KnowledgeObject koToDelete = (KnowledgeObject) margheritaObj.tHandle();
            index.deleteContent(koToDelete.tId());

            try (Transaction tx = kb().beginTransaction(ResKey.text("Delete test object"))) {
                margheritaObj.tDelete();
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
            List<EmbeddingMatch<TextSegment>> resultsAfter = index.searchVectorIndex(query);

            boolean foundMargheritaAfter = false;
            for (EmbeddingMatch<TextSegment> r : resultsAfter) {
                String pl = r.embedded().text();
                if (pl != null && pl.contains("Margherita")) {
                    foundMargheritaAfter = true;
                    break;
                }
            }
            assertFalse("Should NOT find Margherita Pizza in the semantic search results after deletion",
                foundMargheritaAfter);
        });
    }

	/** */
    public void testSemanticSearchUpdate() throws Exception {
        withTestObjects(objects -> {
            VectorIndex index = VectorIndex.getInstance();

            // 1. Setup: Index a specific pizza
            TLObject margheritaObj = null;
            for (TLObject obj : objects.getObjects()) {
                if ("Margherita Pizza".equals(LabelProviderService.getLabel(obj))) {
                    margheritaObj = obj;
                    break;
                }
            }
            if (margheritaObj == null) {
				fail("Margherita Pizza should exist");
				return;
			}

			KnowledgeObject ko = (KnowledgeObject) margheritaObj.tHandle();
            index.addContent(new ContentObject() {
                @Override
                public KnowledgeObject getKnowledgeObject() {
                    return ko;
                }

                @Override
                public String getContent() throws IndexException {
                    return "Margherita Pizza with tomato and cheese";
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
            List<EmbeddingMatch<TextSegment>> initialResults = index.searchVectorIndex("margherita pizza");
            assertTrue("Should find original pizza",
                initialResults.stream().anyMatch(m -> m.embedded().text().contains("Margherita")));

            index.addContent(new ContentObject() {
                @Override
                public KnowledgeObject getKnowledgeObject() {
                    return ko;
                }

                @Override
                public String getContent() throws IndexException {
                    return "Truffle Mushroom Deluxe Pizza with premium mushrooms and white truffle oil";
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
            // Searching for "Truffle" should find it
            List<EmbeddingMatch<TextSegment>> resultsTruffle = index.searchVectorIndex("truffle mushroom pizza");
            assertTrue("Should find the updated content 'Truffle'. Results: " + resultsTruffle,
                resultsTruffle.stream().anyMatch(m -> m.embedded().text().contains("Truffle")));

            // Searching for "Margherita" should NOT find it in the core text content
            List<EmbeddingMatch<TextSegment>> resultsMargherita = index.searchVectorIndex("margherita pizza");
            boolean textStillFoundMargherita = false;
            for (EmbeddingMatch<TextSegment> r : resultsMargherita) {
                String fullJson = r.embedded().text();

                // Only consider it a failure if "Margherita" is in the actual indexed text, not the metadata name
                try {
                    org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                    org.json.simple.JSONObject jo = (org.json.simple.JSONObject) parser.parse(fullJson);
                    org.json.simple.JSONObject contentJo = (org.json.simple.JSONObject) jo.get("content");
                    if (contentJo != null) {
                        String text = (String) contentJo.get("_text");
                        if (text != null && text.contains("Margherita")) {
                            textStillFoundMargherita = true;
                        }
                    }
                } catch (Exception e) {
                    // Fallback to simple check if JSON parsing fails, but be aware of metadata
                    if (fullJson.contains("Margherita")) {
                        textStillFoundMargherita = true;
                    }
                }
            }

            assertFalse(
                "Should NOT find 'Margherita' in the text content anymore after update. Results: " + resultsMargherita,
                textStillFoundMargherita);
        });
    }

    private void withTestObjects(TestConsumer test) throws Exception {
        List<TLObject> createdObjects = new ArrayList<>();

        // 1. Transaction: Create ingredients and commit
        try (Transaction tx = kb().beginTransaction(ResKey.text("Create test ingredients"))) {
            TLObject ingredient1 = createObject(
                "new(`luigis.pizzashop:Ingredient`)" +
                    "..set(`luigis.pizzashop:Component#name`, 'Tomato Sauce')" +
                    "..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#ML`)" +
                    "..set(`luigis.pizzashop:Component#costPerBaseUnit`, 0.005)");
            createdObjects.add(ingredient1);

            TLObject ingredient2 = createObject(
                "new(`luigis.pizzashop:Ingredient`)" +
                    "..set(`luigis.pizzashop:Component#name`, 'Mozzarella Cheese')" +
                    "..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#G`)" +
                    "..set(`luigis.pizzashop:Component#costPerBaseUnit`, 0.012)");
            createdObjects.add(ingredient2);

            tx.commit();
        }

        // Second transaction: Create products that reference the ingredients
        try (Transaction tx = kb().beginTransaction(ResKey.text("Create test products"))) {
            // Product 1
            TLObject product1 = createObject(
                "{ " +
                    "  ingredient1 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Tomato Sauce').firstElement(); "
                    +
                    "  ingredient2 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Mozzarella Cheese').firstElement(); "
                    +
                    "  new(`luigis.pizzashop:Product`)" +
                    "    ..set(`luigis.pizzashop:NamedEntities#name`, 'Margherita Pizza')" +
                    "    ..set(`luigis.pizzashop:Product#plannedQuantity`, 100.0)" +
                    "    ..set(`luigis.pizzashop:Product#budget`, 500.0)" +
                    "    ..set(`luigis.pizzashop:Product#components`, list(" +
                    "      new(`luigis.pizzashop:ProductComponent`)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient1)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#amount`, 50.0)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#ML`), "
                    +
                    "      new(`luigis.pizzashop:ProductComponent`)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient2)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#amount`, 100.0)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#G`)));"
                    +
                    "}");
            createdObjects.add(product1);

            // Product 2
            TLObject product2 = createObject(
                "{ " +
                    "  ingredient2 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Mozzarella Cheese').firstElement(); "
                    +
                    "  new(`luigis.pizzashop:Product`)" +
                    "    ..set(`luigis.pizzashop:NamedEntities#name`, 'Pepperoni Pizza')" +
                    "    ..set(`luigis.pizzashop:Product#plannedQuantity`, 80.0)" +
                    "    ..set(`luigis.pizzashop:Product#budget`, 600.0)" +
                    "    ..set(`luigis.pizzashop:Product#components`, list(" +
                    "      new(`luigis.pizzashop:ProductComponent`)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient2)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#amount`, 150.0)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#G`)));"
                    +
                    "}");
            createdObjects.add(product2);

            // Product 3
            TLObject product3 = createObject(
                "{ " +
                    "  ingredient1 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Tomato Sauce').firstElement(); "
                    +
                    "  new(`luigis.pizzashop:Product`)" +
                    "    ..set(`luigis.pizzashop:NamedEntities#name`, 'Veggie Pizza')" +
                    "    ..set(`luigis.pizzashop:Product#plannedQuantity`, 60.0)" +
                    "    ..set(`luigis.pizzashop:Product#budget`, 450.0)" +
                    "    ..set(`luigis.pizzashop:Product#components`, list(" +
                    "      new(`luigis.pizzashop:ProductComponent`)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient1)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#amount`, 30.0)" +
                    "        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#ML`)));"
                    +
                    "}");
            createdObjects.add(product3);

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
        return suite(VectorIndexIntegrationTest.class);
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
