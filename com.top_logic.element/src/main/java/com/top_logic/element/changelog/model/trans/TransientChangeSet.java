/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog.model.trans;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.TlChangelogFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientTLObjectImpl;

/**
 * Transient {@link ChangeSet}.
 */
public class TransientChangeSet extends TransientTLObjectImpl implements ChangeSet {

	private Consumer<? super TransientChangeSet> _initializer;

	private Collection<String> _uninitialized;

	/** 
	 * Creates a {@link TransientChangeSet}.
	 */
	public TransientChangeSet() {
		this(null);
	}

	/**
	 * Creates a {@link TransientChangeSet}.
	 * 
	 * @param initializer
	 *        Initializer for this {@link ChangeSet}. The initializer is called when one of the
	 *        attributes in <code>uninitialized</code> is accessed.
	 * @param uninitialized
	 *        Names of the attributes whose access triggers the initializer call.
	 */
	public TransientChangeSet(Consumer<? super TransientChangeSet> initializer, String... uninitialized) {
		super(TlChangelogFactory.getChangeSetType(), null);
		_initializer = initializer;
		_uninitialized = Set.of(uninitialized);
	}

	/**
	 * {@link Consumer} that is called at the first call to {@link #tValue(TLStructuredTypePart)}.
	 * 
	 * @return May be <code>null</code>, either when the {@link ChangeSet} has no initializer or was
	 *         already initialized.
	 */
	public Consumer<? super TransientChangeSet> initializer() {
		return _initializer;
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		Consumer<? super TransientChangeSet> initializer = initializer();
		if (initializer != null && _uninitialized.contains(part.getName())) {
			// Ensure no stack overflow, when the initializer calls this method.
			_initializer = null;
			try {
				initializer.accept(this);
			} catch (Throwable ex) {
				// Initializing failed. Retry?
				_initializer = initializer;
			}
		}
		return super.tValue(part);
	}

}
