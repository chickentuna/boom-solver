package boomlabs;

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