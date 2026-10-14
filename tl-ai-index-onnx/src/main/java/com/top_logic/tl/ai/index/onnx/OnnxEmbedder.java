/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.onnx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.tl.ai.index.embed.AbstractEmbedder;
import com.top_logic.tl.ai.index.embed.EmbeddingException;

/**
 * An {@link com.top_logic.tl.ai.index.embed.Embedder} that runs the model in this process, on the
 * CPU: ONNX Runtime for the model, the Hugging Face tokenizer of DJL for the text.
 *
 * <p>
 * The model files are not part of any jar. Operations puts the model's ONNX file and its
 * {@code tokenizer.json} from the official model repository into {@link Config#getModelDir()}; see
 * {@code tl-ai-index/README.md}.
 * </p>
 *
 * <h2>Rules of operation</h2>
 *
 * <ul>
 * <li><b>Offline.</b> The tokenizer library downloads a GPU build of itself and reports telemetry
 * unless {@code ai.djl.offline} is set; this class sets it before the tokenizer is loaded. The native
 * library is then taken from the jar.</li>
 * <li><b>Bounded CPU.</b> {@link Config#getThreads()} threads per inference, no spinning between runs,
 * and inference runs on the index worker, which has the lowest thread priority.</li>
 * <li><b>Bounded memory.</b> A batch is split so that it holds at most
 * {@link Config#getMaxBatchTokens()} tokens: ONNX Runtime's memory arena keeps its largest peak.</li>
 * <li><b>One text per inference</b> by default ({@link Config#getRowsPerInference()}). A dynamically
 * quantized (int8) model computes the scale of its activations over the whole batch, so a text's
 * vector would depend on the texts beside it.</li>
 * <li><b>Restart on redeploy.</b> A native library loaded by a web application's class loader stays
 * in memory until the JVM ends.</li>
 * </ul>
 */
@Label("In-process ONNX model")
public class OnnxEmbedder extends AbstractEmbedder<OnnxEmbedder.Config<?>> {

	/**
	 * How the vectors of a text's tokens become one vector.
	 */
	public enum Pooling {
		/** The vector of the first token; arctic-embed, bge. */
		CLS,

		/** The mean of all tokens' vectors; e5. */
		MEAN;
	}

	/**
	 * Configuration of an {@link OnnxEmbedder}.
	 */
	public interface Config<I extends OnnxEmbedder> extends AbstractEmbedder.Config<I> {

		/** Configuration name of {@link #getModelDir()}. */
		String MODEL_DIR = "model-dir";

		/** Configuration name of {@link #getModelFile()}. */
		String MODEL_FILE = "model-file";

		/** Configuration name of {@link #getTokenizerFile()}. */
		String TOKENIZER_FILE = "tokenizer-file";

		/** Configuration name of {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration name of {@link #getPooling()}. */
		String POOLING = "pooling";

		/** Configuration name of {@link #getThreads()}. */
		String THREADS = "threads";

		/** Configuration name of {@link #getMaxTokens()}. */
		String MAX_TOKENS = "max-tokens";

		/** Configuration name of {@link #getMaxBatchTokens()}. */
		String MAX_BATCH_TOKENS = "max-batch-tokens";

		/** Configuration name of {@link #getRowsPerInference()}. */
		String ROWS_PER_INFERENCE = "rows-per-inference";

		/**
		 * The directory with the model's ONNX file and its tokenizer; aliases like {@code %VAR_PATH%}
		 * are resolved.
		 *
		 * @see #MODEL_DIR
		 */
		@Name(MODEL_DIR)
		@Mandatory
		String getModelDir();

		/**
		 * The ONNX file in the model directory, e.g. {@code model_int8.onnx}.
		 *
		 * @see #MODEL_FILE
		 */
		@Name(MODEL_FILE)
		@StringDefault("model.onnx")
		String getModelFile();

		/**
		 * The tokenizer file in the model directory.
		 *
		 * @see #TOKENIZER_FILE
		 */
		@Name(TOKENIZER_FILE)
		@StringDefault("tokenizer.json")
		String getTokenizerFile();

		/**
		 * The name stored with each vector; the name of the model directory when none is given.
		 *
		 * @see #MODEL
		 */
		@Name(MODEL)
		@Nullable
		String getModel();

		/**
		 * How the token vectors become one vector: {@code CLS} for arctic-embed, {@code MEAN} for e5.
		 *
		 * @see #POOLING
		 */
		@Name(POOLING)
		@FormattedDefault("CLS")
		Pooling getPooling();

		/**
		 * Threads one inference uses.
		 *
		 * @see #THREADS
		 */
		@Name(THREADS)
		@IntDefault(4)
		int getThreads();

		/**
		 * Tokens of a text the model reads at most; the rest is cut.
		 *
		 * @see #MAX_TOKENS
		 */
		@Name(MAX_TOKENS)
		@IntDefault(512)
		int getMaxTokens();

		/**
		 * Tokens of one inference at most, padding included.
		 *
		 * @see #MAX_BATCH_TOKENS
		 */
		@Name(MAX_BATCH_TOKENS)
		@IntDefault(8192)
		int getMaxBatchTokens();

		/**
		 * Texts in one inference at most.
		 *
		 * <p>
		 * A dynamically quantized (int8) model scales its activations over the whole batch, so with more
		 * than one text a vector depends on its neighbours: for arctic-embed-m-v2.0 int8 the cosine
		 * between a text's vector alone and in a batch was 0.976 to 0.985, for multilingual-e5-small int8
		 * 0.997. One text per inference cost arctic 11 % of its throughput and e5-small 40 % on a laptop
		 * CPU. Raise it for a model without dynamic quantization, or for e5-small when throughput
		 * matters more than reproducible vectors.
		 * </p>
		 *
		 * @see #ROWS_PER_INFERENCE
		 */
		@Name(ROWS_PER_INFERENCE)
		@IntDefault(1)
		int getRowsPerInference();
	}

	private final OrtEnvironment _environment;

	private final OrtSession _session;

	private final HuggingFaceTokenizer _tokenizer;

	private final boolean _typeIds;

	private final String _model;

	/**
	 * Creates an {@link OnnxEmbedder} and loads the model.
	 */
	@CalledByReflection
	public OnnxEmbedder(InstantiationContext context, Config<?> config) {
		super(context, config);
		Path dir = Path.of(AliasManager.getInstance().replace(config.getModelDir()));
		Path modelFile = dir.resolve(config.getModelFile());
		Path tokenizerFile = dir.resolve(config.getTokenizerFile());
		if (!Files.isRegularFile(modelFile) || !Files.isRegularFile(tokenizerFile)) {
			throw new IllegalArgumentException("The embedding model needs " + modelFile + " and " + tokenizerFile
				+ "; see tl-ai-index/README.md for where to get them.");
		}
		_model = config.getModel() == null ? dir.getFileName().toString() : config.getModel();
		if (System.getProperty("ai.djl.offline") == null) {
			System.setProperty("ai.djl.offline", "true");
		}
		try {
			_tokenizer = HuggingFaceTokenizer.builder()
				.optTokenizerPath(tokenizerFile)
				.optMaxLength(config.getMaxTokens())
				.optTruncation(true)
				.optPadding(true)
				.build();
			_environment = OrtEnvironment.getEnvironment();
			try (OrtSession.SessionOptions options = new OrtSession.SessionOptions()) {
				options.setIntraOpNumThreads(config.getThreads());
				options.setInterOpNumThreads(1);
				options.addConfigEntry("session.intra_op.allow_spinning", "0");
				options.addConfigEntry("session.inter_op.allow_spinning", "0");
				_session = _environment.createSession(modelFile.toString(), options);
			}
			_typeIds = _session.getInputNames().contains("token_type_ids");
		} catch (IOException | OrtException ex) {
			throw new IllegalArgumentException("Cannot load the embedding model from " + dir + ": " + ex.getMessage(),
				ex);
		}
	}

	@Override
	public String modelId() {
		return _model;
	}

	@Override
	protected String fingerprintDetails() {
		return getConfig().getModelFile() + "|" + getConfig().getPooling() + "|" + getConfig().getMaxTokens() + "|"
			+ getConfig().getRowsPerInference();
	}

	@Override
	protected List<float[]> embedBatch(List<String> texts) throws EmbeddingException {
		List<float[]> result = new ArrayList<>(texts.size());
		int budget = getConfig().getMaxBatchTokens();
		int rows = Math.max(1, getConfig().getRowsPerInference());
		List<String> group = new ArrayList<>();
		int longest = 0;
		for (String text : texts) {
			int length = rows == 1 ? 0 : _tokenizer.encode(text).getIds().length;
			int newLongest = Math.max(longest, length);
			if (!group.isEmpty() && (group.size() >= rows || newLongest * (group.size() + 1) > budget)) {
				result.addAll(run(group));
				group.clear();
				newLongest = length;
			}
			group.add(text);
			longest = newLongest;
		}
		if (!group.isEmpty()) {
			result.addAll(run(group));
		}
		return result;
	}

	/**
	 * One inference over the given texts, padded to the longest of them.
	 */
	private List<float[]> run(List<String> texts) throws EmbeddingException {
		Encoding[] encodings = _tokenizer.batchEncode(texts);
		int rows = encodings.length;
		long[][] ids = new long[rows][];
		long[][] mask = new long[rows][];
		long[][] types = new long[rows][];
		for (int i = 0; i < rows; i++) {
			ids[i] = encodings[i].getIds();
			mask[i] = encodings[i].getAttentionMask();
			types[i] = new long[ids[i].length];
		}
		Map<String, OnnxTensor> inputs = new HashMap<>();
		try {
			inputs.put("input_ids", OnnxTensor.createTensor(_environment, ids));
			inputs.put("attention_mask", OnnxTensor.createTensor(_environment, mask));
			if (_typeIds) {
				inputs.put("token_type_ids", OnnxTensor.createTensor(_environment, types));
			}
			try (OrtSession.Result output = _session.run(inputs)) {
				return pool(output.get(0), mask);
			}
		} catch (OrtException ex) {
			throw new EmbeddingException("The embedding model failed: " + ex.getMessage(), ex);
		} finally {
			for (OnnxTensor tensor : inputs.values()) {
				tensor.close();
			}
		}
	}

	private List<float[]> pool(OnnxValue output, long[][] mask) throws OrtException {
		Object value = output.getValue();
		List<float[]> result = new ArrayList<>(mask.length);
		if (value instanceof float[][] pooled) {
			for (float[] vector : pooled) {
				result.add(vector);
			}
			return result;
		}
		float[][][] hidden = (float[][][]) value;
		for (int i = 0; i < hidden.length; i++) {
			int dimensions = hidden[i][0].length;
			float[] vector = new float[dimensions];
			if (getConfig().getPooling() == Pooling.CLS) {
				System.arraycopy(hidden[i][0], 0, vector, 0, dimensions);
			} else {
				int count = 0;
				for (int t = 0; t < hidden[i].length; t++) {
					if (mask[i][t] == 1) {
						count++;
						for (int d = 0; d < dimensions; d++) {
							vector[d] += hidden[i][t][d];
						}
					}
				}
				for (int d = 0; d < dimensions; d++) {
					vector[d] /= Math.max(1, count);
				}
			}
			result.add(vector);
		}
		return result;
	}

	@Override
	public void close() {
		try {
			_session.close();
		} catch (OrtException ex) {
			// Closing at shutdown; nothing to do about it.
		}
		_tokenizer.close();
	}

}
