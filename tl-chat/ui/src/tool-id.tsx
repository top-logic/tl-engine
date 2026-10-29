import { React } from 'tl-react-bridge'

/**
 * A tool, named by its technical id and set as code.
 *
 * Tools have no display name of their own - the registry records an id and an English description
 * written for the model. The id is shown as it is rather than read out as a sentence: set as code,
 * it is plainly a name, and it is the same word the trace, the documentation and the model use.
 * Prettifying it invented English phrases, in a German interface too (#550).
 */
export function ToolId({ id }: { id: string }) {
  return <code className="tool-id">{id}</code>
}
