package boomlabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionApplicator {

	public static State apply(State state, Action action) {
		if (action.type == Action.Type.ATTACK) {
			applyAttack(state, action);
		} else if (action.type == Action.Type.PLAY_MINION) {
			applyPlayMinion(state, action);
		} else if (action.type == Action.Type.HERO_POWER) {
			applyHeroPower(state, action);
		} else if (action.type == Action.Type.SPELL) {
			applySpell(state, action);
		}

		List<Minion> allMinions = Stream.concat(state.friends.stream(), state.foes.stream()).collect(Collectors.toList());
		for (Minion m : allMinions) {
			if (m.life <= 0) {
				List<Minion> team = m.team == Team.FOE ? state.foes : state.friends;
				removeInstance(team, m);
				// TODO: proc sylvanas
			}
		}
		allMinions = Stream.concat(state.friends.stream(), state.foes.stream()).collect(Collectors.toList());
		for (Minion m : allMinions) {
			if (m.type == MinionType.CLERIC) {
				for (int i = 0; i < state.minionsHealed; ++i) {
					if (m.team == Team.FOE) {
						mill(state);
					} else {
						draw(state);
					}
				}
			}
		}
		return state;
	}

	private static void applySpell(State state, Action action) {
		state.mana -= action.spell.cost;
		state.spells.remove(action.spell);

		boolean soulPriestEffect = state.activeSoulPriestEffect();
		if (action.spell == Spell.BATTERY_PACK) {
			state.mana = 10;
		} else if (action.spell == Spell.CIRCLE_OF_HEAL) {
			List<Minion> newFoes = new ArrayList<>();
			List<Minion> newFriends = new ArrayList<>();
			List<Minion> allMinions = Stream.concat(state.friends.stream(), state.foes.stream()).collect(Collectors.toList());
			for (Minion minion : allMinions) {
				Minion newMinion = new Minion(minion);
				tryHeal(state, newMinion, soulPriestEffect, 4);
				if (minion.team == Team.FRIEND) {
					newFriends.add(newMinion);
				} else {
					newFoes.add(newMinion);
				}
			}
			state.foes = newFoes;
			state.friends = newFriends;
		} else if (action.spell == Spell.BINDING_HEAL) {
			Minion newMinion = new Minion(action.target);
			tryHeal(state, newMinion, soulPriestEffect, 5);

			List<Minion> team = newMinion.team == Team.FOE ? state.foes : state.friends;
			int idx = removeInstance(team, action.target);
			team.add(idx, newMinion);

			state.life += soulPriestEffect ? -5 : 5;
		} else if (action.spell == Spell.PW_SHIELD) {
			// TODO maxLife effect
			draw(state);
		} else if (action.spell == Spell.THE_SWAP) {
			List<Minion> newFoes = new ArrayList<>();
			List<Minion> newFriends = new ArrayList<>();
			for (Minion m : state.foes) {
				Minion newfoe = new Minion(m);
				newfoe.team = Team.FRIEND;
				newFriends.add(newfoe);
			}
			for (Minion m : state.friends) {
				Minion newFriend = new Minion(m);
				newFriend.team = Team.FOE;
				newFoes.add(newFriend);
			}
			state.friends = newFriends;
			state.foes = newFoes;

		} else if (action.spell == Spell.TREACHERY) {
			removeInstance(state.friends, action.target);
			if (state.foes.size() < 7) {
				Minion newMinion = new Minion(action.target);
				newMinion.team = Team.FOE;
				state.foes.add(newMinion);
			}

		}

		// Proc pyromancer
		if (state.friends.stream().anyMatch(m -> m.type == MinionType.PYROMANCER)) {
			List<Minion> newFoes = new ArrayList<>();
			List<Minion> newFriends = new ArrayList<>();
			List<Minion> allMinions = Stream.concat(state.friends.stream(), state.foes.stream()).collect(Collectors.toList());
			for (Minion minion : allMinions) {
				Minion newMinion = new Minion(minion);
				newMinion.life -= 1;
				if (minion.team == Team.FRIEND) {
					newFriends.add(newMinion);
				} else {
					newFoes.add(newMinion);
				}
			}
			state.foes = newFoes;
			state.friends = newFriends;
		}

	}

	private static void draw(State state) {
		if (state.deck.isEmpty()) {
			state.life -= state.fatigue;
			state.fatigue++;
		} else {
			MinionType card = state.deck.get(0);
			state.deck.remove(0);
			state.hand.add(card);
		}
	}

	private static void mill(State state) {
		state.bossLife -= state.bossFatigue;
		state.bossFatigue++;
	}

	static void tryHeal(State state, Minion newMinion, boolean soulPriestEffect, int amount) {
		if (soulPriestEffect) {
			newMinion.life -= amount;
		} else {
			if (newMinion.life < newMinion.type.maxLife) {
				newMinion.life = Math.min(newMinion.life + amount, newMinion.type.maxLife);
				state.minionsHealed++;
			}
		}
	}

	private static void applyHeroPower(State state, Action action) {
		boolean soulPriestEffect = state.activeSoulPriestEffect();
		state.heroPower = false;
		state.mana -= 2;
		if (action.target == null) {
			state.life += soulPriestEffect ? -2 : 2;
		} else {
			Minion newMinion = new Minion(action.target);
			List<Minion> team = newMinion.team == Team.FOE ? state.foes : state.friends;
			tryHeal(state, newMinion, soulPriestEffect, 2);
			int idx = removeInstance(team, action.target);
			team.add(idx, newMinion);
		}

	}

	private static void applyPlayMinion(State state, Action action) {
		Minion playedMinion = new Minion(action.minionCard, Team.FRIEND);
		state.mana -= playedMinion.type.cost;
		state.hand.remove(action.minionCard);

		state.friends.add(playedMinion);

		if (playedMinion.type == MinionType.UNDERCOVER_REPORTER) {
			if (state.friends.stream().filter(m->m.type == MinionType.CLERIC).findFirst().isPresent()) {
				state.deck.add(0, MinionType.CLERIC);
				state.deck.add(0, MinionType.CLERIC);	
			} else {
				Optional<Minion> opt = state.friends.stream().filter(m->m.type != MinionType.CLERIC && m != playedMinion).findFirst();
				if (opt.isPresent()) {
					state.deck.add(0, opt.get().type);
					state.deck.add(0, opt.get().type);
				}
			}
		} else if (playedMinion.type == MinionType.FUNGAL_ENCHANTER) {
			boolean soulPriestEffect = state.activeSoulPriestEffect();
			if (soulPriestEffect) {
				state.life -= 2;
			} else {
				state.life += 2;
			}
			List<Minion> newMinionList = new ArrayList<>();
			for (Minion affected : state.friends) {
				Minion newMinion = new Minion(affected);
				tryHeal(state, newMinion, soulPriestEffect, 2);
				newMinionList.add(newMinion);
			}
			state.friends = newMinionList;
		} else if (playedMinion.type == MinionType.ELVEN_ARCHER) {
			if (action.target != null) {
				Minion newMinion = new Minion(action.target);
				List<Minion> team = newMinion.team == Team.FOE ? state.foes : state.friends;
				newMinion.life--;
				int idx = removeInstance(team, action.target);
				team.add(idx, newMinion);
			}
		}

	}

	private static void applyAttack(State next, Action action) {
		Minion minion = action.source;
		Minion target = action.target;

		int initialIdx = removeInstance(next.friends, minion);
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
			int idx = removeInstance(next.foes, t);

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
					Minion leper = new Minion(MinionType.LEPER, Team.FOE);
					next.foes.add(idx + 1, leper);
				}
			}
		} else {
			next.friends.add(initialIdx, newMinion);
		}
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

	public static int removeInstance(List<Minion> minions, Minion minion) {
		int idx = indexOfInstance(minions, minion);
		minions.remove(idx);
		return idx;
	}
}
