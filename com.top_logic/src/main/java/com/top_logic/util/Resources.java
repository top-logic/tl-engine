/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Locale;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.DefaultBundle;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.DataObject;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;

/**
 * This is a class for providing language specific translations for words.
 * You can specify the location of the file to be used in the "top-logic.xml" 
 * via the entry "I18N.Location.name".
 *<p>
 * The current Locale is extracted from the {@link com.top_logic.util.TLContext}.
 * </p>
 * 
 * @author     <a href="mailto:mga@top-logic.com"">Michael G&auml;nsler</a>
 */
public class Resources extends DefaultBundle {

	private static Property<Resources> RESOURCES = TypedAnnotatable.property(Resources.class, "resources");

    /** The constant for the locale attribute at the Person KO. */
    public static final String LOCALE                = "locale";
 
	/** The pattern string for the locale part of a resource file. */
	public static final String RESOURCE_FILE_LOCALE_PATTERN =
		"(_[(\\w\\w)&&[^(xx)]].(_[(\\w\\w)&&[^(XX)]].)?)?";

    /**
	 * Create a Resources from aLocale.
	 */
	protected Resources(ResourcesModule owner, Locale locale, Map<String, String> bundle, I18NBundleSPI fallback) {
		super(owner, locale, bundle, fallback);
    }

    /**
     * Delivers the description of the instance of this class.
     * StringBuffer converts the contents of the string buffer to a string
     * 
     * @return   a string = the locale-specific objects
     */
    @Override
	public String toString () {
        return (this.getClass ().getName () + " [" +
			"locale: " + getLocale() +
                    ']');
    }

	/**
	 * Builds an encoded message for {@link #decodeMessageFromKeyWithEncodedArguments(String)}.
	 * 
	 * @param messageKey
	 *        A resource key that takes arguments.
	 * @param arguments
	 *        The arguments to encode.
	 * @return An encoded resource message.
	 * 
	 * @see #encodeMessage(ResKey, Object)
	 * 
	 * @deprecated Use {@link ResKey#message(ResKey, Object...)}
	 */
	@Deprecated
	public static ResKey encodeMessage(ResKey messageKey, Object... arguments) {
		return ResKey.message(messageKey, arguments);
	}

	/**
	 * Builds an encoded message for {@link #decodeMessageFromKeyWithEncodedArguments(String)}.
	 * 
	 * @param messageKey
	 *        A resource key that takes arguments.
	 * @param argument
	 *        The single argument to encode.
	 * @return An encoded resource message.
	 * 
	 * @see #encodeMessage(ResKey, Object...)
	 * 
	 * @deprecated Use {@link ResKey#message(ResKey, Object...)}
	 */
	@Deprecated
	public static ResKey encodeMessage(ResKey messageKey, Object argument) {
		return ResKey.message(messageKey, argument);
	}

	/**
	 * Transform a literal text into a format that can be decoded using
	 * {@link #decodeMessageFromKeyWithEncodedArguments(String)} to reproduce exactly the given
	 * text. literal text.
	 * 
	 * @param literalText
	 *        The text to encode.
	 * @return An encoded form that produces exactly the given text when passing the result to
	 *         {@link #decodeMessageFromKeyWithEncodedArguments(String)}.
	 * 
	 * @deprecated Use {@link ResKey#text(String)}
	 */
	@Deprecated
	public static ResKey encodeLiteralText(String literalText) {
		return ResKey.text(literalText);
	}

	/**
	 * A derived resource key from the key in the given encoded message and the given suffix.
	 * 
	 * @param keyWithEncodedArguments
	 *        A message in a format compatible with
	 *        {@link #decodeMessageFromKeyWithEncodedArguments(String)}.
	 * @param suffix
	 *        A key suffix.
	 * @return The concatenation of the resource key in the given encoded message and the given
	 *         suffix.
	 * 
	 * @deprecated Use {@link ResKey#suffix(String)}
	 */
	@Deprecated
	public static ResKey derivedKey(ResKey keyWithEncodedArguments, String suffix) {
		return keyWithEncodedArguments.suffix(suffix);
	}

	/**
	 * @deprecated Better use {@link #getString(ResKey)}
	 */
	@Deprecated
	public final String getMessage(ResKey aKey) {
		return getString(aKey);
	}

	@Override
	public Object transformMessageArgument(Object obj) {
		if (obj instanceof TLObject || obj instanceof KnowledgeItem) {
			return MetaLabelProvider.INSTANCE.getLabel(obj);
		}
		return super.transformMessageArgument(obj);
	}

    /**
     * Try to find the optimum locale for the current user.
     *
     * @return    The optimum locale for the current user.
     */
    public static final Locale findBestLocale () {
        return getDefaultLocale();
    }

    /**
     * Try to find the optimum locale from a DataObject (usually a Person).
     *
     * Will set the LOCALE value at the TopLogicThreadInfo as a side effect.
     *
     * @return  The optimum locale as found in the DataObject.
     */
    public static Locale findBestLocale(DataObject anObject) {
		Locale locale = null;
        if (anObject != null) {
            try {
				String localeSetting = (String) anObject.getAttributeValue(LOCALE);
				if (localeSetting == null || localeSetting.isEmpty()) {
					return getDefaultLocale();
				}
				locale = ResourcesModule.localeFromString(localeSetting);
            }
            catch (Exception ex) {
                Logger.error ("Unable to find best locale ", ex, Resources.class);
            }
        }
		if (locale == null) {
			locale = getDefaultLocale();
            if (anObject == null) {
                Logger.info ("Using default locale, because no object given!", Resources.class);
            }
            else {
                Logger.info ("No locale found, using default for " + anObject, Resources.class);
            }
        }
		return locale;
    }

    /**
     * This method delivers resources for the default (server) Locale.
     *
     * @return    An instance of the Resources class for the default locale.
     */
    public static final Resources getInstance() {
		return getInstance(TLContextManager.getSubSession());
    }

	/**
	 * Returns the {@link Resources} used by the given session.
	 * 
	 * @param session
	 *        The session to get {@link Resources} for. May be <code>null</code>.
	 * 
	 * @return The resources that are suitable for the given session. If there is no session, the
	 *         {@link Resources} suitable for the server locale are returned.
	 */
	public static Resources getInstance(SubSessionContext session) {
		if (session != null) {
			Resources cachedResources = session.get(RESOURCES);
			if (cachedResources != null && cachedResources.isValid()) {
				return cachedResources;
			}
			synchronized (Resources.class) {
				Resources concurrentlyCreatedResources = session.get(RESOURCES);
				if (concurrentlyCreatedResources != null && concurrentlyCreatedResources.isValid()) {
					return concurrentlyCreatedResources;
				}
				Resources newResources = Resources.getInstance(getLocale(session));
				session.set(RESOURCES, newResources);
				return newResources;
			}
		} else {
			return Resources.getInstance(Resources.findBestLocale());
		}
	}

	/**
	 * The {@link Locale} that should be used for the current sub-session.
	 * 
	 * @return Never null.
	 */
	public static Locale getCurrentLocale() {
		return getLocale(TLContextManager.getSubSession());
	}

	/**
	 * The {@link Locale} that should be used for the given sub-session.
	 * 
	 * @param session
	 *        If null, a user independent default is used.
	 * @return Never null.
	 */
	public static Locale getLocale(SubSessionContext session) {
		if (session == null) {
			return findBestLocale();
		}
		Locale locale = session.getCurrentLocale();
		if (locale == null) {
			return findBestLocale();
		}
		return locale;
	}

    /**
     * This method delivers an instance of resources for a given locale,
     *
     * @return   An instance of the resources class for the given locale.
     */
    public static Resources getInstance (Locale aLocale) {
    	return (Resources) ResourcesModule.getInstance().getBundle(aLocale);
    }

    /**
	 * A {@link I18NBundle} for the {@link #getDefaultLocale() system locale}.
	 */
	public static Resources getSystemInstance() {
		return getInstance(getDefaultLocale());
	}

	/**
	 * The {@link I18NBundle} for the locale of log statements.
	 * 
	 * @see ResourcesModule#getLogLocale()
	 */
	public static Resources getLogInstance() {
		return getInstance(ResourcesModule.getLogLocale());
	}

	/**
	 * This method delivers the only instance of Resources.
	 *
	 * @param aLocale
	 *        Should consists of language<b>_</b>country[<b>_</b>variant]
	 * @return An instance of the resources class for the given locale.
	 */
    public static Resources getInstance(String aLocale) {
        return Resources.getInstance(ResourcesModule.localeFromString(aLocale));
    }
    
    /**
	 * Use this via request.getHeader("accept-language") on JSP-Pages.
	 *
	 * @param headerValue
	 *        the value of the "accept-language" Header from a HTTP-Request.
	 *
	 * @return an instance of the Resources class for the given Language.
	 */
	public static Resources getAcceptLanguageInstance(String headerValue) {
		if (headerValue != null) {
			return Resources.getInstance(getAcceptLanguageLocale(headerValue));
		} else {
            return Resources.getInstance();
        }       
    }

	/**
	 * Use this via request.getHeader("accept-language") on JSP-Pages to get the locale.
	 * 
	 * @param headerValue
	 *        The value of the "accept-language" header from a HTTP-request, e.g.
	 *        <code>en-US,de;q=0.7,en;q=0.3</code>. May be <code>null</code>.
	 * 
	 * @return An instance of the {@link Locale} class for the given language or
	 *         {@link #getDefaultLocale()} if language was <code>null</code>.
	 */
	public static Locale getAcceptLanguageLocale(String headerValue) {
		if (headerValue == null || headerValue.isEmpty()) {
			return getDefaultLocale();
		}

		// More than one language?
		int next = headerValue.indexOf(',');
		if (next < 0) {
			next = headerValue.length();
		}

		// Options for the language.
		int end = headerValue.indexOf(';');
		if (end < 0 || end > next) {
			end = next;
		}

		return Locale.forLanguageTag(headerValue.substring(0, end));
	}

	/**
	 * The system locale.
	 * 
	 * @see #getSystemInstance()
	 */
    public static Locale getDefaultLocale() {
        return ResourcesModule.localeFromString(Locale.getDefault().getLanguage());
    }

    /**
     * This method returns the display language for a given language regarding
     * the Locale of the current user. <br />
     * e.g.                            <br />
     * language         = de           <br />
     * user locale      = en           <br />
     * display language = German
     * 
     * @param aLanguage A language (e.g. "en" or "de")
     * @return Returns the display language for a given language regarding the
     *         Locale of the current user.
     */
    public static String getDisplayLanguage(String aLanguage){
		Locale locale = ResourcesModule.localeFromString(aLanguage);
		return getDisplayLanguage(locale);
	}

	/**
	 * This method returns the display language for a given {@link Locale} regarding the locale of
	 * the current user. <br />
	 * e.g. <br />
	 * language = de <br />
	 * user locale = en_GB <br />
	 * display language = German
	 * 
	 * @param locale
	 *        A language (e.g. {@link Locale#GERMAN} or {@link Locale#UK}).
	 * 
	 * @return Returns the display language for a given locale regarding the locale of the current
	 *         user.
	 * 
	 * @since 5.7.5
	 */
	public static String getDisplayLanguage(Locale locale) {
		return getInstance().getDisplayLanguageImpl(locale);
	}

	/**
	 * The label of the given {@link Locale} in the the {@link #getLocale()} of this instance.
	 *
	 * @param locale
	 *        The {@link Locale} to display.
	 * @return A label for the given {@link Locale}.
	 */
	protected String getDisplayLanguageImpl(Locale locale) {
		return locale.getDisplayLanguage(getLocale());
	}

	/**
	 * Retrieves the {@link HTMLFragment} for the given {@link HtmlResKey}.
	 *
	 * @param key
	 *        The key to resolve within this {@link Resources}.
	 */
	public HTMLFragment getHtml(HtmlResKey key) {
		return key.getHtml(getCurrentLocale());
	}

}
