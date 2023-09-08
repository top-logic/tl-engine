/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.storage;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.meta.kbbased.storage.AssociationQueryBasedStorage;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.StaticItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.wysiwyg.i18n.I18NStructuredTextAttributeStorage;
import com.top_logic.util.error.TopLogicException;

/**
 * The common parts of the {@link StructuredTextAttributeStorage} and the
 * {@link I18NStructuredTextAttributeStorage}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class CommonStructuredTextAttributeStorage<C extends AssociationQueryBasedStorage.Config<?>>
		extends AssociationQueryBasedStorage<C> {

	/**
	 * Identifier for the SHA-1 algorithm that is used by {@link MessageDigest#getInstance(String)}
	 */
	public static final String SHA_1_PROTOCOL = "SHA-1";

	/** Name of the column for the binary image data. */
	public static final String DATA_ATTRIBUTE_NAME = "data";

	/** Name of the column for the image content type. */
	public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "content-type";

	/** Name of the column for the image file name. */
	public static final String FILENAME_ATTRIBUTE_NAME = "filename";

	/** Name of the column for the hash of the binary image data. */
	public static final String HASH_ATTRIBUTE_NAME = "hash";

	private AssociationSetQuery<? extends KnowledgeItem> _imagesQuery;

	/** {@link TypedConfiguration} constructor for {@link CommonStructuredTextAttributeStorage}. */
	public CommonStructuredTextAttributeStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_imagesQuery = createQuery(getImagesTableName(), attribute, StaticItem.class);
	}

	/**
	 * The name of the {@link MOClass} storing the images.
	 * 
	 * @return Never null.
	 */
	protected abstract String getImagesTableName();

	/**
	 * Updates the {@link BinaryData}, hash and content type of those images contained in both
	 * collections.
	 */
	protected boolean updateImages(Set<KnowledgeItem> oldImages, Map<String, BinaryData> newImages) {
		if (oldImages.isEmpty()) {
			return false;
		}
		Set<KnowledgeItem> possibleToBeUpdated = set(oldImages);

		possibleToBeUpdated.removeIf(oldImage -> newImages.get(getFileName(oldImage)) == null);

		boolean someImageChanged = false;
		for (KnowledgeItem oldImage : possibleToBeUpdated) {
			String oldFilename = getFileName(oldImage);
			String oldHash = getHash(oldImage);

			BinaryData newBinaryData = newImages.get(oldFilename);
			String newHash = calcHash(newBinaryData);

			if (!oldHash.equals(newHash)) {
				setHash(oldImage, newHash);
				setData(oldImage, newBinaryData);
				setContentType(oldImage, newBinaryData.getContentType());
				someImageChanged = true;
			}
		}
		return someImageChanged;
	}

	/** Removes those old images, whose file names are not in the new file names {@link Set}. */
	protected boolean removeImages(Set<KnowledgeItem> oldImages, Set<String> newFileNames) {
		Set<KnowledgeItem> possibleToBeRemoved = set(oldImages);
		possibleToBeRemoved.removeIf(oldImage -> newFileNames.contains(getFileName(oldImage)));
		getKnowledgeBase().deleteAll(possibleToBeRemoved);
		return !possibleToBeRemoved.isEmpty();
	}

	/** The file names of the given images. */
	protected Set<String> getFileNames(Set<KnowledgeItem> images) {
		Set<String> filenames = set();
		images.forEach(image -> filenames.add(getFileName(image)));
		return filenames;
	}

	/**
	 * The images stored for the given object in this attribute.
	 * 
	 * @param owner
	 *        The object whose images should be returned.
	 * @return An unmodifiable view. Never null.
	 */
	protected Set<? extends KnowledgeItem> fetchImages(TLObject owner) {
		return resolveQuery(owner, getImagesQuery());
	}

	/**
	 * The query that finds the images of a given base object.
	 * <p>
	 * Must not be called before the storage is {@link #init(TLStructuredTypePart) initialized}.
	 * </p>
	 * 
	 * @return Never null.
	 */
	protected AssociationSetQuery<? extends KnowledgeItem> getImagesQuery() {
		return _imagesQuery;
	}

	/** Creates an {@link KnowledgeItem} representing an image, filled with the given values. */
	protected KnowledgeItem createImageItem(TLObject owner, TLStructuredTypePart attribute, BinaryData binaryData,
			String fileName) {
		KnowledgeItem image = createImageItem();
		fillImage(owner, attribute, binaryData, fileName, image);
		return image;
	}

	/**
	 * Sets the given values in the corresponding attributes of the given image {@link TLObject}.
	 */
	protected void fillImage(TLObject owner, TLStructuredTypePart attribute, BinaryData binaryData,
			String fileName, KnowledgeItem image) {
		setObject(image, owner);
		setAttribute(image, attribute);
		setFileName(image, fileName);
		setContentType(image, binaryData.getContentType());
		setData(image, binaryData);
		setHash(image, calcHash(binaryData));
	}

	/** Creates an empty {@link KnowledgeItem} representing an image. */
	protected KnowledgeItem createImageItem() {
		return createItem(getImagesTableName());
	}

	/**
	 * Calculates the hash of the content of the given {@link BinaryData}.
	 * 
	 * @see #getHash(KnowledgeItem)
	 */
	protected String calcHash(BinaryData image) {
		MessageDigest messageDigest = getMessageDigest();
		readAllIntoDigest(messageDigest, image);
		return Hex.encodeHexString(messageDigest.digest());
	}

	private MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance(SHA_1_PROTOCOL);
		} catch (NoSuchAlgorithmException ex) {
			throw new TopLogicException(com.top_logic.model.wysiwyg.storage.I18NConstants.HASH_ALGORITHM_ERROR, ex);
		}
	}

	private void readAllIntoDigest(MessageDigest messageDigest, BinaryData image) {
		try (InputStream in = image.getStream()) {
			byte[] buffer = new byte[4096];
			while (true) {
				int numberElements = in.read(buffer);
				if (numberElements == -1) {
					break;
				}
				messageDigest.update(buffer, 0, numberElements);
			}
		} catch (IOException ex) {
			throw new TopLogicException(com.top_logic.model.wysiwyg.storage.I18NConstants.READ_FILE_DATA_ERROR, ex);
		}
	}

	/** Setter for the owner of the given image. */
	protected void setObject(KnowledgeItem image, TLObject newOwner) {
		image.setAttributeValue(OBJECT_ATTRIBUTE_NAME, newOwner.tHandle());
	}

	/** Setter for the attribute in which the given image is stored. */
	protected void setAttribute(KnowledgeItem image, TLStructuredTypePart newAttribute) {
		image.setAttributeValue(META_ATTRIBUTE_ATTRIBUTE_NAME, newAttribute.tHandle());
	}

	/** The file name of the image. */
	protected String getFileName(KnowledgeItem image) {
		return (String) image.getAttributeValue(FILENAME_ATTRIBUTE_NAME);
	}

	/** @see #getFileName(KnowledgeItem) */
	protected void setFileName(KnowledgeItem image, String newFileName) {
		image.setAttributeValue(FILENAME_ATTRIBUTE_NAME, newFileName);
	}

	/** The {@link BinaryData#getContentType() content type} of the image. */
	protected String getContentType(KnowledgeItem image) {
		return (String) image.getAttributeValue(CONTENT_TYPE_ATTRIBUTE_NAME);
	}

	/** @see #getContentType(KnowledgeItem) */
	protected void setContentType(KnowledgeItem image, String newContentType) {
		image.setAttributeValue(CONTENT_TYPE_ATTRIBUTE_NAME, newContentType);
	}

	/** The hash of the binary data. */
	protected String getHash(KnowledgeItem image) {
		return (String) image.getAttributeValue(HASH_ATTRIBUTE_NAME);
	}

	/** @see #getHash(KnowledgeItem) */
	protected void setHash(KnowledgeItem image, String newHash) {
		image.setAttributeValue(HASH_ATTRIBUTE_NAME, newHash);
	}

	/**
	 * The binary data of the image.
	 * 
	 * @param image
	 *        The {@link TLObject} representing the image. Not the owner of the image.
	 * @return Never null.
	 */
	protected BinaryData getData(KnowledgeItem image, String filename, String contentType) {
		BinaryData binaryData = (BinaryData) image.getAttributeValue(DATA_ATTRIBUTE_NAME);
		return new DefaultDataItem(filename, binaryData, contentType);
	}

	/** @see #getData(KnowledgeItem, String, String) */
	protected void setData(KnowledgeItem image, BinaryData newData) {
		image.setAttributeValue(DATA_ATTRIBUTE_NAME, newData);
	}

}
