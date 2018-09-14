package boomlabs;

import java.util.List;

import boomlabs.Action.Type;
import boomlabs.LabSolve.TreeNode;

public class StepEditor {
	static void step(TreeNode parent, List<Action> possible, int step) {
		if (step == 1) {
			Action a = new Action(Type.PLAY_MINION);
			a.minionCard = MinionType.ELVEN_ARCHER;
			a.target = parent.state.friends.get(0);

			assert possible.contains(a);

			possible.clear();
			possible.add(a);
		} else if (step == 2) {
			Action a = new Action(Type.PLAY_MINION);
			a.minionCard = MinionType.UNDERCOVER_REPORTER;
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 3) {
			Action a = new Action(Type.SPELL);
			a.spell = Spell.PW_SHIELD;
			a.target = parent.state.friends.stream().filter(m -> m.type == MinionType.ELVEN_ARCHER).findFirst().get();
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 4) {
			Action a = new Action(Type.PLAY_MINION);
			a.minionCard = MinionType.CLERIC;
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 5) {
			Action a = new Action(Type.SPELL);
			a.spell = Spell.BINDING_HEAL;
			a.target = parent.state.friends.stream().filter(m -> m.isDamaged()).findFirst().get();
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 6) {
			Action a = new Action(Type.PLAY_MINION);
			a.minionCard = MinionType.CLERIC;
			assert possible.contains(a);
			possible.clear();
			possible.add(a);

		} else if (step == 7) {
			Action a = new Action(Type.SPELL);
			a.spell = Spell.THE_SWAP;
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 8) {
			Action a = new Action(Type.PLAY_MINION);
			a.minionCard = MinionType.PYROMANCER;
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 9) {
			Action a = new Action(Type.SPELL);
			a.spell = Spell.BATTERY_PACK;
			assert possible.contains(a);
			possible.add(a);
		} else if (step == 10) {
			Action a = new Action(Type.HERO_POWER);
			a.target = parent.state.foes.stream().filter(m -> m.type == MinionType.UNDERCOVER_REPORTER && m.isDamaged()).findFirst().get();
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 11) {
			Action a = new Action(Type.PLAY_MINION);
			a.minionCard = MinionType.FUNGAL_ENCHANTER;
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 12) {
			Action a = new Action(Type.SPELL);
			a.spell = Spell.TREACHERY;
			a.target = parent.state.friends.stream().filter(m -> m.type == MinionType.CLERIC).findFirst().get();
			assert possible.contains(a);
			possible.clear();
			possible.add(a);
		} else if (step == 13) {
			System.out.println();
		}
	}
}
