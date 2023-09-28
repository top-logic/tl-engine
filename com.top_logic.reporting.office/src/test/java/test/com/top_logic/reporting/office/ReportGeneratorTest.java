/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.reporting.office.ReportGenerator;
import com.top_logic.reporting.office.ReportResult;
import com.top_logic.reporting.office.ReportToken;
import com.top_logic.util.error.TopLogicException;

/**
 * Testcase to generate a report (powerpoint) to evaluate the behaviour of the
 * ReportGenerator.
 * 
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportGeneratorTest extends BasicTestCase {

	public static String EXAMPLE_ALIAS = "%EXAMPLE_ALIAS%";

    public void testReloadableInterface() {
        ReportGenerator theGenerator = ReportGenerator.getInstance();
        assertEquals("ReportGenerator", theGenerator.getName());
        assertNotNull(theGenerator.getDescription());
        assertTrue(theGenerator.usesXMLProperties());

        // now call the reload method and get a report handler!
        theGenerator.reload();
        Map<String, String> env = new HashMap<>(1);
        env.put("successful", "true");
        ReportToken newToken = theGenerator
                .requestReport("wordNoop", env, null);
        assertNotNull(newToken);
    }

    public void testReportHandlerInvalid() throws Exception {
        ReportGenerator theGenerator = ReportGenerator.getInstance();
        Map<String, String> env = new HashMap<>(1);
        env.put("successful", "false");

        ReportToken newToken;

        try {
            newToken = theGenerator.requestReport("noSuchReport", env, null);
            fail("this report is not configured so why do we get a token?");
        }
        catch (TopLogicException exp) {
            // expected behaviour here!
        }

        try {
            newToken = theGenerator.requestReport("invalidKind", env, null);
            fail("Illegal kind configured why to we get here?");
        }
        catch (TopLogicException exp) {
            // expected behaviour here!
        }
        // for completness sake request an excel report :)
        env.put("successful", "true");
        newToken = theGenerator.requestReport("excelNoop", env, null);
        assertNotNull(newToken);
        Thread.sleep(1000);
        assertTrue(theGenerator.reportReady(newToken));

        assertNotNull(theGenerator.getReportResult(newToken).getReportFile());

    }

    public void testGetReportResult() throws Exception {
		BinaryContent resultFile = null;
        try {
            ThreadContext.pushSuperUser();
            Map<String, ?> env = getEnvironment();
            Person thePerson = (Person) env.get("person");
            UserInterface theUser = Person.userOrNull(thePerson);
            ReportToken theToken = ReportGenerator.getInstance()
                    .requestReport("personSlide", env, theUser);
            boolean finished = false;
            // wait for the report to finish but wait at most 10 sec!
            int counter = 0;
            do {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException exp) {
                    exp.printStackTrace();
                }
                finished = ReportGenerator.getInstance().reportReady(theToken);
                counter++;
            } while (counter <= 10 && !finished);

            ReportResult theResult = ReportGenerator.getInstance()
                    .getReportResult(theToken);
            assertNotNull("ReportResult is null", theResult);
            resultFile = theResult.getReportFile();
            assertNotNull("Report was unsuccessfull", resultFile);

            // now check if the content of the generated file contains the
            // expected string!
            try {
				Powerpoint parser = Powerpoint.getInstance(Powerpoint.isXmlFormat(resultFile));
				Map fieldMap = parser.getValues(resultFile.getStream());

                Iterator iter       = fieldMap.values().iterator();
				String exampleAlias = AliasManager.getInstance().getAlias(EXAMPLE_ALIAS);
				assertNotNull("Expected " + EXAMPLE_ALIAS + " to be set. Check the config.xml whether the "
					+ EXAMPLE_ALIAS + " is set!", exampleAlias);
                boolean userNameFound = false;
				boolean exampleAliasFound = false;
                while (iter.hasNext()) {
                    Object element = iter.next();
                    assertTrue(element instanceof String);
                    String content = (String) element;
                    if (content.indexOf("Administrator") > -1) {
                        userNameFound = true;
                    }
					if (content.indexOf(exampleAlias) > -1) {
						exampleAliasFound = true;
                    }
                }
				if (userNameFound) {
					fail(
						"Test should fail due to the known bug in ticket #20523: script replacing was unsuccessful in the result file, ROOT not found");
                }
				if (exampleAliasFound) {
					fail(
						"Test should fail due to the known bug in ticket #20523: static replacing failed, the alias not found");
                }
            }
            catch (OfficeException exp) {
                fail("unable to parse powerpointfile" + resultFile.toString());
            }

        }
        finally {
            ThreadContext.popSuperUser();
        }
    }

    private Map<String, ?> getEnvironment() throws Exception {
        Map<String, Object> environmentMap = new HashMap<>();
        Person theRoot = PersonManager.getManager().getRoot();

        assertNotNull("There is no root user", theRoot);
        environmentMap.put("locale", new Locale("en", "US"));
        environmentMap.put("person", theRoot);
        return environmentMap;
    }

    public ReportGeneratorTest(String arg) {
        super(arg);
    }

    /** Return the suite of Tests to perform */
    public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(ReportGeneratorTest.class));
    }

    public static void main(String[] args) {
        Logger.configureStdout();
        KBSetup.setCreateTables(false); // avoids reset of database
        junit.textui.TestRunner.run(suite());
    }

}