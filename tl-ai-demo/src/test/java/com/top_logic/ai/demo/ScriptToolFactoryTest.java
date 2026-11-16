/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.tools.model.AgentObjectFormat;
import com.top_logic.tl.ai.core.tools.model.AgentObjectListFormat;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.Config;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.tool.api.I18NConstants;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ScriptToolFactory}.
 */
public class ScriptToolFactoryTest extends BasicTestCase {

	/** Name of the text parameter of the echo tools. */
	private static final String MESSAGE = "message";

	/** The model type of the object parameters. */
	static final String ROOM = "demo.events:Room";

	/**
	 * Runs each test as the root account.
	 *
	 * <p>
	 * TL-Script applies the model access rights of {@code TLContext.currentUser()}: {@code new(...)}
	 * checks the CREATE right, and attribute reads are filtered. Those rights deny by default and this
	 * test app configures no {@code <security-config>}, so the fixtures could not be created and every
	 * attribute read would come back empty for an ordinary account. The root account is an
	 * administrator, for whom the engine checks no rights. What these tests are about is TID-to-wrapper
	 * parameter resolution, not authorization.
	 * </p>
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(PersonManager.getManager().getRoot(),
			() -> {
				super.runTest();
				return null;
			});
	}

	/**
	 * Regression for the $parameter-not-resolved bug: a tool whose body is simply {@code $message}
	 * must return the value passed for {@code message}.
	 */
	public void testNameBindingResolvesParameter() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message", mandatoryString(MESSAGE)));

		ToolResult result = tool.execute(Map.of(MESSAGE, "hello"), ToolContext.EMPTY);

		assertEquals("hello", MarkedResults.disclosed(result.text()));
	}

	/**
	 * When a parameter is optional and omitted from the call, its configured default value is the
	 * binding.
	 */
	public void testDefaultValueAppliedWhenMissing() {
		Tool tool = singleTool(scriptConfig("greet", ToolMode.READ, "$greeting + \"!\"",
			"<property name='greeting' type='String' default='hi'/>"));

		ToolResult result = tool.execute(Map.of(), ToolContext.EMPTY);

		assertEquals("hi!", MarkedResults.disclosed(result.text()));
	}

	/**
	 * When a mandatory parameter is omitted, the executor must return an error result naming the
	 * missing parameter.
	 */
	public void testMandatoryMissingFailsCleanly() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message", mandatoryString(MESSAGE)));

		ToolResult result = tool.execute(Map.of(), ToolContext.EMPTY);

		assertTrue("Expected missing-parameter error, got: " + result.text(),
			result.text().contains("The argument '" + MESSAGE + "' is required."));
	}

	/**
	 * A script tool that failed answers as a <em>failure</em>, not as a result that reads like one.
	 *
	 * <p>
	 * A TL-Script tool is authored in the running application, so its body is exactly the one that
	 * fails after an installation edits it. Before Gitea #387 the catch below
	 * {@code ScriptToolExecutor.execute} returned an ordinary result, which reached the provider as a
	 * successful tool call - a script tool could therefore fail invisibly, with only the words
	 * "Error executing tool" to say otherwise.
	 * </p>
	 */
	public void testAFailingScriptToolIsFlagged() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message", mandatoryString(MESSAGE)));

		ToolResult result = tool.execute(Map.of(), ToolContext.EMPTY);

		assertTrue("The call failed, so the answer must say so: " + result.text(), result.failed());
	}

	/** A script tool that ran answers as a success, flag unset. */
	public void testASucceedingScriptToolIsNotFlagged() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message", mandatoryString(MESSAGE)));

		ToolResult result = tool.execute(Map.of(MESSAGE, "hi!"), ToolContext.EMPTY);

		assertFalse("Nothing failed here: " + result.text(), result.failed());
	}

	/**
	 * A list parameter receives a {@link List} from the JSON array the caller passes; the body can
	 * call {@code .size()} on it directly.
	 */
	public void testMultipleParameterReceivesList() {
		Tool tool = singleTool(scriptConfig("count", ToolMode.READ, "$items.size()", stringList("items")));
		ToolResult result = tool.execute(Map.of("items", List.of("a", "b", "c")), ToolContext.EMPTY);

		assertEquals("3", MarkedResults.disclosed(result.text()));
	}

	/** The schema of a list parameter is an array. */
	public void testListParameterSchemaIsArray() {
		Tool tool = singleTool(scriptConfig("count", ToolMode.READ, "$items.size()", stringList("items")));

		String schema = tool.schema().inputSchemaJson();
		assertTrue("Expected an array schema: " + schema, schema.contains("\"array\""));
	}

	/**
	 * An object parameter accepts the id of an object and resolves it to the object before the
	 * script body executes. The body can then navigate the object.
	 *
	 * <p>
	 * Uses {@code demo.events:Room} (concrete class with a {@code name} attribute inherited from
	 * {@code demo.events:Named}) because it is already part of the tl-ai-demo model and is exercised
	 * by {@link InstanceBrowserToolTest}.
	 * </p>
	 */
	public void testObjectParameterResolvesId() throws Exception {
		TLObject room = createRoom("fixture-room");
		try {
			Tool tool = singleTool(scriptConfig("readName", ToolMode.READ,
				"$target.get(`demo.events:Named#name`)", roomParameter("target")));

			ToolResult result = tool.execute(Map.of("target", id(room)), ToolContext.EMPTY);

			assertEquals("fixture-room", MarkedResults.disclosed(result.text()));
		} finally {
			delete(room);
		}
	}

	/**
	 * A list parameter of objects accepts a JSON array of ids, resolves each one and hands a list of
	 * the objects to the body. The body navigates each object's {@code name} attribute and joins the
	 * results - proving individual resolution, not just count.
	 */
	public void testObjectListParameterResolvesIds() throws Exception {
		TLObject roomA = createRoom("a");
		TLObject roomB = createRoom("b");
		TLObject roomC = createRoom("c");
		try {
			Tool tool = singleTool(scriptConfig("listNames", ToolMode.READ,
				"$targets.map(x -> $x.get(`demo.events:Named#name`)).join(\",\")", roomListParameter("targets")));

			ToolResult result =
				tool.execute(Map.of("targets", List.of(id(roomA), id(roomB), id(roomC))), ToolContext.EMPTY);

			assertEquals("a,b,c", MarkedResults.disclosed(result.text()));
		} finally {
			delete(roomA);
			delete(roomB);
			delete(roomC);
		}
	}

	/**
	 * A tool with two parameters must bind both by name regardless of the order in which the caller
	 * passes them. The body {@code $first + "-" + $second} must produce {@code "a-b"} when
	 * {@code first="a"} and {@code second="b"} - even when the arguments are inserted into the map
	 * in reversed declaration order (proving name-based, not position-based, binding).
	 */
	public void testTwoParametersBindByName() {
		Tool tool = singleTool(scriptConfig("concat", ToolMode.READ,
			"$first + \"-\" + $second", mandatoryString("first"), mandatoryString("second")));

		// Declaration-order call
		ToolResult result = tool.execute(Map.of("first", "a", "second", "b"), ToolContext.EMPTY);
		assertEquals("a-b", MarkedResults.disclosed(result.text()));

		// Reversed insertion order in the map - result must be identical because binding is by name
		Map<String, Object> reversed = new LinkedHashMap<>();
		reversed.put("second", "b");
		reversed.put("first", "a");
		assertEquals("a-b", MarkedResults.disclosed(tool.execute(reversed, ToolContext.EMPTY).text()));
	}

	/**
	 * An id that is syntactically valid but names no object is refused with the "not found"
	 * diagnosis rather than throwing.
	 */
	public void testUnknownIdReturnsNotFoundError() throws Exception {
		TLObject room = createRoom("ghost-room");
		// Same storage-type prefix as a real Room, but an id that is never allocated.
		String ghostId = id(room).split(":", 2)[0] + ":" + Long.MAX_VALUE;
		delete(room);

		Tool tool = singleTool(scriptConfig("readName", ToolMode.READ,
			"$target.get(`demo.events:Named#name`)", roomParameter("target")));

		ToolResult result = tool.execute(Map.of("target", ghostId), ToolContext.EMPTY);

		assertTrue(result.failed());
		assertTrue("Expected 'not found' in error result, got: " + result.text(),
			result.text().contains("not found"));
	}

	/**
	 * A parameter with allowed values lists them in the schema, and a value outside them is
	 * refused before the body runs.
	 */
	public void testValueOutsideOptionsIsRefused() {
		Tool tool = singleTool(scriptConfig("paint", ToolMode.READ, "$color",
			"<property name='color' type='String'><mandatory/><options fun='" + Colors.class.getName()
				+ "'/></property>"));

		String schema = tool.schema().inputSchemaJson();
		assertTrue("Allowed values in schema: " + schema,
			schema.contains("\"" + Colors.RED + "\"") && schema.contains("\"" + Colors.GREEN + "\""));

		assertEquals(Colors.RED,
			MarkedResults.disclosed(tool.execute(Map.of("color", Colors.RED), ToolContext.EMPTY).text()));

		ToolResult refused = tool.execute(Map.of("color", "blue"), ToolContext.EMPTY);
		assertTrue(refused.failed());
		assertTrue("Names the refused value: " + refused.text(), refused.text().contains("blue"));
	}

	/** The allowed values of a list parameter apply to each element. */
	public void testOptionsApplyToEachListElement() {
		Tool tool = singleTool(scriptConfig("paint", ToolMode.READ, "$colors.size()",
			"<property name='colors' type='java.util.List' element-type='String'><format value='" + CommaSeparatedStrings.class.getName()
				+ "'/><options fun='" + Colors.class.getName() + "'/></property>"));

		String schema = tool.schema().inputSchemaJson();
		assertTrue("Element values in schema: " + schema,
			schema.contains("\"array\"") && schema.contains("\"" + Colors.GREEN + "\""));

		assertEquals("2", MarkedResults.disclosed(
			tool.execute(Map.of("colors", List.of(Colors.RED, Colors.GREEN)), ToolContext.EMPTY).text()));
		ToolResult refused = tool.execute(Map.of("colors", List.of(Colors.RED, "blue")), ToolContext.EMPTY);
		assertTrue(refused.failed());
		assertTrue("Names the refused value: " + refused.text(), refused.text().contains("blue"));
	}

	/** An object of another model type than the declared one is refused, naming the expected type. */
	public void testObjectOfOtherTypeIsRefused() {
		Tool tool = singleTool(scriptConfig("readName", ToolMode.READ,
			"$target.get(`demo.events:Named#name`)", roomParameter("target")));

		ToolResult result =
			tool.execute(Map.of("target", id(PersonManager.getManager().getRoot())), ToolContext.EMPTY);

		assertTrue(result.failed());
		assertTrue("Names the expected type: " + result.text(), result.text().contains(ROOM));
	}

	/** An object the run may not read is refused like an unknown one; the body does not run. */
	public void testUnreadableObjectIsRefused() throws Exception {
		TLObject room = createRoom("hidden-room");
		try {
			Tool tool = singleTool(scriptConfig("readName", ToolMode.READ,
				"$target.get(`demo.events:Named#name`)", roomParameter("target")));

			ToolResult result = tool.execute(Map.of("target", id(room)), noReadContext());

			assertTrue(result.failed());
			assertTrue("Refused as not found: " + result.text(), result.text().contains("not found"));
			assertFalse("The name is not disclosed: " + result.text(), result.text().contains("hidden-room"));
		} finally {
			delete(room);
		}
	}

	/**
	 * A script tool that says nothing about its result answers exactly as it did before the two
	 * properties existed.
	 */
	public void testResultMetadataIsUnsetByDefault() {
		Tool tool = singleTool(scriptConfig("echo", ToolMode.READ, "$message", mandatoryString(MESSAGE)));

		ToolResult result = tool.execute(Map.of(MESSAGE, "hello"), ToolContext.EMPTY);

		assertNull("No summary unless one is configured.", result.summary());
		assertEquals("The installation's default limit applies.", -1, result.truncationHint());
	}

	/**
	 * A configured script tool states how much of its result may reach the model.
	 */
	public void testTruncationHintReachesTheResult() {
		ScriptToolConfig config = scriptConfig("echo", ToolMode.READ, "$message", mandatoryString(MESSAGE));
		config.update(config.descriptor().getProperty(ScriptToolConfig.TRUNCATION_HINT),
			Integer.valueOf(120));

		ToolResult result =
			singleTool(config).execute(Map.of(MESSAGE, "hello"), ToolContext.EMPTY);

		assertEquals(120, result.truncationHint());
	}

	/**
	 * A configured script tool states what compaction keeps in place of its result, computed from
	 * that result.
	 */
	public void testSummaryIsComputedFromTheResult() {
		ScriptToolConfig config = scriptConfig("count", ToolMode.READ, "$items", stringList("items"));
		config.update(config.descriptor().getProperty(ScriptToolConfig.SUMMARY),
			parseExpr("rows -> $rows.size() + \" rows\""));

		ToolResult result = singleTool(config)
			.execute(Map.of("items", List.of("a", "b", "c")), ToolContext.EMPTY);

		assertEquals("3 rows", MarkedResults.disclosed(result.summary()));
	}

	/**
	 * The commit message expression sees the call's arguments as the implementation does, and a text
	 * it answers is logged as that text.
	 */
	public void testCommitMessageIsComputedFromTheArguments() {
		ScriptToolConfig config = greeter();
		config.update(config.descriptor().getProperty(ScriptToolConfig.COMMIT_MESSAGE),
			parseExpr("\"Greeted \" + $name"));

		assertEquals(ResKey.text("Greeted Ada"), commitMessage(config, "Ada"));
	}

	/** An internationalized text the expression answers is logged as it is, translatable. */
	public void testAnInternationalizedCommitMessageStaysTranslatable() {
		ScriptToolConfig config = greeter();
		config.update(config.descriptor().getProperty(ScriptToolConfig.COMMIT_MESSAGE),
			parseExpr("#(\"{0} begrüßt\"@de, \"Greeted {0}\"@en).fill($name)"));

		ResKey message = commitMessage(config, "Ada");

		assertEquals("Greeted Ada", ResKeyUtil.getTranslation(message, Locale.ENGLISH));
		assertEquals("Ada begrüßt", ResKeyUtil.getTranslation(message, Locale.GERMAN));
	}

	/** Without an expression, or with an empty answer, the tool's label is the message. */
	public void testWithoutACommitMessageTheLabel() {
		ScriptToolConfig config = greeter();
		config.setLabel(ResKey.text("Greet"));

		assertEquals(ResKey.text("Greet"), commitMessage(config, "Ada"));

		config.update(config.descriptor().getProperty(ScriptToolConfig.COMMIT_MESSAGE), parseExpr("\"\""));
		assertEquals(ResKey.text("Greet"), commitMessage(config, "Ada"));
	}

	/** Without an expression and a label, the generic message naming the tool. */
	public void testWithoutCommitMessageAndLabelTheGenericMessage() {
		assertEquals(I18NConstants.TOOL_CALL__TOOL.fill("greet"),
			commitMessage(greeter(), "Ada"));
	}

	private static ScriptToolConfig greeter() {
		return scriptConfig("greet", ToolMode.DATA_WRITE, "\"Hello \" + $name", mandatoryString("name"));
	}

	private static ResKey commitMessage(ScriptToolConfig config, String name) {
		return singleTool(config).commitMessage(Map.of("name", name), ToolContext.EMPTY);
	}

	// --- helpers ---

	/**
	 * The allowed values of a parameter in {@link ScriptToolFactoryTest#testValueOutsideOptionsIsRefused()}.
	 */
	public static class Colors extends Function0<List<String>> {

		static final String RED = "red";

		static final String GREEN = "green";

		@Override
		public List<String> apply() {
			return List.of(RED, GREEN);
		}
	}

	private static String mandatoryString(String name) {
		return "<property name='" + name + "' type='String'><mandatory/></property>";
	}

	private static String stringList(String name) {
		return "<property name='" + name + "' type='java.util.List' element-type='String'><mandatory/>"
			+ "<format value='" + CommaSeparatedStrings.class.getName() + "'/></property>";
	}

	private static String roomParameter(String name) {
		return "<property name='" + name + "' type='" + TLObject.class.getName() + "'><mandatory/>"
			+ "<format value='" + AgentObjectFormat.class.getName() + "'/>"
			+ "<object-type value='" + ROOM + "'/></property>";
	}

	private static String roomListParameter(String name) {
		return "<property name='" + name + "' type='java.util.List' element-type='" + TLObject.class.getName()
			+ "'><mandatory/>"
			+ "<format value='" + AgentObjectListFormat.class.getName() + "'/>"
			+ "<object-type value='" + ROOM + "'/></property>";
	}

	static Tool singleTool(ScriptToolConfig config) {
		Config factoryConfig = TypedConfiguration.newConfigItem(Config.class);
		factoryConfig.getTools().put(config.getName(), config);
		ScriptToolFactory factory =
			new ScriptToolFactory(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, factoryConfig);
		List<Tool> tools = factory.createTools();
		assertEquals("Expected exactly one tool", 1, tools.size());
		return tools.get(0);
	}

	/**
	 * A tool configuration.
	 *
	 * @param properties
	 *        The XML declarations of the parameters, each a {@code <property>} element.
	 */
	private static ScriptToolConfig scriptConfig(String name, ToolMode mode, String body, String... properties) {
		ScriptToolConfig c = TypedConfiguration.newConfigItem(ScriptToolConfig.class);
		c.setName(name);
		c.setImplementation(parseExpr(body));
		if (properties.length > 0) {
			c.setParameters(readParameters("<parameters>" + String.join("", properties) + "</parameters>"));
		}
		// ScriptToolConfig.MODE has a default of DATA_WRITE; use the untyped update path
		// when a non-default mode is needed (e.g. READ for the echo tool).
		c.update(c.descriptor().getProperty(ScriptToolConfig.MODE), mode);
		return c;
	}

	private static ConfigurationDescriptorConfig readParameters(String xml) {
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(ScriptToolConfig.PARAMETERS,
				TypedConfiguration.getConfigurationDescriptor(ConfigurationDescriptorConfig.class)));
		reader.setSource(CharacterContents.newContent(xml));
		try {
			return (ConfigurationDescriptorConfig) reader.read();
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Invalid parameter declaration: " + xml, ex);
		}
	}

	/** A run whose principal may read no object. */
	private static ToolContext noReadContext() {
		ModelSecurity noRead = (ModelSecurity) Proxy.newProxyInstance(ModelSecurity.class.getClassLoader(),
			new Class<?>[] { ModelSecurity.class }, (proxy, method, args) -> {
				if (method.getName().equals("canRead")) {
					return Boolean.FALSE;
				}
				return method.invoke(ModelSecurity.UNCHECKED, args);
			});
		SecurityContext security = (SecurityContext) Proxy.newProxyInstance(SecurityContext.class.getClassLoader(),
			new Class<?>[] { SecurityContext.class }, (proxy, method, args) -> {
				if (method.getName().equals("modelSecurity")) {
					return noRead;
				}
				return method.invoke(SecurityContext.UNRESTRICTED, args);
			});
		return new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
	}

	static String id(TLObject object) {
		return object.tId().asString();
	}

	static TLObject createRoom(String name) throws ParseException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
			TLObject room = createObject(
				"new(`" + ROOM + "`)" +
				"..set(`demo.events:Named#name`, '" + name + "')" +
				"..set(`demo.events:Room#capacity`, 10)");
			tx.commit();
			return room;
		}
	}

	static void delete(TLObject object) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.NONE)) {
			if (object.tValid()) {
				object.tDelete();
			}
			tx.commit();
		}
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
