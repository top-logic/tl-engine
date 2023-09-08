/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.util.Utils;

/**
 * Display a {@link BinaryDataSource} as image.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayImageControl extends AbstractConstantControl {

	/**
	 * The default dimension when calling
	 * {@link DisplayImageControl#DisplayImageControl(BinaryDataSource)}.
	 */
	public static final Dimension DEFAULT_DIMENSION;

	static {
		int defaultHeight = 320;
		int defaultWidth = 480;
		Properties config = Configuration.getConfiguration(DisplayImageControl.class).getProperties();

		int height;
		try {
			height = ConfigUtil.getIntValue(config, "defaultDimension.height", defaultHeight);
		} catch (ConfigurationException ex) {
			Logger.error("Unable to get default dimension height", ex, DisplayImageControl.class);
			height = defaultHeight;
		}

		int width;
		try {
			width = ConfigUtil.getIntValue(config, "defaultDimension.width", defaultWidth);
		} catch (ConfigurationException ex) {
			Logger.error("Unable to get default dimension width", ex, DisplayImageControl.class);
			width = defaultWidth;
		}
		DEFAULT_DIMENSION = new Dimension(width, height);
	}

	private final DeliverContentHandler imageHandler;

	private Dimension dimension;

	BinaryDataSource _dataItem;

	private int _urlSuffix = 0;

	/**
	 * Creates a {@link DisplayImageControl}.
	 * 
	 * @param dataItem
	 *        The item to be displayed, must not be <code>null</code>.
	 */
	public DisplayImageControl(BinaryDataSource dataItem) {
		this(dataItem, DEFAULT_DIMENSION);
	}

	/**
	 * Creates a {@link DisplayImageControl}.
	 * 
	 * @param dataItem
	 *        The item to be displayed, must not be <code>null</code>.
	 * @param dimension
	 *        The image dimension, may be <code>null</code>.
	 */
	public DisplayImageControl(BinaryDataSource dataItem, Dimension dimension) {
		this.imageHandler = new DeliverContentHandler();
		setDataItem(dataItem, dimension);
	}

	/**
	 * Sets the {@link BinaryDataSource} to render.
	 * 
	 * @param dataItem
	 *        The new {@link BinaryDataSource} holding the image to render.
	 * @param dimension
	 *        The dimension for the image. It is not guaranteed that the image has this dimension.
	 *        The dimension may be adapted to have the same width / height relation as the original
	 *        image. May be <code>null</code>. In that case the image has original size.
	 */
	public void setDataItem(BinaryDataSource dataItem, Dimension dimension) {
		if (dataItem == null) {
			throw new IllegalArgumentException("No display image with null data item.");
		}
		if (_dataItem == dataItem) {
			return;
		}
		_dataItem = dataItem;
		imageHandler.setData(_dataItem);

		_urlSuffix++;
		setDimension(dataItem, dimension);
		requestRepaint();
	}

	private void setDimension(BinaryDataSource dataItem, Dimension dimension) {
		if (dimension == null) {
			this.dimension = null;
		} else {
			Dimension calculatedDimension;
			try {
				calculatedDimension = calculateDimension(dataItem, dimension);
			} catch (IOException ex) {
				Logger.warn("Failed to calculate image dimension", ex, DisplayImageControl.class);
				calculatedDimension = dimension;
			}
			this.dimension = calculatedDimension;
		}
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		FrameScope urlContext = getURLContext();
		urlContext.registerContentHandler(urlContext.createNewID(), imageHandler);
	}

	@Override
	protected void internalDetach() {
		getURLContext().deregisterContentHandler(imageHandler);
		super.internalDetach();
	}

	@Override
	protected String getTypeCssClass() {
		return "cImageDisplay";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(IMG);
			out.writeAttribute(ALT_ATTR, _dataItem.getName());
			out.writeAttribute(SRC_ATTR, getURLContext().getURL(context, imageHandler).appendParameter("itemVersion", _urlSuffix).getURL());

    		if (this.dimension != null) {
				// This does not work in the IE8 for big images (size > (15000 + x) pixel)
				// in that case, just don't write out these attributes as workaround.
				out.writeAttribute(WIDTH_ATTR, this.dimension.width);
				out.writeAttribute(HEIGHT_ATTR, this.dimension.height);
    		}
			out.endEmptyTag();
		}
		out.endTag(SPAN);
	}

	private FrameScope getURLContext() {
		return getScope().getFrameScope();
	}

	private static Dimension calculateDimension(BinaryDataSource dataItem, Dimension availableSpace)
			throws IOException {
		Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByMIMEType(dataItem.getContentType());
		if (imageReaders.hasNext()) {
			ImageReader firstReader = imageReaders.next();
			try {
				try (InputStream dataStream = dataItem.toData().getStream()) {
					firstReader.setInput(ImageIO.createImageInputStream(dataStream));

					return Utils.getImageDimension(availableSpace, firstReader.getAspectRatio(0));
				}
			} finally {
				firstReader.reset();
			}
		} else {
			return availableSpace;
		}
	}
}
