/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.operations.I18NConstants;
import com.top_logic.model.search.expr.config.operations.WebResourceFunctions;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for the TL-Script function <code>webResource()</code>.
 *
 * @see WebResourceFunctions
 */
@SuppressWarnings("javadoc")
public class TestWebResourceFunctions extends AbstractSearchExpressionTest {

	/**
	 * A resource of the test web application.
	 */
	private static final String SAMPLE = "/webResource/sample.svg";

	private static final String SAMPLE_CONTENTS =
		"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"/>\n";

	private Person _user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("webResourceUser");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user.tDelete();
			tx.commit();
		}
		_user = null;
		super.tearDown();
	}

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	public void testReadResource() throws Exception {
		Object result = eval("webResource('" + SAMPLE + "')");

		assertTrue("Not a binary value: " + result, result instanceof BinaryDataSource);
		BinaryDataSource data = (BinaryDataSource) result;
		assertEquals("sample.svg", data.getName());
		assertEquals("image/svg+xml", data.getContentType());
		assertEquals(SAMPLE_CONTENTS, eval("webResource('" + SAMPLE + "').binaryText()"));
		assertEquals("image/svg+xml", eval("webResource('" + SAMPLE + "').binaryContentType()"));
	}

	public void testUnknownContentType() throws Exception {
		BinaryDataSource data = (BinaryDataSource) eval("webResource('/webResource/sample.tlunknown')");

		assertEquals("sample.tlunknown", data.getName());
		assertEquals(BinaryDataSource.CONTENT_TYPE_OCTET_STREAM, data.getContentType());
	}

	public void testMissingResource() throws Exception {
		assertNull(eval("webResource('/webResource/no-such-resource.svg')"));
		assertNull(eval("webResource(null)"));
	}

	public void testRejectedPaths() throws Exception {
		assertRejected("webResource/sample.svg");
		assertRejected("/webResource/../webResource/sample.svg");
		assertRejected("/..");
		assertRejected("file:///etc/passwd");
		assertRejected("/webResource\\sample.svg");
	}

	public void testRefusedInteractivelyForNonAdmin() throws Exception {
		becomeUser(_user);
		try {
			evalInteractive("webResource('" + SAMPLE + "')");
			fail("A user who is not an administrator must not read a web resource interactively.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.model.search.expr.I18NConstants.PERMISSION_DENIED__NAME,
				ex.getErrorKey().plain());
		}
	}

	public void testAllowedInteractivelyForAdmin() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(SAMPLE_CONTENTS, evalInteractive("webResource('" + SAMPLE + "').binaryText()"));
	}

	public void testAllowedNonInteractivelyForNonAdmin() throws Exception {
		becomeUser(_user);
		assertEquals(SAMPLE_CONTENTS, eval("webResource('" + SAMPLE + "').binaryText()"));
	}

	private void assertRejected(String path) throws Exception {
		try {
			Object result = eval("path -> webResource($path)", path);
			fail("Path must be rejected: " + path + ", got: " + result);
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_WEB_RESOURCE_PATH__PATH, ex.getErrorKey().plain());
		}
	}

	private Object evalInteractive(String script, Object... args) throws Exception {
		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(script));
		return executor.executeWith(executor.context(true, null, null), Args.some(args));
	}

	public static Test suite() {
		return suite(TestWebResourceFunctions.class,
			MimeTypesModule.Module.INSTANCE,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
