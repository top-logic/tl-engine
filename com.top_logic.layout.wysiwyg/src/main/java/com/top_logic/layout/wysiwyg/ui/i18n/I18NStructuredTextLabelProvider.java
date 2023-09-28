/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.label.NullSafeLabelProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.util.TLContext;

/**
 * {@link LabelProvider} for an {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStructuredTextLabelProvider extends NullSafeLabelProvider {

	@Override
	protected String getLabelNullSafe(Object model) {
		StructuredText localized = ((I18NStructuredText) model).localize(TLContext.getLocale());
		return MetaLabelProvider.INSTANCE.getLabel(localized);
	}

}