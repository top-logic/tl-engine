/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;

import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * Drops invented citations and renumbers the markers that remain.
 *
 * <p>
 * A citation is kept only when its id or url appears in this turn's <em>other</em> tool results.
 * Markers whose citation did not survive are stripped from the answer. Surviving markers are
 * rewritten to {@code [1]}, {@code [2]}, … in order of first appearance.
 * </p>
 */
public final class CitationValidator {

	/** Config name of the cite tool. */
	public static final String CITE_TOOL = "cite.cite";

	/** Provider name of the cite tool. */
	public static final String CITE_TOOL_PROVIDER = "cite__cite";

	private static final Pattern MARKER = Pattern.compile("\\[(\\d+)\\]");

	private CitationValidator() {
		// Utility
	}

	/**
	 * Result of validating one answer.
	 *
	 * @param text cleaned markdown, markers rewritten or stripped
	 * @param citations kept citations, indexed from 1 in first-appearance order
	 */
	public record Result(String text, List<Citation> citations) {}

	/**
	 * A cite-tool declaration, before it is matched against tool results.
	 *
	 * @param claimedIndex marker the model was told to write
	 * @param toolName cite tool name
	 * @param toolCallId cite tool call id
	 * @param kind host kind
	 * @param id cited id
	 * @param label human label
	 * @param url web URL, or empty
	 */
	public record Declaration(int claimedIndex, String toolName, String toolCallId, String kind, String id,
			String label, String url) {}

	/**
	 * Validates {@code answer} against {@code declared} citations and the turn's tool-result
	 * evidence.
	 *
	 * @param answer raw assistant text
	 * @param declared cite-tool declarations this turn
	 * @param evidence tool result strings from this turn, excluding the cite tool itself
	 * @return cleaned text and the citations that survived
	 */
	public static Result validate(String answer, List<Declaration> declared, List<String> evidence) {
		if (answer == null || answer.isEmpty()) {
			logDrop("no citations at all", 0, 0);
			return new Result(answer == null ? "" : answer, List.of());
		}
		Map<Integer, Declaration> kept = new LinkedHashMap<>();
		int dropped = 0;
		for (Declaration item : declared) {
			if (supportedBy(item, evidence)) {
				kept.put(item.claimedIndex(), item);
				Logger.debug("Citation kept: kind=" + item.kind() + " id=" + item.id(), CitationValidator.class);
			} else {
				dropped++;
				Logger.debug("Invented citation dropped: kind=" + item.kind() + " id=" + item.id() + " url="
					+ item.url(), CitationValidator.class);
			}
		}
		List<Integer> appearance = new ArrayList<>();
		Matcher scan = MARKER.matcher(answer);
		while (scan.find()) {
			int n = Integer.parseInt(scan.group(1));
			if (!appearance.contains(n)) {
				appearance.add(n);
			}
		}
		Map<Integer, Integer> remap = new LinkedHashMap<>();
		List<Citation> citations = new ArrayList<>();
		int next = 1;
		for (int claimed : appearance) {
			Declaration item = kept.get(claimed);
			if (item == null) {
				continue;
			}
			remap.put(claimed, next);
			citations.add(new Citation(next, item.toolName(), item.toolCallId(), item.kind(), item.id(),
				item.label(), item.url()));
			next++;
		}
		String cleaned = rewriteMarkers(answer, remap);
		if (dropped > 0 || remap.size() != appearance.size()) {
			Logger.debug("Citation renumbering: kept " + citations.size() + " of " + declared.size()
				+ " declarations, dropped invented or unmarked.", CitationValidator.class);
		}
		if (citations.isEmpty()) {
			logDrop("no citations at all", dropped, declared.size());
		}
		return new Result(cleaned, List.copyOf(citations));
	}

	/**
	 * Validates the last assistant text of {@code log}'s current turn and writes the cleaned text
	 * and citations back onto that {@link Part.TextPart}.
	 *
	 * @param log conversation of this run
	 * @param answer extracted response text
	 * @return the same result that was written onto the log
	 */
	public static Result apply(ConversationLog log, String answer) {
		Turn turn = current(log);
		if (turn == null) {
			return validate(answer, List.of(), List.of());
		}
		List<Declaration> declared = new ArrayList<>();
		List<String> evidence = new ArrayList<>();
		int citeIndex = 1;
		for (Part part : turn.assistantParts()) {
			if (!(part instanceof Part.ToolCallPart tool)) {
				continue;
			}
			if (isCite(tool.toolName())) {
				declared.add(fromTool(tool, citeIndex++));
			} else if (tool.result() != null && !tool.result().isBlank()) {
				evidence.add(tool.result());
			}
		}
		Result result = validate(answer, declared, evidence);
		rewriteLastText(turn, result);
		return result;
	}

	private static void rewriteLastText(Turn turn, Result result) {
		Part.TextPart last = null;
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart text) {
				last = text;
			}
		}
		if (last == null) {
			return;
		}
		turn.updatePart(last.id(),
			new Part.TextPart(last.id(), last.timestamp(), result.text(), result.citations()));
	}

	private static Turn current(ConversationLog log) {
		if (log == null) {
			return null;
		}
		Turn open = log.currentTurn();
		if (open != null) {
			return open;
		}
		List<Turn> turns = log.turns();
		return turns.isEmpty() ? null : turns.get(turns.size() - 1);
	}

	private static boolean isCite(String name) {
		return CITE_TOOL.equals(name) || CITE_TOOL_PROVIDER.equals(name);
	}

	private static Declaration fromTool(Part.ToolCallPart tool, int fallbackIndex) {
		int claimed = fallbackIndex;
		String result = tool.result();
		if (result != null) {
			try {
				claimed = Integer.parseInt(result.trim());
			} catch (NumberFormatException ex) {
				// keep sequential fallback
			}
		}
		Map<String, Object> args = parseArgs(tool.argsJson());
		return new Declaration(claimed, tool.toolName(), tool.id(), str(args.get("kind")),
			str(args.get("id")), str(args.get("label")), str(args.get("url")));
	}

	private static Map<String, Object> parseArgs(String argsJson) {
		if (argsJson == null || argsJson.isBlank()) {
			return Map.of();
		}
		try {
			return JsonTree.parseObject(argsJson);
		} catch (IOException | RuntimeException ex) {
			return Map.of();
		}
	}

	private static String str(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	private static boolean supportedBy(Declaration item, List<String> evidence) {
		String id = item.id();
		String url = item.url();
		for (String blob : evidence) {
			if (id != null && !id.isBlank() && blob.contains(id)) {
				return true;
			}
			if (url != null && !url.isBlank() && blob.contains(url)) {
				return true;
			}
		}
		return false;
	}

	private static String rewriteMarkers(String answer, Map<Integer, Integer> remap) {
		Matcher matcher = MARKER.matcher(answer);
		StringBuilder out = new StringBuilder();
		int last = 0;
		while (matcher.find()) {
			out.append(answer, last, matcher.start());
			Integer next = remap.get(Integer.parseInt(matcher.group(1)));
			if (next != null) {
				out.append('[').append(next).append(']');
			}
			last = matcher.end();
		}
		out.append(answer, last, answer.length());
		return out.toString();
	}

	private static void logDrop(String reason, int dropped, int declared) {
		Logger.debug("Citations: " + reason + " (dropped=" + dropped + ", declared=" + declared + ")",
			CitationValidator.class);
	}
}
