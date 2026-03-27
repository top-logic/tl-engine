/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.TreeConnection;
import com.top_logic.react.flow.data.TreeConnector;
import com.top_logic.react.flow.data.TreeLayout;

/**
 * A simple test {@link FlowChartBuilder} that creates a three-node tree diagram for end-to-end
 * validation.
 *
 * <p>
 * The diagram has the following structure:
 * </p>
 *
 * <pre>
 *         ┌───────────┐
 *         │  Parent   │
 *         └─────┬─────┘
 *         ┌─────┴─────┐
 *    ┌────┴────┐  ┌───┴─────┐
 *    │ Child A │  │ Child B │
 *    └─────────┘  └─────────┘
 * </pre>
 */
public class TestFlowChartBuilder extends AbstractConfiguredInstance<TestFlowChartBuilder.Config>
		implements FlowChartBuilder {

	/**
	 * Configuration for {@link TestFlowChartBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<TestFlowChartBuilder> {

		@Override
		@ClassDefault(TestFlowChartBuilder.class)
		Class<? extends TestFlowChartBuilder> getImplementationClass();

	}

	/**
	 * Creates a {@link TestFlowChartBuilder} from configuration.
	 *
	 * @param context
	 *        The instantiation context.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TestFlowChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public Diagram getModel(Object businessModel, LayoutComponent aComponent) {
		Box parent = node("Parent");
		Box childA = node("Child A");
		Box childB = node("Child B");

		return Diagram.create().setRoot(
			Padding.create().setAll(20).setContent(
				TreeLayout.create()
					.addNode(parent)
					.addNode(childA)
					.addNode(childB)
					.addConnection(TreeConnection.create()
						.setParent(connector(parent))
						.setChild(connector(childA)))
					.addConnection(TreeConnection.create()
						.setParent(connector(parent))
						.setChild(connector(childB)))));
	}

	private static Box node(String label) {
		return Border.create().setContent(
			Padding.create().setAll(5).setContent(
				Text.create().setValue(label)));
	}

	private static TreeConnector connector(Box anchor) {
		return TreeConnector.create().setAnchor(anchor).setConnectPosition(0.5);
	}

}
