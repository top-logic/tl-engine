/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Service that provides {@link BookmarkHandler} for different {@link TLType}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies(ModelService.Module.class)
public class BookmarkService extends KBBasedManagedClass<BookmarkService.Config> {

	/**
	 * Typed configuration interface definition for {@link BookmarkService}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends KBBasedManagedClass.Config<BookmarkService> {

		/**
		 * Default {@link BookmarkHandler} that is used for an object when no special handler is
		 * configured for object's type.
		 */
		@Mandatory
		PolymorphicConfiguration<BookmarkHandler> getDefaultBookmarkHandler();

		/**
		 * {@link BookmarkHandler} for identified by the type, the handler is responsible for.
		 * 
		 * <p>
		 * When a {@link BookmarkHandler} is requested for some type the handler configured for the
		 * nearest generalisation of that type is returned.
		 * </p>
		 */ 
		@Key(BookmarkHandlerConfig.TYPE_SPEC)
		Map<String, BookmarkHandlerConfig> getBookmarkHandlers();
	}

	/**
	 * Configuration of a {@link BookmarkHandler} for some {@link TLType}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface BookmarkHandlerConfig extends TypeRef {

		/**
		 * The {@link BookmarkHandler} used for {@link #getTypeSpec()}.
		 */
		@Mandatory
		PolymorphicConfiguration<BookmarkHandler> getImpl();

	}

	private BookmarkHandler _defaultHandler;

	private Map<TLType, BookmarkHandler> _handlerByType;

	/**
	 * Create a {@link BookmarkService}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create the new object in.
	 * @param config
	 *        The configuration object to be used for instantiation.
	 */
	public BookmarkService(InstantiationContext context, Config config) {
		super(context, config);
		_defaultHandler = context.getInstance(config.getDefaultBookmarkHandler());
		_handlerByType = getSpecialHandlers(context, config);
	}

	private LinkedHashMap<TLType, BookmarkHandler> getSpecialHandlers(InstantiationContext context, Config config) {
		Map<TLType, BookmarkHandler> unorderedProvider = resolveConfiguredHandlers(context, config);
		return orderTopologically(unorderedProvider);
	}

	private LinkedHashMap<TLType, BookmarkHandler> orderTopologically(Map<TLType, BookmarkHandler> handlers) {
		List<TLType> orderedTypes = CollectionUtilShared.topsort(this::specializations, handlers.keySet(), false);
		LinkedHashMap<TLType, BookmarkHandler> sortedHandlers = new LinkedHashMap<>();
		for (TLType type : orderedTypes) {
			sortedHandlers.put(type, handlers.get(type));
		}
		return sortedHandlers;
	}

	private Map<TLType, BookmarkHandler> resolveConfiguredHandlers(InstantiationContext context, Config config) {
		Map<TLType, BookmarkHandler> handlers = new HashMap<>();
		for (BookmarkHandlerConfig tmp : config.getBookmarkHandlers().values()) {
			TLType type;
			TLObject resolvedPart = TLModelUtil.resolveQualifiedName(tmp.getTypeSpec());
			if (resolvedPart instanceof TLType) {
				type = (TLType) resolvedPart;
			} else {
				context.error("Configured part is not a type: " + resolvedPart);
				continue;
			}
			handlers.put(type, context.getInstance(tmp.getImpl()));
		}
		return handlers;
	}

	private Collection<? extends TLType> specializations(TLType generalization) {
		if (generalization instanceof TLClass) {
			return ((TLClass) generalization).getSpecializations();
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@link BookmarkHandler} to use when no specialised handler can be found.
	 */
	public BookmarkHandler getDefaultHandler() {
		return _defaultHandler;
	}

	/**
	 * All known specialised {@link BookmarkHandler}.
	 */
	public Collection<BookmarkHandler> getSpecialisedHandlers() {
		return _handlerByType.values();
	}

	/**
	 * {@link BookmarkHandler} responsible for the given item.
	 */
	public BookmarkHandler getBookmarkHandler(TLObject item) {
		return getBookmarkHandler(item.tType());
	}

	/**
	 * {@link BookmarkHandler} responsible for the given type.
	 */
	public BookmarkHandler getBookmarkHandler(TLType type) {
		BookmarkHandler bookmarkProvider = _handlerByType.get(type);
		if (bookmarkProvider != null) {
			return bookmarkProvider;
		}
		for (Entry<TLType, BookmarkHandler> configuredBookmark : _handlerByType.entrySet()) {
			if (TLModelUtil.isCompatibleType(configuredBookmark.getKey(), type)) {
				return configuredBookmark.getValue();
			}
		}
		return getDefaultHandler();
	}

	/**
	 * Access to the single {@link BookmarkService} instance.
	 */
	public static BookmarkService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link BookmarkService}.
	 */
	public static final class Module extends TypedRuntimeModule<BookmarkService> {

		/**
		 * Singleton {@link BookmarkService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<BookmarkService> getImplementation() {
			return BookmarkService.class;
		}
	}

}

