
package com.ardor3d.example.basic;

import com.ardor3d.framework.Updater;
import com.ardor3d.image.Texture;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

/**
 * @author Thomas �hl�n, thahlen@gmail.com
 */
public class RollingBlock implements Updater {
    private enum RollType {
        X_AXIS_CW, X_AXIS_CCW, Y_AXIS_CW, Y_AXIS_CCW, Z_AXIS_CW, Z_AXIS_CCW;
    }

    /** x,y and z extent */
    private final double xExt;
    private final double yExt;
    private final double zExt;

    private final double xyRadius;
    private final double xzRadius;
    private final double yzRadius;

    private final Node node;
    private final Box cube;

    private BlockOrientation state = BlockOrientation.BASE_ORIENTATION;
    private final Matrix3 orientation = new Matrix3();

    private boolean isRolling = false;
    private RollType rollType;
    private double rollSpeed = 0.025;

    private final Vector3 translation = new Vector3();
    private final Vector3 position = new Vector3();

    /** Fields used for calculation during a roll */
    private double radius;
    private final Vector3 offset = new Vector3();
    private final Vector3 pivot = new Vector3();
    private final Vector3 u = new Vector3();
    private final Vector3 v = new Vector3();
    private double angle = 0;

    /**
     * 
     * @param xExtent
     *            - block size along the X Axis
     * @param yExtent
     *            - block size along the Y Axis
     * @param zExtent
     *            - block size along the Z Axis
     */
    public RollingBlock(final double xExtent, final double yExtent, final double zExtent) {
        xExt = xExtent / 2;
        yExt = yExtent / 2;
        zExt = zExtent / 2;

        xzRadius = Math.sqrt(xExt * xExt + zExt * zExt);
        xyRadius = Math.sqrt(xExt * xExt + yExt * yExt);
        yzRadius = Math.sqrt(yExt * yExt + zExt * zExt);

        node = new Node();
        cube = new Box("", Vector3.ZERO, xExt, yExt, zExt);
        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.load("images/ardor3d_white_256.jpg", Texture.MinificationFilter.Trilinear, true));
        cube.setRenderState(ts);
        node.attachChild(cube);
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    public double getRollSpeed() {
        return rollSpeed;
    }

    public void setRollSpeed(final double rollSpeed) {
        this.rollSpeed = rollSpeed;
    }

    public ReadOnlyVector3 getTranslation() {
        return position;
    }

    public void setTranslation(final double x, final double y, final double z) {
        translation.set(x, y, z);
        position.set(translation);
        node.setTranslation(translation);
    }

    /**
     * Roll clockwise around world X axis
     */
    public void rollXCW() {
        rollX(1);
    }

    /**
     * Roll counterclockwise around world X axis
     */
    public void rollXCCW() {
        rollX(-1);
    }

    /**
     * Roll clockwise around world Y axis
     */
    public void rollYCW() {
        rollY(1);
    }

    /**
     * Roll counterclockwise around world Y axis
     */
    public void rollYCCW() {
        rollY(-1);
    }

    /**
     * Roll clockwise around world Y axis
     */
    public void rollZCW() {
        rollZ(1);
    }

    /**
     * Roll counterclockwise around world Y axis
     */
    public void rollZCCW() {
        rollZ(-1);
    }

    private void rollX(int oSign) {
        if (isRolling) {
            return;
        }
        isRolling = true;

        xAxisRoll(oSign, 1);

        // Need to find out direction of the local axis aligned with world X
        if (state.xAxis().isXAligned()) {
            if (state.xAxis() != Axis.POS_X) {
                oSign = -oSign;
            }
        } else if (state.yAxis().isXAligned()) {
            if (state.yAxis() != Axis.POS_X) {
                oSign = -oSign;
            }
        } else {
            if (state.zAxis() != Axis.POS_X) {
                oSign = -oSign;
            }
        }
        rollType = oSign > 0 ? RollType.X_AXIS_CW : RollType.X_AXIS_CCW;
    }

    private void rollY(int oSign) {
        if (isRolling) {
            return;
        }
        isRolling = true;

        yAxisRoll(oSign, 1);

        // Need to find out direction of the local axis aligned with world Y
        if (state.xAxis().isYAligned()) {
            if (state.xAxis() != Axis.POS_Y) {
                oSign = -oSign;
            }
        } else if (state.yAxis().isYAligned()) {
            if (state.yAxis() != Axis.POS_Y) {
                oSign = -oSign;
            }
        } else {
            if (state.zAxis() != Axis.POS_Y) {
                oSign = -oSign;
            }
        }
        rollType = oSign > 0 ? RollType.Y_AXIS_CW : RollType.Y_AXIS_CCW;
    }

    private void rollZ(int oSign) {
        if (isRolling) {
            return;
        }
        isRolling = true;

        zAxisRoll(oSign, 1);

        // Need to find out direction of the local axis aligned with world Z
        if (state.xAxis().isZAligned()) {
            if (state.xAxis() != Axis.POS_Z) {
                oSign = -oSign;
            }
        } else if (state.yAxis().isZAligned()) {
            if (state.yAxis() != Axis.POS_Z) {
                oSign = -oSign;
            }
        } else {
            if (state.zAxis() != Axis.POS_Z) {
                oSign = -oSign;
            }
        }
        rollType = oSign > 0 ? RollType.Z_AXIS_CW : RollType.Z_AXIS_CCW;
    }

    public void xAxisRoll(final int oSign, final int pSign) {
        double tx, ty, tz;
        tx = ty = tz = 0;

        if (state.xAxis().isXAligned()) {
            radius = yzRadius;
            ty = oSign * (yExt + zExt);
            if (state.yAxis().isYAligned()) {
                tz = pSign * (yExt - zExt);
                pivot.set(0, oSign * yExt, pSign * -zExt);
                u.set(0, oSign * -yExt, pSign * zExt).normalizeLocal();
                v.set(0, oSign * zExt, pSign * yExt).normalizeLocal();
            } else {
                tz = pSign * (zExt - yExt);
                pivot.set(0, oSign * zExt, pSign * -yExt);
                u.set(0, oSign * -zExt, pSign * yExt).normalizeLocal();
                v.set(0, oSign * yExt, pSign * zExt).normalizeLocal();
            }
        } else if (state.yAxis().isXAligned()) {
            radius = xzRadius;
            ty = oSign * (xExt + zExt);
            if (state.xAxis().isYAligned()) {
                tz = pSign * (xExt - zExt);
                pivot.set(0, oSign * xExt, pSign * -zExt);
                u.set(0, oSign * -xExt, pSign * zExt).normalizeLocal();
                v.set(0, oSign * zExt, pSign * xExt).normalizeLocal();
            } else {
                tz = pSign * (zExt - xExt);
                pivot.set(0, oSign * zExt, pSign * -xExt);
                u.set(0, oSign * -zExt, pSign * xExt).normalizeLocal();
                v.set(0, oSign * xExt, pSign * zExt).normalizeLocal();
            }
        } else {
            radius = xyRadius;
            ty = oSign * (xExt + yExt);
            if (state.xAxis().isYAligned()) {
                tz = pSign * (xExt - yExt);
                pivot.set(0, oSign * xExt, pSign * -yExt);
                u.set(0, oSign * -xExt, pSign * yExt).normalizeLocal();
                v.set(0, oSign * yExt, pSign * xExt).normalizeLocal();
            } else {
                tz = pSign * (yExt - xExt);
                pivot.set(0, oSign * yExt, pSign * -xExt);
                u.set(0, oSign * -yExt, pSign * xExt).normalizeLocal();
                v.set(0, oSign * xExt, pSign * yExt).normalizeLocal();
            }
        }
        offset.set(tx, ty, tz);
    }

    /** Rolling around Y axis */
    public void yAxisRoll(final int oSign, final int pSign) {
        double tx, ty, tz;
        tx = ty = tz = 0;

        // A block can have 6 different alignments (if the center is symmetrical along each axis)
        if (state.xAxis().isYAligned()) {
            radius = yzRadius;
            tx = oSign * -(yExt + zExt);
            if (state.yAxis().isXAligned()) {
                tz = pSign * (yExt - zExt);
                pivot.set(oSign * -yExt, 0, pSign * -zExt);
                u.set(oSign * yExt, 0, pSign * zExt).normalizeLocal();
                v.set(oSign * -zExt, 0, pSign * yExt).normalizeLocal();
            } else {
                tz = pSign * (zExt - yExt);
                pivot.set(oSign * -zExt, 0, pSign * -yExt);
                u.set(oSign * zExt, 0, pSign * yExt).normalizeLocal();
                v.set(oSign * -yExt, 0, pSign * zExt).normalizeLocal();
            }
        } else if (state.yAxis().isYAligned()) {
            radius = xzRadius;
            tx = oSign * -(xExt + zExt);
            if (state.xAxis().isXAligned()) {
                tz = pSign * (xExt - zExt);
                pivot.set(oSign * -xExt, 0, pSign * -zExt);
                u.set(oSign * xExt, 0, pSign * zExt).normalizeLocal();
                v.set(oSign * -zExt, 0, pSign * xExt).normalizeLocal();
            } else {
                tz = pSign * (zExt - xExt);
                pivot.set(oSign * -zExt, 0, pSign * -xExt);
                u.set(oSign * zExt, 0, pSign * xExt).normalizeLocal();
                v.set(oSign * -xExt, 0, pSign * zExt).normalizeLocal();
            }
        } else {
            radius = xyRadius;
            tx = oSign * -(xExt + yExt);
            if (state.xAxis().isXAligned()) {
                tz = pSign * (xExt - yExt);
                pivot.set(oSign * -xExt, 0, pSign * -yExt);
                u.set(oSign * xExt, 0, pSign * yExt).normalizeLocal();
                v.set(oSign * -yExt, 0, pSign * xExt).normalizeLocal();
            } else {
                tz = pSign * (yExt - xExt);
                pivot.set(oSign * -yExt, 0, pSign * -xExt);
                u.set(oSign * yExt, 0, pSign * xExt).normalizeLocal();
                v.set(oSign * -xExt, 0, pSign * yExt).normalizeLocal();
            }
        }
        offset.set(tx, ty, tz);
    }

    /** Rolling around Z axis */
    public void zAxisRoll(final int oSign, final int pSign) {
        double tx, ty, tz;
        tx = ty = tz = 0;

        // A block can have 6 different alignments (if the center is symmetrical along each axis)
        if (state.xAxis().isZAligned()) {
            radius = yzRadius;
            tx = oSign * (yExt + zExt);
            if (state.yAxis().isXAligned()) {
                ty = pSign * (yExt - zExt);
                pivot.set(oSign * yExt, pSign * -zExt, 0);
                u.set(oSign * -yExt, pSign * zExt, 0).normalizeLocal();
                v.set(oSign * zExt, pSign * yExt, 0).normalizeLocal();
            } else {
                ty = pSign * (zExt - yExt);
                pivot.set(oSign * zExt, pSign * -yExt, 0);
                u.set(oSign * -zExt, pSign * yExt, 0).normalizeLocal();
                v.set(oSign * yExt, pSign * zExt, 0).normalizeLocal();
            }
        } else if (state.yAxis().isZAligned()) {
            radius = xzRadius;
            tx = oSign * (xExt + zExt);
            if (state.xAxis().isXAligned()) {
                ty = pSign * (xExt - zExt);
                pivot.set(oSign * xExt, pSign * -zExt, 0);
                u.set(oSign * -xExt, pSign * zExt, 0).normalizeLocal();
                v.set(oSign * zExt, pSign * xExt, 0).normalizeLocal();
            } else {
                ty = pSign * (zExt - xExt);
                pivot.set(oSign * zExt, pSign * -xExt, 0);
                u.set(oSign * -zExt, pSign * xExt, 0).normalizeLocal();
                v.set(oSign * xExt, pSign * zExt, 0).normalizeLocal();
            }
        } else {
            radius = xyRadius;
            tx = oSign * (xExt + yExt);
            if (state.xAxis().isXAligned()) {
                ty = pSign * (xExt - yExt);
                pivot.set(oSign * xExt, pSign * -yExt, 0);
                u.set(oSign * -xExt, pSign * yExt, 0).normalizeLocal();
                v.set(oSign * yExt, pSign * xExt, 0).normalizeLocal();
            } else {
                ty = pSign * (yExt - xExt);
                pivot.set(oSign * yExt, pSign * -xExt, 0);
                u.set(oSign * -yExt, pSign * xExt, 0).normalizeLocal();
                v.set(oSign * xExt, pSign * yExt, 0).normalizeLocal();
            }
        }
        offset.set(tx, ty, tz);
    }

    public Mesh getBlockMesh() {
        return cube;
    }

    public Node getBlockNode() {
        return node;
    }

    @Override
    public void update(final ReadOnlyTimer timer) {
        double cosA, sinA;
        final Vector3 rotationAxis = new Vector3();
        final Matrix3 rotation = new Matrix3();

        if (isRolling) {
            if (angle > 0.5 * Math.PI) {
                angle = 0.5 * Math.PI;
                isRolling = false;
            }

            cosA = Math.cos(angle);
            sinA = Math.sin(angle);
            position.set(cosA * u.getX() + sinA * v.getX(), cosA * u.getY() + sinA * v.getY(), cosA * u.getZ() + sinA
                    * v.getZ());
            position.multiplyLocal(radius);
            position.addLocal(pivot);
            position.addLocal(translation);
            node.setTranslation(position);

            rotationAxis.set(u).crossLocal(v); // XXX: Could precalculate this
            rotation.fromAngleNormalAxis(angle, rotationAxis);
            rotation.multiply(orientation, rotation);
            cube.setRotation(rotation);

            angle += Math.PI * rollSpeed;

            // is Rolling done?
            if (!isRolling) {
                angle = 0;
                updateOffset();
            }
        }
    }

    private void updateOffset() {
        switch (rollType) {
            case X_AXIS_CW:
                state = state.xAxisCW();
                break;
            case X_AXIS_CCW:
                state = state.xAxisCCW();
                break;
            case Y_AXIS_CW:
                state = state.yAxisCW();
                break;
            case Y_AXIS_CCW:
                state = state.yAxisCCW();
                break;
            case Z_AXIS_CW:
                state = state.zAxisCW();
                break;
            case Z_AXIS_CCW:
                state = state.zAxisCCW();
                break;
        }

        // If we use a grid here would be the spot to calculate the absolute translation given the
        // offset
        // _translation.addLocal( offset );
        translation.set(position);
        state.updateOrientation(orientation);
    }
}