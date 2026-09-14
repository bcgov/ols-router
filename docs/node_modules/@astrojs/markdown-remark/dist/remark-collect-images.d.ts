import type { Root } from 'mdast';
import type { VFile } from 'vfile';
import type { AstroMarkdownOptions } from '@astrojs/internal-helpers/markdown';
export declare function remarkCollectImages(opts: AstroMarkdownOptions['image']): (tree: Root, vfile: VFile) => void;
