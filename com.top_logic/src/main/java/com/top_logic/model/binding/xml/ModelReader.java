/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.binding.xml;

import static com.top_logic.basic.xml.XMLStreamUtil.*;
import static com.top_logic.model.binding.xml.TLModelBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.model.InvalidModelException;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.access.IdentityMapping;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.util.TLModelUtil;

/**
 * Reader for a {@link TLModel} from its default XML format specified by
 * {@link TLModelBindingConstants}.
 * 
 * @deprecated TODO #2829: Delete TL 6 deprecation: Old-style syntax not based on typed
 *             configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class ModelReader {

	private final XMLStreamReader reader;
	private final TLModel model;

	/**
	 * Temporary document local identifiers for referencing anonymous parts.
	 */
	private Map<String, TLModelPart> partsById = new HashMap<>();

	/**
	 * {@link PartResolver}s that represent potential forward references that
	 * must be resolved after reading the complete document.
	 */
	private List<PartResolver> resolvers = new ArrayList<>();
	
	private static final Map<String, TLPrimitive.Kind> KINDS_BY_NAME;
	static {
		Map<String, TLPrimitive.Kind> kindsByName = new HashMap<>();
		for (Kind kind : Kind.values()) {
			kindsByName.put(kind.name(), kind);
		}
		KINDS_BY_NAME = kindsByName;
	}

	ConfigurationReader.Handler annotationParser;

	private DefaultInstantiationContext _context;

	
	/**
	 * Creates a {@link ModelReader}.
	 * 
	 * @see #readModel()
	 * 
	 * @param reader
	 *        The {@link XMLStreamReader} to read events from.
	 * @param model
	 *        The model to fill with contents read from the given reader.
	 */
	public ModelReader(XMLStreamReader reader, TLModel model) {
		this.reader = reader;
		this.model = model;
	}
	
	/**
	 * The model that is filled by this reader.
	 */
	protected TLModel getModel() {
		return model;
	}

	/**
	 * Starts the reading process.
	 */
	public void readModel() throws XMLStreamException {
		LogProtocol protocol = new LogProtocol(ModelReader.class);
		_context = new DefaultInstantiationContext(protocol);

		annotationParser = new ConfigurationReader.Handler(
			_context,
			Collections.singletonMap(
				TLModelBindingConstants.ANNOTATION_ELEMENT,
				TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class)));

		XMLStreamUtil.nextStartTag(reader);
		{
			String localName = reader.getLocalName();
			if (MODEL_ELEMENT.equals(localName)) {
				readModelContents();
			} else if (MODULE_ELEMENT.equals(localName)) {
				readModuleContents();
			} else {
				errorExpecting(MODEL_ELEMENT, MODULE_ELEMENT);
			}
		}
		for (PartResolver resolver : resolvers) {
			resolver.resolveReference();
		}
		resolvers.clear();
		partsById.clear();
		annotationParser = null;

		checkContext();
	}

	private void checkContext() {
		try {
			_context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unable to read model.", ex);
		}
	}

	private void readModelContents() throws XMLStreamException {
		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}
			
			if (isModelElement()) {
				if (MODULE_ELEMENT.equals(reader.getLocalName())) {
					readModuleContents();
				} else {
					errorExpecting(MODULE_ELEMENT);
				}
			} else {
				readAnnotationElement(model);
			}
		}
	}

	private void readAnnotationElement(TLModelPart modelPart) throws XMLStreamException {
		String ns = reader.getNamespaceURI();
		if (NS_MODEL.equals(ns)) {
			if (!reader.getLocalName().equals(TLModelBindingConstants.ANNOTATION_ELEMENT)) {
				throw errorExpecting(TLModelBindingConstants.ANNOTATION_ELEMENT);
			}
			ConfigurationItem annotationBuilder = annotationParser.parseContents(reader, null);
			checkContext();
			if (annotationBuilder != null) {
				TLAnnotation annotation =
					(TLAnnotation) ((annotationBuilder instanceof ConfigBuilder)
						? ((ConfigBuilder) annotationBuilder).createConfig(_context) : annotationBuilder);
				checkContext();
				if (annotation != null) {
					modelPart.setAnnotation(annotation);
				}
			}
		} else {
			// Skip foreign namespace elements.
			XMLStreamUtil.skipUpToMatchingEndTag(reader);
		}
	}

	private void readModuleContents() throws XMLStreamException {
		String name = readStringMandatory(NAMED_ELEMENT_NAME_ATTR);
		TLModule module = TLModelUtil.addModule(model, name);
		
		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}
			
			if (isModelElement()) {
				String localName = reader.getLocalName();
				if (CLASS_ELEMENT.equals(localName)) {
					readClassContents(module);
				} else if (ASSOCIATION_ELEMENT.equals(localName)) {
					readAssociationContents(module);
				} else if (ENUMERATION_ELEMENT.equals(localName)) {
					readEnumerationContents(module);
				} else if (DATATYPE_ELEMENT.equals(localName)) {
					readDatatypeContents(module);
				} else {
					errorExpecting(CLASS_ELEMENT, ASSOCIATION_ELEMENT, ENUMERATION_ELEMENT);
				}
			} else {
				readAnnotationElement(module);
			}
		}
	}
	
	
	private void readClassContents(TLModule module) throws XMLStreamException {
		String name = readStringMandatory(NAMED_ELEMENT_NAME_ATTR);
		final TLClass clazz = TLModelUtil.addClass(module, name);

		clazz.setAbstract(readBoolean(CLASS_ABSTRACT_ATTR));
		clazz.setFinal(readBoolean(CLASS_FINAL_ATTR));
		
		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}

			if (isModelElement()) {
				String localName = reader.getLocalName();
				if (PROPERTY_ELEMENT.equals(localName)) {
					readPropertyContents(clazz);
				} else if (REFERENCE_ELEMENT.equals(localName)) {
					readReferenceContents(clazz);
				} else if (CLASS_GENERALIZATIONS_ELEMENT.equals(localName)) {
					addResolver(new TypeResolver(clazz, module.getName(), readStringMandatory(TYPE_REFERENCE_TYPE_NAME_ATTR)) {
						@Override
						protected void resolvedType(TLType type) {
							clazz.getGeneralizations().add((TLClass) type);
						}
					});
					nextEndTag(reader);
					assert CLASS_GENERALIZATIONS_ELEMENT.equals(reader.getLocalName());
				} else {
					errorExpecting(PROPERTY_ELEMENT, REFERENCE_ELEMENT, CLASS_GENERALIZATIONS_ELEMENT);
				}
			} else {
				readAnnotationElement(clazz);
			}
		}
	}

	private boolean isModelElement() {
		return NS_MODEL.equals(reader.getNamespaceURI()) && !TLModelBindingConstants.ANNOTATION_ELEMENT.equals(reader.getLocalName());
	}

	private void readPropertyContents(TLStructuredType type) throws XMLStreamException {
		String name = readStringMandatory(NAMED_ELEMENT_NAME_ATTR);
		String typeSpec = readStringMandatory(TYPE_PART_TYPE_ATTR);

		TLProperty property = TLModelUtil.addProperty(type, name, null);
		
		setTypeName(((TLModule) type.getScope()).getName(), property, typeSpec);
		
		readAnnotationContents(property);
		assert PROPERTY_ELEMENT.equals(reader.getLocalName());
	}

	private void readAnnotationContents(TLModelPart part)
			throws XMLStreamException {
		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}

			if (isModelElement()) {
				throw error("Received '" + reader.getLocalName() + "' element, expected end tag.");
			} else {
				readAnnotationElement(part);
			}
		}
	}

	private void readReferenceContents(TLClass clazz) throws XMLStreamException {
		String name = readStringMandatory(NAMED_ELEMENT_NAME_ATTR);
		final TLReference reference = TLModelUtil.addReference(clazz, name, null);
		
		addPart(readString(ID_ATTR), reference);

		String associationName = reader.getAttributeValue(null, REFERENCE_ASSOCIATION_NAME_ATTR);
		String associationId = reader.getAttributeValue(null, REFERENCE_ASSOCIATION_ID_ATTR);

		String endName = reader.getAttributeValue(null, REFERENCE_END_NAME_ATTR);
		String endId = reader.getAttributeValue(null, REFERENCE_END_ID_ATTR);
		if (endName == null && endId == null) {
			throw error("Either '" + REFERENCE_END_NAME_ATTR + "' or '" + REFERENCE_END_ID_ATTR + "' attribute expected.");
		}

		if ((associationName == null) && (associationId == null)) {
			boolean associationFound = false;
			while (true) {
				int tag = reader.nextTag();
				if (tag == XMLStreamReader.END_ELEMENT) {
					if (!associationFound) {
						throw error("Expecting local association specification.");
					}
					break;
				}

				if (isModelElement()) {
					String localName = reader.getLocalName();
					if (ASSOCIATION_ELEMENT.equals(localName)) {
						if (associationFound) {
							throw error("Duplicate local association specification.");
						}
						TLAssociation localAssociation = readAssociationContents((TLModule) clazz.getScope());

						TLAssociationEnd end = getEnd(localAssociation, endName);
						reference.setEnd(end);
						associationFound = true;
					} else {
						errorExpecting(ASSOCIATION_ELEMENT);
					}
				} else {
					readAnnotationElement(reference);
				}
			}
		} else {
			// The referenced association may not yet be read.
			addResolver(new EndResolver(reference, ((TLNamedPart) clazz.getScope()).getName(), associationName, associationId, endName, endId) {
				@Override
				protected void resolvedEnd(TLAssociationEnd end) {
					reference.setEnd(end);
				}
			});
			
			while (true) {
				int tag = reader.nextTag();
				if (tag == XMLStreamReader.END_ELEMENT) {
					assert REFERENCE_ELEMENT.equals(reader.getLocalName());
					break;
				}

				if (isModelElement()) {
					errorExpecting();
				} else {
					readAnnotationElement(reference);
				}
			}
		}
	}

	private TLAssociationEnd getEnd(TLAssociation localAssociation, String endName) throws XMLStreamException {
		TLAssociationEnd end = (TLAssociationEnd) localAssociation.getPart(endName);
		if (end == null) {
			throw error("Missing end '" + endName + "' in association '" + localAssociation + "'.");
		}
		return end;
	}

	private TLAssociation readAssociationContents(TLModule module) throws XMLStreamException {
		String name = readString(NAMED_ELEMENT_NAME_ATTR);
		final TLAssociation association = TLModelUtil.addAssociation(module, name);

		addPart(readString(ID_ATTR), association);

		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}
			
			if (isModelElement()) {
				String localName = reader.getLocalName();
				if (END_ELEMENT.equals(localName)) {
					readAssociationEndContents(association);
				} else if (PROPERTY_ELEMENT.equals(localName)) {
					readPropertyContents(association);
				} else if (ASSOCIATION_UNIONS_ELEMENT.equals(localName)) {
					addResolver(new AssociationResolver(association, module.getName(), readString(REFERENCE_ASSOCIATION_NAME_ATTR), readString(REFERENCE_ASSOCIATION_ID_ATTR)) {
						@Override
						protected void resolvedAssociation(TLAssociation otherAssociation) {
							association.getUnions().add(otherAssociation);
						}
					});
					nextEndTag(reader);
					assert ASSOCIATION_UNIONS_ELEMENT.equals(reader.getLocalName());
				} else if (ASSOCIATION_SUBSETS_ELEMENT.equals(localName)) {
					String associationName = readString(REFERENCE_ASSOCIATION_NAME_ATTR);
					String associationId = readString(REFERENCE_ASSOCIATION_ID_ATTR);
					addResolver(new AssociationResolver(association, module.getName(), associationName, associationId) {
						@Override
						protected void resolvedAssociation(TLAssociation otherAssociation) {
							/* Actually "association.getSubsets().add(otherAssociation);" but
							 * subsets is not modifiable */
							otherAssociation.getUnions().add(association);
						}
					});
					nextEndTag(reader);
					assert ASSOCIATION_SUBSETS_ELEMENT.equals(reader.getLocalName());
				} else {
					errorExpecting(END_ELEMENT, PROPERTY_ELEMENT, ASSOCIATION_UNIONS_ELEMENT, ASSOCIATION_SUBSETS_ELEMENT);
				}
			} else {
				readAnnotationElement(association);
			}
		}

		return association;
	}

	private void readAssociationEndContents(TLAssociation association) throws XMLStreamException {
		String name = readString(NAMED_ELEMENT_NAME_ATTR);
		final TLAssociationEnd end = TLModelUtil.addEnd(association, name, null);
		addPart(readString(ID_ATTR), end);
		
		end.setAggregate(readBoolean(END_AGGREGATE_ATTR));
		end.setBag(readBoolean(END_BAG_ATTR));
		end.setComposite(readBoolean(END_COMPOSITE_ATTR));
		end.setMandatory(readBoolean(END_MANDATORY_ATTR));
		end.setMultiple(readBoolean(END_MULTIPLE_ATTR));
		end.setNavigate(readBoolean(END_NAVIGATE_ATTR, true));
		end.setOrdered(readBoolean(END_ORDERED_ATTR));

		String typeName = readStringMandatory(TYPE_PART_TYPE_ATTR);

		setTypeName(((TLModule) association.getScope()).getName(), end, typeName);

		readAnnotationContents(end);
		assert END_ELEMENT.equals(reader.getLocalName());
	}

	private void setTypeName(String moduleName, final TLTypePart part, String typeName) {
		Kind primitiveKind = getPrimitiveKind(typeName);
		if (primitiveKind != null) {
			part.setType(TLCore.getPrimitiveType(model, primitiveKind));
		} else {
			addResolver(new TypeResolver(part, moduleName, typeName) {
				@Override
				protected void resolvedType(TLType type) {
					part.setType(type);
				}
			});
		}
	}

	private static Kind getPrimitiveKind(String typeName) {
		return KINDS_BY_NAME.get(typeName);
	}

	private void readEnumerationContents(TLModule module) throws XMLStreamException {
		String name = readString(NAMED_ELEMENT_NAME_ATTR);
		final TLEnumeration enumeration = TLModelUtil.addEnumeration(module, name);

		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}
			
			if (isModelElement()) {
				String localName = reader.getLocalName();
				if (ENUMERATION_CLASSIFIER_ELEMENT.equals(localName)) {
					readClassifierContents(enumeration);
				} else {
					errorExpecting(ENUMERATION_CLASSIFIER_ELEMENT);
				}
			} else {
				readAnnotationElement(enumeration);
			}
		}
	}

	private void readClassifierContents(TLEnumeration enumeration) throws XMLStreamException {
		String name = readString(NAMED_ELEMENT_NAME_ATTR);
		final TLClassifier classifier = TLModelUtil.addClassifier(enumeration, name);
		
		if (readBoolean(ENUMERATION_CLASSIFIER_DEFAULT_ATTR)) {
			classifier.setDefault(true);
		}

		readAnnotations(classifier);
		assert ENUMERATION_CLASSIFIER_ELEMENT.equals(reader.getLocalName());
	}

	private void readDatatypeContents(TLModule module) throws XMLStreamException {
		String name = readString(NAMED_ELEMENT_NAME_ATTR);
		String mappingName = readString(STORAGE_MAPPING_ATTR);
		StorageMapping<?> mapping;
		try {
			mapping = StringServices.isEmpty(mappingName) ?
				IdentityMapping.INSTANCE :
				ConfigUtil.getInstanceMandatory(StorageMapping.class, STORAGE_MAPPING_ATTR, mappingName);
		} catch (ConfigurationException ex) {
			throw error("Unable to resolve storage mapping", ex);
		}
		final TLPrimitive datatype = TLModelUtil.addDatatype(module, name, mapping);
		readAnnotations(datatype);
	}

	private void readAnnotations(final TLModelPart modelPart) throws XMLStreamException {
		while (true) {
			int tag = reader.nextTag();
			if (tag == XMLStreamReader.END_ELEMENT) {
				break;
			}

			readAnnotationElement(modelPart);
		}
	}

	private void addPart(String id, TLModelPart part) {
		if (id != null) {
			partsById.put(id, part);
		}
	}
	
	TLModelPart getPart(String id) {
		return partsById.get(id);
	}

	private void addResolver(PartResolver resolver) {
		resolvers.add(resolver);
	}
	
	private String readStringMandatory(String attributeLocalName) throws XMLStreamException {
		String value = readString(attributeLocalName);
		if (value == null) {
			throw error("Missing attribute value for attribute '" + attributeLocalName + "'.");
		}
		return value;
	}

	private String readString(String attributeLocalName) {
		return reader.getAttributeValue(null, attributeLocalName);
	}

	private boolean readBoolean(String attr) throws XMLStreamException {
		return readBoolean(attr, false);
	}


	private boolean readBoolean(String attr, boolean defaultValue) throws XMLStreamException {
		String value = readString(attr);
		if (value == null) {
			return defaultValue;
		}
		
		if ("true".equals(value)) {
			return true;
		}
		if ("false".equals(value)) {
			return false;
		}
		
		throw error("Expected boolean value, found '" + value + "'.");
	}

	private XMLStreamException errorExpecting(String... expectedTagNames) throws XMLStreamException {
		throw error("Expected tags '" + Arrays.asList(expectedTagNames) + "' but found '" + reader.getLocalName() + "'.");
	}

	private XMLStreamException error(String message) throws XMLStreamException {
		throw error(message, null);
	}

	private XMLStreamException error(String message, Exception ex) throws XMLStreamException {
		throw new XMLStreamException(message, reader.getLocation(), ex);
	}

	/**
	 * Deferred reference to a {@link TLModelPart}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private abstract class PartResolver {
		
		private final TLModelPart context;

		public PartResolver(TLModelPart context) {
			this.context = context;
		}
		
		protected TLModelPart getContext() {
			return context;
		}
		
		public abstract void resolveReference();
		
	}
	
	/**
	 * {@link PartResolver} that resolves a textual type reference.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private abstract class TypeResolver extends PartResolver {
		private final String localModuleName;
		private final String typeName;

		/**
		 * Creates a {@link TypeResolver}.
		 * 
		 * @param localModuleName
		 *        The module name in which the reference was encountered.
		 * @param typeName
		 *        The type name to resolve.
		 */
		public TypeResolver(TLModelPart context, String localModuleName, String typeName) {
			super(context);
			
			this.localModuleName = localModuleName;
			this.typeName = typeName;
		}
		
		@Override
		public void resolveReference() {
			String moduleName;
			String localName;
			int localNameSeparatorIndex = typeName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
			if (localNameSeparatorIndex > 0) {
				moduleName = typeName.substring(0, localNameSeparatorIndex);
				localName = typeName.substring(localNameSeparatorIndex + 1);
			} else {
				moduleName = localModuleName;
				localName = typeName;
			}
			
			TLModule module = getModel().getModule(moduleName);
			if (module == null) {
				throw new InvalidModelException("Module reference '" + moduleName + "' cannot be resolved in '"
					+ localModuleName + "' referred to from '" + getContext() + "'.");
			}
			TLType type = module.getType(localName);
			
			if (type == null) {
				throw new InvalidModelException("Type reference '" + typeName + "' cannot be resolved in '" + localModuleName + "' referred to from '" + getContext() + "'.");
			}
			
			resolvedType(type);
		}
		
		protected abstract void resolvedType(TLType type);
	}
	
	/**
	 * {@link PartResolver} that resolves an association either by name or by ID.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private abstract class AssociationResolver extends PartResolver {

		private final String localModuleName;
		private final String associationName;
		private final String associationId;

		/**
		 * Creates a {@link AssociationResolver}.
		 * 
		 * @param localModuleName
		 *        The name of the module in which the reference was encountered.
		 * @param associationName
		 *        The name of the referenced association.
		 * @param associationId
		 *        The ID of the referenced association.
		 */
		public AssociationResolver(TLModelPart context, String localModuleName, String associationName, String associationId) {
			super(context);
			this.localModuleName = localModuleName;
			this.associationName = associationName;
			this.associationId = associationId;
		}
		
		@Override
		public void resolveReference() {
			TLAssociation association;
			if (associationName != null) {
				String moduleName;
				String localName;
				int localNameSeparatorIndex = associationName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
				if (localNameSeparatorIndex > 0) {
					moduleName = associationName.substring(0, localNameSeparatorIndex);
					localName = associationName.substring(localNameSeparatorIndex + 1);
				} else {
					moduleName = localModuleName;
					localName = associationName;
				}
				
				TLModule module = getModel().getModule(moduleName);
				if (module == null) {
					throw new InvalidModelException("Module of association '" + associationName + "' does not exist, referred to from '" + getContext() + "'.");
				}
				
				association = (TLAssociation) module.getType(localName);
			} else {
				association = (TLAssociation) getPart(associationId);
			}
			
			resolvedAssociation(association);
		}

		protected abstract void resolvedAssociation(TLAssociation association);

	}

	/**
	 * {@link PartResolver} that resolves an association end.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private abstract class EndResolver extends AssociationResolver {

		private final String endName;
		private final String endId;

		/**
		 * Creates a {@link EndResolver}.
		 * 
		 * @param localModuleName
		 *        See
		 *        {@link AssociationResolver#AssociationResolver(TLModelPart, String, String, String)}.
		 * @param associationName
		 *        See
		 *        {@link AssociationResolver#AssociationResolver(TLModelPart, String, String, String)}.
		 * @param associationId
		 *        See
		 *        {@link AssociationResolver#AssociationResolver(TLModelPart, String, String, String)}.
		 * @param endName
		 *        The name of the referenced end.
		 * @param endId
		 *        The ID of the referenced end.
		 */
		public EndResolver(TLModelPart context, String localModuleName, String associationName, String associationId, String endName, String endId) {
			super(context, localModuleName, associationName, associationId);
			this.endName = endName;
			this.endId = endId;
		}
		
		@Override
		protected void resolvedAssociation(TLAssociation association) {
			TLAssociationEnd end;
			if (endName != null) {
				end = (TLAssociationEnd) association.getPart(endName);
			} else {
				end = (TLAssociationEnd) getPart(endId);
			}
			resolvedEnd(end);
		}

		protected abstract void resolvedEnd(TLAssociationEnd end);

	}
	
}
