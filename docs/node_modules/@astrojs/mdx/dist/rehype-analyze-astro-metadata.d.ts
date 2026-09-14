import type { AstroMetadata, RehypePlugin } from '@astrojs/internal-helpers/markdown';
import type { VFile } from 'vfile';
export declare const rehypeAnalyzeAstroMetadata: RehypePlugin;
export declare function getAstroMetadata(file: VFile): AstroMetadata | undefined;
