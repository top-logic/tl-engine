/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.model.TLObject;

/**
 * {@link Associations} implementation which stores that link within a set.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class AssociationCollection<T extends TLObject> extends Associations<T, Set<T>> {

	/**
	 * The cached objects.
	 * 
	 * <p>
	 * Must be treated unmodifiable after {@link #_bufferPublished} to prevent concurrent
	 * modification exceptions in customer code that iterates of the results and e.g. deletes
	 * contained objects.
	 * </p>
	 */
	private Set<T> _buffer;

	/**
	 * Unmodifiable variant of {@link #_buffer}.
	 */
	private Set<T> _bufferView;

	/**
	 * Whether a reference to {@link #_bufferView} (and therefore implicitly a reference to
	 * {@link #_buffer}) has been handed out and must no longer change spontaneously to prevent
	 * concurrent modification exceptions in customer code.
	 */
	private boolean _bufferPublished;

	public AssociationCollection(AssociationSetCache<T> associationCache, long minValidity,
			long maxValidity, Set<T> associationBuffer, boolean localCache) {
		super(associationCache, minValidity, maxValidity, localCache);
		initBuffer(associationBuffer);
	}

	@Override
	public synchronized Set<T> getAssociations() {
		_bufferPublished = true;
		return _bufferView;
	}

	@Override
	protected Iterable<T> getAssociationItems() {
		return getAssociations();
	}

	@Override
	protected synchronized boolean addLinkResolved(T link) {
		copyBufferOnWrite();
		return _buffer.add(link);
	}

	@Override
	protected synchronized boolean removeLinkResolved(TLObject link) {
		copyBufferOnWrite();
		return _buffer.remove(link);
	}

	private void copyBufferOnWrite() {
		if (_bufferPublished) {
			copyBuffer();
			_bufferPublished = false;
		}
	}

	private void copyBuffer() {
		initBuffer(new HashSet<>(_buffer));
	}
	
	private void initBuffer(Set<T> associationBuffer) {
		_buffer = associationBuffer;
		_bufferView = Collections.unmodifiableSet(associationBuffer);
	}

}

