package boomlabs;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import boomlabs.Minion.MinionType;

class Minion {
	enum MinionType {
		HYDRA(2, 4), NOVICE(1, 1), RHINO(2, 5), MEKGINEER(9, 7), LEPER(1, 1);
		int maxLife;
		int attack;

		private MinionType(int attack, int maxLife) {
			this.maxLife = maxLife;
			this.attack = attack;
		}
	}

	int life, attack;
	int attacksLeft = 1;
	MinionType type;

	public Minion(MinionType type) {
		this.type = type;
		this.life = type.maxLife;
		this.attack = type.attack;
	}

	public Minion(Minion other) {
		this.attack = other.attack;
		this.attacksLeft = other.attacksLeft;
		this.life = other.life;
		this.type = other.type;
	}

	@Override
	public String toString() {
		return type.name() + " " + attack + "/" + String.valueOf(life) + (this.attacksLeft == 2 ? "w" : "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attack;
		result = prime * result + attacksLeft;
		result = prime * result + life;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Minion other = (Minion) obj;
		if (attack != other.attack)
			return false;
		if (attacksLeft != other.attacksLeft)
			return false;
		if (life != other.life)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}

class Action {

	enum Type {
		ATTACK();
	}

	Type type;
	Minion minion;
	Minion target;

	public Action(Type type) {
		this.type = type;
	}

	public String toString(LabSolve.TreeNode node) {
		int targetIdx = State.indexOfInstance(node.parent.state.foes, target);
		return "Action [minion=" + minion + ", target=" + target + " #" + (targetIdx + 1) + "]";
	}

	@Override
	public String toString() {
		return LabSolve.join(minion, type.name(), target);
	}

}

class State {
	List<Minion> foes = new ArrayList<>();
	Map<Minion, Integer> minions = new HashMap<>();
	int life;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((foes == null) ? 0 : foes.hashCode());
		result = prime * result + life;
		result = prime * result + ((minions == null) ? 0 : minions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (foes == null) {
			if (other.foes != null)
				return false;
		} else if (!foes.equals(other.foes))
			return false;
		if (life != other.life)
			return false;
		if (minions == null) {
			if (other.minions != null)
				return false;
		} else if (!minions.equals(other.minions))
			return false;
		return true;
	}

	public State() {
	}

	public State(State state) {
		this();
		foes.addAll(state.foes);
		minions.putAll(state.minions);
		life = state.life;
	}

	public static int indexOfInstance(List<Minion> minions, Minion minion) {
		int idx = 0;
		while (idx < minions.size()) {
			if (minions.get(idx) == minion) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	public State apply(Action action) {
		State next = new State(this);
		if (action.type == Action.Type.ATTACK) {
			Minion minion = action.minion;
			Minion target = action.target;

			int count = next.minions.get(minion);
			if (count == 1) {
				next.minions.remove(minion);
			} else {
				next.minions.put(minion, count - 1);
			}

			Minion newMinion = new Minion(minion);

			newMinion.life -= target.attack;
			newMinion.attacksLeft -= 1;

			List<Minion> targets = new ArrayList<>(3);
			if (newMinion.type == MinionType.HYDRA) {
				int idx = indexOfInstance(next.foes, target);

				if (idx - 1 >= 0) {
					targets.add(next.foes.get(idx - 1));
				}
				targets.add(target);
				if (idx + 1 < next.foes.size()) {
					targets.add(next.foes.get(idx + 1));
				}

			} else {
				targets.add(target);
			}

			for (Minion t : targets) {
				int idx = indexOfInstance(next.foes, t);
				next.foes.remove(idx);

				Minion newfoe = new Minion(t);
				newfoe.life -= newMinion.attack;

				if (newfoe.life <= 0) {
					if (t.type == MinionType.LEPER) {
						next.life -= 2;
					}
				} else {
					next.foes.add(idx, newfoe);
				}
			}
			if (newMinion.life <= 0) {
				List<Minion> fifo = new ArrayList<>();
				for (Minion foe : next.foes) {
					if (foe.type == MinionType.MEKGINEER) {
						fifo.add(foe);
					}
				}

				for (Minion foe : fifo) {
					int idx = indexOfInstance(next.foes, foe);
					if (next.foes.size() < 7) {
						Minion leper = new Minion(MinionType.LEPER);
						next.foes.add(idx + 1, leper);
					}
				}

				if (newMinion.type == MinionType.RHINO) {
					if (next.minions.keySet().stream().anyMatch(p -> p.type == MinionType.HYDRA)) {
						next.life = -99;
					}
				}
				
			} else {
				Integer newCount = next.minions.get(newMinion);
				if (newCount == null) {
					next.minions.put(newMinion, 1);
				} else {
					next.minions.put(newMinion, newCount + 1);
				}
			}
		}
		return next;
	}

}

public class LabSolve {

	State state;

	static public String joinWith(String separator, Object... args) {
		return Stream.of(args).map(String::valueOf).collect(Collectors.joining(separator));
	}

	static public String join(Object... args) {
		return joinWith(" ", args);
	}

	public static void main(String args[]) {
		new LabSolve();
	}

	long time;

	public LabSolve() {
		state = new State();
		state.life = 15;
		state.foes.add(new Minion(MinionType.MEKGINEER));
		state.foes.add(new Minion(MinionType.MEKGINEER));
		state.foes.add(new Minion(MinionType.MEKGINEER));

		Minion hydra = new Minion(MinionType.HYDRA);
		hydra.attacksLeft = 2;
		state.minions.put(hydra, 3);
		state.minions.put(new Minion(MinionType.NOVICE), 3);
		state.minions.put(new Minion(MinionType.RHINO), 1);

		TreeNode root = new TreeNode();
		root.state = state;
		best = root;
		computeActionTree(root);
		System.out.println("Nothing found");
	}

	/***
	 * If a is better than b, returns negative. If b is better than a, returns
	 * positive. Otherwise returns 0.
	 **/
	Comparator<State> stateComparator = (a, b) -> {
		return 0;
	};

	static class TreeNode {
		State state;
		Action action;
		TreeNode parent;
		List<TreeNode> children = new ArrayList<>();
	}

	Map<State, State> memo = new HashMap<>();

	void computeActionTree(TreeNode parent) {
		List<Action> possible = computePossibleActions(parent.state);
		for (Action action : possible) {
			TreeNode node = new TreeNode();
			State next = parent.state.apply(action);
			State memed = memo.get(next);
			if (memed != null) {
				next = memed;
				continue;
			} else {
				memo.put(next, next);
			}
			node.state = next;
			node.action = action;
			node.parent = parent;
			parent.children.add(node);

			if (isSuccess(node.state)) {
				System.out.println("Success");
//				print(node);
//				System.exit(0);
			}else
			if (!isFail(node.state)) {
				computeActionTree(node);
			} else {
//				System.out.println("Abandonned branch:");
//				print(node);
			}
		}

	}

	private void print(TreeNode node) {
		List<String> actions = new LinkedList<>();
		TreeNode cur = node;
		while (cur != null && cur.action != null) {
			actions.add(0, cur.action.toString(cur));
			cur = cur.parent;
		}

		for (String a : actions) {
			System.out.println(a);
		}

	}

	TreeNode best;

	private boolean isSuccess(State state) {
		return state.life > 0 && state.minions.isEmpty() && state.foes.isEmpty();
	}

	private boolean isFail(State state) {
		for (Entry<Minion, Integer> minionCount : state.minions.entrySet()) {
			Minion minion = minionCount.getKey();
			if (minion.attacksLeft == 0) {
				return true;
			}
		}
		if (state.life <= 0) {
			return true;
		}
		return false;
	}
	private List<Action> computePossibleActions(State state) {
		List<Action> actions = new ArrayList<>();
		for (Entry<Minion, Integer> mc : state.minions.entrySet()) {
			for (Minion o : state.foes) {
				Action a = new Action(Action.Type.ATTACK);
				a.minion = mc.getKey();
				a.target = o;
				actions.add(a);
			}
		}
		return actions;
	}

}