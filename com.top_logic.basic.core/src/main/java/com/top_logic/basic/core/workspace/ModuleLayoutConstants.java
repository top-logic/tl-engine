/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

/**
 * Constants defining the {@link ModuleLayout}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModuleLayoutConstants {

	/**
	 * The main aspect in a src folder.
	 */
	public static final String MAIN_ASPECT = "main";

	/**
	 * The test aspect in a src folder.
	 */
	public static final String TEST_ASPECT = "test";

	/**
	 * Local name of the directory containing a web application.
	 */
	public static final String WEBAPP_LOCAL_DIR_NAME = "webapp";

	/**
	 * Name of the directory for application layouts.
	 */
	public static final String LAYOUTS_DIR_NAME = "layouts";

	/**
	 * @see ModuleLayout#getWorkspaceDir()
	 */
	public static final String WORKSPACE = "workspace";
	
	/**
	 * @see ModuleLayout#getWebappDir()
	 */
	public static final String WEBAPP_DIR =
		Environment.getSystemPropertyOrEnvironmentVariable("tl_webapp_dir",
			"src/" + MAIN_ASPECT + "/" + WEBAPP_LOCAL_DIR_NAME);
	
	/** Path to the test web application. */
	public static final String TEST_WEBAPP_DIR =
		Environment.getSystemPropertyOrEnvironmentVariable("tl_test_webapp_dir",
			"src/" + TEST_ASPECT + "/" + WEBAPP_LOCAL_DIR_NAME);

	/**
	 * Name of the <code>webapp</code> directory in a deploy folder.
	 */
	public static final String DEPLOY_WEBAPP_DIR = WEBAPP_LOCAL_DIR_NAME;

	/**
	 * @see ModuleLayout#getWebinfDir()
	 */
	public static final String WEB_INF_DIR = "WEB-INF";
	
	/**
	 * A web application resource path starting with "/" pointing to the directory
	 * {@link #WEB_INF_DIR}.
	 */
	public static final String WEB_INF_RESOURCE_PREFIX = "/" + WEB_INF_DIR;

	/**
	 * The location of the {@link #WEB_INF_DIR} relative to the module root.
	 * 
	 * @see ModuleLayout#getWebinfDir()
	 */
	public static final String WEB_INF_LOC = WEBAPP_DIR + "/" + WEB_INF_DIR;

	/**
	 * Name of the module's build directory.
	 */
	public static final String TARGET_DIR = "target";

	/**
	 * Name of the module's compiler output directory.
	 */
	public static final String CLASSES_DIR = "classes";

	/**
	 * Name of the module's compiler output directory for test classes.
	 */
	public static final String TEST_CLASSES_DIR = "test-classes";

	/**
	 * The location of the {@link #CLASSES_DIR} relative to the module root.
	 * 
	 * @see #CLASSES_DIR
	 */
	public static final String CLASSES_LOC = TARGET_DIR + "/" + CLASSES_DIR;

	/**
	 * @see ModuleLayout#getConfDir()
	 */
	public static final String CONF_PATH = WEB_INF_DIR + "/" + "conf";
	
	/**
	 * A web application resource path starting with "/" pointing to the configuration directory
	 * within {@link #WEB_INF_DIR}.
	 * 
	 * @see ModuleLayout#getConfDir()
	 */
	public static final String CONF_RESOURCE_PREFIX = "/" + CONF_PATH;

	/**
	 * Local name of the <code>autoconf</code> directory.
	 * 
	 * @see #AUTOCONF_PATH
	 */
	String AUTOCONF_DIR = "autoconf";

	/**
	 * Path relative to the modules web application root that points to the <code>autoconf</code>
	 * directory of the module.
	 * 
	 * <p>
	 * All application configuration files (`*.config.xml`) put into this folder are automatically
	 * loaded during application startup. In contrast to regular configuration files in the
	 * `WEB-INF/conf` folder, files in this folder are not required to be referenced from a
	 * `metaConf.txt` file.
	 * </p>
	 * 
	 * <p>
	 * This folder is mainly used from in-app tooling that requires to modify the application
	 * configuration. Those tools put new configuration files into this folder instead of modifying
	 * the existing application configuration.
	 * </p>
	 * 
	 * <p>
	 * Please not that the following restrictions apply to configuration files in this folder:
	 * </p>
	 * 
	 * <ul>
	 * <li>The order in which configuration files are read is not consistent with the module
	 * inheritance relation. Instead, configuration files are read in alphabetical order of their
	 * file names. Therefore, a configuration file `a.config.xml` is read before `b.config.xml` even
	 * if `b.config.xml` is located in an inherited module.</li>
	 * <li>Due to the loosely defined order of the files, using configuration directives that depend
	 * on the order of the configurations should not be used (such as `config:override`, or
	 * `config:position`).</li>
	 * <li>Only typed configurations may be used in this folder, no alias definitions.</li>
	 * </ul>
	 */
	String AUTOCONF_PATH = WEB_INF_DIR + "/" + AUTOCONF_DIR;

	/**
	 * Resource name of the folder containing <code>autoconf</code> files when resolving them as web
	 * application resources.
	 * 
	 * @see #AUTOCONF_PATH
	 */
	String AUTOCONF_FOLDER_RESOURCE = "/" + AUTOCONF_PATH + "/";

	/**
	 * @see ModuleLayout#getResourcesDir()
	 */
	public static final String RESOURCES_PATH = CONF_PATH + "/" + "resources";
	
	/**
	 * Web application resource prefix of the location, where application resource files are stored.
	 * 
	 * @see #RESOURCES_PATH
	 */
	public static final String RESOURCES_RESOURCE_PREFIX = "/" + RESOURCES_PATH;

	/**
	 * Path relative to the web application root folder to the directory where application layouts
	 * are stored.
	 * 
	 * <p>
	 * Note: Use {@link #LAYOUT_RESOURCE_PREFIX} as prefix for resources that are resolved by the
	 * web application.
	 * </p>
	 * 
	 * @see ModuleLayout#getLayoutDir()
	 */
	public static final String LAYOUT_PATH = WEB_INF_DIR + "/" + LAYOUTS_DIR_NAME;

	/**
	 * Resource prefix for all application layouts.
	 */
	public static final String LAYOUT_RESOURCE_PREFIX = "/" + LAYOUT_PATH + "/";

	/**
	 * The location of the {@link #LAYOUT_PATH} relative to the module root.
	 * 
	 * @see ModuleLayout#getLayoutDir()
	 */
	public static final String LAYOUT_LOC = WEBAPP_DIR + "/" + LAYOUT_PATH;

	/**
	 * The local file name of the file referencing configuration fragments to load.
	 */
	public static final String META_CONF_NAME = "metaConf.txt";

	/**
	 * Relative path from the web application root folder to the location of the
	 * <code>metaConf.txt</code> file.
	 * 
	 * <p>
	 * Note: For resolving the file as web application resource, use {@link #META_CONF_RESOURCE}.
	 * </p>
	 * 
	 * @see ModuleLayout#getMetaConfResource()
	 * 
	 * @see #META_CONF_RESOURCE
	 */
	public static final String META_CONF_PATH = CONF_PATH + "/" + META_CONF_NAME;
	
	/**
	 * Web application resource name of the <code>metaConf.txt</code> file.
	 */
	public static final String META_CONF_RESOURCE = "/" + META_CONF_PATH;

	/**
	 * @see ModuleLayout#getTestSourceDir()
	 */
	public static final String SRC_MAIN_DIR =
		Environment.getSystemPropertyOrEnvironmentVariable("tl_source_dir", "src/" + MAIN_ASPECT + "/java");

	/**
	 * @see ModuleLayout#getTestSourceDir()
	 */
	public static final String SRC_TEST_DIR = Environment.getSystemPropertyOrEnvironmentVariable("tl_test_source_dir", "src/test/java");

	/**
	 * Path from the web application root folder to the root folder of the module.
	 * 
	 * <p>
	 * Assumes a module layout <code>com.top_logic.module/src/main/webapp/WEB-INF/web.xml</code>.
	 * </p>
	 */
	public static final String PATH_TO_MODULE_ROOT =
		Environment.getSystemPropertyOrEnvironmentVariable("tl_webapp_to_module", "../../..");

	/** Path to the deploy folder starting from */
	String DEPLOY_FOLDER_NAME = "deploy";

	/**
	 * Name of the deploy folder containing the local web application, i.e. the content of this
	 * folder are not inherited to other modules.
	 */
	String DEPLOY_LOCAL_FOLDER_NAME = "local";

	/**
	 * Name of the deploy folder containing the private data for the web application. Contents of
	 * this folder are not included into a TopLogic-Studio.
	 * 
	 * <p>
	 * The folder typically contains data with local infrastructure dependent configurations, like
	 * host-names, user names, or passwords.
	 * </p>
	 */
	String DEPLOY_PRIVATE_FOLDER_NAME = "private";
}
