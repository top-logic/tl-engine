/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Message to display when name does not match pattern
	 * {@link PartNameConstraints#RECOMMENDED_TYPE_PART_NAME_PATTERN}.
	 */
	public static ResKey RECOMMENDED_TYPE_PART_NAME_MISMATCH;

	/**
	 * Message to display when name does not match pattern
	 * {@link PartNameConstraints#RECOMMENDED_CLASSIFIER_NAME_PATTERN}.
	 */
	public static ResKey RECOMMENDED_CLASSIFIER_NAME_MISMATCH;

	/**
	 * Message to display when name does not match pattern
	 * {@link PartNameConstraints#RECOMMENDED_TYPE_NAME_PATTERN}.
	 */
	public static ResKey RECOMMENDED_TYPE_NAME_MISMATCH;

	/**
	 * Message to display when name does not match pattern
	 * {@link PartNameConstraints#MANDATORY_TYPE_PART_NAME_PATTERN}.
	 */
	public static ResKey MANDATORY_TYPE_PART_NAME_MISMATCH;

	/**
	 * Message to display when name does not match pattern
	 * {@link PartNameConstraints#MANDATORY_MODULE_NAME_PATTERN}.
	 */
	public static ResKey MANDATORY_MODULE_NAME_MISMATCH;

	/**
	 * Message to display when name does not match pattern
	 * {@link PartNameConstraints#MANDATORY_TYPE_NAME_PATTERN}.
	 */
	public static ResKey MANDATORY_TYPE_NAME_MISMATCH;

    static {
        initConstants(I18NConstants.class);
    }

}
