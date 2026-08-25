/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;

/**
 * {@link ConfigControlProvider} that edits a {@link ResKey} property the way an
 * <i>internationalized string</i> model attribute is edited: an inline field in the user's own
 * language, plus a button opening the editor for the other languages.
 *
 * <p>
 * The {@code impl} for a {@link ConfigControlService.FormatMapping} entry that claims a property
 * whose value provider produces a {@link ResKey}, e.g. {@link ResKey.ValueFormat}. Without that
 * claim, such a property would reach the built-in fallback, which can only offer the value's
 * <em>encoded</em> form as text - a single string carrying every language at once, in the
 * framework's own encoding. Nobody edits a translation that way; the same value in a model has an
 * input per language and machine translation between them, and a configured one should be no
 * different.
 * </p>
 *
 * <p>
 * Claiming the property is also what gives this control the value it needs: the claim makes
 * {@link ConfigControlService#createModel(com.top_logic.basic.config.ConfigurationItem,
 * com.top_logic.basic.config.PropertyDescriptor) createModel} hand out the typed
 * {@link ConfigFieldModel} over the {@link ResKey} itself, rather than the
 * {@link ConfigFormatFieldModel} over its encoded text - and a {@link ResKey} is exactly what
 * {@link ReactI18NStringInputControl} reads and writes.
 * </p>
 */
public class I18NStringFormatProvider extends AbstractConfiguredInstance<I18NStringFormatProvider.Config>
		implements ConfigControlProvider {

	/**
	 * Configuration options for {@link I18NStringFormatProvider}.
	 *
	 * <p>
	 * No {@code @ClassDefault} is needed: the type parameter is bound directly to
	 * {@link I18NStringFormatProvider} (there is no further specialization of this configuration),
	 * so {@link #getImplementationClass()}'s default already follows from that bound - see
	 * {@link PolymorphicConfiguration}'s own {@code @implNote}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<I18NStringFormatProvider> {

		/**
		 * The number of visible rows of the inline input, or {@code 0} to render it as a single
		 * line.
		 */
		@IntDefault(0)
		int getRows();

	}

	/**
	 * Creates a {@link I18NStringFormatProvider}.
	 */
	@CalledByReflection
	public I18NStringFormatProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		String label = Labels.propertyLabel(model.getProperty(), false);
		return ReactI18NStringInputControl.createEditor(context, model, getConfig().getRows(), label);
	}

}
