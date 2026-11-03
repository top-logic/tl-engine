/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandTrigger;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetBinding.RemoveMode;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the types a {@link RowSetTableControl} offers for new rows: a concrete create type is
 * created directly, an abstract one stands for its concrete specializations the user may create,
 * and the {@link RowSetTableControl#getAddRowCommand() add-row command} lets the user choose among
 * several of them in a menu.
 */
public class TestRowSetCreateTypes extends AbstractModelAccessTest {

	/** Name of the {@link #PROJECT} composition of the abstract type {@link #DELIVERABLE}. */
	private static final String DELIVERABLES = "deliverables";

	/** Name of the abstract type with the concrete specializations {@link #SPEC} and {@link #PLAN}. */
	private static final String DELIVERABLE = "Deliverable";

	/** Name of the concrete {@link #DELIVERABLE} only {@link #ROLE_RESPONSIBLE} may create. */
	private static final String SPEC = "Spec";

	/** Name of the concrete {@link #DELIVERABLE} no role may create. */
	private static final String PLAN = "Plan";

	/** Name of the abstract type without concrete specialization. */
	private static final String EMPTY = "Empty";

	/** ID of the client-side element the add-row command is triggered from. */
	private static final String TRIGGER_ID = "addButton";

	private Menu _menu;

	private ReactContext _reactContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_menu = new Menu();
		ContextMenuOpener opener = new ContextMenuOpener(_menu);
		_reactContext = new ForwardingReactContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"))) {
			@Override
			public ContextMenuOpener getContextMenuOpener() {
				return opener;
			}
		};
		opener.bindReactContext(() -> _reactContext);
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_menu = null;
		super.tearDown();
	}

	/**
	 * A concrete row type is the only type offered, and adding a row creates it without a menu.
	 */
	public void testConcreteType() {
		becomeUser(_responsible);
		RowSetTableControl table = editedTable(new AttributeRowSetBinding(TASKS));

		assertEquals(List.of(type(TASK)), table.createTypes());
		assertTrue(table.getAddRowCommand().isVisible());

		TLObject row = addRowOfferedType(table);
		assertNull("No menu for a single type.", _menu._items);
		assertNotNull(row);
		assertSame(type(TASK), row.tType());
	}

	/**
	 * An abstract row type offers its concrete specializations, sorted by label; adding a row
	 * opens a menu at the triggering button, and choosing an entry creates a row of its type,
	 * which is saved with the form.
	 */
	public void testAbstractTypeOffersConcreteSpecializations() {
		becomeUser(_root);
		AttributeRowSetBinding binding = new AttributeRowSetBinding(DELIVERABLES);
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, binding);

		assertEquals(Set.of(type(SPEC), type(PLAN)), new HashSet<>(binding.getCreateTypes()));
		List<TLClass> offered = table.createTypes();
		assertEquals(Set.of(type(SPEC), type(PLAN)), new HashSet<>(offered));
		assertEquals(labels(offered).stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(), labels(offered));
		assertTrue(table.getAddRowCommand().isVisible());

		assertNull("A row is created only once a type is chosen.", addRowOfferedType(table));
		assertEquals(TRIGGER_ID, _menu._anchorId);
		assertEquals(labels(offered), _menu.labels());
		assertTrue(table.session().currentRows().isEmpty());

		_menu.choose(MetaLabelProvider.INSTANCE.getLabel(type(PLAN)));
		TLObject row = single(table.session().currentRows());
		assertSame(type(PLAN), row.tType());

		form.executeSave();
		TLObject saved = single(_project.tValueByName(DELIVERABLES));
		assertFalse(saved.tTransient());
		assertSame(type(PLAN), saved.tType());
	}

	/**
	 * Only the concrete specializations the user may create are offered: with a single one left,
	 * adding a row creates it directly, and a type that is not offered is not created.
	 */
	public void testTypesFilteredByCreateRight() {
		becomeUser(_responsible);
		RowSetTableControl table = editedTable(new AttributeRowSetBinding(DELIVERABLES));

		assertEquals(List.of(type(SPEC)), table.createTypes());
		assertNull("Not offered to the user.", table.addRowOfType(type(PLAN)));

		TLObject row = addRowOfferedType(table);
		assertNull("No menu for a single type.", _menu._items);
		assertNotNull(row);
		assertSame(type(SPEC), row.tType());
	}

	/**
	 * Without a concrete type the user may create, the table offers no row creation.
	 */
	public void testNoCreatableType() {
		becomeUser(_responsible);
		// The only concrete archive type may be created by no role.
		RowSetTableControl archive = editedTable(new AttributeRowSetBinding("archive"));
		assertFalse(archive.getAddRowCommand().isVisible());
		assertTrue(archive.createTypes().isEmpty());
		assertNull(archive.addRow());
	}

	/**
	 * An abstract create type without concrete specialization offers no row creation, not even to
	 * a user bypassing the access rights.
	 */
	public void testAbstractTypeWithoutConcreteSpecialization() {
		becomeUser(_root);
		TLClass empty = type(EMPTY);
		RowSetTableControl table = editedTable(new QueryRowSetBinding(List::of, empty,
			List.copyOf(ModelAccessPolicy.concreteTypes(empty)), RemoveMode.NONE));

		assertTrue(table.createTypes().isEmpty());
		assertFalse(table.getAddRowCommand().isVisible());
		assertNull(table.addRow());
	}

	/**
	 * Executes the add-row command as triggered by the button {@link #TRIGGER_ID}.
	 *
	 * @return The row the command added, {@code null} if it added none.
	 */
	private TLObject addRowOfferedType(RowSetTableControl table) {
		List<TLObject> before = new ArrayList<>(table.session().currentRows());
		CommandModel command = table.getAddRowCommand();
		HandlerResult result = CommandTrigger.runTriggeredBy(TRIGGER_ID, () -> command.executeCommand(_reactContext));
		assertTrue(result.isSuccess());
		List<TLObject> after = new ArrayList<>(table.session().currentRows());
		after.removeAll(before);
		return after.isEmpty() ? null : single(after);
	}

	private RowSetTableControl editedTable(RowSetBinding binding) {
		return editedTable(newForm(), binding);
	}

	private RowSetTableControl editedTable(FormControl form, RowSetBinding binding) {
		RowSetTableControl table =
			new RowSetTableControl(new DefaultViewContext(_reactContext), form, binding, List.of(), RowEditPolicy.ALL);
		table.init();
		assertTrue(form.enterEditMode());
		return table;
	}

	private FormControl newForm() {
		return new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
	}

	private static TLObject single(Object value) {
		List<?> list = (List<?>) value;
		assertEquals(1, list.size());
		return (TLObject) list.get(0);
	}

	private static List<String> labels(List<TLClass> types) {
		return types.stream().map(MetaLabelProvider.INSTANCE::getLabel).toList();
	}

	/**
	 * {@link ContextMenuOpener.MenuRenderer} recording the menu shown last.
	 */
	private static class Menu implements ContextMenuOpener.MenuRenderer {

		List<MenuEntry> _items;

		String _anchorId;

		Function<String, HandlerResult> _selectHandler;

		@Override
		public void show(int x, int y, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler) {
			fail("The type menu is placed at its trigger.");
		}

		@Override
		public void show(String anchorId, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler) {
			_anchorId = anchorId;
			_items = items;
			_selectHandler = selectHandler;
		}

		@Override
		public void hide() {
			// Nothing displayed.
		}

		List<String> labels() {
			return _items.stream().map(MenuEntry::label).toList();
		}

		void choose(String label) {
			for (MenuEntry entry : _items) {
				if (label.equals(entry.label())) {
					assertTrue(_selectHandler.apply(entry.id()).isSuccess());
					return;
				}
			}
			fail("No entry '" + label + "' in " + labels());
		}
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its data columns and their cells, and the labels of the offered types.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetCreateTypes.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
