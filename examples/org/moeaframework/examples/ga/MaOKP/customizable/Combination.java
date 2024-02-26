package org.moeaframework.examples.ga.MaOKP.customizable;

import java.util.Map;

public class Combination {
	private int fixedObjA;
	private int fixedObjB;
	private Map<Integer, Integer> objsAssignedToFixedObjs;

	public Combination(int fixedObjA, int fixedObjB, Map<Integer, Integer> objsAssignedToFixedObjs) {
		this.fixedObjA = fixedObjA;
		this.fixedObjB = fixedObjB;
		this.objsAssignedToFixedObjs = objsAssignedToFixedObjs;
	}

	public int getFixedObjA() {
		return fixedObjA;
	}

	public int getFixedObjB() {
		return fixedObjB;
	}

	public Map<Integer, Integer> getObjsAssignedToFixedObjs() {
		return objsAssignedToFixedObjs;
	}

}
