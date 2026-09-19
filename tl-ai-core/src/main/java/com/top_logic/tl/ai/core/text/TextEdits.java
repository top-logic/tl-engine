/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies an anchored edit to a text: find an exact passage, replace it, and say precisely what
 * happened when that could not be done.
 *
 * <p>
 * <b>Why this exists.</b> An agent that changes one sentence of an 8,000-token document has, without
 * this, exactly one way to do it: emit all 8,000 tokens again. That costs the whole value per edit
 * and it lets the untouched parts drift - reflowed whitespace, a rephrased heading, a mangled table -
 * because nothing forces them to come back unchanged. An anchored edit costs the changed snippet and
 * cannot touch anything else, because everything else is never re-emitted.
 * </p>
 *
 * <p>
 * <b>The contract is byte-for-byte.</b> The anchor is matched against the stored characters as they
 * stand, and what replaces it is stored as it stands. Nothing is canonicalised on the way in or on
 * the way out, so a caller that read a value verbatim and quoted a passage of it back gets a match,
 * and a caller that did not gets a refusal rather than a guess.
 * </p>
 *
 * <p>
 * <b>A fallback decides where, never what.</b> Every level of the {@link Matching#TOLERANT cascade}
 * compares a normalised anchor with a normalised text and then maps the hit back onto the offsets of
 * the text as stored, so what is replaced is always a range of the original. An anchor that names a
 * line with the wrong indentation therefore finds that line and replaces the line, leaving the stored
 * indentation as it was; to change the indentation the anchor has to be exact, which the first level
 * of the cascade then applies.
 * </p>
 *
 * <p>
 * <b>Dependency-free by design.</b> {@link String} in, {@link Result} out, no knowledge base, no
 * model, no services - so an application that addresses its own text differently (by heading, by
 * section, by line range) reuses the part where the reliability actually lives without inheriting the
 * generic tool built on it. See {@code tl-ai-core/doc/text-edits.md}.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.tools.text.TextEditTool
 */
public final class TextEdits {

	/**
	 * How far the match cascade may go.
	 *
	 * @see #TOLERANT
	 */
	public enum Matching {

		/**
		 * The anchor must occur character for character.
		 *
		 * <p>
		 * The honest setting for a caller that produced the anchor itself - a migration, a template
		 * expansion, an application's own addressing layer. A miss is then a bug in the caller, and
		 * matching something almost-equal instead would hide it.
		 * </p>
		 */
		STRICT,

		/**
		 * Exact first; failing that, ignore the line endings; failing that, ignore how whitespace is
		 * spelled.
		 *
		 * <p>
		 * The setting for an anchor a language model quoted. The two fallbacks cover the miss classes
		 * that have nothing to do with the author's intent - a value stored with CRLF and quoted back
		 * with LF, an indentation level retyped with spaces instead of a tab, a non-breaking space in
		 * the source that came back as a plain one. A fallback that matched is always
		 * {@link Result#match() reported}, so a caller never has to assume which level answered.
		 * </p>
		 */
		TOLERANT;
	}

	/**
	 * How the anchor was found, in the order the cascade tries.
	 *
	 * <p>
	 * Also the level of the normalisation a level applies: every level compares a normalised anchor
	 * with a normalised text and then maps the hit back to the offsets of the <em>original</em>, so
	 * what gets replaced is always a range of the text as stored.
	 * </p>
	 */
	public enum Match {

		/** The anchor occurs character for character. */
		EXACT,

		/** The anchor occurs once {@code \r\n} and {@code \r} are read as {@code \n} on both sides. */
		LINE_ENDINGS,

		/**
		 * The anchor occurs once every run of whitespace is read as one space on both sides, and the
		 * anchor's own leading and trailing whitespace is dropped.
		 *
		 * <p>
		 * "Whitespace" here includes the space characters Java does not call whitespace - the
		 * non-breaking space, the thin space, the ideographic space - because a text that went through
		 * a word processor is full of them and none of them is a difference the author intended.
		 * </p>
		 */
		WHITESPACE;
	}

	/**
	 * Why an edit was refused.
	 *
	 * <p>
	 * Each value is a distinct thing for the caller to do next, which is the only reason to have a
	 * code at all: the accompanying {@link Result#message()} says which one in words.
	 * </p>
	 */
	public enum Failure {

		/**
		 * The anchor is empty and the text is not.
		 *
		 * <p>
		 * An empty anchor occurs everywhere and therefore addresses nothing. It is accepted against an
		 * empty text, where it addresses the one position that exists.
		 * </p>
		 */
		EMPTY_ANCHOR,

		/** The anchor does not occur, at any level of the cascade. */
		NOT_FOUND,

		/**
		 * The anchor occurs more than once and the caller asked for a single replacement.
		 *
		 * <p>
		 * Never resolved by taking the first one: which occurrence the caller meant is not knowable,
		 * and silently editing one of them is the failure mode this whole class exists to prevent. The
		 * {@link Result#lines() line numbers} of every occurrence come back so the caller can extend
		 * the anchor.
		 * </p>
		 */
		AMBIGUOUS;
	}

	/**
	 * What one edit did, or why it did nothing.
	 *
	 * <p>
	 * One type for both outcomes, because every caller has to handle both and a caller that forgot
	 * would otherwise get a plausible-looking text back. {@link #applied()} is the question to ask
	 * first; {@link #text()} is {@code null} until it answers {@code true}.
	 * </p>
	 */
	public static final class Result {

		private final String _text;

		private final Failure _failure;

		private final String _message;

		private final Match _match;

		private final int _replacements;

		private final int[] _lines;

		private final int _changeOffset;

		private Result(String text, Failure failure, String message, Match match, int replacements,
				int[] lines, int changeOffset) {
			_text = text;
			_failure = failure;
			_message = message;
			_match = match;
			_replacements = replacements;
			_lines = lines;
			_changeOffset = changeOffset;
		}

		static Result applied(String text, Match match, String did, int replacements, int[] lines,
				int changeOffset) {
			return new Result(text, null, describe(match, did), match, replacements, lines, changeOffset);
		}

		static Result failed(Failure failure, String message, int[] lines) {
			return new Result(null, failure, message, null, 0, lines, -1);
		}

		private static String describe(Match match, String did) {
			return switch (match) {
				case EXACT -> did;
				case LINE_ENDINGS -> did + " The anchor matched only after reading '\\r\\n' and '\\r'"
					+ " as '\\n': the stored line endings differ from the ones you passed.";
				case WHITESPACE -> did + " The anchor matched only after reading every run of"
					+ " whitespace as one space: the stored whitespace differs from the one you"
					+ " passed, and what was changed is the text as stored.";
			};
		}

		/** Whether the edit was applied. */
		public boolean applied() {
			return _failure == null;
		}

		/**
		 * The edited text, or <code>null</code> if the edit was refused.
		 */
		public String text() {
			return _text;
		}

		/**
		 * Why the edit was refused, or <code>null</code> if it was applied.
		 */
		public Failure failure() {
			return _failure;
		}

		/**
		 * What happened, in one or two sentences a language model can act on.
		 *
		 * <p>
		 * Set on success as well as on failure: a caller that fell back to a tolerant level has to be
		 * able to say so, because the author of the anchor is then wrong about what is stored.
		 * </p>
		 */
		public String message() {
			return _message;
		}

		/**
		 * The level of the cascade that found the anchor, or <code>null</code> if none did.
		 */
		public Match match() {
			return _match;
		}

		/** How many occurrences were replaced; {@code 0} on failure. */
		public int replacements() {
			return _replacements;
		}

		/**
		 * The 1-based line numbers, in the <em>original</em> text, at which the anchor was found.
		 *
		 * <p>
		 * Populated for an applied edit and for {@link Failure#AMBIGUOUS}, which is the case that needs
		 * them: the caller has to extend its anchor, and the line numbers say which occurrences it has
		 * to tell apart. Empty otherwise; never <code>null</code>.
		 * </p>
		 */
		public int[] lines() {
			return _lines.clone();
		}

		/**
		 * The offset in {@link #text()} at which the first change begins, or {@code -1} on failure.
		 *
		 * <p>
		 * For a caller that wants to show the changed region back without re-reading the whole value.
		 * </p>
		 */
		public int changeOffset() {
			return _changeOffset;
		}
	}

	/** How much of a text a diagnostic message may quote. */
	private static final int QUOTE_LENGTH = 60;

	/** How many line numbers {@link Failure#AMBIGUOUS} lists before it counts the rest. */
	private static final int MAX_LISTED_LINES = 10;

	private TextEdits() {
		// Static utility.
	}

	/**
	 * Replaces the one occurrence of {@code anchor} with {@code replacement}.
	 *
	 * @param source
	 *        The text to edit. <code>null</code> is read as the empty text - an attribute that was
	 *        never set and one set to {@code ""} are the same text to edit.
	 * @param anchor
	 *        The passage to find, character for character. <code>null</code> is read as empty.
	 * @param replacement
	 *        What to put there instead. <code>null</code> is read as empty, which deletes the
	 *        passage.
	 * @return The edited text, or why it was refused; never <code>null</code>.
	 */
	public static Result replace(String source, String anchor, String replacement) {
		return replace(source, anchor, replacement, Matching.TOLERANT);
	}

	/**
	 * Replaces the one occurrence of {@code anchor} with {@code replacement}.
	 *
	 * @param matching
	 *        How far the match cascade may go.
	 * @see #replace(String, String, String)
	 */
	public static Result replace(String source, String anchor, String replacement, Matching matching) {
		return edit(source, anchor, replacement, matching, false, Placement.INSTEAD);
	}

	/**
	 * Replaces every occurrence of {@code anchor} with {@code replacement}.
	 *
	 * <p>
	 * Occurrences are taken left to right and do not overlap. Ambiguity is not a failure here: asking
	 * for every occurrence is the caller saying it does not need to tell them apart.
	 * </p>
	 *
	 * @see #replace(String, String, String)
	 */
	public static Result replaceAll(String source, String anchor, String replacement) {
		return replaceAll(source, anchor, replacement, Matching.TOLERANT);
	}

	/**
	 * Replaces every occurrence of {@code anchor} with {@code replacement}.
	 *
	 * @param matching
	 *        How far the match cascade may go.
	 * @see #replaceAll(String, String, String)
	 */
	public static Result replaceAll(String source, String anchor, String replacement, Matching matching) {
		return edit(source, anchor, replacement, matching, true, Placement.INSTEAD);
	}

	/**
	 * Inserts {@code insertion} directly behind the one occurrence of {@code anchor}.
	 *
	 * <p>
	 * The anchor itself stays. This is the anchored append: the caller names a passage it can
	 * identify and adds text after it without re-emitting either.
	 * </p>
	 *
	 * @see #replace(String, String, String)
	 */
	public static Result insertAfter(String source, String anchor, String insertion) {
		return insertAfter(source, anchor, insertion, Matching.TOLERANT);
	}

	/**
	 * Inserts {@code insertion} directly behind the one occurrence of {@code anchor}.
	 *
	 * @param matching
	 *        How far the match cascade may go.
	 * @see #insertAfter(String, String, String)
	 */
	public static Result insertAfter(String source, String anchor, String insertion, Matching matching) {
		return edit(source, anchor, insertion, matching, false, Placement.AFTER);
	}

	/**
	 * Inserts {@code insertion} directly in front of the one occurrence of {@code anchor}.
	 *
	 * @see #insertAfter(String, String, String)
	 */
	public static Result insertBefore(String source, String anchor, String insertion) {
		return insertBefore(source, anchor, insertion, Matching.TOLERANT);
	}

	/**
	 * Inserts {@code insertion} directly in front of the one occurrence of {@code anchor}.
	 *
	 * @param matching
	 *        How far the match cascade may go.
	 * @see #insertBefore(String, String, String)
	 */
	public static Result insertBefore(String source, String anchor, String insertion, Matching matching) {
		return edit(source, anchor, insertion, matching, false, Placement.BEFORE);
	}

	/**
	 * The 1-based number of the line the given offset sits on.
	 *
	 * <p>
	 * Counts {@code \r\n}, {@code \n} and a lone {@code \r} as one line break each, so a value stored
	 * with either convention is numbered the way its author sees it.
	 * </p>
	 *
	 * @param text
	 *        The text to count in. <code>null</code> is read as empty.
	 * @param offset
	 *        A character offset into it.
	 * @return The line number, starting at {@code 1}.
	 */
	public static int lineOf(String text, int offset) {
		if (text == null) {
			return 1;
		}
		int limit = Math.min(offset, text.length());
		int line = 1;
		for (int i = 0; i < limit; i++) {
			char c = text.charAt(i);
			if (c == '\n') {
				line++;
			} else if (c == '\r') {
				line++;
				if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
					i++;
				}
			}
		}
		return line;
	}

	/** Where the payload goes relative to the matched range. */
	private enum Placement {
		/** The payload takes the matched range's place. */
		INSTEAD,

		/** The payload goes behind the matched range, which stays. */
		AFTER,

		/** The payload goes in front of the matched range, which stays. */
		BEFORE;
	}

	/**
	 * One matched range, in the coordinates of the original text.
	 */
	private record Range(int start, int end) {
		// Intentionally empty.
	}

	/**
	 * The one implementation every operation above is a spelling of.
	 */
	private static Result edit(String source, String anchor, String payload, Matching matching,
			boolean all, Placement placement) {
		String text = source == null ? "" : source;
		String needle = anchor == null ? "" : anchor;
		String insert = payload == null ? "" : payload;

		if (needle.isEmpty()) {
			if (!text.isEmpty()) {
				return Result.failed(Failure.EMPTY_ANCHOR,
					"The anchor is empty, so it addresses no passage of a text that is not empty."
						+ " Pass the exact passage to change, copied from the text as it is stored.",
					new int[0]);
			}
			// The empty anchor against the empty text: the one position that exists, matched once.
			return apply(text, List.of(new Range(0, 0)), insert, placement, Match.EXACT);
		}

		Match level = null;
		List<Range> found = List.of();
		for (Match candidate : levels(matching)) {
			List<Range> matches = find(text, needle, candidate);
			if (!matches.isEmpty()) {
				level = candidate;
				found = matches;
				break;
			}
		}

		if (level == null) {
			return Result.failed(Failure.NOT_FOUND, notFoundMessage(text, needle, matching), new int[0]);
		}
		if (!all && found.size() > 1) {
			return Result.failed(Failure.AMBIGUOUS, ambiguousMessage(text, found, level),
				linesOf(text, found));
		}
		return apply(text, found, insert, placement, level);
	}

	/**
	 * The cascade for the given setting.
	 */
	private static Match[] levels(Matching matching) {
		return matching == Matching.STRICT
			? new Match[] { Match.EXACT }
			: new Match[] { Match.EXACT, Match.LINE_ENDINGS, Match.WHITESPACE };
	}

	/**
	 * Every non-overlapping occurrence of {@code needle} in {@code text} at the given level, in the
	 * coordinates of {@code text}.
	 *
	 * <p>
	 * Always every occurrence, even when the caller asked for one: the count and the lines are what a
	 * caller whose anchor turned out to be ambiguous needs in order to extend it, and a scan that
	 * stopped at the second could report neither. One pass over a document costs nothing next to the
	 * round trip the caller is saved.
	 * </p>
	 */
	private static List<Range> find(String text, String needle, Match level) {
		if (level == Match.EXACT) {
			return scan(text, needle, null);
		}
		Normalized haystack = normalize(text, level);
		String normalized = normalizeAnchor(needle, level);
		if (normalized.isEmpty()) {
			// An anchor that is nothing but whitespace has no tolerant reading: at this level it would
			// match every gap in the text. Its exact reading was already tried and is the only one.
			return List.of();
		}
		return scan(haystack.text(), normalized, haystack);
	}

	/**
	 * Left-to-right, non-overlapping occurrences, mapped back through {@code coordinates} when the
	 * scan ran on a normalised text.
	 */
	private static List<Range> scan(String haystack, String needle, Normalized coordinates) {
		List<Range> result = new ArrayList<>();
		int from = 0;
		while (true) {
			int hit = haystack.indexOf(needle, from);
			if (hit < 0) {
				return result;
			}
			int end = hit + needle.length();
			result.add(coordinates == null
				? new Range(hit, end)
				: new Range(coordinates.startOf(hit), coordinates.endOf(end - 1)));
			from = end;
		}
	}

	/**
	 * Builds the edited text.
	 */
	private static Result apply(String text, List<Range> matches, String payload, Placement placement,
			Match level) {
		StringBuilder edited = new StringBuilder(text.length() + payload.length() * matches.size());
		int cursor = 0;
		int changeOffset = -1;
		for (Range match : matches) {
			edited.append(text, cursor, match.start());
			if (placement == Placement.AFTER) {
				edited.append(text, match.start(), match.end());
			}
			if (changeOffset < 0) {
				changeOffset = edited.length();
			}
			edited.append(payload);
			if (placement == Placement.BEFORE) {
				edited.append(text, match.start(), match.end());
			}
			cursor = match.end();
		}
		edited.append(text, cursor, text.length());
		return Result.applied(edited.toString(), level, describe(placement, matches.size()),
			matches.size(), linesOf(text, matches), changeOffset);
	}

	/**
	 * What the edit did, in one sentence, before the level of the match is appended to it.
	 */
	private static String describe(Placement placement, int count) {
		String occurrences = count == 1 ? "1 occurrence" : count + " occurrences";
		return switch (placement) {
			case INSTEAD -> "Replaced " + occurrences + ".";
			case AFTER -> "Inserted the text behind " + occurrences + " of the anchor.";
			case BEFORE -> "Inserted the text in front of " + occurrences + " of the anchor.";
		};
	}

	private static int[] linesOf(String text, List<Range> matches) {
		int[] lines = new int[matches.size()];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lineOf(text, matches.get(i).start());
		}
		return lines;
	}

	private static String ambiguousMessage(String text, List<Range> found, Match level) {
		int[] lines = linesOf(text, found);
		StringBuilder message = new StringBuilder();
		message.append("The anchor occurs ").append(found.size()).append(" times, on line");
		if (lines.length > 1) {
			message.append('s');
		}
		message.append(' ');
		for (int i = 0; i < Math.min(lines.length, MAX_LISTED_LINES); i++) {
			if (i > 0) {
				message.append(", ");
			}
			message.append(lines[i]);
		}
		if (lines.length > MAX_LISTED_LINES) {
			message.append(" and ").append(lines.length - MAX_LISTED_LINES).append(" more");
		}
		message.append(". Which one is meant is not decidable, so nothing was changed. Extend the"
			+ " anchor with surrounding text until it occurs once, or ask for every occurrence to be"
			+ " replaced.");
		if (level != Match.EXACT) {
			message.append(" Note that the anchor was matched ")
				.append(level == Match.LINE_ENDINGS ? "ignoring line endings" : "ignoring whitespace")
				.append(", so occurrences that differ only in whitespace are counted as well.");
		}
		return message.toString();
	}

	/**
	 * What to tell a caller whose anchor is nowhere in the text.
	 *
	 * <p>
	 * "Not found" alone costs a whole round trip to learn nothing, so the longest prefix of the anchor
	 * that <em>does</em> occur is reported with the text that follows it: that is the point at which
	 * the caller's idea of the text and the text itself part company, which is the one fact needed to
	 * fix the anchor in a single further step.
	 * </p>
	 */
	private static String notFoundMessage(String text, String needle, Matching matching) {
		String haystack = text;
		String anchor = needle;
		Normalized coordinates = null;
		if (matching != Matching.STRICT) {
			Normalized normalized = normalize(text, Match.WHITESPACE);
			String normalizedAnchor = normalizeAnchor(needle, Match.WHITESPACE);
			if (!normalizedAnchor.isEmpty()) {
				// An anchor of nothing but whitespace normalises away; comparing it to the text would
				// find agreement everywhere. It is diagnosed against the text as stored instead.
				haystack = normalized.text();
				anchor = normalizedAnchor;
				coordinates = normalized;
			}
		}
		int agreed = longestPrefix(haystack, anchor);
		if (agreed == 0) {
			return "The anchor does not occur in the text, and neither does its beginning."
				+ " Read the value first and copy the passage to change out of it.";
		}
		int at = haystack.indexOf(anchor.substring(0, agreed));
		int sourceOffset = coordinates == null ? at : coordinates.startOf(at);
		String continues = haystack.substring(Math.min(at + agreed, haystack.length()));
		return "The anchor does not occur in the text. It agrees with the text up to '"
			+ quote(anchor.substring(0, agreed), true) + "' (line " + lineOf(text, sourceOffset)
			+ ") and differs after that: there the text continues with '" + quote(continues, false)
			+ "'. Read the value and copy the passage to change out of it.";
	}

	/**
	 * The length of the longest prefix of {@code needle} that occurs in {@code haystack}.
	 *
	 * <p>
	 * Binary search rather than a scan: the property is monotone - if a prefix of length {@code k}
	 * occurs then so does every shorter one - and the texts this runs on are whole documents.
	 * </p>
	 */
	private static int longestPrefix(String haystack, String needle) {
		int low = 0;
		int high = needle.length();
		while (low < high) {
			int mid = (low + high + 1) / 2;
			if (haystack.contains(needle.substring(0, mid))) {
				low = mid;
			} else {
				high = mid - 1;
			}
		}
		return low;
	}

	/**
	 * At most {@link #QUOTE_LENGTH} characters of {@code value}, on one line.
	 *
	 * @param fromEnd
	 *        Whether to keep the end of the value rather than its beginning - which is what a caller
	 *        reading "agrees up to here" needs to see.
	 */
	private static String quote(String value, boolean fromEnd) {
		String cut;
		if (value.length() <= QUOTE_LENGTH) {
			cut = value;
		} else if (fromEnd) {
			cut = "..." + value.substring(value.length() - QUOTE_LENGTH);
		} else {
			cut = value.substring(0, QUOTE_LENGTH) + "...";
		}
		return cut.replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n").replace("\t", "\\t");
	}

	/**
	 * A text with every character's origin in the text it was normalised from.
	 *
	 * @param text
	 *        The normalised text.
	 * @param start
	 *        Per normalised character, the offset in the original at which it begins.
	 * @param end
	 *        Per normalised character, the offset in the original just past what it consumed. Differs
	 *        from the next {@code start} exactly where characters were dropped, which is why both are
	 *        kept: a match must not drag the whitespace behind it into the replaced range.
	 */
	private record Normalized(String text, int[] start, int[] end) {

		int startOf(int index) {
			return start[index];
		}

		int endOf(int index) {
			return end[index];
		}
	}

	/**
	 * The anchor at the given level, with its own outer whitespace dropped at the whitespace level.
	 *
	 * <p>
	 * Dropping it is what makes "tolerate trailing whitespace" true: an anchor a caller cut out of a
	 * rendering carries the indentation of its first line and the newline after its last, and neither
	 * is part of the passage it means.
	 * </p>
	 */
	private static String normalizeAnchor(String anchor, Match level) {
		String normalized = normalize(anchor, level).text();
		return level == Match.WHITESPACE ? normalized.strip() : normalized;
	}

	/**
	 * Normalises {@code source} to the given level, keeping every character's origin.
	 */
	private static Normalized normalize(String source, Match level) {
		int length = source.length();
		StringBuilder text = new StringBuilder(length);
		int[] start = new int[length];
		int[] end = new int[length];
		int i = 0;
		while (i < length) {
			char c = source.charAt(i);
			int consumed;
			char emitted;
			if (level == Match.WHITESPACE && isSpace(c)) {
				int run = i;
				while (run < length && isSpace(source.charAt(run))) {
					run++;
				}
				consumed = run - i;
				emitted = ' ';
			} else if (c == '\r') {
				consumed = i + 1 < length && source.charAt(i + 1) == '\n' ? 2 : 1;
				emitted = '\n';
			} else {
				consumed = 1;
				emitted = c;
			}
			start[text.length()] = i;
			end[text.length()] = i + consumed;
			text.append(emitted);
			i += consumed;
		}
		return new Normalized(text.toString(), start, end);
	}

	/**
	 * Whether the character is whitespace for the purpose of the tolerant level.
	 *
	 * <p>
	 * Both questions, not just {@link Character#isWhitespace(char)}: that one answers {@code false}
	 * for the non-breaking space and its relatives, which are precisely the characters a text that
	 * passed through a word processor is full of and which no author of an anchor means to be
	 * different from a space.
	 * </p>
	 */
	private static boolean isSpace(char c) {
		return Character.isWhitespace(c) || Character.isSpaceChar(c);
	}
}
