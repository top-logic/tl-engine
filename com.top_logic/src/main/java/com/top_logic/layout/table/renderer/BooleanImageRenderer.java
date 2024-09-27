/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.util.Utils;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class BooleanImageRenderer implements Renderer<Boolean> {

    /** Default renderer for dates. */
    public static final BooleanImageRenderer INSTANCE = new BooleanImageRenderer();

    /**
     * @see com.top_logic.layout.Renderer#write(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, java.lang.Object)
     */
    @Override
	public void write(DisplayContext aContext, TagWriter anOut, Boolean aValue) throws IOException {
		boolean theFlag = Utils.isTrue(aValue);
        
		ResKey tooltip =
			theFlag ? I18NConstants.TRUE_LABEL : I18NConstants.FALSE_LABEL;

		ThemeImage image =
			theFlag ? Icons.TRUE_DISABLED : com.top_logic.layout.provider.Icons.FALSE_DISABLED;

		image.writeWithPlainTooltip(aContext, anOut, tooltip);
    }
}

