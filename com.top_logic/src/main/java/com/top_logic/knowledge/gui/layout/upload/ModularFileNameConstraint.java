/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static java.util.Collections.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.constraints.AbstractConstraint;

/**
 * A {@link Constraint} for file names whose checks can be individually enabled.
 * <p>
 * The settings which are probably the same for the whole application can be configured.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ModularFileNameConstraint extends AbstractConstraint {

	/** {@link ConfigurationItem} for the {@link ModularFileNameConstraint}. */
	public interface Config extends ConfigurationItem {

		/** The default value for {@link #getMaxFileNameSize()} */
		int DEFAULT_MAX_FILE_NAME_SIZE = 100;

		/** The default value for {@link #getAllowWindowsIncompatibleCharacters}. */
		boolean DEFAULT_ALLOW_WINDOWS_INCOMPATIBLE_CHARACTERS = false;

		/** Property name of {@link #getMaxFileNameSize()}. */
		String MAX_FILE_NAME_SIZE = "max-file-name-size";

		/** Property name of {@link #getAllowWindowsIncompatibleCharacters()}. */
		String ALLOW_WINDOWS_INCOMPATIBLE_CHARACTERS = "allow-windows-incompatible-characters";

		/**
		 * The maximum allowed file name size}.
		 * <p>
		 * The default value is {@value #DEFAULT_MAX_FILE_NAME_SIZE}.
		 * </p>
		 * <p>
		 * The limit is arbitrary. It should be a round number to avoid confusing the user. The only
		 * real constraint here is, that some Windows installations have problems with paths longer
		 * than approximately 255 characters. But that is the whole path, not just the file name.
		 * This number is therefore lower. But it is not too low, as it should not constraint the
		 * user needlessly: This application is probably running on a Linux server, not Windows.
		 * </p>
		 * <p>
		 * This is just the default value, which is used if the value is not passed in the
		 * constructor.
		 * </p>
		 */
		@IntDefault(DEFAULT_MAX_FILE_NAME_SIZE)
		@Name(MAX_FILE_NAME_SIZE)
		int getMaxFileNameSize();

		/**
		 * Whether characters which Windows does not support in file names should be allowed.
		 * <p>
		 * The default value is {@value #DEFAULT_ALLOW_WINDOWS_INCOMPATIBLE_CHARACTERS}.
		 * </p>
		 */
		@BooleanDefault(DEFAULT_ALLOW_WINDOWS_INCOMPATIBLE_CHARACTERS)
		@Name(ALLOW_WINDOWS_INCOMPATIBLE_CHARACTERS)
		boolean getAllowWindowsIncompatibleCharacters();

	}

	private static final String WINDOWS_INCOMPATIBLE_CHARACTERS_EXCEPT_SLASHES = ":*?\"<>|";

	private static final Pattern FORBIDDEN_CHARACTERS_PATTERN =
		Pattern.compile("[" + WINDOWS_INCOMPATIBLE_CHARACTERS_EXCEPT_SLASHES + "]");

	private final int _maxSize;

	private final boolean _allowFolderTraversal;

	private final boolean _allowSubFolder;

	private final boolean _allowWindowsIncompatibleCharacters;

	private final Collection<String> _allowedEndings;

	/**
	 * Creates a {@link ModularFileNameConstraint} which does not allow folder traversal, sub
	 * folders or characters that windows does not allow.
	 */
	public ModularFileNameConstraint(String... allowedEndings) {
		this(Arrays.asList(allowedEndings));
	}

	/**
	 * Creates a {@link ModularFileNameConstraint} which does not allow folder traversal or sub
	 * folders and which uses the configured default for the maximum file size and whether windows
	 * incompatible characters are allowed.
	 */
	public ModularFileNameConstraint(Collection<String> allowedEndings) {
		this(getDefaultMaximumFileSize(), false, false, getAllowWindowsIncompatibleCharacters(), allowedEndings);
	}

	/**
	 * Creates a {@link ModularFileNameConstraint}.
	 * 
	 * @param maxSize
	 *        The maximum size in {@link Character}s for the file name. The default is
	 *        {@link #getDefaultMaximumFileSize()}. To disable this limit, use for example
	 *        {@link Integer#MAX_VALUE}.
	 * @param allowFolderTraversal
	 *        Whether this is a path which is allowed to navigate out of the base directory.
	 * @param allowSubFolder
	 *        Whether this is a path, not just a file name.
	 * @param allowWindowsIncompatibleCharacters
	 *        Does not check for '/' or '\' as those are already allowed or forbidden with the
	 *        parameter <code>allowSubFolder</code>.
	 * @param allowedEndings
	 *        The endings the file is allowed to have. Null or an empty {@link Collection} mean,
	 *        every ending is allowed.
	 */
	public ModularFileNameConstraint(int maxSize, boolean allowFolderTraversal, boolean allowSubFolder,
			boolean allowWindowsIncompatibleCharacters, Collection<String> allowedEndings) {
		if (maxSize < 1) {
			throw new IllegalArgumentException("The maximum file name size has to be larger than 0.");
		}
		_maxSize = maxSize;
		_allowFolderTraversal = allowFolderTraversal;
		_allowSubFolder = allowSubFolder;
		_allowWindowsIncompatibleCharacters = allowWindowsIncompatibleCharacters;
		_allowedEndings = allowedEndings == null ? emptySet() : list(allowedEndings);
	}

	@Override
	public boolean check(Object value) throws CheckException {
		String fileName = (String) value;
		if (StringServices.isEmpty(fileName)) {
			throw new CheckException(I18NConstants.FORBIDDEN_FILE_NAME_EMPTY);
		}
		if (fileName.length() > _maxSize) {
			throw new CheckException(I18NConstants.FORBIDDEN_FILE_NAME_TOO_LONG__NAME_SIZE_MAX
				.fill(fileName, fileName.length(), _maxSize));
		}
		if ((!_allowFolderTraversal) && isFolderTraversal(fileName)) {
			throw new CheckException(I18NConstants.FORBIDDEN_FILE_NAME_FOLDER_TRAVERSAL__NAME.fill(fileName));
		}
		if (!(_allowSubFolder) && isSubFolder(fileName)) {
			throw new CheckException(I18NConstants.FORBIDDEN_FILE_NAME_SUB_FOLDER__NAME.fill(fileName));
		}
		if (!(_allowWindowsIncompatibleCharacters) && containsWindowsIncompatibleCharacters(fileName)) {
			throw new CheckException(I18NConstants.FORBIDDEN_FILE_NAME_WINDOWS_INCOMPATIBLE_CHARACTERS__NAME_CHARACTERS
				.fill(fileName, WINDOWS_INCOMPATIBLE_CHARACTERS_EXCEPT_SLASHES));
		}
		if ((!_allowedEndings.isEmpty()) && hasForbiddenEnding(fileName)) {
			String allowedEndings = String.join(", ", _allowedEndings);
			throw new CheckException(I18NConstants.FORBIDDEN_FILE_NAME_WRONG_ENDING__NAME_ENDINGS
				.fill(fileName, allowedEndings));
		}
		return true;
	}

	private boolean isFolderTraversal(String fileName) {
		return fileName.startsWith("../")
			|| fileName.startsWith("..\\")
			|| fileName.contains("/../")
			|| fileName.contains("\\..\\");
	}

	private boolean isSubFolder(String fileName) {
		return fileName.contains("/")
			|| fileName.contains("\\");
	}

	private boolean containsWindowsIncompatibleCharacters(String fileName) {
		return FORBIDDEN_CHARACTERS_PATTERN.matcher(fileName).find();
	}

	private boolean hasForbiddenEnding(String fileName) {
		return _allowedEndings.stream().noneMatch(fileName::endsWith);
	}

	/** @see Config#getMaxFileNameSize() */
	public static int getDefaultMaximumFileSize() {
		return getConfig().getMaxFileNameSize();
	}

	/** @see Config#getAllowWindowsIncompatibleCharacters() */
	public static boolean getAllowWindowsIncompatibleCharacters() {
		return getConfig().getAllowWindowsIncompatibleCharacters();
	}

	/** @see Config */
	public static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

}
