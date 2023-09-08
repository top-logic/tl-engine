/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;

/**
 * Constants for well known {@link MetaObject} and {@link MOAttribute}'s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BasicTypes {

	/**
	 * Name of the persistent type representing a branch.
	 */
	public static final String BRANCH_TYPE_NAME = "Branch";

	/**
	 * Name of the persistent type representing a revision.
	 */
	public static final String REVISION_TYPE_NAME = "Revision";

	/**
	 * Name of the persistent type representing a knowledge item.
	 */
	public static final String ITEM_TYPE_NAME = "Item";

	/**
	 * Name of the persistent type representing a knowledge object.
	 */
	public static final String OBJECT_TYPE_NAME = "Object";

	/**
	 * Name of the persistent type representing a knowledge object.
	 */
	public static final String KNOWLEDGE_OBJECT_TYPE_NAME = "KnowledgeObject";

	/**
	 * Name of the persistent type representing a knowledge association.
	 */
	public static final String ASSOCIATION_TYPE_NAME = "Association";

	/**
	 * Branch identifier attribute name in all items.
	 */
	public static final String BRANCH_ATTRIBUTE_NAME = "_branch";

	/**
	 * Name of the database column of attribute {@link BasicTypes#BRANCH_ATTRIBUTE_NAME}.
	 */
	public static final String BRANCH_DB_NAME = "BRANCH";

	/**
	 * Object identifier attribute name in all items.
	 */
	public static final String IDENTIFIER_ATTRIBUTE_NAME = "_identifier";

	/**
	 * Name of the database column of attribute {@link BasicTypes#IDENTIFIER_ATTRIBUTE_NAME}.
	 */
	public static final String IDENTIFIER_DB_NAME = "IDENTIFIER";

	/**
	 * First valid revision attribute name in all items.
	 */
	public static final String REV_MIN_ATTRIBUTE_NAME = "_rev_min";

	/**
	 * Name of the database column of attribute {@link BasicTypes#REV_MIN_ATTRIBUTE_NAME}.
	 */
	public static final String REV_MIN_DB_NAME = "REV_MIN";

	/**
	 * Last valid revision attribute name in all items.
	 */
	public static final String REV_MAX_ATTRIBUTE_NAME = "_rev_max";

	/**
	 * Name of the database column of attribute {@link BasicTypes#REV_MAX_ATTRIBUTE_NAME}.
	 */
	public static final String REV_MAX_DB_NAME = "REV_MAX";

	/**
	 * Create revision attribute name in all items.
	 */
	public static final String REV_CREATE_ATTRIBUTE_NAME = "createRev";

	/**
	 * Name of the database column of attribute {@link BasicTypes#REV_CREATE_ATTRIBUTE_NAME}.
	 */
	public static final String REV_CREATE_DB_NAME = "REV_CREATE";

	/**
	 * Name of the date attribute of {@link #REVISION_TYPE_NAME}.
	 */
	public static final String REVISION_DATE_ATTRIBUTE = "date";

	/**
	 * Name of the revision attribute of {@link #REVISION_TYPE_NAME}.
	 */
	public static final String REVISION_REV_ATTRIBUTE = IDENTIFIER_ATTRIBUTE_NAME;

	/**
	 * The persistent type of {@link #BRANCH_TYPE_NAME} objects.
	 */
	public static MOClass getBranchType(TypeContext types) {
		return lookupType(types, BRANCH_TYPE_NAME);
	}

	/**
	 * The persistent type of {@link #REVISION_TYPE_NAME} objects.
	 */
	public static MOClass getRevisionType(TypeContext types) {
		return lookupType(types, REVISION_TYPE_NAME);
	}

	/**
	 * The persistent type of {@link #ITEM_TYPE_NAME} objects.
	 */
	public static MOClass getItemType(TypeContext types) {
		return lookupType(types, ITEM_TYPE_NAME);
	}

	/**
	 * The persistent type of all application objects.
	 */
	public static MOClass getObjectType(TypeContext types) {
		return lookupType(types, OBJECT_TYPE_NAME);
	}

	/**
	 * The persistent type of {@link #KNOWLEDGE_OBJECT_TYPE_NAME} objects.
	 */
	public static MOClass getKnowledgeObjectType(TypeContext types) {
		return lookupType(types, KNOWLEDGE_OBJECT_TYPE_NAME);
	}


	/**
	 * The persistent type of {@link #ASSOCIATION_TYPE_NAME} objects.
	 */
	public static MOClass getKnowledgeAssociationType(TypeContext types) {
		return lookupType(types, ASSOCIATION_TYPE_NAME);
	}

	/**
	 * Searches for the {@link MOClass} with the given name in the given types.
	 */
	@FrameworkInternal
	protected static MOClass lookupType(TypeContext types, String typeName) {
		try {
			return (MOClass) types.getType(typeName);
		} catch (UnknownTypeException ex) {
			throw new IllegalStateException("Knowledge base without '" + typeName + "' type.");
		}
	}

}
