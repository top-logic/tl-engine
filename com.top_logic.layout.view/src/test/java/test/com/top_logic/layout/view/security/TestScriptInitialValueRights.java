/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import static com.top_logic.model.search.expr.I18NConstants.*;

import java.util.List;
import java.util.function.Consumer;

import junit.framework.Test;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Tests the write check of the TL-Script <code>set()</code>,
 * {@link Update#checkWritePermission(TLObject, TLStructuredTypePart)}, on an object created in the
 * current transaction: it is decided by the right to set an initial value, see
 * {@link ModelAccessRights#isAllowedInitial(Person, TLObject, TLStructuredTypePart, BoundCommandGroup)}.
 */
public class TestScriptInitialValueRights extends AbstractModelAccessTest {

	/** Name of the {@link #STEP} attribute only {@link #ROLE_RESPONSIBLE} may write. */
	private static final String NOTE_ATTRIBUTE = "note";

	/**
	 * A user without the role the attribute grant requires on the access parent may not set the
	 * attribute of a new object.
	 */
	public void testRefusedWithoutAttributeGrant() {
		inNewStep(step -> {
			becomeUser(_roleless);
			assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, () -> set(step, NOTE_ATTRIBUTE, "x"));
			assertNull(step.tValueByName(NOTE_ATTRIBUTE));
		});
	}

	/**
	 * A user holding the role the attribute grant requires on the access parent sets the attribute
	 * of a new object.
	 */
	public void testAllowedWithAttributeGrant() {
		inNewStep(step -> {
			becomeUser(_responsible);
			set(step, NOTE_ATTRIBUTE, "x");
			assertEquals("x", step.tValueByName(NOTE_ATTRIBUTE));
		});
	}

	/**
	 * An attribute without a grant of its own is covered by the right to create the object.
	 */
	public void testAllowedWithoutAttributeGrant() {
		inNewStep(step -> {
			becomeUser(_roleless);
			set(step, NAME, "x");
			assertEquals("x", step.tValueByName(NAME));
		});
	}

	/**
	 * An attribute granted to no role is refused even for a user holding every role on the access
	 * parent.
	 */
	public void testRefusedGrantedToNoRole() {
		inNewStep(step -> {
			becomeUser(_responsible);
			assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, () -> set(step, SECRET, "x"));
		});
	}

	/**
	 * Setting an attribute of a new object created by the script itself is checked as well.
	 */
	public void testObjectCreatedByScript() throws ParseException {
		becomeUser(_roleless);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject step = (TLObject) execute(search("new(`" + qualified(STEP) + "`)"));
			// Puts the object in its container, which the user may not write.
			_project.tUpdateByName(STEPS, List.of(step));
			assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, () -> set(step, NOTE_ATTRIBUTE, "x"));
			tx.rollback();
		}
	}

	/**
	 * A committed object is decided by the rights to write the attribute of the object.
	 */
	public void testCommittedObject() {
		TLObject step = newPart(_project, STEPS, STEP);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			becomeUser(_roleless);
			assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, () -> set(step, NOTE_ATTRIBUTE, "x"));
			assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, () -> set(step, NAME, "x"));
			becomeUser(_responsible);
			set(step, NOTE_ATTRIBUTE, "x");
			assertEquals("x", step.tValueByName(NOTE_ATTRIBUTE));
			tx.rollback();
		}
	}

	/**
	 * Creates a step of the project in a transaction that is rolled back after the given test.
	 */
	private void inNewStep(Consumer<TLObject> test) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_project.tUpdateByName(STEPS, List.of(step));
			test.accept(step);
			tx.rollback();
		}
	}

	private static void set(TLObject self, String attribute, Object value) {
		try {
			execute(search("self -> value -> $self.set(`" + qualified(STEP) + "#" + attribute + "`, $value)"),
				self, value);
		} catch (ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestScriptInitialValueRights.class);
	}
}
