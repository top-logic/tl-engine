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

	/** Attribute of a {@link #COLUMN_TABLE_ELEMENT} naming the column. */
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
	
	/** Attribute of a {@link #TABLE_TEMPLATE_ELEMENT} with the number of rows. */
	String ROWS_TABLE_ATTRIBUTE = "rows";
	/** Attribute of a {@link #TABLE_TEMPLATE_ELEMENT} with the number of columns. */
	String COLUMNS_TABLE_ATTRIBUTE = "columns";
	
	/**
	 * attributes of a {@link #IMAGE_TEMPLATE_ELEMENT}
	 */
	String SRC_IMG_ATTRIBUTE = "src";
	/** Attribute of a {@link #IMAGE_TEMPLATE_ELEMENT} with the alternative text. */
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
	/** ID of {@link #TABLE_TEMPLATE_ELEMENT}. */
	int TABLE_TEMPLATE_ELEMENT_ID = 0;
	/** Element defining the default title of a table. */
	String DEFAULT_TITLE_TABLE_ELEMENT = "default-title";
	/** ID of {@link #DEFAULT_TITLE_TABLE_ELEMENT}. */
	int DEFAULT_TITLE_TABLE_ELEMENT_ID = 1;
	/** Element defining the default header of a table. */
	String DEFAULT_HEADER_TABLE_ELEMENT = "default-header";
	/** ID of {@link #DEFAULT_HEADER_TABLE_ELEMENT}. */
	int DEFAULT_HEADER_TABLE_ELEMENT_ID = 2;
	/** Element defining the default footer of a table. */
	String DEFAULT_FOOTER_TABLE_ELEMENT = "footer";
	/** ID of {@link #DEFAULT_FOOTER_TABLE_ELEMENT}. */
	int DEFAULT_FOOTER_TABLE_ELEMENT_ID = 3;
	/** Element defining the header of a table. */
	String HEADER_TABLE_ELEMENT = "header";
	/** ID of {@link #HEADER_TABLE_ELEMENT}. */
	int HEADER_TABLE_ELEMENT_ID = 4;
	/** Element defining a header line of a table. */
	String HEADER_LINE_TABLE_ELEMENT = "header-line";
	/** ID of {@link #HEADER_LINE_TABLE_ELEMENT}. */
	int HEADER_LINE_TABLE_ELEMENT_ID = 5;
	/** Element defining the body of a table. */
	String BODY_TABLE_ELEMENT = "body";
	/** ID of {@link #BODY_TABLE_ELEMENT}. */
	int BODY_TABLE_ELEMENT_ID = 6;
	/** Element defining the footer of a table. */
	String FOOTER_TABLE_ELEMENT = "footer";
	/** ID of {@link #FOOTER_TABLE_ELEMENT}. */
	int FOOTER_TABLE_ELEMENT_ID = 7;
	/** Element defining a footer line of a table. */
	String FOOTER_LINE_TABLE_ELEMENT = "footer-line";
	/** ID of {@link #FOOTER_LINE_TABLE_ELEMENT}. */
	int FOOTER_LINE_TABLE_ELEMENT_ID = 8;
	/** Element defining a column of a table. */
	String COLUMN_TABLE_ELEMENT = "column";
	/** ID of {@link #COLUMN_TABLE_ELEMENT}. */
	int COLUMN_TABLE_ELEMENT_ID = 9;
	/**
	 * Element that expands to an internationalized string identified by
	 * {@link FormTemplateConstants#KEY_TEXT_ATTRIBUTE key}.
	 */
	String TEXT_TEMPLATE_ELEMENT = "text";
	/** ID of {@link #TEXT_TEMPLATE_ELEMENT}. */
	int TEXT_TEMPLATE_ELEMENT_ID = 10;
	/**
	 * Group {@link #TEMPLATE_NS} element.
	 */
	String GROUP_TEMPLATE_ELEMENT = "group";
	/** ID of {@link #GROUP_TEMPLATE_ELEMENT}. */
	int GROUP_TEMPLATE_ELEMENT_ID = 11;
	/**
	 * List {@link #TEMPLATE_NS} element.
	 */
	String LIST_TEMPLATE_ELEMENT = "list";
	/** ID of {@link #LIST_TEMPLATE_ELEMENT}. */
	int LIST_TEMPLATE_ELEMENT_ID = 12;
	/**
	 * Element that expands to a list of all members of a {@link FormContainer}.
	 */
	String ITEMS_LIST_ELEMENT = "items";
	/** ID of {@link #ITEMS_LIST_ELEMENT}. */
	int ITEMS_LIST_ELEMENT_ID = 13;
	/**
	 * Subelement of {@link #LIST_TEMPLATE_ELEMENT}
	 */
	String LIST_ITEM_ELEMENT = "listItem";
	/** ID of {@link #LIST_ITEM_ELEMENT}. */
	int LIST_ITEM_ELEMENT_ID = 14;
	
	/** Element expanding to a simple table. */
	String SIMPLE_TABLE_ELEMENT ="simple-table";
	/** ID of {@link #SIMPLE_TABLE_ELEMENT}. */
	int SIMPLE_TABLE_ELEMENT_ID = 15;
	
	/** Element displaying an image. */
	String IMAGE_TEMPLATE_ELEMENT = "img";
	/** ID of {@link #IMAGE_TEMPLATE_ELEMENT}. */
	int IMAGE_TEMPLATE_ELEMENT_ID = 16;

	/**
	 * Element referencing a {@link HTMLFragment} for embedding dynamically
	 * rendered content.
	 */
	String FRAGMENT_TEMPLATE_ELEMENT = "fragment";
	/** ID of {@link #FRAGMENT_TEMPLATE_ELEMENT}. */
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

	/**
	 * Map from the element names of the {@link #TEMPLATE_NS} to their IDs.
	 *
	 * @see NamespaceUtil#elementMap(Class)
	 */
	Map TEMPLATE_ELEMENT_ID_MAP = NamespaceUtil.elementMap(FormTemplateConstants.class);


	
}

