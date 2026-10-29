# tl-ai-mcp-server

## Code Style

- Use Java 21 features including pattern matching for instanceof
- Avoid unnecessary casts when the type system can infer types
- Use TLObject instead of Wrapper when sufficient
- Package-private methods (no modifier) for utilities that need testing

## Testing

- Test production code directly rather than duplicating logic in tests
- Make utility methods package-private to enable testing without exposing public API
