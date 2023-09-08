/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.formats;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.LongID;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.meta.DefaultMORepository;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.migration.formats.ValueDumper;
import com.top_logic.knowledge.service.db2.migration.formats.ValueParser;
import com.top_logic.knowledge.service.db2.migration.formats.ValueType;

/**
 * Test for {@link ValueDumper} and {@link ValueParser}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestValueDumpParse extends BasicTestCase {

	private StringWriter _out;

	private ValueDumper _dumper;

	DefaultMORepository _repository;

	private ValueParser<RuntimeException> _valueParser;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_out = new StringWriter();
		_dumper = new ValueDumper(_out);
		_repository = new DefaultMORepository(true);
		_valueParser = new ValueParser<>() {

			@Override
			protected MetaObject resolve(String typeName) throws RuntimeException {
				try {
					return _repository.getMetaObject(typeName);
				} catch (UnknownTypeException ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			protected RuntimeException parseError(String message, Throwable cause) throws RuntimeException {
				throw new RuntimeException(message, cause);
			}

		};
	}

	public void testBoolean() throws IOException {
		testDumpParse(Boolean.TRUE);
		testDumpParse(Boolean.FALSE);
	}

	public void testInt() throws IOException {
		testDumpParse(Integer.MAX_VALUE);
		testDumpParse(Integer.MIN_VALUE);
		testDumpParse(Integer.valueOf(0));
		testDumpParse(Integer.valueOf(156));
	}

	public void testLong() throws IOException {
		testDumpParse(Long.MAX_VALUE);
		testDumpParse(Long.MIN_VALUE);
		testDumpParse(Long.valueOf(10));
		testDumpParse(Long.valueOf(156));
	}

	public void testDate() throws IOException {
		testDumpParse(new Date());
		testDumpParse(new Date(0));
		testDumpParse(new Date(Long.MAX_VALUE));
		testDumpParse(new Date(-1));
	}

	public void testByte() throws IOException {
		testDumpParse(Byte.MAX_VALUE);
		testDumpParse(Byte.MIN_VALUE);
		testDumpParse(Byte.valueOf((byte) 0));
		testDumpParse(Byte.valueOf((byte) 15));
	}

	public void testShort() throws IOException {
		testDumpParse(Short.MAX_VALUE);
		testDumpParse(Short.MIN_VALUE);
		testDumpParse(Short.valueOf((short) 0));
		testDumpParse(Short.valueOf((short) 2536));
	}

	public void testCharacter() throws IOException {
		testDumpParse(Character.MAX_VALUE);
		testDumpParse(Character.MIN_VALUE);
		testDumpParse(Character.valueOf((char) 0));
		testDumpParse(Character.valueOf((char) 2536));
	}

	public void testFloat() throws IOException {
		testDumpParse(Float.MAX_VALUE);
		testDumpParse(Float.MIN_VALUE);
		testDumpParse(Float.valueOf(0.0F));
		testDumpParse(Float.valueOf(12.154F));
	}

	public void testDouble() throws IOException {
		testDumpParse(Double.MAX_VALUE);
		testDumpParse(Double.MIN_VALUE);
		testDumpParse(Double.valueOf(0.0D));
		testDumpParse(Double.valueOf(12.154D));
	}

	public void testString() throws IOException {
		testDumpParse(StringServices.EMPTY_STRING);
		testDumpParse(BasicTestCase.randomString(568, true, true, true, true));
	}

	public void testRef() throws IOException, DuplicateTypeException {
		MetaObject type = new DeferredMetaObject("type");
		_repository.addMetaObject(type);
		testDumpParse(new DefaultObjectKey(1, Revision.CURRENT_REV, type, LongID.valueOf(1265)));
		testDumpParse(new DefaultObjectKey(1, 15, type, StringID.createRandomID()));
	}

	public interface TestValue extends NamedConfiguration {
		// Pure marker.
	}

	public void testConfig() throws IOException {
		TestValue item = TypedConfiguration.newConfigItem(TestValue.class);
		item.setName("myName");

		_out.getBuffer().setLength(0);
		ValueType valueType = ValueDumper.type(item);
		_dumper.writeValue(valueType, item);
		ConfigurationItem object = (ConfigurationItem) _valueParser.parseValue(valueType, _out.getBuffer().toString());
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(item, object));
	}

	public void testNull() throws IOException {
		testDumpParse(null);
	}

	public void testEnum() throws IOException {
		testDumpParse(TimeUnit.HOURS);
		testDumpParse(TimeUnit.MICROSECONDS);
	}

	public void testTLID() throws IOException {
		testDumpParse(LongID.valueOf(156));
		testDumpParse(StringID.createRandomID());
	}

	public void testBinaryData() throws IOException {
		byte[] data = new byte[156];
		Random r = new Random(1265);
		new Random(r.nextLong()).nextBytes(data);
		testDumpParse(BinaryDataFactory.createBinaryData(data));

		new Random(r.nextLong()).nextBytes(data);
		testDumpParse(BinaryDataFactory.createBinaryData(data, "plain/text"));

		new Random(r.nextLong()).nextBytes(data);
		testDumpParse(BinaryDataFactory.createBinaryData(data, "plain/text", "myFile.txt"));

		// Implementation detail: ':' is used as separator between content type and content. Check
		// that it is correctly escaped
		new Random(r.nextLong()).nextBytes(data);
		char separator = (char) ReflectionUtils.getStaticValue(ValueParser.class, "BINARY_DATA_TYPE_SEPARATOR");
		testDumpParse(BinaryDataFactory.createBinaryData(data, "plain" + separator + "text"));
	}

	private void testDumpParse(Object value) throws IOException {
		_out.getBuffer().setLength(0);
		ValueType valueType = ValueDumper.type(value);
		_dumper.writeValue(valueType, value);
		Object object = _valueParser.parseValue(valueType, _out.getBuffer().toString());

		if (value instanceof BinaryData) {
			assertEqualsContent((BinaryData) value, (BinaryData) object);
		} else {
			assertEquals(value, object);
		}
	}

	private void assertEqualsContent(BinaryData expected, BinaryData actual) throws IOException {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getSize(), actual.getSize());
		assertEquals(expected.getContentType(), actual.getContentType());
		assertEquals(
			StreamUtilities.readStreamContents(expected.getStream()),
			StreamUtilities.readStreamContents(actual.getStream()));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestValueDumpParse}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestValueDumpParse.class);
	}

}
