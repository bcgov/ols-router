function unified(opts = {}) {
  const processor = {
    name: "unified",
    options: {
      remarkPlugins: [...opts.remarkPlugins ?? []],
      rehypePlugins: [...opts.rehypePlugins ?? []],
      remarkRehype: { ...opts.remarkRehype },
      gfm: opts.gfm,
      smartypants: opts.smartypants
    },
    async createRenderer(shared) {
      const { createMarkdownProcessor } = await import("./index.js");
      return createMarkdownProcessor({
        ...shared,
        remarkPlugins: processor.options.remarkPlugins,
        rehypePlugins: processor.options.rehypePlugins,
        remarkRehype: processor.options.remarkRehype,
        // `unified({ gfm, smartypants })` wins; fall back to the deprecated
        // top-level `markdown.gfm` / `markdown.smartypants` while they still exist.
        gfm: processor.options.gfm ?? shared.gfm,
        smartypants: processor.options.smartypants ?? shared.smartypants
      });
    }
  };
  return processor;
}
function isUnifiedProcessor(p) {
  return p.name === "unified";
}
export {
  isUnifiedProcessor,
  unified
};
