/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.tree.CollapseNodeArguments;
import com.top_logic.layout.react.control.tree.ExpandNodeArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.controlprovider.MetaResourceControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ResourceDisplayByExpression;
import com.top_logic.layout.view.element.TreeElement;
import com.top_logic.layout.view.element.TreeStructureConfig;
import com.top_logic.layout.view.table.ValueAsConfig;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.SelectionMode;
import com.top_logic.util.model.ModelService;

/**
 * Tests parsing and instantiation of {@link TreeElement}, and the nodes its tree opens when it is
 * displayed.
 */
public class TestTreeElement extends BasicTestCase {

	/** Name of the views given inline. */
	private static final String INLINE_VIEW = "inline-tree.view.xml";

	/** Name of the channel holding the object the tree of {@link #EXPANSION_VIEW} is built from. */
	private static final String INPUT = "input";

	/** Name of an input channel of {@link #EXPANSION_VIEW} that does not change its root. */
	private static final String EXTRA = "extra";

	/**
	 * A tree built from the object of the {@link #INPUT} channel, its initial expansion depth
	 * declared by the attribute filled in.
	 *
	 * <p>
	 * The object {@code root} holds {@code a} and {@code b}, {@code a} holds {@code a1} and
	 * {@code a2}, {@code a1} holds {@code a11}. The object {@code other} holds {@code x} and
	 * {@code y}, {@code x} holds {@code x1}. The object {@code shared} holds {@code a} and
	 * {@code z}.
	 * </p>
	 */
	private static final String EXPANSION_VIEW = """
		<view>
			<tree %s>
				<inputs>
					<input channel="input"/>
					<input channel="extra"/>
				</inputs>
				<root>$input</root>
				<children>if($node == "root", ["a", "b"],
					if($node == "a", ["a1", "a2"],
					if($node == "a1", ["a11"],
					if($node == "other", ["x", "y"],
					if($node == "x", ["x1"],
					if($node == "shared", ["a", "z"], []))))))</children>
			</tree>
		</view>
		""";

	/**
	 * Node state key of the depth of a node.
	 *
	 * @implNote Restated here because {@link ReactTreeControl} keeps it private.
	 */
	private static final String NODE_DEPTH = "depth";

	/**
	 * Node state key telling whether a node is open.
	 *
	 * @implNote Restated here because {@link ReactTreeControl} keeps it private.
	 */
	private static final String NODE_EXPANDED = "expanded";

	/**
	 * Node state key telling whether a node has no children.
	 *
	 * @implNote Restated here because {@link ReactTreeControl} keeps it private.
	 */
	private static final String NODE_LEAF = "leaf";

	private ViewContext _context;

	private ViewChannel _input;

	private ViewChannel _extra;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_input = register(INPUT);
		_extra = register(EXTRA);
		_input.set("root");
	}

	private ViewChannel register(String name) {
		ViewChannel channel = new DefaultViewChannel(name);
		_context.registerChannel(name, channel);
		return channel;
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_input = null;
		_extra = null;

		super.tearDown();
	}

	/**
	 * Tests that a view XML with a {@code <tree>} element can be parsed into configuration.
	 */
	public void testParseTreeConfig() throws Exception {
		TreeElement.Config treeConfig = readTreeConfig();

		// Verify inputs.
		assertEquals("Should have one input", 1, treeConfig.getInputs().size());
		assertEquals("Input channel name", "rootInput", treeConfig.getInputs().get(0).getChannel().getChannelName());

		// Verify root expression is present (non-null).
		assertNotNull("Root expression should be set", treeConfig.getRoot());

		// Verify children expression is present (non-null).
		assertNotNull("Children expression should be set", treeConfig.getChildren());

		// Verify selection channel.
		assertNotNull("Selection should be set", treeConfig.getSelection());
		assertEquals("Selection channel name", "selectedNode", treeConfig.getSelection().getChannelName());
	}

	/**
	 * Tests that the function telling what holds an object in the tree is optional, and is read
	 * where the tree declares it.
	 */
	public void testParents() throws Exception {
		assertNull("A tree that does not say what holds an object is searched for a node.",
			TypedConfiguration.newConfigItem(TreeElement.Config.class).getParents());

		assertNotNull("The tree declares what holds an object in it.", readTreeConfig().getParents());
	}

	/**
	 * Tests that a tree declaring observed types without saying what holds an object fails to
	 * load, naming the missing property.
	 */
	public void testObservedTypesRequireParents() {
		try {
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(observingTree(""), INLINE_VIEW)));
			fail("A tree observing types places their objects through the parents function.");
		} catch (ConfigurationException | AbortExecutionException ex) {
			String reported = messages(ex);
			assertTrue("The failure names the property: " + reported,
				reported.contains(TreeElement.Config.PARENTS));
		}
	}

	/**
	 * Tests that a tree declaring observed types together with the parents function loads.
	 */
	public void testObservedTypesWithParentsLoad() throws Exception {
		ViewElement.Config config = ViewLoader.parseConfig(List.of(CharacterContents.newContent(
			observingTree("<parents>$rootInput</parents>"), INLINE_VIEW)));

		assertNotNull(((TreeElement.Config) config.getContent()).getParents());
	}

	/**
	 * A view with a tree observing a type, with the given additional content.
	 */
	private static String observingTree(String content) {
		return """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<channels>
					<channel name="rootInput"/>
				</channels>
				<tree observed-types="test.types:A">
					<inputs>
						<input channel="rootInput"/>
					</inputs>
					<root>$rootInput</root>
					<children>list()</children>
					%s
				</tree>
			</view>
			""".formatted(content);
	}

	/** The messages along the cause chain of the given failure. */
	private static String messages(Throwable failure) {
		StringBuilder result = new StringBuilder();
		for (Throwable current = failure; current != null; current = current.getCause()) {
			result.append(current.getMessage()).append('\n');
		}
		return result.toString();
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTreeElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

		UIElement element = context.getInstance(readViewConfig(context));
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Tests that a tree selects one node at a time unless it configures the selection of any number
	 * of them.
	 */
	public void testSelectionMode() throws Exception {
		assertEquals("A tree selects one node at a time unless it says otherwise.",
			SelectionMode.SINGLE,
			TypedConfiguration.newConfigItem(TreeElement.Config.class).getSelectionMode());

		assertEquals("The tree configures the selection of any number of nodes.",
			SelectionMode.MULTI, readTreeConfig().getSelectionMode());
	}

	/**
	 * Tests that a node of a tree displays the object it stands for without leading away from the
	 * tree, and that a tree saying nothing about its node display gets exactly that.
	 */
	public void testNodeContent() throws Exception {
		assertNodeDisplay("A tree displays its nodes this way unless it says otherwise.",
			TypedConfiguration.newConfigItem(TreeElement.Config.class));

		assertNodeDisplay("The test tree says nothing about its node display.", readTreeConfig());
	}

	/**
	 * Tests that a display of an object leads to it wherever the application shows it, so that the
	 * tree is the only place dropping that link.
	 */
	public void testAConfiguredNodeContentLeadsToItsObject() {
		assertTrue("A display of an object leads to the place the application shows it at.",
			TypedConfiguration.newConfigItem(MetaResourceControlProvider.Config.class).getLink());
	}

	/**
	 * Tests that a tree shows its nodes through a display computing their parts with expressions,
	 * whose expressions see the object of a node as their only variable - a display gets no
	 * channels, so the inputs of the tree are none of its variables.
	 */
	public void testScriptedNodeContent() throws Exception {
		ViewElement.Config config = ViewLoader.parseConfig(List.of(CharacterContents.newContent("""
			<view>
				<channels>
					<channel name="rootInput"/>
				</channels>
				<tree>
					<inputs>
						<input channel="rootInput"/>
					</inputs>
					<root>$rootInput</root>
					<children>list()</children>
					<nodeContent class="%s" label="'Node ' + $value"/>
				</tree>
			</view>
			""".formatted(ResourceDisplayByExpression.class.getName()), INLINE_VIEW)));
		TreeElement.Config tree = (TreeElement.Config) config.getContent();

		PolymorphicConfiguration<?> nodeContent = tree.getNodeContent();
		assertTrue("Node content: " + nodeContent, nodeContent instanceof ResourceDisplayByExpression.Config);
		ResourceDisplayByExpression.Config display = (ResourceDisplayByExpression.Config) nodeContent;
		assertEquals(List.of(ValueAsConfig.DEFAULT_VALUE_AS), InputScript.variables(display,
			display.descriptor().getProperty(ResourceDisplayByExpression.Config.LABEL)));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);
		assertNotNull(context.getInstance(tree));
		context.checkErrors();
	}

	private void assertNodeDisplay(String message, TreeElement.Config treeConfig) {
		PolymorphicConfiguration<?> nodeContent = treeConfig.getNodeContent();

		assertNotNull(message, nodeContent);
		assertTrue(message + " Node content: " + nodeContent, nodeContent instanceof TreeElement.NodeDisplay);

		TreeElement.NodeDisplay display = (TreeElement.NodeDisplay) nodeContent;
		assertTrue("A node shows the icon of its object.", display.getImage());
		assertTrue("A node shows the label of its object.", display.getLabel());
		assertFalse("A click on a node selects it instead of leading away from the tree.",
			display.getLink());
	}

	/**
	 * The {@link com.top_logic.layout.view.element.TreeElement.Config} of the test view.
	 */
	private TreeElement.Config readTreeConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

		ViewElement.Config config = readViewConfig(context);

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TreeElement config.
		assertTrue("Content should be TreeElement config",
			config.getContent() instanceof TreeElement.Config);

		return (TreeElement.Config) config.getContent();
	}

	/**
	 * Reads the test view.
	 */
	private ViewElement.Config readViewConfig(DefaultInstantiationContext context) throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTreeElement.class, "test-tree.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		return (ViewElement.Config) reader.read();
	}

	/**
	 * A tree saying nothing about its expansion opens its top-level nodes: their children are
	 * displayed, closed.
	 */
	public void testTopLevelOpenByDefault() throws Exception {
		assertEquals(TreeStructureConfig.DEFAULT_INITIAL_EXPANSION_DEPTH,
			TypedConfiguration.newConfigItem(TreeElement.Config.class).getInitialExpansionDepth());

		ReactTreeControl control = createControl("");
		assertEquals("a open, a1 closed, a2 and b leaves.",
			List.of("0+", "1-", "1.", "0."), nodes(control));
	}

	/**
	 * A tree of initial expansion depth 2 opens the children of its top-level nodes as well.
	 */
	public void testDepthTwo() throws Exception {
		ReactTreeControl control = createControl(depth(2));
		assertEquals("a and a1 open.", List.of("0+", "1+", "2.", "1.", "0."), nodes(control));
	}

	/**
	 * A tree of initial expansion depth 0 shows its top-level nodes closed.
	 */
	public void testDepthZero() throws Exception {
		ReactTreeControl control = createControl(depth(0));
		assertEquals("a closed.", List.of("0-", "0."), nodes(control));
	}

	/**
	 * A node the user closed stays closed when the tree is built anew from the same root.
	 */
	public void testClosedNodeStaysClosed() throws Exception {
		ReactTreeControl control = createControl("");
		control.attach();
		try {
			control.executeClientCommand(ReactTreeControl.COLLAPSE_COMMAND,
				Map.of(CollapseNodeArguments.NODE_ID, nodeId(control, 0)));
			assertEquals(List.of("0-", "0."), nodes(control));

			_extra.set("changed");
			assertEquals("The tree is built anew from the same root, a stays closed.",
				List.of("0-", "0."), nodes(control));
		} finally {
			control.detach();
		}
	}

	/**
	 * A tree built from another root starts afresh: the top levels of the new tree are open, the
	 * nodes below closed - also the nodes of objects the tree before held, whatever the user did with
	 * them there.
	 */
	public void testDepthAppliesToNewRoot() throws Exception {
		ReactTreeControl control = createControl("");
		control.attach();
		try {
			_input.set("other");
			assertEquals("x open, y a leaf.", List.of("0+", "1.", "0."), nodes(control));

			_input.set("root");
			control.executeClientCommand(ReactTreeControl.EXPAND_COMMAND,
				Map.of(ExpandNodeArguments.NODE_ID, nodeId(control, 1)));
			assertEquals("a1 opened by the user.", List.of("0+", "1+", "2.", "1.", "0."), nodes(control));
			control.executeClientCommand(ReactTreeControl.COLLAPSE_COMMAND,
				Map.of(CollapseNodeArguments.NODE_ID, nodeId(control, 0)));
			assertEquals("a closed by the user.", List.of("0-", "0."), nodes(control));

			_input.set("shared");
			assertEquals("a open again, a1 closed again, z a leaf.", List.of("0+", "1-", "1.", "0."),
				nodes(control));
		} finally {
			control.detach();
		}
	}

	private static String depth(int depth) {
		return TreeStructureConfig.INITIAL_EXPANSION_DEPTH + "=\"" + depth + "\"";
	}

	/** The control of the tree of the {@link #EXPANSION_VIEW} with the given attributes. */
	private ReactTreeControl createControl(String attributes) throws Exception {
		ViewElement.Config config = ViewLoader.parseConfig(List.of(
			CharacterContents.newContent(EXPANSION_VIEW.formatted(attributes), INLINE_VIEW)));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestTreeElement.class);
		TreeElement element = (TreeElement) instantiation.getInstance(config.getContent());
		instantiation.checkErrors();
		return (ReactTreeControl) element.createControl(_context);
	}

	/**
	 * The displayed nodes of the given tree in display order, each as its depth followed by
	 * {@code +} for an open node, {@code -} for a closed one, and {@code .} for a leaf.
	 */
	private static List<String> nodes(ReactTreeControl control) {
		List<String> result = new ArrayList<>();
		for (Object node : (List<?>) state(control).get(ReactTreeControl.NODES)) {
			Map<?, ?> nodeState = (Map<?, ?>) node;
			String mark = Boolean.TRUE.equals(nodeState.get(NODE_LEAF)) ? "."
				: Boolean.TRUE.equals(nodeState.get(NODE_EXPANDED)) ? "+" : "-";
			result.add(((Number) nodeState.get(NODE_DEPTH)).intValue() + mark);
		}
		return result;
	}

	/** The id of the displayed node at the given index. */
	private static String nodeId(ReactTreeControl control, int index) {
		List<?> nodes = (List<?>) state(control).get(ReactTreeControl.NODES);
		return (String) ((Map<?, ?>) nodes.get(index)).get(ReactTreeControl.NODE_ID);
	}

	/** The state of the given control as the client parses it. */
	private static Map<?, ?> state(ReactTreeControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The structure of the tree is given by TL-Script expressions, whose compilation and
	 *           evaluation need the application model and the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase} they are executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTreeElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
