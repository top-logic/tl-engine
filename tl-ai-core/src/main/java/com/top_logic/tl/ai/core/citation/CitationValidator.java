/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 *
 * <p>
 * <b>A turn that cited nothing is left completely alone</b> - no numbering is in force, so
 * {@code werte[0]}, a hand-written footnote and a code sample are prose and stay exactly as
 * written. In a turn that <em>did</em> cite, {@code [n]} is that turn's own numbering: a marker
 * pointing at no surviving citation is deleted, because a citation-shaped thing that resolves to
 * nothing is worse than no marker. No marker inside an inline code span or a fenced code block is
 * touched in either case.
 * </p>
 */
public final class CitationValidator {

	/**
	 * Bare name of the cite tool, as the model calls it and as a recorded
	 * {@link Part.ToolCallPart} carries it.
	 */
	public static final String CITE_TOOL_NAME = "cite";

	/** Config name of the cite tool, as an agent definition and the access policy write it. */
	public static final String CITE_TOOL = "cite.cite";

	/** Provider-safe registry name of the cite tool. */
	public static final String CITE_TOOL_PROVIDER = "cite__cite";

	private static final Pattern MARKER = Pattern.compile("\\[(\\d+)\\]");

	/** Shorter than this a "passage" says nothing and is more likely a stray word. */
	private static final int MIN_PASSAGE = 12;

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
	 * @param passage the sentence of the source the model says the fact rests on, or empty
	 */
	public record Declaration(int claimedIndex, String toolName, String toolCallId, String kind, String id,
			String label, String url, String passage) {

		/** A declaration that named no passage. */
		public Declaration(int claimedIndex, String toolName, String toolCallId, String kind, String id,
				String label, String url) {
			this(claimedIndex, toolName, toolCallId, kind, id, label, url, "");
		}
	}

	/**
	 * What to do with the markers of one turn: the numbers that survive, the numbers to delete, and
	 * the citations the surviving numbers point at.
	 */
	private record Plan(Map<Integer, Integer> remap, Set<Integer> strip, List<Citation> citations) {

		static Plan untouched() {
			return new Plan(Map.of(), Set.of(), List.of());
		}

		boolean isNoop() {
			return remap.isEmpty() && strip.isEmpty();
		}
	}

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
		String text = answer == null ? "" : answer;
		Plan plan = plan(List.of(text), declared, evidence);
		return new Result(rewrite(text, plan), plan.citations());
	}

	/**
	 * Validates every assistant text of {@code log}'s current turn and writes the cleaned texts and
	 * the surviving citations back onto those {@link Part.TextPart}s.
	 *
	 * @param log conversation of this run
	 * @param answer extracted response text
	 * @return the same citations that were written onto the log, with {@code answer} cleaned the
	 *         same way
	 */
	public static Result apply(ConversationLog log, String answer) {
		String text = answer == null ? "" : answer;
		Turn turn = current(log);
		if (turn == null) {
			return validate(text, List.of(), List.of());
		}
		List<Declaration> declared = new ArrayList<>();
		List<String> evidence = new ArrayList<>();
		List<Part.TextPart> texts = new ArrayList<>();
		int citeIndex = 1;
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart textPart) {
				texts.add(textPart);
			} else if (part instanceof Part.ToolCallPart tool) {
				if (isCite(tool.toolName())) {
					declared.add(fromTool(tool, citeIndex++));
				} else if (tool.result() != null && !tool.result().isBlank()) {
					evidence.add(tool.result());
				}
			}
		}
		List<String> scanned = new ArrayList<>(texts.size() + 1);
		for (Part.TextPart textPart : texts) {
			scanned.add(textPart.text() == null ? "" : textPart.text());
		}
		scanned.add(text);

		Plan plan = plan(scanned, declared, evidence);
		if (!plan.isNoop()) {
			for (Part.TextPart textPart : texts) {
				String before = textPart.text() == null ? "" : textPart.text();
				String after = rewrite(before, plan);
				List<Citation> own = citationsIn(after, plan);
				if (!after.equals(before) || !own.equals(textPart.citations())) {
					turn.updatePart(textPart.id(),
						new Part.TextPart(textPart.id(), textPart.timestamp(), after, own));
				}
			}
		}
		return new Result(rewrite(text, plan), plan.citations());
	}

	/**
	 * Decides the fate of every marker number of the turn.
	 *
	 * <p>
	 * A turn that declared nothing is left completely alone: without a declaration there is no
	 * claim to check, and every {@code [n]} in the text belongs to the model's prose.
	 * </p>
	 */
	private static Plan plan(List<String> texts, List<Declaration> declared, List<String> evidence) {
		if (declared.isEmpty()) {
			Logger.debug("Citations: the turn declared none; the answer is left unchanged.",
				CitationValidator.class);
			return Plan.untouched();
		}
		// Keyed by tool call id: two cite calls that claim the same number must both be seen, or one
		// source disappears without a trace.
		Map<String, Declaration> kept = new LinkedHashMap<>();
		Set<Integer> claimed = new LinkedHashSet<>();
		int dropped = 0;
		int anonymous = 0;
		for (Declaration item : declared) {
			claimed.add(Integer.valueOf(item.claimedIndex()));
			if (!declaredKind(item.kind())) {
				dropped++;
				Logger.debug("Citation of an undeclared kind dropped: kind=" + item.kind() + " id="
					+ item.id() + ". Declare it with <citation-kind> or the model cannot be told about"
					+ " it, and nothing can resolve it.", CitationValidator.class);
				continue;
			}
			if (supportedBy(item, evidence)) {
				String key = item.toolCallId() == null || item.toolCallId().isBlank()
					? "#" + (anonymous++)
					: item.toolCallId();
				kept.put(key, item);
				Logger.debug("Citation kept: kind=" + item.kind() + " id=" + item.id(), CitationValidator.class);
			} else {
				dropped++;
				Logger.debug("Invented citation dropped: kind=" + item.kind() + " id=" + item.id() + " url="
					+ item.url(), CitationValidator.class);
			}
		}
		Map<Integer, Declaration> byIndex = new LinkedHashMap<>();
		for (Declaration item : kept.values()) {
			Declaration clash = byIndex.putIfAbsent(Integer.valueOf(item.claimedIndex()), item);
			if (clash != null) {
				Logger.debug("Two citations claim marker [" + item.claimedIndex() + "]; keeping "
					+ clash.id() + ", ignoring " + item.id(), CitationValidator.class);
			}
		}

		List<Integer> appearance = new ArrayList<>();
		for (String text : texts) {
			for (int marker : markersOf(text)) {
				Integer boxed = Integer.valueOf(marker);
				if (byIndex.containsKey(boxed) && !appearance.contains(boxed)) {
					appearance.add(boxed);
				}
			}
		}
		Map<Integer, Integer> remap = new LinkedHashMap<>();
		List<Citation> citations = new ArrayList<>();
		int next = 1;
		for (Integer claimedIndex : appearance) {
			Declaration item = byIndex.get(claimedIndex);
			remap.put(claimedIndex, Integer.valueOf(next));
			String passage = verifiedPassage(item, evidence);
			String excerpt = passage.isEmpty()
				? CitationExcerpt.ofSource(item.id(), item.label(), !kindHasPassage(item.kind()), evidence)
				: CitationExcerpt.around(passage, evidence);
			// The popover highlights the passage inside the excerpt, and the excerpt is rendered
			// text: the passage has to be the same rendering of the same words, or the highlight
			// silently finds nothing.
			citations.add(new Citation(next, item.toolName(), item.toolCallId(), item.kind(),
				kindLabel(item.kind()), item.id(), item.label(), item.url(),
				CitationExcerpt.plain(passage), excerpt));
			next++;
		}
		// In a turn that cited, [n] is the turn's own numbering: a number that points at no surviving
		// citation is a citation-shaped thing that resolves to nothing, whether a cite call claimed
		// it and lost or the model wrote it out of nowhere. Both go.
		//
		// A turn that cited NOTHING is a different matter and is handled above: there is no numbering
		// in force, so every bracket is prose and the answer is not touched at all. Code spans and
		// fenced blocks are never touched either way.
		Set<Integer> strip = new LinkedHashSet<>(claimed);
		for (String text : texts) {
			strip.addAll(markersOf(text));
		}
		strip.removeAll(byIndex.keySet());

		if (dropped > 0) {
			Logger.debug("Citations: kept " + citations.size() + " of " + declared.size()
				+ " declarations, dropped " + dropped + " without evidence in this turn.",
				CitationValidator.class);
		}
		return new Plan(remap, strip, List.copyOf(citations));
	}

	/** The citations of {@code plan} whose marker actually occurs in {@code text}. */
	private static List<Citation> citationsIn(String text, Plan plan) {
		Set<Integer> present = new LinkedHashSet<>(markersOf(text));
		List<Citation> result = new ArrayList<>();
		for (Citation citation : plan.citations()) {
			if (present.contains(Integer.valueOf(citation.index()))) {
				result.add(citation);
			}
		}
		return List.copyOf(result);
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

	/**
	 * Whether {@code name} names the cite tool.
	 *
	 * <p>
	 * A tool has three names and the recorded one is the bare {@link #CITE_TOOL_NAME} the model
	 * calls; the other two occur in configuration and in the registry. Matching only one of them is
	 * what made every declaration invisible before.
	 * </p>
	 */
	public static boolean isCite(String name) {
		return CITE_TOOL_NAME.equals(name) || CITE_TOOL.equals(name) || CITE_TOOL_PROVIDER.equals(name);
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
			str(args.get("id")), str(args.get("label")), str(args.get("url")), str(args.get("passage")));
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

	/**
	 * Whether a tool result of this turn actually contains the cited id or URL.
	 *
	 * <p>
	 * Matched as a whole token, not as a substring: {@code "7"} is not evidenced by {@code "700"}
	 * and {@code ".../page/1/"} is not evidenced by {@code ".../page/12/"}.
	 * </p>
	 */
	private static boolean supportedBy(Declaration item, List<String> evidence) {
		for (String blob : evidence) {
			if (containsToken(blob, item.id()) || containsToken(blob, item.url())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The passage the model claims to have read, but only when the turn's evidence really contains
	 * it.
	 *
	 * <p>
	 * The marker's popover shows this sentence as what the fact rests on, so it has to be the
	 * source's wording rather than the model's memory of it.
	 * </p>
	 */
	/**
	 * Whether the application declared this kind.
	 *
	 * <p>
	 * Unknown outside an application - a unit test validating a hand-built turn - and then nothing
	 * is refused for being undeclared.
	 * </p>
	 */
	private static boolean declaredKind(String kind) {
		return !CitationKinds.isActive() || CitationKinds.getInstance().isDeclared(kind);
	}

	/** What to call this kind in front of a reader; empty outside an application. */
	private static String kindLabel(String kind) {
		return CitationKinds.isActive() ? CitationKinds.getInstance().label(kind) : "";
	}

	/** Whether a source of this kind has a sentence to quote at all. */
	private static boolean kindHasPassage(String kind) {
		return !CitationKinds.isActive() || CitationKinds.getInstance().hasPassage(kind);
	}

	private static String verifiedPassage(Declaration item, List<String> evidence) {
		if (!kindHasPassage(item.kind())) {
			// A structured object has fields, not prose. Asking for a verbatim sentence there is what
			// made the model serialise the object as JSON that no tool result then matched.
			return "";
		}
		String passage = item.passage() == null ? "" : item.passage().trim();
		if (passage.length() < MIN_PASSAGE) {
			return "";
		}
		for (String blob : evidence) {
			if (blob != null && blob.contains(passage)) {
				return passage;
			}
		}
		Logger.debug("Citation passage not found in this turn's tool results; dropped: "
			+ passage.substring(0, Math.min(60, passage.length())), CitationValidator.class);
		return "";
	}

	private static boolean containsToken(String blob, String token) {
		if (blob == null || token == null || token.isBlank()) {
			return false;
		}
		int at = blob.indexOf(token);
		while (at >= 0) {
			boolean leftFree = at == 0 || !isTokenChar(blob.charAt(at - 1));
			int after = at + token.length();
			boolean rightFree = after >= blob.length() || !isTokenChar(blob.charAt(after));
			if (leftFree && rightFree) {
				return true;
			}
			at = blob.indexOf(token, at + 1);
		}
		return false;
	}

	private static boolean isTokenChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	/** The marker numbers of {@code text}, in order, skipping code spans and fenced blocks. */
	private static List<Integer> markersOf(String text) {
		if (text == null || text.isEmpty()) {
			return List.of();
		}
		boolean[] code = codeMask(text);
		List<Integer> found = new ArrayList<>();
		Matcher scan = MARKER.matcher(text);
		while (scan.find()) {
			if (code[scan.start()]) {
				continue;
			}
			found.add(Integer.valueOf(Integer.parseInt(scan.group(1))));
		}
		return found;
	}

	/**
	 * Applies {@code plan} to one text: surviving markers renumbered, dropped ones deleted together
	 * with the space in front of them, everything else untouched.
	 */
	private static String rewrite(String text, Plan plan) {
		if (text == null || text.isEmpty() || plan.isNoop()) {
			return text == null ? "" : text;
		}
		boolean[] code = codeMask(text);
		Matcher matcher = MARKER.matcher(text);
		StringBuilder out = new StringBuilder();
		int last = 0;
		while (matcher.find()) {
			if (code[matcher.start()]) {
				continue;
			}
			Integer marker = Integer.valueOf(Integer.parseInt(matcher.group(1)));
			// Every marker outside code is either renumbered or removed: `plan` collected them all,
			// and a turn that cited nothing never gets here - `isNoop()` returned above.
			Integer renumbered = plan.remap().get(marker);
			if (renumbered != null) {
				out.append(text, last, matcher.start()).append('[').append(renumbered).append(']');
				last = matcher.end();
			} else {
				// "Alpha [1]." must become "Alpha.", not "Alpha ." - the marker took the space with
				// it when the model wrote it.
				int cut = matcher.start();
				while (cut > last && isBlank(text.charAt(cut - 1))) {
					cut--;
				}
				out.append(text, last, cut);
				last = matcher.end();
			}
		}
		out.append(text, last, text.length());
		return out.toString();
	}

	private static boolean isBlank(char c) {
		return c == ' ' || c == '\t';
	}

	/**
	 * Which characters of {@code text} sit inside an inline code span or a fenced code block.
	 *
	 * <p>
	 * A {@code [1]} in a code sample is code, and a validator that edits it corrupts the answer it
	 * was meant to clean.
	 * </p>
	 */
	private static boolean[] codeMask(String text) {
		boolean[] mask = new boolean[text.length()];
		String fence = null;
		int pos = 0;
		while (pos <= text.length()) {
			int eol = text.indexOf('\n', pos);
			int end = eol < 0 ? text.length() : eol;
			String line = text.substring(pos, end);
			String trimmed = line.stripLeading();
			if (fence != null) {
				mark(mask, pos, end);
				if (closesFence(trimmed, fence)) {
					fence = null;
				}
			} else {
				String opener = fenceRun(trimmed);
				if (opener != null) {
					fence = opener;
					mark(mask, pos, end);
				} else {
					markInlineCode(mask, text, pos, end);
				}
			}
			if (eol < 0) {
				break;
			}
			pos = eol + 1;
		}
		return mask;
	}

	/** The fence a line opens ({@code ```} or {@code ~~~}, three or more), or <code>null</code>. */
	private static String fenceRun(String trimmed) {
		char marker = trimmed.isEmpty() ? 0 : trimmed.charAt(0);
		if (marker != '`' && marker != '~') {
			return null;
		}
		int run = 0;
		while (run < trimmed.length() && trimmed.charAt(run) == marker) {
			run++;
		}
		return run >= 3 ? trimmed.substring(0, run) : null;
	}

	private static boolean closesFence(String trimmed, String fence) {
		String run = fenceRun(trimmed);
		return run != null && run.charAt(0) == fence.charAt(0) && run.length() >= fence.length()
			&& trimmed.substring(run.length()).isBlank();
	}

	private static void markInlineCode(boolean[] mask, String text, int from, int to) {
		int at = from;
		while (at < to) {
			if (text.charAt(at) != '`') {
				at++;
				continue;
			}
			int open = at;
			while (at < to && text.charAt(at) == '`') {
				at++;
			}
			int ticks = at - open;
			int close = closingTicks(text, at, to, ticks);
			if (close < 0) {
				continue;
			}
			mark(mask, open, close + ticks);
			at = close + ticks;
		}
	}

	private static int closingTicks(String text, int from, int to, int ticks) {
		int at = from;
		while (at < to) {
			if (text.charAt(at) != '`') {
				at++;
				continue;
			}
			int start = at;
			while (at < to && text.charAt(at) == '`') {
				at++;
			}
			if (at - start == ticks) {
				return start;
			}
		}
		return -1;
	}

	private static void mark(boolean[] mask, int from, int to) {
		for (int i = Math.max(0, from); i < Math.min(to, mask.length); i++) {
			mask[i] = true;
		}
	}
}
