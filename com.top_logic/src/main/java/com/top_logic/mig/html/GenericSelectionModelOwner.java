/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwner;

/**
 * {@link GenericModelOwner} for {@link SelectionModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericSelectionModelOwner extends GenericModelOwner<SelectionModel> implements SelectionModelOwner {

	/**
	 * Algorithm to find {@link SelectionModel} as annotated model.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class AnnotatedModel extends GenericModelOwner.AbstractAnnotatedModel<SelectionModel> {

		/** Singleton {@link AnnotatedModel} instance. */
		public static final AnnotatedModel INSTANCE = new AnnotatedModel();

		private static final Property<SelectionModel> MODEL_PROPERTY =
			TypedAnnotatable.property(SelectionModel.class, "model");

		private AnnotatedModel() {
			// singleton instance
		}

		@Override
		protected Property<SelectionModel> modelProperty() {
			return MODEL_PROPERTY;
		}

	}

	/**
	 * Creates a new {@link GenericSelectionModelOwner}.
	 */
	public GenericSelectionModelOwner(Object reference, Mapping<Object, SelectionModel> algorithm) {
		super(reference, algorithm);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return getModel();
	}

}

