/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.pbe;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.Computation;
import com.top_logic.util.TLContext;

/**
 * {@link EncryptionService} that manages a password protected symmetric key.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(ConnectionPoolRegistry.Module.class)
public class PasswordBasedEncryptionService extends EncryptionService {

	/**
	 * Storage key for the PBE encrypted encryption key.
	 */
	private static final String ENCRYPTION_KEY_DBPROPERTY = "encryption-key";

	/**
	 * Option to configure the password based encryption (PBE) algorithm to use.
	 */
	public static final String PBE_ALGORITHM_PROPERTY = "pbeAlgorithm";

	/**
	 * Option to configure the number of salt bytes used in addition to the password for encryption.
	 */
	public static final String SALT_LENGTH_PROPERTY = "saltLength";

	/**
	 * Option to configure the the number of iterations that are required to create the key from the
	 * password.
	 * 
	 * <p>
	 * Higher values make brute-force attacks harder.
	 * </p>
	 */
	public static final String ITERATIONS_PROPERTY = "iterations";

	/**
	 * Option to configure the encryption algorithm to use.
	 */
	public static final String ENCRYPTION_ALGORITHM_PROPERTY = "encryptionAlgorithm";

	/**
	 * @see PasswordBasedEncryptionService#PBE_ALGORITHM_PROPERTY
	 */
	private final String _pbeAlgorithm;

	/**
	 * @see PasswordBasedEncryptionService#ITERATIONS_PROPERTY
	 */
	private final int _iterations;

	/**
	 * @see PasswordBasedEncryptionService#SALT_LENGTH_PROPERTY
	 */
	private final int _saltLength;

	private final String _encryptionAlgorithm;

	private final DBProperties _dbProperties;

	/**
	 * Configuration for {@link PasswordBasedEncryptionService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends EncryptionService.Config {
		/**
		 * The password based encryption algorithm for encrypting the encryption key.
		 */
		String getPbeAlgorithm();

		/**
		 * Salt length the algorithm uses.
		 */
		int getSaltLength();

		/**
		 * Iterations the algorithm uses.
		 */
		int getIterations();

		/**
		 * The algorithm for data encryption.
		 */
		String getEncryptionAlgorithm();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link PasswordBasedEncryptionService}.
	 */
	public PasswordBasedEncryptionService(InstantiationContext context, Config config) {
		super(context, config);

		_pbeAlgorithm = config.getPbeAlgorithm();
		_saltLength = config.getSaltLength();
		_iterations = config.getIterations();
		_encryptionAlgorithm = config.getEncryptionAlgorithm();

		_dbProperties = new DBProperties(ConnectionPoolRegistry.getDefaultConnectionPool());
	}

	/**
	 * Installs the initial application password.
	 * 
	 * @param connection
	 *        The transaction to use.
	 * @param password
	 *        The password to encrypt the encryption key with.
	 * 
	 * @throws ModuleException
	 *         If the application is not in the startup phase.
	 * @throws InvalidPasswordException
	 *         If the encryption algorithm cannot deal with the given password.
	 */
	public static void setupPassword(PooledConnection connection, final String password) throws ModuleException,
			InvalidPasswordException, SQLException {

		Module module = Module.INSTANCE;
		if (module.isActive()) {
			throw new ModuleException("Service already started.", module.getImplementation());
		}
	
		ModuleUtil.INSTANCE.startUp(module);
		try {
			final PasswordBasedEncryptionService service =
				(PasswordBasedEncryptionService) module.getImplementationInstance();
	
			service.internalSetupPassword(connection, password);
		} finally {
			ModuleUtil.INSTANCE.shutDown(module);
		}
	}

	void internalSetupPassword(PooledConnection connection, String password) throws InvalidPasswordException, SQLException {
		if (hasEncryptionKey()) {
			throw new IllegalStateException("Key was already set up.");
		}
	
		storeEncryptionKey(connection, pbe(password.toCharArray()), createEncryptionKey());
	}

	/**
	 * Changes the application password.
	 * 
	 * @param connection
	 *        The transaction to use.
	 * @param oldPassword
	 *        The old password.
	 * @param newPassword
	 *        The new password.
	 * @throws InvalidPasswordException
	 *         If the old password does not match.
	 * @throws ModuleException
	 *         If the application is not in the startup phase.
	 */
	public static void changePassword(PooledConnection connection, final String oldPassword, final String newPassword)
			throws ModuleException, InvalidPasswordException, SQLException {

		Module module = Module.INSTANCE;
		if (module.isActive()) {
			throw new ModuleException("Service already started.", module.getImplementation());
		}

		ModuleUtil.INSTANCE.startUp(module);
		try {
			final PasswordBasedEncryptionService service =
				(PasswordBasedEncryptionService) module.getImplementationInstance();

			service.internalChangePassword(connection, oldPassword, newPassword);
		} finally {
			ModuleUtil.INSTANCE.shutDown(module);
		}
	}

	void internalChangePassword(PooledConnection connection, String oldPassword, String newPassword)
			throws InvalidPasswordException, SQLException {
		if (!hasEncryptionKey()) {
			throw new IllegalStateException("No key was set up yet.");
		}
	
		PasswordBasedEncryption oldEncryption = pbe(oldPassword.toCharArray());
		SecretKey oldEncryptionKey = retrieveEncryptionKey(oldEncryption);
	
		// TODO: Create new key and re-encrypt all data.
		SecretKey newEncryptionKey = oldEncryptionKey;
	
		PasswordBasedEncryption newEncryption = pbe(newPassword.toCharArray());
		storeEncryptionKey(connection, newEncryption, newEncryptionKey);
	}

	/**
	 * Starts the {@link PasswordBasedEncryptionService}.
	 * 
	 * @param password
	 *        The application password.
	 */
	public static void startUp(final char[] password) throws ModuleException, InvalidPasswordException {
		Module module = Module.INSTANCE;
		if (module.isActive()) {
			throw new ModuleException("Service already started.", module.getImplementation());
		}

		boolean success = false;
		ModuleUtil.INSTANCE.startUp(module);
		try {
			PasswordBasedEncryptionService service =
				(PasswordBasedEncryptionService) module.getImplementationInstance();

			usePassword(service, password);

			success = true;
		} finally {
			if (!success) {
				ModuleUtil.INSTANCE.shutDown(module);
			}
		}
	}

	/**
	 * Enters the given password in the given service instance.
	 */
	public static void usePassword(final PasswordBasedEncryptionService service, final char[] password)
			throws InvalidPasswordException {
		InvalidPasswordException problem = TLContext.inSystemContext(PasswordBasedEncryptionService.class,
			new Computation<InvalidPasswordException>() {
				@Override
				public InvalidPasswordException run() {
					try {
						service.internalStartUp(password);
						return null;
					} catch (InvalidPasswordException ex) {
						return ex;
					}
				}
		});
		if (problem != null) {
			throw problem;
		}
	}

	void internalStartUp(char[] password) throws InvalidPasswordException {
		installEncryptionKey(retrieveEncryptionKey(pbe(password)));
	}

	private PasswordBasedEncryption pbe(char[] password) throws InvalidPasswordException {
		PasswordBasedEncryption pbe =
			new PasswordBasedEncryption(getRandom(), _pbeAlgorithm, _saltLength, _iterations, password);
		return pbe;
	}

	private boolean hasEncryptionKey() {
		return _dbProperties.getProperty(ENCRYPTION_KEY_DBPROPERTY) != null;
	}

	private SecretKey retrieveEncryptionKey(PasswordBasedEncryption pbe) throws InvalidPasswordException {
		try {
			String encodedKey = _dbProperties.getProperty(ENCRYPTION_KEY_DBPROPERTY);
			assert encodedKey != null;

			byte[] encodedKeySpec = pbe.decryptEncoded(encodedKey);

			return new SecretKeySpec(encodedKeySpec, _encryptionAlgorithm);
		} catch (IllegalBlockSizeException ex) {
			throw errorInvalidPassword();
		} catch (BadPaddingException ex) {
			throw errorInvalidPassword();
		}
	}

	private SecretKey createEncryptionKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(_encryptionAlgorithm);
			keyGenerator.init(getRandom());

			return keyGenerator.generateKey();
		} catch (NoSuchAlgorithmException ex) {
			throw new UnsupportedOperationException("Unsupported encryption algorithm.", ex);
		}
	}

	private void storeEncryptionKey(PooledConnection connection, PasswordBasedEncryption pbe, SecretKey key)
			throws SQLException {
		byte[] encodedKey = key.getEncoded();
		String newEncodedKey = pbe.encryptEncoded(encodedKey);
		DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, ENCRYPTION_KEY_DBPROPERTY, newEncodedKey);
	}

	private static InvalidPasswordException errorInvalidPassword() {
		return new InvalidPasswordException();
	}

}
