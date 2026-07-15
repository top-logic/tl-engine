/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link UIElement} that renders a Chart.js chart via the React control layer.
 *
 * <p>
 * The chart data is computed by a TL-Script expression ({@link Config#getData()}) that
 * receives the input channel values as arguments. The expression returns a Chart.js
 * configuration map that may contain TL objects in {@code datasets[].metadata}.
 * </p>
 *
 * @see ReactChartJsControl
 */
public class ChartElement implements UIElement {

	/**
	 * Configuration for {@link ChartElement}.
	 */
	@TagName("chart")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ChartElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getData()}. */
		String DATA = "data";

		/** Configuration name for {@link #getHandlers()}. */
		String HANDLERS = "handlers";

		/** Configuration name for {@link #getTooltips()}. */
		String TOOLTIPS = "tooltips";

		/** Configuration name for {@link #getZoomEnabled()}. */
		String ZOOM_ENABLED = "zoomEnabled";

		/** Configuration name for {@link #getCSSClass()}. */
		String CSS_CLASS = "cssClass";

		/** Configuration name for {@link #getNoDataMessage()}. */
		String NO_DATA_MESSAGE = "noDataMessage";

		/**
		 * Input channels whose values are passed as arguments to the {@link #getData()} function.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script expression producing the Chart.js configuration map.
		 *
		 * <p>
		 * Receives as many arguments as {@link #getInputs() input channels} are declared.
		 * Must return a map with keys {@code type}, {@code data}, and optionally {@code options}.
		 * Datasets may contain {@code metadata} arrays with TL objects, and {@code onClick},
		 * {@code onLegendClick}, {@code tooltip} string keys referencing configured handlers.
		 * </p>
		 */
		@Mandatory
		@Name(DATA)
		Expr getData();

		/**
		 * Named click handlers. Each handler has a {@link ClickHandlerConfig#getName() name}
		 * that is referenced from dataset {@code onClick} / {@code onLegendClick} keys.
		 */
		@Name(HANDLERS)
		@EntryTag("handler")
		List<ClickHandlerConfig> getHandlers();

		/**
		 * Named tooltip providers. Each provider has a {@link TooltipProviderConfig#getName() name}
		 * that is referenced from dataset {@code tooltip} keys.
		 */
		@Name(TOOLTIPS)
		@EntryTag("tooltip")
		List<TooltipProviderConfig> getTooltips();

		/**
		 * Whether zoom and pan interaction is enabled.
		 */
		@Name(ZOOM_ENABLED)
		@BooleanDefault(false)
		boolean getZoomEnabled();

		/**
		 * Additional CSS class for the chart container element.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCSSClass();

		/**
		 * Message shown when the chart has no data. If {@code null}, an empty chart is shown.
		 */
		@Name(NO_DATA_MESSAGE)
		@Nullable
		ResKey getNoDataMessage();
	}

	private final QueryExecutor _dataFun;

	private final Map<String, ClickHandlerConfig> _handlers;

	private final Map<String, QueryExecutor> _tooltips;

	private final List<ChannelRef> _inputs;

	private final boolean _zoomEnabled;

	private final String _cssClass;

	private final ResKey _noDataMessage;

	/**
	 * Creates a new {@link ChartElement} from configuration.
	 */
	@CalledByReflection
	public ChartElement(InstantiationContext context, Config config) {
		_dataFun = QueryExecutor.compile(config.getData());
		_inputs = config.getInputs();
		_zoomEnabled = config.getZoomEnabled();
		_cssClass = config.getCSSClass();
		_noDataMessage = config.getNoDataMessage();

		_handlers = new HashMap<>();
		for (ClickHandlerConfig handler : config.getHandlers()) {
			_handlers.put(handler.getName(), handler);
		}

		_tooltips = new HashMap<>();
		for (TooltipProviderConfig tooltip : config.getTooltips()) {
			_tooltips.put(tooltip.getName(), QueryExecutor.compile(tooltip.getExpr()));
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> channels = _inputs.stream()
			.map(ref -> context.resolveChannel(ref))
			.toList();

		Object[] inputValues = channels.stream()
			.map(ViewChannel::get)
			.toArray();

		ReactChartJsControl control = new ReactChartJsControl(
			context, _dataFun, _handlers, _tooltips,
			_zoomEnabled, _cssClass, _noDataMessage,
			inputValues);

		// Re-evaluate on channel changes.
		for (ViewChannel channel : channels) {
			ViewChannel.ChannelListener listener = (sender, oldVal, newVal) -> {
				Object[] values = channels.stream().map(ViewChannel::get).toArray();
				control.updateChartData(values);
			};
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}

		return control;
	}
}
