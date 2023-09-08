/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.convert.AbstractObjectConverter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Modifies the written events such that it seems the events occurs on a given branch.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TargetBranchConverter extends AbstractObjectConverter {

	private final long _branchID;

	/**
	 * Creates a new {@link TargetBranchConverter}.
	 * 
	 * @param branchID
	 *        the ID of the branch to convert events to.
	 */
	public TargetBranchConverter(long branchID) {
		_branchID = branchID;
	}

	@Override
	protected ObjectBranchId translate(ItemEvent event, ObjectBranchId id) {
		ObjectBranchId adaptedId;
		if (id.getBranchId() != _branchID) {
			adaptedId = new ObjectBranchId(_branchID, id.getObjectType(), id.getObjectName());
		} else {
			adaptedId = id;
		}
		return adaptedId;
	}

	@Override
	protected ObjectKey translate(ItemEvent event, String attribute, ObjectKey value) {
		try {
			MOReference reference = MetaObjectUtils.getReference(event.getObjectType(), attribute);
			ObjectKey adaptedKey;
			if (reference.isBranchGlobal() || value.getBranchContext() == _branchID) {
				adaptedKey = value;
			} else {
				long historyContext = value.getHistoryContext();
				MetaObject targetType = value.getObjectType();
				TLID targetName = value.getObjectName();
				adaptedKey = new DefaultObjectKey(_branchID, historyContext, targetType, targetName);
			}
			return adaptedKey;
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(attribute + " is an attribute of " + event, ex);
		}
	}

	/**
	 * Creates an {@link EventWriter} which transforms the events to the given branch and writes the
	 * transformed events to the given {@link EventWriter}.
	 */
	public static EventWriter createEventConverter(long targetBranch, EventWriter out) {
		return StackedEventWriter.createWriter(new TargetBranchConverter(targetBranch), out);
	}

}
