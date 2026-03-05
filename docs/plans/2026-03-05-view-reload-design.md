# View Reload Design

**Ticket:** #29108
**Date:** 2026-03-05
**Status:** Approved

## Problem

`ViewServlet` caches parsed `ViewElement` trees in a `ConcurrentHashMap<String, ViewElement>` that is never invalidated. During development, XML view definition changes require a server restart to take effect.

## Solution: Timestamp-guarded cache entries

Replace the bare `ViewElement` cache value with an entry that also stores the source file's `lastModified` timestamp. On each request, compare the file's current timestamp against the cached one. If they differ, re-parse and replace the entry.

### Cache entry structure

A private inner class in `ViewServlet`:

```java
private static final class CachedView {
    final ViewElement _view;
    final long _lastModified;

    CachedView(ViewElement view, long lastModified) {
        _view = view;
        _lastModified = lastModified;
    }
}
```

Cache type: `ConcurrentHashMap<String, CachedView>`.

### Modified getOrLoadView() logic

```
getOrLoadView(viewPath):
  1. file = FileManager.getInstance().getIDEFileOrNull(viewPath)
  2. currentModified = (file != null) ? file.lastModified() : 0
  3. cached = _viewCache.get(viewPath)
  4. if cached != null AND cached.lastModified == currentModified:
       return cached.view
  5. view = loadView(viewPath)   // existing parsing logic, unchanged
  6. _viewCache.put(viewPath, new CachedView(view, currentModified))
  7. return view
```

Uses `put()` instead of `putIfAbsent()` to replace stale entries. Concurrent requests hitting a stale entry simultaneously may parse the same file twice — harmless since `ViewElement` trees are stateless. No additional synchronization needed.

### Always active

No dev-mode flag. The overhead of one `File.lastModified()` syscall per request is negligible compared to network I/O and HTML rendering.

### Edge cases

- **File deleted:** `getIDEFileOrNull()` returns `null`, `lastModified` = 0. Existing `loadView()` throws `ConfigurationException` — unchanged.
- **File created after cache miss:** Next request sees `lastModified > 0` (differs from cached `0`), triggers reload.
- **Rapid saves within same timestamp tick:** Filesystem resolution is typically 1s (ext4) or 10ms (NTFS). Acceptable for manual editing.

### Session scope

Only new page loads get the updated view. Existing sessions keep their current control tree until the user refreshes.

### What doesn't change

- `loadView()` XML parsing logic
- `ViewElement`, `UIElement`, channel system
- Session handling, bootstrap page, control tree creation
- No new configuration properties

## Alternatives considered

**File watcher (WatchService):** Event-driven, zero per-request overhead. Rejected due to platform-dependent reliability, background thread complexity, and being overkill for a sub-millisecond timestamp check.

**TTL-based cache expiry:** Entries expire after configurable duration. Rejected due to artificial delay and unnecessary complexity.
