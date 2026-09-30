/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a {@link FormControl} in edit mode follows a change stored to its object by someone
 * else than the form, e.g. by a command run while the form is being edited.
 *
 * <p>
 * A field the user has left alone shows the stored value and stays unchanged; a field the user has
 * changed keeps the user's value, which the next save writes. The change is delivered by the
 * {@link ModelScope} the form observes its object in, as the scope of a browser window delivers it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFormFollowsStoredChanges extends BasicTestCase {

	/** Name of the attribute the user edits in the tests. */
	private static final String TITLE = "title";

	/** Name of the attribute the user leaves alone in the tests. */
	private static final String STATUS = "status";

	/** Key of the value an input shows, see {@link ReactFormFieldControl#VALUE}. */
	private static final String SHOWN_VALUE = "value";

	private ReactContext _context;

	private TLClass _ticketType;

	private RecordingScope _scope;

	private IdentifiedObject _ticket;

	private FormControl _form;

	private AttributeFieldControl _title;

	private AttributeFieldControl _status;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.formFollowsStoredChanges");
		TLPrimitive text =
			TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		_ticketType = TLModelUtil.addClass(module, "Ticket");
		TLModelUtil.addProperty(_ticketType, TITLE, text);
		TLModelUtil.addProperty(_ticketType, STATUS, text);

		_ticket = new IdentifiedObject(_ticketType);
		_ticket.tUpdateByName(TITLE, "Login fails");
		_ticket.tUpdateByName(STATUS, "open");

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_scope = new RecordingScope();

		_form = new FormControl(_context, _ticket, "no model", NoTokenHandling.INSTANCE);
		_form.setModelScope(_scope);
		_title = field(TITLE);
		_status = field(STATUS);
		_form.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		_title = null;
		_status = null;
		_ticket = null;
		_scope = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * A stored change shows in a field the user has left alone, while the field the user has
	 * changed keeps the user's value, which the save writes over the stored one.
	 */
	public void testUntouchedFieldsFollowStoredChanges() {
		_form.enterEditMode();
		model(_title).setValue("Login fails on Mondays");

		storeChange("Login broken", "closed");

		assertEquals("A field the user has left alone shows the stored value.",
			"closed", shown(_status));
		assertFalse("A field showing the stored value holds no change of the user.", _status.isDirty());
		assertEquals("A field the user has changed keeps the user's value.",
			"Login fails on Mondays", shown(_title));
		assertTrue("The user's change is still to be saved.", _title.isDirty());
		assertTrue(_form.isDirty());

		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertEquals("The save writes the user's value.",
			"Login fails on Mondays", _ticket.tValueByName(TITLE));
		assertEquals("The save keeps the stored value of the field the user has left alone.",
			"closed", _ticket.tValueByName(STATUS));
	}

	/**
	 * A field the user has changed and changed back holds no change, so it follows a stored
	 * change, and the save does not write the value the field started with over it.
	 */
	public void testRevertedFieldFollowsStoredChanges() {
		_form.enterEditMode();
		model(_status).setValue("in progress");
		model(_status).setValue("open");
		assertFalse("Changing a field back leaves it unchanged.", _status.isDirty());

		storeChange("Login fails", "closed");

		assertEquals("closed", shown(_status));
		assertFalse(_status.isDirty());
		assertFalse("Nothing is left to save.", _form.isDirty());

		_form.executeSave();

		assertEquals("The save does not write the value the field started with.",
			"closed", _ticket.tValueByName(STATUS));
	}

	/**
	 * A field whose input was rejected holds text the user typed, which a stored change must not
	 * replace.
	 */
	public void testFieldWithRejectedInputKeepsIt() {
		_form.enterEditMode();
		ResKey inputError = ResKey.text("Not a status.");
		model(_status).setError(inputError);

		storeChange("Login fails", "closed");

		assertEquals("A field showing a rejected input keeps it.", "open", shown(_status));
		assertEquals(inputError, model(_status).getInputError());
	}

	/**
	 * In view mode, the fields show the stored values.
	 */
	public void testViewModeShowsStoredChanges() {
		storeChange("Login broken", "closed");

		assertEquals("Login broken", shown(_title));
		assertEquals("closed", shown(_status));
		assertFalse(_title.isDirty());
		assertFalse(_status.isDirty());
	}

	/**
	 * Stores the given values to the displayed ticket, as a command of the same window does, and
	 * delivers the resulting change through the window's scope.
	 */
	private void storeChange(String title, String status) {
		_ticket.tUpdateByName(TITLE, title);
		_ticket.tUpdateByName(STATUS, status);
		_scope.reportUpdate(_ticket);
	}

	private AttributeFieldControl field(String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(_context, _form, _form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	/**
	 * The value the input of the given field shows, as the client receives it.
	 */
	private static Object shown(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(SHOWN_VALUE);
	}

	/**
	 * The model of the input the given field is edited in, as the input control writes the user's
	 * entries to it.
	 */
	private static AbstractFieldModel model(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	/**
	 * A mapping storing the values as they are, as the core datatypes of that application type use
	 * it.
	 */
	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Object with an identity that is not transient, which is what a form observes in its scope.
	 */
	private static class IdentifiedObject extends TransientTLObjectImpl {

		private static final MOClassImpl TABLE = new MOClassImpl("test");

		private static long _nextId = 1;

		private final ObjectKey _id =
			new DefaultObjectKey(1, Revision.CURRENT_REV, TABLE, LongID.valueOf(_nextId++));

		IdentifiedObject(TLStructuredType type) {
			super(type, null);
		}

		@Override
		public ObjectKey tId() {
			return _id;
		}

		@Override
		public boolean tTransient() {
			return false;
		}
	}

	/**
	 * {@link ModelScope} the test delivers object changes through, standing in for the scope of a
	 * browser window.
	 */
	private static class RecordingScope implements ModelScope {

		private final Map<ObjectKey, Set<ModelListener>> _objectListeners = new LinkedHashMap<>();

		@Override
		public boolean addModelListener(ModelListener listener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addModelListener(TLStructuredType type, ModelListener listener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addModelListener(TLObject object, ModelListener listener) {
			return _objectListeners.computeIfAbsent(object.tId(), x -> new LinkedHashSet<>()).add(listener);
		}

		@Override
		public boolean removeModelListener(ModelListener listener) {
			return false;
		}

		@Override
		public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
			return false;
		}

		@Override
		public boolean removeModelListener(TLObject object, ModelListener listener) {
			Set<ModelListener> listeners = _objectListeners.get(object.tId());
			return listeners != null && listeners.remove(listener);
		}

		/**
		 * Reports the given object as updated to everybody listening for it.
		 */
		void reportUpdate(TLObject object) {
			Set<ModelListener> listeners = _objectListeners.getOrDefault(object.tId(), Set.of());
			ModelChangeEvent event = new UpdateOf(object);
			for (ModelListener listener : Set.copyOf(listeners)) {
				listener.notifyChange(event);
			}
		}
	}

	/**
	 * The change of a single object, as a {@link ModelScope} reports it.
	 *
	 * @param object
	 *        The object that was updated.
	 */
	private record UpdateOf(TLObject object) implements ModelChangeEvent {

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return existingObject == object ? ChangeType.UPDATED : ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return Stream.of(object);
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return object.tType() == type ? getUpdated() : Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return Stream.empty();
		}
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormFollowsStoredChanges.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
