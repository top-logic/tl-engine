/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.Set;

import junit.framework.Test;

import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test that the role a role rule gives through a reference is gone as soon as the change of the
 * reference taking it away is committed, also for the queries asked on the same thread before.
 */
@SuppressWarnings("javadoc")
public class TestRoleRevokeByReference extends AbstractModelAccessTest {

	public void testRevokeByReference() {
		becomeUser(_roleless);
		BoundRole responsible = BoundedRole.getRoleByName(ROLE_RESPONSIBLE);
		assertNotNull(responsible);

		assertFalse("No role before the grant.", anywhere(_roleless, responsible));
		assertFalse(budgetReadable());

		setResponsible(_roleless);
		assertTrue("Role after the grant.", anywhere(_roleless, responsible));
		assertTrue(onProject(_roleless, responsible));
		assertTrue(budgetReadable());

		setResponsible(_responsible);
		assertFalse("Role on the project after the revoke.", onProject(_roleless, responsible));
		assertFalse("Role on the task after the revoke.", onTask(_roleless, responsible));
		assertFalse("Role on any object after the revoke.", anywhere(_roleless, responsible));
		assertFalse("Budget readable after the revoke.", budgetReadable());
	}

	private boolean budgetReadable() {
		return ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.READ, type(PROJECT), part(PROJECT, BUDGET))
			.isExecutable();
	}

	private static boolean anywhere(Person person, BoundRole role) {
		return AccessManager.getInstance().hasRoleOnAnyObject(person, Set.of(role));
	}

	private boolean onProject(Person person, BoundRole role) {
		return AccessManager.getInstance().hasRole(person, (BoundObject) _project, Set.of(role));
	}

	private boolean onTask(Person person, BoundRole role) {
		return AccessManager.getInstance().hasRole(person, (BoundObject) _task, Set.of(role));
	}

	private void setResponsible(Person person) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(RESPONSIBLE, person);
			tx.commit();
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestRoleRevokeByReference.class);
	}

}
