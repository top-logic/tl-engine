/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;

/**
 * The class {@link DataSetup} creates some {@link KnowledgeObject}s for testing.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DataSetup extends ThreadContextSetup {

	private static ArrayList<KnowledgeObject> knowledgeObjects;
	
	private final String dataRoot;

	/**
	 * Factory method to create setup for the given test
	 */
	public static DataSetup createDataSetup(Test test, String dataRoot) {
		return new DataSetup(test, dataRoot);
	}

	private DataSetup(Test test, String dataRoot) {
		super(test);
		this.dataRoot = dataRoot;
	}

	@Override
	protected void doSetUp() throws Exception {
		
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		DataSetup.knowledgeObjects = new ArrayList<>(1024);

		final Transaction createDocTx = kb.beginTransaction();
		{
			// make some indexing
			DataAccessProxy theDir = new DataAccessProxy("testRoot://" + dataRoot);
			int addedContent = addContainer(theDir, kb);
			assertTrue("No content to index ?", addedContent > 0);
		}
		createDocTx.commit();

	}

	private int addContainer(DataAccessProxy container, KnowledgeBase kb) throws Exception {
		String theName = container.getName();
		if ((theName.startsWith(".") && theName.length() > 1) || "CVS".equals(theName)) {
			return 0; // ignore CVS .svn and such ...
		}
		if (container.isContainer()) {
			int result = 0;
			DataAccessProxy[] entries = container.getEntries();
			int size = entries.length;
			for (int i = 0; i < size; i++) {
				DataAccessProxy entry = entries[i];
				result += addContainer(entry, kb);
			}
			return result;
		} else {
			// container is in this case an entry
			return addEntry(container, kb);
		}
	}

	private int addEntry(DataAccessProxy anEntry, KnowledgeBase kb) {
		Document doc = Document.createDocument(anEntry.getPath(), anEntry.getPath(), kb);

		KnowledgeObject docKO = doc.tHandle();
		getKnowledgeObjects().add(docKO);
		return 1;
	}


	@Override
	protected void doTearDown() throws Exception {
		List<KnowledgeObject> createdObjects = getKnowledgeObjects();
		DataSetup.knowledgeObjects = null;

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction removeTx = kb.beginTransaction()) {
			kb.deleteAll(createdObjects);
			removeTx.commit();
		}
	}

	/**
	 * Returns the modifiable list of created KO's. Must just be called in a test which uses this setup
	 */
	public static ArrayList<KnowledgeObject> getKnowledgeObjects() {
		return knowledgeObjects;
	}

}

