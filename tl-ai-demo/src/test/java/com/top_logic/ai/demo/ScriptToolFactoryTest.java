/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.Config;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScriptToolFactory}.
 */
public class ScriptToolFactoryTest extends BasicTestCase {

	private Person _previousUser;

	/**
	 * Runs each test as the root account in super-user mode.
	 *
	 * <p>
	 * Since engine {@code 8.0.0-alpha7}, TL-Script applies the model access rights of
	 * {@code TLContext.currentUser()}: {@code new(...)} checks the CREATE right, and attribute reads
	 * are filtered. Those rights deny by default and this test app configures no
	 * {@code <security-config>}, so without a bypass the fixtures cannot be created and every
	 * attribute read comes back empty. Both parts are needed - the engine denies a
	 * <code>null</code> user outright, before it ever looks at super-user mode. What these tests are
	 * about is TID-to-wrapper parameter resolution, not authorization.
	 * </p>
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
	 * Regression for the $parameter-not-resolved bug: a tool whose body is simply {@code $message}
	 * must return the value passed for {@code message}.
	 */
	public void testNameBindingResolvesParameter() {
		Tool tool = singleTool(scriptConfig("echo",
			ToolMode.READ,
			"$message",
			param("message", "tl.core:String", /*mandatory*/ true)));

		ToolResult result = tool.execute(Map.of("message", "hello"), ToolContext.EMPTY);

		assertEquals("hello", MarkedResults.disclosed(result.text()));
	}

	/**
	 * When a parameter is optional and omitted from the call, its configured default expression
	 * must be evaluated and the result used as the binding.
	 */
	public void testDefaultValueAppliedWhenMissing() {
		ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
		p.setName("greeting");
		p.setType(TLModelPartRef.ref("tl.core:String"));
		p.setMandatory(false);
		p.setDefaultValue(parseExpr("\"hi\""));

		Tool tool = singleTool(scriptConfig("greet", ToolMode.READ, "$greeting + \"!\"", p));
		ToolResult result = tool.execute(Map.of(), ToolContext.EMPTY);

		assertEquals("hi!", MarkedResults.disclosed(result.text()));
	}

	/**
	 * When a mandatory parameter is omitted, the executor must return an error result whose text
	 * contains the canonical missing-parameter message.
	 */
	public void testMandatoryMissingFailsCleanly() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message",
			param("message", "tl.core:String", /*mandatory*/ true)));

		ToolResult result = tool.execute(Map.of(), ToolContext.EMPTY);

		// ScriptToolExecutor catches exceptions and returns them as the result text.
		assertTrue("Expected missing-parameter error, got: " + result.text(),
			result.text().contains("Missing required parameter 'message'"));
	}

	/**
	 * A parameter declared as multiple must receive a {@link List} from the caller; the body can
	 * call {@code .size()} on it directly.
	 */
	public void testMultipleParameterReceivesList() {
		ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
		p.setName("items");
		p.setType(TLModelPartRef.ref("tl.core:String"));
		p.setMultiple(true);
		p.setMandatory(true);

		Tool tool = singleTool(scriptConfig("count", ToolMode.READ, "$items.size()", p));
		ToolResult result = tool.execute(Map.of("items", List.of("a", "b", "c")), ToolContext.EMPTY);

		assertEquals("3", MarkedResults.disclosed(result.text()));
	}

	/**
	 * A parameter typed as a {@link com.top_logic.model.TLClass} must accept a TID string and
	 * resolve it to the corresponding {@link TLObject} wrapper before the script body executes.
	 * The body can then navigate the wrapper using {@code .get(`Type#attr`)} TL-Script syntax.
	 *
	 * <p>
	 * Uses {@code luigis.pizzashop:Ingredient} (concrete class with a {@code name} attribute
	 * inherited from {@code luigis.pizzashop:NamedEntities}) because it is already part of the
	 * tl-ai-demo model and is exercised by {@link InstanceBrowserToolTest}.
	 * </p>
	 */
	public void testTLClassParameterResolvesTid() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		String tid;
		TLObject ingredient;
		try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
			ingredient = createObject(
				"new(`luigis.pizzashop:Ingredient`)" +
				"..set(`luigis.pizzashop:Component#name`, 'fixture-ingredient')" +
				"..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#G`)" +
				"..set(`luigis.pizzashop:Component#costPerBaseUnit`, 1.0)");
			tx.commit();
			tid = ingredient.tId().asString();
		}

		try {
			ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
			p.setName("target");
			p.setType(TLModelPartRef.ref("luigis.pizzashop:Ingredient"));
			p.setMandatory(true);

			Tool tool = singleTool(scriptConfig("readName", ToolMode.READ,
				"$target.get(`luigis.pizzashop:NamedEntities#name`)", p));

			ToolResult result = tool.execute(Map.of("target", tid), ToolContext.EMPTY);

			assertEquals("fixture-ingredient", MarkedResults.disclosed(result.text()));
		} finally {
			try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
				if (ingredient.tValid()) {
					ingredient.tDelete();
				}
				tx.commit();
			}
		}
	}

	/**
	 * A parameter typed as a {@link com.top_logic.model.TLClass} with {@code multiple=true} must
	 * accept a {@link List} of TID strings, resolve each one to a {@link TLObject}, and hand a list
	 * of wrappers to the body. The body navigates each wrapper's {@code name} attribute and joins
	 * the results — proving individual resolution, not just count.
	 *
	 * <p>
	 * Uses the same {@code luigis.pizzashop:Ingredient} type as
	 * {@link #testTLClassParameterResolvesTid()}.
	 * </p>
	 */
	public void testMultipleTLClassParameterResolvesListOfTids() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		TLObject ingredientA;
		TLObject ingredientB;
		TLObject ingredientC;
		String tidA;
		String tidB;
		String tidC;

		try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
			ingredientA = createObject(
				"new(`luigis.pizzashop:Ingredient`)" +
				"..set(`luigis.pizzashop:Component#name`, 'a')" +
				"..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#G`)" +
				"..set(`luigis.pizzashop:Component#costPerBaseUnit`, 1.0)");
			ingredientB = createObject(
				"new(`luigis.pizzashop:Ingredient`)" +
				"..set(`luigis.pizzashop:Component#name`, 'b')" +
				"..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#G`)" +
				"..set(`luigis.pizzashop:Component#costPerBaseUnit`, 1.0)");
			ingredientC = createObject(
				"new(`luigis.pizzashop:Ingredient`)" +
				"..set(`luigis.pizzashop:Component#name`, 'c')" +
				"..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#G`)" +
				"..set(`luigis.pizzashop:Component#costPerBaseUnit`, 1.0)");
			tx.commit();
			tidA = ingredientA.tId().asString();
			tidB = ingredientB.tId().asString();
			tidC = ingredientC.tId().asString();
		}

		try {
			ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
			p.setName("targets");
			p.setType(TLModelPartRef.ref("luigis.pizzashop:Ingredient"));
			p.setMandatory(true);
			p.setMultiple(true);

			// $targets.map(x -> $x.get(`…#name`)) produces a list of name strings;
			// .join(",") concatenates them into a single comma-separated string.
			Tool tool = singleTool(scriptConfig("listNames", ToolMode.READ,
				"$targets.map(x -> $x.get(`luigis.pizzashop:NamedEntities#name`)).join(\",\")", p));

			ToolResult result = tool.execute(Map.of("targets", List.of(tidA, tidB, tidC)), ToolContext.EMPTY);

			assertEquals("a,b,c", MarkedResults.disclosed(result.text()));
		} finally {
			try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
				if (ingredientA.tValid()) {
					ingredientA.tDelete();
				}
				if (ingredientB.tValid()) {
					ingredientB.tDelete();
				}
				if (ingredientC.tValid()) {
					ingredientC.tDelete();
				}
				tx.commit();
			}
		}
	}

	/**
	 * A tool with two parameters must bind both by name regardless of the order in which the caller
	 * passes them. The body {@code $first + "-" + $second} must produce {@code "a-b"} when
	 * {@code first="a"} and {@code second="b"} — even when the arguments are inserted into the map
	 * in reversed declaration order (proving name-based, not position-based, binding).
	 */
	public void testTwoParametersBindByName() {
		ScriptParameter first = param("first", "tl.core:String", /*mandatory*/ true);
		ScriptParameter second = param("second", "tl.core:String", /*mandatory*/ true);

		Tool tool = singleTool(scriptConfig("concat", ToolMode.READ,
			"$first + \"-\" + $second", first, second));

		// Declaration-order call
		ToolResult result = tool.execute(Map.of("first", "a", "second", "b"), ToolContext.EMPTY);
		assertEquals("a-b", MarkedResults.disclosed(result.text()));

		// Reversed insertion order in the map — result must be identical because binding is by name
		Map<String, Object> reversed = new LinkedHashMap<>();
		reversed.put("second", "b");
		reversed.put("first", "a");
		assertEquals("a-b", MarkedResults.disclosed(tool.execute(reversed, ToolContext.EMPTY).text()));
	}

	/**
	 * When a TID string is syntactically valid but refers to an object that does not exist in the
	 * knowledge base, the executor must return an error result containing the canonical
	 * "not found" message rather than throwing.
	 *
	 * <p>
	 * Strategy: create a real {@code Ingredient}, capture its TID (which reveals the KB storage
	 * type name, e.g. {@code StructuredElement:&lt;id&gt;}), delete the object, then invoke with a
	 * TID that uses the same storage-type prefix but a non-existent numeric id. This avoids
	 * hard-coding the KB storage type name, which differs from the TL model class name.
	 * </p>
	 */
	public void testUnknownTidReturnsNotFoundError() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		// Build a ghost TID: same storage-type prefix as a real Ingredient, but id 0 which
		// is never allocated by the KB sequence.
		String ghostTid;
		TLObject ingredient;
		try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
			ingredient = createObject(
				"new(`luigis.pizzashop:Ingredient`)" +
				"..set(`luigis.pizzashop:Component#name`, 'ghost-ingredient')" +
				"..set(`luigis.pizzashop:Component#baseUnit`, `luigis.pizzashop:UnitOfMeasure#G`)" +
				"..set(`luigis.pizzashop:Component#costPerBaseUnit`, 1.0)");
			tx.commit();
		}
		// Determine the KB storage type prefix (e.g. "StructuredElement"), then build a TID
		// with a large id (Long.MAX_VALUE) that is never allocated.
		String realTid = ingredient.tId().asString();
		String typePrefix = realTid.split(":", 2)[0];
		ghostTid = typePrefix + ":" + Long.MAX_VALUE;

		// Clean up the real object before running the tool so the KB is in a defined state.
		try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
			if (ingredient.tValid()) {
				ingredient.tDelete();
			}
			tx.commit();
		}

		ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
		p.setName("target");
		p.setType(TLModelPartRef.ref("luigis.pizzashop:Ingredient"));
		p.setMandatory(true);

		Tool tool = singleTool(scriptConfig("readName", ToolMode.READ,
			"$target.get(`luigis.pizzashop:NamedEntities#name`)", p));

		ToolResult result = tool.execute(Map.of("target", ghostTid), ToolContext.EMPTY);

		assertTrue("Expected 'not found' in error result, got: " + result.text(),
			result.text().contains("not found"));
	}

	/**
	 * A script tool that says nothing about its result answers exactly as it did before the two
	 * properties existed.
	 */
	public void testResultMetadataIsUnsetByDefault() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message",
			param("message", "tl.core:String", /*mandatory*/ true)));

		ToolResult result = tool.execute(Map.of("message", "hello"), ToolContext.EMPTY);

		assertNull("No summary unless one is configured.", result.summary());
		assertEquals("The installation's default limit applies.", -1, result.truncationHint());
	}

	/**
	 * A configured script tool states how much of its result may reach the model.
	 */
	public void testTruncationHintReachesTheResult() {
		ScriptToolConfig config = scriptConfig("echo", ToolMode.READ, "$message",
			param("message", "tl.core:String", /*mandatory*/ true));
		config.update(config.descriptor().getProperty(ScriptToolConfig.TRUNCATION_HINT),
			Integer.valueOf(120));

		ToolResult result =
			singleTool(config).execute(Map.of("message", "hello"), ToolContext.EMPTY);

		assertEquals(120, result.truncationHint());
	}

	/**
	 * A configured script tool states what compaction keeps in place of its result, computed from
	 * that result.
	 */
	public void testSummaryIsComputedFromTheResult() {
		ScriptToolConfig config = scriptConfig("count", ToolMode.READ, "$items",
			multipleParam("items", "tl.core:String"));
		config.update(config.descriptor().getProperty(ScriptToolConfig.SUMMARY),
			parseExpr("rows -> $rows.size() + \" rows\""));

		ToolResult result = singleTool(config)
			.execute(Map.of("items", List.of("a", "b", "c")), ToolContext.EMPTY);

		assertEquals("3 rows", MarkedResults.disclosed(result.summary()));
	}

	// --- helpers ---

	private static ScriptParameter multipleParam(String name, String typeRef) {
		ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
		p.setName(name);
		p.setType(TLModelPartRef.ref(typeRef));
		p.setMultiple(true);
		p.setMandatory(true);
		return p;
	}

	private static Tool singleTool(ScriptToolConfig config) {
		Config factoryConfig = TypedConfiguration.newConfigItem(Config.class);
		factoryConfig.getTools().put(config.getName(), config);
		ScriptToolFactory factory =
			new ScriptToolFactory(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, factoryConfig);
		List<Tool> tools = factory.createTools();
		assertEquals("Expected exactly one tool", 1, tools.size());
		return tools.get(0);
	}

	private static ScriptToolConfig scriptConfig(String name, ToolMode mode, String body,
			ScriptParameter... params) {
		ScriptToolConfig c = TypedConfiguration.newConfigItem(ScriptToolConfig.class);
		c.setName(name);
		c.setImplementation(parseExpr(body));
		for (ScriptParameter p : params) {
			c.getParameters().add(p);
		}
		// ScriptToolConfig.MODE has a default of DATA_WRITE; use the untyped update path
		// when a non-default mode is needed (e.g. READ for the echo tool).
		c.update(c.descriptor().getProperty(ScriptToolConfig.MODE), mode);
		return c;
	}

	private static ScriptParameter param(String name, String typeRef, boolean mandatory) {
		ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
		p.setName(name);
		p.setType(TLModelPartRef.ref(typeRef));
		p.setMandatory(mandatory);
		return p;
	}

	/**
	 * Creates a {@link TLObject} by evaluating a TL-Script expression.
	 */
	private static TLObject createObject(String script) throws ParseException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(script));
		Expr parsed = parser.expr();
		SearchExpression expr = SearchBuilder.toSearchExpression(ModelService.getApplicationModel(), parsed);
		QueryExecutor executor =
			QueryExecutor.compile(PersistencyLayer.getKnowledgeBase(), ModelService.getApplicationModel(), expr);
		return (TLObject) executor.executeWith(null, null, Args.some());
	}

	/**
	 * Parses a TL-Script expression string into an {@link Expr} AST using {@link ExprFormat}.
	 * (Note: {@code SearchBuilder.toExpr(String)} does not exist in engine 8.x; the correct entry
	 * point is {@code ExprFormat.INSTANCE.getValue(String, CharSequence)}.)
	 */
	private static Expr parseExpr(String src) {
		try {
			return ExprFormat.INSTANCE.getValue("implementation", src);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to parse TL-Script expression: " + src, ex);
		}
	}

	/**
	 * Creates the test suite with required service dependencies (KB + ModelService +
	 * SearchBuilder).
	 */
	public static Test suite() {
		return suite(ScriptToolFactoryTest.class);
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
		// Needed to run the tests as the root account, see setUp().
		modules.add(PersonManager.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}

}
