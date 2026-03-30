/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.react.flow.callback.DiagramHandler;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.server.control.FlowDiagramControl;
import com.top_logic.util.model.ModelService;

/**
 * {@link UIElement} for embedding a flow diagram in the React View framework.
 *
 * <p>
 * The diagram is created by evaluating a TL-Script expression ({@link Config#getCreateChart()})
 * with the current values of the configured {@link Config#getInputs() input channels} as positional
 * arguments. Diagram handlers are injected as implicit variables into the script.
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

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getCreateChart()}. */
		String CREATE_CHART = "createChart";

		/** Configuration name for {@link #getObserved()}. */
		String OBSERVED = "observed";

		/** Configuration name for {@link #getHandlers()}. */
		String HANDLERS = "handlers";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/**
		 * References to {@link ViewChannel}s whose current values become positional arguments to
		 * the {@link #getCreateChart()} expression.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script expression that creates a {@link Diagram}.
		 *
		 * <p>
		 * Takes the input channel values as positional arguments. Handler variables are available
		 * as implicitly defined variables.
		 * </p>
		 */
		@Name(CREATE_CHART)
		@NonNullable
		@FormattedDefault("flowChart()")
		Expr getCreateChart();

		/**
		 * Optional TL-Script function for determining the business objects to observe for a given
		 * diagram element's user object.
		 *
		 * <p>
		 * The function receives a diagram element as first argument and the component's model as
		 * second argument.
		 * </p>
		 */
		@Name(OBSERVED)
		Expr getObserved();

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

	private final QueryExecutor _getObserved;

	private final List<DiagramHandler> _handlers;

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

		SearchExpression createChart =
			SearchBuilder.toSearchExpression(ModelService.getApplicationModel(), config.getCreateChart());

		// Inject handler variables as lambda wrappers.
		_handlers = new ArrayList<>();
		for (Entry<String, DiagramHandler> handler : handlerMap.entrySet()) {
			createChart = lambda(handler.getKey(), createChart);
			_handlers.add(handler.getValue());
		}

		_createChart = QueryExecutor.compile(createChart);
		_getObserved = QueryExecutor.compileOptional(config.getObserved());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ChannelRef> inputRefs = _config.getInputs();
		List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
		for (ChannelRef ref : inputRefs) {
			inputChannels.add(context.resolveChannel(ref));
		}

		// 2. Read channel values.
		Object[] channelValues = readChannelValues(inputChannels);

		// 3. Build Args: handlers prepended, then channel values.
		Args args = Args.some(channelValues);
		for (DiagramHandler handler : _handlers) {
			args = Args.cons(handler, args);
		}

		// 4. Execute createChart expression.
		Diagram diagram;
		Object result = _createChart.executeWith(args);
		if (result instanceof Diagram) {
			diagram = (Diagram) result;
		} else {
			diagram = Diagram.create();
		}

		// 5. Create FlowDiagramControl.
		FlowDiagramControl control = new FlowDiagramControl(context, diagram);

		// 6. Wire selection channel if configured.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			control.setSelectionChannel(selectionChannel);
		}

		return control;
	}

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int i = 0; i < channels.size(); i++) {
			values[i] = channels.get(i).get();
		}
		return values;
	}

}
