/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.List;

import junit.framework.Test;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the attribute rights of an object to be created, decided by
 * {@link ModelAccessRights#isAllowedInitial(Person, TLObject, TLStructuredTypePart, BoundCommandGroup)},
 * and {@link ModelAccessRights#hasGrant(TLStructuredTypePart, BoundCommandGroup)}.
 */
public class TestInitialAttributeRights extends AbstractModelAccessTest {

	/** Name of the {@link #STEP} and {@link #TASK} attribute only {@link #ROLE_RESPONSIBLE} may write. */
	private static final String NOTE = "note";

	/**
	 * A step decides by its container, a task by its own roles.
	 */
	public void testFixture() {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		assertNotNull(rights.getAccessParent(type(STEP)));
		assertNull(rights.getAccessParent(type(TASK)));
	}

	/**
	 * A role granted on the attribute and held on the access parent allows the operation.
	 */
	public void testGrantedRoleOnAccessParent() {
		assertTrue(writeInitial(_responsible, NOTE, step(_project)));
	}

	/**
	 * A role granted on the attribute but not held on the access parent refuses the operation.
	 */
	public void testGrantedRoleNotHeld() {
		assertFalse(writeInitial(_roleless, NOTE, step(_project)));
	}

	/**
	 * Without an attribute-level grant, the right to create the object covers the initial value.
	 */
	public void testNoAttributeGrant() {
		assertTrue(writeInitial(_roleless, NAME, step(_project)));
		assertTrue(writeInitial(_roleless, NAME, step(null)));
		assertTrue(writeInitial(_roleless, NAME, task(_project)));
	}

	/**
	 * Without a user, an attribute without a grant of its own is not restricted, one with a grant is
	 * refused.
	 */
	public void testWithoutUser() {
		try {
			assertTrue(writeInitial(null, NAME, step(_project)));
			assertTrue(writeInitial(null, NAME, step(null)));
			assertFalse(writeInitial(null, NOTE, step(_project)));
		} finally {
			// Cleaning up the fixture needs a user.
			becomeUser(_root);
		}
	}

	/**
	 * For a restricted user, an attribute without a grant of its own is not restricted, one with a
	 * grant is refused, even where the user holds the granted role.
	 */
	public void testRestrictedUser() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_responsible.setRestrictedUser(Boolean.TRUE);
			tx.commit();
		}
		assertTrue(writeInitial(_responsible, NAME, step(_project)));
		assertFalse(writeInitial(_responsible, NOTE, step(_project)));
	}

	/**
	 * A grant listing no role refuses the operation, wherever the object is created.
	 */
	public void testGrantedToNoRole() {
		assertFalse(writeInitial(_responsible, SECRET, step(_project)));
		assertFalse(writeInitial(_responsible, SECRET, step(null)));
		assertFalse(writeInitial(_responsible, SECRET, task(_project)));
		assertFalse(writeInitial(_responsible, SECRET, project()));
	}

	/**
	 * A user bypassing the model security may set every attribute.
	 */
	public void testRootBypass() {
		assertTrue(writeInitial(_root, SECRET, step(_project)));
		assertTrue(writeInitial(_root, NOTE, step(null)));
		assertTrue(writeInitial(_root, SECRET, project()));
	}

	/**
	 * An object without access parent is not accessible until it is put into a container, so its
	 * attributes are not restricted.
	 */
	public void testFreeStanding() {
		assertTrue(writeInitial(_roleless, NOTE, step(null)));
	}

	/**
	 * A draft in a draft in a committed object is decided by the roles on the committed object.
	 */
	public void testNestedDrafts() {
		TLObject inner = step(step(_project));
		assertTrue(writeInitial(_responsible, NOTE, inner));
		assertFalse(writeInitial(_roleless, NOTE, inner));
	}

	/**
	 * The attribute grants of a type deciding by its own roles do not restrict an object to be
	 * created, since the roles it will hold are computed only once it exists.
	 */
	public void testSelfDeciding() {
		assertTrue(writeInitial(_roleless, NOTE, task(_project)));
	}

	/**
	 * A draft whose chain of access parents reaches an object to be created that decides by its own
	 * roles is not restricted: the roles deciding are unknown.
	 */
	public void testInSelfDecidingDraft() {
		assertTrue(writeInitial(_roleless, NOTE, step(project())));
	}

	/**
	 * A container being built in the current transaction holds no roles yet, the chain continues to
	 * its own container.
	 */
	public void testUncommittedContainer() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_project.tUpdateByName(STEPS, List.of(step));
			assertTrue(writeInitial(_responsible, NOTE, step(step)));
			assertFalse(writeInitial(_roleless, NOTE, step(step)));
			assertFalse(writeInitial(_roleless, NOTE, step));
			tx.rollback();
		}
	}

	/**
	 * A form object editing a container stands for the edited container.
	 */
	public void testEditedContainer() {
		TLObject draft = step(new TLObjectOverlay(_project));
		assertTrue(writeInitial(_responsible, NOTE, draft));
		assertFalse(writeInitial(_roleless, NOTE, draft));
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

	private static TLObject step(TLObject container) {
		return TransientObjectFactory.INSTANCE.createObject(type(STEP), container);
	}

	private static TLObject task(TLObject container) {
		return TransientObjectFactory.INSTANCE.createObject(type(TASK), container);
	}

	private static TLObject project() {
		return TransientObjectFactory.INSTANCE.createObject(type(PROJECT));
	}

	private static boolean writeInitial(Person person, String attribute, TLObject draft) {
		// The bypass of a super-user is decided for the user of the current interaction.
		becomeUser(person);
		TLStructuredTypePart part = draft.tType().getPartOrFail(attribute);
		return ModelAccessRights.getInstance().isAllowedInitial(person, draft, part, SimpleBoundCommandGroup.WRITE);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestInitialAttributeRights.class);
	}
}
