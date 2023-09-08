/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.function.Consumer;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.event.ItemChange;

/**
 * Superclass for {@link Rewriter} that rewrites the values of an {@link ItemChange} and does not
 * skip additional events.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributeRewriter extends Rewriter {

	/**
	 * Configuration of an {@link AttributeRewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		/**
		 * The algorithm that rewrites the {@link ItemChange}.
		 */
		@Mandatory
		@DefaultContainer
		PolymorphicConfiguration<? extends Consumer<? super ItemChange>> getAlgorithm();

		/**
		 * Setter for {@link #getAlgorithm()}.
		 */
		void setAlgorithm(PolymorphicConfiguration<? extends Consumer<? super ItemChange>> algorithm);
	}

	private final Consumer<? super ItemChange> _algorithm;

	/**
	 * Creates a new {@link AttributeRewriter}.
	 */
	public AttributeRewriter(InstantiationContext context, Config config) {
		super(context, config);
		_algorithm = context.getInstance(config.getAlgorithm());

	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		_algorithm.accept(event);
		return APPLY_EVENT;
	}

}
