/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import com.top_logic.basic.io.FileUtilities;

/**
 * Manager for creating a KeyEntry in a KeyStore 
 * from existing certificates and a private key.
 * 
 * PREREQUISITES: Steps you have to perform to get a valid certificate from Deutsche Telekom AG
 *	1. Create a private key (if you don't have one already) [use -idea|-des|-des3 cmd line option to generate encrypted keys]
 *		openssl genrsa -out privkey.pem 1024
 *	2. Create a certificate request
 *		openssl req -new -outform PEM -out certreq.pem -key privkey.pem
 *	3. Go to the Telekom Trust Center web site and enter all data incl. the certificate request.
 *		address: https://wwwca.telesec.de/Pub_Cert/ServPass/index.html
 *		for requesting or renewing a ServerPass.
 *	4. Print the stuff, get a subscription and send it to the Telekom
 *	5. Wait for the email and get the certificate from the website
 * 		address: https://wwwca.telesec.de/Pub_Cert/ServPass/Z_Abhol/index.html
 * 		Easiest way seems to be:
 * 			Download the certificate chain
 * 			Edit the document
 * 				Save the user/ca/root certificate parts in the files user/ca/root.crt
 * 				NOTE: the .crt files begin with -----BEGIN CERTIFICATE----- and end with
 * 						-----END CERTIFICATE----- followed by a NEW LINE!
 * 
 * NOTE: you can generate a self-signed test certificate with the command:
 * 	openssl req -x509 -newkey rsa:1024 -keyout privkey.pem -out user.crt
 * 
 * NOTE2: you can also generate your own root CA and sign certificates with it like this:
 *  1. Create a root CA
 *    a) Create a private root CA key
 * 		 openssl genrsa -out privkey_root_bos.pem 1024
 *    b) Create a certificate request for the CA
 * 		 openssl req -new -outform PEM -out careq_root_bos.pem -key privkey_root_bos.pem
 *    c) Create a root CA certificate
 * 		 openssl x509 -req -days 3650 -in careq_root_bos.pem -extensions v3_ca -signkey privkey_root_bos.pem -out cacert_root_bos.crt
 *  2. Create the certificate for the server
 *    a) Create a private key for the server
 * 		 genrsa -out privkey_www.pem 1024
 *    b) Create a certificate request for the server
 * 		 openssl req -new -outform PEM -out certreq_www.pem -key privkey_www.pem
 *    c) Sign the certificate for the server with the root CA certificate
 * 		 openssl x509 -req -days 3650 -in certreq_www.pem -extensions v3_usr -CA cacert_root_bos.crt -CAkey privkey_root_bos.pem -CAcreateserial -out cert_www.crt
 *    d) Convert the server private key for importing with the KeyEntryManager
 * 		 openssl pkcs8 -topk8 -outform DER -in privkey_www.pem -out privkey_www.key -nocrypt -v1 PBE-SHA1-DES
 * 
 * REMARK: Steps I performed to create a KeyEntry for the NewsServer:
 *	1. Import the certificates user/ca/root.crt [the certificate chain, e.g. B&amp;P->Telekom CA->GTE Root] with the java keytool:
 *	   	keytool -import -v -trustcacerts -file %user/ca/root%.crt -keystore %aKeyStoreFileName% -alias %aUserCrtAlias/aCACrtAlias/aRootAlias%
 *  2. Convert the PrivateKey (privkey.pem) into DER-encoded PKCS#8 format (privkey.key) whith openssl:
 *	   	openssl pkcs8 -topk8 -outform DER -in privkey.pem -out privkey.key -nocrypt -v1 PBE-SHA1-DES
 *  3. Use this KeyEntryManager to create a new KeyStore with a KeyEntry that
 *     contains the certificate and the private key:
 *		KeyEntryManager theKEM = new KeyEntryManager ();
 *		theKEM.importKeyEntry (new FileInputStream (%aKeyStoreFileName%),
 *							   %aKeyStorePassword%.toCharArray (),
 *							   new String[]  {%aUserCrtAlias%, %aCACrtAlias%, %aRootAlias%},
 *							   new FileInputStream (%aKeyFileName%)
 *							   %aKeyEntryAlias%,
 *							   %aKeyEntryPassword%.toCharArray (),
 *							   new FileOutputStream (%aNewKeyStoreFileName%),
 *							   %aNewKeyStorePassword%.toCharArray ());
 *
 * REMARK2:
 *  The recommended way to generate your keystore is with {@link com.top_logic.base.security.util.CertificateManager}
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class KeyEntryManager  {

	private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();
	
	/**
	 * Add a KeyEntry with a given Key and Certificates already in the KeyStore
	 *
	 * @param	aKeyStore				InputStream from which the KeyStore (JKS format) is loaded
	 * @param	aKeyStorePassword		the password protecting the KeyStore, null for DEFAULT_PASSWORD
	 * @param	aCertificateNames		Alias names under which the Certificates forming a
	 *									CertificateChain are known in the KeyStore
	 * @param	aKey					InputStream from which the DER-encoded PKCS#8-encoded RSA PrivateKey can be read
	 * @param	anAlias					alias name for the created KeyEntry
	 * @param	aKeyEntryPassword		the password to protect the KeyEntry, null for DEFAULT_PASSWORD
	 * @param	aNewKeyStore			OutputStream to which the modified KeyStore will be written
	 * @param	aNewKeyStorePassword	the password to protect the new KeyStore, null for DEFAULT_PASSWORD
	 * @return  true, if everything worked, false otherwise
	 */
	public boolean importKeyEntry (InputStream aKeyStore,
								   char[] aKeyStorePassword,
						   		   String[] aCertificateNames,
						   		   InputStream aKey,
								   String anAlias,
								   char[] aKeyEntryPassword,
						   		   OutputStream aNewKeyStore,
								   char[] aNewKeyStorePassword) {
		// Load KeyStore
		KeyStore theKeyStore = null;
		try {
			theKeyStore = KeyStore.getInstance ("JKS", "SUN");
		}
		catch (Exception kex) {
			System.out.println ("Couldn't instantiate KeyStore: " + kex);
			return false;		
		}
		if ((aKeyStorePassword == null) || isEmpty(aKeyStorePassword)) {
			aKeyStorePassword = DEFAULT_PASSWORD;
		} 
		try {
			theKeyStore.load (aKeyStore, aKeyStorePassword);
		}
		catch (Exception iex) {
			System.out.println ("Couldn't load KeyStore: " + iex);
			return false;
		}

		// Load Key
		ByteArrayOutputStream theBAOS = new ByteArrayOutputStream ();
		try {
			FileUtilities.copyStreamContents(aKey, theBAOS);
		}
		catch (IOException iex) {
			System.out.println ("Couldn't read Key: " + iex);
			return false;
		}
		byte[] theKey = theBAOS.toByteArray ();
		KeySpec theKeySpec = new PKCS8EncodedKeySpec(theKey);
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
		}
		catch (Exception ex) {
			System.out.println ("Couldn't create KeyFactory: " + ex);
			return false;		
		}
		PrivateKey bobPubKey = null;
		try {
			bobPubKey = keyFactory.generatePrivate(theKeySpec);
			theKey = bobPubKey.getEncoded ();			
		}
		catch (Exception ex) {
			System.out.println ("Couldn't create PrivateKey: " + ex);
			return false;		
		}

		
		// Create CertificateChain
		int i = 0;
		Certificate[] theCertificates = new Certificate[aCertificateNames.length];
		try {
			for (; i<theCertificates.length; i++) {
				theCertificates[i] = theKeyStore.getCertificate (aCertificateNames[i]);
			}
		}
		catch (KeyStoreException kex)  {
			System.out.println ("Couldn't get certificate \"" + aCertificateNames[i] + "\": " + kex);
			return false;
		}

		// Create the KeyEntry
		if ((aKeyEntryPassword == null) || isEmpty(aKeyEntryPassword)) {
			aKeyEntryPassword = DEFAULT_PASSWORD;
		} 
		try {
			theKeyStore.setKeyEntry (anAlias, bobPubKey, aKeyEntryPassword, theCertificates);
		}
		catch (KeyStoreException kex)  {
			System.out.println ("Couldn't create KeyEntry \"" + anAlias + "\": " + kex);
			return false;
		}

		// Save KeyStore
		if ((aNewKeyStorePassword == null) || isEmpty(aNewKeyStorePassword)) {
			aNewKeyStorePassword = DEFAULT_PASSWORD;
		}
		try {
			theKeyStore.store (aNewKeyStore, aNewKeyStorePassword);
		}
		catch (Exception kex)  {
			System.out.println ("Couldn't store KeyStore" + kex);
			return false;
		}

		try {
			/* Key theKeyS = */ theKeyStore.getKey (anAlias, aKeyEntryPassword);
		}
		catch (Exception kex)  {
			System.out.println ("Couldn't get Key \"" + anAlias + "\": " + kex);
			return false;
		}
		
		return true;
	}

	/**
	 * TODO KBU make this work via command line!!!
	 * Use this method to extend a KeyStore with a given certificate with a KeyEntry
	 * (Certificate/Key pair needed to communicate via SSL with a web server, e.g. Tomcat).
	 * For prerequisites and general process cf. class documentation above.
	 * The method accepts the following parameters:
	 *		-keystorefile		path to the Java keystore file containing the certificate
	 *		-keystorepass		password for keystore
	 *		-certalias			alias(es) of the imported certificate (chain) used for creating the KeyEntry
	 *		-keyfile			path to the key file with which the certificate is encrypted
	 *		-keyentryalias		alias for the KeyEntry in the keystore
	 *		-keyentrypass		password for the KeyEntry
	 *		-newkeystorefile	path to new keystore file (must be different from old one!!!)
	 *		-newkeystorepass	password for new keystore (DEFAULT_KEY if not specified)
	 *
	 * @param aParams the cmd line args (not used currently)
	 */
	public static void main (String[] aParams) {
		try {
			KeyEntryManager theKEM = new KeyEntryManager ();
			boolean success;
			final FileInputStream aKeyStore = new FileInputStream ("C:\\.keystore");
			try {
				final FileInputStream aKey = new FileInputStream("C:\\pos.key");
				try {
					final FileOutputStream aNewKeyStore = new FileOutputStream("C:\\temp\\.keystore");
					try {
						success = theKEM.importKeyEntry(
							aKeyStore,
							DEFAULT_PASSWORD,
							new String[] { "bmwzumcert", "telekomcacert", "gterootcert" },
							aKey,
							"bmwzumkeyentry",
							DEFAULT_PASSWORD,
							aNewKeyStore,
							DEFAULT_PASSWORD);
					} finally {
						aNewKeyStore.close();
					}
				} finally {
					aKey.close();
				}
			} finally {
				aKeyStore.close();
			}
			System.out.println ((success) ? "Success!" : "Errors occured");	   
		} catch (Throwable ex) {
			System.err.println ("Sorry, couldn't create KeyEntry!");
			ex.printStackTrace ();
		}
	}

	private static boolean isEmpty(char[] chars) {
		return chars.length == 0;
	}

}
