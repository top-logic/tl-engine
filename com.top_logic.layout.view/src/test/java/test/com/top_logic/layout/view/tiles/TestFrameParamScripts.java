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
		FrameRouteConfig frame = parse("<frame route='item/:item' view='item.view.xml'>"
			+ "<param name='item' expr=\"'id-' + $item\" reverse=\"$value + '!'\"/>"
			+ "</frame>");
		FrameParamConfig param = frame.getParams().get(0);
		assertScope(List.of("item"), param, FrameParamConfig.EXPR);
		assertScope(List.of(FrameParamConfig.VALUE), param, FrameParamConfig.REVERSE);

		FrameRoute route = FrameRoute.create(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, frame);
		assertEquals("item/id-42", route.segment(Map.of("item", "42")));
		RouteMatch match = route.pattern().match("item/id-42");
		assertNotNull(match);
		assertEquals(Map.of("item", "id-42!"), route.resolveParams(match));
	}

	/**
	 * Tests that a parameter script declaring a parameter is rejected.
	 */
	public void testLambdaHeaderRejected() throws Exception {
		FrameRouteConfig frame = parse("<frame route='item/:item' view='item.view.xml'>"
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
