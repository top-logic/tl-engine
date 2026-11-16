/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.form.RecordingErrorSink;
import test.com.top_logic.layout.view.form.TestFormLocking;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.handler.DefaultLockHandler;
import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;

/**
 * Tests that a table edited in place locks the object of a row while it writes a value of its
 * cells, and writes nothing while someone else holds the lock.
 */
@SuppressWarnings("javadoc")
public class TestInPlaceLocking extends AbstractModelAccessTest {

	private static final String TABLE = "table";

	private static final String PROJECT_CHANNEL = "project";

	/** The column edited in place, reading and writing the name of the project. */
	private static final String COLUMN = "n";

	private RecordingErrorSink _messages;

	private ViewContext _context;

	/** The controls created by the test, disposed after it. */
	private List<ReactControl> _controls;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_messages = new RecordingErrorSink();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")))
				.withErrorSink(_messages);
		DefaultViewChannel project = new DefaultViewChannel(PROJECT_CHANNEL);
		project.set(_project);
		_context.registerChannel(PROJECT_CHANNEL, project);
		_controls = new ArrayList<>();
	}

	@Override
	protected Person initialUser() {
		// Bypasses the model access rights: the tests are about locking, not about the edit right.
		return _root;
	}

	@Override
	protected void tearDown() throws Exception {
		for (ReactControl control : _controls) {
			control.cleanupTree();
		}
		_controls = null;
		_context = null;
		_messages = null;
		super.tearDown();
	}

	/**
	 * (a) A form holds the lock of the object: a value entered in the cell of its row is not
	 * written, the cell shows the stored value again, and the user is told that the value was not
	 * saved and who holds the lock.
	 */
	public void testLockedObjectIsNotWritten() throws Exception {
		FormControl form = lockingForm();
		String stored = name();

		FieldModel cell = cell(table(""));
		List<Object> shown = new ArrayList<>();
		cell.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				shown.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not observed.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not observed.
			}
		});

		cell.setValue("changed");

		assertEquals("The locked object is not written.", stored, name());
		assertEquals(stored, cell.getValue());
		assertEquals("The cell is told to show the stored value again.", List.of(stored), shown);
		assertEquals(List.of(TestFormLocking.lockConflictInfo(I18NConstants.CELL_VALUE_NOT_SAVED, _project)),
			_messages.shown());
		assertTrue("The lock of the form is kept.", form.isEditMode());
		TestFormLocking.assertLocked(_project);
	}

	/**
	 * (b) Nobody holds the lock of the object: the entered value is written, and no lock is held
	 * afterwards.
	 */
	public void testFreeObjectIsWrittenAndUnlocked() throws Exception {
		FieldModel cell = cell(table(""));

		cell.setValue("changed");

		assertEquals("changed", name());
		assertEquals(List.of(), _messages.shown());
		TestFormLocking.assertNotLocked(_project);
	}

	/**
	 * (c) A table with {@link NoTokenHandling} writes without asking for the lock.
	 */
	public void testNoTokenHandlingWritesDespiteLock() throws Exception {
		lockingForm();

		FieldModel cell =
			cell(table("<lockHandler class='" + NoTokenHandling.class.getName() + "'/>"));
		cell.setValue("changed");

		assertEquals("changed", name());
		assertEquals(List.of(), _messages.shown());
	}

	/**
	 * A form in another window that edits the project, holding its lock.
	 */
	private FormControl lockingForm() {
		FormControl result = new FormControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")), _project,
			"no model", DefaultLockHandler.newInstance(Token.DEFAULT_OPERATION));
		_controls.add(result);
		assertTrue(result.enterEditMode());
		return result;
	}

	private String name() {
		return (String) _project.tValueByName(NAME);
	}

	/**
	 * The field model of the cell of the project in the {@link #COLUMN} of the given table.
	 */
	private FieldModel cell(TableViewControl<?> table) {
		@SuppressWarnings("unchecked")
		CellContent content = ((TableViewControl<Object>) table).getView().column(COLUMN).renderCell(_project);
		ReactControl control = ((CellControlFactory) ((CellContent.Raw) content).payload()).create(_context);
		_controls.add(control);
		ReactControl field = ((ReactFormFieldChromeControl) control).getField();
		return ((ReactFormFieldControl) field).getFieldModel();
	}

	/**
	 * A table edited in place, showing the project with a column editing its name.
	 *
	 * @param lockHandler
	 *        The configuration of the lock handler of the table, empty for the default.
	 */
	private TableViewControl<?> table(String lockHandler) throws ConfigurationException {
		String attribute = "`" + qualified(PROJECT) + "#" + NAME + "`";
		String xml = "<table inputs='" + PROJECT_CHANNEL + "' rows='[$" + PROJECT_CHANNEL + "]' edit-in-place='true'>"
			+ lockHandler
			+ "<columns><computed-column name='" + COLUMN + "' type='tl.core:String'>"
			+ "<value>$row.get(" + attribute + ")</value>"
			+ "<update>$row.set(" + attribute + ", $value)</update>"
			+ "</computed-column></columns></table>";
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestInPlaceLocking.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(TABLE, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		TableElement.Config config = (TableElement.Config) reader.read();
		TableElement element = (TableElement) context.getInstance(config);
		context.checkErrors();
		TableViewControl<?> result = (TableViewControl<?>) element.createControl(_context);
		_controls.add(result);
		return result;
	}

	/**
	 * The test suite, starting the services building the columns of a table and their cells.
	 */
	public static Test suite() {
		return suiteWith(TestInPlaceLocking.class, LockService.Module.INSTANCE, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
