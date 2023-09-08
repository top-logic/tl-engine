/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.base.security.authorisation.roles.ACL;
import com.top_logic.base.security.authorisation.symbols.Symbol;
import com.top_logic.base.security.authorisation.symbols.SymbolFactory;
import com.top_logic.basic.AbstractReloadable;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.DataAccessException;


/**
 * File based SymbolFactory.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FileSymbolFactory extends AbstractReloadable implements SymbolFactory {

	/**
	 * {@link ConfigurationItem} describing the structure of a symbols file.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * List of symbol definitions.
		 */
		List<SymbolConfig> getSymbols();
	}

	/**
	 * Configuration of a symbol.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface SymbolConfig extends NamedConfigMandatory {

		/**
		 * Comma separated list of ACL roles. User with such a role are allowed.
		 */
		String getAllow();

		/**
		 * Comma separated list of ACL roles. User with such a role are denied.
		 */
		String getDeny();

	}

    /** The path to the symbols. */
    private static final String DEFAULT_SYMBOL_PATH = "/WEB-INF/database/Symbols.xml";

    /** The only instance of this class. */
    private static FileSymbolFactory singleton;

    /** The map containing all symbols. */
    private Map symbolMap;

    private String symbolPath;

    /**
     * Protected constructor to match singleton pattern.
     */
    protected FileSymbolFactory (String symbolPath) {
    	if(StringServices.isEmpty(symbolPath)){
    		this.symbolPath = DEFAULT_SYMBOL_PATH;
    	}else{
    		this.symbolPath = symbolPath;
    	}
    	
    	// Not allowed, see super-class constructor and getInstance():
    	// Registration is performed after initialization.
    	//
    	// ReloadableManager.getInstance().addReloadable(this);
    }

    /**
     * Method to create a new instance of symbol
     *
     * @return    A new, empty symbol object.
     */
    @Override
	public Symbol createSymbol () {
        return new FileSymbol ();
    }

    /**
     * Method to load the requested symbol from the system.
     *
     * @param 	aName    The name of the required symbol.
     * @return	The symbol, contains user information for the requested name.
     * @throws	DataAccessException    If the method encounters an error 
     *                                 accessing the required information.
     */
    @Override
	public Symbol loadSymbol (String aName) {
        return ((Symbol) this.getMap ().get (aName));
    }

    /**
     * Return all symbols known by the system.
     *
     * @return    A list of known symbols.
     */
    @Override
	public List getAllSymbols () {
        Collection theColl = this.getMap ().values ();

        if (theColl instanceof List) {
            return ((List) theColl);
        }
        else {
            return new ArrayList (theColl);
        }
    }

    /**
     * Method to save alterations to a symbols information to the ldap directory.
     *
     * @param    aSymbol    The object containing the symbol information 
     * 					    to be saved to the directory.
     * @throws	DataAccessException    If the method encounters an error 
     *                                 accessing the required information.	 
     */
    @Override
	public void saveSymbol (Symbol aSymbol) {
        Map theMap = this.getMap ();

        theMap.put (aSymbol.getSymbolName (), aSymbol);

        this.storeAllSymbols ();
    }

    /**
     * Method to add a new Symbol to the directory.
     *
     * @param    aSymbol    The user details to be added to the directory.
     * @throws	 DataAccessException    If the method encounters an error 
     *                                  accessing the required information.	 
     */
    @Override
	public void addSymbol (Symbol aSymbol) {
        this.saveSymbol (aSymbol);
    }

    /**
     * Method to make multiple searches to the directory.
     *
     * @param     aName     The name of the symbol's attribute.
     * @param     aValue    The value of the symbol's attribute.
     * @return    A list of symbol objects. 
     * @throws	  DataAccessException     If the method encounters an error 
     *                                   accessing the required information.	 
     */
    @Override
	public List getSelectedSymbols (String aName, String aValue) {
        List   theList   = new ArrayList (1);
        Symbol theSymbol = this.loadSymbol (aName);

        if (theSymbol != null) {
            theList.add (theSymbol);
        }

        return (theList);
    }

    /**
     * Method to delete a named Symbol from the directory.
     *
     * @param    aName    The name of the Symbol to be deleted.
     */
    @Override
	public void deleteSymbol (String aName) {
        this.getMap ().remove (aName);

        this.storeAllSymbols ();
    }

    /**
     * Returns the map containing all known symbols.
     *
     * @return    The requested map.
     */
    protected Map getMap () {
        if (this.symbolMap == null) {
            this.symbolMap = this.initMap ();
        }

        return (this.symbolMap);
    }

    /**
     * Store the symbols to the file system.
     */
    protected void storeAllSymbols () throws DataAccessException {
        Map theMap    = this.getMap ();
        int theResult = new FileSymbolWriter (this).write (theMap);

        if (theResult < theMap.keySet ().size ()) {
            throw new DataAccessException ("Writing of symbols incomplete (" +
                                           "only " + theResult + " written)!");
        }
    }

    protected String getSymbolPath(){
    	return this.symbolPath;
    }
    /**
     * Create and initialize the map of all symbols.
     *
     * @return    The new created and filled map.
     */
    protected Map initMap () {
        Map theMap = new HashMap ();

        this.loadSymbols (theMap);

        return (theMap);
    }

    /**
     * Load the symbols from the file system into the given map.
     *
     * @param    aMap    The map to be filled.
     */
    protected void loadSymbols (Map aMap) {
		try {
			BinaryData symbolFile = FileManager.getInstance().getData(getSymbolPath());
	        
			Protocol protocol = new LogProtocol(FileSymbolFactory.class);
			Map<String, ConfigurationDescriptor> globalDescriptors = Collections.emptyMap();
			ConfigurationItem readFile = ConfigurationReader.readContent(protocol, globalDescriptors, symbolFile);
			protocol.checkErrors();

			if (!(readFile instanceof Config)) {
				Logger.error(
					"File " + getSymbolPath() + " has not the correct structure as defined in "
						+ Config.class.getName(), FileSymbolFactory.class);
				return;
			}
			List<SymbolConfig> symbolConfigs = ((Config) readFile).getSymbols();
			if (symbolConfigs.isEmpty()) {
				return;
			}

			for (SymbolConfig config : symbolConfigs) {
				String symbolName = config.getName();
				Symbol symbol = createSymbol();
				symbol.setSymbolName(symbolName);
				symbol.setAllow(new ACL(config.getAllow()));
				symbol.setDeny(new ACL(config.getDeny()));

				aMap.put(symbolName, symbol);
			}

		} 
		catch (Exception ex) {
			Logger.fatal("Unable to load symbols!", ex, this);
		}
    }

    /**
     * Extracts the value of the given key from the matching child of the
     * given element.
     *
     * This method expects, that there is one child of the given element,
     * which has an attribute matching to the given key and returns the
     * attribute value from that found element.
     *
     * @param    anElem    The element to be inspected.
     * @param    aKey      The name attribute of the child to be found.
     * @return   The value attribute of the found child or null.
     */
    protected String getEntry (Element anElem, String aKey) {
        String theName;
        String theResult = null;
        Node   theNode   = anElem.getFirstChild ();

        while ((theResult == null) && (theNode != null)) {
            if (theNode instanceof Element) {
                theName = ((Element) theNode).getAttribute ("name");

                if (aKey.equals (theName)) {
                    theResult = ((Element) theNode).getAttribute ("value");
                }
            }

            if (theResult == null) {
                theNode = theNode.getNextSibling ();
            }
        }

        return (theResult);
    }

    /**
     * Method to return the only instance of this class.
     *
     * @return    The only instance of this class.
     */
    public static FileSymbolFactory getInstance (String symbolPath) {
        if (singleton == null) {
            FileSymbolFactory newInstance = new FileSymbolFactory (symbolPath);
            
            // TODO: This class seem not to be "truely" reloadable, make it so.
            newInstance.registerForReload();
            
			singleton = newInstance;
        }

        return (singleton);
    }
    
    /** TODO: Provide a name, after making this class "really" {@link Reloadable}. */
    @Override
	public String getName () {
        return null;
    }

    /** TODO: Provide a description, after making this class "really" {@link Reloadable}. */
    @Override
	public String getDescription () {
        return null;
    }
    
//
//    public static void main (String [] args) {
//        FileSymbolFactory theFac  = new FileSymbolFactory ();
//        List              theList = theFac.getAllSymbols ();
//        Iterator          theIt   = theList.iterator ();
//
//        while (theIt.hasNext ()) {
//            System.out.println (theIt.next ());
//        }
//
//        theFac.storeAllSymbols ();
//    }
}
