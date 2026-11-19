import { describe, expect, it } from 'vitest';
import { DROP_ARG_X, DROP_ARG_Y, putDropPoint } from './drag-drop';

describe('putDropPoint', () => {
  it('adds both coordinates of a point', () => {
    const args: Record<string, unknown> = { zone: 'none' };
    expect(putDropPoint(args, { x: 12.5, y: -3 })).toBe(args);
    expect(args).toEqual({ zone: 'none', [DROP_ARG_X]: 12.5, [DROP_ARG_Y]: -3 });
  });

  it('leaves the arguments of a drop without a point unchanged', () => {
    const args: Record<string, unknown> = { zone: 'none' };
    putDropPoint(args, null);
    putDropPoint(args, undefined);
    expect(args).toEqual({ zone: 'none' });
  });
});
