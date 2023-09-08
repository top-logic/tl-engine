/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;


/**
 * Test access to a powerpoint template and read and replace some values.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReadWritePowerpointTest extends TestCase {
 
      /**
     * Constructor for ReadWriteTest.
     */
    public ReadWritePowerpointTest(String arg0) {
        super(arg0);
    }
    
    public void testAccessPowerpoint() throws Exception {
		BinaryData template = getPowerpointFile(true);
        assertNotNull("Template not found", template);
        
        // now use the powerpoint class to access the file:
		Powerpoint pptHandler = Powerpoint.getInstance(Powerpoint.isXmlFormat(template));
        Map fields = pptHandler.getValues(template);
        assertNotNull("No fields found in the template",fields);
        
        ArrayList valueList = new ArrayList(2);
        valueList.add("Seppl");
        valueList.add("Berger");
        Object[] keys = getKeysForValues(fields,valueList);
        assertNotNull ("key for Seppl not found",keys[0]);
        //writeFields (fields);
    }
    
    
    public void testAccessPowerpointExpression() throws Exception{
		BinaryData template = getPowerpointFile(true);
        assertNotNull("Template not found", template);
        
        // now use the powerpoint class to access the file:
		Powerpoint pptHandler = Powerpoint.getInstance(Powerpoint.isXmlFormat(template));
        Map fields = pptHandler.getValues(template);
        assertNotNull("No fields found in the template",fields);
        
        // the testtemplate contains two expressions so we need to find them.
        List theKeys = getKeysWithExpressions(fields);
        assertEquals (3,theKeys.size());
        
        // now count the concrete expressions:
        int exp = 0;
        Iterator iter = theKeys.iterator();
        StringBuffer replaced = new StringBuffer ();
        while (iter.hasNext()) {
            Object key =  iter.next();
            Object value = fields.get(key);
            if (value instanceof String) {
                //System.err.println("Original: " + value.toString());
                //test for expression by regular expression :)
                Matcher matcher = expPattern.matcher((String)value);
                
                while (matcher.find()) {
                    exp++;
                    if (matcher.group(1).equals("static") && matcher.group(2).equals ("PERSON_IMAGE")) {
                        matcher.appendReplacement(replaced,"Daheim");
                    } else {
                        matcher.appendReplacement(replaced,"kann ich noch nicht");
                    }
                    //System.err.print(matcher.group(0) + " : ");
                    //System.err.println("type: '" + matcher.group(1) + "' value: '" + matcher.group(2) + "'");
                }
                matcher.appendTail(replaced);
                //System.err.println();System.err.println (replaced.toString());
            }
        }
        assertEquals (3,exp);
        assertTrue (replaced.toString().indexOf("Daheim") > -1);
        assertTrue (replaced.toString().indexOf("<exp type=script>") == -1);
        //writeFields(fields);
    }
    
//    private static void writeFields (Map aFieldMap) {
//        Iterator iter = aFieldMap.keySet().iterator();
//        while (iter.hasNext()) {
//            Object key = (Object) iter.next();
//            System.err.println(key.toString() + " : " + aFieldMap.get(key).toString()); 
//        }
//    }
    
    protected static Pattern expPattern = Pattern.compile ("<exp type=([^>]*)>([^<]*)</exp>");

    /**
     * List all the key where the related values contain an expression.
     * 
     * @return a modifieable List of Strings ...
     */
    private static List getKeysWithExpressions (Map aFieldMap) {
        List result = new ArrayList();
        Iterator iter = aFieldMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object value = aFieldMap.get(key);
            if (value instanceof String) {
                //test for expression by regular expression :)
                Matcher matcher = expPattern.matcher((String)value);
                if (matcher.find()) {
                    result.add(key);
                }
            }
        }
        return result;
    }
    /**
     * TODO enhance for multiple occurrences of the value in the map.
     * depending on the given value list we extract the keys from the map by comparing the values.
     * @return an object array of the same size as the value list with the keys (if available).
     */
    private static Object[] getKeysForValues (Map aFieldMap, List aValueList) {
        Object[] result = new Object[aValueList.size()];
        Iterator iter = aFieldMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object value = aFieldMap.get(key);
            int pos = aValueList.indexOf(value);
            if (pos > -1) {
                result[pos] = key;
            }
        }
        return result;
    }

	private BinaryData getPowerpointFile(boolean isTemplate) throws Exception {
        if (isTemplate) {
			BinaryData theFile = FileManager.getInstance().getData(CommonTestFiles.TEMPLATE_PPT);
            return theFile;
            //return CommonTestFiles.getResultFile();
        } else {
			return BinaryDataFactory.createBinaryData(CommonTestFiles.getResultFile());
        }
    
    
    }
    
    /** Return the suite of Tests to perform */
    public static Test suite () {
        return OfficeTestSetup.createOfficeTestSetup(new TestSuite (ReadWritePowerpointTest.class));
    }
 
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }


}
