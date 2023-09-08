/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import java.text.ParseException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.form.GalleryPreview;
import com.top_logic.model.search.form.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link TilePreviewPartProvider.Content} using a configured image as content of the preview.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ImageContentProvider extends AbstractPreviewContent<ImageContentProvider.Config> {

	/**
	 * Typed configuration interface definition for {@link ImageContentProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractPreviewContent.Config<ImageContentProvider> {

		/** Configuration name for the value of {@link #getImage()}. */
		String IMAGE = "image";

		/**
		 * {@link Expr} computing the icon of the preview.
		 * 
		 * <p>
		 * The expression is expected to accept one input element, the model for the preview. It is
		 * expected that the {@link Expr} returns either a {@link ThemeImage} or the source for an
		 * image, e.g. "css:fas fa-bug".
		 * </p>
		 *
		 * @return The image to be rendered rendered.
		 */
		@Name(IMAGE)
		@Mandatory
		Expr getImage();

	}

	private QueryExecutor _image;

	/**
	 * Create a {@link ImageContentProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ImageContentProvider(InstantiationContext context, Config config) {
		super(context, config);
		_image = QueryExecutor.compile(config.getImage());
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		Object image = CollectionUtil.getSingleValueFrom(_image.execute(model));
		if (image == null) {
			return defaultPreview(Fragments.empty());
		}
		if (image instanceof GalleryImage) {
			return GalleryPreview.createGalleryContent((GalleryImage) image);
		}
		ThemeImage themeImage;
		if (image instanceof ThemeImage) {
			themeImage = (ThemeImage) image;
		} else if (image instanceof CharSequence) {
			try {
				themeImage = (ThemeImage) ThemeImageFormat.INSTANCE.parseObject(image.toString());
			} catch (ParseException ex) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_IMAGE_DEFINITION__SOURCE__POSITION
					.fill(image.toString(), ex.getErrorOffset()));
			}
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NO_IMAGE__OBJ.fill(image));
		}
		return imagePreview(themeImage);
	}

}

