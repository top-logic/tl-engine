/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.migration.model;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.element.config.AssociationConfig;
import com.top_logic.element.config.AssociationConfig.EndConfig;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.AttributedTypeConfig;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.EndAspect;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.InterfaceConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.ReferenceConfig.ReferenceKind;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.NamedPartConfig;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;

/**
 * Utilities for migration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationUtils {

	/** Tag name of a {@link AttributeConfig}. */
	private static final String ATTRIBUTE_CONFIG_TAG_NAME = ElementSchemaConstants.PROPERTY_ELEMENT;

	/** Tag name of a {@link EndConfig}. */
	private static final String END_CONFIG_TAG_NAME = ElementSchemaConstants.ASSOCIATION_END_ELEMENT;
	/** Tag name of a {@link ReferenceConfig}. */
	private static final String REFERENCE_CONFIG_TAG_NAME = ElementSchemaConstants.REFERENCE_ELEMENT;

	/**
	 * Modifies the stored application model base line.
	 * 
	 * @param log
	 *        {@link Log} to write errors and messages to
	 * @param connection
	 *        Connection to access the database.
	 * @param modification
	 *        {@link Function} receiving the parsed model definition, or <code>null</code>, if no
	 *        model baseline exists. The function can modify the document and returns whether
	 *        changes were made. If changes were made, the document is stored to the database.
	 */
	public static void modifyTLModel(Log log, PooledConnection connection,
			Function<Document, Boolean> modification) {
		String xml;
		try {
			xml = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
				DynamicModelService.APPLICATION_MODEL_PROPERTY);
		} catch (IllegalArgumentException | SQLException ex) {
			log.info("Cannot read the stored application model baseline.", Log.WARN);
			xml = null;
		}

		Document document;
		if (xml == null) {
			document = null;
		} else {
			try {
				document = DOMUtil.parse(xml);
			} catch (IllegalArgumentException ex) {
				log.error("Invalid model baseline: " + xml, ex);
				return;
			}
		}

		boolean changed = modification.apply(document);

		if (document != null && changed) {
			try {
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
					DynamicModelService.APPLICATION_MODEL_PROPERTY, DOMUtil.toString(document));
			} catch (IllegalArgumentException | SQLException ex) {
				log.error("Cannot store the upgraded application model baseline.");
				return;
			}

			log.info("Upgraded stored model.");
		}
	}

	/**
	 * Creates a new {@link TLProperty}.
	 * 
	 * @param partName
	 *        Name of the {@link TLProperty} to create.
	 * @param target
	 *        Name of {@link TLProperty#getType()} of the new {@link TLProperty}.
	 */
	public static Element createAttribute(Log log, Document model, QualifiedPartName partName,
			QualifiedTypeName target, Boolean mandatory, Boolean multiple, Boolean bag, Boolean ordered,
			Boolean isAbstract, AnnotatedConfig<?> annotations) throws MigrationException {
		return internalCreatePart(log, model, partName, target, ATTRIBUTE_CONFIG_TAG_NAME, mandatory,
			multiple, bag, ordered, isAbstract, annotations);
	}

	/**
	 * Creates a new inverse {@link TLReference}.
	 * 
	 * @param referenceName
	 *        Name of the {@link TLReference} to create.
	 * @param inverseReference
	 *        Name of {@link TLReference} for which the reference is the inverse reference.
	 */
	public static Element createBackReference(Log log, Document model, QualifiedPartName referenceName,
			QualifiedPartName inverseReference,
			Boolean mandatory, Boolean composite,
			Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered, Boolean isAbstract, Boolean navigate,
			HistoryType historyType, AnnotatedConfig<?> annotations, String endName) throws MigrationException {
		Element reference =
			createReference(log, model, referenceName, inverseReference.getOwner(), mandatory, composite, aggregate,
				multiple, bag, ordered, isAbstract, navigate, historyType, null, annotations, endName);
		reference.setAttribute(ReferenceConfig.INVERSE_REFERENCE, inverseReference.getPartName());
		reference.setAttribute(ReferenceConfig.KIND, ReferenceKind.BACKWARDS.getExternalName());
		return reference;
	}

	/**
	 * Creates a new {@link TLReference}.
	 * @param referenceName
	 *        Name of the {@link TLReference} to create.
	 * @param target
	 *        Name of {@link TLReference#getType()} of the new {@link TLReference}.
	 */
	public static Element createReference(Log log, Document model, QualifiedPartName referenceName,
			QualifiedTypeName target,
			Boolean mandatory, Boolean composite,
			Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered, Boolean isAbstract, Boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, AnnotatedConfig<?> annotations, String endName)
			throws MigrationException {
		Element reference = internalCreateEndAspect(log, model, referenceName, target,
			REFERENCE_CONFIG_TAG_NAME, mandatory, composite, aggregate, multiple, bag, ordered, isAbstract,
			navigate, historyType, deletionPolicy, annotations);
		if (endName != null) {
			reference.setAttribute(ReferenceConfig.END, endName);
		}
		return reference;
	}

	/**
	 * Creates a new {@link TLAssociationEnd}.
	 * @param endName
	 *        Name of the {@link TLAssociationEnd} to create.
	 * @param target
	 *        Name of {@link TLAssociationEnd#getType()} of the new {@link TLAssociationEnd}.
	 */
	public static Element createEnd(Log log, Document model, QualifiedPartName endName, QualifiedTypeName target,
			Boolean mandatory, Boolean composite,
			Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered, Boolean isAbstract, Boolean navigate,
			HistoryType historyType,
			DeletionPolicy deletionPolicy, AnnotatedConfig<?> annotations) throws MigrationException {
		return internalCreateEndAspect(log, model, endName, target, END_CONFIG_TAG_NAME,
			mandatory, composite, aggregate, multiple, bag, ordered, isAbstract, navigate, historyType, deletionPolicy,
			annotations);
	}

	private static Element internalCreateEndAspect(Log log, Document model, QualifiedPartName part,
			QualifiedTypeName target,
			String tagName,
			Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered,
			Boolean isAbstract, Boolean navigate,
			HistoryType historyType,
			DeletionPolicy deletionPolicy, AnnotatedConfig<?> annotations) throws MigrationException {
		Element end = internalCreatePart(log, model, part, target, tagName, mandatory, multiple, bag, ordered,
			isAbstract, annotations);
		if (composite != null) {
			end.setAttribute(EndAspect.COMPOSITE_PROPERTY, Boolean.toString(composite.booleanValue()));
		}
		if (aggregate != null) {
			end.setAttribute(EndAspect.AGGREGATE_PROPERTY, Boolean.toString(aggregate.booleanValue()));
		}
		if (navigate != null) {
			end.setAttribute(EndAspect.NAVIGATE_PROPERTY, Boolean.toString(navigate.booleanValue()));
		}
		if (historyType != null) {
			end.setAttribute(EndAspect.HISTORY_TYPE_PROPERTY, historyType.getExternalName());
		}
		if (deletionPolicy != null) {
			end.setAttribute(EndAspect.DELETION_POLICY_PROPERTY, deletionPolicy.getExternalName());
		}

		return end;
	}

	private static Element internalCreatePart(Log log, Document model, QualifiedPartName reference,
			QualifiedTypeName targetType,
			String tagName, Boolean mandatory, Boolean multiple, Boolean bag, Boolean ordered, Boolean isAbstract,
			AnnotatedConfig<?> annotations)
			throws MigrationException {
		Element module = getTLModuleOrFail(model, reference.getModuleName());
		Element owner = getTLTypeOrFail(log, module, reference.getTypeName());
		Element attributes = getAttributes(log, owner);
		if (attributes == null) {
			attributes = model.createElement(AttributedTypeConfig.ATTRIBUTES);
			owner.appendChild(attributes);
		}
		Element newAttribute = model.createElement(tagName);
		newAttribute.setAttribute(PartConfig.NAME, reference.getPartName());
		String type;
		if (targetType.getModuleName().equals(reference.getModuleName())) {
			type = targetType.getTypeName();
		} else {
			type = targetType.getName();
		}
		setTargetType(newAttribute, type);
		if (mandatory != null) {
			newAttribute.setAttribute(PartConfig.MANDATORY, Boolean.toString(mandatory.booleanValue()));
		}
		if (multiple != null) {
			newAttribute.setAttribute(PartConfig.MULTIPLE_PROPERTY, Boolean.toString(multiple.booleanValue()));
		}
		if (bag != null) {
			newAttribute.setAttribute(PartConfig.BAG_PROPERTY, Boolean.toString(bag.booleanValue()));
		}
		if (ordered != null) {
			newAttribute.setAttribute(PartConfig.ORDERED_PROPERTY, Boolean.toString(ordered.booleanValue()));
		}
		if (isAbstract != null) {
			newAttribute.setAttribute(PartConfig.ABSTRACT_PROPERTY, Boolean.toString(isAbstract.booleanValue()));
		}
		updateModelPartAnnotations(log, newAttribute, null, Util.toString(annotations));

		attributes.appendChild(newAttribute);
		return newAttribute;
	}

	private static void setTargetType(Element structuredTypePart, String targetType) {
		structuredTypePart.setAttribute(PartConfig.TYPE_SPEC, targetType);
	}

	/**
	 * Fetches an existing {@link TLModule} from the given model.
	 * 
	 * @throws MigrationException
	 *         When no such module exists.
	 */
	public static Element getTLModuleOrFail(Document model, String name) throws MigrationException {
		NodeList nodes = model.getElementsByTagName(ModelConfig.MODULE);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element module = (Element) nodes.item(i);
			String moduleName = name(module);
			if (Utils.equals(name, moduleName)) {
				return module;
			}
		}
		throw new MigrationException("No module with name " + name + " found.");
	}

	/**
	 * Fetches an existing {@link TLType} from the given module.
	 * 
	 * @throws MigrationException
	 *         When no such type exists.
	 */
	public static Element getTLTypeOrFail(Log log, Element module, String typeName) throws MigrationException {
		Element tlClass = getTLClass(log, module, typeName);
		if (tlClass != null) {
			return tlClass;
		}
		Element tlDataType = getTLDatatype(log, module, typeName);
		if (tlDataType != null) {
			return tlDataType;
		}
		Element enumType = getTLEnumeration(log, module, typeName);
		if (enumType != null) {
			return enumType;
		}
		Element associationType = getTLAssociation(log, module, typeName);
		if (associationType != null) {
			return associationType;
		}
		throw new MigrationException(
			"No such type: " + TLModelUtil.qualifiedName(name(module), typeName));
	}

	private static Element getTLDatatype(Log log, Element module, String dataTypeName) {
		Element dataType = getUniqueChild(module,
			child -> DatatypeConfig.TAG_NAME.equals(child.getTagName()) && dataTypeName.equals(name(child)),
			child -> log.error(
				"Multiple datatype objects with name '" + dataTypeName + "': " + DOMUtil.toString(module)));
		if (dataType != null) {
			return dataType;
		}
		// check for existence of surrounding "types" tag.
		Element types = getTypesChild(log, module);
		if (types == null) {
			return null;
		} else {
			return getTLDatatype(log, types, dataTypeName);
		}
	}

	private static Element getTypesChild(Log log, Element module) {
		return getUniqueChild(module,
			child -> ModuleConfig.TYPES.equals(child.getTagName()),
			child -> log.error(
				"Multiple types children in module: " + DOMUtil.toString(module)));
	}

	private static Element getTLClass(Log log, Element module, String className) {
		Element classType = getUniqueChild(module,
			child -> {
				switch (child.getTagName()) {
					case InterfaceConfig.TAG_NAME:
					case ClassConfig.TAG_NAME:
						return className.equals(name(child));
					default:
						return false;
				}
			},
			child -> log.error("Multiple class objects with name '" + className + "': " + DOMUtil.toString(module)));
		if (classType != null) {
			return classType;
		}
		// check for existence of surrounding "types" tag.
		Element types = getTypesChild(log, module);
		if (types == null) {
			return null;
		} else {
			return getTLClass(log, types, className);
		}
	}

	private static Element getTLAssociation(Log log, Element module, String associationName) {
		Element associationType = getUniqueChild(module,
			child -> AssociationConfig.TAG_NAME.equals(child.getTagName()) && associationName.equals(name(child)),
			child -> log.error(
				"Multiple association objects with name '" + associationName + "': " + DOMUtil.toString(module)));
		if (associationType != null) {
			return associationType;
		}
		// check for existence of surrounding "types" tag.
		Element types = getTypesChild(log, module);
		if (types == null) {
			return null;
		} else {
			return getTLAssociation(log, types, associationName);
		}
	}

	private static String name(Element namedModelPart) {
		return namedModelPart.getAttribute(NamedPartConfig.NAME);
	}

	private static Element getTLEnumeration(Log log, Element module, String enumName) {
		Element enumType = getUniqueChild(module,
			child -> EnumConfig.TAG_NAME.equals(child.getTagName()) && enumName.equals(name(child)),
			child -> log.error("Multiple enumerations with name '" + enumName + "': " + DOMUtil.toString(module)));
		if (enumType != null) {
			return enumType;
		}
		// check for existence of surrounding "types" tag.
		Element types = getTypesChild(log, module);
		if (types == null) {
			return null;
		} else {
			return getTLEnumeration(log, types, enumName);
		}

	}

	/**
	 * Adds a generalisation for the given specialisation.
	 */
	public static void addGeneralisation(Log log, Document model, QualifiedTypeName specialisation,
			QualifiedTypeName generalisation)
			throws MigrationException {
		addGeneralisation(log, model,
			specialisation.getModuleName(), specialisation.getTypeName(),
			generalisation.getModuleName(), generalisation.getTypeName());
	}

	/**
	 * Adds a generalisation for the given specialisation.
	 */
	public static void addGeneralisation(Log log, Document model, String specialisationModule,
			String specialisationName, String generalisationModule, String generalisationName)
			throws MigrationException {
		Element specModule = getTLModuleOrFail(model, specialisationModule);
		Element specType = getTLTypeOrFail(log, specModule, specialisationName);
		Element genModule = getTLModuleOrFail(model, generalisationModule);
		Element genType = getTLTypeOrFail(log, genModule, generalisationName);
		assert genType != null;

		Element generalizations = getGeneralizations(log, specType);
		if (generalizations == null) {
			generalizations = specType.getOwnerDocument().createElement(ObjectTypeConfig.GENERALIZATIONS);
			specType.appendChild(generalizations);
		}
		Element newGeneralization = specType.getOwnerDocument().createElement(ExtendsConfig.TAG_NAME);
		if (specialisationModule.equals(generalisationModule)) {
			newGeneralization.setAttribute(ExtendsConfig.TYPE, generalisationName);
		} else {
			newGeneralization.setAttribute(ExtendsConfig.TYPE,
				TLModelUtil.qualifiedName(generalisationModule, generalisationName));
		}
		generalizations.appendChild(newGeneralization);
	}

	private static Element getGeneralizations(Log log, Element structuredType) {
		return getUniqueChild(structuredType,
			child -> ObjectTypeConfig.GENERALIZATIONS.equals(child.getTagName()),
			child -> log.error("Multiple generalizations: " + DOMUtil.toString(structuredType)));
	}

	/**
	 * Removes a generalisation from the given specialisation.
	 */
	public static boolean removeGeneralisation(Log log, Document tlModel, QualifiedTypeName specialisation,
			QualifiedTypeName generalisation) throws MigrationException {
		return removeGeneralisation(log, tlModel, specialisation.getModuleName(), specialisation.getTypeName(),
			generalisation.getModuleName(), generalisation.getTypeName());
	}

	/**
	 * Removes a generalisation from the given specialisation.
	 */
	public static boolean removeGeneralisation(Log log, Document model, String specialisationModule,
			String specialisationName, String generalisationModule, String generalisationName)
			throws MigrationException {
		Element specModule = getTLModuleOrFail(model, specialisationModule);
		Element specType = getTLTypeOrFail(log, specModule, specialisationName);

		Element generalizations = getGeneralizations(log, specType);
		if (generalizations == null) {
			return false;
		}
		Element extensionToRemove = findGeneralisation(generalizations,
			generalisationModule.equals(specialisationModule), generalisationModule, generalisationName);
		if (extensionToRemove == null) {
			return false;
		}
		extensionToRemove.getParentNode().removeChild(extensionToRemove);
		return true;

	}

	/**
	 * Reorders the generalisation <code>generalisation</code> of <code>specialisation</code> before
	 * <code>before</code>.
	 */
	public static boolean reorderGeneralisation(Log log, Document tlModel, QualifiedTypeName specialisation,
			QualifiedTypeName generalization, QualifiedTypeName before) throws MigrationException {
		if (before == null) {
			return reorderGeneralisation(log, tlModel, specialisation.getModuleName(), specialisation.getTypeName(),
				generalization.getModuleName(), generalization.getTypeName(), null, null);
		} else {
			return reorderGeneralisation(log, tlModel, specialisation.getModuleName(), specialisation.getTypeName(),
				generalization.getModuleName(), generalization.getTypeName(), before.getModuleName(),
				before.getTypeName());
		}
	}

	/**
	 * Reorders the generalisation <code>generalisation</code> of <code>specialisation</code> before
	 * <code>before</code>.
	 */
	private static boolean reorderGeneralisation(Log log, Document model, String specialisationModule,
			String specialisationName, String generalisationModule, String generalisationName, String beforeModule,
			String beforeName) throws MigrationException {
		Element specModule = getTLModuleOrFail(model, specialisationModule);
		Element specType = getTLTypeOrFail(log, specModule, specialisationName);

		Element generalizations = getGeneralizations(log, specType);
		if (generalizations == null) {
			log.error("Type '" + TLModelUtil.qualifiedName(specialisationModule, specialisationName)
					+ "' has no generalizations.");
			return false;
		}
		Element extensionToMove =
			findGeneralisation(generalizations, generalisationModule.equals(specialisationModule), generalisationModule,
				generalisationName);
		if (extensionToMove == null) {
			log.error("Type '" + TLModelUtil.qualifiedName(specialisationModule, specialisationName)
					+ "' has no generalisation '" + TLModelUtil.qualifiedName(generalisationModule, generalisationName)
					+ "'.");
			return false;
		}
		Element beforeExtension;
		if (beforeModule == null) {
			beforeExtension = null;
		} else {
			beforeExtension = findGeneralisation(generalizations, beforeModule.equals(specialisationModule),
				beforeModule, beforeName);
			if (beforeExtension == null) {
				log.error("Type '" + TLModelUtil.qualifiedName(specialisationModule, specialisationName)
						+ "' has no generalisation '" + TLModelUtil.qualifiedName(beforeModule, beforeName)
						+ "'.");
				return false;
			}
		}
		extensionToMove.getParentNode().insertBefore(extensionToMove, beforeExtension);
		return true;
	}

	private static Element findGeneralisation(Element generalizations, boolean sameModule, String generalisationModule,
			String generalisationName) {
		String qGeneralisationName = TLModelUtil.qualifiedName(generalisationModule, generalisationName);

		Element extensionToRemove = null;
		NodeList extensions = generalizations.getElementsByTagName(ExtendsConfig.TAG_NAME);
		for (int i = 0; i < extensions.getLength(); i++) {
			Element extension = (Element) extensions.item(i);
			String type = extension.getAttribute(ExtendsConfig.TYPE);
			if (qGeneralisationName.equals(type)) {
				extensionToRemove = extension;
				break;
			}
			if (sameModule && generalisationName.equals(type)) {
				extensionToRemove = extension;
				break;
			}
		}
		return extensionToRemove;
	}

	/**
	 * Fetches an existing {@link TLTypePart} from the given {@link TLType}.
	 * 
	 * @throws MigrationException
	 *         If no such part exists.
	 */
	public static Element getTLTypePartOrFail(Log log, Element type, String partName) throws MigrationException {
		Element part = getTLStructuredTypePart(log, type, partName);
		if (part != null) {
			return part;
		}
		part = getTLClassifier(log, type, partName);
		if (part != null) {
			return part;
		}
		throw new MigrationException("No part '" + partName + "' found in " + name(type) + ".");
	}

	private static Element getTLStructuredTypePart(Log log, Element type, String partName) {
		Element attributes = getAttributes(log, type);
		if (attributes == null) {
			return null;
		}
		return getUniqueChild(attributes,
			child -> {
				switch (child.getTagName()) {
					case ATTRIBUTE_CONFIG_TAG_NAME:
					case END_CONFIG_TAG_NAME:
					case REFERENCE_CONFIG_TAG_NAME:
						return partName.equals(name(child));
					default:
						return false;
				}
			},
			child -> log.error("Multiple part with name '" + partName + "': " + DOMUtil.toString(type)));
	}

	private static Element getAttributes(Log log, Element structuredType) {
		return getUniqueChild(structuredType,
			child -> AttributedTypeConfig.ATTRIBUTES.equals(child.getTagName()),
			child -> log.error("Multiple attributes: " + DOMUtil.toString(structuredType)));
	}

	private static Element getTLClassifier(Log log, Element enumeration, String classifierName) {
		return getUniqueChild(enumeration,
			child -> ClassifierConfig.TAG_NAME.equals(child.getTagName()) && classifierName.equals(name(child)),
			child -> log
				.error("Multiple classifier with name '" + classifierName + "': " + DOMUtil.toString(enumeration)));
	}

	/**
	 * Adds a new {@link SingletonConfig} annotation to the given module.
	 */
	public static void addModuleSingleton(Log log, Element module, QualifiedTypeName type, String name) {
		Element singletons;
		Element annotations = getModelPartAnnotations(log, module);
		if (annotations != null) {
			singletons = getUniqueChild(annotations,
				child -> TLSingletons.TAG_NAME.equals(child.getTagName()),
				child -> log.error("Multiple singletons annotation in module " + DOMUtil.toString(module)));
		} else {
			Document tlModel = module.getOwnerDocument();
			annotations = tlModel.createElement(AnnotatedConfig.ANNOTATIONS);
			module.appendChild(annotations);
			singletons = null;
		}
		if (singletons == null) {
			singletons = annotations.getOwnerDocument().createElement(TLSingletons.TAG_NAME);
			annotations.appendChild(singletons);
		}
		createSingleton(module, singletons, type, name);
	}

	private static Element createSingleton(Element module, Element singletons, QualifiedTypeName type, String name) {
		Element newSingleton = module.getOwnerDocument().createElement("singleton");
		String typeName;
		if (type.getModuleName().equals(name(module))) {
			typeName = type.getTypeName();
		} else {
			typeName = type.getName();
		}
		newSingleton.setAttribute(SingletonConfig.TYPE_SPEC, typeName);
		if (!StringServices.isEmpty(name)) {
			newSingleton.setAttribute(SingletonConfig.NAME, name);
		}
		singletons.appendChild(newSingleton);
		return newSingleton;
	}

	/**
	 * Adds the given annotations increment to {@link TLModule#getAnnotations()}.
	 */
	public static void addModuleAnnotations(Log log, Document model, String moduleName,
			AnnotatedConfig<? extends TLAnnotation> increment) throws MigrationException {
		Element module;
		try {
			module = getTLModuleOrFail(model, moduleName);
		} catch (MigrationException ex) {
			log.info("No module with name '" + moduleName + "' found.", Protocol.WARN);
			return;
		}
		try {
			addModelPartAnnotations(log, module, increment);
		} catch (ConfigurationException ex) {
			throw new MigrationException("Unable to parse annotations for module '" + moduleName + "'.", ex);
		}
	}

	private static void addModelPartAnnotations(Log log, Element part,
			AnnotatedConfig<? extends TLAnnotation> increment)
			throws ConfigurationException {
		Element moduleAnnotations = getModelPartAnnotations(log, part);
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		String baseAnnotations;
		if (moduleAnnotations == null) {
			baseAnnotations = StringServices.EMPTY_STRING;
		} else {
			baseAnnotations = toAnnotationConfigs(moduleAnnotations);
		}
		newAnnotations = Util.addAnnotations(baseAnnotations, increment);
		updateModelPartAnnotations(log, part, moduleAnnotations, Util.toString(newAnnotations));
	}

	/**
	 * @param annotations
	 *        An element with local name {@link AnnotatedConfig#ANNOTATIONS}.
	 * 
	 * @return A serialized {@link AnnotationConfigs} with tag name "config".
	 */
	private static String toAnnotationConfigs(Element annotations) {
		return "<config>" + DOMUtil.toStringRaw(annotations) + "</config>";
	}

	private static boolean removeModelPartAnnotations(Log log, Element part,
			Collection<? extends Class<? extends TLAnnotation>> toRemove) {
		if (toRemove.isEmpty()) {
			return false;
		}
		Element moduleAnnotations = getModelPartAnnotations(log, part);
		if (moduleAnnotations == null) {
			return false;
		}
		boolean changes = false;
		AnnotationConfigs annotationConfigs;
		try {
			annotationConfigs = Util.parsePersistentAnnotations(toAnnotationConfigs(moduleAnnotations));
		} catch (ConfigurationException ex) {
			log.error("Unable to parse anntations for part '" + part.getTagName() + "'.", ex);
			return false;
		}
		for (Class<? extends TLAnnotation> annotationClass : toRemove) {
			TLAnnotation existingAnnotation = annotationConfigs.getAnnotation(annotationClass);
			if (existingAnnotation != null) {
				changes = true;
				annotationConfigs.getAnnotations().remove(existingAnnotation);
			}
		}
		if (changes) {
			updateModelPartAnnotations(log, part, moduleAnnotations, Util.toString(annotationConfigs));
		}
		return changes;
	}

	/**
	 * Updates {@link TLModelPart#getAnnotations()} of the given model part.
	 * 
	 * @param modelPart
	 *        The model part to set annotations.
	 * @param annotationsElement
	 *        The {@value TLModelPart#ANNOTATIONS_ATTR} child of the <code>modelPart</code> or
	 *        <code>null</code> if there is no such child.
	 * @param newAnnotations
	 *        New Annotations to set: Either <code>null</code> or a serialized
	 *        {@link AnnotationConfigs}.
	 */
	public static void updateModelPartAnnotations(Log log, Element modelPart, Element annotationsElement,
			String newAnnotations) {
		if (newAnnotations == null) {
			if (annotationsElement != null) {
				modelPart.removeChild(annotationsElement);
			}
		} else {
			Document parsedAnnotations = DOMUtil.parse(newAnnotations);
			Element annotations = parsedAnnotations.getDocumentElement();
			// Annotations is of type
			// com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs
			Element modelPartAnnotations = getModelPartAnnotations(log, annotations);
			if (modelPartAnnotations == null) {
				// empty annotations.
				if (annotationsElement != null) {
					modelPart.removeChild(annotationsElement);
				}
				return;
			}
			Node adaptedNode = modelPart.getOwnerDocument().importNode(modelPartAnnotations, true);

			if (annotationsElement != null) {
				modelPart.replaceChild(adaptedNode, annotationsElement);
			} else {
				modelPart.insertBefore(adaptedNode, modelPart.getFirstChild());
			}
		}
	}

	/**
	 * Reads the {@link TLModelPart#getAnnotations()} of a {@link TLModelPart}.
	 * 
	 * @return The stored {@link AnnotatedConfig annotations} object. May be <code>null</code>.
	 */
	public static Element getModelPartAnnotations(Log log, Element annotatedConfig) {
		return getUniqueChild(annotatedConfig,
			child -> AnnotatedConfig.ANNOTATIONS.equals(child.getTagName()),
			child -> log.error("Multiple annotations in " + DOMUtil.toString(annotatedConfig)));
	}

	private static Element getUniqueChild(Element elem, Predicate<Element> matcher, Consumer<Element> clash) {
		Element result = null;
		for (Element child : DOMUtil.elements(elem)) {
			if (matcher.test(child)) {
				if (result != null) {
					clash.accept(child);
					continue;
				}
				result = child;
			}
		}
		return result;

	}

	/**
	 * Adds the given annotations increment to {@link TLType#getAnnotations()}.
	 */
	public static void addTypeAnnotations(Log log, Element module, String typeName,
			AnnotatedConfig<? extends TLAnnotation> annotations) throws MigrationException {
		Element type;
		try {
			type = getTLTypeOrFail(log, module, typeName);
		} catch (MigrationException ex) {
			String qTypeName = TLModelUtil.qualifiedName(name(module), typeName);
			log.info("No type '" + qTypeName + "' found.", Protocol.WARN);
			return;
		}

		try {
			addModelPartAnnotations(log, type, annotations);
		} catch (ConfigurationException ex) {
			String qTypeName = TLModelUtil.qualifiedName(name(module), typeName);
			throw new MigrationException("Unable to parse annotations for type '" + qTypeName + "'.", ex);
		}
	}

	/**
	 * Adds the given annotations increment to {@link TLTypePart#getAnnotations()}.
	 */
	public static void addTypePartAnnotations(Log log, Element owner, String partName,
			AnnotatedConfig<? extends TLAnnotation> increment) throws MigrationException {
		Element part;
		try {
			part = getTLTypePartOrFail(log, owner, partName);
		} catch (MigrationException ex) {
			log.info("No type part '" + name(owner) + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + partName + "' found.",
				Protocol.WARN);
			return;
		}
		try {
			addModelPartAnnotations(log, part, increment);
		} catch (ConfigurationException ex) {
			throw new MigrationException("Unable to parse annotations for part '" + name(owner)
					+ TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + partName + "'.",
				ex);
		}

	}

	/**
	 * Creates a new {@link TLClass} in the model identified by the given type name.
	 * 
	 * @see #createClassType(Log, Element, String, boolean, Boolean, AnnotatedConfig)
	 */
	public static Element createClassType(Log log, Document model, QualifiedTypeName typeName, boolean isAbstract,
			Boolean isFinal, AnnotatedConfig<TLTypeAnnotation> annotations) throws MigrationException {
		Element module = getTLModuleOrFail(model, typeName.getModuleName());
		return createClassType(log, module, typeName.getTypeName(), isAbstract, isFinal, annotations);

	}

	/**
	 * Creates a new {@link TLClass} in the given module with given name.
	 */
	public static Element createClassType(Log log, Element module, String typeName, boolean isAbstract,
			Boolean isFinal, AnnotatedConfig<TLTypeAnnotation> annotations) {
		String tagName = isAbstract ? InterfaceConfig.TAG_NAME : ClassConfig.TAG_NAME;
		Element classType = createType(log, module, tagName, typeName, annotations);
		if (isFinal != null) {
			classType.setAttribute(ClassConfig.FINAL, Boolean.toString(isFinal.booleanValue()));
		}
		return classType;
	}

	/**
	 * Creates a new {@link TLAssociation} in the model identified by the given type name.
	 */
	public static Element createAssociationType(Log log, Document model, QualifiedTypeName typeName,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws MigrationException {
		Element module = getTLModuleOrFail(model, typeName.getModuleName());
		return createAssociationType(log, module, typeName.getTypeName(), annotations);
	}

	/**
	 * Creates a new {@link TLAssociation} in the given module with given name.
	 */
	public static Element createAssociationType(Log log, Element module, String typeName,
			AnnotatedConfig<TLTypeAnnotation> annotations) {
		return createType(log, module, AssociationConfig.TAG_NAME, typeName, annotations);
	}

	/**
	 * Creates a new {@link TLPrimitive} in the model identified by the given type name.
	 */
	public static Element createDatatype(Log log, Document model, QualifiedTypeName typeName, Kind kind,
			DBColumnType columnType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws MigrationException {
		Element module = getTLModuleOrFail(model, typeName.getModuleName());
		return createDatatype(log, module, typeName.getTypeName(), kind, columnType, storageMapping, annotations);
	}

	/**
	 * Creates a new {@link TLPrimitive} in the given module with given name.
	 */
	public static Element createDatatype(Log log, Element module, String typeName, Kind kind,
			DBColumnType columnType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations) {
		Element datatype = createType(log, module, DatatypeConfig.TAG_NAME, typeName, annotations);
		datatype.setAttribute(DatatypeConfig.KIND, kind.getExternalName());
		copyColumnType(datatype, columnType);
		if (storageMapping != null) {
			String serializedStorageMapping = storageMappingToString(storageMapping);
			Element storageMappingElement = DOMUtil.parse(serializedStorageMapping).getDocumentElement();
			datatype.appendChild(module.getOwnerDocument().importNode(storageMappingElement, true));
		}
		return datatype;
	}

	private static void copyColumnType(Element datatype, DBColumnType columnType) {
		if (columnType.getDBType() != null) {
			datatype.setAttribute(DOXMLConstants.DB_TYPE_ATTRIBUTE, columnType.getDBType().getExternalName());
		}
		if (columnType.getDBSize() != null) {
			datatype.setAttribute(DOXMLConstants.DB_SIZE_ATTRIBUTE,
				Integer.toString(columnType.getDBSize().intValue()));
		}
		if (columnType.getDBPrecision() != null) {
			datatype.setAttribute(DOXMLConstants.DB_PREC_ATTRIBUTE,
				Integer.toString(columnType.getDBPrecision().intValue()));
		}
		if (columnType.isBinary() != null) {
			datatype.setAttribute(DOXMLConstants.BINARY_ATTRIBUTE,
				Boolean.toString(columnType.isBinary().booleanValue()));
		}
	}

	private static String storageMappingToString(PolymorphicConfiguration<StorageMapping<?>> config) {
		StringWriter buffer = new StringWriter();
		try {
			try (ConfigurationWriter w = new ConfigurationWriter(buffer)) {
				w.write(DatatypeConfig.STORAGE_MAPPING, PolymorphicConfiguration.class, config);
			}
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
		return buffer.toString();

	}

	/**
	 * Creates a new {@link TLEnumeration} in the model identified by the given type name.
	 */
	public static Element createEnumType(Log log, Document tlModel, QualifiedTypeName enumName,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, enumName.getModuleName());
		return createEnumType(log, module, enumName.getTypeName(), annotations);
	}

	/**
	 * Creates a new {@link TLEnumeration} in the given module with given name.
	 */
	public static Element createEnumType(Log log, Element module, String typeName,
			AnnotatedConfig<TLTypeAnnotation> annotations) {
		return createType(log, module, EnumConfig.TAG_NAME, typeName, annotations);
	}

	private static Element createType(Log log, Element module, String tagName, String typeName,
			AnnotatedConfig<TLTypeAnnotation> annotations) {
		Element newType = module.getOwnerDocument().createElement(tagName);

		newType.setAttribute(TypeConfig.NAME, typeName);
		updateModelPartAnnotations(log, newType, null, Util.toString(annotations));
		appendTypeToModule(log, module, newType);
		return newType;
	}

	private static void appendTypeToModule(Log log, Element module, Element newType) {
		Element types = getTypesChild(log, module);
		if (types != null) {
			// Types are surrounded by a "types" tag
			types.appendChild(newType);
		} else {
			module.appendChild(newType);
		}
	}

	/**
	 * Creates a new {@link TLClassifier} in the given {@link TLEnumeration} with given name.
	 */
	public static Element createClassifier(Log log, Document tlModel, QualifiedPartName classifierName,
			AnnotatedConfig<TLClassifierAnnotation> annotations)
			throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, classifierName.getModuleName());
		Element enumeration = getTLEnumeration(log, module, classifierName.getTypeName());
		if (enumeration == null) {
			throw new MigrationException("No enumeration with name " + classifierName.getTypeName() + " in module "
					+ classifierName.getModuleName() + ".");
		}

		Element classifierElement = tlModel.createElement(ClassifierConfig.TAG_NAME);
		classifierElement.setAttribute(ClassifierConfig.NAME, classifierName.getPartName());
		updateModelPartAnnotations(log, classifierElement, null, Util.toString(annotations));
		enumeration.appendChild(classifierElement);
		return classifierElement;
	}

	@SuppressWarnings("unchecked")
	static <T> T nullIfUnset(ConfigurationItem config, String propertyName) {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		if (config.valueSet(property)) {
			return (T) config.value(property);
		}
		return null;
	}

	/**
	 * Creates a new {@link TLModule}.
	 */
	public static Element createModule(Log log, Document tlModel, String name,
			AnnotatedConfig<TLModuleAnnotation> annotations) {
		Element module = tlModel.createElement(ModelConfig.MODULE);
		module.setAttribute(ModuleConfig.NAME, name);
		updateModelPartAnnotations(log, module, null, Util.toString(annotations));
		tlModel.getDocumentElement().appendChild(module);
		return module;
	}

	/**
	 * Deletes the {@link TLType} with the given name.
	 * 
	 * @return <code>true</code> iff the type could be found and was removed.
	 */
	public static boolean deleteType(Log log, Document tlModel, QualifiedTypeName typeName) {
		Element module;
		try {
			module = getTLModuleOrFail(tlModel, typeName.getModuleName());
		} catch (MigrationException ex) {
			log.info("No module " + typeName.getModuleName() + " found to delete type " + typeName + ".", Log.WARN);
			return false;
		}
		Element type;
		try {
			type = getTLTypeOrFail(log, module, typeName.getTypeName());
		} catch (MigrationException ex) {
			log.info("No type " + typeName.getName() + " found to delete.", Log.WARN);
			return false;
		}
		type.getParentNode().removeChild(type);
		return true;
	}

	/**
	 * Deletes the {@link TLModule} with the given name.
	 * 
	 * @return <code>true</code> iff the module could be found and was removed.
	 */
	public static boolean deleteModule(Log log, Document tlModel, String moduleName) {
		Element module;
		try {
			module = getTLModuleOrFail(tlModel, moduleName);
		} catch (MigrationException ex) {
			log.info("No module " + moduleName + " found to delete.", Log.WARN);
			return false;
		}
		module.getParentNode().removeChild(module);
		return true;
	}

	/**
	 * Deletes the {@link TLTypePart} with the given name.
	 * 
	 * @return <code>true</code> iff the part could be found and was removed.
	 */
	public static boolean deleteTypePart(Log log, Document tlModel, QualifiedPartName partName) {
		Element module;
		try {
			module = getTLModuleOrFail(tlModel, partName.getModuleName());
		} catch (MigrationException ex) {
			log.info("No module " + partName.getModuleName() + " found to delete part " + partName + ".", Log.WARN);
			return false;
		}
		Element type;
		try {
			type = getTLTypeOrFail(log, module, partName.getTypeName());
		} catch (MigrationException ex) {
			log.info("No type " + partName.getOwner().getName() + " found to delete part " + partName + ".", Log.WARN);
			return false;
		}

		Element part;
		try {
			part = getTLTypePartOrFail(log, type, partName.getPartName());
		} catch (MigrationException ex) {
			log.info("No part " + partName.getName() + " found to delete.", Log.WARN);
			return false;
		}
		Node attributes = part.getParentNode();
		attributes.removeChild(part);
		if (!DOMUtil.elements(attributes).iterator().hasNext()) {
			// All parts removed. Remove attributes tag.
			attributes.getParentNode().removeChild(attributes);
		}
		return true;
	}

	/**
	 * Sets {@link PartConfig#isOverride()} for the given {@link TLTypePart}.
	 */
	public static void setOverride(Log log, Document tlModel, QualifiedPartName partName, boolean newOverride)
			throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, partName.getModuleName());
		Element type = getTLTypeOrFail(log, module, partName.getTypeName());
		Element part = getTLTypePartOrFail(log, type, partName.getPartName());
		if (newOverride) {
			part.setAttribute(PartConfig.OVERRIDE, Boolean.toString(newOverride));
			Attr inverseReferenceAttribute = part.getAttributeNode(ReferenceConfig.INVERSE_REFERENCE);
			if (inverseReferenceAttribute != null) {
				log.info("Removed attribute '" + ReferenceConfig.INVERSE_REFERENCE
						+ "' from " + partName.getName() + ", because it is redundant and not allowed in overrides.");
				part.removeAttribute(ReferenceConfig.INVERSE_REFERENCE);
			}
		} else {
			part.removeAttribute(PartConfig.OVERRIDE);
		}
	}

	/**
	 * Removes some annotations from a {@link TLModule}.
	 */
	public static boolean removeModuleAnnotations(Log log, Document tlModel, String moduleName,
			Collection<? extends Class<? extends TLAnnotation>> toRemove) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, moduleName);
		return removeModelPartAnnotations(log, module, toRemove);
	}

	/**
	 * Removes some annotations from a {@link TLType}.
	 */
	public static boolean removeTypeAnnotations(Log log, Document tlModel,
			String moduleName, String typeName,
			Set<Class<? extends TLAnnotation>> toRemove) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, moduleName);
		Element type = getTLTypeOrFail(log, module, typeName);
		return removeModelPartAnnotations(log, type, toRemove);
	}

	/**
	 * Removes some annotations from a {@link TLTypePart}.
	 */
	public static boolean removeTypePartAnnotations(Log log, Document tlModel,
			String moduleName, String typeName, String partName,
			Set<Class<? extends TLAnnotation>> toRemove) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, moduleName);
		Element type = getTLTypeOrFail(log, module, typeName);
		Element part = getTLTypePartOrFail(log, type, partName);
		return removeModelPartAnnotations(log, part, toRemove);
	}

	/**
	 * Reorders the {@link TLStructuredTypePart} <code>partToReorder</code> before
	 * <code>before</code>.
	 */
	public static boolean reorderStructuredTypePart(Log log, Document tlModel, QualifiedPartName partToReorder,
			String before) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, partToReorder.getModuleName());
		Element type = getTLClass(log, module, partToReorder.getTypeName());
		if (type == null) {
			throw new MigrationException("No class '" + partToReorder.getOwner() + "' found.");
		}
		Element part = getTLStructuredTypePart(log, type, partToReorder.getPartName());
		if (part == null) {
			throw new MigrationException("No part '" + partToReorder + "' found.");
		}
		Element beforeElement;
		if (before == null) {
			beforeElement = null;
		} else {
			beforeElement = getTLStructuredTypePart(log, type, before);
			if (beforeElement == null) {
				throw new MigrationException("No part '" + before + "' found in '" + partToReorder.getOwner() + "'.");
			}
		}
		part.getParentNode().insertBefore(part, beforeElement);
		return true;
	}

	/**
	 * Reorders the {@link TLClassifier} <code>partToReorder</code> before <code>before</code>.
	 */
	public static boolean reorderClassifier(Log log, Document tlModel, QualifiedPartName partToReorder, String before)
			throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, partToReorder.getModuleName());
		Element enumeration = getTLEnumeration(log, module, partToReorder.getTypeName());
		if (enumeration == null) {
			throw new MigrationException("No enumeration '" + partToReorder.getOwner() + "' found.");
		}
		Element part = getTLClassifier(log, enumeration, partToReorder.getPartName());
		if (part == null) {
			throw new MigrationException("No classifier '" + partToReorder + "' found.");
		}
		Element beforeElement;
		if (before == null) {
			beforeElement = null;
		} else {
			beforeElement = getTLClassifier(log, enumeration, before);
			if (beforeElement == null) {
				throw new MigrationException(
					"No classifier '" + before + "' found in '" + partToReorder.getOwner() + "'.");
			}
		}
		part.getParentNode().insertBefore(part, beforeElement);
		return true;
	}

	/**
	 * Declares the given <code>defaultClassifier</code> as "default". If <code>null</code> no
	 * classifier will be "default".
	 */
	public static boolean setDefaultClassifier(Log log, Document tlModel, QualifiedTypeName enumName,
			String defaultClassifier) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, enumName.getModuleName());
		Element enumeration = getTLEnumeration(log, module, enumName.getTypeName());
		if (enumeration == null) {
			throw new MigrationException("No enumeration '" + enumName + "' found.");
		}
		NodeList classifiers = enumeration.getElementsByTagName(ClassifierConfig.TAG_NAME);
		boolean changes = false;
		for (int i = 0; i < classifiers.getLength(); i++) {
			Element classifier = (Element) classifiers.item(i);
			String currentDefault = classifier.getAttribute(ClassifierConfig.DEFAULT);
			if (Boolean.toString(true).equals(currentDefault)) {
				if (defaultClassifier == null || !defaultClassifier.equals(name(classifier))) {
					changes = true;
					classifier.removeAttribute(ClassifierConfig.DEFAULT);
				}
			} else {
				if (defaultClassifier != null && defaultClassifier.equals(name(classifier))) {
					changes = true;
					classifier.setAttribute(ClassifierConfig.DEFAULT, Boolean.toString(true));
				}
			}
		}
		return changes;
	}

	/**
	 * Sets the given {@link AnnotatedConfig annotation} to a {@link TLModule}.
	 */
	public static boolean setModuleAnnotations(Log log, Document model, String moduleName,
			AnnotatedConfig<? extends TLAnnotation> annotations) {
		Element module;
		try {
			module = getTLModuleOrFail(model, moduleName);
		} catch (MigrationException ex) {
			log.info("No module with name '" + moduleName + "' found.", Protocol.WARN);
			return false;
		}
		setModelPartAnnotations(log, module, annotations);
		return true;
	}

	/**
	 * Sets the given {@link AnnotatedConfig annotation} to a {@link TLType}.
	 */
	public static boolean setTypeAnnotations(Log log, Document model, String moduleName, String typeName,
			AnnotatedConfig<? extends TLAnnotation> annotations) {
		Element module;
		try {
			module = getTLModuleOrFail(model, moduleName);
		} catch (MigrationException ex) {
			log.info("No module with name '" + moduleName + "' found.", Protocol.WARN);
			return false;
		}
		Element type;
		try {
			type = getTLTypeOrFail(log, module, typeName);
		} catch (MigrationException ex) {
			log.info("No type with name '" + TLModelUtil.qualifiedName(moduleName, typeName) + "' found.",
				Protocol.WARN);
			return false;
		}
		setModelPartAnnotations(log, type, annotations);
		return true;
	}

	/**
	 * Sets the given {@link AnnotatedConfig annotation} to a {@link TLTypePart}.
	 */
	public static boolean setTypePartAnnotations(Log log, Document model, String moduleName, String typeName,
			String partName,
			AnnotatedConfig<? extends TLAnnotation> annotations) {
		Element module;
		try {
			module = getTLModuleOrFail(model, moduleName);
		} catch (MigrationException ex) {
			log.info("No module with name '" + moduleName + "' found.", Protocol.WARN);
			return false;
		}
		Element type;
		try {
			type = getTLTypeOrFail(log, module, typeName);
		} catch (MigrationException ex) {
			log.info("No type with name '" + TLModelUtil.qualifiedName(moduleName, typeName) + "' found.",
				Protocol.WARN);
			return false;
		}
		Element part;
		try {
			part = getTLTypePartOrFail(log, type, partName);
		} catch (MigrationException ex) {
			log.info(
				"No type with name '" + TLModelUtil.qualifiedTypePartName(moduleName, typeName, partName) + "' found.",
				Protocol.WARN);
			return false;
		}
		setModelPartAnnotations(log, part, annotations);
		return true;
	}

	private static void setModelPartAnnotations(Log log, Element part,
			AnnotatedConfig<? extends TLAnnotation> newAnnotations) {
		Element moduleAnnotations = getModelPartAnnotations(log, part);
		updateModelPartAnnotations(log, part, moduleAnnotations, Util.toString(newAnnotations));
	}

	/**
	 * Updated the {@link TLAssociation} with the given name.
	 */
	public static void updateAssociation(Log log, Document tlModel, QualifiedTypeName typeName,
			QualifiedTypeName newName,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, typeName.getModuleName());
		Element type = getTLAssociation(log, module, typeName.getTypeName());
		if (type == null) {
			throw new MigrationException("No such association '" + typeName + "'.");
		}

		if (!annotations.getAnnotations().isEmpty()) {
			updateModelPartAnnotations(log, type, getModelPartAnnotations(log, type), Util.toString(annotations));
		}
		if (newName == null) {
			return;
		}
		String newModuleName = newName.getModuleName();
		String oldModuleName = typeName.getModuleName();
		if (!newModuleName.equals(oldModuleName)) {
			Element newModule = getTLModuleOrFail(tlModel, newModuleName);
			appendTypeToModule(log, newModule, type);
		}
		String newTypeName = newName.getTypeName();
		String oldTypeName = typeName.getTypeName();
		if (!newTypeName.equals(oldTypeName)) {
			type.setAttribute(ObjectTypeConfig.NAME, newTypeName);
		}
		updateTypeReferences(log, tlModel, oldModuleName, oldTypeName, newModuleName, newTypeName);
	}

	/**
	 * Updated the {@link TLPrimitive} with the given name.
	 */
	public static void updateDatatype(Log log, Document tlModel, QualifiedTypeName typeName, QualifiedTypeName newName,
			Kind kind,
			DBColumnType columnType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, typeName.getModuleName());
		Element type = getTLDatatype(log, module, typeName.getTypeName());
		if (type == null) {
			throw new MigrationException("No such datatype '" + typeName + "'.");
		}

		if (kind != null) {
			type.setAttribute(DatatypeConfig.KIND, kind.getExternalName());
		}
		if (storageMapping != null) {
			String serializedStorageMapping = storageMappingToString(storageMapping);
			Element storageMappingElement = DOMUtil.parse(serializedStorageMapping).getDocumentElement();
			Node newStorageMappingNode = type.getOwnerDocument().importNode(storageMappingElement, true);
			Element existingStorageMapping = findStorageMappingNode(log, type, typeName);
			if (existingStorageMapping != null) {
				type.replaceChild(newStorageMappingNode, existingStorageMapping);
			} else {
				type.appendChild(newStorageMappingNode);
			}
		}
		if (columnType != null) {
			copyColumnType(type, columnType);
		}

		if (!annotations.getAnnotations().isEmpty()) {
			updateModelPartAnnotations(log, type, getModelPartAnnotations(log, type), Util.toString(annotations));
		}
		if (newName == null) {
			return;
		}
		String newModuleName = newName.getModuleName();
		String oldModuleName = typeName.getModuleName();
		if (!newModuleName.equals(oldModuleName)) {
			Element newModule = getTLModuleOrFail(tlModel, newModuleName);
			appendTypeToModule(log, newModule, type);
		}
		String newTypeName = newName.getTypeName();
		String oldTypeName = typeName.getTypeName();
		if (!newTypeName.equals(oldTypeName)) {
			type.setAttribute(ObjectTypeConfig.NAME, newTypeName);
		}
		updateTypeReferences(log, tlModel, oldModuleName, oldTypeName, newModuleName, newTypeName);
	}

	/**
	 * There are two different configuration children that are typically written as tags:
	 * {@link DatatypeConfig#getAnnotations()} and {@link DatatypeConfig#STORAGE_MAPPING}.
	 * 
	 * If there is no {@link DatatypeConfig#STORAGE_MAPPING} and there is exactly one child element
	 * with an unexpected tag, it can be assumed that this is the tag name of a special storage
	 * mapping. This is possible, because the property {@link DatatypeConfig#getStorageMapping()} is
	 * annotated as {@link DefaultContainer} which allows to use the tag name.
	 * 
	 * If there are more that one unexpected child, better do not replace anything.
	 */
	private static Element findStorageMappingNode(Log log, Element datatype, QualifiedTypeName typeName) {
		Element potentialStorageMapping = null;
		for (Element child : DOMUtil.elements(datatype)) {
			switch (child.getTagName()) {
				case DatatypeConfig.ANNOTATIONS: {
					// not a storage mapping
					break;
				}
				case DatatypeConfig.STORAGE_MAPPING: {
					// Storage mapping found
					return child;
				}
				default: {
					if (potentialStorageMapping != null) {
						// Can not determine the correct child.
						return null;
					}
					potentialStorageMapping = child;

				}
			}
		}
		if (potentialStorageMapping == null) {
			// No storage mapping found.
			return null;
		}
		log.info("No storage mapping found in data type '" + typeName + "'. Interpret node '"
				+ DOMUtil.toString(potentialStorageMapping, false, false) + "' as storage mapping configuration node.",
			Protocol.WARN);
		return potentialStorageMapping;
	}

	/**
	 * Updated the {@link TLClass} with the given name.
	 */
	public static void updateClass(Log log, Document tlModel, QualifiedTypeName typeName, QualifiedTypeName newName,
			Boolean isAbstract, Boolean isFinal, AnnotatedConfig<TLTypeAnnotation> annotations) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, typeName.getModuleName());
		Element type = getTLClass(log, module, typeName.getTypeName());
		if (type == null) {
			throw new MigrationException("No such class '" + typeName + "'.");
		}
		
		if (isAbstract != null) {
			switch (type.getTagName()) {
				case ClassConfig.TAG_NAME:
					type.setAttribute(ClassConfig.ABSTRACT, Boolean.toString(isAbstract.booleanValue()));
					break;
				case InterfaceConfig.TAG_NAME:
					if (!isAbstract.booleanValue()) {
						type.getOwnerDocument().renameNode(type, null, ClassConfig.TAG_NAME);
					}
					break;
				default:
					log.error("Unexpected tag name for type " + typeName + ": " + DOMUtil.toString(type));
					break;
			}
		}

		if (isFinal != null) {
			switch (type.getTagName()) {
				case ClassConfig.TAG_NAME:
					type.setAttribute(ClassConfig.FINAL, Boolean.toString(isFinal.booleanValue()));
					break;
				case InterfaceConfig.TAG_NAME:
					if (isFinal.booleanValue()) {
						log.error("Unable to set interface " + typeName + " to final: " + DOMUtil.toString(type));
					}
					break;
				default:
					log.error("Unexpected tag name for type " + typeName + ": " + DOMUtil.toString(type));
					break;
			}
		}
		if (!annotations.getAnnotations().isEmpty()) {
			updateModelPartAnnotations(log, type, getModelPartAnnotations(log, type), Util.toString(annotations));
		}
		if (newName == null) {
			return;
		}
		String newModuleName = newName.getModuleName();
		String oldModuleName = typeName.getModuleName();
		if (!newModuleName.equals(oldModuleName)) {
			Element newModule = getTLModuleOrFail(tlModel, newModuleName);
			appendTypeToModule(log, newModule, type);
		}
		String newTypeName = newName.getTypeName();
		String oldTypeName = typeName.getTypeName();
		if (!newTypeName.equals(oldTypeName)) {
			type.setAttribute(ObjectTypeConfig.NAME, newTypeName);
		}
		updateTypeReferences(log, tlModel, oldModuleName, oldTypeName, newModuleName, newTypeName);
	}

	/**
	 * Updated the {@link TLEnumeration} with the given name.
	 */
	public static void updateEnum(Log log, Document tlModel, QualifiedTypeName typeName, QualifiedTypeName newName,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, typeName.getModuleName());
		Element type = getTLEnumeration(log, module, typeName.getName());
		if (type == null) {
			throw new MigrationException("No such enum '" + typeName + "'.");
		}

		if (!annotations.getAnnotations().isEmpty()) {
			updateModelPartAnnotations(log, type, getModelPartAnnotations(log, type), Util.toString(annotations));
		}
		if (newName == null) {
			return;
		}
		String newModuleName = newName.getModuleName();
		String oldModuleName = typeName.getModuleName();
		if (!newModuleName.equals(oldModuleName)) {
			Element newModule = getTLModuleOrFail(tlModel, newModuleName);
			appendTypeToModule(log, newModule, type);
		}
		String newTypeName = newName.getTypeName();
		String oldTypeName = typeName.getTypeName();
		if (!newTypeName.equals(oldTypeName)) {
			type.setAttribute(ObjectTypeConfig.NAME, newTypeName);
		}
		updateTypeReferences(log, tlModel, oldModuleName, oldTypeName, newModuleName, newTypeName);
	}

	/**
	 * Changes all existing references from <code>sourceType</code> to <code>targetType</code>.
	 */
	public static void updateTypeReferences(Log log, Document tlModel, QualifiedTypeName sourceType,
			QualifiedTypeName targetType) {
		updateTypeReferences(log, tlModel, sourceType.getModuleName(), sourceType.getTypeName(),
			targetType.getModuleName(), targetType.getTypeName());
	}

	private static void updateTypeReferences(Log log, Document tlModel, String oldModuleName, String oldTypeName,
			String newModuleName, String newTypeName) {
		doForAllModules(tlModel, (module, arg) -> {
			String currentModuleName = name(module);
			doForStructuredTypes(module, (type, arg2) -> {
				Element extendsConfigHolder;
				switch (type.getTagName()) {
					case InterfaceConfig.TAG_NAME:
					case ClassConfig.TAG_NAME: {
						// Adapt generalizations
						extendsConfigHolder = getGeneralizations(log, type);
						break;
					}
					case AssociationConfig.TAG_NAME: {
						extendsConfigHolder = getUniqueChild(type,
							child -> AssociationConfig.SUBSETS.equals(child.getTagName()),
							child -> log.error("Multiple subsetss: " + DOMUtil.toString(type)));
						break;
					}
					default:
						extendsConfigHolder = null;
				}
				if (extendsConfigHolder != null) {
					for (Element extendsConf : DOMUtil.elements(extendsConfigHolder)) {
						String extendsType = extendsConf.getAttribute(ExtendsConfig.TYPE);
						String transformed = transformTargetType(currentModuleName, extendsType, oldModuleName,
							oldTypeName, newModuleName, newTypeName);
						if (transformed != extendsType) {
							extendsConf.setAttribute(ExtendsConfig.TYPE, transformed);
						}
					}
				}
				doForStructuredTypeParts(log, type, (part, arg3) -> {
					// change targetType
					String targetType = part.getAttribute(PartConfig.TYPE_SPEC);
					String transformed = transformTargetType(currentModuleName, targetType, oldModuleName, oldTypeName,
						newModuleName, newTypeName);
					if (transformed != targetType) {
						setTargetType(part, transformed);
					}
				}, arg2);
			}, arg);
		}, null);
	}

	/**
	 * Creates the new target type after type renaming if necessary.
	 * 
	 * <p>
	 * Returns the new target type when type <code>oldModuleName:oldTypeName</code> is renamed to
	 * <code>newModuleName:newTypeName</code>
	 * </p>
	 * 
	 * @param ownerModule
	 *        Name of the module in which the type <code>oldTargetType</code> is used.
	 * @param oldTargetType
	 *        Name of the referenced Type which must be adapted eventually. Either a qualified name,
	 *        or a local type in the module <code>ownerModule</code>.
	 * @return Either the a reference to the <code>oldTargetType</code> if does not represent the
	 *         renamed type of a reference to the new name (may be a local reference).
	 */
	private static String transformTargetType(String ownerModule, String oldTargetType, String oldModuleName,
			String oldTypeName, String newModuleName, String newTypeName) {
		int typeSeparator = oldTargetType.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		String targetModule;
		String targetType;
		if (typeSeparator < 0) {
			targetModule = ownerModule;
			targetType = oldTargetType;
		} else {
			targetModule = oldTargetType.substring(0, typeSeparator);
			targetType = oldTargetType.substring(typeSeparator + 1);
		}
		if (targetType.equals(oldTypeName) && targetModule.equals(oldModuleName)) {
			if (newModuleName.equals(ownerModule)) {
				return newTypeName;
			} else {
				return TLModelUtil.qualifiedName(newModuleName, newTypeName);
			}
		}
		return oldTargetType;
	}

	private static <A> void doForAllModules(Document tlModel, BiConsumer<Element, A> handle, A arg) {
		NodeList modules = tlModel.getElementsByTagName(ModelConfig.MODULE);
		for (int i = 0; i < modules.getLength(); i++) {
			handle.accept((Element) modules.item(i), arg);
		}
	}

	private static <A> void doForStructuredTypes(Element module, BiConsumer<Element, A> handle, A arg) {
		for (Element structuredType : DOMUtil.elements(module)) {
			switch (structuredType.getTagName()) {
				case InterfaceConfig.TAG_NAME:
				case ClassConfig.TAG_NAME:
				case AssociationConfig.TAG_NAME:
					handle.accept(structuredType, arg);
					break;
				case ModuleConfig.TYPES:
					/* The types in the given module are surrounded by a "types" tag. Descend into
					 * the "types" tag and ignore following tags. */
					Element types = structuredType;
					doForStructuredTypes(types, handle, arg);
					return;
			}
		}
	}

	private static <A> void doForStructuredTypeParts(Log log, Element type, BiConsumer<Element, A> handle, A arg) {
		Element attributes = getAttributes(log, type);
		if (attributes == null) {
			return;
		}
		for (Element structuredTypePart : DOMUtil.elements(attributes)) {
			handle.accept(structuredTypePart, arg);
		}
	}

	/**
	 * Updates the {@link TLStructuredTypePart#getType()} of the given part.
	 */
	public static void updateTargetType(Log log, Document tlModel, QualifiedPartName partName, QualifiedTypeName target)
			throws MigrationException {
		Element module = getTLModuleOrFail(tlModel, partName.getModuleName());
		Element type = getTLTypeOrFail(log, module, partName.getTypeName());
		Element part = getTLTypePartOrFail(log, type, partName.getPartName());

		internalUpdatePart(log, tlModel, part, partName, null, target, null, null, null, null, null, null);
		if (REFERENCE_CONFIG_TAG_NAME.equals(part.getTagName())) {
			// Reference: Update inverse!
			adaptInverseReference(log, tlModel, module, type, part, null, target);
		}
	}

	/**
	 * Updates the given reference.
	 */
	public static void updateProperty(Log log, Document tlModel, QualifiedPartName propertyName,
			QualifiedPartName newName, QualifiedTypeName newType,
			Boolean mandatory, Boolean multiple, Boolean bag, Boolean ordered, Boolean isAbstract,
			AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws MigrationException {

		Element module = getTLModuleOrFail(tlModel, propertyName.getModuleName());
		Element type = getTLTypeOrFail(log, module, propertyName.getTypeName());
		Element part = getTLTypePartOrFail(log, type, propertyName.getPartName());

		internalUpdatePart(log, tlModel, part, propertyName, newName, newType, mandatory, multiple, bag, ordered,
			isAbstract, annotations);
	}

	/**
	 * Updates the given reference.
	 */
	public static void updateAssociationEnd(Log log, Document tlModel, QualifiedPartName endName,
			QualifiedPartName newName, QualifiedTypeName newType,
			Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered,
			Boolean isAbstract, Boolean canNavigate, HistoryType historyType,
			AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws MigrationException {

		Element module = getTLModuleOrFail(tlModel, endName.getModuleName());
		Element type = getTLTypeOrFail(log, module, endName.getTypeName());
		Element part = getTLTypePartOrFail(log, type, endName.getPartName());

		internalUpdateEndAspect(log, tlModel, endName, newName, newType, mandatory, composite, aggregate,
			multiple, bag, ordered, isAbstract, canNavigate, historyType, annotations, part);
	}

	/**
	 * Updates the given reference.
	 */
	public static void updateReference(Log log, Document tlModel, QualifiedPartName referenceName,
			QualifiedPartName newName, QualifiedTypeName newType,
			Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered,
			Boolean isAbstract, Boolean canNavigate, HistoryType historyType,
			AnnotatedConfig<TLAttributeAnnotation> annotations,
			QualifiedPartName newEnd)
			throws MigrationException {

		Element module = getTLModuleOrFail(tlModel, referenceName.getModuleName());
		Element type = getTLTypeOrFail(log, module, referenceName.getTypeName());
		Element part = getTLTypePartOrFail(log, type, referenceName.getPartName());

		if (newName != null || newType != null) {
			adaptInverseReference(log, tlModel, module, type, part, newName, newType);
		}

		internalUpdateEndAspect(log, tlModel, referenceName, newName, newType, mandatory, composite, aggregate,
			multiple, bag, ordered, isAbstract, canNavigate, historyType, annotations, part);
	}

	/**
	 * Updates the given inverse reference.
	 */
	public static void updateInverseReference(Log log, Document tlModel, QualifiedPartName referenceName,
			String newReferenceName,
			Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered,
			Boolean isAbstract, Boolean canNavigate, HistoryType historyType,
			AnnotatedConfig<TLAttributeAnnotation> annotations,
			QualifiedPartName newEnd) throws MigrationException {

		Element module = getTLModuleOrFail(tlModel, referenceName.getModuleName());
		Element type = getTLTypeOrFail(log, module, referenceName.getTypeName());
		Element part = getTLTypePartOrFail(log, type, referenceName.getPartName());

		internalUpdateEndAspect(log, tlModel, referenceName, null, null, mandatory, composite, aggregate,
			multiple, bag, ordered, isAbstract, canNavigate, historyType, annotations, part);

		if (newReferenceName != null) {
			part.setAttribute(ReferenceConfig.NAME, newReferenceName);
		}

	}

	private static boolean isEnumType(Element type) {
		return EnumConfig.TAG_NAME.equals(type.getTagName());
	}

	private static void adaptInverseReference(Log log, Document tlModel, Element module, Element type,
			Element reference, QualifiedPartName newName, QualifiedTypeName newType) throws MigrationException {
		String moduleName = name(module);
		String typeName = name(type);
		String referenceName = name(reference);

		String targetTypeSpec = reference.getAttribute(ReferenceConfig.TYPE_SPEC);
		int typeSeparator = targetTypeSpec.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		String targetModuleName;
		String targetTypeName;
		if (typeSeparator < 0) {
			// local type name, defined in same module
			targetModuleName = moduleName;
			targetTypeName = targetTypeSpec;
		} else {
			targetModuleName = targetTypeSpec.substring(0, typeSeparator);
			targetTypeName = targetTypeSpec.substring(typeSeparator + 1);
		}
		Element targetType;
		boolean targetInSameModule = targetModuleName.equals(moduleName);
		if (targetInSameModule) {
			if (targetTypeName.equals(typeName)) {
				targetType = type;
			} else {
				targetType = getTLTypeOrFail(log, module, targetTypeName);
			}
		} else {
			targetType = getTLTypeOrFail(log, getTLModuleOrFail(tlModel, targetModuleName), targetTypeName);
		}
		if (isEnumType(targetType)) {
			return;
		}
		Element attributes = getAttributes(log, type);
		if (attributes == null) {
			return;
		}
		Element inverseReference = getUniqueChild(attributes,
			child -> {
				if (!REFERENCE_CONFIG_TAG_NAME.equals(child.getTagName())) {
					return false;
				}
				if (!referenceName.equals(child.getAttribute(ReferenceConfig.INVERSE_REFERENCE))) {
					return false;
				}
				// Check correct type.
				String backRefTypeSpec = child.getAttribute(ReferenceConfig.TYPE_SPEC);
				int backRefTypeSeparator = backRefTypeSpec.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
				if (backRefTypeSeparator < 0) {
					// local type name
					if (targetInSameModule) {
						return backRefTypeSpec.equals(typeName);
					} else {
						return false;
					}
				}
				return typeName.equals(backRefTypeSpec.substring(backRefTypeSeparator + 1)) &&
						moduleName.equals(backRefTypeSpec.substring(0, backRefTypeSeparator));
			},
			child -> log.error("Multiple inverse references to part '"
					+ TLModelUtil.qualifiedTypePartName(moduleName, typeName, referenceName) + "'."));
		if (inverseReference == null) {
			// no inverse reference
			return;
		}
		if (newName != null) {
			inverseReference.setAttribute(ReferenceConfig.INVERSE_REFERENCE, newName.getPartName());
			setTargetType(inverseReference,
				TLModelUtil.qualifiedName(newName.getModuleName(), newName.getTypeName()));
		}
		if (newType != null) {
			String inverseRefPartName = name(inverseReference);
			QualifiedPartName origInverseRefName = TypedConfiguration.newConfigItem(QualifiedPartName.class);
			origInverseRefName
				.setName(TLModelUtil.qualifiedTypePartName(targetModuleName, targetTypeName, inverseRefPartName));
			QualifiedPartName newInverseRefName = createPartName(newType, inverseRefPartName);
			moveStructuredTypePart(log, tlModel, inverseReference, origInverseRefName, newInverseRefName);
		}
	}

	private static void internalUpdateEndAspect(Log log, Document tlModel, QualifiedPartName origName,
			QualifiedPartName newName, QualifiedTypeName newType, Boolean mandatory, Boolean composite,
			Boolean aggregate, Boolean multiple, Boolean bag, Boolean ordered, Boolean isAbstract, Boolean canNavigate,
			HistoryType historyType, AnnotatedConfig<TLAttributeAnnotation> annotations, Element part)
			throws MigrationException {
		internalUpdatePart(log, tlModel, part, origName, newName, newType, mandatory, multiple, bag,
			ordered, isAbstract, annotations);
		if (composite != null) {
			part.setAttribute(EndAspect.COMPOSITE_PROPERTY, Boolean.toString(composite.booleanValue()));
		}
		if (aggregate != null) {
			part.setAttribute(EndAspect.AGGREGATE_PROPERTY, Boolean.toString(aggregate.booleanValue()));
		}
		if (canNavigate != null) {
			part.setAttribute(EndAspect.NAVIGATE_PROPERTY, Boolean.toString(canNavigate.booleanValue()));
		}
		if (historyType != null) {
			part.setAttribute(EndAspect.HISTORY_TYPE_PROPERTY, historyType.getExternalName());
		}
	}

	private static void internalUpdatePart(Log log, Document tlModel, Element part, QualifiedPartName origName,
			QualifiedPartName newName, QualifiedTypeName newType, Boolean mandatory, Boolean multiple, Boolean bag,
			Boolean ordered, Boolean isAbstract, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws MigrationException {
		if (newName != null) {
			moveStructuredTypePart(log, tlModel, part, origName, newName);
		}
		if (newType != null) {
			String newTypeSpec;
			if ((newName != null && newName.getModuleName().equals(newType.getModuleName()))
					|| origName.getModuleName().equals(newType.getModuleName())) {
				newTypeSpec = newType.getTypeName();
			} else {
				newTypeSpec = newType.getName();
			}
			setTargetType(part, newTypeSpec);
		}
		if (mandatory != null) {
			part.setAttribute(PartConfig.MANDATORY, Boolean.toString(mandatory.booleanValue()));
		}
		if (multiple != null) {
			part.setAttribute(PartConfig.MULTIPLE_PROPERTY, Boolean.toString(multiple.booleanValue()));
		}
		if (bag != null) {
			part.setAttribute(PartConfig.BAG_PROPERTY, Boolean.toString(bag.booleanValue()));
		}
		if (ordered != null) {
			part.setAttribute(PartConfig.ORDERED_PROPERTY, Boolean.toString(ordered.booleanValue()));
		}
		if (isAbstract != null) {
			part.setAttribute(PartConfig.ABSTRACT_PROPERTY, Boolean.toString(isAbstract.booleanValue()));
		}
		if (annotations != null && !annotations.getAnnotations().isEmpty()) {
			updateModelPartAnnotations(log, part, getModelPartAnnotations(log, part), Util.toString(annotations));
		}
	}

	private static void moveStructuredTypePart(Log log, Document tlModel, Element part, QualifiedPartName origName,
			QualifiedPartName newName) throws MigrationException {
		if (!newName.getModuleName().equals(origName.getModuleName())) {
			// Part was moved to different module.
			qualifyTypes(part, origName.getModuleName());
		}
		Element newModule = getTLModuleOrFail(tlModel, newName.getModuleName());
		Element newOwner = getTLTypeOrFail(log, newModule, newName.getTypeName());
		Element attributes = getAttributes(log, newOwner);
		if (attributes == null) {
			attributes = tlModel.createElement(AttributedTypeConfig.ATTRIBUTES);
			newOwner.appendChild(attributes);
		}
		part.setAttribute(PartConfig.NAME, newName.getPartName());
		attributes.appendChild(part);
	}

	/**
	 * Qualifies all type references in the given part with the given module name.
	 */
	private static void qualifyTypes(Element typePart, String module) {
		String type = typePart.getAttribute(PartConfig.TYPE_SPEC);
		if (!StringServices.isEmpty(type) && isLocalTypeName(type)) {
			// Reference to local type.
			setTargetType(typePart, TLModelUtil.qualifiedName(module, type));
		}
	}

	private static boolean isLocalTypeName(String type) {
		return type.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) < 0;
	}

	private static QualifiedPartName createPartName(QualifiedTypeName type, String part) {
		QualifiedPartName newPart = TypedConfiguration.newConfigItem(QualifiedPartName.class);
		newPart.setName(TLModelUtil.qualifiedTypePartName(type.getModuleName(), type.getTypeName(), part));
		return newPart;
	}

}
