/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.ComputedMOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * {@link TypeProvider} for {@link DBKnowledgeBase} internal types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BasicTypeProvider implements TypeProvider {

	/**
	 * Singleton {@link BasicTypeProvider} instance.
	 */
	public static final BasicTypeProvider INSTANCE = new BasicTypeProvider();

	private BasicTypeProvider() {
		// Singleton constructor.
	}

	@Override
	public void createTypes(Log log, MOFactory typeFactory, MORepository moRepository) {
		try {
			moRepository.addMetaObject(RevisionImpl.newRevisionXRefType());

			HistoryCleanupTable.INSTANCE.createTypes(log, typeFactory, moRepository);

			moRepository.addMetaObject(BranchSupport.newBranchType());
			moRepository.addMetaObject(BranchSupport.newBranchSwitchType());

			moRepository.addMetaObject(
				AbstractFlexDataManager.createFlexDataType(AbstractFlexDataManager.FLEX_DATA,
					AbstractFlexDataManager.FLEX_DATA_DB_NAME, moRepository.multipleBranches()));
		} catch (DataObjectException ex) {
			log.error("Creating basic types failed.", ex);
		}
	}

	static DBAttribute[] primaryKeyColumns(MOAttribute branch, MOAttributeImpl... additional) {
		DBAttribute[] branchDBColumns = branch.getDbMapping();
		DBAttribute[] primaryKeyColumns = new DBAttribute[branchDBColumns.length + additional.length];
		System.arraycopy(branchDBColumns, 0, primaryKeyColumns, 0, branchDBColumns.length);
		for (int index = 0, size = additional.length; index < size; index++) {
			primaryKeyColumns[branchDBColumns.length + index] = additional[index];
		}
		return primaryKeyColumns;
	}

	static void addMaxMinIndexes(MOKnowledgeItemImpl type, DBAttribute revMaxAttribute, DBAttribute revMinAttribute)
			throws UnreachableAssertion {
		try {
			MOIndexImpl revMaxIndex =
				new MOIndexImpl("rev_max", null, new DBAttribute[] { revMaxAttribute }, !DBIndex.UNIQUE,
					DBIndex.CUSTOM, !DBIndex.IN_MEMORY, DBIndex.NO_COMPRESS);
			type.addIndex(revMaxIndex);
			MOIndexImpl revMinIndex =
				new MOIndexImpl("rev_min", null, new DBAttribute[] { revMinAttribute }, !DBIndex.UNIQUE,
					DBIndex.CUSTOM, !DBIndex.IN_MEMORY, DBIndex.NO_COMPRESS);
			type.addIndex(revMinIndex);
		} catch (IncompatibleTypeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * The attribute storing {@link KnowledgeItem#getBranchContext()}.
	 */
	static final MOAttribute newBranchAttribute(boolean withDBColumn) {
		MOAttribute attribute;
		IBranchStorage branchStorage;
		if (withDBColumn) {
			attribute = IdentifierTypes.newBranchReference(BasicTypes.BRANCH_ATTRIBUTE_NAME, BasicTypes.BRANCH_DB_NAME);
			branchStorage = BranchStorage.INSTANCE;
		} else {
			attribute =
				new ComputedMOAttribute(BasicTypes.BRANCH_ATTRIBUTE_NAME, IdentifierTypes.BRANCH_REFERENCE_MO_TYPE);
			branchStorage = TrunkStorage.INSTANCE;
		}
		attribute.setMandatory(true);
		attribute.setImmutable(false);
		attribute.setStorage(branchStorage);
		attribute.setSystem(true);
		return attribute;
	}

}