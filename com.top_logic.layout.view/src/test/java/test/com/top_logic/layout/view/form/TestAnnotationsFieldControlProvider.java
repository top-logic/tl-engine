/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.layout.view.form.AnnotationsFieldControlProvider;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.JavaPackage;

/**
 * Tests for {@link AnnotationsFieldControlProvider} - the annotations of a model element, edited in
 * a container built for them.
 */
public class TestAnnotationsFieldControlProvider extends TestCase {

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/** One concrete module annotation to edit - the base interface cannot be instantiated. */
	private static TLModuleAnnotation someAnnotation() {
		return TypedConfiguration.newConfigItem(JavaPackage.class);
	}

	private ModuleConfig container() {
		return TypedConfiguration.newConfigItem(ModuleConfig.class);
	}

	/** The first button anywhere below the given control carrying the given label. */
	private ReactButtonControl buttonLabelled(ReactControl control, String label) {
		if (control instanceof ReactButtonControl button
			&& label.equals(button.scriptingScalarState().get("label"))) {
			return button;
		}
		for (ReactControl child : control.scriptingChildren()) {
			ReactButtonControl found = buttonLabelled(child, label);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/** The button that adds an annotation. */
	private ReactButtonControl addButton(ReactControl editor) {
		for (ReactControl child : editor.scriptingChildren()) {
			if (child instanceof ReactButtonControl button
				&& String.valueOf(button.scriptingScalarState().get("label")).startsWith("+")) {
				return button;
			}
			ReactButtonControl found = addButton(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * The admissible annotation types come from the container's own property, so the "+" has
	 * something to create - {@link TLAnnotation} itself could never be instantiated.
	 */
	public void testTheContainerSuppliesTheAdmissibleTypes() {
		ModuleConfig container = container();
		FieldModel model = new AbstractFieldModel(new ArrayList<>());

		ReactControl editor = AnnotationsFieldControlProvider.createControl(_context, model, () -> container);
		ReactButtonControl add = addButton(editor);
		assertNotNull("An annotation list must offer a way to add one.", add);

		// Without options this would ask the collection for an element of its declared type, which
		// is the abstract TLModuleAnnotation - nothing TypedConfiguration can instantiate.
		HandlerResult result = add.executeCommand("click", Collections.emptyMap());

		assertTrue("Adding an annotation must not fail: " + result, result.isSuccess());
	}

	/** Nothing is handed to the field while the editor is merely being built. */
	public void testBuildingAloneChangesNothing() {
		List<TLAnnotation> values = new ArrayList<>();
		FieldModel model = new AbstractFieldModel(values);

		AnnotationsFieldControlProvider.createControl(_context, model, this::container);

		assertSame("An untouched form must not look edited.", values, model.getValue());
	}

	/** The container holds copies of what the element has, so the element is left alone. */
	public void testTheAnnotationsAreCopiedIntoTheContainer() {
		TLModuleAnnotation original = someAnnotation();
		List<TLAnnotation> values = new ArrayList<>(List.of(original));
		FieldModel model = new AbstractFieldModel(values);
		ModuleConfig container = container();

		AnnotationsFieldControlProvider.createControl(_context, model, () -> container);

		assertEquals("The container must have taken the annotation over.", 1, container.getAnnotations().size());
		assertNotSame("...as a copy, not as the element's own object.",
			original, container.getAnnotations().iterator().next());
	}

	/** An element with no annotations yet starts with an empty container. */
	public void testAnUnannotatedElement() {
		ModuleConfig container = container();

		AnnotationsFieldControlProvider.createControl(_context, new AbstractFieldModel(null), () -> container);

		assertEquals(0, container.getAnnotations().size());
	}

	/**
	 * A value the field is given from elsewhere rebuilds the editor.
	 *
	 * <p>
	 * A field control is not rebuilt when the form is shown a different object - the field model is
	 * rebound instead. An editor that read the annotations once would go on showing the previous
	 * element's.
	 * </p>
	 */
	public void testAValueFromOutsideRebuildsTheEditor() {
		List<ModuleConfig> built = new ArrayList<>();
		FieldModel model = new AbstractFieldModel(new ArrayList<>());

		AnnotationsFieldControlProvider.createControl(_context, model, () -> {
			ModuleConfig fresh = container();
			built.add(fresh);
			return fresh;
		});
		assertEquals("Built once to begin with.", 1, built.size());

		model.setValue(new ArrayList<>(List.of(someAnnotation())));

		assertEquals("The editor must have been built afresh.", 2, built.size());
		assertEquals("...over what the field holds now.", 1, built.get(1).getAnnotations().size());
	}

	/**
	 * What the editor itself writes back must not count as a change from outside.
	 *
	 * <p>
	 * Without that distinction, adding an annotation would hand the field a new value, the field
	 * would report a change, and the editor would rebuild itself out from under the user - losing
	 * the entry just added.
	 * </p>
	 */
	public void testTheEditorsOwnResultDoesNotRebuild() {
		List<ModuleConfig> built = new ArrayList<>();
		FieldModel model = new AbstractFieldModel(new ArrayList<>());

		ReactControl holder = AnnotationsFieldControlProvider.createControl(_context, model, () -> {
			ModuleConfig fresh = container();
			built.add(fresh);
			return fresh;
		});
		ReactButtonControl add = addButton(holder);
		assertNotNull(add);
		add.executeCommand("click", Collections.emptyMap());

		// A new entry is pending until confirmed, and the container is still empty while it is - so
		// only confirming changes the field's value, which is what could restart the editor.
		ReactButtonControl confirm = buttonLabelled(holder, "\u2713");
		assertNotNull("A pending entry must offer a Confirm button.", confirm);
		confirm.executeCommand("click", Collections.emptyMap());

		assertEquals("The annotation must have reached the field.",
			1, ((List<?>) model.getValue()).size());
		assertEquals("Its own write-back must not restart it.", 1, built.size());
	}

	/**
	 * An annotation whose options reach out of it can be rendered.
	 *
	 * <p>
	 * {@link TLSingletons} holds {@code SingletonConfig}s, whose type is chosen from options built by
	 * a mapping that reads the module's name off the surrounding form model. Without that model - or
	 * with a nameless container - the mapping's constructor fails and the whole field is refused with
	 * "Error during instantiation". This is the case the design warned about: an annotation copied on
	 * its own has nothing to navigate to.
	 * </p>
	 */
	public void testAnAnnotationWhoseOptionsReachOutwards() {
		TLSingletons singletons = TypedConfiguration.newConfigItem(TLSingletons.class);
		// With an entry, as mandatorStructure has: an empty collection builds no field for the
		// entry's type, so nothing would ever ask for the options that need the form model.
		SingletonConfig singleton = TypedConfiguration.newConfigItem(SingletonConfig.class);
		singleton.setName("ROOT");
		singletons.getSingletons().add(singleton);
		List<TLAnnotation> values = new ArrayList<>(List.of(singletons));
		FieldModel model = new AbstractFieldModel(values);
		ModuleConfig container = container();
		container.setName("mandatorStructure");

		ReactControl holder = AnnotationsFieldControlProvider.createControl(_context, model, () -> container);

		assertNotNull(holder);
		assertEquals("The annotation must have been taken over.", 1, container.getAnnotations().size());
	}

	/**
	 * Every container the provider can build must actually be buildable.
	 *
	 * <p>
	 * One kind working says nothing about the others: they are separate configuration interfaces,
	 * and an abstract one cannot be instantiated at all.
	 * </p>
	 */
	public void testEveryContainerKindCanBeBuilt() {
		for (Class<? extends ConfigurationItem> type : List.of(
			ModuleConfig.class, ClassConfig.class, EnumConfig.class, EnumConfig.ClassifierConfig.class,
			ReferenceConfig.class, AttributeConfig.class)) {
			try {
				ConfigurationItem container = TypedConfiguration.newConfigItem(type);
				assertNotNull(container);
				assertNotNull("A container must carry an annotations property.",
					container.descriptor().getProperty(AnnotatedConfig.ANNOTATIONS));
			} catch (RuntimeException ex) {
				fail("Cannot build the container " + type.getName() + ": " + ex.getMessage());
			}
		}
	}

	/** Suite requiring the services the configuration editor builds its fields with. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAnnotationsFieldControlProvider.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
