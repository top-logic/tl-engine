/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.control;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.control.WithPlaceHolder;
import com.top_logic.layout.form.i18n.I18NField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * A {@link TranslatedI18nFieldControl} for string attributes.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TranslatedI18nStringFieldControl extends TranslatedI18nFieldControl<TextInputControl>
		implements WithPlaceHolder {

	/** The {@link ControlProvider} for the {@link TranslatedI18nStringFieldControl}. */
	public static class Provider extends TranslatedI18nFieldControl.Provider {

		/**
		 * Create a {@link TranslatedI18nStringFieldControl.Provider}.
		 * 
		 * @param context
		 *        the {@link InstantiationContext} to create the new object in
		 * @param config
		 *        the configuration object to be used for instantiation
		 */
		public Provider(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected Control createControl(I18NField<?, ?, ?> i18nField, ComponentChannel language, String style) {
			return new TranslatedI18nStringFieldControl(i18nField, language, style);
		}

	}

	/**
	 * Creates a {@link TranslatedI18nStringFieldControl}.
	 * <p>
	 * See
	 * {@link TranslatedI18nFieldControl#TranslatedI18nFieldControl(I18NField, ComponentChannel, String)}
	 * for the parameter documentation.
	 * </p>
	 */
	public TranslatedI18nStringFieldControl(I18NField<?, ?, ?> i18nField, ComponentChannel language, String style) {
		super(i18nField, language, style);
	}

	@Override
	protected TextInputControl createInnerControl(FormField innerField, String style) {
		return new TextInputControl(innerField);
	}

	@Override
	public String getPlaceHolder() {
		return getInnerControl().getPlaceHolder();
	}

	@Override
	public void setPlaceHolder(String placeHolder) {
		getInnerControl().setPlaceHolder(placeHolder);
	}

}
