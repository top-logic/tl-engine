/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.util.ResKey;

/**
 * Specifies a custom resource key for a {@link ResKey} field in an {@link I18NConstantsBase}
 * subclass.
 *
 * <p>
 * By default, the resource key for a field is derived from the fully-qualified class name and the
 * field name (e.g. <tt>class.com.my.pkg.I18NConstants.MY_FIELD</tt>). This annotation overrides
 * that default with a custom key.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * &#64;CustomKey("js.audioPlayer.play")
 * public static ResKey JS_AUDIO_PLAYER_PLAY;
 * </pre>
 *
 * <p>
 * The field must <b>not</b> be pre-initialized (e.g. with {@link ResKey#internalCreate(String)}).
 * The annotation is read both at runtime (by {@link I18NConstantsBase#initConstants(Class)}) and at
 * build time (by the TLDoclet) to ensure that the JavaDoc-based localization is stored under the
 * correct key.
 * </p>
 *
 * @see I18NConstantsBase
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CustomKey {

	/**
	 * The custom resource key to use instead of the normative key derived from the class and field
	 * name.
	 */
	String value();

}
