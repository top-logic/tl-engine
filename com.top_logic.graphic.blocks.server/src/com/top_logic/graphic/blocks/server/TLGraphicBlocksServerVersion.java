/*
 * Copyright (c) 2018 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.graphic.blocks.server;

import com.top_logic.basic.AbstractVersion;

/**
 * Version class of the application.
 *
 * @author    <a href="mailto:support@top-logic.com">Top-Logic Support</a>
 */
public class TLGraphicBlocksServerVersion extends AbstractVersion {

    /** Singleton {@link TLGraphicBlocksServerVersion} instance. */
	public static final TLGraphicBlocksServerVersion INSTANCE = new TLGraphicBlocksServerVersion();

	private TLGraphicBlocksServerVersion() {
		// Singleton constructor.
	}

	/**
     * Main function to identify this package.
     * 
     * @param args Will be ignored in here.
     */
    public static void main(String args[]) {
        INSTANCE.printVersions(System.out);
    }
}
