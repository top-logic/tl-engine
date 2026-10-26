/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.annotation;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.func.Function0;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;

/**
 * All available {@link StructuredText} editor features.
 * 
 * <p>
 * Option function for {@link StructuredTextEditorConfig#getFeatures()}, attached to that property
 * by the form customizations in <code>modelWysiwygConf.config.xml</code>. The options are the
 * {@link StructuredTextConfigService#getFeatures() features} of the HTML editor.
 * </p>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredEditorConfigOptions extends Function0<Set<String>> {

	@Override
	public Set<String> apply() {
		Map<String, Object> features = StructuredTextConfigService.getInstance().getFeatures();

		return features.keySet();
	}

}
