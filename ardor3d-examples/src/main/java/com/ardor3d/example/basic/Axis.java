
package com.ardor3d.example.basic;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class Axis {
    public final static Axis POS_X = new Axis(0, 0x01, Vector3.UNIT_X);
    public final static Axis NEG_X = new Axis(1, 0x02, Vector3.NEG_UNIT_X);
    public final static Axis POS_Y = new Axis(2, 0x04, Vector3.UNIT_Y);
    public final static Axis NEG_Y = new Axis(3, 0x08, Vector3.NEG_UNIT_Y);
    public final static Axis POS_Z = new Axis(4, 0x10, Vector3.UNIT_Z);
    public final static Axis NEG_Z = new Axis(5, 0x20, Vector3.NEG_UNIT_Z);

    public final int index;
    public final int mask;
    public final ReadOnlyVector3 vector;

    private Axis(final int i, final int m, final ReadOnlyVector3 v) {
        index = i;
        mask = m;
        vector = v;
    }

    public boolean isXAligned() {
        return (mask & (0x01 | 0x02)) != 0;
    }

    public boolean isYAligned() {
        return (mask & (0x04 | 0x08)) != 0;
    }

    public boolean isZAligned() {
        return (mask & (0x10 | 0x20)) != 0;
    }

    @Override
    public String toString() {
        switch (index) {
            case 0:
                return "POS_X";
            case 1:
                return "NEG_X";
            case 2:
                return "POS_Y";
            case 3:
                return "NEG_Y";
            case 4:
                return "POS_Z";
            case 5:
                return "NEG_Z";
        }
        return "?";
    }
}
