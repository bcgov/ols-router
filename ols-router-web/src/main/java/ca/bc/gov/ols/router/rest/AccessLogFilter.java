/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.rest;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Writes one access line per request to STDOUT for collection by the pod logger. */
@Component
public class AccessLogFilter extends OncePerRequestFilter {

	public static final String ERROR_MESSAGE_ATTR = "ols.accessLog.errorMessage";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {
		long start = System.nanoTime();
		boolean failed = false;
		try {
			chain.doFilter(request, response);
		} catch(ServletException | IOException | RuntimeException e) {
			failed = true;
			request.setAttribute(ERROR_MESSAGE_ATTR, e.toString());
			throw e;
		} finally {
			// A failure may escape before the container has set the response status.
			log(request, failed ? 500 : response.getStatus(), start);
		}
	}

	private void log(HttpServletRequest request, int status, long start) {
		long ms = (System.nanoTime() - start) / 1_000_000;
		StringBuilder sb = new StringBuilder(256);
		sb.append("ACCESS ").append(Instant.now())
				.append(" client=").append(clean(clientIp(request)))
				.append(" method=").append(clean(request.getMethod()))
				.append(" path=").append(clean(request.getRequestURI()))
				.append(" params=\"").append(clean(request.getQueryString())).append('"')
				.append(" status=").append(status)
				.append(" time_ms=").append(ms);
		Object error = request.getAttribute(ERROR_MESSAGE_ATTR);
		if(error != null) {
			sb.append(" error=\"").append(clean(error.toString())).append('"');
		} else if(status >= 400) {
			HttpStatus httpStatus = HttpStatus.resolve(status);
			sb.append(" error=\"").append(httpStatus == null ? "HTTP error"
					: httpStatus.getReasonPhrase()).append('"');
		}
		System.out.println(sb);
	}

	private static String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if(xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	/** Prevent request input or exception text from creating another log line. */
	private static String clean(String s) {
		if(s == null) {
			return "";
		}
		int nl = s.indexOf('\n');
		if(nl >= 0) {
			s = s.substring(0, nl);
		}
		return s.replace('\r', ' ').replace('"', '\'');
	}
}