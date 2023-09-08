/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.resources;

import static com.top_logic.layout.provider.label.ClassLabelProvider.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;
import com.top_logic.layout.provider.AbstractMappingResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link InspectorTreeNode}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorNodeResourceProvider extends AbstractMappingResourceProvider {
	
	private static final Property<IdIndex> ID_INDEX = TypedAnnotatable.property(IdIndex.class, "idIndex");

	/**
	 * Create a {@link InspectorNodeResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InspectorNodeResourceProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getLabel(Object object) {
		InspectorTreeNode node = node(object);
		String name = node.getProperty().name();
		if (StringServices.isEmpty(name)) {
			return toString(node, node.getBusinessObject());
		}
		return name;
	}

	@Override
	public ThemeImage getImage(Object object, Flavor aFlavor) {
		ThemeImage result = super.getImage(object, aFlavor);
		if (result == null) {
			InspectorTreeNode node = node(object);
			boolean isPrivate = node.getProperty().isPrivate();
			Object value = node.getBusinessObject();
			return JavaFieldResourceProvider.getTypeIcon(isPrivate, value == null ? Object.class : value.getClass());
		}
		return result;
	}

	/**
	 * An inspect-representation of any object.
	 * 
	 * @param object
	 *        The object to show.
	 * @return The value presents in the inspector.
	 */
	public static String toString(InspectorTreeNode node, Object object) {
		if (object == null) {
			return null;
		}

		if (object instanceof Collection<?>) {
			return object.getClass().getSimpleName() + "(" + ((Collection<?>) object).size() + ")";
		}
		if (object instanceof Map<?, ?>) {
			return object.getClass().getSimpleName() + "(" + ((Map<?, ?>) object).size() + ")";
		}
		if (object.getClass().isArray()) {
			return object.getClass().getComponentType().getSimpleName() + "[" + Array.getLength(object) + "]";
		}
		if (object instanceof CharSequence) {
			return '"' + object.toString() + '"';
		}
		if (object.getClass().isEnum()) {
			if (object instanceof ExternallyNamed) {
				return "enum:" + ((ExternallyNamed) object).getExternalName();
			}
			return "enum:" + ((Enum<?>) object).name();
		}
		if (object.getClass().isPrimitive() || object.getClass() == Boolean.class
			|| object.getClass() == Byte.class || object.getClass() == Short.class
			|| object.getClass() == Integer.class || object.getClass() == Long.class
			|| object.getClass() == Float.class || object.getClass() == Double.class) {
			return object.toString();
		}
		if (object instanceof ConfigurationItem) {
			String configName = simpleName(((ConfigurationItem) object).descriptor().getConfigurationInterface());
			if (object instanceof PolymorphicConfiguration<?>) {
				return configName + "("
					+ simpleName(((PolymorphicConfiguration<?>) object).getImplementationClass()) + ")" + ":"
					+ index(node).id(object);
			}
			return configName + ":" + index(node).id(object);
		}
		if (object instanceof NamedConstant) {
			return "const:" + object.toString();
		}
		if (object instanceof EventType<?, ?, ?>) {
			return "evt:" + object.toString();
		}
		if (object instanceof ResPrefix) {
			return object.toString();
		}
		if (object instanceof ResKey) {
			return "resource:" + object.toString();
		}
		if (object instanceof Class) {
			return "class:" + simpleName((Class<?>) object);
		}
		String label = label(object);
		return simpleName(object.getClass()) + (StringServices.isEmpty(label) ? "" : "(" + label + ")") + ":"
			+ index(node).id(object);
	}

	private static String label(Object object) {
		if (object instanceof LayoutComponent) {
			LayoutComponent comp = (LayoutComponent) object;
			return Resources.getInstance().getString(comp.getTitleKey(), comp.getName().qualifiedName());
		}
		return MetaResourceProvider.INSTANCE.getLabel(object);
	}

	private static IdIndex index(InspectorTreeNode node) {
		InspectorTreeNode root = node.getModel().getRoot();
		IdIndex result = root.get(ID_INDEX);
		if (result == null) {
			result = new IdIndex();
			root.set(ID_INDEX, result);
		}
		return result;
	}

	static class IdIndex {

		private int _nextId = 1;

		private Map<Object, Integer> _idByObj = new IdentityHashMap<>();

		public Integer id(Object obj) {
			Integer result = _idByObj.get(obj);
			if (result == null) {
				result = Integer.valueOf(_nextId++);
				_idByObj.put(obj, result);
			}
			return result;
		}

	}

	@Override
	protected Object mapValue(Object object) {
		InspectorTreeNode node = node(object);
		Object property = node.getProperty();
		Object delegate = property == null ? node.getBusinessObject() : property;
		return delegate;
	}

	static InspectorTreeNode node(Object object) {
		return (InspectorTreeNode) object;
	}

}
