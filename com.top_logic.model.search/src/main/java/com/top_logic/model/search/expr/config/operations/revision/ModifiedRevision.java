/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.changelog.LastChangeRevision;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.core.TlCoreFactory;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.changelog.ChangeLog;

/**
 * Generic method determining the revision in which a given {@link TLObject} was modified the last
 * time.
 * 
 * <p>
 * A modification of an object is a change of any of its stored attribute values. This includes
 * values stored outside the object's own row, such as references stored in link tables and
 * translations of internationalized attributes.
 * </p>
 * 
 * <p>
 * For a composition stored in a link table, adding a part to or removing a part from the
 * composition is a modification of the container, but a change of a part itself is not. For a
 * composition stored in the table of its parts (each part row references its container), every
 * change of a part is also a modification of the container. To find changes within a whole
 * composition subtree, see {@link ChangeLog}.
 * </p>
 * 
 * @implNote The Java entry point for the computation is {@link LastChangeRevision#of(TLObject)}.
 *           In contrast, {@link TLObject#tLastModificationDate()},
 *           {@link TLObject#tLastModificationTime()}, {@link TLObject#tLastModifier()} and the
 *           life-cycle attribute {@link LifecycleAttributes#MODIFIED} only reflect changes of the
 *           object's own row.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModifiedRevision extends GenericMethod {

	/**
	 * Creates a new {@link ModifiedRevision}.
	 */
	protected ModifiedRevision(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ModifiedRevision(getName(), arguments);
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
		return LastChangeRevision.of(tlObject);
	}

	/**
	 * Each time a value of an object is changed, the modified revision changes, so the value can not
	 * be determined at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return arguments[0] == null;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ModifiedRevision}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ModifiedRevision> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ModifiedRevision build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new ModifiedRevision(getConfig().getName(), args);
		}

	}

}

