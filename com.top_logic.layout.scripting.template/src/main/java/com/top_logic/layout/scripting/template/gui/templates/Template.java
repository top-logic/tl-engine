/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter.State;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.template.TemplateParseResult;
import com.top_logic.template.TemplateTypes;
import com.top_logic.template.model.TemplateDataObject;
import com.top_logic.template.model.TemplateMOAttribute;
import com.top_logic.template.xml.ConfigurableExpansionModel;
import com.top_logic.template.xml.TemplateSchema;
import com.top_logic.template.xml.TemplateXMLParser;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * Representation of a parsed template.
 * 
 * @see TemplateParseResult
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Template {

	private final MOStructure _type;

	private final ConfigurableExpansionModel _expansionModel;

	private final TemplateSource _source;

	private TemplateDataObject _parameterData;

	/**
	 * Creates a {@link Template}.
	 * 
	 * @param source
	 *        See {@link #getSource()}.
	 * @param parseResult
	 *        The {@link TemplateParseResult} with parser data.
	 */
	public Template(TemplateSource source, TemplateParseResult parseResult) {
		DefaultTypeSystem types = new DefaultTypeSystem(parseResult.getTypes());
		MOStructure type;
		try {
			type = (MOStructure) types.getType(TemplateTypes.IMPLICIT_THIS_TYPE);
		} catch (UnknownTypeException ex) {
			throw new UnreachableAssertion(ex);
		}

		Map<String, Object> fakeValues = new HashMap<>();
		for (MOAttribute attribute : type.getAttributes()) {
			if (!((TemplateMOAttribute) attribute).hasDefaultValue()) {
				String fakeValue;
				if (attribute.isMandatory()) {
					fakeValue = "NONE";
				} else {
					fakeValue = null;
				}
				fakeValues.put(attribute.getName(), fakeValue);
			}
		}
	
		ConfigurableExpansionModel expansionModel =
			new ConfigurableExpansionModel(parseResult, fakeValues);

		_source = source;
		_type = type;
		_expansionModel = expansionModel;
		_parameterData = new TemplateDataObject(type);
	}

	/**
	 * Loads a {@link Template} from the given {@link TemplateSource}.
	 * 
	 * @param source
	 *        The {@link TemplateSource} to load.
	 * @return The parsed template.
	 */
	public static Template load(TemplateSource source) throws IOException, XMLStreamException {
		TemplateParseResult parseResult = new TemplateXMLParser().parse(source);
		return new Template(source, parseResult);
	}

	/**
	 * The type of the data this {@link Template} operates on.
	 * 
	 * @see TemplateTypes#IMPLICIT_THIS_TYPE
	 */
	public MOStructure getType() {
		return _type;
	}

	/**
	 * The parameters of this {@link Template}.
	 * 
	 * @see #getType()
	 */
	public Iterable<? extends MOAttribute> getParameters() {
		return _type.getAttributes();
	}

	/**
	 * The {@link TemplateSource} this {@link Template} was parsed from.
	 */
	public TemplateSource getSource() {
		return _source;
	}

	/**
	 * The default value of the given parameter.
	 */
	public Object getDefaultValue(MOAttribute attribute) {
		return _parameterData.getValue(attribute, _expansionModel);
	}

	/**
	 * Loads the template content (excluding the parameter declarations) as String.
	 * 
	 * @param formatConfig
	 *        How to format the template source.
	 * @return The formatted template body.
	 * @throws IOException
	 *         If reading the {@link TemplateSource} fails.
	 * @throws SAXException
	 *         If parsing the template source fails due to invalid content.
	 */
	public String getContent(XMLPrettyPrinter.Config formatConfig) throws IOException, SAXException {
		StringWriter buffer = new StringWriter();
		XMLPrettyPrinter printer = new XMLPrettyPrinter(buffer, formatConfig);
		printer.getOut().setState(State.ELEMENT_CONTENT);

		InputStream in = _source.getContent();
		try {
			Document document = DOMUtil.getDocumentBuilder().parse(in);

			for (Element element : DOMUtil.elementsNS(document.getDocumentElement(), TemplateSchema.TEMPLATE_NS,
				TemplateSchema.BODY_ELEMENT)) {
				for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
					printer.write(node);
				}
			}
		} finally {
			in.close();
		}
		return buffer.toString();
	}

}