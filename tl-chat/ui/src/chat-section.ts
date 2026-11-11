import { BACKGROUND_COMMAND, React } from 'tl-react-bridge'
import type { CommandOptions } from 'tl-react-bridge'

const { useEffect, useRef } = React

/**
 * Reports a section's current count on its {@code badge} channel.
 *
 * The chat's counts are derived in the browser store, and the rail is a separate view element that
 * sees nothing but its channels - so the number has to travel browser to server to rail. Only a
 * change is sent, and only when a channel is configured, so a section nobody counts costs nothing.
 * The report runs in the background.
 */
export function useBadgeChannel(
  command: (name: string, args?: Record<string, unknown>, options?: CommandOptions) => Promise<unknown>,
  enabled: boolean,
  count: number,
): void {
  const reported = useRef<number | null>(null)
  useEffect(() => {
    if (!enabled) return
    if (reported.current === count) return
    reported.current = count
    void command('badge', { count }, BACKGROUND_COMMAND)
  }, [command, enabled, count])
}
