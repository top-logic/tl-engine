/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.io.Reader;
import java.util.Collection;
import java.util.Date;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClassifier;

/**
 * Builder for the full-text representation of an object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FullTextBuBuffer {

	/**
	 * Adds the given value to this full-text representation.
	 */
	void add(CharSequence text);

	/**
	 * Adds the given value to this full-text representation.
	 */
	void add(Date value);

	/**
	 * Adds the given value to this full-text representation.
	 */
	void add(ResKey key);

	/**
	 * Adds the given value to this full-text representation.
	 */
	void add(TLClassifier classifier);

	/**
	 * Adds the given value to this full-text representation.
	 */
	void add(FullTextSearchable searchable);

	/**
	 * Adds the given value to this full-text representation.
	 */
	void add(Collection<?> value);

	/**
	 * Adds the contents of the given {@link Reader} to this full-text representation.
	 */
	void add(Reader reader);

	/**
	 * Adds the given object to this full-text representation.
	 * 
	 * <p>
	 * This method is a short-cut for checking the type of the given value and calling one of the
	 * {@link #add(CharSequence)}, {@link #add(Date)},... methods.
	 * </p>
	 * 
	 * <p>
	 * Note: If The given value is a persistent object, then it most likely implements
	 * {@link FullTextSearchable} itself. This will cause {@link #add(FullTextSearchable)} to be
	 * called. This adds the complete full-text of the given value to the full-text of the current
	 * object. Most probably this is not what you want. Call {@link #add(CharSequence)} with the
	 * label of the persistent object, instead, see {@link #genericAddLabel(Object)}.
	 * </p>
	 */
	void genericAddValue(Object value);

	/**
	 * Adds the label fo the given value/object to the currently generated full-text.
	 * 
	 * <p>
	 * In contrast to {@link #genericAddValue(Object)}, this method is safe to be called with
	 * arbitrary values including persistent objects.
	 * </p>
	 */
	void genericAddLabel(Object value);

}
