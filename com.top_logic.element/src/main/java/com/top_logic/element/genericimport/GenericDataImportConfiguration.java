/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverter;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;
import com.top_logic.element.genericimport.interfaces.GenericCreateHandler;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;
import com.top_logic.element.genericimport.interfaces.GenericImporter;
import com.top_logic.element.genericimport.interfaces.GenericTypeResolver;
import com.top_logic.element.genericimport.interfaces.GenericUpdateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValidator;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.knowledge.objects.CreateException;

/**
 * Configuration of a generic data import, read from an XML file.
 *
 * <p>
 * Defines the global import components (importer, cache, type resolver, logger) and per internal
 * type the converter, validator, create and update handlers and the column-attribute mappings.
 * </p>
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportConfiguration {

    private static final Class[] SIGNATURE = new Class[] { Properties.class };

    // global config keys
    private static final String IMPORTER              = "importer";
    private static final String CACHE                 = "cache";
    private static final String TYPE_RESOLVER         = "typeResolver";
    private static final String LOGGER                = "logger";

    // typed config keys
    private static final String TYPED_SECTION         = "typed";
    private static final String FOREIGN_KEY           = "foreignKey";
    private static final String CONVERTER             = "converter";

    // special converter config, Holds columnAttributeMappings
    private static final String IDX_CONVERTER_MAPPING = "idxcm";
    private static final String UPDATE_HANDLER        = "updateHandler";
    private static final String CREATE_HANDLER        = "createHandler";
    private static final String VALIDATOR             = "validator";

    // instance cache for converters, importers etc...
    private static final String INSTANCE_SUFFIX       = "inst";

    // properties suffix
    private static final String PROPERTIES_SUFFIX     = "props";
    private static final String GLOBAL                = "global";

    private static final String PROP_CLASS            = "class";
    private static final String PROP_NAME             = "name";
    /**
     * Name of the property holding the internal type a typed configuration section belongs to.
     */
    public  static final String PROP_TYPE             = "type";

    /**
     * Name of the global option that decides whether the import commits its changes.
     */
    public static final String DO_COMMIT = "commit";
    /**
     * Name of the global option that decides whether the import creates new objects.
     */
    public static final String DO_CREATE = "create";
    /**
     * Name of the global option that decides whether the import updates existing objects.
     */
    public static final String DO_UPDATE = "update";

    // the config is a Map of Maps of Maps
    // Map<aType, Map<anIDX, Map<Key,Value>>>
    private Map config = new HashMap();

    protected GenericDataImportConfiguration() {
        this.config = new HashMap();
        this.config.put(TYPED_SECTION, new HashMap());
    }

    private Object getOrInitInstance(String anObjectName) {
        Object theInstance = this.getValue(anObjectName + INSTANCE_SUFFIX);
        if (theInstance == null) {
            Properties theProps = this.getProperties(anObjectName);
            theInstance = getInstanceOf(theProps, PROP_CLASS, true);
            if (theInstance instanceof GenericDataImportConfigurationAware) {
                ((GenericDataImportConfigurationAware) theInstance).setImportConfiguration(this, null);
            }
            this.setValue(anObjectName + INSTANCE_SUFFIX, theInstance);
        }
        return theInstance;
    }

    private Object getOrInitTypedInstance(String aType, String anObjectName) {
        Object theInstance = this.getTypedValue(aType, anObjectName + INSTANCE_SUFFIX);
        if (theInstance == null) {
            Properties theProps = this.getTypedProperties(aType, anObjectName);
            theInstance = getInstanceOf(theProps, PROP_CLASS, true);
            if (theInstance instanceof GenericDataImportConfigurationAware) {
                ((GenericDataImportConfigurationAware) theInstance).setImportConfiguration(this, aType);
            }
            this.setTypedValue(aType, anObjectName + INSTANCE_SUFFIX, theInstance);
        }
        return theInstance;
    }

    /**
     * Sets whether the import commits its changes.
     * @see #isDoCommit()
     */
    public void setDoCommit(boolean doCommit) {
        Properties theProps = this.getProperties(GLOBAL);
        theProps.setProperty("commit", Boolean.valueOf(doCommit).toString());
    }

    /**
     * Sets whether the import updates existing objects.
     * @see #isDoUpdate()
     */
    public void setDoUpdate(boolean doUpdate) {
        Properties theProps = this.getProperties(GLOBAL);
        theProps.setProperty("update", Boolean.valueOf(doUpdate).toString());
    }

    /**
     * Sets whether the import creates new objects.
     * @see #isDoCreate()
     */
    public void setDoCreate(boolean doCreate) {
        Properties theProps = this.getProperties(GLOBAL);
        theProps.setProperty("create", Boolean.valueOf(doCreate).toString());
    }

    /**
     * Whether the import commits its changes (default: {@code false}).
     */
    public boolean isDoCommit() {
        Properties theProps = this.getProperties(GLOBAL);
        return Boolean.valueOf(theProps.getProperty("commit", "false")).booleanValue();
    }

    /**
     * Whether the import creates new objects (default: {@code true}).
     */
    public boolean isDoCreate() {
        Properties theProps = this.getProperties(GLOBAL);
        return Boolean.valueOf(theProps.getProperty("create", "true")).booleanValue();
    }

    /**
     * Whether the import updates existing objects (default: {@code true}).
     */
    public boolean isDoUpdate() {
        Properties theProps = this.getProperties(GLOBAL);
        return Boolean.valueOf(theProps.getProperty("update", "true")).booleanValue();
    }

    /**
     * The number of imported objects after which a commit is done (default: 20).
     */
    public int getCommitInterval() {
        Properties theProps = this.getProperties(GLOBAL);
        return Integer.valueOf(theProps.getProperty("commitInterval", "20")).intValue();
    }

    /**
     * The logger receiving the progress and messages of the import.
     */
    public LoggerProgressInfo getLogger() {
        return (LoggerProgressInfo) this.getOrInitInstance(LOGGER);
    }

    /**
     * The importer reading the raw data to import.
     */
    public GenericImporter getImporter() {
        return (GenericImporter) this.getOrInitInstance(IMPORTER);
    }

    /**
     * The cache of already existing and imported objects.
     */
    public GenericCache getCache() {
        return (GenericCache) this.getOrInitInstance(CACHE);
    }

    /**
     * The resolver that determines the internal type of an imported data set.
     */
    public GenericTypeResolver getTypeResolver() {
        return (GenericTypeResolver) this.getOrInitInstance(TYPE_RESOLVER);
    }

    /**
     * The validator for objects of the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public GenericValidator getValidator(String aType) {
        return (GenericValidator) this.getOrInitTypedInstance(aType, VALIDATOR);
    }

    /**
     * The handler creating new objects of the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public GenericCreateHandler getCreateHandler(String aType) {
        return (GenericCreateHandler) this.getOrInitTypedInstance(aType, CREATE_HANDLER);
    }

    /**
     * The handler updating existing objects of the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public GenericUpdateHandler getUpdateHandler(String aType) {
        return (GenericUpdateHandler) this.getOrInitTypedInstance(aType, UPDATE_HANDLER);
    }

    /**
     * The converter transforming imported data of the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public GenericConverter getConverter(String aType) {
        return (GenericConverter) this.getOrInitTypedInstance(aType, CONVERTER);
    }

    private synchronized void setProperties(String aKey, Properties someProps) {
        this.config.put(aKey + PROPERTIES_SUFFIX, someProps);
    }

    private synchronized Properties getProperties(String aKey) {
        Properties theProps = (Properties) this.config.get(aKey + PROPERTIES_SUFFIX);
        if (theProps == null) {
            theProps = new Properties();
            this.setProperties(aKey, theProps);
        }
        return theProps;
    }

    private synchronized void setValue(String aKey, Object aValue) {
        this.config.put(aKey, aValue);
    }

    private Object getValue(String aKey) {
        return this.config.get(aKey);
    }

    private Map getTypedConfig() {
        return (Map) this.config.get(TYPED_SECTION);
    }

    /**
     * All internal types for which a typed configuration exists.
     */
    public Set getInternalTypes() {
        return Collections.unmodifiableSet(getTypedConfig().keySet());
    }

    /**
     * The name of the attribute that identifies objects of the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public String getForeignKey(String aType) {
        return this.getTypedProperties(aType, FOREIGN_KEY).getProperty(PROP_NAME);
    }

    /**
     * Sets the name of the attribute that identifies objects of the given internal type.
     *
     * @see #getForeignKey(String)
     */
    public void setForeignKey(String aType, String anAttr) {
        this.getTypedProperties(aType, FOREIGN_KEY).setProperty(PROP_NAME, anAttr);
    }

    private synchronized void setTypedValue(String aType, Object aKey, Object aValue) {
        Map theConf = this.getOrInitTypedConfig(aType);
        theConf.put(aKey, aValue);
    }

    private Object getTypedValue(String aType, Object aKey) {
        Map    theMap = (Map) this.getTypedConfig().get(aType);
        return theMap != null ? theMap.get(aKey) : null;
    }

    private Properties getTypedProperties(String aType, String aKey) {
        return (Properties) this.getTypedValue(aType, aKey + PROPERTIES_SUFFIX);
    }

    /**
     * Sets the properties of a typed configuration section.
     *
     * @param aType
     *        The internal type name.
     * @param aKey
     *        The name of the configuration section.
     * @param someProps
     *        The properties of the section.
     */
    protected synchronized void setTypedProperties(String aType, String aKey, Properties someProps) {
        this.setTypedValue(aType, aKey + PROPERTIES_SUFFIX, someProps);
    }

    private synchronized Map getOrInitTypedConfig(String aType) {
        Map theConf = (Map) this.getTypedConfig().get(aType);
        if (theConf == null) {
            theConf = new HashMap(2);
            theConf.put(IDX_CONVERTER_MAPPING, new HashMap());
            this.getTypedConfig().put(aType, theConf);
        }
        return theConf;
    }

    /**
     * Registers the given column-attribute mapping for the given internal type, replacing a
     * mapping for the same attribute.
     *
     * @param aType
     *        The internal type name.
     * @param aMapping
     *        The new mapping.
     */
    public synchronized void setMapping(String aType, ColumnAttributeMapping aMapping) {
        Map theConf = this.getOrInitTypedConfig(aType);

        ((Map) theConf.get(IDX_CONVERTER_MAPPING)).put(aMapping.getAttributeName(), aMapping);
    }

    /**
     * The names of all import columns mapped for the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public Set getColumns(String aType) {
        Map theConf = ((Map) this.getOrInitTypedConfig(aType).get(IDX_CONVERTER_MAPPING));

        Set theCols = new HashSet(theConf.size());

        for (Iterator theIter = theConf.values().iterator(); theIter.hasNext();) {
            ColumnAttributeMapping theMapping = (ColumnAttributeMapping) theIter.next();
            if (!StringServices.isEmpty(theMapping.getColumn())) {
                theCols.add(theMapping.getColumn());
            }
        }

        return theCols;
    }

    /**
     * The names of all attributes mapped for the given internal type.
     *
     * @param aType
     *        The internal type name.
     */
    public Set getAttributes(String aType) {
        return this.getIndexKeys(aType, IDX_CONVERTER_MAPPING);
    }

    private Set getIndexKeys(String aType, String aIdx) {
        Map    theConf = (Map) this.getTypedConfig().get(aType);
        return theConf == null ? Collections.EMPTY_SET : ((Map) theConf.get(aIdx)).keySet();
    }

    /**
     * The mapping of the given import column for the given internal type.
     *
     * @param aType
     *        The internal type name.
     * @param aColumn
     *        The name of the import column.
     * @return The mapping, or {@code null} if the column is not mapped.
     */
    public ColumnAttributeMapping getMappingForColumn(String aType, String aColumn) {

        Map theConf = ((Map) this.getOrInitTypedConfig(aType).get(IDX_CONVERTER_MAPPING));

        for (Iterator theIter = theConf.values().iterator(); theIter.hasNext();) {
            ColumnAttributeMapping theMapping = (ColumnAttributeMapping) theIter.next();
            if (aColumn.equals(theMapping.getColumn())) {
                return theMapping;
            }
        }

        return null;
    }

    /**
     * The mapping of the given attribute for the given internal type.
     *
     * @param aType
     *        The internal type name.
     * @param anAttributeName
     *        The name of the attribute.
     * @return The mapping, or {@code null} if the attribute is not mapped.
     */
    public ColumnAttributeMapping getMappingForAttribute(String aType, String anAttributeName) {
        return this.getMapping(aType, IDX_CONVERTER_MAPPING, anAttributeName);
    }

    private ColumnAttributeMapping getMapping(String aType, String aIdx, String aKey) {
        Map    theConf = (Map) this.getTypedConfig().get(aType);
        return theConf == null ? null : (ColumnAttributeMapping) ((Map) theConf.get(aIdx)).get(aKey);
    }

    /**
     * Mapping of an import column to an attribute of the imported objects, optionally with a
     * function converting the imported value.
     */
    public static class ColumnAttributeMapping {

        private String  column;
        private final String attribute;
        private final String referenceTarget;
        private boolean isReference;
        private boolean allowChange;
        private GenericConverterFunction function;
        private boolean ignoreExistance;

        /**
         * Create a new {@link ColumnAttributeMapping}
         * @param anAttributeName must not be null
         */
        public ColumnAttributeMapping(String aColumn, String anAttributeName, GenericConverterFunction aFunction) {
            this(aColumn, anAttributeName, false, null, aFunction, true, false);
        }



        /**
         * Whether the import ignores that a referenced object does not exist.
         */
        public boolean isIgnoreExistance() {
            return (this.ignoreExistance);
        }

        
        protected ColumnAttributeMapping(String aColumn, String anAttributeName, boolean isRef, String referenceTarget, GenericConverterFunction aFunction, boolean allowFunctionChange, boolean anIgnoreExistance) {
            if (StringServices.isEmpty(anAttributeName)) {
                throw new IllegalArgumentException("attribute name must not be null!");
            }
            this.column    = aColumn;
            this.attribute = anAttributeName;
            this.referenceTarget = referenceTarget;
            this.isReference = ! StringServices.isEmpty(this.referenceTarget) || isRef;

            this.function  = aFunction;
            this.allowChange = allowFunctionChange;
            this.ignoreExistance = anIgnoreExistance;
        }

        /**
         * This method returns the attribute.
         *
         * @return    Returns the attribute.
         */
        public String getAttributeName() {
            return (this.attribute);
        }

        /**
         * Returns whether this mapping is for a reference type attribute.
         */
        public boolean isReference() {
            return this.isReference;
        }

        /**
         * Sets whether this mapping is for a reference type attribute.
         *
         * @see #isReference()
         */
        public void setIsReference(boolean aIsReference) {
            this.isReference = aIsReference;
        }

        /**
         * The name of the internal type of objects referenced by this mapping, or {@code null}.
         */
        public String getReferenceTarget() {
            return this.referenceTarget;
        }

        /**
         * This method returns the column.
         *
         * @return    Returns the column.
         */
        public String getColumn() {
            return (this.column);
        }

        /**
         * This method sets the column.
         *
         * @param    aColumn    The column to set.
         */
        public void setColumn(String aColumn) {
            this.column = aColumn;
        }

        /**
         * This method returns the function.
         *
         * @return    Returns the function.
         */
        public GenericConverterFunction getFunction() {
            return (this.function);
        }

        /**
         * Sets the function converting the imported value.
         *
         * <p>
         * The function is only changed, if this mapping allows a change of its function.
         * </p>
         *
         * @see #getFunction()
         */
        public void setFunction(GenericConverterFunction aMapping) {
            if (allowChange) {
                this.function = aMapping;
            }
        }
    }

    /**
     * Creates an instance of the class named in the given properties.
     *
     * <p>
     * The class is instantiated with a constructor taking the given {@link Properties}, if such
     * constructor exists, with its default constructor otherwise.
     * </p>
     *
     * @param aProp
     *        The properties naming the class and passed to the instance.
     * @param aKey
     *        The name of the property holding the class name.
     * @param isMandatory
     *        Whether a missing class name is an error.
     * @return The new instance, or {@code null} if no class is given or the class cannot be
     *         instantiated.
     */
    public static Object getInstanceOf(Properties aProp, String aKey, boolean isMandatory) {
        String theClass = aProp.getProperty(aKey);

        if (! StringServices.isEmpty(theClass)) {
            try {
                Class theClazz = Class.forName(theClass);

                try {
                    Constructor theConst = theClazz.getConstructor(SIGNATURE);
                    return theConst.newInstance(new Object[] { aProp });
                } catch (NoSuchMethodException nsmx) {
                    // ignore
                } catch (InstantiationException iex) {
                    // ignore
                }

                // fallback to default constructor
                return theClazz.newInstance();
            } catch (Exception ex) {
                Logger.error("Unable to instanciate class " + theClass, ex, GenericDataImportTask.class);
            }
        } else if (isMandatory) {
            throw new IllegalArgumentException(aKey + " must be configured!");
        }

        return null;
    }

    /**
     * Reads an import configuration from the given XML file.
     *
     * @param aFilename
     *        The name of the file, resolved by the {@link FileManager}.
     * @return The parsed configuration, or {@code null} in case of an error.
     */
    public static GenericDataImportConfiguration readConfiguration(String aFilename) throws IOException {
		try (InputStream in = FileManager.getInstance().getStream(aFilename)) {
			return GenericDataImportConfiguration.readConfiguration(in);
		}
    }

    /**
     * Reads an import configuration from the given XML stream.
     *
     * @param input
     *        The stream with the XML definition.
     * @return The parsed configuration, or {@code null} in case of an error.
     */
    public static GenericDataImportConfiguration readConfiguration(InputStream input) {
        try {
			DocumentBuilder theBuilder = DOMUtil.newDocumentBuilder();
            Document        theDoc     = theBuilder.parse(input);

            GenericDataImportConfigurationParser theParser = new GenericDataImportConfigurationParser();
            theParser.parse(theDoc);

            return theParser.config;
        }
        catch (Exception e) {
            Logger.error("Error while reading generic import converter config from XML.", e, GenericDataImportConfiguration.class);
            return null;
        }
    }

    private static interface NodeHandler {
        public void handleNode(Node aNode);
    }

    private static class GenericDataImportConfigurationRecursiveParser implements NodeHandler {

        Map dispatch;

        public GenericDataImportConfigurationRecursiveParser() {
            this.dispatch = new HashMap();
        }

        public void registerHandler(String aTagname, NodeHandler aHandler) {
            this.dispatch.put(aTagname, aHandler);
        }

        @Override
		public final void handleNode(Node aNode) {
            this._handleNode(aNode);

            NodeList theChilds = aNode.getChildNodes();

            Set unHandled = new HashSet();
            for (int i=0; i<theChilds.getLength(); i++) {
                Node        theNode    = theChilds.item(i);
                String      theName    = theNode.getNodeName();
                NodeHandler theHandler = (NodeHandler) this.dispatch.get(theName);
                if (theHandler != null) {
                    theHandler.handleNode(theNode);
                }
                else if (theNode.getNodeType() == Node.ELEMENT_NODE){
                    unHandled.add(theName);
                }
            }
            if (! unHandled.isEmpty()) {
                throw new IllegalArgumentException("Unhandled tags found: " + StringServices.toString(unHandled, ","));
            }
        }

        public void _handleNode(Node aNode) {
        }
    }

    private static class GenericDataImportConfigurationParser extends GenericDataImportConfigurationRecursiveParser {

        private static final String TAG_META_ELEMENT              = "element";
        private static final String ATTR_NAME                     = PROP_NAME;

        private static final String TAG_GLOBAL                    = GLOBAL;
        private static final String TAG_LOGGER                    = LOGGER;
        private static final String TAG_IMPORTER                  = IMPORTER;
        private static final String TAG_CONVERTER                 = CONVERTER;
        private static final String TAG_TYPE_RESOLVER             = TYPE_RESOLVER;
        private static final String TAG_UPDATE_HANDLER            = UPDATE_HANDLER;
        private static final String TAG_CREATE_HANDLER            = CREATE_HANDLER;
        private static final String TAG_CACHE                     = CACHE;
        private static final String TAG_VALIDATOR                 = VALIDATOR;
        private static final String TAG_ENTRY                     = "entry";

        private static final String TAG_FOREIGN_KEY               = FOREIGN_KEY;

        private static final String TAG_STATIC_CA_MAPPING         = "columnAttributeMapping";
        private static final String TAG_MAPPING                   = "mapping";
        private static final String TAG_VALUE_MAPPING             = "valueMapping";

        private static final String ATTR_COLUMN                   = "column";
        private static final String ATTR_ATTRIBUTE                = "attribute";
        private static final String ATTR_IS_REFERENCE             = "isReference";
        private static final String ATTR_REFERENCETARGET          = "referenceTarget";
        private static final String ATTR_IGNORE_EXISTANCE         = "ignoreExistance";

        public  static final String ATTR_CLASS                    = PROP_CLASS;
        public  static final String ATTR_VALUE                    = "value";

        // Constructors

        GenericDataImportConfiguration config;

        /**
         * Creates a {@link MetaElementBasedConverter}.ConfigParser.
         *
         */
        protected GenericDataImportConfigurationParser() {
            config = new GenericDataImportConfiguration();

            this.registerHandler(TAG_IMPORTER,     new GenericDataImportConfigSectionHandler());
            this.registerHandler(TAG_GLOBAL,       new GenericDataImportConfigSectionHandler());
            this.registerHandler(TAG_LOGGER,       new GenericDataImportConfigSectionHandler());
            this.registerHandler(TAG_CACHE,        new GenericDataImportConfigSectionHandler());
            this.registerHandler(TAG_TYPE_RESOLVER,new GenericDataImportConfigSectionHandler());
            this.registerHandler(TAG_META_ELEMENT, new GenericDataImportConfigElementHandler());
        }

        public void parse(Document aDoc) {
            this.handleNode(aDoc.getFirstChild());
        }

        /**
         * Puts name-value-pair from <xyzTag name="" value="" /> into the given properties
         *
         * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
         */
        private class GenericDataImportConfigXMLPropertiesHandler implements NodeHandler {

            private Properties properties;

            public GenericDataImportConfigXMLPropertiesHandler(Properties someProperties) {
                this.properties = someProperties;
            }

            @Override
			public void handleNode(Node aNode) {
                NamedNodeMap theAttr = aNode.getAttributes();
                this.properties.setProperty(NodeHelper.getAsString(theAttr, ATTR_NAME), NodeHelper.getAsString(theAttr, ATTR_VALUE));
            }
        }

        /**
         * Puts all attribute=value pairs of an Node into internal properties.
         * Sets these properties to the {@link GenericDataImportConfiguration#setTypedValue(String, Object , Object)}
         *
         * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
         */
        private class GenericDataImportConfigSectionHandler extends GenericDataImportConfigurationRecursiveParser {

            protected String     type;
            protected Properties properties;

            public GenericDataImportConfigSectionHandler() {
                this(null);
            }

            public GenericDataImportConfigSectionHandler(String aType) {
                this.type       = aType;
                this.properties = new Properties();
                if (this.type != null) {
                    this.properties.setProperty(PROP_TYPE, this.type);
                }
                this.registerHandler(TAG_ENTRY, new GenericDataImportConfigXMLPropertiesHandler(properties));
            }

            @Override
			public void _handleNode(Node aNode) {
                NamedNodeMap theAttrs = aNode.getAttributes();
                for (int i=0; i<theAttrs.getLength(); i++) {
                    Node theNode = theAttrs.item(i);
                    this.properties.setProperty(theNode.getNodeName(), theNode.getNodeValue());
                }
                if (this.type != null) {
                    config.setTypedProperties(type, aNode.getNodeName(), this.properties);
                }
                else {
                    config.setProperties(aNode.getNodeName(), this.properties);
                }
            }
        }

        private class GenericDataImportConfigElementHandler extends GenericDataImportConfigSectionHandler {

            public GenericDataImportConfigElementHandler() {
                super(null);
            }

            @Override
			public void _handleNode(Node aNode) {
                super._handleNode(aNode);
                this.type = NodeHelper.getAsString(aNode.getAttributes(), ATTR_NAME);

                this.registerHandler(TAG_CONVERTER,      new GenericDataImportConfigConverterHandler(this.type));
                this.registerHandler(TAG_UPDATE_HANDLER, new GenericDataImportConfigSectionHandler(this.type));
                this.registerHandler(TAG_CREATE_HANDLER, new GenericDataImportConfigSectionHandler(this.type));
                this.registerHandler(TAG_VALIDATOR,      new GenericDataImportConfigSectionHandler(this.type));
                this.registerHandler(TAG_FOREIGN_KEY,    new GenericDataImportConfigSectionHandler(this.type));
            }
        }

        private class GenericDataImportConfigConverterHandler extends GenericDataImportConfigSectionHandler {
            public GenericDataImportConfigConverterHandler(String aType) {
                super(aType);
                this.registerHandler(TAG_STATIC_CA_MAPPING, new GenericDataImportConfigCAMappingsHandler(aType));
                this.registerHandler(TAG_VALUE_MAPPING,     new GenericDataImportConfigValueMappingsHandler(aType));
            }
        }

        private class GenericDataImportConfigCAMappingsHandler extends GenericDataImportConfigurationRecursiveParser {
            public GenericDataImportConfigCAMappingsHandler(String aType) {
                this.registerHandler(TAG_MAPPING, new GenericDataImportConfigCAMappingHandler(aType));
            }
        }

        private class GenericDataImportConfigCAMappingHandler implements NodeHandler {

            private String type;

            public GenericDataImportConfigCAMappingHandler(String aType) {
                this.type = aType;
            }

            @Override
			public void handleNode(Node aNode) {
                NamedNodeMap theAttrs = aNode.getAttributes();
                String theCol    = NodeHelper.getAsString(theAttrs, ATTR_COLUMN);
                String theAttr   = NodeHelper.getAsString(theAttrs, ATTR_ATTRIBUTE);
                String theTarget = NodeHelper.getAsString(theAttrs, ATTR_REFERENCETARGET, null);
                boolean theIsRef           = Boolean.valueOf(NodeHelper.getAsString(theAttrs, ATTR_IS_REFERENCE, "false")).booleanValue();
                boolean theIgnoreExistance = Boolean.valueOf(NodeHelper.getAsString(theAttrs, ATTR_IGNORE_EXISTANCE, "false")).booleanValue();

                ColumnAttributeMapping theMapping = new ColumnAttributeMapping(theCol, theAttr, theIsRef, theTarget, null, true, theIgnoreExistance);
                config.setMapping(this.type, theMapping);
            }

        }

        private class GenericDataImportConfigValueMappingsHandler extends GenericDataImportConfigurationRecursiveParser {
            public GenericDataImportConfigValueMappingsHandler(String aType) {
                this.registerHandler(TAG_MAPPING, new GenericDataImportConfigValueMappingHandler(aType));
            }
        }

        private class GenericDataImportConfigValueMappingHandler implements NodeHandler {

            private String type;

            public GenericDataImportConfigValueMappingHandler(String aType) {
                this.type = aType;
            }

            @Override
			public void handleNode(Node aNode) {
                NamedNodeMap theAttrs = aNode.getAttributes();
                String  theAttr = NodeHelper.getAsString(theAttrs, ATTR_ATTRIBUTE);
                GenericConverterFunction theFunc = (GenericConverterFunction) NodeHelper.getAsInstanceOfClass(theAttrs, ATTR_CLASS);

                if (theFunc instanceof GenericDataImportConfigurationAware) {
                    ((GenericDataImportConfigurationAware) theFunc).setImportConfiguration(config, this.type);
                }

                ColumnAttributeMapping theMapping = config.getMappingForAttribute(this.type, theAttr);
                if (theMapping == null) {
                    theMapping = new ColumnAttributeMapping(null, theAttr, false, null, theFunc, false, false);
                }
                else {
                    theMapping.function    = theFunc;
                    theMapping.allowChange = false;
                }
                config.setMapping(this.type, theMapping);
            }
        }
    }

    /**
     * The {@link EmptyCache} is empty and does nothing.
     *
     * @author    <a href=mailto:TEH@top-logic.com>TEH</a>
     */
    public static class EmptyCache extends AbstractGenericDataImportBase implements GenericCache {

        /**
         * Singleton {@link EmptyCache} instance.
         */
        public static final GenericCache INSTANCE = new EmptyCache();

        // Constructors

        public EmptyCache() {
            super(new Properties());
        }

        @Override
		public boolean add(String aType, Object aKey, Object aValue) {
            return false;
        }

        @Override
		public boolean contains(String aType, Object aKey) {
            return false;
        }

        @Override
		public Object get(String aType, Object aKey) {
            return null;
        }

        @Override
		public void reload() {
        }

        /**
         * @see com.top_logic.element.genericimport.interfaces.GenericCache#merge(com.top_logic.element.genericimport.interfaces.GenericCache)
         */
		@Override
		public void merge(GenericCache aAnotherCache) {
        }
    }

    /**
     * {@link GenericValidator} accepting every object.
     */
    public static class TrueValidator extends AbstractGenericDataImportBase implements GenericValidator {

        /**
         * Singleton {@link TrueValidator} instance.
         */
        public static final GenericValidator INSTANCE = new TrueValidator();

        // Constructors

        public TrueValidator() {
            super(new Properties());
        }

        @Override
		public ValidationResult validateSimpleTypes(GenericValueMap aDO, GenericCache aCache) {
            return ValidationResult.VALID_RESULT;
        }

        @Override
		public ValidationResult validateReferenceTypes(GenericValueMap aDO, GenericCache aCache) {
            return ValidationResult.VALID_RESULT;
        }
    }

    /**
     * {@link GenericConverter} returning the given data unchanged.
     */
    public static class DefaultConverter implements GenericConverter {

        /**
         * Singleton {@link DefaultConverter} instance.
         */
        public static final GenericConverter INSTANCE = new DefaultConverter();

        public DefaultConverter() {
        }

        @Override
		public GenericValueMap convert(GenericValueMap aDO, GenericCache aCache) throws DataObjectException {
            return aDO;
        }

        /**
         * This converter has no configuration.
         *
         * @return Always {@code null}.
         */
        public GenericDataImportConfiguration getConfiguration() {
            return null;
        }

        /**
         * Ignores the given configuration.
         *
         * @param aConfig
         *        Ignored.
         */
        public void setConfiguration(GenericDataImportConfiguration aConfig) {
        }
    }

    /**
     * Exception signaling a failure while updating an object during the import.
     */
    public static class UpdateException extends CreateException {
        public UpdateException(String message) {
            super (message);
        }

        public UpdateException(Throwable rootCause) {
            super(rootCause);
        }

        public UpdateException(String s, Throwable rootCause) {
            super(s, rootCause);
        }
    }
}

