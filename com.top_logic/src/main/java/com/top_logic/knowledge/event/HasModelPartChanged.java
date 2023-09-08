/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModelPart;

/**
 * {@link KnowledgeEventVisitor} checking whether a {@link TLModelPart} has changed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasModelPartChanged extends AbstractKnowledgeEventVisitor<Boolean, Void> {

	/**
	 * {@link HasModelPartChanged} instance with default {@link TLModelPart model part type names}.
	 */
	public static final HasModelPartChanged INSTANCE =
		new HasModelPartChanged(ApplicationObjectUtil.DEFAULT_MODEL_PART_TYPE_NAMES);

	private final Set<String> _modelPartTypes;

	/**
	 * Creates a new {@link HasModelPartChanged}.
	 * 
	 * @param modelPartTypeNames
	 *        The name of {@link MetaObject} that contain model part elements.
	 */
	public HasModelPartChanged(Set<String> modelPartTypeNames) {
		_modelPartTypes = modelPartTypeNames;
	}

	/**
	 * Checks whether the {@link ChangeSet} contains a change for a {@link TLModelPart}.
	 * 
	 * @param cs
	 *        The {@link ChangeSet} to analyse.
	 * @return Whether the {@link ChangeSet} contains any {@link KnowledgeEvent}.
	 */
	public boolean hasModelPartChanged(ChangeSet cs) {
		for (ObjectCreation event : cs.getCreations()) {
			if (hasModelPartChanged(event)) {
				return true;
			}
		}
		for (ItemUpdate event : cs.getUpdates()) {
			if (hasModelPartChanged(event)) {
				return true;
			}
		}
		for (ItemDeletion event : cs.getDeletions()) {
			if (hasModelPartChanged(event)) {
				return true;
			}
		}
		for (BranchEvent event : cs.getBranchEvents()) {
			if (hasModelPartChanged(event)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the given {@link KnowledgeEvent} contains a change for a {@link TLModelPart}.
	 */
	public boolean hasModelPartChanged(KnowledgeEvent event) {
		return event.visit(this, null);
	}

	@Override
	protected Boolean visitItemEvent(ItemEvent event, Void arg) {
		String changedType = event.getObjectType().getName();
		return _modelPartTypes.contains(changedType);
	}

	@Override
	public Boolean visitBranch(BranchEvent event, Void arg) {
		Set<String> branchedTypes = event.getBranchedTypeNames();
		return CollectionUtil.containsAny(branchedTypes, _modelPartTypes);
	}

	@Override
	protected Boolean visitKnowledgeEvent(KnowledgeEvent event, Void arg) {
		return Boolean.FALSE;
	}

}

