/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.layout;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.service.openapi.common.OpenAPIConstants;

/**
 * Transfer type of a multipart body.
 * 
 * @see "https://www.w3.org/TR/html4/interact/forms.html#h-17.13.4.1"
 */
@Label("Transfer type for multi part body")
public enum MultiPartBodyTransferType implements ExternallyNamed {

	/**
	 * The values for the body are transfered as key/value pairs in the request body.
	 */
	@Label(OpenAPIConstants.APPLICATION_URL_ENCODED_CONTENT_TYPE)
	URL_ENCODED {

	@Override
		public String getExternalName() {
			return OpenAPIConstants.APPLICATION_URL_ENCODED_CONTENT_TYPE;
		}

	},

	/**
	 * The values for the body are transfered as separate parts in the request body.
	 */
	@Label(OpenAPIConstants.MULTIPART_FORM_DATA_CONTENT_TYPE)
	FORM_DATA {

		@Override
		public String getExternalName() {
			return OpenAPIConstants.MULTIPART_FORM_DATA_CONTENT_TYPE;
		}

	},
	;

}
