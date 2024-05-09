package ca.bc.gov.ols.router.restrictions.rdm;

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

public class RdmFetcher {
	private final static Logger logger = LoggerFactory.getLogger(RdmFetcher.class.getCanonicalName());
	
	//private String rdmApiUrl = "https://dev-rdm-public.th.gov.bc.ca/api";
	private String rdmApiUrl = "https://tst-rdm-public.th.gov.bc.ca/api";
	private static final String RESTRICTIONS_ACTIVE_ENDPOINT = "/view/restrictions_active";
	private static final String RESTRICTIONS_DELETED_ENDPOINT = "/view/restrictions_deleted";
	private static final int OFFSET_INCREMENT = 500;
	
	public RdmFetcher(RouterConfig config) {
		rdmApiUrl = config.getRdmApiUrl();
	}

	public List<Restriction> fetchAll(RdmParser parser) throws IOException {
		List<Restriction> allRestrictions = new ArrayList<Restriction>();
		int offset = 0;
		while(true) {
			Reader pageReader = fetchPage(rdmApiUrl + RESTRICTIONS_ACTIVE_ENDPOINT + "?limit=" + OFFSET_INCREMENT + "&offset=" + offset);
			offset += OFFSET_INCREMENT;
			List<Restriction> restrictions = parser.parseRestrictions(pageReader);
			if(restrictions.isEmpty()) {
				break;
			}
			allRestrictions.addAll(restrictions);
		}
		return allRestrictions;
	}
	
	public List<Restriction> fetchChanges(RdmParser parser) throws IOException {	
		List<Restriction> changedRestrictions = new ArrayList<Restriction>();
		int offset = 0;
		while(true) {
			Reader pageReader = fetchPage(rdmApiUrl + RESTRICTIONS_ACTIVE_ENDPOINT + "?limit=" + OFFSET_INCREMENT + "&offset=" + offset + 
					"&filter=LAST_UPDATE_TIMESTAMP BETWEEN '2024-02-10' AND '9999-12-31'");
			offset += OFFSET_INCREMENT;
			List<Restriction> restrictions = parser.parseRestrictions(pageReader);
			if(restrictions.isEmpty()) {
				break;
			}
		}
		// TODO: should we also fetch the deletes here, or in a separate function?
		return changedRestrictions;
	}
	
	private Reader fetchPage(String urlString) throws IOException {
		logger.info("Fetching page: {}", urlString);
		URL url = new URL(urlString);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		return reader;
	}
		
}
