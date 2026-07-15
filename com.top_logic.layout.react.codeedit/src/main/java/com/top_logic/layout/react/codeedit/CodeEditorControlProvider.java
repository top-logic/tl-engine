/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.codeedit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.ReactFieldControlProvider;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} that renders a source-code editor for a string or text
 * attribute.
 *
 * <p>
 * Selected for an attribute through an {@code <input-control>} annotation; the configured language
 * fixes syntax highlighting and validation.
 * </p>
 */
public class CodeEditorControlProvider
		extends AbstractConfiguredInstance<CodeEditorControlProvider.Config<?>>
		implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link CodeEditorControlProvider}.
	 */
	public interface Config<I extends CodeEditorControlProvider> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getLanguage()}. */
		String LANGUAGE = "language";

		/**
		 * The source language. Determines syntax highlighting and, where supported, validation.
		 *
		 * <p>
		 * Defaults to plain text when not set.
		 * </p>
		 */
		@Name(LANGUAGE)
		CodeEditorLanguage getLanguage();

	}

	/**
	 * Creates a new {@link CodeEditorControlProvider} from configuration.
	 */
	@CalledByReflection
	public CodeEditorControlProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		CodeEditorLanguage language = getConfig().getLanguage();
		return new ReactCodeEditorControl(context, model,
			language != null ? language : CodeEditorLanguage.PLAIN);
	}

}
