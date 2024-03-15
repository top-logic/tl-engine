/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.ref;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.editor.AllComponents;
import com.top_logic.layout.editor.ComponentNameMapping;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Selection from all application components including invisible ones.
 */
@TagName("global-target")
public interface GlobalComponent extends NamedComponent {

	@Override
	@Options(fun = AllComponents.class, mapping = ComponentNameMapping.class)
	@ControlProvider(SelectionControlProvider.class)
	ComponentName getName();

}
