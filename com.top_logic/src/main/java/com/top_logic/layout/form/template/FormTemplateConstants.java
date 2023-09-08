/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.MemberStyle;
import com.top_logic.layout.form.template.model.internal.TemplateControl;

/**
 * Constants for the form template language.
 * 
 * @deprecated Use {@link TagTemplate} with {@link TemplateControl}
 */
@Deprecated
public interface FormTemplateConstants {

	/**
	 * The namespace URI for the template language.
	 */
	String TEMPLATE_NS = "http://www.top-logic.com/ns/form/template/1.0";

	String NAME_COLUMN_ATTRIBUTE = "name";

	/**
	 * Attribute of a {@link #TEMPLATE_NS} element requesting to render a certain aspect of a
	 * {@link FormMember}.
	 * 
	 * @see MemberStyle
	 */
	String STYLE_TEMPLATE_ATTRIBUTE = "style";
	
	/**
	 * Value of {@link #STYLE_TEMPLATE_ATTRIBUTE}.
	 * 
	 * @see MemberStyle#LABEL
	 */
	String STYLE_LABEL_VALUE = "label";

	/**
	 * Value of {@link #STYLE_TEMPLATE_ATTRIBUTE}.
	 * 
	 * @see MemberStyle#LABEL_WITH_COLON
	 */
	String STYLE_LABEL_WITH_COLON_VALUE = "label-with-colon";

	/**
	 * Value of {@link #STYLE_TEMPLATE_ATTRIBUTE}.
	 * 
	 * @see MemberStyle#ERROR
	 */
	String STYLE_ERROR_VALUE = "error";

	/**
	 * Value of {@link #STYLE_TEMPLATE_ATTRIBUTE}.
	 * 
	 * @see MemberStyle#DIRECT
	 */
	String STYLE_DIRECT_VALUE = "direct";

	/**
	 * Key attribute of a {@link #TEXT_TEMPLATE_ELEMENT}.
	 */
	String KEY_TEXT_ATTRIBUTE = "key";
	
	String ROWS_TABLE_ATTRIBUTE = "rows";
	String COLUMNS_TABLE_ATTRIBUTE = "columns";
	
	/**
	 * attributes of a {@link #IMAGE_TEMPLATE_ELEMENT}
	 */
	String SRC_IMG_ATTRIBUTE = "src";
	String ALT_IMG_ATTRIBUTE = "alt";
	
	/**
	 * Resource key whose value points to a {@link ThemeImage}.
	 */
	String KEY_IMG_ATTRIBUTE = "key";
	
	
	//Elements and their ID
	
	/**
	 * Table {@link #TEMPLATE_NS} element.
	 */
	String TABLE_TEMPLATE_ELEMENT = "table";
	int TABLE_TEMPLATE_ELEMENT_ID = 0;
	String DEFAULT_TITLE_TABLE_ELEMENT = "default-title";
	int DEFAULT_TITLE_TABLE_ELEMENT_ID = 1;
	String DEFAULT_HEADER_TABLE_ELEMENT = "default-header";
	int DEFAULT_HEADER_TABLE_ELEMENT_ID = 2;
	String DEFAULT_FOOTER_TABLE_ELEMENT = "footer";
	int DEFAULT_FOOTER_TABLE_ELEMENT_ID = 3;
	String HEADER_TABLE_ELEMENT = "header";
	int HEADER_TABLE_ELEMENT_ID = 4;
	String HEADER_LINE_TABLE_ELEMENT = "header-line";
	int HEADER_LINE_TABLE_ELEMENT_ID = 5;
	String BODY_TABLE_ELEMENT = "body";
	int BODY_TABLE_ELEMENT_ID = 6;
	String FOOTER_TABLE_ELEMENT = "footer";
	int FOOTER_TABLE_ELEMENT_ID = 7;
	String FOOTER_LINE_TABLE_ELEMENT = "footer-line";
	int FOOTER_LINE_TABLE_ELEMENT_ID = 8;
	String COLUMN_TABLE_ELEMENT = "column";
	int COLUMN_TABLE_ELEMENT_ID = 9;
	/**
	 * Element that expands to an internationalized string identified by
	 * {@link FormTemplateConstants#KEY_TEXT_ATTRIBUTE key}.
	 */
	String TEXT_TEMPLATE_ELEMENT = "text";
	int TEXT_TEMPLATE_ELEMENT_ID = 10;
	/**
	 * Group {@link #TEMPLATE_NS} element.
	 */
	String GROUP_TEMPLATE_ELEMENT = "group";
	int GROUP_TEMPLATE_ELEMENT_ID = 11;
	/**
	 * List {@link #TEMPLATE_NS} element.
	 */
	String LIST_TEMPLATE_ELEMENT = "list";
	int LIST_TEMPLATE_ELEMENT_ID = 12;
	/**
	 * Element that expands to a list of all members of a {@link FormContainer}.
	 */
	String ITEMS_LIST_ELEMENT = "items";
	int ITEMS_LIST_ELEMENT_ID = 13;
	/**
	 * Subelement of {@link #LIST_TEMPLATE_ELEMENT}
	 */
	String LIST_ITEM_ELEMENT = "listItem";
	int LIST_ITEM_ELEMENT_ID = 14;
	
	String SIMPLE_TABLE_ELEMENT ="simple-table";
	int SIMPLE_TABLE_ELEMENT_ID = 15;
	
	String IMAGE_TEMPLATE_ELEMENT = "img";
	int IMAGE_TEMPLATE_ELEMENT_ID = 16;

	/**
	 * Element referencing a {@link HTMLFragment} for embedding dynamically
	 * rendered content.
	 */
	String FRAGMENT_TEMPLATE_ELEMENT = "fragment";
	int FRAGMENT_TEMPLATE_ELEMENT_ID = 17;

	/**
	 * Name of the {@link HTMLFragment} referenced by a
	 * {@link #FRAGMENT_TEMPLATE_ELEMENT} element.
	 */
	String NAME_FRAGMENT_TEMPLATE_ATTRIBUTE = "name";
	
	/**
	 * Template that embeds another template skipping the embedded template's root element.
	 */
	String EMBEDD_TEMPLATE_ELEMENT = "embedd";

	/**
	 * Template that delays rendering just before the end of the current block.
	 */
	String SUFFIX_TEMPLATE_ELEMENT = "suffix";

	/**
	 * @see #EMBEDD_TEMPLATE_ELEMENT
	 */
	int EMBEDD_TEMPLATE_ELEMENT_ID = 18;
	
	/**
	 * Resource template expression, see {@link TemplateExpression}.
	 */
	String EXPR_TEMPLATE_ELEMENT = "expr";

	Map TEMPLATE_ELEMENT_ID_MAP = NamespaceUtil.elementMap(FormTemplateConstants.class);


	
}

