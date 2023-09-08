/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.provider.ProxyResourceProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Renderer for {@link DynamicImage}s that buffers the image contents in a
 * temporary file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BufferedDynamicImageRenderer implements Renderer<DynamicImage>, HTMLConstants {

	public static final String IMAGES_PATH_NAME = "/images/tmp";

	private static final String IMAGEMAP_IS_POSTFIX = "-imagemap";

	private ResourceProvider itemResourceProvider;

	/**
	 * Default instance using a {@link ProxyResourceProvider} for image map
	 * items.
	 */
	public static final BufferedDynamicImageRenderer INSTANCE = 
		new BufferedDynamicImageRenderer(MetaResourceProvider.INSTANCE);
	
	public BufferedDynamicImageRenderer(ResourceProvider itemResourceProvider) {
		this.itemResourceProvider = itemResourceProvider;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, DynamicImage value)
			throws IOException {
		DynamicImage image = value;
		
		writeImage(context, out, image);
	}

	private void writeImage(DisplayContext context, TagWriter out, DynamicImage image) throws IOException, FileNotFoundException {
		Map imageMap = image.getImageMap();
		
		String imageID = image.getID();
		String mapID = (imageMap != null) ? imageID + IMAGEMAP_IS_POSTFIX : null;
		String imageSrc = dumpImage(context, image);
		
		out.beginBeginTag(IMG);
		{
			out.writeAttribute(ID_ATTR, image.getID());
			out.writeAttribute(WIDTH_ATTR, image.getWidth());
			out.writeAttribute(HEIGHT_ATTR, image.getHeight());
			out.writeAttribute(USEMAP_ATTR, mapID);
			out.writeAttribute(ALT_ATTR, image.getLabel());
			out.writeAttribute(SRC_ATTR, imageSrc);
		}
		out.endEmptyTag();
		
		if (imageMap != null) {
			writeMap(context, out, imageMap, mapID);
		}
	}

	public void writeMap(DisplayContext context, TagWriter out, Map imageMap, String mapID) throws IOException {
		out.beginBeginTag(MAP);
		out.writeAttribute(ID_ATTR, mapID);
		out.writeAttribute(NAME_ATTR, mapID);
		out.endBeginTag();
		for (Iterator it = imageMap.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			Object item = entry.getKey();
			String coords = (String) entry.getValue();

			out.beginBeginTag(AREA);
			out.writeAttribute(SHAPE_ATTR, POLY_SHAPE_VALUE);
			out.writeAttribute(COORDS_ATTR, coords);
			out.writeAttribute(HREF_ATTR, "javascript:" + itemResourceProvider.getLink(context, item));
			out.writeAttribute(ALT_ATTR, itemResourceProvider.getLabel(item));
			out.endEmptyTag();
		}
		out.endTag(MAP);
	}

	public final HTMLFragment newImageMapFragment(Map imageMap, String mapID) {
		return new ImageMapFragment(imageMap, mapID);
	}

	public String dumpImage(DisplayContext context, DynamicImage image) throws IOException, FileNotFoundException {
		File imageFile = dumpImageToFile(context, image);
	
		return Settings.getInstance().getImageTempDirName() + "/" + imageFile.getName();
	}

	public File dumpImageToFile(DisplayContext context, DynamicImage image) throws IOException, FileNotFoundException {
		File imagePath = Settings.getInstance().getImageTempDir();
		File imageFile = File.createTempFile("dynamic-image", ".png", imagePath);

		FileOutputStream theStream = new FileOutputStream(imageFile);
		try {
			image.encode(context, theStream, "PNG");
		} finally {
			theStream.close();
		}
		return imageFile;
	}

	public BinaryData dumpImageToBinaryData(DisplayContext context, DynamicImage image) throws IOException {
		ByteArrayOutputStream theStream = new ByteArrayOutputStream();
		try {
			image.encode(context, theStream, "PNG");
		} finally {
			theStream.close();
		}
		return BinaryDataFactory.createBinaryData(theStream.toByteArray(), ImageComponent.MIME_PNG);
	}

	private class ImageMapFragment implements HTMLFragment {

		private Map _imageMap;

		private String _mapId;

		public ImageMapFragment(Map imageMap, String mapId) {
			_imageMap = imageMap;
			_mapId = mapId;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			writeMap(context, out, _imageMap, _mapId);
		}

	}

}
