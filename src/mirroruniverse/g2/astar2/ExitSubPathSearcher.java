package mirroruniverse.g2.astar2;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mirroruniverse.g2.Map;
import mirroruniverse.sim.MUMap;

public class ExitSubPathSearcher extends MUAStar {

	protected Queue<Node<Integer>> hangingNodes;

	public ExitSubPathSearcher(Map leftMap, Map rightMap,
			Queue<Node<Integer>> hangingNodes) {
		super(leftMap, rightMap);
		this.hangingNodes = hangingNodes;
	}

	@Override
	protected List<Integer> generateSuccessors(Node<Integer> node) {
		List<Integer> successors = new LinkedList<Integer>();

		if (closedStates.contains(node.state))
			return successors;

		int[] coordinates = Encoder.decode(node.state);
		int x1 = coordinates[0];
		int y1 = coordinates[1];
		int x2 = coordinates[2];
		int y2 = coordinates[3];

		for (int i = 1; i != MUMap.aintDToM.length; ++i) {
			int[] aintMove = MUMap.aintDToM[i];
			int deltaX = aintMove[0];
			int deltaY = aintMove[1];

			if (!leftMap.isExit(x1, y1)) {
				x1 += deltaX;
				y1 += deltaY;

				if (!leftMap.isValid(x1, y1)) {
					// if it's not valid, roll back
					x1 -= deltaX;
					y1 -= deltaY;
				}
			}

			if (!rightMap.isExit(x2, y2)) {
				x2 += deltaX;
				y2 += deltaY;

				if (!rightMap.isValid(x2, y2)) {
					// if it's not valid, roll back
					x2 -= deltaX;
					y2 -= deltaY;
				}
			}

			Integer newState = Encoder.encode(x1, y1, x2, y2);
			if (!newState.equals(node.state)
					&& (!closedStates.contains(newState)))
				successors.add(newState);
		}
		return successors;
	}

	@Override
	public List<Integer> search(Integer start, Integer goal) {
		this.goal = goal;
		List<Integer> bestSubSolution = null;
		int minSubCost = Integer.MAX_VALUE;
		int minCost = Integer.MAX_VALUE;
		Node<Integer> bestCandidate = null;

		while (!hangingNodes.isEmpty()) {
			Node<Integer> candidate = hangingNodes.poll();
			List<Integer> subSolution = compute(candidate.state, minSubCost);
			if (subSolution == null)
				continue;
			int subCost = subSolution.size();
			if (subCost <= minSubCost) {
				minSubCost = subCost;
				if (candidate.g + subCost < minCost) {
					bestCandidate = candidate;
					bestSubSolution = subSolution;
				}
			}
		}
		
		List<Integer> bestSolution = this.constructSolution(bestCandidate);
		bestSubSolution.remove(0);
		bestSolution.addAll(bestSubSolution);
		return bestSolution;
	}

	public List<Integer> compute(Integer candidate, int depthLimit) {
		this.closedStates.clear();
		this.fringe.clear();
		Node<Integer> root = new Node<Integer>(candidate, h(candidate));
		fringe.offer(root);
		while (true) {
			Node<Integer> node = fringe.poll();
			if (node == null)
				return null;

			if (node.f > depthLimit)
				return null;

			if (this.isGoal(node.state))
				return constructSolution(node);

			expand(node);
			closedStates.add(node.state);
		}
	}
}