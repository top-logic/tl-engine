/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.mig.html.GenericSelectionModelOwner;

/**
 * Superclass for generic owner of models. It
 * 
 * <p>
 * In contrast to other model owners a {@link GenericModelOwner} is not itself the owner of the
 * model, but computes the model with a algorithm from a reference object that can be determined by
 * scripting framework.
 * </p>
 * 
 * <p>
 * This class is designed to be overridden by concrete owner classes.
 * </p>
 * 
 * @see GenericSelectionModelOwner
 * @see GenericModelOwnerNaming
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericModelOwner<T> {

	private final Object _reference;

	private final Mapping<Object, T> _algorithm;

	/**
	 * Creates a new {@link GenericModelOwner}.
	 * 
	 * @param reference
	 *        The object which can be resolved by the scripting framework.
	 * @param algorithm
	 *        The algorithm derive the actual model from the given reference object.
	 */
	public GenericModelOwner(Object reference, Mapping<Object, T> algorithm) {
		_reference = reference;
		_algorithm = algorithm;
	}

	/**
	 * Computes the model hold by this {@link GenericModelOwner}.
	 */
	protected T getModel() {
		return getAlgorithm().map(getReference());
	}

	/**
	 * The algorithm which is used by this {@link GenericModelOwner} to compute {@link #getModel()}.
	 */
	public Mapping<Object, T> getAlgorithm() {
		return _algorithm;
	}

	/**
	 * The reference object which is used by this {@link GenericModelOwner} to compute
	 * {@link #getModel()}.
	 */
	public Object getReference() {
		return _reference;
	}

	/**
	 * Algorithm to get model from a {@link TypedAnnotatable}.
	 * 
	 * <p>
	 * This algorithm can be used to create a {@link GenericModelOwner} when the model can be
	 * attached to a recordable {@link TypedAnnotatable}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected abstract static class AbstractAnnotatedModel<T> implements Mapping<Object, T> {

		@Override
		public T map(Object input) {
			return ((TypedAnnotatable) input).get(modelProperty());
		}

		/**
		 * Annotates the given {@link TypedAnnotatable} with the given model.
		 */
		public void annotate(TypedAnnotatable annotatable, T model) {
			annotatable.set(modelProperty(), model);
		}

		/**
		 * The {@link Property} that is used to find the model.
		 */
		protected abstract Property<T> modelProperty();

	}

}

