import type { RehypePlugin } from '@astrojs/internal-helpers/markdown';
/**
 * Rehype plugin that adds `id` attributes to headings based on their text content.
 *
 * @see https://docs.astro.build/en/guides/markdown-content/#heading-ids-and-plugins
 */
export declare function rehypeHeadingIds(): ReturnType<RehypePlugin>;
