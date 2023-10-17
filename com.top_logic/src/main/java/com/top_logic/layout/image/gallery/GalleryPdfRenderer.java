/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * {@link PDFRenderer} for {@link GalleryImage} lists.
 */
public class GalleryPdfRenderer implements PDFRenderer {

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		List<GalleryImage> images = (List<GalleryImage>) value;
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "tImageGallery");
		out.endBeginTag();
		for (var image : images) {
			BinaryDataSource imageData = image.getImage();

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "tGalleryImage");
			out.endBeginTag();
			{
				out.beginBeginTag(IMG);
				out.beginAttribute(SRC_ATTR);
				{
					writeDataValue(out, imageData);
				}
				out.endAttribute();
				out.endEmptyTag();
			}
			out.endTag(DIV);
		}
		out.endTag(DIV);
	}

	private static void writeDataValue(TagWriter out, BinaryDataSource imageData) throws IOException {
		out.write("data:");
		String contentType = imageData.getContentType();
		if (BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
			// Try to find better content type from file name
			contentType = MimeTypes.getInstance().getMimeType(imageData.getName());
		}
		out.write(contentType);
		out.write(";base64,");

		imageData.deliverTo(Base64.getEncoder().wrap(new ASCIIAppendable(out)));
	}

	private static final class ASCIIAppendable extends OutputStream {

		private Appendable _out;

		/**
		 * Creates a {@link ASCIIAppendable}.
		 */
		public ASCIIAppendable(Appendable out) {
			_out = out;
		}

		@Override
		public void write(int b) throws IOException {
			_out.append((char) (b & 0x7F));
		}

	}

}
