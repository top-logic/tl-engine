/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import junit.framework.Test;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the attribute rights of an object to be created, decided in its creation context by
 * {@link ModelAccessRights#isAllowedInitial(Person, TLClass, TLObject, TLStructuredTypePart, BoundCommandGroup)},
 * and {@link ModelAccessRights#hasGrant(TLStructuredTypePart, BoundCommandGroup)}.
 */
public class TestInitialAttributeRights extends AbstractModelAccessTest {

	/** Name of the {@link #TASK} attribute only {@link #ROLE_RESPONSIBLE} may write. */
	private static final String NOTE = "note";

	/**
	 * A role granted on the attribute and held in the context allows the operation.
	 */
	public void testGrantedRoleInContext() {
		assertTrue(writeInitial(_responsible, part(TASK, NOTE), _project));
	}

	/**
	 * A role granted on the attribute but not held in the context refuses the operation.
	 */
	public void testGrantedRoleNotHeld() {
		assertFalse(writeInitial(_roleless, part(TASK, NOTE), _project));
	}

	/**
	 * Without an attribute-level grant, the right to create the object covers the initial value.
	 */
	public void testNoAttributeGrant() {
		assertTrue(writeInitial(_roleless, part(TASK, NAME), _project));
		assertTrue(writeInitial(_roleless, part(TASK, NAME), null));
	}

	/**
	 * A grant listing no role refuses the operation in every context.
	 */
	public void testGrantedToNoRole() {
		TLStructuredTypePart secret = part(TASK, SECRET);
		assertFalse(writeInitial(_responsible, secret, _project));
		assertFalse(writeInitial(_responsible, secret, null));
		assertFalse(writeInitial(_responsible, part(PROJECT, SECRET), null));
	}

	/**
	 * A user bypassing the model security may set every attribute.
	 */
	public void testRootBypass() {
		assertTrue(writeInitial(_root, part(TASK, SECRET), _project));
		assertTrue(writeInitial(_root, part(TASK, NOTE), null));
		assertTrue(writeInitial(_root, part(PROJECT, SECRET), null));
	}

	/**
	 * Without a context, the roles on the security root decide.
	 */
	public void testNullContext() {
		assertFalse("The responsible holds the role on the project only.",
			writeInitial(_responsible, part(TASK, NOTE), null));
	}

	/**
	 * A context being built in the current transaction has no roles yet; only a grant listing no
	 * role refuses.
	 */
	public void testUncommittedContext() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject project = DynamicModelService.getFactoryFor(MODULE).createObject(type(PROJECT));
			assertTrue(writeInitial(_roleless, part(TASK, NOTE), project));
			assertFalse(writeInitial(_roleless, part(TASK, SECRET), project));
			tx.rollback();
		}
	}

	/**
	 * Whether an attribute has a grant of its own tells an attribute without restriction from one
	 * granted to no role, both listing no allowed roles.
	 */
	public void testHasGrant() {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		assertTrue(rights.hasGrant(part(TASK, NOTE), SimpleBoundCommandGroup.WRITE));
		assertFalse(rights.hasGrant(part(TASK, NOTE), SimpleBoundCommandGroup.READ));
		assertTrue(rights.hasGrant(part(TASK, SECRET), SimpleBoundCommandGroup.WRITE));
		assertTrue(rights.getAllowedRoles(part(TASK, SECRET), SimpleBoundCommandGroup.WRITE).isEmpty());
		assertFalse(rights.hasGrant(part(TASK, NAME), SimpleBoundCommandGroup.WRITE));
		assertTrue(rights.getAllowedRoles(part(TASK, NAME), SimpleBoundCommandGroup.WRITE).isEmpty());
	}

	private static boolean writeInitial(Person person, TLStructuredTypePart attribute, TLObject context) {
		// The bypass of a super-user is decided for the user of the current interaction.
		becomeUser(person);
		return ModelAccessRights.getInstance().isAllowedInitial(person, (TLClass) attribute.getOwner(), context,
			attribute, SimpleBoundCommandGroup.WRITE);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestInitialAttributeRights.class);
	}
}
