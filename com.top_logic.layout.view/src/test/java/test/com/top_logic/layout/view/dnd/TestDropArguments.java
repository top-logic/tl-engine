/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.dnd;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.command.ArgumentsProbe;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropPoint;
import com.top_logic.layout.react.control.dnd.DropRequest;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.dnd.DeclaredDrop;
import com.top_logic.layout.view.dnd.DropArguments;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropConfig;
import com.top_logic.layout.view.dnd.DropSignature;
import com.top_logic.layout.view.element.TreeDropConfig;
import com.top_logic.layout.view.element.TreeElement;

/**
 * Tests the named arguments of a drop: the dragged objects and the objects its
 * {@link DropSignature} refers to, see {@link DropArguments}.
 */
public class TestDropArguments extends AbstractModelAccessTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	/**
	 * Tests the names of the arguments of each signature, by default and renamed.
	 */
	public void testNamesPerSignature() throws Exception {
		DropConfig plain = drops("<drop target='node'/>").get(0);
		assertEquals(List.of("objects", "target", "x", "y"), DropArguments.names(plain, DropSignature.CONTROL));
		assertEquals(List.of("objects", "target", "x", "y"), DropArguments.names(plain, DropSignature.ONTO));
		assertEquals(List.of("objects", "before"), DropArguments.names(plain, DropSignature.ORDERED_LIST));
		assertEquals(List.of("objects", "parent", "before"), DropArguments.names(plain, DropSignature.ORDERED_TREE));

		DropConfig renamed =
			drops("<drop target='ordered' as='nodes' parent-as='folder' before-as='next' target-as='onto'/>").get(0);
		assertEquals(List.of("nodes", "onto", "x", "y"), DropArguments.names(renamed, DropSignature.ONTO));

		DropConfig pointRenamed = drops("<drop target='node' x-as='left' y-as='top'/>").get(0);
		assertEquals(List.of("objects", "target", "left", "top"),
			DropArguments.names(pointRenamed, DropSignature.ONTO));
		assertEquals(List.of("objects", "target", "left", "top"),
			DropArguments.names(pointRenamed, DropSignature.CONTROL));
		assertEquals(List.of("nodes", "next"), DropArguments.names(renamed, DropSignature.ORDERED_LIST));
		assertEquals(List.of("nodes", "folder", "next"), DropArguments.names(renamed, DropSignature.ORDERED_TREE));
		assertEquals("nodes", DropArguments.scope(renamed, DropSignature.ORDERED_TREE).defaultName());
	}

	/**
	 * Tests that the refusal function uses the arguments of the drop as variables.
	 */
	public void testRefuseIfUsesReferences() throws Exception {
		DeclaredDrop insert = compile(
			"<drop target='ordered' as='nodes' parent-as='folder' "
				+ "refuse-if=\"$folder == 'locked' || $before == 'last' || $nodes.size() != 1\"/>",
			DropSignature.ORDERED_TREE);

		assertFalse(isTrue(insert.refuseIf().execute(List.of("a"), "open", "first")));
		assertTrue(isTrue(insert.refuseIf().execute(List.of("a"), "locked", "first")));
		assertTrue(isTrue(insert.refuseIf().execute(List.of("a"), "open", "last")));
		assertTrue(isTrue(insert.refuseIf().execute(List.of("a", "b"), "open", "first")));

		DeclaredDrop onto = compile("<drop target='node' refuse-if=\"$target == $objects[0]\"/>", DropSignature.ONTO);
		assertTrue(isTrue(onto.refuseIf().execute(List.of("a"), "a", null, null)));
		assertFalse(isTrue(onto.refuseIf().execute(List.of("a"), "b", null, null)));
	}

	/**
	 * Tests that the refusal function sees the coordinates of the point the drop is made at, by
	 * their default names and renamed, and {@code null} without a point.
	 */
	public void testRefuseIfUsesPoint() throws Exception {
		DeclaredDrop plain = compile("<drop target='node' refuse-if=\"$x == null || $y == null || $x > $y\"/>",
			DropSignature.ONTO);
		assertFalse(isTrue(plain.refuseIf().execute(List.of("a"), "t", 1.0, 2.0)));
		assertTrue(isTrue(plain.refuseIf().execute(List.of("a"), "t", 3.0, 2.0)));
		assertTrue("Without a point the coordinates are null.",
			isTrue(plain.refuseIf().execute(List.of("a"), "t", null, null)));

		DeclaredDrop renamed = compile(
			"<drop target='tree' x-as='left' y-as='top' refuse-if=\"$left &lt; 0 || $top &lt; 0\"/>",
			DropSignature.CONTROL);
		assertFalse(isTrue(renamed.refuseIf().execute(List.of("a"), null, 0.0, 5.0)));
		assertTrue(isTrue(renamed.refuseIf().execute(List.of("a"), null, -1.0, 5.0)));
	}

	/**
	 * Tests that the actions of a drop see the coordinates of the point the drop is made at, taken
	 * from its location.
	 */
	public void testActionsSeePoint() throws Exception {
		TreeDropConfig config = drops(
			"<drop target='node' y-as='top'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</drop>").get(0);
		DeclaredDrop drop = compile(config, DropSignature.ONTO);

		ViewActionChain.run(new DefaultViewContext(null), drop.actions(), DropArguments.arguments(drop.names(),
			DropSignature.ONTO, List.of("a"), new DropLocation.Onto("n", new DropPoint(4.5, 6))), null);
		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of("objects", "target", "x", "top"), seen.names());
		assertEquals("n", seen.get("target"));
		assertEquals(Double.valueOf(4.5), seen.get("x"));
		assertEquals(Double.valueOf(6), seen.get("top"));

		ViewActionChain.run(new DefaultViewContext(null), drop.actions(), DropArguments.arguments(drop.names(),
			DropSignature.ONTO, List.of("a"), new DropLocation.Onto("n")), null);
		ActionArguments withoutPoint = ArgumentsProbe.SEEN.get(1);
		assertNull(withoutPoint.get("x"));
		assertNull(withoutPoint.get("top"));
	}

	/**
	 * Tests that naming a coordinate of an insertion, which has no point, is a configuration error.
	 */
	public void testNamingPointOfInsertionIsError() throws Exception {
		DropConfig config = drops("<drop target='ordered' x-as='left'/>").get(0);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDropArguments.class);
		DeclaredDrop.compile(context, config, DropSignature.ORDERED_TREE);
		assertTrue(context.hasErrors());
	}

	/**
	 * Tests that naming a reference the signature of the drop does not have is a configuration
	 * error.
	 */
	public void testRenamingMissingReferenceIsError() throws Exception {
		DropConfig config = drops("<drop target='node' before-as='next'/>").get(0);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDropArguments.class);
		DeclaredDrop.compile(context, config, DropSignature.ONTO);
		assertTrue(context.hasErrors());
	}

	/**
	 * Tests that the actions of a drop see the references as named arguments, the dragged objects
	 * being the main argument, and that the script editor offers the same names.
	 */
	public void testActionsSeeReferences() throws Exception {
		List<TreeDropConfig> drops = drops(
			"<drop target='ordered' parent-as='folder'>"
				+ "<execute-script function=\"[$folder, $before]\"><with-result as='places'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</with-result></execute-script>"
				+ "</drop>");
		TreeDropConfig config = drops.get(0);
		DeclaredDrop drop = compile(config, DropSignature.ORDERED_TREE);

		ActionArguments arguments = DropArguments.arguments(drop.names(), DropSignature.ORDERED_TREE,
			List.of("a"), new DropLocation.Insert("p", "b"));
		ViewActionChain.run(new DefaultViewContext(null), drop.actions(), arguments, null);

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of("objects", "folder", "before", "places"), seen.names());
		assertEquals(List.of("p", "b"), seen.get("places"));
		assertEquals(List.of("a"), seen.get("objects"));
		assertEquals("objects", arguments.defaultName());

		assertEquals("The editor offers the names of the drop.", List.of("objects", "folder", "before"),
			ActionScopes.scopeAt(config).names());
		assertEquals(List.of("objects", "folder", "before"),
			ActionScopes.scopeAt(config.getActions().get(0)).names());
	}

	/**
	 * Tests that the refusal function and the actions of a drop with an input see the current
	 * value of the input channel under the name of the input.
	 */
	public void testRefuseIfAndActionsSeeInput() throws Exception {
		TreeDropConfig config = drops(
			"<drop target='node' input='shown as pinned' refuse-if='$objects.containsSome($pinned)'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</drop>").get(0);
		DeclaredDrop drop = compile(config, DropSignature.ONTO);
		assertEquals(List.of("objects", "target", "x", "y", "pinned"), drop.names());
		assertEquals("The editor offers the input.", List.of("objects", "target", "x", "y", "pinned"),
			ActionScopes.scopeAt(config).names());

		DefaultViewContext context = new DefaultViewContext(null);
		DefaultViewChannel shown = new DefaultViewChannel("shown");
		context.registerChannel("shown", shown);
		DropBinding binding = DeclaredDrop.bind(context, null, () -> {
			// Nothing to announce.
		}, List.of(drop));

		shown.set(List.of("a"));
		assertFalse("An object the input holds is refused.", binding.check(request("a")).isAccepted());
		assertTrue(binding.check(request("b")).isAccepted());

		shown.set(List.of("b"));
		assertTrue("The refusal follows the current value of the input.", binding.check(request("a")).isAccepted());
		assertFalse(binding.check(request("b")).isAccepted());

		DropRequest accepted = request("a");
		binding.onDrop(accepted.event(accepted.location(DropMode.ONTO)));
		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of("objects", "target", "x", "y", "pinned"), seen.names());
		assertEquals(List.of("b"), seen.get("pinned"));
		assertEquals("t", seen.get("target"));
	}

	/**
	 * Tests that a drop without input has no argument for one, and that a refusal function using
	 * an input the drop does not declare is a configuration error.
	 */
	public void testNoInputNoArgument() throws Exception {
		TreeDropConfig config = drops("<drop target='node' refuse-if='$objects.isEmpty()'/>").get(0);
		assertEquals(List.of("objects", "target", "x", "y"), compile(config, DropSignature.ONTO).names());
		assertEquals(List.of("objects", "target", "x", "y"), ActionScopes.scopeAt(config).names());

		DropConfig undeclared = drops("<drop target='node' refuse-if='$objects.containsSome($pinned)'/>").get(0);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDropArguments.class);
		try {
			DeclaredDrop.compile(context, undeclared, DropSignature.ONTO);
			fail("A refusal function using an input the drop does not declare is not accepted.");
		} catch (ConfigurationError expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("pinned"));
		}
	}

	private static DropRequest request(String object) {
		return DropRequest.at(null, null, List.of(object), new DropLocation.Onto("t"));
	}

	private static boolean isTrue(Object value) {
		return Boolean.TRUE.equals(value);
	}

	private static DeclaredDrop compile(String dropXml, DropSignature signature) throws ConfigurationException {
		return compile(drops(dropXml).get(0), signature);
	}

	private static DeclaredDrop compile(DropConfig config, DropSignature signature) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDropArguments.class);
		DeclaredDrop result = DeclaredDrop.compile(context, config, signature);
		context.checkErrors();
		return result;
	}

	/** The drops of a tree declaring the given drop. */
	private static List<TreeDropConfig> drops(String dropXml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDropArguments.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap("tree", TypedConfiguration.getConfigurationDescriptor(TreeElement.Config.class)));
		reader.setSource(CharacterContents.newContent(
			"<tree root=\"'root'\" children='[]'>" + dropXml + "</tree>"));
		TreeElement.Config tree = (TreeElement.Config) reader.read();
		context.checkErrors();
		return tree.getDrops();
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestDropArguments.class);
	}
}
