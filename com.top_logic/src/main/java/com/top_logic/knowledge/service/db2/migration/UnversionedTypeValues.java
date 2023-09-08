/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.Mappings;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;

/**
 * Iterator computing the {@link RowValue} from the {@link Wrapper} of a {@link KnowledgeObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class UnversionedTypeValues<T extends KnowledgeItem> extends AbstractIterator<T> implements CloseableIterator<T> {

	private static final int CHUNK_SIZE = 50;

	private Iterator<T> _values = noMoreValues();

	private final Iterator<T> _searchResult;

	private PreloadContext _preloadContext = new PreloadContext();

	public UnversionedTypeValues(Iterator<T> searchResult) {
		_searchResult = searchResult;
		readChunk();
	}

	private void readChunk() {
		// Old values are not longer needed
		_preloadContext.close();

		List<T> items = new ArrayList<>();
		int i = 0;
		while (i++ < CHUNK_SIZE && _searchResult.hasNext()) {
			items.add(_searchResult.next());
		}
		if (items.isEmpty()) {
			_values = noMoreValues();
		} else {
			_preloadContext = new PreloadContext();
			List<TLObject> wrappers = Mappings.map(item -> item.getWrapper(), items);
			FlexAttributeFetch.INSTANCE.prepare(_preloadContext, wrappers);
			_values = items.iterator();
		}
	}

	@Override
	protected T computeNext() {
		return getObject();
	}

	private T getObject() {
		if (_values == UnversionedTypeValues.<T> noMoreValues()) {
			return null;
		}
		if (_values.hasNext()) {
			return _values.next();
		} else {
			readChunk();
			return getObject();
		}
	}

	@Override
	public void close() {
		_preloadContext.close();
	}

	private static <T> Iterator<T> noMoreValues() {
		return Collections.emptyIterator();
	}
}
