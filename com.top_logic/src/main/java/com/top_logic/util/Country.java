/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Country identified by the ISO country code.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Country implements Comparable {

	/** The sorted list of all countries. */
	private static Hashtable countryTable;

	private static final Map<String, String[]> missingNamesMap = loadMissingNamesMap();

	/** The ISO code of the country. */
	private String code;

	/** The name of the country. */
	private String name;

	/** The locale of the country. */
	private Locale locale;

	/**
	 * Creates a new instance of a country defined by the given ISO code.
	 *
	 * If there is no known name for that ISO code, the name will be the
	 * code itself.
	 *
	 * @param    aCode    The two character ISO code of the country.
	 */
	public Country (String aCode) {
		this.code = aCode;
		this.name = new Locale ("", aCode).getDisplayCountry ();
		this.locale = null;
	}

	/**
	 * Creates a new instance of a country defined by the given ISO code.
	 *
	 * If there is no known name for that ISO code, the name will be the
	 * code itself.
	 *
	 * @param    aCode    The two character ISO code of the country.
	 */
	private Country (String aCode, String aLanguage) {
		this.code = aCode;
        this.locale = new Locale (aLanguage);
        this.name = Country.getDisplayName(aCode, this.locale);
	}

    /**
     * Compare the given object to the name of this one.
     *
     * If the given instance is also a country, the name of that country
     * will be used for comparison, otherwise the given object itself.
     *
     * @param    anObject    The object to be compared to.
     * @return   The result of the string compare.
     */
    @Override
	public int compareTo (Object anObject) {
        if (anObject instanceof Country) {
        	Collator theColl = null;
        	if (this.locale == null) {
        		theColl = Collator.getInstance();
        	}
        	else {
        		theColl =  Collator.getInstance(this.locale);
        	}
            return (theColl.compare(this.name, ((Country) anObject).name));
        }
        else {
            return (this.name.compareTo ((String) anObject));
        }
    }

	/**
	 * Returns the debug representation of this instance.
	 *
	 * @return    The debug representation.
	 */
	@Override
	public String toString () {
		return (this.getClass ().getName () + " [" +
				"code: " + this.code +
				", name: " + this.name +
				']');
	}

	/**
	 * Returns the ISO code of this country (is UpperCase).
	 *
	 * @return    The two character ISO code.
	 */
	public String getCode () {
		return (this.code);
	}

	/**
	 * Returns the name of this country.
	 *
	 * @return    The name of this country.
	 */
	public String getName () {
		return (this.name);
	}
	
	/**
	 * @param aLocale
	 *        a {@link Locale} specifying the language to present the name in. Must not be
	 *        <code>null</code>.
	 * @return the name of the country in the language given by the locale.
	 */
	public String getName(Locale aLocale){
	    return Country.getDisplayName(this.code, aLocale);
	}

	/**
	 * Returns the list of all known countries in the system.
	 *
	 * The list will be sorted by the names of the countries.
	 *
	 * @return    The requested list.
	 */
	public static synchronized Country [] getCountryList () {
		return getCountryList ("");
	}
   
	/**
	 * Returns the list of all known countries in the system.
	 *
	 * The list will be sorted by the names of the countries.
	 *
	 * @return    The requested list.
	 */
	public static synchronized Country [] getCountryList (String aLanguage) {
		if (countryTable == null) {
			countryTable = new Hashtable();
		}
		
		if (aLanguage == null) {
			aLanguage = "";
		}
		
		Country[] countries = (Country[]) countryTable.get(aLanguage);
		
		if (countries == null) {
			String [] theList = Locale.getISOCountries ();

			List theCountries = new ArrayList();

			for (int thePos = 0; thePos < theList.length; thePos++) {
				Country theCountry = new Country (theList [thePos], aLanguage);
				if(theCountry.getName().length() == 2){
				    if(resolveCountryName(theCountry)) {
				        theCountries.add(theCountry);
				    }
				} else{
                    theCountries.add (theCountry);
				}
			}

			Collections.sort (theCountries);
			
			int theSize = theCountries.size();
			countries = new Country [theSize];
			for (int i=0; i<theSize; i++) {
				countries[i] = (Country) theCountries.get(i);
			}
			
			countryTable.put (aLanguage, countries);
		}

		return (countries);
	}
	
	private static boolean resolveCountryName(Country aCountry){
	    Map missingCountryTranslations = getMissingNamesMap();
	    if (missingCountryTranslations.containsKey(aCountry.getCode())){
	        getMissingName(aCountry.getCode(), aCountry.locale);
	        return true;
	    }
	    return false;
	}

    private static String getMissingName(String aCountryCode, Locale aLanguageLocale) {
        String[] values = (String[])getMissingNamesMap().get(aCountryCode);
        if(values != null){
            if("de".equals(aLanguageLocale.getLanguage())){
                return values[1];
            } else{
                return values[0];
            }            
        }

        return aCountryCode;
    }
	public static Map getMissingNamesMap(){
        return missingNamesMap;
	}

	/**
	 * Load the missing names from a text (ssv - file located parallel to the country class!
	 */
	private static Map<String, String[]> loadMissingNamesMap() {
		Map<String, String[]> aMap = new HashMap<>(50);
	    Properties props = new Properties();
	    
	    try {
            props.load(Country.class.getResourceAsStream("missingCodes_en_de.properties"));
            Enumeration iter = props.keys();
            while (iter.hasMoreElements()) {
                String key = (String) iter.nextElement();
                String[] value = props.getProperty(key).split(";");
                aMap.put(key, value);
            }
        }
        catch (Exception ex) {
            System.err.println("Unable to locate file with country names");
        }
		return Collections.unmodifiableMap(aMap);
	}
	
	/**
	 * Utility method to evaluate a country name for a given country code and a {@link Locale}
	 * defining the language to present the country name in.
	 * 
	 * @param aCode
	 *        uppercase two-letter ISO-3166 code for the country
	 * @param locale
	 *        a {@link Locale} defining the language to get the countries name in. Must not be
	 *        <code>null</code>.
	 */
	public static String getDisplayName(String aCode, Locale locale) {
		Objects.requireNonNull(locale, "Locale must not be null");
        
        if (getMissingNamesMap().containsKey(aCode)){
			return getMissingName(aCode, locale);
        } else {
			final Locale secondLoc = new Locale(locale.getLanguage(), aCode);
			String displayName = secondLoc.getDisplayCountry(locale);
            
            return displayName;
        }
    }

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Country) {
			return this.code.equals(((Country) obj).code);
		}
		else {
			return super.equals(obj);
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.code.hashCode();
	}

}
