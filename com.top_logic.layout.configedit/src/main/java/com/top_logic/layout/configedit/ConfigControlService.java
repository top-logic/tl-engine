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
 * Resolves a property whose value can go into a single widget: directly by its Java type, by
 * selecting from a fixed set of options, or as text through a
 * {@link PropertyDescriptor#getValueProvider() value provider} that can turn the value into text
 * and back. {@code createModel} and {@code createControl} both reject any property that fits
 * none of those - see {@code checkSupportedKind}'s own {@code JavaDoc} for exactly which
 * {@link PropertyKind}s that excludes and why. Such a property (a structure, a collection, or a
 * {@link PropertyKind#COMPLEX} property with only a value binding and no format) is rendered by a
 * dedicated nested editor or type selector before this service is ever asked.
 * </p>
 *
 * <p>
 * {@code createModel} decides the value
 * domain: a property that is not {@link #isSpecialized(PropertyDescriptor) specialized} and whose
 * Java type one of the built-in widgets handles directly - {@code String}, {@code boolean}, a
 * numeric type, or {@code Date} - gets the plain, typed {@link ConfigFieldModel}; otherwise a
 * property edited by selecting (an option list, or an enum) gets a
 * {@link ConfigSelectFieldModel}; everything else with a
 * {@link PropertyDescriptor#getValueProvider() value provider} gets the format-aware
 * {@link ConfigFormatFieldModel} (text).
 * </p>
 *
 * <p>
 * {@code createControl} then resolves the widget:
 * </p>
 * <ol>
 * <li>{@code @Encrypted} always wins, deliberately ahead of every other step - see below.</li>
 * <li>{@link ConfigControl} annotation on the property or on its value type.</li>
 * <li>A {@link ConfigSelectFieldModel} gets a select.</li>
 * <li>The value-type-to-provider map configured in this service.</li>
 * <li>The built-in fallback: the direct widget for a non-specialized property whose type
 * {@code createModel} also treated as directly editable, otherwise text parsed and formatted
 * through the property's own {@code ConfigurationValueProvider}.</li>
 * </ol>
 *
 * <p>
 * {@code @Encrypted} runs before the {@link ConfigControl} annotation, not just before the
 * built-in steps: a module may override the control for a property, but it must never be able to
 * make a secret readable by choosing a control that displays it in the clear. This is a
 * deliberate security decision on its own terms, independent of the specialization veto below.
 * </p>
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
	 *        The property to bind to. Must be a {@link PropertyKind#PLAIN}, {@link PropertyKind#REF},
	 *        or {@link PropertyKind#COMPLEX} property.
	 * @throws IllegalArgumentException
	 *         If {@code property} is none of {@link PropertyKind#PLAIN}, {@link PropertyKind#REF},
	 *         or {@link PropertyKind#COMPLEX}.
	 */
	public ConfigFieldModel createModel(ConfigurationItem config, PropertyDescriptor property) {
		checkSupportedKind(property);

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
	 *        Its {@link ConfigFieldModel#getProperty() property} must be a
	 *        {@link PropertyKind#PLAIN}, {@link PropertyKind#REF}, or {@link PropertyKind#COMPLEX}
	 *        property.
	 * @throws IllegalArgumentException
	 *         If the model's property is none of {@link PropertyKind#PLAIN},
	 *         {@link PropertyKind#REF}, or {@link PropertyKind#COMPLEX}.
	 */
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		PropertyDescriptor property = model.getProperty();
		checkSupportedKind(property);

		// 1. Encrypted always wins, deliberately ahead of every other step including an explicit
		// ConfigControl annotation: a module may override the control for a property, but it must
		// never be able to make a secret readable by choosing a control that displays it in the
		// clear. This is a security decision, independent of anything the other steps decide.
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
	 * Ensures the given property is one this service can actually put into a single widget.
	 *
	 * <p>
	 * The rule, not just a list of kinds: this service edits a property whose value it can put
	 * into one widget - directly by its Java type ({@link #isDirectlyEditable(Class)}), by
	 * selecting from a fixed set of options, or as text through a
	 * {@link PropertyDescriptor#getValueProvider() value provider} that can turn the value into
	 * text and back. Everything else belongs to a nested editor or a type selector instead, so a
	 * caller that hands such a property to this service by mistake gets a thrown exception here,
	 * not a silently broken form (e.g. a text field bound to a raw {@code List} or {@code Map}).
	 * </p>
	 *
	 * <p>
	 * Concretely: {@link PropertyKind#PLAIN} and {@link PropertyKind#REF} always qualify.
	 * {@link PropertyKind#COMPLEX} qualifies only when the property also has a
	 * {@link PropertyDescriptor#getValueProvider() value provider} - a type such as
	 * {@link com.top_logic.basic.util.ResKey}, annotated with both {@code @Format} and a
	 * {@code ConfigurationValueBinding}, is classified {@code COMPLEX} rather than {@code PLAIN}
	 * (see {@code PropertyDescriptorImpl#initKind}: a value binding wins the kind decision
	 * unconditionally, before the value provider is even considered) even though a property of
	 * that type is edited as text through its format exactly like a {@code PLAIN} one. A
	 * {@code COMPLEX} property with only a binding and no value provider - the framework's real
	 * examples are {@code AbstractListBinding}, {@code MapAttributeBinding}, and
	 * {@code XMLFragmentString}, none of which pair with a {@code ConfigurationValueProvider} -
	 * has no way to become text and is rejected, the same as an {@link PropertyKind#ITEM},
	 * {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY}, or {@link PropertyKind#MAP} property.
	 * A {@link PropertyKind#DERIVED} property is rejected as well, by the same rule read from the
	 * other side: its value is computed from other properties, so there is nothing to write back -
	 * it is displayed rather than edited, and a widget bound to it would offer an input that cannot
	 * take effect.
	 * </p>
	 */
	private static void checkSupportedKind(PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		boolean supported = kind == PropertyKind.PLAIN || kind == PropertyKind.REF
			|| (kind == PropertyKind.COMPLEX && property.getValueProvider() != null);
		if (!supported) {
			throw new IllegalArgumentException(
				"ConfigControlService cannot edit property '" + property.getPropertyName() + "' (kind "
					+ kind + "): its value cannot be put into a single widget - not directly by its "
					+ "Java type, not by selecting, and not as text through a value provider. It must "
					+ "be rendered by a dedicated editor (a nested editor or a type selector) instead "
					+ "of reaching this service.");
		}
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
	 * Only ever called for a {@link PropertyKind#PLAIN}, {@link PropertyKind#REF}, or
	 * {@link PropertyKind#COMPLEX} property -
	 * {@link #checkSupportedKind(PropertyDescriptor)} has already rejected every other kind by the
	 * time either public entry point reaches this method. That matters because
	 * {@link ConfigPropertyOptions#optionProvider(PropertyDescriptor) answering non-null} for an
	 * {@link PropertyKind#ITEM}/{@link PropertyKind#LIST} property does not mean "edit by
	 * selecting" (see its own {@code JavaDoc}) - a caller invoking this method directly on such a property
	 * would be relying on a guarantee this method no longer makes on its own.
	 * </p>
	 *
	 * <p>
	 * {@code @Encrypted} forces {@code false} unconditionally, exactly like an explicit control
	 * annotation - a secret must never be offered as a dropdown of options, since that would
	 * display its value (and every other option) in the clear. This is what makes
	 * {@link #isSpecialized(PropertyDescriptor)}'s inclusion of {@code @Encrypted} hold for every
	 * property, not only the ones whose Java type a "direct" widget would otherwise handle: without
	 * it, an {@code @Encrypted} enum or an {@code @Encrypted} property with {@code @Options} would
	 * still reach {@link ConfigSelectFieldModel} here, and {@code createControl} would then hand
	 * that select-domain model to the text-only password control.
	 * </p>
	 */
	private boolean isSelect(PropertyDescriptor property) {
		if (controlAnnotation(property) != null) {
			// An explicitly named control decides on its own.
			return false;
		}
		if (property.getAnnotation(Encrypted.class) != null) {
			// A secret is never offered as a dropdown of options - see this method's own doc comment.
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
	 * Whether the given property carries its own value-to-text/options mapping and must therefore
	 * never be handed to a type-specific widget bound to its raw, typed value.
	 *
	 * <p>
	 * Three of the four conditions mirror the classic declarative form's {@code ValueEditor}
	 * (there: {@code isSpecializedPrimitive}) - options, an explicit {@code @Format}, or a value
	 * binding. This is what keeps a {@code Date} formatted as a time of day out of the plain date
	 * picker, which could neither show nor accept the time.
	 * </p>
	 *
	 * <p>
	 * {@code @Encrypted} is added on top, for a reason {@code ValueEditor} does not share: its
	 * password control is a text control (see {@link ReactPasswordInputControl}), so an encrypted
	 * property must go through the same text domain as any other formatted property - never the
	 * raw typed value a "direct" widget (e.g. a checkbox for a {@code boolean}) would bind to.
	 * Without this, an encrypted non-{@code String} property would pair a text control with a
	 * model holding the raw typed value, exactly the domain mismatch this method exists to
	 * prevent for {@code @Format} and options.
	 * </p>
	 */
	private boolean isSpecialized(PropertyDescriptor property) {
		return ConfigPropertyOptions.optionProvider(property) != null
			|| property.getAnnotation(Format.class) != null
			|| property.getValueBinding() != null
			|| property.getAnnotation(Encrypted.class) != null;
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
