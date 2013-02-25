
package com.ardor3d.example.basic;

import com.ardor3d.math.Matrix3;

/**
 * Finite State Machine for 3D orientation of a Block
 * 
 * 24 states each state with 6 links. The links represent a rotation CW or CCW around the X,Y and Z axis
 * 
 * @author Thomas �hl�n, thahlen@gmail.com
 */
public class BlockOrientation {

    /** This is default orientation [POS_X,POS_Y,POS_Z] */
    public final static BlockOrientation BASE_ORIENTATION;

    private final static Axis[] s_xAxes = new Axis[] { Axis.POS_X, Axis.NEG_X, Axis.POS_Y, Axis.NEG_Y, Axis.POS_Z,
            Axis.NEG_Z };
    /**
     * An array containing all 4 orthogonal axes ordered CW for each face normal. Faces normals ordered as s_xAxis array
     */
    private final static Axis[][] s_axes = new Axis[][] { { Axis.POS_Z, Axis.POS_Y, Axis.NEG_Z, Axis.NEG_Y },
            { Axis.NEG_Z, Axis.POS_Y, Axis.POS_Z, Axis.NEG_Y }, { Axis.POS_Z, Axis.NEG_X, Axis.NEG_Z, Axis.POS_X },
            { Axis.POS_Z, Axis.POS_X, Axis.NEG_Z, Axis.NEG_X }, { Axis.NEG_X, Axis.POS_Y, Axis.POS_X, Axis.NEG_Y },
            { Axis.POS_X, Axis.POS_Y, Axis.NEG_X, Axis.NEG_Y }, };

    private static Axis nextCCW(final Axis rotationAxis, final Axis orthogonalAxis) {
        return next(rotationAxis, orthogonalAxis, -1);
    }

    private static Axis nextCW(final Axis rotationAxis, final Axis orthogonalAxis) {
        return next(rotationAxis, orthogonalAxis, 1);
    }

    /**
     * This will take given orthogonal axis and rotate it either CW or CCW around given rotation(face normal) axis and
     * return the resulting new axis
     * 
     * @param rotationAxis
     *            - the axis of rotation(face normal)
     * @param orthogonalAxis
     *            - an axis orthogonal to the rotation axis
     * @param sign
     *            - 1 for CW rotation and -1 for CCW rotation
     * @return
     */
    private static Axis next(final Axis rotationAxis, final Axis orthogonalAxis, final int sign) {
        Axis[] axes;
        int index;

        axes = s_axes[rotationAxis.index];
        // find the index for given orthogonalAxis
        for (int i = 0; i < 4; i++) {
            if (axes[i] == orthogonalAxis) {
                index = i + sign;
                index = index < 0 ? 3 : index % 4;
                return axes[index];
            }
        }
        throw new RuntimeException("Can't Happen");
    }

    /* Build the State Diagram for block rotations */
    static {
        final BlockOrientation[][] orientations = new BlockOrientation[6][6];

        // Build all the 24 states we can get with a right handed coordinate system
        Axis yAxis, zAxis;
        for (final Axis xAxis : s_xAxes) {
            for (int i = 0; i < 4; i++) {
                yAxis = s_axes[xAxis.index][(i + 1) % 4];
                zAxis = s_axes[xAxis.index][i];
                orientations[xAxis.index][yAxis.index] = new BlockOrientation(xAxis, yAxis, zAxis);
            }
        }

        BASE_ORIENTATION = orientations[Axis.POS_X.index][Axis.POS_Y.index];

        // Time to link all states. 24 states where every state have 6 links which
        // give a total of 144 links.
        BlockOrientation o;
        Axis axisA, axisB;
        for (final Axis xAxis : s_xAxes) {
            for (int i = 0; i < 4; i++) {
                yAxis = s_axes[xAxis.index][(i + 1) % 4];
                zAxis = s_axes[xAxis.index][i];

                o = orientations[xAxis.index][yAxis.index];
                // 6 possible alignments
                if (xAxis.isXAligned()) {
                    if (yAxis.isYAligned()) {
                        // X,Y,Z
                        axisA = nextCW(xAxis, yAxis);
                        o.xAxisCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCCW(xAxis, yAxis);
                        o.xAxisCCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCW(yAxis, xAxis);
                        o.yAxisCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCCW(yAxis, xAxis);
                        o.yAxisCCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCW(zAxis, xAxis);
                        axisB = nextCW(zAxis, yAxis);
                        o.zAxisCW = orientations[axisA.index][axisB.index];
                        axisA = nextCCW(zAxis, xAxis);
                        axisB = nextCCW(zAxis, yAxis);
                        o.zAxisCCW = orientations[axisA.index][axisB.index];
                    } else {
                        // X,Z,Y
                        axisA = nextCW(xAxis, yAxis);
                        o.xAxisCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCCW(xAxis, yAxis);
                        o.xAxisCCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCW(zAxis, xAxis);
                        axisB = nextCW(zAxis, yAxis);
                        o.yAxisCW = orientations[axisA.index][axisB.index];
                        axisA = nextCCW(zAxis, xAxis);
                        axisB = nextCCW(zAxis, yAxis);
                        o.yAxisCCW = orientations[axisA.index][axisB.index];
                        axisA = nextCW(yAxis, xAxis);
                        o.zAxisCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCCW(yAxis, xAxis);
                        o.zAxisCCW = orientations[axisA.index][yAxis.index];
                    }
                } else if (xAxis.isYAligned()) {
                    if (yAxis.isXAligned()) {
                        // Y,X,Z
                        axisA = nextCW(yAxis, xAxis);
                        o.xAxisCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCCW(yAxis, xAxis);
                        o.xAxisCCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCW(xAxis, yAxis);
                        o.yAxisCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCCW(xAxis, yAxis);
                        o.yAxisCCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCW(zAxis, xAxis);
                        axisB = nextCW(zAxis, yAxis);
                        o.zAxisCW = orientations[axisA.index][axisB.index];
                        axisA = nextCCW(zAxis, xAxis);
                        axisB = nextCCW(zAxis, yAxis);
                        o.zAxisCCW = orientations[axisA.index][axisB.index];
                    } else {
                        // Y,Z,X
                        axisA = nextCW(zAxis, xAxis);
                        axisB = nextCW(zAxis, yAxis);
                        o.xAxisCW = orientations[axisA.index][axisB.index];
                        axisA = nextCCW(zAxis, xAxis);
                        axisB = nextCCW(zAxis, yAxis);
                        o.xAxisCCW = orientations[axisA.index][axisB.index];
                        axisA = nextCW(xAxis, yAxis);
                        o.yAxisCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCCW(xAxis, yAxis);
                        o.yAxisCCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCW(yAxis, xAxis);
                        o.zAxisCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCCW(yAxis, xAxis);
                        o.zAxisCCW = orientations[axisA.index][yAxis.index];
                    }
                } else {
                    if (yAxis.isYAligned()) {
                        // Z,Y,X
                        axisA = nextCW(zAxis, xAxis);
                        axisB = nextCW(zAxis, yAxis);
                        o.xAxisCW = orientations[axisA.index][axisB.index];
                        axisA = nextCCW(zAxis, xAxis);
                        axisB = nextCCW(zAxis, yAxis);
                        o.xAxisCCW = orientations[axisA.index][axisB.index];
                        axisA = nextCW(yAxis, xAxis);
                        o.yAxisCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCCW(yAxis, xAxis);
                        o.yAxisCCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCW(xAxis, yAxis);
                        o.zAxisCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCCW(xAxis, yAxis);
                        o.zAxisCCW = orientations[xAxis.index][axisA.index];
                    } else {
                        // Z,X,Y
                        axisA = nextCW(yAxis, xAxis);
                        o.xAxisCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCCW(yAxis, xAxis);
                        o.xAxisCCW = orientations[axisA.index][yAxis.index];
                        axisA = nextCW(zAxis, xAxis);
                        axisB = nextCW(zAxis, yAxis);
                        o.yAxisCW = orientations[axisA.index][axisB.index];
                        axisA = nextCCW(zAxis, xAxis);
                        axisB = nextCCW(zAxis, yAxis);
                        o.yAxisCCW = orientations[axisA.index][axisB.index];
                        axisA = nextCW(xAxis, yAxis);
                        o.zAxisCW = orientations[xAxis.index][axisA.index];
                        axisA = nextCCW(xAxis, yAxis);
                        o.zAxisCCW = orientations[xAxis.index][axisA.index];
                    }
                }
            }
        }
    }

    private final Axis _xAxis;
    private final Axis _yAxis;
    private final Axis _zAxis;

    /** Every orientation can transit into 6 other orientations */
    private BlockOrientation xAxisCW;
    private BlockOrientation xAxisCCW;
    private BlockOrientation yAxisCW;
    private BlockOrientation yAxisCCW;
    private BlockOrientation zAxisCW;
    private BlockOrientation zAxisCCW;

    private BlockOrientation(final Axis xAxis, final Axis yAxis, final Axis zAxis) {
        _xAxis = xAxis;
        _yAxis = yAxis;
        _zAxis = zAxis;
    }

    public Axis xAxis() {
        return _xAxis;
    }

    public Axis yAxis() {
        return _yAxis;
    }

    public Axis zAxis() {
        return _zAxis;
    }

    public BlockOrientation xAxisCW() {
        return xAxisCW;
    }

    public BlockOrientation xAxisCCW() {
        return xAxisCCW;
    }

    public BlockOrientation yAxisCW() {
        return yAxisCW;
    }

    public BlockOrientation yAxisCCW() {
        return yAxisCCW;
    }

    public BlockOrientation zAxisCW() {
        return zAxisCW;
    }

    public BlockOrientation zAxisCCW() {
        return zAxisCCW;
    }

    @Override
    public String toString() {
        return "[" + _xAxis + "," + _yAxis + "," + _zAxis + "]";
    }

    public void updateOrientation(final Matrix3 store) {
        store.setColumn(0, _xAxis.vector);
        store.setColumn(1, _yAxis.vector);
        store.setColumn(2, _zAxis.vector);
    }
}
