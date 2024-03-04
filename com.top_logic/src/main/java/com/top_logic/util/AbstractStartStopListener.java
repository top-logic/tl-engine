/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.NodeState;
import com.top_logic.base.cluster.ClusterManagerException;
import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.Environment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.WebappFileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.basic.thread.LoggingExceptionHandler;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.version.Version;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mig.html.ContainerDetector;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.license.InvalidLicenceException;
import com.top_logic.util.license.LicenseTool;

/**
 * This class defines what a minimum StartStopListener must do.
 *
 * In case you do not setup some basic components (DataSources, Knowledgebase)
 * you wont last very long, so think carefully what you set up.
 *
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class AbstractStartStopListener implements ServletContextListener {

	/**
	 * Message to dump when password was set up.
	 */
	public static final Property<String> PASSWORD_INITIALIZATION_MESSAGE =
		TypedAnnotatable.property(String.class, "passwordInitializationMessage");

	private static Date STARTUP_DATE = null;

	/** Validity time of the {@link #_startupToken}. */
	private long _lockTimeout;

    /** Time to sleep, if another node is currently starting up. */
	private long _startupSleep;

    /** This flag indicates whether system should enter maintenance window mode on startup. */
    private boolean enterMaintenanceMode;

    /** {@link Lock} to synchronize cluster startup. */ 
	private Lock _startupToken;

	private boolean _deferredBoot;

	/** Indicating whether an error occurred during startup. */
	private boolean errorOnStartup;

	/** Startup context for additional {@link BasicRuntimeModule} startups. */
	protected ModuleContext _moduleContext;

    /**
     * Need empty default CTor for invocation by container.
     */
    public AbstractStartStopListener() {
        super();
    }

    /** Override to return the Version of your package */
    protected abstract Version getVersion();

	/**
	 * Transient initializations that do not require a database commit.
	 * 
	 * @param aContext
	 *        use to extract environment as needed
	 * 
	 * @see #initSubClassHook(ServletContext)
	 */
    protected void initBasics(ServletContext aContext) throws Exception {
		// empty here, override as needed
    }

	/**
	 * Hook for setup operation that potentially touch persistent state.
	 * 
	 * <p>
	 * Code in this hook is wrapped by begin() and commit() on the default
	 * {@link KnowledgeBase}.
	 * </p>
	 * 
	 * @see #initBasics(ServletContext)
	 */
    protected abstract void initSubClassHook(ServletContext context) throws Exception;

    /**
	 * Setup basic system and call {@link #initSubClassHook(ServletContext)}.
	 *
	 * <p>
	 * Declared <code>final</code> to enforce minimal consistent error
	 * reporting in case anything is going wrong during startup.
	 * </p>
	 */
    @Override
	public final void contextInitialized(ServletContextEvent event) {
		_moduleContext = ModuleUtil.beginContext();

        final ServletContext context = event.getServletContext();
        errorOnStartup = false;
        
        try {
			ServletContextService.startUpServletContextService(context);

        	ModuleUtil.INSTANCE.startUp(ContainerDetector.Module.INSTANCE);

			ContainerDetector container = ContainerDetector.getInstance();
			final boolean isTest = container.isTesting();

    		if (!isTest) {
				File application = ServletContextService.getInstance().getApplication();
				if (Environment.isDeployed()) {
					FileManager.setInstance(new WebappFileManager(context, new DefaultFileManager(application)));
				} else {
					FileManager.setInstance(MultiFileManager.createForModularApplication());
				}
    		}

        	if (!isTest) {
				LicenseTool.getInstance().printHeader();

        		System.out.println("**** Starting ****");
        	}

			long startTime = now();
        	
			XMLProperties.restartXMLProperties(initXMLProperties(context));
			/* Start ThreadContextManager as early as possible (i.e. direct after XML properties),
			 * because each Access to TLContext fails. (#14544) */
			ModuleUtil.INSTANCE.startUp(ThreadContextManager.Module.INSTANCE);

        	if (!isTest) { // Leave logging as configured by test ...
        		ModuleUtil.INSTANCE.startUp(LoggerAdminBean.Module.INSTANCE);
        	}
			/* Set the UncaughtExceptionHandler as soon as the logger is configured: The
			 * UncaughtExceptionHandler should be initialized as soon as possible to catch as many
			 * problems as possible. But it might need the logger. */
			initDefaultUncaughtExceptionHandler();

			this.checkJavaTmpDir();

			if (!isTest) {
				Version.getApplicationVersion().printVersions(System.out);
			}

			this.configure();

			if (_deferredBoot) {
				DeferredBootService.startUp(new Computation<Exception>() {
					@Override
					public Exception run() {
						try {
							boot();
							return null;
						} catch (Exception ex) {
							return ex;
						}
					}
				});
			} else {
				boot();
			}

			Logger.info("Application startup took " + (DebugHelper.getTime(now() - startTime)),
				AbstractStartStopListener.class);

			STARTUP_DATE = new Date();

            if (!isTest) {
                System.out.println("***** up and running (" + this.getCurrentTime() + ") *****");
            }

        }
		catch (InvalidLicenceException ex) {
			System.err.println();
			System.err.println(ex.getMessage());
			System.err.println();
			errorOnStartup = true;
			shutdownApplication(context);
			throw new RuntimeException(ex);
		}
        catch (RuntimeException ex) {
            reportProblem(ex);
            errorOnStartup = true;
			shutdownApplication(context);
            // RuntimeException instances can be thrown without being wrapped.
            throw ex;
        }
        catch (Error ex) {
        	reportProblem(ex);
        	errorOnStartup = true;
			shutdownApplication(context);
            // Error instances can be also thrown without being wrapped.
        	throw ex;
        } catch (Throwable ex) {
            reportProblem(ex);
            errorOnStartup = true;
			shutdownApplication(context);
            // All other Exception instances must be wrapped into a RuntimeException.
            throw new RuntimeException(ex);
        }
	}

	void boot() throws Exception {
		// Run in systemContext as some Modules needs an existing ThreadContext
		Exception problem =
		    ThreadContext.inSystemContext(this.getClass(), new Computation<Exception>() {
		    	@Override
				public Exception run() {
		    		try {
						System.out.println("Starting module system ...");
						ClusterManager.startUpClusterManager();
						ClusterManager clusterManagerInstance = ClusterManager.getInstance();

						startTokenSystem(clusterManagerInstance);
						try {
							startupModuleSystem(Version.getApplicationVersion());
							System.out.println("Module system started successfully");

							initApplication(ServletContextService.getInstance().getServletContext());

							if (!ContainerDetector.getInstance().isTesting()) {
								infoAndStarter(Version.getApplicationVersion());
							}
						} catch (InvalidLicenceException e) {
							return e.dropStack();
						} finally {
							releaseTokenSystem(clusterManagerInstance);
						}

						String message = TLContext.getContext().get(PASSWORD_INITIALIZATION_MESSAGE);
						if (message != null) {
							System.out.println();
							System.out.println("*****" + "*".repeat(message.length()) + "*****");
							System.out.println("***  " + " ".repeat(message.length()) + "  ***");
							System.out.println("***  " + message + "  ***");
							System.out.println("***  " + " ".repeat(message.length()) + "  ***");
							System.out.println("*****" + "*".repeat(message.length()) + "*****");
							System.out.println();
						}

						return null;
					} catch (Exception e) {
						System.out.println("System startup failed.");
						return e;
					}
		    	}

		    });
		
		if (problem != null) {
			throw problem;
		}
	}

    /**
	 * Dump the given problem to {@link System#err} since this is the only
	 * destination that is save to write to at this early setup stage.
	 */
	private void reportProblem(Throwable problem) {
		System.err.println();
		System.err.println("Failed to start due to the following reason:");
		problem.printStackTrace();
		System.err.println();
	}

    /**
     * This method initializes the application.
     */
    protected void initApplication(ServletContext aContext) throws Exception {
        Version theVersion = this.getVersion();

        this.initBasics(aContext);

        KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
        Transaction tx = kb.beginTransaction(
        	Messages.APPLICATION_STARTUP__NAME_VERSION.fill(theVersion.getName(), theVersion.getVersionString()));
        {
        	this.initSubClassHook(aContext);

            if (enterMaintenanceMode) {
                Logger.info("Entering maintenance mode.", AbstractStartStopListener.class);
                MaintenanceWindowManager.getInstance().enterMaintenanceWindow();
            }
        }
        tx.commit();

        /* Initialise security. */
		AccessManager.getInstance().initSecurityAfterStartup();
    }

	/**
	 * Create an XMLPropertiesConfig object for the given ServletContext.
	 * 
	 * Override in case you use a different setup of your XMLProperties.
	 * 
	 * @throws Exception
	 *         in case the Configuration is broken beyond repair.
	 */
	protected XMLPropertiesConfig initXMLProperties(final ServletContext context) throws Exception {
		return new XMLPropertiesConfig(context.getContextPath(), context.getRealPath("/") == null, null);
    }

	/**
	 * Sets the {@link Thread#setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler) default
	 * UncaughtExceptionHandler}.
	 * <p>
	 * Hook for subclasses, in case an application has to prevent this or wants to use another
	 * {@link LoggingExceptionHandler}.
	 * </p>
	 */
	protected void initDefaultUncaughtExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(LoggingExceptionHandler.INSTANCE);
	}

    /**
     * Configure variables from the StartStopListener section. 
     */
	private void configure() {
		StartStopListenerConfig cfg = ApplicationConfig.getInstance().getConfig(StartStopListenerConfig.class);

		enterMaintenanceMode = cfg.getEnterMaintenanceMode();
		_deferredBoot = cfg.getDeferredBoot();
		_lockTimeout = cfg.getLockTimeout();
		_startupSleep = cfg.getStartupSleep();
    }

    /**
     * Check if the 'java.io.tmpdir' is an existing directory. 
     * 
     * It is fatal if this directory does not exist.
     */
    private void checkJavaTmpDir() throws FileNotFoundException {
        String ioTmpDirPath = System.getProperty("java.io.tmpdir");
        File   ioTmpDir     = new File(ioTmpDirPath);
        if (!ioTmpDir.exists()) {
            String msg = "java.io.tmpdir='" + ioTmpDirPath + "' does not exists";
            System.err.println(msg);
			Logger.fatal(msg, AbstractStartStopListener.class);
            throw new FileNotFoundException(msg);
        }
    }

    /**
     * Returns the current time as string.
     *
     * This method will be used to inform about the starting time of the server.
     *
     * @return    The current time as string.
     */
    private String getCurrentTime() {
        return CalendarUtil.newSimpleDateFormat("hh:mm:ss dd.MM.yyyy").format (new Date());
    }

    /**
     * Return (type) name of active Knowledgebase.
     */
    private String getKBase () {
        KnowledgeBase kBase = PersistencyLayer.getKnowledgeBase();
        if (kBase == null)
            return null;
        
        final String NAME = "KnowledgeBase :";
        String theName = kBase.getName();
        if (theName.startsWith (NAME)) {
            theName = theName.substring (NAME.length ()).trim ();
        }

        return theName;
    }

    /**
     * Display startup info for the System on standard out.
     */
	void infoAndStarter(Version aVers) {
        System.out.println ();
        System.out.println ("    Application: " + aVers.toString());
        System.out.println ("  Knowledgebase: " + this.getKBase ());
        System.out.println ("User management: " + getDataAccessDeviceIDs());
        System.out.println (" Authentication: " + getAuthenticationDeviceIDs());
       
        /*
         * DebugHelper.infoAndStarter provide information that doesn't
         * depend on a WebApplication such as OS, User and Java. 
         */
        DebugHelper.infoAndStarter();

		System.out.println("       Web Path: " + ServletContextService.getInstance().getApplication().getPath());
    }

    /**
     * the configured AuthenticationDeviceIDs as formatted String
     */
    private String getAuthenticationDeviceIDs(){
    	return getSecurityDeviceIDsAsString(false);
    }

    /**
     * the configured DataAccessDeviceIDs as formatted String
     */
    private String getDataAccessDeviceIDs(){
    	return getSecurityDeviceIDsAsString(true);
    }

    /**
     * Helper Method to get the configured security device IDs and return them as formatted string.
     *
     * @param    flag    Indicates whether the DataAccess or Authentication DevIDs should be returned,
     *                   true for DataAccessDevice IDs...
     * @return   The requested DevIDs as String.
     */
    private String getSecurityDeviceIDsAsString(boolean flag) {
        StringBuffer            theResult  = new StringBuffer();
    	TLSecurityDeviceManager theManager = TLSecurityDeviceManager.getInstance();
    	Collection              theColl    = (flag) ? theManager.getConfiguredDataAccessDeviceIDs()
                                                    : theManager.getConfiguredAuthenticationDeviceIDs();

        for (Iterator theIt = theColl.iterator(); theIt.hasNext(); ) {
    		theResult.append(theIt.next());

            if (theIt.hasNext()) {
    			theResult.append(", ");
    		}
    	}

        return theResult.toString();
    }

    /**
     * Is called from contextDestroyed before basic features are shut down.
     *
     * You can still use System.out and err here.
     */
    protected abstract void exitSubClassHook(ServletContext context);

    /**
     * Call {@link #exitSubClassHook(ServletContext)} then tear down anything we can.
     */
    @Override
	public final void contextDestroyed(ServletContextEvent anEvent) {
		if (!errorOnStartup) {
			/* Avoid executing shutdown twice when exception was thrown during startup
			 * and container calls contextDestroyed in reaction. */
			shutdownApplication(anEvent.getServletContext());
		}
    }

	/**
	 * Shuts the application down.
	 */
	protected void shutdownApplication(ServletContext servletContext) throws Error {
		System.out.println("***** " + getVersion().getName() + " is going down! *****");

        try {
            clusterShutdown();
			exitSubClassHook(servletContext);
            shutDownModuleSystem();
        }
        catch (RuntimeException e) {
			Logger.error("contextDestroyed failed with an runtime exception: " + e.getMessage(), e,
				AbstractStartStopListener.class);
            throw e;
        }
        catch (Error e) {
			Logger
				.error("contextDestroyed failed with an error: " + e.getMessage(), e, AbstractStartStopListener.class);
            throw e;
		}

        // help the RMI Process(es) to calm down
        // From jdk141/docs/guide/rmi/faq.html#noexit
        System.gc();
        System.runFinalization();

//        // release default connection a clean way.
//        this.closeStmCache();
//        ContainerDetector.releaseInstance();
	}

	/**
	 * Set {@link NodeState#SHUTDOWN} in ClusterManager.
	 */
	private void clusterShutdown() {
		if (ClusterManager.Module.INSTANCE.isActive()) {
			Class<? extends AbstractStartStopListener> contextClass = this.getClass();
			ThreadContext.inSystemContext(contextClass, new Computation<Void>() {
				@Override
				public Void run()
				{
					try {
						ClusterManager.getInstance().setNodeState(NodeState.SHUTDOWN);
					}
					catch (ClusterManagerException cmx) {
						System.err.println("Failed to set SHUTDOWN in Cluster Manager, still shuttind down");
						cmx.printStackTrace();
					}
					return null;
				}
			});
		}
	}

	/**
	 * Start {@link LockService} eventually {@link #aquireStartupContext()} and set
	 * {@link NodeState#STARTUP}
	 */
	protected final void startTokenSystem(ClusterManager cMgr) throws Exception {
		ModuleUtil.INSTANCE.startUp(LockService.Module.INSTANCE);

        if (cMgr.isClusterMode()) {
			ModuleUtil.INSTANCE.startUp(PersistencyLayer.Module.INSTANCE);

			_startupToken = aquireStartupContext();
        }
		cMgr.setNodeState(ClusterManager.NodeState.STARTUP);
    }

	/**
	 * Set {@link NodeState#RUNNING} and eventually release {@link #_startupToken}
	 */
	protected final void releaseTokenSystem(ClusterManager cmGr) {
		cmGr.setNodeState(NodeState.RUNNING);
		if (_startupToken != null) {
			_startupToken.unlock();
			_startupToken = null;
		}
	}

	private Lock aquireStartupContext() throws InterruptedException {
		ClusterManager cm = ClusterManager.getInstance();
		Lock lock = createStartupToken();
		long maxWait = Math.max(_lockTimeout, lock.getLockTimeout() + _startupSleep);
		long waitTimeout = now() + maxWait;
		while (true) {
			cm.updateLifesign();

			if (lock.tryLock()) {
				return lock;
			}

			if (now() > waitTimeout) {
				throw new RuntimeException(
					"Failed to aquire startup token, max wait time exceeded timout of "
							+ DebugHelper.getTimeOnly(maxWait) + ".");
			}

			Logger.info("Could not lock startup token sleeping for " + DebugHelper.getTimeOnly(_startupSleep) + ".",
				AbstractStartStopListener.class);
			Thread.sleep(_startupSleep);
		}
	}

	private Lock createStartupToken() {
		return LockService.getInstance().createLock("startup", (Object) null);
	}

	private long now() {
		return System.currentTimeMillis();
	}
	 
	void startupModuleSystem(final Version theVersion) throws ModuleException, KnowledgeBaseException {
		
		ModuleUtil.INSTANCE.startUp(PersistencyLayer.Module.INSTANCE);
		
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(
			Messages.APPLICATION_STARTUP__NAME_VERSION.fill(theVersion.getName(), theVersion.getVersionString()));
		{
			ModuleUtil.INSTANCE.startConfiguredModules();
		}
		commitStartupChanges(tx, theVersion);
	}

	/**
	 * Commits the changes done during initial startup of the module system.
	 * 
	 * @param tx
	 *        The transaction in which the start up occurred.
	 * @param applicationVersion
	 *        The {@link Version} of the started application.
	 * 
	 * @throws KnowledgeBaseException
	 *         if commit transaction failed.
	 */
	protected void commitStartupChanges(Transaction tx, Version applicationVersion) throws KnowledgeBaseException {
		tx.commit();
	}

	void shutDownModuleSystem() {
		if (_moduleContext != null) {
			_moduleContext.close();
		}
	}

	/**
	 * Returns the {@link Date} at which the application was completely started.
	 * 
	 * @throws IllegalStateException
	 *         iff application has not been successfully booted.
	 */
	public static Date startUpDate() {
		if (STARTUP_DATE == null) {
			throw new IllegalStateException("Application was not yet started.");
		}
		return STARTUP_DATE;
	}

}
