/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.DBType;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.instance.importer.resolver.NoValueResolver;
import com.top_logic.model.instance.importer.resolver.ValueResolver;
import com.top_logic.model.instance.importer.schema.AttributeValueConf;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;
import com.top_logic.model.instance.importer.schema.InstanceRefConf;
import com.top_logic.model.instance.importer.schema.ModelRefConf;
import com.top_logic.model.instance.importer.schema.ObjectConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf.ResolverDef;
import com.top_logic.model.instance.importer.schema.PrimitiveValueConf;
import com.top_logic.model.instance.importer.schema.ValueConf;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * Algorithm for exporting objects to configurations for re-importing later on.
 */
public class XMLInstanceExporter {

	private ObjectsConf _objects = TypedConfiguration.newConfigItem(ObjectsConf.class);

	private int _nextId = 1;

	private Map<TLObject, Integer> _exportIds = new HashMap<>();

	private Map<TLModelPart, String> _modelNames = new HashMap<>();

	private Set<TLObject> _queue = new LinkedHashSet<>();

	private Resolvers _resolvers = new Resolvers();

	private Map<TLModelPart, Boolean> _excludedParts = new HashMap<>();

	/**
	 * Creates a {@link XMLInstanceExporter}.
	 */
	public XMLInstanceExporter() {
		super();
	}

	/**
	 * Adds an {@link InstanceResolver} for creating stable references to existing objects not part
	 * of the export.
	 */
	public void addResolver(TLStructuredType type, InstanceResolver resolver) {
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<? extends InstanceResolver> config =
			(PolymorphicConfiguration<? extends InstanceResolver>) InstanceAccess.INSTANCE.getConfig(resolver);

		ResolverDef def = TypedConfiguration.newConfigItem(ResolverDef.class);
		def.setType(TLModelPartRef.ref(type));
		def.setImpl(config);
		_objects.getResolvers().put(def.getType(), def);
		_resolvers.put(type, resolver);

		_excludedParts.put(type, Boolean.FALSE);
	}

	/**
	 * Adds a type part to exclude from export.
	 */
	public void addExclude(TLModelPart part) {
		_excludedParts.put(part, Boolean.TRUE);
	}

	/**
	 * Adds the given object to the export.
	 */
	public void export(TLObject obj) {
		if (obj == null) {
			return;
		}
		Integer existing = _exportIds.get(obj);
		if (existing != null) {
			// Already part of the export.
			return;
		}

		Integer id = nextId();
		_exportIds.put(obj, id);
		_queue.add(obj);
	}

	private ObjectConf exportObject(TLObject obj) {
		Integer id = nextId();
		_exportIds.put(obj, id);
		return export(obj, id);
	}

	/**
	 * The exported configuration.
	 */
	public ObjectsConf getExportConfig() {
		processQueue();
		return _objects;
	}

	private void processQueue() {
		while (!_queue.isEmpty()) {
			List<TLObject> batch = new ArrayList<>(_queue);
			_queue.clear();
			for (TLObject next : batch) {
				Integer id = _exportIds.get(next);
				ObjectConf conf = export(next, id);

				_objects.getObjects().add(conf);
			}
		}
	}

	private ObjectConf export(TLObject obj, Integer id) {
		ObjectConf conf = TypedConfiguration.newConfigItem(ObjectConf.class);
		conf.setId(id.toString());
		conf.setType(typeName(obj.tType()));

		exportAttributes(conf.getAttributes(), obj);

		return conf;
	}

	private void exportAttributes(List<AttributeValueConf> attributes, TLObject obj) {
		for (TLStructuredTypePart part : obj.tType().getAllParts()) {
			if (exclude(part)) {
				continue;
			}

			try {
				exportAttribute(attributes, obj, part);
			} catch (Exception ex) {
				Logger.error("Failed to export attribute '" + part + "'.", ex, XMLInstanceExporter.class);
			}
		}
	}

	private boolean exclude(TLModelPart part) {
		Boolean exclude = _excludedParts.get(part);
		if (exclude == null) {
			exclude = Boolean.valueOf(computeExclude(part));
			_excludedParts.put(part, exclude);
		}
		return exclude.booleanValue();
	}

	/**
	 * Whether to exclude the given {@link TLStructuredTypePart} from export.
	 * 
	 * @param part
	 *        The part in question.
	 */
	protected boolean computeExclude(TLModelPart part) {
		if (part instanceof TLStructuredTypePart attribute) {
			if (attribute.isDerived()) {
				return true;
			}

			return exclude(attribute.getType());
		} else {
			return false;
		}
	}

	private void exportAttribute(List<AttributeValueConf> attributes, TLObject obj, TLStructuredTypePart part) {
		AttributeValueConf valueConf = TypedConfiguration.newConfigItem(AttributeValueConf.class);
		valueConf.setName(part.getName());

		Object value = obj.tValue(part);

		if (part.getModelKind() == ModelKind.REFERENCE) {
			List<ValueConf> references = valueConf.getCollectionValue();
			exportRef(references, part, value, ((TLReference) part).isComposite());
		} else {
			ValueResolver valueResolver = _resolvers.valueResolver(part);
			if (valueResolver != NoValueResolver.INSTANCE) {
				List<ValueConf> collectionValue = valueConf.getCollectionValue();

				if (value instanceof Collection<?> collection) {
					for (Object entry : collection) {
						ValueConf entryConf = valueResolver.createValueConf(part, entry);
						if (entryConf != null) {
							collectionValue.add(entryConf);
						}
					}
				} else {
					ValueConf entryConf = valueResolver.createValueConf(part, value);
					if (entryConf != null) {
						collectionValue.add(entryConf);
					}
				}
			} else {
				TLPrimitive type = (TLPrimitive) part.getType();
				if (value instanceof Collection<?> collection) {
					List<ValueConf> collectionValue = valueConf.getCollectionValue();
					for (Object entry : collection) {
						PrimitiveValueConf entryConf = TypedConfiguration.newConfigItem(PrimitiveValueConf.class);
						entryConf.setValue(serialize(type, entry));
						collectionValue.add(entryConf);
					}
				} else {
					valueConf.setValue(serialize(type, value));
				}
			}
		}

		attributes.add(valueConf);
	}

	private void exportRef(List<ValueConf> references, TLStructuredTypePart part, Object value, boolean composite) {
		if (value instanceof Collection<?> collection) {
			for (Object entry : collection) {
				exportRefEntry(references, part, entry, composite);
			}
		} else {
			exportRefEntry(references, part, value, composite);
		}
	}

	private void exportRefEntry(List<ValueConf> references, TLStructuredTypePart part, Object value,
			boolean composite) {
		if (value == null) {
			return;
		}

		if (value instanceof TLModelPart modelPart) {
			ModelRefConf ref = TypedConfiguration.newConfigItem(ModelRefConf.class);
			ref.setName(typeName(modelPart));
			references.add(ref);
		} else {
			Integer existing = _exportIds.get(value);
			if (existing != null) {
				InstanceRefConf ref = TypedConfiguration.newConfigItem(InstanceRefConf.class);
				ref.setId(existing.toString());
				references.add(ref);
			} else {
				TLObject target = (TLObject) value;
				if (exclude(target.tType())) {
					return;
				}

				if (composite) {
					ObjectConf config = exportObject(target);
					references.add(config);
					return;
				}

				InstanceResolver resolver = _resolvers.resolver(target.tType());
				if (resolver != null) {
					GlobalRefConf ref = TypedConfiguration.newConfigItem(GlobalRefConf.class);
					ref.setKind(typeName(target.tType()));
					ref.setId(resolver.buildId(target));
					references.add(ref);
				} else {
					Logger
						.warn("Link in reference '" + part + "' to object of type '"
							+ TLModelUtil.qualifiedName(target.tType())
						+ "' without resolver ignored.", XMLInstanceExporter.class);
				}
			}
		}
	}

	private Integer nextId() {
		return Integer.valueOf(_nextId++);
	}

	private String typeName(TLModelPart part) {
		return _modelNames.computeIfAbsent(part, TLModelUtil::qualifiedName);
	}

	/**
	 * Serializes a primitive value in a format that can be stored in an XML configuration.
	 */
	public static String serialize(TLPrimitive type, Object value) {
		if (value == null) {
			return "";
		}
		return serializeStorageValue(type.getDBType(), type.getStorageMapping().getStorageObject(value));
	}

	private static String serializeStorageValue(DBType dbType, Object value) {
		switch (dbType) {
			case BLOB: {
				return Base64.encodeBase64String(binary(value));
			}
			case BOOLEAN:
				return Boolean.toString(((Boolean) value).booleanValue());
			case DATE:
			case TIME:
			case DATETIME:
				return XmlDateTimeFormat.INSTANCE.format(value);
			case DECIMAL:
			case DOUBLE:
				return Double.toString(((Number) value).doubleValue());
			case FLOAT:
				return Float.toString(((Number) value).floatValue());
			case BYTE:
				return Byte.toString(((Number) value).byteValue());
			case SHORT:
				return Short.toString(((Number) value).shortValue());
			case INT:
				return Integer.toString(((Number) value).intValue());
			case LONG:
				return Long.toString(((Number) value).longValue());
			case CLOB:
			case STRING:
			case CHAR:
				return value.toString();
			case ID:
				return ((TLID) value).toExternalForm();
		}

		throw new UnreachableAssertion("Unsupported DB type: " + dbType);
	}

	private static byte[] binary(Object value) {
		if (value instanceof BinaryDataSource data) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			try {
				data.deliverTo(buffer);
			} catch (IOException ex) {
				// Extremely unlikely, since writing to a buffer.
				throw new IOError(ex);
			}
			return buffer.toByteArray();
		}
		if (value instanceof byte[] data) {
			return data;
		}
		throw new IllegalArgumentException("Not expected for binary data: " + value.getClass());
	}

}
