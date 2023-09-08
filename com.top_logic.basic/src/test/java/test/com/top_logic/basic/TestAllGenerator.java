/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.generate.JavaGenerator;

/**
 * Generator of TestAll files for `test.com.top_logic` packages.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class TestAllGenerator {

	/**
	 * parameter to determine whether the TestAll must be created also for
	 * subpackages.
	 */
	private static final String RECURSIVE_OPTION = "-recursively";

	/**
	 * parameter to determine whether existing TestAll must be overridden
	 */
	private static final String OVERRIDE_OPTION = "-override";

	/**
	 * parameter for the folder for which the TestAll must be created
	 */
	private static final String DEST_OPTION = "-dest";

	/**
	 * Filter that determines whether there is an existing test
	 */
	private static FilenameFilter CONTAINS_TEST_CLASS = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("Test") && name.endsWith(".java") && Character.isUpperCase(name.charAt(4));
		}
	};

	/**
	 * {@link FileFilter} which accepts non hidden directories.
	 */
	private static final FileFilter FOLDER_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			boolean accept = pathname.isDirectory();
			String dirName = pathname.getName();
			accept = accept && !dirName.equalsIgnoreCase("cvs");
			accept = accept && !dirName.equalsIgnoreCase("svn");
			accept = accept && !StringServices.startsWithChar(dirName, '.');

			return accept;
		}
	};

	/**
	 * whether creating must also happens for subpackages
	 */
	private static boolean recursively;

	/**
	 * destination folder for the TestAll
	 */
	private static File destination;

	/**
	 * whether existing TestAll must be overridden
	 */
	private static boolean override;

	public static void main(String[] args) {

		Protocol log = new LogProtocol(TestAllGenerator.class);
		parseArgs(args, log);

		if (!destination.isDirectory()) {
			destination = destination.getParentFile();
		}
		try {
			destination = destination.getCanonicalFile();
		} catch (IOException ex) {
			throw log.fatal("Unable to determine canonical file from '" + destination + "'", ex);
		}

		String pckg = determinePackage(log, destination);
		createTestAll(log, destination, pckg);

		log.checkErrors();
	}

	private static void parseArgs(String[] args, Protocol log) {
		for (int i = 0; i < args.length; i++) {
			String option = args[i];
			if (RECURSIVE_OPTION.equals(option)) {
				recursively = true;
				log.info("Create TestAll recursively");
			} else if (DEST_OPTION.equals(option)) {
				i++;
				if (i == args.length) {
					throw log.fatal("Destination must be set");
				}
				destination = new File(args[i]);
				if (!destination.exists()) {
					throw log.fatal("Can not determine target folder from: " + args[i]);
				}
			} else if (OVERRIDE_OPTION.equals(option)) {
				override = true;
				log.info("Override existing TestAll");
			} else {
				log.info("Unknown Option: " + option);
			}
		}
		if (destination == null) {
			throw log.fatal("Option '" + DEST_OPTION + "' must be set");
		}
	}

	/**
	 * This method determines the java {@link Package} corresponding to the
	 * given file.
	 * 
	 * @param f
	 *        the source directory to write the TestAll to
	 * 
	 * @return the package name for the TestAll file
	 */
	private static String determinePackage(Protocol log, File f) {
		log.info("Determine package from target folder '" + f + "'", Protocol.DEBUG);

		String canonicalPath = f.getPath();
		String dottedPath = canonicalPath.replace(File.separatorChar, '.');

		int indexOf = dottedPath.indexOf("test.com.top_logic");
		if (indexOf == -1) {
			throw log.fatal("Unable to determine package starting with 'test.com.top_logic' in the given folder '" + f
					+ "'");
		}
		return dottedPath.substring(indexOf);
	}

	private static void createTestAll(Protocol log, File targetFolder, String pckgName) {
		log.info("Check for TestAll in '" + targetFolder + "'", Protocol.VERBOSE);
		File testAllFile = new File(targetFolder, "TestAll.java");
		if (override || !testAllFile.exists()) {
			if (!recursively || targetFolder.list(CONTAINS_TEST_CLASS).length > 0) {
				createFile(log, testAllFile, pckgName);
			} else {
				log.info("No test files in '" + targetFolder + "'.", Protocol.DEBUG);
			}
		} else {
			log.info("File '" + testAllFile + "' already exists", Protocol.DEBUG);
		}
		if (recursively) {
			File[] subfolders = targetFolder.listFiles(FOLDER_FILTER);
			for (File folder : subfolders) {
				createTestAll(log, folder, pckgName + '.' + folder.getName());
			}
		}
	}

	private static void createFile(Protocol log, File testAllFile, String pckgName) {
		log.info("Create TestAll in package '" + pckgName + "'");
		try {
			new TestAllTemplate(pckgName).generate(testAllFile);
		} catch (IOException ex) {
			log.fatal("Unable to write File '" + testAllFile + "'", ex);
		}
	}

	/**
	 * Template to create TestAlls.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class TestAllTemplate extends JavaGenerator {

		public TestAllTemplate(String packageName) {
			super(packageName);
		}

		@Override
		protected void writeBody() {
			importLine("junit.framework.Test");
			importLine("test.com.top_logic.basic.BasicTestCase");
			importLine("test.com.top_logic.basic.TestUtils");
			nl();
			importLine("com.top_logic.basic.LogProtocol");
			nl();
			javadocStart();
			commentLine("All tests in this package.");
			commentLine("");
			writeCvsTags();
			javadocStop();
			line("public final class " + className() + " {");
			nl();
			javadocStart();
			commentLine("Creates a test containing all tests in package '" + packageName() + "' and all subpackages.");
			javadocStop();
			line("public static Test suite() {");
			{
				line("final String testPackageName = TestAll.class.getPackage().getName();");
				line("final LogProtocol log = new LogProtocol(TestAll.class);");
				line("final Test allTests = BasicTestCase.createTests(testPackageName, log, true);");
				nl();
				comment("merge tests to avoid multiple unnecessary setups");
				line("TestUtils.rearrange(allTests);");
				nl();
				line("return allTests;");
			}
			line("}");
			nl();
			line("}");
		}

		@Override
		public String className() {
			return "TestAll";
		}

	}

}
