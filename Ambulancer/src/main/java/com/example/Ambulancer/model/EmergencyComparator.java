package com.example.Ambulancer.model;

import java.util.Comparator;

public class EmergencyComparator implements Comparator<Emergency> {
    @Override
    public int compare(Emergency e1, Emergency e2) {
        if (!e1.type.equals(e2.type)) {
            if (e1.type.equals("ACCIDENT")) return -1;
            if (e2.type.equals("ACCIDENT")) return 1;
            if (e1.type.equals("SYMPTOMS")) return -1;
            if (e2.type.equals("SYMPTOMS")) return 1;
            return 0;
        }
        if (e1.type.equals("SYMPTOMS")) {
            return e2.subPriority - e1.subPriority;
        }
        return Long.compare(e1.timestamp, e2.timestamp);
    }
}
