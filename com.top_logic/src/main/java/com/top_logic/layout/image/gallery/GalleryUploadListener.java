/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.image.util.ImageType;
import com.top_logic.layout.image.util.ImageUtils;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ValueListener} for uploaded files to image gallery.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryUploadListener implements ValueListener {

	private static final int THUMBNAIL_SIZE = 128;

	private final TableData _tableData;

	GalleryUploadListener(TableData tableData) {
		_tableData = tableData;
	}

	@Override
	public void valueChanged(final FormField field, Object oldValue, Object newValue) {
		if (newValue != null) {
			triggerEditDialogValidation((DataField) field);
		}
	}

	private void triggerEditDialogValidation(final DataField field) {
		DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(new ToBeValidated() {
			@Override
			public void validate(DisplayContext context) {
				if (!field.hasError()) {
					List<BinaryData> files = field.getDataItems();
					for (BinaryData file : files) {
						if (isNotPartOfGallery(file)) {
							addToGallery(file);
						}
					}
					field.setValue(null);
				}
			}
		});
	}

	private boolean isNotPartOfGallery(BinaryDataSource file) {
		return !_tableData.getTableModel().containsRowObject(file);
	}

	private void addToGallery(BinaryData file) {
		addTableRow(createGalleryImage(file));
	}

	private void addTableRow(GalleryImage newGalleryImage) {
		int rowIndexAfterInsert = _tableData.getViewModel().getRowCount();
		((EditableRowTableModel) _tableData.getTableModel()).addRowObject(newGalleryImage);
		TableUtil.selectRow(_tableData, rowIndexAfterInsert);
	}

	/**
	 * Creates a {@link GalleryImage} out of {@link BinaryDataSource} with a thumbnail.
	 * 
	 * @param file
	 *        The image to upload as {@link BinaryDataSource}
	 * @return The {@link GalleryImage}
	 */
	public static GalleryImage createGalleryImage(BinaryData file) {
		ImageType imageType = getImageType(file);
		BinaryData databaseImage = getDatabaseImage(file, imageType);
		BinaryData thumbnail = getThumbnail(databaseImage, imageType);
		return new TransientGalleryImage(databaseImage, thumbnail);
	}

	private static ImageType getImageType(BinaryDataSource uploadedImage) {
		String fileNameExtension = FilenameUtils.getExtension(uploadedImage.getName());
		if (ImageDataUtil.isSupportedImageExtension(fileNameExtension)) {
			return ImageDataUtil.getImageType(fileNameExtension);
		} else {
			throw invalidImageException(uploadedImage);
		}
	}

	private static BinaryData getDatabaseImage(BinaryData file, ImageType imageType) {
		if (file.getSize() <= getMaxFileSize()) {
			return file;
		} else {
			BinaryData scaledImage =
				getImage(file, imageType, getTransformedImageWidth(), getTransformedImageHeight());
			if (scaledImage.getSize() <= getMaxFileSize()) {
				return ImageDataUtil.castMimeType(file.getName(), scaledImage);
			} else {
				throw new TopLogicException(
					I18NConstants.ERROR_MAX_IMAGE_SIZE_EXCEEDED__NAME_MAX_UNIT.fill(
						file.getName(), getMaxSizeMegabyte(), "MB"));
			}
		}
	}

	private static BinaryData getThumbnail(BinaryData databaseImage, ImageType imageType) {
		return getImage(databaseImage, imageType, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
	}

	private static BinaryData getImage(BinaryData imageData, ImageType imageType, int imageHeight,
			int imageWidth) {
		BufferedImage image = ImageUtils.createImage(imageData.toData());
		if (image != null) {
			BufferedImage scaledImage = ImageUtils.scaleToBoundary(image, imageWidth, imageHeight);
			return ImageUtils.getImageData(scaledImage, imageType);
		} else {
			throw invalidImageException(imageData);
		}
	}

	private static TopLogicException invalidImageException(BinaryDataSource image) {
		return new TopLogicException(
			I18NConstants.NO_VALID_IMAGE_ERROR__NAME_EXTENSION.fill(image.getName(),
				ImageDataUtil.getSupportedImageExtensions()));
	}

	/**
	 * Maximum size of uploaded images.
	 */
	public static long getMaxFileSize() {
		return getMaxSizeMegabyte() * 1024 * 1024;
	}

	private static int getMaxSizeMegabyte() {
		return ApplicationConfig.getInstance().getConfig(ImageSizeConfig.class).getMaxDatabaseSizeMegabyte();
	}

	private static int getTransformedImageWidth() {
		return ApplicationConfig.getInstance().getConfig(ImageSizeConfig.class).getTransformedImageWidth();
	}

	private static int getTransformedImageHeight() {
		return ApplicationConfig.getInstance().getConfig(ImageSizeConfig.class).getTransformedImageHeight();
	}
}