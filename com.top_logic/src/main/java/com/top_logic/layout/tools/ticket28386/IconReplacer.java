/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket28386;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.layout.tools.DescendingRewriter;

/**
 * Replacement of old image references with new ones.
 */
public class IconReplacer extends DescendingRewriter {

	static final String[][] replacements = {
		{ "bi bi-alarm-fill", "fa-solid fa-stopwatch" },
		{ "bi bi-arrow-clockwise", "fa-solid fa-arrow-rotate-right" },
		{ "bi bi-arrow-counterclockwise", "fa-solid fa-arrow-rotate-left" },
		{ "bi bi-arrow-down", "fa-solid fa-arrow-down" },
		{ "bi bi-arrow-down-up", "fa-solid fa-arrow-up" },
		{ "bi bi-arrow-left", "fa-solid fa-arrow-left" },
		{ "bi bi-arrow-left-right", "fa-solid fa-arrow-right-arrow-left" },
		{ "bi bi-arrow-repeat", "fa-solid fa-arrows-rotate" },
		{ "bi bi-arrow-right", "fa-solid fa-arrow-right" },
		{ "bi bi-arrow-right-square-fill", "fa-solid fa-square-caret-right" },
		{ "bi bi-arrows", "fa-solid fa-arrows-left-right" },
		{ "bi bi-arrows-angle-contract", "fa-solid fa-down-left-and-up-right-to-center" },
		{ "bi bi-arrows-angle-expand", "fa-solid fa-up-right-and-down-left-from-center" },
		{ "bi bi-arrows-expand-vertical", "fa-solid fa-arrows-up-down" },
		{ "bi bi-arrow-up", "fa-solid fa-arrow-up" },
		{ "bi bi-backspace-fill", "fa-solid fa-delete-left" },
		{ "bi bi-bar-chart-line-fill", "fa-solid fa-chart-column" },
		{ "bi bi-bar-chart-steps", "fa-solid fa-chart-gantt" },
		{ "bi bi-binoculars-fill", "fa-solid fa-binoculars" },
		{ "bi bi bi-square", "fa-regular fa-square" },
		{ "bi bi bi-trash3", "fa-solid fa-trash" },
		{ "bi bi-book", "fa-solid fa-book-open" },
		{ "bi bi-bookmark-fill", "fa-solid fa-bookmark" },
		{ "bi bi-box-arrow-in-right", "fa-solid fa-arrow-right-to-bracket" },
		{ "bi bi-box-arrow-right", "fa-solid fa-arrow-right-from-bracket" },
		{ "bi bi-box-arrow-up-right", "fa-solid fa-up-right-from-square" },
		{ "bi bi-braces", "fa-solid fa-code" },
		{ "bi bi-briefcase-fill", "fa-solid fa-briefcase" },
		{ "bi bi-brush-fill", "fa-solid fa-paintbrush" },
		{ "bi bi-brush-fill red", "fa-solid fa-paintbrush red" },
		{ "bi bi-bug-fill", "fa-solid fa-bug" },
		{ "bi bi-buildings-fill", "fa-solid fa-shop" },
		{ "bi bi-bullseye", "fa-solid fa-bullseye" },
		{ "bi bi-calendar", "fa-solid fa-calendar" },
		{ "bi bi-calendar-check", "fa-solid fa-calendar-check" },
		{ "bi bi-caret-up-fill", "fa-solid fa-caret-up" },
		{ "bi bi-chat-left-quote-fill", "fa-solid fa-message" },
		{ "bi bi-check", "fa-solid fa-check" },
		{ "bi bi-check2-square", "fa-solid fa-square-check" },
		{ "bi bi-check-all", "fa-solid fa-check-double" },
		{ "bi bi-check-circle-fill green", "fa-solid fa-circle-check green" },
		{ "bi bi-check-lg", "fa-solid fa-check" },
		{ "bi bi-check-square-fill", "fa-solid fa-square-check" },
		{ "bi bi-chevron-double-down", "fa-solid fa-angles-down" },
		{ "bi bi-chevron-double-left", "fa-solid fa-angles-left" },
		{ "bi bi-chevron-double-right", "fa-solid fa-angles-right" },
		{ "bi bi-chevron-double-up", "fa-solid fa-angles-up" },
		{ "bi bi-chevron-down", "fa-solid fa-chevron-down" },
		{ "bi bi-chevron-left", "fa-solid fa-chevron-left" },
		{ "bi bi-chevron-right", "fa-solid fa-chevron-right" },
		{ "bi bi-chevron-up", "fa-solid fa-chevron-up" },
		{ "bi bi-circle-fill", "fa-solid fa-circle" },
		{ "bi bi-circle-fill green", "fa-solid fa-circle green" },
		{ "bi bi-circle-fill yellow", "fa-solid fa-circle yellow" },
		{ "bi bi-clipboard-fill", "fa-solid fa-clipboard" },
		{ "bi bi-clipboard-minus-fill", "fa-solid fa-paste" },
		{ "bi bi-clipboard-plus-fill", "fa-solid fa-copy" },
		{ "bi bi-clock", "fa-solid fa-clock" },
		{ "bi bi-clock-fill", "fa-solid fa-clock" },
		{ "bi bi-clock-history", "fa-solid fa-clock-rotate-left" },
		{ "bi bi-code-slash", "fa-solid fa-code" },
		{ "bi bi-collection-fill", "fa-solid fa-layer-group" },
		{ "bi bi-columns", "fa-solid fa-table-columns" },
		{ "bi bi-dash", "fa-solid fa-minus" },
		{ "bi bi-dash-lg", "fa-solid fa-minus" },
		{ "bi bi-dash-square-fill", "fa-solid fa-square-minus" },
		{ "bi bi-database-down", "fa-solid fa-circle-down" },
		{ "bi bi-diagram-3-fill", "fa-solid fa-diagram-project" },
		{ "bi bi-download", "fa-solid fa-download" },
		{ "bi bi-easel-fill", "fa-solid fa-repeat" },
		{ "bi bi-exclamation-triangle-fill", "fa-solid fa-triangle-exclamation" },
		{ "bi bi-file-arrow-down-fill", "fa-solid fa-file-arrow-down" },
		{ "bi bi-file-arrow-up-fill", "fa-solid fa-file-arrow-up" },
		{ "bi bi-file-earmark-diff-fill", "fa-solid fa-rotate" },
		{ "bi bi-file-earmark-fill", "fa-solid fa-file" },
		{ "bi bi-file-earmark-plus", "fa-solid fa-file-circle-plus" },
		{ "bi bi-file-earmark-plus-fill", "fa-solid fa-file-circle-plus" },
		{ "bi bi-file-earmark-text-fill", "fa-solid fa-file-lines" },
		{ "bi bi-file-plus-fill", "fa-solid fa-file-circle-plus" },
		{ "bi bi-files", "fa-solid fa-equals" },
		{ "bi bi-file-spreadsheet-fill", "fa-solid fa-table" },
		{ "bi bi-file-text-fill", "fa-solid fa-file-lines" },
		{ "bi bi-filetype-pdf", "fa-solid fa-file-pdf" },
		{ "bi bi-filetype-ppt", "fa-solid fa-file-powerpoint" },
		{ "bi bi-filetype-xml", "fa-solid fa-file-code" },
		{ "bi bi-file-word-fill", "fa-solid fa-file-word" },
		{ "bi bi-file-zip-fill", "fa-solid fa-file-zipper" },
		{ "bi bi-floppy2-fill", "fa-solid fa-floppy-disk" },
		{ "bi bi-folder2-open", "fa-solid fa-folder-open" },
		{ "bi bi-folder-plus", "fa-solid fa-folder-plus" },
		{ "bi bi-fonts", "fa-solid fa-font" },
		{ "bi bi-forward-fill", "fa-solid fa-forward" },
		{ "bi bi-fullscreen", "fa-solid fa-expand" },
		{ "bi bi-fullscreen-exit", "fa-solid fa-compress" },
		{ "bi bi-funnel-fill", "fa-solid fa-filter" },
		{ "bi bi-funnel-fill tablefilter", "fa-solid fa-filter tablefilter" },
		{ "bi bi-gear-fill", "fa-solid fa-gear" },
		{ "bi bi-globe", "fa-solid fa-globe" },
		{ "bi bi-hash", "fa-solid fa-hashtag" },
		{ "bi bi-hdd-network-fill", "fa-solid fa-network-wired" },
		{ "bi bi-hdd-rack-fill", "fa-solid fa-server" },
		{ "bi bi-house", "fa-solid fa-house" },
		{ "bi bi-house-fill", "fa-solid fa-house" },
		{ "bi bi-image", "fa-solid fa-image" },
		{ "bi bi-info-circle-fill", "fa-solid fa-circle-info" },
		{ "bi bi-info-circle-fill yellow", "fa-solid fa-circle-info yellow" },
		{ "bi bi-info-square", "fa-solid fa-circle-info" },
		{ "bi bi-keyboard-fill", "fa-solid fa-keyboard" },
		{ "bi bi-layout-three-columns", "fa-solid fa-table-columns" },
		{ "bi bi-layout-wtf", "fa-solid fa-chart-simple" },
		{ "bi bi-link-45deg", "fa-solid fa-link" },
		{ "bi bi-list", "fa-solid fa-bars" },
		{ "bi bi-lock-fill", "fa-solid fa-lock" },
		{ "bi bi-menu-button-wide", "fa-solid fa-rectangle-list" },
		{ "bi bi-pause", "fa-solid fa-pause" },
		{ "bi bi-pause-fill red", "fa-solid fa-pause red" },
		{ "bi bi-pause-fill yellow", "fa-solid fa-pause yellow" },
		{ "bi bi-pencil", "fa-solid fa-pen" },
		{ "bi bi-pencil-fill", "fa-solid fa-pen" },
		{ "bi bi-pencil-square", "fa-solid fa-square-pen" },
		{ "bi bi-people-fill", "fa-solid fa-user-group" },
		{ "bi bi-person-badge-fill", "fa-solid fa-id-badge" },
		{ "bi bi-person-fill", "fa-solid fa-user" },
		{ "bi bi-person-fill-gear", "fa-solid fa-user-gear" },
		{ "bi bi-person-fill-lock", "fa-solid fa-user-lock" },
		{ "bi bi-pie-chart-fill", "fa-solid fa-chart-pie" },
		{ "bi bi-play-fill", "fa-solid fa-play" },
		{ "bi bi-play-fill green", "fa-solid fa-play green" },
		{ "bi bi-plus", "fa-solid fa-plus" },
		{ "bi bi-plus-circle", "fa-solid fa-circle-plus" },
		{ "bi bi-plus-circle-fill", "fa-solid fa-circle-plus" },
		{ "bi bi-plus-lg", "fa-solid fa-plus" },
		{ "bi bi-plus-square", "fa-solid fa-square-plus" },
		{ "bi bi-qr-code", "fa-solid fa-qrcode" },
		{ "bi bi-question-circle-fill", "fa-solid fa-circle-question" },
		{ "bi bi-question-circle-fill yellow", "fa-solid fa-circle-question yellow" },
		{ "bi bi-question-lg", "fa-solid fa-question" },
		{ "bi bi-question-square-fill", "fa-solid fa-notdef" },
		{ "bi bi-record-fill red", "fa-solid fa-circle red" },
		{ "bi bi-save2-fill", "fa-solid fa-floppy-disk" },
		{ "bi bi-save-fill", "fa-solid fa-floppy-disk" },
		{ "bi bi-scissors", "fa-solid fa-scissors" },
		{ "bi bi-search", "fa-solid fa-magnifying-glass" },
		{ "bi bi-search search", "fa-solid fa-magnifying-glass search" },
		{ "bi bi-shield-fill-check", "fa-solid fa-shield" },
		{ "bi bi-square", "fa-solid fa-square" },
		{ "bi bi-stop-circle-fill", "fa-solid fa-circle-stop" },
		{ "bi bi-stop-fill", "fa-solid fa-stop" },
		{ "bi bi-stop-fill red", "fa-solid fa-stop red" },
		{ "bi bi-table", "fa-solid fa-tablet" },
		{ "bi bi-terminal-fill", "fa-solid fa-terminal" },
		{ "bi bi-terminal-fill red", "fa-solid fa-terminal red" },
		{ "bi bi-three-dots-vertical", "fa-solid fa-ellipsis-vertical" },
		{ "bi bi-translate", "fa-solid fa-language" },
		{ "bi bi-trash", "fa-solid fa-trash" },
		{ "bi bi-trash3", "fa-solid fa-trash" },
		{ "bi bi-trash3-fill", "fa-solid fa-trash" },
		{ "bi bi-unlock-fill", "fa-solid fa-unlock" },
		{ "bi bi-upload", "fa-solid fa-upload" },
		{ "bi bi-window", "fa-solid fa-message" },
		{ "bi bi-window-split", "fa-solid fa-table-columns" },
		{ "bi bi-x-circle-fill red", "fa-solid fa-circle-xmark red" },
		{ "bi bi-x-lg", "fa-solid fa-xmark" },
		{ "bi bi-x-octagon-fill", "fa-solid fa-triangle-exclamation" },
		{ "bi bi-x-octagon-fill red", "fa-solid fa-triangle-exclamation red" },
		{ "bi bi-x-square-fill", "fa-solid fa-square-xmark" },
	};

	private Pattern _pattern;

	private Map<String, String> _replacements;

	/**
	 * Creates a {@link IconReplacer}.
	 */
	public IconReplacer() {
		StringBuilder pattern = new StringBuilder();
		pattern.append("\\b(");

		_replacements = new HashMap<>();
		boolean first = true;
		for (String[] replacement : replacements) {
			_replacements.put(replacement[0], replacement[1]);
			if (first) {
				first = false;
			} else {
				pattern.append("|");
			}
			pattern.append(Pattern.quote(replacement[0]));
		}
		pattern.append(")\\b(?!X-)");

		_pattern = Pattern.compile(pattern.toString());
	}

	@Override
	protected void handleFile(File file) throws IOException {
		String contents = StreamUtilities.readAllFromStream(new FileInputStream(file));

		Matcher matcher = _pattern.matcher(contents);
		if (matcher.find()) {
			StringBuffer result = new StringBuffer();
			matcher.appendReplacement(result, _replacements.get(matcher.group(1)));
			while (matcher.find()) {
				matcher.appendReplacement(result, _replacements.get(matcher.group(1)));
			}
			matcher.appendTail(result);

			System.out.println("Updating: " + file.getAbsolutePath());
			try (FileOutputStream out = new FileOutputStream(file)) {
				try (OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
					w.write(result.toString());
				}
			}
		}
	}

	@Override
	protected boolean matches(File file) {
		return file.getName().endsWith(".xml") || file.getName().endsWith(".css") || file.getName().endsWith(".jsp")
			|| (file.getName().endsWith(".java") && !file.getName().equals("IconReplacer.java"));
	}

	public static void main(String[] args) throws Exception {
		new IconReplacer().runMain(args);
	}
}
