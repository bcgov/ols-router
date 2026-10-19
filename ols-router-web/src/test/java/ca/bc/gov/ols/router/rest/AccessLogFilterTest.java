/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.bc.gov.ols.router.rest.exceptions.ExceptionMapper;

@Tag("Prod")
class AccessLogFilterTest {

	private final AccessLogFilter filter = new AccessLogFilter();

	@Test
	void logsSuccessfulRequestWithForwardedClient() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/route");
		request.setQueryString("from=1&to=2");
		request.addHeader("X-Forwarded-For", "192.0.2.1, 10.0.0.1");
		String line = capture(() -> filter.doFilter(request, new MockHttpServletResponse(),
				(req, res) -> {}));
		assertTrue(line.startsWith("ACCESS "));
		assertTrue(line.contains("client=192.0.2.1 method=GET path=/route params=\"from=1&to=2\" status=200 time_ms="));
		assertFalse(line.contains(" error="));
		assertEquals(1, line.lines().count());
	}

	@Test
	void logsMappedErrorMessageButDoesNotChangeResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/route");
		request.setRemoteAddr("203.0.113.3");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String line = capture(() -> filter.doFilter(request, response, (req, res) -> {
			var error = new ExceptionMapper().handleRuntimeException(
					new IllegalStateException("failed\nextra details"), request);
			assertTrue(error.getBody().getMessage().contains("java.lang.IllegalStateException"));
			response.setStatus(error.getStatusCode().value());
		}));
		assertTrue(line.contains("client=203.0.113.3"));
		assertTrue(line.contains("status=500"));
		assertTrue(line.contains("error=\"Unexpected internal problem: failed\""));
		assertEquals(1, line.lines().count());
	}

	@Test
	void logsEscapingExceptionOnceAs500() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/route");
		String line = capture(() -> assertThrows(IllegalArgumentException.class,
				() -> filter.doFilter(request, new MockHttpServletResponse(),
						(req, res) -> { throw new IllegalArgumentException("bad\nother line"); })));
		assertTrue(line.contains("status=500"));
		assertTrue(line.contains("error=\"java.lang.IllegalArgumentException: bad\""));
		assertEquals(1, line.lines().count());
	}

	@Test
	void logsMessageForHandledBadRequestAndFallbackForOtherErrors() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bad");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String line = capture(() -> filter.doFilter(request, response, (req, res) -> {
			var error = new ExceptionMapper().handleIllegalArgumentException(
					new IllegalArgumentException("invalid value"), request);
			response.setStatus(error.getStatusCode().value());
		}));
		assertTrue(line.contains("status=400"));
		assertTrue(line.contains("error=\"Invalid parameter: invalid value\""));

		MockHttpServletRequest missing = new MockHttpServletRequest("GET", "/missing");
		String missingLine = capture(() -> filter.doFilter(missing, new MockHttpServletResponse(),
				(req, res) -> ((MockHttpServletResponse)res).setStatus(404)));
		assertTrue(missingLine.contains("status=404"));
		assertTrue(missingLine.contains("error=\"Not Found\""));
	}

	private static String capture(ThrowingAction action) throws Exception {
		PrintStream original = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try(PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			System.setOut(capture);
			try {
				action.run();
			} finally {
				System.setOut(original);
			}
		}
		return output.toString(StandardCharsets.UTF_8).stripTrailing();
	}

	private interface ThrowingAction {
		void run() throws Exception;
	}
}