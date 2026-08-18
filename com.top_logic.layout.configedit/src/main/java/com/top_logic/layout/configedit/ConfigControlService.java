/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * Service resolving the input control for a configuration property.
 *
 * <p>
 * Resolution chain, mirroring the classic declarative form's own
 * {@code com.top_logic.layout.form.values.edit.editor.ValueEditor}:
 * </p>
 * <ol>
 * <li>{@code @Encrypted} always wins: such a property is edited in a password field, whichever of
 * the following steps would otherwise have applied.</li>
 * <li>{@link ConfigControl} annotation on the property or on its value type.</li>
 * <li>A property that is not "specialized" (no options, no {@code @Format}, no value binding) and
 * whose Java type one of the widgets below handles directly - {@code String}, {@code boolean}, a
 * numeric type, or {@code Date} - is edited by that widget, bound to the plain, typed value.</li>
 * <li>Otherwise, a property edited by selecting (an option list, or an enum) gets a select.
 * Restricted to {@link PropertyKind#PLAIN} and {@link PropertyKind#REF} properties.</li>
 * <li>Otherwise, the value-type-to-provider map configured in this service.</li>
 * <li>Otherwise, the built-in text fallback, parsed and formatted through the property's own
 * {@code ConfigurationValueProvider}.</li>
 * </ol>
 *
 * <p>
 * The specialization veto is why a property with its own format is never handed to the "direct"
 * widget for its Java type: a {@code Date} formatted as a time of day is edited through that
 * format as text, not in a date picker that could not show or accept a time - the same defect
 * {@code DatePickerControlProvider} was fixed against on the model-attribute side.
 * </p>
 *
 * <p>
 * The configured map is what lets a module above this one contribute a control - the TL-Script
 * editor, for instance, lives in {@code tl-model-search-react}.
 * </p>
 *
 * @implNote {@link ConfigPropertyOptions#optionProvider(PropertyDescriptor)} also answers for a
 *           polymorphic {@link PropertyKind#ITEM}/{@link PropertyKind#ARRAY}/{@link PropertyKind#LIST}/
 *           {@link PropertyKind#MAP} property, which is edited through a dedicated type selector,
 *           never through a plain select - that is why the "edited by selecting" step above is
 *           restricted to {@link PropertyKind#PLAIN} and {@link PropertyKind#REF}.
 */
public class ConfigControlService extends ConfiguredManagedClass<ConfigControlService.Config> {

	/**
	 * Configuration options for {@link ConfigControlService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ConfigControlService> {

		/**
		 * Value-type-to-provider mappings, keyed by the Java type of the property value.
		 */
		@Key(ProviderMapping.TYPE)
		Map<Class<?>, ProviderMapping> getProviders();

	}

	/**
	 * A single value-type-to-provider mapping entry.
	 */
	public interface ProviderMapping extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The Java type of the property value this mapping applies to.
		 */
		@Name(TYPE)
		@Mandatory
		Class<?> getType();

		/**
		 * The control provider to use for properties of this value type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ConfigControlProvider> getImpl();

	}

	private final InstantiationContext _context;

	private Map<Class<?>, ConfigControlProvider> _providerByType;

	/**
	 * Creates a {@link ConfigControlService} from configuration.
	 */
	@CalledByReflection
	public ConfigControlService(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		_providerByType = new HashMap<>();
		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			_providerByType.put(mapping.getType(), _context.getInstance(mapping.getImpl()));
		}
	}

	/**
	 * Creates the {@link ConfigFieldModel} for the given property.
	 *
	 * <p>
	 * A property that is not {@link #isSpecialized(PropertyDescriptor) specialized} and whose Java
	 * type one of the built-in widgets handles directly gets the plain {@link ConfigFieldModel}
	 * (bound to the raw typed value). Otherwise, a property edited by selecting gets a
	 * {@link ConfigSelectFieldModel}; everything else with a
	 * {@link PropertyDescriptor#getValueProvider() value provider} gets the format-aware
	 * {@link ConfigFormatFieldModel} (text); anything left over falls back to the plain
	 * {@link ConfigFieldModel} rather than failing.
	 * </p>
	 *
	 * @param config
	 *        The configuration item holding the property.
	 * @param property
	 *        The property to bind to.
	 */
	public ConfigFieldModel createModel(ConfigurationItem config, PropertyDescriptor property) {
		if (!isSpecialized(property) && isDirectlyEditable(property.getType())) {
			return new ConfigFieldModel(config, property);
		}
		if (isSelect(property)) {
			return new ConfigSelectFieldModel(config, property, selectOptions(config, property), false);
		}
		if (property.getValueProvider() != null) {
			return new ConfigFormatFieldModel(config, property);
		}
		return new ConfigFieldModel(config, property);
	}

	/**
	 * Resolves and creates the input control for the given field model.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model, created by {@link #createModel(ConfigurationItem, PropertyDescriptor)}.
	 */
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		PropertyDescriptor property = model.getProperty();

		// 1. Encrypted always wins, whichever of the following steps would otherwise apply -
		// the same way the classic ValueEditor/PlainEditor set the password control provider on
		// the field only after it was created, regardless of which branch created it.
		if (property.getAnnotation(Encrypted.class) != null) {
			return new ReactPasswordInputControl(context, model);
		}

		// 2. Annotation on the property or on its value type.
		ConfigControl annotation = controlAnnotation(property);
		if (annotation != null) {
			return newProvider(annotation.value()).createControl(context, model);
		}

		// 3. Edited by selecting.
		if (model instanceof ConfigSelectFieldModel selectModel) {
			return new ReactSelectFormFieldControl(context, selectModel, selectLabels(property));
		}

		// 4. Configured provider by value type.
		ConfigControlProvider mapped = _providerByType.get(property.getType());
		if (mapped != null) {
			return mapped.createControl(context, model);
		}

		// 5. Built-in fallback.
		return fallback(context, model, property);
	}

	/**
	 * The built-in widget for a property that reached neither the annotation, the select, nor the
	 * configured-map step.
	 *
	 * <p>
	 * Only offers the type-specific widgets ({@link ReactCheckboxControl}, a number input, the
	 * plain {@link com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind#DATE date
	 * picker}) for a property that is not {@link #isSpecialized(PropertyDescriptor) specialized} -
	 * the same veto {@link #createModel(ConfigurationItem, PropertyDescriptor)} applies, so the
	 * value domain (plain versus format text) and the widget never disagree. A specialized property
	 * (e.g. one with its own {@code @Format}, such as a time of day) is edited as its format's text
	 * instead, exactly like the classic declarative form's {@code PlainEditor}.
	 * </p>
	 */
	private ReactControl fallback(ReactContext context, ConfigFieldModel model, PropertyDescriptor property) {
		Class<?> type = property.getType();

		if (!isSpecialized(property)) {
			if (type == boolean.class || type == Boolean.class) {
				return new ReactCheckboxControl(context, model);
			}
			if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
				return new ReactNumberInputControl(context, model, 0);
			}
			if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
				return new ReactNumberInputControl(context, model, 2);
			}
			if (Date.class.isAssignableFrom(type)) {
				return new ReactDatePickerControl(context, model, ReactDatePickerControl.Kind.DATE);
			}
		}
		// String, and every specialized/typed value the format model turned into text.
		return new ReactTextInputControl(context, model);
	}

	/**
	 * Whether the given property is edited by selecting from options.
	 *
	 * <p>
	 * Restricted to {@link PropertyKind#PLAIN} and {@link PropertyKind#REF} properties: only those
	 * are ever handed to this service in the first place (an {@link PropertyKind#ITEM} or
	 * {@link PropertyKind#LIST} property is rendered by a nested editor or a type selector before
	 * this service is asked), and {@link ConfigPropertyOptions#optionProvider(PropertyDescriptor)
	 * answering non-null} for those other kinds does not mean "edit by selecting".
	 * </p>
	 */
	private boolean isSelect(PropertyDescriptor property) {
		if (controlAnnotation(property) != null) {
			// An explicitly named control decides on its own.
			return false;
		}
		PropertyKind kind = property.kind();
		if (kind != PropertyKind.PLAIN && kind != PropertyKind.REF) {
			return false;
		}
		if (property.getType().isEnum()) {
			// A plain enum has an intrinsic, fixed option list even without an @Options
			// annotation.
			return true;
		}
		return ConfigPropertyOptions.optionProvider(property) != null;
	}

	/**
	 * The options to offer for a property {@link #isSelect(PropertyDescriptor) edited by
	 * selecting}.
	 */
	private List<?> selectOptions(ConfigurationItem config, PropertyDescriptor property) {
		if (ConfigPropertyOptions.optionProvider(property) != null) {
			return ConfigPropertyOptions.optionsFor(config, property);
		}
		// Plain enum without an @Options function: its constants are the options.
		return Arrays.asList(property.getType().getEnumConstants());
	}

	/**
	 * The {@link LabelProvider} for the options of a property
	 * {@link #isSelect(PropertyDescriptor) edited by selecting}.
	 */
	private LabelProvider selectLabels(PropertyDescriptor property) {
		LabelProvider labels = ConfigPropertyOptions.optionLabels(property);
		return labels != null ? labels : MetaLabelProvider.INSTANCE;
	}

	/**
	 * Whether the given property carries its own value-to-text/options mapping, as the classic
	 * declarative form's {@code ValueEditor} defines it (there: {@code isSpecializedPrimitive}).
	 *
	 * <p>
	 * Such a property is never handed to the type-specific widget for its Java type - not even a
	 * {@code boolean} or a {@code Date} - but goes through its own format or option list instead.
	 * This is what keeps a {@code Date} formatted as a time of day (an {@code @Format}) out of the
	 * plain date picker, which could neither show nor accept the time.
	 * </p>
	 */
	private boolean isSpecialized(PropertyDescriptor property) {
		return ConfigPropertyOptions.optionProvider(property) != null
			|| property.getAnnotation(Format.class) != null
			|| property.getValueBinding() != null;
	}

	/**
	 * Whether one of the built-in widgets handles the given Java type directly (bound to the raw
	 * typed value), when the property is not {@link #isSpecialized(PropertyDescriptor)}.
	 */
	private boolean isDirectlyEditable(Class<?> type) {
		return type == String.class
			|| type == boolean.class || type == Boolean.class
			|| type == int.class || type == Integer.class || type == long.class || type == Long.class
			|| type == double.class || type == Double.class || type == float.class || type == Float.class
			|| Date.class.isAssignableFrom(type);
	}

	private ConfigControl controlAnnotation(PropertyDescriptor property) {
		ConfigControl annotation = property.getAnnotation(ConfigControl.class);
		if (annotation != null) {
			return annotation;
		}
		return property.getType().getAnnotation(ConfigControl.class);
	}

	private ConfigControlProvider newProvider(Class<? extends ConfigControlProvider> providerClass) {
		try {
			return providerClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException(
				"Cannot instantiate control provider '" + providerClass.getName()
					+ "'. A public constructor without arguments is required.",
				ex);
		}
	}

	/**
	 * The {@link ConfigControlService} singleton.
	 */
	public static ConfigControlService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link ConfigControlService}.
	 */
	public static final class Module extends TypedRuntimeModule<ConfigControlService> {

		/**
		 * Singleton {@link ConfigControlService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ConfigControlService> getImplementation() {
			return ConfigControlService.class;
		}

	}

}
