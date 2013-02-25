
package com.ardor3d.example.basic;

import java.nio.FloatBuffer;

import com.ardor3d.example.ExampleBase;
import com.ardor3d.framework.Canvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Thomas �hl�n, thahlen@gmail.com
 */
public class RollingBlockExample extends ExampleBase {
    private Line _axisLines;
    private Quad _plane = new Quad("", 100, 100);
    private RollingBlock _rc;

    public static void main(final String[] args) {
        start(RollingBlockExample.class);
    }

    @Override
    protected void initExample() {
        _canvas.setVSyncEnabled(true);
        final Camera cam = _canvas.getCanvasRenderer().getCamera();
        cam.setLocation(new Vector3(0, -25, 15));
        cam.lookAt(Vector3.ZERO, Vector3.UNIT_Z);

        _plane = new Quad("", 100, 100);
        final Texture tex = TextureManager.load("images/ardor3d_white_256.jpg", Texture.MinificationFilter.Trilinear,
                true);
        tex.setWrap(WrapMode.Repeat);
        final TextureState ts = new TextureState();
        ts.setTexture(tex);
        _plane.setRenderState(ts);
        final FloatBuffer tbuf = _plane.getMeshData().getTextureBuffer(0);
        tbuf.rewind();
        tbuf.put(0).put(20);
        tbuf.put(0).put(0);
        tbuf.put(20).put(0);
        tbuf.put(20).put(20);
        _root.attachChild(_plane);

        _rc = new RollingBlock(2, 4, 6);
        _rc.setTranslation(10, 0, 3);
        _root.attachChild(_rc.getBlockNode());

        _axisLines = new Line();
        _axisLines.setLineWidth(3);
        _axisLines.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        _axisLines.getMeshData().setIndexMode(IndexMode.Lines);
        final FloatBuffer vBuf = BufferUtils.createVector3Buffer(6);
        vBuf.put(0).put(0).put(0);
        vBuf.put(5).put(0).put(0);
        vBuf.put(0).put(0).put(0);
        vBuf.put(0).put(5).put(0);
        vBuf.put(0).put(0).put(0);
        vBuf.put(0).put(0).put(5);
        final FloatBuffer cBuf = BufferUtils.createVector4Buffer(6);
        cBuf.put(1).put(0).put(0).put(1);
        cBuf.put(1).put(0).put(0).put(1);
        cBuf.put(0).put(1).put(0).put(1);
        cBuf.put(0).put(1).put(0).put(1);
        cBuf.put(0).put(0).put(1).put(1);
        cBuf.put(0).put(0).put(1).put(1);
        _axisLines.getMeshData().setVertexBuffer(vBuf);
        _axisLines.getMeshData().setColorBuffer(cBuf);
        _rc.getBlockNode().attachChild(_axisLines);

        _frameHandler.addUpdater(_rc);
    }

    @Override
    protected void updateExample(final ReadOnlyTimer timer) {
        final Vector3 position = new Vector3(_rc.getTranslation());
        position.setZ(2);
        _canvas.getCanvasRenderer().getCamera().lookAt(position, Vector3.UNIT_Z);
        _axisLines.setRotation(_rc.getBlockMesh().getRotation());
    }

    @Override
    public void registerInputTriggers() {
        // super.registerInputTriggers();

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                exit();
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LEFT), new TriggerAction() {
            public void perform(final Canvas canvas, final TwoInputStates inputState, final double tpf) {
                _rc.rollYCW();
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.RIGHT), new TriggerAction() {
            public void perform(final Canvas canvas, final TwoInputStates inputState, final double tpf) {
                _rc.rollYCCW();
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.UP), new TriggerAction() {
            public void perform(final Canvas canvas, final TwoInputStates inputState, final double tpf) {
                _rc.rollXCW();
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DOWN), new TriggerAction() {
            public void perform(final Canvas canvas, final TwoInputStates inputState, final double tpf) {
                _rc.rollXCCW();
            }
        }));
    }
}
