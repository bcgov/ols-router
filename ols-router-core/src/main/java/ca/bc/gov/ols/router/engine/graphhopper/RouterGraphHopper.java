/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.graphhopper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DouglasPeucker;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import ca.bc.gov.ols.router.engine.graphhopper.isochrone.RasterHullBuilder;


public class RouterGraphHopper extends GraphHopper {

	public RouterGraphHopper(GraphHopperGraphBuilder builder) {
		super();
		//load("");
	    setGraphHopperLocation("");
        setEncodingManager(builder.getEncodingManager());
        setCHEnable(false);
        
		loadGraph(builder.getGHStorage());
	}
	
    public boolean load(String graphHopperFolder) {
//        if (Helper.isEmpty(graphHopperFolder))
//            throw new IllegalStateException("graphHopperLocation is not specified. call init before");

//        if (fullyLoaded)
//            throw new IllegalStateException("graph is already successfully loaded");

//        if (graphHopperFolder.endsWith("-gh"))
//        {
//            // do nothing  
//        } else if (graphHopperFolder.endsWith(".osm") || graphHopperFolder.endsWith(".xml"))
//        {
//            throw new IllegalArgumentException("To import an osm file you need to use importOrLoad");
//        } else if (!graphHopperFolder.contains("."))
//        {
//            if (new File(graphHopperFolder + "-gh").exists())
//                graphHopperFolder += "-gh";
//        } else
//        {
//            File compressed = new File(graphHopperFolder + ".ghz");
//            if (compressed.exists() && !compressed.isDirectory())
//            {
//                try
//                {
//                    new Unzipper().unzip(compressed.getAbsolutePath(), graphHopperFolder, removeZipped);
//                } catch (IOException ex)
//                {
//                    throw new RuntimeException("Couldn't extract file " + compressed.getAbsolutePath()
//                            + " to " + graphHopperFolder, ex);
//                }
//            }
//        }

//        setGraphHopperLocation(graphHopperFolder);

//        setEncodingManager(GraphHopperGraphBuilder.getEncodingManager());
        
//        if (!allowWrites && dataAccessType.isMMap())
//            dataAccessType = DAType.MMAP_RO;
//
//        GHDirectory dir = new GHDirectory(ghLocation, dataAccessType);
//        GraphExtension ext = getEncodingManager().needsTurnCostsSupport()
//                ? new TurnCostExtension() : new GraphExtension.NoOpExtension();
//        setCHEnable(false);
//        if (chEnabled)
//        {
//            initCHAlgoFactories();
//            ghStorage = new GraphHopperStorage(new ArrayList<Weighting>(algoFactories.keySet()), dir, encodingManager, hasElevation(), ext);
//        } else
//            ghStorage = new GraphHopperStorage(dir, encodingManager, hasElevation(), ext);

//        ghStorage.setSegmentSize(defaultSegmentSize);

//        Lock lock = null;
//        try
//        {
//            // create locks only if writes are allowed, if they are not allowed a lock cannot be created 
//            // (e.g. on a read only filesystem locks would fail)
//            if (ghStorage.getDirectory().getDefaultType().isStoring() && isAllowWrites())
//            {
//                lockFactory.setLockDir(new File(ghLocation));
//                lock = lockFactory.create(fileLockName, false);
//                if (!lock.tryLock())
//                    throw new RuntimeException("To avoid reading partial data we need to obtain the read lock but it failed. In " + ghLocation, lock.getObtainFailedReason());
//            }
//
//            if (!ghStorage.loadExisting())
//                return false;
//
//            postProcessing();
//            fullyLoaded = true;
//            return true;
//        } finally
//        {
//            if (lock != null)
//                lock.release();
//        }
        return true;
    }
    
    public List<GHResponse> route1ToMany( GHRequest request )
    {
        GHResponse response = new GHResponse();
        List<Path> paths = get1ToManyPaths(request, response);
        if (response.hasErrors()) {
            return Collections.nCopies(request.getPoints().size()-1, response);
        }
        boolean tmpEnableInstructions = request.getHints().getBool("instructions", false);
        boolean tmpCalcPoints = request.getHints().getBool("calcPoints", true);
        double wayPointMaxDistance = request.getHints().getDouble("wayPointMaxDistance", 1d);
        Locale locale = request.getLocale();
        DouglasPeucker peucker = new DouglasPeucker().setMaxDistance(wayPointMaxDistance);
        List<GHResponse> responses = new ArrayList<GHResponse>(paths.size());
        for(Path path : paths) {
        	response = new GHResponse();
	        new PathMerger().
	                setCalcPoints(tmpCalcPoints).
	                setDouglasPeucker(peucker).
	                setEnableInstructions(tmpEnableInstructions).
	                setSimplifyResponse(wayPointMaxDistance > 0).
	                doWork(response, Collections.singletonList(path), getTranslationMap().getWithFallBack(locale));
	        responses.add(response);
        }
        return responses;
    }

    protected List<Path> get1ToManyPaths( GHRequest request, GHResponse rsp )
    {
        String vehicle = request.getVehicle();
        if (vehicle.isEmpty())
            vehicle = getEncodingManager().fetchEdgeEncoders().get(0).toString();

        if (!getEncodingManager().supports(vehicle))
        {
            rsp.addError(new IllegalArgumentException("Vehicle " + vehicle + " unsupported. "
                    + "Supported are: " + getEncodingManager()));
            return Collections.emptyList();
        }

        TraversalMode tMode;
        String tModeStr = request.getHints().get("traversal_mode", getTraversalMode().toString());
        try
        {
            tMode = TraversalMode.fromString(tModeStr);
        } catch (Exception ex)
        {
            rsp.addError(ex);
            return Collections.emptyList();
        }

        List<GHPoint> points = request.getPoints();
        if (points.size() < 2)
        {
            rsp.addError(new IllegalStateException("At least 2 points has to be specified, but was:" + points.size()));
            return Collections.emptyList();
        }

        long visitedNodesSum = 0;
        FlagEncoder encoder = getEncodingManager().getEncoder(vehicle);
        EdgeFilter edgeFilter = new DefaultEdgeFilter(encoder);

        StopWatch sw = new StopWatch().start();
        List<QueryResult> qResults = new ArrayList<QueryResult>(points.size());
        Set<Integer> failPoints = new HashSet<Integer>();
        for (int placeIndex = 0; placeIndex < points.size(); placeIndex++)
        {
            GHPoint point = points.get(placeIndex);
            QueryResult res = getLocationIndex().findClosest(point.lat, point.lon, edgeFilter);
            if (res.isValid()) {
                qResults.add(res);            	
            } else {
            	failPoints.add(placeIndex);
            }
        }

        //if (rsp.hasErrors())
        if(failPoints.contains(0)) {
        	rsp.addError(new IllegalArgumentException("Cannot locate fromPoint on network"));
        	return Collections.emptyList();
        }

        String debug = "idLookup:" + sw.stop().getSeconds() + "s";

        Weighting weighting;
        Graph routingGraph = getGraphHopperStorage();

        if (isCHEnabled())
        {
            boolean forceCHHeading = request.getHints().getBool("force_heading_ch", false);
            if (!forceCHHeading && request.hasFavoredHeading(0))
                throw new IllegalStateException("Heading is not (fully) supported for CHGraph. See issue #483");
            weighting = getWeightingForCH(request.getHints(), encoder);
            routingGraph = getGraphHopperStorage().getGraph(CHGraph.class, weighting);
        } else
            weighting = createWeighting(request.getHints(), encoder);

//        RoutingAlgorithmFactory tmpAlgoFactory = getAlgorithmFactory(weighting);
        QueryGraph queryGraph = new QueryGraph(routingGraph);
        queryGraph.lookup(qResults);
        weighting = createTurnWeighting(weighting, queryGraph, encoder);

        QueryResult fromQResult = qResults.get(0);

        double weightLimit = request.getHints().getDouble("defaultWeightLimit", Double.MAX_VALUE);
//        String algoStr = request.getAlgorithm().isEmpty() ? AlgorithmOptions.DIJKSTRA_BI : request.getAlgorithm();
//        AlgorithmOptions algoOpts = AlgorithmOptions.start().
//                algorithm(algoStr).traversalMode(tMode).flagEncoder(encoder).weighting(weighting).
//                build();

//        boolean viaTurnPenalty = request.getHints().getBool("pass_through", false);
    	int[] toNodes = new int[(points.size() - 1) - failPoints.size()];
    	int offset = 0;
        for (int placeIndex = 1; placeIndex < points.size(); placeIndex++) {
        	if(!failPoints.contains(placeIndex)) {
        		toNodes[placeIndex-1-offset] = qResults.get(placeIndex-offset).getClosestNode();
        	} else {
        		offset++;
        	}
        }
        // enforce start direction
        queryGraph.enforceHeading(fromQResult.getClosestNode(), request.getFavoredHeading(0), false);

        // enforce end direction - can't really do this the way it is setup for 1-many
        //queryGraph.enforceHeading(toQResult.getClosestNode(), request.getFavoredHeading(placeIndex), true);

        sw = new StopWatch().start();
        //RoutingAlgorithm algo = tmpAlgoFactory.createAlgo(queryGraph, algoOpts);
        ParallelDijkstra algo = new ParallelDijkstra(queryGraph, encoder, weighting, tMode);
        algo.setWeightLimit(weightLimit);
        algo.setMaxPairs(request.getHints().getInt("maxPairs", Integer.MAX_VALUE));
        debug += ", algoInit:" + sw.stop().getSeconds() + "s";

        sw = new StopWatch().start();
        List<Path> paths = algo.calcPaths(fromQResult.getClosestNode(), toNodes);
//        if (path.getTime() < 0)
//            throw new RuntimeException("Time was negative. Please report as bug and include:" + request);

//        debug += ", " + algo.getName() + "-routing:" + sw.stop().getSeconds() + "s, " + path.getDebugInfo();

        // reset all direction enforcements in queryGraph to avoid influencing next path
//        queryGraph.clearUnfavoredStatus();

        visitedNodesSum += algo.getVisitedNodes();

        if (rsp.hasErrors())
            return Collections.emptyList();

        if (points.size() - 1 - failPoints.size() != paths.size())
            throw new RuntimeException("There should be exactly one more places than paths. places:" + points.size() + ", paths:" + paths.size());

        rsp.setDebugInfo(debug);
        rsp.getHints().put("visited_nodes.sum", visitedNodesSum);
        rsp.getHints().put("visited_nodes.average", (float) visitedNodesSum / (points.size() - 1));

        List<Path> finalPaths = new ArrayList<Path>(points.size() - 1);
        offset = 0;
        for(int i = 0; i < points.size() - 1; i++) {
        	// failPoints is indexed with 0 as original from-point, 1 as first to-point
        	if(failPoints.contains(i + 1)) {
        		finalPaths.add(new Path(queryGraph, encoder));
        		offset++;
        	} else {
        		finalPaths.add(paths.get(i - offset));
        	}
        }
        return finalPaths;
    }

	public List<Geometry> isoline(GHRequest request) {
        GHResponse response = new GHResponse();
        return getIsolines(request, response);
//        if (response.hasErrors()) {
//            return Collections.nCopies(request.getPoints().size()-1, response);
//        }
//        boolean tmpEnableInstructions = request.getHints().getBool("instructions", false);
//        boolean tmpCalcPoints = request.getHints().getBool("calcPoints", true);
//        double wayPointMaxDistance = request.getHints().getDouble("wayPointMaxDistance", 1d);
//        Locale locale = request.getLocale();
//        DouglasPeucker peucker = new DouglasPeucker().setMaxDistance(wayPointMaxDistance);
//        List<GHResponse> responses = new ArrayList<GHResponse>(paths.size());
//        for(Path path : paths) {
//        	response = new GHResponse();
//	        new PathMerger().
//	                setCalcPoints(tmpCalcPoints).
//	                setDouglasPeucker(peucker).
//	                setEnableInstructions(tmpEnableInstructions).
//	                setSimplifyResponse(wayPointMaxDistance > 0).
//	                doWork(response, Collections.singletonList(path), getTranslationMap().getWithFallBack(locale));
//	        responses.add(response);
//        }
//        return responses;
	}
    
    protected List<Geometry> getIsolines(GHRequest request, GHResponse rsp) {
        String vehicle = request.getVehicle();
        if (vehicle.isEmpty())
            vehicle = getEncodingManager().fetchEdgeEncoders().get(0).toString();

        if (!getEncodingManager().supports(vehicle))
        {
            rsp.addError(new IllegalArgumentException("Vehicle " + vehicle + " unsupported. "
                    + "Supported are: " + getEncodingManager()));
            return Collections.emptyList();
        }

//        TraversalMode tMode;
//        String tModeStr = request.getHints().get("traversal_mode", getTraversalMode().toString());
//        try
//        {
//            tMode = TraversalMode.fromString(tModeStr);
//        } catch (Exception ex)
//        {
//            rsp.addError(ex);
//            return Collections.emptyList();
//        }

        List<GHPoint> points = request.getPoints();
        if (points.size() < 1)
        {
            rsp.addError(new IllegalStateException("At least 1 point has to be specified, but none were."));
            return Collections.emptyList();
        }

        long visitedNodesSum = 0;
        FlagEncoder encoder = getEncodingManager().getEncoder(vehicle);
        EdgeFilter edgeFilter = new DefaultEdgeFilter(encoder);

        StopWatch sw = new StopWatch().start();
        List<QueryResult> qResults = new ArrayList<QueryResult>(points.size());
        Set<Integer> failPoints = new HashSet<Integer>();
        for (int placeIndex = 0; placeIndex < points.size(); placeIndex++)
        {
            GHPoint point = points.get(placeIndex);
            QueryResult res = getLocationIndex().findClosest(point.lat, point.lon, edgeFilter);
            if (res.isValid()) {
                qResults.add(res);            	
            } else {
            	failPoints.add(placeIndex);
            }
        }

        //if (rsp.hasErrors())
        if(failPoints.contains(0)) {
        	rsp.addError(new IllegalArgumentException("Cannot locate fromPoint on network"));
        	return Collections.emptyList();
        }

        String debug = "idLookup:" + sw.stop().getSeconds() + "s";

        Weighting weighting;
        Graph routingGraph = getGraphHopperStorage();

        if (isCHEnabled())
        {
            boolean forceCHHeading = request.getHints().getBool("force_heading_ch", false);
            if (!forceCHHeading && request.hasFavoredHeading(0))
                throw new IllegalStateException("Heading is not (fully) supported for CHGraph. See issue #483");
            weighting = getWeightingForCH(request.getHints(), encoder);
            routingGraph = getGraphHopperStorage().getGraph(CHGraph.class, weighting);
        } else
            weighting = createWeighting(request.getHints(), encoder);

//        RoutingAlgorithmFactory tmpAlgoFactory = getAlgorithmFactory(weighting);
        QueryGraph queryGraph = new QueryGraph(routingGraph);
        queryGraph.lookup(qResults);
        weighting = createTurnWeighting(weighting, queryGraph, encoder);

        QueryResult fromQResult = qResults.get(0);

        int zoneLimit = request.getHints().getInt("ZoneSize", 1);
        int zoneCount = request.getHints().getInt("ZoneCount", 1);
        int totalLimit = zoneLimit * zoneCount;
        
        //double weightLimit = request.getHints().getDouble("defaultWeightLimit", Double.MAX_VALUE);
//        String algoStr = request.getAlgorithm().isEmpty() ? AlgorithmOptions.DIJKSTRA_BI : request.getAlgorithm();
//        AlgorithmOptions algoOpts = AlgorithmOptions.start().
//                algorithm(algoStr).traversalMode(tMode).flagEncoder(encoder).weighting(weighting).
//                build();

//        boolean viaTurnPenalty = request.getHints().getBool("pass_through", false);
    	int[] toNodes = new int[(points.size() - 1) - failPoints.size()];
    	int offset = 0;
        for (int placeIndex = 1; placeIndex < points.size(); placeIndex++) {
        	if(!failPoints.contains(placeIndex)) {
        		toNodes[placeIndex-1-offset] = qResults.get(placeIndex-offset).getClosestNode();
        	} else {
        		offset++;
        	}
        }
        // enforce start direction
        queryGraph.enforceHeading(fromQResult.getClosestNode(), request.getFavoredHeading(0), false);

        // enforce end direction - can't really do this the way it is setup for 1-many
        //queryGraph.enforceHeading(toQResult.getClosestNode(), request.getFavoredHeading(placeIndex), true);

        sw = new StopWatch().start();
        //RoutingAlgorithm algo = tmpAlgoFactory.createAlgo(queryGraph, algoOpts);
        Isochrone algo = new Isochrone(queryGraph, encoder, weighting, request.getHints().getBool("reverseFlow", false));
        algo.setWeightLimit(totalLimit);
        debug += ", algoInit:" + sw.stop().getSeconds() + "s";

        sw = new StopWatch().start();
        List<List<Double[]>> buckets = algo.searchGPS(fromQResult.getClosestNode(), zoneCount);
//        if (path.getTime() < 0)
//            throw new RuntimeException("Time was negative. Please report as bug and include:" + request);

//        debug += ", " + algo.getName() + "-routing:" + sw.stop().getSeconds() + "s, " + path.getDebugInfo();

        // reset all direction enforcements in queryGraph to avoid influencing next path
//        queryGraph.clearUnfavoredStatus();

        visitedNodesSum += algo.getVisitedNodes();

        if (rsp.hasErrors())
            return Collections.emptyList();

        rsp.setDebugInfo(debug);
        rsp.getHints().put("visited_nodes.sum", visitedNodesSum);
        rsp.getHints().put("visited_nodes.average", (float) visitedNodesSum / (points.size() - 1));

        // bigger raster distance => bigger raster => less points => stranger buffer results, but faster
        double rasterDistance = 0.75;
        // bigger buffer distance => less holes, lower means less points!
        double bufferDistance = 0.003;
        // precision of the 'circles'
        int quadrantSegments = 3;

        RasterHullBuilder rasterHullBuilder = new RasterHullBuilder();
		List<Geometry> polyList = rasterHullBuilder.calcList(buckets, buckets.size() - 1, rasterDistance, bufferDistance, quadrantSegments);
		
		//List<Geometry> polyList = new ArrayList<Geometry>(buckets.size());
		GeometryFactory gf = new GeometryFactory();
		for(List<Double[]> bucket: buckets) {
		//List<Double[]> bucket = buckets.get(0);
			for(Double[] p: bucket) {
				polyList.add(gf.createPoint(new Coordinate(p[0], p[1])));
			}
		}
		return polyList;
    }

    
    public List<Geometry> loop(GHRequest request) {
        String vehicle = request.getVehicle();
        if (vehicle.isEmpty())
            vehicle = getEncodingManager().fetchEdgeEncoders().get(0).toString();

        List<GHPoint> points = request.getPoints();
        if(points.size() != 1) {
        	throw new IllegalArgumentException("Exactly 1 point must be specified, but none were.");
        }
        GHPoint point = points.get(0);
        
        FlagEncoder encoder = getEncodingManager().getEncoder(vehicle);
        EdgeFilter edgeFilter = new DefaultEdgeFilter(encoder);

        QueryResult res = getLocationIndex().findClosest(point.lat, point.lon, edgeFilter);
        if(!res.isValid()) {
            throw new IllegalArgumentException("Cannot locate fromPoint on network");            	
        }

        Graph routingGraph = getGraphHopperStorage();

//        if (isCHEnabled())
//        {
//            boolean forceCHHeading = request.getHints().getBool("force_heading_ch", false);
//            if (!forceCHHeading && request.hasFavoredHeading(0))
//                throw new IllegalStateException("Heading is not (fully) supported for CHGraph. See issue #483");
//            weighting = getWeightingForCH(request.getHints(), encoder);
//            routingGraph = getGraphHopperStorage().getGraph(CHGraph.class, weighting);
//        } else
//            weighting = createWeighting(request.getHints(), encoder);
		GeometryFactory gf = new GeometryFactory();

        EdgeIteratorState curEdge = res.getClosestEdge();
        int startEdge = curEdge.getEdge();
    	int curNode = curEdge.getAdjNode();
        EdgeExplorer edgeExplorer = routingGraph.createEdgeExplorer();
        //EdgeIterator edgeIt = edgeExplorer.setBaseNode(fromQResult.getClosestNode());
        
        ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
        for(int i = 0; i < 20; i++) {
        	EdgeIterator edgeIt = edgeExplorer.setBaseNode(curNode);
        	PointList pl = edgeIt.fetchWayGeometry(1);
        	for(GHPoint p : pl) {
        		coords.add(new Coordinate(p.lon, p.lat));
        	}
        	curNode = edgeIt.getAdjNode();
        }
        coords.add(coords.get(0));
		
		List<Geometry> polyList = new ArrayList<Geometry>(1);
		polyList.add(new Polygon(new LinearRing(new CoordinateArraySequence(coords.toArray(new Coordinate[coords.size()])), gf), new LinearRing[0], gf));
		return polyList;
    }

}
