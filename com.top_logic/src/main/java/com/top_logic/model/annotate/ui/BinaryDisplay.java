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
 * Display settings for an attribute that is entered by uploading a binary data file.
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
@Label("Binary display")
public interface BinaryDisplay extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Configuration name for the {@link #getValue()} property.
	 */
	String VALUE = "value";

	/**
	 * How to display this attribute by default.
	 * 
	 * @see BinaryPresentation
	 */
	@Name(VALUE)
	@Label("Presentation")
	BinaryPresentation getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(BinaryPresentation value);

	/**
	 * Presentation of binary data for a binary attribute.
	 * 
	 * @see ReferencePresentation
	 */
	public enum BinaryPresentation implements ExternallyNamed {

		/**
		 * Display as data item.
		 */
		@Label("Data item")
		DATA_ITEM("data-item"),

		/**
		 * Display as image upload.
		 */
		@Label("Image upload")
		IMAGE_UPLOAD("image-upload");

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
		 * @return For convenience reasons to use "throw noSuchBinary(...)".
		 */
		public static UnreachableAssertion noSuchBinary(BinaryPresentation o) {
			throw new UnreachableAssertion("There is no such " + BinaryPresentation.class.getName() + ": " + o);
		}

	}

	/**
	 * Creates a {@link BinaryDisplay} annotation with the given presentation value.
	 */
	static BinaryDisplay display(BinaryPresentation presentation) {
		BinaryDisplay result = TypedConfiguration.newConfigItem(BinaryDisplay.class);
		result.setValue(presentation);
		return result;
	}
}
