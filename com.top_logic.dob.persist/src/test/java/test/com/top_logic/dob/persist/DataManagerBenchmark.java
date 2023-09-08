/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.persist;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DataManagerBenchmark implements Runnable {
	
	public class TestDataObject extends ExampleDataObject {

		public static final String TYPE = "test";
		
		private final TLID id;

		public TestDataObject(TLID id, HashMap attributes) {
			super(attributes);
			this.id = id;
		}
		
		@Override
		public TLID getIdentifier() {
			return id;
		}
		
		@Override
		public String getMetaObjectName() {
			return TYPE;
		}
	}

	private final String benchmarkName;
	
	private final DataManager storage;
	
	private final int objectCount;
	private final int attributeCount;
	
	private final int modificationsPerObjectCount;
	private final int readCount;

	private HashMap attributes;

	private final Random rnd = new Random();

	private final int modifiedAttributesCount;



	public DataManagerBenchmark(String benchmarkName, DataManager storage, int objectCount, int attributeCount, int modificationsPerObjectCount, int modifiedAttributesCount, int readCount) {
		this.benchmarkName = benchmarkName;
		this.storage = storage;
		
		this.objectCount = objectCount;
		this.attributeCount = attributeCount;
		this.modificationsPerObjectCount = modificationsPerObjectCount;
		this.modifiedAttributesCount = modifiedAttributesCount;
		this.readCount = readCount;
	}

	@Override
	public void run() {
		benchmarkCreate();
		benchmarkUpdate();
		benchmarkRead();
	}

	public void benchmarkCreate() {
		time(objectCount, "object creation", 
			new Runnable() {
				@Override
				public void run() {
					performCreate();
				}
			});
	}

	public void benchmarkUpdate() {
		time(modificationsPerObjectCount * objectCount * modifiedAttributesCount, "attribute modification", 
			new Runnable() {
				@Override
				public void run() {
					performUpdate();
				}
			});
	}

	public void benchmarkRead() {
		time(readCount, "object read",
			new Runnable() {
				@Override
				public void run() {
					performRead();
				}
			});
	}

	private void time(int count, String operationName, Runnable job) {
		long elapsed = -System.currentTimeMillis();
		job.run();
		elapsed += System.currentTimeMillis();
		
		Logger.info(benchmarkName + ": Performed " + count + " " + operationName + "s. " + (((float) elapsed) / count) + "ms per " + operationName + ".", this);
	}

	private void performCreate() {
		for (int n = 0; n < objectCount; n++) {
			createObject(n);
		}
	}

	/**
	 * Creates an initial object version identified by the given number.
	 */
	private void createObject(int objectNr) {
		attributes = new HashMap(attributeCount);
		for (int m = 0; m < attributeCount; m++) {
			attributes.put(toAttributeName(m), "init-" + objectNr + "-" + m);
		}
		TestDataObject dataObject = new TestDataObject(toObjectId(objectNr), attributes);
		try {
			storage.store(dataObject);
		} catch (SQLException ex) {
			Logger.error("Error storing " + dataObject, ex, DataManagerBenchmark.class);
		}
	}

	private String toAttributeName(int attributeNr) {
		return "attr-" + attributeNr;
	}

	private TLID toObjectId(int objectNr) {
		// Prevent inserting in index order.
		return StringID.valueOf("obj-" + revert(Integer.toString(objectNr)));
	}
	
	private String revert(String s) {
		int length = s.length();
		StringBuffer result = new StringBuffer(length);
		for (int n = length - 1; n >= 0; n--) {
			result.append(s.charAt(n));
		}
		return result.toString();
	}

	private void performUpdate() {
		{
			int modificationCount = objectCount * modificationsPerObjectCount;
			for (int n = 0; n < modificationCount; n++) {
				int objectNr = rnd.nextInt(objectCount);
				updateObject(n, objectNr);
			}
		}
	}

	private void updateObject(int modificationNr, int objectNr) throws DataObjectException {
		DataObject object;
		TLID objectID = toObjectId(objectNr);
		try {
			object = storage.load(TestDataObject.TYPE, objectID);
		} catch (SQLException ex) {
			Logger.error("Error loading object for id " + objectID, ex, DataManagerBenchmark.class);
			return;
		}
		
		for (int m = 0; m < modifiedAttributesCount; m++) {
			int attributeNr = rnd.nextInt(attributeCount);
			object.setAttributeValue(toAttributeName(attributeNr), "mod-" + modificationNr + "-" + objectNr + "-" + attributeNr);
		}
		
		try {
			storage.store(object);
		} catch (SQLException ex) {
			Logger.error("Unable to update " + object, ex, DataManagerBenchmark.class);
		}
	}

	private void performRead() {
		for (int n = 0; n < readCount; n++) {
			int objectNr = rnd.nextInt(objectCount);
			TLID objectID = toObjectId(objectNr);
			try {
				storage.load(TestDataObject.TYPE, objectID);
			} catch (SQLException ex) {
				Logger.error("Error loading object for id " + objectID, ex, DataManagerBenchmark.class);
			}
		}
	}
	
}
