/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.filter;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Defines which {@link TLModelPart parts of the model} which should be filtered out.
 * <p>
 * There are multiple reasons for filtering the model: Hiding security relevant information, hiding
 * parts of the model from the customer for business reasons, hiding technical necessary types that
 * add no value to the search, hiding technical data structures that the user won't understand, etc.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ModelFilter<C extends ModelFilter.Config<?>> implements ConfiguredInstance<C> {

	/**
	 * The {@link TypedConfiguration} of the {@link ModelFilter}.
	 */
	public interface Config<T extends ModelFilter<?>> extends PolymorphicConfiguration<T> {

		/** Property name of {@link #getWhitelist()}. */
		String WHITELIST = "whitelist";

		/** Property name of {@link #getBlacklist()}. */
		String BLACKLIST = "blacklist";

		/**
		 * {@link TLModelPart}s matched by one of these filters are displayed, even if they are
		 * {@link #getBlacklist() blacklisted}.
		 * <p>
		 * A {@link TLModelPart} that is matched by no filter and neither the blacklist nor the
		 * whitelist, it is displayed. Use the {@link #getBlacklist() blacklist} to define which
		 * parts of the model should not be displayed. Use the {@link #getWhitelist()} to define
		 * exceptions that should be displayed, even though they are matched by the blacklist.
		 * </p>
		 */
		@InstanceFormat
		@EntryTag("filter")
		@Name(WHITELIST)
		List<TypedFilter> getWhitelist();

		/**
		 * {@link TLModelPart}s matched by one of these filters are not displayed, <b>unless there
		 * is a matching whitelist entry</b>.
		 * 
		 * @see #getWhitelist()
		 */
		@InstanceFormat
		@EntryTag("filter")
		@Name(BLACKLIST)
		List<TypedFilter> getBlacklist();

	}

	private final C _config;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ModelFilter}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ModelFilter(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	/**
	 * Filters which {@link TLModelPart}s should be displayed.
	 * 
	 * @param modelParts
	 *        Is allowed to be null. Is not allowed to contain null.
	 * @return A modifiable and resizable {@link List} containing the model parts that should be
	 *         displayed.
	 */
	public <T extends TLModelPart> List<T> filterModel(Collection<? extends T> modelParts) {
		List<T> result = new ArrayList<>();
		for (T modelPart : nonNull(modelParts)) {
			if (Logger.isDebugEnabled(ModelFilter.class)) {
				logFilterResult(modelPart);
			}
			if (isBlacklisted(modelPart) && !isWhitelisted(modelPart)) {
				continue;
			}
			result.add(modelPart);
		}
		return result;
	}

	/**
	 * Logs whether a {@link TLModelPart} is accepted or rejected.
	 * <p>
	 * Is called if debug logging is enabled.
	 * </p>
	 * 
	 * @param modelPart
	 *        Is not allowed to be null.
	 */
	protected void logFilterResult(TLModelPart modelPart) {
		String message = "Filtering " + TLModelUtil.qualifiedName(modelPart) + " for the search."
			+ " Blacklisted: " + isBlacklisted(modelPart) + ", Whitelisted: " + isWhitelisted(modelPart);
		Logger.debug(message, ModelFilter.class);
	}

	/**
	 * Is the {@link TLModelPart} {@link Config#getBlacklist() blacklisted}?
	 * 
	 * @param modelPart
	 *        Is not allowed to be null.
	 */
	protected boolean isBlacklisted(TLModelPart modelPart) {
		return isMatched(modelPart, getConfig().getBlacklist());
	}

	/**
	 * Is the {@link TLModelPart} {@link Config#getWhitelist() whitelisted}?
	 * 
	 * @param modelPart
	 *        Is not allowed to be null.
	 */
	protected boolean isWhitelisted(TLModelPart modelPart) {
		return isMatched(modelPart, getConfig().getWhitelist());
	}

	/**
	 * Is the {@link TLModelPart} {@link TypedFilter#matches(Object)} by the given
	 * {@link TypedFilter}s?
	 * 
	 * @param modelPart
	 *        Is not allowed to be null.
	 * @param filters
	 *        Is allowed to be null. Is not allowed to contain null.
	 */
	protected boolean isMatched(TLModelPart modelPart, List<TypedFilter> filters) {
		for (TypedFilter filter : nonNull(filters)) {
			if (!filter.getType().isInstance(modelPart)) {
				continue;
			}
			FilterResult filterResult = filter.matches(modelPart);
			if (filterResult == FilterResult.TRUE) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add(Config.BLACKLIST, getConfig().getBlacklist())
			.add(Config.WHITELIST, getConfig().getWhitelist())
			.build();
	}

}
