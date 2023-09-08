/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket20846;

import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.tools.LayoutRewrite;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.assistent.BoundAssistentComponent;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * Layout XML migration tool for simplifying the component configuration.
 * 
 * @see "Ticket #20846"
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MigrateTicket20846 extends LayoutRewrite {

	boolean _complete = false;

	public static void main(String[] args) throws Exception {
		new MigrateTicket20846().runMain(args);
	}

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (option.equals("minimal")) {
			_complete = false;
		} else if (option.equals("complete")) {
			_complete = true;
		} else {
			return super.longOption(option, args, i);
		}
		return i;
	}

	@Override
	protected boolean process(Document layout) {
		descend(layout);
		return true;
	}

	private void descend(Node layout) {
		if (layout.getNodeType() == Node.ELEMENT_NODE) {
			if (!processElement((Element) layout)) {
				return;
			}
		}

		for (Node child : DOMUtil.children(layout)) {
			descend(child);
		}

		if (layout.getNodeType() == Node.ELEMENT_NODE) {
			if (!processElementAfter((Element) layout)) {
				return;
			}
		}
	}

	/**
	 * Processes the given {@link Element} node.
	 * 
	 * @param layout
	 *        The layout element to process.
	 * @return <code>false</code> to skip processing contents.
	 */
	private boolean processElement(Element layout) {
		Iterator<Element> componentLayoutConstraintIt =
			DOMUtil.elementsNS(layout, null, "componentLayoutConstraint").iterator();
		if (componentLayoutConstraintIt.hasNext()) {
			Element constraintsContainer = componentLayoutConstraintIt.next();
			constraintsContainer.getParentNode().removeChild(constraintsContainer);
			
			Iterator<Element> layoutConstraintIt =
				DOMUtil.elementsNS(constraintsContainer, null, "layoutConstraint").iterator();
			
			Iterator<Element> childListIt = DOMUtil.elementsNS(layout, null, "childList").iterator();
			Element childList = childListIt.next();

			Iterator<Element> childIt = DOMUtil.elements(childList).iterator();
			while (childIt.hasNext()) {
				Element child = childIt.next();
				Element constraint = layoutConstraintIt.next();

				constraint.getParentNode().removeChild(constraint);
				if (constraint.getAttributes().getLength() > 0) {
					Element infoContainer;
					if ("include".equals(child.getLocalName())) {
						Iterator<Element> injects = DOMUtil.elementsNS(child, null, "inject").iterator();
						Element inject;
						if (injects.hasNext()) {
							inject = injects.next();
						} else {
							inject = child.getOwnerDocument().createElementNS(null, "inject");
							child.appendChild(inject);
						}
						infoContainer = inject;
					} else {
						infoContainer = child;
					}
					infoContainer.insertBefore(constraint, infoContainer.getFirstChild());

					replaceTagName(constraint, "layoutInfo");
				}
			}

			childList.removeAttributeNS(null, "size");
			replaceTagName(childList, "components");
		}
		if ("tab".equals(layout.getLocalName())) {
			String classValue = layout.getAttributeNS(null, "class");
			if ("com.top_logic.layout.component.TabComponent$TabbedLayoutComponent".equals(classValue)) {
				layout.removeAttributeNS(null, "class");

				Element container = layout.getOwnerDocument().createElementNS(null, "components");
				for (Node child : DOMUtil.elements(layout)) {
					if ("info".equals(child.getLocalName())) {
						continue;
					}

					child.getParentNode().removeChild(child);
					container.appendChild(child);
				}
				layout.appendChild(container);
			}

			for (Element info : DOMUtil.elementsNS(layout, null, "info")) {
				if ("com.top_logic.knowledge.gui.layout.IconResTabInfo".equals(info.getAttributeNS(null, "class"))) {
					layout.setAttributeNS(null, "id", info.getAttributeNS(null, "TabInfo.reference"));
				}
				info.removeAttributeNS(null, "class");
				info.removeAttributeNS(null, "TabInfo.reference");
				info.removeAttributeNS(null, "TabInfo.cssClass");
				info.removeAttributeNS(null, "TabInfo.selectedCSSclass");

				NamedNodeMap attributes = info.getAttributes();
				for (int n = 0, length = attributes.getLength(); n < length; n++) {
					Attr attr = (Attr) attributes.item(n);

					String name = attr.getLocalName();
					if ("TabInfo.name".equals(name)) {
						name = "label";
					}
					else if ("TabInfo.unselectedImage".equals(name)) {
						name = "image";
					}
					else if ("TabInfo.selectedImage".equals(name)) {
						name = "imageSelected";

						if (attr.getValue().equals(info.getAttributeNS(null, "TabInfo.unselectedImage"))) {
							continue;
						}
					}
					else if (name.startsWith("TabInfo.")) {
						name = name.substring("TabInfo.".length());
					}
					layout.setAttributeNS(null, name, attr.getValue());
				}

				info.getParentNode().removeChild(info);
			}
		}
		else if ("component".equals(layout.getLocalName())) {
			Element replacement;
			replacement = simplifyTag(layout, TabComponent.Config.TAG_NAME, TabComponent.class.getName());
			replacement = simplifyTag(layout, TableComponent.Config.TAG_NAME, TableComponent.class.getName());
			replacement = simplifyTag(layout, TreeComponent.Config.TAG_NAME, TreeComponent.class.getName());
			replacement = simplifyTag(layout, FormComponent.Config.TAG_NAME, FormComponent.class.getName());
			replacement = simplifyTag(layout, EditComponent.Config.TAG_NAME, EditComponent.class.getName());
			replacement = simplifyTag(layout, "attributedEditor",
				"com.top_logic.element.meta.form.component.DefaultEditAttributedComponent");
			replacement = simplifyTag(layout, "grid", "com.top_logic.element.layout.grid.GridComponent");
			replacement = simplifyTag(layout, BoundLayout.Config.TAG_NAME, BoundLayout.class.getName());
			if (replacement != null) {
				String horizontal = replacement.getAttributeNS(null, "horizontal");
				if ("false".equals(horizontal)) {
					replacement.removeAttributeNS(null, "horizontal");
				}
			}
			replacement = simplifyTag(layout, ButtonComponent.Config.TAG_NAME, ButtonComponent.class.getName());
			if (replacement != null) {
				replacement.removeAttributeNS(null, "target");
				for (Node child : DOMUtil.elementsNS(replacement, null, "layoutInfo")) {
					child.getParentNode().removeChild(child);
				}
			}
			simplifyTag(layout, BoundAssistentComponent.Config.TAG_NAME, BoundAssistentComponent.class.getName());
			simplifyTag(layout, CompoundSecurityLayout.Config.TAG_NAME, CompoundSecurityLayout.class.getName());
		}
		return true;
	}

	private boolean processElementAfter(Element layout) {
		Node parent = layout.getParentNode();
		if (hasParent(parent, "include")) {
			return true;
		}
		if ("components".equals(layout.getLocalName())) {
			if (_complete || hasParent(parent, "tab")) {
				embeddContainer(layout);
			}
		}
		else if ("tabs".equals(layout.getLocalName())) {
			if (_complete) {
				embeddContainer(layout);
			}
		}
		else if ("steps".equals(layout.getLocalName())) {
			Iterator<Element> infosIt = DOMUtil.elementsNS(parent, null, "infos").iterator();
			if (infosIt.hasNext()) {
				Element infos = infosIt.next();
				infos.getParentNode().removeChild(infos);
				Iterator<Element> infoIt = DOMUtil.elements(infos).iterator();
				for (Element component : DOMUtil.elements(layout)) {
					Element step = layout.getOwnerDocument().createElementNS(null, "step");
					parent.insertBefore(step, layout);
					component.getParentNode().removeChild(component);
					step.appendChild(component);

					Element info = infoIt.next();
					copyAttributes(info, step);

					String key = step.getAttributeNS(null, "stepNamei18nKey");
					if (key != null && !key.isEmpty()) {
						step.setAttributeNS(null, "labelKeySuffix", key);
					}
					step.removeAttributeNS(null, "stepNamei18nKey");
				}
				parent.removeChild(layout);
			}
		}
		else if ("layout".equals(layout.getLocalName())) {
			simplifyLayouts:
			if (_complete) {
				NamedNodeMap attributes = layout.getAttributes();
				for (int n = 0, length = attributes.getLength(); n < length; n++) {
					Attr attr = (Attr) attributes.item(n);
					if (!attr.getName().equals("horizontal")) {
						// Some "special" attribute, do not embed.
						break simplifyLayouts;
					}
				}

				Element uniqueContents = null;
				for (Element child : DOMUtil.elements(layout)) {
					if (uniqueContents != null) {
						// More than one child, do not embed.
						break simplifyLayouts;
					}

					uniqueContents = child;
				}

				if (uniqueContents != null) {
					embeddContainer(layout);
				}
			}
		}
		return true;
	}

	private boolean hasParent(Node parent, String tagName) {
		return parent != null && tagName.equals(parent.getLocalName());
	}

	private void embeddContainer(Element layout) {
		Node parent = layout.getParentNode();
		if (parent.getNodeType() == Node.DOCUMENT_NODE) {
			parent.removeChild(layout);
			for (Node content : DOMUtil.children(layout)) {
				if (content.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				content.getParentNode().removeChild(content);
				parent.appendChild(content);
			}
		} else {
			for (Node content : DOMUtil.children(layout)) {
				content.getParentNode().removeChild(content);
				parent.insertBefore(content, layout);
			}
			parent.removeChild(layout);
		}
	}

	private Element simplifyTag(Element layout, String tagName, String className) {
		String classValue = layout.getAttributeNS(null, "class");
		boolean matches = className.equals(classValue);
		if (matches) {
			layout.removeAttributeNS(null, "class");
			Element replacement = replaceTagName(layout, tagName);
			descend(replacement);
			return replacement;
		}
		return null;
	}

	private Element replaceTagName(Element layout, String newName) {
		Element replacement = layout.getOwnerDocument().createElementNS(null, newName);
		replace(layout, replacement);
		copyAttributes(layout, replacement);
		return replacement;
	}

	private void copyAttributes(Element source, Element target) {
		NamedNodeMap attributes = source.getAttributes();
		for (int n = 0, length = attributes.getLength(); n < length; n++) {
			Attr attribute = (Attr) attributes.item(n);
			target.setAttributeNS(attribute.getNamespaceURI(), attribute.getLocalName(),
				attribute.getValue());
		}
	}

	private void replace(Element layout, Element replacement) {
		Node parent = layout.getParentNode();
		if (parent.getNodeType() == Node.DOCUMENT_NODE) {
			parent.removeChild(layout);
			parent.appendChild(replacement);
		} else {
			parent.insertBefore(replacement, layout);
			parent.removeChild(layout);
		}
		for (Node child : DOMUtil.children(layout)) {
			layout.removeChild(child);
			replacement.appendChild(child);
		}
	}

}
