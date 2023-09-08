/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * This Renderer renders always a constant text, independent of the value to render.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ConstantTextRenderer implements Renderer<Object> {

    /** Stores the constant text to render. */
    private String text;


    /**
     * Creates a new instance of this class.
     *
     * @param text
     *        the constant text to render.
     */
    public ConstantTextRenderer(String text) {
        this.text = text;
    }

    @Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
        out.writeText(text);
    }

}
