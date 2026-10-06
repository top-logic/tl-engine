/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.codeedit.control.EditorControlConfig;
import com.top_logic.layout.codeedit.editor.DefaultCodeEditor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.service.openapi.common.document.Described;
import com.top_logic.service.openapi.common.document.ParameterLocation;

/**
 * Concrete request parameter, i.e. a {@link RequestParameter} that holds the own implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConcreteRequestParameter<C extends ConcreteRequestParameter.Config<?>>
		extends RequestParameter<C> {

	/**
	 * Configuration of a parameter.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		ParameterConfiguration.NAME_ATTRIBUTE,
		ParameterConfiguration.VARIABLE_NAME,
		ParameterConfiguration.DESCRIPTION,
		ParameterConfiguration.FORMAT,
		ParameterConfiguration.REQUIRED,
		ParameterConfiguration.SCHEMA,
		ParameterConfiguration.EXAMPLE,
		ParameterConfiguration.MULTIPLE,
	})
	public interface ParameterConfiguration extends Described, NamedConfigMandatory {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Regular expression that a TL-Script variable name must match.
		 */
		String SCRIPT_IDENTIFIER_PATTERN = "[A-Za-z_][A-Za-z0-9_]*";

		/**
		 * @see #getVariableName()
		 */
		String VARIABLE_NAME = "variable-name";

		/**
		 * @see #getRequired()
		 */
		String REQUIRED = "required";

		/**
		 * @see #getFormat()
		 */
		String FORMAT = "format";

		/**
		 * @see #getSchema()
		 */
		String SCHEMA = "schema";

		/**
		 * @see #getExample()
		 */
		String EXAMPLE = "example";

		/**
		 * @see #isMultiple()
		 */
		String MULTIPLE = "multiple";

		/**
		 * A brief description of the parameter. This could contain examples of use.
		 * <i>CommonMark</i> syntax <i>may</i> be used for rich text representation.
		 */
		@Override
		String getDescription();

		/**
		 * The name of the TL-Script variable that holds the value of this parameter in the
		 * operation script.
		 * 
		 * <p>
		 * The {@link #getName()} is the name of the parameter in the HTTP request (e.g. the name of
		 * a header or a query parameter). If no variable name is given, the value is accessible in
		 * the operation script under the parameter name. A variable name must be given if the
		 * parameter name is not a valid TL-Script variable name, e.g. for a header
		 * <code>X-Gitea-Event</code>. A TL-Script variable name starts with a letter or an
		 * underscore and contains only letters, digits, and underscores.
		 * </p>
		 */
		@Name(VARIABLE_NAME)
		@Nullable
		@Constraint(value = ValidVariableName.class, args = @Ref(NAME_ATTRIBUTE))
		String getVariableName();

		/**
		 * Setter for {@link #getVariableName()}.
		 */
		void setVariableName(String value);

		/**
		 * The name of the TL-Script variable that holds the value of this parameter.
		 * 
		 * @return The {@link #getVariableName()}, if given, the {@link #getName()} otherwise.
		 */
		default String effectiveVariableName() {
			String variableName = getVariableName();
			if (variableName == null || variableName.isEmpty()) {
				return getName();
			}
			return variableName;
		}

		/**
		 * Determines whether this parameter is mandatory.
		 */
		@Name(REQUIRED)
		boolean getRequired();

		/**
		 * Setter for {@link #getRequired()}.
		 */
		void setRequired(boolean value);

		/**
		 * The schema defining the type used for the parameter.
		 */
		@Name(FORMAT)
		ParameterFormat getFormat();

		/**
		 * Setter for {@link #getFormat()}.
		 */
		void setFormat(ParameterFormat value);

		/**
		 * {@link #getSchema()} defines the schema (in JSON format) of the expected argument. For
		 * the definition of a JSON schema see
		 * <code>https://spec.openapis.org/oas/v3.0.3#schema-object</code>.
		 */
		@DynamicMode(fun = ParameterConfiguration.VisibleOnObjectParam.class, args = @Ref(FORMAT))
		@EditorControlConfig(language = CodeEditorControl.MODE_JSON, prettyPrinting = true)
		@PropertyEditor(DefaultCodeEditor.class)
		@Name(SCHEMA)
		@Nullable
		String getSchema();

		/**
		 * Setter for {@link #getSchema()}.
		 */
		void setSchema(String value);

		/**
		 * {@link #getExample()} defines an example for the argument. The example must match the
		 * {@link #getSchema()}.
		 */
		@DynamicMode(fun = ParameterConfiguration.VisibleOnObjectParam.class, args = @Ref(FORMAT))
		@EditorControlConfig(language = CodeEditorControl.MODE_JSON, prettyPrinting = true)
		@PropertyEditor(DefaultCodeEditor.class)
		@Name(EXAMPLE)
		@Nullable
		String getExample();

		/**
		 * Setter for {@link #getExample()}.
		 */
		void setExample(String value);

		/**
		 * Whether this parameter is given multiple times.
		 */
		@Name(MULTIPLE)
		boolean isMultiple();

		/**
		 * Setter for {@link #isMultiple()}.
		 */
		void setMultiple(boolean value);

		/**
		 * {@link Function1} that hides a {@link FormField} unless the given parameter is an
		 * {@link ParameterFormat#OBJECT} parameter.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		class VisibleOnObjectParam extends Function1<FieldMode, ParameterFormat> {

			@Override
			public FieldMode apply(ParameterFormat arg) {
				if (arg == ParameterFormat.OBJECT) {
					return FieldMode.ACTIVE;
				}
				return FieldMode.INVISIBLE;
			}

		}

		/**
		 * {@link GenericValueDependency} checking that a {@link ParameterConfiguration} is
		 * accessible as TL-Script variable.
		 * 
		 * <p>
		 * An explicitly given {@link ParameterConfiguration#getVariableName() variable name} must be
		 * a valid TL-Script variable name. Without an explicit variable name, the
		 * {@link ParameterConfiguration#getName() parameter name} must be one.
		 * </p>
		 * 
		 * @see ParameterConfiguration#SCRIPT_IDENTIFIER_PATTERN
		 */
		class ValidVariableName extends GenericValueDependency<String, String> {

			private static final Pattern SCRIPT_IDENTIFIER = Pattern.compile(SCRIPT_IDENTIFIER_PATTERN);

			/**
			 * Creates a {@link ValidVariableName}.
			 */
			public ValidVariableName() {
				super(String.class, String.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> variableName, PropertyModel<String> name) {
				String explicitName = variableName.getValue();
				if (explicitName != null && !explicitName.isEmpty()) {
					if (!isScriptIdentifier(explicitName)) {
						variableName.setProblemDescription(
							I18NConstants.ERROR_INVALID_VARIABLE_NAME__NAME.fill(explicitName));
					}
					return;
				}

				String parameterName = name.getValue();
				if (parameterName != null && !parameterName.isEmpty() && !isScriptIdentifier(parameterName)) {
					variableName.setProblemDescription(
						I18NConstants.ERROR_VARIABLE_NAME_REQUIRED__NAME.fill(parameterName));
				}
			}

			/**
			 * The parameter name is valid on its own, only the variable name is at fault.
			 */
			@Override
			public boolean isChecked(int index) {
				return index == 0;
			}

			/**
			 * Whether the given name is a valid TL-Script variable name.
			 */
			public static boolean isScriptIdentifier(String name) {
				return SCRIPT_IDENTIFIER.matcher(name).matches();
			}

		}

	}

	/**
	 * Configuration options for {@link ConcreteRequestParameter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface Config<I extends ConcreteRequestParameter<?>>
			extends ParameterConfiguration, RequestParameter.Config<I> {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Service method to get the {@link ParameterLocation} where this configuration is used.
		 */
		default ParameterLocation getParameterLocation() {
			ParameterUsedIn annotation = getImplementationClass().getAnnotation(ParameterUsedIn.class);
			if (annotation == null) {
				throw new IllegalStateException("No Annotation declared at " + getImplementationClass());
			}
			return annotation.value();
		}

		/**
		 * The names of the TL-Script variables that this parameter binds around the operation
		 * script.
		 */
		default List<String> scriptVariableNames() {
			return Collections.singletonList(effectiveVariableName());
		}

		@Override
		default ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> resolveParameter(
				Map<String, ReferencedParameter> globalParams) {
			return this;
		}

	}

	/**
	 * Creates a {@link ConcreteRequestParameter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConcreteRequestParameter(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The names of parameters that will be filled in {@link #parse(Map, HttpServletRequest, Map)}.
	 * 
	 * @see Config#scriptVariableNames()
	 */
	public List<String> getScriptParameterNames() {
		return getConfig().scriptVariableNames();
	}

	/**
	 * The name of the TL-Script variable holding the value of this parameter.
	 * 
	 * @see ParameterConfiguration#effectiveVariableName()
	 */
	public String getVariableName() {
		return getConfig().effectiveVariableName();
	}

	/**
	 * The script variable names that are bound by more than one of the given parameters.
	 * 
	 * @param parameters
	 *        The parameters that are bound together around one operation script.
	 * @return The clashing variable names in the order of their first occurrence, empty if all
	 *         variable names are unique.
	 */
	public static Set<String> clashingVariableNames(
			Collection<? extends Config<?>> parameters) {
		Set<String> seen = new HashSet<>();
		Set<String> clashes = new LinkedHashSet<>();
		for (Config<?> parameter : parameters) {
			for (String variableName : parameter.scriptVariableNames()) {
				if (!seen.add(variableName)) {
					clashes.add(variableName);
				}
			}
		}
		return clashes;
	}

	/**
	 * Fills the parameter in the given {@link Map} with values from the request.
	 * 
	 * @param parameters
	 *        The parameter map to fill.
	 * @param req
	 *        The current request.
	 * @param parametersRaw
	 *        The extracted raw path parameters from the request URL.
	 * @throws InvalidValueException
	 *         If parsing fails.
	 */
	public void parse(Map<String, Object> parameters, HttpServletRequest req, Map<String, String> parametersRaw)
			throws InvalidValueException {
		Object value = getValue(req, parametersRaw);
		if (value == null) {
			checkNonMandatory();

			return;
		}
		parameters.put(getVariableName(), value);
	}

	/**
	 * Retrieves the parameter value.
	 */
	protected abstract Object getValue(HttpServletRequest req, Map<String, String> parametersRaw)
			throws InvalidValueException;

	/**
	 * Parses a parameters raw value.
	 */
	protected final Object parse(String rawValue) throws InvalidValueException {
		return parse(rawValue, getConfig().getFormat(), getConfig().getName());

	}

	/**
	 * Parses the given raw value according to the given {@link ParameterFormat}.
	 *
	 * @param rawValue
	 *        The raw raw value to parse. May be <code>null</code>.
	 * @param format
	 *        Format defining the expected value type. <b>Must not be
	 *        {@link ParameterFormat#BINARY}.</b> Binary data can not be retrieved from a string.
	 * @param parameterName
	 *        Name of the parameter where the <code>rawValue</code> is taken from.
	 * @return Parsed version of <code>rawValue</code>. May be <code>null</code> iff the input is
	 *         <code>null</code>.
	 * @throws InvalidValueException
	 *         When the value can not be parsed by the given format.
	 * @throws IllegalArgumentException
	 *         If format is {@link ParameterFormat#BINARY}.
	 */
	protected static Object parse(String rawValue, ParameterFormat format, String parameterName)
			throws InvalidValueException {
		if (rawValue.isEmpty()) {
			return null;
		}
		switch (format) {
			case STRING:
				return rawValue;
			case DOUBLE:
				try {
					return Double.parseDouble(rawValue);
				} catch (NumberFormatException ex) {
					throw new InvalidValueException(
						"Invalid double format in parameter '" + parameterName + "': " + rawValue, ex);
				}
			case OBJECT:
				try {
					return JSON.fromString(rawValue);
				} catch (ParseException ex) {
					throw new InvalidValueException(
						"Invalid JSON value in parameter '" + parameterName + "': " + rawValue, ex);
				}
			case BOOLEAN:
				return Boolean.parseBoolean(rawValue);
			case INTEGER:
				try {
					return (int) Double.parseDouble(rawValue);
				} catch (NumberFormatException ex) {
					throw new InvalidValueException(
						"Invalid integer format in parameter '" + parameterName + "': " + rawValue, ex);
				}
			case FLOAT:
				try {
					return Float.parseFloat(rawValue);
				} catch (NumberFormatException ex) {
					throw new InvalidValueException(
						"Invalid float format in parameter '" + parameterName + "': " + rawValue, ex);
				}
			case LONG:
				try {
					return (long) Double.parseDouble(rawValue);
				} catch (NumberFormatException ex) {
					throw new InvalidValueException(
						"Invalid long format in parameter '" + parameterName + "': " + rawValue, ex);
				}
			case DATE_TIME:
			case DATE:
				try {
					return XmlDateTimeFormat.INSTANCE.parseObject(rawValue);
				} catch (java.text.ParseException ex) {
					throw new InvalidValueException(
						"Invalid format in parameter '" + parameterName + "': " + rawValue
								+ ". Dates must be formatted in XML dateTime format: http://www.w3.org/TR/xmlschema-2/#dateTime",
						ex);
				}
			case BINARY:
				throw new IllegalArgumentException();
			case BYTE:
				try {
					return BinaryDataFactory.createBinaryData(Base64.getDecoder().decode(rawValue));
				} catch (IllegalArgumentException ex) {
					throw new InvalidValueException(
						"Invalid Base64 encoded value in '" + parameterName + "': " + rawValue, ex);
				}
		}
		throw new UnreachableAssertion("No such format: " + format);
	}

	/**
	 * Report a failure, if this parameter is mandatory.
	 * 
	 * @see #checkNonMandatory(ParameterConfiguration)
	 */
	protected final void checkNonMandatory() throws InvalidValueException {
		checkNonMandatory(getConfig());
	}

	/**
	 * Checks that the given parameter is not {@link ParameterConfiguration#getRequired()
	 * mandatory}.
	 *
	 * @param parameter
	 *        The parameter to check.
	 * @throws InvalidValueException
	 *         iff the given {@link ParameterConfiguration} is
	 *         {@link ParameterConfiguration#getRequired() mandatory}.
	 */
	protected static void checkNonMandatory(ParameterConfiguration parameter) throws InvalidValueException {
		if (parameter.getRequired()) {
			throw new InvalidValueException(
				"Parameter '" + parameter.getName() + "' is mandatory but no value is given.");
		}
	}

}

