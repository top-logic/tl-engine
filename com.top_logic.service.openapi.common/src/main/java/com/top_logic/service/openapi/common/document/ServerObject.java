/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.Map;

import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * An object representing a Server.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#server-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ServerObject.URL,
	ServerObject.DESCRIPTION,
	ServerObject.VARIABLES,
})
public interface ServerObject extends Described {

	/** Configuration name for the value of {@link #getUrl()}. */
	String URL = "url";

	/** Configuration name for the value of {@link #getVariables()}. */
	String VARIABLES = "variables";

	/**
	 * A URL to the target host. This URL supports Server Variables and MAY be relative, to indicate
	 * that the host location is relative to the location where the <i>OpenAPI</i> document is
	 * being served. Variable substitutions will be made when a variable is named in {brackets}.
	 */
	@Mandatory
	@Name(URL)
	String getUrl();

	/**
	 * Setter for {@link #getUrl()};
	 */
	void setUrl(String value);

	/**
	 * An optional string describing the host designated by the URL.
	 */
	@Override
	String getDescription();
	
	/**
	 * A map between a variable name and its value. The value is used for substitution in the
	 * server's URL template.
	 */
	@Key(ServerVariableObject.NAME_ATTRIBUTE)
	@Name(VARIABLES)
	Map<String, ServerVariableObject> getVariables();

}

