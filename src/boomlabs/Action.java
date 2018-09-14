package boomlabs;

class Action {

	enum Type {
		ATTACK, HERO_POWER, SPELL, PLAY_MINION;
	}

	Type type;

	MinionType minionCard;
	Spell spell;
	Minion source;
	Minion target;

	public Action(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		if (type == Type.SPELL) {
			String r = spell.name();
			if (target != null) {
				r += " on " + target.toString();
				
			}
			return r;
		} else if (type == Type.HERO_POWER) {
			String r = type.name();
			if (target != null) {
				r += " on " + target.toString();
			} else {
				r += " on self";
			}
			return r;
		} else if (type == Type.PLAY_MINION) {
			String r = "Play " + minionCard.name();
			if (target != null) {
				r += " on " + target.toString();
			}
			return r;
		}
		return "Action [minion=" + source + ", target=" + target + " #" + (1) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((minionCard == null) ? 0 : minionCard.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((spell == null) ? 0 : spell.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Action other = (Action) obj;
		if (minionCard != other.minionCard)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (spell != other.spell)
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}