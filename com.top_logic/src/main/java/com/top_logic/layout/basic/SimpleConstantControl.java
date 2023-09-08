/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * A {@link Control} displaying a {@link #getModel() model} my means of a {@link Renderer}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SimpleConstantControl<T> extends ConstantControl<T> {

    /** The renderer to be used in {@link #internalWrite(DisplayContext, TagWriter)}. */
	private Renderer<? super T> renderer;

    /** 
     * Create a new SimpleConstantControl with Commands.
     */
	public SimpleConstantControl(T aModel, Map<String, ControlCommand> commandsByName, Renderer<? super T> aRenderer) {
        super(aModel, commandsByName);
        this.renderer = aRenderer;
    }

    /** 
     * Create a new SimpleConstantControl with aModel and some renderer.
     */
	public SimpleConstantControl(T aModel, Renderer<? super T> aRenderer) {
        super(aModel);
        this.renderer = aRenderer;
    }

    /** 
     * Create a new SimpleConstantControl with aModel and the {@link ResourceRenderer#INSTANCE}
     */
    public SimpleConstantControl(T aModel) {
        this(aModel, ResourceRenderer.INSTANCE);
    }

    @Override
    protected void internalWrite(DisplayContext context, TagWriter out)
            throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
        this.renderer.write(context, out, this.getModel());
		out.endTag(SPAN);
    }

}

