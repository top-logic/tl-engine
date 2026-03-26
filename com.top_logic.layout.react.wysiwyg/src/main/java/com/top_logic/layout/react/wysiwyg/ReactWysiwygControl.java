/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * React WYSIWYG editor control for {@code tl.model.wysiwyg:Html} attributes.
 *
 * <p>
 * Renders the {@code TLWysiwygEditor} React component backed by TipTap. Converts between
 * {@link StructuredText} (server model) and HTML strings (client). Serves embedded images via
 * {@link DataProvider} and accepts image uploads via {@link UploadHandler}.
 * </p>
 *
 * <p>
 * Image URLs are rewritten when sending HTML to the client: bare filenames in
 * {@code <img src="file.png">} become full download URLs
 * {@code <img src="/react-api/data?controlId=...&key=file.png">}. The reverse transformation is
 * applied when receiving HTML back from the client.
 * </p>
 */
public class ReactWysiwygControl extends ReactFormFieldControl implements UploadHandler, DataProvider {

	private static final String TOOLBAR = "toolbar";

	private static final String IMAGE_URL = "imageUrl";

	private static final String KEY_PARAM = "&key=";

	private static final List<String> DEFAULT_TOOLBAR = Arrays.asList(
		"bold", "italic", "underline", "strike",
		"|",
		"heading",
		"|",
		"bulletList", "orderedList", "blockquote",
		"|",
		"link", "image", "table", "codeBlock",
		"|",
		"color",
		"|",
		"undo", "redo"
	);

	private final String _imageUrlPrefix;

	private StructuredText _shadowCopy;

	/**
	 * Creates a new {@link ReactWysiwygControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the {@link StructuredText} value.
	 */
	public ReactWysiwygControl(ReactContext context, FieldModel model) {
		super(context, model, "TLWysiwygEditor");

		_imageUrlPrefix = context.getContextPath() + "/react-api/data?controlId=" + getID()
			+ "&windowName=" + context.getWindowName() + KEY_PARAM;

		initShadowCopy();
		putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));
		putState(TOOLBAR, DEFAULT_TOOLBAR);
	}

	private void initShadowCopy() {
		StructuredText value = (StructuredText) getFieldModel().getValue();
		if (value != null) {
			_shadowCopy = value.copy();
		} else {
			_shadowCopy = new StructuredText();
		}
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		if (newValue instanceof StructuredText) {
			_shadowCopy = ((StructuredText) newValue).copy();
			putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));
		} else {
			_shadowCopy = new StructuredText();
			putState(VALUE, "");
		}
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		String html = rawValue != null ? rawValue.toString() : "";
		String cleanHtml = stripImageUrls(html);
		_shadowCopy.setSourceCode(cleanHtml);
		return _shadowCopy.copy();
	}

	@Override
	public BinaryData getDownloadData(String key) {
		if (_shadowCopy == null || key == null) {
			return null;
		}
		return _shadowCopy.getImages().get(key);
	}

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		for (Part part : parts) {
			String submittedFileName = part.getSubmittedFileName();
			if (submittedFileName == null || submittedFileName.isEmpty()) {
				// Skip non-file parts (controlId, windowName).
				continue;
			}
			try {
				String fileName = uniqueImageKey(submittedFileName);
				BinaryData imageData = BinaryDataFactory.createUploadData(part);
				_shadowCopy.addImage(fileName, imageData);
				putState(IMAGE_URL, fileName);
			} catch (IOException ex) {
				throw new TopLogicException(I18NConstants.ERROR_IMAGE_UPLOAD_FAILED, ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Rewrites bare image filenames in {@code <img src>} to full download URLs for the client.
	 */
	private String rewriteImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		doc.outputSettings().indentAmount(0).outline(true);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			if (!src.contains("react-api/data") && _shadowCopy.getImages().containsKey(src)) {
				img.attr("src", _imageUrlPrefix + URLEncoder.encode(src, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	/**
	 * Strips download URLs from {@code <img src>} back to bare image keys for storage.
	 */
	private String stripImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		doc.outputSettings().indentAmount(0).outline(true);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			int keyIndex = src.indexOf(KEY_PARAM);
			if (keyIndex >= 0) {
				String key = src.substring(keyIndex + KEY_PARAM.length());
				int ampIndex = key.indexOf('&');
				if (ampIndex >= 0) {
					key = key.substring(0, ampIndex);
				}
				img.attr("src", URLDecoder.decode(key, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	private String extractHtml(StructuredText text) {
		if (text == null) {
			return "";
		}
		return text.getSourceCode();
	}

	private String uniqueImageKey(String name) {
		if (name == null) {
			name = "image";
		}
		Map<String, BinaryData> images = _shadowCopy.getImages();
		if (!images.containsKey(name)) {
			return name;
		}
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String ext = dot > 0 ? name.substring(dot) : "";
		int counter = 1;
		String candidate;
		do {
			candidate = base + "_" + counter + ext;
			counter++;
		} while (images.containsKey(candidate));
		return candidate;
	}

}
