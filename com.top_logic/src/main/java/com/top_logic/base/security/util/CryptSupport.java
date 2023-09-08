/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;


/**
 * Support class for en- and decoding a given message. This class only
 * schedules the access to the real coder.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class CryptSupport extends ManagedClass {

    /** The coder for messages. */
    private MessageCoder coder;

    /**
     * Creates an instance of this class. Is private because of
     * singleton pattern.
     */
    CryptSupport () {
    }

    /**
     * Encode the given message. If the given message is null, null will be
     * returned. The key will be taken from the defined key pair in this
     * instance.
     *
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    public String encodeString (String aMessage) {
        if (StringServices.isEmpty(aMessage)) {
            return aMessage;    // Catches null and ""
        }
        
        return (this.getEncoder ().encodeString (aMessage));
    }

    /**
     * Decode the given message. If the given message is null, null will be
     * returned. The key will be taken from the defined key pair in this
     * instance.
     *
     * @param    aMessage      The message to be decoded.
     * @return   The decoded message.
     */
    public String decodeString (String aMessage) {
        if (StringServices.isEmpty(aMessage)) {
            return aMessage; // Cathes null and ""
        }
        
        return (this.getEncoder ().decodeString (aMessage));
    }

    /**
     * Encode the given message using the given key. If the given message is
     * null, null will be returned.
     *
     * @param    aKey          The public key to be used for encoding.
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    public String encodeString (Object aKey, String aMessage) {
        if (StringServices.isEmpty(aMessage)) {
            return aMessage; // Cathes null and ""
        }
        
        return (this.getEncoder ().encodeString (aKey, aMessage));
    }

    /**
     * Decode the given message using the given key. If the given message is
     * null, null will be returned.
     *
     * @param    aKey          The private key to be used for decoding.
     * @param    aMessage      The message to be decoded.
     * @return   The decoded message.
     */
    public String decodeString (Object aKey, String aMessage) {
        if (StringServices.isEmpty(aMessage)) {
            return aMessage; // Catches null and ""
        }
        
        return (this.getEncoder ().decodeString (aKey, aMessage));
    }

    /**
     * Returns the coder responsible for message coding.
     *
     * @return    The requested coder.
     */
    private MessageCoder getEncoder () {
        if (this.coder == null) {
            this.coder = new RSA ();
        }

        return (this.coder);
    }

    /**
     * Returns the only instance of this class.
     *
     * @return    The only instance of this class.
     */
    public static CryptSupport getInstance () {
        return Module.INSTANCE.getImplementationInstance();
    }
    
	/**
	 * {@link BasicRuntimeModule} of {@link CryptSupport}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
    public static final class Module extends BasicRuntimeModule<CryptSupport> {

		public static final Module INSTANCE = new Module();

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return NO_DEPENDENCIES;
		}

		@Override
		public Class<CryptSupport> getImplementation() {
			return CryptSupport.class;
		}

		@Override
		protected CryptSupport newImplementationInstance() throws ModuleException {
			return new CryptSupport();
		}
	}

}
