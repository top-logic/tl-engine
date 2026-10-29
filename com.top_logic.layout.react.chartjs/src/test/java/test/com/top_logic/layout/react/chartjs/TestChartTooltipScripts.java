/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.chartjs;

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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.chartjs.ChartElement;
import com.top_logic.layout.react.chartjs.ReactChartJsControl;
import com.top_logic.layout.react.chartjs.TooltipProviderConfig;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.model.ModelService;

/**
 * Tests the tooltip scripts of a {@link ChartElement chart}, which see the inputs of the chart and
 * the metadata object of the data point as variables.
 */
public class TestChartTooltipScripts extends BasicTestCase {

	private static final String CHART = "chart";

	/** Name of the state key the control publishes the tooltip under. */
	private static final String TOOLTIP_CONTENT = "tooltipContent";

	/**
	 * Tests the variables of a tooltip script, in the editor and at runtime.
	 */
	public void testTooltipVariables() throws Exception {
		ChartElement.Config chart = parse("<chart inputs='factor as f' data=\"{'type': 'bar', 'data': {"
			+ "'labels': ['a'], 'datasets': [{'data': [1], 'metadata': ['m'], 'tooltip': 'named'},"
			+ " {'data': [2], 'metadata': ['n'], 'tooltip': 'default'}]}}\">"
			+ "<tooltips>"
			+ "<tooltip name='named' as='point' expr=\"$f + ':' + $point\"/>"
			+ "<tooltip name='default' expr=\"$f + '/' + $element\"/>"
			+ "</tooltips></chart>");
		TooltipProviderConfig named = chart.getTooltips().get(0);
		TooltipProviderConfig unnamed = chart.getTooltips().get(1);
		assertScope(List.of("f", "point"), named, TooltipProviderConfig.EXPR);
		assertScope(List.of("f", TooltipProviderConfig.DEFAULT_AS), unnamed, TooltipProviderConfig.EXPR);

		ViewContext context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		DefaultViewChannel factor = new DefaultViewChannel("factor");
		context.registerChannel("factor", factor);
		factor.set("x");

		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestChartTooltipScripts.class);
		ChartElement element = (ChartElement) instantiation.getInstance(chart);
		instantiation.checkErrors();
		ReactChartJsControl control = (ReactChartJsControl) element.createControl(context);

		assertEquals("x:m", tooltip(control, 0));

		factor.set("y");
		assertEquals("The tooltip sees the current value of the input.", "y/n", tooltip(control, 1));
	}

	private static Object tooltip(ReactChartJsControl control, int datasetIndex) {
		control.executeCommand("tooltip", Map.of("datasetIndex", datasetIndex, "index", 0));
		Map<?, ?> content = (Map<?, ?>) control.scriptingScalarState().get(TOOLTIP_CONTENT);
		assertNotNull("A tooltip is published.", content);
		return content.get("html");
	}

	private static void assertScope(List<String> expected, ConfigurationItem config, String property) {
		assertEquals("Editor scope of '" + property + "'.", expected, ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property))));
		assertEquals("Runtime scope of '" + property + "'.", expected,
			InputScript.variables(config, config.descriptor().getProperty(property)));
	}

	private static ChartElement.Config parse(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChartTooltipScripts.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap(CHART, TypedConfiguration.getConfigurationDescriptor(ChartElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ChartElement.Config result = (ChartElement.Config) reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled and evaluated against the application model, the chart
	 *           control reads its colors from the theme.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestChartTooltipScripts.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}
}
