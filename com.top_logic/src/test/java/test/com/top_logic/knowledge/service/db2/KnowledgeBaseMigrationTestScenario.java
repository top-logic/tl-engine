/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.search.Flex;


/**
 * Interface repeating constants of {@link KnowledgeBaseMigrationTestScenarioImpl} as
 * workaround for missing static import in Java 2.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KnowledgeBaseMigrationTestScenario {
	
	/**
	 * Reference is monomorphic?
	 */
	boolean MONOMORPHIC = KnowledgeBaseTestScenarioConstants.MONOMORPHIC;

	/**
	 * Reference is branch global?
	 */
	boolean BRANCH_GLOBAL = KnowledgeBaseTestScenarioConstants.BRANCH_GLOBAL;

	/**
	 * Abstract super class of {@link #T_B_NAME} and {@link #T_C_NAME}
	 */
	String T_A_NAME = KnowledgeBaseTestScenarioConstants.A_NAME;

	/**
	 * String Attribute in {@link #T_A_NAME}
	 */
	String T_A1_NAME = "a1";

	/**
	 * Binary string Attribute in {@link #T_A_NAME}
	 */
	String T_A2_NAME = "a2";

	/**
	 * Row attribute of {@link #T_A_NAME} which was a {@link Flex} attribute before migration.
	 * 
	 * @see KnowledgeBaseTestScenarioConstants#F3_NAME
	 */
	String T_F1_NAME = "f1";

	/**
	 * Concrete super class of {@link #T_C_NAME} extending abstract base class {@link #T_A_NAME}.
	 */
	String T_B_NAME = "b";
	
	/**
	 * String attribute of {@link #T_B_NAME}
	 */
	String T_B1_NAME = "b1";

	/**
	 * String attribute of {@link #T_B_NAME} renamed during migration.
	 * 
	 * @see KnowledgeBaseTestScenarioConstants#B2_NAME
	 */
	String T_B2_NAME = "t_b2";

	/**
	 * String attribute of {@link #T_B_NAME}.
	 * 
	 * @see KnowledgeBaseTestScenarioConstants#B3_NAME
	 */
	String T_B3_NAME = KnowledgeBaseTestScenarioConstants.B3_NAME;

	/**
	 * Unversioned variant of {@link #T_B_NAME}.
	 */
	String T_U_NAME = KnowledgeBaseTestScenarioConstants.U_NAME;

	/**
	 * Concrete subclass of {@link #T_B_NAME} with "deep" inheritance hierarchy.
	 */
	String T_C_NAME = KnowledgeBaseTestScenarioConstants.C_NAME;
	
	/**
	 * String attribute of {@link #T_C_NAME}.
	 */
	String T_C1_NAME = "c1";

	/**
	 * Integer attribute of {@link #T_C_NAME}. Type has changed during migration.
	 * 
	 * @see KnowledgeBaseTestScenarioConstants#C2_NAME
	 */
	String T_C2_NAME = "c2";

	/**
	 * Integer attribute of {@link #T_C_NAME}. Created during migration.
	 */
	String T_C3_NAME = "c3";

	/**
	 * Another subclass of {@link #T_A_NAME}
	 */
	String T_D_NAME = KnowledgeBaseTestScenarioConstants.D_NAME;

	/**
	 * String attribute of {@link #T_D_NAME}
	 */
	String T_D1_NAME = "d1";

	/**
	 * Type for testing attribute types.
	 */
	String T_X_NAME = KnowledgeBaseTestScenarioConstants.X_NAME;
	
	/**
	 * Boolean attribute.
	 */
	String T_X1_NAME = "x1";

	/**
	 * Byte attribute.
	 */
	String T_X2_NAME = "x2";
    
	/**
	 * Character attribute.
	 */
	String T_X3_NAME = "x3";
    
	/**
	 * Date attribute.
	 */
	String T_X4_NAME = "x4";
    
	/**
	 * Double attribute.
	 */
	String T_X5_NAME = "x5";
    
	/**
	 * Float attribute.
	 */
	String T_X6_NAME = "x6";

	/**
	 * Integer attribute.
	 */
	String T_X7_NAME = "x7";
    
	/**
	 * Long attribute.
	 */
	String T_X8_NAME = "x8";
    
	/**
	 * Short attribute.
	 */
	String T_X9_NAME = "x9";
    
	/**
	 * String attribute.
	 */
	String T_X10_NAME = "x10";
	
	
	/**
	 * Type for testing indices.
	 */
	String T_Y_NAME = KnowledgeBaseTestScenarioConstants.Y_NAME;
    
	/**
	 * Integer attribute.
	 */
	String T_Y1_NAME = "y1";
    
	/**
	 * Long attribute.
	 */
	String T_Y2_NAME = "y2";
    
	/**
	 * String attribute.
	 */
	String T_Y3_NAME = "y3";
	
	/**
	 * @see KnowledgeBaseMigrationTestScenarioImpl#Z
	 */
	String T_Z_NAME = KnowledgeBaseTestScenarioConstants.Z_NAME;
	
	/**
	 * @see KnowledgeBaseMigrationTestScenarioImpl#Z1
	 */
	String T_Z1_NAME = "z1";
	
	/**
	 * @see KnowledgeBaseMigrationTestScenarioImpl#Z2
	 */
	String T_Z2_NAME = "z2";
	
	/**
	 * Some {@link KnowledgeAssociation}. Migrated from
	 * {@link KnowledgeBaseTestScenarioConstants#AB_NAME}
	 */
	String T_AB_NAME = KnowledgeBaseTestScenarioConstants.AB_NAME;
	
	/**
	 * String attribute of {@link #T_AB_NAME}
	 */
	String T_AB1_NAME = "ab1";

	/**
	 * String attribute of {@link #T_AB_NAME}. Renamed during migration.
	 * 
	 * @see KnowledgeBaseTestScenarioConstants#AB2_NAME
	 */
	String T_AB2_NAME = "t_ab2";

	/**
	 * Some {@link KnowledgeAssociation}.
	 */
	String T_BC_NAME = KnowledgeBaseTestScenarioConstants.BC_NAME;

	/**
	 * String attribute of {@link #T_BC_NAME}
	 */
	String T_BC1_NAME = "bc1";

	/**
	 * String attribute of {@link #T_BC_NAME}
	 */
	String T_BC2_NAME = "bc2";

	/**
	 * Unversioned {@link KnowledgeAssociation}
	 */
	String T_UA_NAME = KnowledgeBaseTestScenarioConstants.UA_NAME;

	/**
	 * String attribute of {@link #T_UA_NAME}
	 */
	String T_UA1_NAME = "ua1";

	/**
	 * String attribute of {@link #T_UA_NAME}
	 */
	String T_UA2_NAME = "ua2";

	/**
	 * Concrete subclass of {@link #T_D_NAME}
	 */
	String T_E_NAME = KnowledgeBaseTestScenarioConstants.E_NAME;

}
