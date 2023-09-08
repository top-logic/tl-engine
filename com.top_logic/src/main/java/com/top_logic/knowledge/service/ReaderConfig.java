/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Comparator;
import java.util.Set;

import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.event.ItemEvent;

/**
 * The {@link ReaderConfig} is a configuration of an {@link EventReader} used in
 * {@link KnowledgeBase}
 * 
 * @see KnowledgeBase#getChangeSetReader(ReaderConfig)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReaderConfig {

	/**
	 * Returns the first revision to read.
	 */
	Revision getStartRev();

	/**
	 * Returns the last revision to read (inclusive).
	 */
	Revision getStopRev();

	/**
	 * Returns the IDs of the {@link Branch branches} to read from. <code>null</code> means reading.
	 * from all branches.
	 */
	Set<Long> getBranches();

	/**
	 * Returns the names of the types to read. <code>null</code> means all types. are considered in
	 * search.
	 */
	Set<String> getTypeNames();

	/**
	 * Determines whether {@link CommitEvent commit events} must be reported.
	 */
	boolean needCommitEvents();

	/**
	 * Determines whether {@link BranchEvent branch events} must be reported.
	 */
	boolean needBranchEvents();

	/**
	 * Determines the order in which the event should be delivered
	 */
	Comparator<? super ItemEvent> getOrder();

}
