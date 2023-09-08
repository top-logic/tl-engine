/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template;

import java.io.File;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileLocation;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.template.ScriptUtil;
import com.top_logic.layout.scripting.util.LazyActionProvider;

/**
 * {@link LazyActionProvider} for scripted tests that are created via templates. Locates the file by
 * the given file path, which is resolved with the {@link FileManager}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
final class TemplateScriptActionProvider extends LazyActionProvider {

	private final FileLocation _file;

	private final Map<String, ?> _templateParameters;

	/**
	 * Creates a new {@link LazyActionProvider} that uses a file with the given path.
	 * <p>
	 * The {@link File} itself cannot be given to delay errors until the {@link ApplicationAction}
	 * is requested, i.e. until the test is executed. Otherwise, a mistake on {@link File}
	 * instantiation could kill the whole test tree creation.
	 * </p>
	 * 
	 * @param file
	 *        The location of the file. Must not be <code>null</code>.
	 * @param templateParameters
	 *        The parameter values for the template. Is allowed to be <code>null</code>.
	 */
	TemplateScriptActionProvider(FileLocation file, Map<String, ?> templateParameters) {
		_file = file;
		_templateParameters = templateParameters;
	}

	@Override
	protected ApplicationAction createAction() {
		return ScriptUtil.DEFAULT.createActionFromTemplate("filesystem:" + _file.get().getAbsolutePath(),
			_templateParameters);
	}

}