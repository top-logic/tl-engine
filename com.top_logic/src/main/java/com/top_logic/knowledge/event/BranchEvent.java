/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Set;

import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link KnowledgeEvent} that represents a branch creation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BranchEvent extends KnowledgeEvent {

	private long branchId;
	private long baseBranchId;
	private long baseRevisionNumber;
	
	private Set<String> branchedTypeNames;

	/**
	 * Creates a {@link BranchEvent}
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param branchId
	 *        See {@link #getBranchId()}
	 * @param baseBranchId
	 *        See {@link #getBaseBranchId()}
	 * @param baseRevisionNumber
	 *        See {@link #getBaseRevisionNumber()}
	 */
	public BranchEvent(long revision, long branchId, long baseBranchId, long baseRevisionNumber) {
		super(revision);
		
		this.branchId = branchId;
		this.baseBranchId = baseBranchId;
		this.baseRevisionNumber = baseRevisionNumber;
	}

	@Override
	public EventKind getKind() {
		return EventKind.branch;
	}
	
	/**
	 * Unique identifier of the branch of this event.
	 */
	public long getBranchId() {
		return branchId;
	}

	/**
	 * Modifies the {@link #getBranchId()} of this event.
	 */
	public void setBranchId(long branchId) {
		this.branchId = branchId;
	}
	
	/**
	 * The {@link Branch} ID, from which this branch was spawned.
	 * 
	 * <p>
	 * Not <code>null</code> for all {@link Branch}es except trunk.
	 * </p>
	 */
	public long getBaseBranchId() {
		return this.baseBranchId;
	}

	/**
	 * Modifies the {@link #getBaseBranchId()} of this event.
	 */
	public void setBaseBranchId(long baseBranchId) {
		this.baseBranchId = baseBranchId;
	}
	
	/**
	 * The {@link Revision#getCommitNumber()} in the {@link #getBaseBranchId()
	 * base branch} from which this {@link Branch} was spawned.
	 */
	public long getBaseRevisionNumber() {
		return baseRevisionNumber;
	}

	/**
	 * Modifies the {@link #getBaseRevisionNumber()} of this event.
	 */
	public void setBaseRevisionNumber(long baseRevisionNumber) {
		this.baseRevisionNumber = baseRevisionNumber;
	}

	/**
	 * Set names of types that have data in the branch created with this event.
	 */
	public Set<String> getBranchedTypeNames() {
		return this.branchedTypeNames;
	}

	/**
	 * @see #getBranchedTypeNames()
	 */
	public void setBranchedTypeNames(Set<String> branchedTypes) {
		this.branchedTypeNames = branchedTypes;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof BranchEvent)) {
			return false;
		}
		
		BranchEvent otherEvent = ((BranchEvent) other);
		return equalsBranchEvent(otherEvent);
	}

	protected final boolean equalsBranchEvent(BranchEvent otherEvent) {
		return equalsKnowledgeEvent(otherEvent) && 
			this.branchId == otherEvent.branchId &&
			this.baseBranchId == otherEvent.baseBranchId && 
			this.baseRevisionNumber == otherEvent.baseRevisionNumber &&
			this.branchedTypeNames.equals(otherEvent.branchedTypeNames);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + 69337 * (int) this.branchId;
	}

	@Override
	protected void appendProperties(StringBuilder result) {
		super.appendProperties(result);
		result.append(", branchId: ");
		result.append(this.branchId);
	}

	@Override
	public <R, A> R visit(KnowledgeEventVisitor<R, A> v, A arg) {
		return v.visitBranch(this, arg);
	}

}
