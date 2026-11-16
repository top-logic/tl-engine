/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Display settings for an attribute, that contains binary data and allows uploading of files.
 * 
 * <p>
 * This annotation is useful for attributes of type {@link BinaryData}.
 * </p>
 * 
 * @see ReferenceDisplay
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
@TagName("binary-display")
@TargetType({
	// This type is a binary file.
	TLTypeKind.BINARY
})
@InApp
@Label("Binary data display")
public interface BinaryDisplay extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Configuration name for the {@link #getPresentation()} property.
	 */
	String PRESENTATION = "presentation";

	/**
	 * How to display this attribute by default.
	 * 
	 * @see BinaryPresentation
	 */
	@Name(PRESENTATION)
	@Label("Presentation")
	BinaryPresentation getPresentation();

	/**
	 * @see #getPresentation()
	 */
	void setPresentation(BinaryPresentation value);

	/**
	 * Creates a {@link BinaryDisplay} annotation with the given presentation value.
	 */
	static BinaryDisplay display(BinaryPresentation presentation) {
		BinaryDisplay result = TypedConfiguration.newConfigItem(BinaryDisplay.class);
		result.setPresentation(presentation);
		return result;
	}

	/**
	 * Presentation / Display options for a binary attribute.
	 * 
	 * <p>
	 * In the React UI, {@link #DATA_ITEM} (the default) shows a multi-valued binary value as file
	 * chips, {@link #LIST} as a list with one file field per value, and {@link #IMAGE_UPLOAD} as an
	 * image field with a preview per image. A single binary value is shown as one file field, or as
	 * an image field with its preview for {@link #IMAGE_UPLOAD}.
	 * </p>
	 * 
	 * @see ReferencePresentation
	 */
	public enum BinaryPresentation implements ExternallyNamed {

		/**
		 * Display as button to upload files and a list of the uploaded files.
		 * 
		 * <p>
		 * In the React UI, a multi-valued binary value is shown as file chips: one chip per file
		 * and a button to add several files at once.
		 * </p>
		 */
		@Label("Button with file list")
		DATA_ITEM("data-item"),

		/**
		 * Display as field to upload via click or drop and display of the uploaded image.
		 * 
		 * <p>
		 * In the React UI, a single binary value as well as a multi-valued one: without an image, an
		 * area to drop an image on or to click for the file picker; with an image, its preview and a
		 * button removing it. Several images are shown as a row of such previews, each replaced by
		 * dropping or picking another file and, for an ordered attribute, moved within the row; the
		 * area at the end of the row adds further images. A value that is only displayed shows the
		 * previews alone.
		 * </p>
		 */
		@Label("Image drop field")
		IMAGE_UPLOAD("image-upload"),

		/**
		 * Display a multi-valued binary value as a list with one file field per value; files are
		 * added several at once.
		 * 
		 * <p>
		 * A single binary value is shown like {@link #DATA_ITEM}.
		 * </p>
		 */
		@Label("List of file fields")
		LIST("list");

		private final String _externalName;

		private BinaryPresentation(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Service method to use in "default" case in switch over this enum to assert that all cases
		 * are covered.
		 * 
		 * @param o
		 *        The {@link BinaryPresentation} which must not exist.
		 * @return For convenience reasons to use <code>throw noSuchBinary(...)</code>.
		 */
		public static UnreachableAssertion noSuchBinary(BinaryPresentation o) {
			throw new UnreachableAssertion("There is no such " + BinaryPresentation.class.getName() + ": " + o);
		}

	}
}
