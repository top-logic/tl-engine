/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.io.WrappedIOException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.MemoryBinaryContent;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.xml.TagWriter;

/** Use this class to implement Commandline-tools with GNU-like Syntax.
 *<p>
 *  Example main [-h | --help] parameters
 *</p>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class XMain extends Main {

	/**
	 * Marker for no old properties.
	 */
	private static final NamedConstant NO_PROPERTIES = new NamedConstant("none");

    /** Section in XMLProperties to use. */
    protected String              section;

    /** Path to the web application root directory. */
	protected String webApp = ModuleLayoutConstants.WEBAPP_DIR;
	
	private File _workspace;

	/**
	 * Additional path to install to {@link FileManager} in addition to {@link #webApp}.
	 */
	private final List<Path> _overlays = new ArrayList<>();

	/**
	 * Additional configurations to install to {@link XMLProperties} in addition to default
	 * configuration in {@link #_metaConf}.
	 */
	private final List<BinaryData> _additionalConfigs = new ArrayList<>();

	private String _metaConf;
	
    /** Properties to use, eventually extracted from the xProperties */
    protected Properties          properties;

	private Object _previousConfig = NO_PROPERTIES;

	private final Map<String, String> _variables = new HashMap<>();

	private ModuleContext _moduleCtx;

	private String[] _deployAspects = ArrayUtil.EMPTY_STRING_ARRAY;

    /** Empty Constructor for normal, commandline usage */
    protected XMain() {
        super();
    }

	/** CTor for other, internal usages */
    public XMain(boolean isInteractive) {
        super(isInteractive); 
    }
    
    /** Constructor to provide a default section */
    protected XMain(String aSection) {
        super();
        section = aSection;
    }

    /** This is called by showHelp to show the possible options */
    @Override
	protected void showHelpOptions() {
        super.showHelpOptions();
        info("\t-p | --properties         <file> Use given Properties file");
		info("\t-w | --webapp             use given web application directory");
		info("\t     --workspace          use given Eclipse workspace directory");
		info("\t-a | --aspect             Comma separated names of deployment aspect folders.");
        info("\t-m | --metaconf           use given file as metaconfiguration for XMLProperties");
		info("\t-o | --overlay            Additional paths to resolve resource from, e.g. \"deploy/environment-1;deploy/dbms-customization\"");
		info("\t-v | --var <name>=<value> Set an additional configuration variable e.g. \"%SOME_VAR%=some value\"");
        info("\t-s | --section            name   Section in XMLProperties to use");
        info("\t-M | --Multiprops         <file> path to multi-loader-properties-file");
//      The XML-Properties are redundant nowadays.
        info("\t-x | --xmlproperties      deprecated, use -m <file> Use given XML Properties file for initial properties (subsequent use resets properties)");
        info("\t-X | --pushxmlproperties  deprecated, use -m <file> Use given XML Properties file to overwrite existing properties");
    }
    

    /** Get Properties either direct or via the XMLProperties.
    */
    protected Properties getProperties () {

        if (properties == null) {
        	initXMLProperties();

            properties = Configuration.getConfigurationByName(section)
                                      .getProperties();

            if (interactive) {
                info ("Using properties from section '" + section + "'");
            }
        }

        return properties;
    }

	/**
	 * Initialize {@link XMLProperties}.
	 */
	protected void initXMLProperties() {
		if (!XMLProperties.Module.INSTANCE.isActive()) {
			startupXMLProperties();
		}
	}
	
	/**
	 * Call this as needed to setup your modules.
	 */
	protected void setupModuleContext(BasicRuntimeModule<?>... neededModules) throws ModuleException {
		initXMLProperties();

        if (neededModules == null || neededModules.length == 0) {
            return;
        }
        
		if (_moduleCtx != null) {
            throw new IllegalStateException("Yo must call setupModuleContext() only once, check hasModuleContext()");
        }
        
        ModuleUtil moul = ModuleUtil.INSTANCE;
		_moduleCtx = moul.begin();
        for (int i = 0, n = neededModules.length; i < n; i++) {
            moul.startUp(neededModules[i]);
        }

    }
    
	/**
	 * Whether {@link #setupModuleContext(BasicRuntimeModule...)} was called with at least one
	 * Module.
	 */
	protected boolean hasModuleContext() {
		return _moduleCtx != null;
	}
	
   
   	/**
	 * {@link ModuleUtil#shutDown(BasicRuntimeModule)} for all modules in {@link #_moduleCtx}.
	 */
   protected void tearDownModuleContext() {
		if (_moduleCtx != null) {
			_moduleCtx.close();
       }
   }
   
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        tearDownModuleContext();
		if (_previousConfig != NO_PROPERTIES) {
			if (_previousConfig == null) {
				ModuleUtil.INSTANCE.shutDown(XMLProperties.Module.INSTANCE);
			} else {
				XMLProperties.restartXMLProperties((XMLPropertiesConfig) _previousConfig);
			}

		}
    }

	private void startupXMLProperties() {
		// The file manager is needed for the configuration to resolve files
		initFileManager();

	    try {
			XMLPropertiesConfig oldConfig = currentXMLConfig();

			XMLProperties.restartXMLProperties(getXMLPropertiesConfig());
			
			this.storeXMLProperties(oldConfig);
		} catch (ModuleException ex) {
			throw fatal("Unable to start XMLProperties", ex);
		}
	}

	/**
	 * Gets the configuration for the {@link XMLProperties}.
	 */
	protected XMLPropertiesConfig getXMLPropertiesConfig() {
		XMLPropertiesConfig newConfig = new XMLPropertiesConfig(null, false, _metaConf);
		if (!_variables.isEmpty()) {
			try {
				try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
					try (Writer writer = new OutputStreamWriter(buffer, StringServices.CHARSET_UTF_8);
							TagWriter out = new TagWriter(writer)) {
						out.beginTag(XMLProperties.ROOT_ELEMENT);
						out.beginTag(XMLProperties.ALIAS_NODE);
						for (Entry<String, String> entry : _variables.entrySet()) {
							out.beginBeginTag(XMLProperties.ENTRY_NODE);
							out.writeAttribute(XMLProperties.KEY_ATTR, entry.getKey());
							out.writeAttribute(XMLProperties.VALUE_ATTR, entry.getValue());
							out.endEmptyTag();
						}
						out.endTag(XMLProperties.ALIAS_NODE);
						out.endTag(XMLProperties.ROOT_ELEMENT);
					}
					newConfig.pushAdditionalContent(new MemoryBinaryContent(buffer.toByteArray(), "<custom>"), null);
				}
			} catch (IOException ex) {
				/* No IOException because everything happens in memory. */
				throw new WrappedIOException(ex);
			}
		}
		for (BinaryData f : _additionalConfigs) {
			newConfig.pushAdditionalContent(f);
		}
		return newConfig;
	}
	
    /**
     * Initialize {@link FileManager}.
     */
    protected void initFileManager() {
		if (FileManager.getInstanceOrNull() == null) {
			fetchMultiLoadProps();
		}
    }

    /** Set Properties from the Commandline option "-p ".
     */
    protected void fetchProperties (String fileName) {
        try {
            properties = new Properties ();
            properties.load (new FileInputStream (fileName));
            
            info("Using properties from file '" + fileName + "'");
        } catch (FileNotFoundException fnfx) {
            error("Properties file '" + fileName + "' not found."); 
        } catch (IOException ex) {
            error("Error reading properties from '" + fileName + "'", ex); 
        }
    }
    
    /**
	 * Use meta configuration from given file.
	 * 
	 * @param metaConf
	 *        usually {@link ModuleLayoutConstants#META_CONF_RESOURCE}
	 */
    protected void setMetaConf (String metaConf) {
    	_metaConf = metaConf;
        if (interactive) {
            info ("Using meta conf: " + _metaConf);
        }
    }

    /**
     * Sets the web application root.
     */
    protected void setWebapp (String appPath) {
        webApp = appPath;
        if (interactive) {
            info ("Using web application root: " + webApp);
        }
    }
    
	/**
	 * Sets the Eclipse workspace directory.
	 */
	protected final void setWorkspace(String dir) {
		setWorkspace(new File(dir));
	}

	/**
	 * Sets the workspace directory to operate on.
	 */
	public void setWorkspace(File workspace) {
		_workspace = workspace;
		if (!_workspace.isDirectory()) {
			error("Workspace is not a directory: " + _workspace.getAbsolutePath());
		}
		if (interactive) {
			info("Using workspace dir: " + _workspace.getAbsolutePath());
		}
	}

	/**
	 * The workspace directory to operate on, or <code>null</code> if not set.
	 */
	public final File getWorkspace() {
		return _workspace == null ? getDefaultWorkspace() : _workspace;
	}

	/**
	 * The {@link #getWorkspace()}, if none was {@link #setWorkspace(File) set}.
	 */
	protected File getDefaultWorkspace() {
		return null;
	}

	/**
	 * Get the name of the web application to be used.
	 * 
	 * @return the name of the web application to be used.
	 */
	protected String getWebapp () {
		return this.webApp;
	}

	private void setOverlayPath(String concatenatedPath) {
		String[] paths = concatenatedPath.split(Pattern.quote(File.pathSeparator));
		for (String path : paths) {
			addOverlay(path);
		}
	}

	private void setAspectNames(String names) {
		String[] aspects;
		if ("-".equals(names)) {
			// Note: The '-' is a marker for no aspect at all. This is a workaround for not being
			// able passing empty arguments from Ant to an invoked Java VM in an OS-independent
			// fashion.
			aspects = ArrayUtil.EMPTY_STRING_ARRAY;
		} else {
			aspects = names.trim().split("\\s*,\\s*");
		}
		_deployAspects = aspects;
	}

	/**
	 * Adds an additional configuration file to the {@link XMLProperties}.
	 */
	protected void addAdditionalConfig(BinaryData file) {
		if (interactive) {
			info("Using additional config: " + file);
		}
		_additionalConfigs.add(file);
	}

	/**
	 * Removes an additional configuration file, formerly added by
	 * {@link #addAdditionalConfig(BinaryData)}.
	 */
	protected void removeAdditionalConfig(BinaryData file) {
		if (!_additionalConfigs.remove(file)) {
			error("File " + file + " is not additional content.");
		}
	}

    /**
	 * Adds an overlay path to resolve from in addition to {@link #getWebapp()}.
	 * 
	 * @param path
	 *        The path to resolve resources from.
	 */
    protected void addOverlay(String path) {
		addOverlay(Paths.get(path));
	}

	private void addOverlay(Path dir) {
		if (!checkDir("Overlay path", dir)) {
			return;
		}
		if (interactive) {
			info("Using resource overlay dir: " + dir);
		}
		_overlays.add(dir);
	}

	private boolean checkDir(String name, Path dir) {
		if (!Files.exists(dir)) {
			error(name + " does not exist: " + dir);
			return false;
    	}
		if (!Files.exists(dir)) {
			error(name + " is no directory: " + dir);
			return false;
    	}
		return true;
	}

	/** Set XMLProperties from the Commandline option "-x "
     */
    protected void fetchXProperties (String fileName)  {
        try {
			XMLPropertiesConfig formerXMLConfig = currentXMLConfig();
			XMLProperties.startWithConfigFile(fileName);
			storeXMLProperties(formerXMLConfig);
            if (interactive) {
                info("Using XML properties from '" + fileName + "'");
            }
        } catch (ModuleException mx) {
        	error("Cannot load XML properties from '" + fileName + "'", mx);
        }
    }

	/** 
	 * Fetches the current configuration of the {@link XMLProperties} or <code>null</code> when currently not started.
	 */
	private XMLPropertiesConfig currentXMLConfig() {
		XMLPropertiesConfig currentConfig;
		if (XMLProperties.Module.INSTANCE.isActive()) {
			currentConfig = XMLProperties.Module.INSTANCE.config();
		} else {
			currentConfig = null;
		}
		return currentConfig;
	}
    
	/**
	 * Setup Configuration from MultiLoader Properties
	 */
	protected void fetchMultiLoadProps() {
		// The file manager is needed for the configuration to resolve files
		List<Path> resourcePaths = Workspace.applicationModules(_deployAspects);
		FileManager multiLoaderFileManager = MultiFileManager.createMultiFileManager(resourcePaths);
		FileManager.setInstance(multiLoaderFileManager);
    }

    /** 
     * Push XMLProperties from the command line option "-X "
     * 
     * (Can be useful to push extra configurations (e.g. for deployment)
     */
    protected void pushXProperties (String fileName) {
        // use MultiProperties
        try {
			XMLPropertiesConfig formerXMLConfig = currentXMLConfig();
			MultiProperties.restartWithConfig(BinaryDataFactory.createBinaryData(new File(fileName)));
			storeXMLProperties(formerXMLConfig);

            info("Push XMLProperties from file '" + fileName + "'");
        } catch (IOException ex) {
        	error("Failed to push XML properties from file '" + fileName + "'", ex);
        } catch (ModuleException ex) {
        	error("Failed to push XML properties from file '" + fileName + "'", ex);
        }
    }


    /** Care about long Options --p[roperties] --x[mlproperties] --s[ection] --o[utput] 
    *
    * @param option Text after the '--', may be empty but not null.
    * @param args   The Original Arguments, you should use args[i]
    * @param i      The index after the current Option
    *
    * @return next index where parsing should continue, usually i or i+1;
    */
    @Override
	protected int longOption (String option, String args[], int i) {
    	if ("overlay".equals(option)) {
    		setOverlayPath(args[i++]);
    	}
		else if ("aspect".equals(option)) {
			setAspectNames(args[i++]);
		}
		else if ("var".equals(option)) {
			setVar(args[i++]);
		}
    	else if ("metaconf".equals(option)) {
    		setMetaConf(args[i++]);
    	}
    	else if ("pushxmlproperties".equals(option)) {
            pushXProperties (args[i++]);   
    	}
    	else if ("properties".equals(option)) {
    		fetchProperties (args[i++]);
    	}
    	else if ("section".equals(option)) {
    		section = args[i++];
    	}
    	else if ("webapp".equals(option)) {
    		setWebapp(args[i++]);
    	}
		else if ("workspace".equals(option)) {
			setWorkspace(args[i++]);
		}
    	else if ("xmlproperties".equals(option)) {
    		fetchXProperties (args[i++]);
    	}
    	else {
    		return super.longOption (option, args, i);
    	}
    	return i;
    }

	/** Care about short Options -p -x -s
     *
     * @param c     Character after the '-', 0 for a '-' only.
     * @param args  The Original Arguments, you should use args[i]
     * @param i     The index after the current Option
     *
     * @return next index where parsing should continue, usually i or i+1;
     */
    @Override
	protected int shortOption (char c, String args[], int i) {
        try {
            switch (c) {
				case 'o':
					setOverlayPath(args[i++]);
					break;
				case 'a':
					setAspectNames(args[i++]);
					break;
				case 'v':
					setVar(args[i++]);
					break;
                case 'm': setMetaConf           (args[i++]); break;
                case 'p': fetchProperties       (args[i++]); break;
                case 's': section              = args[i++];  break;
                case 'w': setWebapp             (args[i++]); break;
                case 'x': fetchXProperties      (args[i++]); break;
                case 'X': pushXProperties       (args[i++]); break;
                default:
                    return super.shortOption (c, args, i);
                }
        } catch (IndexOutOfBoundsException iox) {
            error("Missing argument for -" + c);
            showHelp();
        }
        return i;
    }

	private void setVar(String nameValue) {
		int separatorIndex = nameValue.indexOf('=');
		if (separatorIndex < 0) {
			error("Missing '=' in variable assignment: " + nameValue);
			return;
		}

		_variables.put(nameValue.substring(0, separatorIndex), nameValue.substring(separatorIndex + 1));
	}

    @Override
	protected void beforeActualPerformance() {
		super.beforeActualPerformance();

		initXMLProperties();
	}

	@Override
	protected void doActualPerformance () throws Exception {
        // nothing happens here right now
    }

    public static void main (String args[]) throws Exception {
        new XMain ().runMainCommandLine (args);
    }

    /**
     * Set the XMLProperties (e.g. in test-cases from testSetup).
     */
	private void storeXMLProperties(XMLPropertiesConfig formerXMLConfig) {
		if (_previousConfig == NO_PROPERTIES) {
			_previousConfig = formerXMLConfig;
		}
    }


}
