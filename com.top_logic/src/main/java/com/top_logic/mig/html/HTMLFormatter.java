/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;

/**
 * Thread-safe access to common formats to be used consistently throughout an application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HTMLFormatter {

    /**
	 * Returns {@link HTMLFormatter} for users {@link TimeZone} and {@link Locale}.
	 *
	 * Creates a new {@link HTMLFormatter} if it doesn't exist yet.
	 */
	static public Formatter getInstance() {
		return getInstance(ThreadContext.getLocale());
    }

	/**
	 * Returns {@link HTMLFormatter} for users {@link TimeZone} and given {@link Locale}.
	 *
	 * Creates a new {@link HTMLFormatter} if it doesn't exist yet.
	 */
	static public Formatter getInstance(Locale locale) {
		return getInstance(ThreadContext.getTimeZone(), locale);
    }
            
	/**
	 * Returns {@link HTMLFormatter} for given {@link TimeZone} and {@link Locale}.
	 *
	 * Creates a new {@link HTMLFormatter} if it doesn't exist yet.
	 */
	static public Formatter getInstance(TimeZone timeZone, Locale locale) {
		return FormatterService.getFormatter(timeZone, locale);
	}

	/**
	 * Format an object with a suitable Format
	 * 
	 * @param anObject
	 *        the object
	 * @return a formatted String, an empty String if anObject is <code>null</code>.
	 */
	public static String formatObject(Object anObject) {
		return formatObject(getInstance(), anObject);
	}

    /**
     * Format an object with a suitable Format
     * 
     * @param anObject the object
     * @return a formatted String, an empty String if anObject is <code>null</code>.
     */
	public static String formatObject(Formatter formatter, Object anObject) {
        if (anObject == null) {
            return "";
        }
        else if (anObject instanceof Number) {
			return formatter.getNumberFormat().format(anObject);
        }
        else if (anObject instanceof Date) {
			return formatter.getDateFormat().format((Date) anObject);
        }
        else if (anObject instanceof Collection<?>) {
            Collection<?>  theCol = (Collection<?>) anObject;
            if (theCol.isEmpty()) {
                return "";
            }
            Iterator<?>  theElts = theCol.iterator();
            StringBuilder theBuf = new StringBuilder(theCol.size() << 6); // * 64
            Object       theElt = theElts.next();
            theBuf.append(formatObject(theElt));

            while (theElts.hasNext()) {
                theBuf.append(", ");
                theElt = theElts.next();
                theBuf.append(formatObject(theElt));
            }
            
            return theBuf.toString();
        } else if (anObject instanceof Boolean) {
            if (((Boolean) anObject).booleanValue()) {
				return Resources.getInstance().getString(I18NConstants.YES_LABEL);
            } else {
				return Resources.getInstance().getString(I18NConstants.NO_LABEL);
            }
        } else if (anObject instanceof Map<?,?>) {
            Map<?,?> map = (Map<?,?>) anObject;
            
            if (map.size() == 0) {
                return "";
            }
            
            return formatObject(map.values());
        }else if (anObject instanceof WebFolder) {
            WebFolder     webFolder = (WebFolder) anObject;
            StringBuilder buffer    = new StringBuilder();
            getWebFolderAsString(webFolder, buffer);
            
            return buffer.toString();
		} else if (anObject instanceof TLObject) {
            return MetaResourceProvider.INSTANCE.getLabel(anObject);
        } else {
            return anObject.toString();
        }
    }

	/**
	 * Formats the given {@link WebFolder}.
	 */
	protected static void getWebFolderAsString(WebFolder aWebFolder, StringBuilder aBuffer) {
        aBuffer.append(MetaLabelProvider.INSTANCE.getLabel(aWebFolder));
        aBuffer.append('\n');
		Collection<? extends TLObject> children = aWebFolder.getContent();
		for (TLObject child : children) {
			if (child instanceof Document) {
				aBuffer.append(((Document) child).getName());
				aBuffer.append('\n');
			} else if (child instanceof WebFolder) {
				getWebFolderAsString((WebFolder) child, aBuffer);
			}
        }
    }
    

}
