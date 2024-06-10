package ca.bc.gov.ols.router.datasource;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.router.restrictions.rdm.RdmFetcher;
import ca.bc.gov.ols.router.restrictions.rdm.RdmParser;
import ca.bc.gov.ols.router.restrictions.rdm.Restriction;

public class DataUpdateManager {

	RdmParser rdmParser;
	RdmFetcher rdmFetcher;
	List<Restriction> restrictions = new ArrayList<Restriction>();
	
	public DataUpdateManager(RouterConfig config) {
		rdmParser = new RdmParser(new GeometryFactory(RouterConfig.BASE_PRECISION_MODEL, 3005));
		rdmFetcher = new RdmFetcher(config);
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
}
