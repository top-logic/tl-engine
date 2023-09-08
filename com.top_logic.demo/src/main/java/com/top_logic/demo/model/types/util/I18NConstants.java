/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.util.Resources;

/**
 * Internationalization constants of this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * {@link MailFolder} empty.
	 */
	public static ResKey NOT_EXECUTABLE_EMPTY_MAIL_FOLDER;

	/**
	 * No {@link MailFolder}.
	 */
	public static ResKey NOT_EXECUTABLE_NO_MAIL_FOLDER;

	/**
	 * File name of export.
	 */
	public static ResKey1 DEMO_EXPORT_FILENAME__NAME;

	/**
	 * Failure message.
	 */
	public static ResKey1 DEMO_EXPORT_FAILURE__MSG;

	/**
	 * {@link Resources} prefixes used by {@link TypeGenerator}
	 */
	public static ResPrefix TYPE_GENERATOR_DIALOG;

	public static ResPrefix TYPE_GENERATOR;
	
	static {
		initConstants(I18NConstants.class);
	}
}
