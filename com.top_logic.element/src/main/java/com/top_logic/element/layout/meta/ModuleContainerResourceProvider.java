/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.MetaElementTreeModelBuilder.ModuleContainer;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link ModuleContainer}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ModuleContainerResourceProvider extends AbstractResourceProvider {

	/**
	 * Singleton instance.
	 */
	public static final ModuleContainerResourceProvider INSTANCE = new ModuleContainerResourceProvider();

	@Override
	public String getLabel(Object object) {
		ModuleContainer container = (MetaElementTreeModelBuilder.ModuleContainer) object;

		String name = container.getName();
		String label = container.getLabel();

		ResKey key = ResKey.fallback(TLModelNamingConvention.modelPartNameKey(name), ResKey.text(label));

		return Resources.getInstance().getString(key);
	}

}
