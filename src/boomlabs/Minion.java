package boomlabs;

class Minion {

	// Hash
	int life, attack;
	int attacksLeft = 0;
	MinionType type;

	// Don't hash
	Team team;

	public Minion(MinionType type, Team team) {
		this.type = type;
		this.life = type.maxLife;
		this.attack = type.attack;
		this.team = team;
	}

	public Minion(Minion other) {
		this.attack = other.attack;
		this.attacksLeft = other.attacksLeft;
		this.life = other.life;
		this.type = other.type;
		this.team = other.team;
	}

	@Override
	public String toString() {
		return (team == Team.FOE ? "his " : "my ") + (life < type.maxLife ? "damaged " : "") + type.name();
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

	public boolean isDamaged() {
		return life < type.maxLife;
	}

}