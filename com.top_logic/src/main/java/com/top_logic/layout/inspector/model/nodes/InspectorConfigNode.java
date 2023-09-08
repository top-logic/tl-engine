/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;

/**
 * {@link InspectorTreeNode} representing a {@link ConfigurationItem} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorConfigNode extends InspectorTreeNode {

	private final NamedConstant IMPL_CLASS_PROP_ID = TypedConfiguration
		.getConfigurationDescriptor(PolymorphicConfiguration.class)
		.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME).identifier();

	/**
	 * Creates a {@link InspectorConfigNode}.
	 */
	public InspectorConfigNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			InspectProperty property, ConfigurationItem userObject) {
		super(model, parent, userObject, property);
	}

	@Override
	public boolean isLeaf() {
		return item().descriptor().getProperties().isEmpty();
	}

	@Override
	public List<InspectorTreeNode> makeChildren() {
		ConfigurationItem item = item();
		List<? extends PropertyDescriptor> properties = new ArrayList<>(item.descriptor().getProperties());

		Collections.sort(properties, new Comparator<PropertyDescriptor>() {
			@Override
			public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
				return o1.getPropertyName().compareTo(o2.getPropertyName());
			}
		});

		Filter<? super InspectorTreeNode> filter = filter();
		ArrayList<InspectorTreeNode> result = new ArrayList<>(properties.size());
		for (PropertyDescriptor property : properties) {
			Object value = item.value(property);
			PropertyDescriptor keyProperty = property.getKeyProperty();
			if (keyProperty != null && !(value instanceof Map<?, ?>)) {
				BiFunction<Object, PropertyDescriptor, Object> keyExtractor;
				if (keyProperty.identifier() == IMPL_CLASS_PROP_ID) {
					keyExtractor = (x, p) -> x instanceof ConfigurationItem ? ((ConfigurationItem) x).value(p)
							: x != null ? x.getClass() : null;
				} else {
					ConfigurationAccess configAccess = property.getConfigurationAccess();
					keyExtractor = (x, p) -> configAccess.getConfig(x).value(p);
				}
				HashMap<Object, Object> map = new HashMap<>();
				for (Object entry : (Collection<?>) value) {
					map.put(keyExtractor.apply(entry, keyProperty), entry);
				}
				value = map;
			}
			InspectorTreeNode newNode = makeValueNode(new ConfigProperty(property), value);
			if (filter.accept(newNode)) {
				result.add(newNode);
			}
		}
		return result;
	}

	private ConfigurationItem item() {
		return (ConfigurationItem) getBusinessObject();
	}

	/**
	 * {@link InspectProperty} representing a {@link PropertyDescriptor}.
	 */
	public static class ConfigProperty extends InspectProperty {

		private PropertyDescriptor _property;

		/**
		 * Creates a {@link ConfigProperty}.
		 *
		 * @param property
		 *        See {@link #getProperty()}.
		 */
		public ConfigProperty(PropertyDescriptor property) {
			_property = property;
		}

		/**
		 * The {@link PropertyDescriptor} being inspected.
		 */
		public PropertyDescriptor getProperty() {
			return _property;
		}

		@Override
		public PropertyDescriptor getValue() {
			return _property;
		}

		@Override
		public String name() {
			return _property.getPropertyName();
		}

		@Override
		public Object staticType() {
			return _property.getType();
		}

		@Override
		public boolean isPrivate() {
			Hidden hidden = _property.getAnnotation(Hidden.class);
			boolean isPrivate = hidden != null && hidden.value();
			return isPrivate;
		}

		@Override
		public boolean isDefaultValue(InspectorTreeNode valueNode) {
			return Utils.equals(_property.getDefaultValue(), valueNode.getBusinessObject());
		}

	}

}
