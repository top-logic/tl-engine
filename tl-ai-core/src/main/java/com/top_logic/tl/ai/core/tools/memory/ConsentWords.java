/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.memory;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Whether a piece of text a person wrote contains one of a set of words.
 *
 * <p>
 * The one matching rule {@link MemoryConsent} is built on, in one place so that "the user asked to
 * remember something", "the user agreed" and "the user declined" are all decided the same way.
 * </p>
 *
 * <h2>What it does and does not claim</h2>
 *
 * <p>
 * This is a check on <em>form</em>, not on meaning: it can say that the user raised the subject of
 * remembering in this turn, never that the sentence the agent is about to store is the sentence the
 * user meant. Nothing here tries to. The coarse check is still the whole point - it is what keeps
 * an agent from writing a memory in a turn where the user said nothing about memory at all, which
 * is the failure the ticket is about.
 * </p>
 */
public class ConsentWords {

	/** Combining marks, removed after the decomposition below. */
	private static final Pattern MARKS = Pattern.compile("\\p{M}+");

	/** Every run of whitespace, collapsed so a multi-word entry matches across a line break. */
	private static final Pattern SPACES = Pattern.compile("\\s+");

	private ConsentWords() {
		// Static utility.
	}

	/**
	 * Whether the text uses one of the given words.
	 *
	 * <p>
	 * Case and diacritics are ignored, so "Merk" matches "merk" and "MERKE" matches "merke". The
	 * match is on word boundaries of letters and digits, so "merken" does not fire inside
	 * "anmerkenswert" and "ja" does not fire inside "Jahr". A word may be several words long
	 * ("keep in mind"); a run of whitespace in the text matches the single space in it.
	 * </p>
	 *
	 * @param text
	 *        What the person wrote; {@code null} or blank uses no word.
	 * @param words
	 *        The words to look for; {@code null} or empty is used by nothing.
	 */
	public static boolean uses(String text, List<String> words) {
		if (text == null || text.isBlank() || words == null || words.isEmpty()) {
			return false;
		}
		String normalized = normalize(text);
		for (String word : words) {
			if (word == null || word.isBlank()) {
				continue;
			}
			if (contains(normalized, normalize(word))) {
				return true;
			}
		}
		return false;
	}

	private static boolean contains(String text, String word) {
		if (word.isEmpty()) {
			return false;
		}
		Matcher matcher = Pattern
			.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(word) + "(?![\\p{L}\\p{N}])")
			.matcher(text);
		return matcher.find();
	}

	/** Lower case, without diacritics, with every run of whitespace collapsed to one space. */
	private static String normalize(String text) {
		String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
		String withoutMarks = MARKS.matcher(decomposed).replaceAll("");
		return SPACES.matcher(withoutMarks).replaceAll(" ").toLowerCase(Locale.ROOT).strip();
	}
}
