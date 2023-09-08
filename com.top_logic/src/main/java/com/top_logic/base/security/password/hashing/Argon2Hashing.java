/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password.hashing;

import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.Argon2Parameters.Builder;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.encryption.SecureRandomService;

/**
 * {@link PasswordHashingAlgorithm} based on Argon2.
 * 
 * <p>
 * The created hash contains (almost) all configuration informations, such that the hash created
 * with one configuration can be verified with an {@link Argon2Hashing} with different
 * configuration.
 * </p>
 * 
 * <p>
 * See: https://github.com/P-H-C/phc-winner-argon2
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Argon2Hashing extends AbstractConfiguredInstance<Argon2Hashing.Config>
		implements PasswordHashingAlgorithm {

	/**
	 * Type of Argon2 algorithm.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public enum Argon2Type implements ExternallyNamed {

		/**
		 * {@link #ARGON2D} is a hybrid of {@link #ARGON2I} and {@link #ARGON2D}.
		 */
		ARGON2ID("argon2id", Argon2Parameters.ARGON2_id),

		/**
		 * {@link #ARGON2I} is designed to resist side-channel attacks.
		 */
		ARGON2I("argon2i", Argon2Parameters.ARGON2_i),

		/**
		 * {@link #ARGON2D} provides the highest resistance against GPU cracking attacks
		 */
		ARGON2D("argon2d", Argon2Parameters.ARGON2_d),
		;

		final int _type;

		private final String _externalName;

		private Argon2Type(String externalName, int type) {
			_externalName = externalName;
			_type = type;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	/**
	 * Typed configuration interface definition for {@link Argon2Hashing}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<Argon2Hashing> {

		/** Name for the configuration value of {@link #getPepper()}. */
		String PEPPER = "pepper";

		/** Name for the configuration value of {@link #getSaltSize()}. */
		String SALT_SIZE = "salt-size";

		/** Name for the configuration value of {@link #getHashSize()}. */
		String HASH_SIZE = "hash-size";

		/** Name for the configuration value of {@link #getVersion()}. */
		String VERSION = "version";

		/** Name for the configuration value of {@link #getIterations()}. */
		String ITERATIONS = "iterations";

		/** Name for the configuration value of {@link #getMemory()}. */
		String MEMORY = "memory";

		/** Name for the configuration value of {@link #getParallelism()}. */
		String PARALLELISM = "parallelism";

		/** Name for the configuration value of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The type of algorithm to use.
		 */
		@Name(TYPE)
		Argon2Type getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(Argon2Type type);

		/**
		 * The number of iterations
		 */
		@IntDefault(3)
		@Name(ITERATIONS)
		int getIterations();

		/**
		 * Setter for {@link #getIterations()}.
		 */
		void setIterations(int iterations);

		/**
		 * The memory usage in KiB.
		 */
		@IntDefault(4096)
		@Name(MEMORY)
		int getMemory();

		/**
		 * Setter for {@link #getMemory()}.
		 */
		void setMemory(int memory);

		/**
		 * The parallelism to use during hash computation.
		 */
		@IntDefault(1)
		@Name(PARALLELISM)
		int getParallelism();

		/**
		 * Setter for {@link #getParallelism()}.
		 */
		void setParallelism(int parallessism);

		/**
		 * Version of Argon2.
		 */
		@IntDefault(Argon2Hashing.DEFAULT_ARGON2_VERSION)
		@Name(VERSION)
		int getVersion();

		/**
		 * Setter for {@link #getVersion()}.
		 */
		void setVersion(int version);

		/**
		 * Hash output length to in bytes.
		 */
		@IntDefault(32)
		@Name(HASH_SIZE)
		int getHashSize();

		/**
		 * Setter for {@link #getHashSize()}.
		 */
		void setHashSize(int hashSize);

		/**
		 * Salt length in bytes.
		 */
		@IntDefault(16)
		@Name(SALT_SIZE)
		int getSaltSize();

		/**
		 * Setter for {@link #getSaltSize()}.
		 */
		void setSaltSize(int saltSize);

		/**
		 * A value that is included into the computation of the hash, but is not contained in the
		 * hash, i.e. a hash constructed with configuration "pepper1" can not be verified with
		 * configuration "pepper2".
		 * 
		 * <p>
		 * <b>WARNING:</b> After changing the pepper, all formerly created hash values are invalid.
		 * </p>
		 */
		@Name(PEPPER)
		@Encrypted
		String getPepper();

		/**
		 * Setter for {@link #getPepper()}.
		 */
		void setPepper(String pepper);
	}

	static final int DEFAULT_ARGON2_VERSION = Argon2Parameters.ARGON2_VERSION_13;

	private final byte[] _secret;

	/**
	 * Create a {@link Argon2Hashing}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public Argon2Hashing(InstantiationContext context, Config config) {
		super(context, config);
		_secret = getSecret(config);
	}

	private byte[] getSecret(Config config) {
		String pepper = config.getPepper();
		if (pepper.isEmpty()) {
			return null;
		}
		return pepper.getBytes(StringServices.CHARSET_UTF_8);
	}

	@Override
	public String createHash(char[] password) {
		Config config = getConfig();
		int type = config.getType()._type;
		int iterations = config.getIterations();
		int memory = config.getMemory();
		int parallelism = config.getParallelism();
		int version = config.getVersion();
		byte[] salt = new byte[config.getSaltSize()];
		SecureRandomService.getInstance().getRandom().nextBytes(salt);
		Argon2Parameters parameter = parameters(
			type,
			iterations,
			memory,
			parallelism,
			version,
			salt);
		byte[] out = createRawHash(parameter, password, config.getHashSize());
		String encoded = toEncodedString(parameter, out);
		return encoded;
	}

	private Argon2Parameters parameters(int type, int iterations, int memory, int parallelism, int version,
			byte[] salt) {
		Builder builder = new Argon2Parameters.Builder(type)
			.withCharToByteConverter(PasswordConverter.UTF8)
			.withIterations(iterations)
			.withMemoryAsKB(memory)
			.withParallelism(parallelism)
			.withSalt(salt)
			.withVersion(version);
		if (_secret != null) {
			builder.withSecret(_secret);
		}
		return builder.build();
	}

	private String toEncodedString(Argon2Parameters parameter, byte[] out) {
		StringBuilder builder = new StringBuilder();
		builder.append('$');
		switch (parameter.getType()) {
			case Argon2Parameters.ARGON2_id: {
				builder.append("argon2id");
				break;
			}
			case Argon2Parameters.ARGON2_i: {
				builder.append("argon2i");
				break;
			}
			case Argon2Parameters.ARGON2_d: {
				builder.append("argon2d");
				break;
			}
			default:
				throw new IllegalArgumentException();
		}
		builder.append('$');
		if (parameter.getVersion() != DEFAULT_ARGON2_VERSION) {
			builder.append("v=");
			builder.append(parameter.getVersion());
			builder.append('$');
		}
		builder.append("m=");
		builder.append(parameter.getMemory());
		builder.append(",t=");
		builder.append(parameter.getIterations());
		builder.append(",p=");
		builder.append(parameter.getLanes());
		builder.append('$');
		builder.append(Base64.getEncoder().withoutPadding().encodeToString(parameter.getSalt()));
		builder.append('$');
		builder.append(Base64.getEncoder().withoutPadding().encodeToString(out));
		return builder.toString();
 }

	private byte[] createRawHash(Argon2Parameters parameter, char[] password, int hashLength) {
		byte[] out = new byte[hashLength];
		Argon2BytesGenerator generator = new Argon2BytesGenerator();
		generator.init(parameter);
		generator.generateBytes(password, out);
		return out;
	}

	@Override
	public boolean verify(char[] password, String encoded) {
		String[] parts = encoded.split("\\$");
		int type = parts[1].endsWith("id") ? Argon2Parameters.ARGON2_id
			: parts[1].endsWith("i") ? Argon2Parameters.ARGON2_i
				: parts[1].endsWith("d") ? Argon2Parameters.ARGON2_d : -1;
		boolean hasVersion = !parts[2].startsWith("m=");
		String version = hasVersion ? parts[2].split("=")[1] : ("" + DEFAULT_ARGON2_VERSION);
		String[] paramParts = (hasVersion ? parts[3] : parts[2]).split(",");
		String m = paramParts[0].split("=")[1];
		String t = paramParts[1].split("=")[1];
		String p = paramParts[2].split("=")[1];
		
		String saltEncoded = hasVersion ? parts[4] : parts[3];
		byte[] salt = Base64.getDecoder().decode(saltEncoded);
		String hashEncoded = hasVersion ? parts[5] : parts[4];
		byte[] expectedHash = Base64.getDecoder().decode(hashEncoded);
		
		Argon2Parameters parameters = parameters(
			type,
			Integer.parseInt(t),
			Integer.parseInt(m),
			Integer.parseInt(p),
			Integer.parseInt(version),
			salt);
		byte[] actualHash = createRawHash(parameters, password, expectedHash.length);
		return Arrays.equals(expectedHash, actualHash);
	}

}

