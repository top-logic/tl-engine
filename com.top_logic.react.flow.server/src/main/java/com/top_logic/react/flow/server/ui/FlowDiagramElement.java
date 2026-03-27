/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.server.control.FlowDiagramControl;

/**
 * {@link UIElement} for embedding a flow diagram in the React View framework.
 *
 * <p>
 * Usage in view.xml:
 * </p>
 *
 * <pre>
 * &lt;flow-diagram builder="com.example.MyFlowChartBuilder" /&gt;
 * </pre>
 *
 * <p>
 * The configured {@link FlowChartBuilder} is instantiated once and shared across all sessions. Each
 * session gets its own {@link FlowDiagramControl} via {@link #createControl(ViewContext)}.
 * </p>
 *
 * <p>
 * Note: The {@link FlowChartBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)}
 * API currently requires a {@link com.top_logic.mig.html.layout.LayoutComponent}, which is not
 * available in the View framework. Until the builder API is adapted, the diagram is created empty
 * and must be populated by the application via {@link FlowDiagramControl#setModel(Diagram)}.
 * </p>
 */
public class FlowDiagramElement implements UIElement {

	/**
	 * Configuration for {@link FlowDiagramElement}.
	 */
	@TagName("flow-diagram")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(FlowDiagramElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getBuilder()}. */
		String BUILDER = "builder";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/**
		 * The builder that creates the diagram model from the business object.
		 *
		 * <p>
		 * The builder is instantiated once and shared across all sessions. It must be stateless.
		 * </p>
		 */
		@Name(BUILDER)
		@Mandatory
		PolymorphicConfiguration<FlowChartBuilder> getBuilder();

		/**
		 * Optional reference to a {@link ViewChannel} to write the selected node's user object to.
		 *
		 * <p>
		 * When a node is selected in the diagram, its user object is written to the referenced
		 * channel. Other view elements (forms, tables) can observe this channel to react to
		 * selection changes.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	private final FlowChartBuilder _builder;

	/**
	 * Creates a {@link FlowDiagramElement} from configuration.
	 *
	 * @param context
	 *        The instantiation context for resolving nested configuration.
	 * @param config
	 *        The configuration for this element.
	 */
	@CalledByReflection
	public FlowDiagramElement(InstantiationContext context, Config config) {
		_config = config;
		_builder = context.getInstance(config.getBuilder());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// TODO: The FlowChartBuilder API currently takes (Object, LayoutComponent).
		// The LayoutComponent is not available in the View framework. Pass nulls for now.
		// The builder API will need adaptation to work with ViewContext in a later iteration.
		Diagram diagram;
		if (_builder != null) {
			diagram = _builder.getModel(null, null);
		} else {
			diagram = Diagram.create();
		}
		FlowDiagramControl control = new FlowDiagramControl(context, diagram);

		// Bind selection channel if configured.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			control.setSelectionChannel(selectionChannel);
		}

		return control;
	}

}
