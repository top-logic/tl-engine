/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.PopupEditControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.i18n.I18NStringTextPopupControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} creating {@link I18NStringTextPopupControl}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStringTextPopupControlProvider extends PopupEditControl.CP {

	/**
	 * Configuration options for {@link I18NStringTextPopupControlProvider}.
	 */
	public interface Config<I extends I18NStringTextPopupControlProvider> extends PopupEditControl.CP.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link I18NStringTextPopupControlProvider} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public I18NStringTextPopupControlProvider(InstantiationContext context, I18NStringTextPopupControlProvider.Config<?> config) {
		super(context, config);
	}

	@Override
	protected Control createInput(FormMember member) {
		FormField field = (FormField) member;
		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(field);
		int rows;
		int columns;
		if (update != null) {
			rows = I18NStringTagProvider.rows(update);
			columns = I18NStringTagProvider.columns(update);
		} else {
			rows = 0;
			columns = TextInputControl.NO_COLUMNS;
		}
		return new I18NStringTextPopupControl(getSettings(), field, rows, columns);
	}

}