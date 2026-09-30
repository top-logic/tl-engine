/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.element.meta.gui.DynamicTypeContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.FormPageTag;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * {@link FormPageTag} that allows naming title fields using model attribute names instead of
 * {@link FormMember} names.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaFormPageTag extends FormPageTag {

	private TLClass _metaElement;

	private String _domain;

	private String _titleAttribute;

	private String _subtitleAttribute;

	/**
	 * The {@link TLClass} that describes the type being displayed in the form.
	 */
	public TLClass getType() {
		if (_metaElement == null) {
			Wrapper model = getAttributedModel();
			if (model != null) {
				_metaElement = (TLClass) model.tType();
			} else {
				LayoutComponent component = getComponent();
				if (component instanceof DynamicTypeContext) {
					_metaElement = ((DynamicTypeContext) component).getMetaElement();
				}
			}
		}
		return _metaElement;
	}

	/**
	 * Type safe access to {@link #getModel()}.
	 */
	protected final Wrapper getAttributedModel() {
		return (Wrapper) super.getModel();
	}

	@Override
	protected TLType guessType() {
		TLClass type = getType();
		if (type != null) {
			return type;
		}
		return super.guessType();
	}

	/**
	 * Sets model attribute displayed in the title area.
	 * 
	 * @param titleAttribute
	 *        The name of the attribute.
	 * 
	 * @see #setTitleField(String)
	 */
	@CalledFromJSP
	public void setTitleAttribute(String titleAttribute) {
		_titleAttribute = titleAttribute;
	}

	/**
	 * Sets the model attribute displayed in the sub-title area.
	 * 
	 * @param subtitleAttribute
	 *        The name of the attribute being displayed.
	 * 
	 * @see #setSubtitleField(String)
	 */
	@CalledFromJSP
	public void setSubtitleAttribute(String subtitleAttribute) {
		_subtitleAttribute = subtitleAttribute;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		if (_subtitleAttribute != null) {
			/* Ensure side effect, adding field name to list of rendered field as early as possible. */
			setSubtitleField(toFieldName(_subtitleAttribute));
		}
		if (_titleAttribute != null) {
			/* Ensure side effect, adding field name to list of rendered field as early as possible. */
			setTitleField(toFieldName(_titleAttribute));
		}
		return super.startElement();
	}

	private String toFieldName(String attributeName) {
		TLClass type = getType();
		if (type == null) {
			// No valid model, use the default title.
			return null;
		}

		TLStructuredTypePart attribute = type.getPart(attributeName);
		if (attribute == null) {
			// No such attribute, at least not in the currently displayed object.
			return null;
		}
		Wrapper model = getAttributedModel();

		MetaTagUtil.addDisplayedAttribute(pageContext, model, (TLStructuredTypePart) attribute);
		String fieldName = MetaAttributeGUIHelper.internalID(attribute, model, _domain);
		return fieldName;
	}

	/**
	 * The domain of displayed attributes.
	 * 
	 * @see MetaAttributeGUIHelper#getAttributeIDCreate(TLStructuredTypePart, String)
	 * @see #setTitleAttribute(String)
	 * @see #setSubtitleAttribute(String)
	 */
	@CalledFromJSP
	public void setDomain(String domain) {
		_domain = domain;
	}

	@Override
	protected void teardown() {
		_domain = null;
		_metaElement = null;
		_titleAttribute = null;
		_subtitleAttribute = null;

		super.teardown();
	}

}
