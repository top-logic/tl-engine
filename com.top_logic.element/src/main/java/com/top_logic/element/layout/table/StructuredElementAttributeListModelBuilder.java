/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import com.top_logic.basic.col.StructureView;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.element.layout.tree.StructuredElementStructureView;
import com.top_logic.model.TLObject;

/**
 * Build a list model with values from an attribute of a {@link TLObject}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredElementAttributeListModelBuilder<C extends StructuredElementAttributeListModelBuilder.Config<?>>
		extends StructureReferenceListModelBuilder<C> {

	/**
	 * Configuration for getting attributes from a structured element.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config<I extends StructuredElementAttributeListModelBuilder<?>>
			extends StructureReferenceListModelBuilder.Config<I> {

		@Override
		@ImplementationClassDefault(StructuredElementStructureView.class)
		@ItemDefault
		PolymorphicConfiguration<StructureView<TLObject>> getStructure();

	}

	/**
	 * Creates a new {@link StructuredElementAttributeListModelBuilder} from the given
	 * configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link StructuredElementAttributeListModelBuilder}.
	 */
	public StructuredElementAttributeListModelBuilder(InstantiationContext context, C config)
			throws ConfigurationException {
		super(context, config);
	}

}
