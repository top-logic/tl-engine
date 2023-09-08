/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwner;

/**
 * {@link GenericModelOwner} for {@link BreadcrumbDataOwner}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericBreadcrumbDataOwner extends GenericModelOwner<BreadcrumbData> implements BreadcrumbDataOwner {

	/**
	 * Algorithm to find {@link BreadcrumbData} as annotated model.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class AnnotatedModel extends GenericModelOwner.AbstractAnnotatedModel<BreadcrumbData> {
	
		/** Singleton {@link AnnotatedModel} instance. */
		public static final AnnotatedModel INSTANCE = new AnnotatedModel();
	
		private static final Property<BreadcrumbData> MODEL_PROPERTY =
			TypedAnnotatable.property(BreadcrumbData.class, "model");
	
		private AnnotatedModel() {
			// singleton instance
		}
	
		@Override
		protected Property<BreadcrumbData> modelProperty() {
			return MODEL_PROPERTY;
		}
	
	}

	/**
	 * Creates a new {@link GenericBreadcrumbDataOwner}.
	 */
	public GenericBreadcrumbDataOwner(Object reference, Mapping<Object, BreadcrumbData> algorithm) {
		super(reference, algorithm);
	}

	@Override
	public BreadcrumbData getBreadcrumbData() {
		return getModel();
	}

}

