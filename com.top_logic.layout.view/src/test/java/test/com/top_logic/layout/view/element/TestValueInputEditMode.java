/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.meta.form.constraint.I18NConstants;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFileChipsControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.element.ValueInputElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.FormModelListener;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.Resources;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a {@link ValueInputElement} inside a form follows the form's edit mode, that an
 * {@link ValueInputElement.Config#getEditMode() edit mode channel} decides instead, and that
 * {@link ValueInputElement.Config#getReadonly() readonly} wins over both. Further, the files of a
 * multi-valued binary input: edited as file chips or as a list, offering the accepted types, and
 * reporting missing files of a mandatory input at the chips.
 */
public class TestValueInputEditMode extends TestCase {

	/** Position of the labelled text input following the edit mode of its form. */
	private static final int FOLLOWS_FORM = 0;

	/** Position of the read-only labelled text input. */
	private static final int READONLY = 1;

	/** Position of the labelled text input whose edit mode channel decides. */
	private static final int EDIT_MODE_CHANNEL = 2;

	/** Position of the read-only labelled text input naming an edit mode channel. */
	private static final int READONLY_OVER_CHANNEL = 3;

	/** Position of the labelled mandatory input of several files accepting PDF files. */
	private static final int MANDATORY_FILES = 4;

	/** Position of the input of several files asking for a list. */
	private static final int FILE_LIST = 5;

	/** The channel deciding about the edit mode of the inputs naming it. */
	private static final String EDITING = "editing";

	private ViewContext _viewContext;

	private ViewChannel _editing;

	private TestFormModel _form;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_viewContext = new DefaultViewContext(new HeadlessReactContext());
		for (String name : List.of("term", "files")) {
			_viewContext.registerChannel(name, new DefaultViewChannel(name));
		}
		_editing = new DefaultViewChannel(EDITING);
		_viewContext.registerChannel(EDITING, _editing);
		_form = new TestFormModel();
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		_editing = null;
		_viewContext = null;
		super.tearDown();
	}

	/**
	 * Runs the test in an interaction: an input resolves the type of its value in the application
	 * model, which reads the knowledge base.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContextManager.<Void, Throwable, RuntimeException> inSystemInteraction(TestValueInputEditMode.class,
			() -> {
				super.runTest();
				return null;
			});
	}

	/** Outside a form, an input can be changed. */
	public void testOutsideAFormTheInputIsEditable() throws Exception {
		assertTrue(model(chrome(FOLLOWS_FORM)).isEditable());
	}

	/** Inside a form, an input can be changed only while the form is edited. */
	public void testInsideAFormTheInputFollowsTheEditMode() throws Exception {
		_viewContext.setFormModel(_form);
		ReactFormFieldChromeControl chrome = chrome(FOLLOWS_FORM);
		FieldModel model = model(chrome);
		assertFalse("A form in view mode shows the value only.", model.isEditable());

		_form.setEditMode(true);
		assertTrue("A form in edit mode lets the value be changed.", model.isEditable());

		_form.setEditMode(false);
		assertFalse("Leaving the edit mode makes the value read-only again.", model.isEditable());

		chrome.cleanupTree();
		assertEquals("A disposed input no longer listens to the form.", 0, _form._listeners.size());
	}

	/** A read-only input cannot be changed, whatever the form's edit mode. */
	public void testReadonlyWinsOverTheForm() throws Exception {
		_viewContext.setFormModel(_form);
		_form.setEditMode(true);

		assertFalse(model(chrome(READONLY)).isEditable());
	}

	/** An edit mode channel decides instead of the form. */
	public void testTheEditModeChannelDecidesInsteadOfTheForm() throws Exception {
		_viewContext.setFormModel(_form);
		_form.setEditMode(true);
		FieldModel model = model(chrome(EDIT_MODE_CHANNEL));
		assertFalse("A channel without value lets nothing be changed, though the form is edited.",
			model.isEditable());

		_editing.set(Boolean.TRUE);
		assertTrue(model.isEditable());

		_form.setEditMode(false);
		assertTrue("The form's mode is ignored.", model.isEditable());

		_editing.set(Boolean.FALSE);
		assertFalse(model.isEditable());
	}

	/** A read-only input cannot be changed, whatever its edit mode channel says. */
	public void testReadonlyWinsOverTheEditModeChannel() throws Exception {
		_editing.set(Boolean.TRUE);

		assertFalse(model(chrome(READONLY_OVER_CHANNEL)).isEditable());
	}

	/**
	 * Several files are edited as file chips offering the accepted types; a mandatory input is
	 * marked required while it can be changed.
	 */
	public void testFilesAreEditedAsChips() throws Exception {
		_viewContext.setFormModel(_form);
		ReactFormFieldChromeControl chrome = chrome(MANDATORY_FILES);
		ReactFormFieldControl input = field(chrome);

		assertEquals(ReactFileChipsControl.class, input.getClass());
		assertEquals(".pdf", input.scriptingScalarState().get(UploadSupport.ACCEPT));
		assertFalse("A mandatory input is not marked required while the form is viewed.", required(chrome));

		_form.setEditMode(true);
		assertTrue("A mandatory input is marked required while the form is edited.", required(chrome));
	}

	/** Missing files of a mandatory input are reported at the chips until a file is added. */
	public void testMissingFilesAreReportedAtTheChips() throws Exception {
		ReactFormFieldChromeControl chrome = chrome(MANDATORY_FILES);
		FieldModel model = model(chrome);
		assertTrue(required(chrome));
		assertNull("An input the user did not touch shows no error.", error(chrome));

		model.setValue(List.of(file("a.pdf")));
		assertNull(error(chrome));

		model.setValue(List.of());
		assertEquals("Removing the last file reports the missing files at the chips.",
			Resources.getInstance().getString(I18NConstants.ERROR_MANDATORY_FIELD_EMPTY), error(chrome));

		model.setValue(List.of(file("b.pdf")));
		assertNull("A file added clears the message.", error(chrome));
	}

	/** Several files asking for a list are edited as a list of file fields. */
	public void testFilesAskingForAListAreEditedAsAList() throws Exception {
		IReactControl control = element(FILE_LIST);

		assertEquals(ReactValueListControl.class, control.getClass());
	}

	/** Whether the given chrome marks its input required. */
	private static boolean required(ReactFormFieldChromeControl chrome) {
		return Boolean.TRUE.equals(chrome.scriptingScalarState().get(FormFieldState.REQUIRED__PROP));
	}

	/** The error the given chrome shows, or {@code null}. */
	private static Object error(ReactFormFieldChromeControl chrome) {
		return chrome.scriptingScalarState().get(FormFieldState.ERROR__PROP);
	}

	/** A file of the given name. */
	private static BinaryData file(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "application/pdf", name);
	}

	/** The model of the input the given chrome labels. */
	private static FieldModel model(ReactFormFieldChromeControl chrome) {
		return field(chrome).getFieldModel();
	}

	/** The input control the given chrome labels. */
	private static ReactFormFieldControl field(ReactFormFieldChromeControl chrome) {
		List<ReactControl> children = chrome.displayedChildren();
		assertEquals("The chrome labels exactly one field: " + children, 1, children.size());
		return (ReactFormFieldControl) children.get(0);
	}

	/** The chrome built by the labelled {@code <value-input>} at the given position. */
	private ReactFormFieldChromeControl chrome(int index) throws Exception {
		IReactControl control = element(index);
		assertTrue("An input with a label is wrapped in a chrome, but is " + control,
			control instanceof ReactFormFieldChromeControl);
		return (ReactFormFieldChromeControl) control;
	}

	/** The control built by the {@code <value-input>} at the given position. */
	private IReactControl element(int index) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestValueInputEditMode.class);
		UIElement element = context.getInstance(parseInputs().get(index));
		context.checkErrors();
		return element.createControl(_viewContext);
	}

	/** The {@code <value-input>} configurations of the test view. */
	private List<PolymorphicConfiguration<? extends UIElement>> parseInputs() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestValueInputEditMode.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestValueInputEditMode.class, "test-input-edit-mode.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return ((PanelElement.Config) config.getContent()).getChildren();
	}

	/**
	 * {@link FormModel} whose edit mode the test switches.
	 */
	private static final class TestFormModel implements FormModel {

		final List<FormModelListener> _listeners = new ArrayList<>();

		private boolean _editMode;

		@Override
		public TLObject getCurrentObject() {
			return null;
		}

		@Override
		public boolean isEditMode() {
			return _editMode;
		}

		void setEditMode(boolean editMode) {
			_editMode = editMode;
			for (FormModelListener listener : new ArrayList<>(_listeners)) {
				listener.onFormStateChanged(this);
			}
		}

		@Override
		public void addFormModelListener(FormModelListener listener) {
			_listeners.add(listener);
		}

		@Override
		public void removeFormModelListener(FormModelListener listener) {
			_listeners.remove(listener);
		}
	}

	/**
	 * React context of a test that displays no persistent object, and therefore has no
	 * {@link ModelScope} to observe one in.
	 */
	private static final class HeadlessReactContext extends DefaultReactContext {

		HeadlessReactContext() {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module for resolving the element tags, the model
	 * the inputs resolve their types in, and the {@link FieldControlService} building their
	 * controls.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestValueInputEditMode.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
