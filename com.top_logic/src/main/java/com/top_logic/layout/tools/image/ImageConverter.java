/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.xml.sax.SAXException;

import com.top_logic.basic.Main;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;

/**
 * Tool for bulk-converting GIF to PNG images.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImageConverter extends Main implements FileHandler {

	public static void main(String[] args) throws Exception {
		new ImageConverter().runMain(args);
	}

	private List<Pattern> _excludePatterns = new ArrayList<>();

	@Override
	protected int longOption(String option, String[] args, int i) {
		if ("excludes".equals(option)) {
			String simplePattern = args[i];
			_excludePatterns.add(compileGlobPattern(simplePattern));
			return i + 1;
		}
		return super.longOption(option, args, i);
	}

	/**
	 * Creates a {@link Pattern} from a simple file name glob expression.
	 * 
	 * @param glob
	 *        A pattern with the only specials <code>*</code> and <code>**</code>, where
	 *        <code>*</code> matches a file name (excluding path separators) and <code>**</code>
	 *        matching any text including path separator characters.
	 * @return {@link Pattern} of paths matching the given glob expression.
	 */
	private Pattern compileGlobPattern(String glob) {
		StringBuilder patternSrc = new StringBuilder();
		int start = 0;
		while (true) {
			int index = glob.indexOf("**", start);
			String part;
			if (index >= 0) {
				part = glob.substring(start, index);
			} else {
				part = glob.substring(start);
			}
			patternSrc.append(part.replace("*", "[^/\\\\]*").replaceAll("[/\\\\]",
				Matcher.quoteReplacement("[/\\\\]")));
			if (index < 0) {
				break;
			}
			patternSrc.append(".*");
			start = index + "**".length();
		}
		Pattern pattern = Pattern.compile(patternSrc.toString());
		return pattern;
	}

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		FileUtil.handleFile(args[i], this);
		return i + 1;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		// Actions are performed while parameter parsing.
	}

	@Override
	public void handleFile(String fileName) throws IOException, SAXException {
		File file = new File(fileName);
		if (file.isDirectory()) {
			scan(file);
		} else {
			update(file);
		}
	}

	private void scan(File dir) throws SAXException, IOException {
		if (matchesExclude(dir)) {
			System.out.println("Skipping excluded dir: " + dir.getPath());
			return;
		}
		File[] contents = dir.listFiles();
		if (contents == null) {
			System.err.println("Cannot access directory: " + dir.getPath());
			return;
		}
		for (File content : contents) {
			if (content.getName().startsWith(".")) {
				continue;
			}
			if (content.isDirectory()) {
				scan(content);
			} else {
				if (!content.getName().endsWith(".gif")) {
					continue;
				}
				update(content);
			}
		}
	}

	private void update(File file) throws IOException {
		if (matchesExclude(file)) {
			System.out.println("Skipping excluded file: " + file.getPath());
			return;
		}

		File output = new File(file.getParentFile(), convertFileName(file.getName()));
		if (output.exists()) {
			System.err.println("Not updating existing file: " + file.getPath());
			return;
		}

		System.out.println("Converting: " + file.getPath());

		BufferedImage image = ImageIO.read(file);
		ImageIO.write(image, "png", output);

		file.delete();
	}

	private boolean matchesExclude(File file) {
		String path = file.getPath();
		for (Pattern pattern : _excludePatterns) {
			if (pattern.matcher(path).matches()) {
				return true;
			}
		}
		return false;
	}

	private String convertFileName(String name) {
		return name.substring(0, name.lastIndexOf('.')) + ".png";
	}

}
