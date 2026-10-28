/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.model.CommittedModelChanges;
import test.com.top_logic.layout.view.model.CommittedModelChanges.ScopedReactContext;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.AttributeSelectFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the {@code <field>} of a reference offers the instances of the class it refers to as
 * they are now, while it is displayed: an instance created after the field was built is offered, a
 * deleted one no longer.
 *
 * <p>
 * The form displays a class of the application model, the field its {@link #MODULE module}, a
 * reference to the instances of {@link #MODULE_TYPE}. The test creates and deletes modules in
 * committed transactions, delivered through the scope of the window the form is displayed in.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAttributeSelectFollowsModel extends TestCase {

	/** The reference of a type to the module defining it. */
	private static final String MODULE = "module";

	/** The class the {@link #MODULE} reference refers to. */
	private static final String MODULE_TYPE = "tl.model:TLModule";

	/** The class of the displayed object. */
	private static final String DISPLAYED_TYPE = "tl.model:TLClass";

	/** Name of the module a test creates to see it offered by the field. */
	private static final String CREATED_MODULE = "test.select.created";

	/** The message the form shows when it displays no object. */
	private static final String NO_MODEL_MESSAGE = "no model";

	private GlobalModelEventForwarder _scope;

	private ReactContext _context;

	private TLObject _displayed;

	private FormControl _form;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scope = CommittedModelChanges.modelScope();
		_context = new ScopedReactContext(_scope);
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		_displayed = null;
		_context = null;
		_scope = null;
		super.tearDown();
	}

	/**
	 * Runs the test in an interaction: the form reads and edits objects of the application model.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> inSystemContext(TestAttributeSelectFollowsModel.class,
			() -> {
				_displayed = (TLObject) TLModelUtil.findType(DISPLAYED_TYPE);
				_form = new FormControl(_context, _displayed, NO_MODEL_MESSAGE, NoTokenHandling.INSTANCE);
				_form.setModelScope(_scope);
				_form.attach();
				super.runTest();
				return null;
			});
	}

	/**
	 * A displayed reference field offers an instance created meanwhile, and no longer offers it once
	 * it is deleted.
	 */
	public void testAReferenceFieldFollowsTheInstancesOfItsClass() {
		AttributeFieldControl field = displayedField();
		assertFalse("The module does not exist yet.", offeredNames(field).contains(CREATED_MODULE));

		TLModule module = CommittedModelChanges.createModule(_scope, CREATED_MODULE);
		assertTrue("A created instance is offered at once: " + offeredNames(field),
			offeredNames(field).contains(CREATED_MODULE));

		CommittedModelChanges.deleteModule(_scope, module);
		assertFalse("A deleted instance is no longer offered.", offeredNames(field).contains(CREATED_MODULE));
	}

	/**
	 * A field that is not displayed does not follow the model, and catches up with what happened
	 * meanwhile as soon as it is displayed again.
	 */
	public void testAHiddenFieldCatchesUpWhenDisplayedAgain() {
		AttributeFieldControl field = displayedField();
		field.getChromeControl().detach();

		TLModule module = CommittedModelChanges.createModule(_scope, CREATED_MODULE);
		try {
			assertFalse("A hidden field offers what it offered when it was left.",
				offeredNames(field).contains(CREATED_MODULE));

			field.getChromeControl().attach();
			assertTrue("The field displayed again offers the instance created while it was hidden.",
				offeredNames(field).contains(CREATED_MODULE));
		} finally {
			CommittedModelChanges.deleteModule(_scope, module);
		}
	}

	/**
	 * While the form is edited, a created instance is offered and the value the user selected
	 * stays selected.
	 */
	public void testAnEditedFieldKeepsTheSelection() {
		AttributeFieldControl field = displayedField();
		assertTrue(_form.enterEditMode());
		SelectFieldModel model = selectModel(field);
		Object selected = otherOption(model);
		model.setValue(selected);
		assertTrue("The selection is a change of the user.", field.isDirty());

		TLModule module = CommittedModelChanges.createModule(_scope, CREATED_MODULE);
		try {
			assertTrue("A created instance is offered while the form is edited: " + offeredNames(field),
				offeredNames(field).contains(CREATED_MODULE));
			assertEquals("The selection of the user stays as it is.", selected, model.getValue());
			assertTrue("The selection is still to be saved.", field.isDirty());
		} finally {
			_form.executeCancel();
			CommittedModelChanges.deleteModule(_scope, module);
		}
	}

	/**
	 * A model change computes the options of a displayed field once, and a field built on a value
	 * that does not refer to a class observes nothing.
	 */
	public void testAModelChangeComputesTheOptionsOnce() {
		TLStructuredTypePart part = modulePart();
		AtomicInteger computations = new AtomicInteger();
		AttributeSelectFieldModel.OptionSource counting = (self, overlays, dependencies) -> {
			computations.incrementAndGet();
			return AttributeOptions.optionsFor(self, part, overlays, dependencies);
		};
		Set<TLStructuredType> moduleType = Set.of((TLClass) TLModelUtil.findType(MODULE_TYPE));
		AttributeSelectFieldModel observing = new AttributeSelectFieldModel(_displayed, part, _form, counting,
			moduleType);
		ReactControl control = FieldControlService.getInstance().createFieldControl(_context, part, observing);
		control.attach();

		AtomicInteger unobservedComputations = new AtomicInteger();
		AttributeSelectFieldModel unobserving = new AttributeSelectFieldModel(_displayed, part, _form,
			(self, overlays, dependencies) -> {
				unobservedComputations.incrementAndGet();
				return AttributeOptions.optionsFor(self, part, overlays, dependencies);
			});
		ReactControl unobservingControl =
			FieldControlService.getInstance().createFieldControl(_context, part, unobserving);
		unobservingControl.attach();

		computations.set(0);
		unobservedComputations.set(0);
		TLModule module = CommittedModelChanges.createModule(_scope, CREATED_MODULE);
		try {
			assertEquals("A model change computes the options once.", 1, computations.get());
			assertTrue(names(observing.getOptions()).contains(CREATED_MODULE));
			assertEquals("A field observing no type does not follow the model.", 0, unobservedComputations.get());
		} finally {
			control.detach();
			unobservingControl.detach();
			observing.dispose();
			unobserving.dispose();
			CommittedModelChanges.deleteModule(_scope, module);
		}
	}

	/**
	 * The field of the {@link #MODULE} reference of the displayed class, displayed.
	 */
	private AttributeFieldControl displayedField() {
		AttributeFieldControl result =
			new AttributeFieldControl(_context, _form, _form, MODULE, null, false, null);
		result.createChromeControl();
		result.getChromeControl().attach();
		assertTrue("A reference is chosen from options.", selectModel(result) != null);
		return result;
	}

	private TLStructuredTypePart modulePart() {
		return _displayed.tType().getPart(MODULE);
	}

	/**
	 * The model of the select input of the given field.
	 */
	private static SelectFieldModel selectModel(AttributeFieldControl field) {
		ReactFormFieldControl input = (ReactFormFieldControl) field.getInnerControl();
		return (SelectFieldModel) input.getFieldModel();
	}

	/**
	 * An option other than the value of the given field.
	 */
	private static Object otherOption(SelectFieldModel model) {
		Object value = model.getValue();
		for (Object option : model.getOptions()) {
			if (option != value) {
				return option;
			}
		}
		throw new AssertionError("The field offers a single option only: " + model.getOptions());
	}

	/**
	 * The names of the modules the given field offers.
	 */
	private static List<String> offeredNames(AttributeFieldControl field) {
		return names(selectModel(field).getOptions());
	}

	private static List<String> names(List<?> modules) {
		return modules.stream().map(module -> ((TLModule) module).getName()).toList();
	}

	/**
	 * Test suite requiring a knowledge base holding the application model, and the
	 * {@link FieldControlService} building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAttributeSelectFollowsModel.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}

}
