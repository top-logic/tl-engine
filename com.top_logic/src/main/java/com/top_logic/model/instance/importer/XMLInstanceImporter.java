/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.sql.DBType;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.instance.exporter.Resolvers;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.instance.importer.resolver.NoInstanceResolver;
import com.top_logic.model.instance.importer.resolver.ValueResolver;
import com.top_logic.model.instance.importer.schema.AttributeValueConf;
import com.top_logic.model.instance.importer.schema.CustomValueConf;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;
import com.top_logic.model.instance.importer.schema.InstanceRefConf;
import com.top_logic.model.instance.importer.schema.ModelRefConf;
import com.top_logic.model.instance.importer.schema.ObjectConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf.ResolverDef;
import com.top_logic.model.instance.importer.schema.PrimitiveValueConf;
import com.top_logic.model.instance.importer.schema.ValueConf;
import com.top_logic.model.instance.importer.schema.ValueVisitor;
import com.top_logic.model.util.TLModelUtil;

/**
 * Object instance importer instantiating interconnected objects based on a XML configuration.
 * 
 * @see ObjectsConf
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLInstanceImporter implements ValueVisitor<Object, TLStructuredTypePart> {

	private final TLFactory _factory;

	private final TLModel _model;

	private Log _log = new LogProtocol(XMLInstanceImporter.class);

	private Map<String, TLObject> _objectById = new HashMap<>();

	private Resolvers _resolvers = new Resolvers();

	private InstanceResolver _defaultResolver = NoInstanceResolver.INSTANCE;

	/**
	 * Creates a {@link XMLInstanceImporter}.
	 *
	 * @param model
	 *        See {@link #getModel()}.
	 * @param factory
	 *        See {@link #getFactory()}.
	 */
	public XMLInstanceImporter(TLModel model, TLFactory factory) {
		_model = model;
		_factory = factory;
	}

	/**
	 * The model based on which the import configuration is based on.
	 */
	public TLModel getModel() {
		return _model;
	}

	/**
	 * The {@link TLFactory} that is responsible for creating objects to import.
	 */
	public TLFactory getFactory() {
		return _factory;
	}

	/**
	 * The {@link InstanceResolver} to resolve {@link GlobalRefConf}s of the given kind.
	 * 
	 * @param kind
	 *        The type kind to register the given resolver for. See
	 *        {@link InstanceResolver#resolve(Log, String, String)}.
	 * 
	 * @see #addResolver(String, InstanceResolver)
	 */
	public InstanceResolver getResolver(String kind) {
		return resolver(kind);
	}

	/**
	 * @see #getResolver(String)
	 */
	public void addResolver(String kind, InstanceResolver resolver) {
		_resolvers.put(kind, resolver);
	}

	/**
	 * {@link InstanceResolver} for all kinds for which no special resolver has been registered.
	 * 
	 * @see #addResolver(String, InstanceResolver)
	 */
	public InstanceResolver getDefaultResolver() {
		return _defaultResolver;
	}

	/**
	 * @see #getDefaultResolver()
	 */
	public void setDefaultResolver(InstanceResolver defaultResolver) {
		_defaultResolver = defaultResolver;
	}

	/**
	 * The object that was imported for the given configured ID.
	 * 
	 * @param id
	 *        The ID the import data was associated with. See {@link ObjectConf#getId()}.
	 * @return The imported object.
	 * 
	 * @see #getAllImportedObjects()
	 */
	@SuppressWarnings("unchecked")
	public <T extends TLObject> T getObject(String id) {
		return (T) _objectById.get(id);
	}

	/**
	 * All objects that have been imported in the call to {@link #importInstances(ObjectsConf)}.
	 * 
	 * @see #getObject(String)
	 */
	public Collection<TLObject> getAllImportedObjects() {
		return _objectById.values();
	}

	/**
	 * The {@link Log} that is used for reporting errors in the import process.
	 * 
	 * @see #setLog(Log)
	 */
	public Log getLog() {
		return _log;
	}

	/**
	 * @see #getLog()
	 */
	public void setLog(Log log) {
		_log = log;
		_resolvers.setLog(log);
	}

	/**
	 * Starts the import of the given import description.
	 * 
	 * @param objects
	 *        The description of the objects to import.
	 * @return The top-level objects that were present in the given configuration.
	 */
	public List<TLObject> importInstances(ObjectsConf objects) {
		for (ResolverDef def : objects.getResolvers().values()) {
			InstanceResolver resolver = TypedConfigUtil.createInstance(def.getImpl());
			addResolver(def.getType().qualifiedName(), resolver);
		}

		List<ObjectConf> configs = objects.getObjects();
		return importInstances(configs);
	}

	/**
	 * Instantiates all given confiurations.
	 * 
	 * @return The objects imported from the given configurations.
	 */
	public List<TLObject> importInstances(List<ObjectConf> configs) {
		List<TLObject> result = new ArrayList<>();
		for (ObjectConf config : configs) {
			TLObject obj = createObject(config);
			result.add(obj);
		}

		for (ObjectConf config : configs) {
			importObject(config);
		}
		return result;
	}

	private TLObject createObject(ObjectConf config) {
		TLClass type = (TLClass) TLModelUtil.findType(_model, config.getType());
		TLObject obj = _factory.createObject(type);
		addObject(config.getId(), obj);
		return obj;
	}

	private TLObject importObject(ObjectConf config) {
		TLObject obj = _objectById.get(config.getId());
		TLClass type = (TLClass) obj.tType();
		for (AttributeValueConf valueConf : config.getAttributes()) {
			String attrName = valueConf.getName();
			TLStructuredTypePart part = type.getPart(attrName);
			if (part == null) {
				_log.error("No such part '" + attrName + "' in type '" + type + "' at " + valueConf.location());
				continue;
			}

			Object value;
			try {
				value = resolveValue(part, valueConf);
			} catch (Exception ex) {
				_log.error("Failed to resolve value at " + valueConf.location(), ex);
				continue;
			}
			try {
				obj.tUpdate(part, value);
			} catch (Exception ex) {
				_log.error("Failed to set value '" + value + "' to attribute '" + part + "' at " + valueConf.location(),
					ex);
			}
		}
		return obj;
	}

	private Object resolveValue(TLStructuredTypePart part, AttributeValueConf valueConf) {
		Object value;
		if (part.getModelKind() == ModelKind.REFERENCE) {
			if (valueConf.getValue() != null) {
				_log.error("Reference attribute must not be assigned a plain value at " + valueConf.location());
			}
			value = resolveCollectionValue(part, valueConf);
		} else {
			if (valueConf.getCollectionValue().size() > 0) {
				value = resolveCollectionValue(part, valueConf);
			} else {
				value = parse(_log, (TLPrimitive) part.getType(), valueConf.getValue());
			}
		}
		return value;
	}

	private Object resolveCollectionValue(TLStructuredTypePart part, AttributeValueConf valueConf) {
		List<Object> refs = resolve(part, valueConf.getCollectionValue());
		Object value;
		if (part.isMultiple()) {
			value = refs;
		} else {
			if (refs.size() > 1) {
				_log.error("Multiple values cannot be stored into singleton reference '" + part + "' at "
					+ valueConf.location());
				value = refs.get(0);
			} else {
				value = CollectionUtil.getSingleValueFromCollection(refs);
			}
		}
		return value;
	}

	private List<Object> resolve(TLStructuredTypePart part, List<ValueConf> references) {
		if (references.isEmpty()) {
			return Collections.emptyList();
		}
		if (references.size() == 1) {
			return Collections.singletonList(resolveSingle(part, references.get(0)));
		}
		ArrayList<Object> result = new ArrayList<>(references.size());
		for (ValueConf ref : references) {
			Object element = resolveSingle(part, ref);
			if (element != null) {
				result.add(element);
			}
		}
		return result;
	}

	private Object resolveSingle(TLStructuredTypePart part, ValueConf ref) {
		return ref.visit(this, part);
	}

	@Override
	public Object visit(PrimitiveValueConf ref, TLStructuredTypePart attribute) {
		String value = ref.getValue();
		return parse(_log, (TLPrimitive) attribute.getType(), value);
	}

	@Override
	public Object visit(CustomValueConf ref, TLStructuredTypePart arg) {
		ValueResolver resolver = _resolvers.valueResolver(arg);
		return resolver.resolve(arg, ref);
	}

	@Override
	public TLObject visit(InstanceRefConf ref, TLStructuredTypePart arg) {
		TLObject result = _objectById.get(ref.getId());
		if (result == null) {
			_log.error("Unresolved reference '" + ref.getId() + "' at " + ref.location());
		}
		return result;
	}

	@Override
	public Object visit(ObjectConf ref, TLStructuredTypePart arg) {
		try {
			createObject(ref);
			return importObject(ref);
		} catch (Exception ex) {
			_log.error("Cannot import object " + ref.getId() + " of type '" + ref.getType() + "'.");
			return null;
		}
	}

	@Override
	public TLObject visit(GlobalRefConf ref, TLStructuredTypePart arg) {
		try {
			String kind = ref.getKind();
			return resolver(kind).resolve(_log, kind, ref.getId());
		} catch (Exception ex) {
			_log.error("Cannot resolve object of type '" + ref.getKind() + "' with ID '" + ref.getId() + "'.", ex);
			return null;
		}
	}

	private InstanceResolver resolver(String kind) {
		InstanceResolver resolver = _resolvers.get(kind);
		if (resolver == null) {
			resolver = _defaultResolver;
		}
		return resolver;
	}

	@Override
	public TLObject visit(ModelRefConf ref, TLStructuredTypePart arg) {
		String name = ref.getName();
		int typeStart = name.indexOf(':');
		int partStart = name.indexOf('#');

		if (typeStart >= 0) {
			String moduleName = name.substring(0, typeStart);
			TLModule module = resolveModule(ref, moduleName);
			if (module == null) {
				return null;
			}
			if (partStart >= 0) {
				String typeName = name.substring(typeStart + 1, partStart);
				TLType type = resolveType(ref, module, typeName);
				if (type == null) {
					return null;
				}

				String partName = name.substring(partStart + 1);

				switch (type.getModelKind()) {
					case DATATYPE: {
						_log.error(
							"Primitive type '" + typeName + "' has no part '" + partName + "' at " + ref.location());
						return null;
					}
					case ENUMERATION: {
						TLClassifier part = resolveClassifier((TLEnumeration) type, partName);
						if (part == null) {
							_log.error(
								"Enum '" + typeName + "' has no classifier '" + partName + "' at " + ref.location());
							return null;
						}
						return part;
					}
					case ASSOCIATION:
					case CLASS: {
						TLStructuredTypePart part = ((TLStructuredType) type).getPart(partName);
						if (part == null) {
							_log.error("Type '" + typeName + "' has no part '" + partName + "' at " + ref.location());
							return null;
						}
						return part;
					}

					default:
						_log.error("Unknown type kind '" + type.getModelKind() + "' of '" + typeName + "' at "
							+ ref.location());
						return null;
				}


			} else {
				String typeName = name.substring(typeStart + 1);
				return resolveType(ref, module, typeName);
			}
		} else {
			if (partStart >= 0) {
				String moduleName = name.substring(0, partStart);

				TLModule module = resolveModule(ref, moduleName);
				if (module == null) {
					return null;
				}

				String partName = name.substring(partStart + 1);
				TLObject singleton = module.getSingleton(partName);
				if (singleton == null) {
					_log.error(
						"No singleton '" + partName + "' defined in module '" + moduleName + "' at " + ref.location());
				}
				return singleton;
			} else {
				String moduleName = name;

				TLModule module = resolveModule(ref, moduleName);
				if (module == null) {
					return null;
				}

				return module;
			}
		}
	}

	private TLClassifier resolveClassifier(TLEnumeration type, String partName) {
		for (TLClassifier classifier : type.getClassifiers()) {
			if (classifier.getName().equals(partName)) {
				return classifier;
			}
		}
		return null;
	}

	private TLModule resolveModule(ModelRefConf ref, String moduleName) {
		TLModule module = _model.getModule(moduleName);
		if (module == null) {
			_log.error("Module '" + moduleName + "' not found in '" + ref.getName() + "' at " + ref.location());
			return null;
		}
		return module;
	}

	private TLType resolveType(ModelRefConf ref, TLModule module, String typeName) {
		TLType type = module.getType(typeName);
		if (type == null) {
			_log.error("Type '" + typeName + "' not found in '" + ref.getName() + "' at " + ref.location());
			return null;
		}
		return type;
	}

	/**
	 * Parses a serialized primitive type value.
	 */
	public static Object parse(Log log, TLPrimitive type, String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return type.getStorageMapping().getBusinessObject(parseStorageValue(log, type.getDBType(), value));
	}

	private static Object parseStorageValue(Log log, DBType dbType, String value) {
		switch (dbType) {
			case BLOB: {
				try {
					byte[] result = Base64.decodeBase64(value);
					return BinaryDataFactory.createBinaryData(result);
				} catch (Exception ex) {
					log.error("Invalid binary format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			}
			case BOOLEAN:
				return Boolean.valueOf(value);
			case DATE:
			case TIME:
			case DATETIME:
				try {
					return XmlDateTimeFormat.INSTANCE.parseObject(value);
				} catch (ParseException ex) {
					log.error("Invalid date format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case DECIMAL:
			case DOUBLE:
				try {
					return Double.parseDouble(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid number format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case FLOAT:
				try {
					return Float.parseFloat(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid number format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case BYTE:
				try {
					return Byte.parseByte(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid number format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case SHORT:
				try {
					return Short.parseShort(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid number format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case INT:
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid number format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case LONG:
				try {
					return Long.parseLong(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid number format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case CLOB:
			case STRING:
			case CHAR:
				return value;
			case ID:
				try {
					return LongID.fromExternalForm(value);
				} catch (NumberFormatException ex) {
					log.error("Invalid ID format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
		}

		throw new UnreachableAssertion("Unsupported DB type: " + dbType);
	}

	/**
	 * Adds a named object to this scenario.
	 * 
	 * @param id
	 *        The name of the given object within this scenario.
	 * @param obj
	 *        The new object.
	 */
	public void addObject(String id, TLObject obj) {
		if (id != null) {
			_objectById.put(id, obj);
		}
	}

	/**
	 * Utility to read an import definition from the given XML contents.
	 * 
	 * @param source
	 *        The XML definition to read.
	 * @return Descriptions of objects to import. See {@link #importInstances(ObjectsConf)}.
	 * 
	 * @see #importInstances(ObjectsConf)
	 */
	public static List<ObjectConf> loadConfigs(Content source) throws ConfigurationException {
		return loadConfig(source).getObjects();
	}

	/**
	 * Utility to read an import definition from the given XML contents.
	 * 
	 * @param source
	 *        The XML definition to read.
	 * @return Descriptions of objects to import. See {@link #importInstances(ObjectsConf)}.
	 * 
	 * @see #importInstances(ObjectsConf)
	 */
	public static ObjectsConf loadConfig(Content source) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(
			new DefaultInstantiationContext(XMLInstanceImporter.class),
			Collections.singletonMap(
				"objects",
				TypedConfiguration.getConfigurationDescriptor(ObjectsConf.class)));
		reader.setSource(source);
		ObjectsConf config = (ObjectsConf) reader.read();
		return config;
	}

}
