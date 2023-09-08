/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.mode;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;

/**
 * {@link DynamicMode#fun() Function} that allows setting annotated properties to a specific mode
 * defined when creating the form.
 * 
 * <p>
 * The custom mode can be defined by by setting the {@link #MODE_PROPERTY} property on the
 * {@link InitializerProvider} when creating the form.
 * </p>
 * 
 * @see EditorFactory#EditorFactory(com.top_logic.layout.form.values.edit.initializer.InitializerProvider)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomMode extends Function0<FieldMode> {

	/**
	 * {@link Property} to set for customizing
	 */
	public static final Property<FieldMode> MODE_PROPERTY =
		TypedAnnotatable.property(FieldMode.class, "customMode", FieldMode.ACTIVE);

	private FieldMode _customMode;

	/**
	 * Creates a {@link CustomMode}.
	 */
	public CustomMode(DeclarativeFormOptions options) {
		_customMode = options.get(MODE_PROPERTY);
	}

	@Override
	public FieldMode apply() {
		return _customMode;
	}

}
