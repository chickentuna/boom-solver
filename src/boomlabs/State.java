package boomlabs;

import java.util.ArrayList;
import java.util.List;

class State {
	// To hash
	List<Minion> foes = new ArrayList<>();
	List<Minion> friends = new ArrayList<>();
	List<Spell> spells = new ArrayList<>();
	List<MinionType> hand = new ArrayList<>();;
	int life;
	boolean heroPower = true;
	int mana = 10;
	List<MinionType> deck = new ArrayList<>();
	int fatigue = 1;
	int bossFatigue = 1;
	int bossLife;

	// Not to hash
	int minionsHealed = 0;

	public boolean activeSoulPriestEffect() {
		return friends.stream().anyMatch(m -> m.type == MinionType.SOULPRIEST);
	}
	
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
		result = prime * result + bossFatigue;
		result = prime * result + ((friends == null) ? 0 : friends.hashCode());
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
		if (friends == null) {
			if (other.friends != null)
				return false;
		} else if (!friends.equals(other.friends))
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
		friends.addAll(state.friends);
		spells.addAll(state.spells);
		hand.addAll(state.hand);
		deck.addAll(state.deck);

		life = state.life;
		heroPower = state.heroPower;
		mana = state.mana;
		fatigue = state.fatigue;
		bossFatigue = state.bossFatigue;
		bossLife = state.bossLife;
	}

	public State apply(Action action) {
		State next = new State(this);
		return ActionApplicator.apply(next, action);
	}

}