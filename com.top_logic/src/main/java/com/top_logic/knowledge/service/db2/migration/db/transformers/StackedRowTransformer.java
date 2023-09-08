/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Iterator;

import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;

/**
 * {@link RowWriter} delivering a {@link RowValue} through a {@link RowTransformer} to different
 * {@link RowWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StackedRowTransformer implements RowWriter {

	private final RowTransformer _transformer;

	private final RowWriter _out;

	StackedRowTransformer(RowTransformer transformer, RowWriter out) {
		_transformer = transformer;
		_out = out;
	}

	@Override
	public void write(RowValue row) {
		_transformer.transform(row, _out);
	}

	/**
	 * @see #createRowWriter(Iterator, RowWriter)
	 */
	public static RowWriter createRowWriter(Iterable<RowTransformer> transformers, RowWriter out) {
		return createRowWriter(transformers.iterator(), out);
	}

	/**
	 * Creates a {@link RowWriter} that passes a {@link RowValue} through all given
	 * {@link RowTransformer} and finally delivers the {@link RowValue} to the given
	 * {@link RowWriter}.
	 */
	public static RowWriter createRowWriter(Iterator<RowTransformer> iterator, RowWriter out) {
		if (iterator.hasNext()) {
			RowTransformer transformer = iterator.next();
			return new StackedRowTransformer(transformer, createRowWriter(iterator, out));
		} else {
			return out;
		}
	}

}
