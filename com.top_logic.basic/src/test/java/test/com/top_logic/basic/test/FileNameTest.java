/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.test;

import static com.top_logic.basic.io.FileUtilities.*;
import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.regex.Pattern;

import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.io.file.FileNameRule;

/**
 * Represents a combination of a file and a {@link FileNameRule}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@DeactivatedTest("This is a helper for TestFileNames, not an independent test.")
public class FileNameTest {

	private final FileNameRule _rule;

	private final Path _file;

	/**
	 * @param rule
	 *        The rule to check. Is not allowed to be null.
	 * @param file
	 *        The path of the file. It has to be relative to the eclipse project. That makes it
	 *        possible to formulate path-dependent rules. Is not allowed to be null.
	 */
	public FileNameTest(FileNameRule rule, Path file) {
		_rule = requireNonNull(rule);
		_file = requireNonNull(file);
	}

	/** Checks whether the file complies with the rule. */
	public boolean compliesWithRule() {
		return matches(getFile(), getRule().getRegex());
	}

	/** Checks whether the file is exempted from this rule. */
	public boolean isException() {
		for (Pattern regex : getRule().getExceptions()) {
			if (matches(getFile(), regex)) {
				return true;
			}
		}
		return false;
	}

	private boolean matches(Path file, Pattern regex) {
		return regex.matcher(toStringWithSlashes(file)).matches();
	}

	/**
	 * Writes the path with "/" as separator.
	 * <p>
	 * This is used to guarantee the rules a non-platform dependent format.
	 * </p>
	 */
	private CharSequence toStringWithSlashes(Path file) {
		return String.join("/", getNameParts(file));
	}

	/** Creates the error message for the case that this file does not comply with the rule. */
	public String createErrorMessage() {
		return "This file name is not allowed: '" + getFile().getFileName() + "'. It is in violation of the rule "
			+ getRule().getName() + ". Regex: \"" + getRule().getRegex() + "\". Path: " + getFile();
	}

	private FileNameRule getRule() {
		return _rule;
	}

	private Path getFile() {
		return _file;
	}

}
