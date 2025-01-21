/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.exporter;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.WithStorageAttribute;
import com.top_logic.model.annotate.ui.TLIDColumn;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.model.instance.annotation.TLInstanceResolver;
import com.top_logic.model.instance.annotation.TLValueResolver;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.instance.importer.resolver.NoValueResolver;
import com.top_logic.model.instance.importer.resolver.ValueResolver;
import com.top_logic.model.util.TLModelUtil;

/**
 * Common {@link InstanceResolver} lookup functionality.
 * 
 * @see XMLInstanceExporter
 * @see XMLInstanceImporter
 */
public class Resolvers {

	private Log _log = new LogProtocol(XMLInstanceExporter.class);

	private final Map<TLStructuredType, InstanceResolver> _resolverByType = new HashMap<>();

	private final Map<String, InstanceResolver> _resolverByKind = new HashMap<>();

	private final Map<String, TLStructuredType> _typeByName = new HashMap<>();

	private final Map<TLStructuredTypePart, ValueResolver> _valueResolvers = new HashMap<>();

	/**
	 * Adds a custom resolver for the given type.
	 */
	public void put(TLStructuredType type, InstanceResolver resolver) {
		_resolverByType.put(type, resolver);
	}

	/**
	 * Looks up or builds a resolver for the requested type.
	 */
	public InstanceResolver resolver(TLStructuredType type) {
		InstanceResolver result = _resolverByType.get(type);
		if (result != null || _resolverByType.containsKey(type)) {
			return result;
		}

		result = computeResolver(type);
		if (result == null) {
			result = findInheritedResolver(type);
		}

		// Remember for the rest of the import.
		_resolverByType.put(type, result);

		return result;
	}

	/**
	 * Looks up a resolver by a type kind.
	 */
	public InstanceResolver get(String kind) {
		InstanceResolver result = _resolverByKind.get(kind);
		if (result != null) {
			return result;
		}
		TLStructuredType type = _typeByName.computeIfAbsent(kind, this::lookupType);
		InstanceResolver typeResolver = resolver(type);
		_resolverByKind.put(kind, typeResolver);
		return typeResolver;
	}

	private TLStructuredType lookupType(String name) {
		TLType type = TLModelUtil.findType(name);
		return (TLStructuredType) type;
	}

	/**
	 * Adds a custom resolver for a custom kind.
	 */
	public void put(String kind, InstanceResolver resolver) {
		_resolverByKind.put(kind, resolver);
	}

	/**
	 * Hook for subclasses dynamically looking up {@link InstanceResolver}s for types.
	 * 
	 * @param type
	 *        The type of instances that should be exported by reference.
	 */
	protected InstanceResolver computeResolver(TLStructuredType type) {
		TLInstanceResolver resoverAnnotation = type.getAnnotation(TLInstanceResolver.class);
		if (resoverAnnotation != null) {
			PolymorphicConfiguration<? extends InstanceResolver> resolverConfig = resoverAnnotation.getImpl();
			InstanceResolver resolver = TypedConfigUtil.createInstance(resolverConfig);
			return resolver;
		}
		TLIDColumn annotation = type.getAnnotation(TLIDColumn.class);
		if (annotation != null) {
			String idAttributeName = annotation.getValue();
			TLStructuredTypePart idAttribute = type.getPart(idAttributeName);
			if (idAttribute != null) {
				return idResolver(type, idAttribute);
			}
		} else {
			TLStructuredTypePart nameAttribute = type.getPart("name");
			if (nameAttribute != null) {
				return idResolver(type, nameAttribute);
			}
		}

		return null;
	}

	private InstanceResolver idResolver(TLStructuredType type, TLStructuredTypePart idAttribute) {
		TableName annotation = type.getAnnotation(TableName.class);
		if (annotation == null) {
			return null;
		}

		String tableName = annotation.getName();
		StorageDetail storage = idAttribute.getStorageImplementation();

		String idColumn = idAttribute.getName();
		if (storage instanceof ConfiguredInstance<?> configuredInstance) {
			PolymorphicConfiguration<?> storageConfig = configuredInstance.getConfig();
			if (storageConfig instanceof WithStorageAttribute columnStorageConfig) {
				idColumn = columnStorageConfig.getStorageAttribute();
			}
		}

		TLType idType = idAttribute.getType();
		if (idType instanceof TLPrimitive idValueType) {
			String searchColumn = idColumn;
			return new InstanceResolver() {
				@Override
				public TLObject resolve(String kind, String id) {
					Object value = XMLInstanceImporter.parse(_log, idValueType, id);
					KnowledgeObject item = (KnowledgeObject) PersistencyLayer.getKnowledgeBase()
						.getObjectByAttribute(tableName, searchColumn, value);
					return item.getWrapper();
				}

				@Override
				public String buildId(TLObject obj) {
					Object id = obj.tValue(idAttribute);
					return XMLInstanceExporter.serialize(idValueType, id);
				}
			};
		}

		return null;
	}

	private InstanceResolver findInheritedResolver(TLStructuredType type) {
		if (type instanceof TLClass classType) {
			for (TLClass generalization : classType.getGeneralizations()) {
				InstanceResolver result = resolver(generalization);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Fetches a {@link ValueResolver} to use for the given attribute.
	 */
	public ValueResolver valueResolver(TLStructuredTypePart attribute) {
		ValueResolver existing = _valueResolvers.get(attribute);
		if (existing != null) {
			return existing;
		}

		ValueResolver newResolver = lookupValueResolver(attribute);
		_valueResolvers.put(attribute, newResolver);
		return newResolver;
	}

	private ValueResolver lookupValueResolver(TLStructuredTypePart attribute) {
		TLValueResolver annotation = attribute.getAnnotation(TLValueResolver.class);
		if (annotation == null) {
			return NoValueResolver.INSTANCE;
		}

		return TypedConfigUtil.createInstance(annotation.getImpl());
	}

}
