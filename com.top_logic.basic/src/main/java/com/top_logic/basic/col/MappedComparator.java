/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;

/**
 * {@link Comparator} that compares objects according to some aspect
 * retrieved by a {@link Mapping} from the compared objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappedComparator<T, C> extends AbstractConfiguredInstance<MappedComparator.Config<T, C>>
		implements Comparator<T> {
	
	private final Comparator<? super C> valueOrder;
	private final Mapping<? super T, ? extends C> mapping;
	
	private final Comparator<? super T> globalOrder;

	/**
	 * {@link ConfigurationItem} of {@link MappedComparator}.
	 */
	public interface Config<T, C> extends PolymorphicConfiguration<MappedComparator<T, C>> {

		/** Property name of {@link #getValueOrder()} */
		public static final String VALUE_ORDER_ATTRIBUTE = "valueOrder";

		/** Property name of {@link #getMapping()} */
		public static final String MAPPING_ATTRIBUTE = "mapping";

		/** Property name of {@link #getGlobalOrder()} */
		public static final String GLOBAL_ORDER_ATTRIBUTE = "globalOrder";

		/**
		 * {@link Comparator}, that is used for comparison of output objects of given
		 *         {@link #getMapping()}. {@link ComparableComparator} is used as default.
		 */
		@InstanceFormat
		@InstanceDefault(ComparableComparator.class)
		@Name(VALUE_ORDER_ATTRIBUTE)
		Comparator<? super C> getValueOrder();

		/**
		 * Setter for {@link #getValueOrder()}.
		 */
		void setValueOrder(Comparator<? super C> order);

		/**
		 * {@link Mapping}, whose mapping output is used for comparison by
		 *         {@link #getValueOrder()}.
		 */
		@InstanceFormat
		@Mandatory
		@Name(MAPPING_ATTRIBUTE)
		Mapping<? super T, ? extends C> getMapping();

		/**
		 * Setter for {@link #getMapping()}.
		 */
		void setMapping(Mapping<? super T, ? extends C> mapping);

		/**
		 * Additional {@link Comparator}, that is used, when comparison of objects by
		 *         {@link #getValueOrder()} treats two objects as equal. Can be used to establish an
		 *         stable order, even for equal objects. {@link Equality} is used by default.
		 */
		@InstanceFormat
		@InstanceDefault(Equality.class)
		@Name(GLOBAL_ORDER_ATTRIBUTE)
		Comparator<? super T> getGlobalOrder();

		/**
		 * Setter for {@link #getGlobalOrder()}.
		 */
		void setGlobalOrder(Comparator<? super T> order);

	}

	/**
	 * Creates a {@link MappedComparator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MappedComparator(InstantiationContext context, Config<T, C> config) {
		super(context, config);
		this.valueOrder = config.getValueOrder();
		this.mapping = config.getMapping();
		this.globalOrder = config.getGlobalOrder();
	}

	/**
	 * Same as
	 * {@link MappedComparator#MappedComparator(Mapping, Comparator, Comparator)}
	 * without global order.
	 */
	public MappedComparator(Mapping<? super T, ? extends C> valueMapping, Comparator<? super C> valueOrder) {
		this(valueMapping, valueOrder, Equality.INSTANCE);
	}

	/**
	 * Creates a new {@link MappedComparator}.
	 * @param valueMapping
	 *        A {@link Mapping} to retrieve values to actually compare.
	 * @param valueOrder
	 *        The {@link Comparator} to compare the
	 *        {@link Mapping#map(Object) mapping results}.
	 * @param globalOrder
	 *        The order used for comparison, if the given value order is
	 *        unable to distinguish objects. The global order can be used to
	 *        use multiple {@link MappedComparator}s in a
	 *        chain.
	 */
	public MappedComparator(Mapping<? super T, ? extends C> valueMapping, Comparator<? super C> valueOrder, Comparator<? super T> globalOrder) {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			toConfig(valueMapping, valueOrder, globalOrder));
	}

	private static <T, C> Config<T, C> toConfig(Mapping<? super T, ? extends C> valueMapping,
			Comparator<? super C> valueOrder, Comparator<? super T> globalOrder) {
		@SuppressWarnings("unchecked")
		Config<T, C> config = TypedConfiguration.newConfigItem(Config.class);
		config.setMapping(valueMapping);
		config.setValueOrder(valueOrder);
		config.setGlobalOrder(globalOrder);
		return config;
	}

	@Override
	public int compare(T o1, T o2) {
		int valueComparison = valueOrder.compare(mapping.map(o1), mapping.map(o2));
		if (valueComparison != 0) {
			return valueComparison;
		}
		
		// Values are equal. Compare according to some stable order.
		return globalOrder.compare(o1, o2);
	}
	
}