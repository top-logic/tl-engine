/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.tl.ai.core.security.EffectiveToolAccess;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;

/**
 * One matrix of the policy editor: roles against the names of one dimension, read from and written to
 * a {@link PolicyDraft}.
 *
 * <p>
 * Like the draft, a projection rather than a model of its own: every question reads the draft's
 * entries and every change rewrites them, so what the matrix shows is what the saved file says.
 * </p>
 */
/*
 * Issue #404. The rules that make the matrix safe to click in live here, where they can be tested
 * without a user interface:
 *
 * - A run section locks a role it does not name. Its cells show what the section default gives the
 *   role and cannot be clicked: a first click would silently restrict the role to that one name.
 * - Emptying the last cell of a named role keeps the role named, which means "may use none". Only the
 *   row state returns a role to the default, so that nothing ever widens by accident.
 * - The capability matrix has no locked rows: an entry of the tool access section that grants and
 *   forbids nothing changes nothing, so a first click may create one.
 */
public final class PolicyMatrix {

	/**
	 * Whether a section names a role.
	 */
	public enum RowState {

		/** The section has no entry for the role; its default decides. */
		NOT_NAMED,

		/**
		 * The section has an entry for the role. In a run section, that restricts the role to what the
		 * entry grants.
		 */
		NAMED;
	}

	private final PolicyDraft _draft;

	private final MatrixDimension _dimension;

	/**
	 * Creates a {@link PolicyMatrix}.
	 *
	 * @param draft
	 *        The policy to read and write.
	 * @param dimension
	 *        Which matrix of it.
	 */
	public PolicyMatrix(PolicyDraft draft, MatrixDimension dimension) {
		_draft = draft;
		_dimension = dimension;
	}

	/** The draft this matrix reads and writes. */
	public PolicyDraft draft() {
		return _draft;
	}

	/** Which matrix this is. */
	public MatrixDimension dimension() {
		return _dimension;
	}

	/**
	 * The roles that are rows: every role the application has, and every role the policy names.
	 *
	 * <p>
	 * All roles rather than the configured ones, because restricting a role has to start somewhere: a
	 * matrix that listed only the named roles could never name another one. A role the policy names and
	 * the application does not have stays a row, so that it can be seen and removed.
	 * </p>
	 *
	 * @param existingRoles
	 *        The roles the application has.
	 * @return The role names; the application's own first, then the framework's, then the internal
	 *         ones, each group by name.
	 */
	public List<String> roles(Collection<String> existingRoles) {
		Set<String> result = new TreeSet<>(existingRoles);
		result.addAll(_draft.runRoleNames());
		result.removeIf(name -> name == null || name.isBlank());
		List<String> sorted = new ArrayList<>(result);
		sorted.sort(BY_ROLE_KIND_NAME);
		return sorted;
	}

	/**
	 * The names that are columns: every name the application has, and every name the policy mentions.
	 *
	 * @param knownNames
	 *        The names the application has. Ignored for {@link MatrixDimension#CAPABILITIES}, whose
	 *        columns are the capability groups the draft can decide about.
	 * @return The names, in the order they are shown.
	 */
	public List<String> columns(Collection<String> knownNames) {
		if (!_dimension.isRunSection()) {
			List<String> groups = new ArrayList<>(_draft.grantableGroups());
			List<String> order = EffectiveToolAccess.capabilityOrder();
			groups.sort(Comparator.comparingInt((String group) -> {
				int index = order.indexOf(group);
				return index < 0 ? order.size() : index;
			}).thenComparing(Comparator.naturalOrder()));
			return groups;
		}
		Set<String> result = new TreeSet<>(knownNames);
		result.addAll(_draft.mentionedNames(_dimension.runDimension()));
		return new ArrayList<>(result);
	}

	/** Whether the section has an entry for the given role. */
	public RowState rowState(String roleName) {
		boolean named = _dimension.isRunSection()
			? _draft.isRunRestricted(_dimension.runDimension(), roleName)
			: _draft.isToolAccessNamed(roleName);
		return named ? RowState.NAMED : RowState.NOT_NAMED;
	}

	/**
	 * Gives the role an entry of the section, or takes it away.
	 *
	 * <p>
	 * Taking it away drops everything the entry says. In a run section that returns the role to the
	 * section default; in the capability matrix it takes back what the role grants and forbids.
	 * </p>
	 */
	public void setRowState(String roleName, RowState state) {
		boolean named = state == RowState.NAMED;
		if (_dimension.isRunSection()) {
			_draft.setRunRestricted(_dimension.runDimension(), roleName, named);
		} else {
			_draft.setToolAccessNamed(roleName, named);
		}
	}

	/**
	 * Whether the cells of the given role cannot be clicked, because the section default decides for
	 * it.
	 */
	public boolean isLocked(String roleName) {
		return _dimension.isRunSection() && rowState(roleName) == RowState.NOT_NAMED;
	}

	/** What the role says about the given name. */
	public CellState cell(String roleName, String name) {
		if (_dimension.isRunSection()) {
			return _draft.runCellState(_dimension.runDimension(), roleName, name);
		}
		return _draft.capabilityState(roleName, name);
	}

	/**
	 * Puts one cell into the given state.
	 *
	 * @return Whether the cell could be written; <code>false</code> for a {@link #isLocked(String)
	 *         locked} row, which is left alone.
	 */
	public boolean setCell(String roleName, String name, CellState state) {
		if (_dimension.isRunSection()) {
			return _draft.setRunCellState(_dimension.runDimension(), roleName, name, state);
		}
		_draft.setCapabilityState(roleName, name, state);
		return true;
	}

	/**
	 * How many cells a bulk edit would change, without changing them.
	 *
	 * @see #apply(Collection, Collection, CellState, boolean)
	 */
	public int affectedCells(Collection<String> roleNames, Collection<String> names, CellState state,
			boolean restrictFirst) {
		int result = 0;
		for (String roleName : roleNames) {
			if (isLocked(roleName) && !restrictFirst) {
				continue;
			}
			for (String name : names) {
				// A locked row is restricted first; its cells then read as absent.
				CellState before = isLocked(roleName) ? CellState.ABSENT : cell(roleName, name);
				if (before != state) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * Puts the given cells of the given roles into one state.
	 *
	 * @param roleNames
	 *        The roles to change.
	 * @param names
	 *        The names whose cells to change.
	 * @param state
	 *        The state to reach.
	 * @param restrictFirst
	 *        Whether a locked row is named first. Without it, locked rows are skipped, so that a bulk
	 *        edit never restricts a role nobody asked to restrict.
	 * @return How many cells changed.
	 */
	public int apply(Collection<String> roleNames, Collection<String> names, CellState state,
			boolean restrictFirst) {
		int result = affectedCells(roleNames, names, state, restrictFirst);
		for (String roleName : roleNames) {
			if (isLocked(roleName)) {
				if (!restrictFirst) {
					continue;
				}
				setRowState(roleName, RowState.NAMED);
			}
			for (String name : names) {
				setCell(roleName, name, state);
			}
		}
		return result;
	}

	// ---------------------------------------------- the two rows no role owns

	/**
	 * Whether the basic permissions grant the given capability group, as the baseline row shows it.
	 *
	 * <p>
	 * Under {@link BaselineChoice#DEFAULT} that is the shipped default, which no cell of the policy
	 * says; the row shows it locked.
	 * </p>
	 */
	public boolean baselineCell(String group) {
		switch (_draft.baselineChoice()) {
			case DEFAULT:
				return RoleToolAccess.DEFAULT_BASELINE.contains(group);
			case NOTHING:
				return false;
			default:
				return _draft.baselineGroups().contains(group);
		}
	}

	/**
	 * Grants the given capability group to everybody, or stops doing so.
	 *
	 * <p>
	 * Only while the basic permissions are an own list; the other two states say nothing a cell could
	 * change.
	 * </p>
	 *
	 * @return Whether the cell could be written.
	 */
	public boolean setBaselineCell(String group, boolean granted) {
		if (_draft.baselineChoice() != BaselineChoice.CUSTOM) {
			return false;
		}
		_draft.setBaselineGroups(toggled(_draft.baselineGroups(), group, granted));
		return true;
	}

	/**
	 * Whether an unattended run may reach the given capability group, as the background row shows it.
	 */
	public boolean backgroundCell(String group) {
		switch (_draft.backgroundChoice()) {
			case ALL:
				return true;
			case UNDECLARED:
				return false;
			default:
				return _draft.backgroundGroups().contains(group);
		}
	}

	/**
	 * Lets an unattended run reach the given capability group, or stops it.
	 *
	 * <p>
	 * Only while the limit is an own list.
	 * </p>
	 *
	 * @return Whether the cell could be written.
	 */
	public boolean setBackgroundCell(String group, boolean admitted) {
		if (_draft.backgroundChoice() != BackgroundChoice.CUSTOM) {
			return false;
		}
		_draft.setBackgroundGroups(toggled(_draft.backgroundGroups(), group, admitted));
		return true;
	}

	private static List<String> toggled(Set<String> groups, String group, boolean contained) {
		Set<String> result = new LinkedHashSet<>(groups);
		if (contained) {
			result.add(group);
		} else {
			result.remove(group);
		}
		return new ArrayList<>(result);
	}

	/** The order roles are listed in: the application's own first, then the framework's. */
	static final Comparator<String> BY_ROLE_KIND_NAME = (left, right) -> {
		int byKind = SecurityRoleKind.of(left).ordinal() - SecurityRoleKind.of(right).ordinal();
		return byKind != 0 ? byKind : left.compareToIgnoreCase(right);
	};

}
