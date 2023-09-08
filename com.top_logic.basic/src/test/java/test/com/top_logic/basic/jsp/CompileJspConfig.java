/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * A {@link CompileJspConfig} is a configuration for a {@link JspCompileTask} 
 * 
 * @author    <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class CompileJspConfig {
	
	private final File webapp;

	private List<File> jspFiles = Collections.emptyList();

	public CompileJspConfig(File webapp) {
		this.webapp = webapp;
	}
	
	public void setJspFiles(List<File> jspFiles) {
		this.jspFiles = jspFiles;
	}
	
	public List<File> getJspFiles() {
		return jspFiles;
	}

	public File getWebapp() {
		return webapp;
	}

	public boolean isVerbose() {
		return true;
	}

	public String getOutputDir() {
		return null;
	}

	public String getWebXmlFragment() {
		return null;
	}

	public String getWebXml() {
		return null;
	}
	
}
