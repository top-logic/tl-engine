/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * The CellObjectResourceProvider extracts the value from the CellObject and uses the
 * MetaResourceProvider to get the resources.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class CellObjectResourceProvider extends AbstractResourceProvider {

    @Override
	public ThemeImage getImage(Object aObject, Flavor aFlavor) {
		if (aObject instanceof CellObject) {
			CellObject cellObject = (CellObject) aObject;
			if (cellObject.isImageDisabled()) return null;
			return cellObject.getImage();
		} else {
			return null;
		}
    }

    @Override
	public String getTooltip(Object aObject) {
		if (aObject instanceof CellObject) {
			CellObject cellObject = (CellObject) aObject;
			if (cellObject.isTooltipDisabled()) return null;
			String theTooltip = cellObject.getTooltip();
			return theTooltip == null ? null : theTooltip;
		} else {
			return null;
		}
    }

    @Override
	public String getType(Object aObject) {
        return "CellObject";
    }

    @Override
	public String getLabel(Object aObject) {
		if (aObject instanceof CellObject) {
			return MetaResourceProvider.INSTANCE.getLabel(((CellObject) aObject).getValue());
		} else {
			return null;
		}
    }

	@Override
	public String getCssClass(Object anObject) {
		if (anObject instanceof CellObject) {
			return ((CellObject) anObject).getCssClass();
		} else {
			return null;
		}
	}
}
