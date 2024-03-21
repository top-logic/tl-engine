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
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Display settings for an attribute that is entered by selecting from a list of options.
 * 
 * <p>
 * This annotation is useful for attributes of type {@link TLEnumeration} and those that have an
 * options annotation.
 * </p>
 * 
 * @implNote The part of the name "classification" originates from displaying the classifiers of a
 *           {@link TLEnumeration}. The usage of this annotation has be generalized to all
 *           attributes with atomic value types that are entered by a selecting from a list of
 *           options.
 * 
 * @see ReferenceDisplay
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("classification-display")
@TargetType({
	// Those types also might have an options annotation.
	TLTypeKind.FLOAT, TLTypeKind.INT, TLTypeKind.STRING,

	// This type has canonical options.
	TLTypeKind.ENUMERATION
})
@InApp
@Label("Options display")
public interface ClassificationDisplay extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Configuration name for the {@link #getValue()} property.
	 */
	String VALUE = "value";

	/**
	 * How to display this attribute by default.
	 * 
	 * @see ClassificationPresentation
	 */
	@Name(VALUE)
	@Label("Presentation")
	ClassificationPresentation getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(ClassificationPresentation value);

	/**
	 * Presentation of options for an attribute with enumerated values.
	 * 
	 * @see ReferencePresentation
	 */
	public enum ClassificationPresentation implements ExternallyNamed {

		/**
		 * Display as drop-down select box.
		 */
		@Label("Select box")
		DROP_DOWN("drop-down"),

		/**
		 * Display as input field with pop-up select dialog.
		 */
		@Label("Select dialog")
		POP_UP("pop-up"),

		/**
		 * Shows all possible values using a vertical list of radio buttons.
		 */
		@Label("Radio buttons")
		RADIO("radio"),

		/**
		 * Shows all possible values using horizontal list of radio buttons.
		 */
		@Label("Radio buttons (single line)")
		RADIO_INLINE("radio-inline"),

		/**
		 * Display as check-list pop-up.
		 */
		CHECKLIST("checklist");

		private final String _externalName;

		private ClassificationPresentation(String externalName) {
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
		 *        The {@link ClassificationPresentation} which must not exist.
		 * @return For convenience reasons to use "throw noSuchEnum(...)".
		 */
		public static UnreachableAssertion noSuchEnum(ClassificationPresentation o) {
			throw new UnreachableAssertion("There is no such " + ClassificationPresentation.class.getName() + ": " + o);
		}

	}

	/**
	 * Creates a {@link ClassificationDisplay} annotation with the given presentation value.
	 */
	static ClassificationDisplay display(ClassificationPresentation presentation) {
		ClassificationDisplay result = TypedConfiguration.newConfigItem(ClassificationDisplay.class);
		result.setValue(presentation);
		return result;
	}
}
