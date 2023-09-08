/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml;

import static com.top_logic.template.xml.TemplateSchema.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.xml.LoggingXMLStreamReader;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.template.FormatMap;
import com.top_logic.template.TemplateFormat;
import com.top_logic.template.TemplateParseResult;
import com.top_logic.template.TemplateTypes;
import com.top_logic.template.model.TemplateMOAttribute;
import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;
import com.top_logic.template.parser.TemplateParserTokenManager;
import com.top_logic.template.tree.AssignStatement;
import com.top_logic.template.tree.AttributeValue;
import com.top_logic.template.tree.DefineStatement;
import com.top_logic.template.tree.Expression;
import com.top_logic.template.tree.ForeachStatement;
import com.top_logic.template.tree.IfStatement;
import com.top_logic.template.tree.InvokeStatement;
import com.top_logic.template.tree.LiteralText;
import com.top_logic.template.tree.Template;
import com.top_logic.template.tree.TemplateNode;
import com.top_logic.template.tree.parameter.ListParameterValue;
import com.top_logic.template.tree.parameter.ParameterValue;
import com.top_logic.template.tree.parameter.PrimitiveParameterValue;
import com.top_logic.template.tree.parameter.StructuredParameterValue;
import com.top_logic.template.xml.TemplateSettings.OutputFormat;
import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Parser that reads a {@link Template} from its XML syntax defined in {@link TemplateSchema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateXMLParser {

	private static final String XML_NAMESPACE_PREFIX = "xmlns";

	private static final int SYNTAX_MODE = 
		Arrays.asList(TemplateParserTokenManager.lexStateNames).indexOf("Syntax");

	static abstract class AttributeParser {

		/**
		 * Consumes an attribute.
		 * 
		 * @param namespaceUri
		 *        The namespace URI of the attribute, <code>null</code> if attribute has no
		 *        namespace.
		 * @param prefix
		 *        The namespace prefix, if the attribute has a namespace, <code>null</code>
		 *        otherwise.
		 * @param localName
		 *        The attribute local name.
		 * @param value
		 *        The attribute value.
		 * @throws ParseException
		 *         If parsing for this attribute fails.
		 */
		public void parseAttribute(String namespaceUri, String prefix, String localName, String value) throws ParseException {
			if (namespaceUri == null) {
				parseTemplateAttribute(localName, value);
			} 
			
			else if (ANNOTATION_NS.equals(namespaceUri)) {
				parseAnnotationAttribute(localName, value);
			}
			
			else {
				parseForeignNamespaceAttribute(namespaceUri, prefix, localName, value);
			}
		}

		/**
		 * Consumes an attribute from a non-template namespace..
		 * 
		 * @param namespaceUri
		 *        The namespace URI of the attribute, <code>null</code> if attribute has no
		 *        namespace.
		 * @param prefix
		 *        The namepace prefix, if the attribute has a namespace, <code>null</code>
		 *        otherwise.
		 * @param localName
		 *        The attribute local name.
		 * @param value
		 *        The attribute value.
		 * @throws ParseException
		 *         If parsing for this attribute fails.
		 */
		protected void parseForeignNamespaceAttribute(String namespaceUri, String prefix, String localName, String value) throws ParseException {
			// Hook for subclasses.
		}

		/**
		 * Consumes an attribute from the {@link TemplateSchema#ANNOTATION_NS} namespace..
		 * 
		 * @param localName
		 *        The attribute local name.
		 * @param value
		 *        The attribute value.
		 * @throws ParseException
		 *         If parsing for this attribute fails.
		 */
		protected void parseAnnotationAttribute(String localName, String value) throws ParseException {
			// Hook for subclasses.
		}

		/**
		 * Consumes an attribute from the {@link TemplateSchema#TEMPLATE_NS} namespace..
		 * 
		 * @param localName
		 *        The attribute local name.
		 * @param value
		 *        The attribute value.
		 * @throws ParseException
		 *         If parsing for this attribute fails.
		 */
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			// Hook for subclasses.
		}
	}

	static class NoAttributes extends AttributeParser {
		
		@Override
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			throw new ParseException("Unsupported template attribute '" + localName + "'.");
		}
		
		@Override
		protected void parseAnnotationAttribute(String localName, String value) throws ParseException {
			throw new ParseException("Annotation not supported.");
		}
		
		@Override
		protected void parseForeignNamespaceAttribute(String namespaceUri, String prefix, String localName, String value) throws ParseException {
			throw new ParseException("Unsupported attribute namespace '" + namespaceUri + "'.");
		}
		
	}

	static class AnnotationAttributes extends NoAttributes {
		Map<String, String> attributes = new HashMap<>();
		
		@Override
		protected void parseAnnotationAttribute(String localName, String value) throws ParseException {
			attributes.put(localName, value);
		}
	}
	
	final class ForEachAttributes extends AnnotationAttributes {
		String varName;

		Expression expr;

		@Override
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			if (FOREACH__VAR_ATTR.equals(localName)) {
				varName = value;
			}
			
			else if (FOREACH__VALUES_ATTR.equals(localName)) {
				this.expr = parseExpression(value);
			}

			else {
				super.parseTemplateAttribute(localName, value);
			}
		}
	}
	
	final class IfAttributes extends AnnotationAttributes {
		Expression condition;
		
		@Override
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			if (IF__CONDITION_ATTR.equals(localName)) {
				condition = parseExpression(value);
			}
			
			else {
				super.parseTemplateAttribute(localName, value);
			}
		}
	}

	class ValueAttributes extends AnnotationAttributes {
		Expression expr;
		
		@Override
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			if (VALUE__EXPR_ATTR.equals(localName)) {
				expr = parseExpression(value);
			}
			
			else {
				super.parseTemplateAttribute(localName, value);
			}
		}
	}
	
	final class DefineAttributes extends ValueAttributes {
		String var;
		
		@Override
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			if (DEFINE__VAR_ATTR.equals(localName)) {
				var = value;
			}
			
			else {
				super.parseTemplateAttribute(localName, value);
			}
		}
	}
	
	final class InvokeAttributes extends NoAttributes {

		private final TemplateSource _outerTemplate;

		private TemplateSource _templateSource;

		private TemplateFormat _templateFormat = TemplateFormat.getDefault();

		public InvokeAttributes(TemplateSource outerTemplate) {
			_outerTemplate = outerTemplate;
		}

		@Override
		protected void parseTemplateAttribute(String localName, String value) throws ParseException {
			if (TEMPLATE_ATTRIBUTE.equals(localName)) {
				if (StringServices.equals(value, TemplateSource.SELF_INVOCATION)) {
					_templateSource = _outerTemplate;
				} else {
					_templateSource = TemplateSourceFactory.getInstance().resolve(_outerTemplate, value);
				}
			} else if (TEMPLATE_FORMAT_ATTRIBUTE.equals(localName)) {
				_templateFormat = TemplateFormat.parse(value);
			} else {
				super.parseTemplateAttribute(localName, value);
			}
		}

		public TemplateSource getTemplateSource() {
			return _templateSource;
		}

		public TemplateFormat getTemplateFormat() {
			return _templateFormat;
		}

	}

	/**
	 * The name of the XSD file used for verifying certain parts of the header of template files.
	 */
	public static final String TEMPLATE_XSD = "XmlTemplate.xsd";

	private StringWriter buffer;
	private TagWriter serializer;
	private boolean tagOpen;
	private int tagOpenPos;

	private WhitespaceFilteringXMLStreamReader in;
	private TemplateSource _templateSource;
	private TemplateSettings _settings;

	/**
	 * Parses the {@link Template} in its XML from from the given {@link Reader}.
	 * <p>
	 * The templateDir is required to resolve the relatives path to other templates invoked by this
	 * one.
	 * </p>
	 * 
	 * @throws IOException
	 *         If reading accessing the template source fails.
	 * @throws XMLStreamException
	 *         If parsing the template fails.
	 */
	public TemplateParseResult parse(TemplateSource templateSource) throws IOException, XMLStreamException {
		validateTemplate(templateSource);
		try (InputStream stream = templateSource.getContent()) {
			in = new WhitespaceFilteringXMLStreamReader(LoggingXMLStreamReader.wrap(createXmlParser(stream)));
			try {
				in.deactivateFilter();
				_templateSource = templateSource;
				TemplateParseResult result = parse();
				return result;
			} finally {
				in.close();
				in = null;
			}
		}
	}

	private XMLStreamReader createXmlParser(InputStream input) throws XMLStreamException {
		return XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(input);
	}

	private void validateTemplate(TemplateSource templateSource) {
		Schema schema = loadTemplateSchema();
		try {
			Validator validator = XsltUtil.safeValidator(schema);
			validator.validate(new SAXSource(new InputSource(templateSource.getContent())));
		} catch (Exception exception) {
			throw new RuntimeException("Template validation failed: " + exception.getMessage(), exception);
		}
	}

	private Schema loadTemplateSchema() {
		try {
			StreamSource schemaStream = new StreamSource(getClass().getResourceAsStream(TEMPLATE_XSD));
			SchemaFactory schemaFactory = XsltUtil.safeSchemaFactory();
			return schemaFactory.newSchema(schemaStream);
		} catch (Exception exception) {
			throw new RuntimeException("Loading template schema failed: " + exception.getMessage(), exception);
		}
	}

	private TemplateParseResult parse() {
		skipToTemplateStartTag(TEMPLATE_ELEMENT);
		Pair<Collection<MetaObject>, FormatMap> typesAndFormatMap = parseHeader();
		Template template = parseBody();
		skipToTemplateEndTag(TEMPLATE_ELEMENT);
		Collection<MetaObject> types = typesAndFormatMap.getFirst();
		FormatMap formatMap = typesAndFormatMap.getSecond();
		return new TemplateParseResult(template, _settings, types, formatMap);
	}

	private Pair<Collection<MetaObject>, FormatMap> parseHeader() {
		skipToTemplateStartTag(HEAD_ELEMENT);
		_settings = parseSettings();
		initOutputFields();
		applySettingOutputXmlHeader();
		applySettingIgnoreWhitespaces();
		skipToNextTag();
		Map<String, MetaObject> typeMap = TemplateTypes.buildTypesMap();
		if (in.getLocalName().equals(TYPES_ELEMENT)) {
			parseTypes(typeMap);
			skipToNextTag();
		}
		if (in.getLocalName().equals(PARAMETERS_ELEMENT)) {
			parseTypeDefinition(true, typeMap);
			skipToNextTag();
		} else {
			TemplateTypes.buildEmptyThisType(typeMap);
		}
		skipToTemplateEndTag(HEAD_ELEMENT);
		Collection<MetaObject> types = typeMap.values();
		FormatMap formatMap = TemplateTypes.buildFormatMap();
		return new Pair<>(types, formatMap);
	}

	private void applySettingIgnoreWhitespaces() {
		if (_settings.isIgnoreWhitespaces()) {
			in.activateFilter();
		} else {
			in.deactivateFilter();
		}
	}

	private void applySettingOutputXmlHeader() {
		try {
			if (_settings.isOutputXmlHeader()) {
				serializer.writeXMLHeader(_settings.getOutputEncoding().name());
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void initOutputFields() {
		buffer = new StringWriter();
		serializer = new TagWriter(buffer);
	}

	private TemplateSettings parseSettings() {
		skipToTemplateStartTag(SETTINGS_ELEMENT);
		String outputFormat = in.getAttributeValue(null, OUTPUT_FORMAT_ATTRIBUTE);
		TemplateSettingsBuilder settingsBuilder = new TemplateSettingsBuilder(OutputFormat.parse(outputFormat));

		String outputEncoding = in.getAttributeValue(null, OUTPUT_ENCODING_ATTRIBUTE);
		if (!StringServices.isEmpty(outputEncoding)) {
			settingsBuilder.setOutputEncoding(Charset.forName(outputEncoding));
		}

		String outputByteOrderMark = in.getAttributeValue(null, OUTPUT_BYTE_ORDER_MARK_ATTRIBUTE);
		if (!StringServices.isEmpty(outputByteOrderMark)) {
			settingsBuilder.setOutputByteOrderMark(Boolean.parseBoolean(outputByteOrderMark));
		}

		String outputXmlHeader = in.getAttributeValue(null, OUTPUT_XML_HEADER_ATTRIBUTE);
		if (!StringServices.isEmpty(outputXmlHeader)) {
			settingsBuilder.setOutputXmlHeader(Boolean.parseBoolean(outputXmlHeader));
		}

		String ignoreWhitespaces = in.getAttributeValue(null, IGNORE_WHITESPACES_ATTRIBUTE);
		if (!StringServices.isEmpty(ignoreWhitespaces)) {
			settingsBuilder.setIgnoreWhitespaces(Boolean.parseBoolean(ignoreWhitespaces));
		}

		skipToTemplateEndTag(SETTINGS_ELEMENT);
		return settingsBuilder.build();
	}

	private void parseTypes(Map<String, MetaObject> typesMap) {
		skipToTemplateStartTag(TYPES_ELEMENT);
		skipWhitespaceAndCommentsToNextTemplateTag();
		while (in.getLocalName().equals(TYPE_ELEMENT) && XMLStreamUtil.isAtStartTag(in)) {
			parseTypeDefinition(false, typesMap);
			skipToTemplateEndTag(TYPE_ELEMENT);
			skipWhitespaceAndCommentsToNextTemplateTag();
		}
		if (!XMLStreamUtil.isAtEndTag(in)) {
			throw new RuntimeException("Expected end tag </" + TYPES_ELEMENT + "> but found start tag <"
				+ in.getLocalName() + " ... >.");
		}
		if (!in.getLocalName().equals(TYPES_ELEMENT)) {
			throw new RuntimeException("Expected tag '" + TYPES_ELEMENT + "' but found '" + in.getLocalName()
				+ "'.");
		}
	}

	private void parseTypeDefinition(
			boolean isThisDefinition, Map<String, MetaObject> typeMap) {
		assert XMLStreamUtil.isAtStartTag(in) : "Expected event type "
			+ XMLStreamConstants.START_ELEMENT + " but found " + XMLStreamUtil.getEventName(in.getEventType()) + ".";
		assert in.getLocalName().equals(PARAMETERS_ELEMENT) || in.getLocalName().equals(TYPE_ELEMENT) : "Expected tag '"
			+ PARAMETERS_ELEMENT + "' or '" + TYPE_ELEMENT + "' but found '" + in.getLocalName() + "'.";
		assert in.getNamespaceURI().equals(TEMPLATE_NS) : "Expected namespace '" + TEMPLATE_NS + "' but found '"
			+ in.getNamespaceURI() + "'.";

		String typeName =
			isThisDefinition ? TemplateTypes.IMPLICIT_THIS_TYPE : in.getAttributeValue(null, NAME_ATTRIBUTE);
		String attributeTagName = getAttributeTagName(isThisDefinition);
		MOClassImpl newType = new MOClassImpl(typeName);
		newType.setSuperclass((MOClass) typeMap.get(TemplateTypes.TYPE_NAME_ROOT));
		// For recursive types, the type has to exist before it is parsed.
		typeMap.put(typeName, newType);
		skipToNextTag();
		while (XMLStreamUtil.isAtStartTag(in)) {
			addAttribute(newType, parseAttribute(newType, typeMap));
			skipToTemplateEndTag(attributeTagName);
			skipToNextTag();
		}
		newType.freeze();

		if (isThisDefinition) {
			skipToTemplateEndTag(PARAMETERS_ELEMENT);
		} else {
			skipToTemplateEndTag(TYPE_ELEMENT);
		}
	}

	private void addAttribute(MOClassImpl type, MOAttribute attribute) {
		try {
			type.addAttribute(attribute);
		} catch (DuplicateAttributeException exception) {
			throw new RuntimeException(exception);
		}
	}

	private MOAttribute parseAttribute(MOClassImpl newType, Map<String, MetaObject> typeMap) {
		boolean isThisDefinition = newType.getName().equals(TemplateTypes.IMPLICIT_THIS_TYPE);
		String attributeTagName = getAttributeTagName(isThisDefinition);
		skipToTemplateStartTag(attributeTagName);
		String attributeName = in.getAttributeValue(null, NAME_ATTRIBUTE);
		String typeName = in.getAttributeValue(null, TYPE_ATTRIBUTE);
		if (!typeMap.containsKey(typeName)) {
			throw new RuntimeException("There is no type named '" + typeName + "'. Attribute: '" + attributeName
				+ "'. Known types: " + typeMap);
		}
		MetaObject rawType = typeMap.get(typeName);
		boolean isMultiple = StringServices.equals(in.getAttributeValue(null, MULTIPLE_ATTRIBUTE), "true");
		MetaObject finalType = isMultiple ? MOCollectionImpl.createListType(rawType) : rawType;
		/* 'mandatory' means something different for the template engine than for
		 * MOAttribute.setMandatory, but for simplicity, we reuse the MOAttribute.setMandatory here.
		 * For templates, it is a value constraint meaning: "The empty value is not allowed" For
		 * most types, the empty value is null in Java, but for Strings it's also the empty string
		 * and for XML it's also the empty XML tree. */
		boolean isMandatory = StringServices.equals(in.getAttributeValue(null, MANDATORY_ATTRIBUTE), "true");
		String defaultValueFromAttribute = in.getAttributeValue(null, DEFAULT_ATTRIBUTE);
		ParameterValue<TemplateNode> defaultValue;
		if (defaultValueFromAttribute == null) {
			skipToNextTag();
			if (in.getLocalName().equals(DEFAULT_ELEMENT)) {
				defaultValue = parseDefaultValueTag(finalType);
			} else {
				defaultValue = null;
			}
		} else {
			ensureNoDefaultValueTag(attributeTagName);
			AttributeValue parsedAttributeValue = parseAttributeValue(defaultValueFromAttribute, DEFAULT_ATTRIBUTE);
			defaultValue = new PrimitiveParameterValue<>(parsedAttributeValue);
		}
		MOAttributeImpl moAttribute =
			new TemplateMOAttribute(attributeName, finalType, defaultValue, isMandatory);
		workaroundIrrelevantWarning(moAttribute, typeName);
		skipToTemplateEndTag(attributeTagName);
		return moAttribute;
	}

	private ParameterValue<TemplateNode> parseDefaultValueTag(MetaObject type) {
		checkIsInTemplateNamespace();
		if (type instanceof MOPrimitive) {
			return parseDefaultValueTagPrimitive();
		}
		skipWhitespaceAndCommentsToNextTemplateTag();
		if (type instanceof MOCollection) {
			MOCollection collectionType = (MOCollection) type;
			return parseDefaultValueTagList(collectionType.getElementType());
		}
		if (type instanceof MOClass) {
			return parseDefaultValueTagStructure((MOClass) type);
		}
		if (XMLStreamUtil.isAtEndTag(in)) {
			return parseDefaultValueTagEmpty();
		}
		throw new RuntimeException("Unexpected template tag in default value definition: " + in.getLocalName());
	}

	private PrimitiveParameterValue<TemplateNode> parseDefaultValueTagEmpty() {
		return new PrimitiveParameterValue<>(new LiteralText(""));
	}

	private ParameterValue<TemplateNode> parseDefaultValueTagStructure(MOClass type) {
		checkIsAtTag();
		checkIsInTemplateNamespace();
		Map<String, ParameterValue<TemplateNode>> defaultValue = new HashMap<>();
		while (XMLStreamUtil.isAtStartTag(in)) {
			checkIsTemplateAttributeStartTag();
			String name = in.getAttributeValue(null, NAME_ATTRIBUTE);
			if (name == null) {
				throw new RuntimeException(
					"Attribute-Tag in default value definition has no name attribute!");
			}
			String valueAttribute = in.getAttributeValue(null, VALUE_ATTRIBUTE);
			if (valueAttribute == null) {
				MOAttribute attribute = getAttribute(type, name);
				defaultValue.put(name, parseDefaultValueTag(attribute.getMetaObject()));
			} else {
				defaultValue.put(name, new PrimitiveParameterValue<>(new LiteralText(valueAttribute)));
				skipWhitespaceAndCommentsToNextTemplateTag();
			}
			checkIsTemplateAttributeEndTag();
			skipWhitespaceAndCommentsToNextTemplateTag();
		}
		checkIsAtEndTag();
		checkIsInTemplateNamespace();
		return new StructuredParameterValue<>(defaultValue);
	}

	private ListParameterValue<TemplateNode> parseDefaultValueTagList(MetaObject elementType) {
		checkIsAtTag();
		checkIsInTemplateNamespace();
		if (XMLStreamUtil.isAtEndTag(in)) {
			checkIsDefaultTag();
			return new ListParameterValue<>(Collections.<ParameterValue<TemplateNode>> emptyList());
		}
		checkIsTemplateEntryStartTag();
		List<ParameterValue<TemplateNode>> entryTemplates = new ArrayList<>();
		while (!XMLStreamUtil.isAtEndTag(in)) {
			checkIsTemplateEntryStartTag();
			String valueAttribute = in.getAttributeValue(null, VALUE_ATTRIBUTE);
			if (valueAttribute == null) {
				entryTemplates.add(parseDefaultValueTag(elementType));
				checkIsTemplateEntryEndTag();
			} else {
				XMLStreamUtil.nextEvent(in);
				XMLStreamUtil.skipWhitespaceAndComments(in);
				checkIsTemplateEntryEndTag();
				entryTemplates.add(new PrimitiveParameterValue<>(new LiteralText(valueAttribute)));
			}
			XMLStreamUtil.nextEvent(in);
			XMLStreamUtil.skipWhitespaceAndComments(in);
		}
		return new ListParameterValue<>(entryTemplates);
	}

	private ParameterValue<TemplateNode> parseDefaultValueTagPrimitive() {
		try {
			String tagName = in.getLocalName();
			List<TemplateNode> innderNodes = parseTemplateContentNodes();
			checkIsAtEndTag();
			checkIsInTemplateNamespace();
			checkIsTag(tagName);
			Template defaultValue = new Template(innderNodes);
			return new PrimitiveParameterValue<>(defaultValue);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void checkIsTemplateAttributeStartTag() {
		checkIsAtStartTag();
		checkIsInTemplateNamespace();
		checkIsAttributeTag();
	}

	private void checkIsTemplateAttributeEndTag() {
		checkIsAtEndTag();
		checkIsInTemplateNamespace();
		checkIsAttributeTag();
	}

	private void checkIsAttributeTag() {
		if (!in.getLocalName().equals(ATTRIBUTE_ELEMENT)) {
			throw new RuntimeException("Expected tag '" + ATTRIBUTE_ELEMENT + "' but found '" + in.getLocalName()
				+ "'.");
		}
	}

	private void checkIsTag(String tagName) {
		if (!in.getLocalName().equals(tagName)) {
			throw new RuntimeException("Expected tag '" + tagName + "' but found '" + in.getLocalName() + "'.");
		}
	}

	private MOAttribute getAttribute(MOClass moClass, String attribute) {
		try {
			return moClass.getAttribute(attribute);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void checkIsDefaultTag() {
		if (!in.getLocalName().equals(DEFAULT_ELEMENT)) {
			throw new RuntimeException("Expected tag '" + DEFAULT_ELEMENT + "' but found '" + in.getLocalName()
				+ "'.");
		}
	}

	private void checkIsAtTag() {
		if (!XMLStreamUtil.isAtTag(in)) {
			throw new RuntimeException("Expected start or end tag but found: "
				+ XMLStreamUtil.getEventName(in.getEventType()));
		}
	}

	private void checkIsAtStartTag() {
		checkIsAtTag();
		if (in.getEventType() != XMLStreamConstants.START_ELEMENT) {
			throw new RuntimeException("Expected start tag but found end tag '"
				+ in.getLocalName() + "'.");
		}
	}

	private void checkIsAtEndTag() {
		checkIsAtTag();
		if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
			throw new RuntimeException("Expected end tag but found start tag '"
				+ in.getLocalName() + "'.");
		}
	}

	private void checkIsTemplateEntryEndTag() {
		checkIsAtEndTag();
		checkIsEntryTag();
		checkIsInTemplateNamespace();
	}

	private void checkIsTemplateEntryStartTag() {
		checkIsAtStartTag();
		checkIsEntryTag();
		checkIsInTemplateNamespace();
	}

	private void checkIsEntryTag() {
		if (!in.getLocalName().equals(ENTRY_ELEMENT)) {
			throw new RuntimeException("Expected tag '" + ENTRY_ELEMENT + "' but found '" + in.getLocalName()
				+ "'.");
		}
	}

	private void checkIsInTemplateNamespace() {
		if (StringServices.isEmpty(in.getNamespaceURI())) {
			throw new RuntimeException("Expected namespace '" + TEMPLATE_NS + "' but found none.");
		}
		if (!in.getNamespaceURI().equals(TEMPLATE_NS)) {
			throw new RuntimeException("Expected namespace '" + TEMPLATE_NS + "' but found '"
				+ in.getNamespaceURI() + "'.");
		}
	}

	private void ensureNoDefaultValueTag(String attributeTagName) {
		skipToNextTag();
		if (!XMLStreamUtil.isAtEndTag(in)) {
			throw new RuntimeException("Expected </" + attributeTagName + "> but found a tag named '"
				+ in.getLocalName() + "'.");
		}
	}

	private String getAttributeTagName(boolean isThisDefinition) {
		return isThisDefinition ? PARAMETER_ELEMENT : ATTRIBUTE_ELEMENT;
	}

	/**
	 * Workaround for an irrelevant warning logged by
	 * <code>AbstractMOAttribute#checkAttributeValue(...)</code>.
	 * <p>
	 * The warning is:<br/>
	 * <i>Length of given string (58) is longer than 10, will eventually be truncated.</i>
	 * </p>
	 */
	private void workaroundIrrelevantWarning(MOAttributeImpl moAttribute, String typeName) {
		if (moAttribute.getMetaObject().equals(MOPrimitive.CLOB)) {
			assert typeName.equalsIgnoreCase(TemplateTypes.TYPE_NAME_XML);
			moAttribute.setSQLSize(Integer.MAX_VALUE);
		}
	}

	private Template parseBody() {
		try {
			skipToTemplateStartTag(BODY_ELEMENT);
			List<TemplateNode> nodes = parseTemplateContentNodes();
			flushTagsAndBuffer(nodes);
			assert XMLStreamUtil.isAtEndTag(in) : "Expected event type "
				+ XMLStreamConstants.END_ELEMENT + " but found " + XMLStreamUtil.getEventName(in.getEventType()) + ".";
			assert in.getLocalName().equals(BODY_ELEMENT) : "Expected tag '" + BODY_ELEMENT + "' but found '"
				+ in.getLocalName() + "'.";
			assert in.getNamespaceURI().equals(TEMPLATE_NS) : "Expected namespace '" + TEMPLATE_NS + "' but found '"
				+ in.getNamespaceURI() + "'.";
			return new Template(nodes);
		} catch (XMLStreamException ex) {
			throw new RuntimeException("Invalid XML: " + ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new RuntimeException("Template XML cannot be read: " + ex.getMessage(), ex);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Calls {@link XMLStreamUtil#skipToStartTag(XMLStreamReader, String, String)} with
	 * {@link TemplateSchema#TEMPLATE_NS} as namespace and {@link #in} as {@link XMLStreamReader}.
	 */
	private void skipToTemplateStartTag(String tagName) {
		XMLStreamUtil.skipToStartTag(in, tagName, TEMPLATE_NS);
	}

	/**
	 * Calls {@link XMLStreamUtil#skipToEndTag(XMLStreamReader, String, String)} with
	 * {@link TemplateSchema#TEMPLATE_NS} as namespace and {@link #in} as {@link XMLStreamReader}.
	 */
	private void skipToTemplateEndTag(String tagName) {
		XMLStreamUtil.skipToEndTag(in, tagName, TEMPLATE_NS);
	}

	/** @see XMLStreamUtil#skipToNextTag(XMLStreamReader) */
	private void skipToNextTag() {
		XMLStreamUtil.skipToNextTag(in);
	}

	private void parseContent(List<TemplateNode> nodes, boolean inTemplate) throws XMLStreamException, IOException, ParseException {
		while (true) {
			int nextEvent = in.next();
			switch (nextEvent) {
				case XMLStreamConstants.END_DOCUMENT:
				case XMLStreamConstants.END_ELEMENT: {
					if (inTemplate) {
						flushTagsAndBuffer(nodes);
					}
					return;
				}

				case XMLStreamConstants.START_ELEMENT: {
					if (TEMPLATE_NS.equals(in.getNamespaceURI())) {
						flushTagsAndBuffer(nodes);
						
						nodes.add(parseTemplateOperation());
					} else {
						parseXMLElement(nodes);
					}
					break;
				}
				
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.SPACE: {
					flushTag();
					serializer.writeText(in.getText());
					break;
				}
				
				case XMLStreamConstants.COMMENT: {
					flushTag();
					serializer.writeCommentPlain(in.getText());
					break;
				}

				case XMLStreamConstants.PROCESSING_INSTRUCTION:
				case XMLStreamConstants.ENTITY_REFERENCE:
				case XMLStreamConstants.ATTRIBUTE:
				case XMLStreamConstants.DTD:
				case XMLStreamConstants.NAMESPACE:
				case XMLStreamConstants.NOTATION_DECLARATION:
				case XMLStreamConstants.ENTITY_DECLARATION: {
					throw new UnsupportedOperationException("This XML streaming event is not supported, yet. Code: "
						+ nextEvent);
				}
				
				case XMLStreamConstants.START_DOCUMENT: {
					throw new UnreachableAssertion("Start Document in the middle of the Document.");
				}
				
				default: {
					throw new RuntimeException("Unknown XML streaming event. Code: " + nextEvent);
				}
			}
		}
	}

	private void parseXMLElement(List<TemplateNode> nodes) throws IOException, XMLStreamException,
			ParseException {
		String tagName = getQualifiedName();
		beginTag(tagName);
		copyNamespaces(nodes);
		copyAttributes(nodes);
		parseContent(nodes, false);
		
		endTag(tagName);
	}

	private void copyNamespaces(List<TemplateNode> nodes) throws IOException {
		HashMap<String, String> requiredNamespacesByPrefix = new HashMap<>();
		String elementNS = StringServices.nonEmpty(in.getNamespaceURI());
		if (elementNS != null) {
			requiredNamespacesByPrefix.put(StringServices.nonEmpty(in.getPrefix()), elementNS);
		}
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			String ns = in.getAttributeNamespace(n);
			if (ns != null) {
				String prefix = in.getAttributePrefix(n);
				requiredNamespacesByPrefix.put(prefix, ns);
			}
		}

		for (int n = 0, cnt = in.getNamespaceCount(); n < cnt; n++) {
			String prefix = in.getNamespacePrefix(n);
			String url = in.getNamespaceURI(n);
			requiredNamespacesByPrefix.remove(prefix);
			copyNamespaceDeclaration(nodes, prefix, url);
		}

		// Note: This is a dirty hack to make the template expand to a well-formed XML document, even
		// if some of the namespaces used inside the template are defined somewhere in the template
		// header. Those namespace declarations would otherwise not be present in the template
		// expansion.
		for (Entry<String, String> entry : requiredNamespacesByPrefix.entrySet()) {
			copyNamespaceDeclaration(nodes, entry.getKey(), entry.getValue());
		}

	}

	private void copyNamespaceDeclaration(List<TemplateNode> nodes, String prefix, String elementNS)
			throws IOException {
		String namespaceAttribute =
			StringServices.isEmpty(prefix) ? XML_NAMESPACE_PREFIX : (XML_NAMESPACE_PREFIX + ":" + prefix);
		copyNamespaceAttribute(nodes, elementNS, namespaceAttribute);
	}

	private void copyNamespaceAttribute(List<TemplateNode> nodes, String url, String namespaceAttribute)
			throws IOException {
		copyAttribute(nodes, namespaceAttribute, url);
	}

	private void copyAttributes(List<TemplateNode> nodes) throws IOException {
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			String attributeValue = in.getAttributeValue(n);
			String qualifiedAttributeName = getAttributeQualifiedName(n);
			copyAttribute(nodes, qualifiedAttributeName, attributeValue);
		}
	}

	private void copyAttribute(List<TemplateNode> nodes, String attributeName, String attributeValue)
			throws IOException {
		serializer.beginAttribute(attributeName);
		/* Write an empty string to trigger writing of the attribute name. This is needed because
		 * the underlying writer is modified and the name of the attribute is expected to be
		 * contained. */
		serializer.writeAttributeText(StringServices.EMPTY_STRING);
		flushBuffer(nodes);
		AttributeValue attributeContent = parseAttributeValue(attributeValue, attributeName);
		nodes.add(attributeContent);
		serializer.endAttribute();
	}

	private AttributeValue parseAttributeValue(String value, String attributeName) {
		List<TemplateNode> attributeContent = new ArrayList<>();
		char[] newAttributeValue = new char[value.length()];
		int newIndex = 0;
		for (int oldIndex = 0; oldIndex < value.length();) {
			if ((value.charAt(oldIndex) == '$') && (oldIndex + 1 < value.length())) {
				if (value.charAt(oldIndex + 1) == '$') {
					newAttributeValue[newIndex] = '$';
					// Skip the next character, as "$$" should be replaced by "$".
					oldIndex += 2;
					newIndex += 1;
					continue;
				} else if (value.charAt(oldIndex + 1) == '{') {
					if (newIndex > 0) {
						attributeContent.add(new LiteralText(new String(newAttributeValue, 0, newIndex)));
					}
					int endMarkerIndex = value.indexOf('}', oldIndex + 2);
					if (endMarkerIndex == -1) {
						throw new RuntimeException("Start of template expression '${' was found in attribute "
							+ attributeName + " at position " + oldIndex
							+ " but no end of the template expression '}'.");
					}
					String template = value.substring(oldIndex + 2, endMarkerIndex);
					attributeContent.add(
						new AssignStatement(parseExpression(template), Collections.<String, String> emptyMap()));
					oldIndex += template.length() + 3; // 3 for "${}"
					newIndex = 0;
					continue;
				}
			}
			newAttributeValue[newIndex] = value.charAt(oldIndex);
			oldIndex += 1;
			newIndex += 1;
		}
		if (newIndex > 0) {
			attributeContent.add(new LiteralText(new String(newAttributeValue, 0, newIndex)));
		}
		return new AttributeValue(attributeContent);
	}

	private TemplateNode parseTemplateOperation() throws XMLStreamException, IOException, ParseException {
		String operationName = in.getLocalName();
		
		if (VALUE_ELEMENT.equals(operationName)) {
			ValueAttributes attrs;
			parseAttributes(attrs = new ValueAttributes());
			AssignStatement result = new AssignStatement(attrs.expr, attrs.attributes);
			
			XMLStreamUtil.nextEvent(in);
			XMLStreamUtil.skipWhitespaceAndComments(in);
			if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
				throw new ParseException("No content expected for '" + VALUE_ELEMENT + "'.");
			}
			
			return result;
		}
		
		else if (FOREACH_ELEMENT.equals(operationName)) {
			ForEachAttributes attrs;
			parseAttributes(attrs = new ForEachAttributes());
			TemplateNode contentNode = parseTemplateContent();
			
			return new ForeachStatement(attrs.varName, attrs.expr, attrs.attributes, contentNode);
		}
		
		else if (IF_ELEMENT.equals(operationName)) {
			IfAttributes attrs;
			parseAttributes(attrs = new IfAttributes());
			if (attrs.condition == null) {
				throw new RuntimeException("Missing condition on if-statement.");
			}
			IfStatement result = new IfStatement(attrs.condition, attrs.attributes);
			
			parseIfParts(result);
			
			return result;
		}
		
		else if (DEFINE_ELEMENT.equals(operationName)) {
			DefineAttributes attrs;
			parseAttributes(attrs = new DefineAttributes());
			
			DefineStatement result = new DefineStatement(attrs.var, attrs.expr, attrs.attributes);
			
			XMLStreamUtil.nextEvent(in);
			XMLStreamUtil.skipWhitespaceAndComments(in);
			if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
				throw new ParseException("No content expected for '" + DEFINE_ELEMENT + "'.");
			}

			return result;
		}

		else if (TEMPLATE_ELEMENT.equals(operationName)) {
			parseAttributes(new NoAttributes());
			
			List<TemplateNode> contentNodes = parseTemplateContentNodes();
			Template result = new Template(contentNodes);
			
			return result;
		}
		
		else if (INVOKE_ELEMENT.equals(operationName)) {
			InvokeAttributes attributes = new InvokeAttributes(_templateSource);
			parseAttributes(attributes);
			TemplateSource templateSource = attributes.getTemplateSource();
			TemplateFormat templateFormat = attributes.getTemplateFormat();
			InvokeStatement result = new InvokeStatement(templateSource, templateFormat);
			parseInvoke(result);
			return result;
		}

		throw new XMLStreamException("Unsupported template element '" + operationName + "'.");
	}

	private void parseIfParts(IfStatement result) throws XMLStreamException, IOException, ParseException {
		skipWhitespaceAndCommentsToNextTemplateTag();
		checkIsAtStartTag();
		if (THEN_ELEMENT.equals(in.getLocalName())) {
			result.setThenStm(parseTemplateContent());
			skipWhitespaceAndCommentsToNextTemplateTag();
			if (ELSE_ELEMENT.equals(in.getLocalName())) {
				result.setElseStm(parseTemplateContent());
				skipWhitespaceAndCommentsToNextTemplateTag();
			}
		} else if (ELSE_ELEMENT.equals(in.getLocalName())) {
			result.setElseStm(parseTemplateContent());
			skipWhitespaceAndCommentsToNextTemplateTag();
		} else {
			throw new ParseException("Unexpected part of '" + IF_ELEMENT + "': " + in.getLocalName());
		}
		checkIsAtEndTag();
	}

	private void skipWhitespaceAndCommentsToNextTemplateTag() {
		XMLStreamUtil.nextEvent(in);
		XMLStreamUtil.skipWhitespaceAndComments(in);
		checkIsAtTag();
		checkIsInTemplateNamespace();
	}

	private void parseInvoke(InvokeStatement result) {
		XMLStreamUtil.nextEvent(in);
		XMLStreamUtil.skipWhitespaceAndComments(in);
		parseInvokeStructure(result.getParameterValues(), true);
	}

	private void parseInvokeStructure(Map<String, ParameterValue<TemplateNode>> resultMap, boolean isAtParameterLevel) {
		// current event: <parameter> or <attribute>
		while (!XMLStreamUtil.isAtEndTag(in)) {
			checkIsAtStartTag();
			checkIsParameterOrAttributeTag(isAtParameterLevel);
			parseInvokeStructureEntry(resultMap);
			// current event: </parameter> or </attribute>
			checkIsAtEndTag();
			checkIsInTemplateNamespace();
			XMLStreamUtil.nextEvent(in);
			XMLStreamUtil.skipWhitespaceAndComments(in);
		}
		// current event: </invoke>, </parameter>, </attribute> or </entry>
	}

	private void checkIsParameterOrAttributeTag(boolean isAtParameterLevel) {
		checkIsInTemplateNamespace();
		if (isAtParameterLevel) {
			if (StringServices.equals(in.getLocalName(), PARAMETER_ELEMENT)) {
				return;
			}
		} else {
			if (StringServices.equals(in.getLocalName(), ATTRIBUTE_ELEMENT)) {
				return;
			}
		}
		throw new RuntimeException("Expected tags <" + PARAMETER_ELEMENT + ">, <" + ATTRIBUTE_ELEMENT + "> or </"
			+ INVOKE_ELEMENT + "> but found: " + in.getLocalName());
	}

	private void checkIsParameterOrAttributeOrEntryTag() {
		checkIsInTemplateNamespace();
		if (StringServices.equals(in.getLocalName(), PARAMETER_ELEMENT)) {
			return;
		}
		if (StringServices.equals(in.getLocalName(), ATTRIBUTE_ELEMENT)) {
			return;
		}
		if (StringServices.equals(in.getLocalName(), ENTRY_ELEMENT)) {
			return;
		}
		throw new RuntimeException("Expected tags '" + PARAMETER_ELEMENT + "' or '" + ATTRIBUTE_ELEMENT
			+ "' but found: " + in.getLocalName());
	}

	private void parseInvokeStructureEntry(Map<String, ParameterValue<TemplateNode>> resultMap) {
		// current event: <parameter> or <attribute> or <entry>
		String attributeName = in.getAttributeValue(null, NAME_ATTRIBUTE);
		ParameterValue<TemplateNode> attributeValue = parseInvokeValue();
		resultMap.put(attributeName, attributeValue);
		// current event: </parameter> or </attribute> or </entry>
	}

	private ParameterValue<TemplateNode> parseInvokeValue() {
		// current event: <parameter> or <attribute> or <entry>
		String valueFromAttribute = in.getAttributeValue(null, VALUE_ATTRIBUTE);
		if (valueFromAttribute == null) {
			return getInvokeValueFromTag();
		} else {
			ParameterValue<TemplateNode> attributeValue =
				new PrimitiveParameterValue<>(parseAttributeValue(valueFromAttribute, VALUE_ATTRIBUTE));
			XMLStreamUtil.nextEvent(in);
			XMLStreamUtil.skipWhitespaceAndComments(in);
			return attributeValue;
		}
		// current event: </parameter> or </attribute> or </entry>
	}

	private ParameterValue<TemplateNode> getInvokeValueFromTag() {
		try {
			// current event: <parameter> or <attribute> or <entry>
			XMLStreamUtil.nextEvent(in);
			XMLStreamUtil.skipWhitespaceAndComments(in);
			checkIsAtStartTag();
			checkIsInTemplateNamespace();
			ParameterValue<TemplateNode> result;
			if (in.getLocalName().equals(VALUE_ELEMENT)) {
				TemplateNode valueNode = parseTemplateContent();
				result = new PrimitiveParameterValue<>(valueNode);
				checkIsTemplateValueEndTag();
				XMLStreamUtil.nextEvent(in);
				XMLStreamUtil.skipWhitespaceAndComments(in);
			} else if (in.getLocalName().equals(ATTRIBUTE_ELEMENT)) {
				Map<String, ParameterValue<TemplateNode>> resultMap =
					new HashMap<>();
				parseInvokeStructure(resultMap, false);
				result = new StructuredParameterValue<>(resultMap);
			} else if (in.getLocalName().equals(ENTRY_ELEMENT)) {
				List<ParameterValue<TemplateNode>> entryTemplates = new ArrayList<>();
				do {
					entryTemplates.add(parseInvokeValue());
					checkIsTemplateEntryEndTag();
					XMLStreamUtil.nextEvent(in);
					XMLStreamUtil.skipWhitespaceAndComments(in);
				} while (XMLStreamUtil.isAtStartTag(in) && in.getLocalName().equals(ENTRY_ELEMENT));
				result = new ListParameterValue<>(entryTemplates);
			} else {
				throw new RuntimeException("Expected tags '" + VALUE_ELEMENT + "' or '" + ATTRIBUTE_ELEMENT
					+ "' but found tag '" + in.getLocalName() + "'.");
			}
			checkIsAtEndTag();
			checkIsParameterOrAttributeOrEntryTag();
			// current event: </parameter> or </attribute> or <entry>
			return result;
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void checkIsTemplateValueEndTag() {
		checkIsAtEndTag();
		checkIsInTemplateNamespace();
		if (!StringServices.equals(in.getLocalName(), VALUE_ELEMENT)) {
			throw new RuntimeException("Expected tag </" + VALUE_ELEMENT + "> but found: " + in.getLocalName());
		}
	}

	private TemplateNode parseTemplateContent() throws XMLStreamException, IOException, ParseException {
		List<TemplateNode> innderNodes = parseTemplateContentNodes();
		
		return innderNodes.size() == 1 ? innderNodes.get(0) : new Template(innderNodes);
	}

	private List<TemplateNode> parseTemplateContentNodes() throws XMLStreamException, IOException,
			ParseException {
		List<TemplateNode> innderNodes = new ArrayList<>();
		parseContent(innderNodes, true);
		return innderNodes;
	}

	private void parseAttributes(AttributeParser attributeParser) throws ParseException {
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			attributeParser.parseAttribute(XMLStreamUtil.getAttributeNamespace(in, n), in.getAttributePrefix(n), in.getAttributeLocalName(n), in.getAttributeValue(n));
		}
	}

	private void flushTagsAndBuffer(List<TemplateNode> nodes) {
		flushTag();
		flushBuffer(nodes);
	}

	private void flushBuffer(List<TemplateNode> nodes) {
		StringBuffer internalBuffer = buffer.getBuffer();
		if (internalBuffer.length() > 0) {
			nodes.add(new LiteralText(internalBuffer.toString()));
			internalBuffer.setLength(0);
		}
	}

	private void beginTag(String tagName) {
		flushTag();
		serializer.beginBeginTag(tagName);
		this.tagOpen = true;
		this.tagOpenPos = in.getLocation().getCharacterOffset();
	}

	private void flushTag() {
		if (tagOpen) {
			serializer.endBeginTag();
			tagOpen = false;
		}
	}

	private void endTag(String tagName) {
		if (tagOpen) {
			if (in.getLocation().getCharacterOffset() > tagOpenPos) {
				serializer.endBeginTag();
				serializer.endTag(tagName);
			} else {
				serializer.endEmptyTag();
			}
			tagOpen = false;
		} else {
			serializer.endTag(tagName);
		}
	}

	private String getAttributeQualifiedName(int n) {
		String prefix = in.getAttributePrefix(n);
		if ( ! StringServices.isEmpty(prefix)) {
			return prefix + ":" + in.getAttributeLocalName(n);
		} else {
			return in.getAttributeLocalName(n);
		}
	}

	private String getQualifiedName() {
		String prefix = in.getPrefix();
		if ( ! StringServices.isEmpty(prefix)) {
			return prefix + ":" + in.getLocalName();
		} else {
			return in.getLocalName();
		}
	}

	Expression parseExpression(String value) {
		try {
			TemplateParser parser = new TemplateParser(new StringReader(value));
			parser.token_source.SwitchTo(SYNTAX_MODE);
			Expression result = parser.Expression();
			return result;
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
