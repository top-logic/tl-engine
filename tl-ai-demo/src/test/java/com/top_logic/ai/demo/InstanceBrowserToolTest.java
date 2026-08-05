/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
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
import com.top_logic.tl.ai.core.tools.model.InstanceBrowserTool;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link InstanceBrowserTool}.
 */
public class InstanceBrowserToolTest extends BasicTestCase {

	private static final String PIZZASHOP_MODULE = "luigis.pizzashop";
	private static final String PRODUCT_CLASS = "Product";
	private static final String INGREDIENT_CLASS = "Ingredient";

	private Person _previousUser;

	/**
	 * Runs each test as the root account in super-user mode.
	 *
	 * <p>
	 * Since engine {@code 8.0.0-alpha7}, TL-Script applies the model access rights of
	 * {@code TLContext.currentUser()}, which deny by default. The fixtures of these tests are
	 * created with TL-Script {@code new(...)}, so without a bypass they cannot be created at all.
	 * What these tests are about is instance listing and pagination, not authorization.
	 * </p>
	 *
	 * @see ScriptToolFactoryTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext context = TLContext.getContext();
		_previousUser = context.getPerson();
		context.setCurrentPerson(PersonManager.getManager().getRoot());
		ThreadContext.pushSuperUser();
	}

	@Override
	protected void tearDown() throws Exception {
		ThreadContext.popSuperUser();
		TLContext.getContext().setCurrentPerson(_previousUser);
		super.tearDown();
	}

	/**
	 * Tests listing instances of a type without pagination.
	 */
	public void testListInstancesBasic() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, null, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain module", result.contains("\"module\""));
			assertTrue("Result should contain module name", result.contains(PIZZASHOP_MODULE));
			assertTrue("Result should contain type", result.contains("\"type\""));
			assertTrue("Result should contain type name", result.contains(PRODUCT_CLASS));
			assertTrue("Result should contain totalCount", result.contains("\"totalCount\""));
			assertTrue("Result should contain instances array", result.contains("\"instances\""));
			assertTrue("Result should contain limit", result.contains("\"limit\""));
			assertTrue("Result should contain offset", result.contains("\"offset\""));
			assertTrue("Result should contain hasMore", result.contains("\"hasMore\""));

			// Should have created 3 products
			assertTrue("Should contain 3 products", result.contains("\"totalCount\":3"));
		});
	}

	/**
	 * Tests listing instances with custom limit.
	 */
	public void testListInstancesWithLimit() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, 2, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain limit", result.contains("\"limit\":2"));
			// With 3 total products and limit 2, there should be more
			assertTrue("Should indicate more results available", result.contains("\"hasMore\":true"));
			assertTrue("Should contain nextCursor", result.contains("\"nextCursor\""));
		});
	}

	/**
	 * Tests pagination using cursor.
	 */
	public void testListInstancesWithPagination() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();

			// Get first page with limit 2
			String firstPage = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, 2, null);
			assertNotNull("First page should not be null", firstPage);

			// Extract next cursor (simple string matching since we don't want to parse JSON)
			int cursorStart = firstPage.indexOf("\"nextCursor\":\"") + 14;
			int cursorEnd = firstPage.indexOf("\"", cursorStart);
			String cursor = firstPage.substring(cursorStart, cursorEnd);

			// Get second page using cursor
			String secondPage = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, 2, cursor);
			assertNotNull("Second page should not be null", secondPage);
			assertTrue("Second page should have offset 2", secondPage.contains("\"offset\":2"));
			// With 3 total, offset 2 should have no more results
			assertTrue("Should not have more results", secondPage.contains("\"hasMore\":false"));
		});
	}

	/**
	 * Tests that limit is clamped to maximum allowed value.
	 */
	public void testListInstancesLimitClamping() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, 10000, null);

			assertNotNull("Result should not be null", result);
			// Limit should be clamped to 1000 (MAX_LIMIT)
			assertTrue("Limit should be clamped to 1000", result.contains("\"limit\":1000"));
		});
	}

	/**
	 * Tests getting a specific instance by TID.
	 */
	public void testGetInstanceBasic() throws Exception {
		withTestObjects(objects -> {
			// Get the TID of the first product
			TLObject product = objects.getProduct(0);
			assertNotNull("Product1 should exist", product);

			String tid = product.tId().asString();

			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(tid);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain tid", result.contains("\"tid\""));
			assertTrue("Result should contain type info", result.contains("\"type\""));
			assertTrue("Result should contain attributes", result.contains("\"attributes\""));
			assertTrue("Result should contain metadata", result.contains("\"metadata\""));

			// Should contain Product type information
			assertTrue("Should contain Product type", result.contains(PRODUCT_CLASS));
			assertTrue("Should contain module", result.contains(PIZZASHOP_MODULE));

			// Should contain attribute values
			assertTrue("Should contain name attribute", result.contains("name"));
			assertTrue("Should contain plannedQuantity attribute", result.contains("plannedQuantity"));
		});
	}

	/**
	 * Tests that instance attributes include their values.
	 */
	public void testGetInstanceAttributeValues() throws Exception {
		withTestObjects(objects -> {
			TLObject product = objects.getProduct(0);
			String tid = product.tId().asString();

			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(tid);

			assertNotNull("Result should not be null", result);
			// Product1 has name="Margherita Pizza"
			assertTrue("Should contain product name", result.contains("Margherita"));
			// Product1 has planned quantity=100.0
			assertTrue("Should contain planned quantity", result.contains("100"));
		});
	}

	/**
	 * Tests that instance metadata includes container information.
	 */
	public void testGetInstanceMetadata() throws Exception {
		withTestObjects(objects -> {
			TLObject product = objects.getProduct(0);
			String tid = product.tId().asString();

			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(tid);

			assertNotNull("Result should not be null", result);
			assertTrue("Should contain metadata", result.contains("\"metadata\""));
			assertTrue("Should contain transient flag", result.contains("\"transient\""));
		});
	}

	/**
	 * Tests error case: null module name.
	 */
	public void testListInstancesNullModule() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(null, PRODUCT_CLASS, null, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention module required",
				result.contains("required") || result.contains("Module name"));
		});
	}

	/**
	 * Tests error case: empty module name.
	 */
	public void testListInstancesEmptyModule() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances("", PRODUCT_CLASS, null, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention module required",
				result.contains("required") || result.contains("Module name"));
		});
	}

	/**
	 * Tests error case: null type name.
	 */
	public void testListInstancesNullType() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, null, null, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention type required",
				result.contains("required") || result.contains("Type name"));
		});
	}

	/**
	 * Tests error case: empty type name.
	 */
	public void testListInstancesEmptyType() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, "", null, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention type required",
				result.contains("required") || result.contains("Type name"));
		});
	}

	/**
	 * Tests error case: type not found.
	 */
	public void testListInstancesTypeNotFound() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, "NonExistentType", null, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention type not found",
				result.contains("not found") || result.contains("NonExistentType"));
		});
	}

	/**
	 * Tests error case: invalid limit (negative).
	 */
	public void testListInstancesInvalidLimitNegative() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, -1, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention limit must be positive",
				result.contains("positive") || result.contains("Limit"));
		});
	}

	/**
	 * Tests error case: invalid limit (zero).
	 */
	public void testListInstancesInvalidLimitZero() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, 0, null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention limit must be positive",
				result.contains("positive") || result.contains("Limit"));
		});
	}

	/**
	 * Tests error case: null TID.
	 */
	public void testGetInstanceNullTid() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(null);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention TID required",
				result.contains("required") || result.contains("TID"));
		});
	}

	/**
	 * Tests error case: empty TID.
	 */
	public void testGetInstanceEmptyTid() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance("");

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention TID required",
				result.contains("required") || result.contains("TID"));
		});
	}

	/**
	 * Tests error case: invalid TID format (missing colon).
	 */
	public void testGetInstanceInvalidTidFormat() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance("InvalidTid");

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention invalid format",
				result.contains("Invalid") || result.contains("format"));
		});
	}

	/**
	 * Tests error case: TID not found.
	 */
	public void testGetInstanceTidNotFound() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance("Product:99999");

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
		});
	}

	/**
	 * Tests listing instances of different types.
	 */
	public void testListInstancesMultipleTypes() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();

			// List products
			String productsResult = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, null, null);
			assertNotNull("Products result should not be null", productsResult);
			assertTrue("Should have products", productsResult.contains("\"totalCount\":3"));

			// List ingredients
			String ingredientsResult = tool.listInstances(PIZZASHOP_MODULE, INGREDIENT_CLASS, null, null);
			assertNotNull("Ingredients result should not be null", ingredientsResult);
			assertTrue("Should have ingredients", ingredientsResult.contains("\"totalCount\":2"));
		});
	}

	/**
	 * Tests that empty cursor is treated as offset 0.
	 */
	public void testListInstancesEmptyCursor() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(PIZZASHOP_MODULE, PRODUCT_CLASS, null, "");

			assertNotNull("Result should not be null", result);
			assertTrue("Empty cursor should start at offset 0", result.contains("\"offset\":0"));
		});
	}

	/**
	 * Helper method to execute a test with test objects created via TLScript.
	 */
	private void withTestObjects(TestConsumer test) throws Exception {
		List<TLObject> createdObjects = new ArrayList<>();

		// First transaction: Create ingredients and commit
		try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
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
		try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
			// Product 1 with 2 components
			TLObject product1 = createObject(
				"{ " +
				"  ingredient1 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Tomato Sauce').firstElement(); " +
				"  ingredient2 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Mozzarella Cheese').firstElement(); " +
				"  new(`luigis.pizzashop:Product`)" +
				"    ..set(`luigis.pizzashop:NamedEntities#name`, 'Margherita Pizza')" +
				"    ..set(`luigis.pizzashop:Product#plannedQuantity`, 100.0)" +
				"    ..set(`luigis.pizzashop:Product#budget`, 500.0)" +
				"    ..set(`luigis.pizzashop:Product#components`, list(" +
				"      new(`luigis.pizzashop:ProductComponent`)" +
				"        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient1)" +
				"        ..set(`luigis.pizzashop:ProductComponent#amount`, 50.0)" +
				"        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#ML`), " +
				"      new(`luigis.pizzashop:ProductComponent`)" +
				"        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient2)" +
				"        ..set(`luigis.pizzashop:ProductComponent#amount`, 100.0)" +
				"        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#G`)));" +
				"}");
			createdObjects.add(product1);

			// Product 2 with 1 component
			TLObject product2 = createObject(
				"{ " +
				"  ingredient2 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Mozzarella Cheese').firstElement(); " +
				"  new(`luigis.pizzashop:Product`)" +
				"    ..set(`luigis.pizzashop:NamedEntities#name`, 'Pepperoni Pizza')" +
				"    ..set(`luigis.pizzashop:Product#plannedQuantity`, 80.0)" +
				"    ..set(`luigis.pizzashop:Product#budget`, 600.0)" +
				"    ..set(`luigis.pizzashop:Product#components`, list(" +
				"      new(`luigis.pizzashop:ProductComponent`)" +
				"        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient2)" +
				"        ..set(`luigis.pizzashop:ProductComponent#amount`, 150.0)" +
				"        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#G`)));" +
				"}");
			createdObjects.add(product2);

			// Product 3 with 1 component
			TLObject product3 = createObject(
				"{ " +
				"  ingredient1 = all(`luigis.pizzashop:Ingredient`).filter(i -> $i.get(`luigis.pizzashop:Component#name`) == 'Tomato Sauce').firstElement(); " +
				"  new(`luigis.pizzashop:Product`)" +
				"    ..set(`luigis.pizzashop:NamedEntities#name`, 'Veggie Pizza')" +
				"    ..set(`luigis.pizzashop:Product#plannedQuantity`, 60.0)" +
				"    ..set(`luigis.pizzashop:Product#budget`, 450.0)" +
				"    ..set(`luigis.pizzashop:Product#components`, list(" +
				"      new(`luigis.pizzashop:ProductComponent`)" +
				"        ..set(`luigis.pizzashop:ProductComponent#component`, $ingredient1)" +
				"        ..set(`luigis.pizzashop:ProductComponent#amount`, 30.0)" +
				"        ..set(`luigis.pizzashop:ProductComponent#unit`, `luigis.pizzashop:UnitOfMeasure#ML`)));" +
				"}");
			createdObjects.add(product3);

			tx.commit();
		}

		try {
			TestObjects objects = new TestObjects(createdObjects);
			try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
				test.accept(objects);
				// Roll back any modifications done by the test callback.
				tx.rollback(ResKey.NONE, null);
			}
		} finally {
			// Clean up created objects
			try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
				for (TLObject obj : createdObjects) {
					if (obj.tValid()) {
						obj.tDelete();
					}
				}
				tx.commit();
			}
		}
	}

	/**
	 * Creates an object using TLScript.
	 */
	private TLObject createObject(String script) throws ParseException {
		SearchExpression expr = search(script);
		Object result = execute(expr);
		return (TLObject) result;
	}

	/**
	 * Parses and builds a TLScript search expression.
	 */
	private static SearchExpression search(String expr) throws ParseException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(expr));
		Expr result = parser.expr();
		return SearchBuilder.toSearchExpression(model(), result);
	}

	/**
	 * Executes a search expression.
	 */
	private static Object execute(SearchExpression search, Object... args) {
		QueryExecutor executor = compile(kb(), model(), search);
		return executor.executeWith(null, null, Args.some(args));
	}

	/**
	 * Gets the knowledge base.
	 */
	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Gets the application model.
	 */
	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	/**
	 * Functional interface for test consumers that can throw exceptions.
	 */
	@FunctionalInterface
	private interface TestConsumer {
		void accept(TestObjects objects) throws Exception;
	}

	/**
	 * Container for test objects.
	 */
	private static class TestObjects {
		private final List<TLObject> objects;

		TestObjects(List<TLObject> objects) {
			this.objects = objects;
		}

		TLObject getProduct(int index) {
			// Products are at indices 2, 3, 4 (after 2 ingredients)
			return objects.get(index + 2);
		}
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(InstanceBrowserToolTest.class);
	}

	/**	*/
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
		// Needed to run the tests as the root account, see setUp().
		modules.add(PersonManager.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
