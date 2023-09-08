/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.supplier;

import java.util.Date;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link SupplierSearchExpressionBuilder} for the current date and time.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchExpressionNow extends SupplierSearchExpressionBuilder {

	/** {@link TypedConfiguration} constructor for {@link SearchExpressionNow}. */
	public SearchExpressionNow(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object getValue() {
		return new Date();
	}

	@Override
	public TLType getType() {
		return TLModelUtil.findType(TypeSpec.DATE_TIME_TYPE);
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

}
