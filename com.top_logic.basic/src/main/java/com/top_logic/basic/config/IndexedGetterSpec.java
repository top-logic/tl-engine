/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Method;
import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.annotation.Indexed;

/**
 * Information to resolve an {@link Indexed} getter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class IndexedGetterSpec implements ResolverPart {

	private final Method _accessMethod;

	private final String _collectionPropertyName;

	private final MethodImplProxy _methodImplProxy;

	public IndexedGetterSpec(ConfigurationDescriptorImpl descriptor, Method accessMethod, String collectionPropertyName) {
		_accessMethod = accessMethod;
		_collectionPropertyName = collectionPropertyName;
		_methodImplProxy = new MethodImplProxy();
		descriptor.addImplementation(_accessMethod, _methodImplProxy);
	}

	@Override
	public void resolve(Protocol protocol, ConfigurationDescriptor descriptor) {
		MethodBasedPropertyDescriptor collectionProperty =
			MethodBasedPropertyDescriptor.cast(descriptor.getProperty(_collectionPropertyName));
		if (collectionProperty == null) {
			protocol.error("No such property '" + _collectionPropertyName + "' in @Indexed annotation of method '"
				+ _accessMethod + "'.");
			return;
		}

		PropertyDescriptor keyProperty = collectionProperty.getKeyProperty();
		if (keyProperty == null) {
			protocol.error("Missing @Key annotation in property '" + _collectionPropertyName + "' type '"
				+ descriptor.getConfigurationInterface() + "'.");
			return;
		}

		Class<?> valueType = _accessMethod.getReturnType();
		if (!valueType.isAssignableFrom(collectionProperty.getElementType())) {
			protocol.error("Expected '" + collectionProperty.getElementType() + "' or super as return type of '"
				+ _accessMethod + "'.");
			return;
		}

		if (_accessMethod.getParameterTypes().length != 1) {
			protocol.error("Indexed access method '" + _accessMethod + "' must have exactly one paramter.");
			return;
		}

		Class<?> keyAccessType = _accessMethod.getParameterTypes()[0];
		Class<?> keyType = keyProperty.getType();
		if (!keyAccessType.isAssignableFrom(keyType)) {
			protocol.error("Parameter of indexed access method '" + _accessMethod + "' must have key type '"
				+ keyType + "' or a super type thereof.");
			return;
		}

		if (Map.class.isAssignableFrom(collectionProperty.getType())) {
			_methodImplProxy.init(new IndexedMapGetter(collectionProperty));
		} else {
			_methodImplProxy.init(new IndexedCollectionGetter(collectionProperty, keyProperty));
			if (collectionProperty.isInstanceValued() && !isConfiguredInstance(collectionProperty.getElementType())) {
				protocol.error("The type of a instance-valued collection property with an indexed getter "
					+ "must be assignment compatible to '" + ConfiguredInstance.class.getName() + "'.");
			}
		}

		collectionProperty.addIndexedGetter(_accessMethod);
	}

	private boolean isConfiguredInstance(Class<?> elementType) {
		return ConfiguredInstance.class.isAssignableFrom(elementType);
	}

}
