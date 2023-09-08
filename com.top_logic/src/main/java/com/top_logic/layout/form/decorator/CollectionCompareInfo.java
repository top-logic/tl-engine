/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * Compare information for collections of objects.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CollectionCompareInfo extends CompareInfo {
	
	private final List<String> _oldTips;

	private final List<String> _newTips;

	private final ResourceProvider _provider;

	private Map<Object, Object> _baseValues;

	private Map<Object, Object> _changeValues;

	/**
	 * Creates a new {@link CollectionCompareInfo}.
	 */
	public CollectionCompareInfo(List<?> baseValue, List<?> changeValue, ResourceProvider resourceProvider) {
		super(baseValue, changeValue, MetaLabelProvider.INSTANCE);

		_oldTips = getTooltips(baseValue, resourceProvider);
		_newTips = getTooltips(changeValue, resourceProvider);
		_provider = resourceProvider;
	}
	
	private static List<String> getTooltips(Collection<?> someValues, final ResourceProvider resourceProvider) {
		if (CollectionUtil.isEmptyOrNull(someValues)) {
			return Collections.emptyList();
		} else {
			Mapping<Object, String> tooltipMapping = new Mapping<>() {

				@Override
				public String map(Object input) {
					String tooltip = resourceProvider.getTooltip(input);
					if (tooltip != null) {
						return tooltip;
					}
					return resourceProvider.getLabel(input);
				}
				
			};
			return Mappings.map(tooltipMapping, someValues);
		}
	}

	@Override
	public List<?> getBaseValue() {
		return (List<?>) super.getBaseValue();
	}

	@Override
	public List<?> getChangeValue() {
		return (List<?>) super.getChangeValue();
	}

	@Override
	public String getBaseValueTooltip() {
		return toString(_oldTips);
	}

	@Override
	public String getChangeValueTooltip() {
		return toString(_newTips);
	}

	@Override
	protected ChangeInfo createChangeInfo() {
		List<?> oldValue = CollectionUtil.nonNull(getBaseValue());
		List<?> newValue = CollectionUtil.nonNull(getChangeValue());

		ChangeInfo info;
		// TODO #20539: Care for order of values.
		if (sameLabels(oldValue, newValue)) {
			info = compareTooltips();
		} else if (oldValue.isEmpty()) {
			info = ChangeInfo.CREATED;
		} else if (newValue.isEmpty()) {
			info = ChangeInfo.REMOVED;
		} else {
			info = ChangeInfo.CHANGED;
		}
		return info;
	}

	/**
	 * Find the matching old value for the given new one.
	 * 
	 * @param newValue
	 *        The new object to get the old one for.
	 * @return The requested old object.
	 */
	public Object findOldValue(Object newValue) {
		return getBaseValueMap().get(identifier(newValue));
	}

	/**
	 * Find the matching new value for the given old one.
	 * 
	 * @param oldValue
	 *        The old object to get the new one for.
	 * @return The requested new object.
	 */
	public Object findNewValue(Object oldValue) {
		return getChangeValueMap().get(identifier(oldValue));
	}

	/**
	 * Return the used resource provider.
	 * 
	 * @return The resource provider provided to this instance.
	 */
	public ResourceProvider getResourceProvider() {
		return _provider;
	}

	private boolean sameLabels(List<?> oldValue, List<?> newValue) {
		switch (oldValue.size()) {
			case 0:
				return newValue.isEmpty();
			case 1:
				if( newValue.size() != 1) {
					return false;
				}
				String oldLabel = _provider.getLabel(oldValue.get(0));
				String newLabel = _provider.getLabel(newValue.get(0));
				return StringServices.equals(oldLabel, newLabel);
			default:
				break;
		}
		return toStringSet(oldValue).equals(toStringSet(newValue));
	}

	private Set<String> toStringSet(List<?> someValues) {
		switch (someValues.size()) {
			case 0:
				return Collections.emptySet();
			case 1:
				return Collections.singleton(_provider.getLabel(someValues.get(0)));
			default:
				Set<String> labels = new HashSet<>();
				for (Object value : someValues) {
					labels.add(_provider.getLabel(value));
				}
				return labels;
		}
	}

	private Map<Object, Object> getBaseValueMap() {
		if (_baseValues == null) {
			_baseValues = indexByID(getBaseValue());
		}
		return _baseValues;
	}

	private Map<Object, Object> getChangeValueMap() {
		if (_changeValues == null) {
			_changeValues = indexByID(getChangeValue());
		}
		return _changeValues;
	}

	private Map<Object, Object> indexByID(Collection<?> someValues) {
		return MapUtil.mapValuesInto(new HashMap<>(), CompareInfo.identifierMapping(), someValues);
	}

	private ChangeInfo compareTooltips() {
		if (CollectionUtil.containsSame(_oldTips, _newTips)) {
			return ChangeInfo.NO_CHANGE;
		} else {
			return ChangeInfo.DEEP_CHANGED;
		}
	}

	private String toString(List<?> someObjects) {
		return StringServices.toString(someObjects, "<br />");
	}
}
