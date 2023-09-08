/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.config.EndAspect;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link MigrationProcessor} handling properties like an {@link EndAspect}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractEndAspectProcessor<C extends AbstractEndAspectProcessor.Config<?>>
		extends AbstractConfiguredInstance<C> implements MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link AbstractEndAspectProcessor}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends AbstractEndAspectProcessor<?>>
			extends PolymorphicConfiguration<I>, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * Qualified name of the {@link EndAspect}.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * See {@link PartConfig#getMandatory()}
		 */
		@Name(PartConfig.MANDATORY)
		boolean isMandatory();

		/**
		 * See {@link EndAspect#isComposite()}.
		 */
		@Name(EndAspect.COMPOSITE_PROPERTY)
		boolean isComposite();

		/**
		 * See {@link EndAspect#isAggregate()}.
		 */
		@Name(EndAspect.AGGREGATE_PROPERTY)
		boolean isAggregate();

		/**
		 * See {@link PartConfig#isMultiple()}.
		 */
		@Name(PartConfig.MULTIPLE_PROPERTY)
		boolean isMultiple();

		/**
		 * See {@link PartConfig#isBag()}.
		 */
		@Name(PartConfig.BAG_PROPERTY)
		boolean isBag();

		/**
		 * See {@link PartConfig#isOrdered()}.
		 */
		@Name(PartConfig.ORDERED_PROPERTY)
		boolean isOrdered();

		/**
		 * See {@link EndAspect#canNavigate()}.
		 */
		@Name(EndAspect.NAVIGATE_PROPERTY)
		boolean canNavigate();
	}

	/**
	 * Create a {@link AbstractEndAspectProcessor}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public AbstractEndAspectProcessor(InstantiationContext context, C config) {
		super(context, config);
	}

}

