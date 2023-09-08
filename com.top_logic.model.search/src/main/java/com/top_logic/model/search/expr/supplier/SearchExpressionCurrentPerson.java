/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.supplier;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.util.TLContextManager;

/**
 * {@link SupplierSearchExpressionBuilder} for the current {@link Person}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchExpressionCurrentPerson extends SupplierSearchExpressionBuilder {

	/** {@link TypedConfiguration} constructor for {@link SearchExpressionCurrentPerson}. */
	public SearchExpressionCurrentPerson(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public TLObject getValue() {
		return TLContextManager.getSubSession().getPerson();
	}

	@Override
	public TLType getType() {
		return Person.getPersonType();
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

}
