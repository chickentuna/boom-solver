package boomlabs;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import boomlabs.Action.Type;
import boomlabs.Minion.MinionType;

enum Target {
	NONE, FRIENDLY, ANY
}

enum Spell {
	THE_SWAP(0, Target.NONE), BATTERY_PACK(0, Target.NONE), CIRCLE_OF_HEAL(0, Target.NONE), PW_SHIELD(1, Target.ANY), BINDING_HEAL(1, Target.ANY), TREACHERY(3, Target.FRIENDLY);

	int cost;
	Target target;

	private Spell(int cost, Target target) {
		this.cost = cost;
		this.target = target;
	}
}

class Minion {
	public static final int dunno = -1;

	enum MinionType {
		HYDRA(2, 4), NOVICE(1, 1), RHINO(2, 5), MEKGINEER(9, 7), LEPER(1, 1), SOULPRIEST(3, 5, 4), FUNGAL_ENCHANTER(3, 3, 3), UNDERCOVER_REPORTER(1, 2, 2), ELVEN_ARCHER(1, 1, 1), CLERIC(1, 3, 1), SYLVANAS(5, 5), PYROMANCER(3, 2, 2);
		int maxLife;
		int attack;
		int cost;

		private MinionType(int attack, int maxLife) {
			this(attack, maxLife, -1);
		}

		private MinionType(int attack, int maxLife, int cost) {
			this.cost = cost;
			this.maxLife = maxLife;
			this.attack = attack;
		}
	}

	int life, attack;
	int attacksLeft = 0;
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
		ATTACK, HERO_POWER, SPELL, PLAY_MINION;
	}

	Type type;

	MinionType minionCard;
	Spell spell;
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

	List<Spell> spells = new ArrayList<>();
	List<MinionType> hand = new ArrayList<>();;
	int life;
	boolean heroPower = true;
	int mana = 10;
	List<MinionType> deck = new ArrayList<>();
	int fatigue = 1;
	int bossHealth;
	
	int minionsHealed = 0;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deck == null) ? 0 : deck.hashCode());
		result = prime * result + ((foes == null) ? 0 : foes.hashCode());
		result = prime * result + ((hand == null) ? 0 : hand.hashCode());
		result = prime * result + (heroPower ? 1231 : 1237);
		result = prime * result + life;
		result = prime * result + mana;
		result = prime * result + ((minions == null) ? 0 : minions.hashCode());
		result = prime * result + ((spells == null) ? 0 : spells.hashCode());
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
		if (deck == null) {
			if (other.deck != null)
				return false;
		} else if (!deck.equals(other.deck))
			return false;
		if (foes == null) {
			if (other.foes != null)
				return false;
		} else if (!foes.equals(other.foes))
			return false;
		if (hand == null) {
			if (other.hand != null)
				return false;
		} else if (!hand.equals(other.hand))
			return false;
		if (heroPower != other.heroPower)
			return false;
		if (life != other.life)
			return false;
		if (mana != other.mana)
			return false;
		if (minions == null) {
			if (other.minions != null)
				return false;
		} else if (!minions.equals(other.minions))
			return false;
		if (spells == null) {
			if (other.spells != null)
				return false;
		} else if (!spells.equals(other.spells))
			return false;
		return true;
	}

	public State() {
	}

	public State(State state) {
		foes.addAll(state.foes);
		minions.putAll(state.minions);
		spells.addAll(state.spells);
		hand.addAll(state.hand);
		deck.addAll(state.deck);
		
		life = state.life;
		heroPower = state.heroPower;
		mana = state.mana;		
		fatigue = state.fatigue;
		bossHealth = state.bossHealth;
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

	public void applyAttack(State next, Action action) {
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

	public State apply(Action action) {
		State next = new State(this);
		if (action.type == Action.Type.ATTACK) {
			applyAttack(next, action);
		} else if (action.type == Action.Type.PLAY_MINION) {
			applyPlayMinion(next, action);
		} else if (action.type == Action.Type.HERO_POWER) {
			applyHeroPower(next, action);
		} else if (action.type == Action.Type.SPELL) {
			applySpell(next, action);
		}
		return next;
	}

	private void applyPlayMinion(State next, Action action) {
		Minion minion = new Minion(action.minionCard);
		next.mana -= minion.type.cost;
		Integer count = next.minions.get(minion);
		if (count == null) {
			next.minions.put(minion, 1);
		} else {
			next.minions.put(minion, count + 1);
		}
		if (minion.type == MinionType.UNDERCOVER_REPORTER) {
			next.deck.add(MinionType.CLERIC);
			next.deck.add(MinionType.CLERIC);
		} else if (minion.type == MinionType.FUNGAL_ENCHANTER) {
			
			if (next.minions.keySet().stream().anyMatch(m -> m.type == MinionType.SOULPRIEST)) {
				next.life += 2;
				for (Minion m : next.minions)
			} else {
				next.life -= 2;
			}
			
			
		}
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
		state.life = 1;
		state.bossHealth = 999;
		state.deck.add(MinionType.PYROMANCER);
		state.foes.add(new Minion(MinionType.CLERIC));

		state.minions.put(new Minion(MinionType.CLERIC), 1);
		state.minions.put(new Minion(MinionType.SYLVANAS), 1);

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
				print(node);
				System.exit(0);
			} else if (!isFail(node.state)) {
				computeActionTree(node);
			} else {
				System.out.println("Abandonned branch:");
				print(node);
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
		return state.bossHealth <= 0 && state.life > 0;
	}

	private boolean isFail(State state) {
		if (state.life <= 0) {
			return true;
		}
		return false;
	}

	private List<Action> computePossibleActions(State state) {
		List<Action> actions = new ArrayList<>();
		for (Minion minion : state.minions.keySet()) {
			if (minion.attacksLeft > 0) {
				for (Minion target : state.foes) {
					Action a = new Action(Action.Type.ATTACK);
					a.minion = minion;
					a.target = target;
					actions.add(a);
				}
			}
		}
		if (state.heroPower && state.mana >= 2) {
			for (Minion m : state.minions.keySet()) {
				if (m.life < m.type.maxLife) {
					Action a = new Action(Type.HERO_POWER);
					a.target = m;
					actions.add(a);
				}
			}
			for (Minion foe : state.foes) {
				if (foe.life < foe.type.maxLife) {
					Action a = new Action(Type.HERO_POWER);
					a.target = foe;
					actions.add(a);
				}
			}
			Action a = new Action(Type.HERO_POWER);
			actions.add(a);
		}

		for (Spell spell : state.spells) {
			if (spell.cost <= state.mana) {

				if (spell.target == Target.ANY || spell.target == Target.FRIENDLY) {
					for (Minion minion : state.minions.keySet()) {
						Action a = new Action(Type.SPELL);
						a.spell = spell;
						a.target = minion;
						actions.add(a);
					}
				}
				if (spell.target == Target.ANY) {
					for (Minion foe : state.foes) {
						Action a = new Action(Type.SPELL);
						a.spell = spell;
						a.target = foe;
						actions.add(a);
					}
				}
				if (spell.target == Target.NONE) {
					Action a = new Action(Type.SPELL);
					a.spell = spell;
					actions.add(a);

				}

			}
		}

		for (MinionType minionCard : state.hand) {
			if (minionCard.cost <= state.mana && count(state.minions) < 7) {
				Action a = new Action(Type.PLAY_MINION);
				a.minionCard = minionCard;
				actions.add(a);
			}
		}
		return actions;
	}

	private int count(Map<Minion, Integer> map) {
		return map.values().stream().collect(Collectors.summingInt(i -> i));
	}
}