/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;

import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.ElementHandler;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;

/**
 * Custom {@link FormContext} that has {@link FormField}s to create or edit a
 * {@link DemoAddress} object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoAddressContext extends FormContext {
    
	private final List<String> lazyCities = new LazyListUnmodifyable<>() {

	    /**
		 * Fetch the cities list from disk on demand.
		 * 
		 * @see com.top_logic.basic.col.DeferredReference#get()
		 */
		@Override
		protected List<String> initInstance() {
			try {
				return createCitiesList();
			} catch (Exception ex) {
				if (ex instanceof RuntimeException) 
					throw (RuntimeException) ex;
				throw new RuntimeException(ex);
			}
		}
	};
	
	public final StringField street = FormFactory.newStringField("street");
	{
		street.addConstraint(new StringLengthConstraint(0, 16));
		
	}
	public final StringField streetNumber = FormFactory.newStringField("streetNumber");
	public final StringField zipCode = FormFactory.newStringField("zipCode");
	public final SelectField city = FormFactory.newSelectField("city", lazyCities);
	public final SelectField cities = FormFactory.newSelectField("cities", lazyCities, true, false);

    public DemoAddressContext(String name) {
		this(name, ResPrefix.legacyClass(DemoAddressContext.class));
    }
    
	public DemoAddressContext(String name, ResPrefix i18nPrefix) {
        super(name, i18nPrefix);
        
        addMembers(new FormMember[] { 
        	street, streetNumber, zipCode, city, cities});
    }
    
    public void loadWith(DemoAddress address) {
        street.setValue(address.getStreet()); 
        streetNumber.setValue(address.getStreetNumber()); 
        zipCode.setValue(address.getZipCode()); 
        city.setValue(address.getCity()); 
    }
    
    public void saveIn(DemoAddress address) {
        address.street = street.getAsString();
        address.streetNumber = streetNumber.getAsString();
        address.zipCode = zipCode.getAsString();
        address.city = (String) city.getSingleSelection();
    }
    
    private static List<String> createCitiesList() 
    	throws IOException, ParserConfigurationException, SAXException, FactoryConfigurationError 
    {
    	final ArrayList<String> result = new ArrayList<>();
		SAXParser parser = SAXUtil.newSAXParser();
    	GZIPInputStream citiesStream = 
    		new GZIPInputStream(DemoAddressContext.class.getResourceAsStream("cities.xml.gz"));
		parser.parse(
    		citiesStream, 
    		new DispatchingHandler(true) {
    			{
    				registerHandler("city", new ElementHandler() {
    					@Override
						public void endElement(String uri, String localName, String qName) 
    						throws SAXException 
    					{
    						super.endElement(uri, localName, qName);
    						result.add(getString());
    					}
    				});
    			}
    		});
		citiesStream.close();
    	return result;
    }
    
    /**
     * Benchmark the reading time of the cities list.
     * 
     * <p>
     * Reading the cities list is "fast": "Time elapsed: 0.359s".
     * </p>
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, FactoryConfigurationError {
    	long elapsed = -System.currentTimeMillis();
    	
    	System.out.println("Reading cities list.");

    	List<String> citiesList = createCitiesList();
		System.out.println("Found " + citiesList.size() + " cities.");
    	
    	elapsed += System.currentTimeMillis();
    	
    	System.out.println("Time elapsed: " + (elapsed / 1000.0f) + "s");
    }
}
