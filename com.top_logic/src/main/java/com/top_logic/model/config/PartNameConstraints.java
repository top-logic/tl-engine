/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.constraint.annotation.RegexpErrorKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;

/**
 * Constraints for the name of model parts.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PartNameConstraints {

	/** Pattern that the name of a {@link TLType} must match. */
	public static final String MANDATORY_TYPE_NAME_PATTERN =
		"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:\\.\\p{javaJavaIdentifierPart}+)*";

	/**
	 * {@link RegexpErrorKey} for {@link I18NConstants#MANDATORY_TYPE_NAME_MISMATCH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class MandatoryTypeNameKey implements RegexpErrorKey {

		/** {@link MandatoryTypeNameKey} instance. */
		public static final MandatoryTypeNameKey INSTANCE = new MandatoryTypeNameKey();

		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.MANDATORY_TYPE_NAME_MISMATCH;
		}

	}

	/** Recommended pattern for the name of a {@link TLType}. */
	public static final String RECOMMENDED_TYPE_NAME_PATTERN = "\\p{javaUpperCase}\\p{javaJavaIdentifierPart}*";

	/**
	 * {@link RegexpErrorKey} for {@link I18NConstants#RECOMMENDED_TYPE_NAME_MISMATCH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class RecommendedTypeNameKey implements RegexpErrorKey {
		
		/** {@link RecommendedTypeNameKey} instance. */
		public static final RecommendedTypeNameKey INSTANCE = new RecommendedTypeNameKey();
		
		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.RECOMMENDED_TYPE_NAME_MISMATCH;
		}
		
	}
	
	/** Pattern that the name of a {@link TLModule} must match. */
	public static final String MANDATORY_MODULE_NAME_PATTERN =
		"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*";

	/**
	 * {@link RegexpErrorKey} for {@link I18NConstants#MANDATORY_MODULE_NAME_MISMATCH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class MandatoryModuleNameKey implements RegexpErrorKey {

		/** {@link MandatoryModuleNameKey} instance. */
		public static final MandatoryModuleNameKey INSTANCE = new MandatoryModuleNameKey();

		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.MANDATORY_MODULE_NAME_MISMATCH;
		}

	}

	/** Pattern that the name of a {@link TLTypePart} must match. */
	public static final String MANDATORY_TYPE_PART_NAME_PATTERN =
		"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:[\\.\\-]\\p{javaJavaIdentifierPart}+)*";

	/**
	 * {@link RegexpErrorKey} for {@link I18NConstants#MANDATORY_TYPE_PART_NAME_MISMATCH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class MandatoryTypePartNameKey implements RegexpErrorKey {

		/** {@link MandatoryTypePartNameKey} instance. */
		public static final MandatoryTypePartNameKey INSTANCE = new MandatoryTypePartNameKey();

		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.MANDATORY_TYPE_PART_NAME_MISMATCH;
		}

	}

	/** Recommended pattern for the name of a {@link TLTypePart}. */
	public static final String RECOMMENDED_TYPE_PART_NAME_PATTERN = "\\p{javaLowerCase}\\p{javaJavaIdentifierPart}*";

	/**
	 * {@link RegexpErrorKey} for {@link I18NConstants#RECOMMENDED_TYPE_PART_NAME_MISMATCH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class RecommendedTypePartNameKey implements RegexpErrorKey {

		/** {@link RecommendedTypePartNameKey} instance. */
		public static final RecommendedTypePartNameKey INSTANCE = new RecommendedTypePartNameKey();

		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.RECOMMENDED_TYPE_PART_NAME_MISMATCH;
		}

	}

	/** Recommended pattern for the name of a {@link TLClassifier}. */
	public static final String RECOMMENDED_CLASSIFIER_NAME_PATTERN =
		"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:\\.\\p{javaJavaIdentifierPart}+)*";

	/**
	 * {@link RegexpErrorKey} for {@link I18NConstants#RECOMMENDED_CLASSIFIER_NAME_MISMATCH}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class RecommendedClassifierNameKey implements RegexpErrorKey {

		/** {@link RecommendedClassifierNameKey} instance. */
		public static final RecommendedClassifierNameKey INSTANCE = new RecommendedClassifierNameKey();

		@Override
		public ResKey getKey(String pattern, String input) {
			return I18NConstants.RECOMMENDED_CLASSIFIER_NAME_MISMATCH;
		}

	}

}

