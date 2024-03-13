/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.io.binary.TestFileBasedBinaryData;
import test.com.top_logic.basic.io.binary.TestingBinaryData;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexData;

/**
 * Base class for {@link FlexDataManager} test cases.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFlexDataManagerTest extends AbstractDBKnowledgeBaseTest {

	/**
	 * UTF-8 string with only characters for the multi-lingual plane fits into the
	 * {@link AbstractFlexDataManager}'s <code>varchar</code> column.
	 */
	static final String SMALL_STRING = BasicTestCase.randomString(AbstractFlexDataManager.VARCHAR_DATA_ATTR_LEN / 2 + 10, false, false, true, false);
	
	/**
	 * UTF-8 string that exactly fits into the {@link AbstractFlexDataManager}'s
	 * <code>varchar</code> column.
	 */
	static final String MEDIUM_STRING = BasicTestCase.randomString(AbstractFlexDataManager.VARCHAR_DATA_ATTR_LEN, true, true, true, false);

	/**
	 * UTF-8 string that must safely fit into the {@link AbstractFlexDataManager}'s
	 * <code>varchar</code> column.
	 * 
	 * <p>
	 * Note: This make the test robust against +-1 errors in the decision when to use the
	 * <code>varchar</code> column in the implementation.
	 * </p>
	 */
	static final String MEDIUM_STRING_1 = BasicTestCase.randomString(AbstractFlexDataManager.VARCHAR_DATA_ATTR_LEN - 1, true, true, true, false);
	
    static final String LONG_STRING = StringServices.getRandomString(new Random(34L), 512);
    
    /**
     * ASCII attribute name with maximal length.
     */
    static final String LONG_ATTR_NAME = BasicTestCase.randomString(AbstractFlexDataManager.MAX_ATTRIBUTE_NAME_LENGTH, true, false, false, false);
    
	static final BinaryData SMALL_BINARY_DATA = BinaryDataFactory.createBinaryData("AvadaKedavra".getBytes(), "text/plain");

	/**
	 * 800k of random data.
	 * 
	 * <p>
	 * Note: The underlying stream must not produce empty chunks, because the
	 * MySQL driver has a bug that makes it stop at reading an empty chunk.
	 * </p>
	 * 
	 * <p>
	 * Note: With the default settings of MySQL, only 1 MB of data can be sent
	 * in a single statement. For larger data, the MySQL variable
	 * <code>max_allowed_packet</code> must be adjusted.
	 * </p>
	 */
	static final BinaryData LONG_BINARY_DATA = new TestingBinaryData(42, 800 * 1024);
    
    /** @see #LONG_BINARY_DATA */
	static final BinaryData LONG_BINARY_DATA_2 = new TestingBinaryData(43, 800 * 1024);
    
    /** @see #LONG_BINARY_DATA */
	static final BinaryData LONG_BINARY_DATA_3 = new TestingBinaryData(44, 800 * 1024);

	static {
		((TestingBinaryData) LONG_BINARY_DATA).setContentType("example/x.testContentType");
		((TestingBinaryData) LONG_BINARY_DATA_2).setContentType("example/x.testContentType");
		((TestingBinaryData) LONG_BINARY_DATA_3).setContentType("example/x.testContentType");
	}

	/*package protected*/ FlexDataManager flexData;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.flexData = createFlexDataManager();
	}

	protected abstract FlexDataManager createFlexDataManager() throws SQLException;
	
    protected abstract boolean supportsBinaryData();

	/**
	 * Creates an object of a subclass of {@link KnowledgeBaseTestScenarioConstants#A_NAME} that can
	 * be stored by the {@link FlexDataManager} under test.
	 * 
	 * @param a1
	 *        {@link KnowledgeBaseTestScenarioConstants#A1_NAME a1} of the result object.
	 * 
	 * @see #newDObject(String) Creation of objects of different type.
	 */
	protected abstract KnowledgeObject newAObject(String a1) throws DataObjectException;

	/**
	 * Creates an object of a subclass of {@link KnowledgeBaseTestScenarioConstants#D_NAME} that can
	 * be stored by the {@link FlexDataManager} under test.
	 * 
	 * @param a1
	 *        {@link KnowledgeBaseTestScenarioConstants#A1_NAME a1} of the result object.
	 * 
	 * @see #newAObject(String) Creation of objects of different type.
	 */
	protected abstract KnowledgeObject newDObject(String a1) throws DataObjectException;

	@Override
	protected void tearDown() throws Exception {
		this.flexData = null;
		
		super.tearDown();
	}

	public void testAllTypes() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			commit(tx);
		}

		{
			Transaction tx = begin();
			FlexData data = loadData(b1, true);
			data.setAttributeValue("true", true);
			data.setAttributeValue("false", false);
			data.setAttributeValue("long", 42L);
			data.setAttributeValue("int", 13);
			data.setAttributeValue("float", 13.25f);
			data.setAttributeValue("double", 13.125d);
			data.setAttributeValue("string", "Hello world");
			data.setAttributeValue("emptyString", "");
			data.setAttributeValue("clob", largeString());
			data.setAttributeValue("blob", LONG_BINARY_DATA);
			storeData(b1, data);
			commit(tx);
		}

		FlexData newData = loadData(b1, false);
		assertEquals(true, newData.getAttributeValue("true"));
		assertEquals(false, newData.getAttributeValue("false"));
		assertEquals(42L, newData.getAttributeValue("long"));
		assertEquals(13, newData.getAttributeValue("int"));
		assertEquals(13.25f, newData.getAttributeValue("float"));
		assertEquals(13.125d, newData.getAttributeValue("double"));
		assertEquals("Hello world", newData.getAttributeValue("string"));
		assertEquals("", newData.getAttributeValue("emptyString"));
		assertEquals(largeString(), newData.getAttributeValue("clob"));
		assertBinaryDataEquals(LONG_BINARY_DATA, newData.getAttributeValue("blob"));
	}

	private String largeString() {
		StringBuilder result = new StringBuilder();

		for (int n = 0; n < 8000; n++) {
			char ch = (char) ('A' + (n % 26));
			result.append(ch);
		}

		return result.toString();
	}

	public void testReadRetry() throws Throwable {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			FlexData data = loadData(b1, true);
			data.setAttributeValue("f1", "f1value");
			storeData(b1, data);
			commit(tx);
		}
		
		DBKnowledgeBaseTestSetup.simulateConnectionBreakdown();
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				FlexData newData = loadData(b1, false);
				assertNotNull(newData);
				assertEquals("f1value", newData.getAttributeValue("f1"));
			}
		});
	}

	public void testNoInitalData() throws DataObjectException {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			
			assertEquals("No data for a new object.", 0, loadData(b1, false).getAttributes().size());
			
			commit(tx);
		}
		
		assertEquals("No data for a new object.", 0, loadData(b1, false).getAttributes().size());
	}

	public void testAssociateDataUponCreate() throws DataObjectException {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			
			final FlexData data = loadData(b1, true);
			assertNotNull(data);
			
			data.setAttributeValue(LONG_ATTR_NAME, "longattr");
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr2", LONG_STRING);
			data.setAttributeValue("attr4", MEDIUM_STRING);
			data.setAttributeValue("attr4a", MEDIUM_STRING_1);
			data.setAttributeValue("attr5", SMALL_STRING);
			if (supportsBinaryData()) {
			    data.setAttributeValue("attr3", LONG_BINARY_DATA);
			    data.setAttributeValue("attr3a", LONG_BINARY_DATA_2);
			    data.setAttributeValue("attr3b", LONG_BINARY_DATA_3);
			}
			
			storeData(b1, data);
			commit(tx);
		}
		
		FlexData persistentData = loadData(b1, false);
		assertNotNull(persistentData);
		assertEquals("longattr"   , getValue(persistentData, LONG_ATTR_NAME));
		assertEquals("value1"   , getValue(persistentData, "attr1"));
		assertEquals(LONG_STRING, getValue(persistentData, "attr2"));
		assertEquals(MEDIUM_STRING, getValue(persistentData, "attr4"));
		assertEquals(MEDIUM_STRING_1, getValue(persistentData, "attr4a"));
		assertEquals(SMALL_STRING, getValue(persistentData, "attr5"));
		if (supportsBinaryData()) {
			assertBinaryDataEquals(LONG_BINARY_DATA, getValue(persistentData, "attr3"));
			assertBinaryDataEquals(LONG_BINARY_DATA_2, getValue(persistentData, "attr3a"));
			assertBinaryDataEquals(LONG_BINARY_DATA_3, getValue(persistentData, "attr3b"));
		    // Break Implementation of FileBaseBinaryData to provoke reload
		    TestFileBasedBinaryData.cleanupFiles();
			assertBinaryDataEquals(LONG_BINARY_DATA, getValue(persistentData, "attr3"));
		}
	}

	public void testAssociateDataToExistingObject() throws DataObjectException {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			
			commit(tx);
		}
		
		final FlexData data = loadData(b1, true);
		assertNotNull(data);
		
		{
			Transaction tx = begin();
			if (supportsBinaryData()) {
			    data.setAttributeValue("attr0", SMALL_BINARY_DATA);
			}
			data.setAttributeValue(LONG_ATTR_NAME, "longattr");
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr2", LONG_STRING);
			data.setAttributeValue("attr4", MEDIUM_STRING);
			data.setAttributeValue("attr4a", MEDIUM_STRING_1);
			data.setAttributeValue("attr5", SMALL_STRING);
			
			storeData(b1, data);
			commit(tx);
		}
		
		FlexData persistentData = loadData(b1, false);
		assertNotNull(persistentData);
        if (supportsBinaryData()) {
			assertBinaryDataEquals(SMALL_BINARY_DATA, getValue(persistentData, "attr0"));
        }
        assertEquals("longattr", getValue(persistentData, LONG_ATTR_NAME));
		assertEquals("value1"   , getValue(persistentData, "attr1"));
		assertEquals(LONG_STRING, getValue(persistentData, "attr2"));
		assertEquals(MEDIUM_STRING, getValue(persistentData, "attr4"));
		assertEquals(MEDIUM_STRING_1, getValue(persistentData, "attr4a"));
		assertEquals(SMALL_STRING, getValue(persistentData, "attr5"));
	}
	
	static String filledWith(int len, char c) {
	    char[] fillin = new char[len];
	    Arrays.fill(fillin, c);    
	    return new String(fillin);
	}

	/**
	 * Test using UTF-8 data which will need extra space in the DB.
	 * 
	 * This is a regression test for #1854.
	 */
    public void testEncoding() throws DataObjectException {
    	String s128 = filledWith(128,'\u20ac'); // EURO 
    	String s254 = filledWith(254,'\u00c4'); // A-Umlaut
    	String s511 = filledWith(511,'\u00df'); // SZLig
    	
    	final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newAObject("ee");
			final FlexData data = loadData(b1, true);
	        
	        data.setAttributeValue("attr128", s128);
	        data.setAttributeValue("attr254", s254);
	        data.setAttributeValue("attr511", s511);
	        storeData(b1, data);
	        commit(tx);
		}
		
		FlexData persistentData = loadData(b1, false);
        assertEquals(s128      , getValue(persistentData, "attr128"));
        assertEquals(s254      , getValue(persistentData, "attr254"));
        assertEquals(s511      , getValue(persistentData, "attr511"));
    }

    public void testUpdateData() throws DataObjectException {
    	final KnowledgeObject b1;
		final FlexData data;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			data = loadData(b1, true);
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr2", Boolean.TRUE);
			if (supportsBinaryData()) {
			    data.setAttributeValue("attr3", LONG_BINARY_DATA);
			    data.setAttributeValue("attr3a", LONG_BINARY_DATA_2);
			    data.setAttributeValue("attr3b", LONG_BINARY_DATA_3);
			}
			storeData(b1, data);
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			data.setAttributeValue("attr1", "newValue1");
			data.setAttributeValue("attr2", null);
			data.setAttributeValue("attr3", Boolean.FALSE);
	        if (supportsBinaryData()) {
	            data.setAttributeValue("attr4", SMALL_BINARY_DATA);
	        }
			storeData(b1, data);
			commit(tx);
		}
		
		FlexData persistentData = loadData(b1, false);
		assertNotNull(persistentData);
		assertEquals("newValue1"      , getValue(persistentData, "attr1"));
		assertNull  (                   getValue(persistentData, "attr2"));
		assertEquals(Boolean.FALSE    , getValue(persistentData, "attr3"));
		if (supportsBinaryData()) {
			assertBinaryDataEquals(SMALL_BINARY_DATA, getValue(persistentData, "attr4"));
		}
	}

	public void testUpdateDataRedudantly() throws DataObjectException {
		final KnowledgeObject b1;
		final FlexData data;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			data = loadData(b1, true);
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr1a", Integer.valueOf(12));
			data.setAttributeValue("attr2", "value2");
			data.setAttributeValue("attr2b", Double.valueOf(Math.PI));
			data.setAttributeValue("attr3a", new Date(22222222));
			if (supportsBinaryData()) {
	            data.setAttributeValue("attr4", SMALL_BINARY_DATA);
	        }
			storeData(b1, data);
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			data.setAttributeValue("attr1", "tempChange1");
			data.setAttributeValue("attr1a", null);
			data.setAttributeValue("attr2", "tempChange2");
			data.setAttributeValue("attr2a", "tempAdd2a");
			data.setAttributeValue("attr2b", null);
			data.setAttributeValue("attr3", "tempAdd");
			data.setAttributeValue("attr3", null);
			data.setAttributeValue("attr3a", "tempUpdate3a");
			data.setAttributeValue("attr3a", null);
			data.setAttributeValue("attr4" , null);
			
			// Update after intermediate update
			data.setAttributeValue("attr1", "newValue1");
			// Update after intermediate delete
			data.setAttributeValue("attr1a", Integer.valueOf(99));
			
			// Delete after intermediate update
			data.setAttributeValue("attr2", null);
			// Delete after intermediate add
			data.setAttributeValue("attr2a", null);
			// Delete after intermediate delete
			data.setAttributeValue("attr2b", null);
			
			// Add after intermediate add and delete
			data.setAttributeValue("attr3", "value3");
			// Add after intermediate update and delete
			data.setAttributeValue("attr3a", "value3a");
			
			if (supportsBinaryData()) {
	            data.setAttributeValue("attr4", LONG_BINARY_DATA);
	            data.setAttributeValue("attr4a", LONG_BINARY_DATA_2);
	            data.setAttributeValue("attr4b", LONG_BINARY_DATA_3);
	        }
	        storeData(b1, data);
			commit(tx);
		}
		
		FlexData persistentData = loadData(b1, false);
		assertNotNull(persistentData);
		assertEquals("newValue1", getValue(persistentData, "attr1"));
		assertEquals(Integer.valueOf(99), getValue(persistentData, "attr1a"));
		assertNull(getValue(persistentData, "attr2"));
		assertNull(getValue(persistentData, "attr2a"));
		assertNull(getValue(persistentData, "attr2b"));
		assertEquals("value3", getValue(persistentData, "attr3"));
		assertEquals("value3a", getValue(persistentData, "attr3a"));
		if (supportsBinaryData()) {
			assertBinaryDataEquals(LONG_BINARY_DATA, getValue(persistentData, "attr4"));
			assertBinaryDataEquals(LONG_BINARY_DATA_2, getValue(persistentData, "attr4a"));
			assertBinaryDataEquals(LONG_BINARY_DATA_3, getValue(persistentData, "attr4b"));
		}
	}

	private void assertFlexValuesEquals(final FlexData expected, final FlexData actual) {
		if (expected == null) {
			assertNull("Expected null value but got: " + actual, actual);
		} else {
			assertNotNull("Expected non null value but got.");
			assertEquals("Different set of attributes.", set(expected.getAttributes()), set(actual.getAttributes()));
			for (String attribute : expected.getAttributes()) {
				assertEquals("Values for attribute '" + attribute + "' are different.",
					expected.getAttributeValue(attribute), actual.getAttributeValue(attribute));
			}
		}
	}

	private static void assertBinaryDataEquals(BinaryData expected, Object actual) {
		assertEquals(expected, actual);
		if (expected != null) {
			assertEquals(expected.getContentType(), ((BinaryData) actual).getContentType());
		}
	}

	protected Object getValue(FlexData values, String name) throws NoSuchAttributeException {
		return values.getAttributeValue(name);
	}

	public void testDeleteData() throws DataObjectException {
		final KnowledgeObject b1;
		final FlexData data;
		Revision createRevision;
		{
			Transaction tx = begin();
			b1 = newAObject("a1");
			data = loadData(b1, true);
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr2", "value2");
			storeData(b1, data);
			commit(tx);
			createRevision = tx.getCommitRevision();
		}
		
		final FlexData persistentData = loadData(b1, false);
		assertNotNull(persistentData);
		
		{
			Transaction tx = begin();
			deleteData(b1);
			commit(tx);
		}
		
		final FlexData reloadedData = loadData(b1, false);
		assertEquals(0, reloadedData.getAttributes().size());
		
		if (((MOClass) b1.tTable()).isVersioned()) {
			final KnowledgeItem historicB1 = HistoryUtils.getKnowledgeItem(createRevision, b1);
			final FlexData historicData = loadData(historicB1, false);
			assertNotNull(historicData);
			assertFlexValuesEquals(persistentData, historicData);
		}
	}

	protected FlexData loadData(final KnowledgeItem b1, boolean mutable) {
		return flexData.load(b1.getKnowledgeBase(), b1.tId(), mutable);
	}

	protected void storeData(final KnowledgeItem b1, final FlexData data) {
		commitHandler().addCommittable(new CommittableAdapter() {
			@Override
			public boolean prepare(CommitContext context) {
				return flexData.store(b1.tId(), data, context);
			}
		});
	}
	
	protected void deleteData(final KnowledgeObject b1) {
		commitHandler().addCommittable(new CommittableAdapter() {
			@Override
			public boolean prepare(CommitContext context) {
				return flexData.delete(b1.tId(), context);
			}
		});
	}

	static class AttributeLoaderChecker implements AttributeLoader<KnowledgeObject> {

		List<KnowledgeObject> _loaded = new ArrayList<>();

		List<KnowledgeObject> _loadedEmpty = new ArrayList<>();

		@Override
		public void loadData(long dataRevision, KnowledgeObject baseObject, FlexData data) {
			_loaded.add(baseObject);
		}

		@Override
		public void loadEmpty(long dataRevision, KnowledgeObject baseObject) {
			_loadedEmpty.add(baseObject);
		}

	}

	public void testLoadAll() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject aObj = newAObject("a1");
		FlexData data = loadData(aObj, true);
		data.setAttributeValue("attr1", "value1");
		storeData(aObj, data);
		commit(tx);

		AttributeLoaderChecker checker = new AttributeLoaderChecker();
		loadData(list(aObj, aObj), checker);

		assertTrue("Duplicate loading should not lead to empty loading: " + checker._loadedEmpty,
			checker._loadedEmpty.isEmpty());
	}

	public void testLoadAllWithDifferentObjects() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject aObj = newAObject("a1");
		FlexData aData = loadData(aObj, true);
		aData.setAttributeValue("attr1", "value1");
		storeData(aObj, aData);
		KnowledgeObject dObj = newAObject("a1");
		FlexData dData = loadData(dObj, true);
		dData.setAttributeValue("attr1", "value1");
		storeData(dObj, dData);
		commit(tx);

		AttributeLoaderChecker checker = new AttributeLoaderChecker();
		loadData(list(aObj, aObj, dObj, dObj, dObj), checker);

		assertTrue("Duplicate loading should not lead to empty loading: " + checker._loadedEmpty,
			checker._loadedEmpty.isEmpty());
	}

	public void testLoadAllMaxCountReached() throws DataObjectException, SQLException {
		Transaction tx = begin();
		int maxSetSize = kb().getConnectionPool().getSQLDialect().getMaxSetSize();
		List<KnowledgeObject> baseObjects = new ArrayList<>();
		for (int i = 0; i < maxSetSize + 1; i++) {
			KnowledgeObject aObj = newAObject("a1");
			FlexData aData = loadData(aObj, true);
			aData.setAttributeValue("attr1", "value1");
			storeData(aObj, aData);
			baseObjects.add(0, aObj);
			baseObjects.add(aObj);
		}
		commit(tx);

		AttributeLoaderChecker checker = new AttributeLoaderChecker();
		loadData(baseObjects, checker);

		assertTrue("Duplicate loading should not lead to empty loading: " + checker._loadedEmpty,
			checker._loadedEmpty.isEmpty());
	}

	protected <T extends KnowledgeItem> void loadData(List<T> items, AttributeLoader<T> callback) {
		if (items.size() > 0) {
			flexData.loadAll(callback, KnowledgeItem.KEY_MAPPING, items, items.get(0).getKnowledgeBase());
		}
	}

}
