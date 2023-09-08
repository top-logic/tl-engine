/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.stacktrace.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Algorithm encoding and decoding line numbers on files with hidden (stripped) contents.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LineNumberEncoding {

	private static final Pattern RANGE_PATTERN = Pattern.compile("\\[(\\d+), (\\d+)\\]");

	private int _lineCnt;
	private IntRanges _excluded;
	
	private int[] _encodedLine;
	
	private int[] _decodedLine;
	private int[] _excludedLine;

	/** 
	 * Creates a {@link LineNumberEncoding}.
	 *
	 */
	public LineNumberEncoding(int lineCnt, IntRanges excluded) {
		_lineCnt = lineCnt;
		_excluded = excluded;
	}

	/**
	 * Must be called before invoking {@link #encode(int)}.
	 */
	public void initEncoder() {
		_encodedLine = new int[_lineCnt + 1];

		int newLine = 1;
		int newExcluded = _lineCnt + 1;
		int line = 1;
		for (Range next : _excluded) {
			int start = next.getStart();
			int stop = next.getStop();
			
			while (line < start) {
				_encodedLine[line++] = newLine++;
			}
			for (int excludedLine = start; excludedLine < stop; excludedLine++) {
				_encodedLine[line++] = newExcluded++;
			}
		}
		while (line <= _lineCnt) {
			_encodedLine[line++] = newLine++;
		}
	}
	
	/**
	 * Must be called before invoking {@link #decode(int)}.
	 */
	public void initDecoder() {
		int cntIncluded = 0;
		int cntExcluded = 0;
	
		{
			int last = 1;
			for (Range next : _excluded) {
				int start = next.getStart();
				int stop = next.getStop();
	
				cntIncluded += start - last;
				cntExcluded += stop - start;
				
				last = stop;
			}
			cntIncluded += _lineCnt - last + 1;
		}
		_decodedLine = new int[cntIncluded + 1];
		_excludedLine = new int[cntExcluded + 1];
		
		int line = 1;
		int encodedLine = 1;
		int hiddenLine = 1;
		for (Range next : _excluded) {
			int start = next.getStart();
			int stop = next.getStop();
			
			while (line < start) {
				_decodedLine[encodedLine++] = line++;
			}
			for (int excludedLine = start; excludedLine < stop; excludedLine++) {
				_excludedLine[hiddenLine++] = line++;
			}
		}
		while (line <= _lineCnt) {
			_decodedLine[encodedLine++] = line++;
		}
	}

	/**
	 * Encodes a line.
	 * 
	 * @see #initEncoder()
	 */
	public int encode(int line) {
		return _encodedLine[line];
	}

	/**
	 * Decodes a line.
	 * 
	 * @see #initDecoder()
	 */
	public int decode(int encodedLine) {
		if (encodedLine > _lineCnt) {
			return _excludedLine[encodedLine - _lineCnt];
		} else {
			return _decodedLine[encodedLine];
		}
	}

	/**
	 * Parses a {@link LineNumberEncoding} from its encoded form.
	 */
	public static LineNumberEncoding fromString(String encoded) {
		// e.g. value is something like "89, {[31, 31], [34, 34], [50, 50]}"
	
		int lineCntSepIndex = encoded.indexOf(',');
		int lineCnt = Integer.parseInt(encoded.substring(0, lineCntSepIndex));
		Matcher matcher = RANGE_PATTERN.matcher(encoded);
		matcher.region(lineCntSepIndex + 1, encoded.length());
	
		IntRanges ranges = new IntRanges();
		while (matcher.find()) {
			int from = Integer.parseInt(matcher.group(1));
			int to = Integer.parseInt(matcher.group(2));
			ranges.addRange(from, to + 1);
		}
	
		return new LineNumberEncoding(lineCnt, ranges);
	}
}
