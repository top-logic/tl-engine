/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.ContentSectionConfig;
import com.top_logic.layout.view.element.SplitPanelElement;
import com.top_logic.layout.view.element.SplitPanelElement.PaneConfig;
import com.top_logic.layout.view.element.StackElement;
import com.top_logic.layout.view.element.SwitchElement;
import com.top_logic.layout.view.element.TabBarElement;
import com.top_logic.layout.view.element.TextElement;

/**
 * Tests that a same-path overlay of a view addresses an element by its id with a referencing
 * element ({@code config:ref}), descends into the element lists of the view by the ids of their
 * elements, and reaches an anonymous element by its position.
 *
 * <p>
 * The views are folded the way the application reads a view several modules provide, by
 * {@link ViewLoader#parseConfig(List)}.
 * </p>
 */
public class TestViewOverlayKeyedLists extends TestCase {

	/**
	 * A split panel whose detail pane switches on the kind of the selection. The case for devices
	 * shows an anonymous stack of an anonymous text and a tab bar.
	 */
	private static final String BASE = """
			<view>
				<channels>
					<channel name="selection"/>
				</channels>
				<split-panel id="main">
					<panes>
						<pane id="list">
							<text>
								<label><en>List</en></label>
							</text>
						</pane>
						<pane id="detail">
							<switch id="kind" input="selection">
								<case id="device" test="x -> true">
									<stack>
										<text>
											<label><en>Device</en></label>
										</text>
										<tab-bar id="tabs">
											<tab id="general">
												<label><en>General</en></label>
												<text>
													<label><en>General content</en></label>
												</text>
											</tab>
										</tab-bar>
									</stack>
								</case>
								<default>
									<text>
										<label><en>Nothing selected</en></label>
									</text>
								</default>
							</switch>
						</pane>
					</panes>
				</split-panel>
			</view>
			""";

	/** The base with an id on the stack of the device case, and a second anonymous text in it. */
	private static final String BASE_NAMED_STACK = """
			<view>
				<channels>
					<channel name="selection"/>
				</channels>
				<split-panel id="main">
					<panes>
						<pane id="detail">
							<switch id="kind" input="selection">
								<case id="device" test="x -> true">
									<stack id="header">
										<text>
											<label><en>Device</en></label>
										</text>
										<tab-bar id="tabs">
											<tab id="general">
												<label><en>General</en></label>
											</tab>
										</tab-bar>
										<text>
											<label><en>Footer</en></label>
										</text>
									</stack>
								</case>
							</switch>
						</pane>
					</panes>
				</split-panel>
			</view>
			""";

	/**
	 * Adds a tab to the tab bar of the device case: the path names every element by its id, the
	 * anonymous stack by its position.
	 */
	private static final String ADD_TAB = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<split-panel id="main">
					<panes>
						<pane id="detail">
							<switch id="kind">
								<case id="device">
									<stack config:index="0">
										<tab-bar id="tabs">
											<tab id="virtual">
												<label><en>Virtual</en></label>
											</tab>
										</tab-bar>
									</stack>
								</case>
							</switch>
						</pane>
					</panes>
				</split-panel>
			</view>
			""";

	/** The device case of the base and the tab added by {@link #ADD_TAB}. */
	public void testAddTabByIdPath() throws Exception {
		ViewElement.Config view = view(BASE, ADD_TAB);
		SplitPanelElement.Config split = split(view);

		assertEquals("The overlay adds no pane.", List.of("list", "detail"), paneIds(split));
		SwitchElement.Config switchConfig = switchOf(pane(split, "detail"));
		assertEquals("The overlay adds no case.", 1, switchConfig.getCases().size());
		assertEquals("The default content is the one of the base.", 1, switchConfig.getDefault().size());

		List<UIElement.Config> content = switchConfig.getCases().get(0).getContent();
		assertEquals("The anonymous stack is updated, not duplicated.", 1, content.size());
		StackElement.Config stack = (StackElement.Config) content.get(0);
		assertEquals("The anonymous text stays in front of the tab bar.", 2, stack.getChildren().size());
		assertEquals(text("Device"), ((TextElement.Config) stack.getChildren().get(0)).getLabel());

		TabBarElement.Config tabBar = (TabBarElement.Config) stack.getChildren().get(1);
		assertEquals(List.of("general", "virtual"), tabIds(tabBar));
		assertEquals(text("Virtual"), tabBar.getTabs().get(1).getLabel());
		assertEquals("The content of the base's tab is kept.", 1, tabBar.getTabs().get(0).getChildren().size());
	}

	/**
	 * An element placed before an element of a list named by its id.
	 */
	public void testPositionBeforeById() throws Exception {
		ViewElement.Config view = view(BASE_NAMED_STACK, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<split-panel id="main">
						<panes>
							<pane id="detail">
								<switch id="kind">
									<case id="device">
										<stack id="header">
											<text id="note" config:position="before" config:reference="tabs">
												<label><en>Note</en></label>
											</text>
										</stack>
									</case>
								</switch>
							</pane>
						</panes>
					</split-panel>
				</view>
				""");
		StackElement.Config stack = deviceStack(view);
		assertEquals(List.of("", "note", "tabs", ""), elementIds(stack.getChildren()));
		assertEquals(text("Note"), ((TextElement.Config) stack.getChildren().get(1)).getLabel());
	}

	/**
	 * An element marked as override replaces the element of the same id.
	 */
	public void testOverrideReplacesTab() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<split-panel id="main">
						<panes>
							<pane id="detail">
								<switch id="kind">
									<case id="device">
										<stack config:index="0">
											<tab-bar id="tabs">
												<tab id="general" config:override="true">
													<label><en>Replaced</en></label>
												</tab>
											</tab-bar>
										</stack>
									</case>
								</switch>
							</pane>
						</panes>
					</split-panel>
				</view>
				""");
		TabBarElement.Config tabBar = (TabBarElement.Config) deviceStack(view).getChildren().get(1);
		assertEquals(List.of("general"), tabIds(tabBar));
		ContentSectionConfig tab = tabBar.getTabs().get(0);
		assertEquals(text("Replaced"), tab.getLabel());
		assertEquals("The replacement has no content of the base.", 0, tab.getChildren().size());
	}

	/**
	 * An anonymous element addressed by its position is updated.
	 */
	public void testIndexAddressesAnonymousElement() throws Exception {
		ViewElement.Config view = view(BASE_NAMED_STACK, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<split-panel id="main">
						<panes>
							<pane id="detail">
								<switch id="kind">
									<case id="device">
										<stack id="header">
											<text config:index="2">
												<label><en>Changed footer</en></label>
											</text>
										</stack>
									</case>
								</switch>
							</pane>
						</panes>
					</split-panel>
				</view>
				""");
		StackElement.Config stack = deviceStack(view);
		assertEquals(List.of("", "tabs", ""), elementIds(stack.getChildren()));
		assertEquals(text("Device"), ((TextElement.Config) stack.getChildren().get(0)).getLabel());
		assertEquals(text("Changed footer"), ((TextElement.Config) stack.getChildren().get(2)).getLabel());
	}

	/**
	 * A pane with an id the base does not have is appended: an unknown id is a new entry, not an
	 * error.
	 */
	public void testUnknownPaneIdAppends() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view>
					<split-panel id="main">
						<panes>
							<pane id="extra">
								<text/>
							</pane>
						</panes>
					</split-panel>
				</view>
				""");
		assertEquals(List.of("list", "detail", "extra"), paneIds(split(view)));
	}

	/**
	 * An update of an id the base does not have fails.
	 */
	public void testUpdateOfUnknownIdFails() {
		try {
			view(BASE, """
					<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
						<split-panel id="main">
							<panes>
								<pane id="no-such-pane" config:operation="update">
									<text/>
								</pane>
							</panes>
						</split-panel>
					</view>
					""");
			fail("An update of an entry the base does not have must fail.");
		} catch (ConfigurationException ex) {
			// Expected: no entry to update.
		}
	}

	/**
	 * Two panes with the same id in one source are rejected.
	 */
	public void testDuplicateIdFails() {
		try {
			view("""
					<view>
						<split-panel>
							<panes>
								<pane id="same"><text/></pane>
								<pane id="same"><text/></pane>
							</panes>
						</split-panel>
					</view>
					""");
			fail("Two panes with the same id must be rejected.");
		} catch (ConfigurationException ex) {
			// Expected: duplicate key.
		}
	}

	/**
	 * Two elements with the same id in one list are rejected, anonymous elements of the same kind
	 * are not.
	 */
	public void testDuplicateElementIdFails() throws ConfigurationException {
		view("""
				<view>
					<stack>
						<text/>
						<text/>
					</stack>
				</view>
				""");
		try {
			view("""
					<view>
						<stack>
							<text id="same"/>
							<text id="same"/>
						</stack>
					</view>
					""");
			fail("Two elements with the same id must be rejected.");
		} catch (ConfigurationException ex) {
			// Expected: duplicate key.
		}
	}

	/**
	 * Adds a tab to the tab bar of the device case with a referencing element: the tab bar is
	 * addressed by its id, the path to it is not written.
	 */
	private static final String ADD_TAB_BY_REFERENCE = """
			<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<tab-bar config:ref="tabs">
					<tab id="virtual">
						<label><en>Virtual</en></label>
					</tab>
				</tab-bar>
			</view>
			""";

	/** The device case of the base and the tab added by {@link #ADD_TAB_BY_REFERENCE}. */
	public void testAddTabByReference() throws Exception {
		ViewElement.Config view = view(BASE, ADD_TAB_BY_REFERENCE);
		assertTabAdded(view);
	}

	/** A referencing element merges exactly like the path to the element it addresses. */
	public void testReferenceMergesLikePath() throws Exception {
		assertTrue("The reference and the path produce the same view.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(view(BASE, ADD_TAB), view(BASE, ADD_TAB_BY_REFERENCE)));
	}

	/**
	 * A referencing element nested in another one: the inner one addresses an element below the
	 * element the outer one addresses, the elements between them are not written.
	 */
	public void testNestedReferences() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<switch config:ref="kind">
						<tab-bar config:ref="tabs">
							<tab id="virtual">
								<label><en>Virtual</en></label>
							</tab>
						</tab-bar>
					</switch>
				</view>
				""");
		assertTabAdded(view);
	}

	/** A referencing element written in an element reached by its path. */
	public void testReferenceInsideElementOnThePath() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<split-panel id="main">
						<panes>
							<pane id="detail">
								<tab-bar config:ref="tabs">
									<tab id="virtual">
										<label><en>Virtual</en></label>
									</tab>
								</tab-bar>
							</pane>
						</panes>
					</split-panel>
				</view>
				""");
		assertTabAdded(view);
	}

	/** A referencing element marked as override replaces the element it addresses, which keeps its id. */
	public void testReferenceOverridesTab() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<tab config:ref="general" config:override="true">
						<label><en>Replaced</en></label>
					</tab>
				</view>
				""");
		TabBarElement.Config tabBar = (TabBarElement.Config) deviceStack(view).getChildren().get(1);
		assertEquals(List.of("general"), tabIds(tabBar));
		ContentSectionConfig tab = tabBar.getTabs().get(0);
		assertEquals(text("Replaced"), tab.getLabel());
		assertEquals("The replacement has no content of the base.", 0, tab.getChildren().size());
	}

	/** A referencing element removes the element it addresses. */
	public void testReferenceRemovesCase() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<case config:ref="device" config:operation="remove"/>
				</view>
				""");
		SwitchElement.Config switchConfig = switchOf(pane(split(view), "detail"));
		assertEquals(0, switchConfig.getCases().size());
		assertEquals("The default content stays.", 1, switchConfig.getDefault().size());
	}

	/** A referencing element moves the element it addresses within its list. */
	public void testReferenceMovesPane() throws Exception {
		ViewElement.Config view = view(BASE, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<pane config:ref="detail" config:position="begin"/>
				</view>
				""");
		assertEquals(List.of("detail", "list"), paneIds(split(view)));
	}

	/** A referencing element addresses only elements below the element it is written in. */
	public void testReferenceNotBelowEnclosingFails() {
		try {
			view(BASE, """
					<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
						<pane config:ref="list">
							<tab-bar config:ref="tabs">
								<tab id="virtual">
									<label><en>Virtual</en></label>
								</tab>
							</tab-bar>
						</pane>
					</view>
					""");
			fail("A reference to an element outside the enclosing element must fail.");
		} catch (ConfigurationException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("No entry with the key 'tabs'"));
		}
	}

	/** An id names one thing in a view: two elements of different lists with the same id are rejected. */
	public void testDuplicateIdAcrossListsFails() {
		try {
			view("""
					<view>
						<stack>
							<text id="same"/>
							<tab-bar id="bar">
								<tab id="same">
									<label><en>Same</en></label>
								</tab>
							</tab-bar>
						</stack>
					</view>
					""");
			fail("Two parts with the same id must be rejected.");
		} catch (ConfigurationException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Duplicate value 'same'"));
		}
	}

	private static void assertTabAdded(ViewElement.Config view) {
		SplitPanelElement.Config split = split(view);
		assertEquals("The overlay adds no pane.", List.of("list", "detail"), paneIds(split));
		SwitchElement.Config switchConfig = switchOf(pane(split, "detail"));
		assertEquals("The overlay adds no case.", 1, switchConfig.getCases().size());
		assertEquals("The default content is the one of the base.", 1, switchConfig.getDefault().size());

		List<UIElement.Config> content = switchConfig.getCases().get(0).getContent();
		assertEquals("The anonymous stack is updated, not duplicated.", 1, content.size());
		StackElement.Config stack = (StackElement.Config) content.get(0);
		assertEquals("The anonymous text stays in front of the tab bar.", 2, stack.getChildren().size());

		TabBarElement.Config tabBar = (TabBarElement.Config) stack.getChildren().get(1);
		assertEquals(List.of("general", "virtual"), tabIds(tabBar));
		assertEquals(text("Virtual"), tabBar.getTabs().get(1).getLabel());
		assertEquals("The content of the base's tab is kept.", 1, tabBar.getTabs().get(0).getChildren().size());
	}

	/**
	 * The merged view written and read again is the merged view, ids included.
	 */
	public void testMergedViewWritesIds() throws Exception {
		ViewElement.Config merged = view(BASE, ADD_TAB);

		StringWriter out = new StringWriter();
		try (ConfigurationWriter writer = new ConfigurationWriter(out)) {
			writer.write("view", ViewElement.Config.class, merged);
		}
		String xml = out.toString();
		assertTrue(xml, xml.contains("id=\"virtual\""));
		assertTrue(xml, xml.contains("id=\"detail\""));
		assertTrue(xml, xml.contains("id=\"device\""));

		ViewElement.Config reread = view(xml);
		assertTrue("The written view reads as the merged one.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(merged, reread));
	}

	private static ViewElement.Config view(String... sources) throws ConfigurationException {
		List<CharacterContent> contents = new ArrayList<>();
		for (int n = 0; n < sources.length; n++) {
			contents.add(CharacterContents.newContent(sources[n], "test-keyed-" + n + ".view.xml"));
		}
		return ViewLoader.parseConfig(contents);
	}

	private static SplitPanelElement.Config split(ViewElement.Config view) {
		assertTrue("The view shows a split panel.", view.getContent() instanceof SplitPanelElement.Config);
		return (SplitPanelElement.Config) view.getContent();
	}

	private static PaneConfig pane(SplitPanelElement.Config split, String id) {
		for (PaneConfig pane : split.getPanes()) {
			if (id.equals(pane.getId())) {
				return pane;
			}
		}
		fail("No pane '" + id + "'.");
		return null;
	}

	private static SwitchElement.Config switchOf(PaneConfig pane) {
		assertEquals(1, pane.getChildren().size());
		return (SwitchElement.Config) pane.getChildren().get(0);
	}

	private static StackElement.Config deviceStack(ViewElement.Config view) {
		List<UIElement.Config> content = switchOf(pane(split(view), "detail")).getCases().get(0).getContent();
		assertEquals(1, content.size());
		return (StackElement.Config) content.get(0);
	}

	private static List<String> paneIds(SplitPanelElement.Config split) {
		return split.getPanes().stream().map(PaneConfig::getId).collect(Collectors.toList());
	}

	private static List<String> tabIds(TabBarElement.Config tabBar) {
		return tabBar.getTabs().stream().map(ContentSectionConfig::getId).collect(Collectors.toList());
	}

	private static List<String> elementIds(List<UIElement.Config> elements) {
		return elements.stream().map(element -> nonNull(element.getId())).collect(Collectors.toList());
	}

	private static String nonNull(String value) {
		return value == null ? "" : value;
	}

	private static ResKey text(String english) {
		return ResKey.builder().add(Locale.ENGLISH, english).build();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * and the {@link ResourcesModule}, which writes the labels of the view.
	 */
	public static Test suite() throws ModuleException {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestViewOverlayKeyedLists.class,
			TypeIndex.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
