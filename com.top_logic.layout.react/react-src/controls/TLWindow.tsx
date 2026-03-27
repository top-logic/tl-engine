import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef, useState } = React;

const I18N_KEYS = {
  'js.window.close': 'Close',
  'js.window.maximize': 'Maximize',
  'js.window.restore': 'Restore',
};

type ResizeDir = 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'nw';

const RESIZE_HANDLES: ResizeDir[] = ['n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw'];

/**
 * Window chrome: title bar, close button, scrollable body, footer actions, resize handles.
 *
 * State:
 * - title: string
 * - width: string (CSS value, e.g. "500px")
 * - height: string | null
 * - resizable: boolean
 * - child: ChildDescriptor
 * - actions: ChildDescriptor[]
 */
const TLWindow: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const title = (state.title as string) ?? '';
  const serverWidth = (state.width as string) ?? '32rem';
  const serverHeight = (state.height as string | null) ?? null;
  const serverMinHeight = (state.minHeight as string | null) ?? null;
  const resizable = state.resizable === true;
  const child = state.child;
  const actions = (state.actions as unknown[]) ?? [];
  const toolbarButtons = (state.toolbarButtons as unknown[]) ?? [];

  // Local dimensions during resize (null = use server values).
  const [localWidth, setLocalWidth] = useState<number | null>(null);
  const [localHeight, setLocalHeight] = useState<number | null>(null);

  // Position state: null = centered by flexbox, set = absolute positioning.
  const [position, setPosition] = useState<{ x: number; y: number } | null>(null);
  const positionRef = useRef<{ x: number; y: number } | null>(null);

  // Maximize state.
  const [maximized, setMaximized] = useState(false);
  const regularBoundsRef = useRef<{ x: number; y: number; w: number | null; h: number | null } | null>(null);

  // Refs to track latest local dimensions inside event handlers (avoids stale closures).
  const localWidthRef = useRef<number | null>(null);
  const localHeightRef = useRef<number | null>(null);

  const windowRef = useRef<HTMLDivElement>(null);
  const dragState = useRef<{
    dir: ResizeDir;
    startX: number;
    startY: number;
    startW: number;
    startH: number;
    startPos: { x: number; y: number };
  } | null>(null);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleMouseDown = useCallback((dir: ResizeDir, e: React.MouseEvent) => {
    e.preventDefault();
    const el = windowRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    dragState.current = {
      dir,
      startX: e.clientX,
      startY: e.clientY,
      startW: rect.width,
      startH: rect.height,
      startPos: positionRef.current ? { ...positionRef.current } : { x: rect.left, y: rect.top },
    };

    const handleMouseMove = (ev: MouseEvent) => {
      const ds = dragState.current;
      if (!ds) return;
      const dx = ev.clientX - ds.startX;
      const dy = ev.clientY - ds.startY;
      let w = ds.startW;
      let h = ds.startH;

      // Calculate position deltas for N/W handles.
      let posXDelta = 0;
      let posYDelta = 0;

      if (ds.dir.includes('e')) w = ds.startW + dx;
      if (ds.dir.includes('w')) { w = ds.startW - dx; posXDelta = dx; }
      if (ds.dir.includes('s')) h = ds.startH + dy;
      if (ds.dir.includes('n')) { h = ds.startH - dy; posYDelta = dy; }

      const newW = Math.max(200, w);
      const newH = Math.max(100, h);

      // Clamp position deltas if size hit minimum.
      if (ds.dir.includes('w') && newW === 200) posXDelta = ds.startW - 200;
      if (ds.dir.includes('n') && newH === 100) posYDelta = ds.startH - 100;

      localWidthRef.current = newW;
      localHeightRef.current = newH;
      setLocalWidth(newW);
      setLocalHeight(newH);

      // Update position when resizing from N/W edge.
      if (posXDelta !== 0 || posYDelta !== 0) {
        const newPos = {
          x: ds.startPos.x + posXDelta,
          y: ds.startPos.y + posYDelta,
        };
        positionRef.current = newPos;
        setPosition(newPos);
      }
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      const lw = localWidthRef.current;
      const lh = localHeightRef.current;
      if (lw != null || lh != null) {
        sendCommand('resize', {
          ...(lw != null ? { width: Math.round(lw) + 'px' } : {}),
          ...(lh != null ? { height: Math.round(lh) + 'px' } : {}),
        });
        // Keep local dimensions — server uses putStateSilent() which does not push back.
      }
      dragState.current = null;
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  }, [sendCommand]);

  const handleTitleMouseDown = useCallback((e: React.MouseEvent) => {
    // Only left mouse button; ignore clicks on buttons inside the header.
    if (e.button !== 0 || (e.target as HTMLElement).closest('button')) return;
    e.preventDefault();

    const el = windowRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();

    // If first drag, initialize position from current rendered position.
    const startPos = positionRef.current ?? { x: rect.left, y: rect.top };
    const offsetX = e.clientX - startPos.x;
    const offsetY = e.clientY - startPos.y;

    const handleMouseMove = (ev: MouseEvent) => {
      const viewW = window.innerWidth;
      const viewH = window.innerHeight;
      let newX = ev.clientX - offsetX;
      let newY = ev.clientY - offsetY;

      // Constrain to viewport.
      const elW = el.offsetWidth;
      const elH = el.offsetHeight;
      if (newX + elW > viewW) newX = viewW - elW;
      if (newY + elH > viewH) newY = viewH - elH;
      if (newX < 0) newX = 0;
      if (newY < 0) newY = 0;

      const pos = { x: newX, y: newY };
      positionRef.current = pos;
      setPosition(pos);
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  }, []);

  const handleToggleMaximize = useCallback(() => {
    if (maximized) {
      // Restore.
      const rb = regularBoundsRef.current;
      if (rb) {
        setPosition(rb.x !== -1 ? { x: rb.x, y: rb.y } : null);
        setLocalWidth(rb.w);
        setLocalHeight(rb.h);
      }
      setMaximized(false);
    } else {
      // Save current bounds.
      const el = windowRef.current;
      const rect = el?.getBoundingClientRect();
      regularBoundsRef.current = {
        x: positionRef.current?.x ?? (rect?.left ?? -1),
        y: positionRef.current?.y ?? (rect?.top ?? -1),
        w: localWidth ?? (rect?.width ?? null),
        h: localHeight ?? null,
      };
      setMaximized(true);
      setPosition({ x: 0, y: 0 });
      setLocalWidth(null);
      setLocalHeight(null);
    }
  }, [maximized, localWidth, localHeight]);

  const style: React.CSSProperties = maximized
    ? { position: 'absolute' as const, top: 0, left: 0, width: '100vw', height: '100vh', maxHeight: '100vh', borderRadius: 0 }
    : {
        width: localWidth != null ? localWidth + 'px' : serverWidth,
        ...(localHeight != null
          ? { height: localHeight + 'px' }
          : serverHeight != null
            ? { height: serverHeight }
            : {}),
        ...(serverMinHeight != null && localHeight == null
          ? { minHeight: serverMinHeight }
          : {}),
        maxHeight: position ? '100vh' : '80vh',
        ...(position
          ? { position: 'absolute' as const, left: position.x + 'px', top: position.y + 'px' }
          : {}),
      };

  const titleId = controlId + '-title';

  return (
    <div
      id={controlId}
      className="tlWindow"
      style={style}
      ref={windowRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
    >
      <div
        className={`tlWindow__header${maximized ? ' tlWindow__header--maximized' : ''}`}
        onMouseDown={maximized ? undefined : handleTitleMouseDown}
        onDoubleClick={handleToggleMaximize}
      >
        <span className="tlWindow__title" id={titleId}>{title}</span>
        {toolbarButtons.length > 0 && (
          <div className="tlWindow__toolbar">
            {toolbarButtons.map((btn, i) => (
              <span key={i} className="tlWindow__toolbarButton">
                <TLChild control={btn} />
              </span>
            ))}
          </div>
        )}
        <button
          type="button"
          className="tlWindow__maximizeBtn"
          onClick={handleToggleMaximize}
          title={maximized ? i18n['js.window.restore'] : i18n['js.window.maximize']}
        >
          {maximized ? (
            // Restore icon: two overlapping squares.
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <rect x="4" y="8" width="12" height="12" rx="1.5" fill="none" stroke="currentColor" strokeWidth="2" />
              <path d="M8 8V5.5A1.5 1.5 0 0 1 9.5 4H18.5A1.5 1.5 0 0 1 20 5.5V14.5A1.5 1.5 0 0 1 18.5 16H16" fill="none" stroke="currentColor" strokeWidth="2" />
            </svg>
          ) : (
            // Maximize icon: single square.
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <rect x="4" y="4" width="16" height="16" rx="1.5" fill="none" stroke="currentColor" strokeWidth="2" />
            </svg>
          )}
        </button>
        <button
          type="button"
          className="tlWindow__closeBtn"
          onClick={handleClose}
          title={i18n['js.window.close']}
        >
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" />
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" />
          </svg>
        </button>
      </div>
      <div className="tlWindow__body">
        <TLChild control={child} />
      </div>
      {actions.length > 0 && (
        <div className="tlWindow__footer">
          {actions.map((action, i) => (
            <TLChild key={i} control={action} />
          ))}
        </div>
      )}
      {resizable && !maximized && RESIZE_HANDLES.map(dir => (
        <div
          key={dir}
          className={`tlWindow__resizeHandle tlWindow__resizeHandle--${dir}`}
          onMouseDown={(e) => handleMouseDown(dir, e)}
        />
      ))}
    </div>
  );
};

export default TLWindow;
