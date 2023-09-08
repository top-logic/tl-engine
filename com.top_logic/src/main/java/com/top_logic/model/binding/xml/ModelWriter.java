/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.binding.xml;

import static com.top_logic.basic.xml.WrappedXMLStreamException.*;
import static com.top_logic.basic.xml.XMLStreamUtil.*;
import static com.top_logic.model.binding.xml.TLModelBindingConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.WrappedXMLStreamException;
import com.top_logic.basic.xml.XMLStreamIndentationWriter;
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
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * XML serializer for {@link TLModel} documents writing a schema corresponding to
 * {@link TLModelBindingConstants}.
 * 
 * @deprecated TODO #2829: Delete TL 6 deprecation: Old-style syntax not based on typed
 *             configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class ModelWriter extends DefaultDescendingTLModelVisitor<XMLStreamWriter> {

	private Map<TLModelPart, String> ids = new HashMap<>();
	private int nextId;

	/**
	 * Creates a {@link ModelWriter}.
	 * 
	 * <p>
	 * Since this writer is stateful, it must be used for only a single serialization.
	 * </p>
	 */
	private ModelWriter() {
		super(true);
	}

	/**
	 * Serializes the given model to the given file without indentation.
	 * 
	 * @see #writeModel(File, TLModel, boolean)
	 */
	public static void writeModel(File file, TLModel model) throws XMLStreamException, IOException {
		writeModel(file, model, false);
	}

	/**
	 * Serializes the given model to the given file.
	 * 
	 * @param file
	 *        The {@link File} to write the given model to.
	 * @param model
	 *        The model to write.
	 * @param prettyPrint
	 *        Whether the output should be pretty printed with indentation.
	 */
	public static void writeModel(File file, TLModel model, boolean prettyPrint) throws XMLStreamException, IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			writeModel(out, model, prettyPrint);
		} finally {
			out.close();
		}
	}

	/**
	 * Serializes the given model to the given stream.
	 * 
	 * @param out
	 *        The {@link OutputStream} to write the given model to.
	 * @param model
	 *        The model to write.
	 * @param prettyPrint
	 *        Whether the output should be pretty printed with indentation.
	 */
	public static void writeModel(OutputStream out, TLModel model, boolean prettyPrint) throws XMLStreamException {
		XMLOutputFactory outputFactory = XMLStreamUtil.getDefaultOutputFactory();
		XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(out, "utf-8");
		if (prettyPrint) {
			streamWriter = new XMLStreamIndentationWriter(streamWriter);
		}
		try {
			writeModel(streamWriter, model);
		} finally {
			/* Closing streamWriter does <b>not</b> close given OutputStream. */
			streamWriter.close();
		}
	}

	/**
	 * Serializes the given model to the given writer.
	 * 
	 * @param writer
	 *        The {@link XMLStreamWriter} to write the given model to.
	 * @param model
	 *        The model to write.
	 */
	public static void writeModel(XMLStreamWriter writer, TLModelPart model) throws XMLStreamException {
		try {
			writer.writeStartDocument();
			model.visit(new ModelWriter(), writer);
			writer.writeEndDocument();
		} catch (WrappedXMLStreamException ex) {
			ex.unwrap();
		}
	}
	
	private static final Void none = null;

	private static final Set<Class<?>> IGNORED_ANNOTATIONS = new HashSet<>();

	@Override
	public Void visitModel(TLModel model, XMLStreamWriter writer) {
		try {
			String cPrefix = "c";
			String ePrefix = "e";
			String dbPrefix = "db";
			String cachePrefix = "cache";
			String dsPrefix = "ds";
			String jPrefix = "j";
			String aPrefix = "a";
			
			writer.setDefaultNamespace(NS_MODEL);
			
			Map<String, String> namespaceByPrefix = new HashMap<>();
			
			namespaceByPrefix.put(ePrefix, NS_EVOLUTION);
			namespaceByPrefix.put(cPrefix, NS_CONSTRAINT);
			namespaceByPrefix.put(dbPrefix, NS_BINDING_DB);
			namespaceByPrefix.put(cachePrefix, NS_BINDING_CACHE);
			namespaceByPrefix.put(dsPrefix, NS_BINDING_DS);
			namespaceByPrefix.put(jPrefix, NS_BINDING_JAVA);
			namespaceByPrefix.put(aPrefix, ConfigurationSchemaConstants.CONFIG_NS);
			
			writer.writeStartElement(NS_MODEL, MODEL_ELEMENT);
			writer.writeDefaultNamespace(NS_MODEL);
			
			for (Entry<String, String> entry : namespaceByPrefix.entrySet()) {
				writer.setPrefix(entry.getKey(), entry.getValue());
				writer.writeNamespace(entry.getKey(), entry.getValue());
			}
			
			writeAnnotations(model, writer);
			super.visitModel(model, writer);
			
			writer.writeEndElement();
			return none;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}

	private boolean hasAnnotations(TLModelPart model) {
		return model.getAnnotations().size() > 0;
	}
	
	private void writeAnnotations(TLModelPart model, XMLStreamWriter writer) {
		try {
			Collection<? extends TLAnnotation> annotations = model.getAnnotations();
			if (annotations.size() > 0) {
				ConfigurationDescriptor staticType =
					TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class);
				ConfigurationWriter configWriter = new ConfigurationWriter(writer);
				for (ConfigurationItem annotation : sortAnnotations(annotations)) {
					// Ignore annotations with instance formats, since the instances may not yet be
					// well-prepared for configuration extraction.
					if (IGNORED_ANNOTATIONS.contains(annotation.descriptor().getConfigurationInterface())) {
						continue;
					}
					configWriter.writeRootElement(ANNOTATION_ELEMENT, staticType, annotation);
				}
			}
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}

	private <P extends ConfigurationItem> Collection<P> sortAnnotations(Collection<P> parts) {
		ArrayList<P> result = new ArrayList<>(parts);
		Collections.sort(result, new Comparator<P>() {

			@Override
			public int compare(P o1, P o2) {
				String o1Name = o1.getClass().getSimpleName();
				String o2Name = o2.getClass().getSimpleName();
				if (o1Name == null) {
					if (o2Name == null) {
						return 0;
					}

					return 1;
				} else {
					if (o2Name == null) {
						return -1;
					}
				}

				return o1Name.compareTo(o2Name);
			}
		});
		return result;
	}

	@Override
	public Void visitModule(TLModule model, XMLStreamWriter writer) {
		try {
			writer.writeStartElement(NS_MODEL, MODULE_ELEMENT);
			writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
			
			writeAnnotations(model, writer);
			super.visitModule(model, writer);
			
			writer.writeEndElement();
			return none;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}
	
	@Override
	public Void visitEnumeration(TLEnumeration model, XMLStreamWriter writer) {
		try {
			writer.writeStartElement(NS_MODEL, ENUMERATION_ELEMENT);
			writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
			
			writeAnnotations(model, writer);
			super.visitEnumeration(model, writer);
			
			writer.writeEndElement();
			return none;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}
	
	@Override
	public Void visitClassifier(TLClassifier model, XMLStreamWriter writer) {
		try {
			boolean hasAnnotations = hasAnnotations(model);
			writeStartElement(writer, hasAnnotations, NS_MODEL, ENUMERATION_CLASSIFIER_ELEMENT);
			writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
			writeBoolean(writer, ENUMERATION_CLASSIFIER_DEFAULT_ATTR, model.isDefault());
			
			writeAnnotations(model, writer);
			Void result = super.visitClassifier(model, writer);
			
			writeEndElement(writer, hasAnnotations);
			return result;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}
	
	@Override
	public Void visitClass(TLClass model, XMLStreamWriter writer) {
		try {
			writer.writeStartElement(NS_MODEL, CLASS_ELEMENT);
			writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
			writeBoolean(writer, CLASS_ABSTRACT_ATTR, model.isAbstract());
			writeBoolean(writer, CLASS_FINAL_ATTR, model.isFinal());
			
			writeAnnotations(model, writer);
			
			for (TLClass generalization : model.getGeneralizations()) {
				writer.writeEmptyElement(NS_MODEL, CLASS_GENERALIZATIONS_ELEMENT);
				writeTypeReference(writer, TYPE_REFERENCE_TYPE_NAME_ATTR, model, generalization);
			}
			
			super.visitClass(model, writer);
			
			writer.writeEndElement();
			return none;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}

	private void writeTypeReference(XMLStreamWriter writer, String refAttrName, TLType sourceType, TLType referencedType) throws XMLStreamException {
		TLScope sourceScope = sourceType.getScope();
		TLScope referenceScope = referencedType.getScope();
		if (sourceScope != referenceScope) {
			if (referenceScope != null)	{
				if (! (referenceScope instanceof TLModule)) {
					// TODO: Models with anonymous scopes?
					throw new UnsupportedOperationException("Cannot dump model with anonymous scopes.");
				}
				writer.writeAttribute(refAttrName, TLModelUtil.qualifiedName(referencedType));
				return;
			}
		}
		writer.writeAttribute(refAttrName, referencedType.getName());
	}
	
	@Override
	public Void visitProperty(TLProperty model, XMLStreamWriter writer) {
		try {
			boolean hasAnnotations = hasAnnotations(model);
			writeStartElement(writer, hasAnnotations, NS_MODEL, PROPERTY_ELEMENT);
			writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
			writeTypeReference(writer, TYPE_PART_TYPE_ATTR, model.getOwner(), model.getType());
			writeBoolean(writer, TYPE_PART_DERIVED_ATTR, model.isDerived());
			
			writeAnnotations(model, writer);
			Void result = super.visitProperty(model, writer);
			
			writeEndElement(writer, hasAnnotations);
			return result;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}
	
	@Override
	public Void visitReference(TLReference model, XMLStreamWriter writer) {
		try {
			boolean privateAssociation = TLModelUtil.isPrivateAssociation(model.getEnd().getOwner());
			if (privateAssociation) {
				if (model.getEnd().getOwner().getScope() != model.getOwner().getScope()) {
					throw new InvalidModelException("Non-local reference to anonymous association in '" + model + "'.");
				}
			}

			boolean hasAnnotations = hasAnnotations(model);
			boolean hasContents = hasAnnotations || privateAssociation;
			writeStartElement(writer, hasContents, NS_MODEL, REFERENCE_ELEMENT);
			{
				writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
				if (privateAssociation) {
					writeLocalEndReference(writer, model.getEnd());
				} else {
					writeEndReference(writer, model, model.getEnd());
				}
				writeBoolean(writer, TYPE_PART_DERIVED_ATTR, model.isDerived());
				
				writeAnnotations(model, writer);
				super.visitReference(model, writer);
			}
			if (privateAssociation) {
				writeAssociation(model.getEnd().getOwner(), writer);
			}
			writeEndElement(writer, hasContents);
			return none;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}

	private void writeEndReference(XMLStreamWriter writer, TLReference reference, TLAssociationEnd end) throws XMLStreamException {
		TLAssociation association = end.getOwner();
		TLModule associationModule = association.getModule();
		
		String associationName = association.getName();
		if (associationName == null) {
			if (associationModule != reference.getOwner().getScope()) {
				throw new InvalidModelException("Non-local reference to anonymous association in '" + reference + "'.");
			}
			writer.writeAttribute(REFERENCE_ASSOCIATION_ID_ATTR, getId(association));
		} else {
			String namePrefix;
			if (reference.getOwner().getScope() != associationModule) {
				namePrefix = associationModule.getName() + ".";
			} else {
				namePrefix = "";
			}
			
			writer.writeAttribute(REFERENCE_ASSOCIATION_NAME_ATTR, namePrefix + associationName);
		}
		
		writeLocalEndReference(writer, end);
	}

	private void writeLocalEndReference(XMLStreamWriter writer, TLAssociationEnd end) throws XMLStreamException {
		String endName = end.getName();
		if (endName == null) {
			writer.writeAttribute(REFERENCE_END_ID_ATTR, getId(end));
		} else {
			writer.writeAttribute(REFERENCE_END_NAME_ATTR, endName);
		}
	}
	
	private String getId(TLModelPart part) {
		String result = ids.get(part);
		if (result == null) {
			result = "p" + (nextId++);
			ids.put(part, result);
		}
		return result;
	}

	@Override
	public Void visitAssociation(TLAssociation model, XMLStreamWriter writer) {
		if (TLModelUtil.isPrivateAssociation(model)) {
			return none;
		}
		
		try {
			writeAssociation(model, writer);
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
		return none;
	}

	private void writeAssociation(TLAssociation model, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(NS_MODEL, ASSOCIATION_ELEMENT);
		{
			if (model.getName() != null) {
				writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
			} else {
				writer.writeAttribute(ID_ATTR, getId(model));
			}
		
			writeAnnotations(model, writer);
			
			if (false) {
				for (TLAssociation union : model.getUnions()) {
					writer.writeEmptyElement(NS_MODEL, ASSOCIATION_UNIONS_ELEMENT);
					{
						writeTypeReference(writer, REFERENCE_ASSOCIATION_NAME_ATTR, model, union);
					}
				}
			} else {
				for (TLAssociation subset : model.getSubsets()) {
					writer.writeEmptyElement(NS_MODEL, ASSOCIATION_SUBSETS_ELEMENT);
					{
						writeTypeReference(writer, REFERENCE_ASSOCIATION_NAME_ATTR, model, subset);
					}
				}
			}
			
			super.visitAssociation(model, writer);
		}
		writer.writeEndElement();
	}

	@Override
	public Void visitAssociationEnd(TLAssociationEnd model, XMLStreamWriter writer) {
		try {
			boolean hasAnnotations = hasAnnotations(model);
			writeStartElement(writer, hasAnnotations, NS_MODEL, END_ELEMENT);
			{
				if (model.getName() != null) {
					writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
				} else {
					writer.writeAttribute(ID_ATTR, getId(model));
				}
				writeTypeReference(writer, TYPE_PART_TYPE_ATTR, model.getOwner(), model.getType());
				
				writeBoolean(writer, END_AGGREGATE_ATTR, model.isAggregate());
				writeBoolean(writer, END_COMPOSITE_ATTR, model.isComposite());
				writeBoolean(writer, END_MANDATORY_ATTR, model.isMandatory());
				writeBoolean(writer, END_MULTIPLE_ATTR, model.isMultiple());
				writeBoolean(writer, END_ORDERED_ATTR, model.isOrdered());
				writeBoolean(writer, END_BAG_ATTR, model.isBag());

				if (! model.canNavigate()) {
					writer.writeAttribute(END_NAVIGATE_ATTR, Boolean.toString(model.canNavigate()));
				}
				
				writeAnnotations(model, writer);
				super.visitAssociationEnd(model, writer);
			}
			writeEndElement(writer, hasAnnotations);
			return none;
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
	}

	private void writeBoolean(XMLStreamWriter writer, String attr, boolean value) throws XMLStreamException {
		if (value) {
			writer.writeAttribute(attr, Boolean.toString(value));
		}
	}

	@Override
	public Void visitPrimitive(TLPrimitive model, XMLStreamWriter writer) {
		try {
			boolean hasAnnotations = hasAnnotations(model);
			writeStartElement(writer, hasAnnotations, NS_MODEL, DATATYPE_ELEMENT);
			{
				writer.writeAttribute(NAMED_ELEMENT_NAME_ATTR, model.getName());
				writeAnnotations(model, writer);
				super.visitPrimitive(model, writer);

			}
			writeEndElement(writer, hasAnnotations);
		} catch (XMLStreamException ex) {
			throw wrap(ex);
		}
		return none;
	}

}
