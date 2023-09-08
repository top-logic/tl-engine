/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Holds a set of reusable objects for different aspects of the OAS. All objects defined within the
 * components object will have no effect on the API unless they are explicitly referenced from
 * properties outside the components object.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#components-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ComponentsObject.SCHEMAS,
	ComponentsObject.SECURITY_SCHEMES,
	ComponentsObject.PARAMETERS,
})
public interface ComponentsObject extends ConfigurationItem {

	/** Configuration name for the value of {@link #getSecuritySchemes()}. */
	String SECURITY_SCHEMES = "securitySchemes";

	/** Configuration name for the value of {@link #getSchemas()}. */
	String SCHEMAS = "schemas";

	/** Configuration name for the value of {@link #getParameters()}. */
	String PARAMETERS = "parameters";

	/**
	 * Reusable {@link SecuritySchemeObject}
	 */
	@Key(SecuritySchemeObject.SCHEMA_NAME)
	@Name(SECURITY_SCHEMES)
	Map<String, SecuritySchemeObject> getSecuritySchemes();

	/**
	 * Reusable {@link SchemaObject}s.
	 */
	@Key(SchemaObject.NAME_ATTRIBUTE)
	@Name(SCHEMAS)
	@JsonBinding(SchemaObjectsJsonBinding.class)
	Map<String, SchemaObject> getSchemas();

	/**
	 * Reusable {@link ParameterObject}s.
	 */
	@Key(ReferencableParameterObject.REFERENCE_NAME)
	@Name(PARAMETERS)
	Map<String, ReferencableParameterObject> getParameters();

}

