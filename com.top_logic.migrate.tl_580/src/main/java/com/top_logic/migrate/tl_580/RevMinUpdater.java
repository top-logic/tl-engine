/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import com.google.inject.Inject;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Revision;

/**
 * Updates {@link BasicTypes#REV_MIN_ATTRIBUTE_NAME} of unversioned associations. Pre 5.8 the value
 * is always {@link Revision#CURRENT_REV}. This rewriter updates to the create revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevMinUpdater extends RewritingEventVisitor {
	
	private MetaObject _associationType;

	/**
	 * Initialises the {@link MORepository} for this {@link RevMinUpdater}.
	 */
	@Inject
	public void initMORepository(MORepository types) throws UnknownTypeException {
		_associationType = types.getMetaObject(BasicTypes.ASSOCIATION_TYPE_NAME);
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		MetaObject type = event.getObjectType();
		if (type instanceof MOClass && type.isSubtypeOf(_associationType)
			&& !((MOClass) type).isVersioned()) {
			Object createRevision = event.getValues().get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME);
			event.getValues().put(BasicTypes.REV_MIN_ATTRIBUTE_NAME, createRevision);
		}
		return super.visitCreateObject(event, arg);
	}
}

