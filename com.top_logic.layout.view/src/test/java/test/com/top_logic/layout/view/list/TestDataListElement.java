/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.state.DataListState;
import com.top_logic.layout.react.state.TextState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.list.DataListControl;
import com.top_logic.layout.view.list.DataListElement;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link DataListElement} and its {@link DataListControl}: the rows with their lines and
 * groups, the selection written to its channel and read back from it, the commands of a row, and
 * the refresh after a change of an observed type.
 *
 * <p>
 * The rows are modules of the application model, whose names start with {@link #PREFIX}; the
 * changes are committed to the knowledge base and delivered by a {@link GlobalModelEventForwarder},
 * which is the model scope of the list.
 * </p>
 */
public class TestDataListElement extends BasicTestCase {

	/** The list of the fixture, showing the modules named with {@link #PREFIX}. */
	private static final String FIXTURE = "data-list.view.xml";

	/**
	 * A list of the same modules counting their classes, observing the type of the classes as well.
	 */
	private static final String RELATED_FIXTURE = "data-list-related.view.xml";

	/** Prefix of the names of the modules the list shows; the next letter is their group. */
	private static final String PREFIX = "test.datalist.";

	/** A list over classifiers with a template after the lines of each row. */
	private static final String TEMPLATE_FIXTURE = "data-list-template.view.xml";

	/** The key the client asks a text for its tooltip with. */
	private static final String TOOLTIP_KEY = "tooltip";

	/** The channel the list writes its selection to. */
	private static final String SELECTION = "selection";

	/** The channel the list writes the number of its rows to. */
	private static final String COUNT = "count";

	private GlobalModelEventForwarder _scope;

	private ViewContext _context;

	private ViewChannel _selection;

	private ViewChannel _count;

	/** The entries of the menus the row commands opened, one list per menu. */
	private List<List<MenuEntry>> _menus;

	private List<TLModule> _modules;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), kb().getUpdateChain(), relevance);

		_menus = new ArrayList<>();
		ContextMenuOpener opener = new ContextMenuOpener(new ContextMenuOpener.MenuRenderer() {
			@Override
			public void show(int x, int y, List<MenuEntry> items, Consumer<String> selectHandler,
					Runnable closeHandler) {
				_menus.add(items);
			}

			@Override
			public void hide() {
				// Nothing shown.
			}
		});
		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return _scope;
			}

			@Override
			public ContextMenuOpener getContextMenuOpener() {
				return opener;
			}
		});
		_selection = new DefaultViewChannel(SELECTION);
		_count = new DefaultViewChannel(COUNT);
		_context.registerChannel(SELECTION, _selection);
		_context.registerChannel(COUNT, _count);

		_modules = new ArrayList<>();
		_modules.add(createModule(PREFIX + "bravo"));
		_modules.add(createModule(PREFIX + "alpha"));
		_modules.add(createModule(PREFIX + "anna"));
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction()) {
			for (TLModule module : _modules) {
				if (module.tValid()) {
					TLModelUtil.deleteRecursive(module);
				}
			}
			tx.commit();
		}
		_scope.synthesizeModelEvents();
		_modules = null;
		_menus = null;
		_selection = null;
		_count = null;
		_context = null;
		_scope = null;

		super.tearDown();
	}

	/**
	 * Tests that each row carries the lines of its object - an attribute and a computed value with
	 * its emphasis -, its group and its computed classes, in the order of the rows expression, and
	 * that the count channel receives the number of rows.
	 */
	public void testRows() {
		DataListControl list = list();

		Map<?, ?> state = state(list);
		assertEquals("my-list", state.get(ControlState.CSS_CLASS__PROP));
		List<Map<?, ?>> rows = rows(list);
		assertEquals(List.of(PREFIX + "alpha", PREFIX + "anna", PREFIX + "bravo"), primaryTexts(rows));
		assertEquals(List.of("a", "a", "b"), rows.stream().map(row -> row.get(DataListState.Row.GROUP__PROP)).toList());
		assertEquals("is-module", rows.get(0).get(DataListState.Row.CSS_CLASS__PROP));

		List<Map<?, ?>> lines = maps(rows.get(0).get(DataListState.Row.LINES__PROP));
		assertEquals(2, lines.size());
		assertFalse("A line without emphasis says none: " + lines.get(0),
			lines.get(0).containsKey(DataListState.Line.EMPHASIS__PROP));
		assertEquals("Module", lines.get(1).get(DataListState.Line.TEXT__PROP));
		assertEquals("secondary", lines.get(1).get(DataListState.Line.EMPHASIS__PROP));

		assertEquals(Integer.valueOf(3), _count.get());
	}

	/**
	 * Tests what stands in front of and after the lines of each row: an avatar name, an icon of an
	 * icon font, and a badge that is left out for a count of {@code 0}.
	 */
	public void testLeadingAndTrailing() {
		List<Map<?, ?>> rows = rows(list());

		Map<?, ?> alpha = (Map<?, ?>) rows.get(0).get(DataListState.Row.LEADING__PROP);
		assertEquals("alpha", alpha.get(DataListState.Leading.NAME__PROP));
		assertFalse("No image: " + alpha, alpha.containsKey(DataListState.Leading.ICON__PROP)
			|| alpha.containsKey(DataListState.Leading.IMAGE__PROP));
		Map<?, ?> bravo = (Map<?, ?>) rows.get(2).get(DataListState.Row.LEADING__PROP);
		assertEquals("bravo", bravo.get(DataListState.Leading.NAME__PROP));
		assertEquals("css:bi bi-box", bravo.get(DataListState.Leading.ICON__PROP));
		assertFalse(bravo.containsKey(DataListState.Leading.IMAGE__PROP));

		Map<?, ?> anna = (Map<?, ?>) rows.get(1).get(DataListState.Row.TRAILING__PROP);
		assertEquals(String.valueOf((PREFIX + "anna").length()), anna.get(DataListState.Trailing.TEXT__PROP));
		assertEquals(Boolean.TRUE, anna.get(DataListState.Trailing.BADGE__PROP));
		assertFalse("A count of 0 shows no badge.", rows.get(2).containsKey(DataListState.Row.TRAILING__PROP));
	}

	/**
	 * Tests that a template after the lines gives each row a control of its own: a pill in the color
	 * of the classifier the row shows, with the tooltip the template computes; the short form given
	 * beside the template is not shown.
	 */
	public void testTrailingTemplate() throws Exception {
		TLEnumeration states = createStates();
		DataListControl list = list(TEMPLATE_FIXTURE);

		List<Map<?, ?>> rows = rows(list);
		assertEquals(List.of("test-done", "test-open"), primaryTexts(rows));
		Map<String, ReactTextControl> controls = trailingControls(list);
		for (Map<?, ?> row : rows) {
			Map<?, ?> trailing = (Map<?, ?>) row.get(DataListState.Row.TRAILING__PROP);
			assertFalse("The template takes the place of the short form: " + trailing,
				trailing.containsKey(DataListState.Trailing.TEXT__PROP));
			ReactTextControl control = controls.get(trailingId(row));
			assertNotNull("Each row has a control of the template.", control);

			TLClassifier state = states.getClassifier(primaryText(row));
			Map<?, ?> child = (Map<?, ?>) trailing.get(DataListState.Trailing.CONTROL__PROP);
			Map<?, ?> text = (Map<?, ?>) child.get(ChildControl.STATE__PROP);
			assertEquals(MetaLabelProvider.INSTANCE.getLabel(state), text.get(TextState.TEXT__PROP));
			assertEquals(ReactValueColor.roleOf(state), text.get(ReactValueColor.ROLE));
			TooltipContent tooltip = control.getTooltipContent(TOOLTIP_KEY);
			assertEquals("State " + state.getName(), tooltip.getHtml());
		}
		assertEquals(ValueColor.SUCCESS.getExternalName(),
			ReactValueColor.roleOf(states.getClassifier("test-done")));
	}

	/**
	 * Tests that the instances of the template follow the rows: a new row gets a control, a row
	 * that stays keeps its control, and the control of a row that left the list is disposed.
	 */
	public void testTrailingTemplateFollowsTheRows() throws Exception {
		TLEnumeration states = createStates();
		DataListControl list = list(TEMPLATE_FIXTURE);
		Map<String, String> before = trailingIdsByText(list);
		Map<String, ReactTextControl> controlsBefore = trailingControls(list);

		addClassifier(states, "test-new", ValueColor.INFO);
		Map<String, String> added = trailingIdsByText(list);
		assertEquals(List.of("test-done", "test-new", "test-open"), primaryTexts(rows(list)));
		assertEquals("A row that stays keeps its control.", before.get("test-done"), added.get("test-done"));
		assertEquals("A row that stays keeps its control.", before.get("test-open"), added.get("test-open"));
		assertNotNull("A new row gets a control.", added.get("test-new"));
		assertFalse(before.containsValue(added.get("test-new")));

		ReactTextControl done = controlsBefore.get(before.get("test-done"));
		try (Transaction tx = kb().beginTransaction()) {
			TLModelUtil.deleteRecursive(states.getClassifier("test-done"));
			tx.commit();
		}
		_scope.synthesizeModelEvents();
		Map<String, String> removed = trailingIdsByText(list);
		assertEquals(List.of("test-new", "test-open"), primaryTexts(rows(list)));
		assertTrue("The control of a row that left the list is disposed.", done.isDisposed());
		assertEquals(added.get("test-open"), removed.get("test-open"));
		assertFalse(controlsBefore.get(before.get("test-open")).isDisposed());
	}

	/**
	 * Tests that the list follows a change of an object that is no row but of an observed type: a
	 * class created in a module changes the count of its badge, while the list stays displayed.
	 */
	public void testRelatedObjectOfObservedType() throws Exception {
		DataListControl list = list(RELATED_FIXTURE);
		assertFalse("No class, no badge.", rows(list).get(2).containsKey(DataListState.Row.TRAILING__PROP));

		try (Transaction tx = kb().beginTransaction()) {
			TLModelUtil.addClass(_modules.get(0), "Ticket");
			tx.commit();
		}
		_scope.synthesizeModelEvents();

		Map<?, ?> bravo = (Map<?, ?>) rows(list).get(2).get(DataListState.Row.TRAILING__PROP);
		assertEquals("1", bravo.get(DataListState.Trailing.TEXT__PROP));
	}

	/**
	 * Tests that selecting a row writes its object to the selection channel, that a value written
	 * to the channel from elsewhere is shown as selected, and that an empty ID clears the selection.
	 */
	public void testSelection() {
		DataListControl list = list();
		assertEquals("", state(list).get(DataListState.SELECTED_ID__PROP));

		String bravo = idOf(list, PREFIX + "bravo");
		list.executeCommand(DataListControl.CMD_SELECT, Map.of(DataListControl.ARG_ID, bravo));
		assertSame(_modules.get(0), _selection.get());
		assertEquals(bravo, state(list).get(DataListState.SELECTED_ID__PROP));

		_selection.set(_modules.get(1));
		assertEquals(idOf(list, PREFIX + "alpha"), state(list).get(DataListState.SELECTED_ID__PROP));

		list.executeCommand(DataListControl.CMD_SELECT, Map.of(DataListControl.ARG_ID, ""));
		assertNull(_selection.get());
		assertEquals("", state(list).get(DataListState.SELECTED_ID__PROP));
	}

	/**
	 * Tests that the trigger of a row opens the menu of its commands for that row: the row reaches
	 * the command, whose label is computed from it.
	 */
	public void testRowCommands() {
		DataListControl list = list();
		assertEquals(Boolean.TRUE, state(list).get(DataListState.HAS_ROW_MENU__PROP));
		assertEquals("hover", state(list).get(DataListState.ROW_ACTIONS__PROP));

		list.executeCommand(DataListControl.CMD_ROW_MENU, Map.of(DataListControl.ARG_ID,
			idOf(list, PREFIX + "anna"), DataListControl.ARG_X, 10, DataListControl.ARG_Y, 20));
		assertEquals(1, _menus.size());
		List<String> labels = _menus.get(0).stream().map(MenuEntry::label).toList();
		assertEquals(List.of("Rename " + PREFIX + "anna"), labels);

		list.executeCommand(DataListControl.CMD_ROW_MENU, Map.of(DataListControl.ARG_ID,
			idOf(list, PREFIX + "bravo"), DataListControl.ARG_X, 10, DataListControl.ARG_Y, 20));
		assertEquals("Rename " + PREFIX + "bravo", _menus.get(1).get(0).label());
	}

	/**
	 * Tests that the list follows a change of an observed type: a created object appears in its
	 * group, a deleted one disappears and leaves the selection.
	 */
	public void testObservedTypes() throws Exception {
		DataListControl list = list();
		list.executeCommand(DataListControl.CMD_SELECT, Map.of(DataListControl.ARG_ID, idOf(list, PREFIX + "bravo")));

		_modules.add(createModule(PREFIX + "berta"));
		assertEquals(List.of(PREFIX + "alpha", PREFIX + "anna", PREFIX + "berta", PREFIX + "bravo"),
			primaryTexts(rows(list)));
		assertEquals(Integer.valueOf(4), _count.get());

		try (Transaction tx = kb().beginTransaction()) {
			TLModelUtil.deleteRecursive(_modules.get(0));
			tx.commit();
		}
		_scope.synthesizeModelEvents();
		assertEquals(List.of(PREFIX + "alpha", PREFIX + "anna", PREFIX + "berta"), primaryTexts(rows(list)));
		assertNull("The deleted object left the selection.", _selection.get());
	}

	/**
	 * An enumeration in a module of its own, with two colored classifiers the
	 * {@link #TEMPLATE_FIXTURE} lists.
	 */
	private TLEnumeration createStates() throws Exception {
		TLModule module = createModule(PREFIX + "states");
		_modules.add(module);
		TLEnumeration result;
		try (Transaction tx = kb().beginTransaction()) {
			result = TLModelUtil.addEnumeration(module, "State");
			tx.commit();
		}
		addClassifier(result, "test-open", ValueColor.WARNING);
		addClassifier(result, "test-done", ValueColor.SUCCESS);
		return result;
	}

	private void addClassifier(TLEnumeration enumeration, String name, ValueColor role) {
		try (Transaction tx = kb().beginTransaction()) {
			TLClassifier classifier = TLModelUtil.addClassifier(enumeration, name);
			TLColor color = TypedConfiguration.newConfigItem(TLColor.class);
			color.setRole(role);
			classifier.setAnnotation(color);
			tx.commit();
		}
		_scope.synthesizeModelEvents();
	}

	/** The controls of the template after the lines, by their control ID. */
	private static Map<String, ReactTextControl> trailingControls(DataListControl list) {
		Map<String, ReactTextControl> result = new HashMap<>();
		for (ReactControl child : list.scriptingChildren()) {
			if (child instanceof ReactTextControl text) {
				result.put(text.getID(), text);
			}
		}
		return result;
	}

	/** The control IDs of the template instances, by the first line of their row. */
	private static Map<String, String> trailingIdsByText(DataListControl list) {
		Map<String, String> result = new HashMap<>();
		for (Map<?, ?> row : rows(list)) {
			result.put(primaryText(row), trailingId(row));
		}
		return result;
	}

	/** The control ID of the template instance of the given row, as the client receives it. */
	private static String trailingId(Map<?, ?> row) {
		Map<?, ?> trailing = (Map<?, ?>) row.get(DataListState.Row.TRAILING__PROP);
		Map<?, ?> control = (Map<?, ?>) trailing.get(DataListState.Trailing.CONTROL__PROP);
		return (String) control.get(ChildControl.CONTROL_ID__PROP);
	}

	private DataListControl list() {
		return list(FIXTURE);
	}

	private DataListControl list(String fixture) {
		DataListControl result = (DataListControl) element(fixture).createControl(_context);
		result.attach();
		return result;
	}

	private DataListElement element(String fixture) {
		try {
			ViewElement view = new FixtureViews(TestDataListElement.class).getView(fixture);
			ChildGroup group = view.getChildGroups().get(0);
			UIElement content = ((ChildGroup.Elements) group).children().get(0);
			return (DataListElement) content;
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a readable view: " + fixture, ex);
		}
	}

	private TLModule createModule(String name) throws Exception {
		TLModule result;
		try (Transaction tx = kb().beginTransaction()) {
			result = TLModelUtil.addModule(ModelService.getApplicationModel(), name);
			tx.commit();
		}
		_scope.synthesizeModelEvents();
		return result;
	}

	/** The ID of the row whose first line is the given text. */
	private static String idOf(DataListControl list, String text) {
		for (Map<?, ?> row : rows(list)) {
			if (text.equals(primaryText(row))) {
				return (String) row.get(DataListState.Row.ID__PROP);
			}
		}
		throw new AssertionError("No row " + text);
	}

	private static List<String> primaryTexts(List<Map<?, ?>> rows) {
		return rows.stream().map(TestDataListElement::primaryText).toList();
	}

	private static String primaryText(Map<?, ?> row) {
		return (String) maps(row.get(DataListState.Row.LINES__PROP)).get(0).get(DataListState.Line.TEXT__PROP);
	}

	private static List<Map<?, ?>> rows(DataListControl list) {
		return maps(state(list).get(DataListState.ROWS__PROP));
	}

	private static List<Map<?, ?>> maps(Object values) {
		List<Map<?, ?>> result = new ArrayList<>();
		for (Object value : (List<?>) values) {
			result.add((Map<?, ?>) value);
		}
		return result;
	}

	private static Map<?, ?> state(DataListControl list) {
		try {
			return (Map<?, ?>) JSON.fromString(list.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The expressions are compiled and evaluated against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDataListElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
