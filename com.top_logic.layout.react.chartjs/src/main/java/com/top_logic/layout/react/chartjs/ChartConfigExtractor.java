/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Extracts metadata and handler/tooltip references from a TL-Script-produced
 * Chart.js configuration, producing clean JSON for the client.
 *
 * <p>
 * The TL-Script data function may return datasets containing:
 * </p>
 * <ul>
 *   <li>{@code metadata} — array of arbitrary objects parallel to the data array</li>
 *   <li>{@code onClick} — string referencing a configured click handler name</li>
 *   <li>{@code onLegendClick} — string referencing a configured click handler name</li>
 *   <li>{@code tooltip} — string referencing a configured tooltip provider name</li>
 * </ul>
 *
 * <p>
 * After extraction, these keys are removed from the config. Any TLObject found
 * outside of {@code datasets[].metadata} causes a {@link TopLogicException}.
 * </p>
 */
public class ChartConfigExtractor {

	/** Keys extracted from datasets and removed before sending to client. */
	private static final Set<String> DATASET_EXTRACT_KEYS =
		Set.of("metadata", "onClick", "onLegendClick", "tooltip");

	private final Map<String, ClickHandlerConfig> _configuredHandlers;

	private final Set<String> _configuredTooltips;

	/** Extracted metadata: metadata[datasetIndex][dataIndex] -> arbitrary object. */
	private final List<List<Object>> _metadata = new ArrayList<>();

	/** Extracted handler mapping: handlerMap[datasetIndex] -> { onClick, onLegendClick }. */
	private final List<Map<String, String>> _handlerRefs = new ArrayList<>();

	/** Extracted tooltip mapping: tooltipRef[datasetIndex] -> tooltip name. */
	private final List<String> _tooltipRefs = new ArrayList<>();

	/** The cleaned config (no TL objects, no handler/tooltip keys). */
	private Map<String, Object> _cleanConfig;

	/**
	 * Creates a new extractor.
	 *
	 * @param configuredHandlers
	 *        The click handlers declared in the view configuration.
	 * @param configuredTooltips
	 *        The tooltip provider names declared in the view configuration.
	 */
	public ChartConfigExtractor(Map<String, ClickHandlerConfig> configuredHandlers,
			Set<String> configuredTooltips) {
		_configuredHandlers = configuredHandlers;
		_configuredTooltips = configuredTooltips;
	}

	/**
	 * Extracts metadata and handler references from the given raw chart config.
	 *
	 * @param rawConfig
	 *        The chart configuration as returned by the TL-Script function.
	 * @throws TopLogicException
	 *         if the config is invalid.
	 */
	@SuppressWarnings("unchecked")
	public void extract(Object rawConfig) {
		if (!(rawConfig instanceof Map)) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CHART_CONFIG__DETAIL.fill("Expected a map, got: " + rawConfig));
		}

		Map<String, Object> config = new HashMap<>((Map<String, Object>) rawConfig);

		Object type = config.get("type");
		if (!(type instanceof CharSequence)) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CHART_CONFIG__DETAIL.fill("Missing or invalid 'type' key."));
		}

		Object data = config.get("data");
		if (!(data instanceof Map)) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CHART_CONFIG__DETAIL.fill("Missing or invalid 'data' key."));
		}

		Map<String, Object> dataMap = new HashMap<>((Map<String, Object>) data);
		Object datasets = dataMap.get("datasets");
		if (datasets instanceof List) {
			List<?> datasetList = (List<?>) datasets;
			List<Object> cleanDatasets = new ArrayList<>(datasetList.size());
			for (int i = 0; i < datasetList.size(); i++) {
				cleanDatasets.add(extractDataset(i, datasetList.get(i)));
			}
			dataMap.put("datasets", cleanDatasets);
		}

		config.put("data", dataMap);

		// Validate: no TL objects remaining anywhere in the config.
		validateNoTLObjects(config, "");

		_cleanConfig = config;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractDataset(int index, Object dataset) {
		if (!(dataset instanceof Map)) {
			return Map.of();
		}

		Map<String, Object> dsMap = new HashMap<>((Map<String, Object>) dataset);

		// Extract metadata.
		List<Object> metadataList = new ArrayList<>();
		Object metadata = dsMap.remove("metadata");
		if (metadata instanceof List) {
			for (Object item : (List<?>) metadata) {
				metadataList.add(item);
			}
		}
		_metadata.add(metadataList);

		// Extract handler references.
		Map<String, String> handlers = new HashMap<>();
		extractHandlerRef(dsMap, "onClick", handlers);
		extractHandlerRef(dsMap, "onLegendClick", handlers);
		_handlerRefs.add(handlers);

		// Extract tooltip reference.
		String tooltipRef = null;
		Object tooltip = dsMap.remove("tooltip");
		if (tooltip instanceof String) {
			tooltipRef = (String) tooltip;
			if (!_configuredTooltips.contains(tooltipRef)) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_TOOLTIP__NAME.fill(tooltipRef));
			}
		}
		_tooltipRefs.add(tooltipRef);

		return dsMap;
	}

	private void extractHandlerRef(Map<String, Object> dsMap, String key, Map<String, String> handlers) {
		Object ref = dsMap.remove(key);
		if (ref instanceof String) {
			String name = (String) ref;
			if (!_configuredHandlers.containsKey(name)) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_HANDLER__NAME.fill(name));
			}
			handlers.put(key, name);
		}
	}

	@SuppressWarnings("unchecked")
	private void validateNoTLObjects(Object value, String path) {
		if (value instanceof TLObject) {
			throw new TopLogicException(
				I18NConstants.ERROR_OBJECT_OUTSIDE_METADATA__PATH.fill(path));
		}
		if (value instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				validateNoTLObjects(entry.getValue(), path + "." + entry.getKey());
			}
		}
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			for (int i = 0; i < list.size(); i++) {
				validateNoTLObjects(list.get(i), path + "[" + i + "]");
			}
		}
	}

	/** The cleaned Chart.js config (safe for client). */
	public Map<String, Object> getCleanConfig() {
		return _cleanConfig;
	}

	/** Metadata lookup: metadata[datasetIndex][dataIndex]. */
	public List<List<Object>> getMetadata() {
		return _metadata;
	}

	/** Handler references per dataset. */
	public List<Map<String, String>> getHandlerRefs() {
		return _handlerRefs;
	}

	/** Tooltip provider name per dataset (null if none). */
	public List<String> getTooltipRefs() {
		return _tooltipRefs;
	}

	/**
	 * Builds the interaction descriptor sent to the client.
	 */
	public Map<String, Object> buildInteractions() {
		List<Map<String, Object>> dsInteractions = new ArrayList<>();
		for (int i = 0; i < _handlerRefs.size(); i++) {
			Map<String, String> handlers = _handlerRefs.get(i);
			String tooltip = i < _tooltipRefs.size() ? _tooltipRefs.get(i) : null;
			dsInteractions.add(Map.of(
				"clickable", handlers.containsKey("onClick"),
				"legendClickable", handlers.containsKey("onLegendClick"),
				"hasTooltip", tooltip != null
			));
		}
		return Map.of("datasets", dsInteractions);
	}
}
