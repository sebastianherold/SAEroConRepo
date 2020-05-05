package org.processmining.pdc2019.parameters;

import org.processmining.pdc2019.algorithms.PDC2019Set;

public class PDC2019Parameters {

	private PDC2019Set set;
	private int nr;

	public PDC2019Parameters() {
		set = PDC2019Set.TRAIN;
		nr = 1;
	}
	
	public PDC2019Set getSet() {
		return set;
	}

	public void setSet(PDC2019Set set) {
		this.set = set;
	}

	public int getNr() {
		return nr;
	}

	public void setNr(int nr) {
		this.nr = nr;
	}
}
