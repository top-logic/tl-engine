/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import com.top_logic.layout.react.control.layout.ReactSideRailControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.state.SideRailState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.SideRailElement;
import com.top_logic.util.TLContextManager;

/**
 * Tests {@link SideRailElement}: the sections a {@code <side-rail>} is written with, its channels
 * {@code open-channel} and {@code badge-channel}, the visibility of its sections, and what it
 * remembers per user.
 */
public class TestSideRailElement extends BasicTestCase {

	/** A rail with three sections: one with icon and badge, one collapsed, one with visibility. */
	private static final String FIXTURE = "side-rail.view.xml";

	private static final String COUNT_CHANNEL = "count";

	private static final String PRESENT_CHANNEL = "present";

	private static final String OPEN_CHANNEL = "railOpen";

	private static final String BADGE_CHANNEL = "railBadge";

	private static final String SHOWN_CHANNEL = "railShown";

	private static final String OTHER_CHANNEL = "other";

	private static final String EXTRA = "extra";

	private static final String TASKS = "tasks";

	private static final String SOURCES = "sources";

	private static final String DOCUMENT = "document";

	/** The personalization key of the context the rails are created in. */
	private static final String CONTEXT_KEY = "view";

	/** The personalization key the rail of the fixture remembers its state under. */
	private static final String KEY = CONTEXT_KEY + "." + SideRailElement.SIDE_RAIL_SEGMENT;

	private ViewContext _context;

	private ViewChannel _count;

	private ViewChannel _present;

	private ViewChannel _open;

	private ViewChannel _badge;

	private ViewChannel _shown;

	private ViewChannel _other;

	private Locale _previousLocale;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_previousLocale = TLContextManager.getSubSession().getCurrentLocale();
		TLContextManager.getSubSession().setCurrentLocale(Locale.ENGLISH);

		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_count = register(COUNT_CHANNEL);
		_present = register(PRESENT_CHANNEL);
		_open = register(OPEN_CHANNEL);
		_badge = register(BADGE_CHANNEL);
		_shown = register(SHOWN_CHANNEL);
		_other = register(OTHER_CHANNEL);
		assertEquals(CONTEXT_KEY, _context.getPersonalizationKey());
		PersonalConfiguration.getPersonalConfiguration().setJSONValue(KEY, null);
	}

	@Override
	protected void tearDown() throws Exception {
		PersonalConfiguration.getPersonalConfiguration().setJSONValue(KEY, null);
		_context = null;
		TLContextManager.getSubSession().setCurrentLocale(_previousLocale);
		super.tearDown();
	}

	private ViewChannel register(String name) {
		ViewChannel channel = new DefaultViewChannel(name);
		_context.registerChannel(name, channel);
		return channel;
	}

	/**
	 * The sections reach the client in the order they are written in, with label, icon and
	 * expansion; a section whose visibility condition does not hold is hidden.
	 */
	public void testSectionsReachTheClient() {
		ReactSideRailControl rail = rail();
		Map<?, ?> state = state(rail);

		assertEquals("my-rail", state.get(ControlState.CSS_CLASS__PROP));
		assertEquals("right", state.get(SideRailState.SIDE__PROP));
		assertEquals(Boolean.FALSE, state.get(SideRailState.OPEN__PROP));
		assertEquals(320, ((Number) state.get(SideRailState.WIDTH__PROP)).intValue());

		List<?> sections = (List<?>) state.get(SideRailState.SECTIONS__PROP);
		assertEquals(4, sections.size());
		Map<?, ?> tasks = (Map<?, ?>) sections.get(0);
		assertEquals(TASKS, tasks.get(SideRailState.Section.NAME__PROP));
		assertEquals("Tasks", tasks.get(SideRailState.Section.LABEL__PROP));
		assertEquals("css:fa-solid fa-list-check", tasks.get(SideRailState.Section.ICON__PROP));
		assertEquals(Boolean.TRUE, tasks.get(SideRailState.Section.EXPANDED__PROP));

		Map<?, ?> sources = (Map<?, ?>) sections.get(1);
		assertEquals("Without a label, the name is shown.", SOURCES, sources.get(SideRailState.Section.LABEL__PROP));
		assertFalse(sources.containsKey(SideRailState.Section.ICON__PROP));
		assertEquals(Boolean.FALSE, sources.get(SideRailState.Section.EXPANDED__PROP));

		Map<?, ?> document = (Map<?, ?>) sections.get(2);
		assertEquals("The visibility condition does not hold.", Boolean.TRUE,
			document.get(SideRailState.Section.HIDDEN__PROP));
		assertEquals("A section without header.", Boolean.TRUE, document.get(SideRailState.Section.HEADLESS__PROP));
		assertFalse(tasks.containsKey(SideRailState.Section.HEADLESS__PROP));
	}

	/**
	 * A section appears and disappears with the channel of its visibility condition; while it is
	 * shown, its width is the width of the column.
	 */
	public void testVisibilityFollowsTheChannel() {
		ReactSideRailControl rail = rail();
		rail.setOpen(true);
		assertEquals(0, rail.getSectionWidth());

		_present.set("table");
		assertEquals(Boolean.FALSE, section(rail, 2).get(SideRailState.Section.HIDDEN__PROP));
		assertEquals(560, rail.getSectionWidth());

		_present.set(null);
		assertEquals(Boolean.TRUE, section(rail, 2).get(SideRailState.Section.HIDDEN__PROP));
		assertEquals(0, rail.getSectionWidth());
	}

	/**
	 * The open channel follows the rail, and writing it opens and closes the rail.
	 */
	public void testOpenChannel() {
		ReactSideRailControl rail = rail();
		assertEquals("The channel starts with the state of the rail.", Boolean.FALSE, _open.get());

		rail.executeCommand(ReactSideRailControl.SHOW_SECTION_COMMAND,
			Map.of(ReactSideRailControl.NAME_ARG, SOURCES));
		assertEquals(Boolean.TRUE, _open.get());

		_open.set(Boolean.FALSE);
		assertFalse(rail.isOpen());

		_open.set(Boolean.TRUE);
		assertTrue(rail.isOpen());
	}

	/**
	 * The badge channel holds what the sections count beyond what the user has seen; a badge counts
	 * as seen while its section is shown.
	 */
	public void testBadgeChannel() {
		ReactSideRailControl rail = rail();
		assertEquals(Integer.valueOf(0), _badge.get());

		_count.set(Integer.valueOf(3));
		assertEquals(Integer.valueOf(3), _badge.get());
		assertNotNull("The badge stands on the section.", section(rail, 0).get(SideRailState.Section.BADGE__PROP));

		_open.set(Boolean.TRUE);
		assertEquals("The open rail shows the section.", Integer.valueOf(0), _badge.get());

		_open.set(Boolean.FALSE);
		_count.set(Integer.valueOf(5));
		assertEquals(Integer.valueOf(2), _badge.get());
	}

	/**
	 * A rail that is not on screen, e.g. inside a closed dock, sees nothing although it is open;
	 * coming on screen marks what it shows as seen. Its open state does not change.
	 */
	public void testShownChannel() {
		_shown.set(Boolean.FALSE);
		_count.set(Integer.valueOf(2));
		ReactSideRailControl rail = rail();
		_open.set(Boolean.TRUE);
		assertTrue(rail.isOpen());
		assertEquals("Open, but not on screen: nothing is seen.", Integer.valueOf(2), _badge.get());

		_count.set(Integer.valueOf(4));
		assertEquals(Integer.valueOf(4), _badge.get());

		_shown.set(Boolean.TRUE);
		assertEquals("On screen, the open section is seen.", Integer.valueOf(0), _badge.get());
		assertTrue(rail.isOpen());

		_shown.set(Boolean.FALSE);
		_count.set(Integer.valueOf(5));
		assertEquals(Integer.valueOf(1), _badge.get());
		assertTrue("Leaving the screen does not close the rail.", rail.isOpen());
	}

	/**
	 * A revealing section shows itself for every new value of its inputs for which it is visible,
	 * also after the user closed the rail; this is no choice of the user and is not remembered.
	 */
	public void testReveal() {
		ReactSideRailControl rail = rail();
		assertFalse(rail.isOpen());

		_present.set("table");
		assertTrue("The section appearing opens the rail.", rail.isOpen());
		assertTrue(rail.isExpanded(DOCUMENT));
		assertNull("Revealing is no choice of the user.", stored());

		_open.set(Boolean.FALSE);
		_present.set("chart");
		assertTrue("Something new while the section is visible opens the rail again.", rail.isOpen());

		_open.set(Boolean.FALSE);
		_present.set("chart");
		assertFalse("The same value again shows nothing new.", rail.isOpen());

		_present.set(null);
		assertFalse("A section disappearing opens nothing.", rail.isOpen());
	}

	/**
	 * A section without reveal does not open the rail when it appears.
	 */
	public void testNoRevealWithoutTheFlag() {
		ReactSideRailControl rail = rail();
		_other.set("x");
		assertEquals(Boolean.FALSE, section(rail, 3).get(SideRailState.Section.HIDDEN__PROP));
		assertFalse(rail.isOpen());
		assertFalse(rail.isExpanded(EXTRA));
	}

	/**
	 * The remembered state of the user takes precedence over the configured one.
	 */
	public void testRememberedState() {
		store(Map.of(SideRailElement.OPEN_ENTRY, Boolean.TRUE, SideRailElement.WIDTH_ENTRY, Integer.valueOf(400),
			SideRailElement.SECTIONS_ENTRY, Map.of(TASKS, Boolean.FALSE, SOURCES, Boolean.TRUE)));

		ReactSideRailControl rail = rail();
		assertTrue(rail.isOpen());
		assertFalse(rail.isExpanded(TASKS));
		assertTrue(rail.isExpanded(SOURCES));
		assertTrue("Nothing remembered: configured.", rail.isExpanded(DOCUMENT));
		assertEquals(400, ((Number) state(rail).get(SideRailState.WIDTH__PROP)).intValue());
	}

	/**
	 * The user's choices for the column, the sections and the width are remembered; opening the rail
	 * as a drawer is not.
	 */
	public void testChoicesAreRemembered() {
		ReactSideRailControl rail = rail();

		rail.executeCommand(ReactSideRailControl.SHOW_SECTION_COMMAND,
			Map.of(ReactSideRailControl.NAME_ARG, SOURCES));
		rail.executeCommand(ReactSideRailControl.SET_WIDTH_COMMAND,
			Map.of(ReactSideRailControl.WIDTH_ARG, Integer.valueOf(360)));
		assertEquals(Boolean.TRUE, stored().get(SideRailElement.OPEN_ENTRY));
		assertEquals(Map.of(SOURCES, Boolean.TRUE), stored().get(SideRailElement.SECTIONS_ENTRY));
		assertEquals(360, ((Number) stored().get(SideRailElement.WIDTH_ENTRY)).intValue());

		rail.executeCommand(ReactSideRailControl.SET_DRAWER_COMMAND,
			Map.of(ReactSideRailControl.DRAWER_ARG, Boolean.TRUE));
		assertEquals("The drawer starts closed.", Boolean.FALSE, _open.get());
		_open.set(Boolean.TRUE);
		_open.set(Boolean.FALSE);
		assertEquals("The drawer does not change the column choice.", Boolean.TRUE,
			stored().get(SideRailElement.OPEN_ENTRY));
	}

	private static void store(Map<String, Object> state) {
		PersonalConfiguration.getPersonalConfiguration().setJSONValue(KEY, new HashMap<>(state));
	}

	private static Map<?, ?> stored() {
		return (Map<?, ?>) PersonalConfiguration.getPersonalConfiguration().getJSONValue(KEY);
	}

	private ReactSideRailControl rail() {
		try {
			ChildGroup group = new FixtureViews(TestSideRailElement.class).getView(FIXTURE).getChildGroups().get(0);
			UIElement element = ((ChildGroup.Elements) group).children().get(0);
			ReactSideRailControl result = (ReactSideRailControl) element.createControl(_context);
			result.attach();
			return result;
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a readable view: " + FIXTURE, ex);
		}
	}

	private static Map<?, ?> section(ReactControl rail, int index) {
		return (Map<?, ?>) ((List<?>) state(rail).get(SideRailState.SECTIONS__PROP)).get(index);
	}

	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSideRailElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
