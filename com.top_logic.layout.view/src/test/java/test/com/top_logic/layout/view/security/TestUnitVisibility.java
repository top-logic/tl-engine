/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.accordion.ReactAccordionControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.layout.view.element.UnitVisibility;
import com.top_logic.layout.view.element.VisibilityCondition;
import com.top_logic.layout.view.element.WithVisibility;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.AccessGuardControl;
import com.top_logic.layout.view.security.UnitAccess.Display;
import com.top_logic.layout.view.security.UnitHiding;

/**
 * Tests the visibility condition of UI units: {@link UnitVisibility} and its effect on the
 * {@link UnitHiding} of a unit and on the {@link AccessGuardControl} of its content.
 */
public class TestUnitVisibility extends AbstractModelAccessTest {

	/** The channel the conditions and checks read their object from. */
	private static final String OBJECT_CHANNEL = "object";

	/**
	 * A tab bar with a tab shown for projects and checked on them, a tab whose condition yields the
	 * object itself, and an unconditional tab.
	 */
	private static final String VISIBILITY_TABS = "tabs-visibility.view.xml";

	/** An accordion with a section shown for projects beside an unconditional one. */
	private static final String VISIBILITY_ACCORDION = "accordion-visibility.view.xml";

	/** A tab bar whose selected tab is checked on a project and hidden while denied. */
	private static final String HIDE_TABS = "tabs-hide.view.xml";

	/** The ID of the unit shown for projects in the fixtures. */
	private static final String PROJECT_UNIT = "project";

	/** The ID of the tab whose condition yields the object itself. */
	private static final String VALUE_UNIT = "value";

	/** The ID of the unconditional unit in the fixtures. */
	private static final String OPEN = "open";

	/** The ID of the unit checked on the object in {@link #HIDE_TABS}. */
	private static final String GUARDED = "guarded";

	private ViewChannel _object;

	private ViewContext _context;

	private CollectingLogListener _warnings;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_object = new DefaultViewChannel(OBJECT_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(OBJECT_CHANNEL, _object);
		_warnings = new CollectingLogListener(Set.of(Level.WARN), false);
	}

	@Override
	protected void tearDown() throws Exception {
		_warnings.deactivate();
		_warnings = null;
		_context = null;
		_object = null;
		super.tearDown();
	}

	/**
	 * Tests that a tab is shown while its condition holds and hidden while it does not, following
	 * the input channel.
	 */
	public void testTabFollowsCondition() throws ConfigurationException {
		becomeUser(_responsible);
		_object.set(_project);
		ReactTabBarControl tabs = (ReactTabBarControl) content(VISIBILITY_TABS).createControl(_context);
		tabs.attach();
		assertFalse(tabs.isTabHidden(PROJECT_UNIT));
		assertEquals(PROJECT_UNIT, tabs.getActiveTabId());

		_object.set(_category);
		assertTrue("A category is no project.", tabs.isTabHidden(PROJECT_UNIT));
		assertFalse("A category is an object, so the condition of the value tab holds.", tabs.isTabHidden(VALUE_UNIT));
		assertEquals("The selection moves to the next visible tab.", VALUE_UNIT, tabs.getActiveTabId());

		_object.set(_project);
		assertFalse("The tab is shown again.", tabs.isTabHidden(PROJECT_UNIT));
	}

	/**
	 * Tests that a unit is shown while its condition yields a value TL-Script takes as true.
	 */
	public void testTruthValue() throws ConfigurationException {
		becomeUser(_responsible);
		_object.set(_project);
		ReactTabBarControl tabs = (ReactTabBarControl) content(VISIBILITY_TABS).createControl(_context);
		tabs.attach();
		assertFalse("An object is true.", tabs.isTabHidden(VALUE_UNIT));

		_object.set(Boolean.FALSE);
		assertTrue(tabs.isTabHidden(VALUE_UNIT));

		_object.set(List.of(_project));
		assertFalse("A non-empty collection is true.", tabs.isTabHidden(VALUE_UNIT));

		_object.set(List.of());
		assertTrue("An empty collection is not true.", tabs.isTabHidden(VALUE_UNIT));

		_object.set(null);
		assertTrue("Nothing is not true.", tabs.isTabHidden(VALUE_UNIT));
		assertTrue("Nothing is no project.", tabs.isTabHidden(PROJECT_UNIT));
	}

	/**
	 * Tests that an accordion section is shown while its condition holds.
	 */
	public void testAccordionSectionFollowsCondition() throws ConfigurationException {
		becomeUser(_responsible);
		ReactAccordionControl accordion =
			(ReactAccordionControl) content(VISIBILITY_ACCORDION).createControl(_context);
		accordion.attach();
		assertTrue("Without object, the condition does not hold.", accordion.isSectionHidden(PROJECT_UNIT));
		assertFalse(accordion.isSectionHidden(OPEN));

		_object.set(_project);
		assertFalse(accordion.isSectionHidden(PROJECT_UNIT));

		_object.set(_category);
		assertTrue(accordion.isSectionHidden(PROJECT_UNIT));
	}

	/**
	 * Tests that the state of a unit with a condition is reported to its container whenever it
	 * changes, and only then.
	 */
	public void testHidingReportsChanges() throws ConfigurationException {
		UnitVisibility visibility = visibility(
			"<visibility inputs='object' expr='$object.instanceOf(`" + qualified(PROJECT) + "`)'/>");
		UnitHiding hiding = AccessChecks.hiding(null, UnitVisibility.bind(visibility, _context), _context);
		assertNotNull("A unit with a condition can be hidden.", hiding);
		assertTrue(hiding.isHidden());

		List<Boolean> changes = new ArrayList<>();
		ReactControl container = ContentControls.toControl(List.of(), _context);
		hiding.bind(container, changes::add);
		container.attach();

		_object.set(_project);
		assertEquals(List.of(Boolean.FALSE), changes);
		assertFalse(hiding.isHidden());

		_object.set(newObject(qualified(PROJECT), "other"));
		assertEquals("Still a project, no change.", List.of(Boolean.FALSE), changes);

		_object.set(_category);
		assertEquals(List.of(Boolean.FALSE, Boolean.TRUE), changes);
		assertTrue(hiding.isHidden());
	}

	/**
	 * Tests that a unit without condition and without access check hiding it gets no state.
	 */
	public void testNoHidingWithoutCondition() {
		assertNull(AccessChecks.hiding(null, UnitVisibility.bind(null, _context), _context));
	}

	/**
	 * Tests that the access check of a unit is not consulted while its condition does not hold: an
	 * object of another type is not checked, so nothing is logged about it, and the content of the
	 * unit is not displayed.
	 */
	public void testAccessNotCheckedWhileHidden() throws ConfigurationException {
		becomeUser(_responsible);
		_object.set(_project);
		ReactTabBarControl tabs = (ReactTabBarControl) content(VISIBILITY_TABS).createControl(_context);
		tabs.attach();
		AccessGuardControl guard = guard(tabs);
		assertEquals(Display.CONTENT, guard.getDisplay());

		_warnings.activate();
		_object.set(_category);
		assertEquals("Nothing is logged: " + messages(), 0, _warnings.getLogEntries().size());
		assertTrue(tabs.isTabHidden(PROJECT_UNIT));
		assertEquals("The content of a unit left out is not displayed.", Display.EMPTY, guard.getDisplay());

		_object.set(_project);
		assertEquals(Display.CONTENT, guard.getDisplay());
	}

	/**
	 * Tests that the warning missing in {@link #testAccessNotCheckedWhileHidden()} is logged for a
	 * unit checking the same object without condition, so that its absence there is significant.
	 */
	public void testAccessCheckedWithoutCondition() throws ConfigurationException {
		becomeUser(_responsible);
		_object.set(_project);
		ReactTabBarControl tabs = (ReactTabBarControl) content(HIDE_TABS).createControl(_context);
		tabs.attach();

		_warnings.activate();
		_object.set(_category);
		assertTrue(tabs.isTabHidden(GUARDED));
		assertFalse("The category is checked against the project type.", _warnings.getLogEntries().isEmpty());
	}

	private String messages() {
		return LogEntry.joinMessages(_warnings.getLogEntries(), "; ");
	}

	private static AccessGuardControl guard(ReactControl container) {
		for (ReactControl child : container.visibleChildren()) {
			if (child instanceof AccessGuardControl guard) {
				return guard;
			}
		}
		throw new AssertionError("No guarded content displayed by " + container);
	}

	private static UnitVisibility visibility(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestUnitVisibility.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(WithVisibility.VISIBILITY,
			TypedConfiguration.getConfigurationDescriptor(VisibilityCondition.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		UnitVisibility result = UnitVisibility.create((VisibilityCondition) reader.read());
		context.checkErrors();
		return result;
	}

	private static UIElement content(String viewRef) throws ConfigurationException {
		ViewElement view = new FixtureViews(TestUnitVisibility.class).getView(viewRef);
		ChildGroup group = view.getChildGroups().get(0);
		return ((ChildGroup.Elements) group).children().get(0);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suiteWith(TestUnitVisibility.class, TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE);
	}

}
