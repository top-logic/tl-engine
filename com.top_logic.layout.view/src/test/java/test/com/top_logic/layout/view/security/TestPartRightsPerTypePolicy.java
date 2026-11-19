/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import junit.framework.Test;

import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests that the {@link ModelAccessPolicy} decides an attribute by the attribute rights of the type
 * of the objects, not by those of the attribute definition.
 *
 * <p>
 * {@code Named} defines {@code name}, {@code Foo} overrides it, {@code Bar} inherits it unchanged.
 * The role {@code NameReader} may read and write every {@code Named} object, the attribute rights
 * of {@code Foo#name} grant reading and writing the name to {@code NameKeeper} instead (see
 * <code>ModelAccessTest.config.xml</code>). The {@link #_responsible} user holds
 * {@code NameReader} on {@link #_foo} and {@link #_bar}, the {@link #_outsider} holds
 * {@code NameKeeper} on both.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPartRightsPerTypePolicy extends AbstractModelAccessTest {

	private static final String NAMED = "Named";

	private static final String FOO = "Foo";

	private static final String BAR = "Bar";

	private TLObject _foo;

	private TLObject _bar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_foo = newObject(qualified(FOO), FOO);
		_bar = newObject(qualified(BAR), BAR);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			for (TLObject named : new TLObject[] { _foo, _bar }) {
				named.tUpdateByName("accountA", _responsible);
				named.tUpdateByName("accountB", _outsider);
			}
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_foo = null;
		_bar = null;
		super.tearDown();
	}

	/**
	 * The name of a {@code Foo} is hidden for a user holding none of the roles of its attribute
	 * rights, the name of a {@code Bar} is decided by the class rights.
	 */
	public void testOnObject() {
		TLStructuredTypePart name = part(NAMED, NAME);

		becomeUser(_responsible);
		assertHidden(onAttribute(SimpleBoundCommandGroup.READ, _foo, name));
		assertHidden(onAttribute(SimpleBoundCommandGroup.WRITE, _foo, name));
		assertExecutable(onAttribute(SimpleBoundCommandGroup.READ, _bar, name));
		assertExecutable(onAttribute(SimpleBoundCommandGroup.WRITE, _bar, name));

		becomeUser(_outsider);
		assertExecutable(onAttribute(SimpleBoundCommandGroup.READ, _foo, name));
		assertExecutable(onAttribute(SimpleBoundCommandGroup.WRITE, _foo, name));
	}

	/**
	 * Without an object, the attribute rights of the given type decide: a column of the name is
	 * hidden for {@code Foo} objects, but offered for {@code Bar} objects.
	 */
	public void testOnType() {
		TLStructuredTypePart name = part(NAMED, NAME);

		becomeUser(_responsible);
		assertHidden(ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.READ, type(FOO), name));
		assertExecutable(ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.READ, type(BAR), name));
		assertExecutable(
			ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.READ, type(BAR), part(BAR, NAME)));

		becomeUser(_outsider);
		assertExecutable(ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.READ, type(FOO), name));
		assertExecutable(
			ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.READ, type(FOO), part(FOO, NAME)));
	}

	private static ExecutableState onAttribute(BoundCommandGroup operation, TLObject object,
			TLStructuredTypePart attribute) {
		return ModelAccessPolicy.onAttribute(operation, object, attribute);
	}

	private static void assertHidden(ExecutableState state) {
		assertTrue("Expected hidden: " + state, state.isHidden());
	}

	private static void assertExecutable(ExecutableState state) {
		assertTrue("Expected executable: " + state, state.isExecutable());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestPartRightsPerTypePolicy.class);
	}

}
