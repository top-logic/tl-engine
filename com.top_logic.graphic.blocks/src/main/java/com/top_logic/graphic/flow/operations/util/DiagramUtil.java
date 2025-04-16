/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class DiagramUtil {

	public static double[] doubleArray(List<Double> dashes) {
		double[] result = new double[dashes.size()];
		for (int n = 0, cnt = dashes.size(); n < cnt; n++) {
			result[n] = dashes.get(n);
		}
		return result;
	}

	public static List<Double> doubleList(int size) {
		List<Double> result = new ArrayList<>(size);
		for (int n = 0; n < size; n++) {
			result.add(Double.valueOf(0));
		}
		return result;
	}

}
