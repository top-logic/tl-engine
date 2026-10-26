/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.docker;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Certificates of a Docker daemon with TLS verification and of its client, issued by a certificate
 * authority of their own: the server side as {@link SSLContext}, the client side as the directory
 * a Docker client reads (`ca.pem`, `cert.pem`, `key.pem`).
 */
public final class DaemonCertificates {

	private static final String KEY_ALGORITHM = "RSA";

	private static final int KEY_SIZE = 2048;

	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

	private static final Duration VALIDITY = Duration.ofDays(1);

	private static final String LOOPBACK = "127.0.0.1";

	private static final String ALIAS = "entry";

	private static final char[] PASSWORD = "test".toCharArray();

	private final SSLContext _server;

	private final Path _clientDir;

	private DaemonCertificates(SSLContext server, Path clientDir) {
		_server = server;
		_clientDir = clientDir;
	}

	/**
	 * Issues a new authority and the certificates of a server on the loopback address and of a
	 * client.
	 *
	 * @param clientDir
	 *        The directory the files of the client are written to.
	 */
	public static DaemonCertificates create(Path clientDir) throws IOException, GeneralSecurityException {
		try {
			KeyPair caKey = keyPair();
			X509Certificate ca = certificate("CN=tl-factory test CA", caKey, "CN=tl-factory test CA", caKey, true);
			KeyPair serverKey = keyPair();
			X509Certificate server = certificate("CN=" + LOOPBACK, serverKey, "CN=tl-factory test CA", caKey, false);
			KeyPair clientKey = keyPair();
			X509Certificate client = certificate("CN=client", clientKey, "CN=tl-factory test CA", caKey, false);

			Files.createDirectories(clientDir);
			writePem(clientDir.resolve("ca.pem"), ca);
			writePem(clientDir.resolve("cert.pem"), client);
			writePem(clientDir.resolve("key.pem"), clientKey.getPrivate());

			return new DaemonCertificates(serverContext(serverKey.getPrivate(), server, ca), clientDir);
		} catch (OperatorCreationException ex) {
			throw new GeneralSecurityException(ex);
		}
	}

	/**
	 * The context of the TLS server, trusting the clients of the authority.
	 */
	public SSLContext server() {
		return _server;
	}

	/**
	 * The directory holding the certificates of the client.
	 */
	public Path clientDir() {
		return _clientDir;
	}

	private static KeyPair keyPair() throws GeneralSecurityException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		generator.initialize(KEY_SIZE, new SecureRandom());
		return generator.generateKeyPair();
	}

	private static X509Certificate certificate(String subject, KeyPair subjectKey, String issuer, KeyPair issuerKey,
			boolean authority) throws IOException, GeneralSecurityException, OperatorCreationException {
		Instant now = Instant.now();
		X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuer),
			new BigInteger(64, new SecureRandom()), Date.from(now.minus(Duration.ofMinutes(5))),
			Date.from(now.plus(VALIDITY)), new X500Name(subject), subjectKey.getPublic());
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(authority));
		if (!authority) {
			builder.addExtension(Extension.subjectAlternativeName, false,
				new GeneralNames(new GeneralName[] {
					new GeneralName(GeneralName.iPAddress, LOOPBACK),
					new GeneralName(GeneralName.dNSName, "localhost") }));
		}
		return new JcaX509CertificateConverter().getCertificate(
			builder.build(new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(issuerKey.getPrivate())));
	}

	private static SSLContext serverContext(PrivateKey key, X509Certificate certificate, X509Certificate ca)
			throws IOException, GeneralSecurityException {
		KeyStore keys = KeyStore.getInstance(KeyStore.getDefaultType());
		keys.load(null, null);
		keys.setKeyEntry(ALIAS, key, PASSWORD, new X509Certificate[] { certificate, ca });
		KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagers.init(keys, PASSWORD);

		KeyStore trusted = KeyStore.getInstance(KeyStore.getDefaultType());
		trusted.load(null, null);
		trusted.setCertificateEntry(ALIAS, ca);
		TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagers.init(trusted);

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), new SecureRandom());
		return context;
	}

	private static void writePem(Path file, Object content) throws IOException {
		try (Writer out = Files.newBufferedWriter(file, StandardCharsets.US_ASCII);
				JcaPEMWriter pem = new JcaPEMWriter(out)) {
			pem.writeObject(content);
		}
	}

}
