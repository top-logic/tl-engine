/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.control.accordion.ReactAccordionControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.AccordionState;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.AccordionElement;
import com.top_logic.layout.view.security.SecurityScopeService;

/**
 * Tests {@link AccordionElement}: the sections an {@code <accordion>} is written with, what they
 * report to the client, and how the accordion remembers which of them are expanded.
 */
public class TestAccordionElement extends BasicTestCase {

	/** An accordion with a labeled section carrying an icon and a command, and a plain one. */
	private static final String SECTIONS_FIXTURE = "accordion-sections.view.xml";

	/** An exclusive accordion that remembers nothing, with two sections written as expanded. */
	private static final String EXCLUSIVE_FIXTURE = "accordion-exclusive.view.xml";

	/** An accordion with an explicit personalization key. */
	private static final String KEYED_FIXTURE = "accordion-keyed.view.xml";

	/** An accordion whose first section is guarded by a scope the session has no role on. */
	private static final String DENIED_FIXTURE = "accordion-denied.view.xml";

	/** The channel the form in {@link #SECTIONS_FIXTURE} displays. */
	private static final String ITEM_CHANNEL = "item";

	/** The personalization key {@link #KEYED_FIXTURE} is written with. */
	private static final String EXPLICIT_KEY = "test.accordion.explicit";

	/** The personalization key of the context the accordions are created in. */
	private static final String CONTEXT_KEY = "view";

	/** The personalization key an accordion without explicit key remembers its expansion under. */
	private static final String DEFAULT_KEY = CONTEXT_KEY + "." + AccordionElement.ACCORDION_SEGMENT;

	private ViewContext _context;

	private FixtureViews _views;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_views = new FixtureViews(TestAccordionElement.class);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(ITEM_CHANNEL, new DefaultViewChannel(ITEM_CHANNEL));
		assertEquals(CONTEXT_KEY, _context.getPersonalizationKey());
		clearPersonalization();
	}

	@Override
	protected void tearDown() throws Exception {
		clearPersonalization();
		_context = null;
		_views = null;

		super.tearDown();
	}

	private static void clearPersonalization() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setJSONValue(DEFAULT_KEY, null);
		pc.setJSONValue(EXPLICIT_KEY, null);
	}

	/**
	 * Tests that the sections reach the client in the order they are written in, with label, icon
	 * and configured expansion.
	 */
	public void testSectionsReachTheClient() {
		ReactAccordionControl accordion = accordion(SECTIONS_FIXTURE);
		Map<?, ?> state = state(accordion);
		List<Map<String, Object>> sections = sections(accordion);

		assertEquals("Two sections are written: " + sections, 2, sections.size());
		assertEquals(Boolean.FALSE, state.get(AccordionState.EXCLUSIVE__PROP));
		assertEquals("my-accordion", state.get(ControlState.CSS_CLASS__PROP));
		assertFalse(accordion.isExclusive());

		Map<String, Object> general = sections.get(0);
		assertEquals("general", general.get(AccordionState.Section.ID__PROP));
		assertEquals("General", general.get(AccordionState.Section.LABEL__PROP));
		assertEquals("css:bi bi-gear", general.get(AccordionState.Section.ICON__PROP));
		assertEquals(Boolean.TRUE, general.get(AccordionState.Section.EXPANDED__PROP));

		Map<String, Object> advanced = sections.get(1);
		assertEquals("advanced", advanced.get(AccordionState.Section.ID__PROP));
		assertFalse("No icon is written: " + advanced, advanced.containsKey(AccordionState.Section.ICON__PROP));
		assertEquals(Boolean.FALSE, advanced.get(AccordionState.Section.EXPANDED__PROP));
	}

	/**
	 * Tests that the header of a section shows its toolbar commands followed by its button-bar
	 * commands, since a section has no button bar of its own.
	 */
	public void testCommandsBecomeHeaderActions() {
		ReactAccordionControl accordion = accordion(SECTIONS_FIXTURE);

		List<Map<String, Object>> groups = groups(actions(accordion, "general"));
		assertEquals("One group per placement: " + groups, 2, groups.size());
		assertEquals("The toolbar command comes first: " + groups, 1, items(groups.get(0)).size());
		assertEquals("The button-bar command follows: " + groups, 1, items(groups.get(1)).size());
		assertFalse("Groups of one toolbar are told apart by name: " + groups,
			groups.get(0).get(ReactToolbarControl.GROUP_NAME).equals(groups.get(1).get(ReactToolbarControl.GROUP_NAME)));
	}

	/**
	 * Tests that a section without commands of its own is still the command scope of its content:
	 * its header toolbar is empty until the content, created on first expansion, contributes the
	 * edit commands of its form.
	 */
	public void testContentContributesToSectionHeader() {
		ReactAccordionControl accordion = accordion(SECTIONS_FIXTURE);
		ReactToolbarControl actions = actions(accordion, "advanced");

		assertEquals("No commands before the content exists.", List.of(), groups(actions));

		accordion.setExpanded("advanced", true);

		assertSame("The toolbar is kept and only its groups are replaced.", actions, actions(accordion, "advanced"));
		assertFalse("The form's commands show up in the section header.", groups(actions).isEmpty());
	}

	/**
	 * Tests that an exclusive accordion expands only the first of the sections written as expanded.
	 */
	public void testExclusive() {
		ReactAccordionControl accordion = accordion(EXCLUSIVE_FIXTURE);

		assertTrue(accordion.isExclusive());
		assertTrue(accordion.isExpanded("first"));
		assertFalse(accordion.isExpanded("second"));
	}

	/**
	 * Tests that a section the session may not see is left out, rather than shown empty.
	 */
	public void testGuardedSectionIsOmitted() {
		List<Map<String, Object>> sections = sections(accordion(DENIED_FIXTURE));

		assertEquals("Only the unguarded section is offered: " + sections, 1, sections.size());
		assertEquals("open", sections.get(0).get(AccordionState.Section.ID__PROP));
	}

	/**
	 * Tests that the remembered expansion of a section overrides its configured expansion, and that
	 * a section without a remembered entry keeps its configured one.
	 */
	public void testRememberedExpansionOverridesConfiguration() {
		store(DEFAULT_KEY, Map.of("general", Boolean.FALSE, "advanced", Boolean.TRUE));

		ReactAccordionControl accordion = accordion(SECTIONS_FIXTURE);

		assertFalse("Remembered as collapsed.", accordion.isExpanded("general"));
		assertTrue("Remembered as expanded.", accordion.isExpanded("advanced"));
	}

	/**
	 * Tests that a section without a remembered entry keeps its configured expansion.
	 */
	public void testConfiguredExpansionWithoutRememberedEntry() {
		store(DEFAULT_KEY, Map.of("advanced", Boolean.TRUE));

		ReactAccordionControl accordion = accordion(SECTIONS_FIXTURE);

		assertTrue("Nothing remembered: configured expansion.", accordion.isExpanded("general"));
		assertTrue("Remembered as expanded.", accordion.isExpanded("advanced"));
	}

	/**
	 * Tests that expanding or collapsing a section remembers only that section's expansion and
	 * keeps the entries of sections the accordion does not know (those of another accordion sharing
	 * the key).
	 */
	public void testToggleWritesOnlyItsOwnEntry() {
		store(DEFAULT_KEY, Map.of("foreign", Boolean.TRUE));

		ReactAccordionControl accordion = accordion(SECTIONS_FIXTURE);
		assertEquals("Creating the accordion remembers nothing.", Map.of("foreign", Boolean.TRUE),
			stored(DEFAULT_KEY));

		accordion.setExpanded("advanced", true);
		assertEquals(Map.of("foreign", Boolean.TRUE, "advanced", Boolean.TRUE), stored(DEFAULT_KEY));

		accordion.setExpanded("general", false);
		assertEquals(Map.of("foreign", Boolean.TRUE, "advanced", Boolean.TRUE, "general", Boolean.FALSE),
			stored(DEFAULT_KEY));
	}

	/**
	 * Tests that an accordion with {@code personalize="false"} neither reads nor writes a remembered
	 * expansion.
	 */
	public void testUnpersonalizedNeitherReadsNorWrites() {
		store(DEFAULT_KEY, Map.of("first", Boolean.FALSE, "second", Boolean.TRUE));

		ReactAccordionControl accordion = accordion(EXCLUSIVE_FIXTURE);
		assertTrue("The remembered expansion is ignored.", accordion.isExpanded("first"));
		assertFalse("The remembered expansion is ignored.", accordion.isExpanded("second"));

		accordion.setExpanded("second", true);
		assertEquals("Nothing is written.", Map.of("first", Boolean.FALSE, "second", Boolean.TRUE),
			stored(DEFAULT_KEY));
	}

	/**
	 * Tests that an accordion with an explicit personalization key reads and writes its expansion
	 * under that key, not under the one derived from its context.
	 */
	public void testExplicitPersonalizationKey() {
		store(EXPLICIT_KEY, Map.of("first", Boolean.TRUE, "second", Boolean.FALSE));
		store(DEFAULT_KEY, Map.of("first", Boolean.FALSE, "second", Boolean.TRUE));

		ReactAccordionControl accordion = accordion(KEYED_FIXTURE);
		assertTrue(accordion.isExpanded("first"));
		assertFalse(accordion.isExpanded("second"));

		accordion.setExpanded("first", false);
		assertEquals(Map.of("first", Boolean.FALSE, "second", Boolean.FALSE), stored(EXPLICIT_KEY));
		assertEquals("The context-derived key is untouched.",
			Map.of("first", Boolean.FALSE, "second", Boolean.TRUE), stored(DEFAULT_KEY));
	}

	/**
	 * Tests that an exclusive accordion's automatic collapse is remembered as well.
	 */
	public void testExclusiveCollapseIsRemembered() {
		ReactAccordionControl accordion = accordion(KEYED_FIXTURE);
		assertTrue(accordion.isExpanded("second"));

		accordion.setExpanded("first", true);
		assertEquals(Map.of("first", Boolean.TRUE), stored(EXPLICIT_KEY));
	}

	private static void store(String key, Map<String, Boolean> expansion) {
		PersonalConfiguration.getPersonalConfiguration().setJSONValue(key, new HashMap<>(expansion));
	}

	private static Map<?, ?> stored(String key) {
		return (Map<?, ?>) PersonalConfiguration.getPersonalConfiguration().getJSONValue(key);
	}

	/**
	 * The attached accordion control of the given view, as a session displays it.
	 */
	private ReactAccordionControl accordion(String viewRef) {
		AccordionElement element = accordionElement(view(viewRef));
		ReactAccordionControl result = (ReactAccordionControl) element.createControl(_context);
		result.attach();
		return result;
	}

	private ViewElement view(String viewRef) {
		try {
			return _views.getView(viewRef);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a readable view: " + viewRef, ex);
		}
	}

	private static AccordionElement accordionElement(ViewElement view) {
		ChildGroup group = view.getChildGroups().get(0);
		UIElement content = ((ChildGroup.Elements) group).children().get(0);
		return (AccordionElement) content;
	}

	/**
	 * The header toolbar of the section with the given ID.
	 */
	private static ReactToolbarControl actions(ReactAccordionControl accordion, String sectionId) {
		String slot = ScriptingControl.slotSegment(ReactAccordionControl.ACTIONS_SLOT, sectionId);
		for (ReactControl child : accordion.visibleChildren()) {
			if (slot.equals(accordion.scriptingChildSlot(child))) {
				return (ReactToolbarControl) child;
			}
		}
		throw new AssertionError("Section '" + sectionId + "' has no header actions.");
	}

	private static List<Map<String, Object>> groups(ReactToolbarControl toolbar) {
		try {
			Map<?, ?> state = (Map<?, ?>) JSON.fromString(toolbar.stateAsJSON());
			return maps((List<?>) state.get(ReactToolbarControl.GROUPS));
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static List<?> items(Map<String, Object> group) {
		return (List<?>) group.get(ReactToolbarControl.GROUP_ITEMS);
	}

	private static List<Map<String, Object>> maps(List<?> values) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (values == null) {
			return result;
		}
		for (Object value : values) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;
			result.add(map);
		}
		return result;
	}

	private static List<Map<String, Object>> sections(ReactAccordionControl accordion) {
		return maps((List<?>) state(accordion).get(AccordionState.SECTIONS__PROP));
	}

	private static Map<?, ?> state(ReactAccordionControl accordion) {
		try {
			return (Map<?, ?>) JSON.fromString(accordion.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The guarded section asks the {@link SecurityScopeService} whether the session may
	 *           see it, and that service materializes its scopes in the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAccordionElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SecurityScopeService.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
