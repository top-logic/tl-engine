declare module 'tl-react-bridge' {
  import type * as ReactTypes from 'react'

  export interface TLCellProps {
    controlId: string
    state: Record<string, unknown>
    config?: Record<string, unknown>
  }

  export const React: typeof ReactTypes

  export function register(
    name: string,
    component: ReactTypes.ComponentType<TLCellProps>,
  ): void

  /**
   * Renders a child control the server put into this control's state.
   *
   * A container element's Java control serializes each child it displays as `{controlId, module,
   * state}`; this component looks the module up, gives the child its own state store and SSE
   * subscription, and renders it. That is what lets a chat element host arbitrary configured view
   * elements - the steps of a `<presentation-pane>`, for one.
   */
  export const TLChild: ReactTypes.FC<{ control: unknown }>

  /**
   * Ends the view layer's fill chain for the children rendered inside: a child that fills resolves
   * its height against the element around them, and nothing further out reacts to it.
   */
  export const FillBarrier: ReactTypes.FC<{ children?: ReactTypes.ReactNode }>

  /**
   * Opens a keyboard scope for its children on the dispatcher's stack of scopes: the innermost
   * active scope binding a gesture handles it, the scopes around it only when none inside did.
   */
  export const KeyboardScopeProvider: ReactTypes.FC<{
    active?: () => boolean
    modal?: boolean
    children?: ReactTypes.ReactNode
  }>

  /** Binds Escape to `onClose` in the enclosing keyboard scope. */
  export const EscapeToClose: ReactTypes.FC<{ onClose: () => void }>

  export function useTLState(): Record<string, unknown>

  /**
   * Listens to the patches the window's event stream addresses to `controlId`, each one as it
   * arrives. The id need not belong to a mounted control: the chat's frames travel under an id of
   * their own (`openChatStream`).
   */
  export function subscribe(controlId: string, listener: (patch: Record<string, unknown>) => void): void

  /** Whether the page is navigating away and must not talk to the server any more. */
  export function isPageLeaving(): boolean

  /**
   * Whether the key belongs to an input method composition (for example for Japanese), including the
   * Enter that ends it, which Safari reports after the end of the composition. A key handler leaves
   * such a key to the input method; a React handler passes `event.nativeEvent`.
   */
  export function isCompositionKey(event: KeyboardEvent): boolean

  /** Stops a listener `subscribe` added. */
  export function unsubscribe(controlId: string, listener: (patch: Record<string, unknown>) => void): void

  /**
   * The class name of a control's root element: the given classes the component brings itself,
   * followed by the `css-class` the view configured for this one element. Falsy entries are left out.
   */
  export function rootClassName(
    state: Record<string, unknown>,
    ...classes: (string | false | null | undefined)[]
  ): string

  /**
   * The parsed answer of a command: `success`, and the fields of the msgbuf message the command's
   * handler returned on the server (`ReactCommandAnswer`), read by the names both sides agree on.
   */
  export type CommandResponse = Record<string, unknown>

  /** Options of a command sent through the function {@link useTLCommand} hands out. */
  export interface CommandOptions {
    /** Whether the pending debounced field values are sent before the command; absent: `true`. */
    flushFields?: boolean
    /**
     * Whether an answer saying that the UI the command was sent from is outdated - the server no
     * longer knows this page, or the addressed control is no longer displayed - is dropped silently;
     * absent: `false`. For a command that only tidies up after something that is gone.
     */
    ignoreStaleUi?: boolean
    /**
     * Whether the command runs in the background; absent: `false`. A command the user does not wait
     * for - a report, a query the user keeps typing beside, a tidy-up - never locks the page while
     * it is processed.
     */
    background?: boolean
  }

  /** The {@link CommandOptions} of a background command. */
  export const BACKGROUND_COMMAND: Readonly<CommandOptions>

  /**
   * The function that sends a command to the control the calling component renders, through the
   * window's FIFO command channel. It resolves with the command's answer, or with `undefined` when
   * the command failed or the page started leaving before its turn came; it never rejects.
   */
  export function useTLCommand(): (
    command: string,
    args?: Record<string, unknown>,
    options?: CommandOptions,
  ) => Promise<CommandResponse | undefined>

  /**
   * The function that uploads files to the control the calling component renders (`UploadHandler`
   * on the server). It resolves with the answer of the upload, or with `undefined` when the upload
   * failed or was not transmitted - a file larger than the control's `maxUploadSize` state is
   * refused before it is sent; it never rejects.
   */
  export function useTLUpload(): (formData: FormData) => Promise<CommandResponse | undefined>

  /**
   * The URL of the data the control the calling component renders serves (`DataProvider` on the
   * server), to which the caller appends `&key=…`.
   */
  export function useTLDataUrl(): string

  /**
   * The state of a badge: a count or short text, or a dot, in a color role, with the name a screen
   * reader gives it where the content does not say it itself.
   */
  export interface BadgeJson {
    /** What the badge shows: a count, the overflow `99+`, or a short text. Absent for a dot. */
    content: string
    /** Whether the badge is a dot marking the element without counting. */
    dot: boolean
    /** The color role. Absent means `'brand'`. */
    role: BadgeJson.Role
    /** The accessible name of a dot, or what a count counts. */
    label: string
  }

  export namespace BadgeJson {
    /** The color role of a badge. */
    export type Role = 'brand' | 'error' | 'warning' | 'success' | 'info'
  }

  /**
   * Draws the given badge through the part `Badge`, which a component library may replace.
   *
   * `alone`: the badge stands over the corner of an icon whose element is named by
   * {@link labelWithBadge}, so the badge itself is hidden from assistive technology.
   */
  export const StateBadge: ReactTypes.FC<{ badge: Partial<BadgeJson>; alone: boolean }>

  /** The accessible name of an element named by the given label and carrying the given badge. */
  export function labelWithBadge(
    label: string | undefined,
    badge: Partial<BadgeJson> | null | undefined,
  ): string | undefined

  /** The encoded theme image of the "previous" arrow, e.g. of a back button. */
  export const ICON_PREVIOUS: string

  /** The encoded theme image of a close button. */
  export const ICON_CLOSE: string

  /**
   * The texts of the given i18n keys in the language of the session, each falling back to the
   * default given with it until the translations have arrived.
   */
  export function useI18N<K extends string>(keys: Record<K, string>): Record<K, string>

  /** The props of the part `Button`, as far as this bundle draws buttons through it. */
  export interface ButtonPartProps {
    label?: string
    image?: string
    display: 'icon-only' | 'icon-label' | 'label-only'
    appearance: 'primary' | 'secondary' | 'ghost' | 'link'
    onClick?: (event: ReactTypes.MouseEvent<HTMLElement>) => void
  }

  /** The drawing of a part, which a component library may replace. */
  export function usePart(name: 'Button'): ReactTypes.FC<ButtonPartProps>
}
