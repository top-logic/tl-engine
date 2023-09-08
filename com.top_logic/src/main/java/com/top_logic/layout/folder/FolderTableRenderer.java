/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.layout.dnd.DnDFileUtilities;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Providing the correct resource prefix for the web folder table.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FolderTableRenderer extends DefaultTableRenderer {

	/** CSS class for the control element rendered by this control. */
	private static final String FOLDER_CSS_CLASS = "folderView";

	/**
	 * Creates a new {@link FolderTableRenderer}.
	 */
    public static FolderTableRenderer newInstance() {
		return (FolderTableRenderer) TypedConfigUtil.createInstance(newConfigForImplClass(FolderTableRenderer.class));
	}

	public FolderTableRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void write(DisplayContext context, RenderState state, TagWriter out)
			throws IOException {
		super.write(context, state, out);
		DnDFileUtilities.writeDropArea(out, state.getView().getID());
	}
	
	@Override
	public void appendControlCSSClasses(Appendable out, TableControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, FOLDER_CSS_CLASS);
	}

	@Override
	protected View getTitleView(TableControl control) {
		return ((FolderControl) control).getBreadcrumbControl();
	}

}

