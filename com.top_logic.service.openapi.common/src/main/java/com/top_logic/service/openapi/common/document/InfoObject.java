/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.HasURLFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;

/**
 * The object provides metadata about the API. The metadata MAY be used by the clients if needed,
 * and MAY be presented in editing or documentation generation tools for convenience.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#info-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	InfoObject.TITLE,
	InfoObject.DESCRIPTION,
	InfoObject.VERSION,
	InfoObject.TERMS_OF_SERVICE,
	InfoObject.CONTACT,
	InfoObject.LICENSE,
})
public interface InfoObject extends Described {

	/** Configuration name for the value of {@link #getVersion()}. */
	String VERSION = "version";

	/** Configuration name for the value of {@link #getTitle()}. */
	String TITLE = "title";

	/** Configuration name for the value of {@link #getContact()}. */
	String CONTACT = "contact";

	/** Configuration name for the value of {@link #getLicense()}. */
	String LICENSE = "license";

	/** Configuration name for the value of {@link #getTermsOfService()}. */
	String TERMS_OF_SERVICE = "termsOfService";

	/**
	 * The title of the API.
	 */
	@Mandatory
	@Name(TITLE)
	String getTitle();

	/**
	 * Setter for {@link #getTitle()}.
	 */
	void setTitle(String value);

	/**
	 * The version of the <i>OpenAPI</i> document.
	 */
	@Mandatory
	@Name(VERSION)
	String getVersion();

	/**
	 * Setter for {@link #getVersion()}.
	 */
	void setVersion(String value);

	/**
	 * The contact information for the exposed API.
	 */
	@Name(CONTACT)
	@DisplayMinimized
	ContactObject getContact();

	/**
	 * Setter for {@link #getContact()}.
	 */
	void setContact(ContactObject value);

	/**
	 * The license information for the exposed API.
	 */
	@Name(LICENSE)
	@DisplayMinimized
	LicenseObject getLicense();

	/**
	 * Setter for {@link #getLicense()}.
	 */
	void setLicense(LicenseObject value);

	/**
	 * A URL to the Terms of Service for the API. MUST be in the format of a URL.
	 */
	@Constraint(value = HasURLFormat.class, asWarning = true)
	@Name(TERMS_OF_SERVICE)
	String getTermsOfService();

	/**
	 * Setter for {@link #getTermsOfService()}.
	 */
	void setTermsOfService(String value);
}

