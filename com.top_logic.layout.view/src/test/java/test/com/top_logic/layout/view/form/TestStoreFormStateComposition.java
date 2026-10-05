/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.layout.view.command.StoreFormStateAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.FormModelListener;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that {@link StoreFormStateAction} stores the rows added to a composition of the form
 * object, within an outer {@link WithTransactionAction} as well as on its own.
 */
public class TestStoreFormStateComposition extends AbstractModelAccessTest {

	private static final String STORE_FORM_STATE = "store-form-state";

	private static final String WITH_TRANSACTION = "with-transaction";

	private static final String NEW_TASK = "new task";

	/**
	 * A row added to a persistent form object is created when the form state is stored inside an
	 * outer transaction that commits.
	 */
	public void testStoreInOuterTransaction() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project);

		ViewAction action = action("<" + WITH_TRANSACTION + "><" + STORE_FORM_STATE + "/></" + WITH_TRANSACTION + ">");
		Object result = action.execute(viewContext(form), _project);

		assertSame(_project, result);
		assertNewTaskStored();
		assertTrue(form.isEditMode());
		assertFalse(form.isDirty());
	}

	/**
	 * Nothing is stored when the outer transaction storing the form state is rolled back.
	 */
	public void testOuterTransactionRolledBack() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project);

		ViewAction action = action("<" + STORE_FORM_STATE + "/>");
		try (Transaction tx = kb().beginTransaction()) {
			action.execute(viewContext(form), _project);
			// Leaving the outer transaction without commit rolls it back.
		}

		assertNothingStored();
	}

	/**
	 * The form state stored without an outer transaction is committed by the action itself.
	 */
	public void testStoreWithoutOuterTransaction() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project);

		ViewAction action = action("<" + STORE_FORM_STATE + "/>");
		action.execute(viewContext(form), _project);

		assertNewTaskStored();
	}

	/**
	 * A form with a validation error is refused, and no row is created.
	 */
	public void testInvalidFormStoresNothing() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project, InvalidParticipant::new);

		ViewAction action = action("<" + WITH_TRANSACTION + "><" + STORE_FORM_STATE + "/></" + WITH_TRANSACTION + ">");
		try {
			action.execute(viewContext(form), _project);
			fail("An invalid form must be refused.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS, ex.getErrorKey());
		}

		assertNothingStored();
		assertTrue(form.isEditMode());
	}

	/**
	 * A row added to a transient form object stays transient and is transferred to the form object.
	 */
	public void testTransientOwner() throws ConfigurationException {
		becomeUser(_responsible);
		TLObject project = TransientObjectFactory.INSTANCE.createObject(type(PROJECT));
		FormControl form = newForm(project);

		ViewAction action = action("<" + STORE_FORM_STATE + "/>");
		Object result = action.execute(viewContext(form), project);

		assertSame(project, result);
		List<?> rows = (List<?>) project.tValueByName(TASKS);
		assertEquals(1, rows.size());
		TLObject row = (TLObject) rows.get(0);
		assertTrue(row.tTransient());
		assertEquals(NEW_TASK, row.tValueByName(NAME));
		assertEquals(List.of(_task), taskInstances());
	}

	/**
	 * Creates a form over the given object in edit mode, with a composition control over its tasks
	 * holding a new row.
	 */
	private FormControl newForm(TLObject object) {
		return newForm(object, form -> null);
	}

	/**
	 * Creates a form over the given object in edit mode, with a composition control over its tasks
	 * holding a new row, and the listener created by the given function.
	 */
	private FormControl newForm(TLObject object, Function<FormControl, FormModelListener> listener) {
		FormControl form = new FormControl(reactContext(), object, "no model", NoTokenHandling.INSTANCE);
		FormModelListener additional = listener.apply(form);
		if (additional != null) {
			form.addFormModelListener(additional);
		}
		CompositionControl tasks = new CompositionControl(form);
		tasks.init();
		assertTrue(form.enterEditMode());
		assertNotNull(tasks.addRow(row -> row.tUpdateByName(NAME, NEW_TASK)));
		return form;
	}

	private void assertNewTaskStored() {
		List<?> rows = (List<?>) _project.tValueByName(TASKS);
		assertEquals(2, rows.size());
		assertSame(_task, rows.get(0));
		TLObject created = (TLObject) rows.get(1);
		assertFalse(created.tTransient());
		assertEquals(NEW_TASK, created.tValueByName(NAME));
	}

	private void assertNothingStored() {
		assertEquals(List.of(_task), _project.tValueByName(TASKS));
		assertEquals(List.of(_task), taskInstances());
	}

	private static List<TLObject> taskInstances() {
		return MetaElementUtil.getAllDirectInstancesOf(type(TASK), TLObject.class);
	}

	private static DefaultViewContext viewContext(FormControl form) {
		DefaultViewContext result = new DefaultViewContext(reactContext());
		result.setFormModel(form);
		return result;
	}

	private static ReactContext reactContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private static ViewAction action(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestStoreFormStateComposition.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(STORE_FORM_STATE, TypedConfiguration.getConfigurationDescriptor(StoreFormStateAction.Config.class));
		descriptors.put(WITH_TRANSACTION, TypedConfiguration.getConfigurationDescriptor(WithTransactionAction.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<? extends ViewAction> config =
			(PolymorphicConfiguration<? extends ViewAction>) reader.read();
		ViewAction result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * {@link AbstractCompositionControl} over the tasks of the form object, without a presentation.
	 */
	private static class CompositionControl extends AbstractCompositionControl {

		CompositionControl(FormControl form) {
			super(reactContext(), form, TASKS, "TLPanel");
		}

		@Override
		protected void buildContent(List<? extends TLObject> rows, boolean editMode) {
			// No presentation.
		}

		@Override
		protected void refreshRows() {
			// No presentation.
		}
	}

	/**
	 * {@link FormParticipant} reporting a validation error in every edit session of its form.
	 */
	private static class InvalidParticipant implements FormParticipant, FormModelListener {

		private final FormControl _form;

		InvalidParticipant(FormControl form) {
			_form = form;
		}

		@Override
		public void onFormStateChanged(FormModel source) {
			if (source.isEditMode()) {
				_form.registerParticipant(this);
			}
		}

		@Override
		public boolean validate() {
			return false;
		}

		@Override
		public void persist(Transaction tx) {
			fail("An invalid form must not be persisted.");
		}

		@Override
		public void applyState() {
			fail("An invalid form must not be applied.");
		}

		@Override
		public void cancel() {
			// Nothing buffered.
		}

		@Override
		public void revealAll() {
			// Nothing to reveal.
		}

		@Override
		public boolean isDirty() {
			return false;
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestStoreFormStateComposition.class);
	}

}
