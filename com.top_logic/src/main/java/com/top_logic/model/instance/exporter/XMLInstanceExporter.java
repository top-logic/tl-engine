/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.math3.util.Pair;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.db2.InitialDataSetupService;
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
import com.top_logic.util.error.TopLogicException;

/**
 * Algorithm for exporting objects to configurations for re-importing later on.
 */
public class XMLInstanceExporter {

	private ObjectsConf _objects = TypedConfiguration.newConfigItem(ObjectsConf.class);

	private int _nextId = 1;

	private Map<TLObject, Ref> _exportIds = new HashMap<>();

	private Map<TLModelPart, String> _modelNames = new HashMap<>();

	private List<TLObject> _queue = new ArrayList<>();

	private I18NLog _log =
		new LogProtocol(XMLInstanceExporter.class).asI18NLog(ResourcesModule.getInstance().getLogBundle());

	private Resolvers _resolvers = new Resolvers(_log);

	private PartSet _excludedParts = new PartSet(this::computeExclude);

	private PartSet _includedParts = new PartSet(this::computeInclude);

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

		_excludedParts.remove(type);
	}

	/**
	 * Adds a type part to exclude from export.
	 */
	public void addExclude(TLModelPart part) {
		_excludedParts.add(part);
	}

	/**
	 * Adds a type part or type to include in an export.
	 * 
	 * <p>
	 * By default, only composition references are followed. For objects in all other references
	 * there must either exist a resolver, or the reference must be excluded from export. By
	 * explicitly including a reference or a type to an export, objects in those included references
	 * or references of that value type are treated like composition references.
	 * </p>
	 */
	public void addInclude(TLModelPart part) {
		_includedParts.add(part);
	}

	/**
	 * Adds the given object to the export.
	 */
	public void export(TLObject obj) {
		if (obj == null) {
			return;
		}

		// Mark the object to be explicitly part of the export, no matter if it is referenced from
		// some composition reference, or not.
		_queue.add(obj);

		Ref existing = _exportIds.get(obj);
		if (existing != null) {
			// Already part of the export.
			return;
		}

		// Assign an ID for the object that can be used when the object is referenced from the
		// exported graph. This makes the IDs of the top-level objects to be the first n IDs in the
		// export.
		newRef(null, obj);
	}

	/**
	 * The exported configuration.
	 */
	public ObjectsConf getExportConfig() {
		processQueue();
		List<ResKey> errors = _exportIds.entrySet().stream()
			.filter(e -> !e.getValue().isExported())
			.map(e -> new Pair<>(e.getValue().getContainer(), e.getKey().tType()))
			.distinct()
			.map(p -> I18NConstants.REFERENCED_OBJECT_NOT_INCLUDED__TYPE_ATTR
				.fill(TLModelUtil.qualifiedName(p.getSecond()), TLModelUtil.qualifiedName(p.getFirst())))
			.toList();
		
		if (!errors.isEmpty()) {
			TopLogicException ex = null;
			for (ResKey message : errors) {
				ex = new TopLogicException(message, ex);
			}
			assert ex != null;
			throw ex;
		}
		
		return _objects;
	}

	private void processQueue() {
		while (!_queue.isEmpty()) {
			List<TLObject> batch = new ArrayList<>(_queue);
			_queue.clear();
			for (TLObject next : batch) {
				Ref ref = _exportIds.get(next);
				if (ref.isExported()) {
					// Skip.
					continue;
				}
				ObjectConf conf = exportObject(next, ref);

				_objects.getObjects().add(conf);
			}
		}
	}

	private ObjectConf exportObject(TLStructuredTypePart container, TLObject obj) {
		return exportObject(obj, newRef(container, obj));
	}

	private ObjectConf exportObject(TLObject obj, Ref ref) {
		ref.markExported();

		InstanceResolver resolver = _resolvers.resolver(obj.tType());

		ObjectConf conf = TypedConfiguration.newConfigItem(ObjectConf.class);
		conf.setId(Integer.toString(ref.getId()));
		conf.setType(typeName(obj.tType()));
		if (resolver != null) {
			conf.setGlobalId(resolver.buildId(obj));
		}

		exportAttributes(conf.getAttributes(), obj);

		return conf;
	}

	private void exportAttributes(List<AttributeValueConf> attributes, TLObject obj) {
		for (TLStructuredTypePart part : obj.tType().getAllParts()) {
			if (isExluded(part)) {
				continue;
			}

			try {
				exportAttribute(attributes, obj, part);
			} catch (Exception ex) {
				Logger.error("Failed to export attribute '" + part + "'.", ex, XMLInstanceExporter.class);
			}
		}
	}

	private boolean isExluded(TLModelPart part) {
		return _excludedParts.contains(part);
	}

	private boolean isIncluded(TLModelPart part) {
		return _includedParts.contains(part);
	}

	/**
	 * Whether to exclude the given given {@link TLStructuredType} or {@link TLStructuredTypePart}
	 * from export.
	 * 
	 * @param part
	 *        The part in question.
	 */
	protected boolean computeExclude(TLModelPart part) {
		return part instanceof TLStructuredTypePart attr && attr.isDerived();
	}

	/**
	 * Whether to explicitly include the given {@link TLStructuredType} or
	 * {@link TLStructuredTypePart} in the export.
	 * 
	 * @param part
	 *        The part in question.
	 */
	protected boolean computeInclude(TLModelPart part) {
		return false;
	}

	private void exportAttribute(List<AttributeValueConf> attributes, TLObject obj, TLStructuredTypePart part) {
		AttributeValueConf valueConf = TypedConfiguration.newConfigItem(AttributeValueConf.class);
		valueConf.setName(part.getName());

		Object value = obj.tValue(part);

		if (part.getModelKind() == ModelKind.REFERENCE) {
			List<ValueConf> references = valueConf.getCollectionValue();
			exportRef(part, references, value, ((TLReference) part).isComposite() || isIncluded(part));
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

	private void exportRef(TLStructuredTypePart container, List<ValueConf> references, Object value,
			boolean composite) {
		if (value instanceof Collection<?> collection) {
			for (Object entry : collection) {
				exportRefEntry(container, references, entry, composite);
			}
		} else {
			exportRefEntry(container, references, value, composite);
		}
	}

	private void exportRefEntry(TLStructuredTypePart container, List<ValueConf> references, Object value,
			boolean composite) {
		if (value == null) {
			return;
		}

		if (value instanceof TLModelPart modelPart) {
			ModelRefConf ref = TypedConfiguration.newConfigItem(ModelRefConf.class);
			ref.setName(typeName(modelPart));
			references.add(ref);
		} else {
			TLObject target = (TLObject) value;

			Ref existingRef = _exportIds.get(target);
			if (existingRef != null) {
				if (composite && !existingRef.isExported()) {
					references.add(exportObject(target, existingRef));
				} else {
					references.add(refConfig(existingRef));
				}
			} else {
				if (isExluded(target.tType())) {
					return;
				}

				if (composite) {
					references.add(exportObject(container, target));
					return;
				}

				InstanceResolver resolver = _resolvers.resolver(target.tType());
				if (resolver != null) {
					Ref localRef = newRef(container, target);
					localRef.markExported();

					GlobalRefConf globalRef = TypedConfiguration.newConfigItem(GlobalRefConf.class);
					globalRef.setKind(typeName(target.tType()));
					globalRef.setId(resolver.buildId(target));
					globalRef.setLocalId(Integer.toString(localRef.getId()));
					references.add(globalRef);
				} else {
					// The object may be exported later on when it occurs in some composition
					// reference.
					references.add(refConfig(newRef(container, target)));
				}
			}
		}
	}

	private InstanceRefConf refConfig(Ref newRef) {
		InstanceRefConf result = TypedConfiguration.newConfigItem(InstanceRefConf.class);
		result.setId(Integer.toString(newRef.getId()));
		return result;
	}

	private Ref newRef(TLStructuredTypePart container, TLObject obj) {
		Ref newRef = new Ref(_nextId++, container);
		_exportIds.put(obj, newRef);
		return newRef;
	}

	private String typeName(TLModelPart part) {
		return _modelNames.computeIfAbsent(part, TLModelUtil::qualifiedName);
	}

	/**
	 * Exports the given objects and converts them into a binary stream containing XML data.
	 */
	public BinaryDataSource exportInstances(Collection<? extends TLObject> values) {
		return exportInstances(null, values);
	}

	/**
	 * Exports the given objects and converts them into a binary stream containing XML data.
	 */
	public BinaryDataSource exportInstances(String downloadName, Collection<? extends TLObject> values) {
		String typeName = "objects";
		boolean first = true;
		for (TLObject obj : values) {
			if (first) {
				typeName = obj.tType().getName();
				first = false;
			}
			export(obj);
		}
		ObjectsConf export = getExportConfig();
	
		if (downloadName == null) {
			downloadName =
				new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()) + "_" + typeName
					+ InitialDataSetupService.FILE_SUFFIX;
		}
		InstanceXmlData instanceData = new InstanceXmlData(downloadName, export);
		return instanceData;
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

	private static class Ref {

		private int _id;

		private boolean _exported;

		private TLStructuredTypePart _container;

		/**
		 * Creates a {@link Ref}.
		 */
		public Ref(int id, TLStructuredTypePart container) {
			_id = id;
			_container = container;
		}

		public int getId() {
			return _id;
		}

		/**
		 * The reference that initially included the referenced object to the export scope.
		 */
		public TLStructuredTypePart getContainer() {
			return _container;
		}

		/**
		 * Whether the referenced object is already serialized.
		 */
		public boolean isExported() {
			return _exported;
		}

		public void markExported() {
			assert !_exported : "Must not export twice.";

			_exported = true;
		}
	}

	private static class PartSet {
		private final Function<TLModelPart, Boolean> _implicitMembers;

		private final Map<TLModelPart, Boolean> _parts = new HashMap<>();

		/**
		 * Creates a {@link PartSet}.
		 */
		public PartSet(Function<TLModelPart, Boolean> implicitMembers) {
			_implicitMembers = implicitMembers;
		}

		/**
		 * Excludes all parts with the given type from this set.
		 */
		public void remove(TLStructuredType type) {
			_parts.put(type, Boolean.FALSE);
		}

		/**
		 * Determines whether the given part is in this set.
		 */
		public boolean contains(TLModelPart part) {
			Boolean value = _parts.get(part);
			if (value == null) {
				value = Boolean.valueOf(computeContains(part));
				_parts.put(part, value);
			}
			return value.booleanValue();
		}

		/**
		 * Whether to exclude the given {@link TLStructuredTypePart} from export.
		 * 
		 * @param part
		 *        The part in question.
		 */
		protected boolean computeContains(TLModelPart part) {
			if (_implicitMembers.apply(part)) {
				return true;
			}

			if (part instanceof TLStructuredTypePart attribute) {
				return contains(attribute.getType());
			} else {
				return false;
			}
		}

		/**
		 * Adds the given part from this set.
		 */
		public void add(TLModelPart part) {
			_parts.put(part, Boolean.TRUE);
		}

	}
}
