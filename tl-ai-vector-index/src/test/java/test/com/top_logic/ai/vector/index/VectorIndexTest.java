/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.vector.index;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.vector.index.VectorIndex;
import com.top_logic.ai.vector.index.VectorReadFilter;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link VectorIndex}.
 */
public class VectorIndexTest extends BasicTestCase {
	private VectorIndex vectorIndex;

	/* Constants definitions go here */
	private final String filesDir = "src/test/java/test/com/top_logic/ai/vector/index/testdata";

	/* Test suite for Vector indexing
	 * 
	 * 1. Test indexing text files; 1.1 Load text files from filesDir; 1.2 Index the texts contained in
	 * these files; 1.3 Search the vector store to verify that texts are loaded in the database and
	 * indexed; 1.3.1 Search by document id; 1.3.2 Search by keywords, specific to particular content
	 * and verify scores; 1.4 Clean the vector database - delete the points loaded in 1.2;
	 * 
	 * 2. Test model instance indexing; 2.1 Configuration preparation - verify that the VectorIndexing
	 * service has configuration for models to index with vectors and store to vector database without
	 * vector embedding; 2.2 Data preparation; 2.2.1 Create vector indexing test model; 2.2.2 Create
	 * no-vector test model; 2.2.3 Create test model which should not be indexed or stored to the
	 * database; 2.2.4 Create instance(s) in each of the models above; 2.3 Verify the index by document
	 * id; 2.4 Verify the index by search string; verify score; 2.5 Delete the instances; 2.6 Verify
	 * points deletion in the vector store; 2.7 Clean - delete any remaining instances and models; */

	private List<Pair<String, Map<String, Object>>> loadTestFiles() {
		List<Pair<String, Map<String, Object>>> contents = new ArrayList<>();

		Set<String> filenames =
			Stream.of(new File(this.filesDir).listFiles())
				.filter(file -> !file.isDirectory())
				.map(File::getName)
				.collect(Collectors.toSet());

		for (String filename : filenames) {
			try {
				Map<String, Object> payload = new HashMap<>();

				String content = Files.readString(Paths.get(this.filesDir + "/" + filename), StandardCharsets.UTF_8);

				payload.put("content", content);

				contents.add(new Pair<>(filename, payload));
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		return contents;
	}

	/**
	 * Test indexing a text document
	 */
	public void testIndexText() {
		List<Pair<String, Map<String, Object>>> contentsToIndex = this.loadTestFiles();

		this.vectorIndex = VectorIndex.getInstance();

		for (Pair<String, Map<String, Object>> content : contentsToIndex) {
			this.vectorIndex.storeToVectorStore(
				this.vectorIndex.getDocumentUUID(content.getFirst().toString()).toString(),
				content.getSecond());
		}
	}

	/** */
	public void testSearchVectorStore() {
		this.vectorIndex = VectorIndex.getInstance();

		List<String> searchQueries = List.of(
			"Sights of ancient empire remains",
			"Remarkable architecture",
			"Oriental spirit");

		for (String query : searchQueries) {
			Logger.info("=============== Search Vector index for matches for query \"" + query + "\"", this.getClass());
			// UNCHECKED, because this test drives the store directly and has no principal to serve;
			// what a real caller may see is asserted against real accounts by SecDemoVectorIndexTest.
			VectorReadFilter.Hits hits =
				this.vectorIndex.searchVectorIndex(query, ReadFilter.forSecurity(ModelSecurity.UNCHECKED));

			for (VectorReadFilter.Hit hit : hits.hits()) {
				Logger.info(hit.match().toString(), this.getClass());
			}
		}
	}

	/**
	 * Creates a TestCase for a specific test method.
	 */
	private static TestCase createTest(String methodName) {
		return new VectorIndexTest(methodName);
	}

	/**
	 * Constructor to create a test case for a specific method.
	 */
	public VectorIndexTest(String testName) {
		super(testName);
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(VectorIndexTest.class.getName());

		// Add tests in explicit order to ensure test ZZZ runs last
		// Using {@link TestSuite#addTestSuite(Class)} with a custom suite that only contains
		// specific methods
		try {
			suite.addTest(createTest("testIndexText"));
			suite.addTest(createTest("testSearchVectorStore"));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create ordered test suite", ex);
		}

		// Wrap with service setup
		BasicRuntimeModule<?>[] modules = VectorIndexTest.getModules();
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(suite, modules));
	}

	/**
	 * @param test
	 *                          test
	 * @param additionalModules
	 *                          additional modules
	 * @return Test
	 */
	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}

	private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		modules.add(com.top_logic.ai.vector.index.VectorIndexingService.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
