/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.knowledge.gui.AbstractTLItemResourceProvider;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link ResourceProvider} that provides the type image for a grid row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractGridTypeProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		return AbstractTLItemResourceProvider.getMetaElementLabel(GridComponent.getMetaElement(toModel(object)));
	}

	@Override
	public String getType(Object anObject) {
		return modelType(toModel(anObject));
	}

	private String modelType(Object model) {
		return GridComponent.getTypeName(model);
	}

	@Override
	public String getTooltip(Object anObject) {
		Object model = toModel(anObject);

		if (GridComponent.isTransient(model)) {
			return null;
		} else {
			return MetaResourceProvider.INSTANCE.getTooltip(model);
		}
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		Object model = toModel(anObject);

		if (GridComponent.isTransient(model)) {
			return DefaultResourceProvider.getTypeImage(modelType(model), aFlavor);
		} else {
			return MetaResourceProvider.INSTANCE.getImage(model, aFlavor);
		}
	}

	/**
	 * Return the business model out of the given "row" object.
	 * 
	 * @param row
	 *        The row to get the business model from, must not be <code>null</code>.
	 * @return The requested business model, must not be <code>null</code>.
	 */
	protected abstract Object toModel(Object row);

}
