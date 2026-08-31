/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.FieldCollectionValue;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;

/**
 * Tests for {@link FieldCollectionValue} - a collection of configurations held by a form field
 * rather than by a property of a surrounding configuration.
 */
public class TestFieldCollectionValue extends TestCase {

	/** An element of the edited collection. */
	public interface Item extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	private FieldModel _field;

	private FieldCollectionValue _value;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_field = new AbstractFieldModel(new ArrayList<>());
		_value = new FieldCollectionValue(_field, Item.class, "Items");
	}

	private Item item(String name) {
		Item result = TypedConfiguration.newConfigItem(Item.class);
		result.setName(name);
		return result;
	}

	private List<String> names() {
		List<String> result = new ArrayList<>();
		for (ConfigurationItem each : _value.elements()) {
			result.add(((Item) each).getName());
		}
		return result;
	}

	/** A field holding no value at all is an empty collection, not a failure. */
	public void testAnUnsetFieldIsAnEmptyCollection() {
		_field.setValue(null);

		assertEquals(0, _value.elements().size());
	}

	/** Adding puts the element into the field's value. */
	public void testAddingReachesTheField() {
		_value.add(item("first"));

		assertEquals(Arrays.asList("first"), names());
		assertEquals("The field must carry the new list.", 1, ((List<?>) _field.getValue()).size());
	}

	/**
	 * Every change hands the field a new list rather than mutating the one it holds.
	 *
	 * <p>
	 * A field notices a change by comparing what it is given with what it has. Mutating its own
	 * list in place would leave the two the same object, so the surrounding form would never learn
	 * that anything happened - no dirty state, and nothing to save.
	 * </p>
	 */
	public void testTheFieldIsGivenANewListEachTime() {
		Object before = _field.getValue();

		_value.add(item("first"));

		assertNotSame("An in-place change would be invisible to the field.", before, _field.getValue());
	}

	/** What elements() hands out is a copy: changing it alone changes nothing. */
	public void testElementsIsDetached() {
		_value.add(item("first"));

		_value.elements().add(item("smuggled"));

		assertEquals("Only the collection's own operations may change it.",
			Arrays.asList("first"), names());
	}

	/** Removing takes exactly the element at the given position. */
	public void testRemoving() {
		_value.add(item("first"));
		_value.add(item("second"));

		_value.remove(0);

		assertEquals(Arrays.asList("second"), names());
	}

	/** Reordering is offered and moves the element. */
	public void testReordering() {
		_value.add(item("first"));
		_value.add(item("second"));

		assertTrue("A list has an order of its own to rearrange.", _value.isReorderable());

		_value.move(0, 1);

		assertEquals(Arrays.asList("second", "first"), names());
	}

	/** A move that would leave the collection does nothing. */
	public void testAMoveOutOfBoundsIsIgnored() {
		_value.add(item("only"));

		_value.move(0, -1);

		assertEquals(Arrays.asList("only"), names());
	}

	/** Replacing puts the given element in the place of the one at the position. */
	public void testReplacing() {
		_value.add(item("first"));

		_value.replace(0, item("other"));

		assertEquals(Arrays.asList("other"), names());
	}

	/** The position of an element, and -1 for one that is not in the collection. */
	public void testIndexOf() {
		Item first = item("first");
		_value.add(first);

		assertEquals(0, _value.indexOf(first));
		assertEquals(-1, _value.indexOf(item("stranger")));
	}

	/**
	 * The collection is not keyed: a model attribute carries no key annotation, so nothing indexes
	 * its elements and every new entry joins straight away.
	 */
	public void testItIsNotKeyed() {
		assertFalse(_value.isKeyed());
		assertNull(_value.keyProperty(item("first")));
		assertFalse("Nothing claims a key here.", _value.hasEntryWithKey("first"));
	}

	/** A new element is of the element type the collection was given. */
	public void testNewElement() {
		ConfigurationItem created = _value.newElement();

		assertTrue(created instanceof Item);
		assertEquals("A new element is not in the collection until it is added.", 0, _value.elements().size());
	}

	/** The label is what the collection was told it is called. */
	public void testLabel() {
		assertEquals("Items", _value.label());
	}

	/** The collection declares no title of its own - there is no property to carry one. */
	public void testNoDeclaredTitle() {
		assertNull(_value.titleProperty(item("first")));
	}

	/** Suite requiring {@link TypeIndex} for {@link TypedConfiguration}. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFieldCollectionValue.class, TypeIndex.Module.INSTANCE));
	}
}
