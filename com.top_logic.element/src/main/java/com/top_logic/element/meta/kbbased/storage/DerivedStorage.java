/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.element.config.algorithm.GenericAlgorithmConfig;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.MetaAttributeAlgorithm;
import com.top_logic.element.meta.expr.ExpressionEvaluationAlgorithm;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AtomicStorage} that computes its values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DerivedStorage<C extends DerivedStorage.Config<?>> extends AbstractDerivedStorage<C> {

	/**
	 * Configuration options for {@link DerivedStorage}.
	 */
	@TagName("derived-storage")
	public interface Config<I extends DerivedStorage<?>> extends AbstractDerivedStorage.Config<I> {

		/** Property name of {@link #getAlgorithm()}. */
		String ALGORITHM = "algorithm";

		/**
		 * The algorithm to compute attribute values with.
		 */
		@Name(ALGORITHM)
		@ItemDefault(ExpressionEvaluationAlgorithm.Config.class)
		@ImplementationClassDefault(ExpressionEvaluationAlgorithm.class)
		@DefaultContainer
		GenericAlgorithmConfig getAlgorithm();

		/** @see #getAlgorithm() */
		void setAlgorithm(GenericAlgorithmConfig value);

	}

	private final MetaAttributeAlgorithm _algorithm;

	/**
	 * Creates a {@link DerivedStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DerivedStorage(InstantiationContext context, C config) {
		super(context, config);
		_algorithm = context.getInstance(config.getAlgorithm());
	}

	/**
	 * @see Config#getAlgorithm()
	 */
	public MetaAttributeAlgorithm getAlgorithm() {
		return _algorithm;
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		if (_algorithm != null) {
			return _algorithm.calculate((Wrapper) object);
		}
		throw new AttributeException("No algorithm defined for calculating attribute '" + attribute + "'.");
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject self, TLReference reference) {
		if (_algorithm != null) {
			return _algorithm.getReferers(self);
		}
		throw new AttributeException("No algorithm defined for calculating referers of '" + reference + "'.");
	}

}
