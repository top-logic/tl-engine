/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.list;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLUpdateMode;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.annotation.EnumScope;
import com.top_logic.model.config.annotation.MultiSelect;
import com.top_logic.model.config.annotation.SystemEnum;
import com.top_logic.model.config.annotation.UnorderedEnum;
import com.top_logic.util.model.ModelService.ClassificationConfig;

/**
 * Supporting class for managing lists (i.e. FastList).
 *
 * The {@link #checkEnum(KnowledgeBase, EnumConfig) check for lists} will first check, if the list
 * is already stored in the {@link KnowledgeBase}. If it is, it'll be returned. If not, the file
 * defined will be loaded and this class creates the list out of the information defined in the
 * file.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ListInitializationUtil {

	/**
	 * The path to the directiory the list configuration are expected to be.
	 * 
	 * If a list configuration is stored elsewhere the path must be given and start with a '/'.
	 */
	private static final String DEFAULT_LIST_DIR = "/WEB-INF/xml/lists/";

	private static final String FILE_SUFFIX = ".xml";

	private static final Map<String, ConfigurationDescriptor> DESCRIPTORS = Collections.singletonMap(
		EnumConfig.TAG_NAME, TypedConfiguration.getConfigurationDescriptor(EnumConfig.class));

	private ListInitializationUtil() {
		// Statics only.
	}

    /**
	 * Create / Update the list defined in the given file.
	 * 
	 * If there is no such list, the method will create a new one (without commit). If the list
	 * already exists missing elements can be created if withElements param is true New Elements
	 * from the file definition will be created, existing elements remain in place (no deletion)
	 * 
	 * NOTE: do NOT try to attach a list to an {@link TLObject} created by a factory that needs a
	 * {@link TLStructuredTypePart} depending on this list! The correct initialization order is 1.
	 * Create the List 2. Create the ClassificationMetaAttribute (Maybe with the initial setup of
	 * the MetaAttributeFactory and MetaElementFactory that may create global
	 * ClassificationMetaAttributes!) 3. Create {@link TLObject} 4. Attach the list.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create list.
	 * @param enumConf
	 *        The type configuration to be used for building the initial list.
	 * 
	 * @return The enum type.
	 */
	public static TLEnumeration checkEnum(KnowledgeBase kb, EnumConfig enumConf) {
		TLUpdateMode modeAnnotation = TLAnnotations.getAnnotation(enumConf, TLUpdateMode.class);
		boolean withElements = modeAnnotation != null && modeAnnotation.getValue() == TLUpdateMode.Mode.UPDATE;
		String theName = enumConf.getName();
		List<String> theElements = getLiterals(enumConf);
		String theDefault = getDefaultName(enumConf);
		String theType = getScopeAnnotation(enumConf);
		boolean isMulti = getMultiSelectAnnotation(enumConf);
		boolean isSystem = getSystemAnnotation(enumConf);
		String[] theValues = theElements.toArray(new String[theElements.size()]);
		TLAnnotation unordered = getUnorderedAnnotation(enumConf);
		return checkFastList(kb, theName, theType, isMulti, isSystem, theValues, theDefault, withElements, unordered);
	}

	/**
	 * Loads all referenced {@link TLEnumeration} configs into the given {@link ScopeConfig}.
	 */
	public static void loadLegacyEnums(ScopeConfig enumScope, Collection<ClassificationConfig> enumReferences)
			throws IOException, ConfigurationException {
		for (ClassificationConfig classificationConfig : enumReferences) {
			String name = classificationConfig.getName();
			TLUpdateMode.Mode mode = classificationConfig.getMode();

			EnumConfig enumConfig = ListInitializationUtil.loadEnumConfig(name);
			enumConfig.getAnnotations().add(updateMode(mode));

			enumScope.getTypes().add(enumConfig);
		}
	}

	private static TLUpdateMode updateMode(TLUpdateMode.Mode mode) {
		TLUpdateMode result = TypedConfiguration.newConfigItem(TLUpdateMode.class);
		result.setValue(mode);
		return result;
	}

	/**
	 * Load the enumeration with the given name from its definition file.
	 */
	public static EnumConfig loadEnumConfig(String enumName) throws IOException, ConfigurationException {
		String path = getFilePath(enumName);
		final String resource = path;
		BinaryContent content = FileManager.getInstance().getData(resource);

		EnumConfig enumConfig = loadEnumConfig(enumName, content);

		if (!StringServices.equals(enumName, enumConfig.getName())) {
			throw new ConfigurationException("Name of enumeration '" + enumConfig.getName()
				+ "' does not match given file name '" + enumName + "'.");
		}
		return enumConfig;
	}

	private static EnumConfig loadEnumConfig(String enumName, BinaryContent content) throws IOException,
			ConfigurationException {
		String resourceName = enumName;

		BinaryContent in;
		if (StringServices.isEmpty(getDocumentNamespace(content))) {
			File tmp = File.createTempFile(enumName, ".v6.xml", Settings.getInstance().getTempDir());
			try (FileOutputStream outputStream = new FileOutputStream(tmp)) {
				XsltUtil.transformUnsafe(
					new StreamSource(content.getStream()),
					new StreamSource(FileManager.getInstance()
						.getStream(ModuleLayoutConstants.CONF_RESOURCE_PREFIX + "/import-fast-list.xslt")),
					new StreamResult(outputStream), false);
				resourceName = tmp.getAbsolutePath() + "(" + resourceName + ")";
				in = new TransformedFile(tmp, resourceName);
			}
		} else {
			in = content;
		}

		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, DESCRIPTORS);
		reader.setSource(in);
		return (EnumConfig) reader.read();
	}

	private static String getFilePath(String enumName) {
		if (StringServices.startsWithChar(enumName, '/')) {
			return enumName;
		} else {
			if (enumName.endsWith(FILE_SUFFIX)) {
				return DEFAULT_LIST_DIR + enumName;
			} else {
				return DEFAULT_LIST_DIR + enumName + FILE_SUFFIX;
			}
		}
	}

	private static String getDocumentNamespace(BinaryContent content) throws IOException {
		InputStream in = content.getStream();
		try {
			XMLStreamReader reader = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(in);
			XMLStreamUtil.nextStartTag(reader);
			return reader.getNamespaceURI();
		} catch (XMLStreamException ex) {
			throw new IOException("Parsing document failed.", ex);
		} finally {
			in.close();
		}
	}

	/**
	 * Whether the given enumeration is annotated to be multi-select by default.
	 */
	public static boolean getMultiSelectAnnotation(EnumConfig enumConf) {
		MultiSelect annotation = TLAnnotations.getAnnotation(enumConf, MultiSelect.class);
		if (annotation == null) {
			return false;
		}
		return annotation.getValue();
	}

	/**
	 * Whether the given enumeration is annotated to be unordered by default.
	 */
	public static TLAnnotation getUnorderedAnnotation(EnumConfig enumConf) {
		return TLAnnotations.getAnnotation(enumConf, UnorderedEnum.class);
	}

	private static boolean getSystemAnnotation(EnumConfig enumConf) {
		SystemEnum annotation = TLAnnotations.getAnnotation(enumConf, SystemEnum.class);
		if (annotation == null) {
			return false;
		}
		return annotation.getValue();
	}

	private static String getScopeAnnotation(EnumConfig enumConf) {
		EnumScope annotation = TLAnnotations.getAnnotation(enumConf, EnumScope.class);
		if (annotation == null) {
			return EnumScope.DEFAULT_ENUM_SCOPE;
		}
		return annotation.getValue();
	}

	private static List<String> getLiterals(EnumConfig enumConf) {
		ArrayList<String> result = new ArrayList<>();
		for (ClassifierConfig literal : enumConf.getClassifiers()) {
			result.add(literal.getName());
		}
		return result;
	}

	private static String getDefaultName(EnumConfig enumConf) {
		String defaultLiteral = null;
		for (ClassifierConfig literal : enumConf.getClassifiers()) {
			if (literal.isDefault()) {
				if (defaultLiteral != null) {
					throw new ConfigurationError("More that one default literals in '" + enumConf.getName() + "'.");
				}
				defaultLiteral = literal.getName();
			}
		}
		return defaultLiteral;
	}

	/**
	 * Create/Update an ordered list with the given parameters.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create a new list.
	 * @param listName
	 *        The name of the list, must not be <code>null</code>.
	 * @param listType
	 *        The type of the list, must not be <code>null</code>.
	 * @param isMultiSelect
	 *        Flag, if list is a multi select list.
	 * @param isSystem
	 *        Flag, if list is a system list.
	 * @param someValues
	 *        The list of names of the elements within the list, must not be <code>null</code> but
	 *        empty.
	 * @param defaultElement
	 *        The name of the default list element, may be <code>null</code>.
	 * @param withElements
	 *        If true, will create list elements that are defined in the xml but still missing in
	 *        the list. This param will be ignored if the list is newly created, as in this case the
	 *        list elements are created anyway.
	 * @param unordered
	 *        Flag whether the {@link TLEnumeration} shall be unordered. May be <code>null</code>,
	 *        which means ordered.
	 * 
	 * @return The new created list, never <code>null</code>.
	 */
	private static TLEnumeration checkFastList(KnowledgeBase kb, String listName, String listType,
			boolean isMultiSelect,
			boolean isSystem, String[] someValues, String defaultElement, boolean withElements,
			TLAnnotation unordered) {
		FastList enumeration = FastList.getFastList(listName);

		if (enumeration == null) {
			{
				enumeration = createFastList(kb, listName, listType, isMultiSelect, isSystem, unordered);
                withElements = true; //list just created, need to create elements as well
            }
        }

        // create missing list elements
        if(withElements){
			{
                for (int thePos = 0; thePos < someValues.length; thePos++) {
                    String value = someValues[thePos];
					TLClassifier classifier = enumeration.getClassifier(value);
					if (classifier == null) {
						classifier = createListEntry(enumeration, value);
                    }
					if (value.equals(defaultElement)) {
						classifier.setDefault(true);
                    }
                }
				Logger.debug("Created ordered list named '" + listName + "'!", ListInitializationUtil.class);
            }
        }
		return enumeration;
    }

    /**
	 * Create an ordered list with the given parameters.
	 * 
	 * This method will call
	 * {@link FastList#createFastList(KnowledgeBase, String, String, String, boolean, boolean,TLAnnotation)}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create the list.
	 * @param aName
	 *        The name of the list to be created (will be the ID too).
	 * @param aListType
	 *        The type of list to be created.
	 * @param isMultiSelect
	 *        Flag, if list is multi selectable.
	 * @param isSystem
	 *        Flag, if list is system.
	 * @param unordered
	 *        Flag whether the {@link TLEnumeration} shall be unordered. May be <code>null</code>,
	 *        which means ordered.
	 * 
	 * @return The new created list, never <code>null</code>.
	 */
	private static FastList createFastList(KnowledgeBase kb, String aName, String aListType, boolean isMultiSelect,
			boolean isSystem, TLAnnotation unordered) {
		return FastList.createFastList(kb, aName, null, aListType, isMultiSelect, isSystem, unordered);
    }

    /**
	 * Create a list entry within the given list.
	 * 
	 * @param list
	 *        The list to create the entry for.
     * @param elementName
	 *        The name of the element to be created.
	 * @return The new created element of the list, never <code>null</code>.
	 */
	private static FastListElement createListEntry(FastList list, String elementName) {
		return list.addElement(null, elementName, null, 0);
    }

}

