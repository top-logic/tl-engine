/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.command.EnterEditModeAction;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.layout.view.command.LeaveEditModeAction;
import com.top_logic.layout.view.command.StoreFormStateAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the actions {@link EnterEditModeAction} and {@link LeaveEditModeAction} switching the mode
 * of a {@link FormControl} from an action chain.
 */
public class TestEditModeActions extends AbstractModelAccessTest {

	private static final String STORE_FORM_STATE = "store-form-state";

	private static final String WITH_TRANSACTION = "with-transaction";

	private static final String ENTER = EnterEditModeAction.Config.TAG_NAME;

	private static final String LEAVE = LeaveEditModeAction.Config.TAG_NAME;

	private static final String CHANGED_NAME = "changed name";

	/**
	 * A chain storing the form state and then leaving edit mode stores the change and ends in view
	 * mode.
	 */
	public void testStoreThenLeave() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = editingForm(_project);
		form.getOverlay().tUpdateByName(NAME, CHANGED_NAME);

		Object result = run(form,
			"<" + WITH_TRANSACTION + "><" + STORE_FORM_STATE + "/></" + WITH_TRANSACTION + ">",
			"<" + LEAVE + "/>");

		assertSame(_project, result);
		assertEquals(CHANGED_NAME, _project.tValueByName(NAME));
		assertFalse(form.isEditMode());
		assertFalse(form.isDirty());
	}

	/**
	 * Leaving edit mode with unsaved changes is refused, and the form keeps the changes in edit
	 * mode.
	 */
	public void testLeaveDirtyRefused() throws ConfigurationException {
		becomeUser(_responsible);
		Object originalName = _project.tValueByName(NAME);
		FormControl form = editingForm(_project);
		form.getOverlay().tUpdateByName(NAME, CHANGED_NAME);

		ViewAction action = action("<" + LEAVE + "/>");
		assertFails(I18NConstants.ERROR_FORM_HAS_UNSAVED_CHANGES, () -> action.execute(viewContext(form), _project));

		assertTrue(form.isEditMode());
		assertTrue(form.isDirty());
		assertEquals(CHANGED_NAME, form.getOverlay().tValueByName(NAME));
		assertEquals(originalName, _project.tValueByName(NAME));
	}

	/**
	 * Leaving edit mode has no effect on a form in view mode.
	 */
	public void testLeaveInViewMode() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project);

		ViewAction action = action("<" + LEAVE + "/>");
		Object result = action.execute(viewContext(form), _project);

		assertSame(_project, result);
		assertFalse(form.isEditMode());
	}

	/**
	 * Entering edit mode switches a form in view mode over an editable object to edit mode.
	 */
	public void testEnter() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project);

		ViewAction action = action("<" + ENTER + "/>");
		Object result = action.execute(viewContext(form), _project);

		assertSame(_project, result);
		assertTrue(form.isEditMode());
		assertNotNull(form.getOverlay());
	}

	/**
	 * Entering edit mode has no effect on a form already in edit mode: it keeps its pending change.
	 */
	public void testEnterWhileEditing() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = editingForm(_project);
		form.getOverlay().tUpdateByName(NAME, CHANGED_NAME);

		ViewAction action = action("<" + ENTER + "/>");
		Object result = action.execute(viewContext(form), _project);

		assertSame(_project, result);
		assertTrue(form.isEditMode());
		assertTrue(form.isDirty());
		assertEquals(CHANGED_NAME, form.getOverlay().tValueByName(NAME));
	}

	/**
	 * Entering edit mode on a form without an object is refused.
	 */
	public void testEnterWithoutObjectRefused() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(null);

		assertEnterRefused(form);
	}

	/**
	 * Entering edit mode is refused for a user without the right to write the displayed object.
	 */
	public void testEnterWithoutWriteRightRefused() throws ConfigurationException {
		becomeUser(_roleless);
		FormControl form = newForm(_project);

		assertEnterRefused(form);
	}

	/**
	 * Entering edit mode is refused when the edit rule of the form denies it, and the later actions
	 * of the chain do not run.
	 */
	public void testEnterDeniedByEditRuleRefused() throws ConfigurationException {
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		form.setEditRule(input -> ExecutableState.NOT_EXEC_HIDDEN);
		List<Object> laterInputs = new ArrayList<>();
		ViewAction recorder = (context, input) -> {
			laterInputs.add(input);
			return input;
		};

		List<ViewAction> actions = List.of(action("<" + ENTER + "/>"), recorder);
		assertFails(I18NConstants.ERROR_FORM_NOT_EDITABLE,
			() -> ViewActionChain.run(viewContext(form), actions, _project, null));
		assertFalse(form.isEditMode());
		assertEquals(List.of(), laterInputs);
	}

	private void assertEnterRefused(FormControl form) throws ConfigurationException {
		ViewAction action = action("<" + ENTER + "/>");
		assertFails(I18NConstants.ERROR_FORM_NOT_EDITABLE, () -> action.execute(viewContext(form), _project));
		assertFalse(form.isEditMode());
	}

	private static void assertFails(ResKey expected, Runnable action) {
		try {
			action.run();
			fail("Expected the action to be refused.");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
	}

	/**
	 * Runs the actions given by their XML definitions as one action chain on the given form, with
	 * the project as input.
	 *
	 * @return The result of the last action.
	 */
	private Object run(FormControl form, String... xmls) throws ConfigurationException {
		List<ViewAction> actions = new ArrayList<>();
		for (String xml : xmls) {
			actions.add(action(xml));
		}
		List<Object> result = new ArrayList<>();
		ViewActionChain.run(viewContext(form), actions, _project, result::add);
		assertEquals(1, result.size());
		return result.get(0);
	}

	private static FormControl newForm(TLObject object) {
		return new FormControl(reactContext(), object, "no model", NoTokenHandling.INSTANCE);
	}

	private static FormControl editingForm(TLObject object) {
		FormControl form = newForm(object);
		assertTrue(form.enterEditMode());
		return form;
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
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestEditModeActions.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(STORE_FORM_STATE, TypedConfiguration.getConfigurationDescriptor(StoreFormStateAction.Config.class));
		descriptors.put(WITH_TRANSACTION, TypedConfiguration.getConfigurationDescriptor(WithTransactionAction.Config.class));
		descriptors.put(ENTER, TypedConfiguration.getConfigurationDescriptor(EnterEditModeAction.Config.class));
		descriptors.put(LEAVE, TypedConfiguration.getConfigurationDescriptor(LeaveEditModeAction.Config.class));
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
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestEditModeActions.class);
	}

}
