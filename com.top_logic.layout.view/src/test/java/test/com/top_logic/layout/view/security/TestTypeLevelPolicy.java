/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the type level decision of {@link ModelAccessPolicy#isOffered}.
 *
 * <p>
 * A {@value #STEP} delegates to its project and declares Finish to be implied by Read, so asking
 * Finish on a step asks two operations on the project. The user of the test holds the finisher
 * role on a project only, which grants Finish but not Read.
 * </p>
 *
 * <p>
 * An {@value #ANNEX} delegates Read to its project and grants Write to the finisher role itself.
 * </p>
 */
public class TestTypeLevelPolicy extends AbstractModelAccessTest {

	/** Name of the type delegating only Read to its project. */
	private static final String ANNEX = "Annex";

	/**
	 * Finishing is offered because Finish is grantable on the parent type, although Read, the other
	 * operation asked on it, is not: asking one operation on the parent type must not hide the
	 * parent type from the next one.
	 */
	public void testImplyingOperationDoesNotHideImpliedOne() {
		Person finisher = TestPerson.createPerson("viewModelAccessTypeFinisher");
		try {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				_project.tUpdateByName("finisher", finisher);
				tx.commit();
			}
			BoundCommandGroup finish = CommandGroupRegistry.resolve(FINISH);
			becomeUser(finisher);
			assertTrue("Finish of a step is decided by Finish on the project.",
				ModelAccessPolicy.isOffered(finish, type(STEP)));
			assertFalse("Write is granted to the responsible only.",
				ModelAccessPolicy.isOffered(SimpleBoundCommandGroup.WRITE, type(STEP)));
		} finally {
			becomeUser(_root);
			TestPerson.deletePersonAndUser(finisher);
		}
	}

	/**
	 * An operation a restricted access parent does not delegate is offered by the grants of the
	 * type itself, a delegated one by the types the access parent leads to.
	 */
	public void testRestrictedAccessParentOwnGrant() {
		Person finisher = TestPerson.createPerson("viewModelAccessAnnexFinisher");
		try {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				_project.tUpdateByName("finisher", finisher);
				tx.commit();
			}
			becomeUser(finisher);
			assertTrue("Write of an annex is granted to the finisher role by the annex itself.",
				ModelAccessPolicy.isOffered(SimpleBoundCommandGroup.WRITE, type(ANNEX)));
			assertFalse("Read of an annex is decided by Read on the project, not granted to the finisher.",
				ModelAccessPolicy.isOffered(SimpleBoundCommandGroup.READ, type(ANNEX)));

			becomeUser(_roleless);
			assertFalse(ModelAccessPolicy.isOffered(SimpleBoundCommandGroup.WRITE, type(ANNEX)));
		} finally {
			becomeUser(_root);
			TestPerson.deletePersonAndUser(finisher);
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suiteWith(TestTypeLevelPolicy.class, TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE);
	}

}
