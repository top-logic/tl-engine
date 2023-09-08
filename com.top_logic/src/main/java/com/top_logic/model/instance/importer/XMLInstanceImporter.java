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
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.instance.importer.resolver.NoInstanceResolver;
import com.top_logic.model.instance.importer.schema.AttributeValueConf;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;
import com.top_logic.model.instance.importer.schema.InstanceRefConf;
import com.top_logic.model.instance.importer.schema.ModelRefConf;
import com.top_logic.model.instance.importer.schema.ObjectConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.model.instance.importer.schema.RefConf;
import com.top_logic.model.instance.importer.schema.RefVisitor;
import com.top_logic.model.util.TLModelUtil;

/**
 * Object instance importer instantiating interconnected objects based on a XML configuration.
 * 
 * @see ObjectsConf
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLInstanceImporter implements RefVisitor<TLObject, Void> {

	private final TLFactory _factory;

	private final TLModel _model;

	private Log _log = new LogProtocol(XMLInstanceImporter.class);

	private Map<String, TLObject> _objectById = new HashMap<>();

	private Map<String, InstanceResolver> _resolvers = new HashMap<>();

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
	 *        {@link InstanceResolver#resolve(String, String)}.
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
	 * All objects that have been imported in the call to {@link #importInstances(List)}.
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
	}

	/**
	 * Starts the import of the given import description.
	 * 
	 * @param configs
	 *        The description of the objects to import, see {@link ObjectsConf#getObjects()}.
	 */
	public void importInstances(List<ObjectConf> configs) throws ConfigurationException {
		for (ObjectConf config : configs) {
			TLClass type = (TLClass) TLModelUtil.findType(_model, config.getType());
			TLObject obj = _factory.createObject(type, null, null);
			addObject(config.getId(), obj);
		}

		for (ObjectConf config : configs) {
			TLObject obj = _objectById.get(config.getId());
			TLClass type = (TLClass) obj.tType();
			for (AttributeValueConf valueConf : config.getAttributes()) {
				String attrName = valueConf.getName();
				TLStructuredTypePart part = type.getPart(attrName);
				if (part == null) {
					_log.error("No such part '" + attrName + "' in type '" + type + "' at " + valueConf.location());
					continue;
				}
				if (part.getModelKind() == ModelKind.REFERENCE) {
					if (valueConf.getValue() != null) {
						_log.error("Reference attribute must not be assigned a plain value at " + valueConf.location());
					}
					List<TLObject> refs = resolve(valueConf.getReferences());
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
					obj.tUpdate(part, value);
				} else {
					if (valueConf.getReferences().size() > 0) {
						_log.error("Plain attribute must not be assigned a reference value at " + valueConf.location());
					}
					obj.tUpdate(part, parse((TLPrimitive) part.getType(), valueConf.getValue()));
				}
			}
		}
	}

	private List<TLObject> resolve(List<RefConf> references) {
		if (references.isEmpty()) {
			return Collections.emptyList();
		}
		if (references.size() == 1) {
			return Collections.singletonList(resolveSingle(references.get(0)));
		}
		ArrayList<TLObject> result = new ArrayList<>(references.size());
		for (RefConf ref : references) {
			TLObject element = resolveSingle(ref);
			if (element != null) {
				result.add(element);
			}
		}
		return result;
	}

	private TLObject resolveSingle(RefConf ref) {
		return ref.visit(this, null);
	}

	@Override
	public TLObject visit(InstanceRefConf ref, Void arg) {
		TLObject result = _objectById.get(ref.getId());
		if (result == null) {
			_log.error("Unresolved reference '" + ref.getId() + "' at " + ref.location());
		}
		return result;
	}

	@Override
	public TLObject visit(GlobalRefConf ref, Void arg) {
		String kind = ref.getKind();
		return resolver(kind).resolve(kind, ref.getId());
	}

	private InstanceResolver resolver(String kind) {
		InstanceResolver resolver = _resolvers.get(kind);
		if (resolver == null) {
			resolver = _defaultResolver;
		}
		return resolver;
	}

	@Override
	public TLObject visit(ModelRefConf ref, Void arg) {
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

	private Object parse(TLPrimitive type, String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return type.getStorageMapping().getBusinessObject(parseStorageValue(type.getDBType(), value));
	}

	private Object parseStorageValue(DBType dbType, String value) {
		switch (dbType) {
			case BLOB: {
				byte[] result = Base64.decodeBase64(value);
				return BinaryDataFactory.createBinaryData(result);
			}
			case BOOLEAN:
				return Boolean.valueOf(value);
			case DATE:
			case TIME:
			case DATETIME:
				try {
					return XmlDateTimeFormat.INSTANCE.parseObject(value);
				} catch (ParseException ex) {
					_log.error("Invalid date format in '" + value + "':" + ex.getMessage(), ex);
					return null;
				}
			case DECIMAL:
			case DOUBLE:
				return Double.parseDouble(value);
			case FLOAT:
				return Float.parseFloat(value);
			case BYTE:
				return Byte.parseByte(value);
			case SHORT:
				return Short.parseShort(value);
			case INT:
				return Integer.parseInt(value);
			case LONG:
				return Long.parseLong(value);
			case CLOB:
			case STRING:
			case CHAR:
				return value;
			case ID:
				return LongID.fromExternalForm(value);
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
	 * @return Descriptions of objects to import. See {@link #importInstances(List)}.
	 * 
	 * @see #importInstances(List)
	 */
	public static List<ObjectConf> loadConfigs(Content source) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(
			new DefaultInstantiationContext(XMLInstanceImporter.class),
			Collections.singletonMap(
				"objects",
				TypedConfiguration.getConfigurationDescriptor(ObjectsConf.class)));
		reader.setSource(source);
		ObjectsConf config = (ObjectsConf) reader.read();
		return config.getObjects();
	}

}
