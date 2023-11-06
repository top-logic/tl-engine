/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.storage;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.layout.wysiwyg.ui.StructuredText.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.kbbased.storage.AbstractStorage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AbstractStorage} to store source code and images for a HTML attribute.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredTextAttributeStorage<C extends StructuredTextAttributeStorage.Config<?>>
		extends CommonStructuredTextAttributeStorage<C> {

	/** Name of the database table storing the images. */
	private static final String HTML_ATTRIBUTE_STORAGE = "HTMLAttributeStorage";

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        {@link com.top_logic.element.meta.AbstractStorageBase.Config} for the
	 *        {@link StructuredTextAttributeStorage}.
	 */
	public StructuredTextAttributeStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public StructuredText getAttributeValue(TLObject tlObject, TLStructuredTypePart attribute) {
		StructuredText structuredText = new StructuredText();

		setImages(tlObject, structuredText);
		setSourceCode(structuredText, getSourceCode(tlObject, attribute));

		return structuredText;
	}

	private void setImages(TLObject tlObject, StructuredText structuredText) {
		for (KnowledgeItem object : fetchImages(tlObject)) {
			String filename = getFileName(object);
			String contentType = getContentType(object);
			BinaryData data = getData(object, filename, contentType);

			structuredText.addImage(filename, data);
		}
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value) {
		// The collection is copied to avoid ConcurrentModificationExceptions.
		Set<KnowledgeItem> cachedImages = set(fetchImages(object));

		StructuredText structuredText = (StructuredText) value;

		Map<String, BinaryData> images = getImagesNullSafe(structuredText);
		Set<String> filenames = images.keySet();
		boolean imagesUpdated = updateImages(cachedImages, images);
		boolean imagesAdded = addImages(object, attribute, cachedImages, structuredText, filenames);
		boolean imagesRemoved = removeImages(cachedImages, filenames);

		boolean imagesChanged = imagesAdded || imagesUpdated || imagesRemoved;
		boolean sourceCodeChanged = updateSourceCode(object, attribute, getSourceCodeNullSafe(structuredText));

		if (imagesChanged || sourceCodeChanged) {
			/* As an updated attribute does not affect the TLObject itself, Lucene will not create a
			 * new index. Thats why the owner has to be touched. */
			object.tTouch();
		}
	}

	private void setSourceCode(StructuredText structuredText, String sourceCode) {
		structuredText.setSourceCode(StringServices.nonNull(sourceCode));
	}

	private String getSourceCode(TLObject owner, TLStructuredTypePart attribute) {
		return (String) owner.tGetData(attribute.getName());
	}

	private boolean updateSourceCode(TLObject owner, TLStructuredTypePart attribute, String sourceCode) {
		// Do not store empty value.
		sourceCode = StringServices.nonEmpty(sourceCode);
		Object formerValue = owner.tSetData(attribute.getName(), sourceCode);
		return Utils.equals(formerValue, sourceCode);
	}

	private boolean addImages(TLObject self, TLStructuredTypePart attribute, Set<KnowledgeItem> cachedImages,
			StructuredText structuredText, Set<String> filenames) {
		Set<String> possibleToBeAdded = new HashSet<>(filenames);
		Set<String> cachedFilenames = getFileNames(cachedImages);

		possibleToBeAdded.removeAll(cachedFilenames);
		if (possibleToBeAdded.isEmpty()) {
			return false;
		}
		addImages(self, attribute, structuredText, possibleToBeAdded);
		return true;
	}


	private void addImages(TLObject self, TLStructuredTypePart attribute, StructuredText structuredText,
			Set<String> possibleToBeAdded) {
		for (String fileName : possibleToBeAdded) {
			BinaryData binaryData = structuredText.getImages().get(fileName);
			createImageItem(self, attribute, binaryData, fileName);
		}
	}

	@Override
	protected Class<?> getApplicationValueType() {
		return StructuredText.class;
	}

	@Override
	protected String getImagesTableName() {
		return HTML_ATTRIBUTE_STORAGE;
	}

	@Override
	public boolean isEmpty(Object value) {
		if (value instanceof StructuredText) {
			return StringServices.isEmpty(((StructuredText) value).getSourceCode());
		}
		return super.isEmpty(value);
	}

}
