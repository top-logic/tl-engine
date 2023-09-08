/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.message;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.message.Message;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Constants defining the internal format of {@link MessageStoreFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MessageConstants {

	/**	Separator of message key and message arguments. */
	public static final char MSG_ARG_SEPARATOR  = '|';
	
	/** Separator of message namespace and message local name. */
	public static final char MSG_NAME_SEPARATOR = '.';
	
	/** End marker of a message as argument to another message. */
	public static final char MSG_END            = ')';
	
	/** Type code of a <code>null</code> argument. */
	public static final char TYPE_NULL          = '_';
	
	/** Type code of a {@link String} argument. */
	public static final char TYPE_STRING        = '"';
	
	/** Type code of a {@link #TRUE_VALUE}. */
	public static final char TYPE_TRUE          = 't';
	
	/** Encoding of <code>true</code>. */
	public static final String TRUE_VALUE       = TYPE_TRUE + "rue";
	
	/** Type code of a {@link #FALSE_VALUE}. */
	public static final char TYPE_FALSE         = 'f';
	
	/** Encoding of <code>false</code>. */
	public static final String FALSE_VALUE      = TYPE_FALSE + "alse";
	
	/** Suffix to {@link Integer} values. */
	public static final char TYPE_INT           = 'I';
	
	/** Suffix to {@link Long} values. */
	public static final char TYPE_LONG          = 'L';
	
	/** Suffix to {@link Float} values. */
	public static final char TYPE_FLOAT         = 'F';
	
	/** Suffix to {@link Double} values. */
	public static final char TYPE_DOUBLE        = 'D';

	/** Type code of a {@link Class} argument. */
	public static final char TYPE_CLASS         = '%';
	
	/** Type code of a {@link Wrapper} argument. */
	public static final char TYPE_WRAPPER       = '&';
	
	/** Type code of a {@link KnowledgeItem} argument. */
	public static final char TYPE_KO            = '$';
	
	/** Type code of a {@link Map} argument. */
	public static final char TYPE_MAP           = '{';
	
	/** Type code of a {@link List} argument. */
	public static final char TYPE_LIST          = '[';
	
	/** Type code of a {@link Message} argument to another message. */
	public static final char TYPE_MSG           = '(';
	
	/** End of a {@link #TYPE_STRING} marker. */
	public static final char STRING_END         = '"';
	
	/** Separator of {@link #TYPE_LIST} entries. */
	public static final char LIST_NEXT          = ',';
	
	/** End of {@link #TYPE_LIST} marker. */
	public static final char LIST_END           = ']';
	
	/** Separator of value from key in {@link #TYPE_MAP} arguments. */
	public static final char MAP_VALUE          = ':';
	
	/** Separator of {@link #TYPE_MAP} entries. */
	public static final char MAP_NEXT           = ',';
	
	/** End of {@link #TYPE_MAP} marker. */
	public static final char MAP_END            = '}';

	/** Symbol to quote character that would have special meaning in context. */
	public static final char QUOTE_SYMBOL       = '\\';
	
	/** Separator of object name in {@link #TYPE_KO} and {@link #TYPE_WRAPPER} values. */
	public static final char KO_NAME_SEPARATOR     = '-';
	
	/** Separator of branch in {@link #TYPE_KO} and {@link #TYPE_WRAPPER} values. */
	public static final char KO_BRANCH_SEPARATOR   = '#';
	
	/** Separator of revision in {@link #TYPE_KO} and {@link #TYPE_WRAPPER} values. */
	public static final char KO_REVISION_SEPARATOR = '-';
	
	/**
	 * Marker that the revision of a {@link #TYPE_KO} or {@link #TYPE_WRAPPER}
	 * value points to the current revision.
	 */
	public static final String KO_REVISION_CURRENT = "current";

}
