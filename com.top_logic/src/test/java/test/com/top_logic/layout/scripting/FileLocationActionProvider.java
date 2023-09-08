/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;

import java.io.File;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.FileLocation;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionReader;
import com.top_logic.layout.scripting.util.LazyActionProvider;

/**
 * Reads {@link ApplicationAction}s from an XML file which is located with a {@link FileLocation}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class FileLocationActionProvider extends LazyActionProvider {

	private final FileLocation _file;

	/**
	 * Creates a new {@link FileLocationActionProvider}.
	 * <p>
	 * The {@link File} itself cannot be given to delay errors until the {@link ApplicationAction}
	 * is requested, i.e. until the test is executed. Otherwise, a mistake on {@link File}
	 * instantiation would kill the whole test tree creation.
	 * </p>
	 * 
	 * @param file
	 *        The location of the file. Must not be <code>null</code>.
	 */
	FileLocationActionProvider(FileLocation file) {
		_file = file;
	}

	@Override
	protected ApplicationAction createAction() {
		BinaryContent binaryContent = FileBasedBinaryContent.createBinaryContent(_file.get());
		try {
			return ActionReader.INSTANCE.readAction(binaryContent);
		} catch (ConfigurationException exception) {
			String message = "Parsing the test script failed! Script filename: '" + _file.get().getAbsolutePath() + "'";
			throw new RuntimeException(message, exception);
		}
	}
}