import {
  React, useTLState, useTLCommand, TLChild, rootClassName, stateAttr, usePart, useI18N,
} from 'tl-react-bridge';
import type { TLCellProps, SideRailStateJson } from 'tl-react-bridge';

const { useCallback, useLayoutEffect, useRef, useState } = React;

/** Command of a click on a strip icon; the server's `ReactSideRailControl.SHOW_SECTION_COMMAND`. */
export const CMD_SHOW_SECTION = 'showSection';

/** Command reporting whether the rail is a drawer; the server's `ReactSideRailControl.SET_DRAWER_COMMAND`. */
export const CMD_SET_DRAWER = 'setDrawer';

/** Command reporting the dragged width; the server's `ReactSideRailControl.SET_WIDTH_COMMAND`. */
export const CMD_SET_WIDTH = 'setWidth';

/** Argument naming a section; the server's `ReactSideRailControl.NAME_ARG`. */
export const ARG_NAME = 'name';

/** Argument of {@link CMD_SET_DRAWER}; the server's `ReactSideRailControl.DRAWER_ARG`. */
export const ARG_DRAWER = 'drawer';

/** Argument of {@link CMD_SET_WIDTH}; the server's `ReactSideRailControl.WIDTH_ARG`. */
export const ARG_WIDTH = 'width';

/** The glyph of a section without an icon of its own. */
export const DEFAULT_SECTION_ICON = 'css:fa-regular fa-window-maximize';

/** How far one press of an arrow key moves the inner edge of the column, in px. */
const KEY_STEP = 16;

/** The narrowest column a drag leaves, in px. */
const MIN_WIDTH = 160;

const I18N_KEYS = {
  'js.sideRail.strip': 'Sections',
  'js.sideRail.resize': 'Width of the side column',
};

/** The root font size in px, which is what the `drawerBelow` rem value is measured in. */
function remInPx(): number {
  return parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
}

/**
 * The area the rail measures itself against: the element its drawer is anchored in, the nearest
 * positioned ancestor, else its parent.
 */
function anchorOf(element: HTMLElement): HTMLElement | null {
  return (element.offsetParent as HTMLElement | null) ?? element.parentElement;
}

/**
 * A collapsible column beside another element, with sections (design system: tl-side-rail), the
 * client of `<side-rail>`. The state is described by SideRailStateJson.
 *
 * The strip shows an icon per section, with the badge of the section; a click opens the rail with the
 * section expanded, a second click on a shown section closes it. While the rail is open, the column
 * shows the sections (the child `body`, a `TLSideRailSections`). The column stays mounted while the
 * rail is closed, so what the user did in it survives.
 *
 * Where the area of the rail - the element its drawer is anchored in - is narrower than `drawerBelow`
 * rem, the rail keeps only its strip and renders the child `drawer` instead of the column: a modal
 * drawer over the neighbour, which brings its own scrim, focus handling and Escape. The rail reports
 * the change to the server, which then opens and closes the drawer. Measured before paint, so that a
 * narrow area does not first lay out the column it cannot hold.
 */
const TLSideRail: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SideRailStateJson>>();
  const command = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  const sections = (state.sections ?? []).filter((section) => !section.hidden);
  const end = state.side === 'right';
  const strip = state.strip !== false;
  const open = state.open === true;
  const drawerBelow = typeof state.drawerBelow === 'number' ? state.drawerBelow : 0;
  const resizable = state.resizable === true;
  const width = typeof state.width === 'number' ? state.width : 0;
  const sectionWidth = typeof state.sectionWidth === 'number' ? state.sectionWidth : 0;

  const root = useRef<HTMLDivElement | null>(null);
  const [narrow, setNarrow] = useState(false);
  /** What the server was told last; it starts as a column. */
  const reported = useRef(false);

  useLayoutEffect(() => {
    const element = root.current;
    const anchor = element ? anchorOf(element) : null;
    if (drawerBelow <= 0 || !anchor) {
      setNarrow(false);
      return;
    }
    const measure = () => setNarrow(anchor.clientWidth < drawerBelow * remInPx());
    measure();
    const observer = new ResizeObserver(measure);
    observer.observe(anchor);
    return () => observer.disconnect();
  }, [drawerBelow]);

  useLayoutEffect(() => {
    if (reported.current === narrow) return;
    reported.current = narrow;
    void command(CMD_SET_DRAWER, { [ARG_DRAWER]: narrow });
  }, [command, narrow]);

  /** The width a drag in progress gives the column, before the server knows it. */
  const [dragWidth, setDragWidth] = useState<number | null>(null);
  const columnWidth = dragWidth ?? (sectionWidth > 0 ? sectionWidth : width);

  const commitWidth = useCallback((next: number) => {
    setDragWidth(null);
    void command(CMD_SET_WIDTH, { [ARG_WIDTH]: Math.round(next) });
  }, [command]);

  const onHandlePointerDown = (event: React.PointerEvent<HTMLDivElement>) => {
    event.preventDefault();
    const startX = event.clientX;
    const startWidth = columnWidth;
    const rtl = root.current ? getComputedStyle(root.current).direction === 'rtl' : false;
    // A column opening towards the end grows with the pointer moving towards the end.
    const sign = (end ? -1 : 1) * (rtl ? -1 : 1);
    let current = startWidth;
    const move = (e: PointerEvent) => {
      current = Math.max(MIN_WIDTH, startWidth + sign * (e.clientX - startX));
      setDragWidth(current);
    };
    const up = () => {
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', up);
      commitWidth(current);
    };
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', up);
  };

  const onHandleKey = (event: React.KeyboardEvent<HTMLDivElement>) => {
    const grow = end ? 'ArrowLeft' : 'ArrowRight';
    const shrink = end ? 'ArrowRight' : 'ArrowLeft';
    if (event.key !== grow && event.key !== shrink) return;
    event.preventDefault();
    commitWidth(Math.max(MIN_WIDTH, columnWidth + (event.key === grow ? KEY_STEP : -KEY_STEP)));
  };

  const showSection = (name: string) => {
    void command(CMD_SHOW_SECTION, { [ARG_NAME]: name });
  };

  return (
    <div
      id={controlId}
      ref={root}
      className={rootClassName(state, 'tl-side-rail', end ? 'tl-side-rail--end' : 'tl-side-rail--start')}
      data-tl-state={stateAttr(open && 'open', narrow && 'drawer', resizable && 'resizable')}
    >
      {strip ? (
        <nav className="tl-side-rail__strip" aria-label={i18n['js.sideRail.strip']}>
          {sections.map((section) => {
            const shown = open && section.expanded === true;
            return (
              <Button
                key={section.name}
                className="tl-side-rail__strip-item"
                label={section.label}
                image={section.icon || DEFAULT_SECTION_ICON}
                display="icon-only"
                appearance="ghost"
                checked={shown}
                badge={section.badge ?? null}
                onClick={() => showSection(section.name)}
              />
            );
          })}
        </nav>
      ) : null}
      {narrow ? (
        state.drawer ? <TLChild control={state.drawer} /> : null
      ) : (
        <div
          className="tl-side-rail__column"
          hidden={!open}
          style={resizable && columnWidth > 0 ? { width: `${columnWidth}px` } : undefined}
        >
          {state.body ? <TLChild control={state.body} /> : null}
          {resizable ? (
            <div
              className="tl-side-rail__handle"
              role="separator"
              aria-orientation="vertical"
              aria-label={i18n['js.sideRail.resize']}
              aria-valuenow={columnWidth}
              tabIndex={0}
              onPointerDown={onHandlePointerDown}
              onKeyDown={onHandleKey}
            />
          ) : null}
        </div>
      )}
    </div>
  );
};

export default TLSideRail;
