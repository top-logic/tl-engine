/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.knowledge.event.ItemEvent;

/**
 * The {@link ReaderConfigBuilder} is a factory class for creating
 * {@link ReaderConfig}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ReaderConfigBuilder implements ReaderConfig {

	/**
	 * Mapping that maps a branch to its id.
	 */
	private static final Mapping<Branch, Long> BRANCH_ID_MAPPING = new Mapping<>() {

		@Override
		public final Long map(Branch branch) {
			assert branch != null : "Can not map null branch";
			return Long.valueOf(branch.getBranchId());
		}
	};

	private final Revision startRev;
	private final Revision stopRev;

	private Set<Long> branches = null;
	private Set<String> typeNames = null;
	private boolean commitEvents = true;
	private boolean branchEvents = true;
	private Comparator<? super ItemEvent> order = null;

	private ReaderConfigBuilder(Revision startRev, Revision stopRev) {
		this.startRev = startRev;
		this.stopRev = stopRev;
	}

	/**
	 * Sets {@link #getTypeNames()}.
	 * 
	 * @return This {@link ReaderConfigBuilder} for chaining.
	 */
	public ReaderConfigBuilder setTypeNames(Set<String> typeNames) {
		this.typeNames = typeNames;
		return this;
	}

	/**
	 * Sets {@link #getBranches()}.
	 * 
	 * @return This {@link ReaderConfigBuilder} for chaining.
	 */
	public ReaderConfigBuilder setBranches(Set<Long> branches) {
		this.branches = branches;
		return this;
	}

	/**
	 * Sets {@link #needBranchEvents()}.
	 * 
	 * @return This {@link ReaderConfigBuilder} for chaining.
	 */
	public ReaderConfigBuilder needCommits(boolean commits) {
		this.commitEvents = commits;
		return this;
	}

	/**
	 * Sets {@link #getOrder()}.
	 * 
	 * @return This {@link ReaderConfigBuilder} for chaining.
	 */
	public ReaderConfigBuilder setOrder(Comparator<? super ItemEvent> order) {
		this.order = order;
		return this;
	}

	/**
	 * Sets {@link #needBranchEvents()}.
	 * 
	 * @return This {@link ReaderConfigBuilder} for chaining.
	 */
	public ReaderConfigBuilder needBranchEvents(boolean needBranchEvents) {
		this.branchEvents = needBranchEvents;
		return this;
	}

	@Override
	public Revision getStartRev() {
		return this.startRev;
	}

	@Override
	public Revision getStopRev() {
		return this.stopRev;
	}

	@Override
	public Set<Long> getBranches() {
		return this.branches;
	}

	@Override
	public Set<String> getTypeNames() {
		return this.typeNames;
	}

	@Override
	public boolean needCommitEvents() {
		return this.commitEvents;
	}

	@Override
	public boolean needBranchEvents() {
		return this.branchEvents;
	}

	@Override
	public Comparator<? super ItemEvent> getOrder() {
		return this.order;
	}

	/**
	 * Builds a new {@link ReaderConfigBuilder} with the given values.
	 */
	public static ReaderConfigBuilder createConfig(Revision startRev, Revision stopRev) {
		return new ReaderConfigBuilder(startRev, stopRev);
	}

	/**
	 * Builds a new {@link ReaderConfigBuilder} with the given values.
	 */
	public static ReaderConfigBuilder createConfig(Revision startRev, Revision stopRev, String typeName, Branch branch, boolean needCommitEvents,
			boolean needBranchEvents) {
		final Set<String> typeNames = typeName == null ? null : Collections.singleton(typeName);
		final Set<Long> branchIds = branch == null ? null : Collections.singleton(Long.valueOf(branch.getBranchId()));
		return new ReaderConfigBuilder(startRev, stopRev).setTypeNames(typeNames).setBranches(branchIds)
			.needCommits(needCommitEvents).needBranchEvents(needBranchEvents);
	}

	/**
	 * Builds a new {@link ReaderConfigBuilder} with the given values.
	 */
	public static ReaderConfigBuilder createComplexConfig(Revision startRev, Revision stopRev, Set<String> typeNames,
			Set<Branch> branches, boolean needCommitEvents, boolean needBranchEvents,
			Comparator<? super ItemEvent> order) {
		
		Set<Long> branchIds;
		if (branches == null) {
			branchIds = null;
		} else {
			branchIds = Mappings.mapIntoSet(BRANCH_ID_MAPPING, branches);
		}
		
		return new ReaderConfigBuilder(startRev, stopRev).setTypeNames(typeNames).setBranches(branchIds)
			.needCommits(needCommitEvents).needBranchEvents(needBranchEvents).setOrder(order);
	}

}
