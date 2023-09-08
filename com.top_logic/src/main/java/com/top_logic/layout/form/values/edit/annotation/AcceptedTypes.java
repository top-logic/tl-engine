/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.gui.layout.upload.FileNameStrategy;
import com.top_logic.knowledge.gui.layout.upload.NoNameCheck;
import com.top_logic.layout.form.values.edit.editor.BinaryDataEditor;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Annotation to a {@link BinaryData}-valued property specifying the <code>accept</code> attribute
 * of the upload field.
 * 
 * <p>
 * Note: The property must also be annotated with {@link PropertyEditor} selecting the
 * {@link BinaryDataEditor}.
 * </p>
 * 
 * @see HTMLConstants#ACCEPT_ATTR
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AcceptedTypes {

	/**
	 * The accepted file types as client-side hint for the upload dialog, e.g. "image/*".
	 * 
	 * <p>
	 * Note: Without a {@link #checker()}, the client may still be able to upload files that do not
	 * match the criteria given here.
	 * </p>
	 */
	String[] value();

	/**
	 * The 
	 */
	Class<? extends FileNameStrategy> checker() default NoNameCheck.class;

}
