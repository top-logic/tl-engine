/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;

/**
 * Label provider for {@link LayoutTemplate}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutTemplateLabelProvider extends Function1<ResKey, String> {

	@Override
	public ResKey apply(String relativeTemplatePath) {
		ResPrefix prefix = LayoutTemplateUtils.getPrefixResKey(relativeTemplatePath);

		return prefix.key(DynamicComponentService.LABEL_KEY_SUFFIX);
	}

}
