/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;

/**
 * A common superclass that can be used to defer access to {@link BinaryData} or refetch it, if
 * possible.
 * 
 * @author <a href="mailto:klaus@top-logic.com">klaus</a>
 */
public abstract class BinaryDataProxy extends AbstractBinaryData {

    private BinaryData proxied;
    
    private long originalSize;   
    
    /**
	 * Create a {@link BinaryDataProxy} with some known base data.
	 */
    public BinaryDataProxy(BinaryData baseData) {
        this.proxied      = baseData;
        this.originalSize = baseData.getSize();
    }
    
	@Override
	public String getName() {
		return proxied.getName();
	}

    /**
	 * Create a {@link BinaryDataProxy} with some known size.
	 */
    public BinaryDataProxy(long aSize) {
        this.originalSize = aSize;
    }

    /**
     * (Re-) Create the Binary Data from some Knowledge found elsewhere. 
     */
    protected abstract BinaryData createBinaryData() throws IOException;

    /**
     * Lazy access to {@link #proxied} Data that will fall back to {@link #createBinaryData()}
     * 
     * This will set {@link #originalSize} again.
     * 
     */
    public BinaryData getBinaryData() throws IOException {
        if (proxied == null) {
            proxied      = createBinaryData();
            originalSize = proxied.getSize();
        }
        return (proxied);
    }
    
    /** 
     * Relay via {@link #getBinaryData()}
     */
    @Override
	public long getSize() {
        return originalSize;
    }

    /** 
     * Relay via {@link #getBinaryData()}, will try {@link #createBinaryData()} once on {@link IOException}. 
     */
    @Override
	public InputStream getStream() throws IOException {
        int retry = 2;
        while (retry-- > 0) {
            try {
                return getBinaryData().getStream();
            } catch (IOException iox) {
                if (retry > 0) {
                    proxied = null;
                } else {
                    throw iox;
                }
            }
        }
        throw new IOException("Failed to retry getStream()"); // Unreachable
    }

	@Override
	public String getContentType() {
		try {
			return getBinaryData().getContentType();
		} catch (IOException ex) {
			return CONTENT_TYPE_OCTET_STREAM;
		}
	}

}
