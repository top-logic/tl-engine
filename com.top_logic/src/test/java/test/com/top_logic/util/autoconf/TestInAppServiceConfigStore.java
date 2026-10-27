/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.autoconf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.EncodingConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Test for {@link InAppServiceConfigStore}.
 */
public class TestInAppServiceConfigStore extends TestCase {

	/** Alias referenced by the stored configuration. */
	private static final String SECRET_ALIAS = "%TEST_STORE_SECRET%";

	/** The secret {@link #SECRET_ALIAS} stands for. */
	private static final String SECRET = "store-secret";

	private File _file;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		AliasManager.getInstance().setBaseAliases(
			Collections.singletonMap(SECRET_ALIAS, EncodingConfigurationValueProvider.UNENCRYPTED_PREFIX + SECRET));
		_file = BasicTestCase.createNamedTestFile(getName() + InAppServiceConfigStore.FILE_SUFFIX);
	}

	@Override
	protected void tearDown() throws Exception {
		AliasManager.getInstance().setBaseAliases(Collections.emptyMap());
		_file.delete();
		super.tearDown();
	}

	/**
	 * A stored file that references an alias in an {@link Encrypted} property is read with the
	 * alias resolved, and the read configuration can be stored and read again.
	 */
	public void testAliasInEncryptedProperty() throws IOException, ConfigurationException {
		FileUtilities.writeStringToFile(storedFile(SECRET_ALIAS), _file, StringServices.UTF8);

		ApplicationConfig.Config config = InAppServiceConfigStore.read(_file);
		assertEquals(SECRET, secret(config));

		InAppServiceConfigStore.write(_file, config, InAppServiceConfigStore.OVERRIDE_SERVICE);
		assertEquals(SECRET, secret(InAppServiceConfigStore.read(_file)));
	}

	private static String storedFile(String secretValue) {
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
			+ "<application config:interface=\"" + ApplicationConfig.Config.class.getName() + "\"\n"
			+ "	xmlns:config=\"http://www.top-logic.com/ns/config/6.0\">\n"
			+ "	<services>\n"
			+ "		<config service-class=\"" + EncryptedService.class.getName() + "\">\n"
			+ "			<instance class=\"" + EncryptedService.class.getName() + "\" "
			+ EncryptedService.Config.SECRET + "=\"" + secretValue + "\"/>\n"
			+ "		</config>\n"
			+ "	</services>\n"
			+ "</application>\n";
	}

	private static String secret(ApplicationConfig.Config config) {
		Map<Class<? extends ManagedClass>, ?> services = config.getServices();
		assertTrue(services.containsKey(EncryptedService.class));
		EncryptedService.Config<?> instance =
			(EncryptedService.Config<?>) config.getServices().get(EncryptedService.class).getInstance();
		return instance.getSecret();
	}

	/**
	 * Service with an {@link Encrypted} configuration property.
	 */
	public static class EncryptedService extends ManagedClass {

		/**
		 * Configuration options for {@link EncryptedService}.
		 */
		public interface Config<I extends EncryptedService> extends ServiceConfiguration<I> {

			/**
			 * @see #getSecret()
			 */
			String SECRET = "secret";

			/**
			 * The secret.
			 */
			@Name(SECRET)
			@Encrypted
			String getSecret();

		}

		/**
		 * Creates a {@link EncryptedService} from configuration.
		 *
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		public EncryptedService(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

	}

	/**
	 * Suite of tests in this class.
	 */
	public static Test suite() {
		return TestUtils.doNotMerge(
			BasicTestSetup.createBasicTestSetup(new TestSuite(TestInAppServiceConfigStore.class)));
	}

}
