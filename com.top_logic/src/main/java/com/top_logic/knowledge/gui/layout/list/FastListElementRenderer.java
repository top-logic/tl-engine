/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.list;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FastListElementRenderer implements Renderer<FastListElement> {

    public static final FastListElementRenderer INSTANCE = new FastListElementRenderer();

    @Override
	public void write(DisplayContext aContext, TagWriter anOut, FastListElement aValue) throws IOException {
		anOut.writeText(FastListElementLabelProvider.INSTANCE.getLabel(aValue));
    }
}
