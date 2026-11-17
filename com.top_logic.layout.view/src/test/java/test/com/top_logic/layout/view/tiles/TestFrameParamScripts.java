/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.tiles.FrameParamConfig;
import com.top_logic.layout.view.tiles.FrameRoute;
import com.top_logic.layout.view.tiles.FrameRouteConfig;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileFrameRouteParticipant;
import com.top_logic.layout.view.tiles.TileStackScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.model.ModelService;

/**
 * Tests the conversion scripts of a {@link FrameParamConfig route parameter}: the value of the
 * parameter is named like the parameter, the text of the URL is {@value FrameParamConfig#VALUE}.
 */
public class TestFrameParamScripts extends BasicTestCase {

	private static final String FRAME = "frame";

	private static final String TYPE = "type";

	/**
	 * A frame whose item parameter reads the input {@link #TYPE}: the URL carries the type in front
	 * of the item, and an item of another type names nothing.
	 */
	private static final String TYPED_ITEM = "<frame id='item' route='item/:item' view='item.view.xml'>"
		+ "<param name='item' inputs='" + TYPE + "' expr=\"$type + '-' + $item\""
		+ " reverse=\"$value.stringStartsWith($type + '-') ? $value : null\"/>"
		+ "</frame>";

	/**
	 * Tests that both scripts see their value under the documented name, in the editor and at
	 * runtime.
	 */
	public void testParamScripts() throws Exception {
		FrameRouteConfig frame = parse("<frame id='item' route='item/:item' view='item.view.xml'>"
			+ "<param name='item' expr=\"'id-' + $item\" reverse=\"$value + '!'\"/>"
			+ "</frame>");
		FrameParamConfig param = frame.getParams().get(0);
		assertScope(List.of("item"), param, FrameParamConfig.EXPR);
		assertScope(List.of(FrameParamConfig.VALUE), param, FrameParamConfig.REVERSE);

		FrameRoute route = FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
		assertEquals("item/id-42", route.segment(Map.of("item", "42"), Map.of()));
		RouteMatch match = route.pattern().match("item/id-42");
		assertNotNull(match);
		assertEquals(Map.of("item", "id-42!"), route.resolveParams(match, List.of(), Map.of()));
	}

	/**
	 * Tests that the scripts of a parameter with inputs see the current values of the channels of
	 * the view declaring the stack, in front of the value and the text, so that a stack embedded
	 * twice restricts its addresses to the objects of its embedding.
	 */
	public void testParamInputs() throws Exception {
		FrameRoute root = itemStack();
		FrameParamConfig param = parse(TYPED_ITEM).getParams().get(0);
		assertScope(List.of(TYPE, "item"), param, FrameParamConfig.EXPR);
		assertScope(List.of(TYPE, FrameParamConfig.VALUE), param, FrameParamConfig.REVERSE);

		TileStackScope stackA = stack(root, "a");
		TileStackScope stackB = stack(root, "b");

		assertEquals("A URL naming an object of another embedding restores no frame.", List.of(),
			adopt(stackA, "item/b-42"));
		assertEquals("A URL naming an object of its embedding restores the frame with the object.",
			List.of(Map.of("item", "a-42")), adopt(stackA, "item/a-42"));
		assertEquals(List.of(Map.of("item", "b-42")), adopt(stackB, "item/b-42"));

		stackA.popTo(0);
		stackA.push("item", null, Map.of("item", "7"));
		assertEquals("The URL is written with the value of the input.", "item/a-7",
			new TileFrameRouteParticipant(stackA, new RouteManager()).activeRouteSegment().path());
		stackB.popTo(0);
		stackB.push("item", null, Map.of("item", "7"));
		assertEquals("item/b-7",
			new TileFrameRouteParticipant(stackB, new RouteManager()).activeRouteSegment().path());
	}

	/**
	 * Tests that the inputs are read whenever a script is evaluated, so that the address follows
	 * what the input channel currently holds.
	 */
	public void testParamInputsFollowChannel() throws Exception {
		FrameRoute root = itemStack();
		ViewContext view = declaringView("a");
		TileStackScope stack = new TileStackScope(path(), root, null, null, null, view);
		FrameRoute item = stack.declaration("item");

		assertEquals("item/a-7", item.segment(Map.of("item", "7"), stack.inputs(item)));
		view.resolveChannel(new ChannelRef(TYPE)).set("c");
		assertEquals("item/c-7", item.segment(Map.of("item", "7"), stack.inputs(item)));
	}

	/**
	 * Tests that an input naming a channel the view declaring the stack does not know is rejected
	 * when the stack is displayed.
	 */
	public void testUnknownInputRejected() throws Exception {
		FrameRoute root = itemStack();
		try {
			new TileStackScope(path(), root, null, null, null, new DefaultViewContext(null));
			fail("An input naming an unknown channel must be rejected.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(TYPE));
		}
	}

	/**
	 * Tests that inputs of a parameter without a conversion reading them are rejected, for a
	 * placeholder parameter carried as text as well as for a path parameter.
	 */
	public void testInputsWithoutConversionRejected() throws Exception {
		for (String route : List.of("item/:item", "items")) {
			FrameRouteConfig frame = parse("<frame id='item' route='" + route + "' view='item.view.xml'>"
				+ "<param name='item' inputs='" + TYPE + "'/>"
				+ "</frame>");
			try {
				FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
				fail("Inputs without a conversion must be rejected for the route '" + route + "'.");
			} catch (RuntimeException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains("item"));
				assertTrue(ex.getMessage(), ex.getMessage().contains(Inputs.INPUTS));
			}
		}
	}

	/**
	 * Tests that an input named like the value a script is supplied with is rejected.
	 */
	public void testInputNamedLikeValueRejected() throws Exception {
		FrameRouteConfig frame = parse("<frame id='item' route='item/:item' view='item.view.xml'>"
			+ "<param name='item' inputs='value' reverse='$value'/>"
			+ "</frame>");
		try {
			FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
			fail("An input named like the text of the URL must be rejected.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	/**
	 * Tests that a parameter naming no placeholder of the route is taken from the frames below on
	 * the path, from the nearest one holding it.
	 */
	public void testPathParam() throws Exception {
		FrameRouteConfig frame = parse("<frame id='item' route='items' view='items.view.xml'>"
			+ "<param name='item'/>"
			+ "</frame>");
		FrameRoute route = FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
		assertEquals(List.of("item"), route.pathParams());
		assertEquals("The path parameter does not reach the URL.", "items", route.segment(Map.of("item", "42"), Map.of()));

		RouteMatch match = route.pattern().match("items");
		assertNotNull(match);
		List<TileFrame> below = List.of(
			new TileFrame("a", "a.view.xml", null, Map.of("item", "1")),
			new TileFrame("b", "b.view.xml", null, Map.of("item", "42")),
			new TileFrame("c", "c.view.xml", null, Map.of("other", "7")));
		assertEquals("The nearest frame below holding the parameter gives its value.",
			Map.of("item", "42"), route.resolveParams(match, below, Map.of()));
		assertNull("Without a frame below holding the parameter, the frame is not restored.",
			route.resolveParams(match, List.of(new TileFrame("c", "c.view.xml", null, Map.of("other", "7"))),
				Map.of()));;
	}

	/**
	 * Tests that a conversion of a parameter that never reaches the URL is rejected.
	 */
	public void testPathParamConversionRejected() throws Exception {
		for (String attribute : List.of(FrameParamConfig.EXPR, FrameParamConfig.REVERSE)) {
			FrameRouteConfig frame = parse("<frame id='item' route='items' view='items.view.xml'>"
				+ "<param name='item' " + attribute + "='$value'/>"
				+ "</frame>");
			try {
				FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
				fail("A path parameter with '" + attribute + "' must be rejected.");
			} catch (RuntimeException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains("item"));
			}
		}
	}

	/**
	 * Tests that nested declarations and {@code <frame-ref>} entries are the successors of a
	 * declaration, a reference to an enclosing declaration included.
	 */
	public void testFrameGraph() throws Exception {
		FrameRouteConfig folder = parse("<frame id='folder' route='folder/:folder' view='folder.view.xml'>"
			+ "<frame-ref frame='folder'/>"
			+ "<frame id='file' route='file/:file' view='file.view.xml'/>"
			+ "</frame>");
		FrameRoute root = FrameRoute.createGraph(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			"files.view.xml", List.of(folder));

		assertEquals(1, root.successors().size());
		FrameRoute folderRoute = root.successors().get(0);
		assertEquals("folder", folderRoute.id());
		assertEquals(List.of("folder", "file"), folderRoute.successors().stream().map(FrameRoute::id).toList());
		assertSame("The reference names the enclosing declaration itself.", folderRoute,
			folderRoute.successors().get(0));
		assertEquals("A declaration without successors ends every path it is on.", List.of(),
			folderRoute.successors().get(1).successors());
	}

	/**
	 * Tests that a {@code <frame-ref>} naming no declaration of the stack is a configuration error.
	 */
	public void testUnknownFrameRefRejected() throws Exception {
		FrameRouteConfig frame = parse("<frame id='folder' route='folder/:folder' view='folder.view.xml'>"
			+ "<frame-ref frame='missing'/>"
			+ "</frame>");
		try {
			FrameRoute.createGraph(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, "files.view.xml",
				List.of(frame));
			fail("A reference to an undeclared frame must be rejected.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("missing"));
		}
	}

	/**
	 * Tests that an id declared twice in a stack is a configuration error.
	 */
	public void testDuplicateIdRejected() throws Exception {
		FrameRouteConfig frame = parse("<frame id='folder' route='folder/:folder' view='folder.view.xml'>"
			+ "<frame id='folder' route='sub/:folder' view='sub.view.xml'/>"
			+ "</frame>");
		try {
			FrameRoute.createGraph(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, "files.view.xml",
				List.of(frame));
			fail("An id declared twice must be rejected.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("folder"));
		}
	}

	/**
	 * Tests that a frame without a route has no address, but holds the parameters it declares.
	 */
	public void testFrameWithoutRoute() throws Exception {
		FrameRouteConfig frame = parse("<frame id='detail' view='detail.view.xml'><param name='item'/></frame>");
		FrameRoute route = FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);

		assertNull(route.pattern());
		assertNull(route.segment(Map.of("item", "1"), Map.of()));
		assertEquals(java.util.Set.of("item"), route.paramNames());
	}

	/**
	 * Tests that a parameter script declaring a parameter is rejected.
	 */
	public void testLambdaHeaderRejected() throws Exception {
		FrameRouteConfig frame = parse("<frame id='item' route='item/:item' view='item.view.xml'>"
			+ "<param name='item' expr='p -> $p'/>"
			+ "</frame>");
		try {
			FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
			fail("A lambda header must be rejected.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	/**
	 * A stack whose item frame names its item in the URL with the {@link #TYPE} of the embedding in
	 * front, and restores only items of that type.
	 */
	private static FrameRoute itemStack() throws ConfigurationException {
		return FrameRoute.createGraph(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, "items.view.xml",
			List.of(parse(TYPED_ITEM)));
	}

	/**
	 * The stack of the given declarations, displayed in a view whose {@link #TYPE} channel holds
	 * the given value.
	 */
	private static TileStackScope stack(FrameRoute root, String type) {
		return new TileStackScope(path(), root, null, null, null, declaringView(type));
	}

	private static ViewContext declaringView(String type) {
		ViewContext result = new DefaultViewContext(null);
		DefaultViewChannel channel = new DefaultViewChannel(TYPE);
		channel.set(type);
		result.registerChannel(TYPE, channel);
		return result;
	}

	private static DefaultViewChannel path() {
		DefaultViewChannel result = new DefaultViewChannel("navPath");
		result.set(List.of());
		return result;
	}

	/**
	 * Restores the path of the given stack from the given address and returns the parameters of
	 * its frames.
	 */
	private static List<Map<String, Object>> adopt(TileStackScope stack, String address) {
		TileFrameRouteParticipant participant = new TileFrameRouteParticipant(stack, new RouteManager());
		String rest = address;
		while (!rest.isEmpty()) {
			RouteMatch match = null;
			for (RoutePattern pattern : participant.declaredRoutes()) {
				match = pattern.match(rest);
				if (match != null) {
					break;
				}
			}
			if (match == null) {
				break;
			}
			rest = match.remainingPath();
			participant.activateRoute(match);
		}
		return stack.getPath().stream().map(TileFrame::getParams).toList();
	}

	private static void assertScope(List<String> expected, ConfigurationItem config, String property) {
		assertEquals("Editor scope of '" + property + "'.", expected, ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property))));
		assertEquals("Runtime scope of '" + property + "'.", expected,
			InputScript.variables(config, config.descriptor().getProperty(property)));
	}

	private static FrameRouteConfig parse(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFrameParamScripts.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap(FRAME, TypedConfiguration.getConfigurationDescriptor(FrameRouteConfig.class)));
		reader.setSource(CharacterContents.newContent(xml));
		FrameRouteConfig result = (FrameRouteConfig) reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled and evaluated against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFrameParamScripts.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
