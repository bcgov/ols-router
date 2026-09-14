import { type AstroMarkdownOptions } from '@astrojs/internal-helpers/markdown';
import type { AstroIntegration, AstroRenderer } from 'astro';
import type { MarkdownProcessor } from 'astro/markdown';
import type { Options as RemarkRehypeOptions } from 'remark-rehype';
import type { PluggableList } from 'unified';
import type { OptimizeOptions } from './rehype-optimize-static.js';
type SharedMarkdownOptions = Required<Pick<AstroMarkdownOptions, 'syntaxHighlight' | 'shikiConfig'>> & Pick<AstroMarkdownOptions, 'gfm' | 'smartypants'>;
export type MdxOptions = SharedMarkdownOptions & {
    extendMarkdownConfig: boolean;
    recmaPlugins: PluggableList;
    optimize: boolean | OptimizeOptions;
    /**
     * Override the markdown processor for `.mdx` files. Defaults to `config.markdown.processor`.
     * Use this to run `.mdx` files through a different processor (or the same processor with
     * different options) than your `.md` files.
     */
    processor?: MarkdownProcessor;
    /**
     * @deprecated Pass `remarkPlugins` to `unified({ remarkPlugins })` from `@astrojs/markdown-remark` and set it as `markdown.processor` instead — MDX will inherit them. Will be removed in a future major.
     */
    remarkPlugins: PluggableList;
    /**
     * @deprecated Pass `rehypePlugins` to `unified({ rehypePlugins })` from `@astrojs/markdown-remark` and set it as `markdown.processor` instead — MDX will inherit them. Will be removed in a future major.
     */
    rehypePlugins: PluggableList;
    /**
     * @deprecated Pass `remarkRehype` to `unified({ remarkRehype })` from `@astrojs/markdown-remark` and set it as `markdown.processor` instead — MDX will inherit it. Will be removed in a future major.
     */
    remarkRehype: RemarkRehypeOptions;
};
/**
 * @deprecated Import `getContainerRenderer` from `@astrojs/mdx/container-renderer` instead.
 */
export declare function getContainerRenderer(): AstroRenderer;
export default function mdx(partialMdxOptions?: Partial<MdxOptions>): AstroIntegration;
export {};
