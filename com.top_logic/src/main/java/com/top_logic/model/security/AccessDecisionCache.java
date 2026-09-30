/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Memo of the access decisions made during one interaction.
 * <p>
 * An access check decides once per person, object and operation; a second check of the same
 * combination within the interaction answers from the memo. Objects delegating their decision to
 * an access parent profit most: the decision on the parent is stored once and serves every object
 * delegating to it.
 * </p>
 * <p>
 * While a decision is being computed, its entry is marked. A delegation chain that reaches an
 * object whose decision is marked runs in a cycle and is denied instead of recursing forever.
 * </p>
 * <p>
 * The memo is discarded together with the interaction, and it is replaced as soon as the knowledge
 * base has a newer revision than the one it was created for, so that a command committing a change
 * does not render its result from decisions made before the change.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class AccessDecisionCache {

	/**
	 * A person, object and operation whose decision is stored.
	 * 
	 * @param person
	 *        The person asking, <code>null</code> when no person asks.
	 * @param object
	 *        The object accessed.
	 * @param operation
	 *        The id of the operation performed.
	 */
	record Key(ObjectKey person, ObjectKey object, String operation) {
		// Pure value type without additional behavior.
	}

	private static final Property<AccessDecisionCache> PROPERTY =
		TypedAnnotatable.property(AccessDecisionCache.class, "accessDecisions");

	/** Marker of a decision that is being computed. */
	private static final Object COMPUTING = new Object();

	private final long _revision;

	private final Map<Key, Object> _decisions = new HashMap<>();

	private AccessDecisionCache(long revision) {
		_revision = revision;
	}

	/**
	 * The memo of the current interaction for the given revision of the knowledge base.
	 * <p>
	 * Outside an interaction a fresh memo is returned, which then serves a single check and its
	 * delegations only.
	 * </p>
	 * 
	 * @param revision
	 *        The revision the decisions are made for.
	 */
	static AccessDecisionCache current(long revision) {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return new AccessDecisionCache(revision);
		}
		AccessDecisionCache cache = interaction.get(PROPERTY);
		if (cache == null || cache._revision != revision) {
			cache = new AccessDecisionCache(revision);
			interaction.set(PROPERTY, cache);
		}
		return cache;
	}

	/**
	 * The key of the decision for the given person, object and operation.
	 */
	static Key key(Person person, TLObject object, BoundCommandGroup operation) {
		return new Key(person == null ? null : person.tId(), object.tId(), operation.getID());
	}

	/**
	 * The stored decision for the given key.
	 * 
	 * @return <code>null</code> when no decision is stored and none is being computed.
	 */
	Boolean decision(Key key) {
		Object stored = _decisions.get(key);
		return stored instanceof Boolean decision ? decision : null;
	}

	/**
	 * Whether the decision for the given key is being computed, i.e. whether asking for it again
	 * closes a cycle.
	 */
	boolean isComputing(Key key) {
		return _decisions.get(key) == COMPUTING;
	}

	/**
	 * Marks the decision for the given key as being computed.
	 */
	void computing(Key key) {
		_decisions.put(key, COMPUTING);
	}

	/**
	 * Stores the decision for the given key.
	 */
	void decided(Key key, boolean decision) {
		_decisions.put(key, Boolean.valueOf(decision));
	}

}
