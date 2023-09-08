/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import static com.top_logic.layout.processor.LayoutModelUtils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.processor.Expansion.Buffer;
import com.top_logic.layout.processor.Expansion.NodeBuffer;

/**
 * {@link Operation} that inlines all layout references within a layout definition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutInline extends Operation implements LayoutModelConstants, NodeProcessor {

	private final LayoutResolver _resolver;

	private Collection<BinaryData> _usedData = new HashSet<>();

	private boolean _addInlinedAnnotation = true;

	public LayoutInline(LayoutResolver resolver) {
		super(resolver.getProtocol(), resolver.getApplication());

		_resolver = resolver;
	}

	/**
	 * Whether to add the {@link LayoutModelConstants#INLINED_ANNOTATION_ATTR} attribute to the
	 * generated definition.
	 */
	public void setInlinedAnnotation(boolean inlinedAnnotation) {
		_addInlinedAnnotation = inlinedAnnotation;
	}

	/**
	 * Executes the inline process for the given {@link LayoutDefinition} without arguments.
	 */
	public void inline(LayoutDefinition layout) throws ResolveFailure {
		Map<String, ParameterValue> arguments = Collections.emptyMap();
		inline(layout, arguments);
	}

	/**
	 * Executes the inline process for the given {@link LayoutDefinition} with the given arguments.
	 */
	public void inline(LayoutDefinition layout, Map<String, ParameterValue> arguments) throws ResolveFailure {
		_usedData = new HashSet<>();
		addUsedFiles(layout);
		Document document = layout.getLayoutDocument();
		Comment comment = document.createComment(" Generated from '" + layout.getLayoutData().toString() + "' ");
		document.insertBefore(comment, document.getDocumentElement());

		processReferences(layout, arguments);
		checkErrors();
		
		if (_addInlinedAnnotation) {
			LayoutModelUtils.setInlinedAnnotation(document);
		}

		/* Bugfix for Xalan (version 2.7.1): The annotated attributes are created with
		 * Element.createAttributeNS(<ns>,<localName>,<value>), as the prefix does not care. But
		 * Xalan Transformer ignores namespaces if no qualified name (<prefix>:<localName>) is
		 * given. Normalising document transforms local names to qualified names. */
		DOMConfiguration docConfig = document.getDomConfig();
		docConfig.setParameter("infoset", Boolean.TRUE);
		/* Do not replace CDATA sections, by ordinary text nodes. */
		docConfig.setParameter("cdata-sections", Boolean.TRUE);
		document.normalizeDocument();
	}

	private void processReferences(LayoutDefinition layout, Map<String, ParameterValue> arguments) throws ResolveFailure {
		Element targetElement = layout.getLayoutDocument().getDocumentElement();
		TemplateLayout template = new TemplateLayout(layout, targetElement);
		Expansion contextExpander =
			Expansion.newInstance(getProtocol(), getApplication(), layout, this);
		Expansion expansion = template.createContentExpander(contextExpander, null, arguments, this);
		Buffer out = replaceWithPointer(targetElement);
		try {
			expansion.expand(out, template.getContent());
		} catch (ElementInAttributeContextError ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	static NodeBuffer replaceWithPointer(Node node) {
		Node parent = node.getParentNode();
		Node before = node.getNextSibling();
		parent.removeChild(node);

		return new NodeBuffer(parent, before);
	}

	/**
	 * Returns the collection of files which are used to inline the {@link LayoutDefinition} given
	 * the last call of {@link #inline(LayoutDefinition)}.
	 */
	public Collection<BinaryData> getFiles() {
		return _usedData;
	}

	@Override
	public List<? extends Node> expandReference(Element reference, Expansion contextExpansion, Buffer out)
			throws ElementInAttributeContextError {
		String referencedLayoutFileName = getReferenceName(reference, contextExpansion);
		ConstantLayout referencedLayout;
		try {
			referencedLayout = loadTemplate(referencedLayoutFileName);
		} catch (ResolveFailure ex) {
			error("Referenced layout not found in '" + contextExpansion.getTemplate().getLayoutName() + "': "
				+ referencedLayoutFileName, ex);
			return Collections.emptyList();
		}

		Map<String, ParameterValue> arguments = getArguments(reference, contextExpansion);
		String includeId = getIncludeId(reference);
		TemplateLayout template = new TemplateLayout(referencedLayout);
		Expansion contentExpander = template.createContentExpander(contextExpansion, includeId, arguments, this);

		List<? extends Node> expansions = contentExpander.expandTemplate(out, template.getContent());
		if (expansions == Expansion.NO_NODE_EXPANSION) {
			return Expansion.NO_NODE_EXPANSION;
		}

		if (!expansions.isEmpty()) {
			Node insertedNode = expansions.get(0);
			assert insertedNode != null;
			Node firstResultChild = insertedNode.getFirstChild(); // may be null
			injectContents(reference, contentExpander.getArgumentExpansion(),
				new NodeBuffer(insertedNode, firstResultChild));
		}

		return expansions;
	}

	private ConstantLayout loadTemplate(String referenceName) throws ResolveFailure {
		ConstantLayout referencedLayout = _resolver.getLayout(referenceName);
		addUsedFiles(referencedLayout);
		return referencedLayout;
	}

	private void injectContents(Element reference, Expansion expander, Buffer out) throws ElementInAttributeContextError {
		// Copy reference injection contents into the expanded result. This moves all contents of
		// inject elements into the expansion.
		Node next;
		for (Node inject = reference.getFirstChild(); inject != null; inject = next) {
			next = inject.getNextSibling();

			if (DOMUtil.isElement(null, LayoutModelConstants.INJECT_ELEMENT, inject)) {
				for (Node child : DOMUtil.children(inject)) {
					expander.expandNode(child, out);
				}
			}
		}
	}

	private void addUsedFiles(LayoutDefinition layoutDefinition) {
		_usedData.add(layoutDefinition.getLayoutData());
	}

}