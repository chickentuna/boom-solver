package boomlabs;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import boomlabs.Action.Type;

public class LabSolve {

	static public String joinWith(String separator, Object... args) {
		return Stream.of(args).map(String::valueOf).collect(Collectors.joining(separator));
	}

	static public String join(Object... args) {
		return joinWith(" ", args);
	}

	public static void main(String args[]) throws IOException {
		new LabSolve();
	}

	long time;

	public LabSolve() throws IOException {
		State state = new State();
		state.life = 1;
		state.bossLife = 999;
		state.deck.add(MinionType.PYROMANCER);
		state.foes.add(new Minion(MinionType.CLERIC, Team.FOE));

		state.friends.add(new Minion(MinionType.CLERIC, Team.FRIEND));
		state.friends.add(new Minion(MinionType.SYLVANAS, Team.FRIEND));

		state.hand.add(MinionType.UNDERCOVER_REPORTER);
		state.hand.add(MinionType.ELVEN_ARCHER);
		state.hand.add(MinionType.FUNGAL_ENCHANTER);
		state.hand.add(MinionType.SOULPRIEST);

		state.spells.add(Spell.PW_SHIELD);
		state.spells.add(Spell.BINDING_HEAL);
		state.spells.add(Spell.BATTERY_PACK);
		state.spells.add(Spell.TREACHERY);
		state.spells.add(Spell.THE_SWAP);
		state.spells.add(Spell.CIRCLE_OF_HEAL);

		PrintStream file = new PrintStream("fails.txt");

		TreeNode root = new TreeNode(0);
		root.state = state;
		best = root;
		System.out.println("Running...");
		computeActionTree(root, file);
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
		int depth;

		public TreeNode(int depth) {
			this.depth = depth;
		}

		State state;
		Action action;
		TreeNode parent;
		List<TreeNode> children = new ArrayList<>();
	}

	Memo memo = new Memo();

	private static int fails = 0;

	void computeActionTree(TreeNode parent, PrintStream file) throws IOException {
		List<Action> possible = computePlausibleActions(parent.state);

		StepEditor.step(parent, possible, step++);

		if (possible.isEmpty()) {
			file.println(++fails);
			print(parent, file);
			file.println("boss life=" + parent.state.bossLife);
			file.println("mana=" + parent.state.mana);
		}

		for (Action action : possible) {
			TreeNode node = new TreeNode(parent.depth + 1);
			State next = parent.state.apply(action);

			if (memo.contains(next)) {
				continue;
			} else {
				memo.store(next, node.depth);
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
				computeActionTree(node, file);
			} else {
				file.println(++fails);
				print(node, file);
				file.println("boss life=" + node.state.bossLife);
				file.println("mana=" + node.state.mana);
			}
		}

	}

	private void print(TreeNode node, PrintStream file) {
		List<String> actions = new LinkedList<>();
		TreeNode cur = node;
		while (cur != null && cur.action != null) {
			actions.add(0, cur.action.toString());
			cur = cur.parent;
		}

		for (String a : actions) {
			file.println(a);
		}
	}

	private void print(TreeNode node) {
		print(node, System.out);
	}

	TreeNode best;

	private boolean isSuccess(State state) {
		return state.bossLife <= 0 && state.life > 0;
	}

	private boolean isFail(State state) {
		if (state.life <= 0) {
			return true;
		}
		return false;
	}

	int step = 1;

	private List<Action> computePlausibleActions(State state) {

		List<Action> actions = new ArrayList<>();
		for (Minion minion : state.friends) {
			if (minion.attacksLeft > 0) {
				for (Minion target : state.foes) {
					Action a = new Action(Action.Type.ATTACK);
					a.source = minion;
					a.target = target;
					actions.add(a);
				}
			}
		}
		boolean soulpriest = state.activeSoulPriestEffect();
		if (state.heroPower && state.mana >= 2) {

			for (Minion m : state.friends) {
				if (m.life < m.type.maxLife || soulpriest) {
					Action a = new Action(Type.HERO_POWER);
					a.target = m;
					actions.add(a);
				}
			}
			for (Minion foe : state.foes) {
				if (foe.life < foe.type.maxLife || soulpriest) {
					Action a = new Action(Type.HERO_POWER);
					a.target = foe;
					actions.add(a);
				}
			}
			if (!soulpriest) {
				Action a = new Action(Type.HERO_POWER);
				actions.add(a);
			}
		}

		for (Spell spell : state.spells) {
			if (spell.cost <= state.mana) {
				switch (spell) {
				case BATTERY_PACK:
					if (state.mana < 3) {
						Action a = new Action(Type.SPELL);
						a.spell = spell;
						actions.add(a);
					}
					break;
				case BINDING_HEAL:
					for (Minion m : state.friends) {
						if (m.isDamaged() || soulpriest) {
							Action a = new Action(Type.SPELL);
							a.spell = spell;
							a.target = m;
							actions.add(a);
							break;
						}
					}
					for (Minion foe : state.foes) {
						if (foe.isDamaged() || soulpriest) {
							Action a = new Action(Type.SPELL);
							a.spell = spell;
							a.target = foe;
							actions.add(a);
							break;
						}
					}
					break;
				case CIRCLE_OF_HEAL:
					Stream<Minion> allMinions = Stream.concat(state.friends.stream(), state.foes.stream());
					if (allMinions.filter(Minion::isDamaged).count() >= 2) {
						Action a = new Action(Type.SPELL);
						a.spell = spell;
						actions.add(a);
					}
					break;
				case PW_SHIELD:
					if (!state.deck.isEmpty()) {
						for (Minion minion : state.friends) {
							if (minion.type == MinionType.ELVEN_ARCHER) {
								Action a = new Action(Type.SPELL);
								a.spell = spell;
								a.target = minion;
								actions.add(a);
								break;
							}
						}
					}
					break;
				case THE_SWAP:
					if (state.friends.stream().filter(m -> m.type == MinionType.CLERIC).count() >= 2) {
						Action a = new Action(Type.SPELL);
						a.spell = spell;
						actions.add(a);
					}
					break;
				case TREACHERY:
					boolean clericFound = false;
					for (Minion minion : state.friends) {
						if (minion.type == MinionType.CLERIC || minion.type == MinionType.PYROMANCER || true) {
							if (minion.type == MinionType.CLERIC) {
								if (clericFound) {
									continue;
								}
								clericFound = true;
							}
							Action a = new Action(Type.SPELL);
							a.spell = spell;
							a.target = minion;
							actions.add(a);
						}
					}

					break;

				}
			}
		}

		for (MinionType minionCard : state.hand) {
			if (minionCard.cost <= state.mana && state.friends.size() < 7) {
				if (minionCard == MinionType.ELVEN_ARCHER) {
					Stream<Minion> allMinions = Stream.concat(state.friends.stream(), state.foes.stream());

					allMinions.filter(m -> m.life == 1).forEach(m -> {
						Action a = new Action(Type.PLAY_MINION);
						a.minionCard = minionCard;
						a.target = m;
						actions.add(a);
					});

					Optional<Minion> opt = state.friends.stream().filter(m -> m.life > 1).findFirst();
					if (opt.isPresent()) {
						Action a = new Action(Type.PLAY_MINION);
						a.minionCard = minionCard;
						a.target = opt.get();
						actions.add(a);
					}

				} else {
					Action a = new Action(Type.PLAY_MINION);
					a.minionCard = minionCard;
					actions.add(a);
				}
			}
		}
		return actions;
	}
}