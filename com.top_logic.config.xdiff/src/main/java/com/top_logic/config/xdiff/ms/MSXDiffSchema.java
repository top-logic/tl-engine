/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants for the Microsoft XML diff schema known as "Custom XML".
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MSXDiffSchema {

	String CUSTOMCONFIG_XSD = "customconfig.xsd";

	String NO_NAMESPACE_SCHEMA_LOCATION = "noNamespaceSchemaLocation";

	/**
	 * Root element of the {@link MSXDiffSchema}.
	 */
	String CONFIGURATION_ELEMENT = "configuration";

	/**
	 * Child of {@link #CONFIGURATION_ELEMENT}.
	 */
	String COMPONENTS_ELEMENT = "components";

	/**
	 * Child of {@link #COMPONENTS_ELEMENT}.
	 */
	String CONFIG_CUSTOMIZATIONS_ELEMENT = "ConfigCustomizations";

	/**
	 * Child of {@link #CONFIG_CUSTOMIZATIONS_ELEMENT} containing operation elements.
	 */
	String SETTINGS_ELEMENT = "Settings";

	/**
	 * Operation to remove a node.
	 */
	String NODE_DELETE_ELEMENT = "NodeDelete";

	/**
	 * Name of the component against which to remove this node.
	 * 
	 * <p>
	 * This is the tag that shows up in a .xml configuration file, just below the
	 * <code>&lt;components></code> tag. For example, in search.xml, the tag that shows up is
	 * “search”. If this component does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_DELETE_ELEMENT
	 */
	String NODE_DELETE__COMPONENT_ATTRIBUTE = "Component";

	/**
	 * An XPath query of the node to remove.
	 * 
	 * <p>
	 * If this XPath does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_DELETE_ELEMENT
	 */
	String NODE_DELETE__XPATH_ATTRIBUTE = "XPath";

	/**
	 * Specifies the addition of a new XML node somewhere in a given configuration component.
	 * 
	 * <p>
	 * The following parameters are supported.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 */
	String NODE_ADD_ELEMENT = "NodeAdd";

	/**
	 * Name of the component against which to add this node.
	 * 
	 * <p>
	 * This is the tag that shows up in a .xml configuration file, just below the
	 * <code>&lt;components></code> tag. For example, in search.xml, the tag that shows up is
	 * “search”. If this component does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_ADD_ELEMENT
	 */
	String NODE_ADD__COMPONENT_ATTRIBUTE = "Component";

	/**
	 * An XPath query of the parent of the new node.
	 * 
	 * <p>
	 * If the query does not return a node, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_ADD_ELEMENT
	 */
	String NODE_ADD__XPATH_ATTRIBUTE = "XPath";

	/**
	 * Name of the node to add.
	 * 
	 * <p>
	 * For example, to add a new root certificate in authenticator.xml, specify
	 * “rootCertificateSubjectKeyIdentifier”. If NodeName is not specified, the body of the NodeAdd
	 * element is added as a raw node, enabling you to add a complex node. If NodeName is specified
	 * with a body, the body is ignored.
	 * </p>
	 * 
	 * <p>
	 * Required: no
	 * </p>
	 * 
	 * @see #NODE_ADD_ELEMENT
	 */
	String NODE_ADD__NODE_NAME_ATTRIBUTE = "NodeName";

	/**
	 * Specifies the node’s text, if any, such as between its open tag and close tag.
	 * 
	 * <p>
	 * In the case of rootCertificateSubjectKeyIndentifier, for example, the node’s text would be
	 * the actual subject key identifier (SKID). In most cases, if a node doesn’t have text, it does
	 * have attributes. For example, “Sessions” in the TServer.xml configuration file has only
	 * attributes.
	 * </p>
	 * 
	 * <p>
	 * Required: no
	 * </p>
	 * 
	 * @see #NODE_ADD_ELEMENT
	 */
	String NODE_ADD__NODE_TEXT_ATTRIBUTE = "NodeText";

	/**
	 * If this is an insert rather than a simple add at the end of the children nodes, this would be
	 * an XPath query to the sibling that comes after it once it is inserted.
	 * 
	 * <p>
	 * If this parameter is empty, the node is added at the end of the children nodes.
	 * </p>
	 * 
	 * <p>
	 * In the following example, if “BeforeXPath” is empty, the new node is added after
	 * <code>&lt;secondNode></code> if <code>&lt;SomeNode></code> is specified as the parent. If
	 * BeforeXPath points to <code>&lt;firstNode></code>, the new node is inserted before
	 * <code>&lt;firstNode></code>. If BeforeXPath does not refer to a valid child of
	 * <code>&lt;SomeNode></code>, an error occurs.
	 * </p>
	 * 
	 * <xmp> <SomeNode> <firstNode/> <secondNode/> </SomeNode> </xmp>
	 * 
	 * <p>
	 * Required: no
	 * </p>
	 * 
	 * @see #NODE_ADD_ELEMENT
	 */
	String NODE_ADD__BEFORE_XPATH_ATTRIBUTE = "BeforeXPath";

	/**
	 * Changes a node’s text value. The following parameters are supported.
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 */
	String NODE_VALUE_ELEMENT = "NodeValue";

	/**
	 * Name of the component against which to modify the value.
	 * 
	 * <p>
	 * This is the tag that shows up in a .xml configuration file, just below the
	 * <code>&lt;components></code> tag. For example, in search.xml, the tag that shows up is
	 * “search”. If this component does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_VALUE_ELEMENT
	 */
	String NODE_VALUE__COMPONENT_ATTRIBUTE = "Component";

	/**
	 * An XPath query of the node to modify.
	 * 
	 * <p>
	 * If this XPath does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_VALUE_ELEMENT
	 */
	String NODE_VALUE__XPATH_ATTRIBUTE = "XPath";

	/**
	 * New text value for this node.
	 * 
	 * <p>
	 * To remove the value, leave blank.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #NODE_VALUE_ELEMENT
	 */
	String NODE_VALUE__VALUE_ATTRIBUTE = "Value";

	/**
	 * Adds or modifies a node’s attributes.
	 * 
	 * <p>
	 * The following parameters are supported.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 */
	String ATTRIBUTE_SET_ELEMENT = "AttributeSet";

	/**
	 * The name of the component against which to modify attributes.
	 * 
	 * <p>
	 * This is the tag that shows up in a .xml configuration file, just below the
	 * <code>&lt;components></code> tag. For example, in search.xml, the tag that shows up is
	 * “search”. If this component does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #ATTRIBUTE_SET_ELEMENT
	 */
	String ATTRIBUTE_SET__COMPONENT_ATTRIBUTE = "Component";

	/**
	 * An XPath query of the XML node whose attributes are modified.
	 * 
	 * <p>
	 * Note: This is not the attribute itself, but the element hosting the attributes. If this XPath
	 * does not exist, an error occurs.
	 * </p>
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #ATTRIBUTE_SET_ELEMENT
	 */
	String ATTRIBUTE_SET__XPATH_ATTRIBUTE = "XPath";

	/**
	 * Name of the attribute. If this attribute does not exist, it is created. If it does exist, its
	 * value is modified.
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #ATTRIBUTE_SET_ELEMENT
	 */
	String ATTRIBUTE_SET__NAME_ATTRIBUTE = "Name";

	/**
	 * New value for the attribute.
	 * 
	 * <p>
	 * Required: yes
	 * </p>
	 * 
	 * @see #ATTRIBUTE_SET_ELEMENT
	 */
	String ATTRIBUTE_SET__VALUE_ATTRIBUTE = "Value";

	/**
	 * All attribute local names of {@link #NODE_DELETE_ELEMENT}.
	 */
	static final Set<String> NODE_DELETE_ATTRIBUTES = new HashSet<>(Arrays.asList(
		NODE_DELETE__COMPONENT_ATTRIBUTE,
		NODE_DELETE__XPATH_ATTRIBUTE));

	/**
	 * All attribute local names of {@link #NODE_ADD_ELEMENT}.
	 */
	static final Set<String> NODE_ADD_ATTRIBUTES = new HashSet<>(Arrays.asList(
		NODE_ADD__BEFORE_XPATH_ATTRIBUTE,
		NODE_ADD__COMPONENT_ATTRIBUTE,
		NODE_ADD__NODE_NAME_ATTRIBUTE,
		NODE_ADD__NODE_TEXT_ATTRIBUTE,
		NODE_ADD__XPATH_ATTRIBUTE));

	/**
	 * All attribute local names of {@link #NODE_VALUE_ELEMENT}.
	 */
	static final Set<String> NODE_VALUE_ATTRIBUTES = new HashSet<>(Arrays.asList(
		NODE_VALUE__COMPONENT_ATTRIBUTE,
		NODE_VALUE__VALUE_ATTRIBUTE,
		NODE_VALUE__XPATH_ATTRIBUTE));

	/**
	 * All attribute local names of {@link #ATTRIBUTE_SET_ELEMENT}.
	 */
	static final Set<String> ATTRIBUTE_SET_ATTRIBUTES = new HashSet<>(Arrays.asList(
		ATTRIBUTE_SET__COMPONENT_ATTRIBUTE,
		ATTRIBUTE_SET__NAME_ATTRIBUTE,
		ATTRIBUTE_SET__VALUE_ATTRIBUTE,
		ATTRIBUTE_SET__XPATH_ATTRIBUTE));

}
