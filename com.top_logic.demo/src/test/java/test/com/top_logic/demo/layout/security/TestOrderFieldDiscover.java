/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.security;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.demo.DemoSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.structured.AdminElementComponent;
import com.top_logic.element.layout.structured.StructuredElementApplyHandler;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.MaskedValues;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.util.TLContextManager;

/**
 * Tests that the field choosing the position of a structure element ("Position vor") offers only
 * the siblings the current user may discover, and masks an undiscoverable current next sibling.
 *
 * <p>
 * Fixture below the root of the demo types: {@code a1}, {@code a2}, {@code a3} in this order. The
 * user is responsible for {@code a1} and {@code a3} and may not discover {@code a2}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestOrderFieldDiscover extends TestCase {

	private static final String TYPE_A = "DemoTypes:A";

	private static final String RESPONSIBLE = "responsible";

	private static final ResKey AT_END = ResKey.legacy("element.structure.edit.atEnd");

	/** Mandatory attributes of the demo types. */
	private static final String[] MANDATORY_BOOLEANS =
		{ "booleanMandatory", "booleanRadioMandatory", "booleanSelectMandatory" };

	private final List<StructuredElement> _created = new ArrayList<>();

	private Person _user;

	private StructuredElement _a1;

	private StructuredElement _a2;

	private StructuredElement _a3;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inSystem(() -> {
			inTransaction(() -> {
				_user = Person.create(kb(), "orderFieldDiscover",
					TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
				StructuredElement root = root();
				_a1 = createA(root, "orderA1");
				_a2 = createA(root, "orderA2");
				_a3 = createA(root, "orderA3");
				_a1.tUpdateByName(RESPONSIBLE, _user);
				_a3.tUpdateByName(RESPONSIBLE, _user);
			});
			return null;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inSystem(() -> {
			inTransaction(() -> {
				for (StructuredElement element : _created) {
					if (element.tValid()) {
						element.tDelete();
					}
				}
				_user.getRepresentativeGroup().tDelete();
				_user.tDelete();
			});
			return null;
		});
		_created.clear();
		super.tearDown();
	}

	/**
	 * Only discoverable siblings are offered, the element itself never.
	 */
	public void testOffersOnlyDiscoverableSiblings() {
		List<?> options = asUser(() -> AdminElementComponent.newOrderField(_a1, false, AT_END).getOptions());
		assertTrue(options.contains(_a3));
		assertFalse(options.contains(_a2));
		assertFalse(options.contains(_a1));
	}

	/**
	 * An undiscoverable current next sibling stays the value, but is shown as placeholder.
	 */
	public void testMasksUndiscoverableNextSibling() {
		asUser(() -> {
			SelectField field = AdminElementComponent.newOrderField(_a1, false, AT_END);
			assertSame(_a2, field.getSingleSelection());
			assertEquals(MaskedValues.placeholder(), SelectFieldUtils.getOptionLabel(field, _a2));
			assertEquals(_a3.getName(), SelectFieldUtils.getOptionLabel(field, _a3));
			return null;
		});
	}

	/**
	 * A freshly built field holds the current position and is not changed.
	 */
	public void testFreshFieldUnchanged() {
		asUser(() -> {
			SelectField field = AdminElementComponent.newOrderField(_a1, false, AT_END);
			assertFalse(field.isChanged());
			assertSame(_a2, field.getSingleSelection());
			return null;
		});
	}

	/**
	 * With only undiscoverable siblings, the field stays editable: no sibling is offered, the
	 * current next sibling is masked, and choosing the end moves the element there.
	 */
	public void testAtEndWithMaskedNextSibling() {
		setResponsible(_a3, null);
		asUser(() -> {
			SelectField field = AdminElementComponent.newOrderField(_a1, false, AT_END);
			assertFalse("The at-end choice stays available.", field.isImmutable());
			assertEquals(List.of(), field.getOptions());
			assertSame(_a2, field.getSingleSelection());
			assertEquals(MaskedValues.placeholder(), SelectFieldUtils.getOptionLabel(field, _a2));

			field.setValue(List.of());
			assertTrue("Choosing the end is a change.", field.isChanged());
			apply(field, _a1);
			return null;
		});
		List<? extends StructuredElement> children = children();
		assertEquals(children.size() - 1, children.indexOf(_a1));
	}

	/**
	 * A user who discovers all siblings moves the element before the chosen one.
	 */
	public void testMoveBeforeChosenSibling() {
		setResponsible(_a2, _user);
		asUser(() -> {
			SelectField field = AdminElementComponent.newOrderField(_a3, false, AT_END);
			assertTrue(field.getOptions().contains(_a1));
			assertTrue(field.getOptions().contains(_a2));
			field.setAsSingleSelection(_a1);
			assertTrue(field.isChanged());
			apply(field, _a3);
			return null;
		});
		List<? extends StructuredElement> children = children();
		assertEquals(children.indexOf(_a1) - 1, children.indexOf(_a3));
	}

	/**
	 * Saving without touching the field keeps the order.
	 */
	public void testUntouchedFieldKeepsOrder() {
		List<? extends StructuredElement> before = children();
		asUser(() -> {
			SelectField field = AdminElementComponent.newOrderField(_a1, false, AT_END);
			apply(field, _a1);
			return null;
		});
		assertEquals(before, children());
	}

	/**
	 * Stores the given order field of the given element like the apply command of the structure
	 * editor.
	 */
	private static void apply(SelectField field, StructuredElement element) {
		ApplyHandler handler;
		try {
			handler = new ApplyHandler(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				AbstractCommandHandler.createConfig(ApplyHandler.class, "testOrderApply"));
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		FormContext form = new FormContext("testOrder", ResPrefix.forTest("TestOrderFieldDiscover"), List.of(field));
		inTransaction(() -> handler.store(form, element));
	}

	/**
	 * Exposes the storage of {@link StructuredElementApplyHandler}.
	 */
	public static class ApplyHandler extends StructuredElementApplyHandler {

		public ApplyHandler(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		void store(FormContext form, StructuredElement element) {
			storeChanges(null, form, element);
		}

	}

	private void setResponsible(StructuredElement element, Person person) {
		inSystem(() -> {
			inTransaction(() -> element.tUpdateByName(RESPONSIBLE, person));
			return null;
		});
	}

	private static List<? extends StructuredElement> children() {
		return inSystem(() -> List.copyOf(root().getChildren()));
	}

	private StructuredElement createA(StructuredElement root, String name) {
		StructuredElement result = root.createChild(name, (TLClass) TLModelUtil.findType(TYPE_A));
		for (String attribute : MANDATORY_BOOLEANS) {
			result.tUpdateByName(attribute, Boolean.TRUE);
		}
		_created.add(result);
		return result;
	}

	private static StructuredElement root() {
		return (StructuredElement) ((TLClass) TLModelUtil.findType("DemoTypes:Root")).getModule().getSingleton("ROOT");
	}

	private <T> T asUser(Supplier<T> job) {
		return inSystem(() -> TLContextManager.<T, RuntimeException, RuntimeException> inPersonContext(_user, job::get));
	}

	private static <T> T inSystem(Supplier<T> job) {
		return ThreadContext.inSystemContext(TestOrderFieldDiscover.class, job::get);
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				modification.run();
				tx.commit();
			}
		});
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		return DemoSetup.createDemoSetup(TestOrderFieldDiscover.class);
	}

}
