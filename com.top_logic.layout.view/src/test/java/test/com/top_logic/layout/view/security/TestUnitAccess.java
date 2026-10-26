/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.accordion.ReactAccordionControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.AccordionState;
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
import com.top_logic.layout.view.element.SidebarElement;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.AccessControl;
import com.top_logic.layout.view.security.AccessGuardControl;
import com.top_logic.layout.view.security.SecuredObject;
import com.top_logic.layout.view.security.UnitAccess;
import com.top_logic.layout.view.security.UnitAccess.Display;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the access checks of UI units: {@link UnitAccess}, {@link AccessChecks} and the
 * {@link AccessGuardControl} displaying the content of a unit as the check decides.
 */
public class TestUnitAccess extends AbstractModelAccessTest {

	/** The channel the checks read their object from. */
	private static final String OBJECT_CHANNEL = "object";

	/** The built-in operation reading an object. */
	private static final String READ = SimpleBoundCommandGroup.READ.getID();

	/** The built-in operation deleting an object. */
	private static final String DELETE = SimpleBoundCommandGroup.DELETE.getID();

	/** The wire name of the items of a sidebar. */
	private static final String ITEMS = "items";

	/** The wire name of the id of a sidebar item. */
	private static final String ID = "id";

	/** The operation declared for the security root by the engine. */
	private static final String ADMINISTRATION = "Administration";

	/** An accordion whose first section is checked on the security root. */
	private static final String DENIED_ACCORDION = "accordion-denied.view.xml";

	/** A sidebar whose group is checked on the security root. */
	private static final String DENIED_SIDEBAR = "sidebar-denied-group.view.xml";

	private ViewChannel _object;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_object = new DefaultViewChannel(OBJECT_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(OBJECT_CHANNEL, _object);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_object = null;
		super.tearDown();
	}

	/**
	 * Tests that a unit over an object is shown empty while there is no object.
	 */
	public void testNoObjectShowsEmpty() throws ConfigurationException {
		becomeUser(_responsible);
		UnitAccess access = access(onProject(READ));

		assertTrue(access.isOffered());
		assertEquals(Display.EMPTY, decide(access));

		_object.set(Collections.emptyList());
		assertEquals("An empty collection is no object.", Display.EMPTY, decide(access));
	}

	/**
	 * Tests that a unit shows its content when the operation is allowed on its object, and the
	 * placeholder when it is refused.
	 */
	public void testSingleObject() throws ConfigurationException {
		UnitAccess access = access(onProject(FINISH));
		_object.set(_project);

		becomeUser(_responsible);
		assertTrue(access.isOffered());
		assertEquals(Display.CONTENT, decide(access));

		becomeUser(_roleless);
		assertTrue("Some role may finish a project, so the unit is offered.", access.isOffered());
		assertEquals(Display.DENIED, decide(access));
	}

	/**
	 * Tests that a collection is allowed only if every element is.
	 */
	public void testCollection() throws ConfigurationException {
		TLObject other = newObject(qualified(PROJECT), "other");
		UnitAccess access = access(onProject(READ));
		becomeUser(_responsible);

		_object.set(List.of(_project));
		assertEquals(Display.CONTENT, decide(access));

		_object.set(List.of(_project, other));
		assertEquals("The responsible may not read the other project.", Display.DENIED, decide(access));
	}

	/**
	 * Tests that a unit is hidden when no role can hold its operation on the type.
	 */
	public void testNotGrantableIsHidden() throws ConfigurationException {
		UnitAccess access = access(
			"<access-control operation='" + DELETE + "' inputs='object' object='$object' type='" + qualified(CATEGORY)
				+ "'/>");
		becomeUser(_responsible);
		assertFalse("No role may delete a category.", access.isOffered());
	}

	/**
	 * Tests that an object that is no instance of the declared type is denied.
	 */
	public void testTypeMismatchIsDenied() throws ConfigurationException {
		UnitAccess access = access(onProject(READ));
		becomeUser(_responsible);
		_object.set(_category);

		assertEquals("The responsible may read the category, but it is no project.", Display.DENIED,
			decide(access));
	}

	/**
	 * Tests that a unit without object is checked on the security root.
	 */
	public void testSecurityRoot() throws ConfigurationException {
		UnitAccess access = access("<access-control operation='" + ADMINISTRATION + "'/>");
		assertFalse(access.isReactive());

		becomeUser(_roleless);
		assertFalse("Only root administrates the application.", access.isOffered());

		becomeUser(_root);
		assertTrue(access.isOffered());
		assertEquals(Display.CONTENT, access.decide(access.bind(_context)));
	}

	/**
	 * Tests the configuration errors reported when a view is loaded.
	 */
	public void testConfigurationErrors() {
		assertInvalid("An operation must apply to the type.",
			"<access-control operation='" + FINISH + "' inputs='object' object='$object' type='"
				+ qualified(CATEGORY) + "'/>");
		assertInvalid("An operation must apply to the security root.",
			"<access-control operation='" + FINISH + "'/>");
		assertInvalid("An object needs a type.",
			"<access-control operation='" + READ + "' inputs='object' object='$object'/>");
		assertInvalid("A type needs an object.",
			"<access-control operation='" + READ + "' type='" + qualified(PROJECT) + "'/>");
		assertInvalid("The operation must exist.",
			"<access-control operation='NoSuchOperation'/>");
	}

	/**
	 * Tests that the displayed content follows the input of the unit.
	 */
	public void testReactiveGuard() throws ConfigurationException {
		becomeUser(_responsible);
		UnitAccess access = access(onProject(READ));
		ViewContext contentContext = AccessChecks.contentContext(access, _context);
		List<ReactControl> built = new ArrayList<>();
		AccessGuardControl guard = (AccessGuardControl) AccessChecks.guard(access, contentContext, ctx -> {
			ReactControl content = ContentControls.toControl(List.of(), ctx);
			built.add(content);
			return content;
		});
		guard.attach();

		assertEquals(Display.EMPTY, guard.getDisplay());
		assertTrue(built.isEmpty());

		_object.set(_project);
		assertEquals(Display.CONTENT, guard.getDisplay());
		assertEquals(1, built.size());
		assertSame(built.get(0), guard.getActive());

		_object.set(newObject(qualified(PROJECT), "other"));
		assertEquals(Display.DENIED, guard.getDisplay());
		assertTrue("The placeholder is shown.", guard.getActive() instanceof ReactAlertControl);

		_object.set(_project);
		assertEquals(Display.CONTENT, guard.getDisplay());
		assertEquals("The content is built anew.", 2, built.size());

		SecuredObject secured = SecuredObject.of(contentContext);
		assertNotNull("The unit publishes its object to its content.", secured);
		assertEquals(type(PROJECT), secured.getType());
		assertSame(_project, secured.getValue());
		assertTrue(secured.getOperations().appliesTo(
			CommandGroupRegistry.getInstance().getGroup(FINISH)));
	}

	/**
	 * Tests that a section the user can never see is left out, rather than shown empty.
	 */
	public void testGuardedSectionIsOmitted() throws ConfigurationException {
		becomeUser(_roleless);
		ReactAccordionControl accordion = (ReactAccordionControl) content(DENIED_ACCORDION).createControl(_context);

		List<Map<String, Object>> sections = maps(state(accordion).get(AccordionState.SECTIONS__PROP));
		assertEquals("Only the unguarded section is offered: " + sections, 1, sections.size());
		assertEquals("open", sections.get(0).get(AccordionState.Section.ID__PROP));
	}

	/**
	 * Tests that a group the user can never see is left out with everything it gathers.
	 */
	public void testGuardedGroupIsOmitted() throws ConfigurationException {
		becomeUser(_roleless);
		SidebarElement sidebar = (SidebarElement) content(DENIED_SIDEBAR);
		List<Map<String, Object>> items =
			maps(state((ReactSidebarControl) sidebar.createControl(_context)).get(ITEMS));
		assertEquals("Only the unguarded item is offered: " + items, 1, items.size());
		assertEquals("open", items.get(0).get(ID));

		becomeUser(_root);
		items = maps(state((ReactSidebarControl) sidebar.createControl(_context)).get(ITEMS));
		assertEquals("Root sees the group: " + items, 2, items.size());
	}

	private Display decide(UnitAccess access) {
		return access.decide(access.bind(_context));
	}

	private static String onProject(String operation) {
		return "<access-control operation='" + operation + "' inputs='object' object='$object' type='"
			+ qualified(PROJECT) + "'/>";
	}

	private static UnitAccess access(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestUnitAccess.class);
		UnitAccess result = UnitAccess.create(context, parse(context, xml));
		context.checkErrors();
		return result;
	}

	private static void assertInvalid(String message, String xml) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestUnitAccess.class);
		try {
			UnitAccess.create(context, parse(context, xml));
		} catch (ConfigurationException | ConfigurationError ex) {
			return;
		}
		assertTrue(message + ": " + xml, context.hasErrors());
	}

	private static AccessControl parse(DefaultInstantiationContext context, String xml)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(WithAccessControl.ACCESS_CONTROL,
			TypedConfiguration.getConfigurationDescriptor(AccessControl.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		return (AccessControl) reader.read();
	}

	private static UIElement content(String viewRef) throws ConfigurationException {
		ViewElement view = new FixtureViews(TestUnitAccess.class).getView(viewRef);
		ChildGroup group = view.getChildGroups().get(0);
		return ((ChildGroup.Elements) group).children().get(0);
	}

	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static List<Map<String, Object>> maps(Object values) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object value : (List<?>) values) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;
			result.add(map);
		}
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suiteWith(TestUnitAccess.class, TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE);
	}

}
