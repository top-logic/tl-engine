/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.constraint;

import java.util.Arrays;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.form.constraint.UniqueConstraint;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link UniqueConstraint}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestUniqueConstraint extends TLModelTest {

	private TLClass _typeA;

	private TLStructuredTypePart _name;

	private TLStructuredTypePart _str;

	private TLStructuredTypePart _other;

	@Override
	protected TLModel setUpModel() {
		return ModelService.getApplicationModel();
	}

	@Override
	protected TLFactory setUpFactory() {
		return ModelService.getInstance().getFactory();
	}

	@Override
	protected void tearDownModel() {
		// Application model is not modified.
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_typeA = (TLClass) TLModelUtil.findType(getModel(), "TestXMLInstanceImporter:A");
		_name = _typeA.getPart("name");
		_str = _typeA.getPart("str");
		_other = _typeA.getPart("other");
	}

	/**
	 * Tests uniqueness of a single attribute, both for uncommitted objects of the same transaction
	 * and against committed state.
	 */
	public void testGlobalUniqueness() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UniqueConstraint check = newConstraint();

		Transaction tx = kb.beginTransaction();
		try {
			TLObject a = newA();
			a.tUpdate(_name, "TUC-first");
			assertNull("Single object has no conflict.", check.check(a, _name));

			TLObject b = newA();
			b.tUpdate(_name, "TUC-first");
			assertNotNull("Duplicate within the same transaction is detected.", check.check(a, _name));
			assertNotNull(check.check(b, _name));

			b.tUpdate(_name, "TUC-second");
			assertNull("Distinct values do not conflict.", check.check(a, _name));

			tx.commit();
		} finally {
			tx.rollback();
		}

		Transaction tx2 = kb.beginTransaction();
		try {
			TLObject c = newA();
			c.tUpdate(_name, "TUC-first");
			assertNotNull("Duplicate of a committed value is detected.", check.check(c, _name));

			c.tUpdate(_name, "TUC-third");
			assertNull(check.check(c, _name));
		} finally {
			tx2.rollback();
		}
	}

	/**
	 * Tests that a committed object does not conflict with itself.
	 */
	public void testSelfExclusion() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UniqueConstraint check = newConstraint();

		Transaction tx = kb.beginTransaction();
		TLObject a = newA();
		a.tUpdate(_name, "TUC-self");
		tx.commit();

		assertNull("An object does not conflict with itself.", check.check(a, _name));

		Transaction tx2 = kb.beginTransaction();
		try {
			a.tUpdate(_str, "TUC-self-unrelated-change");
			assertNull("A modified object does not conflict with its unmodified state.", check.check(a, _name));
		} finally {
			tx2.rollback();
		}
	}

	/**
	 * Tests uniqueness restricted to the scope of an additional attribute.
	 */
	public void testScopedUniqueness() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UniqueConstraint scopedCheck = newConstraint("str");

		Transaction tx = kb.beginTransaction();
		try {
			TLObject a = newA();
			a.tUpdate(_name, "TUC-scoped");
			a.tUpdate(_str, "TUC-scope-1");

			TLObject b = newA();
			b.tUpdate(_name, "TUC-scoped");
			b.tUpdate(_str, "TUC-scope-2");
			assertNull("Same value in different scopes does not conflict.", scopedCheck.check(a, _name));

			b.tUpdate(_str, "TUC-scope-1");
			assertNotNull("Same value in the same scope conflicts.", scopedCheck.check(a, _name));
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Tests that an unset value participates in the constraint like any other value.
	 */
	public void testNullCountsAsValue() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UniqueConstraint scopedCheck = newConstraint("str");

		Transaction tx = kb.beginTransaction();
		try {
			TLObject a = newA();
			a.tUpdate(_str, "TUC-null-scope");

			TLObject b = newA();
			b.tUpdate(_str, "TUC-null-scope");
			assertNotNull("Two objects with unset value in the same scope conflict.", scopedCheck.check(a, _name));

			b.tUpdate(_name, "TUC-non-null");
			assertNull("Unset value does not conflict with a set value.", scopedCheck.check(a, _name));
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Tests uniqueness scoped by a to-one reference.
	 */
	public void testReferenceScope() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UniqueConstraint scopedCheck = newConstraint("other");

		Transaction tx = kb.beginTransaction();
		TLObject scope1 = newA();
		scope1.tUpdate(_name, "TUC-ref-scope-1");
		TLObject scope2 = newA();
		scope2.tUpdate(_name, "TUC-ref-scope-2");
		tx.commit();

		Transaction tx2 = kb.beginTransaction();
		try {
			TLObject a = newA();
			a.tUpdate(_name, "TUC-ref-scoped");
			a.tUpdate(_other, scope1);

			TLObject b = newA();
			b.tUpdate(_name, "TUC-ref-scoped");
			b.tUpdate(_other, scope2);
			assertNull("Same value in different reference scopes does not conflict.", scopedCheck.check(a, _name));

			b.tUpdate(_other, scope1);
			assertNotNull("Same value in the same reference scope conflicts.", scopedCheck.check(a, _name));
		} finally {
			tx2.rollback();
		}
	}

	private TLObject newA() {
		return getFactory().createObject(_typeA, null, null);
	}

	private UniqueConstraint newConstraint(String... additionalAttributes) {
		UniqueConstraint.Config<?> config = TypedConfiguration.newConfigItem(UniqueConstraint.Config.class);
		config.update(config.descriptor().getProperty(UniqueConstraint.Config.ADDITIONAL_ATTRIBUTES),
			Arrays.asList(additionalAttributes));
		return (UniqueConstraint) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suiteTransient(
			KBSetup.getSingleKBTest(
				ServiceTestSetup.createSetup(TestUniqueConstraint.class, ModelService.Module.INSTANCE)));
	}

}
