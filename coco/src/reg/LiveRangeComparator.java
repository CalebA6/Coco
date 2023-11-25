package reg;

import java.util.Comparator;

public class LiveRangeComparator implements Comparator<LiveRange> {

	@Override
	public int compare(LiveRange lv1, LiveRange lv2) {
		return lv1.numConflicts() - lv2.numConflicts();
	}

}
