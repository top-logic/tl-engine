/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.inspector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.view.inspector.InspectedNode;
import com.top_logic.layout.view.inspector.InspectedNode.StateEntry;

/**
 * Tests the value the UI inspector shows: the flattening of a projected state to addressable leaves
 * and the ascent to the enclosing element.
 */
public class TestInspectedNode extends TestCase {

	private static InspectedNode node(String address, Map<String, Object> state) {
		return new InspectedNode("main",
			address, new ScriptingNodeView(address, "table", null, "TLTable", state, List.of(), List.of()));
	}

	private static Map<String, Object> map(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			result.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return result;
	}

	private static String valueAt(InspectedNode node, String path) {
		for (StateEntry entry : node.stateEntries()) {
			if (path.equals(entry.path())) {
				return entry.value();
			}
		}
		fail("No state entry at '" + path + "', found: " + node.stateEntries());
		return null;
	}

	/**
	 * A scalar state entry is a leaf of its own, rendered as JSON.
	 */
	public void testScalarLeaves() {
		InspectedNode node = node("/table", map("totalRowCount", Integer.valueOf(7), "title", "Demo"));

		assertEquals(List.of("totalRowCount", "title"),
			node.stateEntries().stream().map(StateEntry::path).toList());
		assertEquals("7", valueAt(node, "totalRowCount"));
		assertEquals("\"Demo\"", valueAt(node, "title"));
	}

	/**
	 * A nested object contributes one row per leaf, addressed by the path of the keys leading to it.
	 */
	public void testNestedMapIsFlattened() {
		InspectedNode node = node("/table", map("diagnostics",
			map("hiddenByAccess", map("count", Integer.valueOf(2), "byType", map("tl.accounts:Person", Integer.valueOf(2))))));

		assertEquals(List.of("diagnostics.hiddenByAccess.count", "diagnostics.hiddenByAccess.byType.tl.accounts:Person"),
			node.stateEntries().stream().map(StateEntry::path).toList());
		assertEquals("2", valueAt(node, "diagnostics.hiddenByAccess.count"));
	}

	/**
	 * A list is a leaf: it is rendered as JSON instead of being spread over rows.
	 */
	public void testListIsALeaf() throws Exception {
		InspectedNode node = node("/table", map("columns", List.of("name", "age")));

		assertEquals(List.of("columns"), node.stateEntries().stream().map(StateEntry::path).toList());
		assertEquals(List.of("name", "age"), com.top_logic.basic.json.JSON.fromString(valueAt(node, "columns")));
	}

	/**
	 * An object without entries is a leaf, too: there is nothing below it to address.
	 */
	public void testEmptyMapIsALeaf() {
		InspectedNode node = node("/table", map("filters", Map.of()));

		assertEquals(List.of("filters"), node.stateEntries().stream().map(StateEntry::path).toList());
	}

	/**
	 * A node without state has no entries.
	 */
	public void testEmptyState() {
		assertEquals(List.of(), node("/table", Map.of()).stateEntries());
	}

	/**
	 * The enclosing element is the address without its last segment.
	 */
	public void testParentAddress() {
		assertEquals("/appShell/panel", InspectedNode.parentAddress("/appShell/panel/table[Demo]"));
		assertEquals("/appShell", InspectedNode.parentAddress("/appShell/panel"));
		assertEquals("/appShell", node("/appShell/panel", Map.of()).parentAddress());
	}

	/**
	 * A top-level element, the synthetic root and no address at all have no enclosing element.
	 */
	public void testNoParentAtRoot() {
		assertNull(InspectedNode.parentAddress("/appShell"));
		assertNull(InspectedNode.parentAddress("/"));
		assertNull(InspectedNode.parentAddress(null));
		assertNull(node("/appShell", Map.of()).parentAddress());
	}

}
