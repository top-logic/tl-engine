/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DataItemControl;
import com.top_logic.layout.form.model.DataField;

/**
 * The class {@link DataItemTag} creates a {@link Control} for a
 * {@link DataField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DataItemTag extends AbstractFormFieldControlTag {

	private boolean downloadMode;

	private Boolean forbidEmptyFileUpload;

	public DataItemTag() {
		setDefaultValues();
	}

	private void setDefaultValues() {
		downloadMode = false;
		forbidEmptyFileUpload = null;
	}

	public void setDownloadInline(boolean downloadMode) {
		this.downloadMode = downloadMode;
	}

	public void setForbidEmptyFileUpload(boolean b) {
		this.forbidEmptyFileUpload = b;
	}

	@Override
	protected void teardown() {
		setDefaultValues();
		super.teardown();
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {

		DataField field = (DataField) member;

		DataItemControl control = new DataItemControl(field);
		control.downloadInline(downloadMode);

		if (forbidEmptyFileUpload != null) {
			control.forbidEmptyFileUpload(forbidEmptyFileUpload.booleanValue());
		}

		return control;
	}

}
