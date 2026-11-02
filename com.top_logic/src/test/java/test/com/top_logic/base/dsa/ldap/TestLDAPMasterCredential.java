/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.dsa.ldap;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.spi.InitialContextFactory;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.dsa.ldap.LDAPAccessService;
import com.top_logic.base.dsa.ldap.ServiceProviderInfo;
import com.top_logic.base.security.util.CryptSupport;
import com.top_logic.base.security.util.CryptSupportSecretSource;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretStoreService;

/**
 * Tests the master credential of a {@link LDAPAccessService} given by a secret source: the
 * attribute forms encoded by {@link CryptSupport} or in plain text, the reference to an
 * environment variable read for each connection, and the written form of the configuration.
 *
 * <p>
 * The connections are opened by the {@link RecordingContextFactory}, which records the
 * credentials of each connection instead of contacting a directory server.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestLDAPMasterCredential extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "access-service";

	/** The system property holding the password in the tests referencing a variable. */
	private static final String VARIABLE = "test_ldap_master_credential";

	private static final String PASSWORD = "first-password";

	private static final String ROTATED_PASSWORD = "second-password";

	/** Credentials of the connections opened by the {@link RecordingContextFactory}. */
	static final List<Object> CREDENTIALS = new CopyOnWriteArrayList<>();

	/**
	 * {@link InitialContextFactory} recording the credentials of each context it creates.
	 */
	public static class RecordingContextFactory implements InitialContextFactory {

		@Override
		public Context getInitialContext(Hashtable<?, ?> environment) {
			CREDENTIALS.add(environment.get(Context.SECURITY_CREDENTIALS));
			return (Context) Proxy.newProxyInstance(RecordingContextFactory.class.getClassLoader(),
				new Class<?>[] { DirContext.class }, (proxy, method, args) -> null);
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CREDENTIALS.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		CREDENTIALS.clear();
		super.tearDown();
	}

	/** A credential encoded by {@link CryptSupport} keeps working. */
	public void testCryptSupportAttribute() throws Exception {
		String encoded = CryptSupport.getInstance().encodeString(PASSWORD);
		assertEquals(List.of(PASSWORD), credentials(attribute(encoded), ""));
	}

	/** A credential in plain text keeps working. */
	public void testPlainAttribute() throws Exception {
		assertEquals(List.of(PASSWORD), credentials(attribute(PASSWORD), ""));
	}

	/** Without credential, the connection is opened with an empty credential, as before. */
	public void testNoCredential() throws Exception {
		assertEquals(List.of(""), credentials("", ""));
	}

	/** A literal secret source given as element is decoded like an encrypted value. */
	public void testLiteralElement() throws Exception {
		String content = "<" + ServiceProviderInfo.Config.MASTER_CREDENTIAL
			+ " class='" + LiteralSecretSource.class.getName() + "'"
			+ " " + LiteralSecretSource.Config.VALUE + "='unencrypted:" + PASSWORD + "'/>";
		assertEquals(List.of(PASSWORD), credentials("", content));
	}

	/** The credential from a variable is read for each connection. */
	public void testEnvironmentRotation() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		LDAPAccessService service = service(xml("", environmentCredential()));
		connect(service);
		System.setProperty(VARIABLE, ROTATED_PASSWORD);
		connect(service);

		assertEquals(List.of(PASSWORD, ROTATED_PASSWORD), CREDENTIALS);
	}

	/** The connection parameters handed out by the service contain no credential. */
	public void testNoCredentialInParameters() throws Exception {
		LDAPAccessService service = service(xml(attribute(PASSWORD), ""));
		assertFalse(service.getCopyOfSPI().containsKey(ServiceProviderInfo.KEY_CREDENTIALS));
	}

	/** A reference to a named secret is written as element without class holding only the name. */
	public void testWriteEnvironmentCredential() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		LDAPAccessService.Config config = read(xml("", environmentCredential()));

		String written = toXML(config);
		assertFalse("Credential written: " + written, written.contains(PASSWORD));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + ServiceProviderInfo.Config.MASTER_CREDENTIAL + " name=\"" + VARIABLE + "\""));

		LDAPAccessService.Config copy = (LDAPAccessService.Config) fromXML(written);
		assertTrue(copy.getMasterCredential() instanceof NamedSecretSource.Config);
		assertEquals(written, toXML(copy));
	}

	/** A credential encoded by {@link CryptSupport} is written as given. */
	public void testWriteCryptSupportCredential() throws Exception {
		String encoded = CryptSupport.getInstance().encodeString(PASSWORD);
		LDAPAccessService.Config config = read(xml(attribute(encoded), ""));
		assertTrue(config.getMasterCredential() instanceof CryptSupportSecretSource.Config);

		String written = toXML(config);
		assertFalse("Credential written in plain text: " + written, written.contains(PASSWORD));
		assertTrue("Not written as attribute: " + written,
			written.contains(ServiceProviderInfo.Config.MASTER_CREDENTIAL + "=\"" + encoded + "\""));

		LDAPAccessService.Config copy = (LDAPAccessService.Config) fromXML(written);
		assertEquals(config, copy);
	}

	private List<Object> credentials(String attributes, String content) throws Exception {
		connect(service(xml(attributes, content)));
		return List.copyOf(CREDENTIALS);
	}

	private LDAPAccessService service(String xml) throws Exception {
		LDAPAccessService.Config config = read(xml);
		return new LDAPAccessService("test", config, null);
	}

	private static void connect(LDAPAccessService service) throws Exception {
		service.releaseContext(service.getContext(), true);
	}

	private static String attribute(String value) {
		return ServiceProviderInfo.Config.MASTER_CREDENTIAL + "='" + value + "'";
	}

	private static String xml(String attributes, String content) {
		return "<" + ROOT_TAG
			+ " context-factory='" + RecordingContextFactory.class.getName() + "'"
			+ " provider-url='ldap://localhost:389'"
			+ " base-dn='DC=test'"
			+ " master-principal='master@test' "
			+ attributes + ">" + content + "</" + ROOT_TAG + ">";
	}

	private static String environmentCredential() {
		return "<" + ServiceProviderInfo.Config.MASTER_CREDENTIAL + " name='" + VARIABLE + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(LDAPAccessService.Config.class));
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestLDAPMasterCredential.class, CryptSupport.Module.INSTANCE,
				SecretStoreService.Module.INSTANCE));
	}

}
