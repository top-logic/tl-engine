/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.Map;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.common.webfolder.ui.commands.VersionDialog;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DispatchingAccessor;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.SelfPropertyAccessor;

/**
 * {@link Accessor} for {@link VersionDialog} properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VersionedDocumentProperty {

	private VersionedDocumentProperty() {
		// No instantiation.
	}
	
	private static final Map<String, PropertyAccessor<? super DocumentVersion>> PROPERTIES =
		new MapBuilder<String, PropertyAccessor<? super DocumentVersion>>()
			.put(VersionDialog.VERSION_DOWNLOAD, SelfPropertyAccessor.INSTANCE)
			.put(VersionDialog.VERSION_AUTHOR, VersionAuthorProperty.INSTANCE)
			.put(VersionDialog.VERSION_SIZE, VersionSizeProperty.INSTANCE)
			.put(VersionDialog.VERSION_DATE, VersionDateProperty.INSTANCE)
			.put(VersionDialog.VERSION_DESCRIPTION, VersionDescriptionProperty.INSTANCE)
			.toMap();

	public static final Accessor<DocumentVersion> INSTANCE = new DispatchingAccessor<>(PROPERTIES);

}