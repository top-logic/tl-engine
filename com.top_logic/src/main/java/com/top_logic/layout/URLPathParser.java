/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * The class {@link URLPathParser} parses a URL formerly build using a
 * {@link URLPathBuilder}.
 * 
 * @see URLPathBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class URLPathParser implements URLParser, Cloneable {

	/** Character that is used to separate path resources. */
	public final static char PATH_SEPARATOR_CHAR = '/';

	/** String containing {@link #PATH_SEPARATOR_CHAR} */
	public final static String PATH_SEPARATOR = "" + PATH_SEPARATOR_CHAR;

	/**
	 * points to the next '/' in the given url
	 */
	private int pointer;
	private final String url;

	private URLPathParser(String url, int pointer) {
		this.url = url;
		this.pointer = pointer;
	}

	@Override
	public String removeResource() {
		return get(true);
	}

	/**
	 * Returns the part in the {@link #url} beginning with the character directly after the last
	 * {@link #PATH_SEPARATOR} in {@link #url}.
	 * 
	 * <p>
	 * Note: the resource must not be decoded like in
	 * {@link URLPathBuilder#addResource(CharSequence)} because decoding is done by the container.
	 * </p>
	 * 
	 * @param remove
	 *        whether the pointer shall be updated to the next '/'
	 * 
	 * @throws IllegalStateException
	 *         if the url is empty
	 */
	private String get(boolean remove) throws IllegalStateException {
		if (isEmpty()) {
			throw new IllegalStateException("No more elements.");
		}
		final int resultStartIndex = pointer + 1;
		int end = url.indexOf(PATH_SEPARATOR, resultStartIndex);
		if (end == -1) {
			// In very special cases, the URL might have parameters. Those must not be delivered as
			// path components. This happens, e.g. if a ZIP download is triggered from an
			// interactive script execution. The download URL gets a paramter appended to prevent
			// (additional) compression by the container. When the download URL is parsed by the
			// browser window control, this parameter is still present but must not be interpreted
			// by the control.
			end = url.indexOf('?', resultStartIndex);
			if (end == -1) {
				end = url.length();
			}

			// Even if a parameter separator was hit, the URL has no more components.
			if (remove) {
				pointer = url.length();
			}
		} else {
			if (remove) {
				pointer = end;
			}
		}
		return url.substring(resultStartIndex, end);
	}

	@Override
	public String getResource() {
		return get(false);
	}

	@Override
	public boolean isEmpty() {
		return pointer >= url.length();
	}

	/**
	 * Creates a {@link URLPathParser} from the given request path (excluding the servlet path and
	 * the context path of the application).
	 * 
	 * It may result in an empty {@link URLParser}.
	 */
	public static URLPathParser createURLPathParser(final String pathInfo) {
		if (pathInfo == null) {
			URLPathParser result = new URLPathParser("", 0);
			return result;
		}
		return new URLPathParser(pathInfo, 0);
	}
	
	@Override
	public String toString() {
		return URLPathParser.class.getName() + "[url:" + url + "]";
	}
	
	@Override
	public URLPathParser clone() {
		return new URLPathParser(url, pointer);
	}

	/**
	 * Resets the parser to its initial state after construction.
	 * 
	 * <p>
	 * This operation reverts all {@link #removeResource()} operations.
	 * </p>
	 * 
	 * @return This instance for method chaining. 
	 */
	public URLPathParser reset() {
		if (this.url != null) {
			this.pointer = 0;
		}
		return this;
	}
}
