/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentDefinition.UnsupportedFormatException;
import com.top_logic.mig.html.layout.LayoutVariables;

/**
 * {@link Operation} that expands a single template reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Expansion extends Operation {

	/**
	 * {@link Property} holding the {@link TemplateCallProcessor} to use when visiting a
	 * {@link LayoutModelUtils#isTemplateCall(Node) template-call} node.
	 */
	public static final Property<TemplateCallProcessor> PROCESSOR =
		TypedAnnotatable.property(TemplateCallProcessor.class, "processor");

	/**
	 * Constant returned by expand methods when the expanded text (or attribute value) contains an
	 * optional parameter for which no value was set.
	 */
	public static final List<Node> NO_NODE_EXPANSION = null;

	private static final Pattern VARIABLE_REFERENCE_PATTERN = Pattern.compile(
		"(?:" +
			"\\Q" + LayoutModelConstants.VARIABLE_PREFIX + "\\E" +
			"(" + TemplateLayout.VARIABLE_NAME_PATTERN_SRC + ")" +
			"\\Q" + LayoutModelConstants.VARIABLE_SUFFIX + "\\E" +
		")" + "|" + "(?:" + 
			"\\Q" + LayoutModelConstants.PARAMETER_PREFIX + "\\E" +
			"(" + 
				"(?:" + "(" + "[^\\}]+" + ")" + ":" + ")?" +
				TemplateLayout.VARIABLE_NAME_PATTERN_SRC + 
			")" +
			"\\Q" + LayoutModelConstants.PARAMETER_SUFFIX + "\\E" +
		")");

	private static final Map<String, ? extends ParameterValue> NO_ARGUMENTS = Collections.emptyMap();

	private static final Map<String, ? extends ParameterValue> NO_PARAMETERS = null;

	private final Map<String, ? extends ParameterValue> _variables;

	private final Map<String, ? extends ParameterValue> _parameters;

	private final Set<String> _expandingVariables;

	private final Set<String> _expandingParams;

	private final NodeProcessor _callback;

	private final LayoutDefinition _template;

	private final Expansion _argumentExpansion;

	private final Expansion _context;

	public static Expansion newInstance(Protocol protocol, Application application, LayoutDefinition template,
			NodeProcessor callback) {
		return new Expansion(protocol, application, template, NO_ARGUMENTS, NO_PARAMETERS, null, null, callback);
	}

	public static Expansion newInstance(Protocol protocol, Application application, LayoutDefinition template,
		Map<String, ? extends ParameterValue> declaredParameters, String includeId,
		Map<String, ? extends ParameterValue> arguments, Expansion contextExpansion, NodeProcessor callback) {

		// Build concrete argument map.
		HashMap<String, ParameterValue> allArguments = new HashMap<>(declaredParameters);

		for (Entry<String, ? extends ParameterValue> argumentsEntry : arguments.entrySet()) {
			ParameterValue argument = argumentsEntry.getValue();
			ParameterValue clash = allArguments.put(argumentsEntry.getKey(), argument);
			if (clash != null) {
				argument.setFallback(clash);
			}
		}

		Expansion argumentExpansion;
		if (contextExpansion != null) {
			argumentExpansion = contextExpansion.createArgumentExpansion(includeId, allArguments);

			for (ParameterValue argument : arguments.values()) {
				argument.initExpansion(argumentExpansion);
			}
		} else {
			argumentExpansion = null;
		}

		Expansion result =
			new Expansion(protocol, application, template, allArguments, NO_PARAMETERS, argumentExpansion,
			contextExpansion, callback);

		if (contextExpansion == null) {
			for (ParameterValue argument : arguments.values()) {
				argument.initExpansion(result);
			}
		}

		for (ParameterValue argument : declaredParameters.values()) {
			argument.initExpansion(result);
		}

		return result;
	}

	private Expansion(Protocol protocol, Application application, LayoutDefinition template,
			Map<String, ? extends ParameterValue> variables, Map<String, ? extends ParameterValue> parameters,
			Expansion argumentExpansion, Expansion contextExpansion, NodeProcessor callback) {
		super(protocol, application);

		_template = template;
		_argumentExpansion = argumentExpansion;
		_context = contextExpansion;
		_expandingVariables = new HashSet<>();
		_expandingParams = new HashSet<>();
		_callback = callback;
		_variables = variables;
		_parameters = parameters;

		if (containsUndefined(_variables.values())) {
			Collection<String> undefinedParameters = getUndefined(_variables);

			error("No argument for mandatory parameters " + undefinedParameters + " in reference '"
				+ template.getLayoutName() + "'.");
		}
	}

	public LayoutDefinition getTemplate() {
		return _template;
	}

	private static boolean containsUndefined(Collection<? extends ParameterValue> values) {
		for (ParameterValue value : values) {
			if (!value.isDefined()) {
				return true;
			}
		}
		return false;
	}

	private static Collection<String> getUndefined(Map<String, ? extends ParameterValue> values) {
		ArrayList<String> undefinedParameters = new ArrayList<>();
		for (Entry<String, ? extends ParameterValue> entry : values.entrySet()) {
			if (entry.getValue() instanceof UndefinedValue) {
				undefinedParameters.add(entry.getKey());
			}
		}
		return undefinedParameters;
	}

	public Expansion getArgumentExpansion() {
		if (_argumentExpansion == null) {
			return this;
		}
		return _argumentExpansion;
	}

	private Expansion createArgumentExpansion(String includeId, Map<String, ParameterValue> arguments) {
		Map<String, ParameterValue> parameters = new HashMap<>();

		// Inherited qualified bindings.
		if (_parameters != null) {
			for (Entry<String, ? extends ParameterValue> entry : _parameters.entrySet()) {
				if (entry.getKey().indexOf(':') < 0) {
					continue;
				}
				parameters.put(entry.getKey(), entry.getValue());
			}
		}

		// New local bindings.
		parameters.putAll(arguments);

		if (includeId != null) {
			// Qualified bindings for the local scope.
			for (Entry<String, ? extends ParameterValue> entry : arguments.entrySet()) {
				parameters.put(includeId + ":" + entry.getKey(), entry.getValue());
			}
		}

		Expansion result =
			new Expansion(getProtocol(), getApplication(), _template, _variables, parameters, null, _context,
				_callback);

		return result;
	}

	@Override
	protected BinaryData getResource() {
		return _template.getLayoutData();
	}

	public Map<String, ? extends ParameterValue> getArguments() {
		return _variables;
	}

	public List<? extends Node> expandNode(Node templateNode, Buffer out) throws ElementInAttributeContextError {
		switch (templateNode.getNodeType()) {
			case Node.ELEMENT_NODE:
				return expandElement((Element) templateNode, out);
			case Node.TEXT_NODE: {
				String content = templateNode.getNodeValue();
				if (!content.trim().isEmpty()) {
					return expandTextContents(content, out);
				}
				return Collections.emptyList();
			}
			case Node.CDATA_SECTION_NODE: {
				String content = templateNode.getNodeValue();
				return Collections.singletonList(out.appendCData(content));
			}
			default:
				return Collections.emptyList();
		}
	}

	public List<? extends Node> expandElement(Element templateElement, Buffer out) throws ElementInAttributeContextError {
		if (LayoutModelUtils.isTemplate(templateElement)) {
			// Inline template.
			return expandTemplateElement(templateElement, out);
		} else if (LayoutModelUtils.isTemplateCall(templateElement)) {
			InteractionContext currentInteraction = ThreadContextManager.getInteraction();
			if (currentInteraction != null) {
				TemplateCallProcessor processor = currentInteraction.get(PROCESSOR);
				if (processor != null) {
					return processor.expandElement(this, templateElement, out);
				}
			}
			// Inline typed template.
			return expandTemplateCall(templateElement, out);
		} else if (_callback != null && isReferenceElement(templateElement)) {
			List<? extends Node> expanded = _callback.expandReference(templateElement, this, out);
			enhanceDefinitionFiles(templateElement, expanded);
			return expanded;
		} else {
			Element destParent = out.appendElement(templateElement);

			List<? extends Node> expandedContent = expandContent(templateElement, new NodeBuffer(destParent, null));
			if (expandedContent == Expansion.NO_NODE_EXPANSION) {
				destParent.getParentNode().removeChild(destParent);
				return Collections.emptyList();
			}
			return Collections.singletonList(destParent);
		}
	}

	private List<? extends Node> expandTemplateCall(Element templateCall, Buffer out)
			throws ElementInAttributeContextError {
		Optional<DynamicComponentDefinition> componentDefinition = getTemplateComponentDefinition(templateCall);
		if (componentDefinition.isEmpty()) {
			return Collections.emptyList();
		}
		DynamicComponentDefinition definition = componentDefinition.get();
		Optional<ConfigurationItem> arguments = createArguments(templateCall, definition);

		if (arguments.isPresent()) {
			Map<String, ParameterValue> inlineArguments = new HashMap<>();
			Document templateDocument =
				definition.createComponentTemplate(getProtocol(), arguments.get(), inlineArguments);
			return expandTemplateElement(templateDocument.getDocumentElement(), out, inlineArguments);
		}

		return Collections.emptyList();
	}

	/**
	 * Determines the arguments for the given {@link DynamicComponentDefinition} from the given
	 * {@link LayoutModelConstants#TEMPLATE_CALL_ELEMENT template call} node.
	 * 
	 * @param templateCall
	 *        Node containing the arguments for the given {@link DynamicComponentDefinition}.
	 * @param componentDefinition
	 *        The {@link DynamicComponentDefinition} defining the configuration properties.
	 * 
	 * @return Argument node to instantiate the given {@link DynamicComponentDefinition}. May be
	 *         {@link Optional#isEmpty() empty} in case of errors.
	 */
	public Optional<ConfigurationItem> createArguments(Element templateCall,
			DynamicComponentDefinition componentDefinition) throws ElementInAttributeContextError {
		Optional<Element> argumentNode = getArgumentNode(templateCall);
		if (argumentNode.isEmpty()) {
			return Optional.empty();
		}
		Element argNode = argumentNode.get();

		Element expandedArgNode = argNode.getOwnerDocument().createElementNS(null, argNode.getLocalName());
		expandContent(argNode, new NodeBuffer(expandedArgNode, null));

		BinaryContent content = getTemplateArgumentContent(expandedArgNode);
		try {
			return Optional.of(createArguments(argNode.getLocalName(), componentDefinition, content));
		} catch (ConfigurationException ex) {
			error("Unable to create configuration from arguments.", ex);
			return Optional.empty();
		}
	}

	/**
	 * Determines the {@link DynamicComponentDefinition} of the template that is called by the given
	 * {@link LayoutModelConstants#TEMPLATE_CALL_ELEMENT template call} node.
	 * 
	 * @param templateCall
	 *        Node determining the template to call.
	 * @return Definition of the component to instantiate. May be {@link Optional#isEmpty() empty}
	 *         when the {@link DynamicComponentDefinition} may not be created for some reason.
	 */
	public Optional<DynamicComponentDefinition> getTemplateComponentDefinition(Element templateCall) {
		Optional<String> templateName = getTemplateName(templateCall);
		Optional<BinaryData> templateData = readTemplateData(templateName);
		return getTemplateComponentDefinition(templateData);
	}

	private BinaryContent getTemplateArgumentContent(Element argumentsNode) {
		return DOMUtil.toBinaryContent(argumentsNode,
			getResource().getName() + "#" + LayoutModelConstants.TEMPLATE_CALL_ARGUMENTS_ELEMENT);
	}

	private Optional<DynamicComponentDefinition> getTemplateComponentDefinition(Optional<BinaryData> templateFile) {
		try {
			if (templateFile.isPresent()) {
				return Optional.ofNullable(DynamicComponentDefinition.parseTemplate(getProtocol(), templateFile.get()));
			}
		} catch (IOException ex) {
			error("Error reading file " + templateFile, ex);
		} catch (UnsupportedFormatException ex) {
			error("Not a dynamic layout template:" + templateFile, ex);
		}
		return Optional.empty();
	}

	private Optional<String> getTemplateName(Element templateCall) {
		String templateName = templateCall.getAttribute(LayoutModelConstants.TEMPLATE_CALL_TEMPLATE_ATTR);
		if (StringServices.isEmpty(templateName)) {
			error("No template name given in " + templateCall);
			return Optional.empty();
		}
		return Optional.of(templateName);
	}

	private Optional<Element> getArgumentNode(Element templateCall) {
		Iterable<Element> elements = DOMUtil.elements(templateCall);

		Iterator<Element> children = elements.iterator();
		if (!children.hasNext()) {
			error("No arguments in " + templateCall);
			return Optional.empty();
		}

		return Optional.of(children.next());
	}

	private Optional<BinaryData> readTemplateData(Optional<String> templateName) {
		if (templateName.isPresent()) {
			return Optional.ofNullable(templateData(templateName.get()));
		}

		return Optional.empty();
	}

	private ConfigurationItem createArguments(String definitionsLocalName, DynamicComponentDefinition definition,
			BinaryContent content) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(new DefaultInstantiationContext(getProtocol()),
			Collections.singletonMap(definitionsLocalName, definition.descriptor()));
		reader.setVariableExpander(
				LayoutVariables.layoutExpander(_template.getResolver().getTheme(), _template.getLayoutName()));
		reader.setSource(content);
		return reader.read();
	}

	private BinaryData templateData(String templateName) {
		return FileManager.getInstance().getData(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + templateName);
	}

	private List<? extends Node> expandTemplateElement(Element templateElement, Buffer out)
			throws ElementInAttributeContextError {
		return expandTemplateElement(templateElement, out, Collections.emptyMap());
	}

	private List<? extends Node> expandTemplateElement(Element templateElement, Buffer out,
			Map<String, ? extends ParameterValue> arguments) throws ElementInAttributeContextError {
		TemplateLayout template = new TemplateLayout(_template, templateElement);
		Expansion contentExpander = template.createContentExpander(this, null, arguments, _callback);
		return contentExpander.expand(out, template.getContent());
	}

	private boolean isReferenceElement(Element templateElement) {
		if (!DOMUtil.isElement(null, templateElement)) {
			return false;
		}
		if (LayoutModelUtils.isInclude(templateElement)) {
			return true;
		}
		return false;
	}

	private void enhanceDefinitionFiles(Element templateElement, List<? extends Node> expanded) {
		Node parent = templateElement;
		if (parent instanceof Element) {
			LayoutModelUtils.enhanceDefinitionFiles((Element) parent, expanded);
		}
	}

	void expandAttributes(Element templateElement, Element expansion) {
		NamedNodeMap attributes = templateElement.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			Attr attribute = (Attr) attributes.item(n);
			if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())) {
				continue;
			}
			expandAttribute(attribute, expansion);
		}
	}

	private void expandAttribute(Attr templateAttribute, Element targetElement) {
		String attributeName = templateAttribute.getLocalName();
		String value = expandString(targetElement, attributeName, templateAttribute.getValue());
		if (value == null) {
			return;
		}

		targetElement.setAttributeNS(templateAttribute.getNamespaceURI(), attributeName, value);
	}

	/**
	 * Expands the given template expression in attribute context.
	 * 
	 * @param targetElement
	 *        The target element whose attribute is created.
	 * @param attributeName
	 *        The attribute that should be filled with the expansion.
	 * @param templateValue
	 *        The template expression value to expand.
	 * @return The created string value
	 */
	public String expandString(Element targetElement, String attributeName, String templateValue) {
		String value;
		try {
			TextBuffer buffer = new TextBuffer(targetElement, attributeName);
			List<? extends Node> expanded = expandTextContents(templateValue, buffer);
			if (expanded == Expansion.NO_NODE_EXPANSION) {
				return null;
			}
			value = buffer.getTextValue();
		} catch (ElementInAttributeContextError ex) {
			error("Attribute value '" + templateValue + "' of attribute '" + attributeName
				+ "' expands to node contents.", ex);
			value = "???node-content???";
		}
		return value;
	}

	/**
	 * List of created and inserted {@link Element}.
	 */
	protected List<? extends Node> expandContent(Element templateElement, Buffer out) throws ElementInAttributeContextError {
		return expandRange(templateElement, false, out);
	}

	public List<? extends Node> expandRange(Element rangeParent, boolean contentOnly, Buffer out) throws ElementInAttributeContextError {
		if (!contentOnly) {
			Element parent = out.getParent();
			if (parent != null) {
				expandAttributes(rangeParent, parent);
			}
		}
		Iterable<? extends Node> templateChildren = DOMUtil.children(rangeParent);
		List<Node> createdElements = new ArrayList<>();
		for (Node child : templateChildren) {
			switch (child.getNodeType()) {
				case Node.ELEMENT_NODE: {
					List<? extends Node> expandedElement = expandElement((Element) child, out);
					if (expandedElement == Expansion.NO_NODE_EXPANSION) {
						return Expansion.NO_NODE_EXPANSION;
					}
					createdElements.addAll(expandedElement);
					break;
				}
		
				case Node.CDATA_SECTION_NODE: {
					String content = child.getNodeValue();
					createdElements.add(out.appendCData(content));
					break;
				}
				case Node.TEXT_NODE: {
					String content = child.getNodeValue();
					if (content.trim().isEmpty()) {
						continue;
					}
					List<? extends Node> expanded = expandTextContents(content, out);
					if (expanded == Expansion.NO_NODE_EXPANSION) {
						return Expansion.NO_NODE_EXPANSION;
					}
					createdElements.addAll(expanded);
					break;
				}
			}
		}
		return createdElements;
	}

	public abstract static class Buffer {
	
		public abstract Text appendText(String plainText) throws ElementInAttributeContextError;

		/**
		 * Adds the given text wraped into a CData section.
		 */
		public abstract Text appendCData(String plainText) throws ElementInAttributeContextError;
	
		/**
		 * The parent element this buffer appends to.
		 */
		protected abstract Element getParent();

		public Element appendElement(Element templateElement) throws ElementInAttributeContextError {
			return appendElement(templateElement.getNamespaceURI(), templateElement.getLocalName());
		}

		public abstract Element appendElement(String namespaceURI, String localName)
				throws ElementInAttributeContextError;

		public abstract Comment appendComment(String text);

		public abstract Document document();

		public abstract List<? extends Node> appendRange(Element rangeParent, boolean contentOnly, Expansion expansion)
				throws ElementInAttributeContextError;
	
	}

	abstract static class AbstractBuffer extends Buffer {

		@Override
		public List<? extends Node> appendRange(Element rangeParent, boolean contentOnly, Expansion expansion)
				throws ElementInAttributeContextError {
			return expansion.expandRange(rangeParent, contentOnly, this);
		}

		@Override
		public Element appendElement(String namespaceURI, String localName) {
			return append(document().createElementNS(namespaceURI, localName));
		}

		@Override
		public Text appendText(String plainText) {
			return append(document().createTextNode(plainText));
		}

		@Override
		public Text appendCData(String plainText) {
			return append(document().createCDATASection(plainText));
		}

		@Override
		public Comment appendComment(String text) {
			return append(document().createComment(text));
		}

		protected abstract <N extends Node> N append(N node);

	}

	public static class NodeBuffer extends AbstractBuffer {
		private final Node _parent;

		private final Node _before;

		public NodeBuffer(Node parent, Node before) {
			_parent = parent;
			_before = before;
		}

		@Override
		public Element getParent() {
			if (_parent instanceof Element) {
				return (Element) _parent;
			}
			return null;
		}

		@Override
		protected <N extends Node> N append(N node) {
			_parent.insertBefore(node, _before);
			return node;
		}

		@Override
		public Document document() {
			return owner(_parent);
		}

		private Document owner(Node node) {
			if (node.getNodeType() == Node.DOCUMENT_NODE) {
				return (Document) node;
			} else {
				return node.getOwnerDocument();
			}
		}

	}

	/**
	 * A {@link Buffer} used to expand in attribute context.
	 */
	public static class TextBuffer extends Buffer {

		private final StringBuilder _text = new StringBuilder();

		private final Element _parent;

		private boolean _resultElementProduced;

		private String _attributeName;

		private Element _elementResult;

		/**
		 * Creates a {@link Expansion.TextBuffer}.
		 */
		public TextBuffer(Element parent, String attributeName) {
			_parent = parent;
			_attributeName = attributeName;
		}

		@Override
		protected Element getParent() {
			return _elementResult;
		}

		@Override
		public List<? extends Node> appendRange(Element rangeParent, boolean contentOnly, Expansion expansion)
				throws ElementInAttributeContextError {
			if (_parent == null) {
				// Unable to produce a result element.
				throw new ElementInAttributeContextError();
			}

			Document document = _parent.getOwnerDocument();

			if (!_text.toString().trim().isEmpty()) {
				// Cannot produce mixed content.
				throw new ElementInAttributeContextError();
			}

			if (_elementResult != null) {
				// Can only expand a single argument.
				throw new ElementInAttributeContextError();
			}

			// Operated in element contents, therefore, signal that no attribute should be created
			// (no matter whether an actual output was created).
			_resultElementProduced = true;

			Element attributeTemplate = contentOnly ? first(DOMUtil.elements(rangeParent)) : rangeParent;
			if (attributeTemplate != null) {
				Element elementResult = document.createElementNS(null, _attributeName);
				_parent.appendChild(elementResult);
				_elementResult = elementResult;

				if (contentOnly) {
					ArrayList<Node> result = new ArrayList<>();
					for (Element content : DOMUtil.elements(rangeParent)) {
						// Note: only the contents is expanded, therefore, it is OK to expand it
						// with contentOnly=false.
						result.addAll(expansion.expandRange(content, false, this));
					}
					return result;
				} else {
					return expansion.expandRange(rangeParent, contentOnly, this);
				}
			} else {
				return Collections.emptyList();
			}
		}

		private static Element first(Iterable<Element> elements) {
			Iterator<Element> iterator = elements.iterator();
			return iterator.hasNext() ? iterator.next() : null;
		}

		@Override
		public Element appendElement(String namespaceURI, String localName) throws ElementInAttributeContextError {
			if (_elementResult == null) {
				// Can only expand element content, when directly expanding a template argument.
				throw new ElementInAttributeContextError();
			}

			Document document = _parent.getOwnerDocument();

			Element result = document.createElementNS(namespaceURI, localName);
			_elementResult.appendChild(result);

			return result;
		}

		@Override
		public Text appendText(String plainText) throws ElementInAttributeContextError {
			return handleText(plainText);
		}

		@Override
		public Text appendCData(String plainText) throws ElementInAttributeContextError {
			return handleText(plainText);
		}

		private Text handleText(String plainText) {
			if (_resultElementProduced) {
				if (!plainText.trim().isEmpty()) {
					// Mixed content.
					_elementResult.appendChild(_elementResult.getOwnerDocument().createCDATASection(plainText));
				}
			} else {
				_text.append(plainText);
			}
			return null;
		}

		@Override
		public Comment appendComment(String text) {
			// Skip.
			return null;
		}

		/**
		 * The attribute value produced, or <code>null</code>, if element content was created
		 * instead.
		 */
		public String getTextValue() {
			if (_resultElementProduced) {
				// The result was produced directly.
				return null;
			} else {
				return _text.toString();
			}
		}

		@Override
		public Document document() {
			return null;
		}
	}

	public List<? extends Node> expandTextContents(String charContents, Buffer out) throws ElementInAttributeContextError {
		List<Node> result = new ArrayList<>();
		{
			Matcher matcher = VARIABLE_REFERENCE_PATTERN.matcher(charContents);
			int pos = 0;
			while (matcher.find(pos)) {
				int start = matcher.start();
				if (start > pos) {
					result.add(insertText(out, charContents.substring(pos, start)));
				}

				String reference = matcher.group();
				String variableName = matcher.group(1);
				if (variableName != null) {
					boolean legal = _expandingVariables.add(variableName);
					if (legal) {
						try {
							ParameterValue value = _variables.get(variableName);
							if (value == null) {
								error("Access to undefined variable '" + variableName + "'.");
								result.add(insertText(out, reference));
							} else {
								List<? extends Node> expanded = value.expand(out);
								if (expanded == Expansion.NO_NODE_EXPANSION) {
									return Expansion.NO_NODE_EXPANSION;
								}
								result.addAll(expanded);
							}

						} finally {
							// variable is not longer expanded.
							_expandingVariables.remove(variableName);
						}
					} else {
						error("Cyclic variable reference '" + reference + "'.");
						result.add(insertText(out, reference));
					}

				} else {
					String paramName = matcher.group(2);

					boolean legal = _expandingParams.add(paramName);
					if (legal) {
						try {
							if (_parameters == null) {
								error("Not in reference context, must not use parameter cross reference syntax: '"
									+ reference + "'.");
								result.add(insertText(out, reference));
							} else {
								ParameterValue value = _parameters.get(paramName);
								if (value == null) {
									error("Access to undefined include argument '" + paramName + "'.");
									result.add(insertText(out, reference));
								} else {
									List<? extends Node> expanded = value.expand(out);
									if (expanded == Expansion.NO_NODE_EXPANSION) {
										return Expansion.NO_NODE_EXPANSION;
									}
									result.addAll(expanded);
								}
							}
						} finally {
							// parameter is not longer expanded.
							_expandingParams.remove(paramName);
						}
					} else {
						error("Cyclic parameter reference '" + reference + "'.");
						result.add(insertText(out, reference));
					}
				}

				pos = matcher.end();
			}

			if (pos < charContents.length()) {
				result.add(insertText(out, charContents.substring(pos)));
			}
		}
		return result;
	}

	private Node insertText(Buffer out, String plainText) throws ElementInAttributeContextError {
		return out.appendText(plainText);
	}

	public List<? extends Node> expand(Buffer out, Iterable<? extends Node> content)
			throws ElementInAttributeContextError {

		ArrayList<Node> result = new ArrayList<>();
		for (Node node : content) {
			List<? extends Node> expandedNode = expandNode(node, out);
			if (expandedNode == Expansion.NO_NODE_EXPANSION) {
				return Expansion.NO_NODE_EXPANSION;
			}
			result.addAll(expandedNode);
		}
		return result;
	}

	public List<? extends Node> expandTemplate(Buffer out, Iterable<? extends Node> content)
			throws ElementInAttributeContextError {
		String resourcePath = _template.getLayoutData().toString();
		try {
			out.appendComment(" Included from '" + resourcePath + "' ");
			return expand(out, content);
		} catch (DOMException ex) {
			throw new RuntimeException("Failure when processing '" + resourcePath + "'.", ex);
		}
	}
}