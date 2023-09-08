/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;


import static com.top_logic.config.xdiff.model.NodeFactory.*;
import static com.top_logic.config.xdiff.ms.MSXDiffSchema.*;
import static javax.xml.XMLConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.config.xdiff.compare.XDiff;
import com.top_logic.config.xdiff.compare.XDiffSchema;
import com.top_logic.config.xdiff.model.DocumentBuilder;
import com.top_logic.config.xdiff.model.Node;

/**
 * Translator of an {@link XDiff} result from the {@link XDiffSchema} to the {@link MSXDiffSchema}
 * language.
 * 
 * @see #transform(Node, String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XDiffInterpreter implements MSXDiffProvider {

	private final String _component;

	private final MSXDocumentBuilder _builder = MSXDocumentBuilder.INSTANCE;

	private static Node _xdiff;

	/**
	 * Creates a {@link XDiffInterpreter}.
	 * 
	 * @param xdiff
	 *        The {@link XDiff} result to interpret.
	 * @param component
	 *        See e.g. {@link MSXDiffSchema#NODE_ADD__COMPONENT_ATTRIBUTE}.
	 * 
	 * @see #transform(Node, String)
	 */
	public static XDiffInterpreter xdiffInterpreter(Node xdiff, String component) {
		return new XDiffInterpreter(xdiff, component);
	}

	/**
	 * @see #xdiffInterpreter(Node, String)
	 */
	private XDiffInterpreter(Node xdiff, String component) {
		_xdiff = xdiff;
		_component = component;
	}

	/**
	 * Transforms a difference description in {@link XDiffSchema} into {@link MSXDiffSchema}.
	 * 
	 * @param xdiff
	 *        Difference in the {@link XDiffSchema} language.
	 * @param component
	 *        See {@link MSXDiff#getComponent()}.
	 * @return difference description in the {@link MSXDiffSchema} language.
	 */
	public static Document transform(Node xdiff, String component) {
		return xdiffInterpreter(xdiff, component).sythesizeOperations();
	}

	/**
	 * Produces operations of the given {@link ArtifactType}.
	 */
	@Override
	public List<Node> createOperations(ArtifactType type) {
		switch (type) {
			case NODE_DELETE:
				return createNodeDeletes();
			case NODE_ADD:
				return createNodeAdds();
			case NODE_VALUE:
				return createNodeValues();
			case ATTRIBUTE_SET:
				return createAttributeSets();
			default:
				throw new UnreachableAssertion("No such type: " + type);
		}
		
	}
	
	/**
	 * Produces {@link MSXDiffSchema#NODE_DELETE_ELEMENT}s.
	 */
	public List<Node> createNodeDeletes() {
		ArrayList<Node> result = new ArrayList<>();
		syntesizeDeletes(result);
		return result;
	}

	/**
	 * Produces {@link MSXDiffSchema#NODE_ADD_ELEMENT}s.
	 */
	public List<Node> createNodeAdds() {
		ArrayList<Node> result = new ArrayList<>();
		synthsizeAdds(result);
		return result;
	}

	/**
	 * Produces {@link MSXDiffSchema#NODE_VALUE_ELEMENT}s.
	 */
	public List<Node> createNodeValues() {
		ArrayList<Node> result = new ArrayList<>();
		synthsizeNodeValues(result);
		return result;
	}

	/**
	 * Produces {@link MSXDiffSchema#ATTRIBUTE_SET_ELEMENT}s.
	 */
	public List<Node> createAttributeSets() {
		ArrayList<Node> result = new ArrayList<>();
		synthsizeAttributeSets(result);
		return result;
	}

	private Document sythesizeOperations() {
		List<Node> operations = new ArrayList<>();

		syntesizeDeletes(operations);
		synthsizeAdds(operations);
		synthsizeNodeValues(operations);
		synthsizeAttributeSets(operations);

		return DocumentBuilder.convertToDOM(
			document(
				element(null, CONFIGURATION_ELEMENT)
					.setAttributes(
						attr(W3C_XML_SCHEMA_INSTANCE_NS_URI, NO_NAMESPACE_SCHEMA_LOCATION, CUSTOMCONFIG_XSD))
					.setChildren(
						element(null, COMPONENTS_ELEMENT,
							element(null, CONFIG_CUSTOMIZATIONS_ELEMENT,
								element(null, SETTINGS_ELEMENT, operations))))));
	}

	private void syntesizeDeletes(List<Node> operations) {
		NodeDeleteComputation deleteComputation = new NodeDeleteComputation(_component);
		_xdiff.visit(deleteComputation, null);
		List<MSXDiff> deletes = deleteComputation.getDeletes();
		for (int n = deletes.size() - 1; n >= 0; n--) {
			MSXDiff operation = deletes.get(n);
			operations.add(_builder.toXML(operation));
		}
	}

	private void synthsizeAdds(List<Node> operations) {
		NodeAddComputation addComputation = new NodeAddComputation(_component);
		_xdiff.visit(addComputation, null);
		add(operations, addComputation.getAdds());
	}

	private void synthsizeNodeValues(List<Node> operations) {
		NodeValueSetComputation valueSetComputation = new NodeValueSetComputation(_component);
		_xdiff.visit(valueSetComputation, null);
		add(operations, valueSetComputation.getOperations());

		NodeValueClearComputation valueClearComputation =
			new NodeValueClearComputation(_component, valueSetComputation.getUpdatedPaths());
		_xdiff.visit(valueClearComputation, null);
		add(operations, valueClearComputation.getOperations());
	}

	private void synthsizeAttributeSets(List<Node> operations) {
		AttributeSetComputation computation = new AttributeSetComputation(_component);
		_xdiff.visit(computation, null);
		add(operations, computation.getAttributeSets());
	}

	private void add(List<Node> operations, List<MSXDiff> nodeValues) {
		for (int n = 0, cnt = nodeValues.size(); n < cnt; n++) {
			MSXDiff operation = nodeValues.get(n);
			operations.add(_builder.toXML(operation));
		}
	}

}
