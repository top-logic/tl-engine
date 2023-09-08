/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.ValueDisplay;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * A {@link SearchPart} that has an internationalized name.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TranslatedSearchPart extends SearchPart {

	/** Property name of {@link #getTranslation()}. */
	String TRANSLATION = "translation";

	/** The internationalized name. */
	@Name(TRANSLATION)
	@ReadOnly
	@PropertyEditor(ValueDisplay.class)
	ResKey getTranslation();

	/** @see #getTranslation() */
	void setTranslation(ResKey translation);

}
