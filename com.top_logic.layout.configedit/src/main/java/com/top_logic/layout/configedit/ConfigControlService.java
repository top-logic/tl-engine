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
import com.top_logic.basic.config.ConfigurationValueProvider;
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
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.IdentityOptionMapping;
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
 * A property whose value fits into a single widget is edited directly: by its Java type
 * ({@code String}, {@code boolean}, a numeric type, or {@code Date}), by selecting from a fixed
 * set of options (an option list, or an enum), or as text through a
 * {@link PropertyDescriptor#getValueProvider() value provider} that can turn the value into text
 * and back. A property that fits none of those is rejected outright, not silently rendered by a
 * control that would mishandle its value. Such a property (a structure, a collection, or a
 * {@link PropertyKind#COMPLEX} property with only a value binding and no format) is rendered by a
 * dedicated nested editor or type selector before this service is ever asked.
 * </p>
 *
 * <p>
 * The widget for a property is resolved in this order:
 * </p>
 * <ol>
 * <li>An encrypted property always gets the password field, deliberately ahead of every other
 * step.</li>
 * <li>{@link ConfigControl} annotation on the property or on its value type.</li>
 * <li>A property edited by selecting from a fixed set of options gets a select.</li>
 * <li>The value-type-to-provider map configured in this service ({@link Config#getProviders()}).</li>
 * <li>The value-provider-to-provider map configured in this service ({@link Config#getFormats()}) -
 * claims a property whose {@link PropertyDescriptor#getValueProvider() value provider} (or one of
 * its superclasses) is mapped, ahead of the generic format text field. This is what lets a
 * {@code Date} formatted as a time of day get a time picker instead of plain text: the Java-type
 * map above cannot separate it from a plain date, but the value provider's own class can.</li>
 * <li>The built-in fallback: the direct widget for the property's Java type, or otherwise text
 * parsed and formatted through the property's own {@code ConfigurationValueProvider}.</li>
 * </ol>
 *
 * <p>
 * The configured maps ({@link Config#getProviders()} and {@link Config#getFormats()}) are what
 * let a module above this one contribute a control - the TL-Script editor, for instance, lives in
 * {@code tl-model-search-react}.
 * </p>
 *
 * @implNote {@code createModel} decides the value domain that pairs with the widget above: a
 *           property that is not {@code specialized} and whose Java type one of the built-in
 *           widgets handles directly gets the plain, typed {@link ConfigFieldModel}; otherwise a
 *           property edited by selecting gets a {@link ConfigSelectFieldModel}; everything else
 *           with a {@link PropertyDescriptor#getValueProvider() value provider} gets the
 *           format-aware {@link ConfigFormatFieldModel} (text).
 *           <p>
 *           {@code @Encrypted} runs before the {@link ConfigControl} annotation, not just before
 *           the built-in steps: a module may override the control for a property, but it must
 *           never be able to make a secret readable by choosing a control that displays it in the
 *           clear. This is a deliberate security decision on its own terms, independent of the
 *           specialization veto below.
 *           <p>
 *           The specialization veto is why a property with its own format is never handed to the
 *           "direct" widget for its Java type: a {@code Date} formatted as a time of day is
 *           edited through that format as text, not in a date picker that could not show or
 *           accept a time - the same defect {@code DatePickerControlProvider} was fixed against
 *           on the model-attribute side. A property whose value provider is claimed by the
 *           value-provider-to-provider map is a deliberate exception: such a property counts as
 *           not specialized after all (unless it also has options - see below), so it gets the
 *           plain, typed {@link ConfigFieldModel} instead of the format-aware one, and the
 *           claimed control (bound to that same typed value) is what actually edits it - see
 *           {@link ReactDatePickerControl} for the time-of-day case.
 *           <p>
 *           Options win over a claim: a property that both has {@code @Options} and is claimed
 *           stays specialized and is edited by selecting, not by the claimed control - an
 *           explicit option list is a statement about that one property, narrower than a mapping
 *           registered for a whole value-provider class, so the narrower statement wins.
 *           <p>
 *           The exception above is implemented by
 *           {@link #isSpecialized(PropertyDescriptor, DerivedProperty, ConfigControlProvider)}
 *           itself: it answers {@code false} for a claimed property, which is what keeps the
 *           value domain chosen by {@link #createModel(ConfigurationItem, PropertyDescriptor)}
 *           and the widget chosen by {@link #createControl(ReactContext, ConfigFieldModel)} in
 *           step with each other. The reference lives here rather than in the prose above
 *           because a configured class's own documentation becomes in-app documentation, where a
 *           reference to one of its methods has no meaning.
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

		/**
		 * Value-provider-to-provider mappings, keyed by the {@link ConfigurationValueProvider}
		 * class of the property's {@link PropertyDescriptor#getValueProvider() value provider}.
		 *
		 * <p>
		 * Separate from {@link #getProviders()}: two properties of the same Java value type (e.g.
		 * {@code Date}) can carry different value providers - a plain date and a time of day both
		 * use {@code Date} - so only the value provider's own class, not the Java type, can tell
		 * them apart. Looked up by a property's concrete value-provider class first, then by
		 * walking up its superclasses, so a mapping registered for a base provider also covers
		 * its specializations.
		 * </p>
		 */
		@Key(FormatMapping.PROVIDER)
		Map<Class<?>, FormatMapping> getFormats();

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

	/**
	 * A single value-provider-to-provider mapping entry.
	 */
	public interface FormatMapping extends ConfigurationItem {

		/** Property name of {@link #getProvider()}. */
		String PROVIDER = "provider";

		/**
		 * The {@link ConfigurationValueProvider} class (or a superclass of it) a property's
		 * {@link PropertyDescriptor#getValueProvider() value provider} must be an instance of for
		 * this mapping to apply.
		 */
		@Name(PROVIDER)
		@Mandatory
		Class<? extends ConfigurationValueProvider<?>> getProvider();

		/**
		 * The control provider to use for a property claimed by this mapping.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ConfigControlProvider> getImpl();

	}

	private final InstantiationContext _context;

	private Map<Class<?>, ConfigControlProvider> _providerByType;

	private Map<Class<?>, ConfigControlProvider> _providerByFormat;

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

		_providerByFormat = new HashMap<>();
		for (FormatMapping mapping : getConfig().getFormats().values()) {
			_providerByFormat.put(mapping.getProvider(), _context.getInstance(mapping.getImpl()));
		}
	}

	/**
	 * Creates the {@link ConfigFieldModel} for the given property.
	 *
	 * <p>
	 * A property that is not {@link #isSpecialized(PropertyDescriptor, DerivedProperty, ConfigControlProvider) specialized} and whose Java
	 * type one of the built-in widgets handles directly gets the plain {@link ConfigFieldModel}
	 * (bound to the raw typed value). Otherwise, a property edited by selecting gets a
	 * {@link ConfigSelectFieldModel}; a property {@link #formatProvider(PropertyDescriptor)
	 * claimed} by the configured format-provider map also gets the plain {@link ConfigFieldModel}
	 * - independent of whether its Java type happens to be one of the built-in directly-editable
	 * ones, since the claim itself states that the claimed control edits the value in its typed
	 * form; everything else with a {@link PropertyDescriptor#getValueProvider() value provider}
	 * gets the format-aware {@link ConfigFormatFieldModel} (text); anything left over falls back
	 * to the plain {@link ConfigFieldModel} rather than failing.
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
		return createModel(config, property, config);
	}

	/**
	 * Remembers on the model what is being edited as a whole, so a control built from it later can
	 * resolve options that look outwards.
	 */
	private static <M extends ConfigFieldModel> M withFormModel(M model, ConfigurationItem formModel) {
		model.setFormModel(formModel);
		return model;
	}

	/**
	 * The field model for the given property, told what is being edited as a whole.
	 *
	 * @param formModel
	 *        The root of the configuration under edit. An option function or mapping of the property
	 *        may need it - see
	 *        {@link ConfigPropertyOptions#optionProvider(ConfigurationItem, PropertyDescriptor)} -
	 *        and it cannot be derived from {@code config}, since the way up is not available on every
	 *        configuration.
	 */
	public ConfigFieldModel createModel(ConfigurationItem config, PropertyDescriptor property,
			ConfigurationItem formModel) {
		checkSupportedKind(property);

		// Resolved once and passed to every step below that would otherwise resolve it again
		// (isSpecialized, isSelect, selectOptions) - this is also where the mapping check
		// belongs, see isSelect's own JavaDoc.
		DerivedProperty<? extends Iterable<?>> optionProvider =
			ConfigPropertyOptions.optionProvider(formModel, property);
		ConfigControlProvider formatProvider = formatProvider(property);

		if (!isSpecialized(property, optionProvider, formatProvider) && isDirectlyEditable(property.getType())) {
			return withFormModel(new ConfigFieldModel(config, property), formModel);
		}
		if (isSelect(property, optionProvider)) {
			ConfigSelectFieldModel selectModel =
				new ConfigSelectFieldModel(config, property, selectOptions(config, property, optionProvider), false);
			if (optionProvider != null) {
				// An option function may be computed from other properties, and then its result
				// changes while the user edits those - see ConfigSelectFieldModel#trackOptions.
				// Without a provider the options are a plain enum's constants, which cannot change.
				selectModel.trackOptions(optionProvider);
			}
			return withFormModel(selectModel, formModel);
		}
		if (property.getAnnotation(Encrypted.class) == null && isClaimed(property, formatProvider)) {
			// Claimed: the claim states that the control bound to this model edits the value in
			// its typed form, regardless of whether isDirectlyEditable's built-in set happens to
			// include this property's Java type. Structural, not coincidental: a future mapping
			// for a value provider over e.g. ResKey must not produce a model/control mismatch.
			//
			// The @Encrypted guard mirrors isSpecialized's and createControl's own - a secret must
			// never be pulled away from the format/password domain by a claim, the same invariant
			// stated (and enforced) there. It cannot currently be exercised from here: an
			// @Encrypted property's PropertyDescriptorImpl wraps its value provider in an
			// EncodingConfigurationValueProvider (com.top_logic.basic.config) before this method
			// ever sees it, and that wrapper *delegates* to the original provider through a field
			// rather than extending it, so formatProvider's superclass walk over the wrapper's own
			// class never reaches whatever class was actually registered - formatProvider(property)
			// already answers null for every @Encrypted property today, guard or no guard, as long
			// as no mapping targets EncodingConfigurationValueProvider itself (a framework
			// encryption-wrapper class, never a legitimate registration target). The guard stays
			// anyway, so this method does not rely on that wrapping detail of a
			// different class in a different module to keep its own invariant; if that wrapping
			// ever changed, this line - not an accident three layers away - is what would still
			// hold the line. The observable outcome (an encrypted, claimed property still getting
			// the format model and the password control) is covered by
			// testEncryptedWinsOverFormatProviderMapping, taken via isSpecialized's earlier,
			// always-reachable @Encrypted check instead.
			return withFormModel(new ConfigFieldModel(config, property), formModel);
		}
		if (property.getValueProvider() != null) {
			return withFormModel(new ConfigFormatFieldModel(config, property), formModel);
		}
		return withFormModel(new ConfigFieldModel(config, property), formModel);
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
			return new ReactSelectFormFieldControl(context, selectModel,
				selectLabels(selectModel.getFormModel(), property));
		}

		// 4. Configured provider by value type.
		ConfigControlProvider mapped = _providerByType.get(property.getType());
		if (mapped != null) {
			return mapped.createControl(context, model);
		}

		// 5. Configured provider by the property's value provider class (or one of its
		// superclasses) - claims a specialized property away from the generic format text field
		// the fallback would otherwise give it. Checked before the fallback, not gated behind
		// isSpecialized: isSpecialized already reports such a property as not specialized (see its
		// own JavaDoc), which only decides the *model*'s domain, not which control to use.
		ConfigControlProvider formatProvider = formatProvider(property);
		if (formatProvider != null) {
			return formatProvider.createControl(context, model);
		}

		// 6. Built-in fallback. formatProvider is known null here (the check just above), passed
		// on instead of resolving it a second time inside fallback's own isSpecialized check.
		return fallback(context, model, property, formatProvider);
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
	 * The built-in widget for a property that reached neither the annotation, the select, nor
	 * either configured-map step.
	 *
	 * <p>
	 * Only offers the type-specific widgets ({@link ReactCheckboxControl}, a number input, the
	 * plain {@link com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind#DATE date
	 * picker}) for a property that is not {@link #isSpecialized(PropertyDescriptor, DerivedProperty, ConfigControlProvider) specialized} -
	 * the same veto {@link #createModel(ConfigurationItem, PropertyDescriptor)} applies, so the
	 * value domain (plain versus format text) and the widget never disagree. A specialized property
	 * (e.g. one with its own {@code @Format}, such as a time of day) is edited as its format's text
	 * instead, exactly like the classic declarative form's {@code PlainEditor}.
	 * </p>
	 */
	private ReactControl fallback(ReactContext context, ConfigFieldModel model, PropertyDescriptor property,
			ConfigControlProvider formatProvider) {
		Class<?> type = property.getType();

		if (!isSpecialized(property, ConfigPropertyOptions.optionProvider(model.getFormModel(), property),
			formatProvider)) {
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
		ReactTextInputControl input = new ReactTextInputControl(context, model);
		if (model instanceof ConfigFormatFieldModel) {
			// A format model does not store what it is handed: it parses the text and hands back
			// what the value formats to. Sending a value mid-edit therefore re-renders the field
			// from the normalized text and throws away what was being typed - typing "red, " into a
			// comma separated list would leave "red" behind, comma and space gone from under the
			// cursor. The value is held back until the field is left instead; the plain
			// ConfigFieldModel, which stores its value verbatim, keeps the default debounce and its
			// feedback-while-typing.
			input.setSendValueOnBlur(true);
		}
		return input;
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
	 * {@link #isSpecialized(PropertyDescriptor, DerivedProperty, ConfigControlProvider)}'s
	 * inclusion of {@code @Encrypted} hold for every property, not only the ones whose Java type a
	 * "direct" widget would otherwise handle: without it, an {@code @Encrypted} enum or an
	 * {@code @Encrypted} property with {@code @Options} would still reach
	 * {@link ConfigSelectFieldModel} here, and {@code createControl} would then hand that
	 * select-domain model to the text-only password control.
	 * </p>
	 *
	 * <p>
	 * A non-{@code null} option provider is not enough on its own:
	 * {@link Fields#optionMapping(DerivedProperty)} must also answer
	 * {@link IdentityOptionMapping#INSTANCE}, i.e. the option itself <em>is</em>
	 * the value to store, not something that must first be translated into it. A property such as
	 * {@link com.top_logic.model.util.TLModelPartRef} declares {@code @Options} with a non-identity
	 * mapping (its options are model parts, the stored value is the ref that names one) - handing
	 * such a property to the select model regardless would offer options the client can only send
	 * back as {@code toString()} text, which {@link ConfigSelectFieldModel#setValue(Object)} cannot
	 * parse back into anything meaningful and would reject with an uncaught
	 * {@code IllegalArgumentException} instead of a field error. Such a property falls through
	 * to the generic format text field instead, which already round-trips its value correctly
	 * through the property's own value provider.
	 * </p>
	 *
	 * @param optionProvider
	 *        The property's option provider, as resolved once by the caller via
	 *        {@link ConfigPropertyOptions#optionProvider(PropertyDescriptor)}, or {@code null} if
	 *        it has none.
	 */
	private boolean isSelect(PropertyDescriptor property, DerivedProperty<? extends Iterable<?>> optionProvider) {
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
		if (optionProvider == null) {
			return false;
		}
		// The narrow fix: an option whose mapping is not the identity cannot be sent to the
		// client and parsed back without translation this service does not perform - see this
		// method's own doc comment.
		return Fields.optionMapping(optionProvider) == IdentityOptionMapping.INSTANCE;
	}

	/**
	 * The options to offer for a property {@link #isSelect(PropertyDescriptor, DerivedProperty)
	 * edited by selecting}.
	 *
	 * @param optionProvider
	 *        The property's option provider, as resolved once by the caller, or {@code null} for a
	 *        plain enum without an {@code @Options} function.
	 */
	private List<?> selectOptions(ConfigurationItem config, PropertyDescriptor property,
			DerivedProperty<? extends Iterable<?>> optionProvider) {
		if (optionProvider != null) {
			return ConfigPropertyOptions.toList(optionProvider.get(config));
		}
		// Plain enum without an @Options function: its constants are the options.
		return Arrays.asList(property.getType().getEnumConstants());
	}

	/**
	 * The {@link LabelProvider} for the options of a property
	 * {@link #isSelect(PropertyDescriptor, DerivedProperty) edited by selecting}.
	 *
	 * @param config
	 *        The item the property belongs to - the option labels may be built by a mapping that
	 *        needs the surrounding configuration, see
	 *        {@link ConfigPropertyOptions#optionProvider(ConfigurationItem, PropertyDescriptor)}.
	 */
	private LabelProvider selectLabels(ConfigurationItem formModel, PropertyDescriptor property) {
		LabelProvider labels = ConfigPropertyOptions.optionLabels(formModel, property);
		return labels != null ? labels : MetaLabelProvider.INSTANCE;
	}

	/**
	 * Whether the given property carries its own value-to-text/options mapping and must therefore
	 * never be handed to a type-specific widget bound to its raw, typed value.
	 *
	 * <p>
	 * Three of the four base conditions mirror the classic declarative form's {@code ValueEditor}
	 * (there: {@code isSpecializedPrimitive}) - options, an explicit {@code @Format}, or a value
	 * binding. This is what keeps a {@code Date} formatted as a time of day out of the plain date
	 * picker, which could neither show nor accept the time - <em>unless</em> that same format is
	 * claimed by the {@link Config#getFormats() value-provider-to-provider map}, see below.
	 * </p>
	 *
	 * <p>
	 * {@code @Encrypted} is added on top, for a reason {@code ValueEditor} does not share: its
	 * password control is a text control (see {@link ReactPasswordInputControl}), so an encrypted
	 * property must go through the same text domain as any other formatted property - never the
	 * raw typed value a "direct" widget (e.g. a checkbox for a {@code boolean}) would bind to.
	 * Without this, an encrypted non-{@code String} property would pair a text control with a
	 * model holding the raw typed value, exactly the domain mismatch this method exists to
	 * prevent for {@code @Format} and options. {@code @Encrypted} is checked first and returns
	 * {@code true} unconditionally, before options and the format-provider claim below are even
	 * considered: a module must never be able to make a secret readable by mapping a control to
	 * its value provider, exactly the same security decision {@code createControl} makes by
	 * checking {@code @Encrypted} ahead of every other step, including this map.
	 * </p>
	 *
	 * <p>
	 * A property whose {@link PropertyDescriptor#getValueProvider() value provider} (or one of its
	 * superclasses) is {@link #formatProvider(PropertyDescriptor) claimed} by the configured
	 * value-provider-to-provider map is a deliberate exception to the remaining two base
	 * conditions ({@code @Format}, a value binding): such a property is reported as <em>not</em>
	 * specialized, so {@code createModel} gives it the plain, typed {@link ConfigFieldModel}, and
	 * {@code createControl} hands that same model to the claimed control instead of the format
	 * text field - see this class's own JavaDoc.
	 * </p>
	 *
	 * <p>
	 * Options are checked <em>before</em> the claim, deliberately not folded into that exception:
	 * an {@code @Options} annotation is a statement about one property - exactly this value set -
	 * while a format-provider mapping is a global registration for a value-provider class, meant
	 * to be noticed by every property that happens to use that class. The narrower, explicit
	 * statement must not be overridden by the broader one, so a property that is both claimed and
	 * optioned stays specialized and is edited by selecting - {@code isSelect} and
	 * {@code createControl}'s select step (3, ahead of the claim step 5) then keep the model and
	 * the widget in agreement, the same way they already do for every other optioned property.
	 * </p>
	 *
	 * @param optionProvider
	 *        The property's option provider, as resolved once by the caller via
	 *        {@link ConfigPropertyOptions#optionProvider(PropertyDescriptor)}, or {@code null} if
	 *        it has none. Deliberately not narrowed to {@code isSelect}'s identity-mapping check:
	 *        an {@code @Options} annotation with any mapping still states that the value domain is
	 *        options, not the type-specific widget's raw value.
	 * @param formatProvider
	 *        The property's {@link #formatProvider(PropertyDescriptor) claimed control provider},
	 *        as resolved once by the caller, or {@code null} if none claims it.
	 */
	private boolean isSpecialized(PropertyDescriptor property, DerivedProperty<? extends Iterable<?>> optionProvider,
			ConfigControlProvider formatProvider) {
		if (property.getAnnotation(Encrypted.class) != null) {
			return true;
		}
		if (controlAnnotation(property) != null) {
			// An explicitly named control decides on its own, and edits the value in its typed form
			// - the same reason a format claim below is not a specialization either. Checked in the
			// order createControl resolves in: after @Encrypted, which outranks it there too.
			return false;
		}
		if (optionProvider != null) {
			return true;
		}
		if (formatProvider != null) {
			return false;
		}
		if (_providerByType.containsKey(property.getType())) {
			// A control mapped by value type edits the typed value, like the two above. Checked
			// after the options, matching createControl, where the select comes first.
			return false;
		}
		return property.getAnnotation(Format.class) != null
			|| property.getValueBinding() != null;
	}

	/**
	 * Whether some configured or annotated control claims this property, and therefore edits its
	 * value in its typed form rather than as the text a format would make of it.
	 *
	 * <p>
	 * The three ways to claim a property - the {@link ConfigControl} annotation,
	 * {@link Config#getProviders()} by value type, and {@link Config#getFormats()} by value provider
	 * - all bind a control to the raw typed value. Whether the property's Java type happens to be
	 * one of {@link #isDirectlyEditable(Class)}'s built-in set has nothing to do with it, so
	 * {@code createModel} has to hand out the typed {@link ConfigFieldModel} for a claimed property
	 * either way; leaving it to the built-in set would give a claimed {@code ResKey} or a claimed
	 * {@code Date} with its own {@code @Format} the format model over <em>text</em>, and
	 * {@code createControl} would then hand that text to a control expecting the typed value.
	 * </p>
	 *
	 * @param formatProvider
	 *        The {@link #formatProvider(PropertyDescriptor) format claim}, as resolved once by the
	 *        caller, or {@code null} if none claims it.
	 */
	private boolean isClaimed(PropertyDescriptor property, ConfigControlProvider formatProvider) {
		return formatProvider != null
			|| controlAnnotation(property) != null
			|| _providerByType.containsKey(property.getType());
	}

	/**
	 * The configured {@link ConfigControlProvider} for the given property's
	 * {@link PropertyDescriptor#getValueProvider() value provider}, or {@code null} if the
	 * property has none or none of its classes is mapped.
	 *
	 * <p>
	 * Looked up by the value provider's concrete class first, then by walking up its superclasses,
	 * so a mapping registered for a base provider also covers its specializations - mirroring
	 * {@link Config#getFormats()}'s own contract.
	 * </p>
	 */
	private ConfigControlProvider formatProvider(PropertyDescriptor property) {
		ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
		if (valueProvider == null) {
			return null;
		}
		for (Class<?> type = valueProvider.getClass(); type != null; type = type.getSuperclass()) {
			ConfigControlProvider provider = _providerByFormat.get(type);
			if (provider != null) {
				return provider;
			}
		}
		return null;
	}

	/**
	 * Whether one of the built-in widgets handles the given Java type directly (bound to the raw
	 * typed value), when the property is not {@link #isSpecialized(PropertyDescriptor, DerivedProperty, ConfigControlProvider)}.
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
