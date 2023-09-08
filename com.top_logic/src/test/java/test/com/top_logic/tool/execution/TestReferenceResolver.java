/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.execution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.tool.execution.ReferenceResolver;
import com.top_logic.tool.execution.ReferenceResolver.Analyzer;

/**
 * Test case for {@link ReferenceResolver}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestReferenceResolver extends TestCase {

	static final class ItemAnalyzer implements Analyzer<String, TestReferenceResolver.Item> {
		@Override
		public boolean isAggregation(Item item) {
			return item instanceof Aggregation;
		}

		@Override
		public List<Item> decompose(Item aggregation) {
			return ((Aggregation) aggregation).getItems();
		}

		@Override
		public Item compose(List<Item> items) {
			return new Aggregation(items.toArray(new Item[items.size()]));
		}

		@Override
		public boolean isReference(Item item) {
			return item instanceof Reference;
		}

		@Override
		public String getReferenceId(Item reference) {
			return ((Reference) reference).getId();
		}
	}

	static public abstract class Item {

	}

	static public class Aggregation extends Item {
		private final List<Item> _items;

		public Aggregation(Item... items) {
			_items = Arrays.asList(items);
		}

		List<Item> getItems() {
			return _items;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this || (obj instanceof Aggregation && ((Aggregation) obj).getItems().equals(_items));
		}

		@Override
		public int hashCode() {
			return _items.hashCode();
		}

		@Override
		public String toString() {
			return "Aggregation(" + _items + ")";
		}
	}

	static public class Reference extends Item {
		private final String _id;

		public Reference(String id) {
			_id = id;
		}

		String getId() {
			return _id;
		}

		@Override
		public String toString() {
			return "Reference(" + _id + ")";
		}
	}

	static public class Leaf extends Item {
		private final String _name;

		public Leaf(String name) {
			_name = name;
		}

		@Override
		public String toString() {
			return "Leaf(" + _name + ")";
		}
	}

	public void testResolve() {
		Protocol log = new AssertProtocol("Resolution must succeed.");
		Analyzer<String, Item> analyzer = new ItemAnalyzer();
		Map<String, Item> items = new LinkedHashMap<>();
		items.put("i7", new Reference("i6"));
		items.put("i6", new Reference("i5"));
		items.put("i5", new Reference("i3"));
		items.put("i4", new Aggregation(new Reference("i1"), new Aggregation(new Reference("i2"), new Reference("i7"))));
		items.put("i1", new Leaf("i1"));
		items.put("i2", new Leaf("i2"));
		items.put("i3", new Leaf("i3"));

		Map<String, Item> resolvedItems = new HashMap<>(items);
		new ReferenceResolver<>(log, analyzer, resolvedItems).resolve();
		
		assertEquals(items.get("i1"), resolvedItems.get("i1"));
		assertEquals(items.get("i2"), resolvedItems.get("i2"));
		assertEquals(items.get("i3"), resolvedItems.get("i3"));
		assertEquals(new Aggregation(items.get("i1"), new Aggregation(items.get("i2"), items.get("i3"))),
			resolvedItems.get("i4"));
		assertEquals(items.get("i3"), resolvedItems.get("i5"));
		assertEquals(items.get("i3"), resolvedItems.get("i5"));
		assertEquals(items.get("i3"), resolvedItems.get("i7"));
	}

	public void testDetectUndefined() {
		BufferingProtocol log = new BufferingProtocol();
		Analyzer<String, Item> analyzer = new ItemAnalyzer();
		Map<String, Item> items = new LinkedHashMap<>();
		Reference undefined = new Reference("undefined");
		items.put("i3", new Aggregation(new Reference("i1"), new Aggregation(undefined, new Reference("i2"))));
		items.put("i1", new Leaf("i1"));
		items.put("i2", new Leaf("i2"));

		Map<String, Item> resolvedItems = new HashMap<>(items);
		new ReferenceResolver<>(log, analyzer, resolvedItems).resolve();
		assertTrue(log.hasErrors());
		assertEquals("Undefined reference 'undefined' in item with ID 'i3'.\n", log.getError());

		assertEquals(items.get("i1"), resolvedItems.get("i1"));
		assertEquals(items.get("i2"), resolvedItems.get("i2"));
		assertEquals(new Aggregation(items.get("i1"), new Aggregation(undefined, items.get("i2"))),
			resolvedItems.get("i3"));
	}

	public void testDetectCycle() {
		BufferingProtocol log = new BufferingProtocol();
		Analyzer<String, Item> analyzer = new ItemAnalyzer();
		Map<String, Item> items = new LinkedHashMap<>();
		Reference cycleStart = new Reference("i4");
		items.put("i3", new Aggregation(new Reference("i1"), new Aggregation(cycleStart, new Reference("i2"))));
		items.put("i1", new Leaf("i1"));
		items.put("i2", new Leaf("i2"));
		items.put("i4", new Reference("i5"));
		items.put("i5", new Reference("i3"));
		items.put("i6", new Reference("i7"));
		items.put("i7", new Leaf("i7"));
		
		Map<String, Item> resolvedItems = new LinkedHashMap<>(items);
		new ReferenceResolver<>(log, analyzer, resolvedItems).resolve();
		assertTrue(log.hasErrors());
		assertEquals(
			"Cyclic reference detected in item with IDs: [i3, i4, i5].\n" +
			"Cyclic reference detected in item with IDs: [i4, i5, i3].\n" +
			"Cyclic reference detected in item with IDs: [i5, i3, i4].\n",
			log.getError());
		
		assertEquals(items.get("i1"), resolvedItems.get("i1"));
		assertEquals(items.get("i2"), resolvedItems.get("i2"));
		assertEquals(items.get("i3"), resolvedItems.get("i3"));
		assertEquals(items.get("i4"), resolvedItems.get("i4"));
		assertEquals(items.get("i5"), resolvedItems.get("i5"));
		assertEquals(items.get("i7"), resolvedItems.get("i6"));
		assertEquals(items.get("i7"), resolvedItems.get("i7"));
	}
	
}
