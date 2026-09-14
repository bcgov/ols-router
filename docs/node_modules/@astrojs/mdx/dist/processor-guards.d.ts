import type { UnifiedResolvedOptions } from '@astrojs/markdown-remark';
import type { SatteriResolvedOptions } from '@astrojs/markdown-satteri';
import type { MarkdownProcessor } from 'astro/markdown';
export declare const isUnifiedProcessor: (p: {
    name: string;
}) => p is MarkdownProcessor<UnifiedResolvedOptions>;
export declare const isSatteriProcessor: (p: {
    name: string;
}) => p is MarkdownProcessor<SatteriResolvedOptions>;
