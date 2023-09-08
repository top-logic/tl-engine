/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.HasEMailFormat;
import com.top_logic.basic.config.constraint.impl.HasURLFormat;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Contact information for the exposed API.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3#contact-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ContactObject.NAME_ATTRIBUTE,
	ContactObject.EMAIL,
	ContactObject.URL,
})
@Label("Contact")
public interface ContactObject extends NamedConfiguration {

	/** Configuration name for the value {@link #getUrl()}. */
	String URL = "url";

	/** Configuration name for the value {@link #getEmail()}. */
	String EMAIL = "email";

	/**
	 * The identifying name of the contact person/organization.
	 * 
	 * @see com.top_logic.basic.config.NamedConfiguration#getName()
	 */
	@Override
	String getName();

	/**
	 * The email address of the contact person/organization. MUST be in the format of an email
	 * address.
	 */
	@Constraint(value = HasEMailFormat.class, asWarning = true)
	@Nullable
	@Name(EMAIL)
	String getEmail();

	/**
	 * Setter for {@link #getEmail()}.
	 */
	void setEmail(String value);

	/**
	 * The URL pointing to the contact information. MUST be in the format of a URL.
	 */
	@Constraint(value = HasURLFormat.class, asWarning = true)
	@Nullable
	@Name(URL)
	String getUrl();

	/**
	 * Setter for {@link #getUrl()}.
	 */
	void setUrl(String value);
}

