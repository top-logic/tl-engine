/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.codeedit;

import com.top_logic.layout.configedit.ConfigAnnotationControlProvider;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.form.values.edit.annotation.Code;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * {@link ConfigAnnotationControlProvider} editing the source code of a property annotated with
 * {@link Code} in a {@link ReactCodeEditorControl} for the annotated language.
 *
 * <p>
 * Only a value {@link ConfigControlService#holdsText(ConfigFieldModel) held as text} is edited in
 * the code editor. Any other property is left to the control the {@link ConfigControlService}
 * chooses without the annotation.
 * </p>
 */
public class CodeConfigProvider implements ConfigAnnotationControlProvider<Code> {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, Code annotation) {
		if (!ConfigControlService.holdsText(model)) {
			return null;
		}
		return new ReactCodeEditorControl(context, model, CodeEditorLanguage.of(annotation.language()));
	}

}
