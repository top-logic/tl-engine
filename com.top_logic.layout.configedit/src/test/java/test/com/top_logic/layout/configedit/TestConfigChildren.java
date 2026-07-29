/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigChildren;

/**
 * Tests for {@link ConfigChildren}.
 */
public class TestConfigChildren extends TestCase {

	/**
	 * Configuration holding one list-valued and one single-valued structural property.
	 */
	public interface Container extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Property name for {@link #getSingle()}. */
		String SINGLE = "single";

		/** Property name for {@link #getLabel()}. */
		String LABEL = "label";

		@Name(ITEMS)
		List<Item> getItems();

		@Name(SINGLE)
		Item getSingle();

		/** @see #getSingle() */
		void setSingle(Item value);

		@Name(LABEL)
		String getLabel();
	}

	/**
	 * An element of {@link Container#getItems()}.
	 */
	public interface Item extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	private Container _container;

	private ConfigChildren _list;

	private ConfigChildren _item;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_container = TypedConfiguration.newConfigItem(Container.class);
		_list = ConfigChildren.create(_container, property(Container.ITEMS));
		_item = ConfigChildren.create(_container, property(Container.SINGLE));
	}

	private PropertyDescriptor property(String name) {
		return _container.descriptor().getProperty(name);
	}

	private static Item item(String name) {
		Item result = TypedConfiguration.newConfigItem(Item.class);
		result.setName(name);
		return result;
	}

	/**
	 * Only LIST and ITEM properties are structural.
	 */
	public void testStructuralKinds() {
		assertTrue("A list property is structural", ConfigChildren.isStructural(property(Container.ITEMS)));
		assertTrue("An item property is structural", ConfigChildren.isStructural(property(Container.SINGLE)));
		assertFalse("A string property is not structural", ConfigChildren.isStructural(property(Container.LABEL)));
		assertNull("No accessor is created for a non-structural property",
			ConfigChildren.create(_container, property(Container.LABEL)));
	}

	/**
	 * Adding to a list property appends to the configuration.
	 */
	public void testAddToList() {
		assertTrue("A list always accepts another element", _list.canAdd());
		assertTrue(_list.isList());

		Item first = item("first");
		Item second = item("second");
		assertTrue(_list.add(first));
		assertTrue(_list.add(second));

		assertEquals("Both elements reached the configuration", List.of(first, second), _container.getItems());
		assertEquals(0, _list.indexOf(first));
		assertEquals(1, _list.indexOf(second));
		assertEquals(List.of(first, second), _list.elements());
	}

	/**
	 * Removing from a list property removes from the configuration.
	 */
	public void testRemoveFromList() {
		Item first = item("first");
		Item second = item("second");
		_list.add(first);
		_list.add(second);

		assertTrue(_list.remove(first));
		assertEquals("Only the removed element is gone", List.of(second), _container.getItems());
		assertEquals("The removed element is no longer an element", -1, _list.indexOf(first));
		assertFalse("Removing a non-element fails", _list.remove(first));
	}

	/**
	 * Moving within a list property reorders the configuration.
	 */
	public void testMoveInList() {
		Item first = item("first");
		Item second = item("second");
		Item third = item("third");
		_list.add(first);
		_list.add(second);
		_list.add(third);

		assertTrue(_list.move(second, -1));
		assertEquals(List.of(second, first, third), _container.getItems());

		assertTrue(_list.move(second, 2));
		assertEquals(List.of(first, third, second), _container.getItems());
	}

	/**
	 * A move beyond either end of the list is rejected and changes nothing.
	 */
	public void testMoveOutOfBounds() {
		Item first = item("first");
		Item second = item("second");
		_list.add(first);
		_list.add(second);

		assertFalse("Cannot move the first element up", _list.move(first, -1));
		assertFalse("Cannot move the last element down", _list.move(second, 1));
		assertFalse("Cannot move a non-element", _list.move(item("other"), 1));
		assertEquals("The order is unchanged", List.of(first, second), _container.getItems());
	}

	/**
	 * Replacing keeps the position of the replaced element.
	 */
	public void testReplaceInList() {
		Item first = item("first");
		Item second = item("second");
		Item replacement = item("replacement");
		_list.add(first);
		_list.add(second);

		assertTrue(_list.replace(first, replacement));
		assertEquals(List.of(replacement, second), _container.getItems());
		assertFalse("Replacing a non-element fails", _list.replace(first, replacement));
	}

	/**
	 * A single-valued property holds at most one element.
	 */
	public void testSingleValuedProperty() {
		assertFalse("A single-valued property is not a list", _item.isList());
		assertTrue("An unset property accepts an element", _item.canAdd());
		assertEquals(List.of(), _item.elements());

		Item value = item("only");
		assertTrue(_item.add(value));
		assertSame("The element reached the configuration", value, _container.getSingle());
		assertEquals(List.of(value), _item.elements());
		assertEquals(0, _item.indexOf(value));

		assertFalse("An occupied property accepts no further element", _item.canAdd());
		assertFalse("Adding a second element fails", _item.add(item("other")));
		assertSame("The occupied value is untouched", value, _container.getSingle());

		assertFalse("A single-valued property cannot be reordered", _item.move(value, 1));
	}

	/**
	 * Removing the value of a single-valued property leaves it unset, so that it is not serialized.
	 */
	public void testRemoveSingleValue() {
		Item value = item("only");
		_item.add(value);

		assertTrue(_item.remove(value));
		assertNull("The value is gone", _container.getSingle());
		assertFalse("The property is unset again",
			_container.valueSet(property(Container.SINGLE)));
		assertTrue("The property accepts an element again", _item.canAdd());
	}

	/**
	 * A property whose element type has no subtypes offers exactly that type, and a new element has
	 * it whether or not the type is passed explicitly.
	 */
	public void testNewElement() {
		List<Object> options = _list.allowedTypes().options();
		assertEquals("The declared element type is the only choice", List.of(Item.class), options);

		assertTrue("The declared type is created without an explicit choice",
			_list.newElement(null) instanceof Item);
		assertTrue("The declared type is created for the explicit choice",
			_list.newElement(options.get(0)) instanceof Item);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, used to resolve property options.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigChildren.class, TypeIndex.Module.INSTANCE);
	}
}
