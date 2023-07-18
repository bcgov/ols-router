/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.ols.router.data.enums.RestrictionSource;
import ca.bc.gov.ols.router.data.enums.RestrictionType;
import ca.bc.gov.ols.router.rdm.Restriction;
import ca.bc.gov.ols.rowreader.XsvRowWriter;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

public class RestrictionLookup {
	private static final Logger logger = LoggerFactory.getLogger(RestrictionLookup.class.getCanonicalName());
	
	private TIntObjectHashMap<ArrayList<Restriction>> restrictions = new TIntObjectHashMap<ArrayList<Restriction>>();
	
	public void addRestriction(int edgeId, Restriction r) {
		if(r == null) return;
		ArrayList<Restriction> rs = restrictions.get(edgeId);
		if(rs == null) {
			rs = new ArrayList<Restriction>(1);
			restrictions.put(edgeId, rs);
		}
		rs.add(r);
	}
	
	public void compact() {
		restrictions.forEachEntry( new TIntObjectProcedure<ArrayList<Restriction>>() {
			@Override
			public boolean execute(int edgeId, ArrayList<Restriction> rs) {
				rs.trimToSize();
				return true;
			}
		});
	}
	
	public List<Restriction> lookup(RestrictionSource source, int edgeId) {
		List<Restriction> list = restrictions.get(edgeId);
		if(list == null) return Collections.emptyList();
		return list.stream().filter(r -> source == null || r.source == source).toList();
		// TODO filter restrictions that apply to a given time
	}

	public void analyze(BasicGraph graph) {
		RestrictionAnalyzer ra = new RestrictionAnalyzer(graph);
		restrictions.forEachEntry(ra);
		ra.summarize();
	}
}

class RestrictionAnalyzer implements TIntObjectProcedure<ArrayList<Restriction>> {
	private static final Logger logger = LoggerFactory.getLogger(RestrictionAnalyzer.class.getCanonicalName());
	
	private BasicGraph graph;
	private final XsvRowWriter writer;
	private final List<String> schema;
	private int overlapCount = 0;
	private int itnHeightCount = 0;
	private int itnWidthCount = 0;
	private int itnWeightCount = 0;
	private int rdmHeightCount = 0;
	private int rdmWidthCount = 0;
	private int rdmWeightCount = 0;

	
	public RestrictionAnalyzer(BasicGraph graph) {
		this.graph = graph;
		schema = List.of("ITN Segment ID", "Percent Diff", "ITN Restriction", "RDM Restrictions");
		writer = new XsvRowWriter(new File("C:\\apps\\router\\data\\ITN-RDM Restriction Overlaps Analysis 28June2023.csv"), ',',schema, true);
	}
	
	@Override
	public boolean execute(int edgeId, ArrayList<Restriction> rs) {
		// for RDM restrictions, check if they are on a lane that doesn't exist on ITN segment
		for(Restriction r : rs) {
			int lanes = graph.getNumLanes(edgeId);
			if(r.laneNumber > lanes) {
				logger.warn("RDM Restriction {} on lane {}; segment {} has only {} lanes", r.id, r.laneNumber, r.segmentId, lanes);
			}
		}
		
		// for each restriction type, check if there is more than one source
		for(RestrictionType type : RestrictionType.values()) {
			Restriction itn = null;
			List<Restriction> rdm = new ArrayList<Restriction>();
			for(Restriction r : rs) {
				if(r.type == type) {
					if(r.source == RestrictionSource.ITN) {
						itn = r;
						switch(r.type) {
						case HORIZONTAL:
							itnWidthCount++;
							break;
						case VERTICAL:
							itnHeightCount++;
							break;
						case WEIGHT:
							itnWeightCount++;
							break;
						case UNKNOWN:
						default:
							break;
						}
					} else {
						rdm.add(r);
						switch(r.type) {
						case HORIZONTAL:
							rdmWidthCount++;
							break;
						case VERTICAL:
							rdmHeightCount++;
							break;
						case WEIGHT:
							rdmWeightCount++;
							break;
						case UNKNOWN:
						default:
							break;
						}					}
				}
			}
			if(itn != null && !rdm.isEmpty()) {
				overlapCount++;
				double bestPercentDiff = 100;
				for(Restriction r: rdm) {
					double percentDiff = ( itn.permitableValue - r.permitableValue)/r.permitableValue;
					if(Math.abs(percentDiff) < Math.abs(bestPercentDiff))
						bestPercentDiff = percentDiff;
				}
				HashMap<String,Object> row = new HashMap<String, Object>();
				row.put(schema.get(0), itn.segmentId);
				row.put(schema.get(1), String.format("%.2f", bestPercentDiff*100));
				row.put(schema.get(2), itn.toString());
				row.put(schema.get(3), rdm.toString());
				writer.writeRow(row);
			}
		}
		return true;
	}
	
	public void summarize() {
		writer.close();
		logger.info("ITN restrictions loaded: height: {} width: {} weight: {} total: {}", 
				itnHeightCount, itnWidthCount, itnWeightCount, 
				itnHeightCount + itnWidthCount + itnWeightCount);
		logger.info("RDM restrictions loaded: height: {} width: {} weight: {} total: {}", 
				rdmHeightCount, rdmWidthCount, rdmWeightCount, 
				rdmHeightCount + rdmWidthCount + rdmWeightCount);
		logger.info("Number of Restriction overlaps: {}", overlapCount);
	}
}