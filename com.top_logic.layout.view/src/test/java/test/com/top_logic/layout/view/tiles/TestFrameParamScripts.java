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
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.tiles.FrameParamConfig;
import com.top_logic.layout.view.tiles.FrameRoute;
import com.top_logic.layout.view.tiles.FrameRouteConfig;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.model.ModelService;

/**
 * Tests the conversion scripts of a {@link FrameParamConfig route parameter}: the value of the
 * parameter is named like the parameter, the text of the URL is {@value FrameParamConfig#VALUE}.
 */
public class TestFrameParamScripts extends BasicTestCase {

	private static final String FRAME = "frame";

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
		assertEquals("item/id-42", route.segment(Map.of("item", "42")));
		RouteMatch match = route.pattern().match("item/id-42");
		assertNotNull(match);
		assertEquals(Map.of("item", "id-42!"), route.resolveParams(match, List.of()));
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
		assertEquals("The path parameter does not reach the URL.", "items", route.segment(Map.of("item", "42")));

		RouteMatch match = route.pattern().match("items");
		assertNotNull(match);
		List<TileFrame> below = List.of(
			new TileFrame("a", "a.view.xml", null, Map.of("item", "1")),
			new TileFrame("b", "b.view.xml", null, Map.of("item", "42")),
			new TileFrame("c", "c.view.xml", null, Map.of("other", "7")));
		assertEquals("The nearest frame below holding the parameter gives its value.",
			Map.of("item", "42"), route.resolveParams(match, below));
		assertNull("Without a frame below holding the parameter, the frame is not restored.",
			route.resolveParams(match, List.of(new TileFrame("c", "c.view.xml", null, Map.of("other", "7")))));
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
		assertNull(route.segment(Map.of("item", "1")));
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
