/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static java.util.regex.Pattern.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Tests if the copyright comment, and the class comment is present and correct.
 * <p>
 * If a class should not be checked (for example if the code is generated or foreign), you can
 * configure this test to ignore it in the {@link TypedConfiguration} for tests (for example in
 * "top-logic" its: "top-logic.test.config.xml"). Add an entry with the full qualified name of the
 * class or package that should be ignored.
 * </p>
 * 
 * @author tsa
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestComment extends TestCase {

	/**
	 * The {@link TypedConfiguration} of {@link TestComment}.
	 */
	public interface Config extends ConfigurationItem {

		/** Property name of {@link #getIgnore()}. */
		String IGNORE = "ignore";

		/**
		 * The fully qualified names of the classes and packages that should be ignored.
		 */
		@ListBinding(tag = "entry", attribute = "value")
		@Name(IGNORE)
		List<String> getIgnore();

		/**
		 * Regular expression to check in the header of a file.
		 */
		@Format(RegExpValueProvider.class)
		@Name("copyright-pattern")
		Pattern getCopyrightPattern();

	}

	private static final String ERR_MULTIPLE_COMMENTS = "Multiple comments. ";

	private static final String ERR_LINES_TOO_LONG = "Lines too long. ";

	private static final String ERR_EMPTY_CLASS_COMMENT = "The class comment contains no text. ";

	private static final String ERR_COPYRIGHT_COMMENT = "The copyright comment is wrong. ";

	private static final String ERR_NO_COPYRIGHT = "The copyright comment is missing. ";

	private static final String ERR_SVN_PROPERTIES = "Legacy SVN properties found. ";

	/**
	 * Switch to request automatic (partial) repair.
	 */
    static final boolean DO_REPAIR = Boolean.valueOf(System.getProperty("TestComment.doRepair"));
    
    /**
     * Filter for extracting java src files.
     */
    public static final FileFilter JAVA_DIR_FILTER = new FileFilter() {
        @Override
		public boolean accept(File aFile) {
            String name = aFile.getName();
			return (name.endsWith(".java") && !name.equals("package-info.java")) ||
                (aFile.isDirectory() && !"CVS".equals(name));
        }
    };

	static final Pattern COPYRIGHT_YEAR_PATTERN = Pattern.compile("(\\d\\d\\d\\d(?:\\s?-\\s?\\d\\d\\d\\d)?)");

	static final List<Pair<String, Pattern>> CHECKS = Arrays.asList(
		new Pair<>("Legacy file name comment found", compile("\\@\\(\\#\\)")),
		new Pair<>("Legacy @history tag found", compile(
			"^\\s*\\*\\s*\\@history .*\\R(?:\\s*\\*\\s*\\R)*", Pattern.MULTILINE)),
		new Pair<>("Legacy range comment found", compile(
			"^" + "(\\t|    | \\t|  \\t|   \\t)"
				+ "//"
				+ "\\s*"
				+ "(" + "Statics"
				+ "|" + "Constants"
				+ "|" + "Constructor(s)?"
				+ "|" + "(\\w+ )?Attributes"
				+ "|" + "(\\w+ )?Methods( (from|of) [^\\n\\r]*)?"
				+ "|" + "\\w+ implementations"
				+ "|" + "Attribute name constants"
				+ "|" + "Members"
				+ "|" + "Member(s)?"
				+ "|" + "New functions for[^\\r\\n]*"
				+ "|" + "Inner classes"
				+ "|" + "Implemen(t)?ation of [^\\r\\n]+"
				+ ")(\\.)?",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)));

	static final Pattern HEADER_PATTERN = Pattern.compile("\\s*/\\*.*?\\*/\\s*", Pattern.DOTALL);

	private static final String SVN_KEYWORDS = "Revision|Author|Date|Id";

	/**
	 * Check whether the SVN keywords have been expanded, i.e. whether the SVN properties are set.
	 * <p>
	 * The actual keywords have been extracted to prevent the SVN keyword substitution. When there
	 * are no Dollar signs near them, they will not be replaced.
	 * </p>
	 */
	static final Pattern SVN_KEYWORDS_PATTERN =
		Pattern.compile("\\$\\s*\\b(" + SVN_KEYWORDS + ")\\b\\:?[^\\$]*\\$");

    /** Pattern matching lines containing cvs tags. */
	static final Pattern tagPattern =
        Pattern.compile(
           "\\s*\\/?\\*+\\s*\\@(\\w+)");
    
    /** Pattern matching lines containing text. */
	static final Pattern textCommentPattern =
        Pattern.compile(
            "\\s*\\*\\*?\\s*[\\w\\s{@#\\(\\)}]+"); 
                
    /** Check to find the beginning of a class */
	static final Pattern classPattern =
            Pattern.compile(
                "^([^\\*]* )?(class|interface|enum|@interface)+");

	static final Pattern javaStartPattern = Pattern.compile("\\s*(?=package )");

	/** Map of directories to ignore */
	Set<String> _ignoreSet;

	/** The initial 300 Characters read from a File */
	char[] startChar = new char[300];

	private List<String> _failures;

	/**
	 * Starts the test for all files in this module.
	 */
	public void testSourceFiles() {
		_failures = new ArrayList<>();
		handleDirectory(
			new File(AbstractBasicTestAll.MODULE_LAYOUT.getModuleDir(), ModuleLayoutConstants.SRC_MAIN_DIR), "");
		handleDirectory(AbstractBasicTestAll.MODULE_LAYOUT.getTestSourceDir(), "");
		if (!_failures.isEmpty()) {
			fail(_failures.stream().collect(Collectors.joining("\n")));
		}
	}

    /**
     * Tests the comment of all files in a directory and 
     * calls the test for all sub directories.
     * @param aDir  the directory to start testing. 
     * @param javaName The file name in Java package notation.
     */
	protected void handleDirectory(File aDir, String javaName) {
		if (!aDir.exists()) {
			return;
		}
        handleExistingDirectory(aDir, javaName);
    }

	private void handleExistingDirectory(File aDir, String javaName) {
		int count;
        File[] theJavaFiles = aDir.listFiles(JAVA_DIR_FILTER);
        count = theJavaFiles.length;
        for (int i=0; i<count; i++) {
            File theFile = theJavaFiles[i];
            String baseName = baseName(theFile);
			String javaChildName = javaSubName(javaName, baseName);
            if (theFile.isFile()) {
				doTestFile(javaName, javaChildName, theJavaFiles[i]);
            } else { // theFile.isDirectory()
				handleExistingDirectory(theFile, javaChildName);
            }
        }
	}

	private String javaSubName(String packageName, String simpleName) {
		if ("".equals(packageName)) {
			return simpleName;
		} else {
			return packageName + '.' + simpleName;
		}
	}

    /**
     * File name without file extension.
     */
    private String baseName(File file) {
    	String fileName = file.getName();
		if (file.isDirectory()) {
    		return fileName;
    	} else {
    		int dotIndex = fileName.lastIndexOf('.');
    		if (dotIndex >= 0) {
    			return fileName.substring(0, dotIndex);
    		} else {
    			return fileName;
    		}
    	}
	}
	
	private boolean isIgnored(String packageName, String qualifiedName) {
		return _ignoreSet.contains(qualifiedName) || _ignoreSet.contains(packageName);
	}

	/**
	 * Tests the given file.
	 * 
	 * @param packageName
	 *        The fully qualified package name of the tested file.
	 * @param qualifiedName
	 *        The fully qualified class name.
	 * @param file
	 *        The source file to test.
	 */
	private void doTestFile(String packageName, String qualifiedName, File file) {
		try {
			checkHeader(packageName, qualifiedName, file);
			checkComment(packageName, qualifiedName, file);
		} catch (IOException ex) {
			reportFailure(file, "Error reading file.", ex);
		}
	}

	/**
	 * File header test.
	 */
	private void checkHeader(String packageName, String qualifiedName, File file) throws IOException {
		if (isIgnored(packageName, qualifiedName)) {
			return;
		}

		String contents = FileUtilities.readFileToString(file);
		Matcher headerMatcher = HEADER_PATTERN.matcher(contents);
		boolean containsComment = headerMatcher.find();
		if (containsComment) {
			Pattern pattern = getCopyrightPattern();
			if (pattern != null) {
				Matcher theMatcher = pattern.matcher(contents);
				if (!theMatcher.find()) {
					if (DO_REPAIR) {
						Matcher yearMatcher = COPYRIGHT_YEAR_PATTERN.matcher(headerMatcher.group());
						String year;
						if (yearMatcher.find()) {
							year = yearMatcher.group();
						} else {
							year = currentYear();
						}
						repairFileComment(file, headerMatcher.end(), year);
					} else {
						reportFailure(file, ERR_COPYRIGHT_COMMENT);
					}
				}
			}
		} else {
			if (DO_REPAIR) {
				repairMissingFileComment(file, contents);
			} else {
				reportFailure(file, ERR_NO_COPYRIGHT);
			}
		}

		for (Pair<String, Pattern> check : CHECKS) {
			Matcher matcher = check.getSecond().matcher(contents);
			while (matcher.find()) {
				reportFailure(file, check.getFirst() + ": '" + matcher.group() + "'.");
			}
		}
	}

	private void repairMissingFileComment(File file, String theStart) throws IOException {
		Matcher matcher = javaStartPattern.matcher(theStart);
		if (matcher.lookingAt()) {
			repairFileComment(file, matcher.end(), currentYear());
		} else {
			reportFailure(file, "Cannot repair file due to unrecognized start text.");
		}
	}

	private String currentYear() {
		int currentYear = CalendarUtil.createCalendar().get(Calendar.YEAR);
		return Integer.toString(currentYear);
	}

	private void repairFileComment(File file, int contentStart, String copyrightYear) throws IOException {
		String expectedFileName = file.getName();

		File backUp = new File(file.getParentFile(), expectedFileName + ".backup");
		boolean successBackup = file.renameTo(backUp);
		if (!successBackup) {
			reportFailure(file, "Cannot backup file for repair.");
		}

		System.err.println("Repairing file comment: " + file.getAbsolutePath());
		try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
			out.println("/*");
			out.println(" * SPDX-FileCopyrightText: " + copyrightYear + " (c) Business Operation Systems GmbH <info@top-logic.com>");
			out.println(" *");
			out.println(" * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0");
			out.println(" */");
			
			try (FileReader orig = new FileReader(backUp)) {
				orig.skip(contentStart);
				StreamUtilities.copyReaderWriterContents(orig, out);
			}
		}

		boolean successCleanup = backUp.delete();
		if (!successCleanup) {
			reportFailure(file, "Cannot clean up backup file: " + backUp.getAbsolutePath());
		}
	}

	private static final String DOC_COMMENT_START = "/**";

	/**
	 * Class comment test.
	 */
	private void checkComment(String packageName, String qualifiedName, File file) throws IOException {
		if (isIgnored(packageName, qualifiedName)) {
			return;
		}

		// handle copyright comment (including check for correct file name).
		// For a valid Copyright comment see top of this file.

		boolean deprecated = false;
		boolean noText = true;
		boolean inComment = false;
		boolean longLine = false;
		boolean multipleComments = false;
		boolean svnPropertiesFound = false;
		// handle version tag in class comment.
		// For a valid class comment see top of this class.
		try (final FileReader content = new FileReader(file)) {
			try (LineNumberReader theLineReader = new LineNumberReader(content)) {
				boolean tagFound = false;
				do {
					String theLine = theLineReader.readLine();
					if (theLine == null) {
						reportFailure(file, "Could not find class comment.");
						break;
					}
					Matcher theClassMatcher = classPattern.matcher(theLine);
					if (theClassMatcher.find()) {
						break; // Found start of class or interface
					}

					if (theLine.startsWith(DOC_COMMENT_START)) {
						if (inComment) {
							multipleComments = true;
						} else {
							inComment = true;
						}
					}

					if (inComment) {
						Matcher tagMatcher = tagPattern.matcher(theLine);
						if (tagMatcher.lookingAt()) {
							tagFound = true;

							if ("deprecated".equals(tagMatcher.group(1))) {
								deprecated = true;
							}
						}

						if (theLine.length() > maxCommentLineLength()) {
							longLine = true;
						}

						if (!tagFound) {
							if (textCommentPattern.matcher(theLine).find()) {
								noText = false;
							}
						}
						if (SVN_KEYWORDS_PATTERN.matcher(theLine).find()) {
							svnPropertiesFound = true;
						}
					}
				} while (true);
			}
		}
		boolean errorEmptyComment = shouldTestEmptyComment() && (noText && (!deprecated));
		boolean errorLongLine = shouldTestLongLines() && longLine;
		boolean errorMultipleComments = shouldTestMultipleComments() && multipleComments;
		boolean errorSvnProperties = shouldTestSvnProperties() && svnPropertiesFound;
		if (errorEmptyComment || errorLongLine || errorMultipleComments || errorSvnProperties) {
			if (errorEmptyComment) {
				reportFailure(file, ERR_EMPTY_CLASS_COMMENT);
			}
			if (errorLongLine) {
				reportFailure(file, ERR_LINES_TOO_LONG);
			}
			if (errorMultipleComments) {
				reportFailure(file, ERR_MULTIPLE_COMMENTS);
			}
			if (errorSvnProperties) {
				reportFailure(file, ERR_SVN_PROPERTIES);
			}
		}
	}

	private void reportFailure(File file, String message) {
		reportFailure(file, message, null);
	}

	private void reportFailure(File file, String message, Throwable ex) {
		_failures.add("In '" + file.getAbsolutePath() + "': " + message + (ex != null ? report(ex) : ""));
	}

	private String report(Throwable ex) {
		return ex.getClass().getName() + (ex.getMessage() != null ? " (" + ex.getMessage() + ")" : "");
	}

	/**
	 * test if the comments to all java classes are correct.
	 */
	@Override
	public void run(TestResult aResult) {
		initIgnoreSet();
		
		super.run(aResult);
	}

	private void initIgnoreSet() {
		Config config = config();
		_ignoreSet = new HashSet<>(config.getIgnore());
	}

	static Pattern getCopyrightPattern() {
		return config().getCopyrightPattern();
	}

	private static Config config() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Whether an empty class comment should be reported as error.
	 */
	protected boolean shouldTestEmptyComment() {
		// Test is broken anyway - does not detect comments consisting solely of newlines.
		return false;
	}

	/**
	 * Whether comment lines longer than {@link #maxCommentLineLength()} should be reported as
	 * error.
	 */
	protected boolean shouldTestLongLines() {
		return false;
	}

	/**
	 * Whether multiple class documentation comments should be reported as error.
	 */
	protected boolean shouldTestMultipleComments() {
		return true;
	}

	/**
	 * Whether missing SVN properties should be reported as error.
	 */
	protected boolean shouldTestSvnProperties() {
		return true;
	}

    /** Maximum length for comment lines. */
	protected int maxCommentLineLength() {
		return 100;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestComment.class);
	}

}


