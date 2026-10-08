/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The {@link DragSourceControl#dragKind() kinds} of the drags a {@link DropTarget} accepts.
 *
 * <p>
 * A target either accepts {@link #any() any} drag - one of any kind as well as one without a kind -
 * or exactly the drags whose kind is one of the {@link #kinds() listed} ones; a drag without a kind
 * is then refused. With no kind listed, nothing is accepted (see {@link #NONE}).
 * </p>
 *
 * <p>
 * Kinds are application-level names compared literally, so a source and a target must agree on
 * their spelling.
 * </p>
 *
 * @param any
 *        Whether every drag is accepted, with any kind or none.
 * @param kinds
 *        The kinds of the accepted drags; empty if {@code any} is set.
 */
public record AcceptedKinds(boolean any, Set<String> kinds) {

	/** Accepts every drag, with any kind or none. */
	public static final AcceptedKinds ANY = new AcceptedKinds(true, Set.of());

	/** Accepts no drag at all. */
	public static final AcceptedKinds NONE = new AcceptedKinds(false, Set.of());

	/**
	 * Creates an {@link AcceptedKinds}, keeping the order of the given kinds.
	 */
	public AcceptedKinds {
		kinds = any ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(kinds));
	}

	/**
	 * Accepts exactly the drags of the given kinds.
	 *
	 * @param kinds
	 *        The accepted kinds; empty accepts nothing.
	 */
	public static AcceptedKinds of(Collection<String> kinds) {
		return kinds.isEmpty() ? NONE : new AcceptedKinds(false, new LinkedHashSet<>(kinds));
	}

	/**
	 * Whether a drag of the given kind is accepted.
	 *
	 * @param kind
	 *        The {@link DragSourceControl#dragKind() kind} of the drag, {@code null} for a drag
	 *        without a kind.
	 */
	public boolean accepts(String kind) {
		return any || (kind != null && kinds.contains(kind));
	}

	/**
	 * Whether no drag at all is accepted.
	 */
	public boolean isNone() {
		return !any && kinds.isEmpty();
	}

	/**
	 * Accepts what this or the given {@link AcceptedKinds} accepts.
	 */
	public AcceptedKinds union(AcceptedKinds other) {
		if (any || other.isNone()) {
			return this;
		}
		if (other.any || isNone()) {
			return other;
		}
		Set<String> union = new LinkedHashSet<>(kinds);
		union.addAll(other.kinds);
		return new AcceptedKinds(false, union);
	}

}
