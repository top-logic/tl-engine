/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart;

import java.awt.Dimension;
import java.io.IOException;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.chart.ImageControl;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.PageComponent;

/**
 * Demo chart creator for test scenarios of an {@link ImageControl}.
 * 
 * @author <a href=mailto:sts@top-logic.com>Stefan Steinert</a>
 */
public class DemoChartComponent extends PageComponent implements ImageComponent {

	/**
	 * Minimum width to which the displayed image can be adjusted to.
	 */
	public static final String MIN_WIDTH_PROPERTY = "min-width";

	/**
	 * Minimum height to which the displayed image can be adjusted to.
	 */
	public static final String MIN_HEIGHT_PROPERTY = "min-height";

	/**
	 * Maximum width to which the displayed image can be adjusted to.
	 */
	public static final String MAX_WIDTH_PROPERTY = "max-width";

	/**
	 * Maximum height to which the displayed image can be adjusted to.
	 */
	public static final String MAX_HEIGHT_PROPERTY = "max-height";

	/**
	 * Configuration interface of {@link DemoChartComponent}
	 */
	public interface Config extends PageComponent.Config {

		/**
		 * @see DemoChartComponent#MIN_WIDTH_PROPERTY
		 */
		@Name(MIN_WIDTH_PROPERTY)
		int getMinWidth();

		/**
		 * @see DemoChartComponent#MIN_HEIGHT_PROPERTY
		 */
		@Name(MIN_HEIGHT_PROPERTY)
		int getMinHeight();

		/**
		 * @see DemoChartComponent#MAX_WIDTH_PROPERTY
		 */
		@Name(MAX_WIDTH_PROPERTY)
		@IntDefault(Integer.MAX_VALUE)
		int getMaxWidth();

		/**
		 * @see DemoChartComponent#MAX_HEIGHT_PROPERTY
		 */
		@Name(MAX_HEIGHT_PROPERTY)
		@IntDefault(Integer.MAX_VALUE)
		int getMaxHeight();
	}

	private ImageComponent chartProducer;

	/**
	 * Create a new {@link DemoChartComponent}
	 */
	public DemoChartComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		chartProducer =
			new DemoChartProducer(atts.getMinWidth(), atts.getMaxWidth(), atts.getMinHeight(), atts.getMaxHeight());
	}

	@Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		chartProducer.prepareImage(context, imageId, dimension);
	}

	@Override
	public ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException {
		return chartProducer.createImage(context, imageId, imageType, dimension);
	}

	@Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		return chartProducer.getImageMap(imageId, mapName, dimension);
	}
}
