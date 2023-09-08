
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;

/**
 * Manager for the matching key pairs of a user. 
 * 
 * The keys are RSA keys. First,
 * this manager tries to load the keys from the harddisc. If this fails, new
 * keys will be generated and stored.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class KeyStore {

    /** The path for the stored keys. */
	public static final String KEY_PATH = "/WEB-INF" + FileUtilities.PATH_SEPARATOR
                                        + "database" + FileUtilities.PATH_SEPARATOR
                                        + "keys";

    /** The extention for the stored keys. */
    private static final String KEY_EXT = ".key";

    /** 
     * Private CTor since this class has static methods only.
     */
    private KeyStore () {
        // static methods only
    }

    /**
     * Load the keys for the given user from the harddisc. Both keys have
     * to be on the disc, otherwise the method returns false.
     *
     * @param    anUserID    The ID of the user, whose keys should be loaded.
     * @return   true, if both keys have been loaded.
     */
    public static KeyPair loadKeyPair (String anUserID) throws Exception {
		try (InputStream fileStream = FileManager.getInstance().getStreamOrNull(resourceName("keyPair_" + anUserID))) {
			if (fileStream != null) {
				try (ObjectInputStream oiStream = new ObjectInputStream(fileStream)) {
					KeyPair key = ((KeyPair) oiStream.readObject());
					CryptLogger.debug("Loaded key pair for user \"" + anUserID + "\"!", CryptSupport.class);
					return key;
				}
			}
		}
		return null;
    }

    /**
     * Store the given key pair under the given name in the file system. If there
     * are any errors while storing, the saving fails.
     *
     * @param    aUserID      The name of the key to be saved.
     * @param    aKeyPair     The key pair to be stored.
     */
	public static void saveKeyPair(String aUserID, KeyPair aKeyPair) throws IOException {
		File keyFile = FileManager.getInstance().getIDEFile(resourceName("keyPair_" + aUserID));
		FileUtilities.enforceDirectory(keyFile.getParentFile());
		FileOutputStream fileStream = new FileOutputStream(keyFile);
		try {
			ObjectOutputStream oiStream = new ObjectOutputStream(fileStream);
			try {
				oiStream.writeObject(aKeyPair);
			} finally {
				oiStream.close();
			}
		} finally {
			fileStream.close();
		}
    }

    private static String resourceName(String aName) {
		return KEY_PATH + FileUtilities.PATH_SEPARATOR + aName + KEY_EXT;
	}
}

