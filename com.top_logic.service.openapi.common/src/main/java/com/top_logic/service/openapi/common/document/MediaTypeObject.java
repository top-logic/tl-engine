/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.format.XMLFragmentString;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Each Media Type Object provides schema and examples for the media type identified by its key.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#media-type-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	MediaTypeObject.MEDIA_TYPE,
	MediaTypeObject.SCHEMA,
	MediaTypeObject.EXAMPLE,
})
public interface MediaTypeObject extends ConfigurationItem {

	/** Configuration name for the value of {@link #getExample()}. */
	String EXAMPLE = "example";

	/** Configuration name for the value of {@link #getSchema()}. */
	String SCHEMA = "schema";

	/** Configuration name for the value of {@link #getMediaType()}. */
	String MEDIA_TYPE = "mediaType";

	/**
	 * Name of the represented media type.
	 */
	@Name(MEDIA_TYPE)
	@Mandatory
	String getMediaType();

	/**
	 * Setter for {@link #getMediaType()}.
	 */
	void setMediaType(String value);

	/**
	 * The schema defining the content of the request, response, or parameter.
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
	 * Example of the media type. The example object SHOULD be in the correct format as specified by
	 * the media type.
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

}

