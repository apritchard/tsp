package com.amp.tsp.prefs;

import java.util.List;

public interface PreferenceListener {
	void notify(List<PrefName> changedPrefs);
}
