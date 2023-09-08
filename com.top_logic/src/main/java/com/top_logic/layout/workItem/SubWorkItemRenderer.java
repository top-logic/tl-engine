/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.workItem;

import java.io.IOException;

import com.top_logic.base.workItem.SubWorkItem;
import com.top_logic.base.workItem.WorkItemAccessor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SubWorkItemRenderer extends ResourceRenderer<ResourceRenderer.Config<SubWorkItemRenderer>> {

	/**
	 * Creates a new {@link SubWorkItemRenderer}.
	 */
	public SubWorkItemRenderer(InstantiationContext context, Config<SubWorkItemRenderer> config) {
		super(context, config);
	}

	@Override
	protected void writeImage(DisplayContext aContext, TagWriter anOut, Object aValue, ThemeImage anImage)
			throws IOException {
        boolean useIndent = false;
        if (aValue instanceof SubWorkItem) {
            LayoutComponent theComponent = MainLayout.getComponent(aContext);
            if (theComponent instanceof TableComponent) {
                TableViewModel theViewModel          = ((TableComponent) theComponent).getViewModel();
                int            theSortedColumnNumber = theViewModel.getSortedApplicationModelColumn();
                if (theSortedColumnNumber < 0) {
                    useIndent = true;
                } else {
                    String theSortedColumnName = theViewModel.getColumnName(theViewModel.getSortedColumn());
                    useIndent = WorkItemAccessor.ATTRIBUTE_SELF.equals(theSortedColumnName);
                }
            }
        }
        if (useIndent) {
			super.writeImage(aContext, anOut, "", com.top_logic.layout.table.renderer.Icons.EMPTY);
        } 
        super.writeImage(aContext, anOut, aValue, anImage);
    }
}
