/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.binding.xml;

import com.top_logic.model.DerivedTLTypePart;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLTypePart;

/**
 * Constants for the native XML schema for {@link TLModel} serializations.
 * 
 * @see ModelWriter
 * @deprecated TODO #2829: Delete TL 6 deprecation: Old-style syntax not based on typed
 *             configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface TLModelBindingConstants {

	/**
	 * The main model namespace.
	 */
	String NS_MODEL = "http://www.top-logic.com/ns/model/6.0";

	/**
	 * The namespace for model evolution.
	 */
	String NS_EVOLUTION = "http://www.top-logic.com/ns/model/evolution/6.0";

	/**
	 * The namespace for model constraints.
	 */
	String NS_CONSTRAINT = "http://www.top-logic.com/ns/model/constraint/6.0";

	/**
	 * The namespace for Java binding annotations.
	 */
	String NS_BINDING_JAVA = "http://www.top-logic.com/ns/model/binding/java/6.0";

	/**
	 * The namespace for relational database binding.
	 */
	String NS_BINDING_DB = "http://www.top-logic.com/ns/model/binding/persistency/6.0";

	/**
	 * The namespace for external datasource binding.
	 */
	String NS_BINDING_DS = "http://www.top-logic.com/ns/model/binding/datasource/6.0";

	/**
	 * The namespace for caching annotations.
	 */
	String NS_BINDING_CACHE = "http://www.top-logic.com/ns/model/binding/cache/6.0";

	/**
	 * The root element for a model serialization.
	 */
	String MODEL_ELEMENT = "model";
	
	/**
	 * @see TLNamedPart#getName()
	 */
	String NAMED_ELEMENT_NAME_ATTR = "name";
	
	/**
	 * @see TLPrimitive#getStorageMapping()
	 */
	String STORAGE_MAPPING_ATTR = "storage-mapping";

	/**
	 * @see TLModule
	 */
	String MODULE_ELEMENT = "module";
	
	/**
	 * @see TLEnumeration
	 */
	String ENUMERATION_ELEMENT = "enum";
	
	/**
	 * @see TLClassifier
	 */
	String ENUMERATION_CLASSIFIER_ELEMENT = "classifier";
	
	/**
	 * @see TLClassifier#isDefault()
	 */
	String ENUMERATION_CLASSIFIER_DEFAULT_ATTR = "default";

	/**
	 * @see TLPrimitive
	 */
	String DATATYPE_ELEMENT = "datatype";

	/**
	 * @see TLClass
	 */
	String CLASS_ELEMENT = "class";
	
	/**
	 * @see TLClass#isAbstract()
	 */
	String CLASS_ABSTRACT_ATTR = "abstract";
	
	/**
	 * @see TLClass#isFinal()
	 */
	String CLASS_FINAL_ATTR = "final";
	
	/**
	 * @see TLClass#getGeneralizations()
	 */
	String CLASS_GENERALIZATIONS_ELEMENT = "generalizations";

	/**
	 * Fully qualified name of a referenced type in another {@link TLModule}.
	 * 
	 * <p>
	 * If the module of the referenced and the referencing type are the same,
	 * only the local name may be used.
	 * </p>
	 */
	String TYPE_REFERENCE_TYPE_NAME_ATTR = "name";
	
	/**
	 * @see TLTypePart#getType()
	 */
	String TYPE_PART_TYPE_ATTR = "type";

	/**
	 * @see DerivedTLTypePart#isDerived()
	 */
	String TYPE_PART_DERIVED_ATTR = "derived";
	
	/**
	 * @see TLProperty
	 */
	String PROPERTY_ELEMENT = "property";
	
	/**
	 * @see TLReference
	 */
	String REFERENCE_ELEMENT = "reference";

	/**
	 * @see TLReference#getEnd()
	 */
	String REFERENCE_ASSOCIATION_NAME_ATTR = "association";
	
	/**
	 * Reference to an anonymous {@link TLAssociation}.
	 * 
	 * @see TLReference#getEnd()
	 * @see #ID_ATTR
	 */
	String REFERENCE_ASSOCIATION_ID_ATTR = "association-id";
	
	/**
	 * @see TLReference#getEnd()
	 */
	String REFERENCE_END_NAME_ATTR = "end";
	
	/**
	 * Reference to an anonymous {@link TLAssociationEnd}.
	 * 
	 * @see TLReference#getEnd()
	 * @see #ID_ATTR
	 */
	String REFERENCE_END_ID_ATTR = "end-id";
	
	/**
	 * @see TLAssociation
	 */
	String ASSOCIATION_ELEMENT = "association";

	/**
	 * Document local identifier for anonymous parts.
	 */
	String ID_ATTR = "id";
	
	/**
	 * @see TLAssociation#getUnions()
	 */
	String ASSOCIATION_UNIONS_ELEMENT = "unions";
	
	/**
	 * @see TLAssociation#getSubsets()
	 */
	String ASSOCIATION_SUBSETS_ELEMENT = "subsets";

	/**
	 * @see TLAssociationEnd
	 */
	String END_ELEMENT = "end";
	
	/**
	 * @see TLAssociationEnd#isAggregate()
	 */
	String END_AGGREGATE_ATTR = "aggregate";

	/**
	 * @see TLAssociationEnd#isComposite()
	 */
	String END_COMPOSITE_ATTR = "composite";
	
	/**
	 * @see TLAssociationEnd#isMandatory()
	 */
	String END_MANDATORY_ATTR = "mandatory";
	
	/**
	 * @see TLAssociationEnd#isMultiple()
	 */
	String END_MULTIPLE_ATTR = "multiple";
	
	/**
	 * @see TLAssociationEnd#isOrdered()
	 */
	String END_ORDERED_ATTR = "ordered";
	
	/**
	 * @see TLAssociationEnd#isBag()
	 */
	String END_BAG_ATTR = "bag";
	
	/**
	 * @see TLAssociationEnd#canNavigate()
	 */
	String END_NAVIGATE_ATTR = "navigate";

	/**
	 * Root element for a model annotation.
	 */
	String ANNOTATION_ELEMENT = "annotation";

}
