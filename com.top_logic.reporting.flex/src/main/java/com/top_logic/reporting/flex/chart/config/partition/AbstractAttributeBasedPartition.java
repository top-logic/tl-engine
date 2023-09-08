/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ClassificationCriterion;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;
import com.top_logic.reporting.flex.chart.config.util.ToStringText.NotSetText;

/**
 * Base-class for {@link PartitionFunction}
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public abstract class AbstractAttributeBasedPartition implements PartitionFunction,
		ConfiguredInstance<AbstractAttributeBasedPartition.Config> {

	private static final class LabelKeyComparable implements Comparable<LabelKeyComparable> {
		private final String _label;

		public LabelKeyComparable(Object val) {
			String label = MetaLabelProvider.INSTANCE.getLabel(val);
			_label = label == null ? "-" : label;
		}

		@Override
		public int compareTo(LabelKeyComparable o) {
			return _label.compareTo(o._label);
		}

		@Override
		public String toString() {
			return _label;
		}

		@Override
		public int hashCode() {
			return _label.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof LabelKeyComparable)) {
				return false;
			}
			LabelKeyComparable other = (LabelKeyComparable) obj;
			return _label.equals(other._label);
		}
	}

	public interface ValueProvider {

		public Object getValue(TLObject att, AbstractAttributeBasedPartition function);
	}

	public static class DefaultValueProvider implements ValueProvider {

		public static DefaultValueProvider INSTANCE = new DefaultValueProvider();

		@Override
		public Object getValue(TLObject att, AbstractAttributeBasedPartition function) {
			return att.tValueByName(function.getMetaAttributeName());
		}
	}

	/**
	 * Base-config-interface for {@link AbstractAttributeBasedPartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		/**
		 * Property name of {@link #getMetaAttribute()}.
		 */
		String META_ATTRIBUTE = "meta-attribute";

		/**
		 * Property name of {@link #getAddEmpty()}.
		 */
		String ADD_EMPTY = "add-empty";

		/**
		 * Indirect getter for the {@link TLStructuredTypePart} to prevent problems during startup.
		 * 
		 * @return a {@link Provider} for the {@link TLStructuredTypePart} this {@link PartitionFunction}
		 *         is about
		 */
		@Name(META_ATTRIBUTE)
		@Hidden
		public MetaAttributeProvider getMetaAttribute();

		/**
		 * see {@link #getMetaAttribute()}
		 */
		public void setMetaAttribute(MetaAttributeProvider ma);

		/**
		 * Whether elements with empty value for the attribute ({@link #getMetaAttribute()}) should
		 * be added to a special empty-partition.
		 * 
		 * <p>
		 * By default, objects with empty values are ignored.
		 * </p>
		 */
		@Name(ADD_EMPTY)
		@BooleanDefault(false)
		public boolean getAddEmpty();

		/**
		 * see {@link #getAddEmpty()}
		 */
		public void setAddEmpty(boolean addEmpty);

		@Hidden
		@InstanceFormat
		@InstanceDefault(DefaultValueProvider.class)
		public ValueProvider getValueProvider();

		public void setValueProvider(ValueProvider provider);
	}

	private final Config _config;

	private Criterion _criterion = null;

	private NotSetText _notSetText;

	/**
	 * Config-constructor for {@link AbstractAttributeBasedPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractAttributeBasedPartition(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	private MetaAttributeProvider getMetaAttributeProvider() {
		MetaAttributeProvider map = getConfig().getMetaAttribute();
		return map;
	}

	/**
	 * Shortcut to get configured {@link TLStructuredTypePart}
	 */
	protected TLStructuredTypePart getMetaAttribute() {
		return getMetaAttributeProvider().get();
	}

	/**
	 * Shortcut to get configured {@link TLStructuredTypePart}
	 */
	protected TLStructuredTypePart getMetaAttribute(Object obj) {
		return getMetaAttributeProvider().getMetaAttribute(obj);
	}

	/**
	 * Shortcut to get configured {@link TLStructuredTypePart}
	 */
	protected String getMetaAttributeName() {
		return getMetaAttributeProvider().getMetaAttributeName();
	}

	/**
	 * Shortcut to get the {@link FastList} in case of classification-meta-attributes.
	 */
	protected FastList getFastList() {
		return (FastList) getMetaAttribute().getType();
	}

	/**
	 * Convenience-method to get the elements of a {@link FastList}
	 */
	protected List<FastListElement> getFastListElements(FastList fastList) {
		return fastList.elements();
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {

		List<Partition> result = new ArrayList<>();

		Map<Object, Partition> map = new LinkedHashMap<>();
		initOptions(aParent, map);

		List<TLObject> objects = CollectionUtil.dynamicCastView(TLObject.class, aParent.getObjects());

		handleObjects(aParent, map, objects);

		result.addAll(map.values());
		sortResult(result);
		return result;
	}

	/**
	 * Hook for subclasses
	 */
	protected void handleObjects(Partition aParent, Map<Object, Partition> map, List<TLObject> objects) {
		for (TLObject att : objects) {
			handleObject(aParent, map, att);
		}
	}

	/**
	 * Hook for subclasses
	 */
	protected void handleObject(Partition aParent, Map<Object, Partition> map, TLObject obj) {
		Object value = getValue(obj);
		if (isEmpty(value)) {
			if (getConfig().getAddEmpty()) {
				handleEmpty(aParent, map, obj);
			}
		}
		else {
			handleValue(aParent, map, obj, value);
		}
	}

	/**
	 * Handle the not-empty value and add the attributed to the proper partition
	 */
	protected abstract void handleValue(Partition aParent, Map<Object, Partition> map, TLObject att, Object value);

	/**
	 * Handle the empty value and add the attributed to the proper partition
	 */
	protected abstract void handleEmpty(Partition aParent, Map<Object, Partition> map, TLObject att);

	/**
	 * @param value
	 *        the value to check
	 * @return true if value is null or an empty string or an empty collection.
	 */
	protected boolean isEmpty(Object value) {
		if (isCollectionValued()) {
			return CollectionUtil.isEmptyOrNull((Collection<?>) value);
		}
		else {
			return StringServices.isEmpty(value);
		}
	}

	/**
	 * true if the meta-attribute this partition-function is about returns multi-valued
	 *         data. Attention: A single-valued classification-attribute will return false, even if
	 *         the result is a collection.
	 */
	protected boolean isCollectionValued() {
		return getMetaAttribute().isMultiple();
	}

	/**
	 * Hook to sort the list of {@link Partition}s created by this function.
	 * 
	 * @param result
	 *        the list to sort
	 */
	protected void sortResult(List<Partition> result) {
	}

	/**
	 * Convenience-method to get a value for the configured attribute of a given input.
	 * 
	 * @param att
	 *        the input-object to get the value for
	 * @return the attribute-value for the given input
	 */
	protected Object getValue(TLObject att) {
		return getConfig().getValueProvider().getValue(att, this);
	}

	/**
	 * Hook to create a key for a given input that can be used to determine the partition.
	 * 
	 * @param val
	 *        the input to get a key for
	 * @return the input object, in case of historic wrappers the current-version is returned, if it
	 *         exists
	 */
	protected Comparable<?> getKey(Object val) {
		if (val instanceof Wrapper) {
			if (!WrapperHistoryUtils.isCurrent((Wrapper) val)) {
				Wrapper wrapper = WrapperHistoryUtils.getWrapper(Revision.CURRENT, (Wrapper) val);
				if (wrapper != null) {
					val = wrapper;
				}
			}
		}
		else if (!(val instanceof Comparable<?>)) {
			return new LabelKeyComparable(val);
		}
		return (Comparable<?>) val;
	}

	/**
	 * Hook to init the expected partitions if possible
	 */
	protected void initOptions(Partition aParent, Map<Object, Partition> map) {

		List<Object> keys = getOptions(aParent);
		for (Object obj : keys) {
			addValue(aParent, map, getKey(obj), null);
		}
		if (getConfig().getAddEmpty()) {
			addValue(aParent, map, getNotSetText(), null);
		}
	}

	/**
	 * @param aParent
	 *        the parent to get the expected sub-partition-keys for
	 * @return a list of expected partition-keys. May be empty
	 */
	protected List<Object> getOptions(Partition aParent) {
		return Collections.emptyList();
	}

	/**
	 * Lazy getter for a not-set-text for the current {@link TLStructuredTypePart}
	 * 
	 * @return the key to use if a partition for empty-values should be added
	 */
	protected Comparable<?> getNotSetText() {
		if (_notSetText == null) {
			_notSetText = new NotSetText(getMetaAttribute());
		}
		return _notSetText;
	}

	/**
	 * Adds the given attributed to a {@link Partition} where the given value is the key. Partitions
	 * are lazily added if necessary.
	 * 
	 * @param map
	 *        the result-map with the partitions mapped by their keys.
	 * @param value
	 *        the value determining the partition
	 * @param obj
	 *        the object to add to the partition
	 */
	protected void addValue(Partition aParent, Map<Object, Partition> map, Comparable<?> value, TLObject obj) {
		Partition partition = map.get(value);
		if (partition == null) {
			partition = new Partition(aParent, value, new ArrayList<>());
			map.put(value, partition);
		}
		if (obj != null) {
			partition.getObjects().add(obj);
		}
	}

	@Override
	public Criterion getCriterion() {
		if (_criterion == null) {
			_criterion = initCriterion();
		}
		return _criterion;
	}

	/**
	 * Hook to init the {@link Criterion}.
	 * 
	 * @return see {@link #getCriterion()}
	 */
	protected Criterion initCriterion() {
		return new ClassificationCriterion(getConfig().getMetaAttribute().get());
	}

}
