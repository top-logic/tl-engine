/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.HasURLFormat;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * License information for the exposed API.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#license-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	LicenseObject.NAME_ATTRIBUTE,
	LicenseObject.URL,
})
@Label("License")
public interface LicenseObject extends NamedConfigMandatory {

	/**
	 * Configuration name for {@link #getUrl()}.
	 */
	String URL = "url";

	/**
	 * The license name used for the API.
	 * 
	 * @see com.top_logic.basic.config.NamedConfigMandatory#getName()
	 */
	@Override
	String getName();

	/**
	 * A URL to the license used for the API. MUST be in the format of a URL.
	 */
	@Name(URL)
	@Constraint(value = HasURLFormat.class, asWarning = true)
	String getUrl();

	/**
	 * Setter for {@link #getUrl()}
	 */
	void setUrl(String value);

}

