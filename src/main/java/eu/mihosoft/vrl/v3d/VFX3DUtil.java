/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package eu.mihosoft.vrl.v3d;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.MeshView;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.CullFace;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;

/**
 * Utility class that allows to visualize meshes created with null {@link MathUtil#evaluateFunction(
 *   eu.mihosoft.vrl.javaone2013.math.Function2D,
 *   int, int, float, float, float, double, double, double, double)
 * }.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VFX3DUtil {

    private VFX3DUtil() {
        throw new AssertionError("don't instanciate me!");
    }

  

    /**
     * Adds rotation behavior to the specified node.
     *
     * @param n node
     * @param eventReceiver receiver of the event
     * @param btn mouse button that shall be used for this behavior
     */
    public static void addMouseBehavior(
            Node n, Scene eventReceiver, MouseButton btn) {
        eventReceiver.addEventHandler(MouseEvent.ANY,
                new MouseBehaviorImpl1(n, btn));
    }

    /**
     * Adds rotation behavior to the specified node.
     *
     * @param n node
     * @param eventReceiver receiver of the event
     * @param btn mouse button that shall be used for this behavior
     */
    public static void addMouseBehavior(
            Node n, Node eventReceiver, MouseButton btn) {
        eventReceiver.addEventHandler(MouseEvent.ANY,
                new MouseBehaviorImpl1(n, btn));
    }
}

// rotation behavior implementation
class MouseBehaviorImpl1 implements EventHandler<MouseEvent> {

    private double anchorAngleX;
    private double anchorAngleY;
    private double anchorX;
    private double anchorY;
    private final Rotate rotateX = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
    private final Rotate rotateZ = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
    private MouseButton btn;

    public MouseBehaviorImpl1(Node n, MouseButton btn) {
        n.getTransforms().addAll(rotateX, rotateZ);
        this.btn = btn;

        if (btn == null) {
            this.btn = MouseButton.MIDDLE;
        }
    }

    @Override
    public void handle(MouseEvent t) {
        if (!btn.equals(t.getButton())) {
            return;
        }

        t.consume();

        if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
            anchorX = t.getSceneX();
            anchorY = t.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateZ.getAngle();
            t.consume();
        } else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
            rotateZ.setAngle(anchorAngleY + (anchorX - t.getSceneX()) * 0.7);
            rotateX.setAngle(anchorAngleX - (anchorY - t.getSceneY()) * 0.7);

        }

    }
}
