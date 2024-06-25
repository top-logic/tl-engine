/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link BookmarkHandler} that can be implemented with <em>TLScript</em>.
 */
@InApp
public class BookmarkHandlerByExpression extends AbstractConfiguredInstance<BookmarkHandlerByExpression.Config<?>>
		implements BookmarkHandler {

	/**
	 * Configuration options for {@link BookmarkHandlerByExpression}.
	 */
	@TagName("scripted-bookmark-handler")
	public interface Config<I extends BookmarkHandlerByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Function creating URL arguments that identify the given object.
		 * 
		 * <p>
		 * The function receives the object that should be linked as single argument and must return
		 * a map of URL parameter/value pairs that identify the given object.
		 * </p>
		 * 
		 * <p>
		 * The given {@link #getLinkResolver()} function must be able to resolve the given object
		 * when the returned argument map is passed to it.
		 * </p>
		 */
		@Mandatory
		Expr getLinkGenerator();

		/**
		 * Function resolving an object from a map of URL arguments (parameter value pairs).
		 * 
		 * <p>
		 * The function receives a map of URL parameter value pairs. If those parameters were
		 * produced by the {@link #getLinkGenerator()}, the function is expected to return the
		 * object, for which the URL arguments were produced before. Otherwise, the function must
		 * return <code>null</code> to signal that it cannot retrieve objects for those arguments.
		 * </p>
		 */
		@Mandatory
		Expr getLinkResolver();

	}

	private QueryExecutor _linkGenerator;

	private QueryExecutor _linkResolver;

	/**
	 * Creates a {@link BookmarkHandlerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BookmarkHandlerByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_linkGenerator = QueryExecutor.compile(config.getLinkGenerator());
		_linkResolver = QueryExecutor.compile(config.getLinkResolver());
	}

	@Override
	public boolean appendIdentificationArguments(StringBuilder url, boolean first, Object targetObject) {
		Object id = _linkGenerator.execute(targetObject);
		if (!(id instanceof Map<?, ?>)) {
			throw new TopLogicException(I18NConstants.ERROR_LINK_GENERATOR_MUST_PRODUCE_MAP);
		}
		Map<?, ?> args = (Map<?, ?>) id;
		for (Entry<?, ?> arg : args.entrySet()) {
			url.append(first ? '?' : '&');
			first = false;

			url.append(URLEncoder.encode(arg.getKey().toString(), StandardCharsets.UTF_8));
			url.append("=");
			url.append(URLEncoder.encode(arg.getValue().toString(), StandardCharsets.UTF_8));
		}
		return first;
	}

	@Override
	public Object getBookmarkObject(Map<String, Object> someArguments) {
		return _linkResolver.execute(someArguments);
	}

}
