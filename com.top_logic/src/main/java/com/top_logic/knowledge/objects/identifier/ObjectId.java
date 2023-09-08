/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.identifier;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * The class {@link ObjectId} is a branch and revision independent identifier
 * for a {@link KnowledgeObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ObjectId {

	private final Object name;
	private final MetaObject type;

	public ObjectId(MetaObject type, Object name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * The object name of the identified object.
	 * 
	 * @see KnowledgeObject#getObjectName()
	 */
	public Object getObjectName() {
		return name;
	}

	/**
	 * The type of the identified object.
	 * 
	 * @see KnowledgeObject#tTable()
	 */
	public MetaObject getObjectType() {
		return type;
	}

	@Override
	public int hashCode() {
		return hashCodeObjectId(this);
	}

	static int hashCodeObjectId(ObjectId self) {
		int result = self.getObjectType().hashCode();
		result += 16661 * self.getObjectName().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ObjectId)) {
			return false;
		}
		return equalsObjectId(this, (ObjectId) other);
	}

	static boolean equalsObjectId(ObjectId self, ObjectId otherId) {
		boolean result = self.getObjectName().equals(otherId.getObjectName()) && self.getObjectType().equals(otherId.getObjectType());
		return result;
	}

}
