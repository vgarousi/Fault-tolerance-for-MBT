package org.graphwalker.core.machine;

/**
 * <h1>NodeStatus</h1>
 * Each NodeStatus is associated with a vertex/node which keeps track of its status.
 * The default status is NOT_COVERED.
 * </p>
 *
 * @author Glen O'Donovan
 */
public enum NodeStatus {
  NOT_COVERED, COVERED, FAILED, NOT_REACHABLE
}
