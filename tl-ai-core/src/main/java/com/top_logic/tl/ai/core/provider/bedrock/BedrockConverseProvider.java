/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.AbstractStreamingProvider;
import com.top_logic.tl.ai.core.provider.ContentBlockAccumulator;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;

import de.haumacher.msgbuf.json.JsonReader;

/**
 * {@link AbstractStreamingProvider} for the AWS Bedrock Converse streaming API
 * ({@code /model/{modelId}/converse-stream}).
 *
 * <p>Uses the binary {@code application/vnd.amazon.eventstream} framing decoded by
 * {@link EventStreamReader}. Request serialization is delegated to
 * {@link BedrockConverseRequestBuilder}.</p>
 *
 * <p>The base URL is mandatory — no default is provided because the caller supplies the
 * gateway or regional AWS endpoint. Authentication uses the default Bearer scheme inherited
 * from {@link AbstractStreamingProvider}.</p>
 *
 * <p>Thread-safe: a single instance handles concurrent streams.</p>
 */
public class BedrockConverseProvider extends AbstractStreamingProvider {
	/**
	 * Catalog ID stamped onto the thinking blocks this provider produces.
	 *
	 * <p>
	 * Their continuity tokens only mean something here, so the origin has to travel with them:
	 * it is what lets a later request to another provider drop them instead of being rejected.
	 * </p>
	 */
	private static final String PROVIDER_ID = "bedrock";


	/**
	 * Creates a {@link BedrockConverseProvider} with a shared default {@link HttpClient}.
	 *
	 * @param apiKey
	 *        The API key forwarded as {@code Authorization: Bearer}. May be {@code null}.
	 * @param baseUrl
	 *        The provider base URL (e.g. {@code https://bedrock-runtime.us-east-1.amazonaws.com}).
	 *        Mandatory — no default.
	 */
	public BedrockConverseProvider(String apiKey, String baseUrl) {
		this(apiKey, baseUrl, defaultHttpClient());
	}

	/**
	 * Creates a {@link BedrockConverseProvider} with a caller-supplied {@link HttpClient}.
	 *
	 * @param apiKey
	 *        The API key. May be {@code null}.
	 * @param baseUrl
	 *        The provider base URL. Mandatory.
	 * @param httpClient
	 *        The HTTP client to use.
	 */
	public BedrockConverseProvider(String apiKey, String baseUrl, HttpClient httpClient) {
		super(apiKey, baseUrl, httpClient);
	}

	/**
	 * Creates a {@link BedrockConverseProvider} with configurable timeouts and custom headers.
	 *
	 * @param apiKey
	 *        The API key. May be {@code null}.
	 * @param baseUrl
	 *        The provider base URL. Mandatory.
	 * @param connectTimeoutSeconds
	 *        TCP connect timeout in seconds.
	 * @param requestTimeoutSeconds
	 *        Per-request timeout in seconds.
	 * @param customHeaders
	 *        Additional HTTP headers applied after all provider-specific headers.
	 */
	public BedrockConverseProvider(String apiKey, String baseUrl,
			int connectTimeoutSeconds, int requestTimeoutSeconds,
			Map<String, String> customHeaders) {
		super(apiKey, baseUrl, defaultHttpClient(connectTimeoutSeconds),
			Duration.ofSeconds(requestTimeoutSeconds), customHeaders);
	}

	@Override
	protected String endpointPath(ProviderRequest request) {
		// URLEncoder is form-encoding (space -> '+'), which is technically not RFC 3986 path
		// encoding. Bedrock model IDs contain no spaces or reserved characters, so the output
		// is identical to strict percent-encoding for these inputs.
		return "/model/" + URLEncoder.encode(request.model(), StandardCharsets.UTF_8) + "/converse-stream";
	}

	@Override
	protected String acceptHeader() {
		return "application/vnd.amazon.eventstream";
	}

	@Override
	protected String buildRequestJson(ProviderRequest request) throws ProviderException {
		return BedrockConverseRequestBuilder.buildRequestJson(request);
	}

	@Override
	protected void processStream(InputStream input, StreamHandler handler)
			throws IOException, ProviderException {

		String finishReason = "end_turn";
		int inputTokens = 0;
		int outputTokens = 0;
		int cacheReadTokens = 0;
		int cacheWriteTokens = 0;
		boolean metadataReceived = false;
		ContentBlockAccumulator accumulator = new ContentBlockAccumulator();
		// Tracks which text-block indices have already received a TextStart event.
		Set<Integer> openedTextBlocks = new HashSet<>();

		handler.onEvent(new StreamEvent.StreamStart());

		try (EventStreamReader reader = new EventStreamReader(input)) {
			EventStreamReader.Message message;
			while ((message = reader.next()) != null) {
				Map<String, String> headers = message.headers();

				// Exception frames: emit an Error event and stop.
				String messageType = headers.get(":message-type");
				if ("exception".equals(messageType) || headers.containsKey(":exception-type")) {
					String errorMsg = message.payloadString();
					handler.onEvent(new StreamEvent.Error(errorMsg,
						ProviderException.Kind.INVALID_REQUEST));
					return;
				}

				String eventType = headers.get(":event-type");
				if (eventType == null) {
					continue;
				}

				switch (eventType) {
					case "messageStart" -> {
						// No StreamEvent emitted; role is always "assistant".
					}

					case "contentBlockStart" -> {
						String payload = message.payloadString();
						int blockIndex = parseContentBlockIndex(payload);
						String[] toolUseIdAndName = parseToolUseStartBlock(payload);
						if (toolUseIdAndName != null) {
							accumulator.addBlock(blockIndex, "tool_use",
								toolUseIdAndName[0], toolUseIdAndName[1]);
							handler.onEvent(new StreamEvent.ToolCallStart(
								blockIndex, toolUseIdAndName[0], toolUseIdAndName[1]));
						}
						// Text blocks have no contentBlockStart — they open lazily in contentBlockDelta.
					}

					case "contentBlockDelta" -> {
						String payload = message.payloadString();
						int blockIndex = parseContentBlockIndex(payload);
						String text = parseTextDelta(payload);
						if (text != null && !text.isEmpty()) {
							// Lazily open a text block on the first text delta for this index.
							if (!openedTextBlocks.contains(blockIndex)) {
								accumulator.addBlock(blockIndex, "text", null, null);
								handler.onEvent(new StreamEvent.TextStart(blockIndex));
								openedTextBlocks.add(blockIndex);
							}
							accumulator.appendText(blockIndex, text);
							handler.onEvent(new StreamEvent.TextDelta(blockIndex, text));
						} else {
							String toolInput = parseToolUseDelta(payload);
							if (toolInput != null) {
								accumulator.appendArguments(blockIndex, toolInput);
								handler.onEvent(new StreamEvent.ToolCallDelta(blockIndex, toolInput));
							}
						}
					}

					case "contentBlockStop" -> {
						String payload = message.payloadString();
						int blockIndex = parseContentBlockIndex(payload);
						String blockType = accumulator.getBlockType(blockIndex);
						if (blockType != null) {
							switch (blockType) {
								case "text" -> handler.onEvent(new StreamEvent.TextEnd(blockIndex));
								case "tool_use" -> handler.onEvent(new StreamEvent.ToolCallEnd(blockIndex));
								default -> { /* Unknown block type — ignore. */ }
							}
						}
					}

					case "messageStop" -> {
						finishReason = parseStopReason(message.payloadString());
					}

					case "metadata" -> {
						if (!metadataReceived) {
							metadataReceived = true;
							String payload = message.payloadString();
							int[] usage = parseUsage(payload);
							inputTokens = usage[0];
							outputTokens = usage[1];
							cacheReadTokens = usage[2];
							cacheWriteTokens = usage[3];
							handler.onEvent(new StreamEvent.MessageComplete(
								"", "",
								finishReason,
								new TokenUsage(inputTokens, outputTokens, 0, cacheReadTokens,
									cacheWriteTokens),
								accumulator.buildContentParts(PROVIDER_ID)));
						}
					}

					default -> { /* Ignore unknown event types. */ }
				}
			}
		}

		// If the stream ended without a metadata event, emit a best-effort MessageComplete.
		if (!metadataReceived) {
			handler.onEvent(new StreamEvent.MessageComplete(
				"", "",
				finishReason,
				new TokenUsage(inputTokens, outputTokens, 0, cacheReadTokens, cacheWriteTokens),
				accumulator.buildContentParts(PROVIDER_ID)));
		}
	}

	@Override
	protected void handleErrorResponse(HttpResponse<InputStream> response)
			throws ProviderException {
		int status = response.statusCode();
		String body = drainResponseBody(response);
		Duration retryAfter = parseRetryAfter(response.headers());

		String message = parseBedrockErrorMessage(body);
		if (message == null || message.isBlank()) {
			message = body;
		}

		ProviderException.Kind kind = switch (status) {
			case 401, 403 -> ProviderException.Kind.AUTHENTICATION;
			case 429 -> ProviderException.Kind.RATE_LIMITED;
			default -> {
				if (status >= 500) {
					yield ProviderException.Kind.OVERLOADED;
				}
				yield ProviderException.Kind.INVALID_REQUEST;
			}
		};

		throw new ProviderException(kind, status, message, retryAfter, null);
	}

	// ---- JSON parsing helpers (inline, no msgbuf-generated DTOs for trivial fields) ----

	/**
	 * Extracts {@code contentBlockIndex} from a Bedrock content-block event payload.
	 */
	private static int parseContentBlockIndex(String payload) {
		try (JsonReader json = jsonReader(payload)) {
			json.beginObject();
			while (json.hasNext()) {
				String name = json.nextName();
				if ("contentBlockIndex".equals(name)) {
					return json.nextInt();
				}
				json.skipValue();
			}
		} catch (Exception e) {
			// Ignore parse errors — return 0 as safe default.
		}
		return 0;
	}

	/**
	 * Extracts {@code toolUseId} and {@code name} from a {@code contentBlockStart} payload.
	 *
	 * @return two-element array {@code [toolUseId, name]}, or {@code null} if no toolUse block.
	 */
	private static String[] parseToolUseStartBlock(String payload) {
		// payload: {"contentBlockIndex":N,"start":{"toolUse":{"toolUseId":"...","name":"..."}}}
		String toolUseId = null;
		String name = null;
		try (JsonReader json = jsonReader(payload)) {
			json.beginObject();
			while (json.hasNext()) {
				String key = json.nextName();
				if ("start".equals(key)) {
					json.beginObject();
					while (json.hasNext()) {
						if ("toolUse".equals(json.nextName())) {
							json.beginObject();
							while (json.hasNext()) {
								switch (json.nextName()) {
									case "toolUseId" -> toolUseId = json.nextString();
									case "name" -> name = json.nextString();
									default -> json.skipValue();
								}
							}
							json.endObject();
						} else {
							json.skipValue();
						}
					}
					json.endObject();
				} else {
					json.skipValue();
				}
			}
		} catch (Exception e) {
			// Fall through — return null if toolUse not found.
		}
		if (toolUseId != null && name != null) {
			return new String[] { toolUseId, name };
		}
		return null;
	}

	/**
	 * Extracts the {@code delta.text} field from a {@code contentBlockDelta} payload.
	 *
	 * @return the text string, or {@code null} if the delta has no text field.
	 */
	private static String parseTextDelta(String payload) {
		// payload: {"contentBlockIndex":N,"delta":{"text":"..."}}
		try (JsonReader json = jsonReader(payload)) {
			json.beginObject();
			while (json.hasNext()) {
				String key = json.nextName();
				if ("delta".equals(key)) {
					json.beginObject();
					while (json.hasNext()) {
						if ("text".equals(json.nextName())) {
							return json.nextString();
						}
						json.skipValue();
					}
					json.endObject();
				} else {
					json.skipValue();
				}
			}
		} catch (Exception e) {
			// Fall through.
		}
		return null;
	}

	/**
	 * Extracts the {@code delta.toolUse.input} field from a {@code contentBlockDelta} payload.
	 *
	 * @return the partial JSON argument string, or {@code null} if absent.
	 */
	private static String parseToolUseDelta(String payload) {
		// payload: {"contentBlockIndex":N,"delta":{"toolUse":{"input":"..."}}}
		try (JsonReader json = jsonReader(payload)) {
			json.beginObject();
			while (json.hasNext()) {
				String key = json.nextName();
				if ("delta".equals(key)) {
					json.beginObject();
					while (json.hasNext()) {
						if ("toolUse".equals(json.nextName())) {
							json.beginObject();
							while (json.hasNext()) {
								if ("input".equals(json.nextName())) {
									return json.nextString();
								}
								json.skipValue();
							}
							json.endObject();
						} else {
							json.skipValue();
						}
					}
					json.endObject();
				} else {
					json.skipValue();
				}
			}
		} catch (Exception e) {
			// Fall through.
		}
		return null;
	}

	/**
	 * Extracts and normalises {@code stopReason} from a {@code messageStop} payload.
	 *
	 * <p>Maps {@code stop_sequence} to {@code "end_turn"} for consistency with the rest of the
	 * agent loop; all other values are returned as-is.</p>
	 */
	private static String parseStopReason(String payload) {
		// payload: {"stopReason":"end_turn"|"tool_use"|"max_tokens"|"stop_sequence"|...}
		try (JsonReader json = jsonReader(payload)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("stopReason".equals(json.nextName())) {
					String raw = json.nextString();
					return "stop_sequence".equals(raw) ? "end_turn" : raw;
				}
				json.skipValue();
			}
		} catch (Exception e) {
			// Fall through.
		}
		return "end_turn";
	}

	/**
	 * Extracts token counts from a {@code metadata} payload.
	 *
	 * @return four-element int array
	 *         {@code [inputTokens, outputTokens, cacheReadTokens, cacheWriteTokens]}.
	 */
	private static int[] parseUsage(String payload) {
		// payload: {"usage":{"inputTokens":I,"outputTokens":O,
		//                    "cacheReadInputTokens":R?,"cacheWriteInputTokens":W?,...},...}
		int inputTokens = 0;
		int outputTokens = 0;
		int cacheReadTokens = 0;
		int cacheWriteTokens = 0;
		try (JsonReader json = jsonReader(payload)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("usage".equals(json.nextName())) {
					json.beginObject();
					while (json.hasNext()) {
						switch (json.nextName()) {
							case "inputTokens" -> inputTokens = json.nextInt();
							case "outputTokens" -> outputTokens = json.nextInt();
							case "cacheReadInputTokens" -> cacheReadTokens = json.nextInt();
							case "cacheWriteInputTokens" -> cacheWriteTokens = json.nextInt();
							default -> json.skipValue();
						}
					}
					json.endObject();
				} else {
					json.skipValue();
				}
			}
		} catch (Exception e) {
			// Return whatever was parsed so far.
		}
		return new int[] { inputTokens, outputTokens, cacheReadTokens, cacheWriteTokens };
	}

	/**
	 * Extracts the {@code message} field from a Bedrock error response JSON body.
	 *
	 * @return the message string, or {@code null} if the field is absent or parsing fails.
	 */
	private static String parseBedrockErrorMessage(String body) {
		try (JsonReader json = jsonReader(body)) {
			json.beginObject();
			while (json.hasNext()) {
				if ("message".equals(json.nextName())) {
					return json.nextString();
				}
				json.skipValue();
			}
		} catch (Exception e) {
			// Fall through.
		}
		return null;
	}
}
