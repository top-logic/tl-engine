/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions for the structured text of HTML attributes.
 *
 * <p>
 * A value of such an attribute is a {@link StructuredText}: its HTML source plus the images the
 * source refers to. These functions take that source apart and put it together again, so that a
 * script can compose the content of an HTML attribute.
 * </p>
 */
@ScriptPrefix("html")
public class HtmlFunctions extends TLScriptFunctions {

	/**
	 * The HTML source of the given structured text.
	 *
	 * @param content
	 *        The value of an HTML attribute.
	 * @return The HTML source, empty for content that has none.
	 */
	@Label("HTML source of structured text")
	@SideEffectFree
	public static String source(StructuredText content) {
		return content == null ? StringServices.EMPTY_STRING : content.getSourceCode();
	}

	/**
	 * The images of the given structured text.
	 *
	 * <p>
	 * Together with {@link #source(StructuredText)}, this gives the parts that
	 * {@link #text(String, Object)} puts together again, e.g. to store a changed source with the
	 * images of the original text.
	 * </p>
	 *
	 * @param content
	 *        The value of an HTML attribute.
	 * @return A dictionary from the names the source uses for the images to the image data, empty
	 *         for content without images.
	 */
	@Label("Images of structured text")
	@SideEffectFree
	public static Map<String, BinaryData> images(StructuredText content) {
		return content == null ? new LinkedHashMap<>() : new LinkedHashMap<>(content.getImages());
	}

	/**
	 * Structured text with the given HTML source and the images it shows, to store in an HTML
	 * attribute.
	 *
	 * <p>
	 * An image element of the source shows one of the given images by naming it in its
	 * <code>src</code> attribute, e.g. <code>&lt;img src="logo.png"/&gt;</code>. Such a reference
	 * is stored as a link to the image of the structured text (the name with the prefix
	 * {@link I18NStructuredTextUtil#REF_ID_PREFIX}). A source that already uses such a link, e.g.
	 * the {@link #source(StructuredText) source} of a stored structured text, refers to the given
	 * image of that name as well. An image the source does not name is not stored, an image
	 * element naming no given image stays as it is.
	 * </p>
	 *
	 * @param source
	 *        The HTML source. Without images, it is taken as it is.
	 * @param images
	 *        The images the source shows: a single binary value or a list of binary values, each
	 *        named by its file name, or a dictionary from the names used in the source to binary
	 *        values. Empty for text without images.
	 * @return The value to write to an HTML attribute.
	 */
	@Label("Structured text with HTML source")
	@SideEffectFree
	public static StructuredText text(String source, Object images) {
		String html = StringServices.nonNull(source);
		Map<String, BinaryData> imageMap = toImageMap(images);
		if (imageMap.isEmpty()) {
			return new StructuredText(html);
		}

		Document document = Jsoup.parseBodyFragment(html);
		document.outputSettings().prettyPrint(false);
		I18NStructuredTextUtil.linkImageSources(document, imageMap);
		return new StructuredText(document.body().html(), imageMap);
	}

	/**
	 * The given images as a modifiable map from the names the source uses to the image data.
	 */
	private static Map<String, BinaryData> toImageMap(Object images) {
		Map<String, BinaryData> result = new LinkedHashMap<>();
		if (images == null) {
			// No images.
		} else if (images instanceof Map<?, ?> map) {
			for (Entry<?, ?> entry : map.entrySet()) {
				result.put(String.valueOf(entry.getKey()), toImage(entry.getValue(), images));
			}
		} else if (images instanceof Collection<?> collection) {
			for (Object element : collection) {
				BinaryData image = toImage(element, images);
				result.put(image.getName(), image);
			}
		} else {
			BinaryData image = toImage(images, images);
			result.put(image.getName(), image);
		}
		return result;
	}

	private static BinaryData toImage(Object value, Object images) {
		if (value instanceof BinaryData image) {
			return image;
		}
		throw new TopLogicException(I18NConstants.ERROR_INVALID_HTML_IMAGES__VALUE.fill(images));
	}

	/**
	 * A link leading to the given object, as HTML source to embed into structured text.
	 *
	 * <p>
	 * Displayed structured text offers such a link as a link to the object: following it leads to
	 * the place the application shows objects of that kind.
	 * </p>
	 *
	 * @param object
	 *        The object the link leads to.
	 * @param label
	 *        The text the link is displayed with. Empty uses the object's own label.
	 * @return The link as an HTML anchor element.
	 */
	@Label("Link to an object")
	@SideEffectFree
	public static String objectLink(@Mandatory TLObject object, String label) {
		return TLObjectLinkUtil.getLink(object, label, null);
	}

}
