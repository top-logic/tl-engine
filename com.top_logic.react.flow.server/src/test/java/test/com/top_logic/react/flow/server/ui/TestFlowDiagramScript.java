/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.ui;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.react.flow.callback.DiagramHandler;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.control.FlowDiagramControl;
import com.top_logic.react.flow.server.ui.FlowDiagramElement;
import com.top_logic.util.model.ModelService;

/**
 * Tests the create chart script of a {@link FlowDiagramElement flow diagram}, which sees the
 * inputs and the handlers of the diagram as variables.
 */
public class TestFlowDiagramScript extends BasicTestCase {

	private static final String FLOW_DIAGRAM = "flow-diagram";

	/**
	 * Tests that the script sees the inputs followed by the handlers, in the editor and at runtime.
	 */
	public void testInputsAndHandlers() throws Exception {
		FlowDiagramElement.Config config = parse("<flow-diagram inputs='selection as product'>"
			+ "<handlers><handler name='onClick' class='" + TestHandler.class.getName() + "'/></handlers>"
			+ "<createChart>reactFlowText($product + ':' + if($onClick == null, 'none', 'handler'))</createChart>"
			+ "</flow-diagram>");
		List<String> expected = List.of("product", "onClick");
		assertEquals("Editor scope.", expected, ScriptContextVariablesResolver.resolve(
			new DefaultValueModel(config, config.descriptor().getProperty(FlowDiagramElement.Config.CREATE_CHART))));
		assertEquals("Runtime scope.", expected, FlowDiagramElement.variables(config));

		ViewContext context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		DefaultViewChannel selection = new DefaultViewChannel("selection");
		context.registerChannel("selection", selection);
		selection.set("p1");

		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestFlowDiagramScript.class);
		FlowDiagramElement element = (FlowDiagramElement) instantiation.getInstance(config);
		instantiation.checkErrors();
		FlowDiagramControl control = (FlowDiagramControl) element.createControl(context);
		assertEquals("p1:handler", ((Text) control.getModel().getRoot()).getValue());

		selection.set("p2");
		assertEquals("The diagram is built anew from the input.", "p2:handler",
			((Text) control.getModel().getRoot()).getValue());
	}

	/**
	 * Tests that a script declaring a parameter, and an input named like a handler, are rejected.
	 */
	public void testRejected() throws Exception {
		assertRejected("<flow-diagram inputs='selection'><createChart>s -> $s</createChart></flow-diagram>");
		assertRejected("<flow-diagram inputs='selection as onClick'>"
			+ "<handlers><handler name='onClick' class='" + TestHandler.class.getName() + "'/></handlers>"
			+ "<createChart>$onClick</createChart></flow-diagram>");
	}

	private static void assertRejected(String xml) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(log);
		try {
			instantiation.getInstance(parse(xml));
		} catch (RuntimeException ex) {
			return;
		}
		assertTrue("The configuration must be rejected: " + xml, log.hasErrors());
	}

	private static FlowDiagramElement.Config parse(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFlowDiagramScript.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(FLOW_DIAGRAM,
			TypedConfiguration.getConfigurationDescriptor(FlowDiagramElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		FlowDiagramElement.Config result = (FlowDiagramElement.Config) reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * {@link DiagramHandler} without behavior, standing for a configured handler.
	 */
	public static class TestHandler implements DiagramHandler {

		/**
		 * Configuration of a {@link TestHandler}.
		 */
		public interface Config<I extends TestHandler> extends FlowDiagramElement.Config.HandlerDefinition<I> {

			@Override
			@ClassDefault(TestHandler.class)
			Class<? extends I> getImplementationClass();
		}

		/**
		 * Creates a {@link TestHandler} from configuration.
		 */
		public TestHandler(InstantiationContext context, Config<?> config) {
			// No state.
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The script is compiled and evaluated against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFlowDiagramScript.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
