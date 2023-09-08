/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.util.List;

import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * A simple data holder used to replace slides with a {@link SlideReplacer}
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class SlideReplacement {
	private final String templateFilename;

	private final List<OfficeExportValueHolder> holders;

	/**
	 * Create a new SlideReplacement ...
	 * 
	 * @param aTemplateFilename
	 *        the template file name
	 * @param someHolders
	 *        the value holders
	 */
	public SlideReplacement(String aTemplateFilename, List<OfficeExportValueHolder> someHolders) {
		this.templateFilename = aTemplateFilename;
		this.holders = someHolders;
	}

	/**
	 * the value holders
	 */
	public final List<OfficeExportValueHolder> getOfficeExportValueHolders() {
		return this.holders;
	}

	/**
	 * the template file name
	 */
	public final String getTemplateFilename() {
		return this.templateFilename;
	}

	public final String getTemplateFilename(OfficeExportValueHolder aHolder) {
		if (aHolder != null && aHolder.templateFileName != null) {
			return aHolder.templateFileName;
		}

		return this.getTemplateFilename();
	}
}
