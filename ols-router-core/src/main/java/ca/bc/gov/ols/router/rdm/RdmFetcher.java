package ca.bc.gov.ols.router.rdm;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public class RdmFetcher {
	
	private static final String RDM_API = "https://dev-rdm-public.th.gov.bc.ca/api/view/restrictions_active";	
	int offset = 0;
	private static final int OFFSET_INCREMENT = 250;
	
	
	public static void main(String[] args) throws IOException {
		RdmFetcher fetcher = new RdmFetcher();
		List<Restriction> restrictions = fetcher.fetchAll(new RdmParser(new GeometryFactory(new PrecisionModel(), 4326)));
		System.out.println(restrictions.toString());
		System.out.println("Num restrictions: " + restrictions.size());
	}

	List<Restriction> fetchAll(RdmParser parser) throws IOException {
		offset = 0;
		List<Restriction> restrictions = new ArrayList<Restriction>();
		while(true) {
			List<Restriction> newRestrictions = parser.parseRestrictions(fetchNext());
			restrictions.addAll(newRestrictions);
			if(newRestrictions.size() == 0) break;
		}
		return restrictions;
	}
	
	private InputStreamReader fetchNext() throws IOException {
		URL url = new URL(RDM_API + "?offset=" + offset);
		System.out.println(url.toString());
		final InputStreamReader reader = new InputStreamReader(url.openStream());
		offset += OFFSET_INCREMENT;
		return reader;
	}
	
}
