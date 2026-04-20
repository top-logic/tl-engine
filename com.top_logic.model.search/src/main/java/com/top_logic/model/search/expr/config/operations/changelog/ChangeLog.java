/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.changelog;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.changelog.SubtreeFilter;
import com.top_logic.element.changelog.model.TlChangelogFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.util.model.ModelService;

/**
 * TL-Script function {@code changeLog([obj [, maxEntries [, includeSubtree]]])}.
 *
 * <p>
 * Returns the list of {@code tl.changelog:ChangeSet}s that affect the given object. When
 * {@code includeSubtree} is {@code true} (default), the entire composition subtree rooted at
 * {@code obj} is considered; when {@code false}, only changes to {@code obj} itself.
 * </p>
 *
 * <p>
 * When {@code obj} is {@code null} (or not given), the global application change log is returned
 * without any subtree filter. Combine with {@code maxEntries} to keep the result size manageable.
 * </p>
 *
 * <p>
 * {@code maxEntries} limits the number of returned entries, newest first. A value {@code <= 0}
 * (the default) means unlimited.
 * </p>
 */
public class ChangeLog extends GenericMethod {

	/**
	 * Creates a {@link ChangeLog}.
	 */
	protected ChangeLog(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ChangeLog(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TlChangelogFactory.getChangeSetType();
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject obj = arguments[0] == null ? null : asTLObject(arguments[0]);

		int maxEntries = arguments.length > 1 && arguments[1] != null ? asInt(arguments[1]) : 0;
		boolean includeSubtree = arguments.length > 2 && arguments[2] != null ? asBoolean(arguments[2]) : true;

		KnowledgeBase kb = obj != null ? obj.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		ChangeLogBuilder builder = new ChangeLogBuilder(kb, ModelService.getApplicationModel());
		if (obj != null) {
			builder.setFilter(new SubtreeFilter(obj, includeSubtree));
		}
		if (maxEntries > 0) {
			builder.setNumberEntries(maxEntries);
		}
		return (Collection<?>) builder.build();
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ChangeLog}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ChangeLog> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("object")
			.optional("maxEntries")
			.optional("includeSubtree")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ChangeLog build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new ChangeLog(getConfig().getName(), args);
		}
	}

}
