/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.regex;

import java.util.regex.Matcher;

import com.top_logic.util.error.TopLogicException;

/**
 * A match of a {@link Regex} pattern expression.
 * 
 * @see RegexSearch
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Match {

	private final String _text;

	private final Match.Group[] _groups;

	/**
	 * A region in a {@link Match}.
	 */
	public static class Group {

		private final int _start;

		private final int _end;

		/**
		 * Creates a {@link Group}.
		 */
		public Group(int start, int end) {
			_start = start;
			_end = end;
		}

		/**
		 * Start position of the group match.
		 */
		public int getStart() {
			return _start;
		}

		/**
		 * End position of the group match.
		 */
		public int getEnd() {
			return _end;
		}

	}

	/**
	 * Creates a {@link Match}.
	 */
	public Match(String text, Match.Group[] groups) {
		_text = text;
		_groups = groups;
	}

	@Override
	public String toString() {
		return value(0);
	}

	/**
	 * The text in which the match was found.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * The text of the matched group with the given ID.
	 */
	public String value(int groupId) {
		Match.Group group = group(groupId);
		int start = group.getStart();
		int end = group.getEnd();
		if (start < 0) {
			// No match.
			return null;
		}
		return _text.substring(start, end);
	}

	/**
	 * The match group with the given ID.
	 */
	public Match.Group group(int groupId) {
		if (groupId >= _groups.length || groupId < 0) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_GROUP__ID_CNT.fill(groupId, _groups.length));
		}
		return _groups[groupId];
	}

	/**
	 * Creates a {@link Match} from the given {@link Matcher} state.
	 */
	public static Match from(String text, Matcher matcher) {
		int groupCount = matcher.groupCount() + 1;
		Match.Group[] groups = new Match.Group[groupCount];
		groups[0] = new Group(matcher.start(), matcher.end());
		for (int n = 1; n < groupCount; n++) {
			groups[n] = new Group(matcher.start(n), matcher.end(n));
		}
		return new Match(text, groups);
	}

}