import type { MarkdownProcessor, RehypePlugins, RemarkPlugins, RemarkRehype, Smartypants } from '@astrojs/internal-helpers/markdown';
export interface UnifiedProcessorOptions {
    remarkPlugins?: RemarkPlugins;
    rehypePlugins?: RehypePlugins;
    remarkRehype?: RemarkRehype;
    /** Enable GitHub-Flavored Markdown. Defaults to `true`. */
    gfm?: boolean;
    /** Enable SmartyPants typography. Defaults to `true`; pass an object to configure it. */
    smartypants?: boolean | Smartypants;
}
/**
 * Resolved options on the processor returned by `unified()`. Always populated
 * (the factory normalises absent inputs into defaults).
 */
export interface UnifiedResolvedOptions {
    remarkPlugins: RemarkPlugins;
    rehypePlugins: RehypePlugins;
    remarkRehype: RemarkRehype;
    gfm?: boolean;
    smartypants?: boolean | Smartypants;
}
/**
 * Use the default remark/rehype-based Markdown processor for `markdown.processor`.
 * Extend the pipeline with remark or rehype plugins, or pass options to `remark-rehype`.
 *
 * ```js
 * import { unified } from '@astrojs/markdown-remark';
 * import remarkToc from 'remark-toc';
 *
 * export default defineConfig({
 *   markdown: {
 *     processor: unified({ remarkPlugins: [remarkToc] }),
 *   },
 * });
 * ```
 */
export declare function unified(opts?: UnifiedProcessorOptions): MarkdownProcessor<UnifiedResolvedOptions>;
export declare function isUnifiedProcessor(p: {
    name: string;
}): p is MarkdownProcessor<UnifiedResolvedOptions>;
