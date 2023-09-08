/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

/**
 * Support class for en- and decoding a given message. This class only
 * schedules the access to the real coder.
 *
 * @author    <a href="mailto:mga@top-logic">Michael G&auml;nsler</a>
 */
interface MessageCoder {

    /**
     * Encode the given message. If the given message is null, null will be
     * returned. The key will be taken from the defined key pair in this
     * instance.
     *
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    public String encodeString (String aMessage);

    /**
     * Decode the given message. If the given message is null, null will be
     * returned. The key will be taken from the defined key pair in this
     * instance.
     *
     * @param    aMessage      The message to be decoded.
     * @return   The decoded message.
     */
    public String decodeString (String aMessage);

    /**
     * Encode the given message using the given key. If the given message is
     * null, null will be returned.
     *
     * @param    aKey          The public key to be used for encoding.
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    public String encodeString (Object aKey, String aMessage);

    /**
     * Decode the given message using the given key. If the given message is
     * null, null will be returned.
     *
     * @param    aKey          The private key to be used for decoding.
     * @param    aMessage      The message to be decoded.
     * @return   The decoded message.
     */
    public String decodeString (Object aKey, String aMessage);
}

