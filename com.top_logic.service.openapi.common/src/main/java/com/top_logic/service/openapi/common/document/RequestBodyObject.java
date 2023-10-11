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
 * Describes a single request body.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3#request-body-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	RequestBodyObject.DESCRIPTION,
	RequestBodyObject.REQUIRED,
	RequestBodyObject.CONTENT,
})
public interface RequestBodyObject extends Described {

	/** Configuration name for the value of {@link #getContent()}. */
	String CONTENT = "content";

	/** Configuration name for the value of {@link #isRequired()}. */
	String REQUIRED = "required";

	/**
	 * A brief description of the request body. This could contain examples of use.
	 * 
	 * <p>
	 * <i>CommonMark</i> syntax may be used for rich text representation.
	 * </p>
	 */
	@Override
	String getDescription();

	/**
	 * Determines if the request body is required in the request. Defaults to false.
	 */
	@Name(REQUIRED)
	boolean isRequired();

	/**
	 * Setter for {@link #isRequired()}.
	 */
	void setRequired(boolean value);

	/**
	 * A map containing descriptions of potential response payloads. The key is a media type or
	 * media type range and the value describes it. For responses that match multiple keys, only the
	 * most specific key is applicable. e.g. text/plain overrides text/*.
	 */
	@Name(CONTENT)
	@Mandatory
	@Key(MediaTypeObject.MEDIA_TYPE)
	Map<String, MediaTypeObject> getContent();
}

