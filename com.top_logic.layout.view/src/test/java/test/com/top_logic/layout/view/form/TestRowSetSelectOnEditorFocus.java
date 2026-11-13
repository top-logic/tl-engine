/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding.RemoveMode;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Tests that the focus of a cell editor selects its row exactly in a table whose rows are all
 * edited, and that this selection leaves the cells of the rows as they are.
 */
public class TestRowSetSelectOnEditorFocus extends AbstractModelAccessTest {

	/** The tag a column declaration is read from. */
	private static final String COLUMN = "column";

	/** The state key of the table telling the client to select the row of a focused editor. */
	private static final String SELECT_ON_EDITOR_FOCUS = "selectOnEditorFocus";

	/** The state key of the table holding the rows sent to the client. */
	private static final String ROWS = "rows";

	/** The key of a row of {@link #ROWS} holding the id of the row. */
	private static final String ROW_ID = "id";

	private CapturingQueue _queue;

	private ReactContext _reactContext;

	private TLObject _first;

	private TLObject _second;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new CapturingQueue();
		_reactContext = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_first = newStep("first");
			_second = newStep("second");
			_project.tUpdateByName(STEPS, List.of(_first, _second));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_queue = null;
		_reactContext = null;
		_first = null;
		_second = null;
		super.tearDown();
	}

	/**
	 * A table whose rows are all edited selects the row of a focused editor in edit mode only.
	 */
	public void testAllRowsEdited() {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = table(form, RowEditPolicy.ALL);
		assertEquals("No editor in view mode.", Boolean.FALSE, selectOnEditorFocus(table));

		assertTrue(form.enterEditMode());
		assertEquals(Boolean.TRUE, selectOnEditorFocus(table));
	}

	/**
	 * A table editing only its selected row, or no row, leaves the selection to the clicks on the
	 * rows.
	 */
	public void testNotAllRowsEdited() {
		becomeUser(_root);
		for (RowEditPolicy policy : List.of(RowEditPolicy.SELECTED, RowEditPolicy.NONE)) {
			FormControl form = newForm();
			RowSetTableControl table = table(form, policy);
			assertTrue(form.enterEditMode());
			assertEquals(policy.name(), Boolean.FALSE, selectOnEditorFocus(table));
		}
	}

	/**
	 * Selecting a row of a table whose rows are all edited sends no row again: the editor that
	 * took the focus is not built anew.
	 */
	public void testSelectionSendsNoCells() throws IOException {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = table(form, RowEditPolicy.ALL);
		table.write(new TagWriter());
		assertTrue(form.enterEditMode());
		TableViewControl<?> tableControl = table.getTableControl();
		List<?> rows = (List<?>) state(tableControl).get(ROWS);
		assertEquals(2, rows.size());
		Object rowId = ((Map<?, ?>) rows.get(1)).get(ROW_ID);
		_queue.clear();

		tableControl.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(
			SelectRowArguments.ROW_ID, rowId,
			SelectRowArguments.CTRL_KEY, Boolean.FALSE,
			SelectRowArguments.SHIFT_KEY, Boolean.FALSE));

		String patches = patchesOf(tableControl);
		assertFalse("The selection reached no client: " + _queue.events(), patches.isEmpty());
		assertFalse("Rows sent again: " + patches, patches.contains("\"" + ROWS + "\""));
	}

	/** A table of the steps with an editor of their names. */
	private RowSetTableControl table(FormControl form, RowEditPolicy policy) {
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new QueryRowSetBinding(() -> List.of(_first, _second), type(STEP), List.of(), RemoveMode.NONE),
			List.of(column("<column attribute='" + NAME + "'/>")), policy);
		table.init();
		return table;
	}

	/** The column declaration read from the given {@value #COLUMN} tag. */
	private static ColumnDeclaration column(String xml) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestRowSetSelectOnEditorFocus.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(COLUMN,
			TypedConfiguration.getConfigurationDescriptor(AttributeColumn.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		try {
			ColumnDeclaration result =
				(ColumnDeclaration) context.getInstance((PolymorphicConfiguration<?>) reader.read());
			context.checkErrors();
			return result;
		} catch (ConfigurationException ex) {
			throw new AssertionError("Invalid column: " + xml, ex);
		}
	}

	private static Object selectOnEditorFocus(RowSetTableControl table) {
		Object value = state(table.getTableControl()).get(SELECT_ON_EDITOR_FOCUS);
		return value == null ? Boolean.FALSE : value;
	}

	/** The patches the given control sent to the client, joined. */
	private String patchesOf(ReactControl control) {
		return _queue.events().stream()
			.filter(PatchEvent.class::isInstance)
			.map(PatchEvent.class::cast)
			.filter(patch -> patch.getControlId().equals(control.getID()))
			.map(PatchEvent::getPatch)
			.collect(Collectors.joining());
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	private static TLObject newStep(String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
		result.tUpdateByName(NAME, name);
		return result;
	}

	private FormControl newForm() {
		return new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
	}

	/**
	 * An {@link SSEUpdateQueue} that captures enqueued events for assertions instead of flushing
	 * them to a connection.
	 */
	private static final class CapturingQueue extends SSEUpdateQueue {

		private final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
		}

		List<SSEEvent> events() {
			return _events;
		}

		void clear() {
			_events.clear();
		}
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons and the
	 * services building its data columns and their cells.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetSelectOnEditorFocus.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
