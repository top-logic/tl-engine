/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.text.DateFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.form.AttributeSelectFieldModel.OptionSource;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Service that resolves the appropriate {@link ReactFieldControlProvider} for a model attribute.
 *
 * <p>
 * Resolution chain:
 * </p>
 * <ol>
 * <li>The control the display asks for.</li>
 * <li>{@link TLInputControl} annotation on the attribute or its type (via
 * {@link com.top_logic.model.annotate.DefaultStrategy.Strategy#VALUE_TYPE}).</li>
 * <li>Global type-to-provider map configured in this service.</li>
 * <li>Built-in fallback based on {@link com.top_logic.model.TLPrimitive.Kind}.</li>
 * </ol>
 *
 * <p>
 * Only <em>which</em> provider edits a value is decided here. How many values the field holds is
 * realized by the {@link FieldControlRegistry}, whichever step of the chain found the provider: a
 * field holding several values whose provider edits one value at a time is displayed as a list of
 * element controls, one per value.
 * </p>
 *
 * @implNote Every step ends in
 *           {@link FieldControlRegistry#createControl(ReactContext, FieldSpec, FieldModel, ReactFieldControlProvider)},
 *           never in {@link ReactFieldControlProvider#createControl(ReactContext, FieldSpec, FieldModel)}
 *           itself, which is what keeps type and multiplicity decided in one place.
 */
@Label("Form field controls")
@ServiceDependencies({
	ModelService.Module.class,
})
public class FieldControlService extends ConfiguredManagedClass<FieldControlService.Config> {

	/**
	 * Configuration options for {@link FieldControlService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<FieldControlService> {

		/** Property name of {@link #getValueTypeProviders()}. */
		String VALUE_TYPE_PROVIDERS = "value-type-providers";

		/**
		 * Global type-to-provider mappings keyed by model type reference.
		 */
		@Key(ProviderMapping.TYPE)
		Map<TLModelPartRef, ProviderMapping> getProviders();

		/**
		 * The controls editing values of a plain Java type.
		 *
		 * <p>
		 * A configuration property may hold a kind of value that no model type describes, a TL-Script
		 * expression for instance. Such a value is edited by the control named here, so declaring it
		 * once covers every configuration property holding that kind of value.
		 * </p>
		 */
		@Name(VALUE_TYPE_PROVIDERS)
		@EntryTag("provider")
		List<ValueTypeMapping> getValueTypeProviders();

	}

	/**
	 * A control editing the values of one Java type.
	 */
	public interface ValueTypeMapping extends ConfigurationItem {

		/** Property name of {@link #getValueType()}. */
		String VALUE_TYPE = "value-type";

		/**
		 * The type of the edited value.
		 */
		@Name(VALUE_TYPE)
		@Mandatory
		Class<?> getValueType();

		/**
		 * The control editing values of the type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();

	}

	/**
	 * A single type-to-provider mapping entry.
	 */
	public interface ProviderMapping extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The model type this mapping applies to.
		 */
		@Name(TYPE)
		TLModelPartRef getType();

		/**
		 * The control provider to use for attributes of this type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();

	}

	private final InstantiationContext _context;

	/** The controls configured for a model type, by the qualified name of that type. */
	private final Map<String, ReactFieldControlProvider> _providerByQualifiedType = new HashMap<>();

	private final ReactFieldControlProvider _selectProvider = new SelectControlProvider();

	/**
	 * Creates a {@link FieldControlService} from configuration.
	 */
	@CalledByReflection
	public FieldControlService(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			TLModelPartRef typeRef = mapping.getType();
			if (typeRef != null) {
				TLType type = typeRef.resolveType();
				if (type != null) {
					ReactFieldControlProvider provider = _context.getInstance(mapping.getImpl());
					_providerByQualifiedType.put(TLModelUtil.qualifiedName(type), provider);
					publish(type, provider);
				}
			}
		}

		for (ValueTypeMapping mapping : getConfig().getValueTypeProviders()) {
			FieldControlRegistry.getInstance()
				.register(mapping.getValueType(), _context.getInstance(mapping.getImpl()));
		}
	}

	/**
	 * Makes the control configured for a model type available for every value of that kind, so that a
	 * configuration property holding e.g. a color or an icon is edited like the matching attribute.
	 *
	 * <p>
	 * A control that edits a selection is not published: it requires the value to be held by a
	 * {@link SelectFieldModel}, which only the model side builds.
	 * </p>
	 */
	private void publish(TLType type, ReactFieldControlProvider provider) {
		if (provider instanceof SelectControlProvider) {
			return;
		}
		Class<?> valueType = valueType(type);
		if (valueType != String.class) {
			FieldControlRegistry.getInstance().register(valueType, provider);
		}
	}

	/**
	 * The configured {@link ReactFieldControlProvider} for the given type, or {@code null}.
	 */
	private ReactFieldControlProvider byType(TLType type) {
		return _providerByQualifiedType.get(TLModelUtil.qualifiedName(type));
	}

	/**
	 * Resolves and creates the appropriate input control for the given attribute.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @return A React control for the field input widget.
	 */
	public ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return createFieldControl(context, part, model, null);
	}

	/**
	 * Creates the input control for the given attribute, with the display deciding which control
	 * that is.
	 *
	 * <p>
	 * A display naming a control gets that control, whatever the model says: the attribute's own
	 * {@link TLInputControl} annotation, the rule that options are selected from a list, and the
	 * control configured for the attribute's type all step back. Where the display names none, the
	 * control is resolved as it is for every other field.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @param control
	 *        The control the display asks for, or {@code null} to let the model decide.
	 * @return A React control for the field input widget.
	 */
	public ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldModel model,
			PolymorphicConfiguration<? extends ReactFieldControlProvider> control) {
		return createFieldControl(context, part, fieldSpec(part, model), model, control);
	}

	/**
	 * Creates the input control for the given attribute, described by the given specification.
	 *
	 * <p>
	 * How many values the field holds is part of that specification and need not be what the
	 * attribute says: a column may collect the values of a single-valued attribute over several
	 * objects and show all of them in one cell.
	 * </p>
	 *
	 * @param field
	 *        What is being edited, describing the given attribute.
	 */
	private ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldSpec field,
			FieldModel model, PolymorphicConfiguration<? extends ReactFieldControlProvider> control) {
		// 0. The control the display asks for.
		if (control != null) {
			return createControl(context, field, model, _context.getInstance(control));
		}

		// 1. Annotation on attribute (includes type-level default via VALUE_TYPE strategy).
		TLInputControl annotation = part.getAnnotation(TLInputControl.class);
		if (annotation != null) {
			return createControl(context, field, model, _context.getInstance(annotation.getImpl()));
		}

		return createFieldControl(context, part.getType(), field, model);
	}

	/**
	 * Resolves and creates the input control for a value of the given model type.
	 *
	 * <p>
	 * The entry point for a value that no attribute holds - a value belonging to the view itself.
	 * The attribute-based resolution adds only the lookup of an attribute's own control annotation
	 * and continues here, so both are edited by the same control.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param type
	 *        The model type of the edited value.
	 * @param field
	 *        The description of what is edited, see
	 *        {@link #fieldSpec(TLType, AnnotationLookup, String, boolean, FieldModel)}.
	 * @param model
	 *        The field model providing value, editability, and change notifications. A
	 *        {@link SelectFieldModel} makes the value one chosen from its options.
	 * @return A React control for the field input widget.
	 */
	public ReactControl createFieldControl(ReactContext context, TLType type, FieldSpec field, FieldModel model) {
		// 1. Option-based values use a select control.
		if (model instanceof SelectFieldModel) {
			return createControl(context, field, model, _selectProvider);
		}

		// 2. Configured control by type.
		ReactFieldControlProvider mapped = byType(type);
		if (mapped != null) {
			return createControl(context, field, model, mapped);
		}

		// 3. The control registered for the kind of value the type holds. The same registry
		// serves configuration properties, so both are edited alike.
		return FieldControlRegistry.getInstance().createControl(context, field, model);
	}

	/**
	 * Creates the control the given provider builds for the given field.
	 *
	 * <p>
	 * Where a resolution step ends: the registry decides how the
	 * {@link FieldSpec#isMultiple() multiplicity} of the field is realized, so that a value list is
	 * built around a provider that edits one value at a time no matter which step found that
	 * provider.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param field
	 *        What is being edited.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @param provider
	 *        The resolved control provider.
	 */
	private static ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model,
			ReactFieldControlProvider provider) {
		return FieldControlRegistry.getInstance().createControl(context, field, model, provider);
	}

	/**
	 * Describes the given attribute for the control that edits it.
	 */
	private static FieldSpec fieldSpec(TLStructuredTypePart part, FieldModel model) {
		return fieldSpec(part, part.isMultiple(), model);
	}

	/**
	 * Describes a value of the given attribute for the control that edits it, holding as many
	 * values as stated.
	 *
	 * <p>
	 * The attribute decides whether the order of the values is part of the value: a field holding
	 * several values of an {@link TLStructuredTypePart#isOrdered() ordered} attribute is
	 * {@link FieldSpec#isOrdered() arranged} by the user, while a single value has no order to
	 * arrange.
	 * </p>
	 *
	 * @param multiple
	 *        Whether the described field holds a collection of the attribute's values rather than
	 *        a single one, see {@link ColumnType#collected()}.
	 */
	private static FieldSpec fieldSpec(TLStructuredTypePart part, boolean multiple, FieldModel model) {
		return fieldSpec(part.getType(), part, MetaLabelProvider.INSTANCE.getLabel(part), multiple, model)
			.setOrdered(multiple && part.isOrdered());
	}

	/**
	 * Describes a value for the control that edits it.
	 *
	 * @param type
	 *        The model type of the value, deciding which control edits it.
	 * @param annotations
	 *        Where the display annotations are read from: the attribute holding the value, or the
	 *        type itself where no attribute holds it. An annotation missing there is looked up at
	 *        the type.
	 * @param label
	 *        The label of the edited field, or {@code null} if it has none.
	 * @param multiple
	 *        Whether the value is a collection of values rather than a single one.
	 * @param model
	 *        The field model holding the value.
	 * @return The description to pass to {@link ReactFieldControlProvider#createControl}. Its values
	 *         are not {@link FieldSpec#isOrdered() ordered}: with no attribute holding them, nothing
	 *         stores an order the user could arrange.
	 */
	public static FieldSpec fieldSpec(TLType type, AnnotationLookup annotations, String label, boolean multiple,
			FieldModel model) {
		return FieldSpec.of(valueType(type), label)
			.setMultiple(multiple)
			.setMandatory(model.isMandatory())
			.setEditable(model.isEditable())
			.setMultilineRows(multilineRows(annotations))
			.setBooleanPresentation(booleanPresentation(annotations, type))
			.setTriState(isTriState(type))
			.setDateKind(DatePickerControlProvider.kind(annotations, type))
			.setDateFormat(dateFormat(annotations, type))
			.setNumberFormat(numberFormat(annotations, type));
	}

	/**
	 * The format a numeric attribute is displayed in and entered in, or {@code null} if the attribute
	 * holds no numbers.
	 *
	 * <p>
	 * The attribute's {@code format} annotation where it has one, the user's default format for
	 * whole respectively fractional numbers otherwise. One format serves every place the value
	 * appears: the form field editing it, the table cell showing it, the text that cell is searched
	 * by, and the bounds of that column's filter.
	 * </p>
	 *
	 * <p>
	 * The annotated format need not write digits: a duration is a number of milliseconds written as
	 * {@code 1h 30min}, and the attribute is displayed, entered and filtered in that text.
	 * </p>
	 *
	 * @param part
	 *        The model attribute, or {@code null} for an unresolved one.
	 */
	public static Format numberFormat(TLStructuredTypePart part) {
		if (part == null) {
			return null;
		}
		return numberFormat(part, part.getType());
	}

	/**
	 * The format a numeric value is displayed in and entered in, or {@code null} if the type holds
	 * no numbers.
	 *
	 * <p>
	 * The format of <em>one</em> number, which is also what a field holding several of them writes
	 * each of its values in.
	 * </p>
	 *
	 * @param annotations
	 *        Where the {@code format} annotation is read from.
	 * @param type
	 *        The model type of the value.
	 */
	public static Format numberFormat(AnnotationLookup annotations, TLType type) {
		Class<?> valueType = PrimitiveTypeUtil.asNonPrimitive(valueType(type));
		if (!Number.class.isAssignableFrom(valueType)) {
			return null;
		}
		boolean fractional = valueType == Double.class || valueType == Float.class
			|| (type instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.FLOAT);
		return numberFormat(annotations, fractional);
	}

	/**
	 * The annotated format of the given value, or the default format for its kind of number.
	 *
	 * <p>
	 * A value whose format declaration cannot be resolved is displayed in the default format
	 * instead, so that a misconfigured attribute still shows its value.
	 * </p>
	 */
	private static Format numberFormat(AnnotationLookup annotations, boolean fractional) {
		try {
			return fractional ? DisplayAnnotations.getFloatFormat(annotations)
				: DisplayAnnotations.getLongFormat(annotations);
		} catch (ConfigurationException ex) {
			Logger.error("Invalid format definition at '" + annotations + "'.", ex, FieldControlService.class);
			return defaultNumberFormat(fractional);
		}
	}

	/**
	 * The user's default format for whole respectively fractional numbers.
	 */
	private static Format defaultNumberFormat(boolean fractional) {
		Formatter formatter = HTMLFormatter.getInstance();
		return fractional ? formatter.getDoubleFormat() : formatter.getLongFormat();
	}

	/**
	 * The format a point in time held by the given attribute is displayed in, or {@code null} if
	 * the attribute holds no points in time.
	 *
	 * <p>
	 * The attribute's {@code format} annotation where it has one, the one of its type otherwise,
	 * and the user's default format for the
	 * {@link DatePickerControlProvider#kind(TLStructuredTypePart) kind} of value where neither says
	 * anything. One format serves every place the value appears: the form field showing it, the
	 * table cell, the text that cell is searched by, and the bounds of that column's filter.
	 * </p>
	 *
	 * @param part
	 *        The model attribute, or {@code null} for an unresolved one.
	 */
	public static DateFormat dateFormat(TLStructuredTypePart part) {
		if (part == null) {
			return null;
		}
		return dateFormat(part, part.getType());
	}

	/**
	 * The format a point in time is displayed in, or {@code null} if the type holds no points in
	 * time.
	 *
	 * <p>
	 * The format of <em>one</em> point in time, which is also what a field holding several of them
	 * writes each of its values in.
	 * </p>
	 *
	 * @param annotations
	 *        Where the {@code format} annotation is read from.
	 * @param type
	 *        The model type of the value.
	 */
	public static DateFormat dateFormat(AnnotationLookup annotations, TLType type) {
		if (!Date.class.isAssignableFrom(PrimitiveTypeUtil.asNonPrimitive(valueType(type)))) {
			return null;
		}
		return dateFormat(annotations, type, DatePickerControlProvider.kind(annotations, type));
	}

	/**
	 * The formats a point in time may be typed in, e.g. as the bound of a table filter, or
	 * {@code null} if the type holds no points in time.
	 *
	 * <p>
	 * The {@link #dateFormat(AnnotationLookup, TLType) display format} first, which also writes the
	 * value, then the default formats of the kind of value: a value is accepted the way it is shown
	 * and in the way a person is used to type it.
	 * </p>
	 *
	 * @see #dateFormat(AnnotationLookup, TLType)
	 */
	public static List<DateFormat> dateInputFormats(AnnotationLookup annotations, TLType type) {
		DateFormat displayFormat = dateFormat(annotations, type);
		if (displayFormat == null) {
			return null;
		}
		List<DateFormat> result = new ArrayList<>();
		result.add(displayFormat);
		for (DateFormat defaultFormat : DatePickerControlProvider.kind(annotations, type).inputFormats()) {
			if (!result.contains(defaultFormat)) {
				result.add(defaultFormat);
			}
		}
		return result;
	}

	/**
	 * The annotated format of the given point in time, or the default format for its kind.
	 *
	 * <p>
	 * A value whose format declaration cannot be resolved, or resolves to a format that does not
	 * write dates, is displayed in the default format instead, so that a misconfigured attribute
	 * still shows its value.
	 * </p>
	 */
	private static DateFormat dateFormat(AnnotationLookup annotations, TLType type,
			ReactDatePickerControl.Kind kind) {
		com.top_logic.model.annotate.ui.Format annotation =
			annotation(annotations, type, com.top_logic.model.annotate.ui.Format.class);
		if (annotation == null) {
			return kind.displayFormat();
		}
		try {
			Format format = DisplayAnnotations.toFormat(annotation);
			if (format == null) {
				return kind.displayFormat();
			}
			if (format instanceof DateFormat dateFormat) {
				return dateFormat;
			}
			Logger.error("Format definition at '" + annotations + "' does not write dates: " + format,
				FieldControlService.class);
		} catch (ConfigurationException ex) {
			Logger.error("Invalid format definition at '" + annotations + "'.", ex, FieldControlService.class);
		}
		return kind.displayFormat();
	}

	/**
	 * The Java type of the values of the given model type, which decides the control editing them.
	 */
	private static Class<?> valueType(TLType type) {
		if (type instanceof TLPrimitive primitive) {
			StorageMapping<?> storage = primitive.getStorageMapping();
			if (storage != null) {
				return storage.getApplicationType();
			}
			switch (primitive.getKind()) {
				case BOOLEAN:
				case TRISTATE:
					return Boolean.class;
				case INT:
					return Long.class;
				case FLOAT:
					return Double.class;
				case DATE:
					return Date.class;
				default:
					return String.class;
			}
		}
		return String.class;
	}

	/**
	 * How a boolean value asks to be displayed, {@link BooleanPresentation#CHECKBOX} when it says
	 * nothing.
	 *
	 * <p>
	 * An annotation at the attribute wins over the one of its type, which is what lets a single
	 * attribute deviate from how its type is displayed everywhere else.
	 * </p>
	 */
	private static BooleanPresentation booleanPresentation(AnnotationLookup annotations, TLType type) {
		BooleanDisplay annotation = annotation(annotations, type, BooleanDisplay.class);
		if (annotation == null || annotation.getPresentation() == null) {
			return BooleanPresentation.CHECKBOX;
		}
		return annotation.getPresentation();
	}

	/**
	 * The annotation of the given type at a value: the one at its attribute where it has one, the
	 * one at its type otherwise.
	 *
	 * <p>
	 * An annotation at the attribute wins over the one of its type, which is what lets a single
	 * attribute deviate from how its type is displayed everywhere else.
	 * </p>
	 *
	 * @param annotations
	 *        Where the annotation is read from first: the attribute holding the value, or the type
	 *        itself where no attribute holds it. May be {@code null}.
	 * @param type
	 *        The model type of the value, consulted when the attribute says nothing. May be
	 *        {@code null}.
	 * @param annotationType
	 *        The requested annotation.
	 * @return The annotation, or {@code null} if neither the attribute nor the type carries one.
	 */
	static <T extends TLAnnotation> T annotation(AnnotationLookup annotations, TLType type, Class<T> annotationType) {
		T annotation = annotations == null ? null : annotations.getAnnotation(annotationType);
		if (annotation == null && type != null && type != annotations) {
			annotation = type.getAnnotation(annotationType);
		}
		return annotation;
	}

	/**
	 * Whether a value of the given type keeps a state of its own for "no value".
	 */
	private static boolean isTriState(TLType type) {
		return type instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.TRISTATE;
	}

	/**
	 * The number of text rows the value is displayed with, or {@code 0} for a single line.
	 */
	private static int multilineRows(AnnotationLookup annotations) {
		MultiLine annotation = annotations == null ? null : annotations.getAnnotation(MultiLine.class);
		return annotation != null && annotation.getValue() ? annotation.getRows() : 0;
	}

	/**
	 * Creates a read-only control displaying the given attribute value exactly as a view-mode form
	 * field shows it.
	 *
	 * <p>
	 * The control is resolved through the same chain as
	 * {@link #createFieldControl(ReactContext, TLStructuredTypePart, FieldModel)}, bound to a
	 * non-editable field model holding the given value. Attributes that are edited by selecting
	 * from options get an option-less select model, so their values render with the select
	 * control's read-only representation (labels and icons).
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute the value belongs to.
	 * @param value
	 *        The attribute value to display, may be {@code null}.
	 */
	public ReactControl createDisplayControl(ReactContext context, TLStructuredTypePart part, Object value) {
		return createDisplayControl(context, part, part.isMultiple(), value);
	}

	/**
	 * Creates a read-only control displaying the given value of the given attribute, the field
	 * holding as many values as stated.
	 *
	 * <p>
	 * The attribute decides everything but how many values there are: its annotations, its options
	 * and its own control annotation shape the display, while the multiplicity is the one of the
	 * field, which a column reaching the attribute over a multi-valued step answers for itself, see
	 * {@link ColumnType#collected()}.
	 * </p>
	 *
	 * @param multiple
	 *        Whether the displayed value is a collection of the attribute's values rather than a
	 *        single one.
	 */
	private ReactControl createDisplayControl(ReactContext context, TLStructuredTypePart part, boolean multiple,
			Object value) {
		AbstractFieldModel model = displayModel(part, multiple, value);
		model.setEditable(false);
		return createFieldControl(context, part, fieldSpec(part, multiple, model), model, null);
	}

	/**
	 * Creates a read-only control displaying the given value exactly as a view-mode form field
	 * shows it.
	 *
	 * <p>
	 * The entry point for a value that no attribute holds: what the value is
	 * ({@link ColumnType#type()}, {@link ColumnType#multiple()}) and where its display annotations
	 * come from ({@link ColumnType#annotations()}) is all the display needs. Where an attribute
	 * <em>does</em> hold the value ({@link ColumnType#part()}), that attribute decides the display,
	 * so its options and its own annotations keep shaping the cell - all but how many values the
	 * cell holds, which is the column's answer ({@link ColumnType#multiple()}): a column reaching a
	 * single-valued attribute over a multi-valued step shows all the values it collected.
	 * </p>
	 *
	 * <p>
	 * A value of an unknown type is displayed by its label, the one thing every value has.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param columnType
	 *        What the displayed value is, see {@link ColumnType}.
	 * @param value
	 *        The value to display, may be {@code null}.
	 */
	public ReactControl createDisplayControl(ReactContext context, ColumnType columnType, Object value) {
		TLStructuredTypePart part = columnType.part();
		if (part != null) {
			return createDisplayControl(context, part, columnType.multiple(), value);
		}
		TLType type = columnType.type();
		if (type == null) {
			return new ReactTextControl(context, MetaLabelProvider.INSTANCE.getLabel(value));
		}
		AbstractFieldModel model = displayModel(type, columnType.multiple(), value);
		model.setEditable(false);
		FieldSpec field = fieldSpec(type, columnType.annotations(), null, columnType.multiple(), model);
		return createFieldControl(context, type, field, model);
	}

	/**
	 * The field holding a displayed value of the given type that no attribute declares: an
	 * option-less select model where values of the type are chosen from options, so that objects
	 * and classifiers render with the select control's read-only representation (label and icon),
	 * and a plain field otherwise.
	 *
	 * @param multiple
	 *        Whether the field holds a collection of values rather than a single one.
	 */
	private AbstractFieldModel displayModel(TLType type, boolean multiple, Object value) {
		ReactFieldControlProvider mapped = byType(type);
		boolean select =
			mapped != null ? mapped instanceof SelectControlProvider : AttributeOptions.isStructuralSelect(type);
		if (!select) {
			return new AbstractFieldModel(value);
		}
		return new SimpleSelectFieldModel(selection(multiple, value), Collections.emptyList(), multiple);
	}

	/**
	 * The field holding the displayed value of an attribute: an option-less select model where the
	 * attribute is edited by selecting from options, so that its values render with the select
	 * control's read-only representation, and a plain field otherwise.
	 *
	 * @param multiple
	 *        Whether the field holds a collection of the attribute's values rather than a single
	 *        one.
	 */
	private AbstractFieldModel displayModel(TLStructuredTypePart part, boolean multiple, Object value) {
		if (selectOptionSource(part) == null) {
			return new AbstractFieldModel(value);
		}
		return new SimpleSelectFieldModel(selection(multiple, value), Collections.emptyList(), multiple);
	}

	/**
	 * Normalizes an attribute value to the selection representation expected by the select
	 * control: a {@link java.util.List} for a field holding several values, the raw value
	 * otherwise.
	 */
	private static Object selection(boolean multiple, Object value) {
		if (!multiple) {
			return value;
		}
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return value == null ? Collections.emptyList() : Collections.singletonList(value);
	}

	/**
	 * Creates the {@link AttributeFieldModel} for the given attribute.
	 *
	 * <p>
	 * Returns an {@link AttributeSelectFieldModel} when the attribute is edited by selecting from a
	 * set of options (a configured option-bearing datatype, an enumeration, a reference, an enum
	 * datatype, or an attribute with a TL-Script options annotation), and a plain
	 * {@link AttributeFieldModel} otherwise.
	 * </p>
	 *
	 * @param object
	 *        The object to read/write the attribute value from.
	 * @param part
	 *        The attribute to bind to.
	 * @param form
	 *        The enclosing form.
	 */
	public AttributeFieldModel createModel(TLObject object, TLStructuredTypePart part, FormControl form) {
		OptionSource optionSource = selectOptionSource(part);
		if (optionSource != null) {
			return new AttributeSelectFieldModel(object, part, form, optionSource);
		}
		return new AttributeFieldModel(object, part);
	}

	/**
	 * The {@link OptionSource} for the given attribute if it is edited as a select, or {@code null}
	 * if it uses a plain (non-select) control.
	 */
	private OptionSource selectOptionSource(TLStructuredTypePart part) {
		// 1. Explicit control annotation.
		TLInputControl annotation = part.getAnnotation(TLInputControl.class);
		if (annotation != null) {
			ReactFieldControlProvider provider = _context.getInstance(annotation.getImpl());
			if (provider instanceof SelectControlProvider) {
				return optionSourceFor(part, (SelectControlProvider) provider);
			}
			return null;
		}

		// 2. Configured control by type.
		ReactFieldControlProvider mapped = byType(part.getType());
		if (mapped instanceof SelectControlProvider) {
			return optionSourceFor(part, (SelectControlProvider) mapped);
		}
		if (mapped != null) {
			// A configured non-select control (e.g. color, icon).
			return null;
		}

		// 3. Structural select (enumeration, reference, enum datatype, options generator).
		if (AttributeOptions.isStructuralSelect(part)) {
			return (self, overlays, dependencies) -> AttributeOptions.optionsFor(self, part, overlays, dependencies);
		}
		return null;
	}


	/**
	 * The {@link OptionSource} for an attribute edited by the given {@link SelectControlProvider}.
	 *
	 * <p>
	 * An attribute-level options generator (e.g. supported locales) takes precedence over the
	 * provider's configured option source; otherwise the configured option source is used, falling
	 * back to the attribute's structural options.
	 * </p>
	 */
	private OptionSource optionSourceFor(TLStructuredTypePart part, SelectControlProvider provider) {
		OptionProvider options = provider.getConfiguredOptions();
		if (options != null && AttributeOperations.getOptions(part) == null) {
			OptionProvider configured = options;
			return (self, overlays, dependencies) -> AttributeOptions
				.toList(configured.getOptions(SimpleEditContext.createContext(self, part)));
		}
		return (self, overlays, dependencies) -> AttributeOptions.optionsFor(self, part, overlays, dependencies);
	}

	/**
	 * The {@link FieldControlService} singleton.
	 */
	public static FieldControlService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link FieldControlService}.
	 */
	public static final class Module extends TypedRuntimeModule<FieldControlService> {

		/**
		 * Singleton {@link FieldControlService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<FieldControlService> getImplementation() {
			return FieldControlService.class;
		}

	}

}
