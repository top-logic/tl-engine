/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link ObjectData} for a {@link DefaultSharedObject} instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SharedObjectData extends ObjectData {

	private final Map<String, Object> _properties = new HashMap<>();

	private final Map<String, Set<ObjectData>> _referrersByProperty = new HashMap<>();

	/**
	 * Creates a {@link SharedObjectData}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 */
	protected SharedObjectData(ObjectScope scope) {
		super(scope);
	}

	@Override
	public Object getDataRaw(String property) {
		return _properties.get(property);
	}

	@Override
	public Object setDataRaw(String property, Object rawValue) {
		Object oldValue = _properties.put(property, rawValue);

		if (oldValue instanceof SharedObjectData) {
			((SharedObjectData) oldValue).removeReferrer(this, property);
		}
		if (rawValue instanceof SharedObjectData) {
			((SharedObjectData) rawValue).addReferrer(this, property);
		}

		return oldValue;
	}

	/**
	 * The {@link ObjectData} that point to this instance in the given property.
	 * 
	 * @param property
	 *        The foreign property.
	 * @return All instances that have this instance assigned to the given property (as single
	 *         instance, not within lists or other complex data structures).
	 */
	public Collection<? extends ObjectData> getReferrers(String property) {
		return mkReferrers(property);
	}

	private void addReferrer(ObjectData referrer, String property) {
		mkReferrers(property).add(referrer);
	}

	private Set<ObjectData> mkReferrers(String property) {
		Set<ObjectData> referrers = _referrersByProperty.get(property);
		if (referrers == null) {
			referrers = new HashSet<>();
			_referrersByProperty.put(property, referrers);
		}
		return referrers;
	}

	private void removeReferrer(ObjectData referrer, String property) {
		Set<ObjectData> referrers = _referrersByProperty.get(property);
		if (referrers != null) {
			referrers.remove(referrer);
		}
	}

	@Override
	public Map<String, Object> properties() {
		return _properties;
	}

}
