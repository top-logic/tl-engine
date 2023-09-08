/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.component;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Map;

import com.top_logic.base.chart.DefaultImageData;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.chart.component.AbstractImageComponent;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.image.BufferedDynamicImageRenderer;
import com.top_logic.layout.image.DynamicImage;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ModelBuilder;

/**
 * Component to display a single {@link DynamicImage} created by a
 * {@link ModelBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicImageComponent extends AbstractImageComponent implements HTMLConstants {

	/**
	 * Configuration of a {@link DynamicImageComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractImageComponent.Config {

		/** Configuration name for {@link #getImageModelBuilder}. */
		String IMAGE_MODEL_BUILDER_NAME = "image-model-builder";

		/**
		 * Configuration for the {@link ModelBuilder} that creates the {@link DynamicImage} from the
		 * components model.
		 */
		@Name(IMAGE_MODEL_BUILDER_NAME)
		@Mandatory
		PolymorphicConfiguration<? extends ModelBuilder> getImageModelBuilder();
	}

	BufferedDynamicImageRenderer renderer = BufferedDynamicImageRenderer.INSTANCE;
	
	private final ModelBuilder _imageModelBuilder;
	
	/**
	 * Creates a new {@link DynamicImageComponent}.
	 */
	public DynamicImageComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_imageModelBuilder = context.getInstance(config.getImageModelBuilder());
	}

	/**
	 * The dynamic image with the given ID.
	 * 
	 * @param imageID
	 *        The identifier of the image to render.
	 */
	protected DynamicImage getImageModel(String imageID) {
		return (DynamicImage) getImageModelBuilder().getModel(getModel(), this);
	}

	@Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		DynamicImage image = getImageModel(imageId);
		image.prepare(context, (int) dimension.getWidth(), (int) dimension.getHeight());
	}

	@Override
	public ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException {
		DynamicImage image = getImageModel(imageId);

		BinaryData bytes;
		if (!isWriteToTempFile()) {
			bytes = renderer.dumpImageToBinaryData(context, image);
		} else {
			bytes = BinaryDataFactory.createBinaryData(renderer.dumpImageToFile(context, image));
		}

		return new DefaultImageData(new Dimension(image.getWidth(), image.getHeight()), bytes);
	}

	@Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		DynamicImage image = getImageModel(imageId);

		Map imageMap = image.getImageMap();
		
		return renderer.newImageMapFragment(imageMap, mapName);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		if (!super.supportsInternalModel(anObject)) {
			return false;
		}
		return getImageModelBuilder().supportsModel(anObject, this);
	}

	@Override
	public FormContext createFormContext() {
		return null;
	}

	/**
	 * Returns the configured image model builder.
	 * 
	 * @see Config#getImageModelBuilder()
	 */
	public final ModelBuilder getImageModelBuilder() {
		return _imageModelBuilder;
	}
}
