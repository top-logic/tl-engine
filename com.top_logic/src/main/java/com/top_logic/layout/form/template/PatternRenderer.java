/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import static com.top_logic.basic.core.xml.DOMUtil.*;
import static com.top_logic.layout.form.template.FormPatternConstants.*;
import static com.top_logic.layout.form.template.FormTemplateConstants.*;

import java.io.IOException;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Renderer for a {@link FormTemplateControl}.
 * 
 * <p>
 * The pattern language is implemented by concrete subclasses.
 * </p>
 * 
 * Use {@link TemplateControl}
 */
@Deprecated
public class PatternRenderer extends DefaultControlRenderer<FormTemplateControl> {
	
	protected PatternRenderer() {
		// Singleton constructor.
	}
	
	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, FormTemplateControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		Element node = template(control).getRootElement();
		NamedNodeMap attributes = node.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			Node attribute = attributes.item(n);
			// Only XHTML elements.
			if (attribute.getNamespaceURI() != null) {
				continue;
			}
			// No id attributes.
			if (HTMLConstants.ID_ATTR.equals(attribute.getLocalName())) {
				continue;
			}
			// No id attributes.
			if (HTMLConstants.CLASS_ATTR.equals(attribute.getLocalName())) {
				continue;
			}

			out.writeAttribute(attribute.getLocalName(), attribute.getNodeValue());
		}
	}

	private FormTemplate template(Control control) {
		FormTemplateControl templateControl = (FormTemplateControl) control;
		return templateControl.getTemplate();
	}
	
	@Override
	public void appendControlCSSClasses(Appendable out, FormTemplateControl control) throws IOException {
		super.appendControlCSSClasses(out, control);

		Element node = template(control).getRootElement();
		// Only XHTML elements.
		HTMLUtil.appendCSSClass(out, node.getAttributeNS(null, CLASS_ATTR));
	}

	@Override
	protected String getControlTag(FormTemplateControl control) {
		FormTemplateControl templateControl = (FormTemplateControl) control;
		FormTemplate template = templateControl.getTemplate();

		return template.getRootElement().getLocalName();
	}

	@Override
	protected final void writeControlContents(DisplayContext context, TagWriter out, FormTemplateControl control)
			throws IOException {
		FormTemplateControl templateControl = control;
		FormMember model = templateControl.getModel();
		if (isCollapsed(model)) {
			return;
		}

		FormTemplate template = templateControl.getTemplate();
		Element rootElement = template.getRootElement();
		writeContents(context, out, template, model, rootElement);
	}

	private static boolean isCollapsed(FormMember model) {
		if (model instanceof FormContainer) {
			return ((FormContainer) model).isCollapsed();
		} else {
			return false;
		}
	}

	protected final void writeContents(DisplayContext context, TagWriter out, FormTemplate template, FormMember model,
			Node node) throws IOException {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			writeNode(context, out, template, model, child);
		}
	}

	private void writeNode(DisplayContext context, TagWriter out, FormTemplate template, FormMember model, Node currentNode) throws IOException {
		if (isElement(currentNode)) {
			if (hasNamespace(TEMPLATE_NS, currentNode)) {
				expandTemplate(context, out, template, model, (Element) currentNode);
				return;
			}
			else if (hasNamespace(PATTERN_NS, currentNode)) {
				expandPattern(context, out, template, model, (Element) currentNode);
				return;
			}
		}
		
		// Default: Copy into output.
		copyBeginStart(out, currentNode);
		boolean wasEmptyTag = copyBeginStop(out, currentNode);
		if (!wasEmptyTag) {
			writeContents(context, out, template, model, currentNode);
			copyEnd(out, currentNode);
		}
	}

	/**
	 * Hook for subclasses to interpret elements of the
	 * {@link FormPatternConstants#PATTERN_NS} namespace.
	 * 
	 * @param context
	 *        See {@link Control#write(DisplayContext, TagWriter)}.
	 * @param out
	 *        See {@link Control#write(DisplayContext, TagWriter)}.
	 * @param template
	 *        The template that is currently expanded.
	 * @param model
	 *        The model that is currently rendered.
	 * @param currentElement
	 *        The element from the {@link FormTemplateConstants#TEMPLATE_NS}
	 *        namespace to interpret.
	 */
	protected void expandPattern(DisplayContext context, TagWriter out, FormTemplate template, FormMember model, Element currentElement) throws IOException {
		if (hasLocalName(FIELD_PATTERN_ELEMENT, currentElement)) {
			String name = currentElement.getAttribute(NAME_FIELD_ATTRIBUTE);
			String style = currentElement.getAttribute(FormTemplateConstants.STYLE_TEMPLATE_ATTRIBUTE);

			FormMember member = FormContext.getMemberByRelativeName(model, name);

			Element inlineTemplateElement = getFirstElementChild(currentElement);
			HTMLFragment childDisplay = createChildControl(template, member, style, inlineTemplateElement);

			if (childDisplay != null) {
				childDisplay.write(context, out);
			} else {
				out.writeText("[err:" + member.getQualifiedName() + "]");
			}

			if (template.hasAutomaticErrorDisplay() && member instanceof FormField && StringServices.isEmpty(style)) {
				// TODO BHU: Only write error display, if requested by pattern.

				HTMLFragment errordisplay =
					createChildControl(template, member, FormTemplateConstants.STYLE_ERROR_VALUE, inlineTemplateElement);
				if (errordisplay != null) {
					// TODO BHU: Cannot write a spacer between input and error
					// display, because this spacer would be displayed no matter
					// whether the input and error are visible.
					// 
					// out.writeText(HTMLConstants.NBSP);
					errordisplay.write(context, out);
				}
			}
		}
		else if (hasLocalName(SELF_PATTERN_ELEMENT, currentElement)) {
			String style = currentElement.getAttributeNS(null, FormTemplateConstants.STYLE_TEMPLATE_ATTRIBUTE);
			Element inlineTemplate = getFirstElementChild(currentElement);
			
			writeMember(context, out, template, style, inlineTemplate, model);
		} 
	}

	/**
	 * Hook for subclasses to interpret elements of the
	 * {@link FormTemplateConstants#TEMPLATE_NS} namespace.
	 * 
	 * @param context
	 *        See {@link Control#write(DisplayContext, TagWriter)}
	 * @param out
	 *        See {@link Control#write(DisplayContext, TagWriter)}
	 * @param template
	 *        The template that is currently expanded.
	 * @param model
	 *        The model that is currently rendered.
	 * @param currentElement
	 *        The element from the {@link FormTemplateConstants#TEMPLATE_NS}
	 *        namespace to interpret.
	 */
	protected void expandTemplate(DisplayContext context, TagWriter out, FormTemplate template, FormMember model, Element currentElement) throws IOException {
		if (hasLocalName(ITEMS_LIST_ELEMENT, currentElement)) {
			Element memberTemplate = getFirstElementChild(currentElement);

			// Expand the node for each member of the FormContainer.
			for (Iterator<? extends FormMember> fieldsIterator = ((FormContainer) model).getMembers(); fieldsIterator.hasNext(); ) {
				FormMember listMember = fieldsIterator.next();
				
				if (memberTemplate == null) {
					// Short-cut syntax for <t:items><p:self/></t:items>.
					writeMember(context, out, template, "", null, listMember);
				} else {
					writeNode(context, out, template, listMember, memberTemplate);
				}
			}
		} 
		else if (hasLocalName(TEXT_TEMPLATE_ELEMENT, currentElement)) {
			String textKey = currentElement.getAttribute(FormTemplateConstants.KEY_TEXT_ATTRIBUTE);
			ResKey text = getResource(template, textKey);
			out.writeText(context.getResources().getString(text));
		} 
		else if (hasLocalName(IMAGE_TEMPLATE_ELEMENT, currentElement)) {
			String imageSrc = currentElement.getAttribute(SRC_IMG_ATTRIBUTE);
			if (StringServices.isEmpty(imageSrc)) {
				String imageKey = currentElement.getAttribute(KEY_IMG_ATTRIBUTE);
				if (StringServices.isEmpty(imageKey)) {
					// Skip image.
					Logger.error("Missing '" + SRC_IMG_ATTRIBUTE + "' or '" + KEY_IMG_ATTRIBUTE + "' attribute in template element: " + currentElement.getLocalName(), this);
					return;
				}
				
				ResKey imageResourceSrc = getResourceOrNull(template, imageKey);
				imageSrc = context.getResources().getString(imageResourceSrc);
				if (imageSrc == null) {
					// Mark resouce as missing.
					getResource(template, imageKey);
					return;
				}
				
				if (imageSrc.length() == 0) {
					// Explicitly removed.
					return;
				}
			}
			
			ResKey alt;
			String altKey = currentElement.getAttribute(FormTemplateConstants.ALT_IMG_ATTRIBUTE);
			if (StringServices.isEmpty(altKey)) {
				alt = ResKey.text("");
			} else {
				alt = getResource(template, altKey);
			}
			
			XMLTag icon = ThemeImage.icon(imageSrc).toIcon();
			icon.beginBeginTag(context, out);
			out.writeAttribute(ALT_ATTR, context.getResources().getString(alt));
			icon.endBeginTag(context, out);
			icon.endTag(context, out);
		}
		else if (hasLocalName(FRAGMENT_TEMPLATE_ELEMENT, currentElement)) {
			// Forward control to dynamic content creator.
			template.getFragment(currentElement).write(context, out);
		} 
		else {
			Logger.error("Unexpected element in template: " + currentElement.getLocalName(), this);
		}
	}

	private static ResKey getResourceOrNull(FormTemplate template, String localKey) {
		return template.getResources().getStringResource(localKey, null);
	}

	private static ResKey getResource(FormTemplate template, String localKey) {
		return template.getResources().getStringResource(localKey);
	}

	private void writeMember(DisplayContext context, TagWriter out, FormTemplate template, String style, Element inlineTemplate, FormMember member) throws IOException {
		HTMLFragment display = createChildControl(template, member, style, inlineTemplate);
		if (display != null) {
			display.write(context, out);
		} else {
			out.writeText("[err:" + member.getQualifiedName() + "]");
		}
	}

	protected HTMLFragment createChildControl(FormTemplate template, FormMember member, String style,
			Element inlineTemplate) {
		HTMLFragment result;
		if (inlineTemplate == null) {
			ControlProvider controlProvider = template.getControlProvider();
			
			result = controlProvider.createControl(member, style);
		} else {
			result = FormTemplateControlFactory.createFormTemplateControl(member, template, inlineTemplate);
		}
		
		assert result != null : "No control to display field '" + member.getName() + "'.";
		
		return result;
	}

	/**
	 * Copies the begin of the start tag of the given node to the given stream.
	 */
	protected static void copyBeginStart(TagWriter out, Node node) throws IOException {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE: {
			out.beginBeginTag(node.getLocalName());
			NamedNodeMap attributes = node.getAttributes();
			for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
				Node attribute = attributes.item(n);
				// Only XHTML elements.
				if (attribute.getNamespaceURI() != null) {
					continue;
				}
				// No id attributes.
				if (HTMLConstants.ID_ATTR.equals(attribute.getLocalName())) {
					continue;
				}
				out.writeAttribute(attribute.getLocalName(), attribute.getNodeValue());
			}
			break;
		}
		case Node.CDATA_SECTION_NODE:
		case Node.TEXT_NODE: {
			out.writeText(node.getNodeValue());
			break;
		}
		}
	}
	
	/**
	 * Copies the end of the start tag of the given node to the given stream.
	 * 
	 * @return whether the tag was an empty tag, i.e. whether no stop tag must
	 *         be written
	 */
	protected static boolean copyBeginStop(TagWriter out, Node node) throws IOException {
		boolean isNotEmpty = hasContent(node) || !HTMLUtil.isVoidElement(node);
		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE: {
				if (isNotEmpty) {
					out.endBeginTag();
				} else {
					out.endEmptyTag();
				}
				break;
			}
		}
		return !isNotEmpty;
	}

	/**
	 * Copies the end tag of the given node to the given stream.
	 */
	protected static void copyEnd(TagWriter out, Node node) throws IOException {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE: {
			out.endTag(node.getLocalName());
		}
		}
	}
	
}
