package boomlabs;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class Memo {
	int MAX_SIZE = 200_000;
	PrintStream file;
	Map<State, Integer> map = new HashMap<>();

	Memo() throws FileNotFoundException {
		file = new PrintStream("memo.txt");
	}

	public boolean contains(State next) {
		Integer depth = map.get(next);
		if (depth == null) {
			return false;
		}

		file.println("----");
		file.println(next);

		return true;
	}

	public int size() {
		return map.size();
	}

	public void save(State next, int depth) {
		map.put(next, depth);
	}
	public void store(State next, int depth) {
		if (map.size() >= MAX_SIZE) {
			Optional<Entry<State, Integer>> mostDeeper = map.entrySet()
					.stream()
					.filter(a -> a.getValue() > depth)
					.max((a, b) -> a.getValue() - b.getValue());

			mostDeeper.ifPresent(e -> {
				map.remove(e.getKey());
				save(next, depth);
			});

		} else {
			save(next, depth);
		}

	}
}
