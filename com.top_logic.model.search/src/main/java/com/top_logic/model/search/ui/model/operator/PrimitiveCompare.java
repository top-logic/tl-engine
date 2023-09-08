/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.search.ui.model.structure.InheritedContextType;
import com.top_logic.util.model.ModelService;

/**
 * {@link Operator} for comparing {@link TLPrimitive}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@UseTemplate(ResourceDisplay.class)
@Abstract
public interface PrimitiveCompare<I extends Operator.Impl<?>> extends Operator<I>, InheritedContextType {

	/**
	 * Function for implementing {@link PrimitiveCompare#getValueType()}.
	 */
	abstract class PrimitiveType extends Function0<TLType> {

		/**
		 * The type {@link com.top_logic.model.TLPrimitive.Kind} the {@link PrimitiveCompare}
		 * operates on.
		 */
		protected abstract TLPrimitive.Kind getKind();

		@Override
		public final TLType apply() {
			TLModel model = ModelService.getApplicationModel();
			return TLCore.getPrimitiveType(model, getKind());
		}

	}

	@Override
	boolean getValueMultiplicity();

}
