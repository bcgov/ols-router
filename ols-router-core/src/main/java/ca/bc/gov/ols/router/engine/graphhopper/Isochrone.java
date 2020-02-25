/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.graphhopper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap.PrimitiveEntry;


public class Isochrone extends AbstractRoutingAlgorithm {

    // TODO use same class as used in GTFS module?
    class IsoLabel extends EdgeEntry {
        protected IsoLabel parentLabel;

        public IsoLabel(int edgeId, int adjNode, double weight) {
        	super(edgeId, adjNode, weight);
        	this.edge = edgeId;
            this.adjNode = adjNode;
            this.weight = weight;
        }

        /**
         * This method returns the weight to the origin e.g. to the start for the forward SPT and to the
         * destination for the backward SPT. Where the variable 'weight' is used to let heap select
         * smallest *full* weight (from start to destination).
         */
        public double getWeightOfVisitedPath() {
            return weight;
        }

        @Override
        public String toString() {
            return adjNode + " (" + edge + ") weight: " + weight;
        }

    }

    private IntObjectHashMap<IsoLabel> fromMap;
    private PriorityQueue<IsoLabel> fromHeap;
    private IsoLabel currEdge;
    private int visitedNodes;
    private final boolean reverseFlow;

    public Isochrone(Graph g, FlagEncoder flagEncoder, Weighting weighting, boolean reverseFlow) {
        super(g, flagEncoder, weighting, TraversalMode.NODE_BASED);
        initCollections(1000);
        this.reverseFlow = reverseFlow;
    }

    protected void initCollections(int size) {
        fromHeap = new PriorityQueue<>(size);
        fromMap = new IntObjectHashMap<>(size);
    }

    @Override
    public Path calcPath(int from, int to) {
        throw new IllegalStateException("call search instead");
    }

    /*
     * Time limit in seconds
     */
//    public void setTimeLimit(double limit) {
//        criteria = RoutingCriteria.FASTEST;
//        this.limit = limit * 1000;
//        // we explore until all spt-entries are '>timeLimitInSeconds' 
//        // and add some more into this bucket for car we need a bit more as 
//        // we otherwise get artifacts for motorway endings
//        this.finishLimit = this.limit + Math.max(this.limit * 0.14, 200_000);
//    }

    /*
     * Distance limit in meter
     */
//    public void setDistanceLimit(double limit) {
//        this.limit = limit;
//        this.finishLimit = limit + Math.max(limit * 0.14, 2_000);
//    }

    public List<List<Double[]>> searchGPS(int from, final int bucketCount) {
        searchInternal(from);

        final double bucketSize = weightLimit / bucketCount;
        final List<List<Double[]>> buckets = new ArrayList<>(bucketCount);

        for (int i = 0; i < bucketCount + 1; i++) {
            buckets.add(new ArrayList<Double[]>());
        }
        final NodeAccess na = graph.getNodeAccess();
        for(PrimitiveEntry<IsoLabel> entry : fromMap.entries()) {
        	int nodeId = entry.key();
        	IsoLabel label = entry.value();
        	
            int bucketIndex = (int) (label.weight / bucketSize);
            if (bucketIndex < 0) {
                throw new IllegalArgumentException("edge cannot have negative explore value " + nodeId + ", " + label);
            } else if (bucketIndex > bucketCount) {
                break;
            }

            double lat = na.getLatitude(nodeId);
            double lon = na.getLongitude(nodeId);
            buckets.get(bucketIndex).add(new Double[]{lon, lat});

            // guess center of road to increase precision a bit for longer roads
            if (label.parentLabel != null) {
                nodeId = label.parentLabel.adjNode;
                double lat2 = na.getLatitude(nodeId);
                double lon2 = na.getLongitude(nodeId);
                buckets.get(bucketIndex).add(new Double[]{(lon + lon2) / 2, (lat + lat2) / 2});
            }
        }
        return buckets;
    }

    public List<Set<Integer>> search(int from, final int bucketCount) {
        searchInternal(from);

        final double bucketSize = weightLimit / bucketCount;
        final List<Set<Integer>> list = new ArrayList<>(bucketCount);

        for (int i = 0; i < bucketCount; i++) {
            list.add(new HashSet<Integer>());
        }

        for(PrimitiveEntry<IsoLabel> entry : fromMap.entries()) {
        	int nodeId = entry.key();
        	IsoLabel label = entry.value();
            if (finished()) {
                break;
            }

            int bucketIndex = (int) (label.weight / bucketSize);
            if (bucketIndex < 0) {
                throw new IllegalArgumentException("edge cannot have negative explore value " + nodeId + ", " + label);
            } else if (bucketIndex == bucketCount) {
                bucketIndex = bucketCount - 1;
            } else if (bucketIndex > bucketCount) {
            	break;
            }

            list.get(bucketIndex).add(nodeId);
            break;
        }
        return list;
    }

    public void searchInternal(int from) {
        checkAlreadyRun();
        currEdge = new IsoLabel(-1, from, 0);
        fromMap.put(from, currEdge);
        EdgeExplorer explorer = reverseFlow ? inEdgeExplorer : outEdgeExplorer;
        while (true) {
            visitedNodes++;
            if (finished()) {
                break;
            }

            int neighborNode = currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(neighborNode);
            while (iter.next()) {
                if (!accept(iter, currEdge.edge)) {
                    continue;
                }
                // minor speed up
                if (currEdge.edge == iter.getEdge()) {
                    continue;
                }

                double tmpWeight = weighting.calcWeight(iter, reverseFlow, currEdge.edge) + currEdge.weight;
                int tmpNode = iter.getAdjNode();
                IsoLabel nEdge = fromMap.get(tmpNode);
                if (nEdge == null) {
                    nEdge = new IsoLabel(iter.getEdge(), tmpNode, tmpWeight);
                    nEdge.parentLabel = currEdge;
                    fromMap.put(tmpNode, nEdge);
                    fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight) {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    nEdge.weight = tmpWeight;
                    nEdge.parentLabel = currEdge;
                    fromHeap.add(nEdge);
                }
            }

            if (fromHeap.isEmpty()) {
                break;
            }

            currEdge = fromHeap.poll();
            if (currEdge == null) {
                throw new AssertionError("Empty edge cannot happen");
            }
        }
    }

    @Override
    protected boolean finished() {
        return currEdge.weight >= weightLimit;
    }

    @Override
    protected Path extractPath() {
        if (currEdge == null || !finished()) {
            return createEmptyPath();
        }
        return new Path(graph, flagEncoder).setEdgeEntry(currEdge).extract();
    }

    @Override
    public String getName() {
        return "isochrone";
    }

    @Override
    public int getVisitedNodes() {
        return visitedNodes;
    }

	@Override
	protected boolean isWeightLimitExceeded() {
		// TODO Auto-generated method stub
		return false;
	}
}
