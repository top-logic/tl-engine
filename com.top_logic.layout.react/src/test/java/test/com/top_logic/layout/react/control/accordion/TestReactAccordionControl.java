/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.accordion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.control.accordion.AccordionExpansionListener;
import com.top_logic.layout.react.control.accordion.AccordionSection;
import com.top_logic.layout.react.control.accordion.ReactAccordionControl;
import com.top_logic.layout.react.control.accordion.ToggleSectionArguments;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.AccordionState;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests {@link ReactAccordionControl}: the lazy creation of the section contents, their survival
 * when a section is collapsed, the exclusive mode, revealing a section, and the reports to the
 * expansion listeners.
 */
public class TestReactAccordionControl extends TestCase {

	private static final String FIRST = "first";

	private static final String SECOND = "second";

	private static final String THIRD = "third";

	/** The React module of the content of a section. */
	private static final String CONTENT_MODULE = "TestContent";

	private ReactContext _context;

	/** How often the content of each section was created, by section ID. */
	private Map<String, Integer> _created;

	/** The reported expansion changes, as {@code id=expanded}. */
	private List<String> _changes;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_created = new HashMap<>();
		_changes = new ArrayList<>();
	}

	private AccordionSection section(String id) {
		return new AccordionSection(id, id.toUpperCase(), () -> {
			_created.merge(id, 1, Integer::sum);
			return new ReactControl(_context, null, CONTENT_MODULE);
		});
	}

	private ReactAccordionControl accordion(boolean exclusive, AccordionSection... sections) {
		ReactAccordionControl result = new ReactAccordionControl(_context, null, List.of(sections), exclusive);
		result.addExpansionListener((accordion, id, expanded) -> {
			assertSame(result, accordion);
			_changes.add(id + "=" + expanded);
		});
		return result;
	}

	private int created(String id) {
		return _created.getOrDefault(id, 0);
	}

	/**
	 * Tests that only the content of an initially expanded section is created on attach, and that
	 * nothing is created before.
	 */
	public void testInitiallyExpandedContentCreatedOnAttach() {
		ReactAccordionControl accordion = accordion(false, section(FIRST).withExpanded(true), section(SECOND));
		assertEquals("Nothing is created before the accordion is displayed.", 0, created(FIRST));

		accordion.attach();

		assertEquals(1, created(FIRST));
		assertEquals("A collapsed section has no content.", 0, created(SECOND));
		assertNotNull(content(accordion, FIRST));
		assertNull(content(accordion, SECOND));
		assertTrue(accordion.isExpanded(FIRST));
		assertFalse(accordion.isExpanded(SECOND));
		assertEquals("The initial expansion is no change.", List.of(), _changes);
	}

	/**
	 * Tests that a collapsed section's content is created when it is expanded the first time, kept
	 * when it is collapsed, and not created again when it is expanded again.
	 */
	public void testContentCreatedOnceAndKept() {
		ReactAccordionControl accordion = accordion(false, section(FIRST), section(SECOND));
		accordion.attach();
		assertEquals(0, created(FIRST));

		toggle(accordion, FIRST, true);
		assertEquals(1, created(FIRST));
		Map<?, ?> content = content(accordion, FIRST);
		assertNotNull(content);
		assertEquals(Boolean.TRUE, section(accordion, FIRST).get(AccordionState.Section.EXPANDED__PROP));

		toggle(accordion, FIRST, false);
		assertFalse(accordion.isExpanded(FIRST));
		assertEquals(Boolean.FALSE, section(accordion, FIRST).get(AccordionState.Section.EXPANDED__PROP));
		assertEquals("Collapsing keeps the content in the state.",
			content.get(ChildControl.CONTROL_ID__PROP), content(accordion, FIRST).get(ChildControl.CONTROL_ID__PROP));

		toggle(accordion, FIRST, true);
		assertEquals("Expanding again reuses the content.", 1, created(FIRST));
		assertEquals(0, created(SECOND));
		assertEquals(List.of(FIRST + "=true", FIRST + "=false", FIRST + "=true"), _changes);
	}

	/**
	 * Tests that the content created while the accordion is displayed is attached, and that a
	 * collapsed section's content is not visible.
	 */
	public void testContentAttachedAndVisibility() {
		ReactControl actions = new ReactControl(_context, null, CONTENT_MODULE);
		ReactAccordionControl accordion = accordion(false, section(FIRST).withActions(actions), section(SECOND));
		accordion.attach();
		assertTrue("The header actions are displayed.", actions.isAttached());
		assertEquals(List.of(actions), accordion.visibleChildren());

		accordion.setExpanded(SECOND, true);
		ReactControl content = single(accordion.visibleChildren(), actions);
		assertTrue(content.isAttached());
		assertEquals(ScriptingControl.slotSegment(ReactAccordionControl.SECTION_SLOT, SECOND),
			accordion.scriptingChildSlot(content));
		assertEquals(ScriptingControl.slotSegment(ReactAccordionControl.ACTIONS_SLOT, FIRST),
			accordion.scriptingChildSlot(actions));

		accordion.setExpanded(SECOND, false);
		assertTrue("A collapsed section's content stays displayed, only hidden.", content.isAttached());
		assertEquals(List.of(actions), accordion.visibleChildren());

		accordion.cleanupTree();
		assertTrue(content.isDisposed());
		assertTrue(actions.isDisposed());
	}

	/**
	 * Tests that expanding a section of a detached accordion creates its content only when the
	 * accordion is attached.
	 */
	public void testExpandBeforeAttachIsDeferred() {
		ReactAccordionControl accordion = accordion(false, section(FIRST), section(SECOND));
		accordion.setExpanded(SECOND, true);
		assertEquals(0, created(SECOND));
		assertEquals(List.of(SECOND + "=true"), _changes);

		accordion.attach();
		assertEquals(1, created(SECOND));
		assertNotNull(content(accordion, SECOND));
	}

	/**
	 * Tests that expanding a section of an exclusive accordion collapses the expanded one, which is
	 * reported, and that all sections may be collapsed.
	 */
	public void testExclusive() {
		ReactAccordionControl accordion =
			accordion(true, section(FIRST).withExpanded(true), section(SECOND), section(THIRD));
		accordion.attach();
		assertEquals(Boolean.TRUE, state(accordion).get(AccordionState.EXCLUSIVE__PROP));

		toggle(accordion, SECOND, true);
		assertFalse(accordion.isExpanded(FIRST));
		assertTrue(accordion.isExpanded(SECOND));
		assertFalse(accordion.isExpanded(THIRD));
		assertEquals(List.of(FIRST + "=false", SECOND + "=true"), _changes);
		assertNotNull("The collapsed section keeps its content.", content(accordion, FIRST));

		_changes.clear();
		toggle(accordion, SECOND, false);
		assertFalse(accordion.isExpanded(FIRST));
		assertFalse(accordion.isExpanded(SECOND));
		assertEquals(List.of(SECOND + "=false"), _changes);
	}

	/**
	 * Tests that an exclusive accordion expands only the first of several initially expanded
	 * sections, and that a non-exclusive one expands all of them.
	 */
	public void testInitialExpansion() {
		ReactAccordionControl exclusive = accordion(true,
			section(FIRST), section(SECOND).withExpanded(true), section(THIRD).withExpanded(true));
		assertFalse(exclusive.isExpanded(FIRST));
		assertTrue(exclusive.isExpanded(SECOND));
		assertFalse(exclusive.isExpanded(THIRD));

		ReactAccordionControl multi = accordion(false,
			section(FIRST), section(SECOND).withExpanded(true), section(THIRD).withExpanded(true));
		assertTrue(multi.isExpanded(SECOND));
		assertTrue(multi.isExpanded(THIRD));
	}

	/**
	 * Tests that revealing a collapsed section expands it, and that revealing an expanded one
	 * changes nothing.
	 */
	public void testReveal() {
		ReactAccordionControl accordion = accordion(true, section(FIRST).withExpanded(true), section(SECOND));
		accordion.attach();

		accordion.revealChild(SECOND);
		assertTrue(accordion.isExpanded(SECOND));
		assertFalse(accordion.isExpanded(FIRST));
		assertEquals(1, created(SECOND));
		assertEquals(List.of(FIRST + "=false", SECOND + "=true"), _changes);

		_changes.clear();
		accordion.revealChild(SECOND);
		assertEquals("Revealing an expanded section is no change.", List.of(), _changes);
		assertEquals(1, created(SECOND));
	}

	/**
	 * Tests that a section that does not exist is refused, by the API and by the command, without
	 * changing anything.
	 */
	public void testUnknownSection() {
		ReactAccordionControl accordion = accordion(false, section(FIRST));
		accordion.attach();

		try {
			accordion.revealChild("unknown");
			fail("An unknown section must be refused.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		try {
			accordion.isExpanded("unknown");
			fail("An unknown section must be refused.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}

		HandlerResult result = accordion.executeClientCommand(ReactAccordionControl.TOGGLE_SECTION_COMMAND, Map.of(
			ToggleSectionArguments.SECTION_ID, "unknown",
			ToggleSectionArguments.EXPANDED, Boolean.TRUE));
		assertFalse("The command for an unknown section fails.", result.isSuccess());
		assertFalse(accordion.isExpanded(FIRST));
		assertEquals(List.of(), _changes);
		assertEquals(0, created(FIRST));
	}

	/**
	 * Tests that a duplicate section ID is refused.
	 */
	public void testDuplicateSection() {
		try {
			accordion(false, section(FIRST), section(FIRST));
			fail("Duplicate section IDs must be refused.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/**
	 * Tests that setting a section to the expansion it has changes nothing.
	 */
	public void testNoChange() {
		ReactAccordionControl accordion = accordion(false, section(FIRST));
		accordion.attach();
		toggle(accordion, FIRST, false);
		assertEquals(List.of(), _changes);
		assertEquals(0, created(FIRST));
	}

	/**
	 * Tests that a removed expansion listener is no longer informed.
	 */
	public void testRemoveListener() {
		ReactAccordionControl accordion = new ReactAccordionControl(_context, null, List.of(section(FIRST)), false);
		List<String> changes = new ArrayList<>();
		AccordionExpansionListener listener =
			(a, id, expanded) -> changes.add(id);
		accordion.addExpansionListener(listener);
		accordion.setExpanded(FIRST, true);
		accordion.removeExpansionListener(listener);
		accordion.setExpanded(FIRST, false);
		assertEquals(List.of(FIRST), changes);
	}

	private static void toggle(ReactAccordionControl accordion, String id, boolean expanded) {
		HandlerResult result = accordion.executeClientCommand(ReactAccordionControl.TOGGLE_SECTION_COMMAND, Map.of(
			ToggleSectionArguments.SECTION_ID, id,
			ToggleSectionArguments.EXPANDED, Boolean.valueOf(expanded)));
		assertTrue(result.isSuccess());
	}

	private static ReactControl single(List<ReactControl> controls, ReactControl except) {
		List<ReactControl> rest = new ArrayList<>(controls);
		rest.remove(except);
		assertEquals(1, rest.size());
		return rest.get(0);
	}

	private static Map<?, ?> content(ReactAccordionControl accordion, String id) {
		return (Map<?, ?>) section(accordion, id).get(AccordionState.Section.CONTENT__PROP);
	}

	private static Map<?, ?> section(ReactAccordionControl accordion, String id) {
		for (Object section : (List<?>) state(accordion).get(AccordionState.SECTIONS__PROP)) {
			Map<?, ?> map = (Map<?, ?>) section;
			if (id.equals(map.get(AccordionState.Section.ID__PROP))) {
				return map;
			}
		}
		throw new AssertionError("No section " + id);
	}

	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * The suite, with the services the command dispatch needs.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactAccordionControl.class, ResourcesModule.Module.INSTANCE));
	}

}
