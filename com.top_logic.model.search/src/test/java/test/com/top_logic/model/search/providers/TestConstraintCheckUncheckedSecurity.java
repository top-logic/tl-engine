/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.providers.ConstraintCheckByExpression;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test that the commit check of the {@link ModelService} evaluates a
 * {@link ConstraintCheckByExpression} independently of the read rights of the committing user.
 *
 * <p>
 * The fixture uses the class {@code ConstrainedRecord} of the model {@code TestTLScriptSecurity}:
 * the constraint on its {@code name} rejects an object whose derived reference {@code derivedRef}
 * (a copy of the stored reference {@code stored}) is empty. The referenced object is an
 * {@code Employee}, which a non-administrative user may not read.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConstraintCheckUncheckedSecurity extends AbstractSearchExpressionTest {

	private static final String MODULE = "TestTLScriptSecurity";

	private static final String NAME = "name";

	private static final String STORED = "stored";

	private static final String DERIVED_REF = "derivedRef";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/**
	 * Non-administrative user without any role, in particular without the right to read an
	 * employee.
	 */
	private Person _user;

	/** The employee referenced by {@link #_t}. */
	private TLObject _a;

	/** The constrained object. */
	private TLObject _t;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("constraintCheckUser");

			_a = newObject("Employee");
			_a.tUpdateByName("account", _user);

			_t = newObject("ConstrainedRecord");
			_t.tUpdateByName(NAME, "t");
			_t.tUpdateByName(STORED, _a);

			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType(MODULE + ":" + className);
		return DynamicModelService.getFactoryFor(MODULE).createObject(type);
	}

	/**
	 * In the system context of the test, the constraint sees the derived reference.
	 */
	public void testModifyInSystemContext() {
		rename("t2");

		assertEquals("t2", _t.tValueByName(NAME));
		assertSame(_a, _t.tValueByName(DERIVED_REF));
	}

	/**
	 * A user who may not read the referenced employee can commit a change: the commit check sees
	 * the derived reference. The value of a derived attribute does not depend on the reader (R12 of
	 * the Discover specification), only a secured evaluation reading it is filtered (R4).
	 */
	public void testModifyAsUserWithoutReadRight() throws ParseException {
		TLContextManager.<Void, ParseException, RuntimeException> inPersonContext(_user, () -> {
			assertSame("The derived reference does not depend on the reader.", _a,
				_t.tValueByName(DERIVED_REF));
			assertNull("A secured script of the user does not see the derived reference.",
				eval("t -> $t.get(`" + MODULE + ":ConstrainedRecord#" + DERIVED_REF + "`)", _t));

			rename("t2");

			assertSame("The derived reference still does not depend on the reader.", _a,
				_t.tValueByName(DERIVED_REF));
			return null;
		});

		assertEquals("t2", _t.tValueByName(NAME));
		assertSame(_a, _t.tValueByName(DERIVED_REF));
	}

	/**
	 * A violated constraint rejects the commit in the system context.
	 */
	public void testViolationInSystemContext() {
		assertRejected();
	}

	/**
	 * A violated constraint rejects the commit of a user without read rights as well.
	 */
	public void testViolationAsUserWithoutReadRight() {
		TLContextManager.inPersonContext(_user, this::assertRejected);
	}

	private void rename(String newName) {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_t.tUpdateByName(NAME, newName);
			tx.commit();
		}
	}

	private void assertRejected() {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_t.tUpdateByName(STORED, null);
			tx.commit();
			fail("The constraint on an empty derived reference must reject the commit.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.util.model.I18NConstants.CONSTRAINTS_VIOLATED, ex.getErrorKey());
		}
		assertSame("The rejected change must not be applied.", _a, _t.tValueByName(STORED));
	}

	public static Test suite() {
		return suite(TestConstraintCheckUncheckedSecurity.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
