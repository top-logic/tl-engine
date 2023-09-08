/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.security.ProtectedValueReplacement;

/**
 * {@link AbstractConfiguredFilter}, that is base class for filters, that use selection filters for
 * decision of filter value acceptance.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractSelectionBasedFilter<T extends SelectionFilterConfiguration> extends AbstractConfiguredFilter {

	private T _config;
	private final FilterViewBuilder<T> _viewBuilder;
	private final List<Class<?>> _supportedTypes;
	private ValueIterator _valueIterator;
	private Filter<? super Object> _defaultCombinedEvaluationFilter;
	private Filter<? super Object> _combinedEvaluationFilter;
	private Filter<? super Object> _ruleBasedFilter;
	private Filter<? super Object> _selectionBasedFilter;
	private MultiOptionMatchCounter _counter;
	private boolean _showNonMatchingOptions;

	private boolean _supportsBlockedValues = false;
	private Mapping<Object, ?> _valueMapping;

	/**
	 * Create a new {@link AbstractSelectionBasedFilter}.
	 */
	@SuppressWarnings("synthetic-access")
	protected AbstractSelectionBasedFilter(T config, FilterViewBuilder<T> view,
			List<Class<?>> supportedTypes, boolean showNonMatchingOptions,
			Filter<? super Object> ruleBasedFilter) {
		assert config != null : "Filter configuration must never be null!";
		assert view != null : "Filter view must never be null!";
		_config = config;
		_viewBuilder = view;
		_ruleBasedFilter = ruleBasedFilter;
		_selectionBasedFilter = new SelectionBasedFilter();
		_defaultCombinedEvaluationFilter =
			_combinedEvaluationFilter = FilterFactory.or(_selectionBasedFilter, ruleBasedFilter);
		_counter = DummyMultiOptionMatchCounter.INSTANCE;
		_showNonMatchingOptions = showNonMatchingOptions;
		if (config.hasCollectionValues()) {
			_supportedTypes = Collections.<Class<?>> singletonList(Collection.class);
			_valueIterator = new MultiValueIterator();
		} else {
			_supportedTypes = Collections.unmodifiableList(new ArrayList<>(supportedTypes));
			_valueIterator = new SingleValueIterator();
		}
		if (config.isUseRawOptions()) {
			_valueMapping = Mappings.identity();
		} else {
			_valueMapping = new DynamicLabelProviderMapping();
		}
	}

	@Override
	public FilterViewControl<?> getDisplayControl(DisplayContext context, FormGroup form, int filterControlId) {
		return _viewBuilder.createFilterViewControl(context, _config, form, filterControlId);
	}

	@Override
	public FilterConfiguration getFilterConfiguration() {
		return getTypedConfig();
	}

	/**
	 * {@link FilterConfiguration} casted to the specific type, which is appropriate for this
	 * {@link AbstractSelectionBasedFilter}
	 */
	protected final T getTypedConfig() {
		return _config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFilterConfiguration(FilterConfiguration filterConfig) {
		assert filterConfig != null : "Filter configuration must not be null!";
		_config = (T) filterConfig;
	}

	@Override
	public void startFilterRevalidation(boolean countableRevalidation) {
		if (selectionFilterActive() && !ruleBasedFilterActive()) {
			_combinedEvaluationFilter = _selectionBasedFilter;
		} else if (!selectionFilterActive() && ruleBasedFilterActive()) {
			_combinedEvaluationFilter = _ruleBasedFilter;
		} else if (!selectionFilterActive() && !ruleBasedFilterActive()) {
			_combinedEvaluationFilter = TrueFilter.INSTANCE;
		}
		if (_showNonMatchingOptions) {
			if (doCount(countableRevalidation)) {
				_counter = new DefaultMultiOptionMatchCounter();
			} else {
				_counter = new MultiEmptyValueOptionMatchCounter();
			}
		} else {
			if (doCount(countableRevalidation)) {
				_counter = new OptionCollectingMatchCounter();
			} else {
				_counter = new EmptyValueOptionCollectionMatchCounter();
			}
		}
	}

	private boolean doCount(boolean countableRevalidation) {
		return getTypedConfig().showOptionEntries() && countableRevalidation;
	}

	@Override
	public void stopFilterRevalidation() {
		_combinedEvaluationFilter = _defaultCombinedEvaluationFilter;
		_config.setOptions(_counter.getMatchCount());
		_counter = DummyMultiOptionMatchCounter.INSTANCE;
	}

	@Override
	public void count(Object value) {
		_valueIterator.setIterableValue(value);
		while (_valueIterator.hasNext()) {
			Object mappedValue = _valueMapping.map(_valueIterator.next());
			if (!StringServices.isEmpty(mappedValue)) {
				_counter.increaseCounter(mappedValue);
			}
		}
	}

	@Override
	public boolean accept(Object anObject) {
		if (StringServices.isEmpty(anObject)) {
			return false;
		}

		/* TODO: The AbstractSelectionBasedFilter returns often Object as supported types. This
		 * includes also subtypes. It is currently not possible to determine a list of types that
		 * are not supported. */
		if (anObject instanceof ProtectedValueReplacement && !supportsBlockedValues()) {
			return false;
		}


		boolean accept = false;
		_valueIterator.setIterableValue(anObject);
		while (_valueIterator.hasNext()) {
			Object mappedValue = _valueMapping.map(_valueIterator.next());
			if (StringServices.isEmpty(mappedValue)) {
				accept |= false;
			} else {
				_counter.markOption(mappedValue);
				accept |= _combinedEvaluationFilter.accept(mappedValue);
			}
		}

		return accept;
	}

	@Override
	public final List<Class<?>> getSupportedObjectTypes() {
		return _supportedTypes;
	}

	@Override
	public boolean isActive() {
		return ruleBasedFilterActive()
			|| selectionFilterActive();
	}

	/**
	 * true, if the rule based filter is active, false otherwise.
	 */
	protected abstract boolean ruleBasedFilterActive();

	private boolean selectionFilterActive() {
		return !_config.getFilterPattern().isEmpty();
	}

	@Override
	public void clearFilterConfiguration() {
		_config.clearConfiguration();
	}

	/**
	 * Whether values that must not be seen by the user for security reasons are supported by this
	 * filter.
	 */
	public boolean supportsBlockedValues() {
		return _supportsBlockedValues;
	}

	/**
	 * Setter for {@link #supportsBlockedValues()}.
	 */
	public void setSupportsBlockedValues(boolean supportsBlockedValues) {
		_supportsBlockedValues = supportsBlockedValues;
	}

	private class SelectionBasedFilter implements Filter<Object> {

		@Override
		public boolean accept(Object anObject) {
			return getTypedConfig().matchesFilterPattern(anObject);
		}

	}

	private interface ValueIterator extends Iterator<Object> {

		void setIterableValue(Object iterableValue);
	}

	private static class SingleValueIterator implements ValueIterator {

		private Object _value;

		@Override
		public boolean hasNext() {
			return _value != null;
		}

		@Override
		public Object next() {
			if (hasNext()) {
				Object value = _value;
				_value = null;
				return value;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}


		@Override
		public void setIterableValue(Object iterableValue) {
			_value = iterableValue;
		}

	}

	private static class MultiValueIterator implements ValueIterator {

		private Iterator<?> _collectionIterator;

		@Override
		public boolean hasNext() {
			return _collectionIterator.hasNext();
		}

		@Override
		public Object next() {
			return _collectionIterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setIterableValue(Object iterableValue) {
			_collectionIterator =
				iterableValue == null ? Collections.emptyListIterator() : ((Collection<?>) iterableValue).iterator();
		}
	}

	private class DynamicLabelProviderMapping implements Mapping<Object, String> {

		@Override
		public String map(Object input) {
			String rawLabel = getTypedConfig().getOptionLabelProvider().getLabel(input);
			if (rawLabel != null) {
				return rawLabel.trim();
			} else {
				return rawLabel;
			}
		}

	}
}