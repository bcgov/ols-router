import fs from "node:fs/promises";
import { fileURLToPath } from "node:url";
import {
  markdownConfigDefaults
} from "@astrojs/internal-helpers/markdown";
import { getContainerRenderer as getContainerRendererImpl } from "./container-renderer.js";
import { isSatteriProcessor, isUnifiedProcessor } from "./processor-guards.js";
import { ignoreStringPlugins, safeParseFrontmatter } from "./utils.js";
import { vitePluginMdx } from "./vite-plugin-mdx.js";
import { vitePluginMdxPostprocess } from "./vite-plugin-mdx-postprocess.js";
function getContainerRenderer() {
  console.warn(
    "[@astrojs/mdx] Importing `getContainerRenderer` from `@astrojs/mdx` is deprecated. Import it from `@astrojs/mdx/container-renderer` instead."
  );
  return getContainerRendererImpl();
}
function mdx(partialMdxOptions = {}) {
  let vitePluginMdxOptions = {};
  return {
    name: "@astrojs/mdx",
    hooks: {
      "astro:config:setup": async (params) => {
        const { updateConfig, config, addPageExtension, addContentEntryType, addRenderer } = params;
        addRenderer({
          name: "astro:jsx",
          serverEntrypoint: new URL("../dist/server.js", import.meta.url)
        });
        addPageExtension(".mdx");
        addContentEntryType({
          extensions: [".mdx"],
          async getEntryInfo({ fileUrl, contents }) {
            const parsed = safeParseFrontmatter(contents, fileURLToPath(fileUrl));
            return {
              data: parsed.frontmatter,
              body: parsed.content.trim(),
              slug: parsed.frontmatter.slug,
              rawData: parsed.rawFrontmatter
            };
          },
          contentModuleTypes: await fs.readFile(
            new URL("../template/content-module-types.d.ts", import.meta.url),
            "utf-8"
          ),
          // MDX can import scripts and styles,
          // so wrap all MDX files with script / style propagation checks
          handlePropagation: true
        });
        updateConfig({
          vite: {
            plugins: [vitePluginMdx(vitePluginMdxOptions), vitePluginMdxPostprocess(config)]
          }
        });
      },
      "astro:config:done": ({ config, logger }) => {
        warnDeprecatedMdxPluginOptions(partialMdxOptions, logger);
        const extendMarkdownConfig = partialMdxOptions.extendMarkdownConfig ?? defaultMdxOptions.extendMarkdownConfig;
        const markdownConfig = extendMarkdownConfig ? config.markdown : markdownConfigDefaults;
        const resolvedMdxOptions = applyDefaultOptions({
          options: partialMdxOptions,
          defaults: markdownConfigToMdxOptions(markdownConfig, logger)
        });
        const processor = partialMdxOptions.processor ?? config.markdown.processor;
        if (extendMarkdownConfig && isUnifiedProcessor(processor)) {
          if (partialMdxOptions.remarkPlugins === void 0) {
            resolvedMdxOptions.remarkPlugins = ignoreStringPlugins(
              processor.options.remarkPlugins,
              logger
            );
          }
          if (partialMdxOptions.rehypePlugins === void 0) {
            resolvedMdxOptions.rehypePlugins = ignoreStringPlugins(
              processor.options.rehypePlugins,
              logger
            );
          }
          if (partialMdxOptions.remarkRehype === void 0) {
            resolvedMdxOptions.remarkRehype = { ...processor.options.remarkRehype };
          }
          if (partialMdxOptions.gfm === void 0 && processor.options.gfm !== void 0) {
            resolvedMdxOptions.gfm = processor.options.gfm;
          }
          if (partialMdxOptions.smartypants === void 0 && processor.options.smartypants !== void 0) {
            resolvedMdxOptions.smartypants = processor.options.smartypants;
          }
        }
        if (extendMarkdownConfig && isSatteriProcessor(processor)) {
          const features = processor.options.features;
          if (partialMdxOptions.gfm === void 0 && typeof features.gfm === "boolean") {
            resolvedMdxOptions.gfm = features.gfm;
          }
          if (partialMdxOptions.smartypants === void 0 && typeof features.smartPunctuation === "boolean") {
            resolvedMdxOptions.smartypants = features.smartPunctuation;
          }
        }
        Object.assign(vitePluginMdxOptions, {
          mdxOptions: resolvedMdxOptions,
          srcDir: config.srcDir,
          processor
        });
        vitePluginMdxOptions = {};
      }
    }
  };
}
const defaultMdxOptions = {
  extendMarkdownConfig: true
};
let didWarnAboutDeprecatedMdxPluginOptions = false;
function warnDeprecatedMdxPluginOptions(options, logger) {
  if (didWarnAboutDeprecatedMdxPluginOptions) return;
  const deprecated = ["remarkPlugins", "rehypePlugins", "remarkRehype"].filter(
    (key) => options[key] !== void 0
  );
  if (deprecated.length === 0) return;
  didWarnAboutDeprecatedMdxPluginOptions = true;
  const names = deprecated.map((key) => `\`${key}\``).join(", ");
  const isPlural = deprecated.length > 1;
  logger.warn(
    `${names} on \`mdx({...})\` ${isPlural ? "are" : "is"} deprecated. Pass ${isPlural ? "them" : "it"} to \`unified({...})\` from \`@astrojs/markdown-remark\` and set it as \`markdown.processor\` instead \u2014 MDX will inherit ${isPlural ? "them" : "it"}. Will be removed in a future major.`
  );
}
function markdownConfigToMdxOptions(markdownConfig, _logger) {
  return {
    ...markdownConfig,
    // Deprecated `markdown.{gfm,smartypants}` may be unset (optional in the schema);
    // fall back to the processor defaults so the MDX pipeline still enables them by default.
    gfm: markdownConfig.gfm ?? markdownConfigDefaults.gfm,
    smartypants: markdownConfig.smartypants ?? markdownConfigDefaults.smartypants,
    recmaPlugins: [],
    optimize: false,
    // Plugins come from the processor — merged in astro:config:done.
    remarkPlugins: [],
    rehypePlugins: [],
    remarkRehype: {}
  };
}
function applyDefaultOptions({
  options,
  defaults
}) {
  return {
    syntaxHighlight: options.syntaxHighlight ?? defaults.syntaxHighlight,
    shikiConfig: options.shikiConfig ?? defaults.shikiConfig,
    gfm: options.gfm ?? defaults.gfm,
    smartypants: options.smartypants ?? defaults.smartypants,
    recmaPlugins: options.recmaPlugins ?? defaults.recmaPlugins,
    optimize: options.optimize ?? defaults.optimize,
    remarkPlugins: options.remarkPlugins ?? defaults.remarkPlugins,
    rehypePlugins: options.rehypePlugins ?? defaults.rehypePlugins,
    remarkRehype: options.remarkRehype ?? defaults.remarkRehype
  };
}
export {
  mdx as default,
  getContainerRenderer
};
