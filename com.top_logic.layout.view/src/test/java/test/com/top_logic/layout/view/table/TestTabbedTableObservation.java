/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.json.JSON;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests what a table shows after its tab was away: two tabs display the same objects, one of those
 * objects is edited while a tab is hidden, and the cells of that tab show the stored value as soon
 * as it is displayed again.
 *
 * <p>
 * The tables are wired the way {@link com.top_logic.layout.view.element.TableElement} wires them - a
 * {@link RowSourceObserver} begun when the table's control is attached and stopped when it is
 * detached - and the tab bar is the {@link ReactTabBarControl} of an application window, which
 * detaches the content of the tab it leaves and attaches the content of the tab it displays. The
 * edit is committed to the {@link com.top_logic.knowledge.service.KnowledgeBase} and delivered by a
 * {@link GlobalModelEventForwarder}, which is also the {@link ModelScope} the observation is
 * registered on: what the cells show here is what they show in a session.
 * </p>
 */
public class TestTabbedTableObservation extends AbstractDBKnowledgeBaseTest {

	/** Id of the tab the test returns to. */
	private static final String TAB_FIRST = "first";

	/** Id of the tab the test leaves the first one for. */
	private static final String TAB_SECOND = "second";

	/** Name of the single column, showing the name of its row object. */
	private static final String COLUMN_NAME = "name";

	/** The name an object is created with. */
	private static final String ORIGINAL = "original";

	/** The name an object is given while a tab is hidden. */
	private static final String STORED = "stored";

	/** State property holding the displayed rows of a table. */
	private static final String ROWS = "rows";

	/** Row property holding the cell controls, keyed by column name. */
	private static final String CELLS = "cells";

	/** Property of a child control descriptor holding the state of that control. */
	private static final String STATE = "state";

	/** State property of a text control holding the text it displays. */
	private static final String TEXT = "text";

	/** The React context the controls are built in. */
	private ReactContext _context;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The objects both tables display, in row order. */
	private List<BObj> _objects;

	/** The table of each tab, by tab id, as soon as that tab was displayed. */
	private Map<String, TableViewControl<BObj>> _tables;

	/** The tab bar displaying one of the tables at a time. */
	private ReactTabBarControl _tabBar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_tables = new LinkedHashMap<>();

		UpdateChain updates = kb().getUpdateChain();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), updates, relevance);

		_objects = List.of(create(ORIGINAL), create("other"));

		_tabBar = new ReactTabBarControl(_context, null, List.of(
			new TabDefinition(TAB_FIRST, "First", () -> createTable(TAB_FIRST), null),
			new TabDefinition(TAB_SECOND, "Second", () -> createTable(TAB_SECOND), null)));
	}

	@Override
	protected void tearDown() throws Exception {
		_tabBar.cleanupTree();
		_tabBar = null;
		_tables = null;
		_objects = null;
		_scope = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * Tests that the table of the displayed tab follows an edit of one of its objects at once.
	 */
	public void testDisplayedTableFollowsTheChange() throws Exception {
		_tabBar.attach();
		assertEquals("The cell shows the name the object was created with.",
			ORIGINAL, displayedName(TAB_FIRST, edited()));

		store(STORED);

		assertEquals("The displayed table shows the stored name.", STORED, displayedName(TAB_FIRST, edited()));
	}

	/**
	 * Tests that the table of a tab that is not displayed keeps the cells it was left with: nobody
	 * looks at it, so there is nothing to rebuild while the edit happens.
	 */
	public void testHiddenTableKeepsItsCells() throws Exception {
		_tabBar.attach();
		_tabBar.selectTab(TAB_SECOND);

		store(STORED);

		assertEquals("A hidden table shows what it showed when it was left.",
			ORIGINAL, displayedName(TAB_FIRST, edited()));
		assertEquals("The displayed table shows the stored name.", STORED, displayedName(TAB_SECOND, edited()));
	}

	/**
	 * Tests the scenario of the ticket: a tab visited before is displayed again after one of its
	 * objects was edited elsewhere, and its cells show what that object holds now.
	 */
	public void testReturningTabShowsTheStoredValue() throws Exception {
		_tabBar.attach();
		_tabBar.selectTab(TAB_SECOND);
		_tabBar.selectTab(TAB_FIRST);
		assertEquals("The tab returned to shows what its objects hold.",
			ORIGINAL, displayedName(TAB_FIRST, edited()));

		_tabBar.selectTab(TAB_SECOND);
		store(STORED);

		_tabBar.selectTab(TAB_FIRST);

		assertEquals("The tab displayed again shows the name stored while it was away.",
			STORED, displayedName(TAB_FIRST, edited()));
		assertEquals("Both tabs show the same objects, so they show the same names.",
			displayedName(TAB_SECOND, edited()), displayedName(TAB_FIRST, edited()));
	}

	/**
	 * Tests that the cells of the objects nobody edited are the ones they were: the display of the
	 * returning tab is rebuilt from the objects it holds, and those did not change.
	 */
	public void testUneditedObjectsKeepTheirCells() throws Exception {
		_tabBar.attach();
		_tabBar.selectTab(TAB_SECOND);
		store(STORED);

		_tabBar.selectTab(TAB_FIRST);

		assertEquals("An object nobody edited shows the name it had.", "other", displayedName(TAB_FIRST, other()));
	}

	/**
	 * The object whose name the test stores.
	 */
	private BObj edited() {
		return _objects.get(0);
	}

	/**
	 * The object the test leaves alone.
	 */
	private BObj other() {
		return _objects.get(1);
	}

	/**
	 * The name the table of the given tab displays for the given object, as the client receives it.
	 */
	private String displayedName(String tabId, BObj object) {
		TableViewControl<BObj> table = _tables.get(tabId);
		assertNotNull("The tab was never displayed, so it has no table.", table);

		Map<?, ?> state = parse(table.stateAsJSON());
		List<?> rows = (List<?>) state.get(ROWS);
		Map<?, ?> row = (Map<?, ?>) rows.get(_objects.indexOf(object));
		Map<?, ?> cell = (Map<?, ?>) ((Map<?, ?>) row.get(CELLS)).get(COLUMN_NAME);
		return (String) ((Map<?, ?>) cell.get(STATE)).get(TEXT);
	}

	/**
	 * The given state of a control as the client parses it.
	 */
	private static Map<?, ?> parse(String json) {
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * The table of the given tab, observing its objects while it is displayed.
	 *
	 * <p>
	 * The wiring is the one a {@code <table>} of a view is built with: the observation begins when
	 * the control is attached and stops when it is detached, and what it reads refreshes the table.
	 * </p>
	 */
	private TableViewControl<BObj> createTable(String tabId) {
		List<Column<BObj, ?>> columns = List.<Column<BObj, ?>> of(
			DefaultColumn.<BObj, String> builder(COLUMN_NAME, BObj::getA1).build());
		ListRowSource<BObj> source = new ListRowSource<>(new ArrayList<>(_objects), columns);
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, Set.of());
		state.setSelection(Selection.none(SelectionMode.SINGLE));
		TableViewControl<BObj> control =
			new TableViewControl<>(_context, new DefaultTableView<>(columns, source, state), false);

		RowSourceObserver<BObj> observer = new RowSourceObserver<>(source,
			args -> new ArrayList<>(_objects), Set.of(), List.of(), control::refreshData);
		control.addAttachListener(() -> observer.attach(_scope));
		control.addDetachListener(observer::detach);

		_tables.put(tabId, control);
		return control;
	}

	/**
	 * Creates a committed {@link BObj} with the given name, and delivers the pending events, so
	 * that the creation itself is not among the changes a later observation sees.
	 */
	private BObj create(String name) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
		return result;
	}

	/**
	 * Commits the given name for the {@link #edited() edited object} and delivers the change.
	 */
	private void store(String name) throws Exception {
		Transaction tx = begin();
		try {
			edited().setA1(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
	}

	/**
	 * Delivers the changes committed since the last call to the listeners of {@link #_scope}.
	 */
	private void deliverChanges() {
		_scope.synthesizeModelEvents();
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestTabbedTableObservation.class);
	}

}
