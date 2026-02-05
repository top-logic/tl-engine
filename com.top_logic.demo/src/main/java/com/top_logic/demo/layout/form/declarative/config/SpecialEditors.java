/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.TokenEditor;

/**
 * {@link TypeDemos} part for demonstrating special editors.
 */
@DisplayOrder({
	SpecialEditors.PASSWORD,
	SpecialEditors.TOKEN,
	SpecialEditors.TOKEN_MANDATORY,
})
public interface SpecialEditors extends ConfigurationItem {

	/** Configuration name for {@link #getPassword()}. */
	String PASSWORD = "password";

	/** Configuration name for {@link #getToken()}. */
	String TOKEN = "token";

	/** Configuration name for {@link #getTokenMandatory()}. */
	String TOKEN_MANDATORY = "token-mandatory";

	/**
	 * A mandatory token that is displayed only once in a dialog when edited.
	 *
	 * <p>
	 * The token editor ensures that sensitive token values are shown only during initial entry or
	 * regeneration, not during subsequent edits.
	 * </p>
	 */
	@Mandatory
	@PropertyEditor(TokenEditor.class)
	@Name(TOKEN_MANDATORY)
	@Label("Token (mandatory)")
	String getTokenMandatory();

	/**
	 * An optional token that is displayed only once in a dialog when edited.
	 *
	 * <p>
	 * Similar to {@link #getTokenMandatory()}, but this property is optional.
	 * </p>
	 */
	@PropertyEditor(TokenEditor.class)
	@Name(TOKEN)
	String getToken();

	/**
	 * A password that is stored encrypted.
	 *
	 * <p>
	 * The {@link Encrypted} annotation ensures that the value is encrypted when persisted and
	 * displayed in the UI as a password field with masked characters.
	 * </p>
	 */
	@Encrypted
	@Name(PASSWORD)
	String getPassword();

}