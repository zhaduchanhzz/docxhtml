package com.hvnh;

import java.util.*;

class OrgUnit {
    String unitId;
    String unitName;
    String unitAddress;

    public OrgUnit(String unitId, String unitName, String unitAddress) {
        this.unitId = unitId;
        this.unitName = unitName;
        this.unitAddress = unitAddress;
    }
}

class ScheduleUnitDetail {
    String unitId;
    String unitContent;
    String unitFire;

    public ScheduleUnitDetail(String unitId, String unitContent, String unitFire) {
        this.unitId = unitId;
        this.unitContent = unitContent;
        this.unitFire = unitFire;
    }
}

class UnitTree {
    OrgUnit current;
    List<UnitTree> childUnit = new ArrayList<>();

    public UnitTree(OrgUnit current) {
        this.current = current;
    }

    public void addChild(UnitTree child) {
        childUnit.add(child);
    }
}
