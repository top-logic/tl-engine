/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Arrays;
import java.util.List;

import test.com.top_logic.knowledge.service.TestKABasedCacheOrdered.BObjExtended;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SA;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SB;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SC;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MOAlternativeBuilder;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOAlternativeImpl;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.KIReference;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemUtil;
import com.top_logic.knowledge.service.db2.StaticKnowledgeObjectFactory;
import com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.binding.ImplementationBinding;
import com.top_logic.knowledge.wrap.binding.MonomorphicBinding;
import com.top_logic.knowledge.wrap.binding.PolymorphicBinding;
import com.top_logic.knowledge.wrap.binding.PolymorphicBinding.Config.Binding;

/**
 * Static initializers of types for {@link KnowledgeBase} testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeBaseTestScenarioImpl implements KnowledgeBaseTestScenario, KnowledgeBaseTestScenarioConstants {

	/**
	 * Singleton {@link KnowledgeBaseTestScenarioImpl} instance.
	 */
	public static final KnowledgeBaseTestScenarioImpl INSTANCE = new KnowledgeBaseTestScenarioImpl();

	/**
	 * Creates a {@link KnowledgeBaseTestScenarioImpl}.
	 */
	protected KnowledgeBaseTestScenarioImpl() {
		// Singleton constructor.
	}

	public static final MOAttributeImpl A1 = new MOAttributeImpl(A1_NAME, MOPrimitive.STRING, true);
	static {
		// Search in CLOB types not supported by Oracle.
//		A1.setSQLType(Types.CLOB);
		A1.setSQLType(DBType.STRING);
	}
	
	public static final MOAttributeImpl A2;
    static {
    	A2 = new MOAttributeImpl(A2_NAME, MOPrimitive.STRING);
		// Search in CLOB types not supported by Oracle.
//    	A2.setSQLType(Types.CLOB);
		A2.setSQLType(DBType.STRING);
    	A2.setBinary(true);
    }

    public static final MOAttributeImpl AB1 = new MOAttributeImpl(AB1_NAME, MOPrimitive.STRING);
    
    /**
     * Attribute of {@link #AB} that is renamed to {@link KnowledgeBaseMigrationTestScenarioImpl#AB2}.
     */
    public static final MOAttributeImpl AB2 = new MOAttributeImpl(AB2_NAME, MOPrimitive.STRING);
    
    public static final MOAttributeImpl BC1 = new MOAttributeImpl(BC1_NAME, MOPrimitive.STRING);
    public static final MOAttributeImpl BC2 = new MOAttributeImpl(BC2_NAME, MOPrimitive.STRING);
    
	public static final MOAttributeImpl UA1 = new MOAttributeImpl(UA1_NAME, MOPrimitive.STRING);

	public static final MOAttributeImpl UA2 = new MOAttributeImpl(UA2_NAME, MOPrimitive.STRING);

    public static final MOAttributeImpl B1 = new MOAttributeImpl(B1_NAME, MOPrimitive.STRING);
    public static final MOAttributeImpl B2 = new MOAttributeImpl(B2_NAME, MOPrimitive.STRING);

	/**
	 * Binary String attribute.
	 */
	public static final MOAttributeImpl B3 = new MOAttributeImpl(B3_NAME, MOPrimitive.STRING);
	static {
		B3.setBinary(true);
	}

    public static final MOAttributeImpl F3 = new MOAttributeImpl(F3_NAME, MOPrimitive.STRING);

    public static final MOAttributeImpl C1 = new MOAttributeImpl(C1_NAME, MOPrimitive.STRING);
    public static final MOAttributeImpl C2 = new MOAttributeImpl(C2_NAME, MOPrimitive.STRING);

    public static final MOAttributeImpl D1 = new MOAttributeImpl(D1_NAME, MOPrimitive.STRING);
    
    /**
     * Row attribute of {@link #D} that is made a flex attribute in migration scenario.
     */
    public static final MOAttributeImpl D2 = new MOAttributeImpl(D2_NAME, MOPrimitive.STRING);
    
    /**
     * Placeholder for the predefinded object type.
     */
	public static final MOClass KNOWLEDGE_OBJECT = new DeferredMetaObject(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME);

	/**
	 * Placeholder for the predefinded association type.
	 */
	public static final MOClass KNOWLEDGE_ASSOCIATION = new DeferredMetaObject(BasicTypes.ASSOCIATION_TYPE_NAME);

	/**
	 * Abstract super class of {@link #B} and {@link #C}
	 */
	public static final MOClass A = new MOKnowledgeItemImpl(A_NAME);

	static {
		A.setAbstract(true);
		A.setSuperclass(KNOWLEDGE_OBJECT);
		try {
			A.addAttribute(A1);
			A.addAttribute(A2);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Concrete super class of {@link #C} extending abstract base class {@link #A}.
	 */
	public static final MOClass B = new MOKnowledgeItemImpl(B_NAME);
	static {
		B.setAbstract(false);
		B.setSuperclass(A);
		try {
			B.addAttribute(B1);
			B.addAttribute(B2);
			B.addAttribute(B3);
			B.addAttribute(F3);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setApplicationType(B, BObjExtended.class);
	}

	/**
	 * Unversioned variant of {@link #B}.
	 */
	public static final MOKnowledgeItemImpl U = new MOKnowledgeItemImpl(U_NAME);
	static {
		MOKnowledgeItemUtil.setImplementationFactory(U, StaticKnowledgeObjectFactory.INSTANCE);
		U.setAbstract(false);
		U.setSuperclass(B);
		U.setVersioned(false);
	}
	
	/**
	 * Concrete subclass of {@link #B} with "deep" inheritance hierarchy.
	 */
	public static final MOClass C;
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(C_NAME);
		type.setAbstract(false);
		type.setSuperclass(B);
		type.setFinal(true);
		try {
			type.addAttribute(C1);
			type.addAttribute(C2);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setApplicationType(type, SimpleWrapperFactoryTestScenario.CObj.class);
		C = type;
	}
	
	final static MOClass REFERENCE_TYPE = new DeferredMetaObject(REFERENCE_TYPE_NAME);

	/**
	 * Another subclass of {@link #A}
	 */
	public static final MOClass D = new MOKnowledgeItemImpl(D_NAME);
	static {
		D.setAbstract(false);
		D.setSuperclass(A);
		try {
			D.addAttribute(D1);
			D.addAttribute(D2);
			D.addAttribute(newReferenceById(
				REFERENCE_MONO_HIST_GLOBAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_MONO_HIST_LOCAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_MONO_CUR_GLOBAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_MONO_CUR_LOCAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_MONO_MIXED_GLOBAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_MONO_MIXED_LOCAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL));

			D.addAttribute(newReferenceById(
				REFERENCE_POLY_HIST_GLOBAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_POLY_HIST_LOCAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_POLY_CUR_GLOBAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_POLY_CUR_LOCAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_POLY_MIXED_GLOBAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL));
			D.addAttribute(newReferenceById(
				REFERENCE_POLY_MIXED_LOCAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL));

			setApplicationType(D, DObj.class);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Subclass of {@link KnowledgeBaseTestScenarioConstants#D_NAME} in which polymorphic attributes
	 * are overridden. In this type the references points to objects of type
	 * {@link KnowledgeBaseTestScenarioConstants#D_OVERRIDE_NAME}.
	 */
	public static final MOClass D_OVERRIDE = new MOKnowledgeItemImpl(D_OVERRIDE_NAME);
	static {
		D_OVERRIDE.setSuperclass(D);
		D_OVERRIDE.overrideAttribute(newReferenceById(
			REFERENCE_POLY_HIST_GLOBAL_NAME, D_OVERRIDE, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
		D_OVERRIDE.overrideAttribute(newReferenceById(
			REFERENCE_POLY_HIST_LOCAL_NAME, D_OVERRIDE, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));
		D_OVERRIDE.overrideAttribute(newReferenceById(
			REFERENCE_POLY_CUR_GLOBAL_NAME, D_OVERRIDE, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL));
		D_OVERRIDE.overrideAttribute(newReferenceById(
			REFERENCE_POLY_CUR_LOCAL_NAME, D_OVERRIDE, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL));
		D_OVERRIDE.overrideAttribute(newReferenceById(
			REFERENCE_POLY_MIXED_GLOBAL_NAME, D_OVERRIDE, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL));
		D_OVERRIDE.overrideAttribute(newReferenceById(
			REFERENCE_POLY_MIXED_LOCAL_NAME, D_OVERRIDE, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL));
		setApplicationType(D_OVERRIDE, DObj.class);
	}

	/**
	 * Unversioned variant of {@link #D}.
	 */
	public static final MOKnowledgeItemImpl H = new MOKnowledgeItemImpl(H_NAME);
	static {
		MOKnowledgeItemUtil.setImplementationFactory(H, StaticKnowledgeObjectFactory.INSTANCE);
		H.setAbstract(false);
		H.setSuperclass(D);
		H.setVersioned(false);
	}

	private static MOReference newContainerReferenceByIdForDeletionTest(String name, MetaObject targetType,
			DeletionPolicy deletionPolicy) {
		MOReference result = newReferenceById(name, targetType, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		result.setDeletionPolicy(deletionPolicy);
		result.setContainer(true);
		return result;
	}

	private static MOReference newReferenceByIdForDeletionTest(String name, MetaObject targetType,
			DeletionPolicy deletionPolicy) {
		MOReference result = newReferenceById(name, targetType, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		result.setDeletionPolicy(deletionPolicy);
		return result;
	}

	static MOReference newReferenceById(String name, MetaObject targetType, boolean monomorphic,
			HistoryType historyType, boolean branchGlobal) {
		MOReference result = KIReference.referenceById(name, targetType);
		result.setMonomorphic(monomorphic);
		result.setHistoryType(historyType);
		result.setBranchGlobal(branchGlobal);
		return result;
	}

	private static MOReference newContainerReferenceByValueForDeletionTest(String name, MetaObject targetType,
			DeletionPolicy deletionPolicy) {
		MOReference result = newReferenceByValue(name, targetType, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		result.setDeletionPolicy(deletionPolicy);
		result.setContainer(true);
		return result;
	}

	private static MOReference newReferenceByValueForDeletionTest(String name, MetaObject targetType,
			DeletionPolicy deletionPolicy) {
		MOReference result = newReferenceByValue(name, targetType, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		result.setDeletionPolicy(deletionPolicy);
		return result;
	}

	private static MOReference newReferenceByValue(String name, MetaObject targetType, boolean monomorphic,
			HistoryType historyType, boolean branchGlobal) {
		MOReference result = KIReference.referenceByValue(name, targetType);
		result.setMonomorphic(monomorphic);
		result.setHistoryType(historyType);
		result.setBranchGlobal(branchGlobal);
		return result;
	}

	/**
	 * Another subclass of {@link #A} with many {@link KIReference} attributes that hold the
	 * identifier of the objects.
	 * 
	 * @see KIReference#referenceById(String, MetaObject)
	 */
	public static final MOKnowledgeItemImpl E = new MOKnowledgeItemImpl(E_NAME);
	static {
		E.setAbstract(false);
		E.setSuperclass(D);
		try {
			E.addAttribute(new MOAttributeImpl(E1_NAME, MOPrimitive.INTEGER));
			E.addAttribute(new MOAttributeImpl(E2_NAME, MOPrimitive.INTEGER));
			E.addAttribute(newReferenceById(
				UNTYPED_POLY_CUR_GLOBAL_NAME, new DeferredMetaObject(BasicTypes.ITEM_TYPE_NAME), !MONOMORPHIC,
				HistoryType.CURRENT, BRANCH_GLOBAL));

			E.addAttribute(newReferenceById(
				ALTERNATIVE_POLY_CUR_GLOBAL_NAME, new DeferredMetaObject(BD_NAME), !MONOMORPHIC,
				HistoryType.CURRENT, BRANCH_GLOBAL));

			E.addAttribute(newReferenceByIdForDeletionTest(
				REFERENCE_CLEAR_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.CLEAR_REFERENCE));
			E.addAttribute(newReferenceByIdForDeletionTest(
				REFERENCE_DELETE_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.DELETE_REFERER));
			E.addAttribute(newReferenceByIdForDeletionTest(
				REFERENCE_STABILISE_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.STABILISE_REFERENCE));
			E.addAttribute(newReferenceByIdForDeletionTest(
				REFERENCE_VETO_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.VETO));

			E.addAttribute(newContainerReferenceByIdForDeletionTest(
				REFERENCE_CLEAR_POLICY_CONTAINER_NAME, REFERENCE_TYPE, DeletionPolicy.CLEAR_REFERENCE));
			E.addAttribute(newContainerReferenceByIdForDeletionTest(
				REFERENCE_DELETE_POLICY_CONTAINER_NAME, REFERENCE_TYPE, DeletionPolicy.DELETE_REFERER));
			E.addAttribute(newContainerReferenceByIdForDeletionTest(
				REFERENCE_VETO_POLICY_CONTAINER_NAME, REFERENCE_TYPE, DeletionPolicy.VETO));
			setApplicationType(E, EObj.class);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Another subclass of {@link #A} which has many {@link KIReference} attributes that hold the
	 * whole item.
	 * 
	 * @see KIReference#referenceByValue(String, MetaObject)
	 */
	public static final MOKnowledgeItemImpl G = new MOKnowledgeItemImpl(G_NAME);
	static {
		G.setAbstract(false);
		G.setSuperclass(A);
		try {
			G.addAttribute(newReferenceByValue(
				REFERENCE_MONO_HIST_GLOBAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_MONO_HIST_LOCAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_MONO_CUR_GLOBAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_MONO_CUR_LOCAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_MONO_MIXED_GLOBAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_MONO_MIXED_LOCAL_NAME, REFERENCE_TYPE, MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_POLY_HIST_GLOBAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_POLY_HIST_LOCAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_POLY_CUR_GLOBAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_POLY_CUR_LOCAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_POLY_MIXED_GLOBAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL));
			G.addAttribute(newReferenceByValue(
				REFERENCE_POLY_MIXED_LOCAL_NAME, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL));

			G.addAttribute(newReferenceById(
				UNTYPED_POLY_CUR_GLOBAL_NAME, new DeferredMetaObject(BasicTypes.ITEM_TYPE_NAME), !MONOMORPHIC,
				HistoryType.MIXED, !BRANCH_GLOBAL));

			G.addAttribute(newReferenceByValueForDeletionTest(
				REFERENCE_CLEAR_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.CLEAR_REFERENCE));
			G.addAttribute(newReferenceByValueForDeletionTest(
				REFERENCE_DELETE_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.DELETE_REFERER));
			G.addAttribute(newReferenceByValueForDeletionTest(
				REFERENCE_STABILISE_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.STABILISE_REFERENCE));
			G.addAttribute(newReferenceByValueForDeletionTest(
				REFERENCE_VETO_POLICY_NAME, REFERENCE_TYPE, DeletionPolicy.VETO));

			G.addAttribute(newContainerReferenceByValueForDeletionTest(
				REFERENCE_CLEAR_POLICY_CONTAINER_NAME, REFERENCE_TYPE, DeletionPolicy.CLEAR_REFERENCE));
			G.addAttribute(newContainerReferenceByValueForDeletionTest(
				REFERENCE_DELETE_POLICY_CONTAINER_NAME, REFERENCE_TYPE, DeletionPolicy.DELETE_REFERER));
			G.addAttribute(newContainerReferenceByValueForDeletionTest(
				REFERENCE_VETO_POLICY_CONTAINER_NAME, REFERENCE_TYPE, DeletionPolicy.VETO));
			setApplicationType(G, EObj.class);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Subclass of custom association class {@link #E}
	 */
	public static final MOKnowledgeItemImpl F = new MOKnowledgeItemImpl(F_NAME);
	static {
		F.setAbstract(false);
		F.setSuperclass(E);
		setApplicationType(F, EObj.class);
	}

	/**
	 * Type for testing attribute types.
	 */
	public static final MOClass X;
	
	/**
	 * Boolean attribute.
	 */
	public static final MOAttributeImpl X1 = new MOAttributeImpl(X1_NAME, MOPrimitive.BOOLEAN);

	/**
	 * Byte attribute.
	 */
	public static final MOAttributeImpl X2 = new MOAttributeImpl(X2_NAME, MOPrimitive.BYTE);
    
	/**
	 * Character attribute.
	 */
	public static final MOAttributeImpl X3 = new MOAttributeImpl(X3_NAME, MOPrimitive.CHARACTER);
    
	/**
	 * Date attribute.
	 */
	public static final MOAttributeImpl X4 = new MOAttributeImpl(X4_NAME, MOPrimitive.DATE);
    
	/**
	 * Double attribute.
	 */
	public static final MOAttributeImpl X5 = new MOAttributeImpl(X5_NAME, MOPrimitive.DOUBLE);
    
	/**
	 * Float attribute.
	 */
	public static final MOAttributeImpl X6 = new MOAttributeImpl(X6_NAME, MOPrimitive.FLOAT);
    
	/**
	 * Integer attribute.
	 */
	public static final MOAttributeImpl X7 = new MOAttributeImpl(X7_NAME, MOPrimitive.INTEGER);
    
	/**
	 * Long attribute.
	 */
	public static final MOAttributeImpl X8 = new MOAttributeImpl(X8_NAME, MOPrimitive.LONG);
    
	/**
	 * Short attribute.
	 */
	public static final MOAttributeImpl X9 = new MOAttributeImpl(X9_NAME, MOPrimitive.SHORT);
    
	/**
	 * String attribute.
	 */
	public static final MOAttributeImpl X10 = new MOAttributeImpl(X10_NAME, MOPrimitive.STRING);
	
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(X_NAME);
		type.setAbstract(false);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		MOKnowledgeItemUtil.setImplementationFactory(type, StaticKnowledgeObjectFactory.INSTANCE);
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
	 * {@link MOAlternative} with specialisation {@link #B} and {@link #D}
	 */
	public static final MOAlternative BD;
	static {
		MOAlternativeBuilder builder = new MOAlternativeImpl(BD_NAME);
		builder.registerSpecialisation(B);
		builder.registerSpecialisation(D);
		BD = builder.createAlternative();
	}
	
	/**
	 * Type for testing indices.
	 */
	public static final MOClass Y;
    
	/**
	 * Integer attribute.
	 */
	public static final MOAttributeImpl Y1 = new MOAttributeImpl(Y1_NAME, MOPrimitive.INTEGER, MOAttribute.MANDATORY);
    
	/**
	 * Long attribute.
	 */
	public static final MOAttributeImpl Y2 = new MOAttributeImpl(Y2_NAME, MOPrimitive.LONG, MOAttribute.MANDATORY);
    
	/**
	 * String attribute.
	 */
	public static final MOAttributeImpl Y3 = new MOAttributeImpl(Y3_NAME, MOPrimitive.STRING, MOAttribute.MANDATORY);
	
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(Y_NAME);
		type.setAbstract(false);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		MOKnowledgeItemUtil.setImplementationFactory(type, StaticKnowledgeObjectFactory.INSTANCE);
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
	 */
	public static final MOClass Z;
    
	/**
	 * Name attributes
	 */
	public static final MOAttributeImpl Z1 = new MOAttributeImpl(Z1_NAME, MOPrimitive.STRING);

	/**
	 * Double attribute with float value range.
	 * 
	 * @see TestDBKnowledgeBase#testCleanupAfterOutOfRangeFailure()
	 */
	public static final MOAttributeImpl Z2 = new MOAttributeImpl(Z2_NAME, "Z2", MOPrimitive.DOUBLE, false, false,
		DBType.FLOAT, 10, 2);
	
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(Z_NAME);
		type.setAbstract(false);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		MOKnowledgeItemUtil.setImplementationFactory(type, StaticKnowledgeObjectFactory.INSTANCE);
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
	
	public static final MOClass AB = new MOKnowledgeItemImpl(AB_NAME);
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
	
	public static final MOClass BC = new MOKnowledgeItemImpl(BC_NAME);
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
	
	public static final MOClass UA = new MOKnowledgeItemImpl(UA_NAME);
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
	 * Type with sub-type column for testing dynamic wrapper resolution with shared storage type.
	 */
	public static final MOClass S;
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(S_NAME);
		type.setSuperclass(KNOWLEDGE_OBJECT);
		type.setAbstract(false);
		type.setFinal(true);
		try {
			MOAttributeImpl typeAttr = new MOAttributeImpl(S_TYPE_NAME, MOPrimitive.STRING);
			typeAttr.setInitial(true);
			type.addAttribute(typeAttr);
			MOAttributeImpl s1Attr = new MOAttributeImpl(S1_NAME, MOPrimitive.STRING);
			s1Attr.setInitial(true);
			type.addAttribute(s1Attr);
			MOReference referenceAttr =
				newReferenceById(S_REFERENCE_ATTR, REFERENCE_TYPE, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			type.addAttribute(referenceAttr);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setPolyMorphicBinding(type);
		S = type;
	}

	@Override
	public List<TypeProvider> getTestTypes() {
		return Arrays.asList(
			newCopyProvider(A),
			newCopyProvider(bType()),
			newCopyProvider(U),
			newCopyProvider(C),
			newCopyProvider(D),
			newCopyProvider(D_OVERRIDE),
			newCopyProvider(X),
			newCopyProvider(Y),
			newCopyProvider(Z),
			newCopyProvider(AB),
			newCopyProvider(BC),
			newCopyProvider(UA),
			newCopyProvider(S),
			newCopyProvider(E),
			newCopyProvider(F),
			newCopyProvider(G),
			newCopyProvider(H),
			newCopyProvider(BD)
			);
	}

	protected MOClass bType() {
		return B;
	}

	private static void setPolyMorphicBinding(MOClass type) {
		PolymorphicBinding.Config binding = polyBinding(S_TYPE_NAME);
		addBinding(binding, SA.SA_TYPE, SimpleWrapperFactoryTestScenario.SA.class);
		addBinding(binding, SB.SB_TYPE, SimpleWrapperFactoryTestScenario.SB.class);
		addBinding(binding, SC.SC_TYPE, SimpleWrapperFactoryTestScenario.SC.class);
		setBinding(type, binding);
	}

	public static PolymorphicBinding.Config polyBinding(String keyAtribute) {
		PolymorphicBinding.Config binding = TypedConfiguration.newConfigItem(PolymorphicBinding.Config.class);
		binding.setKeyAttribute(keyAtribute);
		return binding;
	}

	public static void addBinding(PolymorphicBinding.Config polyBinding, String keyValue,
			Class<? extends Wrapper> applicationType) {
		Binding binding = TypedConfiguration.newConfigItem(PolymorphicBinding.Config.Binding.class);
		binding.setKey(keyValue);
		binding.setApplicationType(applicationType);
		polyBinding.getBindings().put(binding.getKey(), binding);
	}

	public static void setApplicationType(MOClass table, Class<? extends Wrapper> applicationType) {
		setBinding(table, monomorphicBinding(applicationType));
	}

	public static void setBinding(MOClass table,
			PolymorphicConfiguration<? extends ImplementationBinding> binding) {
		ImplementationBindingAnnotation bindingAnnotation = TypedConfiguration.newConfigItem(ImplementationBindingAnnotation.class);
		bindingAnnotation.setBinding(binding);
		table.addAnnotation(bindingAnnotation);
	}

	private static MonomorphicBinding.Config monomorphicBinding(Class<? extends Wrapper> applicationType) {
		MonomorphicBinding.Config monoBinding = TypedConfiguration.newConfigItem(MonomorphicBinding.Config.class);
		monoBinding.setApplicationType(applicationType);
		return monoBinding;
	}

	/**
	 * Creates a {@link TypeProvider} which adds a copy of the given type to the
	 * {@link MORepository}.
	 */
	public static TypeProvider newCopyProvider(final MetaObject type) {
		return new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				try {
					typeRepository.addMetaObject(type.copy());
				} catch (DuplicateTypeException ex) {
					log.error("Unable to add " + type.getName(), ex);
				}
			}
		};
	}

}
