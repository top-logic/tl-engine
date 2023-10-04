/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLObject;

/**
 * Utilities for working with {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStructuredTextUtil {

	/** Prefix identifying image references. */
	public static final String REF_ID_PREFIX = "ref:";

	/**
	 * Updates the translation of the attribute value.
	 * 
	 * @param locale
	 *        Is not allowed to be null.
	 * @param newSourceCode
	 *        Is allowed to be null. The new value is passed without check or modification to the
	 *        {@link StructuredText}.
	 */
	public static void updateTranslation(TLObject tlObject, String attribute, Locale locale, String newSourceCode) {
		I18NStructuredText oldText = (I18NStructuredText) tlObject.tValueByName(attribute);
		I18NStructuredText newText = updateSourceCode(oldText, locale, newSourceCode);
		tlObject.tUpdateByName(attribute, newText);
	}

	/**
	 * Creates an {@link I18NStructuredText} which contains for the given {@link Locale}
	 * {@link StructuredText} with the given source code. For all other {@link Locale}s, the
	 * {@link StructuredText} from the given original {@link I18NStructuredText} are copied.
	 * 
	 * @param original
	 *        The base {@link I18NStructuredText}. For all {@link Locale}'s except the specified
	 *        one, the {@link StructuredText} values for the result are taken from this
	 *        {@link I18NStructuredText}. May be <code>null</code> in which case the returned
	 *        {@link I18NStructuredText} contains only an entry for the given {@link Locale}.
	 * @param locale
	 *        The {@link Locale} for which the source code is updated. Must not be
	 *        <code>null</code>.
	 * @param newSourceCode
	 *        The new source code of the {@link StructuredText} for the given {@link Locale}. May be
	 *        <code>null</code>. The new value is passed without check or modification to the
	 *        {@link StructuredText}.
	 * 
	 * @return A new {@link I18NStructuredText} that shares only the source code and
	 *         {@link BinaryData} of the images, but not the {@link StructuredText}s, not the
	 *         {@link I18NStructuredText#getEntries() entries} {@link Map} and not the image
	 *         {@link Map}s.
	 */
	public static I18NStructuredText updateSourceCode(I18NStructuredText original, Locale locale, String newSourceCode) {
		requireNonNull(locale);
		Map<Locale, StructuredText> newContent;
		Map<String, BinaryData> images;
		if (original != null) {
			newContent = copyEntries(original);
			images = original.localizeImages(locale);
		} else {
			newContent = map();
			images = map();
		}
		newContent.put(locale, new StructuredText(newSourceCode, images));
		return new I18NStructuredText(newContent);
	}

	/**
	 * Creates a new {@link I18NStructuredText} with the original contents of the given
	 * {@link TLObject} but with new images.
	 * 
	 * <p>
	 * The images will be added to the {@link StructuredText} of the {@link Locale}.
	 * </p>
	 * 
	 * @param tlObject
	 *        {@link TLObject} to update.
	 * @param attribute
	 *        The attribute to update.
	 * @param locale
	 *        The {@link Locale} of the {@link StructuredText} to update. Never null.
	 * @param newImages
	 *        New images to add to the {@link StructuredText}.
	 */
	public static void updateImages(TLObject tlObject, String attribute, Locale locale,
			Map<String, BinaryData> newImages) {
		requireNonNull(locale);
		I18NStructuredText oldContent = (I18NStructuredText) tlObject.tValueByName(attribute);
		Map<Locale, StructuredText> newContent = copyEntries(oldContent);
		newContent.put(locale, new StructuredText(oldContent.localizeSourceCode(locale), newImages));
		tlObject.tUpdateByName(attribute, new I18NStructuredText(newContent));
	}

	/**
	 * Service method to update a {@link StructuredText} for the given locale in the value of the
	 * given {@link I18NStructuredText} attribute.
	 * 
	 * <p>
	 * New {@link StructuredText} is created from given source code and images.
	 * </p>
	 * 
	 * @param sourceCode
	 *        {@link StructuredText#getSourceCode() Source code} of the new value.
	 * @param newImages
	 *        {@link StructuredText#getImages() Images} of the new value.
	 */
	public static void updateStructuredText(TLObject tlObject, String attribute, Locale locale, String sourceCode,
			Map<String, BinaryData> newImages) {
		updateStructuredText(tlObject, attribute, locale, new StructuredText(sourceCode, newImages));
	}

	/**
	 * Updates the {@link StructuredText} for the given locale in the value of the given
	 * {@link I18NStructuredText} attribute.
	 * 
	 * @param tlObject
	 *        Holder of the {@link I18NStructuredText} to update.
	 * @param attribute
	 *        The {@link I18NStructuredText} attribute to update.
	 * @param locale
	 *        {@link Locale} to update value for.
	 * @param value
	 *        The new value for the given locale. May be <code>null</code> to remove value for
	 *        language.
	 * @return Former value of the given language. May be <code>null</code>.
	 */
	public static StructuredText updateStructuredText(TLObject tlObject, String attribute, Locale locale,
			StructuredText value) {
		requireNonNull(locale);
		I18NStructuredText oldContent = (I18NStructuredText) tlObject.tValueByName(attribute);
		Map<Locale, StructuredText> newContent;
		if (oldContent != null) {
			newContent = copyEntries(oldContent);
		} else {
			newContent = map();
		}
		StructuredText oldValue;
		if (value == null) {
			oldValue = newContent.remove(locale);
		} else {
			oldValue = newContent.put(locale, value);
		}
		tlObject.tUpdateByName(attribute, new I18NStructuredText(newContent));
		return oldValue;
	}

	/**
	 * Creates a new {@link Map} from the {@link I18NStructuredText#getEntries() entries} of the
	 * given {@link I18NStructuredText}.
	 */
	public static Map<Locale, StructuredText> copyEntries(I18NStructuredText original) {
		Map<Locale, StructuredText> newContent = map();
		for (Entry<Locale, StructuredText> entry : original.getEntries().entrySet()) {
			newContent.put(entry.getKey(), entry.getValue().copy());
		}
		return newContent;
	}

	/**
	 * Links image sources to resolve the paths.
	 * 
	 * @param html
	 *        HTML page.
	 * @param imageMap
	 *        Images of the HTML page.
	 */
	public static void linkImageSources(Document html, Map<String, BinaryData> imageMap) {
		Elements images = html.getElementsByTag(HTMLConstants.IMG);
		Set<String> linkedImageIDs = new HashSet<>();

		images.forEach(element -> {
			String imageID = linkImageSource(element, imageMap);
			if (imageID != null) {
				linkedImageIDs.add(imageID);
			}
		});
		removeUnusedImages(imageMap, linkedImageIDs);
	}

	/**
	 * Link img {@link Element} with source.
	 * 
	 * @param element
	 *        {@link Element} with img tag.
	 * @param imageMap
	 *        Images to link.
	 * @return Original value of src attribute.
	 */
	private static String linkImageSource(Element element, Map<String, BinaryData> imageMap) {
		String name = getSrcValue(element);
		if (imageMap.containsKey(name)) {
			setSrcValue(element, getImageRefID(name));
			return name;
		}

		return null;
	}

	/**
	 * Image ref ID to resolve the link.
	 * 
	 * @param imageID
	 *        Name of the image.
	 * @return Image name with ref ID prefix.
	 */
	public static String getImageRefID(String imageID) {
		return REF_ID_PREFIX + imageID;
	}

	/**
	 * Image ID of the given image reference.
	 * 
	 * @param imageRefID
	 *        Image reference formerly given by {@link #getImageRefID(String)}.
	 * @return Image name without ref ID prefix.
	 * @throws IllegalArgumentException
	 *         iff the given reference is not a reference.
	 * 
	 * @see #isImageReference(String)
	 */
	public static String getImageID(String imageRefID) {
		if (!isImageReference(imageRefID)) {
			throw new IllegalArgumentException();
		}
		return imageRefID.substring(REF_ID_PREFIX.length());
	}

	/**
	 * Whether the given image reference is a valid.
	 * 
	 * @param imageRefID
	 *        The reference to check.
	 */
	public static boolean isImageReference(String imageRefID) {
		return imageRefID.startsWith(REF_ID_PREFIX);
	}

	/**
	 * Value of the src attribute of a HTML {@link Element}
	 * 
	 * @param element
	 *        HTML {@link Element} containing the src attribute.
	 * @return Value of the src attribute.
	 */
	public static String getSrcValue(Element element) {
		return element.attr(HTMLConstants.SRC_ATTR);
	}

	/**
	 * Set value of a src attribute in an HTML {@link Element}.
	 * 
	 * @param element
	 *        HTML element whose src attribute will be set.
	 * @param attributeValue
	 *        The new attribute value.
	 */
	public static void setSrcValue(Element element, String attributeValue) {
		element.attr(HTMLConstants.SRC_ATTR, attributeValue);
	}

	/**
	 * Removes images of a {@link Map} of images that are not contained in the set of linked image
	 * IDs.
	 * 
	 * @param images
	 *        Map of images as {@link BinaryData}.
	 * @param linkedImageIDs
	 *        Set of linked images.
	 * 
	 */
	public static void removeUnusedImages(Map<String, BinaryData> images, Set<String> linkedImageIDs) {
		images.entrySet().removeIf((entry) -> !linkedImageIDs.contains(entry.getKey()));
	}
}
