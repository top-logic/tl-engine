/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.awt.Dimension;
import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.control.DisplayImageControl;
import com.top_logic.layout.provider.label.FileSizeLabelProvider;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link AbstractCellRenderer}, that renders {@link GalleryImage}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ImageCellRenderer extends AbstractCellRenderer {

	/** Singleton instance of {@link ImageCellRenderer} */
	public static final ImageCellRenderer INSTANCE = new ImageCellRenderer();

	private static final Dimension THUMBNAIL_DIMENSION = new Dimension(64, 64);
	private static final String GALLERY_IMAGE_LABEL_CLASS = "galleryImageLabel";

	private ImageCellRenderer() {
		// Singleton pattern
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		writeImage(context, out, cell);
		writeImageName(out, cell);
		writeImageSize(out, cell);
	}

	private void writeImage(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		GalleryImage galleryImage = (GalleryImage) cell.getValue();
		DisplayImageControl imageControl =
			new DisplayImageControl(getImageData(galleryImage), THUMBNAIL_DIMENSION);
		imageControl.write(context, out);
	}

	private void writeImageName(TagWriter out, Cell cell) throws IOException {
		GalleryImage galleryImage = (GalleryImage) cell.getValue();
		out.beginBeginTag(HTMLConstants.SPAN);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, GALLERY_IMAGE_LABEL_CLASS);
		out.endBeginTag();
		out.write(galleryImage.getName());
		out.endTag(HTMLConstants.SPAN);
	}

	private void writeImageSize(TagWriter out, Cell cell) throws IOException {
		GalleryImage galleryImage = (GalleryImage) cell.getValue();
		out.beginBeginTag(HTMLConstants.SPAN);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, GALLERY_IMAGE_LABEL_CLASS);
		out.endBeginTag();
		out.writeText("(");
		out.write(FileSizeLabelProvider.INSTANCE.getLabel(galleryImage.getImage().getSize()));
		out.writeText(")");
		out.endTag(HTMLConstants.SPAN);
	}

	private BinaryDataSource getImageData(GalleryImage galleryImage) {
		BinaryDataSource thumbnail = galleryImage.getThumbnail();
		if (thumbnail == null) {
			thumbnail = galleryImage.getImage();
		}
		return ImageDataUtil.castMimeType(galleryImage.getName(), thumbnail);
	}
}