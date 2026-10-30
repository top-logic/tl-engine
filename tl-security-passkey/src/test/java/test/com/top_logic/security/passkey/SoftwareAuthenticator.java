/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

/**
 * An authenticator in test code that answers the ceremonies of a relying party like a platform
 * authenticator holding one discoverable passkey.
 *
 * <p>
 * It takes the options JSON a relying party sends to the browser (<code>{"publicKey": ...}</code>)
 * and returns the JSON the browser sends back (<code>PublicKeyCredential.toJSON()</code>): the
 * client data, the authenticator data, a "none" attestation in CBOR and an ES256 (P-256) signature.
 * The browser part (client data with its origin) is simulated as well, so a test can present a
 * wrong origin.
 * </p>
 */
public class SoftwareAuthenticator {

	/** Key of the options for the browser and of the client data: the challenge. */
	private static final String CHALLENGE = "challenge";

	/** Key of the options for the browser: the public key options. */
	private static final String PUBLIC_KEY = "publicKey";

	/** Key of the creation options: the relying party. */
	private static final String RP = "rp";

	/** Key of the request options: the relying party ID. */
	private static final String RP_ID = "rpId";

	/** Key of the relying party and the user in the creation options: the ID. */
	private static final String ID = "id";

	/** Key of the creation options: the user. */
	private static final String USER = "user";

	/** Key of the client data: the ceremony type. */
	private static final String TYPE = "type";

	/** Key of the client data: the origin. */
	private static final String ORIGIN = "origin";

	/** Key of the client data: whether the ceremony runs in a cross-origin frame. */
	private static final String CROSS_ORIGIN = "crossOrigin";

	/** Key of the credential: the raw ID. */
	private static final String RAW_ID = "rawId";

	/** Key of the credential: the authenticator response. */
	private static final String RESPONSE = "response";

	/** Key of the credential: the extension results. */
	private static final String CLIENT_EXTENSION_RESULTS = "clientExtensionResults";

	/** Key of the credential: how the authenticator is attached. */
	private static final String AUTHENTICATOR_ATTACHMENT = "authenticatorAttachment";

	/** Key of the response: the client data. */
	private static final String CLIENT_DATA_JSON = "clientDataJSON";

	/** Key of the attestation response: the attestation object. */
	private static final String ATTESTATION_OBJECT = "attestationObject";

	/** Key of the responses: the authenticator data. */
	private static final String AUTHENTICATOR_DATA = "authenticatorData";

	/** Key of the attestation response: the transports. */
	private static final String TRANSPORTS = "transports";

	/** Key of the attestation response: the public key in SPKI form. */
	private static final String RESPONSE_PUBLIC_KEY = "publicKey";

	/** Key of the attestation response: the COSE algorithm of the public key. */
	private static final String PUBLIC_KEY_ALGORITHM = "publicKeyAlgorithm";

	/** Key of the assertion response: the signature. */
	private static final String SIGNATURE = "signature";

	/** Key of the assertion response: the user handle. */
	private static final String USER_HANDLE = "userHandle";

	/** Key of the attestation object: the attestation format. */
	private static final String FMT = "fmt";

	/** Key of the attestation object: the attestation statement. */
	private static final String ATT_STMT = "attStmt";

	/** Key of the attestation object: the authenticator data. */
	private static final String AUTH_DATA = "authData";

	/** Client data type of a registration. */
	private static final String WEBAUTHN_CREATE = "webauthn.create";

	/** Client data type of a login. */
	private static final String WEBAUTHN_GET = "webauthn.get";

	/** The credential type. */
	private static final String PUBLIC_KEY_TYPE = "public-key";

	/** Attestation format without attestation. */
	private static final String NONE = "none";

	/** Flag of the authenticator data: user present. */
	private static final int FLAG_UP = 0x01;

	/** Flag of the authenticator data: user verified. */
	private static final int FLAG_UV = 0x04;

	/** Flag of the authenticator data: backup eligible. */
	private static final int FLAG_BE = 0x08;

	/** Flag of the authenticator data: backed up. */
	private static final int FLAG_BS = 0x10;

	/** Flag of the authenticator data: attested credential data included. */
	private static final int FLAG_AT = 0x40;

	/** COSE key parameter: key type. */
	private static final int COSE_KTY = 1;

	/** COSE key parameter: algorithm. */
	private static final int COSE_ALG = 3;

	/** COSE EC2 key parameter: curve. */
	private static final int COSE_CRV = -1;

	/** COSE EC2 key parameter: x coordinate. */
	private static final int COSE_X = -2;

	/** COSE EC2 key parameter: y coordinate. */
	private static final int COSE_Y = -3;

	/** COSE key type EC2. */
	private static final int COSE_KTY_EC2 = 2;

	/** COSE algorithm ES256. */
	private static final int COSE_ALG_ES256 = -7;

	/** COSE curve P-256. */
	private static final int COSE_CRV_P256 = 1;

	/** Size of a P-256 coordinate in bytes. */
	private static final int COORDINATE_SIZE = 32;

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

	private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();

	private final KeyPair _keyPair;

	private final byte[] _credentialId;

	private byte[] _userHandle;

	private long _signatureCount;

	private boolean _userVerification = true;

	private String _origin;

	/**
	 * Creates an authenticator with a new key pair and credential ID.
	 *
	 * @param origin
	 *        The origin the simulated browser puts into the client data.
	 * @param signatureCount
	 *        The signature counter the passkey starts with.
	 */
	public SoftwareAuthenticator(String origin, long signatureCount) throws GeneralSecurityException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp256r1"));
		_keyPair = generator.generateKeyPair();
		_credentialId = new byte[16];
		new SecureRandom().nextBytes(_credentialId);
		_origin = origin;
		_signatureCount = signatureCount;
	}

	/**
	 * The credential ID of the passkey, base64url without padding.
	 */
	public String getCredentialId() {
		return BASE64URL.encodeToString(_credentialId);
	}

	/**
	 * Sets the origin the simulated browser puts into the client data.
	 */
	public void setOrigin(String origin) {
		_origin = origin;
	}

	/**
	 * Sets whether the authenticator reports a verified user.
	 */
	public void setUserVerification(boolean userVerification) {
		_userVerification = userVerification;
	}

	/**
	 * Sets the signature counter; the next login reports this value plus one.
	 */
	public void setSignatureCount(long signatureCount) {
		_signatureCount = signatureCount;
	}

	/**
	 * Creates the passkey for the given creation options.
	 *
	 * @param optionsJson
	 *        The options JSON of the relying party (<code>{"publicKey": {...}}</code>).
	 * @return The JSON form of the created credential.
	 */
	public String create(String optionsJson) throws IOException, GeneralSecurityException {
		JsonNode options = JSON.readTree(optionsJson).get(PUBLIC_KEY);
		String rpId = options.get(RP).get(ID).asText();
		_userHandle = BASE64URL_DECODER.decode(options.get(USER).get(ID).asText());

		byte[] clientData = clientData(WEBAUTHN_CREATE, options.get(CHALLENGE).asText());
		byte[] coseKey = coseKey();
		ByteArrayOutputStream authData = new ByteArrayOutputStream();
		authData.write(sha256(rpId.getBytes(StandardCharsets.UTF_8)));
		authData.write(flags() | FLAG_AT);
		authData.write(counter());
		authData.write(new byte[16]); // AAGUID
		authData.write((_credentialId.length >> 8) & 0xFF);
		authData.write(_credentialId.length & 0xFF);
		authData.write(_credentialId);
		authData.write(coseKey);
		byte[] authenticatorData = authData.toByteArray();

		byte[] attestationObject = attestationObject(authenticatorData);

		ObjectNode response = JSON.createObjectNode();
		response.put(CLIENT_DATA_JSON, BASE64URL.encodeToString(clientData));
		response.put(ATTESTATION_OBJECT, BASE64URL.encodeToString(attestationObject));
		response.put(AUTHENTICATOR_DATA, BASE64URL.encodeToString(authenticatorData));
		ArrayNode transports = response.putArray(TRANSPORTS);
		transports.add("hybrid");
		transports.add("internal");
		response.put(RESPONSE_PUBLIC_KEY, BASE64URL.encodeToString(_keyPair.getPublic().getEncoded()));
		response.put(PUBLIC_KEY_ALGORITHM, COSE_ALG_ES256);
		return credential(response);
	}

	/**
	 * Logs in with the passkey for the given request options.
	 *
	 * @param optionsJson
	 *        The options JSON of the relying party (<code>{"publicKey": {...}}</code>).
	 * @return The JSON form of the assertion.
	 */
	public String get(String optionsJson) throws IOException, GeneralSecurityException {
		JsonNode options = JSON.readTree(optionsJson).get(PUBLIC_KEY);
		String rpId = options.get(RP_ID).asText();

		_signatureCount++;
		byte[] clientData = clientData(WEBAUTHN_GET, options.get(CHALLENGE).asText());
		ByteArrayOutputStream authData = new ByteArrayOutputStream();
		authData.write(sha256(rpId.getBytes(StandardCharsets.UTF_8)));
		authData.write(flags());
		authData.write(counter());
		byte[] authenticatorData = authData.toByteArray();

		Signature signer = Signature.getInstance("SHA256withECDSA");
		signer.initSign(_keyPair.getPrivate());
		signer.update(authenticatorData);
		signer.update(sha256(clientData));
		byte[] signature = signer.sign();

		ObjectNode response = JSON.createObjectNode();
		response.put(CLIENT_DATA_JSON, BASE64URL.encodeToString(clientData));
		response.put(AUTHENTICATOR_DATA, BASE64URL.encodeToString(authenticatorData));
		response.put(SIGNATURE, BASE64URL.encodeToString(signature));
		response.put(USER_HANDLE, BASE64URL.encodeToString(_userHandle));
		return credential(response);
	}

	private String credential(ObjectNode response) throws IOException {
		ObjectNode credential = JSON.createObjectNode();
		credential.put(ID, getCredentialId());
		credential.put(RAW_ID, getCredentialId());
		credential.put(TYPE, PUBLIC_KEY_TYPE);
		credential.put(AUTHENTICATOR_ATTACHMENT, "platform");
		credential.set(RESPONSE, response);
		credential.putObject(CLIENT_EXTENSION_RESULTS);
		return JSON.writeValueAsString(credential);
	}

	private byte[] clientData(String type, String challenge) throws IOException {
		ObjectNode clientData = JSON.createObjectNode();
		clientData.put(TYPE, type);
		clientData.put(CHALLENGE, challenge);
		clientData.put(ORIGIN, _origin);
		clientData.put(CROSS_ORIGIN, false);
		return JSON.writeValueAsBytes(clientData);
	}

	private int flags() {
		int flags = FLAG_UP | FLAG_BE | FLAG_BS;
		if (_userVerification) {
			flags |= FLAG_UV;
		}
		return flags;
	}

	private byte[] counter() {
		return ByteBuffer.allocate(4).putInt((int) _signatureCount).array();
	}

	private byte[] coseKey() throws IOException {
		ECPublicKey publicKey = (ECPublicKey) _keyPair.getPublic();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (CBORGenerator cbor = new CBORFactory().createGenerator(out)) {
			cbor.writeStartObject();
			cbor.writeFieldId(COSE_KTY);
			cbor.writeNumber(COSE_KTY_EC2);
			cbor.writeFieldId(COSE_ALG);
			cbor.writeNumber(COSE_ALG_ES256);
			cbor.writeFieldId(COSE_CRV);
			cbor.writeNumber(COSE_CRV_P256);
			cbor.writeFieldId(COSE_X);
			cbor.writeBinary(unsigned(publicKey.getW().getAffineX()));
			cbor.writeFieldId(COSE_Y);
			cbor.writeBinary(unsigned(publicKey.getW().getAffineY()));
			cbor.writeEndObject();
		}
		return out.toByteArray();
	}

	private static byte[] attestationObject(byte[] authenticatorData) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (CBORGenerator cbor = new CBORFactory().createGenerator(out)) {
			cbor.writeStartObject();
			cbor.writeStringField(FMT, NONE);
			cbor.writeFieldName(ATT_STMT);
			cbor.writeStartObject();
			cbor.writeEndObject();
			cbor.writeFieldName(AUTH_DATA);
			cbor.writeBinary(authenticatorData);
			cbor.writeEndObject();
		}
		return out.toByteArray();
	}

	private static byte[] unsigned(BigInteger coordinate) {
		byte[] bytes = coordinate.toByteArray();
		byte[] result = new byte[COORDINATE_SIZE];
		int length = Math.min(bytes.length, COORDINATE_SIZE);
		System.arraycopy(bytes, bytes.length - length, result, COORDINATE_SIZE - length, length);
		return result;
	}

	private static byte[] sha256(byte[] data) throws GeneralSecurityException {
		return MessageDigest.getInstance("SHA-256").digest(data);
	}

}
