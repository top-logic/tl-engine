
/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Tag and attributes names used for ex/importing Knowledgebases.
 *
 * @author  Klaus Halfmann
 */
public interface KBXMLConstants extends DOXMLConstants {

	/** tag designating a KnowledgeObject definition */
    public static final String KNOWLEDGE_OBJECT_ELEMENT        = "knowledgeobject";

    /** tag designating a KnowledgeObject definition */
    public static final String KNOWLEDGE_ASSOCIATION_ELEMENT   = "knowledgeassociation";

    /** Value for MO_TYPE_ATTRIB describing an knowledge object type */
    public static final String MO_KNOWLEDGE_OBJECT_TYPE_VALUE      = "MOKnowledgeObject";

    /** Name of Tag for an KnowledgeObject Attribute */
    public final static String  KO_ATTRIBUTE_ELEMENT     = "ko_attribute";
    
	/**
	 * Attributes that requests versioning override by type.
	 */
	public static final String MO_VERSIONED_ATTR = "versioned";

}
