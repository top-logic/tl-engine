/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.pbe;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.Base32;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.DBOperation;
import com.top_logic.basic.version.Version;
import com.top_logic.util.BootFailure;
import com.top_logic.util.DeferredBootService;
import com.top_logic.util.DeferredBootUtil;
import com.top_logic.util.TLContext;

/**
 * Utility class to support JSPs that provide the application password during boot.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplicationPasswordUtil {

	private static final String PASSWORD_DIGEST = "SHA1";

	private static final int SALT_SIZE = 8;

	private static final int DIGEST_ITERATIONS = 10000;

	private static final String PASSWORD_ENCODING = "utf-8";

	private static final String APPLICATION_PASSWORD_PROPERTY = "application-password";

	/**
	 * Checks the application key that is necessary to initially install a password.
	 * 
	 * @param applicationKey
	 *        The application key to check.
	 * @return Whether the given key is accepted.
	 * 
	 * @see #setupPassword(PooledConnection, String)
	 */
	@CalledFromJSP
	public static boolean checkApplicationKey(String applicationKey) {
		String applicationName = Version.getApplicationName();
		return checkApplicationKey(applicationName, applicationKey);
	}

	/**
	 * Tests, whether the given application key fits the given application name.
	 */
	public static boolean checkApplicationKey(String applicationName, String applicationKey) {
		byte[] keyBytes;
		try {
			keyBytes = Base32.decodeBase32(applicationKey.replaceAll("[-\\s]", "").toCharArray());
		} catch (IllegalArgumentException ex) {
			return false;
		}

		try {
			return checkApplicationKey(applicationName, keyBytes);
		} catch (NoSuchAlgorithmException ex) {
			throw new UnreachableAssertion(ex);
		} catch (UnsupportedEncodingException ex) {
			throw new UnreachableAssertion(ex);
		} catch (DigestException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	private static boolean checkApplicationKey(String applicationName, byte[] keyBytes)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, DigestException {

		int challengeSize = 8;
		int checkSize = 8;

		if (keyBytes.length < challengeSize + checkSize) {
			return false;
		}

		MessageDigest digest = MessageDigest.getInstance("SHA1");

		byte[] secret = new byte[42];

		// IGNORE FindBugs(DMI_RANDOM_USED_ONLY_ONCE): This creates a stable application-built-in
		// "secret" key without putting the array value in "clear text" into the constant pool of
		// this class.
		new Random(983981274973927423L).nextBytes(secret);

		digest.update(secret);
		digest.update(applicationName.getBytes("utf-8"));
		digest.update(keyBytes, 0, challengeSize);

		int digestSize = digest.getDigestLength();
		byte[] intermediate = new byte[digestSize];
		for (int n = 0; n < 10000; n++) {
			digest.digest(intermediate, 0, digestSize);
			digest.update(intermediate);
		}

		byte[] signature = digest.digest();

		return Arrays.equals(
			ArrayUtil.copy(keyBytes, challengeSize, checkSize),
			ArrayUtil.copy(signature, 0, checkSize));
	}

	/**
	 * Whether a password has been set up.
	 * 
	 * @return <code>false</code>, if no initial password was yet set up.
	 * 
	 * @see #setupPassword(PooledConnection, String)
	 */
	@CalledFromJSP
	public static boolean hasPassword() {
		return getStoredSaltAndPasswordHash() != null;
	}

	/**
	 * Installs an initial application password.
	 * 
	 * @see PasswordBasedEncryptionService#setupPassword(PooledConnection, String)
	 */
	public static void setupPassword(PooledConnection connection, final String password) throws ModuleException,
			InvalidPasswordException, SQLException {
		if (hasPassword()) {
			throw new IllegalStateException("Application password already personalized.");
		}
		PasswordBasedEncryptionService.setupPassword(connection, password);
		storePasswordHash(connection, password);
	}

	static void storePasswordHash(PooledConnection connection, String password) throws SQLException {
		SecureRandom random = new SecureRandom();

		byte[] salt = new byte[SALT_SIZE];
		random.nextBytes(salt);

		byte[] hash = createHash(salt, password);

		String encodedSaltAndPasswordHash = new Base64().encodeToString(ArrayUtil.join(salt, hash));

		storeSaltAndPasswordHash(connection, encodedSaltAndPasswordHash);
	}

	private static void storeSaltAndPasswordHash(final PooledConnection connection,
			final String encodedSaltAndPasswordHash) throws SQLException {
		SQLException problem =
			TLContext.inSystemContext(ApplicationPasswordUtil.class, new Computation<SQLException>() {
				@Override
				public SQLException run() {
					try {
						DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
							APPLICATION_PASSWORD_PROPERTY,
							encodedSaltAndPasswordHash);
						return null;
					} catch (SQLException ex) {
						return ex;
					}
			}
		});

		if (problem != null) {
			throw problem;
		}
	}

	/**
	 * Changes the application password.
	 * 
	 * @param oldPassword
	 *        The old password.
	 * @param newPassword
	 *        The new password to set.
	 * @throws InvalidPasswordException
	 *         If the old password does not match.
	 * @throws ModuleException
	 *         If the application is not in the startup phase.
	 * @throws SQLException
	 *         If database access fails.
	 */
	@CalledFromJSP
	public static void changePassword(final String oldPassword, final String newPassword) throws InvalidPasswordException,
			ModuleException, SQLException {

		checkPassword(oldPassword);

		try {
			TLContext.inSystemContext(ApplicationPasswordUtil.class, new DBOperation() {
				@Override
				protected void update(PooledConnection connection) throws ModuleException, InvalidPasswordException, SQLException {
					PasswordBasedEncryptionService.changePassword(connection, oldPassword, newPassword);
					storePasswordHash(connection, newPassword);
				}
			}).reportProblem();
		} catch (ModuleException ex) {
			throw ex;
		} catch (InvalidPasswordException ex) {
			throw ex;
		} catch (SQLException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Provide the application password after boot.
	 * 
	 * <p>
	 * A password must already be initially setup, see {@link #hasPassword()}.
	 * </p>
	 * 
	 * @param password
	 *        The password to use for encryption.
	 */
	@CalledFromJSP
	public static void usePassword(String password) throws InvalidPasswordException, BootFailure, ModuleException {
		if (!DeferredBootUtil.isBootPending()) {
			throw new IllegalStateException("Application already started.");
		}
		checkPassword(password);

		PasswordBasedEncryptionService.startUp(password.toCharArray());
		DeferredBootService.getInstance().boot();
	}

	private static void checkPassword(String password) throws InvalidPasswordException {
		String encodedSaltAndPasswordHash = getStoredSaltAndPasswordHash();
		if (encodedSaltAndPasswordHash == null) {
			// No password set up.
			throw new InvalidPasswordException();
		}

		byte[] saltAndPasswordHash = new Base64().decode(encodedSaltAndPasswordHash);
		int hashSize = saltAndPasswordHash.length - SALT_SIZE;

		byte[] salt = ArrayUtil.copy(saltAndPasswordHash, 0, SALT_SIZE);
		byte[] passwordHash = ArrayUtil.copy(saltAndPasswordHash, SALT_SIZE, hashSize);

		byte[] recoveredHash = createHash(salt, password);

		if (!Arrays.equals(passwordHash, recoveredHash)) {
			throw new InvalidPasswordException();
		}
	}

	private static byte[] createHash(byte[] salt, String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance(PASSWORD_DIGEST);
			digest.update(salt);
			digest.update(password.getBytes(PASSWORD_ENCODING));
			byte[] recoveredHash = digest.digest();
			for (int n = 0; n < DIGEST_ITERATIONS; n++) {
				recoveredHash = digest.digest(recoveredHash);
			}
			return recoveredHash;
		} catch (NoSuchAlgorithmException ex) {
			throw fail("Unsupported digest algoorithm.", ex);
		} catch (UnsupportedEncodingException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	private static String getStoredSaltAndPasswordHash() {
		return TLContext.inSystemContext(ApplicationPasswordUtil.class, new Computation<String>() {
			@Override
			public String run() {
				ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
				return new DBProperties(pool).getProperty(DBProperties.GLOBAL_PROPERTY, APPLICATION_PASSWORD_PROPERTY);
			}
		});
	}

	private static AssertionError fail(String message, NoSuchAlgorithmException ex) {
		return (AssertionError) new AssertionError(message).initCause(ex);
	}

}
