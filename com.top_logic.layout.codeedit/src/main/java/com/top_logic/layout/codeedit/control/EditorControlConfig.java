/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.control;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.layout.codeedit.editor.DefaultCodeEditor;

/**
 * Annotation to configure a created {@link CodeEditorControl}.
 * 
 * @see DefaultCodeEditor
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface EditorControlConfig {

	/**
	 * The displayed language mode.
	 */
	String language() default CodeEditorControl.MODE_TEXT;

	/**
	 * {@link CodeEditorControl.WarnLevel} defining how client side problems are delivered to the
	 * underlying form field.
	 */
	CodeEditorControl.WarnLevel warnLevel() default CodeEditorControl.WarnLevel.ERROR;

	/**
	 * If the value is <code>true</code> and the configured {@link #language() language} supports
	 * it, the current value will be displayed pretty.
	 */
	boolean prettyPrinting() default false;
}

