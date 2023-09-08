/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;

/**
 * {@link SecuritySelectionFilter} that caches the formerly computed {@link #accept(Object) accept}
 * values to reuse them.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CachingSecuritySelectionFilter extends SecuritySelectionFilter {

	/**
	 * Cache of {@link #accept(Object)}.
	 */
	private final Map<Object, Boolean> _cache = new HashMap<>();

	/**
	 * Creates a new {@link CachingSecuritySelectionFilter}.
	 */
	public CachingSecuritySelectionFilter(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public boolean accept(Object businessObject) {
		Boolean knownValue = _cache.get(key(businessObject));
		if (knownValue == null) {
			boolean accept = super.accept(businessObject);
			_cache.put(key(businessObject), Boolean.valueOf(accept));
			return accept;
		}
		return knownValue.booleanValue();
	}

	private Object key(Object businessObject) {
		if (businessObject instanceof TLObject) {
			return key((TLObject) businessObject);
		} else {
			return businessObject;
		}
	}

	private Object key(TLObject businessObject) {
		return KBUtils.getWrappedObjectKey(businessObject);
	}

	@Override
	protected void handleModelChangedEvent(Object model, Object changedBy) {
		super.handleModelChangedEvent(model, changedBy);
		_cache.remove(key(model));
	}

	@Override
	protected void handleModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		super.handleModelDeletedEvent(models, changedBy);
		for (TLObject deleted : models) {
			_cache.remove(key(deleted));
		}
	}


}

