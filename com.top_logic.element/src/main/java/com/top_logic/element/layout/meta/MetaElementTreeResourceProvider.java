/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.element.layout.meta;

import com.top_logic.element.layout.meta.MetaElementTreeModelBuilder.ModuleContainer;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.resources.TLPartScopedResourceProvider;

/**
 * {@link ResourceProvider} for the model editor meta elements.
 * 
 * <p>
 * The meta element tree contains nodes with {@link TLModelPart}'s and {@link ModuleContainer} as
 * business objects.
 * </p>
 * 
 * <p>
 * For {@link TLModelPart}'s this resource provider delegates to
 * {@link TLPartScopedResourceProvider}.
 * </p>
 * 
 * <p>
 * For {@link ModuleContainer}'s it only provides a label by using the containers technical name to
 * create an i18n key in the same way {@link TLPartScopedResourceProvider} does.
 * </p>
 * 
 * @see TLPartScopedResourceProvider
 * @see MetaElementTreeModelBuilder
 * @see ModuleContainer
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class MetaElementTreeResourceProvider extends AbstractResourceProvider {

	/**
	 * Singleton {@link MetaElementTreeResourceProvider} instance.
	 */
	public static final MetaElementTreeResourceProvider INSTANCE = new MetaElementTreeResourceProvider();

	@Override
	public String getLabel(Object object) {
		if (object instanceof TLModelPart) {
			return TLPartScopedResourceProvider.INSTANCE.getLabel(object);
		} else if (object instanceof MetaElementTreeModelBuilder.ModuleContainer) {
			return ModuleContainerResourceProvider.INSTANCE.getLabel(object);
		}

		return super.getLabel(object);
	}

	@Override
	public String getType(Object object) {
		if (object instanceof TLModelPart) {
			return TLPartScopedResourceProvider.INSTANCE.getType(object);
		} else if (object instanceof MetaElementTreeModelBuilder.ModuleContainer) {
			return ModuleContainerResourceProvider.INSTANCE.getType(object);
		}

		return super.getType(object);
	}

	@Override
	public String getTooltip(Object object) {
		if (object instanceof TLModelPart) {
			return TLPartScopedResourceProvider.INSTANCE.getTooltip(object);
		} else if (object instanceof MetaElementTreeModelBuilder.ModuleContainer) {
			return ModuleContainerResourceProvider.INSTANCE.getTooltip(object);
		}

		return super.getTooltip(object);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object instanceof TLModelPart) {
			return TLPartScopedResourceProvider.INSTANCE.getImage(object, flavor);
		} else if (object instanceof MetaElementTreeModelBuilder.ModuleContainer) {
			return ModuleContainerResourceProvider.INSTANCE.getImage(object, flavor);
		}

		return super.getImage(object, flavor);
	}

	@Override
	public String getLink(DisplayContext context, Object object) {
		if (object instanceof TLModelPart) {
			return TLPartScopedResourceProvider.INSTANCE.getLink(context, object);
		} else if (object instanceof MetaElementTreeModelBuilder.ModuleContainer) {
			return ModuleContainerResourceProvider.INSTANCE.getLink(context, object);
		}

		return super.getLink(context, object);
	}

	@Override
	public String getCssClass(Object object) {
		if (object instanceof TLModelPart) {
			return TLPartScopedResourceProvider.INSTANCE.getCssClass(object);
		} else if (object instanceof MetaElementTreeModelBuilder.ModuleContainer) {
			return ModuleContainerResourceProvider.INSTANCE.getCssClass(object);
		}

		return super.getCssClass(object);
	}

}
