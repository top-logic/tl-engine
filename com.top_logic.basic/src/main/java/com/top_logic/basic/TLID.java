/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.annotation.Format;

/**
 * Internal object identifier.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(IDFormat.class)
public abstract class TLID implements Comparable<TLID> {

	/**
	 * Mapping to convert {@link TLID}s to their {@link TLID#toStorageValue()}.
	 */
	public static final Mapping<TLID, Object> STORAGE_VALUE_MAPPING = new Mapping<>() {
		@Override
		public Object map(TLID input) {
			return input.toStorageValue();
		}
	};

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Primitive value that can be stored to a database column.
	 * 
	 * <p>
	 * To create an identifier from its storage value, the concrete implementation class must be
	 * known.
	 * </p>
	 */
	public abstract Object toStorageValue();

	/**
	 * {@link String} representation of this identifier that can be parsed back to a {@link TLID}
	 * instance, if the concrete implementation class is known.
	 * 
	 * <p>
	 * For string concatenation, use {@link #appendExternalForm(Appendable)} for best efficiency.
	 * </p>
	 * 
	 * @see LongID#fromExternalForm(String)
	 * @see #appendExternalForm(Appendable)
	 */
	public abstract String toExternalForm();

	/**
	 * Efficiently appends {@link #toExternalForm()} to the given writer.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	public abstract void appendExternalForm(Appendable out) throws IOException;

	@Override
	public final String toString() {
		return "ID(" + toExternalForm() + ")";
	}

}
