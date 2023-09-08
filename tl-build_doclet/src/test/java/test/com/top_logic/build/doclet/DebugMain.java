/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

import java.io.File;
import java.util.Optional;
import java.util.spi.ToolProvider;

import com.top_logic.build.doclet.TLDoclet;

/**
 * Invokes JavaDoc by a direct method call to allow debugging the doclet.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DebugMain {

	/**
	 * Main entry point.
	 */
	public static void main(String[] args) {
		new File("target/javadoc").mkdirs();

		String cp = System.getProperty("java.class.path");
		String mp = System.getProperty("jdk.module.path");
		System.out.println("CP: " + cp);
		System.out.println("MP: " + mp);
		String[] docArgs = {
			"-sourcepath", "src/test/java",
			"-classpath", cp + File.pathSeparatorChar + mp,
			"-doclet", TLDoclet.class.getName(),
			"-d", "target/javadoc",
			"-targetMessages", "target/messages_en.properties",
			"-acronyms", "test-acronyms.properties",
			"test.com.top_logic.build.doclet",
			"test.com.top_logic.build.doclet.vardelegate",
			"test.com.top_logic.build.doclet.wildcard",
		};

		Optional<ToolProvider> javadocProvider = ToolProvider.findFirst("javadoc");
		ToolProvider provider = javadocProvider.get();
		provider.run(System.out, System.err, docArgs);
	}
}
