/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.changelog;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.changelog.SubtreeFilter;
import com.top_logic.element.changelog.model.TlChangelogFactory;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * TL-Script function
 * {@code changeLog([obj [, maxEntries [, includeSubtree [, startRev [, stopRev [, author [, includeTechnical [, excludedModules]]]]]]]])}.
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
 *
 * <p>
 * {@code startRev} and {@code stopRev} narrow the analyzed revision range. Each may be passed as
 * either a {@link Revision} object or as a numeric commit number. By default the full history
 * (revision {@code 1} up to the last revision) is analyzed.
 * </p>
 *
 * <p>
 * {@code author} restricts the result to changes committed by the given account. The argument may
 * be either a {@code tl.accounts:Person} or a contact (or any {@link TLObject} carrying an
 * {@code account} reference to a {@code tl.accounts:Person}). {@code null} (the default) produces
 * a system-wide change log without any author filter.
 * </p>
 *
 * <p>
 * {@code includeTechnical} (default {@code false}) controls whether change sets that represent a
 * purely technical change which does not reflect in the model are also reported.
 * </p>
 *
 * <p>
 * {@code excludedModules} is a collection of modules whose changes must not be regarded. Each
 * element may be either a {@link TLModule} reference or a module name as a string.
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

		int maxEntries = arguments[1] != null ? asInt(arguments[1]) : 0;
		boolean includeSubtree = arguments[2] != null ? asBoolean(arguments[2]) : true;
		boolean includeTechnical = arguments[6] != null ? asBoolean(arguments[6]) : false;

		KnowledgeBase kb = obj != null ? obj.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		HistoryManager hm = kb.getHistoryManager();

		Revision startRev = toRevision(hm, arguments[3]);
		Revision stopRev = toRevision(hm, arguments[4]);
		Person author = toAuthor(arguments[5]);
		Set<String> excludedModules = toModuleNames(arguments[7]);

		ChangeLogBuilder builder = new ChangeLogBuilder(kb, ModelService.getApplicationModel());
		if (obj != null) {
			builder.setFilter(new SubtreeFilter(obj, includeSubtree));
		}
		if (maxEntries > 0) {
			builder.setNumberEntries(maxEntries);
		}
		if (startRev != null) {
			builder.setStartRev(startRev);
		}
		if (stopRev != null) {
			builder.setStopRev(stopRev);
		}
		if (author != null) {
			builder.setAuthor(author);
		}
		builder.setIncludeTechnical(includeTechnical);
		if (!excludedModules.isEmpty()) {
			builder.setExcludedModules(excludedModules);
		}
		return builder.build();
	}

	private Revision toRevision(HistoryManager hm, Object value) {
		if (value == null) {
			return null;
		}
		Object arg = asSingleElement(value);
		if (arg == null) {
			return null;
		}
		if (arg instanceof Revision) {
			return (Revision) arg;
		}
		return hm.getRevision(asLong(arg));
	}

	private Person toAuthor(Object value) {
		if (value == null) {
			return null;
		}
		Object single = asSingleElement(value);
		if (single == null) {
			return null;
		}
		if (single instanceof Person person) {
			return person;
		}
		throw new TopLogicException(
			I18NConstants.ERROR_UNEXPECTED_TYPE__EXPECTED_VAL_EXPR.fill(Person.class, value, this));
	}

	private Set<String> toModuleNames(Object value) {
		if (value == null) {
			return Set.of();
		}
		Collection<?> coll = asCollection(value);
		if (coll.isEmpty()) {
			return Set.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (Object element : coll) {
			if (element == null) {
				continue;
			}
			if (element instanceof TLModule module) {
				result.add(module.getName());
			} else if (element instanceof CharSequence) {
				result.add(element.toString());
			} else {
				throw new TopLogicException(
					I18NConstants.ERROR_UNEXPECTED_TYPE__EXPECTED_VAL_EXPR.fill(TLModule.class, element, this));
			}
		}
		return result;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ChangeLog}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ChangeLog> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("object")
			.optional("maxEntries")
			.optional("includeSubtree")
			.optional("startRev")
			.optional("stopRev")
			.optional("author")
			.optional("includeTechnical")
			.optional("excludedModules")
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
