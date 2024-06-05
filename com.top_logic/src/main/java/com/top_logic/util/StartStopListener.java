/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import com.top_logic.basic.version.Version;

/** 
 * Listener for initialize and destroy the system.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class StartStopListener extends AbstractStartStopListener implements ServletContextListener {

	@Override
	protected Version getVersion() {
		return Version.getApplicationVersion();
	}

    /**
     * Here sub-classes can add completely new initializations.
     * <p>
     * This is prefered to overriding the init method,
     * since it does some checks, exception handling etc.
     * which might get lost else.
     * </p>
     *
     * @param aContext Use this to derive resources etc.
     */
    @Override
	protected void initSubClassHook(ServletContext aContext) throws Exception {
        this.initPersons();
        this.initLogEntryConfiguration();
    }
    
    /**
     * Init the Persons from the Security System.
     * @see com.top_logic.knowledge.wrap.person.RefreshUsersTask
     * 
     * Note: destroys TLContext if one exists...
     */
    @Deprecated
    protected void initPersons() {
//        TokenContext     theTokenCtxt   = null;
//        KnowledgeBase theKB = KnowledgeBaseFactory.getDefaultKnowledgeBase();
//        theKB.begin();
//        try {
//            long start = System.currentTimeMillis();
//        	ThreadContext.pushSuperUser(); // else push superuser on old context
//
//            theTokenCtxt = PersonManager.getManager().initUserMap();
//            
//            if (theTokenCtxt != null && theTokenCtxt.isStateAcquired() && theTokenCtxt.check()) {
//                if (!theKB.commit())
//                    throw new RuntimeException("Failed to commit() in initPersons()");
//            }
//            else {
//            	Logger.warn("Init persons: Rolling back person initialisation because of token time - out",this);
//                theKB.rollback();
//            }
//            
//            long delta = System.currentTimeMillis() - start;
//            if (delta > 3000)
//                Logger.info("Init persons needed " + DebugHelper.getTime(delta), this);
//        } catch (Exception e) {
////          rollback anything not commited yet          
//            theKB.rollback();
//            Logger.error("Init persons: unexpected error during refreshing of users:", e, this);
//        } finally {
//            if(theTokenCtxt!=null){
//                try {
//                    theTokenCtxt.unlock();
//                }
//                catch (Exception ex) {
//                    // Ignore, but make sure the rest of finally is executed...
//                }
//            }else{
//                Logger.warn("Init persons: unable to get token for user refresh. Skipping this cycle",this);
//            }
//            
//            ThreadContext.popSuperUser(); // else pop super user on old context
//        }
    }
    
    /**
     * Is called from contextDestroyed before basic features are shut down.
     * 
     * @deprecated Services should not need this as they should know what to do when shutting down
     */
    @Deprecated
    @Override
	protected void exitSubClassHook(ServletContext context) {
    }

    @Deprecated
    protected void initLogEntryConfiguration() {
    }
}
