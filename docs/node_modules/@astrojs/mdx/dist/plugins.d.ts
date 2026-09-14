import type { ResolvedMdxOptions } from './index.js';
interface MdxProcessorExtraOptions {
    sourcemap: boolean;
}
export declare function createMdxProcessor(mdxOptions: ResolvedMdxOptions, extraOptions: MdxProcessorExtraOptions): import("unified").Processor<import("mdast").Root, import("estree").Program, import("estree").Program, import("estree").Program, string>;
export {};
