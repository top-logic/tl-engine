
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

/**
 * These attributes are defined by the superclass "DublinCore".
 *<p>
 *  Almost all KOs therefore inherit these Attributes. But they
 *  are optional and may be removed in case someone would not 
 *  like to use them.
 *</p>
 * @author  Klaus Halfmann / Marco Perra
 */
public interface DCMetaData {

    /** Name of a Doublin Core Attribute */
    public static final String COVERAGE    = "dc_coverage";

    /** Name of a Doublin Core Attribute */
    public static final String DESCRIPTION = "dc_description";

    /** Name of a Doublin Core Attribute */
    public static final String TYPE        = "dc_type";

    /** Name of a Doublin Core Attribute */
    public static final String RELATION    = "dc_relation";

    /** Name of a Doublin Core Attribute */
    public static final String SOURCE      = "dc_source";

    /** Name of a Doublin Core Attribute */
    public static final String SUBJECT     = "dc_subject";

    /** Name of a Doublin Core Attribute */
    public static final String TITLE       = "dc_title";

    /** Name of a Doublin Core Attribute */
    public static final String CONTRIBUTOR = "dc_contributor";

    /** Name of a Doublin Core Attribute */
    public static final String PUBLISHER   = "dc_publisher";

    /** Name of a Doublin Core Attribute */
    public static final String RIGHTS      = "dc_rights";

    /** Name of a Doublin Core Attribute */
    public static final String FORMAT      = "dc_format";

    /** Name of a Doublin Core Attribute */
    public static final String IDENTIFIER  = "dc_identifier";

    /** Name of a Doublin Core Attribute */
    public static final String LANGUAGE    = "dc_language";
}
