package ca.bc.gov.ols.router.rest.openapi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.rest.config.JtsFieldFilterCustomizer;
import ca.bc.gov.ols.router.rest.controllers.RoutingController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Generates the OpenAPI 3.x specification for the ols-router-web REST API.
 *
 * <p>Run via: {@code mvn install -pl ols-router-core -DskipTests && mvn test -pl ols-router-web -Popenapi-spec}
 *
 * <p>The generated spec is written to {@code target/openapi.json}.
 *
 * <p>The {@link Router} bean is mocked because the OpenAPI specification is
 * derived entirely from the controller and parameter annotations, so no road
 * network dataset or configuration store is required.
 */
@SpringBootTest(classes = {OpenApiSpecGeneratorTest.MockContext.class})
@AutoConfigureMockMvc(addFilters = false)
@Tag("OpenApi")
public class OpenApiSpecGeneratorTest {

	private static final String SPEC_PATH = "target/openapi.json";

	@Configuration
	@EnableAutoConfiguration(exclude = { CassandraAutoConfiguration.class,
			UserDetailsServiceAutoConfiguration.class })
	@Import({ RoutingController.class, JtsFieldFilterCustomizer.class })
	static class MockContext {

		@Bean
		Router router() {
			return Mockito.mock(Router.class);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void generateOpenApiSpec() throws Exception {
		MvcResult result = mockMvc.perform(get("/v3/api-docs")
						.accept("application/json"))
				.andExpect(status().isOk())
				.andReturn();

		String spec = result.getResponse().getContentAsString();

		Path outputPath = Paths.get(SPEC_PATH);
		Files.createDirectories(outputPath.getParent());
		Files.writeString(outputPath, spec);

		System.out.println("OpenAPI spec written to " + SPEC_PATH);
	}
}