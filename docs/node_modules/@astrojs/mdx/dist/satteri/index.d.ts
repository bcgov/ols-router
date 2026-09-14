import type { MarkdownHeading } from '@astrojs/internal-helpers/markdown';
import type { SatteriResolvedOptions } from '@astrojs/markdown-satteri';
import { type HastPluginDefinition, type MdastPluginDefinition } from 'satteri';
import type { ResolvedMdxOptions } from '../index.js';
import { type AstroMetadata } from './hast-astro-metadata.js';
export type { MdastPluginDefinition, HastPluginDefinition, MarkdownHeading };
export type { AstroMetadata } from './hast-astro-metadata.js';
declare module 'hast' {
    interface ElementData {
        lang?: string | null;
    }
}
export interface CompileMdxResult {
    code: string;
    astroMetadata: AstroMetadata;
}
interface CreateMdxProcessorContext {
    srcDir: URL;
}
export declare function createMdxProcessor(mdxOptions: ResolvedMdxOptions, satteriOptions: SatteriResolvedOptions, ctx: CreateMdxProcessorContext): {
    process(content: string, filePath: string, frontmatter: Record<string, any>): Promise<CompileMdxResult>;
};
