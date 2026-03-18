/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ReactControl} that renders a Chart.js chart via the {@code TLChart} React component.
 *
 * <p>
 * Manages the server-side state including metadata lookup tables, handler mappings,
 * and tooltip providers. Communicates with the client via SSE state/patch events.
 * </p>
 *
 * @see ChartElement
 * @see ChartConfigExtractor
 */
public class ReactChartJsControl extends ReactControl {

	private static final String CHART_CONFIG = "chartConfig";

	private static final String INTERACTIONS = "interactions";

	private static final String THEME_COLORS = "themeColors";

	private static final String ZOOM_ENABLED = "zoomEnabled";

	private static final String CSS_CLASS = "cssClass";

	private static final String ERROR = "error";

	private static final String NO_DATA_MESSAGE = "noDataMessage";

	private static final String TOOLTIP_CONTENT = "tooltipContent";

	private final QueryExecutor _dataFun;

	private final Map<String, ClickHandlerConfig> _handlers;

	private final Map<String, QueryExecutor> _tooltips;

	private final ResKey _noDataMessage;

	/** Atomic snapshot of per-render metadata, handler and tooltip references. */
	private volatile ChartDataSnapshot _snapshot = ChartDataSnapshot.EMPTY;

	private static final class ChartDataSnapshot {
		static final ChartDataSnapshot EMPTY = new ChartDataSnapshot(
			Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

		final List<List<TLObject>> _metadata;
		final List<Map<String, String>> _handlerRefs;
		final List<String> _tooltipRefs;

		ChartDataSnapshot(List<List<TLObject>> metadata,
				List<Map<String, String>> handlerRefs,
				List<String> tooltipRefs) {
			_metadata = metadata;
			_handlerRefs = handlerRefs;
			_tooltipRefs = tooltipRefs;
		}
	}

	/**
	 * Creates a new {@link ReactChartJsControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param dataFun
	 *        Compiled TL-Script expression producing the chart configuration.
	 * @param handlers
	 *        Named click handler configurations keyed by name.
	 * @param tooltips
	 *        Named tooltip expressions keyed by name.
	 * @param zoomEnabled
	 *        Whether zoom/pan interaction is enabled.
	 * @param cssClass
	 *        Optional extra CSS class for the chart container.
	 * @param noDataMessage
	 *        Message shown when there is no chart data, or {@code null}.
	 * @param initialInputValues
	 *        The initial input channel values to evaluate the data function with.
	 */
	public ReactChartJsControl(ReactContext context, QueryExecutor dataFun,
			Map<String, ClickHandlerConfig> handlers, Map<String, QueryExecutor> tooltips,
			boolean zoomEnabled, String cssClass, ResKey noDataMessage,
			Object[] initialInputValues) {
		super(context, null, "TLChart");

		_dataFun = dataFun;
		_handlers = handlers;
		_tooltips = tooltips;
		_noDataMessage = noDataMessage;

		putState(ZOOM_ENABLED, zoomEnabled);
		if (cssClass != null) {
			putState(CSS_CLASS, cssClass);
		}

		putState(THEME_COLORS, Icons.getChartThemeColors());

		evaluateAndSetChartData(initialInputValues);
	}

	/**
	 * Re-evaluates the TL-Script data function and updates the chart state.
	 *
	 * <p>
	 * Called when input channels change.
	 * </p>
	 */
	public void updateChartData(Object[] inputValues) {
		evaluateAndSetChartData(inputValues);
	}

	private void evaluateAndSetChartData(Object[] inputValues) {
		try {
			Object rawConfig = _dataFun.execute(inputValues);

			ChartConfigExtractor extractor =
				new ChartConfigExtractor(_handlers, _tooltips.keySet());
			extractor.extract(rawConfig);

			_snapshot = new ChartDataSnapshot(
				extractor.getMetadata(),
				extractor.getHandlerRefs(),
				extractor.getTooltipRefs());

			putState(CHART_CONFIG, extractor.getCleanConfig());
			putState(INTERACTIONS, extractor.buildInteractions());
			putState(ERROR, null);

			if (_noDataMessage != null) {
				putState(NO_DATA_MESSAGE, Resources.getInstance().getString(_noDataMessage));
			}
		} catch (TopLogicException ex) {
			putState(ERROR, ex.getMessage());
			putState(CHART_CONFIG, null);
		}
	}

	/**
	 * Handles click events from the React component.
	 *
	 * @param arguments
	 *        Contains {@code datasetIndex} (int), {@code index} (int), and
	 *        {@code zone} ("datapoint" or "legend").
	 */
	@ReactCommand("elementClick")
	HandlerResult handleElementClick(Map<String, Object> arguments) {
		int datasetIndex = ((Number) arguments.get("datasetIndex")).intValue();
		int index = ((Number) arguments.get("index")).intValue();
		String zone = (String) arguments.get("zone");

		String handlerKey = "legend".equals(zone) ? "onLegendClick" : "onClick";

		ChartDataSnapshot snapshot = _snapshot;
		if (datasetIndex < 0 || datasetIndex >= snapshot._handlerRefs.size()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		Map<String, String> dsHandlers = snapshot._handlerRefs.get(datasetIndex);
		String handlerName = dsHandlers.get(handlerKey);
		if (handlerName == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ClickHandlerConfig handlerConfig = _handlers.get(handlerName);
		if (handlerConfig == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		TLObject targetObject = resolveMetadata(snapshot, datasetIndex, index);

		try {
			ViewCommand command = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
				.getInstance(handlerConfig.getAction());
			return command.execute(getReactContext(), targetObject);
		} catch (RuntimeException ex) {
			putState(ERROR, ex.getMessage());
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Handles tooltip requests from the React component.
	 *
	 * @param arguments
	 *        Contains {@code datasetIndex} (int) and {@code index} (int).
	 */
	@ReactCommand("tooltip")
	void handleTooltip(Map<String, Object> arguments) {
		int datasetIndex = ((Number) arguments.get("datasetIndex")).intValue();
		int index = ((Number) arguments.get("index")).intValue();

		ChartDataSnapshot snapshot = _snapshot;
		if (datasetIndex < 0 || datasetIndex >= snapshot._tooltipRefs.size()) {
			return;
		}

		String tooltipName = snapshot._tooltipRefs.get(datasetIndex);
		if (tooltipName == null) {
			return;
		}

		QueryExecutor tooltipExpr = _tooltips.get(tooltipName);
		if (tooltipExpr == null) {
			return;
		}

		TLObject targetObject = resolveMetadata(snapshot, datasetIndex, index);
		if (targetObject == null) {
			return;
		}

		Object html = tooltipExpr.execute(targetObject);
		if (html != null) {
			putState(TOOLTIP_CONTENT, Map.of(
				"html", html.toString(),
				"datasetIndex", datasetIndex,
				"index", index
			));
		}
	}

	private TLObject resolveMetadata(ChartDataSnapshot snapshot, int datasetIndex, int dataIndex) {
		if (datasetIndex < 0 || datasetIndex >= snapshot._metadata.size()) {
			return null;
		}
		List<TLObject> dsMetadata = snapshot._metadata.get(datasetIndex);
		if (dataIndex < 0 || dataIndex >= dsMetadata.size()) {
			return null;
		}
		return dsMetadata.get(dataIndex);
	}
}
