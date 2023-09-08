/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStringArray;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.InAppImplementations;

/**
 * Annotation of {@link InApp#classifiers() classifiers} to applicable in-app implementations to be
 * selected by the {@link InAppImplementations} option provider function.
 * 
 * @see InAppImplementations
 * @see InApp#classifiers()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@TagName("acceptable-classifiers")
public @interface AcceptableClassifiers {

	/**
	 * List of classifiers of {@link InApp} annotated implementations that can be selected by the
	 * {@link InAppImplementations} options provider.
	 * 
	 * @see InApp#classifiers()
	 */
	@Format(CommaSeparatedStringArray.class)
	String[] value();
	
	/**
	 * Whether unclassified implementations are considered to match.
	 */
	boolean unclassified() default true;

}

