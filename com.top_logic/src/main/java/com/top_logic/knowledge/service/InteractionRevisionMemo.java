/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.function.Function;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Access to a memo of values derived from the committed state of a {@link KnowledgeBase}, kept for
 * the current interaction.
 * <p>
 * A memo is created on demand by a factory and stored in the current {@link InteractionContext}.
 * It is discarded together with the interaction, and it is replaced as soon as the knowledge base
 * has a newer {@link HistoryManager#getLastRevision() last revision} than the one it was created
 * for. No memo is offered while the current thread has uncommitted changes in the knowledge base,
 * since such changes are not part of the committed state the memo is derived from.
 * </p>
 *
 * @param <T>
 *        The type of the memo.
 */
public final class InteractionRevisionMemo<T> {

	private final Property<Entry<T>> _property;

	private final Function<? super KnowledgeBase, ? extends T> _factory;

	/**
	 * Creates a {@link InteractionRevisionMemo}.
	 *
	 * @param name
	 *        The name of the property under which the memo is stored in the interaction, for
	 *        debugging.
	 * @param factory
	 *        Creates an empty memo for a knowledge base.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InteractionRevisionMemo(String name, Function<? super KnowledgeBase, ? extends T> factory) {
		_property = (Property) TypedAnnotatable.property(Entry.class, name);
		_factory = factory;
	}

	/**
	 * The memo of the current interaction for the current revision of the given knowledge base.
	 *
	 * @return <code>null</code> when there is no interaction, or when the current thread has
	 *         uncommitted changes in the given knowledge base. Values must then be computed
	 *         directly.
	 */
	public T current(KnowledgeBase kb) {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return null;
		}
		if (!(kb instanceof DBKnowledgeBase dbKB) || dbKB.hasUncommittedChanges()) {
			return null;
		}
		long revision = kb.getHistoryManager().getLastRevision();
		Entry<T> entry = interaction.get(_property);
		if (entry == null || entry._kb != kb || entry._revision != revision) {
			entry = new Entry<>(kb, revision, _factory.apply(kb));
			interaction.set(_property, entry);
		}
		return entry._memo;
	}

	/**
	 * A memo together with the state it was created for.
	 */
	private static final class Entry<T> {

		final KnowledgeBase _kb;

		final long _revision;

		final T _memo;

		Entry(KnowledgeBase kb, long revision, T memo) {
			_kb = kb;
			_revision = revision;
			_memo = memo;
		}

	}

}
