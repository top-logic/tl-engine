/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.field;

import java.util.Date;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;

/**
 * Tests looking up the control that edits a value of a given type.
 */
public class TestFieldControlRegistry extends TestCase {

	private FieldControlRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = FieldControlRegistry.getInstance();
	}

	/**
	 * The types the platform edits itself have a control.
	 */
	public void testPlatformTypes() {
		assertNotNull("A text has a control", _registry.lookup(String.class));
		assertNotNull("A truth value has a control", _registry.lookup(Boolean.class));
		assertNotNull("A number has a control", _registry.lookup(Integer.class));
		assertNotNull("A date has a control", _registry.lookup(Date.class));
		assertNotNull("An internationalized text has a control", _registry.lookup(ResKey.class));
	}

	/**
	 * A primitive type is edited by the control of its wrapper, so a property declared {@code int} and
	 * one declared {@link Integer} are edited the same way.
	 */
	public void testPrimitivesUseTheirWrapper() {
		assertSame(_registry.lookup(Integer.class), _registry.lookup(int.class));
		assertSame(_registry.lookup(Boolean.class), _registry.lookup(boolean.class));
		assertSame(_registry.lookup(Double.class), _registry.lookup(double.class));
	}

	/**
	 * A control registered for a type serves its subtypes, so every number kind is covered by the one
	 * registration for {@link Number}.
	 */
	public void testSubtypesInheritTheControl() {
		ReactFieldControlProvider number = _registry.lookup(Number.class);
		assertNotNull(number);
		assertSame("A long is a number", number, _registry.lookup(Long.class));
		assertSame("A double is a number", number, _registry.lookup(Double.class));
	}

	/**
	 * A type nothing is registered for has no control, so the caller can fall back.
	 */
	public void testUnknownType() {
		assertNull("Nothing edits an arbitrary object", _registry.lookup(Object.class));
		assertNull("Nothing is looked up without a type", _registry.lookup(null));
	}

	/**
	 * A registration replaces the control for its type.
	 */
	public void testRegistrationWins() {
		FieldControlRegistry registry = new FieldControlRegistry() {
			// A separate registry, so the replacement does not affect the shared one.
		};
		ReactFieldControlProvider replacement = (context, field, model) -> null;

		registry.register(String.class, replacement);

		assertSame(replacement, registry.lookup(String.class));
		assertSame("The shared registry is untouched",
			FieldControlRegistry.getInstance().lookup(String.class),
			FieldControlRegistry.getInstance().lookup(String.class));
	}

	/**
	 * The type decides the control; the remaining specification is display detail.
	 */
	public void testSpecCarriesDisplayHints() {
		FieldSpec field = FieldSpec.of(String.class, "Name")
			.setMandatory(true)
			.setMultilineRows(5);

		assertEquals(String.class, field.getValueType());
		assertEquals("Name", field.getLabel());
		assertTrue(field.isMandatory());
		assertEquals(5, field.getMultilineRows());
		assertTrue("A field is editable unless stated otherwise", field.isEditable());
		assertNull("A value is entered freely unless options are stated", field.getOptions());
	}
}
