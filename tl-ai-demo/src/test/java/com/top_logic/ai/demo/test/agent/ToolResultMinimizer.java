/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.session.Part;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Extracts minimal fields from tool results for judge scoring.
 *
 * <p>
 * Only includes fields needed for validation: - Types: qualifiedName, metaType, generalizations -
 * Parts: definedIn, name, kind, type, mandatory, multiple, derived, override
 * </p>
 */
public class ToolResultMinimizer {

	/**
	 * Extracts tool results from completed tool call parts and minimizes them.
	 *
	 * @param toolCalls
	 *        List of tool call parts from the conversation log
	 * @return Map of part IDs to minimized result objects
	 */
	public static Map<String, Object> extractAndMinimizeToolResults(List<Part.ToolCallPart> toolCalls) {
		Map<String, Object> minimized = new HashMap<>();

		for (Part.ToolCallPart call : toolCalls) {
			if (call.status() == Part.ToolCallStatus.COMPLETED && call.result() != null) {
				Object parsed = parseResultJson(call.result());
				if (parsed != null) {
					Object minimizedResult = minimizeResult(parsed);
					if (minimizedResult != null) {
						minimized.put(call.id(), minimizedResult);
					}
				}
			}
		}

		return minimized;
	}

	/**
	 * Minimizes tool results by extracting only fields needed for judge validation.
	 *
	 * @param toolResults
	 *        Map of result references to full tool result objects
	 * @return Map of result references to minimized result objects
	 */
	public static Map<String, Object> minimizeToolResults(Map<String, Object> toolResults) {
		Map<String, Object> minimized = new HashMap<>();

		for (Map.Entry<String, Object> entry : toolResults.entrySet()) {
			String resultRef = entry.getKey();
			Object result = entry.getValue();

			Object minimizedResult = minimizeResult(result);
			if (minimizedResult != null) {
				minimized.put(resultRef, minimizedResult);
			}
		}

		return minimized;
	}

	/**
	 * Parses a JSON result string into a Map/List structure.
	 */
	@SuppressWarnings("unchecked")
	private static Object parseResultJson(String json) {
		if (json == null || json.isEmpty()) {
			return null;
		}
		try {
			JsonReader reader = new JsonReader(new StringR(json));
			return readJsonValue(reader);
		} catch (Exception ex) {
			return json;
		}
	}

	/**
	 * Reads a JSON value recursively.
	 */
	@SuppressWarnings("unchecked")
	private static Object readJsonValue(JsonReader reader) throws java.io.IOException {
		switch (reader.peek()) {
			case BEGIN_OBJECT:
				Map<String, Object> map = new HashMap<>();
				reader.beginObject();
				while (reader.hasNext()) {
					String name = reader.nextName();
					map.put(name, readJsonValue(reader));
				}
				reader.endObject();
				return map;
			case BEGIN_ARRAY:
				List<Object> list = new ArrayList<>();
				reader.beginArray();
				while (reader.hasNext()) {
					list.add(readJsonValue(reader));
				}
				reader.endArray();
				return list;
			case STRING:
				return reader.nextString();
			case NUMBER:
				return reader.nextDouble();
			case BOOLEAN:
				return reader.nextBoolean();
			case NULL:
				reader.nextNull();
				return null;
			default:
				reader.skipValue();
				return null;
		}
	}

	/**
	 * Minimizes a single tool result object.
	 */
	@SuppressWarnings("unchecked")
	private static Object minimizeResult(Object result) {
		if (result == null) {
			return null;
		}

		if (!(result instanceof Map)) {
			return result;
		}

		Map<String, Object> resultMap = (Map<String, Object>) result;

		if (resultMap.containsKey("types")) {
			return minimizeTypeListResponse(resultMap);
		}

		if (resultMap.containsKey("parts")) {
			return minimizeTypePartListResponse(resultMap);
		}

		if (resultMap.containsKey("classifiers")) {
			return minimizeEnumerationResponse(resultMap);
		}

		return resultMap;
	}

	/**
	 * Minimizes a TypeListResponse by extracting only needed fields from types.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> minimizeTypeListResponse(Map<String, Object> response) {
		Map<String, Object> minimized = new HashMap<>();

		Object typesObj = response.get("types");
		if (typesObj instanceof List) {
			List<Object> types = (List<Object>) typesObj;
			List<Map<String, Object>> minimizedTypes = new ArrayList<>();

			for (Object typeObj : types) {
				if (typeObj instanceof Map) {
					Map<String, Object> type = (Map<String, Object>) typeObj;
					Map<String, Object> minimizedType = new HashMap<>();

					if (type.containsKey("qualifiedName")) {
						minimizedType.put("qualifiedName", type.get("qualifiedName"));
					}
					if (type.containsKey("metaType")) {
						minimizedType.put("metaType", type.get("metaType"));
					}
					if (type.containsKey("generalizations")) {
						minimizedType.put("generalizations", type.get("generalizations"));
					}

					if (!minimizedType.isEmpty()) {
						minimizedTypes.add(minimizedType);
					}
				}
			}

			minimized.put("types", minimizedTypes);
		}

		return minimized;
	}

	/**
	 * Minimizes a TypePartListResponse by extracting only needed fields from parts.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> minimizeTypePartListResponse(Map<String, Object> response) {
		Map<String, Object> minimized = new HashMap<>();

		Object partsObj = response.get("parts");
		if (partsObj instanceof List) {
			List<Object> parts = (List<Object>) partsObj;
			List<Map<String, Object>> minimizedParts = new ArrayList<>();

			for (Object partObj : parts) {
				if (partObj instanceof Map) {
					Map<String, Object> part = (Map<String, Object>) partObj;
					Map<String, Object> minimizedPart = new HashMap<>();

					if (part.containsKey("definedIn")) {
						minimizedPart.put("definedIn", part.get("definedIn"));
					}
					if (part.containsKey("name")) {
						minimizedPart.put("name", part.get("name"));
					}
					if (part.containsKey("kind")) {
						minimizedPart.put("kind", part.get("kind"));
					}
					if (part.containsKey("type")) {
						minimizedPart.put("type", part.get("type"));
					}
					if (part.containsKey("mandatory")) {
						minimizedPart.put("mandatory", part.get("mandatory"));
					}
					if (part.containsKey("multiple")) {
						minimizedPart.put("multiple", part.get("multiple"));
					}
					if (part.containsKey("derived")) {
						minimizedPart.put("derived", part.get("derived"));
					}
					if (part.containsKey("override")) {
						minimizedPart.put("override", part.get("override"));
					}

					if (!minimizedPart.isEmpty()) {
						minimizedParts.add(minimizedPart);
					}
				}
			}

			minimized.put("parts", minimizedParts);
		}

		return minimized;
	}

	/**
	 * Minimizes an EnumerationResponse by extracting only needed fields from classifiers.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> minimizeEnumerationResponse(Map<String, Object> response) {
		Map<String, Object> minimized = new HashMap<>();

		if (response.containsKey("enumeration")) {
			minimized.put("enumeration", response.get("enumeration"));
		}
		if (response.containsKey("module")) {
			minimized.put("module", response.get("module"));
		}
		if (response.containsKey("name")) {
			minimized.put("name", response.get("name"));
		}

		Object classifiersObj = response.get("classifiers");
		if (classifiersObj instanceof List) {
			List<Object> classifiers = (List<Object>) classifiersObj;
			List<Map<String, Object>> minimizedClassifiers = new ArrayList<>();

			for (Object classifierObj : classifiers) {
				if (classifierObj instanceof Map) {
					Map<String, Object> classifier = (Map<String, Object>) classifierObj;
					Map<String, Object> minimizedClassifier = new HashMap<>();

					if (classifier.containsKey("name")) {
						minimizedClassifier.put("name", classifier.get("name"));
					}

					if (!minimizedClassifier.isEmpty()) {
						minimizedClassifiers.add(minimizedClassifier);
					}
				}
			}

			minimized.put("classifiers", minimizedClassifiers);
		}

		return minimized;
	}
}
