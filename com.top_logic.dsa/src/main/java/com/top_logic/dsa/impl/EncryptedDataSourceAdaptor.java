/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.NotSupportedException;

/**
 * Add (Stream) Encryption to any kind of DataSourceAdaptor.
 * <p>
 * This class wraps some other DataSourceAdaptor and adds encryption
 * using the Java Cryptographic extension. This will not work (as of now)
 * with structured DataSources, this is better done in some underlying
 * implementation. This will not encrypt the structre of the data and
 * the names. Doing so would ease PlainText Attacks. 
 * </p>
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class EncryptedDataSourceAdaptor extends AbstractDelegatorDataSourceAdaptor {

	public interface Config extends PolymorphicConfiguration<EncryptedDataSourceAdaptor> {

		String IMPLEMENTATION_NAME = "impl";

		@Name(IMPLEMENTATION_NAME)
		PolymorphicConfiguration<? extends DataSourceAdaptor> getImplementation();

		void setImplementation(PolymorphicConfiguration<? extends DataSourceAdaptor> value);
	}

	private final SecretKey _secretKey;
	
	/**
	 * Creates a new {@link EncryptedDataSourceAdaptor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link EncryptedDataSourceAdaptor}.
	 * 
	 */
	public EncryptedDataSourceAdaptor(InstantiationContext context, Config config) {
		this(context.getInstance(config.getImplementation()));
	}

    public EncryptedDataSourceAdaptor(DataSourceAdaptor proxy) {
		if (proxy.isStructured()) {
			Logger.warn("Encrypting a structured DataSourceAdaptor has no effect.", this);
		}

		innerDSA = proxy;
		_secretKey = EncryptionService.getInstance().getEncryptionKey();
	}

	/** Check if DataSource already has some key. */
    public boolean hasKey() {
        return _secretKey != null;
    }

    /**
     * Forwarded to wrapped DataSource, decrypting the data.
    */
    @Override
	public InputStream getEntry(String path) throws DatabaseAccessException {
        try {
            Cipher encoder = Cipher.getInstance(algorithm());
            encoder.init(Cipher.DECRYPT_MODE, _secretKey);
            
			return new CipherInputStream(super.getEntry(path), encoder);
        }
        catch (GeneralSecurityException gsx) {
            throw new DatabaseAccessException(gsx);
        }
    }

    /**
     * Forwarded to wrapped DataSource, decrypting the data.
     */
    @Override
	public InputStream getEntry(String path, String version)
        throws DatabaseAccessException {
        try {
            Cipher encoder = Cipher.getInstance(algorithm());
            encoder.init(Cipher.DECRYPT_MODE, _secretKey);
        
			return new CipherInputStream(super.getEntry(path, version), encoder);
        }
        catch (GeneralSecurityException gsx) {
            throw new DatabaseAccessException(gsx);
        }
    }

    /**
     * Always return null, do not allow access to underlying file(s).
     */
    public File getFile() {
        return null;
    }

    /**
     * Always return null, do not allow access to underlying file(s).
     */
    public File getFile(String entryName) {
        return null;
    }

    /**
     * Forwarded to wrapped DataSource with encrypted data.
     */
    @Override
	public void putEntry(String path, InputStream data)
        throws DatabaseAccessException {
        try {
            Cipher encoder = Cipher.getInstance(algorithm());
            encoder.init(Cipher.ENCRYPT_MODE, _secretKey);
    
			super.putEntry(path, new CipherInputStream(data, encoder));
        }
        catch (GeneralSecurityException gsx) {
            throw new DatabaseAccessException(gsx);
        }
    }

    /**
     * Forwarded to wrapped DataSource.
     */
    @Override
	public OutputStream putEntry(String containerPath, String elementName)
        throws DatabaseAccessException {
        try {
            Cipher encoder = Cipher.getInstance(algorithm());
            encoder.init(Cipher.ENCRYPT_MODE, _secretKey);

			return new CipherOutputStream(super.putEntry(containerPath, elementName), encoder);
        }
        catch (GeneralSecurityException gsx) {
            throw new DatabaseAccessException(gsx);
        }
    }

    /**
     * Forwarded to wrapped DataSource.
     */
    @Override
	public OutputStream getEntryOutputStream(String path)
        throws DatabaseAccessException {
        try {
            Cipher decoder = Cipher.getInstance(algorithm());
            decoder.init(Cipher.DECRYPT_MODE, _secretKey);

			return new CipherOutputStream(super.getEntryOutputStream(path), decoder);
        }
        catch (GeneralSecurityException gsx) {
            throw new DatabaseAccessException(gsx);
        }
    }

    /**
     * Not supported.
     * 
     * @throws NotSupportedException always,  cannot encrypt when appending 
     */
    @Override
	public OutputStream getEntryAppendStream (String path)
             throws DatabaseAccessException {
        throw new NotSupportedException ("cannot encrypt when appending");
    }

    /**
     * @throws  NotSupportedException always since forwarding to the
     *          encrypted file makes no sense.
     */
    @Override
	public String getURL(String path) throws NotSupportedException {
        throw new NotSupportedException("Cannot forward to encryped data");
    }

    /**
     * @throws  NotSupportedException always since forwarding to the
     *          encrypted file makes no sense.
     */
    @Override
	public String getForwardURL(String path) throws NotSupportedException {
        throw new NotSupportedException("Cannot forward to encryped data");
    }

    /**
     * Forwarded to wrapped DataSource.
     */
    @Override
	public String createEntry(
        String containerPath, String elementName, InputStream data)
        throws DatabaseAccessException {
        try {
            Cipher encoder = Cipher.getInstance(algorithm());
            encoder.init(Cipher.ENCRYPT_MODE, _secretKey);

			return super.createEntry(containerPath, elementName,
                new CipherInputStream(data, encoder));
        }
        catch (GeneralSecurityException gsx) {
             throw new DatabaseAccessException(gsx);
        }
    }

    /**
     * Forwarded to wrapped DataSource.
     */
    @Override
	public OutputStream createEntry(String containerPath, String elementName)
        throws DatabaseAccessException {
        try {
            Cipher encoder = Cipher.getInstance(algorithm());
            encoder.init(Cipher.ENCRYPT_MODE, _secretKey);

			return new CipherOutputStream(super.createEntry(containerPath, elementName), encoder);
        }
        catch (GeneralSecurityException gsx) {
             throw new DatabaseAccessException(gsx);
        }
    }

    /** Example how to generate a KEY */
     public static void main(String args[]) throws Exception {

         // See ...jdk1.4.2/docs/guide/security/jce/JCERefGuide.html#AppA
         // Availeable Methods AES, Blowfish, DES, DESede,RC2,RC4,RC5, RSA
         KeyGenerator kgen = KeyGenerator.getInstance("AES");
         kgen.init(128); // 128 ,192 and 256 bits should be be available

         SecretKey skey = kgen.generateKey();
         byte[]    raw  = skey.getEncoded();
        
         System.out.println(StringServices.toHexString(raw));
            
     }

	private String algorithm() {
		return _secretKey.getAlgorithm();
	}

}
