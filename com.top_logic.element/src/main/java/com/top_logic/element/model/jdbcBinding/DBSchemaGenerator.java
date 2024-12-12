/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.config.misc.TypedConfigUtil.*;
import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.dob.schema.config.AttributeConfig.*;
import static com.top_logic.dob.schema.config.MetaObjectConfig.*;
import static com.top_logic.dob.xml.DOXMLConstants.*;
import static com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil.*;
import static com.top_logic.model.impl.generated.TlModelFactory.*;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.exception.NotYetImplementedException;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.schema.config.AssociationConfig;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.knowledge.KIReferenceConfig;
import com.top_logic.knowledge.service.db2.AssociationReference;
import com.top_logic.knowledge.service.db2.DestinationReference;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.annotation.TableName;

/**
 * Generates a {@link MetaObject} model for a {@link TLModule}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class DBSchemaGenerator {

	private static final String ASSOCIATION_PREFIX = "has";

	private final TLModule _module;

	private final boolean _overrideExistingSchema;

	private MetaObjectsConfig _moModuleConfig = newConfigItem(MetaObjectsConfig.class);

	private Map<TLClass, MetaObjectConfig> _classMapping;

	private Map<TLReference, AssociationConfig> _referenceToAssociationMapping = new HashMap<>();

	/** Creates a {@link DBSchemaGenerator}. */
	public DBSchemaGenerator(TLModule module, boolean overrideExistingSchema) {
		_module = requireNonNull(module);
		_overrideExistingSchema = overrideExistingSchema;
	}

	/** The generated schema. */
	public MetaObjectsConfig getMetaObjectConfig() {
		return _moModuleConfig;
	}

	/** The mapping which {@link TLClass} is stored in which table. */
	public Map<TLClass, MetaObjectConfig> getClassMapping() {
		if (_classMapping == null) {
			throw new IllegalStateException("The database schema has not been generated, yet.");
		}
		return _classMapping;
	}

	/** The mapping of TLReferences to the association in which they are stored, if any. */
	public Map<TLReference, AssociationConfig> getReferenceToAssociationMapping() {
		return _referenceToAssociationMapping;
	}

	/** Generates a {@link MetaObject} model for the {@link TLModule} given to the constructor. */
	public void generate() {
		logInfo("Begin module: " + _module.getName());
		_classMapping = generateClasses(_module.getClasses());
		registerTypes(_classMapping.values());
		registerTypes(_referenceToAssociationMapping.values());
		logInfo("End module.");
	}

	/** This also generated associations for references that cannot be stored inline. */
	private Map<TLClass, MetaObjectConfig> generateClasses(Collection<? extends TLClass> classes) {
		return classes.stream()
			.map(tlClass -> new Pair<>(tlClass, generateClass(tlClass)))
			.filter(pair -> pair.getSecond() != null)
			.collect(toMap(Pair::getFirst, Pair::getSecond));
	}

	private void registerTypes(Collection<? extends MetaObjectConfig> moTypes) {
		moTypes.stream()
			.sorted(Comparator.comparing(MetaObjectConfig::getObjectName))
			.forEachOrdered(this::registerType);
	}

	private void registerType(MetaObjectConfig moClass) {
		_moModuleConfig.getTypes().put(moClass.getObjectName(), moClass);
	}

	private MetaObjectConfig generateClass(TLClass tlClass) {
		logInfo("Begin class: " + tlClass.getName());
		TableName existingTable = tlClass.getAnnotation(TableName.class);
		if (existingTable != null) {
			if (!_overrideExistingSchema) {
				logInfo("Skipping TLClass as it already has a table.");
				return null;
			}
			logInfo("TLClass has already a table. Overriding it.");
		}
		MetaObjectConfig moClass = newConfigItem(MetaObjectConfig.class);
		setProperty(moClass, MetaObjectConfig.OBJECT_NAME_ATTRIBUTE, generateMOClassName(tlClass));
		if (tlClass.isAbstract()) {
			setProperty(moClass, ABSTRACT_PROPERTY, true);
		}
		var attributesBySuperClass = getAttributesBySuperClass(tlClass);
		TLClass heaviestSuperClass = getHeaviestSuperClass(attributesBySuperClass);
		Object superMOClass = (heaviestSuperClass == null) ? PersistentObject.OBJECT_TYPE : getMOClass(heaviestSuperClass);
		setProperty(moClass, SUPERCLASS_PROPERTY, superMOClass);
		Set<String> inheritedAttributeNames = new HashSet<>(localNames(attributesBySuperClass.remove(heaviestSuperClass)));
		List<TLClassPart> nonInheritedAttributes = flatten(attributesBySuperClass.values());
		nonInheritedAttributes.removeIf(attribute -> inheritedAttributeNames.contains(attribute.getName()));
		nonInheritedAttributes.addAll(tlClass.getLocalClassParts());
		moClass.getAttributes().addAll(nonInheritedAttributes
			.stream()
			.sorted(Comparator.comparing(TLNamedPart::getName))
			.map(TLClassPart.class::cast)
			.map(this::generateAttribute)
			.filter(Objects::nonNull)
			.collect(toList()));
		logInfo("End class.");
		return moClass;
	}

	private LinkedHashMap<TLClass, List<? extends TLClassPart>> getAttributesBySuperClass(TLClass tlClass) {
		LinkedHashMap<TLClass, List<? extends TLClassPart>> result = new LinkedHashMap<>();
		for (TLClass superClass : tlClass.getGeneralizations()) {
			result.put(superClass, superClass.getAllClassParts());
		}
		return result;
	}

	private TLClass getHeaviestSuperClass(Map<TLClass, List<? extends TLClassPart>> attributesBySuperClass) {
		int maxAttributes = 0;
		TLClass heaviestSuperClass = null;
		for (var entry : attributesBySuperClass.entrySet()) {
			int attributeCount = entry.getValue().size();
			if (attributeCount > maxAttributes) {
				maxAttributes = attributeCount;
				heaviestSuperClass = entry.getKey();
			}
		}
		return heaviestSuperClass;
	}

	private boolean isIrrelevantOverride(TLStructuredTypePart tlAttribute) {
		if (!tlAttribute.isOverride()) {
			return false;
		}
		return tlAttribute.getType().equals(tlAttribute.getDefinition().getType());
	}

	private AttributeConfig generateAttribute(TLStructuredTypePart tlAttribute) {
		logInfo("Begin attribute: " + tlAttribute.getName());
		if (tlAttribute.isDerived()) {
			logInfo("Skipping attribute as it is derived.");
			return null;
		}
		if (isIrrelevantOverride(tlAttribute)) {
			logInfo("Skipping attribute as it is an irrelevant override.");
			return null;
		}
		if ((tlAttribute.getOwner().getModelKind() == ModelKind.ASSOCIATION)
			&& tlAttribute.getModelKind() == ModelKind.REFERENCE
			&& tlAttribute.isMultiple()) {
			throw new RuntimeException("References on associations must not be multiple.");
		}
		AttributeConfig moAttribute = generateAttributeInternal(tlAttribute);
		if (moAttribute == null) {
			return null;
		}
		setProperty(moAttribute, ATTRIBUTE_NAME_KEY, tlAttribute.getName());
		if (tlAttribute.isOverride()) {
			setProperty(moAttribute, OVERRIDE, tlAttribute.isOverride());
		}
		if (isMandatory(tlAttribute)) {
			setProperty(moAttribute, MANDATORY_ATTRIBUTE, isMandatory(tlAttribute));
		}
		if (DisplayAnnotations.isHidden(tlAttribute)) {
			setProperty(moAttribute, HIDDEN_ATTRIBUTE, true);
		}
		logInfo("End attribute.");
		return moAttribute;
	}

	private boolean isMandatory(TLStructuredTypePart tlAttribute) {
		if (tlAttribute.isMandatory()) {
			return true;
		}
		// All references in an association are mandatory
		return tlAttribute.getOwner().getModelKind() == ModelKind.ASSOCIATION
			&& tlAttribute.getModelKind() == ModelKind.REFERENCE;
	}

	private AttributeConfig generateAttributeInternal(TLStructuredTypePart tlAttribute) {
		switch (tlAttribute.getType().getModelKind()) {
			case DATATYPE: {
				return generatePrimitiveAttribute(tlAttribute);
			}
			case ENUMERATION: {
				return generateEnumerationAttribute(tlAttribute);
			}
			case CLASS: {
				if (tlAttribute.getModelKind() == ModelKind.END) {
					throw new UnreachableAssertion("TLAssociationEnds are being filtered out.");
				}
				return generateReferenceAttribute(tlAttribute);
			}
			case ASSOCIATION: {
				throw new UnsupportedOperationException("A reference to an association is highly unlikely"
					+ " and therefore not supported. " + tlAttribute);
			}
			case END: {
				throw new UnreachableAssertion("TLAssociationEnds are being filtered out.");
			}
			case CLASSIFIER: {
				throw new UnreachableAssertion("The type of an attribute is never of model-kind 'classifier'. " + tlAttribute);
			}
			case MODEL: {
				throw new UnreachableAssertion("The type of an attribute is never of model-kind 'model'. " + tlAttribute);
			}
			case MODULE: {
				throw new UnreachableAssertion("The type of an attribute is never of model-kind 'module'. " + tlAttribute);
			}
			case PROPERTY: {
				throw new UnreachableAssertion("The type of an attribute is never of model-kind 'property'. " + tlAttribute);
			}
			case REFERENCE: {
				throw new UnreachableAssertion("The type of an attribute is never of model-kind 'reference'. " + tlAttribute);
			}
			case OBJECT: {
				throw new UnreachableAssertion("The type of an attribute is never of model-kind 'object'. " + tlAttribute);
			}
		}
		throw new UnreachableAssertion("The case statement above has to cover everything.");
	}

	private PrimitiveAttributeConfig generatePrimitiveAttribute(TLStructuredTypePart tlAttribute) {
		if (tlAttribute.isMultiple()) {
			logWarn("Skipping attribute as it is a primitive attribute with multiple values.");
			return null;
		}
		PrimitiveAttributeConfig moPrimitive = newConfigItem(PrimitiveAttributeConfig.class);
		TLType tlType = tlAttribute.getType();
		if (!(tlType instanceof TLPrimitive)) {
			throw new UnreachableAssertion(
				"Attributes whose type's kind is 'data type' always have a type of type TLPrimitive. " + tlAttribute);
		}
		TLPrimitive tlPrimitive = (TLPrimitive) tlType;
		DBType dbType = tlPrimitive.getDBType();
		setProperty(moPrimitive, ATT_TYPE_ATTRIBUTE, toMOPrimitive(dbType));
		moPrimitive.setDBType(dbType);
		Integer dbSize = tlPrimitive.getDBSize();
		if (dbSize != null) {
			moPrimitive.setDBSize(dbSize);
		}
		Integer dbPrecision = tlPrimitive.getDBPrecision();
		if (dbPrecision != null) {
			moPrimitive.setDBPrecision(dbPrecision);
		}
		Boolean binary = tlPrimitive.isBinary();
		if (binary != null) {
			moPrimitive.setBinary(binary);
		}
		return moPrimitive;
	}

	private MOPrimitive toMOPrimitive(DBType dbType) {
		return SchemaSetup.toPrimitive(dbType);
	}

	private ReferenceAttributeConfig generateEnumerationAttribute(TLStructuredTypePart tlAttribute) {
		if (tlAttribute instanceof TLAssociationEnd) {
			return null;
		}
		if (!(tlAttribute instanceof TLReference)) {
			throw new UnreachableAssertion(
				"Attributes whose type's kind is 'enumeration' are always of type TLReference. " + tlAttribute);
		}
		TLReference tlReference = (TLReference) tlAttribute;
		TLType tlType = tlReference.getType();
		if (!(tlType instanceof TLEnumeration)) {
			throw new UnreachableAssertion(
				"Attributes whose type's kind is 'enumeration' are always of type TLEnumeration. " + tlAttribute);
		}
		TLEnumeration tlEnumeration = (TLEnumeration) tlType;
		if (tlReference.isMultiple()) {
			generateAssociationForReference(tlReference, tlEnumeration);
			logInfo("Realizing enum reference with multiple values as association table.");
			return null;
		}
		return generateInlineEnumReference(tlReference);
	}

	private ReferenceAttributeConfig generateInlineEnumReference(TLReference tlReference) {
		ReferenceAttributeConfig moReference = newConfigItem(getMOReferenceConfigType(tlReference));
		setProperty(moReference, MONOMORPHIC_ATTRIBUTE, true);
		if (tlReference.getHistoryType() != HistoryType.CURRENT) {
			setProperty(moReference, HISTORIC_ATTRIBUTE, tlReference.getHistoryType());
		}
		setProperty(moReference, TARGET_TYPE_ATTRIBUTE, KO_NAME_TL_CLASSIFIER);
		setProperty(moReference, BY_VALUE_ATTRIBUTE, Decision.TRUE);
		return moReference;
	}

	private Class<? extends ReferenceAttributeConfig> getMOReferenceConfigType(TLReference tlReference) {
		switch (tlReference.getOwner().getModelKind()) {
			case ASSOCIATION: {
				return AssociationReference.class;
			}
			case CLASS: {
				return KIReferenceConfig.class;
			}
			case CLASSIFIER:
			case DATATYPE:
			case END:
			case ENUMERATION:
			case MODEL:
			case MODULE:
			case PROPERTY:
			case REFERENCE:
			case OBJECT: {
				throw new UnreachableAssertion("The owner of a TLReference is always of kind CLASS or ASSOCIATION:" + tlReference);
			}
		}
		throw new UnreachableAssertion("The case statement above has to cover everything.");
	}

	private KIReferenceConfig generateReferenceAttribute(TLStructuredTypePart tlAttribute) {
		if (!(tlAttribute instanceof TLReference)) {
			throw new UnreachableAssertion(
				"Attributes whose type's kind is 'class' are always of type TLReference. " + tlAttribute);
		}
		TLReference tlReference = (TLReference) tlAttribute;
		TLType targetType = tlReference.getType();
		if (!(targetType instanceof TLClass)) {
			throw new UnreachableAssertion(
				"Attributes whose type's kind is 'class' always are of type TLClass. " + tlAttribute);
		}
		TLClass targetClass = (TLClass) targetType;
		if (tlReference.isMultiple()) {
			generateAssociationForReference(tlReference, targetClass);
			logInfo("Realizing enum reference with multiple values as association table.");
			return null;
		}
		return generateInlineReference(tlReference, targetClass);
	}

	private AssociationConfig generateAssociationForReference(TLReference tlReference, TLStructuredType targetType) {
		AssociationConfig moAssociation = newConfigItem(AssociationConfig.class);
		setProperty(moAssociation, MetaObjectName.OBJECT_NAME_ATTRIBUTE, toAssociationName(tlReference));
		setProperty(moAssociation, SUPERCLASS_PROPERTY, WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
		AssociationReference sourceReference =
			generateAssociationReferenceOverride(tlReference.getOwner(), SourceReference.REFERENCE_SOURCE_NAME);
		moAssociation.getAttributes().add(sourceReference);
		AssociationReference destinationReference =
			generateAssociationReferenceOverride(targetType, DestinationReference.REFERENCE_DEST_NAME);
		moAssociation.getAttributes().add(destinationReference);
		_referenceToAssociationMapping.put(tlReference, moAssociation);
		return moAssociation;
	}

	private String toAssociationName(TLReference tlReference) {
		/* Attribute names are often not unique. Multiple types can have a reference to the same
		 * type of objects. They are often named after the target type. That leads to name clashes.
		 * Therefore, the attribute names are prefixed with their owner names. */
		String className = toMOTypeName(tlReference.getOwner().getName());
		String attributeName = toMOTypeName(tlReference.getName());
		return ASSOCIATION_PREFIX + className + attributeName;
	}

	private AssociationReference generateAssociationReferenceOverride(TLStructuredType targetType, String name) {
		AssociationReference reference = newConfigItem(getAssociationReferenceConfigInterface(name));
		setProperty(reference, ATTRIBUTE_NAME_KEY, name);
		setProperty(reference, TARGET_TYPE_ATTRIBUTE, getMOClass(targetType));
		setProperty(reference, OVERRIDE, true);
		return reference;
	}

	private Class<? extends AssociationReference> getAssociationReferenceConfigInterface(String name) {
		if (name.equals(SourceReference.REFERENCE_SOURCE_NAME)) {
			return SourceReference.class;
		}
		if (name.equals(DestinationReference.REFERENCE_DEST_NAME)) {
			return DestinationReference.class;
		}
		return AssociationReference.class;
	}

	private KIReferenceConfig generateInlineReference(TLReference tlReference, TLClass targetType) {
		KIReferenceConfig moReference = newConfigItem(KIReferenceConfig.class);
		setProperty(moReference, TARGET_TYPE_ATTRIBUTE, getMOClass(targetType));
		setProperty(moReference, MONOMORPHIC_ATTRIBUTE, targetType.getSpecializations().isEmpty());
		if (tlReference.getHistoryType() != HistoryType.CURRENT) {
			setProperty(moReference, HISTORIC_ATTRIBUTE, tlReference.getHistoryType());
		}
		if (tlReference.isComposite()) {
			setProperty(moReference, CONTAINER_ATTRIBUTE, true);
		}
		return moReference;
	}

	private String getMOClass(TLStructuredType type) {
		if (type instanceof TLEnumeration) {
			return KO_NAME_TL_CLASSIFIER;
		}
		if (type instanceof TLClass) {
			return getMOClass((TLClass) type);
		}
		throw new IllegalArgumentException("Unexpected type kind: " + type);
	}

	private String getMOClass(TLClass tlClass) {
		if (tlClass.getModule().equals(_module)) {
			TableName tableAnnotation = tlClass.getAnnotation(TableName.class);
			if ((tableAnnotation != null) && !_overrideExistingSchema) {
				return tableAnnotation.getName();
			}
			return generateMOClassName(tlClass);
		}
		return TLAnnotations.getTable(tlClass);
	}

	private String generateMOClassName(TLClass tlClass) {
		String localName = toMOTypeName(tlClass.getName());
		if (isConflictingMOTypeName(tlClass, localName)) {
			String moduleName = tlClass.getModule().getName();
			String fullName = toMOTypeName(moduleName) + localName;
			if (isConflictingMOTypeName(tlClass, fullName)) {
				throw new NotYetImplementedException("Failed to generate unique name for type: " + qualifiedName(tlClass));
			}
			return fullName;
		}
		return localName;
	}

	private String toMOTypeName(String name) {
		return Arrays.stream(name.split("[^a-zA-Z0-9]+"))
			.map(StringServices::capitalizeString)
			.collect(joining());
	}

	private boolean isConflictingMOTypeName(TLClass tlClass, String localName) {
		if (tlClass.tKnowledgeBase().getMORepository().getTypeOrNull(localName) == null) {
			return false;
		}
		TableName tableAnnotation = tlClass.getAnnotation(TableName.class);
		if (tableAnnotation == null) {
			return true;
		}
		if (!localName.equals(tableAnnotation.getName())) {
			return true;
		}
		return !_overrideExistingSchema;
	}

	private static void logInfo(String message) {
		Logger.info(message, DBSchemaGenerator.class);
	}

	private static void logWarn(String message) {
		Logger.warn(message, DBSchemaGenerator.class);
	}

}
