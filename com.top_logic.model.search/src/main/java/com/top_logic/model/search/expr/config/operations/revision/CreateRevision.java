/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.LifecycleStorageCreated;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.core.TlCoreFactory;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;

/**
 * Generic method determining the create revision of a given {@link TLObject}, i.e. the revision in
 * which the object was created.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateRevision extends GenericMethod {

	/**
	 * Creates a new {@link CreateRevision}.
	 */
	protected CreateRevision(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CreateRevision(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TlCoreFactory.getRevisionType();
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject tlObject = asTLObject(arguments[0]);
		if (tlObject == null) {
			return null;
		}
		if (tlObject.tTransient()) {
			return Revision.CURRENT;
		}
		Revision createRevision = LifecycleStorageCreated.createRevision(tlObject.tHandle());
		if (createRevision == null) {
			return null;
		}
		return createRevision;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link CreateRevision}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CreateRevision> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public CreateRevision build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new CreateRevision(getConfig().getName(), args);
		}

	}

}

