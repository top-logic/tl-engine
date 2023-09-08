/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * If the value to be written is null, this renderer will write a defined string instead.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class NotEmptyRenderer<T> implements Renderer<T> {

	private final Renderer<? super T> innerRenderer;
    private final String emptyString;

    /** 
     * Creates a {@link NotEmptyRenderer}.
     */
	public NotEmptyRenderer(String anEmptyString, Renderer<? super T> anInnerRenderer) {
        this.emptyString = anEmptyString;
        this.innerRenderer = anInnerRenderer;
        
    }

    /**
     * @see com.top_logic.layout.Renderer#write(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, java.lang.Object)
     */
    @Override
	public void write(DisplayContext aContext, TagWriter aOut, T aValue) throws IOException {
        if (aValue == null) {
            aOut.writeContent(this.emptyString);
        }
        else {
            this.innerRenderer.write(aContext, aOut, aValue);
        }
    }

	@Override
	public <X> Renderer<? super X> generic(Class<X> expectedType) {
		Renderer<? super X> genericInner = innerRenderer.generic(expectedType);
		if (genericInner == innerRenderer) {
			@SuppressWarnings("unchecked")
			Renderer<? super X> direct = (Renderer<? super X>) this;
			return direct;
		} else {
			return new NotEmptyRenderer<>(emptyString, genericInner);
		}
	}

}

