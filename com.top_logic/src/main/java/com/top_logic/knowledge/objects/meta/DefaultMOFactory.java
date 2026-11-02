/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.meta;

import com.top_logic.dob.AbstractMOFactory;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.ComputedMOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.sql.DBMetaObject;
import com.top_logic.knowledge.KIReference;
import com.top_logic.knowledge.service.db2.BranchStorage;
import com.top_logic.knowledge.service.db2.TrunkStorage;

/**
 * Default implementation of {@link MOFactory}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultMOFactory extends AbstractMOFactory {

    public static final MOFactory INSTANCE = new DefaultMOFactory();
    
    protected DefaultMOFactory() {
		// Singleton constructor.
	}

	@Override
	public MOReference createMOReference(String name, MetaObject targetType, Boolean byValue) {
		if (byValue != null && byValue.booleanValue()) {
			return KIReference.referenceByValue(name, targetType);
		} else {
			return KIReference.referenceById(name, targetType);
		}
	}


	/**
	 * Adapts the {@link BasicTypes#BRANCH_ATTRIBUTE_NAME branch attribute} to the branch support
	 * of the type system.
	 * 
	 * <p>
	 * With multiple branches, the branch attribute is stored in the column
	 * {@link BasicTypes#BRANCH_DB_NAME} ({@link BranchStorage}). With a single branch, it is
	 * computed as the trunk without a column ({@link TrunkStorage}). A declaration of the other kind
	 * (e.g. a type definition stored by a database with the other branch support) is replaced.
	 * </p>
	 */
	@Override
	public MOAttribute adaptAttribute(MOAttribute declared, boolean multipleBranches) {
		if (!BasicTypes.BRANCH_ATTRIBUTE_NAME.equals(declared.getName())) {
			return declared;
		}
		boolean stored = declared.getDbMapping().length > 0;
		if (stored == multipleBranches) {
			return declared;
		}
		return multipleBranches ? storedBranchAttribute(declared) : computedBranchAttribute(declared);
	}

	private static MOAttribute storedBranchAttribute(MOAttribute declared) {
		DBMetaObject type =
			declared.getMetaObject() instanceof DBMetaObject declaredType ? declaredType : MOPrimitive.LONG;
		MOAttributeImpl result = new MOAttributeImpl(declared.getName(), BasicTypes.BRANCH_DB_NAME, type,
			declared.isMandatory(), declared.isImmutable(), type.getDefaultSQLType(), type.getDefaultSQLSize(),
			type.getDefaultSQLPrecision());
		result.setSystem(declared.isSystem());
		result.setStorage(BranchStorage.INSTANCE);
		return result;
	}

	private static MOAttribute computedBranchAttribute(MOAttribute declared) {
		ComputedMOAttribute result = new ComputedMOAttribute(declared.getName(), declared.getMetaObject());
		result.setMandatory(declared.isMandatory());
		result.setImmutable(declared.isImmutable());
		result.setSystem(declared.isSystem());
		result.setStorage(TrunkStorage.INSTANCE);
		return result;
	}

}
