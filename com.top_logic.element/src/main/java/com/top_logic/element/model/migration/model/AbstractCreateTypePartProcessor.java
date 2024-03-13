/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.migration.data.QualifiedPartName;

/**
 * Abstract {@link MigrationProcessor} creating {@link TLTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCreateTypePartProcessor<C extends AbstractCreateTypePartProcessor.Config<?>>
		extends AbstractConfiguredInstance<C> implements TLModelBaseLineMigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link AbstractCreateTypePartProcessor}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends AbstractCreateTypePartProcessor<?>>
			extends PolymorphicConfiguration<I>, AnnotatedConfig<TLAttributeAnnotation> {

		/** Name for {@link #isOrdered()}. */
		String ORDERED = PartConfig.ORDERED_PROPERTY;

		/** Name for {@link #isBag()}. */
		String BAG = PartConfig.BAG_PROPERTY;

		/** Name for {@link #isMultiple()}. */
		String MULTIPLE = PartConfig.MULTIPLE_PROPERTY;

		/** Name for {@link #isMandatory()}. */
		String MANDATORY = PartConfig.MANDATORY;

		/**
		 * Qualified name of the {@link Config}.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName name);

		/**
		 * See {@link PartConfig#getMandatory()}
		 */
		@Name(MANDATORY)
		boolean isMandatory();

		/**
		 * Setter for {@link #isMandatory()}.
		 */
		void setMandatory(boolean value);

		/**
		 * See {@link PartConfig#isMultiple()}.
		 */
		@Name(MULTIPLE)
		boolean isMultiple();

		/**
		 * Setter for {@link #isMultiple()}.
		 */
		void setMultiple(boolean value);

		/**
		 * See {@link PartConfig#isBag()}.
		 * 
		 * <p>
		 * This value is unused unless the part is {@link #isMultiple() multiple}.
		 * </p>
		 */
		@Name(BAG)
		boolean isBag();

		/**
		 * Setter for {@link #isBag()}.
		 */
		void setBag(boolean value);

		/**
		 * See {@link PartConfig#isOrdered()}.
		 * 
		 * <p>
		 * This value is unused unless the part is {@link #isMultiple() multiple}.
		 * </p>
		 */
		@Name(ORDERED)
		boolean isOrdered();

		/**
		 * Setter for {@link #isOrdered()}.
		 */
		void setOrdered(boolean value);

	}

	/**
	 * Create a {@link AbstractCreateTypePartProcessor}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public AbstractCreateTypePartProcessor(InstantiationContext context, C config) {
		super(context, config);
	}

	<T> T nullIfUnset(String propertyName) {
		return MigrationUtils.nullIfUnset(getConfig(), propertyName);
	}

}

