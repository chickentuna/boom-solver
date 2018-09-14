package boomlabs;

enum Spell {
	THE_SWAP(0, Target.NONE), BATTERY_PACK(0, Target.NONE), 
	CIRCLE_OF_HEAL(0, Target.NONE), PW_SHIELD(1, Target.ANY_MINION), 
	BINDING_HEAL(1, Target.ANY_MINION), TREACHERY(3, Target.FRIENDLY_MINION);

	int cost;
	Target target;

	private Spell(int cost, Target target) {
		this.cost = cost;
		this.target = target;
	}
}