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
 * A container for the expected responses of an operation. The container maps a HTTP response code
 * to the expected response.
 * 
 * The documentation is not necessarily expected to cover all possible HTTP response codes because
 * they may not be known in advance. However, documentation is expected to cover a successful
 * operation response and any known errors.
 * 
 * The default MAY be used as a default response object for all HTTP codes that are not covered
 * individually by the specification.
 * 
 * The Responses Object MUST contain at least one response code, and it SHOULD be the response for a
 * successful operation call.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#responses-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ResponsesObject.STATUS_CODE,
	ResponsesObject.DESCRIPTION,
	ResponsesObject.CONTENT,
})
public interface ResponsesObject extends Described {

	/** Configuration name for the value of {@link #getContent()}. */
	String CONTENT = "content";

	/** Configuration name for the value of {@link #getStatusCode()}. */
	String STATUS_CODE = "statusCode";

	/**
	 * Any HTTP status code can be used as the property name, but only one property per code, to
	 * describe the expected response for that HTTP status code. A Reference Object can link to a
	 * response that is defined in the <i>OpenAPI</i> Object's components/responses section.
	 * This field MUST be enclosed in quotation marks (for example, "200") for compatibility between
	 * JSON and YAML. To define a range of response codes, this field MAY contain the uppercase
	 * wildcard character X. For example, 2XX represents all response codes between [200-299]. Only
	 * the following range definitions are allowed: 1XX, 2XX, 3XX, 4XX, and 5XX. If a response is
	 * defined using an explicit code, the explicit code definition takes precedence over the range
	 * definition for that code.
	 */
	@Name(STATUS_CODE)
	@Mandatory
	String getStatusCode();

	/**
	 * Setter for {@link #getStatusCode()}.
	 */
	void setStatusCode(String value);

	/**
	 * A short description of the response.
	 */
	@Name(DESCRIPTION)
	@Override
	String getDescription();

	/**
	 * A map containing descriptions of potential response payloads. The key is a media type or
	 * media type range and the value describes it. For responses that match multiple keys, only the
	 * most specific key is applicable. e.g. text/plain overrides text/*.
	 */
	@Name(CONTENT)
	@Key(MediaTypeObject.MEDIA_TYPE)
	Map<String, MediaTypeObject> getContent();
}

