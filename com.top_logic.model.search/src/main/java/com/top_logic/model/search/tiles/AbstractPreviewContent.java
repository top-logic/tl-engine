/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;

/**
 * Superclass for {@link TilePreviewPartProvider.Content}, that create the frame of the preview
 * content.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractPreviewContent<C extends AbstractPreviewContent.Config<?>>
		extends AbstractConfiguredInstance<C>
		implements TilePreviewPartProvider.Content {

	/**
	 * Typed configuration interface definition for {@link AbstractPreviewContent}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends AbstractPreviewContent<?>>
			extends PolymorphicConfiguration<AbstractPreviewContent<?>> {

		// configuration interface definition

	}

	/**
	 * Create a {@link AbstractPreviewContent}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractPreviewContent(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Service method to create a default preview for the given content.
	 * 
	 * <p>
	 * Implementor of {@link #createPreviewPart(Object)} should use this method to create the actual
	 * preview part.
	 * </p>
	 * 
	 * @param content
	 *        The actual given content.
	 * @param previewClass
	 *        Additional CSS class, needed by the content. May be <code>null</code>.
	 */
	protected static HTMLFragment defaultPreview(HTMLFragment content, String previewClass) {
		return LabelBasedPreview.defaultPreviewContent(content, previewClass);
	}

	/**
	 * Default preview without additional CSS class.
	 * 
	 * @see #defaultPreview(HTMLFragment, String)
	 */
	protected static HTMLFragment defaultPreview(HTMLFragment preview) {
		return defaultPreview(preview, null);
	}

	/**
	 * Service method to create a default preview for the given image.
	 * 
	 * <p>
	 * Implementor of {@link #createPreviewPart(Object)} should use this method to create the actual
	 * preview part when displaying a simple {@link ThemeImage}.
	 * </p>
	 * 
	 * @param image
	 *        The image to display. May be <code>null</code>.
	 */
	protected static HTMLFragment imagePreview(ThemeImage image) {
		HTMLFragment content;
		if (image != null) {
			content = span("card-img-top", image);
		} else {
			content = empty();
		}
		return defaultPreview(content);

	}

}

