/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;

/**
 * Tests the {@link com.top_logic.dsa.DataSourceAdaptor}.
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public abstract class TestDataSourceAdapter extends BasicTestCase {
	
    /** DemoData used for Streamning */
    public static final byte DATA[] = 
		("der krümmungsgrad der banana beträgt 1,5cm " + TestDataSourceAdapter.class.getName()).getBytes();

    private DataSourceAdaptor dataSourceAdaptor;
	
	
	/**
	 * Default Ctor with function name.
	 */
	public TestDataSourceAdapter(String someName) {
		super(someName);
	}

    /** Create an InputStream with some demo-Data */
    protected InputStream getInputStream() {
        return new ByteArrayInputStream(DATA);
    }

	public void testReadWriteDelete() throws DatabaseAccessException{
		
        try {
            // As the entry does not yet exist the put operation should fail
            dataSourceAdaptor.putEntry(getValidEntryName(), getInputStream());
            fail("Expected DatabaseAccessException");
        } catch (DatabaseAccessException expected) { /* expected */ }
		
		// ok now lets create an entry
        String text = "der krümmungsgrad der banana beträgt 1,5cm";
		String theString = dataSourceAdaptor.createContainer(text, text);
		assertNotNull(theString);
	}
	
	/** 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		/* Our subclasses will provide us with a concrete instance */
		this.dataSourceAdaptor = getInstance();
		assertNotNull(dataSourceAdaptor);
	}
	
	
	/** 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		this.dataSourceAdaptor.close();
        this.dataSourceAdaptor = null;
	}

	/**
	 * Returns an instance of a class that should be tested.
	 * 
	 * @return an instance of a class that should be tested
	 */
	protected abstract DataSourceAdaptor getInstance() throws DatabaseAccessException;
	
	
	/**
	 * Subclasses should return a string that they consider to be a valid entry name.
	 * 
	 * @return return a valid entry name.
	 */
	protected abstract String getValidEntryName();
	
}
