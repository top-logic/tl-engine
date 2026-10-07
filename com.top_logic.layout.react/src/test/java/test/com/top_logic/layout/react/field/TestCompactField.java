/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests displaying a field in little room, see {@link FieldSpec#isCompact()} and
 * {@link ReactCompactFieldControl}.
 */
public class TestCompactField extends TestCase {

	/** The command a button is pressed with. */
	private static final String CLICK = "click";

	private SSEUpdateQueue _queue;

	private ReactContext _context;

	private RecordingDialogManager _dialogs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new SSEUpdateQueue();
		_dialogs = new RecordingDialogManager();
		_queue.setDialogManager(_dialogs);
		_context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
	}

	/**
	 * A compact field whose control needs more room is displayed as a preview with an opener.
	 */
	public void testCompactLargeFieldIsCompacted() {
		FieldSpec field = FieldSpec.of(String.class, "Notes").setMultilineRows(5).setCompact(true);

		ReactControl control = createControl(field, new AbstractFieldModel("First\nSecond"), FieldControlRegistry.TEXT);

		ReactCompactFieldControl compact = assertInstanceof(ReactCompactFieldControl.class, control);
		assertEquals("First", compact.getPreviewText());
	}

	/**
	 * A compact field whose control fits a single line keeps that control.
	 */
	public void testCompactSmallFieldKeepsItsControl() {
		FieldSpec field = FieldSpec.of(String.class, "Name").setCompact(true);

		assertInstanceof(ReactTextInputControl.class,
			createControl(field, new AbstractFieldModel("A"), FieldControlRegistry.TEXT));
	}

	/**
	 * A text area of a single row fits a compact display.
	 */
	public void testSingleRowTextIsNotLarge() {
		FieldSpec field = FieldSpec.of(String.class, "Name").setMultilineRows(1).setCompact(true);

		assertInstanceof(ReactTextInputControl.class,
			createControl(field, new AbstractFieldModel("A"), FieldControlRegistry.TEXT));
	}

	/**
	 * A field that is not compact gets its full control, however much room that needs.
	 */
	public void testNonCompactLargeFieldKeepsItsControl() {
		FieldSpec field = FieldSpec.of(String.class, "Notes").setMultilineRows(5);

		assertInstanceof(ReactTextInputControl.class,
			createControl(field, new AbstractFieldModel("A"), FieldControlRegistry.TEXT));
	}

	/**
	 * A compact field holding several values edited one at a time is compacted: the list of
	 * controls is as high as there are values.
	 */
	public void testCompactValueListIsCompacted() {
		FieldSpec field = FieldSpec.of(String.class, "Names").setMultiple(true).setCompact(true);

		ReactControl control = createControl(field, new AbstractFieldModel(List.of("A", "B\nmore", "C")),
			FieldControlRegistry.TEXT);

		ReactCompactFieldControl compact = assertInstanceof(ReactCompactFieldControl.class, control);
		assertEquals("Each value is previewed by the element control's provider", "A, B, C",
			compact.getPreviewText());
	}

	/**
	 * A compact field holding several values edited as a whole keeps its control, a multi-select
	 * dropdown for instance.
	 */
	public void testCompactCollectionEditorKeepsItsControl() {
		FieldSpec field = FieldSpec.of(String.class, "Names").setMultiple(true).setCompact(true);
		ReactFieldControlProvider collectionEditor = new ReactFieldControlProvider() {
			@Override
			public ReactControl createControl(ReactContext context, FieldSpec spec, FieldModel model) {
				return new ReactTextInputControl(context, model);
			}

			@Override
			public boolean editsCollections() {
				return true;
			}
		};

		assertInstanceof(ReactTextInputControl.class,
			createControl(field, new AbstractFieldModel(List.of("A")), collectionEditor));
	}

	/**
	 * The preview follows the value of the field.
	 */
	public void testPreviewFollowsTheValue() {
		FieldSpec field = FieldSpec.of(String.class, "Notes").setMultilineRows(5).setCompact(true);
		AbstractFieldModel model = new AbstractFieldModel("A");
		ReactCompactFieldControl compact = assertInstanceof(ReactCompactFieldControl.class,
			createControl(field, model, FieldControlRegistry.TEXT));

		model.setValue("B\nC");
		assertEquals("B", compact.getPreviewText());

		compact.cleanupTree();
		model.setValue("D");
		assertEquals("A disposed control no longer listens", "B", compact.getPreviewText());
	}

	/**
	 * The dialog displays the full control, created for the field with all the room it needs.
	 */
	public void testDialogShowsTheFullControl() {
		FieldSpec field = FieldSpec.of(String.class, "Notes").setMultilineRows(5).setCompact(true);
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = assertInstanceof(ReactCompactFieldControl.class,
			createControl(field, new AbstractFieldModel("A"), provider));

		open(compact);

		assertNotNull("The dialog is open", _dialogs._open);
		assertNotNull("The full control is created", provider._lastField);
		assertFalse("The full control is not compact", provider._lastField.isCompact());
		assertEquals(5, provider._lastField.getMultilineRows());
		assertTrue("The field description shared by the caller is untouched", field.isCompact());
	}

	/**
	 * OK writes the edited copy to the field.
	 */
	public void testOkWritesTheValue() {
		AbstractFieldModel model = new AbstractFieldModel("A");
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = compactText(model, provider);

		open(compact);
		provider._lastModel.setValue("B");
		assertEquals("The field is unchanged while the dialog is open", "A", model.getValue());

		List<ReactButtonControl> buttons = dialogButtons();
		assertEquals("Cancel and OK", 2, buttons.size());
		buttons.get(1).executeCommand(CLICK, Map.of());

		assertEquals("B", model.getValue());
		assertEquals("B", compact.getPreviewText());
		assertNull("The dialog is closed", _dialogs._open);
	}

	/**
	 * Cancel discards the edited copy.
	 */
	public void testCancelDiscardsTheValue() {
		AbstractFieldModel model = new AbstractFieldModel("A");
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = compactText(model, provider);

		open(compact);
		provider._lastModel.setValue("B");
		dialogButtons().get(0).executeCommand(CLICK, Map.of());

		assertEquals("A", model.getValue());
		assertNull("The dialog is closed", _dialogs._open);
	}

	/**
	 * The dialog edits a copy of a collection, not the collection the field holds.
	 */
	public void testCollectionIsBuffered() {
		List<String> values = new ArrayList<>(List.of("A", "B"));
		AbstractFieldModel model = new AbstractFieldModel(values);
		FieldSpec field = FieldSpec.of(String.class, "Names").setMultiple(true).setCompact(true);
		ReactCompactFieldControl compact = assertInstanceof(ReactCompactFieldControl.class,
			createControl(field, model, FieldControlRegistry.TEXT));

		open(compact);

		ReactValueListControl list = assertInstanceof(ReactValueListControl.class, dialogEditor());
		Object buffered = list.getFieldModel().getValue();
		assertEquals(values, buffered);
		assertNotSame("The dialog edits a copy", values, buffered);
	}

	/**
	 * A field that may not be edited is displayed read-only, with nothing but a button closing
	 * the dialog.
	 */
	public void testReadOnlyFieldIsOnlyDisplayed() {
		AbstractFieldModel model = new AbstractFieldModel("A");
		model.setEditable(false);
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = compactText(model, provider);

		open(compact);

		assertFalse("The editor is read-only", provider._lastModel.isEditable());
		List<ReactButtonControl> buttons = dialogButtons();
		assertEquals("Only Close", 1, buttons.size());
		buttons.get(0).executeCommand(CLICK, Map.of());
		assertEquals("A", model.getValue());
		assertNull("The dialog is closed", _dialogs._open);
	}

	/**
	 * A field that stopped being editable while the dialog was open is not written.
	 */
	public void testOkRefusedWhenNoLongerEditable() {
		AbstractFieldModel model = new AbstractFieldModel("A");
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = compactText(model, provider);

		open(compact);
		provider._lastModel.setValue("B");
		model.setEditable(false);
		dialogButtons().get(1).executeCommand(CLICK, Map.of());

		assertEquals("A", model.getValue());
	}

	/**
	 * OK is refused while the editor reports an error: the field is unchanged and the dialog stays
	 * open for the user to correct the input.
	 */
	public void testOkRefusedOnInputError() {
		AbstractFieldModel model = new AbstractFieldModel("A");
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = compactText(model, provider);

		open(compact);
		ReactControl dialog = _dialogs._open;
		AbstractFieldModel buffer = (AbstractFieldModel) provider._lastModel;
		buffer.setValue("B");
		buffer.setError(ResKey.text("Not a valid input."));
		dialogButtons().get(1).executeCommand(CLICK, Map.of());

		assertEquals("A", model.getValue());
		assertSame("The dialog stays open", dialog, _dialogs._open);

		buffer.setError(null);
		dialogButtons().get(1).executeCommand(CLICK, Map.of());
		assertEquals("A corrected input is written", "B", model.getValue());
		assertNull("The dialog is closed", _dialogs._open);
	}

	/**
	 * OK is refused while a mandatory field is empty, and the editor shows why.
	 */
	public void testOkRefusedOnMissingMandatoryValue() {
		AbstractFieldModel model = new AbstractFieldModel("A");
		model.setMandatory(true);
		RecordingProvider provider = new RecordingProvider();
		ReactCompactFieldControl compact = compactText(model, provider);

		open(compact);
		ReactControl dialog = _dialogs._open;
		FieldModel buffer = provider._lastModel;
		buffer.setValue("");
		dialogButtons().get(1).executeCommand(CLICK, Map.of());

		assertEquals("A", model.getValue());
		assertSame("The dialog stays open", dialog, _dialogs._open);
		assertTrue("The editor shows the missing value", buffer.hasError());

		buffer.setValue("B");
		assertFalse("A given value withdraws the error", buffer.hasError());
		dialogButtons().get(1).executeCommand(CLICK, Map.of());
		assertEquals("B", model.getValue());
		assertNull("The dialog is closed", _dialogs._open);
	}

	/**
	 * The default preview of a value.
	 */
	public void testDefaultPreviewText() {
		ReactFieldControlProvider provider = (context, field, model) -> null;
		FieldSpec field = FieldSpec.of(String.class, "Name");

		assertEquals("Nothing stands for no value", "", provider.previewText(field, null));
		assertEquals("A", provider.previewText(field, "A"));
		assertEquals("A, B", provider.previewText(field, List.of("A", "B")));
		assertEquals("Only the first line", "First", provider.previewText(field, "First\nSecond"));
		assertEquals("Only the first line", "First", provider.previewText(field, "First\r\nSecond"));
		assertEquals("", provider.previewText(field, List.of()));
	}

	/**
	 * A read-only field without a value offers no dialog: there is nothing to show in it.
	 */
	public void testReadOnlyEmptyFieldHasNoOpener() {
		for (Object empty : new Object[] { null, "", List.of() }) {
			AbstractFieldModel model = new AbstractFieldModel(empty);
			model.setEditable(false);
			ReactCompactFieldControl compact = compactText(model, FieldControlRegistry.TEXT);

			assertFalse("No opener for " + empty, compact.isOpenerShown());
			open(compact);
			assertNull("A click on the hidden opener opens nothing", _dialogs._open);
		}
	}

	/**
	 * The opener of a read-only field is offered as soon as the field holds a value, and hidden
	 * again when the value is gone.
	 */
	public void testReadOnlyOpenerFollowsTheValue() {
		AbstractFieldModel model = new AbstractFieldModel(null);
		model.setEditable(false);
		ReactCompactFieldControl compact = compactText(model, FieldControlRegistry.TEXT);

		model.setValue("A");
		assertTrue(compact.isOpenerShown());
		open(compact);
		assertNotNull("The value is shown in the dialog", _dialogs._open);

		model.setValue(null);
		assertFalse(compact.isOpenerShown());
	}

	/**
	 * An empty field that may be edited offers its editor to enter a value, also once it becomes
	 * editable.
	 */
	public void testEditableEmptyFieldHasOpener() {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactCompactFieldControl compact = compactText(model, FieldControlRegistry.TEXT);
		assertTrue(compact.isOpenerShown());

		model.setEditable(false);
		assertFalse(compact.isOpenerShown());

		model.setEditable(true);
		assertTrue(compact.isOpenerShown());
		open(compact);
		assertNotNull("The editor is offered", _dialogs._open);
	}

	/**
	 * The first line of a source text that holds more than white space.
	 */
	public void testFirstNonBlankLine() {
		assertEquals("", ReactFieldControlProvider.firstNonBlankLine(null));
		assertEquals("", ReactFieldControlProvider.firstNonBlankLine(" \n\t\n"));
		assertEquals("x + 1", ReactFieldControlProvider.firstNonBlankLine("\n  \r\n\tx + 1\ny"));
		assertEquals("x", ReactFieldControlProvider.firstNonBlankLine("x"));
	}

	private ReactCompactFieldControl compactText(AbstractFieldModel model, ReactFieldControlProvider provider) {
		FieldSpec field = FieldSpec.of(String.class, "Notes").setMultilineRows(5).setCompact(true);
		return assertInstanceof(ReactCompactFieldControl.class, createControl(field, model, provider));
	}

	private void open(ReactCompactFieldControl compact) {
		ReactButtonControl opener = descendants(compact, ReactButtonControl.class).get(0);
		opener.executeCommand(CLICK, Map.of());
	}

	private List<ReactButtonControl> dialogButtons() {
		assertNotNull("A dialog is open", _dialogs._open);
		return descendants(_dialogs._open, ReactButtonControl.class);
	}

	private ReactControl dialogEditor() {
		assertNotNull("A dialog is open", _dialogs._open);
		List<ReactValueListControl> lists = descendants(_dialogs._open, ReactValueListControl.class);
		return lists.isEmpty() ? null : lists.get(0);
	}

	private static <T> List<T> descendants(ReactControl root, Class<T> type) {
		List<T> result = new ArrayList<>();
		collect(root, type, result);
		return result;
	}

	private static <T> void collect(ReactControl control, Class<T> type, List<T> result) {
		if (type.isInstance(control)) {
			result.add(type.cast(control));
			// No nested matches looked for: a found control is a leaf of the search.
			return;
		}
		for (ReactControl child : control.displayedChildren()) {
			collect(child, type, result);
		}
	}

	private ReactControl createControl(FieldSpec field, FieldModel model, ReactFieldControlProvider provider) {
		return FieldControlRegistry.getInstance().createControl(_context, field, model, provider);
	}

	private static <T> T assertInstanceof(Class<T> expected, Object actual) {
		assertTrue("Expected a " + expected.getSimpleName() + ", got: " + actual,
			expected.isInstance(actual));
		return expected.cast(actual);
	}

	/**
	 * A text provider recording the field and model of the last control it created.
	 */
	private static final class RecordingProvider implements ReactFieldControlProvider {

		FieldSpec _lastField;

		FieldModel _lastModel;

		@Override
		public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
			_lastField = field;
			_lastModel = model;
			return FieldControlRegistry.TEXT.createControl(context, field, model);
		}

		@Override
		public boolean isLarge(FieldSpec field) {
			return FieldControlRegistry.TEXT.isLarge(field);
		}
	}

	/**
	 * A {@link DialogManager} holding a single open dialog.
	 */
	private static final class RecordingDialogManager implements DialogManager {

		ReactControl _open;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_open = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_open = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			// Only one dialog.
		}
	}

	/**
	 * The test suite, started with the theme a created field needs for its icons and the resource
	 * bundles it needs for its messages.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCompactField.class, ThemeFactory.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}
}
