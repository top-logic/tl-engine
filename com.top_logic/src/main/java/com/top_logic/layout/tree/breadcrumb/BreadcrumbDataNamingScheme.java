/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * The {@link ModelNamingScheme} for {@link BreadcrumbData} instances relies on the
 * {@link BreadcrumbDataOwner} retrievable via {@link BreadcrumbData#getOwner()}.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class BreadcrumbDataNamingScheme extends AbstractModelNamingScheme<BreadcrumbData, BreadcrumbDataName> {
	/**
	 * The instance of the {@link BreadcrumbDataNamingScheme}. This is not a singleton, as
	 * (potential) subclasses can create further instances.
	 */
	public static final BreadcrumbDataNamingScheme INSTANCE =
		new BreadcrumbDataNamingScheme();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected BreadcrumbDataNamingScheme() {
		// See JavaDoc above.
	}

	@Override
	public Class<BreadcrumbDataName> getNameClass() {
		return BreadcrumbDataName.class;
	}

	@Override
	public Class<BreadcrumbData> getModelClass() {
		return BreadcrumbData.class;
	}

	@Override
	public BreadcrumbData locateModel(ActionContext context, BreadcrumbDataName name) {
		BreadcrumbDataOwner breadcrumbDataOwner =
			(BreadcrumbDataOwner) ModelResolver.locateModel(context, name.getBreadcrumbDataOwner());
		return breadcrumbDataOwner.getBreadcrumbData();
	}

	@Override
	protected void initName(BreadcrumbDataName name, BreadcrumbData model) {
		name.setBreadcrumbDataOwner(ModelResolver.buildModelName(model.getOwner()));
	}

}
