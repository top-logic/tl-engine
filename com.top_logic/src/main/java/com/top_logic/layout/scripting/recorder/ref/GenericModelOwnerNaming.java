/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Super class for {@link ModelNamingScheme} for concrete subclasses of {@link GenericModelOwner}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class GenericModelOwnerNaming<T, M extends GenericModelOwner<T>, N extends GenericModelOwnerNaming.GenericModelName<T>>
		extends AbstractModelNamingScheme<M, N> {

	/**
	 * {@link ModelName} for a {@link GenericModelOwner}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface GenericModelName<T> extends ModelName {

		/** Property name of algorithm */
		public static final String ALGORITHM_PROPERTY = "algorithm";

		/**
		 * Reference to the owner which is used as input for the {@link #getAlgorithm()
		 * computation}.
		 */
		@Mandatory
		ModelName getReference();

		/**
		 * Setter for {@link #getReference()}.
		 */
		void setReference(ModelName owner);

		/**
		 * Algorithm that computes the model from the given {@link #getReference()}
		 */
		@Mandatory
		@Name(ALGORITHM_PROPERTY)
		PolymorphicConfiguration<? extends Mapping<Object, T>> getAlgorithm();

		/**
		 * Setter for {@link #getAlgorithm()}.
		 */
		@Name(ALGORITHM_PROPERTY)
		void setAlgorithm(PolymorphicConfiguration<? extends Mapping<Object, T>> algorithm);

		/**
		 * ResKey, attached to algorithm of {@link #getAlgorithm()}. Typically provided by
		 *         {@link ResKeyFromClass}, that is using algorithm's full qualified class name for
		 *         translation.
		 */
		@Derived(fun = ResKeyFromClass.class, args = @Ref({ ALGORITHM_PROPERTY,
			PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME }))
		ResKey getAlgorithmName();
	}

	@Override
	public M locateModel(ActionContext context, N name) {
		Object reference = context.resolve(name.getReference());
		Mapping<Object, T> algorithm =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(name.getAlgorithm());
		return createOwner(reference, algorithm);
	}

	/**
	 * Creates the concrete {@link GenericModelOwner} for the given reference and algorithm.
	 * 
	 * @param reference
	 *        see {@link GenericModelOwner#getReference()}
	 * @param algorithm
	 *        see {@link GenericModelOwner#getAlgorithm()}
	 */
	protected abstract M createOwner(Object reference, Mapping<Object, T> algorithm);

	@Override
	protected void initName(N name, M model) {
		name.setAlgorithm(getAlgorithmConfig(model));
		name.setReference(ModelResolver.buildModelName(model.getReference()));
	}

	@SuppressWarnings("unchecked")
	private PolymorphicConfiguration<? extends Mapping<Object, T>> getAlgorithmConfig(M model) {
		ConfigurationItem config = InstanceAccess.INSTANCE.getConfig(model.getAlgorithm());
		return (PolymorphicConfiguration<? extends Mapping<Object, T>>) config;
	}

}

