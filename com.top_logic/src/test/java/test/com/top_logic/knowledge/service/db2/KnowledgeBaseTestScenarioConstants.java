/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;

/**
 * Names for {@link KnowledgeBaseTestScenarioImpl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KnowledgeBaseTestScenarioConstants {

	boolean MONOMORPHIC = true;

	boolean BRANCH_GLOBAL = true;

	/**
	 * @see KnowledgeBaseTestScenarioImpl#E
	 */
	String E_NAME = "E";

	/**
	 * Integer (ordering) attribute in {@link #E_NAME}.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#E
	 */
	String E1_NAME = "e1";

	/**
	 * Integer (ordering) attribute in {@link #E_NAME}.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#E
	 */
	String E2_NAME = "e2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#G
	 */
	String G_NAME = "G";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#F
	 */
	String F_NAME = "F";

	/**
	 * Name of a polymorphic, current, branch global attribute of {@link #E_NAME} and
	 * {@link #G_NAME} which has no type information, i.e. each item can be stored.
	 */
	String UNTYPED_POLY_CUR_GLOBAL_NAME = "untyped_pcg";

	/**
	 * Name of a polymorphic, current, branch global attribute of {@link #E_NAME} and
	 * {@link #G_NAME} which contains an {@link MOAlternative} {@link #BD_NAME}.
	 */
	String ALTERNATIVE_POLY_CUR_GLOBAL_NAME = "alternative_pcg";

	/**
	 * Name of a polymorphic, current, branch local attribute of {@link #E_NAME} and {@link #G_NAME}
	 * .
	 */
	String REFERENCE_POLY_CUR_LOCAL_NAME = "ref_pcl";

	/**
	 * Name of a polymorphic, historic, branch global attribute of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_POLY_CUR_GLOBAL_NAME = "ref_pcg";

	/**
	 * Name of a polymorphic, mixed, branch local attribute of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_POLY_MIXED_LOCAL_NAME = "ref_pml";

	/**
	 * Name of a polymorphic, mixed, branch global attribute of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_POLY_MIXED_GLOBAL_NAME = "ref_pmg";

	/**
	 * Name of a polymorphic, historic, branch local attribute of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_POLY_HIST_LOCAL_NAME = "ref_phl";

	/**
	 * Name of a polymorphic, historic, branch global attribute of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_POLY_HIST_GLOBAL_NAME = "ref_phg";

	/**
	 * Name of a monomorphic, mixed, branch local attribute of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_MONO_MIXED_LOCAL_NAME = "ref_mml";

	/**
	 * Name of a monomorphic, mixed, branch global attribute of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_MONO_MIXED_GLOBAL_NAME = "ref_mmg";

	/**
	 * Name of a monomorphic, current, branch local attribute of {@link #D_NAME} and {@link #G_NAME}.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#D
	 * @see KnowledgeBaseTestScenarioImpl#G
	 */
	String REFERENCE_MONO_CUR_LOCAL_NAME = "ref_mcl";

	/**
	 * Name of a monomorphic, current, branch global attribute of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_MONO_CUR_GLOBAL_NAME = "ref_mcg";

	/**
	 * Name of a monomorphic, historic, branch local attribute of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_MONO_HIST_LOCAL_NAME = "ref_mhl";

	/**
	 * Name of a monomorphic, historic, branch global attribute of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_MONO_HIST_GLOBAL_NAME = "ref_mhg";

	/**
	 * Name of an attribute with {@link DeletionPolicy#DELETE_REFERER} of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_DELETE_POLICY_NAME = "ref_del_policy";

	/**
	 * Name of an attribute with {@link DeletionPolicy#CLEAR_REFERENCE} of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_CLEAR_POLICY_NAME = "ref_clear_policy";

	/**
	 * Name of an attribute with {@link DeletionPolicy#VETO} of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_VETO_POLICY_NAME = "ref_veto_policy";

	/**
	 * Name of an attribute with {@link DeletionPolicy#STABILISE_REFERENCE} of {@link #E_NAME} and
	 * {@link #G_NAME}.
	 */
	String REFERENCE_STABILISE_POLICY_NAME = "ref_stab_policy";

	/**
	 * Name of an {@link MOReference#isContainer() container} attribute with
	 * {@link DeletionPolicy#DELETE_REFERER} of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_DELETE_POLICY_CONTAINER_NAME = "ref_del_policy_con";

	/**
	 * Name of an {@link MOReference#isContainer() container} attribute with
	 * {@link DeletionPolicy#CLEAR_REFERENCE} of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_CLEAR_POLICY_CONTAINER_NAME = "ref_clear_policy_con";

	/**
	 * Name of an {@link MOReference#isContainer() container} attribute with
	 * {@link DeletionPolicy#VETO} of {@link #E_NAME} and {@link #G_NAME}.
	 */
	String REFERENCE_VETO_POLICY_CONTAINER_NAME = "ref_veto_policy_cont";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#S
	 */
	String S_NAME = "S";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#BC
	 */
	String BC_NAME = "BC";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#UA
	 */
	String UA_NAME = "UA";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#AB
	 */
	String AB_NAME = "AB";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Z
	 */
	String Z_NAME = "Z";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Z1
	 */
	String Z1_NAME = "z1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Z2
	 */
	String Z2_NAME = "z2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Y
	 */
	String Y_NAME = "Y";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Y1
	 */
	String Y1_NAME = "y1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Y2
	 */
	String Y2_NAME = "y2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#Y3
	 */
	String Y3_NAME = "y3";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X
	 */
	String X_NAME = "X";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X1
	 */
	String X1_NAME = "x1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X2
	 */
	String X2_NAME = "x2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X3
	 */
	String X3_NAME = "x3";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X4
	 */
	String X4_NAME = "x4";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X5
	 */
	String X5_NAME = "x5";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X6
	 */
	String X6_NAME = "x6";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X7
	 */
	String X7_NAME = "x7";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X8
	 */
	String X8_NAME = "x8";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X9
	 */
	String X9_NAME = "x9";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#X10
	 */
	String X10_NAME = "x10";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#D
	 */
	String D_NAME = "D";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#D_OVERRIDE
	 */
	String D_OVERRIDE_NAME = "D_OVERRIDE";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#H
	 */
	String H_NAME = "H";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#C
	 */
	String C_NAME = "C";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#U
	 */
	String U_NAME = "U";

	/**
	 * Note: The type name of the type b is lower-case for tests that check correct internal event
	 * ordering.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#B
	 */
	String B_NAME = "b";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#S
	 */
	String S_TYPE_NAME = "sType";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#S
	 */
	String S1_NAME = "s1";

	/**
	 * Name of a polymorphic current local reference in {@link #S_NAME} to objects of type
	 * {@link #REFERENCE_TYPE_NAME}.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#S
	 */
	String S_REFERENCE_ATTR = "ref_poly_cur_local";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#A
	 */
	String A_NAME = "A";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#D1
	 */
	String D1_NAME = "d1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#D2
	 */
	String D2_NAME = "d2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#C1
	 */
	String C1_NAME = "c1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#C2
	 */
	String C2_NAME = "c2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#F3
	 */
	String F3_NAME = "f3";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#B1
	 */
	String B1_NAME = "b1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#B2
	 */
	String B2_NAME = "b2";

	/**
	 * Binary string attribute.
	 * 
	 * @see KnowledgeBaseTestScenarioImpl#B3
	 */
	String B3_NAME = "b3";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#BC1
	 */
	String BC1_NAME = "bc1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#BC2
	 */
	String BC2_NAME = "bc2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#UA1
	 */
	String UA1_NAME = "ua1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#UA2
	 */
	String UA2_NAME = "ua2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#AB1
	 */
	String AB1_NAME = "ab1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#AB2
	 */
	String AB2_NAME = "ab2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#A1
	 */
	String A1_NAME = "a1";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#A2
	 */
	String A2_NAME = "a2";

	/**
	 * @see KnowledgeBaseTestScenarioImpl#REFERENCE_TYPE
	 */
	String REFERENCE_TYPE_NAME = D_NAME;

	/** @see KnowledgeBaseTestScenarioImpl#BD */
	String BD_NAME = "BUnionD";

}
