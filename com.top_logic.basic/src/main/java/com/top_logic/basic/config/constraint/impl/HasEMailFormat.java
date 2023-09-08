/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;

/**
 * {@link Constraint} that checks that value is a valid E-Mail address.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasEMailFormat extends ValueConstraint<String> {

	private static final String PART_CHARS = "A-Za-z0-9.!#$%&'*+-/=?^_`{|}~";

	private static final Pattern MAIL_ADDRESS_PART = Pattern.compile("[A-Za-z0-9\\.!#\\$%&'*+-/=\\?\\^_`\\{|\\}~]+");

	/**
	 * Creates a new {@link HasEMailFormat}.
	 */
	public HasEMailFormat() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		String value = propertyModel.getValue();
		if (StringServices.isEmpty(value)) {
			return;
		}
		int partDomainSeparator = value.indexOf('@');
		if (partDomainSeparator < 0) {
			propertyModel.setProblemDescription(I18NConstants.NO_EMAIL_FORMAT_NO_AT_SIGN__VALUE.fill(value));
			return;
		}
		if (value.indexOf('@', partDomainSeparator + 1) >= 0) {
			propertyModel.setProblemDescription(I18NConstants.NO_EMAIL_FORMAT_MULTIPLE_AT_SIGNS__VALUE.fill(value));
			return;
		}
		if (!MAIL_ADDRESS_PART.matcher(value.substring(0, partDomainSeparator)).matches()) {
			propertyModel.setProblemDescription(
				I18NConstants.NO_EMAIL_FORMAT_INVALID_LOCAL_PART__VALUE__PART_CHARS.fill(value, PART_CHARS));
			return;
		}
		if (!MAIL_ADDRESS_PART.matcher(value.substring(partDomainSeparator + 1)).matches()) {
			propertyModel.setProblemDescription(
				I18NConstants.NO_EMAIL_FORMAT_INVALID_DOMAIN_PART__VALUE__PART_CHARS.fill(value, PART_CHARS));
			return;
		}
	}

}

