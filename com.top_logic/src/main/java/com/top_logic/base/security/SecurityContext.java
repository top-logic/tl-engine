/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security;

import java.util.StringTokenizer;

import com.top_logic.base.security.authorisation.roles.ACL;
import com.top_logic.base.security.authorisation.roles.RoleManager;
import com.top_logic.base.security.authorisation.symbols.Authorisation;
import com.top_logic.base.security.authorisation.symbols.AuthorisationManager;
import com.top_logic.base.security.authorisation.symbols.Symbol;
import com.top_logic.base.security.authorisation.symbols.SymbolException;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.util.TLContext;

/**
 * Developers interface to the SecurityContext. 
 *
 * All methods are static for ease of use. The most commonly used method will be
 * {@link #hasPermission(java.lang.String)} e.g. hasPermission("toplogic.
 * security.user.create") for checking the current users permission to 
 * create new users. Current user will be set by the dispatcher on entry 
 * into request processing.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SecurityContext {

    /** Protocol for a SecuritydataSourceAdaptor to fetch users from. */    
	public static final String SECURITY_PROTOCOL = "security://";

    /** Used by pushSuperUser to indicate the new Security */    
	public static final Integer SUPER1 = Integer.valueOf(1);

	/** a comprehensive empty array */
	private final static String[] EMPTY_ROLE_LIST = {};

	/** 
     * This class has static methods only, there is no use in 
     * creating any instance.
     */
	private SecurityContext() {
        // cannot create this
	}

	/**
	 * Method to determine if a user has a role.
	 *
	 * @param  role a user role 
	 * @return true if the user has the role
	 */
	public static boolean hasRole(String role) {
		return hasRole(getCurrentUser(), role);
	}

	/**
	 * Checks, if the current user has access according to the given ACL.
	 *
	 * @param    anACL    The ACL to be checked.
	 * @return   true, if the user has access.
	 */
	public static boolean hasAccess(ACL anACL) {
		UserInterface ui = getCurrentUser();
		return (isSuperUser(ui) || hasAccess(ui, anACL));
	}

	/**
	 * Checks, if the given user has access according to the given ACL.
	 *
	 * @param    aUser    The user to be checked.
	 * @param    anACL    The ACL to be checked.
	 * @return   true, if the given user has access or the given ACL is null.
	 */
	public static boolean hasAccess(UserInterface aUser, ACL anACL) {
		ACL theUserRoles = null;

		if (aUser != null) {
			theUserRoles = aUser.getACLRoles();
		}

		if (anACL != null) {
			return (anACL.hasAccess(theUserRoles));
		}
		else {
			return (true);
		}
	}

	/**
	 * Method to determine if the current user has one of a number of roles
	 *
	 * @param  roles an array of roles
	 * @return true if the user has the role or if the user id a super user.
	 */
	public static boolean hasAnyRole(String[] roles) {
		if (roles == null) { // no role list -> refuse access
			return false;
		}

		if (roles.length == 0) { // no restrictions specified -> grant access
			return true;
		}

		UserInterface user = getCurrentUser();

		if (isSuperUser(user)) { // super user mode -> no restrictions -> grant access
			return true;
		}

		// Test if the user has one of the given roles

		for (int i = 0; i < roles.length; i++) {
			if (hasRole(user, roles[i])) {
				return true;
			}
		}

		return false;
	} // hasAnyRole()----------------------------------------

	/**
	 * Combination of {@link #hasAnyRole(String[])} and parseRoleList(roles, ",").
	 *
	 * This combinations is needed very often. So this function is optimized.
	 *
	 * @param  roleList  an List of roles seperated by ','
	 * @return boolean   true if the user has the role or if the user is a super user.
	 */
	public static boolean hasAnyRole(String roleList) {

		if (roleList == null) // no role list -> refuse access
			return true;

		if (roleList.length() == 0) // no restrictions specified -> grant access
			return true;

		UserInterface user = getCurrentUser();

		if (isSuperUser(user)) // super user mode -> no restrictions -> grant access
			return true;

		if (user == null)
			return false;

		StringTokenizer theToken = new StringTokenizer(roleList, ",");

		while (theToken.hasMoreTokens()) {
			if (hasRole(user, theToken.nextToken())) {
				return true;
			}
		}

		return false; // no applicable security found.
	} // hasAnyRole()----------------------------------------

	/**
	 * Method to check if a user other than the current user 
	 * has a particular role. Use this only when doing 
	 * checks on somebody other than the current user.
	 *
	 * @param securityUser a user to be checked for a role
	 * @param role         the role to be checked for
	 * @return true if the role is present
	 */
	public static boolean hasRole(UserInterface securityUser, String role) {
		if (securityUser == null) {
			return false;
		}

		return (securityUser != null
				&& ( securityUser.getACLRoles().hasAccess(role)
			      || securityUser.getUserName().equals(role)));
	} // hasRole()----------------------------------------------

	/**
	 * Checks if the current user has at least one of the given roles.
	 *
	 * @param    roleList    a comma separated list of role names
	 * @return   true if no roles are given or the current user has
	 *           at least one of the given roles or the or the super user mode is set.
	 */
	public static String[] parseRoleList(String roleList, String delimitor) {
		if (roleList == null)
			return EMPTY_ROLE_LIST;

		StringTokenizer theToken = new StringTokenizer(roleList, delimitor);
		String[]        roles    = new String[theToken.countTokens()];
		int             i = 0;

		while (theToken.hasMoreTokens()) {
			roles[i++] = theToken.nextToken();
		}

		return (roles);
	} // parseRoleList()--------------------------------------------

	/**
	 * Checks ACL of symbol against roles of current user.
	 * Convienience developer method
	 *
	 * @param  symbol    a function / data symbol to be checked
	 * @return true if the user has permission
	 */
	public static boolean hasPermission(String symbol) throws SymbolException {
        Symbol theSymbol = null;
        try {
            theSymbol = getSymbol(symbol);
        } catch (SymbolException e) {
            Logger.warn("Symbol "+symbol+" not available.", SecurityContext.class);
        }
		return hasPermission(theSymbol);
	} // hasPermission()-------------------------------------

	/**
	 * Method to return the ACL object for a particular symbol. If the given
	 * symbol name is empty or null, null will be returned.
	 *
	 * @param    aSymbol    A function / data symbol.
	 * @return   The access control list for the symbol.
	 */
	public static Symbol getSymbol(String aSymbol) throws SymbolException {
		return SecurityContext.getAuthorisation().getSymbol(aSymbol);
	}

	/**
	 * Method to check a Symbol directly against a users roles. If the given aSymbol
	 * is null, false will be returned.
	 * 
	 * @return   true if the user has a role specified in the access control
	 *           list or the user is a super user.
	 */
	public static boolean hasPermission(Symbol aSymbol) {
		UserInterface ui = getCurrentUser();
		return isSuperUser(ui) || hasPermission(ui, aSymbol);
	}

	/**
	 * Check, whether the given user has the permission on the given symbol.
	 *
	 * The method extracts the roles from the user and compares this to the
	 * allow list stored in the symbol.
	 *
	 * @param    aUser      The user to be inspected.
	 * @param    aSymbol    The symbol containing the allow list.
	 * @return   true, if the user has the permission.
	 */
	protected static boolean hasPermission(UserInterface aUser, Symbol aSymbol) {
		if (aUser == null) {
			return false;
		}

		if (isAdmin(aUser)) {
			return true;
		}
        
        if (aSymbol == null) {
            return false;
        }

        ACL theUserACL = aUser.getACLRoles();
		return  theUserACL.hasAccess(aSymbol.getAllow())
            && !theUserACL.hasAccess(aSymbol.getDeny());
	}

	/**
	 * Checks symbol against roles of current user.
	 *
	 * Convienience method used to get exception if required.
	 *
	 * @param    aSymbol    A function / data symbol to be checked.
	 * @throws   UserSecurityException     If the user does not have the role.
	 */
	public static void checkPermission(String aSymbol)
                                        		throws UserSecurityException {
		if (!hasPermission(aSymbol)) {
			throw new UserSecurityException("Permission denied");
		}
	} // checkPermission()----------------------------------

	/**
	 * Checks symbol against roles of current user.
	 *
	 * Convienience method used to get exception if required.
	 *
	 * @param    aSymbol     The access control list to be checked.
	 * @param    aMessage    The required error message.
	 * @throws   UserSecurityException     If the user does not have.
	 */
	public static void checkPermission(Symbol aSymbol, String aMessage)
                                        		throws UserSecurityException {
		if (!hasPermission(aSymbol)) {
			throw new UserSecurityException(aMessage);
		}
	} // checkPermission()-------------------------------------

	/**
	 * Checks symbol against roles of a user.
	 *
	 * Convienience method used to get exception if required.
	 *
	 * @param    aUser       The user to be checked.
	 * @param    aSymbol     The access control list to be checked.
	 * @param    aMessage    The required error message.
	 * @throws   UserSecurityException     If the user does not have the role.
	 */
	public static void checkPermission(UserInterface aUser,
									   Symbol aSymbol,
									   String aMessage)
										         throws UserSecurityException {
		if (!hasPermission(aUser, aSymbol)) {
			throw new UserSecurityException(aMessage);
		}
	} // checkPermission()---------------------------------------
    

	/** 
     * This method returns the current user.
     * 
     * The user itself is held in the TLContext.
     * 
     * @return    The current user in this thread.
	 */
	public static UserInterface getCurrentUser() {
		TLContext context = TLContext.getContext();

		if (context != null) {
			return context.getCurrentUser();
        }
  		return null;
	}

	/**
	 * Check if the super user mode is set for the current user in the current thread,
	 * or if the given user is a SuperUser (= tl-admin-role).
	 *
	 * @return    true, if the super user mode is set or tl-admin-role applies.
	 */
	public static boolean isSuperUser(UserInterface ui) {
	    if (ui == getCurrentUser()) {
	        return ThreadContext.isSuperUser() || isAdmin(ui);
	    } else {
	        return isAdmin(ui);
	    }
	}

    /**
     * <code>true</code>, if the current user is super user or has the role "admin"
     */
    public static boolean isTechnicalAdmin() {
        return ThreadContext.isSuperUser() || SecurityContext.hasRole("admin");
    }

    /**
	 * Check if the current user is an administrator (tl-admin).
	 *
	 * @return    true, if user's roles contain tl-admin
	 */
	public static boolean isAdmin() {
		return isAdmin(getCurrentUser());
	}

	/**
	 * Check if the given user is an administrator (tl-admin).
	 *
	 * @return    true, if user's roles contain tl-admin
	 */
	public static boolean isAdmin(UserInterface aUser) {
		if (aUser == null) {
			return false;
		}

		ACL theACL = aUser.getACLRoles();

		return ((theACL != null) && theACL.hasRole(RoleManager.ADMIN_ROLE));
	}

	/**
	 * Method to get the instance responsible for authorisation.
	 * 
	 * @return    The instance for authorisation.
	 */
	public static Authorisation getAuthorisation() {
		return AuthorisationManager.getAuthorisation();
	}

}
