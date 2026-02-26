# Spec: React Audio Recording Control with Speech-to-Text

**Ticket**: #29109
**Status**: Draft
**Module**: `com.top_logic.layout.react` (+ demo in `com.top_logic.demo`)

## Overview

A React control (special button) that records audio from the user's microphone and uploads the recording to the server as binary data. The server-side control receives the audio and delivers it to the embedding component (e.g. to store in a `DataField`).

In Phase II, an optional `TranscriptionService` can be plugged in to forward audio to an external speech-to-text API (e.g. OpenAI Whisper) and return transcribed text via SSE.

## Motivation

The current React SSE infrastructure only supports JSON commands (`/react-api/command`). Binary audio data requires a new multipart upload path. This control enables voice input in TopLogic React-based UIs.

## Architecture

```
Browser                          Server
┌──────────────────┐            ┌───────────────────────────────┐
│ TLAudioRecorder  │            │ ReactServlet                  │
│   (MediaRecorder)│──upload──▶ │   /react-api/upload           │
│                  │            │     ▼                         │
│   useTLUpload()  │            │ ReactAudioRecorderControl     │
│                  │◀──SSE────  │     ▼                         │
│   useTLState()   │            │ Consumer<BinaryDataSource>    │
└──────────────────┘            │   (e.g. store in DataField)   │
                                └───────────────────────────────┘
```

## State Shape

```json
{
  "status": "idle",
  "error": null
}
```

**Status flow**: `idle` → `recording` (client-only) → `uploading` (client-only) → `received` (server patch) → `idle` (server patch)

---

## Phase I: Audio Recording & Binary Upload

### Files to Create/Modify

#### Client-Side (React/TypeScript)

##### 1. `useTLUpload` hook — modify `tl-react-bridge.ts`

**File**: `com.top_logic.layout.react/react-src/bridge/tl-react-bridge.ts`

Add a new hook mirroring `useTLCommand` but for `FormData`:

```typescript
export function useTLUpload(): (formData: FormData) => Promise<void> {
  const ctx = useContext(TLControlContext);
  const controlId = ctx.controlId;
  const windowName = ctx.windowName;

  return useCallback(
    async (formData: FormData) => {
      formData.append('controlId', controlId);
      formData.append('windowName', windowName);
      const resp = await fetch(getApiBase() + 'react-api/upload', {
        method: 'POST',
        body: formData,  // no Content-Type header — browser sets multipart boundary
      });
      if (!resp.ok) {
        console.error('[TLReact] Upload failed:', resp.status);
      }
    },
    [controlId, windowName]
  );
}
```

##### 2. `TLAudioRecorder.tsx` — new file

**File**: `com.top_logic.layout.react/react-src/controls/TLAudioRecorder.tsx`

React component that:
- Shows a button with label from state (e.g. "Record")
- On click: requests microphone access (`navigator.mediaDevices.getUserMedia`), starts `MediaRecorder`
- On second click (or stop): stops recording, assembles blob from chunks, uploads via `useTLUpload`
- Displays status feedback (`recording`, `uploading`, result)
- Uses `useTLState` for server-pushed status and local state for recording toggle

Key behavior:
- `MediaRecorder` with `audio/webm` mime type (broad browser support)
- Chunks collected in `dataavailable` handler
- On stop: create `Blob`, wrap in `FormData` with key `"audio"`, call `upload(formData)`
- Client sets local state to `"uploading"` until server patches status back to `"idle"`

##### 3. Register in `controls-entry.ts`

**File**: `com.top_logic.layout.react/react-src/controls-entry.ts`

Add: `register('TLAudioRecorder', TLAudioRecorder);`

##### 4. CSS in `tlReactControls.css`

**File**: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

Minimal styles:
- `.tlAudioRecorder` — container
- `.tlAudioRecorder__button` — record button with pulse animation when recording
- `.tlAudioRecorder__status` — status text display

#### Server-Side (Java)

##### 5. Extend `ReactServlet.java` with upload endpoint

**File**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactServlet.java`

- Add `@MultipartConfig` annotation to the servlet class for Jakarta Servlet multipart support.
- Add `handleUpload(request, response)` method dispatched when path is `/upload`.

```java
private void handleUpload(HttpServletRequest request, HttpServletResponse response) {
    String controlId = request.getParameter("controlId");
    String windowName = request.getParameter("windowName");

    // Resolve the control from the SSE session
    SSEUpdateQueue queue = SSEUpdateQueue.getInstance(request.getSession());
    CommandListener control = queue.getControl(controlId);

    // Dispatch via generic UploadHandler interface
    if (control instanceof UploadHandler handler) {
        handler.handleUpload(displayContext, request.getParts());
    }
}
```

##### 6. `UploadHandler.java` — new interface

**File**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/UploadHandler.java`

```java
/**
 * Interface for React controls that accept multipart file uploads.
 */
public interface UploadHandler {
    HandlerResult handleUpload(DisplayContext context, Collection<Part> parts);
}
```

##### 7. `ReactAudioRecorderControl.java` — new control

**File**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/audio/ReactAudioRecorderControl.java`

```java
public class ReactAudioRecorderControl extends ReactControl implements UploadHandler {

    private static final String STATUS = "status";
    private static final String ERROR = "error";

    private final Consumer<BinaryDataSource> _dataHandler;

    public ReactAudioRecorderControl(Consumer<BinaryDataSource> dataHandler) {
        super(null, "TLAudioRecorder");
        _dataHandler = dataHandler;
        putState(STATUS, "idle");
    }

    @Override
    public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
        putState(STATUS, "received");
        try {
            Part audioPart = parts.stream()
                .filter(p -> "audio".equals(p.getName()))
                .findFirst()
                .orElseThrow();

            byte[] audioData = audioPart.getInputStream().readAllBytes();
            String contentType = audioPart.getContentType();

            BinaryDataSource data = BinaryData.createBinaryData(audioData, contentType, "recording.webm");
            _dataHandler.accept(data);

            putState(STATUS, "idle");
        } catch (Exception ex) {
            putState(ERROR, ex.getMessage());
            putState(STATUS, "idle");
        }
        return HandlerResult.DEFAULT_RESULT;
    }
}
```

The `_dataHandler` callback lets the embedding component do something with the audio data (e.g. store it in a `DataField`).

##### 8. I18N constants

**File**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java`

```java
/**
 * @en Audio upload failed.
 */
public static ResKey AUDIO_UPLOAD_FAILED;
```

##### 9. `package-info.java` for audio package

**New file**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/audio/package-info.java`

#### Demo

##### 10. `DemoAudioRecorderComponent.java` — new demo

**File**: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoAudioRecorderComponent.java`

Demo component that:
- Creates a `FormContext` with a `DataField` for the audio recording
- Creates a `ReactAudioRecorderControl` whose `_dataHandler` stores the received `BinaryDataSource` into the `DataField`
- After upload, the recorded audio is available in the `DataField` as a regular binary value (downloadable, displayable by the framework)

### Implementation Order

1. `tl-react-bridge.ts` — add `useTLUpload` hook
2. `TLAudioRecorder.tsx` — create React component
3. `controls-entry.ts` — register component
4. `tlReactControls.css` — add styles
5. `ReactServlet.java` — add upload endpoint + `@MultipartConfig`
6. `UploadHandler.java` — create interface
7. `ReactAudioRecorderControl.java` — create server control
8. `I18NConstants.java` + `package-info.java`
9. Demo component

Steps 1-4 (client) and 5-8 (server) are independent and can be done in parallel.

### Verification (Phase I)

1. **Build Java**: `cd com.top_logic.layout.react && mvn install -DskipTests=true`
2. **Build React**: `cd com.top_logic.layout.react && npm run build`
   - Verify `tl-react-controls.js` includes TLAudioRecorder
3. **Functional test**:
   - Open demo page with audio recorder control
   - Click record → browser asks for microphone permission
   - Speak, click stop → audio uploads to server
   - Server receives binary data → status updates via SSE
   - Embedding component receives `BinaryDataSource` with audio bytes
   - Error case: deny microphone → graceful error display

---

## Phase II: Speech-to-Text Transcription (Future)

### Additional state

```json
{
  "status": "idle",
  "transcription": "",
  "error": null
}
```

**Extended status flow**: `idle` → `recording` → `uploading` → `transcribing` (server) → `idle` (with transcription)

### Files to Create

#### 1. `TranscriptionService.java` — new interface

**File**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/audio/TranscriptionService.java`

```java
public interface TranscriptionService {
    /**
     * Transcribes audio data to text.
     *
     * @param audioData Raw audio bytes.
     * @param contentType MIME type of the audio (e.g. "audio/webm").
     * @return Transcribed text.
     */
    String transcribe(byte[] audioData, String contentType) throws IOException;
}
```

#### 2. `OpenAITranscriptionService.java` — new implementation

**File**: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/audio/OpenAITranscriptionService.java`

Uses Apache HttpClient 5 (already a dependency via `com.top_logic.service.openapi.client`):

```java
public class OpenAITranscriptionService implements TranscriptionService {
    private final String _apiKey;
    private final String _model; // "whisper-1"

    @Override
    public String transcribe(byte[] audioData, String contentType) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.openai.com/v1/audio/transcriptions");
            post.setHeader("Authorization", "Bearer " + _apiKey);

            String extension = mimeToExtension(contentType); // "audio/webm" -> "webm"
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addBinaryBody("file", audioData, ContentType.parse(contentType), "audio." + extension)
                .addTextBody("model", _model);
            post.setEntity(builder.build());

            return client.execute(post, response -> {
                String json = EntityUtils.toString(response.getEntity());
                return (String) ((Map<?, ?>) JSON.fromString(json)).get("text");
            });
        }
    }
}
```

#### 3. Extend `ReactAudioRecorderControl`

Add optional `TranscriptionService` to the control. When present, after receiving audio:
1. Set status to `"transcribing"`
2. Call `_transcriptionService.transcribe(audioData, contentType)`
3. Patch state with `transcription` result
4. Invoke `_resultHandler` callback with transcribed text

### Design Decisions (Phase II)

#### API key configuration

The OpenAI API key should come from TopLogic's configuration system. `OpenAITranscriptionService` should implement `ConfiguredInstance` with a typed config interface:

```java
public interface Config extends PolymorphicConfiguration<OpenAITranscriptionService> {
    @Mandatory
    String getApiKey();

    @StringDefault("whisper-1")
    String getModel();
}
```

#### Synchronous vs asynchronous transcription

The initial implementation does transcription synchronously in the servlet thread. For production use, this should be async (e.g. via `SchedulerService`), but for a first pass synchronous is simpler since the OpenAI API typically responds in 1-3 seconds. Async can be added later.

## General Design Decisions

### Upload dispatch in ReactServlet

The `UploadHandler` interface keeps the upload path generic and reusable for any future control that needs binary uploads — not just audio recording.

### Audio format

`audio/webm` is used as the MediaRecorder format for broad browser support. OpenAI Whisper also accepts webm natively, so no server-side conversion is needed.
