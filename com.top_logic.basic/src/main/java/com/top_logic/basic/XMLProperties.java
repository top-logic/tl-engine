/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Central class managing all needed properties in one XML file. The access
 * to the different values has is realized by a string key, which is dotted
 * (e.g. TopLogic.general.tempDir).
 *
 * <p>If you want to use the XML file like normal properties, you can
 * convert the lists to an XML file in the following way:</p>
 * <p>Expect to have a properties file "jndi.properties" which contains
 * this:</p>
 * <pre>
 * java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
 * java.naming.provider.url=ldap://tl/o=B&amp;P,c=DE
 * </pre>
 *
 * <p>Please convert the values to the following XML file:</p>
 * <pre>
 * &lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;
 *
 * &lt;root&gt;
 *     &lt;properties&gt;
 *         &lt;section name="jndi"&gt;
 *             &lt;entry name="java.naming.factory.initial"
 *                    value="com.sun.jndi.ldap.LdapCtxFactory" /&gt;
 *             &lt;entry name="java.naming.provider.url"
 *                    value="ldap://tl:389/o=B&amp;P,c=DE" /&gt;
 *         &lt;/section&gt;
 *     &lt;/properties&gt;
 * &lt;/root&gt;
 * </pre>
 * <p><strong>And take care to replace any '&amp;' with the correct '&amp;amp;' and
 * every '&lt;' with '&amp;lt;'...</strong></p>
 *
 * <p>Now you can get the properties for jndi by calling:</p>
 * <pre>
 * Properties theProps = XMLProperites.getProperties ("jndi");
 *
 * System.out.println (theProps.getProperty ("java.naming.factory.initial"));
 * </pre>
 * <p>
 * You can change the XMLProperties by calling 
 * <code>removeAttributes(),removeAttributeSection(),removeProperties(),
 *       setAttributes() or setProperties()</code>
 * after doing so you must call
 *      <code>saveProperties()</code> 
 * to actually write them back. When the
 * <code>autoCommit</code> property is false. Changes will not actually
 * be saved but handled to a Thread (thus avoiding time consuming multiple
 * save operations).
 * </p>
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class XMLProperties extends ManagedClass {

	/**
	 * A property setting (key/value pair).
	 */
	public static class Setting {

		/**
		 * Suffix of the system property name that configures the file name of the last (deployment
		 * specific) configuration fragment, or of the folder containing a file called
		 * {@value ModuleLayoutConstants#META_CONF_NAME} which enumerates the files to be included.
		 * 
		 * <p>
		 * The following properties are looked up in the following order, the first that provides a
		 * non-empty value is used:
		 * </p>
		 * 
		 * <ul>
		 * <li>[context-name]_tl_config</li>
		 * <li>tl_config</li>
		 * <li>jndi_tl_config (looked up from JNDI)</li>
		 * </ul>
		 * 
		 * <p>
		 * Note: This setting is (partially) superseded by the {@link #AUTO_CONF} setting that
		 * provides an external directory from which <b>all</b> configuration files are looked up.
		 * However, no alias overrides are possible from <code>autoconf</code> configurations.
		 * </p>
		 * 
		 * @see #AUTO_CONF
		 */
		public static final String CONFIG_FILE = "tl_config";


		/**
		 * Property name suffix for the system/JNDI property that defines the deployment-specific
		 * <code>autoconf</code> folder.
		 * 
		 * <p>
		 * All configuration fragments (`*.config.xml`) in this folder are loaded at application
		 * startup.
		 * </p>
		 * 
		 * <p>
		 * The same naming convention applies as for {@link Setting#CONFIG_FILE}.
		 * </p>
		 * 
		 * @see ModuleLayoutConstants#AUTOCONF_PATH
		 * @see #CONFIG_FILE
		 */
		public static final String AUTO_CONF = "tl_autoconf_dir";

		private String _property;

		private String _value;

		/**
		 * Creates a {@link Setting}.
		 * 
		 * @param property
		 *        See {@link #getProperty()}.
		 * @param value
		 *        See {@link #getValue()}.
		 */
		public Setting(String property, String value) {
			_property = property;
			_value = value;
		}

		/**
		 * The name of the property that provided the {@link #getValue()}.
		 */
		public String getProperty() {
			return _property;
		}

		/**
		 * The configured value.
		 */
		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return getProperty() + " = " + getValue();
		}

		/**
		 * The external <code>autoconf</code> folder setting.
		 *
		 * @return The setting of the external <code>autoconf</code> folder, or <code>null</code> if
		 *         no such setting was given.
		 */
		public static Setting getAutoconf() {
			return Module.INSTANCE.config().getAutoConfSetting();
		}

		/**
		 * Determines the folder to write in app development files to.
		 * 
		 * <p>
		 * When an external {@link Setting#getAutoconf() autoconf} folder is configured, this is
		 * returned. Otherwise the folder {@link ModuleLayoutConstants#AUTOCONF_PATH} of the top
		 * level web application.
		 * </p>
		 * 
		 * @return The folder to write in app development files to. It is not checked that the given
		 *         file is a folder or exists.
		 * 
		 * @see FileUtilities#enforceDirectory(File) Call to ensure existance of folder.
		 */
		public static File resolveAutoconfFolder() {
			File autoconfFolder;
			Setting autoconfSetting = XMLProperties.Setting.getAutoconf();
			if (autoconfSetting != null) {
				autoconfFolder = new File(autoconfSetting.getValue());
			} else {
				autoconfFolder = new File(Workspace.topLevelWebapp(), ModuleLayoutConstants.AUTOCONF_PATH);
			}
			return autoconfFolder;
		}

		/**
		 * Lookup of a configuration option from an application-specific system property or a JDNI
		 * value.
		 *
		 * @param propertySuffix
		 *        Name suffix of the system property. The concrete system property is prefixed with
		 *        the application name and the context name for lookup.
		 * @return The {@link Setting} describing the value and the property that provided the
		 *         value, or <code>null</code>, if no value was found.
		 */
		public static Setting getSetting(String propertySuffix) {
			return getSetting(Module.INSTANCE.config(), propertySuffix);
		}

		static Setting getSetting(XMLPropertiesConfig config, String propertySuffix) {
			String contextPath = config.getContextPath();
			if (contextPath != null) {
				String propertyPrefix;
				if (contextPath.isEmpty()) {
					// application runs in root context.
					propertyPrefix = "ROOT";
				} else {
					// Context path starts with '/'
					propertyPrefix = contextPath.substring(1);
				}
				String contextProperty = propertyPrefix + '_' + propertySuffix;
				String contextValue = Environment.lookupSystemProperty(contextProperty);
				if (!StringServices.isEmpty(contextValue)) {
					return new Setting(contextProperty, contextValue);
				}
				Logger.info("No system property '" + contextProperty + "'.", XMLProperties.class);
			}

			String globalProperty = propertySuffix;
			String applicationValue = Environment.lookupSystemProperty(globalProperty);
			if (!StringServices.isEmpty(applicationValue)) {
				return new Setting(globalProperty, applicationValue);
			}
			Logger.info("No system property '" + globalProperty + "'.", XMLProperties.class);

			String jndiValue = Environment.lookupJNDIProperty(globalProperty);
			if (!StringServices.isEmpty(jndiValue)) {
				return new Setting(globalProperty, jndiValue);
			}
			Logger.info("No value in JNDI property '" + globalProperty + "'.", XMLProperties.class);

			return null;
		}

	}

	/**
	 * The root element of a configuration section.
	 */
	public static final String ROOT_ELEMENT = "root";

	/** The name for the properties node in this document (is "properties"). */
    public static final String   PROP_NODE      = "properties";

	/** The path to the alias section. */
	public static final String ALIAS_NODE = "alias";

    /** The name for the section of properties (is "section"). */
    public static final String   SECTION_NODE   = "section";

    /** The name for the different entries in the XML file (is "entry"). */
    public static final String   ENTRY_NODE     = "entry";

    /** The attribute name for the name of the entry (is "name"). */
    public static final String   KEY_ATTR       = "name";

    /** The attribute name for the value stored in the entry (is "value"). */
    public static final String   VALUE_ATTR     = "value";

	/**
	 * The attribute name of section elements where inheritance can be turned off (is "inherit").
	 */
	public static final String INHERIT_ATTR = "inherit";

	/**
	 * Value for attribute {@link #INHERIT_ATTR} to mark as "no inherit".
	 */
	public static final String INHERIT_VAL_NO = "no";

	/**
	 * The attribute name for the value stored in the entry (is "encrypt"). The value must be "true"
	 * or "false".
	 */
	private static final String ENCRYPT_ATTR = "encrypt";

    /** The Document containig the actual properties */
    private Document  document;
    
    private BinaryContent underlyingData;
    
    /** The cache for sections */
    protected Map<String, Properties> sectionCache;
    
    /** the cache for path properties */
    protected Map<String, Properties>  pathPropertiesCache;

    /** The cache for attributes */
    protected Map<String,Element> attrCache;
    
    /** the cache for child names lists */
    private Map<String, List<String>> childNamesCache;

	/** The sections in the application. Including all sections in underlying {@link XMLProperties}. */
	protected Map<String, Properties> _sections;

	/** The aliases in the application. Including all aliases in underlying {@link XMLProperties}. */
	protected Properties _aliases;

	/** {@link XMLProperties} to inherit from. May be <code>null</code>. */
	private final XMLProperties _fallback;

	public XMLProperties(XMLProperties fallback, BinaryContent underlyingData) throws IOException {
		_fallback = fallback;
		if (_fallback != null) {
			_fallback.stopCaching();
		}
		this.underlyingData = underlyingData;

		loadProperties();
		initCaching();

		loadDocument();
	}

	public XMLProperties(BinaryContent underlyingData) throws IOException {
		this(null, underlyingData);
	}

	private void loadProperties() throws IOException {
		initVariables();
		if (underlyingData == null) {
			return;
		}
		BufferingProtocol protocol = new BufferingProtocol();
		try (InputStream inputStream = this.underlyingData.getStream()) {
			loadProperties(inputStream, protocol);
			if (protocol.hasErrors()) {
				throw new IOException(
					"Invalid XML structure in " + underlyingData + ": " + protocol.getError());
			}
		} catch (XMLStreamException ex) {
			throw new IOException("Loading '" + underlyingData + "' failed.", ex);
		}
	}
    
	private void loadProperties(InputStream content, Log log) throws XMLStreamException {
		XMLStreamReader reader = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(content);

		// skip to root tag
		XMLStreamUtil.skipToTag(reader);

		while (reader.hasNext()) {
			reader.nextTag();
			if (XMLStreamUtil.isAtEndTag(reader)) {
				// root tag closed
				break;
			}
			String localName = reader.getLocalName();
			if (ALIAS_NODE.equals(localName)) {
				readAliases(reader, log);
			} else if (PROP_NODE.equals(localName)) {
				readSections(reader, log);
			} else {
				log.error("Unexpected node name " + localName + " in " + reader.getLocation() + ". Expected: "
					+ ALIAS_NODE + " or " + PROP_NODE);
				XMLStreamUtil.skipToMatchingEndTag(reader);
			}
		}
	}

	/**
	 * Initial {@link #_sections} and {@link #_aliases} by using the corresponding values of the
	 * fallback {@link XMLProperties}.
	 */
	private void initVariables() {
		Map<String, Properties> properties;
		Properties aliases;
		if (_fallback != null) {
			properties = new LinkedHashMap<>(_fallback._sections);
			aliases = _fallback._aliases;
		} else {
			properties = new LinkedHashMap<>();
			aliases = new Properties();
		}
		_sections = properties;
		_aliases = aliases;
	}

	private void readAliases(XMLStreamReader reader, Log log) throws XMLStreamException {
		Properties config = fetchProperties(reader, log);
		if (config.isEmpty()) {
			// No aliases in content
			return;
		}
		// adapt inherited aliases
		Properties newAliases;
		if (_aliases.isEmpty()) {
			newAliases = config;
		} else {
			newAliases = (Properties) _aliases.clone();
			newAliases.putAll(config);
		}
		_aliases = newAliases;
	}

	private void readSections(XMLStreamReader reader, Log log) throws XMLStreamException {
		Set<String> seenSectionNames = new HashSet<>();
		while (reader.hasNext()) {
			reader.nextTag();
			if (XMLStreamUtil.isAtEndTag(reader)) {
				// properties tag closed
				break;
			}
			String localName = reader.getLocalName();
			if (!SECTION_NODE.equals(localName)) {
				log.error("Unexpected node name " + localName + " in " + reader.getLocation() + ". Expected: "
					+ SECTION_NODE);
				XMLStreamUtil.skipToMatchingEndTag(reader);
				continue;
			}
			String section = reader.getAttributeValue(null, KEY_ATTR);
			if (StringServices.isEmpty(section)) {
				XMLStreamUtil.skipToMatchingEndTag(reader);
				continue;
			}
			if (!seenSectionNames.add(section)) {
				StringBuilder duplicateSection = new StringBuilder();
				duplicateSection.append("The properties section ");
				duplicateSection.append(section);
				duplicateSection.append(" in '");
				duplicateSection.append(underlyingData.toString());
				duplicateSection.append("' is duplicate.");
				throw new ConfigurationError(duplicateSection.toString());
			}
			boolean noInheritance = isNoInheritance(reader);
			Properties config = fetchProperties(reader, log);
			if (noInheritance) {
				_sections.put(section, config);
			} else {
				Properties fallbackSection = _sections.get(section);
				if (fallbackSection == null || fallbackSection.isEmpty()) {
					_sections.put(section, config);
				} else {
					// merge
					Properties mergedProperties = (Properties) fallbackSection.clone();
					mergedProperties.putAll(config);
					_sections.put(section, mergedProperties);
				}
			}
		}

	}

	private boolean isNoInheritance(XMLStreamReader reader) {
		String inheritanceAttr = reader.getAttributeValue(null, INHERIT_ATTR);
		return INHERIT_VAL_NO.equalsIgnoreCase(inheritanceAttr);
	}

	private Properties fetchProperties(XMLStreamReader reader, Log log) throws XMLStreamException {
		Properties config = new Properties();
		while (reader.hasNext()) {
			reader.nextTag();
			if (XMLStreamUtil.isAtEndTag(reader)) {
				break;
			}
			String localName = reader.getLocalName();
			if (!ENTRY_NODE.equals(localName)) {
				log.error("Unexpected node name " + localName + " in " + reader.getLocation() + ". Expected: "
					+ ENTRY_NODE);
				XMLStreamUtil.skipToMatchingEndTag(reader);
				continue;
			}
			String key = reader.getAttributeValue(null, KEY_ATTR);
			if (StringServices.isEmpty(key)) {
				XMLStreamUtil.skipToMatchingEndTag(reader);
				continue;
			}
			String value = reader.getAttributeValue(null, VALUE_ATTR);
			if (value == null) {
				XMLStreamUtil.skipToMatchingEndTag(reader);
				continue;
			}
			String encryptAttr = reader.getAttributeValue(null, ENCRYPT_ATTR);
			if (Boolean.TRUE.toString().equals(encryptAttr)) {
				value = decryptValue(key, value);
			}
			config.put(key, value);
			XMLStreamUtil.skipToMatchingEndTag(reader);
		}
		return config;
	}

	/** Provide a reasonable String for debugging */
	@Override
	public String toString() {
		return (this.getClass().getName() + " ["
				+ "BinaryContent: '" + this.underlyingData
				+ "']");
	}

    /** Setup caches to speed up access */
    protected synchronized void initCaching() {
        sectionCache          = new HashMap<>(128); // POS needs about 98 ...
        pathPropertiesCache   = new HashMap<>();
        attrCache             = new HashMap<>();
        childNamesCache       = new HashMap<>();
    }

    /** remove caches to in case this is not desired (see {@link MultiProperties}). */
    protected synchronized void stopCaching() {
        sectionCache          = null;
        pathPropertiesCache   = null;
        attrCache             = null;
        childNamesCache       = null;
    }

	/**
	 * Returns the aliases in this {@link XMLProperties}.
	 */
	public Properties getAliases() {
		return _aliases;
	}

    /**
     * Get all service section names (i.e. the ones beyond the root/properties element)
     *
     * @return a List of Strings representing the name attributes of section tags
     */ 
    public List getServiceNames () {
		return new ArrayList<>(_sections.keySet());
    }

    /** Return an instance of Properties for the given class-(name).
     *
     * @param   aClass    The class wanting the properties.
     *
     * @return  Properties for aClass.getName (). 
     */
	public final Properties getProperties(Class<?> aClass) {
        return getProperties(getSectionName(aClass));
    }

	private String getSectionName(Class<?> aClass) {
		// Note: For inner classes, this is not the same as Class.getSimpleName().
        String shortName = aClass.getName();
        int packageSeparatorIdx = shortName.lastIndexOf('.');
		shortName = shortName.substring(packageSeparatorIdx + 1);
		return shortName;
	}

    /**
     * Return an instance of Properties containing all child elements from the
     * XML file for the given key.
     * For performance reasons, the sections are cached.
     *
     * @param    aKey    The dotted key to access the properties.
     *
     * @return  Properties for aKey, empty properties in case key was not found.
     */
    public synchronized Properties getProperties (String aKey) {
		Properties properties = _sections.get(aKey);
		if (properties == null) {
			properties = new Properties();
			_sections.put(aKey, properties);
		}
		return properties;
    }

	private String decryptValue(String key, String value) {
		if (!StringServices.isEmpty(value)) {
			// do not decrypt empty values, just leave them empty
			try {
				value = ConfigurationEncryption.decrypt(value);
			} catch (RuntimeException ex) {
				StringBuilder msg = new StringBuilder();
				msg.append("Unable to decode '");
				msg.append(value);
				msg.append("' as value for key '");
				msg.append(key);
				msg.append("'.");
				throw new ConfigurationError(msg.toString(), ex);
			}
		}
		return value;
	}
    
    /**
     * Reset the whole content of this properties.
     *
     * This causes the instance to reload all values from the file.
     * @throws IOException when udnerlying file has vanished or such.
     */
    public synchronized void reset () throws IOException {
        if (sectionCache != null) {
            this.sectionCache       .clear();
            this.attrCache          .clear();
            this.pathPropertiesCache.clear();
            this.childNamesCache    .clear();
        }

        this.loadDocument();

		if (_fallback != null) {
			_fallback.reset();
		}
		loadProperties();
    }

	/**
	 * Build a document out of the underlying input stream. Therefore the method
	 * uses the {@link javax.xml.parsers.DocumentBuilder} provided by the
	 * matching factory.
	 */
	private void loadDocument() throws IOException {
		try {
			if (this.underlyingData == null) {
				// Define a minimal configuration document to prevent crashing later on.
				this.document = DOMUtil.newDocumentBuilder().newDocument();
				Element root = this.document.createElementNS(null, ROOT_ELEMENT);
				this.document.appendChild(root);
				root.appendChild(this.document.createElementNS(null, ALIAS_NODE));
				root.appendChild(this.document.createElementNS(null, PROP_NODE));
				return;
			}
			DocumentBuilder theBuilder = DOMUtil.newDocumentBuilder();
			InputStream stream = underlyingData.getStream();
			if (stream == null) {
				throw new IOException("The underlying " + BinaryContent.class.getSimpleName() + " returned null-stream");
			}
			this.document = theBuilder.parse(stream);
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException("Failed to create " + DocumentBuilder.class.getSimpleName() + "; Error: "
				+ ex.getMessage(), ex);
		} catch (SAXException ex) {
			throw new RuntimeException("Failed to get xml content from underlying "
				+ BinaryContent.class.getSimpleName() + ": '" + underlyingData + "'; Error: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Reset the XMLProperties with the given configuration file.
	 * <p>
	 * This will reset all properties you already got by using {@link #getInstance()}.
	 * </p>
	 * 
	 * @param configFile
	 *        Single configuration file.
	 */
	public static final void startWithConfigFile(String configFile) throws ModuleException {
    	XMLPropertiesConfig newConfig = new XMLPropertiesConfig();
		BinaryData file = XMLProperties.getFile(configFile);
		newConfig.pushAdditionalContent(file);
		XMLProperties.restartXMLProperties(newConfig);
    }

	/**
	 * Returns the only instance of this class.
	 * 
	 * Please use with care, use {@link com.top_logic.basic.Configuration} for
	 * simple, aliasesd Properties.
	 * 
	 * @return The only instance of this class.
	 * 
	 * @throws ConfigurationError
	 *         iff the {@link XMLProperties} are not initialized
	 * 
	 * @see #exists()
	 */
    public static final XMLProperties getInstance () {
    	if (!Module.INSTANCE.isActive()) {
    		throw new ConfigurationError("XMLProperties are not available. The corresponding service was not started.");
    	}
    	return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Determines whether the {@link XMLProperties} were initialized.
	 * 
	 * @return <code>true</code> iff {@link #getInstance()} returns not
	 *         <code>null</code> {@link XMLProperties}.
	 */
    public static final boolean exists() {
    	return Module.INSTANCE.isActive();
    }
    
	/**
	 * The configuration of an active instance, or <code>null</code> if {@link XMLProperties} are
	 * not started.
	 */
	public static XMLPropertiesConfig existingConfig() {
		return exists() ? Module.INSTANCE.config() : null;
	}

	public static XMLProperties createXMLProperties(BinaryData aFile) throws IOException {
		return new XMLProperties(aFile);
    }
    
	public static final class Module extends BasicRuntimeModule<XMLProperties> {

		/**
		 * A Collection which indicates that the returner depends only on the
		 * {@link XMLProperties}.
		 */
		public static final List<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENT_ON_XML_PROPERTIES
    		= Arrays.asList(AliasManager.Module.class);

		private static final List<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
			Arrays.asList(TypeIndex.Module.class);

		/**
		 * Singleton {@link XMLProperties.Module} instance.
		 */
		public static final XMLProperties.Module INSTANCE = new Module();

		private XMLPropertiesConfig config;

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return DEPENDENCIES;
		}

		@Override
		public Class<XMLProperties> getImplementation() {
			return XMLProperties.class;
		}

		@Override
		protected XMLProperties newImplementationInstance() throws ModuleException {
			if (config == null) {
				throw new IllegalStateException(
					"Cannot start configuration service, since setConfig() has not been called.");
			}
			try {
				config.resolve();
			} catch (IOException ex) {
				throw new ModuleException(
					"Unable to resolve configuration for service '" + getImplementation().getName() + "'.", ex,
					this.getImplementation());
			}
			XMLProperties result = null;
			try {
				List<BinaryContent> content = config._content;
				Logger.info("Using configurations: " + content, XMLProperties.class);
				if (content.isEmpty()) {
					return new XMLProperties(null);
				}
				for (BinaryContent c : content) {
					result = MultiProperties.createNewXMLProperties(result, c);
				}
			} catch (IOException ex) {
				throw new ModuleException("Unable to createXMLProperties", ex, this.getImplementation());
			}
			return result;
		}
		
		public XMLPropertiesConfig config() {
			return config;
		}

		void setConfig(XMLPropertiesConfig newConfig) {
			config = newConfig;
		}
		
    }

	public static class XMLPropertiesConfig implements Cloneable {

		private static final String CONTEXT_PATH_PROPERTY = "tl_contextPath";

		private final String _contextPath;

		private final boolean _noRealPath;

		/**
		 * Name of the resource to load the reference to configuration fragments from.
		 * 
		 * <p>
		 * Under normal conditions, this is {@link ModuleLayoutConstants#META_CONF_RESOURCE}.
		 * </p>
		 */
		private final String _metaConfResource;

		private List<BinaryContent> _additionalContent;
		
		List<BinaryContent> _content;

		private List<BinaryContent> _typedConfigs;

		private Set<String> _configurationResources;

		private Setting _autoConfSetting;

		public XMLPropertiesConfig() {
			this(null, false, null);
		}

		public XMLPropertiesConfig(String contextPath, boolean noRealPath, String metaConfResource) {
			_contextPath = contextPath != null ? contextPath : resolveContextFromSystemProperty(null);
			_noRealPath = noRealPath;
			_metaConfResource = metaConfResource;
		}

		/**
		 * Fetches the configured context path.
		 * 
		 * <p>
		 * Lookup for the system or environment variable {@value #CONTEXT_PATH_PROPERTY}.
		 * </p>
		 * 
		 * @param defaultContext
		 *        Context path that is returned when no context path is configured.
		 */
		public static String resolveContextFromSystemProperty(String defaultContext) {
			return Environment.getSystemPropertyOrEnvironmentVariable(CONTEXT_PATH_PROPERTY, defaultContext);
		}

		public void pushAdditionalContent(BinaryContent content) {
			assert content != null: "Can not add null file";
			if (MultiProperties.isTypedConfigName(content.getName())) {
				pushAdditionalContent(null, content);
			} else {
				pushAdditionalContent(content, FileManager.getInstance().getDataOrNull(MultiProperties.typedConfigName(content.getName())));
			}
		}

		public void pushAdditionalContent(BinaryContent content, BinaryContent typedContent) {
			if (_additionalContent == null) {
				_additionalContent = new ArrayList<>();
			}
			_additionalContent.add(content);
			_additionalContent.add(typedContent);
		}
		
		@Override
		public XMLPropertiesConfig clone() {
			{
				XMLPropertiesConfig configClone =
					new XMLPropertiesConfig(getContextPath(), noRealPath(), _metaConfResource);
				if (_additionalContent != null) {
					configClone._additionalContent = new ArrayList<>(_additionalContent);
				}
				if (_content != null) {
					configClone._content = new ArrayList<>(_content);
				}
				if (_typedConfigs != null) {
					configClone._typedConfigs = new ArrayList<>(_typedConfigs);
				}
				if (_autoConfSetting != null) {
					configClone._autoConfSetting = _autoConfSetting;
				}
				return configClone;
			}
		}
		
		/**
		 * Returns the setting for the autoconf directory.
		 */
		public Setting getAutoConfSetting() {
			return _autoConfSetting;
		}

		/**
		 * Resolves the name of the resources for the configuration files.
		 */
		public void resolve() throws IOException {
			_configurationResources = new LinkedHashSet<>();
			_content = new ArrayList<>();
			_typedConfigs = new ArrayList<>();
			
			if (_metaConfResource != null) {
				loadMetaConf(_metaConfResource);
			} else if (this.noRealPath()) {
				loadMetaConf(FileManager.getInstance(),
					FileManager.getInstance().getData(ModuleLayoutConstants.META_CONF_RESOURCE));
			} else {
				loadMetaConf(ModuleLayoutConstants.META_CONF_RESOURCE);
			}

			_autoConfSetting = Setting.getSetting(this, Setting.AUTO_CONF);
			loadAutoConf();

			if (_additionalContent != null) {
				int i = 0;
				while (i < _additionalContent.size()) {
					BinaryContent untypedContent = _additionalContent.get(i++);
					BinaryContent typedContent = _additionalContent.get(i++);
					addContent(null, untypedContent, typedContent);
				}
			}
			MultiProperties.pushSystemProperty(this);
		}
		
		/**
		 * Loads all configuration files in folder WEB-INF/autoconf folders.
		 */
		private void loadAutoConf() {
			FileManager fileManager = FileManager.getInstance();
			List<String> autoconfResources =
				fileManager.getResourcePaths(ModuleLayoutConstants.AUTOCONF_FOLDER_RESOURCE).stream()
					.filter(r -> r.endsWith(".config.xml")).sorted().collect(Collectors.toList());

			// Load all non overridden files in the same global order as they will be loaded after
			// deployment.
			for (String configResource : autoconfResources) {
				addContent(configResource, null, FileManager.getInstance().getData(configResource));
			}

			if (_autoConfSetting != null) {
				Logger.info("Using autoconf of deployment: " + _autoConfSetting, XMLProperties.class);
				File folder = new File(_autoConfSetting.getValue());
				if (folder.isDirectory()) {
					loadAutoConfFolder(folder);
				} else {
					Logger.info("No such autoconf folder: " + folder.getAbsolutePath(), XMLProperties.class);
				}
			}
		}

		private void loadAutoConfFolder(File folder) {
			Logger.debug("Loading autoconf folder: " + folder, XMLProperties.class);
			File[] configs = folder.listFiles(f -> f.isFile() && f.getName().endsWith(".config.xml"));
			if (configs == null) {
				return;
			}

			List<File> configFiles = new ArrayList<>(Arrays.asList(configs));

			// Establish stable order.
			Collections.sort(configFiles, (f1, f2) -> f1.getName().compareTo(f2.getName()));

			for (File configFile : configFiles) {
				Logger.debug("Using autoconf: " + configFile, XMLProperties.class);
				addContent(FileManager.markDirect(configFile.getAbsolutePath()), null,
					BinaryDataFactory.createBinaryData(configFile));
			}
		}

		private void loadMetaConf(String resourceName) throws IOException {
			FileManager resolver = FileManager.getInstance();

			List<BinaryData> metaConfs = resolver.getDataOverlays(resourceName);
			Collections.reverse(metaConfs);
			for (BinaryData metaConf : metaConfs) {
				loadMetaConf(resolver, metaConf);
			}
		}

		/**
		 * Push all files found in the specified File onto the singleton.
		 * <p>
		 * The file contains an arbitrary number of lines each containing the name of an
		 * XMLProperties-file that is pushed. The file names can be absolute or relative to the
		 * directory of the input File.
		 * </p>
		 * @param metaConf
		 *        the "bootstrap" file; must not be null
		 * 
		 * @throws java.io.IOException
		 *         if there was a problem reading the file
		 */
		void loadMetaConf(FileManager resolver, BinaryData metaConf) throws IOException {
			Logger.debug("Loading metaConf.text: " + metaConf, XMLProperties.class);
			try (InputStream in = metaConf.getStream()) {
				for (String configName : XMLPropertiesConfig.readMetaConf(in)) {
					addConfiguration(resolver, configName);
				}
			}
		}

		private void addConfiguration(FileManager resolver, String configName) {
			String resourceName = configurationResourceName(configName);
			BinaryData config = resolver.getData(resourceName);
			addConfiguration(resolver, config.getName(), config);
		}

		void addConfiguration(FileManager resolver, String resourceName, BinaryContent config) {
			BinaryContent untypedConfig;
			BinaryContent typedConfig;
			if (MultiProperties.isTypedConfigName(resourceName)) {
				untypedConfig = null;
				typedConfig = config;
			} else {
				untypedConfig = config;
				typedConfig = resolver.getDataOrNull(MultiProperties.typedConfigName(resourceName));
			}
			addContent(resourceName, untypedConfig, typedConfig);
		}

		private boolean addContent(String resourceName, BinaryContent untypedConfig, BinaryContent typedConfig) {
			if (resourceName != null) {
				if (_configurationResources.contains(resourceName)) {
					return false;
				}
				_configurationResources.add(resourceName);
			}
			if (untypedConfig != null) {
				_content.add(untypedConfig);
			}
			if (typedConfig != null) {
				_typedConfigs.add(typedConfig);
			}
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder("XMLPropertiesConfig[");
			result.append("contextPath:").append(getContextPath());
			result.append(",metaConf:").append(_metaConfResource);
			result.append(",additionalContent:").append(_additionalContent);
			result.append(']');
			return result.toString();
		}

		public List<BinaryContent> getTypedConfigs() {
			return _typedConfigs;
		}

		public String getContextPath() {
			return _contextPath;
		}

		public boolean noRealPath() {
			return _noRealPath;
		}

		static List<String> readMetaConf(InputStream in) throws IOException {
			List<String> configNames = new ArrayList<>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "ascii"))) {
				String configName;
				while ((configName = reader.readLine()) != null) {
					configNames.add(configName);
				}
			}
			return configNames;
		}

		private static String configurationResourceName(String configName) {
			return ModuleLayoutConstants.CONF_RESOURCE_PREFIX + '/' + configName;
		}

	}
	
	public static BinaryData getFile(final String propertiesFileName) {
		BinaryData file = FileManager.getInstance().getDataOrNull(propertiesFileName);
		if (file == null) {
			file = BinaryDataFactory.createBinaryData(new File(propertiesFileName));
		}
		return file;
	}

	/**
	 * Restarts the {@link XMLProperties} using the given resource to find the files holding the
	 * names of the configurations to load.
	 */
	public static void startWithMetaConf(String metaConfResource) throws ModuleException {
		XMLPropertiesConfig newConfig = new XMLPropertiesConfig(null, false, metaConfResource);
		restartXMLProperties(newConfig);
	}

	public static synchronized void restartXMLProperties(final XMLPropertiesConfig newConfig) throws ModuleException {
		final Module xmlModule = Module.INSTANCE;
		if (xmlModule.isActive()) {
			final XMLPropertiesConfig oldConfig = xmlModule.config();
			try {
				ModuleUtil.INSTANCE.restart(xmlModule, new Runnable() {
					
					@Override
					public void run() {
						xmlModule.setConfig(newConfig);
					}
					
				});
			} catch(RestartException ex) {
				Logger.error("Unable to restart '" + xmlModule + "' with configuration '" + newConfig + "'. Trying to reinstall instances.", ex, XMLProperties.class);
				ModuleUtil.INSTANCE.shutDown(xmlModule);
				internalStart(oldConfig, xmlModule);
				for (BasicRuntimeModule<?> dependent: ex.getCurrentlyStartedDependents()) {
					ModuleUtil.INSTANCE.startUp(dependent);
				}
				throw ex;
			}
		} else {
			internalStart(newConfig, xmlModule);
		}
	}

	public static synchronized void startIfInactive(final XMLPropertiesConfig newConfig) throws ModuleException {
		final Module xmlModule = Module.INSTANCE;
		if (!xmlModule.isActive()) {
			internalStart(newConfig, xmlModule);
		}
	}

	private static void internalStart(final XMLPropertiesConfig newConfig, final Module xmlModule)
			throws ModuleException {
		xmlModule.setConfig(newConfig);
		
		ModuleUtil.INSTANCE.startXMLProperties();
	}

}
