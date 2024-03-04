/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;


import java.util.Collection;
import java.util.Collections;

import jakarta.servlet.ServletContext;

import com.top_logic.basic.Logger;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.services.ServletContextService;

/**
 * A (hopefully) small set of functions to work around bugs in J2EEContainers.
 * 
 * As Experience has show some J2EE containers do have bugs that will
 * not be fixed and we have to cope with them.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ContainerDetector extends ManagedClass {
    
    static ContainerDetector instance; 
    
    /** Major Version of J2ee Specification*/
    protected int major;

    /** Minor Version of J2ee Specification*/
    protected int minor;

    /** Info about Server */
    protected String info;
    
    /** Set when this is a WepShere Application server */
    boolean isWAS;
    
    /** Set when this is a BEA WebLogic server */
    boolean isBEA;

    /** Set when this is a Sun GlassFish Enterprise Server */
    boolean isGlassFish;

    /** Set when we are started by HTTPUnit */
    boolean testing;

    /**
     * Create a ContainerDetector via a ServletContext (starting with J2EE 2.3)
     */
    protected ContainerDetector() {
    	ServletContext context = ServletContextService.getInstance().getServletContext();
        major   = context.getMajorVersion();
        minor   = context.getMinorVersion();
        info    = context.getServerInfo();
        
        Logger.info("Detected '"+ info + "' J2EE Container Version " + major + "." + minor, this);

        isWAS       = info.startsWith("IBM WebSphere Application Server");
        isBEA       = info.startsWith("WebLogic Server");
        isGlassFish = info.startsWith("Sun GlassFish Enterprise Server");
        testing = info.indexOf("test") >= 0;
    }

    /**
     * Set ContentLength is Broken for WAS 4.0, 5.0.1 and 5.0.2
     * 
     * for 5.0.1 and 5.0.2 there is a fix, but I do not know how to check this :-(
     * See <a href="http://www.ibm.com/support/docview.wss?uid=swg1PQ74921">PQ74921</a>
     * 
     * @return true when HpttpServletRequest.setContentLength is OK to use.
     */
    public boolean setContentLength() {
        // This will catch 4.0 only since I have no 5.x around (yet) KHA
        // WAS 4.0 is major = 2, minor = 2
        return !isWAS || major > 2 || minor > 2;
    }
    
    /**
     * Return true when started via ServletUnit. 
     */
    public boolean isTesting() {
        return testing;
    }
    
    /**
     * true in case we detected a BEA WebLogic Server
     */
    public boolean isBEA() {
        return (this.isBEA);
    }

    /**
     * true in case we detected a Sun GlassFish Server.
     */
    public boolean isGlassFish() {
        return (this.isGlassFish);
    }

    /**
     * May return null when not running in a WebContainer.
     */
    public static ContainerDetector getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }
    
	public static final class Module extends BasicRuntimeModule<ContainerDetector> {

		public static final Module INSTANCE = new Module();

		private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES
			= Collections.singletonList(ServletContextService.Module.class);

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return DEPENDENCIES;
		}

		@Override
		public Class<ContainerDetector> getImplementation() {
			return ContainerDetector.class;
		}

		@Override
		protected ContainerDetector newImplementationInstance() throws ModuleException {
			return new ContainerDetector();
		}
	}

}
