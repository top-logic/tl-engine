/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming;

/**
 * {@link GenericModelOwnerNaming} for {@link GenericBreadcrumbDataOwner}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericBreadcrumbDataOwnerNaming extends
		GenericModelOwnerNaming<BreadcrumbData, GenericBreadcrumbDataOwner, GenericBreadcrumbDataOwnerNaming.GenericBreadcrumbDataName> {

	/**
	 * {@link com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming.GenericModelName}
	 * for {@link GenericBreadcrumbDataOwnerNaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GenericBreadcrumbDataName extends GenericModelOwnerNaming.GenericModelName<BreadcrumbData> {
		// Marker interface to have correct namespace.
	}

	@Override
	public Class<GenericBreadcrumbDataOwner> getModelClass() {
		return GenericBreadcrumbDataOwner.class;
	}

	@Override
	public Class<GenericBreadcrumbDataName> getNameClass() {
		return GenericBreadcrumbDataName.class;
	}

	@Override
	protected GenericBreadcrumbDataOwner createOwner(Object reference, Mapping<Object, BreadcrumbData> algorithm) {
		return new GenericBreadcrumbDataOwner(reference, algorithm);
	}

}
