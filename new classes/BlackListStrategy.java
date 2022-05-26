package org.graphwalker.core.machine;


import org.graphwalker.core.model.Vertex;

import java.util.*;

import static org.graphwalker.core.model.Edge.RuntimeEdge;

/**
 * <h1>BlackListStrategy</h1>
 * The BlackListStrategy handles the way GraphWalker handles failures during allNodesThatAreNotCovered execution.
 * </p>
 * The default way of handling a failure when executing a allNodesThatAreNotCovered, is to stop the allNodesThatAreNotCovered run and bail out.
 * This class goes back to the previous node and then continues the execution while avoiding the failed node/s
 * </p>
 *
 * @author Glen O'Donovan
 */
public class BlackListStrategy implements ExceptionStrategy {

  @Override
  public void handle(Machine machine, MachineException exception) {
    Context context = exception.getContext();
    List<Vertex.RuntimeVertex> allNodes = context.getModel().getVertices();
    List<RuntimeEdge> allEdges = context.getModel().getEdges();

    if (context.getCurrentElement() instanceof Vertex.RuntimeVertex) {
      ((Vertex.RuntimeVertex) context.getCurrentElement()).setNodeStatus(NodeStatus.FAILED);

      //a mapping from a node to its targets and sources
      HashMap<Vertex.RuntimeVertex, HashSet<Vertex.RuntimeVertex>> GTargetMap = new HashMap<Vertex.RuntimeVertex, HashSet<Vertex.RuntimeVertex>>();
      HashMap<Vertex.RuntimeVertex, HashSet<Vertex.RuntimeVertex>> GSourceMap = new HashMap<Vertex.RuntimeVertex, HashSet<Vertex.RuntimeVertex>>();
      //2 hashmaps in order to keep track of where we have been in the dfs and mem is used to hold the reachability of a node
      HashMap<Vertex.RuntimeVertex, Boolean> isVisited = new HashMap<Vertex.RuntimeVertex, Boolean>();
      HashMap<Vertex.RuntimeVertex, Boolean> mem = new HashMap<Vertex.RuntimeVertex, Boolean>();

      for (Vertex.RuntimeVertex vertex : allNodes) {
        GTargetMap.put(vertex, new HashSet<>());
        GSourceMap.put(vertex, new HashSet<>());
        isVisited.put(vertex, false);
        for (RuntimeEdge edge : allEdges) {
          if (edge.getSourceVertex().equals(vertex)) {
            GTargetMap.get(vertex).add(edge.getTargetVertex());
          }
          if (edge.getTargetVertex().equals(vertex)) {
            GSourceMap.get(vertex).add(edge.getSourceVertex());
          }
        }
      }

      for (Vertex.RuntimeVertex vertex : allNodes) {
        if (!dfs(vertex, GTargetMap, GSourceMap, isVisited, mem)) {
          if(vertex.getNodeStatus().equals(NodeStatus.NOT_COVERED)) {
            vertex.setNodeStatus(NodeStatus.NOT_REACHABLE);
          }
        }
      }

      //execution status change, to keep the program running
      context.setExecutionStatus(ExecutionStatus.EXECUTING);

      //Set the current and next element to the appropriate nodes and edges depending on the surrounding edges of the failed node
      Vertex.RuntimeVertex newStart = ((RuntimeEdge) context.getLastElement()).getSourceVertex();
      boolean nextElementsSet = false;
      for (int i = 0; i < allEdges.size(); i++) {
        if(allEdges.get(i).getSourceVertex().equals(context.getCurrentElement()) && allEdges.get(i).getTargetVertex().equals(((RuntimeEdge)context.getLastElement()).getSourceVertex()))
        {
          machine.getCurrentContext().setCurrentElement(newStart);
          machine.getCurrentContext().setNextEdgeTryAgain(allEdges.get(i));
          nextElementsSet = true;
        }
        if(nextElementsSet)
        {
          break;
        }
      }

      if(!nextElementsSet)
      {
        machine.getCurrentContext().setCurrentElement(newStart);
      }
    }
  }

  public Boolean dfs(Vertex.RuntimeVertex root,
                  HashMap<Vertex.RuntimeVertex, HashSet<Vertex.RuntimeVertex>> GTargetMap,
                  HashMap<Vertex.RuntimeVertex, HashSet<Vertex.RuntimeVertex>> GSourceMap,
                  HashMap<Vertex.RuntimeVertex, Boolean> isVisited,
                  HashMap<Vertex.RuntimeVertex, Boolean> mem)
  {
    /**
     * returns booleans based off if the node in question is reachable or not
     */
    if(isVisited.get(root))
    {
      return mem.get(root);
    }

    if(root.getNodeStatus().equals(NodeStatus.FAILED)){
      mem.put(root, false);
      return false;
    }

    //mark root as visited
    isVisited.put(root, true);

    Boolean isAnySourceReachable = false;
    //logic for marking root as reachable
    for(Vertex.RuntimeVertex source : GSourceMap.get(root)) {
      isAnySourceReachable = source.getNodeStatus().equals(NodeStatus.COVERED) || isAnySourceReachable;
      if (source.getNodeStatus().equals(NodeStatus.NOT_COVERED)) {
        if (dfs(source, GTargetMap, GSourceMap, isVisited, mem)) {
            isAnySourceReachable = true;
        }
      }
    }

    mem.put(root, isAnySourceReachable);

    for(Vertex.RuntimeVertex target : GTargetMap.get(root))
    {
      dfs(target, GTargetMap, GSourceMap, isVisited, mem);
    }

    return isAnySourceReachable;
  }
}


