/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.io.File;
import java.util.Properties;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.reporting.office.AbstractDelegationSymbolResolver;
import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ReportReaderWriter;
import com.top_logic.reporting.office.StaticSymbolResolver;
import com.top_logic.reporting.office.basic.AbstractReport;
import com.top_logic.reporting.office.basic.BasicPPTReaderWriter;
import com.top_logic.util.error.TopLogicException;


/**
 * A relativly simple report which creates a PPT report consists of one slide with some data about a
 * {@link Person}.
 * 
 * @author <a href=mailto:jco@top-logic.com>Jörg Connotte</a>
 */
public class PersonSlideReport extends AbstractReport {

    /**
     * @see com.top_logic.reporting.office.basic.AbstractReport#getTemplateFileName()
     */
    @Override
	protected String getTemplateFileName() {
        return CommonTestFiles.TEMPLATE_PPT;
    }

    /**
     * @see com.top_logic.reporting.office.basic.AbstractReport#getResultFile()
     */
    @Override
	protected File getResultFile() {
        String theID = (this.contextUser != null) ? this.contextUser.getUserName() : "unknown";

        return CommonTestFiles.createUniqueResultFile(theID);
    }

	/**
     * @see com.top_logic.reporting.office.basic.AbstractReport#initResolver()
     */
    @Override
	protected StaticSymbolResolver initResolver() {
        return new PersonReportResolver();
    }

    /**
     * @see com.top_logic.reporting.office.basic.AbstractReport#initReportReaderWriter()
     */
    @Override
	protected ReportReaderWriter initReportReaderWriter() {
        return (new BasicPPTReaderWriter());
    }

    /**
     * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
     */
    public class PersonReportResolver extends AbstractDelegationSymbolResolver {

        /**
         * For now define a method to resolve the image for a user.
         * @see com.top_logic.reporting.office.AbstractDelegationSymbolResolver#initDelegationMap()
         */
        @Override
		protected void initDelegationMap() {
            delegationMap = new Properties();

            delegationMap.setProperty("APP_CONTEXT","getAppContext");
            delegationMap.setProperty("PERSON_IMAGE","getImageFilePathForPerson");
        }
        
        /**
         * just for testing use the AliasManager to resolve something
         * @param aContext the expansion context
         * @return a String or <code>null</code>
         */
        public String getAppContext (ExpansionContext aContext) {
			return AliasManager.getInstance().getAlias(ReportGeneratorTest.EXAMPLE_ALIAS);
        }
        
        /**
         * Try to locate the image of the current person as contained in the
         * business object 'person' in the expansion context. 
         * @param aContext the expansion context
         * @return a file path to the image to enter in the slide or <code>null</code> if something is missing
         */
		public BinaryData getImageFilePathForPerson(ExpansionContext aContext) {
            Person thePerson = (Person) aContext.getBusinessObjects().get("person");

            if (thePerson == null) {
                Logger.error ("neccessary business object 'person' is missing",this);
                return null;
            }
            // first get the User from the person:
            try {
                UserInterface theUser    = thePerson.getUser();
                String        thePath    = "/images/people/";
                String        theSuffix  = ".jpg";
                FileManager   theManager = FileManager.getInstance();
				BinaryData theFile = theManager.getDataOrNull(thePath + theUser.getUserName().toLowerCase() + theSuffix);

                if (theFile ==  null) { // no image? use default image!
                    theFile = theManager.getDataOrNull(thePath + "unknown" + theSuffix);
                }

                return theFile;
            }
            catch (Exception e) {
                throw new TopLogicException (this.getClass(),"getImageFilePathForPerson",null,e);
            }        
        }
    }
}
