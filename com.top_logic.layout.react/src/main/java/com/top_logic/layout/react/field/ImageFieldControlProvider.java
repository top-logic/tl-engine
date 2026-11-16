/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ImageValues;
import com.top_logic.layout.react.control.form.ListElementFieldModel;
import com.top_logic.layout.react.control.form.ReactImageFieldControl;

/**
 * Edits values that show images as a {@link ReactImageFieldControl}: a preview of each image,
 * replaced by dropping or picking a file, removed by a button, and an area taking further images.
 *
 * <p>
 * The control holds a single image and the images of a {@link FieldSpec#isMultiple() multi-valued}
 * field alike, so this provider {@link ReactFieldControlProvider#editsCollections() edits
 * collections}; the images of an {@link FieldSpec#isOrdered() ordered} field are arranged by the
 * user. A preview needs room, so the control is {@link ReactFieldControlProvider#isLarge(FieldSpec)
 * large}: where the field is {@link FieldSpec#isCompact() compact}, a table cell for instance, the
 * names of its images stand for it. The files the file picker offers are the
 * {@link FieldSpec#getAcceptedTypes() accepted types} of the field, or the default types of this
 * provider where the field states none, each up to the {@link FieldSpec#getMaxUploadSize() upload
 * limit} of the field.
 * </p>
 *
 * @see FieldControlRegistry#IMAGE
 * @see GalleryFieldControlProvider
 */
public class ImageFieldControlProvider implements ReactFieldControlProvider {

	/** Separates the names of the images in the preview of a field. */
	private static final String PREVIEW_SEPARATOR = ", ";

	private final ImageValues _imageValues;

	private final String _defaultAcceptedTypes;

	/**
	 * Creates an {@link ImageFieldControlProvider}.
	 *
	 * @param imageValues
	 *        What the values of an edited field are, see {@link ImageValues}.
	 * @param defaultAcceptedTypes
	 *        The file types the file picker offers where the field states no
	 *        {@link FieldSpec#getAcceptedTypes() accepted types} of its own, in the form of
	 *        {@link FieldSpec#getAcceptedTypes()}; <code>null</code> to offer every file then.
	 */
	public ImageFieldControlProvider(ImageValues imageValues, String defaultAcceptedTypes) {
		_imageValues = imageValues;
		_defaultAcceptedTypes = defaultAcceptedTypes;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		ReactImageFieldControl control =
			new ReactImageFieldControl(context, model, _imageValues, field.isMultiple(), field.isOrdered());
		String acceptedTypes = field.getAcceptedTypes() != null ? field.getAcceptedTypes() : _defaultAcceptedTypes;
		if (acceptedTypes != null) {
			control.setAcceptedTypes(acceptedTypes);
		}
		if (field.getMaxUploadSize() > 0) {
			control.setMaxUploadSize(field.getMaxUploadSize());
		}
		return control;
	}

	@Override
	public boolean editsCollections() {
		return true;
	}

	@Override
	public boolean isLarge(FieldSpec field) {
		return true;
	}

	/**
	 * The names of the images of the given value, separated by commas.
	 */
	@Override
	public String previewText(FieldSpec field, Object value) {
		StringBuilder result = new StringBuilder();
		for (Object element : ListElementFieldModel.elementsOfValue(value)) {
			BinaryDataSource image = _imageValues.imageOf(element);
			if (image == null) {
				continue;
			}
			if (result.length() > 0) {
				result.append(PREVIEW_SEPARATOR);
			}
			result.append(image.getName());
		}
		return result.toString();
	}

}
