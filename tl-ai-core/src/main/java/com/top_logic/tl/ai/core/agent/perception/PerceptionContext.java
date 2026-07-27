/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.perception;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * Per-run perception state: which modalities the running model can take, and which content parts
 * tools have queued for the next turn.
 *
 * <p>
 * One instance exists per {@link com.top_logic.tl.ai.core.agent.AgentLoop} run and is published to
 * tools via {@link PerceptionBinding}. The agent loop drains the queue at the start of every turn
 * and appends the collected parts as a user message, so the model perceives them on its next call.
 * </p>
 *
 * <p>
 * Instances are thread-safe: {@code READ} tools run in parallel worker threads and may offer
 * content concurrently.
 * </p>
 */
public final class PerceptionContext {

	private final String _model;

	private final ModelInfo _modelInfo;

	private final List<PerceivedContent> _pending = new ArrayList<>();

	/**
	 * Creates a context for the given model, resolving its catalogue entry via {@link LLMService}.
	 *
	 * <p>
	 * When the service is not running or does not know the model, capability answers degrade to
	 * {@link PerceptionSupport#UNKNOWN} rather than failing.
	 * </p>
	 *
	 * @param model
	 *        The model the run talks to.
	 */
	public static PerceptionContext forModel(String model) {
		LLMService service = LLMService.getInstance();
		return new PerceptionContext(model, service == null ? null : service.getModel(model));
	}

	/**
	 * Creates a context with an explicitly supplied catalogue entry.
	 *
	 * @param model
	 *        The model the run talks to.
	 * @param modelInfo
	 *        The model's catalogue entry, or {@code null} when the catalogue does not know it.
	 */
	public PerceptionContext(String model, ModelInfo modelInfo) {
		_model = Objects.requireNonNull(model, "Model must not be null.");
		_modelInfo = modelInfo;
	}

	/** The model the current run talks to. */
	public String model() {
		return _model;
	}

	/**
	 * Whether the running model can perceive the given modality.
	 *
	 * <p>
	 * Prefers the catalogue's declared {@link ModelInfo.ModelModalities#input() input modalities}.
	 * When a catalogue entry exists but declares no modalities, falls back to the coarser
	 * {@link ModelInfo.ModelCapabilities#attachment() attachment} flag, which says that the model
	 * takes files at all but not which kinds.
	 * </p>
	 */
	public PerceptionSupport support(Modality modality) {
		if (_modelInfo == null || _modelInfo.capabilities() == null) {
			return PerceptionSupport.UNKNOWN;
		}
		ModelInfo.ModelCapabilities capabilities = _modelInfo.capabilities();
		ModelInfo.ModelModalities modalities = capabilities.modalities();
		if (modalities != null && !modalities.input().isEmpty()) {
			for (String input : modalities.input()) {
				if (input != null && input.toLowerCase(Locale.ROOT).equals(modality.catalogName())) {
					return PerceptionSupport.SUPPORTED;
				}
			}
			return PerceptionSupport.UNSUPPORTED;
		}
		return capabilities.attachment() ? PerceptionSupport.SUPPORTED : PerceptionSupport.UNSUPPORTED;
	}

	/**
	 * Queues content for delivery to the model at the start of the next turn.
	 */
	public void offer(PerceivedContent content) {
		Objects.requireNonNull(content, "Content must not be null.");
		synchronized (_pending) {
			_pending.add(content);
		}
	}

	/**
	 * Removes and returns everything queued so far, in offer order.
	 */
	public List<PerceivedContent> takePending() {
		synchronized (_pending) {
			if (_pending.isEmpty()) {
				return List.of();
			}
			List<PerceivedContent> taken = List.copyOf(_pending);
			_pending.clear();
			return taken;
		}
	}
}
