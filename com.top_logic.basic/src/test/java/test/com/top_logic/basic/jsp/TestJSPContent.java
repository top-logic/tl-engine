/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.GenericTest;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;

/**
 * Tests some properties of JSP pages (including usage of Beans, TagLibs).
 * In case of an invalid property the file name and an indication of the failure is printed.
 * <ul>
 *   <li>T: A declared taglib is never used.</li>
 *   <li>B: A declared bean is never used.</li>
 *   <li>S: The page source comment is missing or invalid.</li>
 *   <li>C: The page contains (obsolete) setHeader() statements 
 *          concerning cache properties.
 *   <li>E: Page does not extend TopLogicJspBase (fatal for security !) 
 *   <li>ERROR(x): The file could not be processed correctly (including the line number).</li>
 * </ul>
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestJSPContent extends GenericTest {

	private static final String JSP_SUFFIX = ".jsp";

	/**
	 * Configuration options for {@link TestJSPContent}.
	 */
	public interface Config extends ConfigurationItem {
		/** Property name of {@link #getIgnore()}. */
		String IGNORE = "ignore";

		/**
		 * Pathes to ignore from test.
		 */
		@ListBinding(tag = "entry", attribute = "value")
		@Name(IGNORE)
		List<String> getIgnore();

		/**
		 * Additional checker for the content of the jsp.
		 */
		@Name("content-checkers")
		@EntryTag("checker")
		List<PolymorphicConfiguration<JSPContentChecker>> getContentCheckers();
	}
    
	private static final String JSP_BASE = "com.top_logic.util.TopLogicJspBase";

	private static final Pattern JSP_EXTENSION_PATTERN = Pattern.compile("extends\\s*=\\s*\"([^\"]*)\"");

    /** Some constants defining the tests to perform. */
	public static final int TEST_BEANS = 0x01;
    public static final int TEST_TAGLIBS = 0x02;
    public static final int TEST_CACHE   = 0x08;
    public static final int TEST_DOCTYPE = 0x10;    // not implemented, not, needed these days
    public static final int TEST_EXTENDS = 0x20;
    public static final int TEST_ALL     = 0xFF;
    
	String _lineSeparator = System.getProperty("line.separator");

	int tests = TEST_ALL;
    
    /** number of faulty files found. */
    int faultyCount;
    
    /** number of checked files. */
    int fileCount;

    /** The intial 300 Charaters read from a File */
    char[] startChar = new char[300];
    
    /** The pattern indicating the begin of a useBean statement. */
    static final Pattern beanStartPattern = Pattern.compile(
        "\\<jsp:useBean");
        
    /** The pattern indicating the end of a useBean statement. */
    static final Pattern beanEndPattern = Pattern.compile("\\%\\>");
    
    /** The Pattern indicating the id for the usedBean statement. */
    static final Pattern beanIdPattern = Pattern.compile("id\\s*=\\s*[\"|\']([^\"\']*)[\"\']");
    
    /** The pattern indicating the begin of a useBean statement. */
    static final Pattern tagLibStartPattern = Pattern.compile("\\<\\%\\@\\s*taglib");
        
    /** The pattern indicating the end of a useBean statement. */
    static final Pattern tagLibEndPattern = Pattern.compile("\\%\\>");
    
    /** The Pattern indicating the id for the usedBean statement. */
    static final Pattern tagLibIdPattern = Pattern.compile("prefix\\s*=\\s*[\"|\']([^\"\']*)[\"|\']");

    /** The Pattern for unnecessary setHeader entries. */
    static final Pattern headerEntries = Pattern.compile("setHeader\\(.*((Cache-Control)|(Pragma)|(Expires))");

    /** Collection of pathes to ignore */
    protected  Set<String> ignorePath;
    
    /** base directory to handle */
    protected File basePath;

	private BufferingProtocol _log;

    /**
     * Create a Test for the content of JSP-Files.
     *  
     * @param aBase             base directory (something like /webapp/jsp)
     */
	protected TestJSPContent(File aBase) {
		super("Test JSPs in '" + aBase.getAbsolutePath() + "'");
        basePath   = aBase;
	}

	@Override
	protected void executeTest() throws Throwable {
		_log = new BufferingProtocol();

		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		Set<String> pathesToIgnore = new HashSet<>(config.getIgnore());
		if (pathesToIgnore.isEmpty()) {
			ignorePath = Collections.emptySet();
		} else {
			ignorePath = new HashSet<>(pathesToIgnore.size());

			for (String thePath : pathesToIgnore) {
				ignorePath.add(new File(basePath, thePath).getPath());
			}
		}
		
        if (basePath.exists()) {
			List<JSPContentChecker> contentChecker = TypedConfigUtil.createInstanceList(config.getContentCheckers());
			handleFile(contentChecker, basePath, "");
        } else {
			_log.error("The file/directory '" + basePath.getAbsolutePath() + "' does not exists!");
        }

		if (_log.hasErrors()) {
			fail("\n" + _log.getErrors().stream().collect(Collectors.joining("\n")));
		}
	}

    /**
	 * Tests the content of all JSP files in a directory and calls the test for all sub directories.
	 * 
	 * @param contentChecker
	 *        Additional configured {@link JSPContentChecker} to check the given directory.
	 * @param aDir
	 *        the directory to start testing.
	 * @param relativePath
	 *        The relative path name of the given directory.
	 */
	protected void handleDirectory(List<JSPContentChecker> contentChecker, File aDir, String relativePath) {
		if (aDir == null) {
			return;
		}
		File[] files = aDir.listFiles(JSP_OR_DIR_FILTER);
		if (files == null || files.length == 0) {
            return;
        }
		for (File file : files) {
			handleFile(contentChecker, file, relativePath);
        }
    }

	private void handleFile(List<JSPContentChecker> contentChecker, final File file, String relativePath) {
		if (ignorePath.contains(file.getPath())) {
			return;
		}
		if (file.isFile()) {
			try {
				handleJSPFile(contentChecker, file);
			} catch (Throwable ex) {
				_log.error("Error processing '" + file.getAbsolutePath() + "': " + ex.getMessage(), ex);
			}
		} else {
			handleDirectory(contentChecker, file, relativePath + "/" + file.getName());
		}
	}

    /**
	 * Handles a single java file.
	 */
	protected void handleJSPFile(List<JSPContentChecker> contentChecker, File aFile) throws IOException, ClassNotFoundException {

        // indicates the initial state of the content.
        boolean faultyCacheDecl     = false;
        boolean faultyDoctype       = false;
        boolean extendsJSPBase      = false;
        Collection<String> errors = new ArrayList<>(5);
        
        // caches for defined beans, tag-libraries
        Collection<String> unusedBeans   = new HashSet<>();
        Collection<String> unusedTagLibs = new HashSet<>();

		String contents = FileUtilities.readFileToString(aFile);
		checkContents(contentChecker, errors, aFile, contents);

		Reader theReader = new StringReader(contents);
        try {
            LineNumberReader theLineReader = new LineNumberReader(theReader);
    
            // check content
            String theLine;
            String nextLine = theLineReader.readLine();
            int theLineNumber;
            do {
                theLine = nextLine;
                nextLine = theLineReader.readLine();
                theLineNumber = theLineReader.getLineNumber();
                if (theLine != null) {
                    if (doTest(TEST_BEANS)) {
                        checkUseBean   (theLine, nextLine, unusedBeans, errors, theLineNumber);
                        checkDefineBean(theLine, nextLine, unusedBeans, errors, theLineNumber);
                    }
                    if (doTest(TEST_TAGLIBS)) {
                        checkUseTagLib   (theLine,           unusedTagLibs, errors, theLineNumber);
                        checkDefineTagLib(theLine, nextLine, unusedTagLibs, errors, theLineNumber);
                    }
                    if (doTest(TEST_CACHE)) {
                        faultyCacheDecl = faultyCacheDecl || headerEntries.matcher(theLine).find();
                    }
                    if (doTest(TEST_EXTENDS)) {
						Matcher matcher = JSP_EXTENSION_PATTERN.matcher(theLine);
						if (matcher.find()) {
							extendsJSPBase = extendsJSPBase || isJspBase(matcher.group(1));
						}
                    }
                }
            } while (theLine != null);
        } finally {
            theReader = StreamUtilities.close(theReader);
        }
        
        // set faulty flags
        boolean faultyBean   = !unusedBeans.isEmpty();
        boolean faultyTagLib = !unusedTagLibs.isEmpty();
        
        // print result
        fileCount++;
		if (faultyBean || faultyTagLib || faultyCacheDecl || !extendsJSPBase
			|| !errors.isEmpty()) {

			StringBuilder failureMsg = new StringBuilder();
            faultyCount++;
			failureMsg.append(aFile.getAbsolutePath());
			failureMsg.append(":");
            if (faultyCacheDecl)
				failureMsg.append(" the page contains (obsolete) setHeader() statements concerning cache properties,");
            if (faultyDoctype)
				failureMsg.append(" missing doctype,");
            if (faultyBean) {
				failureMsg.append(" beans: ");
				failureMsg.append(StringServices.toString(unusedBeans, ","));
				failureMsg.append(" are not used,");
            }
            if (faultyTagLib) {
				failureMsg.append(" tagLibs: ");
				failureMsg.append(StringServices.toString(unusedTagLibs, ","));
				failureMsg.append(" are not used,");
            }
            if (!extendsJSPBase) {
				failureMsg.append(" does not extends JSPBase (fatal for security !),");
            }
            if (!errors.isEmpty()) {
				failureMsg.append(" Errors: ");
				failureMsg.append(StringServices.toString(errors, " "));
				failureMsg.append(',');
            }
			_log.error(failureMsg.substring(0, failureMsg.length() - 1));
        }
    }

	private void checkContents(List<JSPContentChecker> contentCheckers, Collection<String> errors, File jsp,
			String contents) {
		for (JSPContentChecker contentChecker : contentCheckers) {
			contentChecker.checkContent(errors, jsp, contents);
		}
	}

	private boolean isJspBase(String classExtension) throws ClassNotFoundException {
		Class<?> extensionClass;
		try {
			extensionClass = Class.forName(classExtension);
		} catch (ClassNotFoundException ex) {
			_log.error("Extension class " + classExtension + " does not exist.");
			return true;
		}
		return Class.forName(JSP_BASE).isAssignableFrom(extensionClass);
	}

    /**
     * Checks for declaration of a bean.
     * 
     * @param aLine              the content of the current line.
     * @param aNextLine          the content of the next line.
     * @param someDeclaredBeans  the collection of decalred beans.
     * @param someErrors         the collection of errors encountered while reading the file.
     * @param aLineNumber        the current line number (used for error reproting). 
     */
    protected void checkDefineBean(
        String aLine,
        String aNextLine,
        Collection<String> someDeclaredBeans,
        Collection<String> someErrors,
        int aLineNumber) {
            
        Matcher theMatcher = beanStartPattern.matcher(aLine);
        boolean containsBeanDecl = theMatcher.find();
        if (containsBeanDecl) {
            theMatcher = beanIdPattern.matcher(aLine);
            boolean containsId = theMatcher.find();
            if ((!containsId) && (aNextLine != null)) {
                theMatcher = beanIdPattern.matcher(aNextLine);
                containsId = theMatcher.find();
            }
            if (containsId) {
                String theId = theMatcher.group(1);
                someDeclaredBeans.add(theId);
            } else {
                someErrors.add(String.valueOf(aLineNumber));
            }
        }
    }
    
    /**
     * Checks if one of the declared beans is used.
     * 
     * @param aLine              the content of the current line.
     * @param aNextLine          the content of the next line.
     * @param someDeclaredBeans  the collection of declared beans.
     * @param someErrors         the collection of errors encountered while reading the file.
     * @param aLineNumber        the current line number (used for error reporting). 
     */
    protected void checkUseBean(
        String aLine                , String aNextLine,
        Collection<String> someDeclaredBeans, Collection<String> someErrors,
        int aLineNumber) {

        if (someDeclaredBeans.isEmpty())
            return;
        
        Iterator<String> it = someDeclaredBeans.iterator();
        while ( it.hasNext()) {
            String theBeanId = it.next();
            // check if the bean id is refered
            int theIdIndex = aLine.indexOf(theBeanId);
            if (theIdIndex > -1) {
                // if the line ends after the bean, look for a dot at the start of the next line
                if (aLine.length() == theIdIndex + theBeanId.length()) {
                    if ((aNextLine != null) && (aNextLine.trim().charAt(0) == '.')) {
                        it.remove();
                    } else {
                        someErrors.add(String.valueOf(aLineNumber));
                    }
                } else {
                    // next char must be a dot
                    if (aLine.charAt(theIdIndex + theBeanId.length()) == '.') {
                        it.remove();
                    } else {
                        someErrors.add(String.valueOf(aLineNumber));
                    }
                }
            }
        }
    }

    /**
     * Checks for declaration of a tag lib.
     * 
     * @param aLine                the content of the current line.
     * @param aNextLine            the content of the next line.
     * @param someDeclaredTagLibs  the collection of decalred tag libs.
     * @param someErrors           the collection of errors encountered while reading the file.
     * @param aLineNumber          the current line number (used for error reproting). 
     */
    protected void checkDefineTagLib(
        String aLine,
        String aNextLine,
        Collection<String> someDeclaredTagLibs,
        Collection<String> someErrors,
        int aLineNumber) {
            
        Matcher theMatcher = tagLibStartPattern.matcher(aLine);
        boolean containsBeanDecl = theMatcher.find();
        if (containsBeanDecl) {
            theMatcher = tagLibIdPattern.matcher(aLine);
            boolean containsId = theMatcher.find();
            if ((!containsId) && (aNextLine != null)) {
                theMatcher = tagLibIdPattern.matcher(aNextLine);
                containsId = theMatcher.find();
            }
            if (containsId) {
                String theId = theMatcher.group(1);
                someDeclaredTagLibs.add(theId);
            } else {
				someErrors.add("Cannot find ID for TLD included on line " + String.valueOf(aLineNumber));
            }
        }
    }
    
    /**
     * Checks if one of the declared tag libs is used.
     * 
     * @param aLine                the content of the current line.
     * @param declaredTagLibs      the collection of declared tag libs.
     * @param someErrors           the collection of errors encountered while reading the file.
     * @param aLineNumber          the current line number (used for error reproting). 
     */
    protected void checkUseTagLib(String aLine,
                                  Collection<String> declaredTagLibs,
                                  Collection<String> someErrors,
                                  int aLineNumber) {
        
        if (declaredTagLibs.isEmpty())
            return;
                
        Iterator<String> it = declaredTagLibs.iterator();
        while (it.hasNext()) {
            String theTagLibId = it.next();
            // check if the tag lib id is refered
            int theIdIndex = aLine.indexOf("<"+theTagLibId+":");
            if (theIdIndex > -1) {
                it.remove();
            }
        }
    }
    
    /**
     * Check if a given test is to be performed.
     */
    final boolean doTest(int aTest) {
        return (tests & aTest) != 0;
    }
    
    /**
     * Filter for extracting jsp files.
     */
    public static final FileFilter JSP_OR_DIR_FILTER = new FileFilter() {
        @Override
		public boolean accept(File file) {
			String name = file.getName();
			if (name.startsWith(".")) {
				return false;
			}
			return file.isDirectory() || name.endsWith(JSP_SUFFIX);
               
        }
    };
    
	/**
	 * Main method to invoke the test on a single JSP file.
	 */
	public static void main(String[] args) {
		File jspFile = new File(args[0]);
		System.out.println("Testing " + jspFile.getAbsolutePath());
		TestRunner.run(suite(jspFile));
	}

	/**
	 * Creates a {@link TestJSPContent} for this module.
	 */
	public static Test suite() {
		return suite(AbstractBasicTestAll.webapp());
	}

	/**
	 * Creates a {@link TestJSPContent} for the given base dir.
	 */
	public static Test suite(File baseDir) {
		return ModuleTestSetup.setupModule(new TestJSPContent(baseDir));
	}

}


