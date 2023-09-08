/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.dsa.ldap;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dsa.DataAccessException;
import com.top_logic.util.TopLogicDirContext;

/**
 * Connnector to an external directory system via LDAP.
 * 
 * Provides read only access only.
 * 
 * @author    <a href="mailto:tri@top-loigc.com">Thomas Trichter</a>
 */
public class LDAPAccessService {
	
	/**
	 * Constants allowing to define the searchScope when executing a query
	 */
	public static final int OBJECT_SCOPE   = SearchControls.OBJECT_SCOPE;
	public static final int ONELEVEL_SCOPE = SearchControls.ONELEVEL_SCOPE;
	public static final int SUBTREE_SCOPE  = SearchControls.SUBTREE_SCOPE;

	/**
	 * Configuration params of this instance
	 */
	protected ServiceProviderInfo cfgParams;

	/** The connections to LDAP currently not used. */
	protected List unusedConnections;
	
	/**
	 * Map which holds the instances for each requested configuration
	 */
	private static Map theInstances;

	
	protected LDAPAccessService(ServiceProviderInfo theInitParams) {
		super();
		this.cfgParams = theInitParams;
		this.unusedConnections = new ArrayList();
	}
	
	protected ServiceProviderInfo getServiceProviderInfo() {
		return this.cfgParams;
	}

	/**
	 * the requested instance according to the given configuration
	 */
	public static synchronized LDAPAccessService getInstance(ServiceProviderInfo theInitParams){
		if (LDAPAccessService.theInstances == null){
			LDAPAccessService.theInstances = new HashMap();
		}
		
		LDAPAccessService theInstance = (LDAPAccessService)LDAPAccessService.theInstances.get(theInitParams);
		if(theInstance == null){
			theInstance = new LDAPAccessService(theInitParams);
			LDAPAccessService.theInstances.put(theInitParams,theInstance);
		}
		
		return theInstance;
	}
	
	/**
	 * Tries to create a LDAP context. To build this, this method uses the data
	 * coming from {@link #getServiceProviderInfo()}.
	 * 
	 * While trying the hostname of serviceproviderinfo is resolved into the IPs
	 * using DNS, then all of the IPs beeing configured for this hostname are
	 * tried until a context is created
	 * 
	 * @return The requested context or null if none of the IPs worked.
	 * 
	 * @throws SecurityException
	 *             If creation fails.
	 */
	protected DirContext createContext() throws NamingException {
		//get copy of the the provider infos
		return createContext(getServiceProviderInfo());
	}

	/**
	 * Tries to create a LDAP context from the given {@link ServiceProviderInfo}.
	 * 
	 * While trying the hostname of {@link ServiceProviderInfo} is resolved into the IPs using DNS,
	 * then all of the IPs being configured for this hostname are tried until a context is created.
	 * 
	 * @return The requested context or <code>null</code> if none of the IPs worked.
	 * 
	 * @throws SecurityException
	 *         If creation fails.
	 */
	public static DirContext createContext(ServiceProviderInfo spi) throws NamingException {
			return new TopLogicDirContext(spi);
	}

	/**
	 * Get the context from the LDAP.
	 * 
	 * If there is no context usable, the method will create a new one. The
	 * created context is a subclass of DirContext, to avoid the connection
	 * timeout from special LDAP services (like ADS).

	 * @return The context to be used.
	 * 
	 * @throws SecurityException
	 *             If creation fails.
	 */
	public synchronized DirContext getContext() throws NamingException {
		DirContext theContext = null;
		//try to get a valid context from those which have been created before
		if (!unusedConnections.isEmpty()) {
			theContext = (DirContext) this.unusedConnections.get(0);
			this.unusedConnections.remove(0);
		}
		// if it was not possible to reuse a previously created context, create
		// a new one
		if (theContext == null) {
				theContext = this.createContext();
		}
		return (theContext);
	}

    /**
     * Release the context from the LDAP.
     * Which means putting the given context into a list
     * of unused contexts for later use
     * 
     * @param aContext
     *            The context to be released.
     * @param dropContext 
     * 			  If this caused a communication exception, it was not valid (probably IP change of AD server), it should not be reused, but dropped then
     */
    public synchronized void releaseContext(DirContext aContext, boolean dropContext) {
        if(!dropContext){
            this.unusedConnections.add(aContext);
        }else{
            try {
//              ... try to close
                aContext.close(); 
            } catch (NamingException e1) {
                Logger.warn("failed to close ldapcontext",e1,this);
            }
//			... set to null                
            aContext = null;      
            unusedConnections.clear();//clear cache, because if this context is not valid, the others in this cache aren't either
        }
    }
    
    /**
     * Execute the given query and return the result of it as Enumeration.
     * This query should define the name of the attribute and the value of
     * that attribute. Additionally, a search DN can be specified which
	 * is appended to the base DN.
     *
	 * @param	 aSearchDN	 the full DN (including base) of the node to search in. If null or empty, configured root node is used.
     * @param    aPattern    The search pattern. (the query)
     * @param    someAttr    The list of attributes to be returned. If null all attribs are returned
     * @param	 searchScope The Scope of the search as defined by the constants of this class
     * @return   The result as enumeration.
     */
    public Enumeration executeQuery (String aSearchDN, String aPattern,	String [] someAttr, int searchScope) throws NamingException {
        Enumeration theEnum    = null;
        DirContext  theContext = this.getContext ();
        boolean dropContext = false;
        try {
            SearchControls theControls = new SearchControls ();
            theControls.setSearchScope (searchScope);
            theControls.setReturningAttributes (someAttr);
			if(StringServices.isEmpty(aSearchDN)){
			    aSearchDN = (String)getServiceProviderInfo().get(ServiceProviderInfo.KEY_SEARCH_BASE);
			}
            theEnum = theContext.search (aSearchDN, aPattern, theControls);
        }
        catch (NamingException ex) {
            dropContext=true;
            throw new DataAccessException ("Unable to execute LDAP query for " +
                                           "pattern \"" + aPattern + "\"!", 
                                           ex);
        }
        finally {
            this.releaseContext (theContext,dropContext);
        }
        return (theEnum);
    }
}