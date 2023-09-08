/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants.*;
import static test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl.*;

import java.util.Arrays;
import java.util.List;

import test.com.top_logic.knowledge.service.TestKABasedCacheOrdered.BObjExtended;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemUtil;
import com.top_logic.knowledge.service.db2.StaticKnowledgeObjectFactory;

/**
 * Static initializers of types for {@link KnowledgeBase} testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class KnowledgeBaseMigrationTestScenarioImpl implements KnowledgeBaseMigrationTestScenario {

	static final boolean MONOMORPHIC = true;

	static final boolean BRANCH_GLOBAL = true;

	private static final MOAttributeImpl A1 = new MOAttributeImpl(T_A1_NAME, MOPrimitive.STRING, true);
	static {
		// Search in CLOB types not supported by Oracle.
//		A1.setSQLType(Types.CLOB);
		A1.setSQLType(DBType.STRING);
	}
	
	private static final MOAttributeImpl A2;
    static {
		A2 = new MOAttributeImpl(T_A2_NAME, MOPrimitive.STRING);
		// Search in CLOB types not supported by Oracle.
//    	A2.setSQLType(Types.CLOB);
		A2.setSQLType(DBType.STRING);
    	A2.setBinary(true);
    }

	private static final MOAttributeImpl AB1 = new MOAttributeImpl(T_AB1_NAME, MOPrimitive.STRING);
    
    /**
     * Renamed {@link KnowledgeBaseTestScenarioImpl#AB2} attribute.
     */
	private static final MOAttributeImpl AB2 = new MOAttributeImpl(T_AB2_NAME, MOPrimitive.STRING);
    
	private static final MOAttributeImpl BC1 = new MOAttributeImpl(T_BC1_NAME, MOPrimitive.STRING);

	private static final MOAttributeImpl BC2 = new MOAttributeImpl(T_BC2_NAME, MOPrimitive.STRING);

	private static final MOAttributeImpl UA1 = new MOAttributeImpl(T_UA1_NAME, MOPrimitive.STRING);

	private static final MOAttributeImpl UA2 = new MOAttributeImpl(T_UA2_NAME, MOPrimitive.STRING);

	private static final MOAttributeImpl B1 = new MOAttributeImpl(T_B1_NAME, MOPrimitive.STRING);
    
    /**
     * Renamed row during attribute.
     * 
     * @see KnowledgeBaseTestScenarioImpl#B2
     */
	private static final MOAttributeImpl B2 = new MOAttributeImpl(T_B2_NAME, MOPrimitive.STRING);

	private static final MOAttributeImpl B3 = new MOAttributeImpl(KnowledgeBaseTestScenarioImpl.B3);

	private static final MOAttributeImpl C1 = new MOAttributeImpl(T_C1_NAME, MOPrimitive.STRING);
    
    /**
     * Attribute with changed type during migration. 
     * 
     * @see KnowledgeBaseTestScenarioImpl#C2
     */
	private static final MOAttributeImpl C2 = new MOAttributeImpl(T_C2_NAME, MOPrimitive.INTEGER);
    
    /**
     * New row attribute after migration.
     */
	private static final MOAttributeImpl C3 = new MOAttributeImpl(T_C3_NAME, MOPrimitive.INTEGER);

	private static final MOAttributeImpl D1 = new MOAttributeImpl(T_D1_NAME, MOPrimitive.STRING);

	private static final MOAttributeImpl F1 = new MOAttributeImpl(T_F1_NAME, MOPrimitive.STRING);
    
	private static final MOClass KNOWLEDGE_OBJECT = new DeferredMetaObject(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME);

	private static final MOClass KNOWLEDGE_ASSOCIATION = new DeferredMetaObject(BasicTypes.ASSOCIATION_TYPE_NAME);

	/**
	 * Abstract super class of {@link #B} and {@link #C}
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#A
	 */
	private static final MOClass A = new MOKnowledgeItemImpl(T_A_NAME);
	static {
		A.setAbstract(true);
		A.setSuperclass(KNOWLEDGE_OBJECT);
		try {
			A.addAttribute(A1);
			A.addAttribute(A2);
			
			// Flex attribute made a row attribute after migration.
			A.addAttribute(F1);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Concrete super class of {@link #C} extending abstract base class {@link #A}.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#B
	 */
	private static final MOClass B = new MOKnowledgeItemImpl(T_B_NAME);
	static {
		B.setAbstract(false);
		B.setSuperclass(A);
		try {
			B.addAttribute(B1);
			B.addAttribute(B2);
			B.addAttribute(B3);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setApplicationType(B, BObjExtended.class);
	}
	
	/**
	 * Unversioned variant of {@link #B}.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#U
	 */
	private static final MOClass U = new MOKnowledgeItemImpl(T_U_NAME);
	static {
		U.setAbstract(false);
		U.setSuperclass(B);
		U.setVersioned(false);
	}
	
	/**
	 * Concrete subclass of {@link #B} with "deep" inheritance hierarchy.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#C
	 */
	private static final MOClass C;
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(T_C_NAME);
		type.setAbstract(false);
		type.setSuperclass(B);
		type.setFinal(true);
		try {
			type.addAttribute(C1);
			type.addAttribute(C2);
			type.addAttribute(C3);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setApplicationType(type, SimpleWrapperFactoryTestScenario.CObj.class);
		C = type;
	}
	
	private final static MOClass REFERENCE_TYPE = new DeferredMetaObject(T_D_NAME);

	private static MOReference REFERENCE_MONO_HIST_GLOBAL = newReferenceById("ref_mhg", REFERENCE_TYPE, MONOMORPHIC,
		HistoryType.HISTORIC, BRANCH_GLOBAL);

	private static MOReference REFERENCE_MONO_HIST_LOCAL = newReferenceById("ref_mhl", REFERENCE_TYPE, MONOMORPHIC,
		HistoryType.HISTORIC, !BRANCH_GLOBAL);

	private static MOReference REFERENCE_MONO_CUR_GLOBAL = newReferenceById("ref_mcg", REFERENCE_TYPE, MONOMORPHIC,
		HistoryType.CURRENT, BRANCH_GLOBAL);

	private static MOReference REFERENCE_MONO_CUR_LOCAL = newReferenceById("ref_mcl", REFERENCE_TYPE, MONOMORPHIC,
		HistoryType.CURRENT, !BRANCH_GLOBAL);

	private static MOReference REFERENCE_MONO_MIXED_GLOBAL = newReferenceById("ref_mmg", REFERENCE_TYPE, MONOMORPHIC,
		HistoryType.MIXED, BRANCH_GLOBAL);

	private static MOReference REFERENCE_MONO_MIXED_LOCAL = newReferenceById("ref_mml", REFERENCE_TYPE, MONOMORPHIC,
		HistoryType.MIXED, !BRANCH_GLOBAL);

	private static MOReference REFERENCE_POLY_HIST_GLOBAL = newReferenceById("ref_phg", REFERENCE_TYPE, !MONOMORPHIC,
		HistoryType.HISTORIC, BRANCH_GLOBAL);

	private static MOReference REFERENCE_POLY_HIST_LOCAL = newReferenceById("ref_phl", REFERENCE_TYPE, !MONOMORPHIC,
		HistoryType.HISTORIC, !BRANCH_GLOBAL);

	private static MOReference REFERENCE_POLY_CUR_GLOBAL = newReferenceById("ref_pcg", REFERENCE_TYPE, !MONOMORPHIC,
		HistoryType.CURRENT, BRANCH_GLOBAL);

	private static MOReference REFERENCE_POLY_CUR_LOCAL = newReferenceById("ref_pcl", REFERENCE_TYPE, !MONOMORPHIC,
		HistoryType.CURRENT, !BRANCH_GLOBAL);

	private static MOReference REFERENCE_POLY_MIXED_GLOBAL = newReferenceById("ref_pmg", REFERENCE_TYPE, !MONOMORPHIC,
		HistoryType.MIXED, BRANCH_GLOBAL);

	private static MOReference REFERENCE_POLY_MIXED_LOCAL = newReferenceById("ref_pml", REFERENCE_TYPE, !MONOMORPHIC,
		HistoryType.MIXED, !BRANCH_GLOBAL);

	/**
	 * Another subclass of {@link #A}
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#D
	 */
	private static final MOClass D = new MOKnowledgeItemImpl(T_D_NAME);
	static {
		D.setAbstract(false);
		D.setSuperclass(A);
		try {
			D.addAttribute(D1);
			// D2 is made a flex attribute after migration.

			D.addAttribute(REFERENCE_MONO_HIST_GLOBAL);
			D.addAttribute(REFERENCE_MONO_HIST_LOCAL);
			D.addAttribute(REFERENCE_MONO_CUR_GLOBAL);
			D.addAttribute(REFERENCE_MONO_CUR_LOCAL);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setApplicationType(D, DObj.class);
	}

	/**
	 * @see KnowledgeBaseTestScenarioImpl#D_OVERRIDE
	 */
	private static final MOClass D_OVERRIDE = new MOKnowledgeItemImpl(
		KnowledgeBaseTestScenarioConstants.D_OVERRIDE_NAME);
	static {
		D_OVERRIDE.setSuperclass(D);
		setApplicationType(D_OVERRIDE, DObj.class);
	}

	/**
	 * Another subclass of {@link #A}
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#E
	 */
	private static final MOKnowledgeItemImpl E = new MOKnowledgeItemImpl(T_E_NAME);
	static {
		E.setAbstract(false);
		E.setSuperclass(D);
		MOKnowledgeItemUtil.setImplementationFactory(E, StaticKnowledgeObjectFactory.INSTANCE);
		try {
			E.addAttribute(newReferenceById(
				UNTYPED_POLY_CUR_GLOBAL_NAME, new DeferredMetaObject(BasicTypes.ITEM_TYPE_NAME), !MONOMORPHIC,
				HistoryType.CURRENT, BRANCH_GLOBAL));

			E.addAttribute(REFERENCE_POLY_HIST_GLOBAL);
			E.addAttribute(REFERENCE_POLY_HIST_LOCAL);
			E.addAttribute(REFERENCE_POLY_CUR_GLOBAL);
			E.addAttribute(REFERENCE_POLY_CUR_LOCAL);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setApplicationType(E, EObj.class);
	}

	/**
	 * Type for testing attribute types.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#X
	 */
	private static final MOClass X;
	
	/**
	 * Boolean attribute.
	 */
	private static final MOAttributeImpl X1 = new MOAttributeImpl(T_X1_NAME, MOPrimitive.BOOLEAN);

	/**
	 * Byte attribute.
	 */
	private static final MOAttributeImpl X2 = new MOAttributeImpl(T_X2_NAME, MOPrimitive.BYTE);
    
	/**
	 * Character attribute.
	 */
	private static final MOAttributeImpl X3 = new MOAttributeImpl(T_X3_NAME, MOPrimitive.CHARACTER);
    
	/**
	 * Date attribute.
	 */
	private static final MOAttributeImpl X4 = new MOAttributeImpl(T_X4_NAME, MOPrimitive.DATE);
    
	/**
	 * Double attribute.
	 */
	private static final MOAttributeImpl X5 = new MOAttributeImpl(T_X5_NAME, MOPrimitive.DOUBLE);
    
	/**
	 * Float attribute.
	 */
	private static final MOAttributeImpl X6 = new MOAttributeImpl(T_X6_NAME, MOPrimitive.FLOAT);
    
	/**
	 * Integer attribute.
	 */
	private static final MOAttributeImpl X7 = new MOAttributeImpl(T_X7_NAME, MOPrimitive.INTEGER);
    
	/**
	 * Long attribute.
	 */
	private static final MOAttributeImpl X8 = new MOAttributeImpl(T_X8_NAME, MOPrimitive.LONG);
    
	/**
	 * Short attribute.
	 */
	private static final MOAttributeImpl X9 = new MOAttributeImpl(T_X9_NAME, MOPrimitive.SHORT);
    
	/**
	 * String attribute.
	 */
	private static final MOAttributeImpl X10 = new MOAttributeImpl(T_X10_NAME, MOPrimitive.STRING);
	
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(T_X_NAME);
		type.setAbstract(false);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		try {
			type.addAttribute(X1);
			type.addAttribute(X2);
			type.addAttribute(X3);
			type.addAttribute(X4);
			type.addAttribute(X5);
			type.addAttribute(X6);
			type.addAttribute(X7);
			type.addAttribute(X8);
			type.addAttribute(X9);
			type.addAttribute(X10);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		X = type;
	}
	
	
	/**
	 * Type for testing indices.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#Y
	 */
	private static final MOClass Y;
    
	/**
	 * Integer attribute.
	 */
	private static final MOAttributeImpl Y1 =
		new MOAttributeImpl(T_Y1_NAME, MOPrimitive.INTEGER, MOAttribute.MANDATORY);
    
	/**
	 * Long attribute.
	 */
	private static final MOAttributeImpl Y2 = new MOAttributeImpl(T_Y2_NAME, MOPrimitive.LONG, MOAttribute.MANDATORY);
    
	/**
	 * String attribute.
	 */
	private static final MOAttributeImpl Y3 = new MOAttributeImpl(T_Y3_NAME, MOPrimitive.STRING, MOAttribute.MANDATORY);
	
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(T_Y_NAME);
		type.setAbstract(false);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		{
			type.addAttribute(Y1);
			type.addAttribute(Y2);
			type.addAttribute(Y3);
		
			type.addIndex(new MOIndexImpl("YI1", Y1.getDbMapping()));
			type.addIndex(new MOIndexImpl("YI2",
				ArrayUtil.join(Y1.getDbMapping(), Y2.getDbMapping(), DBAttribute.class)));
			type.addIndex(new MOIndexImpl("YI3",
				ArrayUtil.join(Y2.getDbMapping(), Y3.getDbMapping(), DBAttribute.class)));
        }
		Y = type;
	}
	
	/**
	 * Type for testing attribute value ranges.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#Z
	 */
	private static final MOClass Z;
    
	/**
	 * Name attributes
	 */
	private static final MOAttributeImpl Z1 = new MOAttributeImpl(T_Z1_NAME, MOPrimitive.STRING);

	/**
	 * Double attribute with float value range.
	 * 
	 * @see TestDBKnowledgeBase#testCleanupAfterOutOfRangeFailure()
	 */
	private static final MOAttributeImpl Z2 = new MOAttributeImpl(T_Z2_NAME, "Z2", MOPrimitive.DOUBLE, false, false,
		DBType.FLOAT, 10, 2);
	
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(T_Z_NAME);
		type.setAbstract(false);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		{
			type.addAttribute(Z2);
			
			// Note: The test for cleanup after failed commit in Oracle depends
			// on the order of attributes. Without cleaned connections after
			// failed commit, the test only breaks, if the string attribute is
			// after the double attribute.
			type.addAttribute(Z1);
        }
		
		Z = type;
	}
	
	/**
	 * @see KnowledgeBaseTestScenarioImpl#AB
	 */
	private static final MOClass AB = new MOKnowledgeItemImpl(T_AB_NAME);
	static {
		AB.setAbstract(false);
		AB.setSuperclass(KNOWLEDGE_ASSOCIATION);
		try {
			AB.addAttribute(AB1);
			AB.addAttribute(AB2);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * @see KnowledgeBaseTestScenarioImpl#BC
	 */
	private static final MOClass BC = new MOKnowledgeItemImpl(T_BC_NAME);
	static {
		BC.setAbstract(false);
		BC.setSuperclass(KNOWLEDGE_ASSOCIATION);
		try {
			BC.addAttribute(BC1);
			BC.addAttribute(BC2);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}
	
	/**
	 * @see KnowledgeBaseTestScenarioImpl#UA
	 */
	private static final MOClass UA = new MOKnowledgeItemImpl(T_UA_NAME);
	static {
		UA.setAbstract(false);
		UA.setSuperclass(KNOWLEDGE_ASSOCIATION);
		UA.setVersioned(false);
		try {
			UA.addAttribute(UA1);
			UA.addAttribute(UA2);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Adds the types created in this scenario to the given repository
	 */
	public static List<TypeProvider> getMigrationTypes() {
		return Arrays.asList(
			KnowledgeBaseTestScenarioImpl.newCopyProvider(A),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(B),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(U),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(C),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(D),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(D_OVERRIDE),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(X),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(Y),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(Z),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(AB),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(BC),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(UA),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(KnowledgeBaseTestScenarioImpl.S),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(E),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(KnowledgeBaseTestScenarioImpl.F),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(KnowledgeBaseTestScenarioImpl.G),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(KnowledgeBaseTestScenarioImpl.H),
			KnowledgeBaseTestScenarioImpl.newCopyProvider(KnowledgeBaseTestScenarioImpl.BD)
			);
	}

}
