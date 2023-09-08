/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Collections.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.searching.FullTextSearchable;

/**
 * Structure containing used images and the source code for a HTML attribute.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredText implements FullTextSearchable {

	private String _sourceCode;

	private Map<String, BinaryData> _images;

	/**
	 * Creates an instance of {@link StructuredText}.
	 */
	public StructuredText(String sourceCode, Map<String, BinaryData> images) {
		setSourceCode(sourceCode);
		setImages(images);
	}

	/**
	 * Creates an instance of {@link StructuredText} containing no images.
	 */
	public StructuredText(String sourceCode) {
		this(sourceCode, new HashMap<>());
	}

	/**
	 * Creates an instance of {@link StructuredText} with no content.
	 */
	public StructuredText() {
		this(StringServices.EMPTY_STRING);
	}

	/**
	 * The source code of this content.
	 */
	public String getSourceCode() {
		return _sourceCode;
	}

	@Override
	public void generateFullText(FullTextBuBuffer buffer) {
		Document document = Jsoup.parse(getSourceCode());
		buffer.add(document.text());
	}

	/**
	 * @see #getSourceCode()
	 */
	public void setSourceCode(String sourceCode) {
		_sourceCode = sourceCode;
	}

	/**
	 * Map containing images for this structured text.
	 */
	public Map<String, BinaryData> getImages() {
		return _images;
	}

	/**
	 * @see #getImages()
	 */
	public void setImages(Map<String, BinaryData> images) {
		_images = images;
	}

	/**
	 * Adds the given image data to this {@link StructuredText}.
	 * 
	 * @param key
	 *        The file name of the image, used in the <code>src</code> attribute of an
	 *        <code>img</code> tag in the {@link #getSourceCode()}.
	 * @param image
	 *        Image to be added to the existing list of already used images.
	 */
	public void addImage(String key, BinaryData image) {
		_images.put(key, image);
	}

	/**
	 * Creates a copy of this {@link StructuredElement}.
	 */
	public StructuredText copy() {
		return new StructuredText(getSourceCode(), map(getImages()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_images == null) ? 0 : _images.hashCode());
		result = prime * result + ((_sourceCode == null) ? 0 : _sourceCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StructuredText other = (StructuredText) obj;
		if (_images == null) {
			if (other._images != null)
				return false;
		} else if (!_images.equals(other._images))
			return false;
		if (_sourceCode == null) {
			if (other._sourceCode != null)
				return false;
		} else if (!_sourceCode.equals(other._sourceCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("content",
				_sourceCode.length() < 256 ? _sourceCode.replace("\r", "&#13;").replace("\n", "&#10;")
					: _sourceCode.substring(0, 256).replace("\r", "&#13;").replace("\n", "&#10;") + "...")
			.add("length", _sourceCode.length())
			.add("images", _images.size())
			.build();
	}

	/** If the {@link StructuredText} is null, the empty {@link String} is returned. */
	public static String getSourceCodeNullSafe(StructuredText structuredText) {
		if (structuredText == null) {
			return "";
		}
		return structuredText.getSourceCode();
	}

	/** If the {@link StructuredText} is null, {@link Collections#emptyMap()} is returned. */
	public static Map<String, BinaryData> getImagesNullSafe(StructuredText structuredText) {
		if (structuredText == null) {
			return emptyMap();
		}
		return structuredText.getImages();
	}

}
