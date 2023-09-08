/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.sensors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.kafka.services.sensors.Sensor;
import com.top_logic.kafka.services.sensors.SensorService;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Build up the list of sensors relevant to the user.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SensorListModelBuilder extends AbstractConfiguredInstance<SensorListModelBuilder.Config>
		implements ListModelBuilder {

	/**
	 * Configuration of this model builder.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends PolymorphicConfiguration<SensorListModelBuilder> {

		/** Configuration name for the value of {@link #getIncludeTopics()}. */
		String INCLUDE_TOPICS_NAME = "include-topics";

		/** Configuration name for the value of {@link #getExcludeTopics()}. */
		String EXCLUDE_TOPICS_NAME = "exclude-topics";

		/**
		 * Optional list of topics to be included.
		 * 
		 * <p>
		 * If not empty, only those topics that are contained in the set (but not in
		 * {@link #getExcludeTopics()}) are contained in the
		 * {@link ListModelBuilder#getModel(Object, LayoutComponent) model}.
		 * </p>
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(INCLUDE_TOPICS_NAME)
		Set<String> getIncludeTopics();

		/**
		 * Optional list of topics to be excluded.
		 * 
		 * <p>
		 * All topics included in this set are not contained in the
		 * {@link ListModelBuilder#getModel(Object, LayoutComponent) model}.
		 * </p>
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(EXCLUDE_TOPICS_NAME)
		Set<String> getExcludeTopics();

	}

	private final Set<String> _includeTopics;

	private final Set<String> _excludeTopics;

	/**
	 * Creates a {@link SensorListModelBuilder}.
	 *
	 * @param context
	 *        The context this instance is be instantiated in.
	 * @param config
	 *        The configuration.
	 */
	public SensorListModelBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_includeTopics = config.getIncludeTopics();
		_excludeTopics = config.getExcludeTopics();
	}

	@Override
	public Collection<?> getModel(Object aModel, LayoutComponent aComponent) {
		return this.addSensors(new ArrayList<>());
    }

    @Override
    public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return (aModel == null);
    }

    @Override
    public boolean supportsListElement(LayoutComponent aComponent, Object anElement) {
        return (anElement instanceof Sensor);
    }

    @Override
    public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anElement) {
        return null;
    }

    /** 
     * Add the relevant sensors to the given list and return that list.
     * 
     * @param    someSensors   List of sensors to add the new sensors to.
     * @return   Given list of sensors.
     */
	protected List<? extends Sensor<?, ?>> addSensors(List<Sensor<?, ?>> someSensors) {
		for (Sensor<?, ?> sensor : SensorService.getInstance().getSensors()) {
			if (includeSensor(sensor)) {
				someSensors.add(sensor);
			}
        }

        return someSensors;
    }

	private boolean includeSensor(Sensor<?, ?> sensor) {
		String topic = sensor.getDataSource();

		if (_excludeTopics.contains(topic)) {
			// topic has to be excluded
			return false;
		}
		if (_includeTopics.isEmpty()) {
			// No include topic, therefore all topics are included.
			return true;
		}
		return _includeTopics.contains(topic);
	}

}
