/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.VetoForwarder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewScripts;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;
import com.top_logic.react.flow.callback.DiagramHandler;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.server.control.DiagramSelectionBinding;
import com.top_logic.react.flow.server.control.FlowDiagramControl;

/**
 * {@link UIElement} for embedding a flow diagram in the React View framework.
 *
 * <p>
 * The diagram is created by evaluating a TL-Script expression ({@link Config#getCreateChart()})
 * that sees the current values of the configured {@link Config#getInputs() input channels} and the
 * configured {@link Config#getHandlers() handlers} as variables.
 * </p>
 */
@InApp
public class FlowDiagramElement implements UIElement {

	/**
	 * Configuration for {@link FlowDiagramElement}.
	 */
	@TagName("flow-diagram")
	public interface Config extends UIElement.Config, Inputs {

		@Override
		@ClassDefault(FlowDiagramElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getCreateChart()}. */
		String CREATE_CHART = "createChart";

		/** Configuration name for {@link #getHandlers()}. */
		String HANDLERS = "handlers";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/**
		 * TL-Script expression that creates a {@link Diagram}.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, followed by the {@link #getHandlers()
		 * handlers}, each as the variable named like the handler. An input named like a handler is
		 * a configuration error and is renamed with {@code as}.
		 * </p>
		 */
		@Name(CREATE_CHART)
		@NonNullable
		@FormattedDefault("flowChart()")
		@ScriptContextVariables(ScriptVariables.class)
		Expr getCreateChart();

		/**
		 * Specification of interactions with the diagram contents.
		 *
		 * <p>
		 * The handlers defined here can be bound to diagram elements in the
		 * {@link #getCreateChart() create chart script} by referencing them as implicitly defined
		 * variables.
		 * </p>
		 */
		@Name(HANDLERS)
		@Key(HandlerDefinition.NAME_ATTRIBUTE)
		Map<String, HandlerDefinition<? extends DiagramHandler>> getHandlers();

		/**
		 * Optional reference to the {@link ViewChannel} holding the selection the diagram shares
		 * with the other elements of the view.
		 *
		 * <p>
		 * The diagram follows the channel in both directions. A selection made in the diagram
		 * becomes the value: one selected node as its user object, several as the set of them, none
		 * as no value at all. A value written by another element marks the nodes carrying it as
		 * user object - a single object, or each of the objects of a set in a diagram that shows
		 * several selected nodes.
		 * </p>
		 *
		 * <p>
		 * A value no node of this diagram carries is shown as no selection and left alone: it is
		 * the selection of whoever wrote it, an object of a different set of elements, which this
		 * diagram simply has nothing to mark for.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Common super-interface for configurations of a {@link DiagramHandler}.
		 */
		@Abstract
		interface HandlerDefinition<T extends DiagramHandler>
				extends PolymorphicConfiguration<T>, NamedConfigMandatory {
			/**
			 * Unique name of the defined handler.
			 *
			 * <p>
			 * The defined handler is available to the {@link Config#getCreateChart() create chart
			 * script} as an implicitly defined variable with that name. When defining a handler
			 * with name <code>onClick</code>, this handler is available to the script as variable
			 * <code>$onClick</code>.
			 * </p>
			 */
			@Override
			String getName();
		}
	}

	private final Config _config;

	private final QueryExecutor _createChart;

	private final Object[] _handlers;

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

		Map<String, DiagramHandler> handlerMap =
			TypedConfiguration.getInstanceMap(context, config.getHandlers());
		List<String> handlerNames = handlerNames(config);
		_handlers = handlerNames.stream().map(handlerMap::get).toArray();

		InputScript.checkInputNames(config.getInputs(), handlerNames);
		_createChart = ViewScripts.compile(config.getCreateChart(), variables(config));
	}

	/**
	 * The variables the {@link Config#getCreateChart() create chart script} of the given
	 * configuration sees.
	 *
	 * @return The names of the {@link Config#getInputs() inputs}, followed by the names of the
	 *         {@link Config#getHandlers() handlers}.
	 */
	public static List<String> variables(Config config) {
		List<String> result = new ArrayList<>(InputRef.names(config.getInputs()));
		result.addAll(handlerNames(config));
		return result;
	}

	private static List<String> handlerNames(Config config) {
		return new ArrayList<>(config.getHandlers().keySet());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());

		// 2. Build initial diagram from current channel values.
		Diagram diagram = buildDiagram(inputChannels);

		// 3. Create FlowDiagramControl.
		FlowDiagramControl control = new FlowDiagramControl(context, diagram);
		control.setCssClass(_config.getCssClass());

		// 4. Listen for input channel changes and rebuild diagram.
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> {
			Diagram newDiagram = buildDiagram(inputChannels);
			control.setModel(newDiagram);
		};
		for (ViewChannel channel : inputChannels) {
			channel.addListener(listener);
		}

		// 5. Wire selection channel if configured.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			DiagramSelectionBinding selectionBinding = new DiagramSelectionBinding(control, selectionChannel);
			control.addCleanupAction(selectionBinding::dispose);

			// Rebuilding the diagram drops the selection, so the unsaved changes blocking the
			// selection are reported when an input is asked, before the input is written.
			for (ViewChannel channel : inputChannels) {
				control.addCleanupAction(VetoForwarder.forward(channel, selectionChannel));
			}
		}

		return control;
	}

	private Diagram buildDiagram(List<ViewChannel> inputChannels) {
		Object result = _createChart.execute(ChannelInputs.arguments(inputChannels, _handlers));
		if (result instanceof Diagram) {
			return (Diagram) result;
		}
		if (result instanceof Box) {
			return Diagram.create().setRoot((Box) result);
		}
		return Diagram.create();
	}

	/**
	 * {@link ScriptContextVariablesProvider} offering the {@link FlowDiagramElement#variables(Config)
	 * variables} of the {@link Config#getCreateChart() create chart script}.
	 */
	public static class ScriptVariables implements ScriptContextVariablesProvider {

		@Override
		public List<String> getVariables(ValueModel valueModel) {
			return variables((Config) valueModel.getModel());
		}
	}

}
