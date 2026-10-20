/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

/**
 * Reciprocal Rank Fusion: two rankings become one by the sum of {@code weight / (k + rank)}.
 *
 * <p>
 * RRF looks at ranks only, so the BM25 score of the word side and the cosine of the meaning side
 * need no common scale. The default {@code k = 60} is the one of Cormack, Clarke and Büttcher
 * (SIGIR 2009). A weight below 1 lets one side count less, e.g. the word side for a German query
 * against English text, where words find little.
 * </p>
 *
 * @param k
 *        The rank constant; a larger value makes the ranks below the top count more.
 * @param wordWeight
 *        The weight of the word ranking.
 * @param meaningWeight
 *        The weight of the meaning ranking.
 */
public record Rrf(int k, double wordWeight, double meaningWeight) {

	/** Equal weights, {@code k = 60}. */
	public static final Rrf DEFAULT = new Rrf(60, 1, 1);

	/**
	 * The contribution of a word rank.
	 *
	 * @param rank
	 *        The rank from 1, or 0 when the word side did not find the object.
	 */
	public double word(int rank) {
		return rank == 0 ? 0 : wordWeight / (k + rank);
	}

	/**
	 * The contribution of a meaning rank.
	 *
	 * @param rank
	 *        The rank from 1, or 0 when the meaning side did not find the object.
	 */
	public double meaning(int rank) {
		return rank == 0 ? 0 : meaningWeight / (k + rank);
	}

	/**
	 * The fused score of an object with the given ranks.
	 */
	public double score(int wordRank, int meaningRank) {
		return word(wordRank) + meaning(meaningRank);
	}

}
