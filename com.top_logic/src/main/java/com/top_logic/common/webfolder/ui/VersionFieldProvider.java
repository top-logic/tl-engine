/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.ui.commands.ContentDownload;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.table.model.FieldProvider;

/**
 * {@link FieldProvider} that creates version download fields.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VersionFieldProvider implements FieldProvider {
	
	/**
	 * Singleton {@link VersionFieldProvider} instance.
	 */
	public static final VersionFieldProvider INSTANCE = new VersionFieldProvider();

	private VersionFieldProvider() {
		// Singleton constructor.
	}
	
	@Override
	public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
		DocumentVersion version = (DocumentVersion) aModel;
		Command theExecutable = new ContentDownload(version.getDocument());
		CommandField theField = WebFolderFieldProvider.createField(aProperty, theExecutable, null, null);

		theField.setLabel(ResKey.text(Integer.toString(version.getRevision())));

	    return theField;
	}
}