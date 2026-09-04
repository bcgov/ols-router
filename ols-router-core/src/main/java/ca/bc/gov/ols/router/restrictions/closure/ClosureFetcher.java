package ca.bc.gov.ols.router.restrictions.closure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.restrictions.rdm.Restriction;

// Fetches road closures from their own remote feed, separate from the RDM restriction feed.
public class ClosureFetcher {
	private final static Logger logger = LoggerFactory.getLogger(ClosureFetcher.class.getCanonicalName());
	
	private String closureApiUrl;
	private static final int OFFSET_INCREMENT = 500;
	
	public ClosureFetcher(RouterConfig config) {
		closureApiUrl = config.getClosureApiUrl();
	}

	public List<Restriction> fetchAll(ClosureParser parser) throws IOException {
		List<Restriction> allClosures = new ArrayList<Restriction>();
		int offset = 0;
		while(true) {
			Reader pageReader = fetchPage(closureApiUrl + "?limit=" + OFFSET_INCREMENT + "&offset=" + offset);
			offset += OFFSET_INCREMENT;
			List<Restriction> closures = parser.parseRestrictions(pageReader);
			if(closures.isEmpty()) {
                break;
			}
            if(closures.size() < OFFSET_INCREMENT) { // the limit does not work yet, so we only fetch the first page for now
                allClosures.addAll(closures);
                break;
            }
			allClosures.addAll(closures);
		}
		return allClosures;
	}
	
	public List<Restriction> fetchChanges(ClosureParser parser) throws IOException {	
		List<Restriction> changedClosures = new ArrayList<Restriction>();
		int offset = 0;
		while(true) {
			Reader pageReader = fetchPage(closureApiUrl + "?limit=" + OFFSET_INCREMENT + "&offset=" + offset + 
					"&filter=LAST_UPDATE_TIMESTAMP BETWEEN '2024-02-10' AND '9999-12-31'");
			offset += OFFSET_INCREMENT;
			List<Restriction> closures = parser.parseRestrictions(pageReader);
			if(closures.isEmpty()) {
				break;
			}
		}
		// TODO: should we also fetch the deletes here, or in a separate function?
		return changedClosures;
	}
	
	private Reader fetchPage(String urlString) throws IOException {
		logger.info("Fetching page: {}", urlString);
		URL url = new URL(urlString);
		// TODO: we might want to allow retrying with a delay, if a connection fails, to allow for a random failure 
		final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		return reader;
	}
		
}
