/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.text.Format;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactBinaryFieldControl;
import com.top_logic.layout.react.control.form.ListElementFieldModel;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * The {@link ReactFieldControlProvider}s that edit values, by value type.
 *
 * <p>
 * A control is looked up by the {@link FieldSpec#getValueType() type of the edited value}, so a model
 * attribute and a configuration property holding the same kind of value are edited the same way. The
 * lookup considers supertypes, so a provider registered for a base type serves its subtypes.
 * </p>
 *
 * <p>
 * The registry starts out with the providers for the types the platform edits itself. An application
 * or another module registers further ones through {@link #register(Class, ReactFieldControlProvider)}.
 * A single field can deviate from its type's provider; how that is expressed is up to the editing
 * side, which passes the provider it resolved instead of asking the registry.
 * </p>
 *
 * <p>
 * Both halves of the decision are made here: which provider edits the value type, and how the
 * multiplicity of the field is realized. A {@link FieldSpec#isMultiple() multi-valued} field whose
 * provider edits one value at a time is wrapped in a {@link ReactValueListControl}, so that each
 * element is edited by the very control its type asks for. An editing side therefore reaches the
 * control through {@link #createControl(ReactContext, FieldSpec, FieldModel)} or
 * {@link #createControl(ReactContext, FieldSpec, FieldModel, ReactFieldControlProvider)} rather than
 * calling a provider itself.
 * </p>
 */
public class FieldControlRegistry {

	/**
	 * Edits a value as a single- or multi-line text.
	 *
	 * <p>
	 * A text of more than one {@link FieldSpec#getMultilineRows() row} is
	 * {@link ReactFieldControlProvider#isLarge(FieldSpec) large}: where the field is
	 * {@link FieldSpec#isCompact() compact}, its first line stands for it.
	 * </p>
	 *
	 * @implNote Declared before {@link #getInstance() the shared registry}, which registers it while
	 *           being created.
	 */
	public static final ReactFieldControlProvider TEXT = new ReactFieldControlProvider() {
		@Override
		public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
			ReactTextInputControl control = new ReactTextInputControl(context, model);
			if (field.getMultilineRows() > 0) {
				control.setMultiline(field.getMultilineRows());
			}
			return control;
		}

		@Override
		public boolean isLarge(FieldSpec field) {
			return isMultiline(field);
		}
	};

	/** Separates the previews of the values of a multi-valued field. */
	private static final String PREVIEW_SEPARATOR = ", ";

	private static final FieldControlRegistry INSTANCE = new FieldControlRegistry();

	private final Map<Class<?>, ReactFieldControlProvider> _providers = new LinkedHashMap<>();

	/**
	 * Creates a {@link FieldControlRegistry} holding the platform's providers.
	 */
	protected FieldControlRegistry() {
		register(String.class, TEXT);
		register(Boolean.class, FieldControlRegistry::createBooleanControl);
		register(Number.class,
			(context, field, model) -> new ReactNumberInputControl(context, model, numberFormat(field)));
		register(Date.class,
			(context, field, model) -> new ReactDatePickerControl(context, model, field.getDateKind(),
				field.getDateFormat()));
		register(BinaryData.class, (context, field, model) -> new ReactBinaryFieldControl(context, model));
		// An internationalized text is edited in the current language, with the other languages
		// reachable through the editor's dialog.
		register(ResKey.class, new ReactFieldControlProvider() {
			@Override
			public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
				return ReactI18NStringInputControl.createEditor(context, model, field.getMultilineRows(),
					field.getLabel());
			}

			@Override
			public boolean isLarge(FieldSpec field) {
				return isMultiline(field);
			}
		});
	}

	/**
	 * The registry the editing sides consult.
	 */
	public static FieldControlRegistry getInstance() {
		return INSTANCE;
	}

	/**
	 * Registers the provider editing values of the given type and its subtypes.
	 *
	 * @param valueType
	 *        The type of the edited value. A primitive type is registered as its wrapper.
	 * @param provider
	 *        Creates the control.
	 */
	public void register(Class<?> valueType, ReactFieldControlProvider provider) {
		_providers.put(wrapperType(valueType), provider);
	}

	/**
	 * The provider editing values of the given type, or {@code null} if none is registered for it or
	 * any of its supertypes.
	 */
	public ReactFieldControlProvider lookup(Class<?> valueType) {
		if (valueType == null) {
			return null;
		}
		for (Class<?> type = wrapperType(valueType); type != null; type = type.getSuperclass()) {
			ReactFieldControlProvider provider = _providers.get(type);
			if (provider != null) {
				return provider;
			}
			for (Class<?> intf : type.getInterfaces()) {
				ReactFieldControlProvider fromInterface = _providers.get(intf);
				if (fromInterface != null) {
					return fromInterface;
				}
			}
		}
		return null;
	}

	/**
	 * Creates the control editing the given value.
	 *
	 * <p>
	 * Falls back to editing the value as {@link #TEXT text} when no provider is registered for its
	 * type, so an unforeseen type is still displayed.
	 * </p>
	 */
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		ReactFieldControlProvider provider = lookup(field.getValueType());
		return createControl(context, field, model, provider == null ? TEXT : provider);
	}

	/**
	 * Creates the control editing the given value with the given provider.
	 *
	 * <p>
	 * The place where the multiplicity of a field is realized. A field holding
	 * {@link FieldSpec#isMultiple() several values} whose provider
	 * {@link ReactFieldControlProvider#editsCollections() edits one value at a time} is displayed as
	 * a {@link ReactValueListControl}: one control per element, each created by the given provider
	 * over the {@link FieldSpec#elementSpec() element specification}. Everything else is handed to
	 * the provider as it stands, through
	 * {@link ReactFieldControlProvider#createField(ReactContext, FieldSpec, FieldModel)}, which
	 * applies what the specification says about the display of the control.
	 * </p>
	 *
	 * <p>
	 * A {@link FieldSpec#isCompact() compact} field whose control
	 * {@link #isLarge(FieldSpec, ReactFieldControlProvider) needs more room} than it has is
	 * displayed as a {@link ReactCompactFieldControl}: the {@link #previewText(FieldSpec,
	 * ReactFieldControlProvider, Object) preview} of its value and a button opening the control
	 * this method creates for the same field displayed with all the room it needs.
	 * </p>
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param field
	 *        What is being edited.
	 * @param model
	 *        Holds the edited value; the whole collection for a multi-valued field.
	 * @param provider
	 *        Creates the control editing a value of this field's type.
	 * @return The control to display.
	 */
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model,
			ReactFieldControlProvider provider) {
		if (field.isCompact() && isLarge(field, provider)) {
			// The full control is displayed in the dialog, where it has the room it needs.
			FieldSpec fullField = field.copy().setCompact(false);
			return new ReactCompactFieldControl(context, model, field.getLabel(),
				value -> previewText(field, provider, value),
				(dialogContext, buffer) -> createControl(dialogContext, fullField, buffer, provider));
		}
		if (field.isMultiple() && !provider.editsCollections()) {
			return new ReactValueListControl(context, model, field, provider);
		}
		return provider.createField(context, field, model);
	}

	/**
	 * Whether the control the given provider creates for the given field needs more room than a
	 * {@link FieldSpec#isCompact() compact} display offers.
	 *
	 * <p>
	 * Either the provider says so for its own control, or the field holds
	 * {@link FieldSpec#isMultiple() several values} the provider edits one at a time: the list of
	 * one control per value is as high as there are values.
	 * </p>
	 *
	 * @param field
	 *        What is being edited.
	 * @param provider
	 *        Creates the control editing a value of this field's type.
	 */
	public static boolean isLarge(FieldSpec field, ReactFieldControlProvider provider) {
		return provider.isLarge(field) || (field.isMultiple() && !provider.editsCollections());
	}

	/**
	 * The single line of text standing for a value of the given field.
	 *
	 * <p>
	 * For a field holding {@link FieldSpec#isMultiple() several values} the provider edits one at a
	 * time, the {@link ReactFieldControlProvider#previewText(FieldSpec, Object) previews} the
	 * provider gives for each of the values, separated by commas. Otherwise the preview the
	 * provider gives for the value as a whole.
	 * </p>
	 *
	 * @param field
	 *        What is being edited.
	 * @param provider
	 *        Creates the control editing a value of this field's type.
	 * @param value
	 *        The value to preview; the whole collection for a multi-valued field.
	 * @return The preview text, never {@code null}.
	 */
	public static String previewText(FieldSpec field, ReactFieldControlProvider provider, Object value) {
		if (field.isMultiple() && !provider.editsCollections()) {
			FieldSpec elementSpec = field.elementSpec();
			StringBuilder result = new StringBuilder();
			for (Object element : ListElementFieldModel.elementsOfValue(value)) {
				if (result.length() > 0) {
					result.append(PREVIEW_SEPARATOR);
				}
				result.append(provider.previewText(elementSpec, element));
			}
			return result.toString();
		}
		return provider.previewText(field, value);
	}

	/**
	 * Whether the given field displays its text on more than one row.
	 *
	 * <p>
	 * A text control of such a field is {@link ReactFieldControlProvider#isLarge(FieldSpec)
	 * large}.
	 * </p>
	 */
	public static boolean isMultiline(FieldSpec field) {
		return field.getMultilineRows() > 1;
	}

	/**
	 * Edits a boolean value the way the field {@link FieldSpec#getBooleanPresentation() asks} for.
	 *
	 * @see #createBooleanControl(ReactContext, FieldModel, BooleanPresentation, boolean)
	 */
	private static ReactControl createBooleanControl(ReactContext context, FieldSpec field, FieldModel model) {
		return createBooleanControl(context, model, field.getBooleanPresentation(), field.isTriState());
	}

	/**
	 * Creates the control editing a boolean value in the given presentation.
	 *
	 * <p>
	 * A checkbox and a switch show the value in place, while radio buttons and a select offer it as
	 * a choice between yes and no: a {@link ReactDropdownSelectControl} choosing one of the two
	 * values, labelled the way a boolean value is labelled everywhere else, so a field reads like
	 * the table cell over the same attribute. Radio buttons stand side by side; the select opens a
	 * list without an input to filter it by, there being nothing to search among two values.
	 * </p>
	 *
	 * <p>
	 * A tri-state value keeps a state for "no value": the checkbox gets a third state, the choice
	 * an option for no value, and a switch - having no third position - stays a checkbox. A
	 * two-valued choice always holds one of its values and offers no choice of no value, whether
	 * the field is mandatory or not.
	 * </p>
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param model
	 *        Holds the edited value.
	 * @param presentation
	 *        How the value is displayed.
	 * @param triState
	 *        Whether the value may also be unknown, {@code null}.
	 * @return The control to display.
	 */
	public static ReactControl createBooleanControl(ReactContext context, FieldModel model,
			BooleanPresentation presentation, boolean triState) {
		if (presentation == BooleanPresentation.RADIO) {
			return new ReactDropdownSelectControl(context, booleanChoice(model, triState),
				MetaLabelProvider.INSTANCE, null, false, SelectDisplay.RADIO, Orientation.HORIZONTAL);
		}
		if (presentation == BooleanPresentation.SELECT) {
			ReactDropdownSelectControl control = new ReactDropdownSelectControl(context,
				booleanChoice(model, triState), MetaLabelProvider.INSTANCE, null, false, SelectDisplay.DROPDOWN);
			control.setFilter(false);
			return control;
		}
		return new ReactCheckboxControl(context, model, presentation, triState);
	}

	/**
	 * The given boolean field offered as a choice between yes and no, mandatory unless the value
	 * may also be unknown.
	 */
	private static FixedOptionsFieldModel booleanChoice(FieldModel model, boolean triState) {
		return new FixedOptionsFieldModel(model, List.of(Boolean.TRUE, Boolean.FALSE), false,
			Boolean.valueOf(!triState));
	}

	/**
	 * The format a numeric value is displayed in and entered in.
	 *
	 * <p>
	 * The {@link FieldSpec#getNumberFormat() format the field asks for}, or the default format for
	 * its value type: two decimal places for a fractional value, none for a whole number - both in
	 * the user's locale.
	 * </p>
	 *
	 * @param field
	 *        The field to be edited.
	 */
	public static Format numberFormat(FieldSpec field) {
		Format format = field.getNumberFormat();
		if (format != null) {
			return format;
		}
		Class<?> type = wrapperType(field.getValueType());
		Formatter formatter = HTMLFormatter.getInstance();
		return type == Double.class || type == Float.class ? formatter.getDoubleFormat() : formatter.getLongFormat();
	}

	/**
	 * The wrapper type of a primitive type, the type itself otherwise.
	 */
	private static Class<?> wrapperType(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == char.class) {
			return Character.class;
		}
		return type;
	}
}
