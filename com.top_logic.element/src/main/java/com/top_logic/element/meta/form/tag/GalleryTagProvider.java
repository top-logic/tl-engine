/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.image.gallery.GalleryTag;
import com.top_logic.model.annotate.ui.TLDimensions;

/**
 * {@link DisplayProvider} for gallery attributes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GalleryTagProvider extends IndirectDisplayProvider {

	/**
	 * Singleton {@link GalleryTagProvider} instance.
	 */
	public static final GalleryTagProvider INSTANCE = new GalleryTagProvider();

	private GalleryTagProvider() {
		// Singleton constructor.
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		GalleryTag galleryTag = new GalleryTag();
		TLDimensions annotation = editContext.getAnnotation(TLDimensions.class);
		if (annotation != null) {
			galleryTag.initWidth(annotation.getWidth());
			galleryTag.initHeight(annotation.getHeight());
		}
		return galleryTag;
	}

}
