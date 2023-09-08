/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CollectionRenderer<T> implements Renderer<Collection<? extends T>> {

	private Renderer<? super T> inner;
    private String separator;

	public CollectionRenderer(Renderer<? super T> anInner, String aSeparator) {
        this.inner     = anInner;
        this.separator = aSeparator;
    }

    @Override
	public void write(DisplayContext context, TagWriter out, Collection<? extends T> value) throws IOException {
		Collection<? extends T> theColl = value;
        boolean    writeSeparator = false;

        if (theColl != null) {
			for (Iterator<? extends T> theIt = theColl.iterator(); theIt.hasNext();) {
				T theValue = theIt.next();
	
	            if (writeSeparator) {
	                out.writeText(this.separator);
	            }
	            else {
	                writeSeparator = true;
	            }
	
	            this.inner.write(context, out, theValue);
	        }
        }
    }
}
