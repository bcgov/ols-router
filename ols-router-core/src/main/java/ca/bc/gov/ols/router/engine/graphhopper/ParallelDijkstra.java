/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.engine.graphhopper;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;


public class ParallelDijkstra extends AbstractRoutingAlgorithm
{
	protected int maxPairs = Integer.MAX_VALUE;
	protected int finishedCount = 0;
	protected int foundCount = 0;
    protected TIntObjectMap<EdgeEntry> fromMap;
    protected PriorityQueue<EdgeEntry> fromHeap;
    protected EdgeEntry currEdge;
    private int visitedNodes;
    //private int[] to = {};
    private TIntObjectMap<TIntList> toMap;
    private EdgeEntry[] toEdges = {};

    public ParallelDijkstra( Graph g, FlagEncoder encoder, Weighting weighting, TraversalMode tMode )
    {
        super(g, encoder, weighting, tMode);
        initCollections(1000);
    }
    
    public void setMaxPairs(int maxPairs) {
        if(maxPairs > 0) {
           	this.maxPairs = maxPairs;
        } else {
        	this.maxPairs = Integer.MAX_VALUE;
        }
    }

    protected void initCollections( int size )
    {
        fromHeap = new PriorityQueue<EdgeEntry>(size);
        fromMap = new TIntObjectHashMap<EdgeEntry>(size);
    }

    public Path calcPath( int from, int to) {
    	throw new UnsupportedOperationException("calcPath() on single destination is not valid for ParallelDijkstra");
    }
    
    public List<Path> calcPaths( int from, int[] to )
    {
        checkAlreadyRun();
        //this.to = to;
        toMap = new TIntObjectHashMap<TIntList>();
        for(int i = 0; i < to.length; i++) {
        	TIntList toIndexes = toMap.get(to[i]);
        	if (toIndexes == null) {
        		toIndexes = new TIntLinkedList(-1);
        		toMap.put(to[i], toIndexes);
        	}
        	toIndexes.add(i);
        }
        finishedCount = Math.min(to.length, maxPairs);
        foundCount = 0;
        toEdges = new EdgeEntry[to.length];
        currEdge = createEdgeEntry(from, 0);
        if (!traversalMode.isEdgeBased())
        {
            fromMap.put(from, currEdge);
        }
        runAlgo();
        List<Path> paths = new ArrayList<Path>(to.length);
        for(EdgeEntry toEdge : toEdges) {
       		paths.add(extractPath(toEdge));
        }
        return paths;
    }

    protected void runAlgo()
    {
        EdgeExplorer explorer = outEdgeExplorer;
        while (true)
        {
            visitedNodes++;
            if (isWeightLimitExceeded() || finished())
                break;

            int startNode = currEdge.adjNode;
            EdgeIterator iter = explorer.setBaseNode(startNode);
            while (iter.next())
            {
                if (!accept(iter, currEdge.edge))
                    continue;

                int traversalId = traversalMode.createTraversalId(iter, false);
                double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;
                if (Double.isInfinite(tmpWeight))
                    continue;

                EdgeEntry nEdge = fromMap.get(traversalId);
                if (nEdge == null)
                {
                    nEdge = new EdgeEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                    nEdge.parent = currEdge;
                    fromMap.put(traversalId, nEdge);
                    fromHeap.add(nEdge);
                } else if (nEdge.weight > tmpWeight)
                {
                    fromHeap.remove(nEdge);
                    nEdge.edge = iter.getEdge();
                    nEdge.weight = tmpWeight;
                    nEdge.parent = currEdge;
                    fromHeap.add(nEdge);
                } else
                    continue;

                updateBestPath(iter, nEdge, traversalId);
            }

            if (fromHeap.isEmpty())
                break;

            currEdge = fromHeap.poll();
            if (currEdge == null)
                throw new AssertionError("Empty edge cannot happen");
        }
    }

//    @Override
//    protected boolean finished()
//    {
//    	int count = 0;
//    	for(int i = 0; i < to.length; i++) {
//    		if(currEdge.adjNode == to[i] && toEdges[i] == null) {
//    			toEdges[i] = currEdge;
//    		}
//    		if(toEdges[i] != null) {
//    			count++;
//    			if(count >= finishedCount) {
//    				return true;
//    			}
//    		}
//    	}
//    	return false;
//    }

    @Override
    protected boolean finished()
    {
    	TIntList toIndexes = toMap.get(currEdge.adjNode);
    	if(toIndexes != null) {
    		TIntIterator it = toIndexes.iterator();
    		while(it.hasNext()) {
    			int index = it.next();
    			if(toEdges[index] == null) {
    				toEdges[index] = currEdge;
    				foundCount++;
    			}
    		}
    	}
    	if(foundCount >= finishedCount) {
			return true;
    	}
    	return false;
    }

	@Override
	protected Path extractPath() {
		throw new UnsupportedOperationException("extractPath() with no parameters is not valid for ParallelDijkstra");
	}

    protected Path extractPath(EdgeEntry toEdge)
    {
        if (toEdge == null)
            return createEmptyPath();

        return new Path(graph, flagEncoder).setWeight(currEdge.weight).setEdgeEntry(toEdge).extract();
    }

    @Override
    public int getVisitedNodes()
    {
        return visitedNodes;
    }

    @Override
    protected boolean isWeightLimitExceeded()
    {
        return currEdge.weight > weightLimit;
    }

    @Override
    public String getName()
    {
        return "paralleldijkstra";
    }

}
