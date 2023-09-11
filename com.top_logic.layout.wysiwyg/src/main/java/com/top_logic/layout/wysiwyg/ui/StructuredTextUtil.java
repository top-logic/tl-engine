/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Utilities for working with {@link StructuredText}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class StructuredTextUtil {

	/**
	 * Returns the source code of the given {@link StructuredText} where its images are Base64
	 * encoded.
	 */
	public static String getCodeWithInlinedImages(StructuredText text) throws IOError {
		String sourceCode = text.getSourceCode();
		Map<String, BinaryData> images = text.getImages();
		org.jsoup.nodes.Document document = Jsoup.parse(sourceCode, "", Parser.xmlParser());

		Elements localImaged = document.select(StructuredTextControl.IMAGE_CSS_SELECTOR);
		for (Element localImg : localImaged) {
			String image = I18NStructuredTextUtil.getImageID(I18NStructuredTextUtil.getSrcValue(localImg));
			BinaryData binaryData = images.get(image);
			if (binaryData == null) {
				InfoService.showError(I18NConstants.IMAGE_NOT_FOUND__IMAGE_NAME.fill(image));
				String altText = localImg.attr(HTMLConstants.ALT_ATTR);
				if (StringServices.isEmpty(altText)) {
					localImg.attr(HTMLConstants.ALT_ATTR, image);
				}
				continue;
			}

			String newSrc;
			try (ByteArrayOutputStream srcBuilder = new ByteArrayOutputStream()) {
				Charset charset = StringServices.CHARSET_UTF_8;
				srcBuilder.write("data:".getBytes(charset));
				String contentType = binaryData.getContentType();
				if (BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
					// Try to find better content type from file name
					contentType = MimeTypes.getInstance().getMimeType(binaryData.getName());
				}
				srcBuilder.write(contentType.getBytes(charset));
				srcBuilder.write(";base64,".getBytes(charset));
				try (InputStream in = binaryData.getStream()) {
					StreamUtilities.copyStreamContents(in, Base64.getEncoder().wrap(srcBuilder));
				}
				newSrc = srcBuilder.toString(charset.name());
			} catch (IOException exception) {
				throw new IOError(exception);
			}
			I18NStructuredTextUtil.setSrcValue(localImg, newSrc);
		}

		return document.html();
	}

}
