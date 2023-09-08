/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.basic.col.Mapping;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.PropertyAccessor;

/**
 * {@link PropertyAccessor} that maps a {@link DocumentVersion} to its {@link Document} and
 * delegates all to an inner {@link PropertyAccessor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentAccessor implements PropertyAccessor<Object> {

	/**
	 * Mapping that maps a {@link DocumentVersion} to its {@link DocumentVersion#getDocument()
	 * document}. Other inputs are returned as given.
	 */
	public static Mapping<Object, Object> DOCUMENT_MAPPING = new Mapping<>() {

		@Override
		public Object map(Object input) {
			if (input instanceof DocumentVersion) {
				return ((DocumentVersion) input).getDocument();
			}
			return input;
		}
	};

	private final PropertyAccessor<Object> _inner;

	/**
	 * Creates a new {@link DocumentAccessor}.
	 * 
	 * @param inner
	 *        The accessor to dispatch to.
	 */
	public DocumentAccessor(PropertyAccessor<Object> inner) {
		_inner = inner;
	}

	@Override
	public Object getValue(Object target) {
		return _inner.getValue(DOCUMENT_MAPPING.map(target));
	}

	@Override
	public void setValue(Object target, Object newValue) {
		_inner.setValue(DOCUMENT_MAPPING.map(target), newValue);
	}

}

