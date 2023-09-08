/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class I18NMetaAttributeCellRenderer extends AbstractCellRenderer {
	private TLClass me;
	
	private final LabelProvider base;
	
	public I18NMetaAttributeCellRenderer(TLClass aME) {
		this.me = aME;
		this.base = DefaultResourceProvider.INSTANCE;
    }

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object theValue = cell.getValue();
        if (theValue != null) {
        	String theMAName = theValue.toString();
			ResKey theResult;
			if (MetaElementUtil.hasMetaAttribute(me, theMAName)) {
    			try {
					TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(this.me, theMAName);
    		        theResult = TLModelI18N.getI18NKey(theMA);
    	        }
    	        catch (NoSuchAttributeException e) {
					throw new UnreachableAssertion(e);
    	        }
    		}
        	else {
        		String baseLabel = this.base.getLabel(theValue);
				theResult = ResPrefix.legacyString(this.me.getName()).key(baseLabel);
        	}

			out.writeText(context.getResources().getString(theResult));
        }
	}
}
