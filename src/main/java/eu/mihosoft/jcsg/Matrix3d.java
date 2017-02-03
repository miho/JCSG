/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg;

import eu.mihosoft.vvecmath.Vector3d;

/**
 * 3D Matrix3d
 *
 * @author cpoliwoda
 */
public class Matrix3d {

    public double m11, m12, m13;
    public double m21, m22, m23;
    public double m31, m32, m33;

    public static final Matrix3d ZERO = new Matrix3d(0, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final Matrix3d UNITY = new Matrix3d(1, 0, 0, 0, 1, 0, 0, 0, 1);

    public Matrix3d(double m11, double m12, double m13,
            double m21, double m22, double m23,
            double m31, double m32, double m33) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    @Override
    public String toString() {
        return "[" + m11 + ", " + m12 + ", " + m13 + "]\n"
                + "[" + m21 + ", " + m22 + ", " + m23 + "]\n"
                + "[" + m31 + ", " + m32 + ", " + m33 + "]";
    }

    /**
     * Returns the product of this matrix and the specified value.
     *
     * @param a the value
     *
     * <b>Note:</b> this matrix is not modified.
     *
     * @return the product of this matrix and the specified value
     */
    public Matrix3d times(double a) {
        return new Matrix3d(
                m11 * a, m12 * a, m13 * a,
                m21 * a, m22 * a, m23 * a,
                m31 * a, m32 * a, m33 * a);
    }

    /**
     * Returns the product of this matrix and the specified vector.
     *
     * @param a the vector
     *
     * <b>Note:</b> the vector is not modified.
     *
     * @return the product of this matrix and the specified vector
     */
    public Vector3d times(Vector3d a) {
        return Vector3d.xyz(
                m11 * a.x() + m12 * a.y() + m13 * a.z(),
                m21 * a.x() + m22 * a.y() + m23 * a.z(),
                m31 * a.x() + m32 * a.y() + m33 * a.z());
    }

    static double maxOf3Values(double[] values) {

        if (values[0] > values[1]) {

            if (values[0] > values[2]) {
                return (values[0]);
            } else {
                return (values[2]);
            }

        } else {

            if (values[1] > values[2]) {
                return (values[1]);
            } else {
                return (values[2]);
            }

        }

    }

}
