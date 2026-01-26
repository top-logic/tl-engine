/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.vector.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.json.simple.JSONObject;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.vector.index.VectorIndex;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link VectorIndex}.
 */
public class VectorIndexTest extends BasicTestCase {
	private final VectorIndex vectorIndex = VectorIndex.getInstance();

	/* Constants definitions go here */
	private final String filesDir = "src/test/java/test/com/top_logic/ai/vector/index/testdata";

	private List<Pair<String, JSONObject>> loadTestFiles() {
		List<Pair<String, JSONObject>> contents = new ArrayList<>();

//		List<String> contents = new ArrayList<>();

		Set<String> filenames =
			Stream.of(new File(this.filesDir).listFiles())
			.filter(file -> !file.isDirectory())
			.map(File::getName)
			.collect(Collectors.toSet());

		for (String filename : filenames) {
			try {
				JSONObject jo = new JSONObject();

				jo.put("content", new String(Files.readAllBytes(Paths.get(this.filesDir + "/" + filename))));

				contents.add(new Pair(filename, jo));
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
		List<Pair<String, JSONObject>> contentsToIndex = this.loadTestFiles();
		
		for (Pair<String, JSONObject> content : contentsToIndex) {
			this.vectorIndex.storeVectorIndexToQdrantDb_(
				this.vectorIndex.getDocumentUUID(content.getFirst().toString()).toString(),
				content.getSecond());
		}
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(VectorIndexTest.class);
	}

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

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
