/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.CollectionLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.model.Partition;

/**
 * {@link PartitionFunction} that partitions by multi-valued attributes.
 * 
 * @see SingleValuePartition
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MultiValuePartition extends AbstractAttributeBasedPartition {

	/**
	 * Config interface for {@link MultiValuePartition}.
	 */
	public interface Config extends AbstractAttributeBasedPartition.Config {

		@Override
		@ClassDefault(MultiValuePartition.class)
		public Class<? extends MultiValuePartition> getImplementationClass();

		/**
		 * Property name of {@link #getPartitionKind()}.
		 */
		public String PARTITION = "partition";

		/**
		 * Determines if the partitions should be based on existing values (
		 * {@link PartitionKind#VALUE}) or based on a fix list of identifying elements (
		 * {@link PartitionKind#CLASSIFICATION}).
		 * 
		 * @see PartitionKind
		 */
		@Name(PARTITION)
		public PartitionKind getPartitionKind();

		/**
		 * see {@link #getPartitionKind()}
		 */
		public void setPartitionKind(PartitionKind type);

	}

	/**
	 * Creates a {@link MultiValuePartition} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MultiValuePartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected void handleObject(Partition aParent, Map<Object, Partition> map, TLObject obj) {
		Collection<?> values = (Collection<?>) getValue(obj);
		if (CollectionUtil.isEmptyOrNull(values)) {
			if (getConfig().getAddEmpty()) {
				addValue(aParent, map, getNotSetText(), obj);
			}
		}
		else {
			switch (getConfig().getPartitionKind()) {
				case CLASSIFICATION:
					for (Object value : values) {
						addValue(aParent, map, getKey(value), obj);
					}
					break;
				case VALUE:
					addValue(aParent, map, getKey(values), obj);
					break;
				default:
					throw new UnsupportedOperationException();
			}

		}

	}

	@Override
	protected Comparable<?> getKey(Object val) {
		if (val instanceof Collection) {
			List list = new ArrayList((Collection) val);
			Collections.sort(list, ComparableComparator.INSTANCE);
			return new CollectionLabelProvider().getLabel(list);
		}
		return super.getKey(val);
	}

	@Override
	protected void handleValue(Partition aParent, Map<Object, Partition> map, TLObject att, Object value) {
		Collection<Object> coll = CollectionUtil.dynamicCastView(Object.class, (Collection<?>) value);
		switch (getConfig().getPartitionKind()) {
			case CLASSIFICATION:
				for (Object obj : coll) {
					addValue(aParent, map, getKey(obj), att);
				}
				break;
			case VALUE:
				addValue(aParent, map, getKey(value), att);
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	@Override
	protected void handleEmpty(Partition aParent, Map<Object, Partition> map, TLObject att) {
		addValue(aParent, map, getNotSetText(), att);
	}

}