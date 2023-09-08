/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;


/**
 * A File complier compiles a set of files into on (big) file.
 * 
 * It is intended to be used with files like .js .css or similar that can be concatenated without
 * losing semantics. This class only works with files found in the same {@link Config#getBaseDir()}.
 * 
 * It is bases on Ideas found around the MultiLoaderFileManager.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class FileCompiler extends ConfiguredManagedClass<FileCompiler.Config> {

	/**
	 * Configuration of the {@link FileCompiler}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfiguredManagedClass.Config<FileCompiler> {

		/** Configuration name for the value {@link Config#isDeployed()}. */
		String DEPLOYED_NAME = "is-deployed";

		/** Configuration name for the value {@link Config#getBaseDir()}. */
		String BASE_DIR_NAME = "base-dir";

		/** Configuration name for the value {@link Config#getTarget()}. */
		String TARGET_NAME = "target";

		/** Configuration name for the value {@link Config#getFiles()}. */
		String FILES_NAME = "files";

		/** Configuration name for the value {@link Config#isAlwaysCheck()}. */
		String ALWAYS_CHECK_NAME = "always-check";

		/** Configuration name for the value {@link Config#getCheckInterval()}. */
		String CHECK_INTERVAL_NAME = "check-interval";

		/** Flag if we are in a deployed state (= file was pre-compiled) */
		@Name(DEPLOYED_NAME)
		boolean isDeployed();

		/**
		 * Setter for {@link #isDeployed()}.
		 * 
		 * @param deployed
		 *        New value of {@link #isDeployed()}.
		 */
		void setDeployed(boolean deployed);

		/** Optional base path uses for all file names, default "" */
		@Name(BASE_DIR_NAME)
		String getBaseDir();

		/**
		 * Setter for {@link #getBaseDir()}.
		 * 
		 * @param baseDir
		 *        New value of {@link #getBaseDir()}.
		 */
		void setBaseDir(String baseDir);

		/**
		 * Name of generated File, relative to {@link #getBaseDir()}.
		 */
		@Name(TARGET_NAME)
		@Mandatory
		String getTarget();

		/**
		 * Setter for {@link #getTarget()}.
		 * 
		 * @param target
		 *        New value of {@link #getTarget()}.
		 */
		void setTarget(String target);

		/**
		 * The named of the files to include into the compiled file.
		 */
		@Name(FILES_NAME)
		@Key(ResourceDeclaration.RESOURCE_ATTRIBUTE)
		List<ResourceDeclaration> getFiles();

		/**
		 * Whether the base files are checked for modification to re-compile the file.
		 * 
		 * @see #getCheckInterval()
		 */
		@Name(ALWAYS_CHECK_NAME)
		boolean isAlwaysCheck();

		/**
		 * Setter for {@link #isAlwaysCheck()}.
		 * 
		 * @param alwaysCheck
		 *        New value of {@link #isAlwaysCheck()}.
		 */
		void setAlwaysCheck(boolean alwaysCheck);

		/**
		 * Interval in ms when a check for modification occurs, i.e. if a base file changed, in at
		 * most this time the change is applied into the compiled file.
		 * 
		 * <p>
		 * Only used when {@link #isAlwaysCheck()} is true.
		 * </p>
		 * 
		 * @see #isAlwaysCheck()
		 */
		@Name(CHECK_INTERVAL_NAME)
		@LongDefault(2000)
		long getCheckInterval();

		/**
		 * Setter for {@link #getCheckInterval()}.
		 * 
		 * @param interval
		 *        New value of {@link #getCheckInterval()}.
		 */
		void setCheckInterval(long interval);

	}

    /** Names of source files (relative to the application root). */
	private List<String> _baseNames;

    /**
     * Action to execute before giving access to the compilation result.
     */
	private UpdateAction _dirtyCheck;

	FileManager _fileManager = FileManager.getInstance();

	private InstantiationContext _context;

	/**
	 * Creates a new {@link FileCompiler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link FileCompiler}.
	 */
	public FileCompiler(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	/**
	 * Override {@link #startUp(InstantiationContext)}.
	 * 
	 * @see com.top_logic.basic.module.ManagedClass#startUp()
	 */
	@Override
	protected final void startUp() {
		super.startUp();
		startUp(_context);
	}

	/**
	 * Starts this module.
	 * 
	 * @param context
	 *        The context to log errors or instantiate sub configurations.
	 */
	protected void startUp(InstantiationContext context) {
		_baseNames = resolveReferences(context, getConfig().getFiles());

		if (getConfig().isAlwaysCheck()) {
			long checkInterval = getConfig().getCheckInterval();
			_dirtyCheck = new UpdateIfDirty(checkInterval);
		} else {
			_dirtyCheck = new NoUpdate();
		}

		if (!isDeployed()) {
			compile();
		}
	}

	@Override
	protected void shutDown() {
		super.shutDown();

		_baseNames = null;
		_dirtyCheck = null;
	}

	/**
	 * Injects the {@link FileManager} to use.
	 */
	@FrameworkInternal
	public void setFileManager(FileManager fileManager) {
		_fileManager = fileManager;
	}

	/**
	 * Checks that the given resources are available.
	 * 
	 * @param log
	 *        The {@link Log} to write errors to
	 * @param resources
	 *        The resources to check.
	 * 
	 * @return List of the resources which could be resolved.
	 */
	protected ArrayList<String> resolveReferences(Log log, List<ResourceDeclaration> resources) {
		ArrayList<String> resourceNames = new ArrayList<>(resources.size());
		for (ResourceDeclaration declaration : resources) {
			String resourcePath = resolveResourcePath(log, declaration);
			if (isAccessable(log, resourcePath)) {
				resourceNames.add(resourcePath);
			}
		}
		return resourceNames;
	}

	/**
	 * Resolved resource path.
	 * 
	 * @return null if the resource is a webjar and his properties could not be read.
	 */
	protected String resolveResourcePath(Log log, ResourceDeclaration resourceDeclaration) {
		return resolveResourcePath(log, resolveResourceName(resourceDeclaration.getResource()));
	}

	/**
	 * Resolves a resource path that may optionally be webjar URL in the format
	 * <code>webjar:group-id/artifact-id:path:file-name</code>.
	 */
	public static String resolveResourcePath(Log log, String resource) {
		String resourcePath;
		// webjar:org.webjars/chartjs:webjars/chartjs:Chart.js
		// /META-INF/maven/org.webjars/chartjs/pom.properties
		// /META-INF/resources/webjars/chartjs/2.9.4/Chart.js
		if (resource.startsWith("webjar:")) {
			int start = "webjar:".length();
			int pathIdx = resource.indexOf(':', start + 1);
			int nameIdx = resource.indexOf(':', pathIdx + 1);
			String props = resource.substring(start, pathIdx);
			String path = resource.substring(pathIdx + 1, nameIdx);
			String name = resource.substring(nameIdx + 1);

			String propertiesPath = "/META-INF/maven/" + props + "/pom.properties";
			try (InputStream in = FileCompiler.class.getResourceAsStream(propertiesPath)) {
				Properties properties = new Properties();
				properties.load(in);

				String version = properties.getProperty("version");
				resourcePath = path + "/" + version + "/" + name;
			} catch (IOException ex) {
				log.error("Cannot read resource properties: " + resource, ex);
				resourcePath = null;
			}
		} else {
			resourcePath = resource;
		}
		return resourcePath;
	}

	/**
	 * True if the resource by the given path could be accessed.
	 */
	protected boolean isAccessable(Log log, String resourcePath) {
		if (resourcePath != null) {
			try {
				@SuppressWarnings("resource")
				InputStream in = _fileManager.getStreamOrNull(resourcePath);
				if (in == null) {
					log.error("Resource does not exist: " + resourcePath);
				} else {
					in.close();
					return true;
				}
			} catch (IOException ex) {
				log.error("Problem accessing resource '" + resourcePath + "'.", ex);
			}
		}

		return false;
	}

	private String resolveResourceName(String resourceName) {
		String resource;
		if (resourceName.startsWith("/")) {
			// Outside base dir.
			resource = resourceName;
		} else if (resourceName.contains(":")) {
			// Full URL.
			resource = resourceName;
		} else {
			// Relative resource.
			String baseDir = getConfig().getBaseDir();
			StringBuilder result = new StringBuilder();
			result.append(baseDir);
			if (!baseDir.endsWith("/")) {
				result.append("/");
			}
			result.append(resourceName);
			resource = result.toString();
		}
		return resource;
	}

    /**
     * Return the {@link Charset} assumed for handling the files.
     * 
     * @return the character set for "US-ASCII", here
     */ 
    protected Charset getCharset() {
        return Charset.forName("US-ASCII");
    }
    
    /** 
     * Starts the compilation.
     */
	void compile() {
        Charset cs = getCharset();
		String target = target();
		try {
			File targetFile = _fileManager.getIDEFile(target);
			targetFile.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(targetFile);
            try {
            	Writer writer = new OutputStreamWriter(out, cs);
				for (String baseName : getBaseNames()) {
                    try {
						InputStream in = _fileManager.getStream(baseName);
                        try {
                        	Reader reader = new InputStreamReader(in, cs);
                        	try {
                        		compile(reader, writer, baseName);
                        	} finally {
                        		reader.close();
                        	}
						} finally {
							in.close();
						}
                    } catch (FileNotFoundException ex) {
                    	Logger.warn("File '" + baseName + "' does not exist.", ex, FileCompiler.class);
                    } catch (IOException ex) {
                        Logger.warn("Failed to compile file '" + baseName + "'.", ex, FileCompiler.class);
                    }
                }
                writer.flush();
			} finally {
				out.close();
			}
        } catch (IOException ex) {
			throw new ConfigurationError("Failed to compileFiles to '" + target + "'", ex);
        }
    }

	/**
	 * Default method of "compilation" is a simple append.
	 * 
	 * @param path
	 *        The path of the source file, may be used to add meta information to the destionation.
	 */
    protected void compile(Reader aIn, Writer aOut, String path) throws IOException {
        StreamUtilities.copyReaderWriterContents(aIn, aOut);
    }

    /**
     * The name of the generated file.
     */
    public String getTarget() {
    	checkForUpdate();
		return target();
    }

	private String target() {
		return resolveResourceName(getConfig().getTarget());
	}

    /**
     * Test, whether source files have changed.
     */
	protected void checkForUpdate() {
		_dirtyCheck.run();
	}
    
    /**
     * The time in {@link System#currentTimeMillis()}, when the {@link #getTarget()} was last created.
     */
    public long getLastUpdate() {
		return _dirtyCheck.getLastUpdate();
    }
    
    /**
	 * @see Config#isDeployed()
	 */
    public boolean isDeployed() {
		return getConfig().isDeployed();
    }

	protected List<String> getBaseNames() {
		return _baseNames;
	}

	static abstract class UpdateAction implements Runnable {

		/**
		 * Time in {@link System#currentTimeMillis()} when the update performed the last time.
		 */
	    private long lastUpdate;

		public UpdateAction() {
			this.lastUpdate = System.currentTimeMillis();
		}
		
		public long getLastUpdate() {
			return lastUpdate;
		}
		
		protected void setLastUpdate(long lastUpdate) {
			this.lastUpdate = lastUpdate;
		}
		
	}
	
	static final class NoUpdate extends UpdateAction {

		@Override
		public void run() {
			// Never update.
		}
		
	}
	
	final class UpdateIfDirty extends UpdateAction {
		
		private final List<File> files;
		
	    /**
	     * When the next check for actuality should be performed.
	     */
	    private long nextCheck;

	    /**
	     * Minimum time in milliseconds between checks.
	     */
		private final long checkInterval;


		public UpdateIfDirty(long checkInterval) {
			this.files = createFiles(getBaseNames());
			this.checkInterval = checkInterval;
		}
		
		private List<File> createFiles(List<String> baseNames) {
	    	ArrayList<File> result = new ArrayList<>(baseNames.size());
	    	for (String name : baseNames) {
				{
					File file = _fileManager.getIDEFileOrNull(name);
					if (file != null) {
						result.add(file);
					}
				}
	    	}
			return result;
		}

		/**
		 * Perform the up-to-date check and eventually call the update action.
		 * 
		 * <p>
		 * Method is synchronized to prevent multiple rendering threads from updating the target
		 * file concurrently.
		 * </p>
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public synchronized void run() {
			long now = System.currentTimeMillis();
			if (now < nextCheck) {
				return;
			}
			
			nextCheck = now + checkInterval;
			
	        for (File file : files) {
				long lastModified = file.lastModified();
	            if (lastModified >= getLastUpdate()) {
					setLastUpdate(now);
					
					compile();
					break;
	            }
	        }
	    }

	}

}