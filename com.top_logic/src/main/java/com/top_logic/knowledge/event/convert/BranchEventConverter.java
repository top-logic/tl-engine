/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.KnowledgeEvent;

/**
 * The class {@link BranchEventConverter} changes a {@link BranchEvent} such that unknown types are
 * removed, and additional types are added to the event.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BranchEventConverter extends AbstractMappingRewriter {

	private final Set<String> _allTypes;

	private final Collection<String> _additionalBranchedTypes;

	/**
	 * Creates a {@link BranchEventConverter}.
	 * 
	 * @param potentialBranchTypes
	 *        the types which are branched at most. If an event was mapped which tries to branch a
	 *        type not contained in this set, it will be removed from the set of branched types.
	 * @param additionalBranchedTypes
	 *        a set of types which are branched in each case.
	 */
	public BranchEventConverter(Set<String> potentialBranchTypes, Collection<String> additionalBranchedTypes) {
		_allTypes = potentialBranchTypes;
		if (additionalBranchedTypes != null) {
			if (!additionalBranchedTypes.isEmpty()) {
				if (!_allTypes.containsAll(additionalBranchedTypes)) {
					HashSet<String> illegalTypes = new HashSet<>(additionalBranchedTypes);
					illegalTypes.removeAll(_allTypes);
					assert !illegalTypes.isEmpty() : "There is a type in the types to branch not a potential branch type";
					throw new IllegalArgumentException("Attempt to branch an unknown type: " + illegalTypes);
				}
				_additionalBranchedTypes = additionalBranchedTypes;
			} else {
				// The check for null occurs in each case. If there are none
				_additionalBranchedTypes = null;
			}
		} else {
			_additionalBranchedTypes = null;
		}
	}

	@Override
	protected ChangeSet mapChangeSet(ChangeSet input) {
		for (BranchEvent event : input.getBranchEvents()) {
			modify(event);
		}
		return input;
	}

	private void modify(BranchEvent event) {
		final Set<String> branchedTypeNames = event.getBranchedTypeNames();
		Iterator<String> branchedTypeIter = branchedTypeNames.iterator();
		while (branchedTypeIter.hasNext()) {
			if (!_allTypes.contains(branchedTypeIter.next())) {
				// Type was removed.
				branchedTypeIter.remove();
			}
		}
		if (_additionalBranchedTypes != null) {
			// add all additional types
			branchedTypeNames.addAll(_additionalBranchedTypes);
		}
	}

	/**
	 * creates an {@link EventRewriter} which not just converts {@link BranchEvent} as
	 * {@link BranchEventConverter} do, but also handles other {@link KnowledgeEvent} by writing
	 * them untouched to the given {@link EventWriter}.
	 * 
	 * @see BranchEventConverter
	 */
	public static final EventRewriter createBranchEventConverter(
			Set<String> potentialBranchTypes, Collection<String> additionalBranchedTypes) {
		return new BranchEventConverter(potentialBranchTypes, additionalBranchedTypes);
	}

}

