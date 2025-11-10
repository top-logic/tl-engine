/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
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

	private static final String DATA = "data:";
	private static final String BASE64_MARKER = ";base64,";

	/**
	 * Returns the source code of the given {@link StructuredText} where its images are Base64
	 * encoded.
	 */
	public static String getCodeWithInlinedImages(StructuredText text) throws IOException {
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
				srcBuilder.write(DATA.getBytes(charset));
				String contentType = binaryData.getContentType();
				if (BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
					// Try to find better content type from file name
					contentType = MimeTypes.getInstance().getMimeType(binaryData.getName());
				}
				srcBuilder.write(contentType.getBytes(charset));
				srcBuilder.write(BASE64_MARKER.getBytes(charset));
				try (InputStream in = binaryData.getStream()) {
					StreamUtilities.copyStreamContents(in, Base64.getEncoder().wrap(srcBuilder));
				}
				newSrc = srcBuilder.toString(charset);
			}
			I18NStructuredTextUtil.setSrcValue(localImg, newSrc);
		}

		return document.html();
	}

	/**
	 * Creates a {@link StructuredText} from the given source, where Base64 encoded images are
	 * extracted and replaced by {@link I18NStructuredTextUtil#REF_ID_PREFIX} references.
	 */
	public static StructuredText fromCodeWithInlinedImages(String source) {
		org.jsoup.nodes.Document document = Jsoup.parse(source, "", Parser.xmlParser());

		Elements localImaged = document.select("img[src^=\"" + DATA + "\"]");
		Map<String, BinaryData> images = Collections.emptyMap();
		for (Element localImg : localImaged) {
			String imgSrc = I18NStructuredTextUtil.getSrcValue(localImg);
			if (imgSrc.startsWith(DATA)) {
				int indexOfBase64 = imgSrc.indexOf(BASE64_MARKER);
				if (indexOfBase64 < 0) {
					continue;
				}
				String imgData = imgSrc .substring(indexOfBase64 + BASE64_MARKER.length());
				String contentType = imgSrc.substring(DATA.length(), indexOfBase64);
				if (contentType.isEmpty()) {
					contentType = BinaryData.CONTENT_TYPE_OCTET_STREAM;
				}
				String imgName = "img" + images.size();
				images = ensureModifyable(images);
				images.put(imgName, BinaryDataFactory.decodeBase64(imgData, contentType, imgName));

				I18NStructuredTextUtil.setSrcValue(localImg, I18NStructuredTextUtil.REF_ID_PREFIX + imgName);
			}
		}

		return new StructuredText(document.html(), images);
	}

	private static Map<String, BinaryData> ensureModifyable(Map<String, BinaryData> images) {
		if (images.isEmpty()) {
			images = new HashMap<>();
		}
		return images;
	}

}
