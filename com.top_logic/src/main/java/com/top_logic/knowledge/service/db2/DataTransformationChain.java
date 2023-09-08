/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.service.db2.TransformingFlexDataManager.DataTransformation;

/**
 * {@link DataTransformation} that chains two {@link DataTransformation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DataTransformationChain implements DataTransformation {

	private final DataTransformation head;

	private final DataTransformation next;

	/**
	 * Creates a {@link DataTransformationChain}.
	 * 
	 * @param head
	 *        The first transformation.
	 * @param next
	 *        The {@link DataTransformation} that encodes the result of the first one.
	 */
	public DataTransformationChain(DataTransformation head, DataTransformation next) {
		this.head = head;
		this.next = next;
	}

	@Override
	public FlexData encode(String typeName, FlexData values) {
		return next.encode(typeName, head.encode(typeName, values));
	}

	@Override
	public FlexData decode(FlexData values, boolean mutable) {
		return head.decode(next.decode(values, mutable), mutable);
	}

}
