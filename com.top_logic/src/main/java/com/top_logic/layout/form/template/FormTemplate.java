/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import static com.top_logic.layout.form.template.FormTemplateConstants.*;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.layout.resources.GlobalResources;

/**
 * Template that is dynamically expanded during rendering.
 * 
 * @deprecated Use {@link TagTemplate} with {@link TemplateControl}.
 */
@Deprecated
public class FormTemplate {
	
	private final ResourceView resources;
	private final Element rootElement;
	private Map<Element, HTMLFragment> fragmentsByElement;
	private final boolean automaticErrorDisplay;
	private final ControlProvider controlProvider;
	private final PatternRenderer renderer;

	/**
	 * @deprecated Better use {@link #FormTemplate(ResPrefix, ControlProvider, boolean, Document)}
	 *             with template constant build from {@link DOMUtil#parseThreadSafe(String)}.
	 */
	@Deprecated
	public FormTemplate(ResPrefix resoucePrefix, ControlProvider controlProvider, boolean automaticErrorDisplay,
			String document) {
		this(resoucePrefix, controlProvider, automaticErrorDisplay, DOMUtil.parse(document));
	}

	/**
	 * @see #FormTemplate(ResourceView, ControlProvider, boolean, Document)
	 */
	public FormTemplate(ResPrefix resoucePrefix, ControlProvider controlProvider, boolean automaticErrorDisplay,
			Document document) {
		this(resoucePrefix, controlProvider, automaticErrorDisplay, document.getDocumentElement());
	}

	/**
	 * Creates a {@link FormTemplate}.
	 * 
	 * @param document
	 *        The document whose document element should be used as template
	 *        {@link #getRootElement() root element}.
	 * 
	 * @see #FormTemplate(ResPrefix, ControlProvider, boolean, Element)
	 */
	public FormTemplate(ResourceView resouces, ControlProvider controlProvider, boolean automaticErrorDisplay,
			Document document) {
		this(resouces, controlProvider, automaticErrorDisplay, document.getDocumentElement());
	}

	/**
	 * @see #FormTemplate(ResourceView, ControlProvider, boolean, Element)
	 */
	public FormTemplate(ResPrefix resoucePrefix, ControlProvider controlProvider, boolean automaticErrorDisplay,
			Element rootElement) {
		this(resoucePrefix, DefaultPatternRenderer.INSTANCE, Collections.<String, HTMLFragment> emptyMap(),
			controlProvider, automaticErrorDisplay, rootElement);
	}

	/**
	 * Creates a {@link FormTemplate}.
	 * 
	 * @param rootElement
	 *        See {@link #getRootElement()}.
	 * 
	 * @see #FormTemplate(ResPrefix, PatternRenderer, Map, ControlProvider, boolean, Element)
	 */
	public FormTemplate(ResourceView resouces, ControlProvider controlProvider, boolean automaticErrorDisplay,
			Element rootElement) {
		this(resouces, DefaultPatternRenderer.INSTANCE, Collections.<String, HTMLFragment> emptyMap(), controlProvider,
			automaticErrorDisplay, rootElement);
	}

	/**
	 * @see #FormTemplate(ResourceView, PatternRenderer, Map, ControlProvider, boolean, Element)
	 */
	public FormTemplate(ResPrefix resourcePrefix, PatternRenderer renderer, Map<String, HTMLFragment> fragmentsByName,
			ControlProvider controlProvider, boolean automaticErrorDisplay, Element rootElement) {
		this(toResourceView(resourcePrefix), renderer, fragmentsByName, controlProvider,
			automaticErrorDisplay, rootElement);
	}

	private static ResourceView toResourceView(ResPrefix resourcePrefix) {
		if (resourcePrefix == null || resourcePrefix.isEmpty()) {
			return GlobalResources.INSTANCE;
		} else {
			return resourcePrefix;
		}
	}

	/**
	 * Creates a {@link FormTemplate}.
	 * 
	 * @param resources
	 *        See {@link #getResources()}
	 * @param renderer
	 *        See {@link #getRenderer()}
	 * @param fragmentsByName
	 *        Map with named {@link HTMLFragment}s. See {@link #getFragment(Node)}.
	 * @param controlProvider
	 *        See {@link #getControlProvider()}.
	 * @param automaticErrorDisplay
	 *        See {@link #hasAutomaticErrorDisplay()}.
	 * @param rootElement
	 *        See {@link #getRootElement()}.
	 */
	public FormTemplate(ResourceView resources, PatternRenderer renderer, Map<String, HTMLFragment> fragmentsByName,
			ControlProvider controlProvider, boolean automaticErrorDisplay, Element rootElement) {
		this.resources = resources;
		this.rootElement = rootElement;
		this.renderer = renderer;
		this.controlProvider = controlProvider;
		this.automaticErrorDisplay = automaticErrorDisplay;
		
		IdentityHashMap<Element, HTMLFragment> resolvedFragments = new IdentityHashMap<>();
		init(fragmentsByName, resolvedFragments, rootElement);
		
		if (resolvedFragments.size() > 0) {
			this.fragmentsByElement = resolvedFragments;
		} else {
			this.fragmentsByElement = Collections.emptyMap();
		}
	}

	/**
	 * Recursively scan the given element for
	 * {@link FormTemplateConstants#FRAGMENT_TEMPLATE_ELEMENT} elements and
	 * build the given resolved fragments map.
	 * 
	 * @param fragementsByName
	 *        The fragment definitions by name.
	 * @param resolvedFragments
	 *        The created mapping of fragment reference elements to fragment
	 *        instances.
	 * @param element
	 *        The {@link Element} to scan.
	 */
	private void init(Map<String, HTMLFragment> fragementsByName, Map<Element, HTMLFragment> resolvedFragments, Element element) {
		if (TEMPLATE_NS.equals(element.getNamespaceURI())) {
			if (FRAGMENT_TEMPLATE_ELEMENT.equals(element.getLocalName())) {
				String name = element.getAttributeNS(null, NAME_FRAGMENT_TEMPLATE_ATTRIBUTE);
				HTMLFragment fragment = fragementsByName.get(name);
				if (fragment == null) {
					throw new IllegalArgumentException("Fragment with name '" + name + "' is not defined.");
				}
				resolvedFragments.put(element, fragment);
			}		
		}
		
		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element) {
				init(fragementsByName, resolvedFragments, (Element) child);
			}
		}
	}

	/**
	 * The root {@link Element} of this {@link FormTemplate}.
	 */
	public Element getRootElement() {
		return rootElement;
	}

	/**
	 * The {@link HTMLFragment} that should be rendered for the given fragment
	 * reference {@link Element}.
	 * 
	 * @param fragmentReference
	 *        An {@link Element} of type
	 *        {@link FormTemplateConstants#FRAGMENT_TEMPLATE_ELEMENT} from this
	 *        {@link FormTemplate}.
	 * 
	 * @return The corresponding {@link HTMLFragment} that should be rendered on
	 *         behalf of the given reference element.
	 */
	public HTMLFragment getFragment(Node fragmentReference) {
		return fragmentsByElement.get(fragmentReference);
	}

	/**
	 * Whether field patterns should automatically expanded into an input
	 * element and an error display.
	 */
	public boolean hasAutomaticErrorDisplay() {
		return automaticErrorDisplay;
	}

	/**
	 * The {@link ControlProvider} to use for field references that have no
	 * inline template.
	 */
	public ControlProvider getControlProvider() {
		return controlProvider;
	}

	/**
	 * Resources to use for constant texts and images.
	 */
	public ResourceView getResources() {
		return resources;
	}

	/**
	 * The {@link PatternRenderer} to use for this template.
	 */
	public PatternRenderer getRenderer() {
		return renderer;
	}

}
