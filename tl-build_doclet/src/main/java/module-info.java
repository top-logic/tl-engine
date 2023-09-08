/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Module for the Java Doc.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
module com.top_logic.build.doclet {

	requires java.xml;
	requires java.compiler;
	requires jdk.javadoc;

	requires com.top_logic.tools.resources;

	exports com.top_logic.build.doclet;

}