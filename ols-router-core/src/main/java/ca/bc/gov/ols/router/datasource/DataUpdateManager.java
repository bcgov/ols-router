package ca.bc.gov.ols.router.datasource;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.restrictions.closure.ClosureFetcher;
import ca.bc.gov.ols.router.restrictions.closure.ClosureParser;
import ca.bc.gov.ols.router.restrictions.rdm.RdmFetcher;
import ca.bc.gov.ols.router.restrictions.rdm.RdmParser;
import ca.bc.gov.ols.router.restrictions.rdm.Restriction;

public class DataUpdateManager {

	RdmParser rdmParser;
	RdmFetcher rdmFetcher;
	List<Restriction> restrictions = new ArrayList<Restriction>();
	ClosureParser closureParser;
	ClosureFetcher closureFetcher;
	List<Restriction> closures = new ArrayList<Restriction>();
	
	public DataUpdateManager(RouterConfig config) {
		rdmParser = new RdmParser(new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL, 3005));
		rdmFetcher = new RdmFetcher(config);
		closureParser = new ClosureParser(new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL, 3005));
		closureFetcher = new ClosureFetcher(config);
	}

	public List<Restriction> loadRdmRestrictions(Reader restrictionReader) throws IOException {
		return restrictions = rdmParser.parseRestrictions(restrictionReader);
	}
	
	public List<Restriction> fetchRdmRestrictions() throws IOException {
		return restrictions = rdmFetcher.fetchAll(rdmParser);
	}

	public List<Restriction> updateRdmRestrictions() throws IOException {
		List<Restriction> changes = rdmFetcher.fetchChanges(rdmParser);
		// TODO: probably also need the deletes
		// TODO: make all changes to the list
		
		return restrictions;
	}
	
	public List<Restriction> loadRoadClosures(Reader closureReader) throws IOException {
		return closures = closureParser.parseRestrictions(closureReader);
	}
	
	public List<Restriction> fetchRoadClosures() throws IOException {
		return closures = closureFetcher.fetchAll(closureParser);
	}

	public List<Restriction> updateRoadClosures() throws IOException {
		List<Restriction> changes = closureFetcher.fetchChanges(closureParser);
		// TODO: probably also need the deletes
		// TODO: make all changes to the list
		
		return closures;
	}
}

