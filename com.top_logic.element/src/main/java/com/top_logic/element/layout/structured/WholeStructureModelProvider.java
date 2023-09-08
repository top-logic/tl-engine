/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ModelProvider} which constantly returns the root of a structure as business model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WholeStructureModelProvider extends AbstractConfiguredInstance<WholeStructureModelProvider.Config>
		implements ModelProvider {

	public interface Config extends PolymorphicConfiguration<ModelProvider> {
		@Name(STRUCTURE_ATTRIBUTE)
		@Mandatory
		String getStructure();
	}

	/** name of structure as configured in factory configuration */
	public static final String STRUCTURE_ATTRIBUTE = "structure";

	/** name of structure this tree operates on */
	protected final String _structureName;
	
	public WholeStructureModelProvider(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		this._structureName = config.getStructure();
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return getRoot();
	}

	public StructuredElement getRoot() {
		final StructuredElementFactory factory = StructuredElementFactory.getInstanceForStructure(this._structureName);
		return factory.getRoot();
	}

}
