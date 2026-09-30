/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.field;

import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests looking up the control that edits a value of a given type.
 */
public class TestFieldControlRegistry extends TestCase {

	/** An icon a search box carries. */
	private static final String SEARCH_ICON = "css:fa-solid fa-magnifying-glass";

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

	/**
	 * A field holding one value is edited by the control of its type, directly.
	 */
	public void testSingleValueUsesTheElementControl() {
		FieldSpec field = FieldSpec.of(String.class, "Name");

		assertInstanceof(ReactTextInputControl.class, createControl(field, FieldControlRegistry.TEXT));
	}

	/**
	 * A field holding several values whose control edits one value at a time is displayed as a list
	 * of such controls.
	 */
	public void testMultipleValuesBecomeAList() {
		FieldSpec field = FieldSpec.of(String.class, "Names").setMultiple(true);

		assertInstanceof(ReactValueListControl.class, createControl(field, FieldControlRegistry.TEXT));
	}

	/**
	 * A control that edits the whole collection itself is handed the field as it stands.
	 */
	public void testCollectionEditorKeepsTheField() {
		FieldSpec field = FieldSpec.of(String.class, "Names").setMultiple(true);
		CollectionEditor provider = new CollectionEditor();

		ReactControl control = createControl(field, provider);

		assertSame(provider.getLastControl(), control);
		assertTrue("The provider sees the field as holding several values", provider.getLastField().isMultiple());
	}

	/**
	 * The element of a multi-valued field is the same field, holding one value.
	 */
	public void testElementSpec() {
		FieldSpec field = FieldSpec.of(String.class, "Names")
			.setMultiple(true)
			.setMultilineRows(4)
			.setTooltip("What they are called")
			.setIcon(SEARCH_ICON)
			.setClearable(true)
			.setDebounce(Long.valueOf(250));

		FieldSpec element = field.elementSpec();

		assertFalse("An element holds one value", element.isMultiple());
		assertEquals(String.class, element.getValueType());
		assertEquals("Names", element.getLabel());
		assertEquals(4, element.getMultilineRows());
		assertEquals("What they are called", element.getTooltip());
		assertEquals("An element is displayed like the field", SEARCH_ICON, element.getIcon());
		assertTrue("An element is displayed like the field", element.isClearable());
		assertEquals("An element reports at the pace of the field", Long.valueOf(250),
			element.getDebounce());
		assertTrue("The field itself is untouched", field.isMultiple());
	}

	/**
	 * A description that states none of the search-field properties leaves the control with the
	 * plain input and the delay it has by default.
	 */
	public void testAPlainFieldAsksForNoSearchAffordances() {
		FieldSpec field = FieldSpec.of(String.class, "Name");

		assertNull("A field carries no icon unless it states one", field.getIcon());
		assertFalse("A field is emptied by deleting its text unless it states otherwise",
			field.isClearable());
		assertNull("A field reports at the pace of its control unless it states a span",
			field.getDebounce());
	}

	private static ReactControl createControl(FieldSpec field, ReactFieldControlProvider provider) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return FieldControlRegistry.getInstance().createControl(context, field,
			new AbstractFieldModel(List.of("A")), provider);
	}

	private static void assertInstanceof(Class<?> expected, Object actual) {
		assertTrue("Expected a " + expected.getSimpleName() + ", got: " + actual,
			expected.isInstance(actual));
	}

	/**
	 * A provider whose control edits the whole collection of values, recording what it was asked
	 * for.
	 */
	private static final class CollectionEditor implements ReactFieldControlProvider {

		private FieldSpec _lastField;

		private ReactControl _lastControl;

		@Override
		public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
			_lastField = field;
			_lastControl = new ReactTextInputControl(context, model);
			return _lastControl;
		}

		@Override
		public boolean editsCollections() {
			return true;
		}

		FieldSpec getLastField() {
			return _lastField;
		}

		ReactControl getLastControl() {
			return _lastControl;
		}
	}

	/**
	 * The test suite, started with the resource bundles a created field needs for its messages.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFieldControlRegistry.class, ResourcesModule.Module.INSTANCE));
	}
}
