/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary.scan;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataPart;
import com.top_logic.basic.io.binary.scan.I18NConstants;
import com.top_logic.basic.io.binary.scan.UploadContentChecker;
import com.top_logic.basic.io.binary.scan.UploadGuardRequest;
import com.top_logic.basic.io.binary.scan.UploadRejectedException;
import com.top_logic.basic.io.binary.scan.UploadSecurityService;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;

/**
 * Test for the {@link UploadGuardRequest} with a {@link UploadContentChecker} rejecting content by
 * a marker in its bytes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestUploadGuard extends BasicTestCase {

	/** Name of the form field holding an uploaded file. */
	private static final String UPLOAD_FIELD = "upload";

	/** Name of a plain form field transmitted along with the upload. */
	private static final String PLAIN_FIELD = "controlId";

	/** Name of the property {@link UploadSecurityService.Config#getCheckers()}. */
	private static final String CHECKERS_PROPERTY = "checkers";

	/** Content type of an uploaded text file. */
	private static final String TEXT_CONTENT_TYPE = "text/plain";

	/** Content type of a request transmitting uploaded files. */
	private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary=42";

	/** Content type of a request transmitting only plain form fields. */
	private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

	/** Marker in the content that makes the {@link MarkerChecker} reject an upload. */
	private static final String INFECTED_MARKER = "infected";

	/** Signature reported by the {@link MarkerChecker} for rejected content. */
	private static final String SIGNATURE = "Test-Signature";

	private static final byte[] CLEAN_CONTENT = "harmless content".getBytes(StandardCharsets.UTF_8);

	private static final byte[] INFECTED_CONTENT =
		("content with the " + INFECTED_MARKER + " marker").getBytes(StandardCharsets.UTF_8);

	private static final MarkerChecker CHECKER = new MarkerChecker();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CHECKER.reset();
	}

	/**
	 * A clean upload is handed on as re-readable {@link BinaryDataPart}s, the plain form field is
	 * passed on unchecked.
	 */
	public void testCleanUpload() throws IOException, ServletException {
		MemoryPart field = new MemoryPart(PLAIN_FIELD, null, null, "c1".getBytes(StandardCharsets.UTF_8));
		MemoryPart upload = new MemoryPart(UPLOAD_FIELD, "clean.txt", TEXT_CONTENT_TYPE, CLEAN_CONTENT);

		HttpServletRequest request = UploadGuardRequest.guard(multipartRequest(field, upload));
		assertTrue("A multipart request must be guarded.", request instanceof UploadGuardRequest);

		List<Part> parts = new ArrayList<>(request.getParts());
		assertEquals(2, parts.size());
		assertEquals("The order of the parts must be preserved.", PLAIN_FIELD, parts.get(0).getName());
		assertEquals("The order of the parts must be preserved.", UPLOAD_FIELD, parts.get(1).getName());

		assertEquals("Only the uploaded file must be checked.",
			Collections.singletonList("clean.txt"), CHECKER.getCheckedNames());
		assertEquals("Each part must be read exactly once.", 1, field.getWriteCount());
		assertEquals("Each part must be read exactly once.", 1, upload.getWriteCount());

		Part uploaded = request.getPart(UPLOAD_FIELD);
		assertTrue("An uploaded part must be re-readable.", uploaded instanceof BinaryDataPart);
		assertEquals("clean.txt", uploaded.getSubmittedFileName());
		assertEquals(TEXT_CONTENT_TYPE, uploaded.getContentType());
		assertEquals(CLEAN_CONTENT.length, uploaded.getSize());
		assertTrue(Arrays.equals(CLEAN_CONTENT, readContent(uploaded)));
		assertTrue("The content must be readable more than once.",
			Arrays.equals(CLEAN_CONTENT, readContent(uploaded)));

		Part plain = request.getPart(PLAIN_FIELD);
		assertTrue(Arrays.equals("c1".getBytes(StandardCharsets.UTF_8), readContent(plain)));
		assertNull("A part that is picked by an unused name has no value.", request.getPart("unused"));
	}

	/**
	 * A rejected upload fails with the message of the rejecting checker, for every access to the
	 * parts.
	 */
	public void testInfectedUpload() throws IOException, ServletException {
		MemoryPart field = new MemoryPart(PLAIN_FIELD, null, null, "c1".getBytes(StandardCharsets.UTF_8));
		MemoryPart upload = new MemoryPart(UPLOAD_FIELD, "virus.txt", TEXT_CONTENT_TYPE, INFECTED_CONTENT);

		HttpServletRequest request = UploadGuardRequest.guard(multipartRequest(field, upload));

		UploadRejectedException rejection = null;
		try {
			request.getParts();
			fail("Infected content must be rejected.");
		} catch (UploadRejectedException ex) {
			rejection = ex;
		}
		assertSame("The message of the rejecting checker must be reported.",
			CHECKER.getLastRejection(), rejection.getErrorKey());

		try {
			request.getPart(UPLOAD_FIELD);
			fail("The rejection must be reported to every consumer of the request.");
		} catch (UploadRejectedException ex) {
			assertSame(rejection, ex);
		}
		assertEquals("The content must not be checked again.", 1, CHECKER.getCheckedNames().size());
	}

	/**
	 * A request that transmits no uploads is not guarded.
	 */
	public void testNoMultipartRequestNotGuarded() {
		HttpServletRequest formRequest = request(FORM_CONTENT_TYPE);
		assertSame(formRequest, UploadGuardRequest.guard(formRequest));

		HttpServletRequest withoutContentType = request(null);
		assertSame(withoutContentType, UploadGuardRequest.guard(withoutContentType));
	}

	/**
	 * A request is not guarded while the {@link UploadSecurityService} is inactive.
	 */
	public void testInactiveServiceNotGuarded() throws Exception {
		ModuleUtil.INSTANCE.shutDown(UploadSecurityService.Module.INSTANCE);
		try {
			HttpServletRequest request = multipartRequest();
			assertSame(request, UploadGuardRequest.guard(request));
		} finally {
			ModuleUtil.INSTANCE.startUp(UploadSecurityService.Module.INSTANCE);
		}
	}

	/**
	 * A checked part delivers its content without copying it again.
	 */
	public void testUploadDataOfCheckedPart() throws IOException, ServletException {
		MemoryPart upload = new MemoryPart(UPLOAD_FIELD, "clean.txt", TEXT_CONTENT_TYPE, CLEAN_CONTENT);

		HttpServletRequest request = UploadGuardRequest.guard(multipartRequest(upload));
		BinaryDataPart part = (BinaryDataPart) request.getPart(UPLOAD_FIELD);

		assertSame(part.getData(), BinaryDataFactory.createUploadData(part));
		assertEquals("The content must not be copied again.", 1, upload.getWriteCount());
	}

	private static byte[] readContent(Part part) throws IOException {
		try (InputStream in = part.getInputStream()) {
			return in.readAllBytes();
		}
	}

	private static HttpServletRequest multipartRequest(Part... parts) {
		return request(MULTIPART_CONTENT_TYPE, parts);
	}

	private static HttpServletRequest request(String contentType, Part... parts) {
		InvocationHandler handler = new RequestStub(contentType, Arrays.asList(parts));
		return (HttpServletRequest) Proxy.newProxyInstance(TestUploadGuard.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, handler);
	}

	/**
	 * {@link HttpServletRequest} answering the content type and the parts the test has set up.
	 */
	private static final class RequestStub implements InvocationHandler {

		private final String _contentType;

		private final List<Part> _parts;

		RequestStub(String contentType, List<Part> parts) {
			_contentType = contentType;
			_parts = parts;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
				case "getContentType":
					return _contentType;
				case "getParts":
					return _parts;
				case "getPart":
					return part((String) args[0]);
				case "toString":
					return "request stub";
				case "hashCode":
					return System.identityHashCode(proxy);
				case "equals":
					return proxy == args[0];
				default:
					throw new UnsupportedOperationException(method.getName());
			}
		}

		private Part part(String name) {
			for (Part part : _parts) {
				if (part.getName().equals(name)) {
					return part;
				}
			}
			return null;
		}

	}

	/**
	 * {@link Part} delivering a fixed content, which can be consumed only once, as the servlet API
	 * allows.
	 */
	private static final class MemoryPart implements Part {

		private final String _name;

		private final String _fileName;

		private final String _contentType;

		private final byte[] _content;

		private int _writeCount;

		MemoryPart(String name, String fileName, String contentType, byte[] content) {
			_name = name;
			_fileName = fileName;
			_contentType = contentType;
			_content = content;
		}

		/**
		 * How often the content of this part was consumed.
		 */
		int getWriteCount() {
			return _writeCount;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(_content);
		}

		@Override
		public String getContentType() {
			return _contentType;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getSubmittedFileName() {
			return _fileName;
		}

		@Override
		public long getSize() {
			return _content.length;
		}

		@Override
		public void write(String fileName) throws IOException {
			_writeCount++;
			try (OutputStream out = new FileOutputStream(fileName)) {
				out.write(_content);
			}
		}

		@Override
		public void delete() throws IOException {
			// Nothing to clean up.
		}

		@Override
		public String getHeader(String name) {
			return null;
		}

		@Override
		public Collection<String> getHeaders(String name) {
			return Collections.emptyList();
		}

		@Override
		public Collection<String> getHeaderNames() {
			return Collections.emptyList();
		}

	}

	/**
	 * {@link UploadContentChecker} rejecting content that contains the {@link #INFECTED_MARKER}.
	 */
	private static final class MarkerChecker implements UploadContentChecker {

		private final List<String> _checkedNames = new ArrayList<>();

		private ResKey _lastRejection;

		@Override
		public ResKey check(BinaryData data) {
			_checkedNames.add(data.getName());
			if (isInfected(data)) {
				_lastRejection = I18NConstants.ERROR_UPLOAD_INFECTED__NAME_SIGNATURE.fill(data.getName(), SIGNATURE);
				return _lastRejection;
			}
			return null;
		}

		private boolean isInfected(BinaryData data) {
			try (InputStream in = data.getStream()) {
				return new String(in.readAllBytes(), StandardCharsets.UTF_8).contains(INFECTED_MARKER);
			} catch (IOException ex) {
				throw new RuntimeException("Cannot read the content to check.", ex);
			}
		}

		/**
		 * The names of the contents this checker has seen, in order.
		 */
		List<String> getCheckedNames() {
			return _checkedNames;
		}

		/**
		 * The message reported for the content rejected last.
		 */
		ResKey getLastRejection() {
			return _lastRejection;
		}

		/**
		 * Forgets what this checker has seen.
		 */
		void reset() {
			_checkedNames.clear();
			_lastRejection = null;
		}

	}

	/**
	 * Setup installing the {@link MarkerChecker} on the {@link UploadSecurityService} before it is
	 * started.
	 */
	private static final class CheckerSetup extends TestSetup {

		private Object _before;

		CheckerSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			UploadSecurityService.Config config = serviceConfig();
			PropertyDescriptor property = checkersProperty(config);
			_before = config.value(property);
			config.update(property, Collections.<UploadContentChecker> singletonList(CHECKER));
		}

		@Override
		protected void tearDown() throws Exception {
			UploadSecurityService.Config config = serviceConfig();
			config.update(checkersProperty(config), _before);
		}

		private UploadSecurityService.Config serviceConfig() throws Exception {
			return (UploadSecurityService.Config) ApplicationConfig.getInstance()
				.getServiceConfiguration(UploadSecurityService.class);
		}

		private PropertyDescriptor checkersProperty(UploadSecurityService.Config config) {
			return config.descriptor().getProperty(CHECKERS_PROPERTY);
		}

	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestUploadGuard.class);
		test = ServiceTestSetup.createSetup(test, Settings.Module.INSTANCE, UploadSecurityService.Module.INSTANCE);
		return ModuleTestSetup.setupModule(new CheckerSetup(test));
	}

}
