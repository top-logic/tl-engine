/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.layout.meta.ValueTypeModel;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.configedit.ConfigCollectionValue;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigPendingEntries;
import com.top_logic.layout.configedit.ConfigPendingEntries.PendingEntry;
import com.top_logic.layout.configedit.ConfigPropertyOptions;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.view.form.AnnotationsFieldControlProvider;
import com.top_logic.layout.view.form.BinaryControlProvider;
import com.top_logic.layout.view.form.BooleanControlProvider;
import com.top_logic.layout.view.form.ColorInputControlProvider;
import com.top_logic.layout.view.form.ConfigFieldControlProvider;
import com.top_logic.layout.view.form.DatePickerControlProvider;
import com.top_logic.layout.view.form.I18NStringControlProvider;
import com.top_logic.layout.view.form.IconSelectControlProvider;
import com.top_logic.layout.view.form.NumberInputControlProvider;
import com.top_logic.layout.view.form.PasswordInputControlProvider;
import com.top_logic.layout.view.form.SelectControlProvider;
import com.top_logic.layout.view.form.TLInputControl;
import com.top_logic.layout.view.form.TextInputControlProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests the implementations {@link TLInputControl#getImpl()} offers in the model editor: the
 * controls fitting the values of the annotated attribute or type.
 *
 * <p>
 * The annotation is added as the list editor of the model editor adds it: pending
 * ({@link ConfigPendingEntries}) for the container the annotations are edited in
 * ({@link AnnotationsFieldControlProvider#annotationContainerFor(com.top_logic.model.TLObject)}),
 * and its options are resolved as the configuration editor resolves them
 * ({@link ConfigPropertyOptions}), so the navigation from the annotation to the annotated element
 * is part of what is tested.
 * </p>
 */
public class TestInputControlOptions extends BasicTestCase {

	/** Qualified name of the core date type. */
	private static final String DATE = TypeSpec.DATE_TYPE;

	/** Qualified name of the core string type. */
	private static final String STRING = TypeSpec.STRING_TYPE;

	/** Qualified name of the core integer type. */
	private static final String INTEGER = TypeSpec.INTEGER_TYPE;

	/** Qualified name of the core binary type. */
	private static final String BINARY = TypeSpec.BINARY_TYPE;

	/** Qualified name of the core boolean type. */
	private static final String BOOLEAN = "tl.core:Boolean";

	/**
	 * A date type offers the date picker, not the number input.
	 */
	public void testDateType() {
		List<Object> options = optionsForType(DATE);

		assertContains(options, DatePickerControlProvider.class);
		assertNotContains(options, NumberInputControlProvider.class);
		assertNotContains(options, TextInputControlProvider.class);
	}

	/**
	 * The color type offers the color selection, not the icon selection.
	 */
	public void testColorType() {
		List<Object> options = optionsForType(TypeSpec.COLOR_TYPE);

		assertContains(options, ColorInputControlProvider.class);
		assertNotContains(options, IconSelectControlProvider.class);
		assertNotContains(options, TextInputControlProvider.class);
	}

	/**
	 * The icon type offers the icon selection, not the color selection.
	 */
	public void testIconType() {
		List<Object> options = optionsForType(TypeSpec.ICON_TYPE);

		assertContains(options, IconSelectControlProvider.class);
		assertNotContains(options, ColorInputControlProvider.class);
	}

	/**
	 * A string type offers the text and the password input, but neither the editor of a
	 * multilingual text nor the input of another kind of value.
	 */
	public void testStringType() {
		List<Object> options = optionsForType(STRING);

		assertContains(options, TextInputControlProvider.class);
		assertContains(options, PasswordInputControlProvider.class);
		assertNotContains(options, I18NStringControlProvider.class);
		assertNotContains(options, DatePickerControlProvider.class);
	}

	/**
	 * The multilingual string type offers its editor, which is restricted to it although its kind
	 * is the one of a plain string.
	 */
	public void testI18NStringType() {
		List<Object> options = optionsForType(TypeSpec.I18N_STRING_TYPE);

		assertContains(options, I18NStringControlProvider.class);
	}

	/**
	 * Numbers, truth values and files each offer their own input.
	 */
	public void testOtherKinds() {
		assertContains(optionsForType(INTEGER), NumberInputControlProvider.class);
		assertContains(optionsForType(BOOLEAN), BooleanControlProvider.class);
		assertContains(optionsForType(BINARY), BinaryControlProvider.class);
		assertNotContains(optionsForType(BINARY), TextInputControlProvider.class);
	}

	/**
	 * A selection fits a value of any type, given options to choose from.
	 */
	public void testSelectionIsOfferedForEveryType() {
		for (String type : List.of(DATE, STRING, BINARY, TypeSpec.COLOR_TYPE)) {
			assertContains(optionsForType(type), SelectControlProvider.class);
		}
	}

	/**
	 * The editors of configuration values and model annotations are never offered.
	 */
	public void testInternalEditorsAreNeverOffered() {
		for (String type : List.of(DATE, STRING, INTEGER, BOOLEAN, BINARY, TypeSpec.COLOR_TYPE)) {
			List<Object> options = optionsForType(type);
			assertNotContains(options, ConfigFieldControlProvider.class);
			assertNotContains(options, AnnotationsFieldControlProvider.class);
		}
	}

	/**
	 * At an attribute, the options are the ones of the attribute's value type: the annotation
	 * reaches the value type through the part model the model editor builds around it.
	 */
	public void testDateAttribute() {
		TLStructuredTypePart attribute = someAttributeOfType(DATE);
		TLInputControl annotation = annotate(attribute);

		ValueTypeModel annotated = (ValueTypeModel) annotation.getAnnotated();
		assertSame("The attribute's value type must be resolved.", attribute.getType(),
			annotated.getResolvedType());
		assertEquals(TLTypeKind.DATE, annotated.getTypeKind());

		List<Object> options = options(annotation);
		assertContains(options, DatePickerControlProvider.class);
		assertNotContains(options, NumberInputControlProvider.class);
	}

	/**
	 * A string attribute offers the text input, not the date picker.
	 */
	public void testStringAttribute() {
		List<Object> options = options(annotate(someAttributeOfType(STRING)));

		assertContains(options, TextInputControlProvider.class);
		assertNotContains(options, DatePickerControlProvider.class);
	}

	/**
	 * At a type, the options are the ones of the type itself.
	 */
	public void testTypeAnnotationResolvesTheTypeItself() {
		TLType type = TLModelUtil.findType(TypeSpec.COLOR_TYPE);
		TLInputControl annotation = annotate(type);

		ValueTypeModel annotated = (ValueTypeModel) annotation.getAnnotated();
		assertSame(type, annotated.getResolvedType());
		assertEquals(TLTypeKind.CUSTOM, annotated.getTypeKind());
	}

	/**
	 * A confirmed annotation is part of the container it was pending for, and still offers what
	 * fits the annotated attribute.
	 */
	public void testConfirmedAnnotation() {
		TLStructuredTypePart attribute = someAttributeOfType(DATE);
		AnnotatedConfig<? extends TLAnnotation> container =
			AnnotationsFieldControlProvider.annotationContainerFor(attribute);
		PropertyDescriptor annotations = container.descriptor().getProperty(AnnotatedConfig.ANNOTATIONS);
		ConfigPendingEntries pending = new ConfigPendingEntries(new ConfigCollectionValue(container, annotations),
			entry -> {
				// Nothing is displayed.
			});
		PendingEntry entry = pending.start(TypedConfiguration.newConfigItem(TLInputControl.class));
		assertSame("A pending annotation must already see the element it is added to.", container,
			((TLInputControl) entry.entry()).getAnnotated());

		assertNull(pending.confirm(entry));

		TLInputControl annotation = (TLInputControl) entry.entry();
		assertTrue(container.getAnnotations().contains(annotation));
		assertSame(container, annotation.getAnnotated());
		assertContains(options(annotation), DatePickerControlProvider.class);
	}

	/**
	 * A discarded annotation is attached nowhere.
	 */
	public void testDiscardedAnnotation() {
		TLStructuredTypePart attribute = someAttributeOfType(DATE);
		ConfigPendingEntries pending = pendingEntries(attribute);
		PendingEntry entry = pending.start(TypedConfiguration.newConfigItem(TLInputControl.class));

		pending.discard(entry);

		assertNull(((TLInputControl) entry.entry()).getAnnotated());
	}

	private List<Object> optionsForType(String qualifiedName) {
		return options(annotate(TLModelUtil.findType(qualifiedName)));
	}

	/**
	 * A new {@link TLInputControl} in the editor of the annotations of the given element, as it
	 * is when the user has just added it: pending in the list editor, not yet confirmed into the
	 * container the model editor edits the annotations in.
	 */
	private static TLInputControl annotate(TLModelPart element) {
		return (TLInputControl) startPending(element).entry();
	}

	/**
	 * Starts a new pending {@link TLInputControl} in the annotations of the given element, as the
	 * list editor does when the user adds an annotation.
	 */
	private static PendingEntry startPending(TLModelPart element) {
		return pendingEntries(element).start(TypedConfiguration.newConfigItem(TLInputControl.class));
	}

	private static ConfigPendingEntries pendingEntries(TLModelPart element) {
		AnnotatedConfig<? extends TLAnnotation> container =
			AnnotationsFieldControlProvider.annotationContainerFor(element);
		PropertyDescriptor annotations = container.descriptor().getProperty(AnnotatedConfig.ANNOTATIONS);
		return new ConfigPendingEntries(new ConfigCollectionValue(container, annotations), entry -> {
			// Nothing is displayed.
		});
	}

	/**
	 * The implementations offered for {@link TLInputControl#getImpl()} of the given annotation.
	 */
	private static List<Object> options(TLInputControl annotation) {
		PropertyDescriptor impl = annotation.descriptor().getProperty(TLInputControl.IMPL_PROPERTY);
		return new ArrayList<>(ConfigPropertyOptions.optionsFor(annotation, impl));
	}

	/**
	 * Some attribute of the application model holding values of the given type.
	 */
	private static TLStructuredTypePart someAttributeOfType(String qualifiedTypeName) {
		TLType type = TLModelUtil.findType(qualifiedTypeName);
		for (TLModule module : type.getModel().getModules()) {
			for (TLClass clazz : module.getClasses()) {
				for (TLStructuredTypePart part : clazz.getLocalClassParts()) {
					if (part.getType() == type) {
						return part;
					}
				}
			}
		}
		fail("No attribute of type " + qualifiedTypeName + " in the application model.");
		return null;
	}

	private static void assertContains(List<Object> options, Class<?> expected) {
		assertTrue("Expected " + expected.getSimpleName() + " in " + options, options.contains(expected));
	}

	private static void assertNotContains(List<Object> options, Class<?> unexpected) {
		assertFalse("Unexpected " + unexpected.getSimpleName() + " in " + options, options.contains(unexpected));
	}

	/**
	 * Suite with the application model and the services the configuration editor resolves options
	 * with.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestInputControlOptions.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ModelService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
