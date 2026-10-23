/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * An {@link Embedder} that calls {@code POST <base-url>/v1/embeddings} of an
 * <code>OpenAI</code>-compatible server.
 *
 * <p>
 * <code>vLLM</code>, text-embeddings-inference, llama.cpp's server and Ollama all offer this route,
 * so one class serves a GPU endpoint of our own as well as a paid API. One request carries a whole batch. A
 * request that fails with a network error, {@code 429} or a {@code 5xx} is retried with a doubling
 * delay; any other answer fails at once.
 * </p>
 *
 * <p>
 * Each attempt is authenticated by the configured {@link CredentialProvider} at the moment it is
 * sent. An answer {@code 401 Unauthorized} invalidates the credentials - a cached access token may
 * have expired or been revoked - and the request is sent once more with fresh ones.
 * </p>
 */
@Label("HTTP embedding endpoint")
public class OpenAiCompatibleEmbedder extends AbstractEmbedder<OpenAiCompatibleEmbedder.Config<?>> {

	/**
	 * Configuration of an {@link OpenAiCompatibleEmbedder}.
	 */
	public interface Config<I extends OpenAiCompatibleEmbedder> extends AbstractEmbedder.Config<I> {

		/** Configuration name of {@link #getBaseUrl()}. */
		String BASE_URL = "base-url";

		/** Configuration name of {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration name of {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration name of {@link #getCredentials()}. */
		String CREDENTIALS = "credentials";

		/** Configuration name of {@link #getTimeoutSeconds()}. */
		String TIMEOUT_SECONDS = "timeout-seconds";

		/** Configuration name of {@link #getRetries()}. */
		String RETRIES = "retries";

		/** Configuration name of {@link #getRetryDelayMillis()}. */
		String RETRY_DELAY_MILLIS = "retry-delay-millis";

		/** Configuration name of {@link #getSendDimensions()}. */
		String SEND_DIMENSIONS = "send-dimensions";

		/**
		 * The address of the server, e.g. {@code https://embed.example.eu}; a trailing {@code /v1}
		 * may be given or not.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		@Mandatory
		String getBaseUrl();

		/**
		 * The model name the server knows the model by.
		 *
		 * @see #MODEL
		 */
		@Name(MODEL)
		@Mandatory
		String getModel();

		/**
		 * The API key, sent as bearer token in the header {@link CredentialProvider#AUTHORIZATION};
		 * none for a server without authentication.
		 *
		 * <p>
		 * Recommended is a reference to the variable or file holding the key, so that the key is not
		 * part of the configuration:
		 * </p>
		 *
		 * <pre>
		 * &lt;api-key name="tl_embed_api_key"/&gt;
		 * </pre>
		 *
		 * <p>
		 * An attribute value is an encrypted key, or a plain key with the prefix
		 * <code>unencrypted:</code>. Configuring both an API key and {@link #getCredentials()
		 * credentials} is an error.
		 * </p>
		 *
		 * @see #API_KEY
		 */
		@Name(API_KEY)
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		/**
		 * How the requests to the server are authenticated, if not with an {@link #getApiKey() API
		 * key}.
		 *
		 * <p>
		 * E.g. with an access token from an OAuth2 authorization server. Without credentials and
		 * API key, the requests are sent without credentials. Configuring both credentials and an
		 * API key is an error.
		 * </p>
		 *
		 * @see #CREDENTIALS
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		/**
		 * How long one request may take.
		 *
		 * @see #TIMEOUT_SECONDS
		 */
		@Name(TIMEOUT_SECONDS)
		@IntDefault(60)
		int getTimeoutSeconds();

		/**
		 * How often a failed request is repeated.
		 *
		 * @see #RETRIES
		 */
		@Name(RETRIES)
		@IntDefault(3)
		int getRetries();

		/**
		 * The delay before the first repetition; it doubles with each further one.
		 *
		 * @see #RETRY_DELAY_MILLIS
		 */
		@Name(RETRY_DELAY_MILLIS)
		@LongDefault(1000)
		long getRetryDelayMillis();

		/**
		 * Whether the request asks the server for the configured number of dimensions.
		 *
		 * <p>
		 * The <code>OpenAI</code> API cuts the vector on the server when asked; many other servers refuse the
		 * parameter. Without it, a longer vector is cut here.
		 * </p>
		 *
		 * @see #SEND_DIMENSIONS
		 */
		@Name(SEND_DIMENSIONS)
		boolean getSendDimensions();
	}

	private final HttpClient _client;

	private final URI _endpoint;

	private final CredentialProvider _credentials;

	/**
	 * Creates an {@link OpenAiCompatibleEmbedder} from configuration.
	 */
	@CalledByReflection
	public OpenAiCompatibleEmbedder(InstantiationContext context, Config<?> config) {
		super(context, config);
		_client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
		_endpoint = URI.create(ModelDiscoveryHelper.normalizeBaseUrl(config.getBaseUrl()) + "/v1/embeddings");
		_credentials = CredentialProvider.create(context, _endpoint.toString(), config.getCredentials(),
			config.getApiKey(), CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER);
	}

	@Override
	public String modelId() {
		return getConfig().getModel();
	}

	@Override
	protected List<float[]> embedBatch(List<String> texts) throws EmbeddingException {
		String body = requestBody(texts);
		long delay = getConfig().getRetryDelayMillis();
		int attempts = getConfig().getRetries() + 1;
		String failure = null;
		boolean renewed = false;
		for (int attempt = 1; attempt <= attempts; attempt++) {
			try {
				HttpResponse<String> response = _client.send(request(body),
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
				int status = response.statusCode();
				if (status == 200) {
					return parse(response.body(), texts.size());
				}
				failure = "HTTP " + status + ": " + abbreviate(response.body());
				if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
					_credentials.invalidate();
					if (!renewed) {
						renewed = true;
						// Once more with fresh credentials, not counted as retry.
						attempt--;
						continue;
					}
				}
				if (status != 429 && status < 500) {
					throw new EmbeddingException("Embedding endpoint " + _endpoint + " refused the request, " + failure);
				}
			} catch (IOException ex) {
				failure = ex.getClass().getSimpleName() + ": " + ex.getMessage();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new EmbeddingException("Interrupted while embedding.", ex);
			}
			if (attempt < attempts) {
				Logger.info("Embedding request to " + _endpoint + " failed (" + failure + "), attempt " + attempt
					+ " of " + attempts + ".", OpenAiCompatibleEmbedder.class);
				sleep(delay);
				delay *= 2;
			}
		}
		throw new EmbeddingException("Embedding endpoint " + _endpoint + " failed " + attempts + " times, last: " + failure);
	}

	private HttpRequest request(String body) throws EmbeddingException {
		HttpRequest.Builder builder = HttpRequest.newBuilder(_endpoint)
			.timeout(Duration.ofSeconds(getConfig().getTimeoutSeconds()))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
		try {
			_credentials.authenticate(RequestAuthentication.of(builder));
		} catch (I18NRuntimeException ex) {
			throw new EmbeddingException(
				"No credentials for the embedding endpoint " + _endpoint + ": " + ex.getMessage(), ex);
		}
		return builder.build();
	}

	private String requestBody(List<String> texts) throws EmbeddingException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("model").value(getConfig().getModel());
			json.name("input").beginArray();
			for (String text : texts) {
				json.value(text);
			}
			json.endArray();
			if (getConfig().getSendDimensions()) {
				json.name("dimensions").value(dimensions());
			}
			json.name("encoding_format").value("float");
			json.endObject();
		} catch (IOException ex) {
			throw new EmbeddingException("Cannot write the embedding request.", ex);
		}
		return buffer.toString();
	}

	/**
	 * Reads {@code {"data": [{"index": i, "embedding": [...]}, ...]}}, ordered by {@code index}.
	 */
	private static List<float[]> parse(String body, int expected) throws EmbeddingException {
		TreeMap<Integer, float[]> byIndex = new TreeMap<>();
		try (JsonReader json = new JsonReader(new StringR(body))) {
			json.beginObject();
			while (json.hasNext()) {
				if (!"data".equals(json.nextName())) {
					json.skipValue();
					continue;
				}
				json.beginArray();
				while (json.hasNext()) {
					Integer index = null;
					float[] vector = null;
					json.beginObject();
					while (json.hasNext()) {
						switch (json.nextName()) {
							case "index" -> index = json.nextInt();
							case "embedding" -> vector = readVector(json);
							default -> json.skipValue();
						}
					}
					json.endObject();
					if (vector == null) {
						throw new EmbeddingException("An embedding entry has no vector.");
					}
					byIndex.put(index == null ? byIndex.size() : index, vector);
				}
				json.endArray();
			}
			json.endObject();
		} catch (IOException | RuntimeException ex) {
			throw new EmbeddingException("Cannot read the embedding answer: " + abbreviate(body), ex);
		}
		if (byIndex.size() != expected) {
			throw new EmbeddingException("The embedding answer has " + byIndex.size() + " vectors for " + expected
				+ " texts.");
		}
		return new ArrayList<>(byIndex.values());
	}

	private static float[] readVector(JsonReader json) throws IOException {
		List<Float> values = new ArrayList<>();
		json.beginArray();
		while (json.hasNext()) {
			values.add((float) json.nextDouble());
		}
		json.endArray();
		float[] result = new float[values.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = values.get(i);
		}
		return result;
	}

	private static String abbreviate(String text) {
		if (text == null) {
			return "";
		}
		return text.length() > 300 ? text.substring(0, 300) + "..." : text;
	}

	private static void sleep(long millis) throws EmbeddingException {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new EmbeddingException("Interrupted while waiting to retry.", ex);
		}
	}

}
