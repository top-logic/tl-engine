/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.format.XMLFragmentString;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Describes a single operation parameter.
 * 
 * A unique parameter is defined by a combination of a name and location.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#parameter-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ParameterObject.NAME_ATTRIBUTE,
	ParameterObject.IN,
	ParameterObject.DESCRIPTION,
	ParameterObject.REQUIRED,
	ParameterObject.SCHEMA,
	ParameterObject.EXAMPLE,
})
public interface ParameterObject extends NamedConfigMandatory, Described, IParameterObject {

	/** Configuration name for the value of {@link #getSchema()}. */
	String SCHEMA = "schema";

	/** Configuration name for the value of {@link #getDescription()}. */
	String DESCRIPTION = "description";

	/** Configuration name for the value of {@link #isRequired()}. */
	String REQUIRED = "required";

	/** Configuration name for the value of {@link #getIn()}. */
	String IN = "in";

	/** Configuration name for the value of {@link #getExample()}. */
	String EXAMPLE = "example";

	/**
	 * The location of the parameter.
	 */
	@Mandatory
	@Name(IN)
	ParameterLocation getIn();

	/**
	 * Setter for {@link #getIn()}.
	 */
	void setIn(ParameterLocation value);

	/**
	 * Determines whether this parameter is mandatory. If the parameter location is "path", this
	 * property is REQUIRED and its value MUST be true. Otherwise, the property MAY be included and
	 * its default value is false.
	 */
	@Constraint(value = RequiredIfParameterInPath.class, args = { @Ref(IN) })
	@Name(REQUIRED)
	boolean isRequired();

	/**
	 * Setter for {@link #isRequired()}.
	 */
	void setRequired(boolean value);

	/**
	 * A brief description of the parameter. This could contain examples of use.
	 */
	@Override
	String getDescription();

	/**
	 * The schema defining the type used for the parameter.
	 */
	@Name(SCHEMA)
	@JsonBinding(SchemaJsonBinding.class)
	@Binding(XMLFragmentString.class)
	@Nullable
	String getSchema();

	/**
	 * Setter for {@link #getSchema()}.
	 */
	void setSchema(String value);

	/**
	 * Example of the parameter's potential value. The example SHOULD match the specified schema and
	 * encoding properties if present. The example field is mutually exclusive of the examples
	 * field. Furthermore, if referencing a schema that contains an example, the example value SHALL
	 * override the example provided by the schema. To represent examples of media types that cannot
	 * naturally be represented in JSON or YAML, a string value can contain the example with
	 * escaping where necessary.
	 */
	@Name(EXAMPLE)
	@JsonBinding(SchemaJsonBinding.class)
	@Binding(XMLFragmentString.class)
	@Nullable
	String getExample();

	/**
	 * Setter for {@link #getExample()}.
	 */
	void setExample(String value);

	/**
	 * Dependency that ensures that a parameter is required, when it is located in
	 * {@link ParameterLocation#PATH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	class RequiredIfParameterInPath extends GenericValueDependency<ParameterLocation, Boolean> {

		/**
		 * Creates a {@link RequiredIfParameterInPath}.
		 */
		public RequiredIfParameterInPath() {
			super(ParameterLocation.class, Boolean.class);
		}

		@Override
		protected void checkValue(PropertyModel<ParameterLocation> in, PropertyModel<Boolean> required) {
			if (in.getValue() != ParameterLocation.PATH) {
				return;
			}
			Boolean isRequired = required.getValue();
			if (isRequired == null || isRequired.booleanValue() != true) {
				required.setProblemDescription(
					I18NConstants.ERROR_REQUIRE_MUST_BE_SET_ON_PATH_PARAMETER);
			}
		}
	}

}

