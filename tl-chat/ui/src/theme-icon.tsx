import { React } from 'tl-react-bridge'

/**
 * Renders a TopLogic `ThemeImage` from its encoded form.
 *
 * The same three forms the engine's own controls render - a CSS icon font (`css:` / `colored:`) and
 * an image path - so an application names its rail icons exactly as it names a `<nav-item>`'s. The
 * engine has this component too, but it is part of its control bundle rather than of the bridge
 * every host module shares, so the chat carries its own dozen lines.
 */
export function ThemeIcon({ encoded, className }: { encoded: string; className?: string }) {
  if (encoded.startsWith('/') || encoded.startsWith('theme:')) {
    return <img src={encoded} alt="" className={className} />
  }
  const cssClass = encoded.startsWith('css:')
    ? encoded.substring(4)
    : encoded.startsWith('colored:')
      ? encoded.substring(8)
      : encoded
  return <i className={className ? `${cssClass} ${className}` : cssClass} aria-hidden="true" />
}
