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
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.util.ResourcesModule;
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

	private I18NLog _log =
		new LogProtocol(XMLInstanceImporter.class).asI18NLog(ResourcesModule.getInstance().getLogBundle());

	private Map<String, TLObject> _objectById = new HashMap<>();

	private Resolvers _resolvers = new Resolvers(_log);

	private InstanceResolver _defaultResolver = NoInstanceResolver.INSTANCE;

	/**
	 * Actions for resolving forwards references in the import.
	 * 
	 * <p>
	 * A forwards references is the usage of an import-local object ID in a value before the object
	 * that is identified by this ID has been imported.
	 * </p>
	 */
	private List<Runnable> _delayed = new ArrayList<>();

	private Object _context;

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
	 * Arbitrary context object that can be used from {@link InstanceResolver}s to find an object by
	 * a local ID.
	 */
	public Object getContext() {
		return _context;
	}

	/**
	 * @see #getContext()
	 */
	public void setContext(Object context) {
		_context = context;
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
	 *        {@link InstanceResolver#resolve(I18NLog, Object, String, String)}.
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
	 * The {@link I18NLog} that is used for reporting errors in the import process.
	 * 
	 * @see #setLog(I18NLog)
	 */
	public I18NLog getLog() {
		return _log;
	}

	/**
	 * @see #getLog()
	 */
	public void setLog(I18NLog log) {
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

		List<ValueConf> configs = objects.getObjects();
		return importInstances(configs);
	}

	/**
	 * Instantiates all given configurations.
	 * 
	 * @return The objects imported from the given configurations.
	 */
	public List<TLObject> importInstances(List<? extends ValueConf> configs) {
		List<TLObject> result = new ArrayList<>();
		for (ValueConf config : configs) {
			TLObject obj;
			try {
				obj = (TLObject) config.visit(this, null);
			} catch (UnresolvedRef ex) {
				obj = null;
			}
			result.add(obj);
		}

		for (Runnable delayed : _delayed) {
			delayed.run();
		}
		_delayed.clear();

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
				_log.error(I18NConstants.NO_SUCH_PART__ATTR_TYPE_LOC.fill(attrName, type, valueConf.location()));
				continue;
			}

			try {
				loadValue(storage(obj, part, valueConf), valueConf);
			} catch (Exception ex) {
				_log.error(I18NConstants.FAILED_TO_RESOLVE_VALUE__VAL_LOC_MSG.fill(valueConf.getValue(),
					valueConf.location(), ex.getMessage()), ex);
				continue;
			}
		}
		return obj;
	}

	private Storage storage(TLObject obj, TLStructuredTypePart part, AttributeValueConf valueConf) {
		return new Storage() {
			@Override
			public TLStructuredTypePart getPart() {
				return part;
			}

			@Override
			public void set(Object value) {
				Object storageValue;

				if (part.isMultiple()) {
					storageValue = asCollection(value);
				} else {
					if (value instanceof Collection<?> coll) {
						if (coll.size() > 1) {
							_log.error(
								I18NConstants.ERROR_STORING_COLLECTION_TO_SINGLE_REF__ATTR_LOC.fill(part,
									valueConf.location()));
							storageValue = CollectionUtil.getFirst(coll);
						} else {
							storageValue = CollectionUtil.getSingleValueFromCollection(coll);
						}
					} else {
						storageValue = value;
					}
				}

				try {
					obj.tUpdate(part, storageValue);
				} catch (Exception ex) {
					_log.error(
						I18NConstants.FAILED_SETTING_VALUE__VAL_ATTR_LOC_MSG.fill(value, part, valueConf.location(),
							ex.getMessage()),
						ex);
				}
			}
		};
	}

	private static Collection<?> asCollection(Object value) {
		if (value instanceof Collection<?> coll) {
			return coll;
		} else if (value == null) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(value);
		}
	}

	private void loadValue(Storage storage, AttributeValueConf valueConf) {
		if (storage.getPart().getModelKind() == ModelKind.REFERENCE) {
			if (valueConf.getValue() != null) {
				_log.error(I18NConstants.CANNOT_ASSIGN_PLAIN_VALUE_TO_REF__ATTR_VAL_LOC.fill(
					storage.getPart(), valueConf.getValue(), valueConf.location()));
			}
			loadCollectionValue(storage, valueConf);
		} else {
			if (valueConf.getCollectionValue().size() > 0) {
				loadCollectionValue(storage, valueConf);
			} else {
				storage.set(parse(_log, (TLPrimitive) storage.getPart().getType(), valueConf.getValue()));
			}
		}
	}

	private void loadCollectionValue(Storage storage, AttributeValueConf valueConf) {
		loadCollectionValue(storage, valueConf.getCollectionValue());
	}

	private void loadCollectionValue(Storage storage, List<? extends ValueConf> references) {
		if (references.isEmpty()) {
			storage.set(Collections.emptyList());
		}
		else if (references.size() == 1) {
			ValueConf valueConf = references.get(0);

			try {
				loadSingle(storage, valueConf);
			} catch (UnresolvedRef ex) {
				_delayed.add(() -> {
					try {
						loadSingle(storage, valueConf);
					} catch (UnresolvedRef ex2) {
						InstanceRefConf ref = ex2.getRef();
						_log.error(I18NConstants.UNRESOLVED_REFERENCE__ID_LOC.fill(ref.getId(), ref.location()));
					}
				});
			}
		}
		else {
			try {
				loadCollection(storage, references);
			} catch (UnresolvedRef ex) {
				_delayed.add(() -> {
					try {
						loadCollection(storage, references);
					} catch (UnresolvedRef ex2) {
						InstanceRefConf ref = ex2.getRef();
						_log.error(I18NConstants.UNRESOLVED_REFERENCE__ID_LOC.fill(ref.getId(), ref.location()));
					}
				});
			}
		}
	}

	private void loadSingle(Storage storage, ValueConf valueConf) throws UnresolvedRef {
		storage.set(resolveSingle(storage.getPart(), valueConf));
	}

	private void loadCollection(Storage storage, List<? extends ValueConf> references) throws UnresolvedRef {
		ArrayList<Object> result = new ArrayList<>(references.size());
		for (ValueConf ref : references) {
			Object element = resolveSingle(storage.getPart(), ref);
			if (element != null) {
				result.add(element);
			}
		}
		storage.set(result);
	}

	private Object resolveSingle(TLStructuredTypePart part, ValueConf ref) throws UnresolvedRef {
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
	public TLObject visit(InstanceRefConf ref, TLStructuredTypePart arg) throws UnresolvedRef {
		TLObject result = _objectById.get(ref.getId());
		if (result == null) {
			throw new UnresolvedRef(ref);
		}
		return result;
	}

	@Override
	public Object visit(ObjectConf ref, TLStructuredTypePart arg) {
		try {
			String globalId = ref.getGlobalId();
			if (globalId != null) {
				// Check, whether the object is already present in the target system.
				String kind = ref.getType();
				TLObject existing = resolver(kind).resolve(_log, _context, kind, globalId);
				if (existing != null) {
					addObject(ref.getId(), existing);
					return existing;
				}
			}
			createObject(ref);
			return importObject(ref);
		} catch (Exception ex) {
			_log.error(I18NConstants.FAILED_TO_IMPORT__ID_TYPE_MSG.fill(ref.getId(), ref.getType(), ex.getMessage()), ex);
			return null;
		}
	}

	@Override
	public TLObject visit(GlobalRefConf ref, TLStructuredTypePart arg) {
		try {
			String kind = ref.getKind();
			TLObject result = resolver(kind).resolve(_log, _context, kind, ref.getId());
			if (result == null) {
				_log.error(
					I18NConstants.FAILED_TO_RESOLVE_OBJECT__TYPE_ID.fill(ref.getKind(), ref.getId()));
			} else {
				String localId = ref.getLocalId();
				if (localId != null) {
					// Remember for later use without resolving the global reference again.
					addObject(localId, result);
				}
			}
			return result;
		} catch (Exception ex) {
			_log.error(
				I18NConstants.FAILED_TO_RESOLVE_OBJECT__TYPE_ID_MSG.fill(ref.getKind(), ref.getId(), ex.getMessage()),
				ex);
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
						_log.error(I18NConstants.PRIMITIVE_TYPE_WITH_PART__TYPE_PART_LOC.fill(typeName, partName,
							ref.location()));
						return null;
					}
					case ENUMERATION: {
						TLClassifier part = resolveClassifier((TLEnumeration) type, partName);
						if (part == null) {
							_log.error(I18NConstants.NO_SUCH_CLASSIFIER__TYPE_PART_LOC.fill(typeName, partName,
								ref.location()));
							return null;
						}
						return part;
					}
					case ASSOCIATION:
					case CLASS: {
						TLStructuredTypePart part = ((TLStructuredType) type).getPart(partName);
						if (part == null) {
							_log.error(
								I18NConstants.NO_SUCH_PART__TYPE_PART_LOC.fill(typeName, partName, ref.location()));
							return null;
						}
						return part;
					}

					default:
						_log.error(I18NConstants.UNKNOWN_TYPE__KIND_TYPE_LOC.fill(type.getModelKind(), typeName,
							ref.location()));
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
						I18NConstants.NO_SUCH_SINGLETON__NAME_MODULE_LOC.fill(partName, moduleName, ref.location()));
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
			_log.error(I18NConstants.NO_SUCH_MODULE__NAME_REF_LOC.fill(moduleName, ref.getName(), ref.location()));
			return null;
		}
		return module;
	}

	private TLType resolveType(ModelRefConf ref, TLModule module, String typeName) {
		TLType type = module.getType(typeName);
		if (type == null) {
			_log.error(I18NConstants.NO_SUCH_TYPE__NAME_REF_LOC.fill(typeName, ref.getName(), ref.location()));
			return null;
		}
		return type;
	}

	/**
	 * Parses a serialized primitive type value.
	 */
	public static Object parse(I18NLog log, TLPrimitive type, String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return type.getStorageMapping().getBusinessObject(parseStorageValue(log, type.getDBType(), value));
	}

	private static Object parseStorageValue(I18NLog log, DBType dbType, String value) {
		switch (dbType) {
			case BLOB: {
				try {
					byte[] result = Base64.decodeBase64(value);
					return BinaryDataFactory.createBinaryData(result);
				} catch (Exception ex) {
					String prefix =
						value.substring(0, Math.min(20, value.length())) + (value.length() > 40 ? "..." : "");
					log.error(I18NConstants.INVALID_BINARY_FORMAT__VAL_MSG.fill(prefix, ex.getMessage()), ex);
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
					log.error(I18NConstants.INVALID_DATE_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
					return null;
				}
			case DECIMAL:
			case DOUBLE:
				try {
					return Double.parseDouble(value);
				} catch (NumberFormatException ex) {
					log.error(I18NConstants.INVALID_NUMBER_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
					return null;
				}
			case FLOAT:
				try {
					return Float.parseFloat(value);
				} catch (NumberFormatException ex) {
					log.error(I18NConstants.INVALID_NUMBER_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
					return null;
				}
			case BYTE:
				try {
					return Byte.parseByte(value);
				} catch (NumberFormatException ex) {
					log.error(I18NConstants.INVALID_NUMBER_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
					return null;
				}
			case SHORT:
				try {
					return Short.parseShort(value);
				} catch (NumberFormatException ex) {
					log.error(I18NConstants.INVALID_NUMBER_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
					return null;
				}
			case INT:
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException ex) {
					log.error(I18NConstants.INVALID_NUMBER_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
					return null;
				}
			case LONG:
				try {
					return Long.parseLong(value);
				} catch (NumberFormatException ex) {
					log.error(I18NConstants.INVALID_NUMBER_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
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
					log.error(I18NConstants.INVALID_ID_FORMAT__VAL_MSG.fill(value, ex.getMessage()), ex);
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
	public static List<ValueConf> loadConfigs(Content source) throws ConfigurationException {
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

	/**
	 * A storage location, where a value can be stored.
	 */
	interface Storage {
		/**
		 * The attribute that describes this storage location.
		 */
		TLStructuredTypePart getPart();

		/**
		 * The setter that accepts the value to be stored.
		 */
		void set(Object value);
	}

}
