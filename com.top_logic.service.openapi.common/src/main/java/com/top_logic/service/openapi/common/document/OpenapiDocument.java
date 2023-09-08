/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Configuration specifying an <i>OpenAPI</i> document.
 * 
 * <p>
 * The configuration currently specifies version {@value #VERSION_3_0_3}.
 * </p>
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html"
 * @see "https://spec.openapis.org/oas/v3.0.3.html#openapi-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	OpenapiDocument.OPENAPI,
	OpenapiDocument.INFO,
	WithServers.SERVERS,
	OpenapiDocument.PATHS,
	OpenapiDocument.COMPONENTS,
	WithSecurity.SECURITY,
	OpenapiDocument.TAGS,
})
public interface OpenapiDocument extends WithServers, WithSecurity {

	/** Version of an <i>OpenAPI</i> document: 3.0.3 */
	String VERSION_3_0_3 = "3.0.3";

	/** Configuration name for the value of {@link #getComponents()}. */
	String COMPONENTS = "components";

	/** Configuration name for the value of {@link #getPaths()}. */
	String PATHS = "paths";

	/** Configuration name for the value of {@link #getInfo()}. */
	String INFO = "info";

	/** Configuration name for the value of {@link #getOpenapi()}. */
	String OPENAPI = "openapi";

	/** Configuration name for the value of {@link #getTags()}. */
	String TAGS = "tags";

	/**
	 * Version number of the <i>OpenAPI</i> Specification that the <i>OpenAPI</i>
	 * document uses.
	 */
	@Mandatory
	@Name(OPENAPI)
	String getOpenapi();

	/**
	 * Setter for {@link #getOpenapi()}.
	 */
	void setOpenapi(String value);

	/**
	 * Provides metadata about the API.
	 */
	@Mandatory
	@Name(INFO)
	InfoObject getInfo();

	/**
	 * Setter for {@link #getInfo()}.
	 */
	void setInfo(InfoObject value);

	/**
	 * An array of Server Objects, which provide connectivity information to a target server. If the
	 * servers property is not provided, or is an empty array, the default value would be a Server
	 * Object with a url value of /.
	 */
	@Override
	List<ServerObject> getServers();

	/**
	 * A declaration of which security mechanisms can be used across the API. The list of values
	 * includes alternative security requirement objects that can be used. Only one of the security
	 * requirement objects need to be satisfied to authorize a request. Individual operations can
	 * override this definition. To make security optional, an empty security requirement ({}) can
	 * be included in the array.
	 */
	@Override
	List<Map<String, List<String>>> getSecurity();

	/**
	 * The available paths and operations for the API.
	 */
	@Key(PathItemObject.PATH)
	@Name(PATHS)
	Map<String, PathItemObject> getPaths();

	/**
	 * An element to hold various schemas for the specification.
	 */
	@Name(COMPONENTS)
	ComponentsObject getComponents();

	/**
	 * Setter for {@link #getComponents()}.
	 */
	void setComponents(ComponentsObject value);

	/**
	 * A list of tags used by the specification with additional metadata. The order of the tags can
	 * be used to reflect on their order by the parsing tools. Not all tags that are used by the
	 * {@link OperationObject} must be declared. The tags that are not declared MAY be organized
	 * randomly or based on the tools logic. Each tag name in the list MUST be unique.
	 */
	@Name(TAGS)
	@Key(TagObject.NAME_ATTRIBUTE)
	List<TagObject> getTags();

}

