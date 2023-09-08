/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.net.MalformedURLException;
import java.net.URL;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;

/**
 * {@link Constraint} that checks that the value can be parsed as {@link URL}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasURLFormat extends ValueConstraint<String> {

	/**
	 * Creates a new {@link HasURLFormat}.
	 */
	public HasURLFormat() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		String value = propertyModel.getValue();
		if (StringServices.isEmpty(value)) {
			return;
		}

		try {
			parseAsURL(value);
		} catch (MalformedURLException ex) {
			String message = ex.getMessage();
			if (StringServices.startsWithIgnoreCase(message, "no protocol:")) {
				propertyModel
					.setProblemDescription(I18NConstants.NO_URL_FORMAT_NO_PROTOCOL__VALUE.fill(value));
				return;
			}
			String unknownProtocolPrefix = "unknown protocol: ";
			if (message.startsWith(unknownProtocolPrefix)) {
				propertyModel
					.setProblemDescription(I18NConstants.NO_URL_FORMAT_UNKNOWN_PROTOCOL__VALUE__PROTOCOL.fill(value,
						message.substring(unknownProtocolPrefix.length())));
				return;
			}
			propertyModel
				.setProblemDescription(I18NConstants.NO_URL_FORMAT__VALUE__MESSAGE.fill(value, message));
		}

	}

	private URL parseAsURL(String value) throws MalformedURLException {
		return new URL(value);
	}

}

