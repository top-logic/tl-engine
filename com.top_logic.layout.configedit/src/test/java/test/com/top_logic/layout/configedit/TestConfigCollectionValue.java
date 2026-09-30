/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigCollectionValue;

/**
 * Tests for {@link ConfigCollectionValue}, the one place that knows how a LIST, an ARRAY and a MAP
 * property differ.
 *
 * <p>
 * Every test here runs the same operations against all three shapes where that is meaningful, since
 * treating the three alike is the class's whole reason to exist. Nothing here needs a rendering
 * context - which is what the extraction from {@code ConfigListEditorControl} bought.
 * </p>
 */
public class TestConfigCollectionValue extends TestCase {

	/** An element of the edited collections. */
	public interface Item extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/** The three shapes one editor treats alike, plus an unkeyed list for contrast. */
	public interface ShapesConfig extends ConfigurationItem {

		/** Property name for {@link #getKeyedList()}. */
		String KEYED_LIST = "keyedList";

		/** Property name for {@link #getPlainList()}. */
		String PLAIN_LIST = "plainList";

		/** Property name for {@link #getArray()}. */
		String ARRAY = "array";

		/** Property name for {@link #getIndex()}. */
		String INDEX = "index";

		@Name(KEYED_LIST)
		@Key(Item.NAME)
		List<Item> getKeyedList();

		@Name(PLAIN_LIST)
		List<Item> getPlainList();

		@Name(ARRAY)
		Item[] getArray();

		@Name(INDEX)
		@Key(Item.NAME)
		Map<String, Item> getIndex();
	}

	/** Common instance type of the polymorphic elements. */
	public interface Handler {
		// Marker interface.
	}

	/** Base configuration of a polymorphic element, declaring the key once. */
	public interface HandlerConfig extends PolymorphicConfiguration<Handler> {

		/** Property name for {@link #getEntryKey()}. */
		String ENTRY_KEY = "entryKey";

		@Name(ENTRY_KEY)
		String getEntryKey();

		/** @see #getEntryKey() */
		void setEntryKey(String value);
	}

	/** A concrete element type, inheriting the key property unchanged. */
	public interface HandlerAConfig extends HandlerConfig {
		// No properties of its own required.
	}

	/** Implementation selected through {@link HandlerAConfig}. */
	public static class HandlerA implements Handler {
		/** Creates a {@link HandlerA} from configuration. */
		public HandlerA(com.top_logic.basic.config.InstantiationContext context, HandlerAConfig config) {
			// No state required for the test.
		}
	}

	/** A keyed collection whose elements are genuine subtypes of its declared element type. */
	public interface PolymorphicConfig extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(ITEMS)
		@Key(HandlerConfig.ENTRY_KEY)
		List<HandlerConfig> getItems();
	}

	private ShapesConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(ShapesConfig.class);
	}

	private ConfigCollectionValue value(String propertyName) {
		return new ConfigCollectionValue(_config, _config.descriptor().getProperty(propertyName));
	}

	private static Item item(String name) {
		Item item = TypedConfiguration.newConfigItem(Item.class);
		item.setName(name);
		return item;
	}

	private static List<String> names(List<ConfigurationItem> elements) {
		List<String> result = new ArrayList<>();
		for (ConfigurationItem element : elements) {
			result.add(((Item) element).getName());
		}
		return result;
	}

	/** The names of the elements as the value itself reports them. */
	private List<String> namesOf(String propertyName) {
		return names(value(propertyName).elements());
	}

	/** Every shape starts out empty rather than reporting a null the caller has to guard. */
	public void testAnUnsetPropertyHasNoElements() {
		assertEquals(Arrays.asList(), namesOf(ShapesConfig.ARRAY));
		assertEquals(Arrays.asList(), namesOf(ShapesConfig.INDEX));
		assertEquals(Arrays.asList(), namesOf(ShapesConfig.PLAIN_LIST));
	}

	/** Adding reaches the configuration in every shape, not only the one held as a live list. */
	public void testAddReachesTheConfiguration() {
		value(ShapesConfig.PLAIN_LIST).add(item("a"));
		assertEquals(1, _config.getPlainList().size());

		value(ShapesConfig.ARRAY).add(item("a"));
		assertEquals(1, _config.getArray().length);

		value(ShapesConfig.INDEX).add(item("a"));
		assertEquals(1, _config.getIndex().size());
		assertTrue("A map is filed under the entry's own key.", _config.getIndex().containsKey("a"));
	}

	/** Removing by index works the same way in every shape. */
	public void testRemoveByIndex() {
		for (String property : new String[] { ShapesConfig.PLAIN_LIST, ShapesConfig.ARRAY, ShapesConfig.INDEX }) {
			ConfigCollectionValue value = value(property);
			value.add(item("a"));
			value.add(item("b"));
			value.add(item("c"));

			value.remove(1);

			assertEquals("Removing the middle element of " + property,
				Arrays.asList("a", "c"), names(value.elements()));
		}
	}

	/** An index outside the collection is ignored rather than throwing. */
	public void testRemoveOutsideTheCollectionDoesNothing() {
		ConfigCollectionValue value = value(ShapesConfig.PLAIN_LIST);
		value.add(item("a"));

		value.remove(-1);
		value.remove(1);

		assertEquals(Arrays.asList("a"), names(value.elements()));
	}

	/** Moving rearranges every shape, the map included. */
	public void testMoveRearrangesEveryShape() {
		for (String property : new String[] { ShapesConfig.PLAIN_LIST, ShapesConfig.ARRAY, ShapesConfig.INDEX }) {
			ConfigCollectionValue value = value(property);
			value.add(item("a"));
			value.add(item("b"));
			value.add(item("c"));

			value.move(2, -1);

			assertEquals("Moving the last element up in " + property,
				Arrays.asList("a", "c", "b"), names(value.elements()));

			value.move(0, 1);

			assertEquals("Moving the first element down in " + property,
				Arrays.asList("c", "a", "b"), names(value.elements()));
		}
	}

	/**
	 * A map's new order survives in the map itself, not merely in what this class reports back.
	 *
	 * <p>
	 * The write-back cannot go through {@link ConfigurationItem#update(PropertyDescriptor, Object)}
	 * for a pure reordering: that compares the maps with {@link Map#equals(Object)}, which ignores
	 * iteration order, and skips a write it considers a no-op.
	 * </p>
	 */
	public void testMovingAMapEntryChangesTheMapsOwnOrder() {
		ConfigCollectionValue value = value(ShapesConfig.INDEX);
		value.add(item("a"));
		value.add(item("b"));

		value.move(1, -1);

		assertEquals("The map's own iteration order must have changed.",
			Arrays.asList("b", "a"), new ArrayList<>(_config.getIndex().keySet()));
	}

	/** A move that would leave the collection is ignored, in either direction. */
	public void testMoveBeyondTheEndsDoesNothing() {
		ConfigCollectionValue value = value(ShapesConfig.PLAIN_LIST);
		value.add(item("a"));
		value.add(item("b"));

		value.move(0, -1);
		value.move(1, 1);
		value.move(5, -1);

		assertEquals(Arrays.asList("a", "b"), names(value.elements()));
	}

	/** Replacing keeps the position. */
	public void testReplaceKeepsThePosition() {
		for (String property : new String[] { ShapesConfig.PLAIN_LIST, ShapesConfig.ARRAY, ShapesConfig.INDEX }) {
			ConfigCollectionValue value = value(property);
			value.add(item("a"));
			value.add(item("b"));

			value.replace(0, item("z"));

			assertEquals("Replacing the first element of " + property,
				Arrays.asList("z", "b"), names(value.elements()));
		}
	}

	/** An element's position is where the editor addresses it. */
	public void testIndexOf() {
		ConfigCollectionValue value = value(ShapesConfig.PLAIN_LIST);
		Item first = item("a");
		Item second = item("b");
		value.add(first);
		value.add(second);

		assertEquals(0, value.indexOf(first));
		assertEquals(1, value.indexOf(second));
		assertEquals("An element that is not in the collection has no position.",
			-1, value.indexOf(item("c")));
	}

	/**
	 * A map is reorderable although it is not {@link PropertyDescriptor#isOrdered() ordered} - that
	 * flag only says a map has no positional index of its own, not that its iteration order is
	 * arbitrary.
	 */
	public void testAMapIsReorderableThoughNotOrdered() {
		assertFalse("Precondition: a MAP property is not flagged as ordered.",
			_config.descriptor().getProperty(ShapesConfig.INDEX).isOrdered());
		assertTrue("A map's entries can still be rearranged.", value(ShapesConfig.INDEX).isReorderable());
		assertTrue(value(ShapesConfig.PLAIN_LIST).isReorderable());
		assertTrue(value(ShapesConfig.ARRAY).isReorderable());
	}

	/** Only a collection indexed by a property of its entries is keyed. */
	public void testIsKeyed() {
		assertTrue(value(ShapesConfig.KEYED_LIST).isKeyed());
		assertTrue(value(ShapesConfig.INDEX).isKeyed());
		assertFalse(value(ShapesConfig.PLAIN_LIST).isKeyed());
		assertFalse(value(ShapesConfig.ARRAY).isKeyed());
	}

	/** An unkeyed collection has no key property to resolve. */
	public void testAnUnkeyedCollectionHasNoKeyProperty() {
		assertNull(value(ShapesConfig.PLAIN_LIST).keyProperty(item("a")));
	}

	/**
	 * The key property comes from the entry's own descriptor, not from the collection's declared
	 * element type.
	 *
	 * <p>
	 * Identity matters, not equality: {@link PropertyDescriptor} overrides neither
	 * {@link Object#equals(Object)} nor {@link Object#hashCode()}, and a subtype's descriptor
	 * creates its own instance even for a property inherited unchanged. The editor hides the key
	 * from the nested form by identity, so the two must be the same object.
	 * </p>
	 */
	public void testTheKeyPropertyComesFromTheEntrysOwnDescriptor() {
		PolymorphicConfig config = TypedConfiguration.newConfigItem(PolymorphicConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(PolymorphicConfig.ITEMS);
		ConfigCollectionValue value = new ConfigCollectionValue(config, property);

		HandlerAConfig entry = TypedConfiguration.newConfigItem(HandlerAConfig.class);
		PropertyDescriptor resolved = value.keyProperty(entry);

		assertNotNull(resolved);
		assertSame("The entry's own descriptor must be the source.",
			entry.descriptor().getProperty(HandlerConfig.ENTRY_KEY), resolved);
		assertNotSame("The declared element type's instance is a different object.",
			property.getKeyProperty(), resolved);
	}

	/** A key already in use is found, whichever shape holds the entries. */
	public void testHasEntryWithKey() {
		for (String property : new String[] { ShapesConfig.KEYED_LIST, ShapesConfig.INDEX }) {
			ConfigCollectionValue value = value(property);
			value.add(item("taken"));

			assertTrue("An existing key must be found in " + property, value.hasEntryWithKey("taken"));
			assertFalse("An unused key must not be reported as taken in " + property,
				value.hasEntryWithKey("free"));
		}
	}

	/** A freshly created element has the collection's own element type. */
	public void testNewElement() {
		ConfigurationItem created = value(ShapesConfig.PLAIN_LIST).newElement();

		assertEquals(Item.class, created.descriptor().getConfigurationInterface());
	}

	/** Suite requiring {@link TypeIndex} for {@link TypedConfiguration}. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigCollectionValue.class, TypeIndex.Module.INSTANCE));
	}
}
