/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The groups a historic version of an object is anchored for: the groups that may read the anchor
 * reference of a current object covering the version.
 *
 * <p>
 * A rule with mode {@link HistoryMode#ANCHORED} grants its role only to groups for which the path
 * of the rule and the gate agree, see {@link #restrict(Collection)}.
 * </p>
 */
public final class GateGroups {

	/** The gate that is closed for each group. */
	public static final GateGroups NONE = new GateGroups(false, Collections.emptySet());

	/** The gate that is open for each group. */
	public static final GateGroups EVERYBODY = new GateGroups(true, Collections.emptySet());

	private final boolean _everybody;

	private final Set<Group> _groups;

	private GateGroups(boolean everybody, Set<Group> groups) {
		_everybody = everybody;
		_groups = groups;
	}

	/**
	 * The gate that is open for the given groups.
	 */
	public static GateGroups of(Set<Group> groups) {
		return groups.isEmpty() ? NONE : new GateGroups(false, groups);
	}

	/**
	 * Whether the gate is open for no group.
	 */
	public boolean isEmpty() {
		return !_everybody && _groups.isEmpty();
	}

	/**
	 * The gate that is open for the groups of this gate and the groups of the given one.
	 */
	public GateGroups union(GateGroups other) {
		if (_everybody || other.isEmpty()) {
			return this;
		}
		if (other._everybody || isEmpty()) {
			return other;
		}
		Set<Group> groups = new HashSet<>(_groups);
		groups.addAll(other._groups);
		return new GateGroups(false, groups);
	}

	/**
	 * The groups that receive a role, when the path of the rule leads to the given groups and this
	 * gate is the gate of the rule.
	 *
	 * <p>
	 * The role is given to each path group that is contained in a gate group, and to each gate group
	 * that is contained in a path group. Of two groups, one of which contains the other, the smaller
	 * one receives the role. A group is contained in another, if it is the same group or a direct or
	 * nested member of it. The representative group of a person is contained in each group the
	 * person is a member of. A user that fulfills path and gate only through two groups neither of
	 * which contains the other does not receive the role.
	 * </p>
	 */
	public Set<Group> restrict(Collection<Group> pathGroups) {
		if (pathGroups.isEmpty() || isEmpty()) {
			return Collections.emptySet();
		}
		if (_everybody) {
			return new HashSet<>(pathGroups);
		}
		Set<Group> result = new HashSet<>();
		for (Group pathGroup : pathGroups) {
			if (_groups.contains(pathGroup)) {
				result.add(pathGroup);
			}
		}
		Set<Group> path = pathGroups instanceof Set ? (Set<Group>) pathGroups : new HashSet<>(pathGroups);
		for (Group pathGroup : pathGroups) {
			if (!result.contains(pathGroup) && containsAny(_groups, containers(pathGroup))) {
				result.add(pathGroup);
			}
		}
		for (Group gateGroup : _groups) {
			if (!result.contains(gateGroup) && containsAny(path, containers(gateGroup))) {
				result.add(gateGroup);
			}
		}
		return result;
	}

	private static boolean containsAny(Set<Group> groups, Collection<Group> candidates) {
		for (Group candidate : candidates) {
			if (groups.contains(candidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The groups the given group is a direct or nested member of.
	 */
	static Set<Group> containers(Group group) {
		Set<Group> result = new HashSet<>();
		List<Group> pending = new ArrayList<>();
		if (group.getBoundObject() instanceof Person person) {
			// The representative group stands for the person.
			pending.addAll(Group.getGroups(person, false, true));
		}
		pending.addAll(Group.getGroups(group, false, true));
		while (!pending.isEmpty()) {
			Group container = pending.remove(pending.size() - 1);
			if (container == group || !result.add(container)) {
				continue;
			}
			pending.addAll(Group.getGroups(container, false, true));
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		return obj instanceof GateGroups other && _everybody == other._everybody && _groups.equals(other._groups);
	}

	@Override
	public int hashCode() {
		return _groups.hashCode() + (_everybody ? 31 : 0);
	}

	@Override
	public String toString() {
		return _everybody ? "everybody" : _groups.toString();
	}

}
