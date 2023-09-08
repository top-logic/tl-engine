/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;


import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;

/**
 * Constants for representing internal identifiers in database columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IdentifierTypes {

	/**
	 * The {@link MetaObject} type of an identifier column.
	 */
	public static final MOPrimitive REFERENCE_MO_TYPE = MOPrimitive.TLID;

	/**
	 * The {@link MetaObject} type of an type column.
	 */
	public static final MOPrimitive TYPE_REFERENCE_MO_TYPE = MOPrimitive.STRING;
	
	/**
	 * The {@link MetaObject} type of a revision column.
	 */
	public static final MOPrimitive REVISION_REFERENCE_MO_TYPE = MOPrimitive.LONG;
	
	/**
	 * The {@link MetaObject} type of a branch column.
	 */
	public static final MOPrimitive BRANCH_REFERENCE_MO_TYPE = MOPrimitive.LONG;

	/**
	 * Attribute referencing a branch.
	 * 
	 * <p>
	 * The returned attribute is mandatory and immutable.
	 * </p>
	 * 
	 * @param attributeName
	 *        Name of the returned attribute.
	 * @param columnName
	 *        Name of the database column.
	 */
	public static MOAttributeImpl newBranchReference(String attributeName, String columnName) {
		MOAttributeImpl attr = new MOAttributeImpl(attributeName, BRANCH_REFERENCE_MO_TYPE, true, true);
		attr.setDBName(columnName);
		return attr;
	}

	/**
	 * Attribute referencing a table.
	 * 
	 * <p>
	 * The returned attribute is mandatory and immutable.
	 * </p>
	 * 
	 * @param attributeName
	 *        Name of the returned attribute.
	 * @param columnName
	 *        Name of the database column.
	 */
	public static MOAttributeImpl newTypeReference(String attributeName, String columnName) {
		MOAttributeImpl attr =
			new MOAttributeImpl(attributeName, TYPE_REFERENCE_MO_TYPE, true, true);
		attr.setDBName(columnName);
		attr.setSQLSize(70);
		attr.setBinary(true);
		return attr;
	}
	
	/**
	 * Attribute containing a revision.
	 * 
	 * <p>
	 * The returned attribute is mandatory.
	 * </p>
	 * 
	 * @param attributeName
	 *        Name of the returned attribute.
	 * @param columnName
	 *        Name of the database column.
	 * @param immutable
	 *        Whether the revision is immutable.
	 */
	public static MOAttributeImpl newRevisionReference(String attributeName, String columnName, boolean immutable) {
		MOAttributeImpl attr = new MOAttributeImpl(attributeName, REVISION_REFERENCE_MO_TYPE, true, immutable);
		attr.setDBName(columnName);
		return attr;
	}
	
	/**
	 * Attribute referencing the ID of an object.
	 * 
	 * @param attributeName
	 *        Name of the returned attribute.
	 * @param columnName
	 *        Name of the database column.
	 */
	public static MOAttributeImpl newIdentifierAttribute(String attributeName, String columnName) {
		MOAttributeImpl attr = new MOAttributeImpl(attributeName, IdentifierTypes.REFERENCE_MO_TYPE);
		attr.setDBName(columnName);
		attr.setBinary(IdentifierTypes.REFERENCE_MO_TYPE.getDefaultSQLType().binaryParam);
		return attr;
	}

}
