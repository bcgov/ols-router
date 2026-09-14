import type { Root } from 'hast';
import type { VFile } from 'vfile';
import { ASTRO_IMAGE_ELEMENT, ASTRO_IMAGE_IMPORT, USES_ASTRO_IMAGE_FLAG } from './image-constants.js';
export { ASTRO_IMAGE_ELEMENT, ASTRO_IMAGE_IMPORT, USES_ASTRO_IMAGE_FLAG };
export declare function rehypeImageToComponent(): (tree: Root, file: VFile) => void;
