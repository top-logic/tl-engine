/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.citation.CitationMarkerSyntax.Marker;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * Turns the links an answer points at its sources with into footnotes, and drops the links that
 * point at nothing the run read.
 *
 * <p>
 * The answer cites by linking: {@code [Sommerfest](tl:GenericObject:11047)}, {@code [laut
 * Tagesschau](https://...)}, or a marker of a syntax the application registered (see
 * {@link CitationMarkerSyntax}). For every marker of every text of the turn:
 * </p>
 *
 * <ul>
 * <li>a reference that names a source on the run's {@link ReadingList}, of a declared kind, becomes
 * a footnote {@code [n]} behind the marker's text. Footnotes are numbered per answer in the order of
 * their first appearance, and a source cited twice keeps its number;</li>
 * <li>a marker none of whose references holds is replaced by its text alone - an invented
 * identifier or address disappears, the sentence stays;</li>
 * <li>except a link to a web address the turn has <em>seen</em> without reading it - a search hit,
 * a page a tool published, an address the user wrote. That is no invention and no source either, and
 * stays the ordinary link it was.</li>
 * </ul>
 *
 * <p>
 * Nothing inside an inline code span or a fenced code block is touched. In an answer that cites, a
 * {@code [n]} the model wrote itself is written as {@code &#91;n&#93;}, which renders the same
 * brackets but cannot be mistaken for a footnote.
 * </p>
 */
public final class CitationValidator {

	/** A literal footnote-shaped bracket in the model's own text. */
	private static final Pattern BRACKET_NUMBER = Pattern.compile("\\[(\\d+)\\]");

	/** Shorter than this a "passage" says nothing and is more likely a stray word. */
	private static final int MIN_PASSAGE = 12;

	private CitationValidator() {
		// Utility
	}

	/**
	 * Result of validating one answer.
	 *
	 * @param text cleaned markdown: footnotes written, dropped links reduced to their text
	 * @param citations kept citations, indexed from 1 in first-appearance order
	 */
	public record Result(String text, List<Citation> citations) {}

	/**
	 * Validates {@code answer} against the sources of the run.
	 *
	 * @param answer
	 *        raw assistant text
	 * @param sources
	 *        what the run read
	 * @param seen
	 *        everything else the turn put in front of the model - its tool results and the user's
	 *        message; a web link into it is kept as a link
	 * @return cleaned text and the citations that survived
	 */
	public static Result validate(String answer, ReadingList sources, List<String> seen) {
		return validate(answer, sources, seen, CitationKinds.syntaxes());
	}

	/**
	 * {@link #validate(String, ReadingList, List)} with the given marker syntaxes rather than the
	 * application's.
	 *
	 * @param syntaxes
	 *        The ways the answer may point at sources, in the order they are asked; see
	 *        {@link CitationKinds#markerSyntaxes()}.
	 */
	public static Result validate(String answer, ReadingList sources, List<String> seen,
			List<CitationMarkerSyntax> syntaxes) {
		String text = answer == null ? "" : answer;
		Footnotes footnotes = new Footnotes(sources, seen, syntaxes);
		footnotes.rewrite(text);
		footnotes.escapeStray = !footnotes.isEmpty();
		return new Result(footnotes.rewrite(text), footnotes.citations());
	}

	/**
	 * Validates every assistant text of {@code log}'s current turn and writes the cleaned texts and
	 * the surviving citations back onto those
	 * {@link com.top_logic.tl.ai.core.session.Part.TextPart}s. The run's sources are recorded on the
	 * turn as well, so the reading list stays readable with the transcript.
	 *
	 * @param log
	 *        conversation of this run
	 * @param answer
	 *        extracted response text
	 * @param sources
	 *        what the run - and every sub-agent whose result it received - read
	 * @return the same citations that were written onto the log, with {@code answer} cleaned the
	 *         same way
	 */
	public static Result apply(ConversationLog log, String answer, ReadingList sources) {
		String text = answer == null ? "" : answer;
		ReadingList list = sources == null ? new ReadingList() : sources;
		Turn turn = current(log);
		if (turn == null) {
			return validate(text, list, List.of());
		}
		turn.setSources(list.sources());

		List<String> seen = new ArrayList<>();
		for (Part part : turn.userParts()) {
			if (part instanceof Part.TextPart userText && userText.text() != null) {
				seen.add(userText.text());
			}
		}
		List<Part.TextPart> texts = new ArrayList<>();
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart textPart) {
				texts.add(textPart);
			} else if (part instanceof Part.ToolCallPart tool && tool.result() != null
					&& !tool.result().isBlank()) {
				seen.add(tool.result());
			}
		}

		// First pass: number the footnotes in the order the reader meets them. Only then is it known
		// whether this answer cites at all, which decides what a bracketed number of its own is.
		Footnotes footnotes = new Footnotes(list, seen, CitationKinds.syntaxes());
		for (Part.TextPart textPart : texts) {
			footnotes.rewrite(textOf(textPart));
		}
		footnotes.rewrite(text);
		footnotes.escapeStray = !footnotes.isEmpty();

		for (Part.TextPart textPart : texts) {
			String before = textOf(textPart);
			footnotes.used.clear();
			String after = footnotes.rewrite(before);
			List<Citation> own = footnotes.usedCitations();
			if (!after.equals(before) || !own.equals(textPart.citations())) {
				turn.updatePart(textPart.id(),
					new Part.TextPart(textPart.id(), textPart.timestamp(), after, own));
			}
		}
		Result result = new Result(footnotes.rewrite(text), footnotes.citations());
		Logger.debug("Citations: " + result.citations().size() + " footnotes from "
			+ list.sources().size() + " sources read.", CitationValidator.class);
		return result;
	}

	private static String textOf(Part.TextPart part) {
		return part.text() == null ? "" : part.text();
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
	 * The footnotes of one answer: which source got which number, and how a text reads with them.
	 */
	private static final class Footnotes {

		private final ReadingList _sources;

		private final List<String> _seen;

		private final List<CitationMarkerSyntax> _syntaxes;

		/** Footnote by the source's identifier, in numbering order. */
		private final Map<String, Citation> _byId = new LinkedHashMap<>();

		/** The footnotes the text rewritten last points at. */
		final List<Citation> used = new ArrayList<>();

		boolean escapeStray;

		Footnotes(ReadingList sources, List<String> seen, List<CitationMarkerSyntax> syntaxes) {
			_sources = sources;
			_seen = seen;
			_syntaxes = syntaxes;
		}

		boolean isEmpty() {
			return _byId.isEmpty();
		}

		List<Citation> citations() {
			return List.copyOf(_byId.values());
		}

		List<Citation> usedCitations() {
			List<Citation> result = new ArrayList<>();
			for (Citation citation : _byId.values()) {
				if (used.contains(citation)) {
					result.add(citation);
				}
			}
			return List.copyOf(result);
		}

		/** {@code text} with its markers resolved; numbers new footnotes as they are met. */
		String rewrite(String text) {
			if (text == null || text.isEmpty()) {
				return "";
			}
			boolean[] code = codeMask(text);
			List<Marker> markers = markers(text, code);
			StringBuilder out = new StringBuilder();
			int last = 0;
			for (Marker marker : markers) {
				out.append(prose(text.substring(last, marker.start())));
				out.append(resolve(marker, text.substring(marker.start(), marker.end())));
				last = marker.end();
			}
			out.append(prose(text.substring(last)));
			return out.toString();
		}

		/**
		 * The markers of every syntax, outside code, in text order; a marker overlapping one an
		 * earlier syntax found is not a marker.
		 */
		private List<Marker> markers(String text, boolean[] code) {
			List<Marker> accepted = new ArrayList<>();
			for (CitationMarkerSyntax syntax : _syntaxes) {
				List<Marker> found;
				try {
					found = syntax.find(text);
				} catch (RuntimeException ex) {
					Logger.error("Citation marker syntax " + syntax.getClass().getName() + " failed.", ex,
						CitationValidator.class);
					continue;
				}
				for (Marker marker : found) {
					if (marker.start() < 0 || marker.end() > text.length() || marker.start() >= marker.end()
						|| code[marker.start()] || overlaps(marker, accepted)) {
						continue;
					}
					accepted.add(marker);
				}
			}
			accepted.sort((a, b) -> Integer.compare(a.start(), b.start()));
			return accepted;
		}

		private static boolean overlaps(Marker marker, List<Marker> accepted) {
			for (Marker other : accepted) {
				if (marker.start() < other.end() && other.start() < marker.end()) {
					return true;
				}
			}
			return false;
		}

		private String resolve(Marker marker, String original) {
			StringBuilder notes = new StringBuilder();
			for (String reference : marker.references()) {
				Citation citation = footnote(reference, marker.passage());
				if (citation != null) {
					if (!used.contains(citation)) {
						used.add(citation);
					}
					String note = "[" + citation.index() + "]";
					if (notes.indexOf(note) < 0) {
						notes.append(note);
					}
				}
			}
			if (notes.length() > 0) {
				return prose(marker.keep()) + notes;
			}
			if (marker.references().size() == 1 && ReadingList.isWebAddress(marker.references().get(0))
				&& seen(marker.references().get(0).trim())) {
				return original;
			}
			Logger.debug("Citation dropped, no source of this run: " + marker.references(),
				CitationValidator.class);
			return prose(marker.keep());
		}

		/** The footnote for {@code reference}, numbered now if it is new; null if it names nothing read. */
		private Citation footnote(String reference, String passage) {
			ReadSource source = _sources.find(reference);
			if (source == null) {
				return null;
			}
			if (!declaredKind(source.kind())) {
				Logger.debug("Citation of an undeclared kind dropped: kind=" + source.kind() + " id="
					+ source.id(), CitationValidator.class);
				return null;
			}
			Citation known = _byId.get(source.id());
			if (known != null) {
				return known;
			}
			List<String> evidence = new ArrayList<>();
			if (!source.text().isEmpty()) {
				evidence.add(source.text());
			}
			evidence.addAll(_seen);
			String verified = verifiedPassage(source, passage, evidence);
			String excerpt;
			if (!verified.isEmpty()) {
				excerpt = CitationExcerpt.around(verified, evidence);
			} else if (!source.text().isEmpty() && kindHasPassage(source.kind())) {
				excerpt = CitationExcerpt.opening(source.text());
			} else {
				excerpt = CitationExcerpt.ofSource(source.id(), source.title(), !kindHasPassage(source.kind()),
					evidence);
			}
			String archivedAt = source.archiveUrl().isEmpty() ? ""
				: DateTimeFormatter.ISO_INSTANT.format(source.readAt());
			// The popover highlights the passage inside the excerpt, and the excerpt is rendered text:
			// the passage has to be the same rendering of the same words, or the highlight finds
			// nothing.
			Citation citation = new Citation(_byId.size() + 1, "", "", source.kind(), kindLabel(source.kind()),
				source.id(), source.title(), source.url(), CitationExcerpt.plain(verified), excerpt,
				source.archiveUrl(), archivedAt);
			_byId.put(source.id(), citation);
			return citation;
		}

		private boolean seen(String url) {
			for (String blob : _seen) {
				if (containsToken(blob, url)) {
					return true;
				}
			}
			return false;
		}

		/** The model's own text between markers, with footnote-shaped brackets defused when needed. */
		private String prose(String text) {
			if (!escapeStray || text.isEmpty()) {
				return text;
			}
			boolean[] code = codeMask(text);
			Matcher bracket = BRACKET_NUMBER.matcher(text);
			StringBuilder out = new StringBuilder();
			int last = 0;
			while (bracket.find()) {
				if (code[bracket.start()]) {
					continue;
				}
				out.append(text, last, bracket.start()).append("&#91;").append(bracket.group(1)).append("&#93;");
				last = bracket.end();
			}
			out.append(text, last, text.length());
			return out.toString();
		}
	}

	/**
	 * The passage the marker names, but only when what the tool read really contains it.
	 *
	 * <p>
	 * The popover shows this sentence as what the statement rests on, so it has to be the source's
	 * wording rather than the model's memory of it.
	 * </p>
	 */
	private static String verifiedPassage(ReadSource source, String passage, List<String> evidence) {
		if (!kindHasPassage(source.kind())) {
			return "";
		}
		String trimmed = passage == null ? "" : passage.trim();
		if (trimmed.length() < MIN_PASSAGE) {
			return "";
		}
		for (String blob : evidence) {
			if (blob != null && blob.contains(trimmed)) {
				return trimmed;
			}
		}
		Logger.debug("Citation passage not found in what was read; dropped: "
			+ trimmed.substring(0, Math.min(60, trimmed.length())), CitationValidator.class);
		return "";
	}

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

	/**
	 * Whether {@code token} occurs in {@code blob} as a whole token, not as a part of a longer one:
	 * {@code ".../page/1"} is not seen in {@code ".../page/12"}.
	 */
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

	/**
	 * Replaces the footnote markers of a text this class validated, and nothing that only looks like
	 * one.
	 *
	 * <p>
	 * In the text of an answer that cites, a {@code [n]} outside code is a marker this class wrote:
	 * a bracketed number of the model's own is written as {@code &#91;n&#93;} there. A {@code [n]} in
	 * an inline code span or a code block - {@code items[1]} - is code and stays as it is.
	 * </p>
	 *
	 * @param text
	 *        The {@link Result#text() text} of a validation.
	 * @param count
	 *        How many footnotes the validation numbered; a number above it is no marker.
	 * @param note
	 *        What a marker of footnote {@code n} is replaced with.
	 * @return The text with its markers replaced.
	 */
	public static String replaceFootnotes(String text, int count, IntFunction<String> note) {
		if (text == null || text.isEmpty() || count == 0) {
			return text == null ? "" : text;
		}
		boolean[] code = codeMask(text);
		Matcher marker = BRACKET_NUMBER.matcher(text);
		StringBuilder out = new StringBuilder();
		int last = 0;
		while (marker.find()) {
			int n = marker.group(1).length() > 9 ? 0 : Integer.parseInt(marker.group(1));
			if (code[marker.start()] || n < 1 || n > count) {
				continue;
			}
			out.append(text, last, marker.start()).append(note.apply(n));
			last = marker.end();
		}
		out.append(text, last, text.length());
		return out.toString();
	}

	/**
	 * Which characters of {@code text} sit inside an inline code span or a fenced code block.
	 *
	 * <p>
	 * A link in a code sample is code, and a validator that edits it corrupts the answer it was
	 * meant to clean.
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
