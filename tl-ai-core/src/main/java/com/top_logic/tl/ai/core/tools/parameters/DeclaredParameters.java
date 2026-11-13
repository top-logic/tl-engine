/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.FalseSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.layout.form.values.edit.InvalidOptionMapping;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.tools.model.AgentObjectFormat;
import com.top_logic.util.Resources;

/**
 * The parameters an agent passes to something it runs, declared as a configuration type.
 *
 * <p>
 * The parameters are declared in the form of the {@code <properties>} of a layout template (a
 * {@link ConfigurationDescriptorConfig}): per parameter its name, type, label and description (the
 * tooltip of the label), whether it is mandatory, its default, its allowed values
 * ({@link Options} or an enumeration type) and its constraints. A parameter holding a model object
 * uses the format {@link AgentObjectFormat} and the type
 * restriction {@link com.top_logic.model.format.ObjectType}.
 * </p>
 *
 * <p>
 * The model receives the parameters as {@link #schema() JSON schema} and passes their values as
 * JSON object. {@link #bind(Map, ModelSecurity)} checks the values against the declaration and
 * returns them by parameter name; what does not fit is refused with a text for the model.
 * </p>
 */
public final class DeclaredParameters {

	/**
	 * No parameters at all.
	 */
	public static final DeclaredParameters NONE = new DeclaredParameters(null);

	/**
	 * The texts for the model are English, like every other text a tool hands to one.
	 */
	private static final Locale MODEL_LOCALE = Locale.ENGLISH;

	private final ConfigurationDescriptor _descriptor;

	private DeclaredParameters(ConfigurationDescriptor descriptor) {
		_descriptor = descriptor;
	}

	/**
	 * The parameters declared by the given configuration.
	 *
	 * @param declaration
	 *        The declaration of the parameters, {@code null} for none.
	 */
	public static DeclaredParameters of(ConfigurationDescriptorConfig declaration) {
		if (declaration == null) {
			return NONE;
		}
		return of(TypedConfiguration.getConfigurationDescriptor(declaration));
	}

	/**
	 * The parameters described by the given configuration type.
	 *
	 * @param descriptor
	 *        The type whose properties are the parameters, {@code null} for none.
	 */
	public static DeclaredParameters of(ConfigurationDescriptor descriptor) {
		if (descriptor == null || descriptor.getProperties().isEmpty()) {
			return NONE;
		}
		return new DeclaredParameters(descriptor);
	}

	/**
	 * Whether there is no parameter.
	 */
	public boolean isEmpty() {
		return _descriptor == null;
	}

	/**
	 * The names of the parameters.
	 */
	public List<String> names() {
		if (_descriptor == null) {
			return List.of();
		}
		return properties().stream().map(PropertyDescriptor::getPropertyName).collect(Collectors.toList());
	}

	/**
	 * The JSON schema of the object holding the values of the parameters.
	 *
	 * <p>
	 * Self-contained: every part is inline, there are no references to other schemas. A parameter
	 * with {@link Options} lists its allowed values.
	 * </p>
	 */
	public Schema schema() {
		if (_descriptor == null) {
			return ObjectSchema.create().setAdditionalProperties(FalseSchema.create());
		}
		Schema schema = new JsonConfigSchemaBuilder().setInline(true).buildConfigSchema(_descriptor);
		if (schema instanceof ObjectSchema object) {
			ConfigurationItem defaults = _descriptor.factory().createNew();
			Map<String, Schema> propertySchemas = new LinkedHashMap<>(object.getProperties());
			for (PropertyDescriptor property : properties()) {
				Schema propertySchema = propertySchemas.get(property.getPropertyName());
				Collection<?> options = options(property, defaults);
				if (propertySchema == null || options == null) {
					continue;
				}
				EnumSchema allowed = EnumSchema.create();
				for (Object option : options) {
					allowed.addEnumLiteral(specification(property, option));
				}
				allowed.setTitle(propertySchema.getTitle());
				allowed.setDescription(propertySchema.getDescription());
				propertySchemas.put(property.getPropertyName(), allowed);
			}
			object.setProperties(propertySchemas);
		}
		return schema;
	}

	/**
	 * The {@link #schema()} as JSON text.
	 */
	public String schemaJson() {
		return JsonSchemaWriter.toJson(schema(), false);
	}

	/**
	 * Checks the values the model passed against the declaration.
	 *
	 * @param arguments
	 *        The values by parameter name, as parsed from the JSON object the model sent;
	 *        {@code null} for none.
	 * @param security
	 *        The rights of the run the values were passed in: an object is resolved with them, see
	 *        {@link AgentObjectFormat#withSecurity(ModelSecurity)}.
	 * @return The values of all parameters by name - the given ones, the defaults of the others.
	 * @throws InvalidArguments
	 *         If the values do not fit the declaration: an unknown parameter, a missing mandatory
	 *         one, a value of the wrong type, a value not among the allowed ones, a violated
	 *         constraint, an object that does not exist or the user may not read. The message says
	 *         which, for the model.
	 */
	public Map<String, Object> bind(Map<String, ?> arguments, ModelSecurity security) throws InvalidArguments {
		try (ThreadLocalBinding<ModelSecurity>.Handle binding = AgentObjectFormat.withSecurity(security)) {
			return bindInternal(arguments);
		}
	}

	private Map<String, Object> bindInternal(Map<String, ?> arguments) throws InvalidArguments {
		Map<String, ?> given = arguments == null ? Map.of() : arguments;
		if (_descriptor == null) {
			if (!given.isEmpty()) {
				throw new InvalidArguments("It takes no arguments, but got: " + String.join(", ", given.keySet()) + ".");
			}
			return Map.of();
		}

		List<String> unknown = new ArrayList<>();
		for (String name : given.keySet()) {
			if (_descriptor.getProperty(name) == null) {
				unknown.add(name);
			}
		}
		if (!unknown.isEmpty()) {
			throw new InvalidArguments("Unknown arguments: " + String.join(", ", unknown) + ". It takes: "
				+ String.join(", ", names()) + ".");
		}
		List<String> missing = new ArrayList<>();
		for (PropertyDescriptor property : properties()) {
			if (property.isMandatory() && given.get(property.getPropertyName()) == null) {
				missing.add("The argument '" + property.getPropertyName() + "' is required.");
			}
		}
		if (!missing.isEmpty()) {
			throw new InvalidArguments(String.join(" ", missing));
		}

		ConfigurationItem item = read(given);

		List<String> problems = new ArrayList<>();
		for (PropertyDescriptor property : properties()) {
			if (!item.valueSet(property)) {
				continue;
			}
			Collection<?> options = options(property, item);
			if (options == null) {
				continue;
			}
			Object value = item.value(property);
			Collection<?> values = value instanceof Collection<?> collection ? collection
				: value == null ? List.of() : List.of(value);
			for (Object element : values) {
				if (!options.contains(element)) {
					problems.add("The value " + specification(property, element) + " of the argument '"
						+ property.getPropertyName() + "' is not allowed. Allowed: "
						+ options.stream().map(option -> specification(property, option))
							.collect(Collectors.joining(", "))
						+ ".");
				}
			}
		}
		ConstraintChecker checker = new ConstraintChecker();
		try {
			checker.check(item);
		} catch (ConfigurationException ex) {
			throw new IllegalStateException("Constraints of the parameters cannot be checked.", ex);
		}
		for (ConstraintFailure failure : checker.getFailures()) {
			if (failure.isWarning()) {
				continue;
			}
			problems.add("The argument '" + failure.getContextProperty().getPropertyName() + "': "
				+ text(failure.getConstraintName()));
		}
		if (!problems.isEmpty()) {
			throw new InvalidArguments(String.join(" ", problems));
		}

		Map<String, Object> result = new LinkedHashMap<>();
		for (PropertyDescriptor property : properties()) {
			result.put(property.getPropertyName(), item.value(property));
		}
		return Collections.unmodifiableMap(result);
	}

	private ConfigurationItem read(Map<String, ?> given) throws InvalidArguments {
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		ConfigurationItem item;
		try {
			item = new JsonConfigurationReader(context, _descriptor)
				.schemaAware()
				.setSource(CharacterContents.newContent(JSON.toString(given)))
				.read();
		} catch (ConfigurationException ex) {
			throw new InvalidArguments(text(ex.getErrorKey()));
		} catch (RuntimeException ex) {
			// A value of the wrong JSON type for its parameter, e.g. an object for a number.
			throw new InvalidArguments(ex.getMessage());
		}
		if (log.hasErrors()) {
			throw new InvalidArguments(String.join(" ", log.getErrors()));
		}
		return item;
	}

	private List<PropertyDescriptor> properties() {
		List<PropertyDescriptor> result = new ArrayList<>();
		for (PropertyDescriptor property : _descriptor.getProperties()) {
			if (property.isAbstract() || property.kind() == PropertyKind.DERIVED) {
				continue;
			}
			result.add(property);
		}
		return result;
	}

	/**
	 * The values allowed for the given parameter by its {@link Options}, {@code null} if it has
	 * none.
	 *
	 * @param item
	 *        The values the arguments of the options function are taken from.
	 */
	private static Collection<?> options(PropertyDescriptor property, ConfigurationItem item) {
		Options annotation = property.getAnnotation(Options.class);
		if (annotation == null) {
			return null;
		}
		IGenericFunction<? extends Iterable<?>> function;
		OptionMapping mapping;
		try {
			function = ConfigUtil.getInstance(annotation.fun());
			mapping = annotation.mapping() == InvalidOptionMapping.class ? null
				: ConfigUtil.getInstance(annotation.mapping());
		} catch (ConfigurationException ex) {
			throw new IllegalStateException("Options of parameter '" + property.getPropertyName()
				+ "' cannot be computed.", ex);
		}
		Ref[] refs = annotation.args();
		Object[] args = new Object[refs.length];
		for (int n = 0; n < refs.length; n++) {
			PropertyValue value = NamePath.resolve(refs[n], item);
			args[n] = value == null ? null : value.get();
		}
		List<Object> result = new ArrayList<>();
		Iterable<?> options = function.invoke(args);
		if (options != null) {
			for (Object option : options) {
				result.add(mapping == null ? option : mapping.toSelection(option));
			}
		}
		return result;
	}

	/**
	 * The text form of a value of the given parameter, as the model writes it.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String specification(PropertyDescriptor property, Object value) {
		ConfigurationValueProvider provider = property.getValueProvider();
		if (provider != null) {
			try {
				return provider.getSpecification(value);
			} catch (RuntimeException ex) {
				// Not a value of the provider's type, fall back to its text.
			}
		}
		return Objects.toString(value);
	}

	private static String text(ResKey key) {
		return Resources.getInstance(MODEL_LOCALE).getString(key);
	}

	/**
	 * Builds the {@link DeclaredParameters} of a declaration, e.g. as derived property of the
	 * configuration holding the declaration.
	 *
	 * @see DeclaredParameters#of(ConfigurationDescriptorConfig)
	 */
	public static class FromDeclaration extends Function1<DeclaredParameters, ConfigurationDescriptorConfig> {

		@Override
		public DeclaredParameters apply(ConfigurationDescriptorConfig declaration) {
			return of(declaration);
		}
	}

	/**
	 * Values passed for {@link DeclaredParameters} that do not fit their declaration.
	 *
	 * <p>
	 * The message says what does not fit, as a text for the model.
	 * </p>
	 */
	public static class InvalidArguments extends Exception {

		/**
		 * Creates an {@link InvalidArguments}.
		 *
		 * @param message
		 *        What does not fit, for the model.
		 */
		public InvalidArguments(String message) {
			super(message);
		}
	}
}
