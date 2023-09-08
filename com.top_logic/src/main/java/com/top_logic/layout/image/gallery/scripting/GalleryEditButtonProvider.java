/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery.scripting;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.image.gallery.GalleryControl;
import com.top_logic.layout.image.gallery.GalleryModel;

/**
 * {@link Mapping}, that retrieves the CommandModel, which opens gallery's edit dialog, from the
 * controlling {@link GalleryModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class GalleryEditButtonProvider implements Mapping<Object, CommandModel> {

	/** Static instance of {@link GalleryEditButtonProvider} */
	public static final GalleryEditButtonProvider INSTANCE = new GalleryEditButtonProvider();

	private GalleryEditButtonProvider() {
		// Singleton
	}

	@Override
	public CommandModel map(Object input) {
		return ((GalleryModel) input).get(GalleryControl.OPEN_EDIT_DIALOG_PROPERTY);
	}

}