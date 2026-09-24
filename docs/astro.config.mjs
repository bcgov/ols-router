import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import starlightOpenAPI, { openAPISidebarGroups } from "starlight-openapi";

export default defineConfig({
	site: "https://bcgov.github.io",
	base: "/ols-router",
	integrations: [
		starlight({
			title: "BC Route Planner",
			logo: {
				src: "./src/assets/bc-mark.svg",
				alt: "BC Government Logo",
			},
			customCss: ["./src/styles/bc-gov.css"],
			plugins: [
				starlightOpenAPI([
					{
						base: "api",
						schema: "public/openapi.json",
						sidebar: { label: "OpenAPI Reference" },
					},
				]),
			],
			social: [
				{
					icon: "github",
					label: "GitHub",
					href: "https://github.com/bcgov/ols-router",
				},
			],
			sidebar: [
				{
					label: "Getting Started",
					items: [
						"getting-started/overview",
						"getting-started/glossary",
						"getting-started/roadmap",
						"getting-started/api-key",
						"getting-started/notice",
					],
				},
				{
					label: "Developer Guide",
					items: [
						"developer-guide",
					],
				},
				{
					label: "Reference",
					items: [
						"reference/rpng-release-notes",
						"reference/rpng-acceptance-test-plan",
						"reference/itn-data-issues",
						"reference/technical-terms",
					],
				},
				...openAPISidebarGroups,
			],
		}),
	],
});
