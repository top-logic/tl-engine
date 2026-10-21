/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.GlobalKey;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test for referencing elements ({@link ConfigurationSchemaConstants#CONFIG_REF}) addressing an
 * entry of a list keyed by a {@link GlobalKey} property without naming the path to it, and for the
 * uniqueness of global keys, when reading stacked sources with the {@link ConfigurationReader}.
 */
@SuppressWarnings("javadoc")
public class TestConfigReference extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "root";

	private static final String REF = "config:" + ConfigurationSchemaConstants.CONFIG_REF;

	private static final String INDEX = "config:" + ConfigurationSchemaConstants.LIST_INDEX;

	private static final String OPERATION = "config:" + ConfigurationSchemaConstants.LIST_OPERATION;

	private static final String POSITION = "config:" + ConfigurationSchemaConstants.LIST_POSITION;

	private static final String REFERENCE = "config:" + ConfigurationSchemaConstants.LIST_REFERENCE_ATTR_NAME;

	private static final String OVERRIDE = "config:" + ConfigurationSchemaConstants.CONFIG_OVERLOADING_OVERRIDE;

	/** An entry whose key is unique only within its list. */
	public interface Item extends ConfigurationItem {

		String NAME = "name";

		String VALUE = "value";

		@Name(NAME)
		String getName();

		@Name(VALUE)
		String getValue();

	}

	/** A node of a tree whose name is unique in the whole tree. */
	public interface Node extends ConfigurationItem {

		String NAME = "name";

		String VALUE = "value";

		String NODES = "nodes";

		String OTHERS = "others";

		String LOCAL = "local";

		String DETAIL = "detail";

		String MAP = "map";

		String BY_VALUE = "by-value";

		String OTHER = "other";

		@Name(NAME)
		@GlobalKey
		String getName();

		@Name(VALUE)
		String getValue();

		@Name(OTHER)
		String getOther();

		/** The children, written directly in the node. */
		@Name(NODES)
		@Key(NAME)
		@EntryTag("node")
		@DefaultContainer
		List<Node> getNodes();

		/** A second list of nodes, written in its own element. */
		@Name(OTHERS)
		@Key(NAME)
		@EntryTag("node")
		List<Node> getOthers();

		/** A list whose key is not global. */
		@Name(LOCAL)
		@Key(Item.NAME)
		@EntryTag("item")
		List<Item> getLocal();

		/** A single node, reached through an item property. */
		@Name(DETAIL)
		Node getDetail();

		/** Nodes in a map, keyed by their name. */
		@Name(MAP)
		@Key(NAME)
		@EntryTag("node")
		Map<String, Node> getMap();

		/** Nodes in a list keyed by another property than the global one. */
		@Name(BY_VALUE)
		@Key(VALUE)
		@EntryTag("node")
		List<Node> getByValue();

	}

	/** The chain a > b > c. */
	private static final String CHAIN = root(
		"<node name='a' value='a1'>"
			+ "<node name='b' value='b1'>"
			+ "<node name='c' value='c1'/>"
			+ "</node>"
			+ "</node>");

	/** The chain a > b > c beside the chain x > y. */
	private static final String TWO_CHAINS = root(
		"<node name='a'>"
			+ "<node name='b'>"
			+ "<node name='c' value='c1'/>"
			+ "</node>"
			+ "</node>"
			+ "<node name='x'>"
			+ "<node name='y' value='y1'/>"
			+ "</node>");

	public void testReferenceBelowRoot() throws ConfigurationException {
		Node root = readRoot(CHAIN, root("<node " + REF + "='c' value='c2'/>"));
		assertChain(root, "a1", "b1", "c2");
	}

	public void testNestedReferences() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node " + REF + "='a' value='a2'>"
				+ "<node " + REF + "='c' value='c2'/>"
				+ "</node>"));
		assertChain(root, "a2", "b1", "c2");
	}

	public void testReferenceInsideElementOnThePath() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node name='a' value='a2'>"
				+ "<node " + REF + "='c' value='c2'/>"
				+ "</node>"));
		assertChain(root, "a2", "b1", "c2");
	}

	public void testReferenceAddsToContent() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node " + REF + "='b'>"
				+ "<node name='d' value='d1'/>"
				+ "</node>"));
		Node b = node(node(root, "a"), "b");
		assertNames(b.getNodes(), "c", "d");
		assertEquals("d1", node(b, "d").getValue());
	}

	public void testReferenceUpdatesEntryOfOtherList() throws ConfigurationException {
		Node root = readRoot(
			root("<node name='a'><others><node name='o' value='o1'/></others></node>"),
			root("<node " + REF + "='o' value='o2'/>"));
		Node a = node(root, "a");
		assertEquals(1, a.getOthers().size());
		assertEquals("o2", a.getOthers().get(0).getValue());
	}

	public void testReferenceToItemProperty() throws ConfigurationException {
		Node root = readRoot(
			root("<detail name='d' value='d1'><node name='e' value='e1'/></detail>"),
			root("<detail " + REF + "='d' value='d2'/>"));
		assertEquals("d2", root.getDetail().getValue());
		assertEquals("The content of the item is kept.", "e1", node(root.getDetail(), "e").getValue());
	}

	public void testRemoveItemProperty() throws ConfigurationException {
		Node root = readRoot(
			root("<detail name='d' value='d1'/>"),
			root("<detail " + REF + "='d' " + OPERATION + "='remove'/>"));
		assertNull(root.getDetail());
	}

	public void testPositioningItemPropertyFails() {
		assertFails("cannot be positioned",
			root("<detail name='d' value='d1'/>"),
			root("<detail " + REF + "='d' " + POSITION + "='begin'/>"));
	}

	public void testReferenceToMapEntry() throws ConfigurationException {
		Node root = readRoot(
			root("<map><node name='m' value='m1'/></map>"),
			root("<node " + REF + "='m' value='m2'/>"));
		assertEquals("m2", root.getMap().get("m").getValue());
	}

	public void testReferenceToEntryOfListWithOtherKey() throws ConfigurationException {
		Node root = readRoot(
			root("<by-value><node name='k' value='v1'/></by-value>"),
			root("<node " + REF + "='k' other='changed'/>"));
		assertEquals(1, root.getByValue().size());
		assertEquals("changed", root.getByValue().get(0).getOther());
		assertEquals("v1", root.getByValue().get(0).getValue());
	}

	public void testDuplicateGlobalKeyAcrossPropertyKindsFails() {
		assertFails("Duplicate value 'x'", root("<detail name='x'/><node name='x'/>"));
	}

	public void testReferenceBelowItemProperty() throws ConfigurationException {
		Node root = readRoot(
			root("<detail name='d'><node name='e' value='e1'/></detail>"),
			root("<node " + REF + "='e' value='e2'/>"));
		assertEquals("e2", node(root.getDetail(), "e").getValue());
	}

	public void testReferenceSeesEntriesReadBeforeInSameSource() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node name='a' value='a2'>"
				+ "<node name='b' value='b2'/>"
				+ "</node>"
				+ "<node " + REF + "='c' value='c2'/>"));
		assertChain(root, "a2", "b2", "c2");
	}

	public void testReferenceSeesEntriesOfOpenElement() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node name='a' value='a2'>"
				+ "<node name='b' value='b2'/>"
				+ "<node " + REF + "='c' value='c2'/>"
				+ "</node>"));
		assertChain(root, "a2", "b2", "c2");
	}

	public void testReferenceToEntryAddedBySameSource() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node name='n' value='n1'/>"
				+ "<node " + REF + "='n' value='n2'/>"));
		assertNames(root.getNodes(), "a", "n");
		assertEquals("n2", node(root, "n").getValue());
	}

	public void testReferenceInsideNewElement() throws ConfigurationException {
		Node root = readRoot(CHAIN, root(
			"<node name='f'>"
				+ "<node name='g' value='g1'/>"
				+ "<node " + REF + "='g' value='g2'/>"
				+ "</node>"));
		Node f = node(root, "f");
		assertNames(f.getNodes(), "g");
		assertEquals("g2", node(f, "g").getValue());
	}

	public void testReferenceInSingleSource() throws ConfigurationException {
		Node root = readRoot(root(
			"<node name='a'><node name='b' value='b1'/></node>"
				+ "<node " + REF + "='b' value='b2'/>"));
		assertEquals("b2", node(node(root, "a"), "b").getValue());
	}

	public void testOverrideReplacesEntryAndKeepsKey() throws ConfigurationException {
		Node root = readRoot(CHAIN, root("<node " + REF + "='b' " + OVERRIDE + "='true' value='b2'/>"));
		Node b = node(node(root, "a"), "b");
		assertEquals("b2", b.getValue());
		assertEquals("The replacement has no children of the base.", 0, b.getNodes().size());
	}

	public void testRemove() throws ConfigurationException {
		Node root = readRoot(CHAIN, root("<node " + REF + "='c' " + OPERATION + "='remove'/>"));
		assertEquals(0, node(node(root, "a"), "b").getNodes().size());
	}

	public void testMoveWithinList() throws ConfigurationException {
		Node root = readRoot(
			root("<node name='b'><node name='c'/><node name='d'/><node name='e'/></node>"),
			root("<node " + REF + "='e' " + POSITION + "='before' " + REFERENCE + "='c'/>"));
		assertNames(node(root, "b").getNodes(), "e", "c", "d");
	}

	public void testRepeatingKeyAllowed() throws ConfigurationException {
		Node root = readRoot(CHAIN, root("<node " + REF + "='c' name='c' value='c2'/>"));
		assertChain(root, "a1", "b1", "c2");
	}

	public void testChangingKeyFails() {
		assertFails("does not change the key", CHAIN, root("<node " + REF + "='c' name='z'/>"));
	}

	public void testUnknownReferenceFails() {
		assertFails("No item with the key 'z'", CHAIN, root("<node " + REF + "='z'/>"));
	}

	public void testReferenceNotBelowEnclosingFails() {
		assertFails("No item with the key 'c'", TWO_CHAINS, root(
			"<node " + REF + "='x'>"
				+ "<node " + REF + "='c' value='c2'/>"
				+ "</node>"));
	}

	public void testReferenceNotBelowElementOnThePathFails() {
		assertFails("No item with the key 'y'", TWO_CHAINS, root(
			"<node name='a'>"
				+ "<node " + REF + "='y' value='y2'/>"
				+ "</node>"));
	}

	public void testReferenceIntoLocalKeyFails() {
		assertFails("No item with the key 'i'",
			root("<local><item name='i' value='i1'/></local>"),
			root("<item " + REF + "='i' value='i2'/>"));
	}

	public void testReferenceOnRootFails() {
		assertFails("not allowed on the root element", CHAIN,
			XML_DECLARATION + "<" + ROOT_TAG + XML_CONFIG_NAMESPACE_DECLARATION + " " + REF + "='a'/>");
	}

	public void testReferenceOnListEntryFails() {
		assertFails("not allowed on an entry of a list property", CHAIN,
			root("<others><node " + REF + "='a'/></others>"));
	}

	public void testReferenceWithIndexFails() {
		assertFails("must not be combined", CHAIN, root("<node " + REF + "='c' " + INDEX + "='0'/>"));
	}

	public void testReferenceWithAddFails() {
		assertFails("must not be combined", CHAIN, root("<node " + REF + "='c' " + OPERATION + "='add'/>"));
	}

	public void testDuplicateGlobalKeyInOneSourceFails() {
		assertFails("Duplicate value 'x'", root(
			"<node name='a'><node name='x'/></node>"
				+ "<node name='b'><node name='x'/></node>"));
	}

	public void testDuplicateGlobalKeyAcrossListsFails() {
		assertFails("Duplicate value 'x'", root(
			"<node name='a'><node name='x'/><others><node name='x'/></others></node>"));
	}

	public void testDuplicateGlobalKeyAcrossSourcesFails() {
		assertFails("Duplicate value 'c'", CHAIN, root("<node name='x'><node name='c'/></node>"));
	}

	public void testLocalKeysMayRepeat() throws ConfigurationException {
		Node root = readRoot(root(
			"<local><item name='i' value='1'/></local>"
				+ "<node name='a'><local><item name='i' value='2'/></local></node>"));
		assertEquals("1", root.getLocal().get(0).getValue());
		assertEquals("2", node(root, "a").getLocal().get(0).getValue());
	}

	public void testAnonymousEntriesTakeNoPartInUniqueness() throws ConfigurationException {
		Node root = readRoot(root(
			"<node value='1'/>"
				+ "<node value='2'/>"
				+ "<node name='a'><node value='3'/></node>"));
		assertEquals(3, root.getNodes().size());
	}

	private static void assertChain(Node root, String aValue, String bValue, String cValue) {
		assertNames(root.getNodes(), "a");
		Node a = node(root, "a");
		assertEquals(aValue, a.getValue());
		assertNames(a.getNodes(), "b");
		Node b = node(a, "b");
		assertEquals(bValue, b.getValue());
		assertNames(b.getNodes(), "c");
		assertEquals(cValue, node(b, "c").getValue());
	}

	private static Node node(Node parent, String name) {
		for (Node node : parent.getNodes()) {
			if (name.equals(node.getName())) {
				return node;
			}
		}
		fail("No node '" + name + "'.");
		return null;
	}

	private static void assertNames(List<Node> nodes, String... expectedNames) {
		List<String> names = new ArrayList<>();
		for (Node node : nodes) {
			names.add(node.getName());
		}
		assertEquals(List.of(expectedNames), names);
	}

	private static String root(String content) {
		return XML_DECLARATION + "<" + ROOT_TAG + XML_CONFIG_NAMESPACE_DECLARATION + ">" + content + "</" + ROOT_TAG
			+ ">";
	}

	private Node readRoot(String... sources) throws ConfigurationException {
		return read(sources);
	}

	private void assertFails(String expectedMessagePart, String... sources) {
		try {
			readRoot(sources);
		} catch (AssertionFailedError | ConfigurationException | RuntimeException ex) {
			BasicTestCase.assertErrorMessage("Unexpected error message.",
				Pattern.compile(Pattern.quote(expectedMessagePart)), ex);
			return;
		}
		fail("Expected failure containing '" + expectedMessagePart + "'.");
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Node.class));
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConfigReference.class));
	}

}
