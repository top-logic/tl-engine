/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.search;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Convenience class for search implementations.
 * <p>
 * Has convenience methods for checking the search result size.
 * </p>
 * <p>
 * Use {@link #addCandidate(Object)} to get the list of all candidates in error messages. The
 * candidates can have a different type than the results. Example: Objects are searched by labels.
 * In this case, adding the labels as candidates can lead to a better error message than adding the
 * objects themselves.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class SearchResult<T> {

	private static final String ERROR_MESSAGE_LIST_ENTRY_SEPARATOR = ", ";

	private List<T> _results = Collections.emptyList();

	private Collection<Object> _candidates = list();

	/**
	 * Adds the given result.
	 * 
	 * @param entry
	 *        Is allowed to be <code>null</code>.
	 */
	public void add(T entry) {
		if (_results.isEmpty()) {
			// Many searches return exactly one result,
			// therefore this implementation is (memory) optimized for that case.
			_results = Collections.singletonList(entry);
			return;
		}
		if (_results.size() == 1) {
			ArrayList<T> multiResultList = new ArrayList<>(4);
			multiResultList.add(_results.get(0));
			_results = multiResultList;
		}
		_results.add(entry);
	}

	/** Calls {@link #add(Object)} if the condition is <code>true</code>. */
	public void addIf(boolean condition, T entry) {
		if (condition) {
			add(entry);
		}
	}

	/**
	 * Candidates are used for error messages:
	 * <p>
	 * If the object is not found, all the candidates are written into the error message.
	 * </p>
	 */
	public void addCandidate(Object candidate) {
		_candidates.add(candidate);
	}

	/**
	 * Returns the {@link List} of search results.
	 * <p>
	 * The internal list is returned, which <b>might be unmodifiable, but might also change</b> if
	 * further search results are added via {@link #add(Object)}.
	 * </p>
	 * 
	 * @return Never <code>null</code>, but might contain <code>null</code>, if {@link #add(Object)}
	 *         is called with <code>null</code> as parameter.
	 */
	public List<T> getResults() {
		return _results;
	}

	/**
	 * Convenience short-cut for {@link #getResult(Object, String)} with <code>null</code> as
	 * default value.
	 */
	public T getResultNullable(String errorPrefix) {
		return getResult(null, errorPrefix);
	}

	/**
	 * Convenience short-cut that calls {@link #checkAtMostOneResult(String)} before returning the
	 * first result, or the default value, if there is no result.
	 */
	public T getResult(T defaultValue, String errorPrefix) {
		checkAtMostOneResult(errorPrefix);
		if (_results.isEmpty()) {
			return defaultValue;
		}
		return _results.get(0);
	}

	/**
	 * Convenience short-cut that calls {@link #checkSingleResult(String)} before returning the
	 * single result.
	 */
	public T getSingleResult(String errorPrefix) {
		checkSingleResult(errorPrefix);
		return _results.get(0);
	}

	/** Throws an {@link IllegalStateException} if at least one result has been found. */
	public void checkEmpty(String errorPrefix) {
		if (!_results.isEmpty()) {
			fail(true, errorPrefix, " Expected to find nothing but found ", _results.size(), " results: ");
		}
	}

	/** Throws an {@link IllegalStateException} if no result has been found. */
	public void checkNonEmpty(String errorPrefix) {
		if (_results.isEmpty()) {
			fail(false, errorPrefix, "Expected at least one result but found nothing.");
		}
	}

	/** Throws an {@link IllegalStateException} if no or multiple results have been found. */
	public void checkSingleResult(String errorPrefix) {
		if (_results.isEmpty()) {
			fail(false, errorPrefix, " Expected to find exactly one result but found nothing.");
		}
		if (_results.size() > 1) {
			fail(true, errorPrefix,
				" Expected to find exactly one result but found ", _results.size(), " results: ");
		}
	}

	/** Throws an {@link IllegalStateException} if multiple results have been found. */
	public void checkAtMostOneResult(String errorPrefix) {
		if (_results.size() > 1) {
			fail(true, errorPrefix,
				" Expected to find at most one result but found ", _results.size(), " results: ");
		}
	}

	private void fail(boolean appendResults, Object... messageParts) {
		StringBuilder errorMessage = new StringBuilder();
		for (Object messagePart : messageParts) {
			errorMessage.append(messagePart);
		}
		if (appendResults) {
			appendResults(errorMessage);
		}
		errorMessage.append(" Candidates: '");
		errorMessage.append(join(_candidates, "', '"));
		errorMessage.append("'");
		throw new IllegalStateException(errorMessage.toString());
	}

	private void appendResults(StringBuilder resultDetail) {
		for (T result : _results) {
			resultDetail.append(debug(result));
			resultDetail.append(ERROR_MESSAGE_LIST_ENTRY_SEPARATOR);
		}
		if (!_results.isEmpty()) {
			resultDetail.delete(
				resultDetail.length() - ERROR_MESSAGE_LIST_ENTRY_SEPARATOR.length(), resultDetail.length());
		}
		resultDetail.append(".");
	}

}
