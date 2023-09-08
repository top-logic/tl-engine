/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.Log;
import com.top_logic.basic.Settings;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Tests for binary attributes in the {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestBinaryDataAttribute extends AbstractDBKnowledgeBaseTest {

	static final String TYPE = "TypeWithBinary";

	static final String TYPE_EXTENSION = "TypeWithBinaryExtension";

	static final String BINARY_ROW_ATTR = "binaryRowAttribute";

	static final String BINARY_FLEX_ATTR = "binaryFlexAttribute";

	private File _tempDir;

	private Object _formerTempDir;

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = new DBKnowledgeBaseTestSetup(self);
		setup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOClass type = new MOKnowledgeItemImpl(TYPE);
				type.setSuperclass(new DeferredMetaObject(B_NAME));
				type.addAttribute(new MOAttributeImpl(BINARY_ROW_ATTR, MOPrimitive.BLOB, false));
				typeRepository.addMetaObject(type);

				MOClass extendedType = new MOKnowledgeItemImpl(TYPE_EXTENSION);
				extendedType.setSuperclass(type);
				typeRepository.addMetaObject(extendedType);

			}
		});
		return setup;
	}

	private BinaryData fileBinaryData(File file, String content) throws IOException {
		FileUtilities.writeStringToFile(content, file, "UTF8");
		return BinaryDataFactory.createBinaryDataWithContentType(file, BinaryData.CONTENT_TYPE_OCTET_STREAM);
	}

	private BinaryContent inMemoryData(String randomString) throws IOException {
		InMemoryBinaryData result = new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM);
		result.write(randomString.getBytes("UTF8"));
		return result;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tempDir = Files.createTempDirectory(TestBinaryDataAttribute.class.getSimpleName()).toFile();
		_formerTempDir = ReflectionUtils.setValue(Settings.getInstance(), "_tempDir", _tempDir);
	}

	@Override
	protected void tearDown() throws Exception {
		ReflectionUtils.setValue(Settings.getInstance(), "_tempDir", _formerTempDir);
		FileUtilities.deleteR(_tempDir);
		super.tearDown();
	}

	public void testCleanTempFolder() throws IOException {
		testCleanRefetchAfterCleanup(TYPE);
	}

	public void testRefetchFromCorrectTable() throws IOException {
		// attribute is defined at some super class.
		testCleanRefetchAfterCleanup(TYPE_EXTENSION);
	}

	private void testCleanRefetchAfterCleanup(String typeWithBlob) throws IOException {
		String content = randomString(5000, true, true, true, true);

		File tmpFile = File.createTempFile("file1", ".dat", Settings.getInstance().getTempDir());
		BinaryData fileData = fileBinaryData(tmpFile, content);
		BinaryContent inMemoryData = inMemoryData(content);

		Transaction tx = begin();
		KnowledgeObject a = newA(typeWithBlob, "a1");
		a.setAttributeValue(BINARY_ROW_ATTR, fileData);
		a.setAttributeValue(BINARY_FLEX_ATTR, fileData);

		assertSameContent(inMemoryData, (BinaryContent) a.getAttributeValue(BINARY_FLEX_ATTR));
		assertSameContent(inMemoryData, (BinaryContent) a.getAttributeValue(BINARY_ROW_ATTR));
		tx.commit();

		assertTrue(tmpFile.delete());

		assertSameContent(inMemoryData, (BinaryContent) a.getAttributeValue(BINARY_FLEX_ATTR));
		assertSameContent(inMemoryData, (BinaryContent) a.getAttributeValue(BINARY_ROW_ATTR));


		FileUtilities.deleteContents(_tempDir);

		assertSameContent(inMemoryData, (BinaryContent) a.getAttributeValue(BINARY_FLEX_ATTR));
		assertSameContent(inMemoryData, (BinaryContent) a.getAttributeValue(BINARY_ROW_ATTR));
	}

	private static void assertSameContent(BinaryContent expected, BinaryContent actual) throws IOException {
		try (InputStream in1 = expected.getStream(); InputStream in2 = actual.getStream()) {
			assertEquals(StreamUtilities.readStreamContents(in1), StreamUtilities.readStreamContents(in2));
		}
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestBinaryDataAttribute}.
	 */
	public static Test suite() {
		return suite(TestBinaryDataAttribute.class);
	}

}
