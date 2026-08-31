/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

import java.util.Locale;
import java.util.Objects;

import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * What a model can perceive, according to the model catalogue.
 *
 * <p>
 * A tool that returns content the model has to look at asks this before attaching it: showing a
 * 5 MB image to a text-only model wastes the call and produces a provider error instead of an
 * answer. Purely a lookup - the delivery of the content is the tool result's own business.
 * </p>
 *
 * @param model
 *        The model the run talks to.
 * @param modelInfo
 *        The model's catalogue entry, or {@code null} when the catalogue does not know it.
 */
public record ModelPerception(String model, ModelInfo modelInfo) {

	/** */
	public ModelPerception {
		Objects.requireNonNull(model, "Model must not be null.");
	}

	/**
	 * Looks up the given model in the catalogue via {@link LLMService}.
	 *
	 * <p>
	 * When the service is not running or does not know the model, capability answers degrade to
	 * {@link PerceptionSupport#UNKNOWN} rather than failing.
	 * </p>
	 *
	 * @param model
	 *        The model the run talks to, or {@code null} when the caller runs outside an agent loop.
	 * @return The lookup, or {@code null} when no model is known.
	 */
	public static ModelPerception forModel(String model) {
		if (model == null || model.isBlank()) {
			return null;
		}
		LLMService service = LLMService.getInstance();
		return new ModelPerception(model, service == null ? null : service.getModel(model));
	}

	/**
	 * Whether the model can perceive the given modality.
	 *
	 * <p>
	 * Prefers the catalogue's declared
	 * {@link com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelModalities#input() input
	 * modalities}. When a catalogue entry exists but declares no modalities, falls back to the
	 * coarser
	 * {@link com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCapabilities#attachment()
	 * attachment} flag, which says that the model takes files at all but not which kinds.
	 * </p>
	 *
	 * <p>
	 * <b>An input list without {@code pdf} is not evidence against PDF.</b> The catalogue's lists
	 * are demonstrably incomplete for that one modality - in the shipped snapshot {@code gpt-4o}
	 * lists {@code pdf} while {@code gpt-4o-2024-08-06}, the dated pin of the same model, does not,
	 * and most of the GPT-5 family lists only {@code text} and {@code image} although the API takes
	 * file inputs. Concluding {@code UNSUPPORTED} from such a list disables the PDF path and tells
	 * the model its model cannot read PDFs, which is worse than either alternative: it is wrong,
	 * and it is unactionable. So PDF falls back to the {@code attachment} flag, exactly as a
	 * missing modality list does. An input list that omits {@code image} is taken at face value -
	 * that one the catalogue gets right, since a text-only model is exactly what it is describing.
	 * </p>
	 */
	public PerceptionSupport support(Modality modality) {
		if (modelInfo == null || modelInfo.capabilities() == null) {
			return PerceptionSupport.UNKNOWN;
		}
		ModelInfo.ModelCapabilities capabilities = modelInfo.capabilities();
		ModelInfo.ModelModalities modalities = capabilities.modalities();
		if (modalities != null && !modalities.input().isEmpty()) {
			for (String input : modalities.input()) {
				if (input != null && input.toLowerCase(Locale.ROOT).equals(modality.catalogName())) {
					return PerceptionSupport.SUPPORTED;
				}
			}
			if (modality != Modality.PDF) {
				return PerceptionSupport.UNSUPPORTED;
			}
		}
		return capabilities.attachment() ? PerceptionSupport.SUPPORTED : PerceptionSupport.UNSUPPORTED;
	}
}
