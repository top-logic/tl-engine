/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;

/**
 * Initializer for a {@link PropertyKind#DERIVED derived} {@link PropertyDescriptor property}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class DerivedPropertyInitializer extends Initializer {

	private final class UpdateListener implements ConfigurationListener {
		private final AbstractConfigItem _item;

		public UpdateListener(AbstractConfigItem item) {
			_item = item;
		}

		@Override
		public void onChange(ConfigurationChange change) {
			updateDerivedValue(_item);
		}
	}

	private final PropertyDescriptorImpl _property;

	private final AlgorithmDependency _algorithmDependency;

	DerivedPropertyInitializer(PropertyDescriptorImpl property, AlgorithmDependency algorithmDependency) {
		assert property != null : "Undefined property.";
		_property = property;
		_algorithmDependency = algorithmDependency;
	}

	@Override
	public void init(final AbstractConfigItem item) {
		_algorithmDependency.installFinally(item.getInterface(), new UpdateListener(item));
		updateDerivedValue(item);
	}

	final void updateDerivedValue(AbstractConfigItem item) {
		Object newValue = _property.computeDerived(item.getInterface());
		Object oldValue = item.updateDirectly(_property, newValue);
		if (!CollectionUtil.equals(newValue, oldValue)) {
			item.onChange(_property).update(_property, oldValue, newValue);
		}
	}

	/**
	 * Creates a {@link AlgorithmDependency} for the given property.
	 * 
	 * @param protocol
	 *        The current error log.
	 * @param derivedProperty
	 *        The property to be initialized.
	 */
	public static AlgorithmDependency createAlgorithm(Protocol protocol, PropertyDescriptorImpl derivedProperty) {
		ConfigurationDescriptor descriptor = derivedProperty.getDescriptor();
		NamePath[] paths = derivedProperty.getAlgorithm().getPaths();
		return AlgorithmDependency.createDependency(protocol, descriptor, paths);
	}

}
