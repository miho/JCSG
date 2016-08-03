/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.v3d;

// TODO: Auto-generated Javadoc
/**
 * 3D Matrix3d.
 *
 * @author cpoliwoda
 */
public class Matrix3d {

    /** The m13. */
    public double m11, m12, m13;
    
    /** The m23. */
    public double m21, m22, m23;
    
    /** The m33. */
    public double m31, m32, m33;

    /** The Constant ZERO. */
    public static final Matrix3d ZERO = new Matrix3d(0, 0, 0, 0, 0, 0, 0, 0, 0);
    
    /** The Constant UNITY. */
    public static final Matrix3d UNITY = new Matrix3d(1, 0, 0, 0, 1, 0, 0, 0, 1);

    /**
     * Instantiates a new matrix3d.
     *
     * @param m11 the m11
     * @param m12 the m12
     * @param m13 the m13
     * @param m21 the m21
     * @param m22 the m22
     * @param m23 the m23
     * @param m31 the m31
     * @param m32 the m32
     * @param m33 the m33
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + m11 + ", " + m12 + ", " + m13 + "]\n"
                + "[" + m21 + ", " + m22 + ", " + m23 + "]\n"
                + "[" + m31 + ", " + m32 + ", " + m33 + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Matrix3d other = (Matrix3d) obj;
        if (Double.doubleToLongBits(this.m11) != Double.doubleToLongBits(other.m11)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m12) != Double.doubleToLongBits(other.m12)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m13) != Double.doubleToLongBits(other.m13)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m21) != Double.doubleToLongBits(other.m21)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m22) != Double.doubleToLongBits(other.m22)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m23) != Double.doubleToLongBits(other.m23)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m31) != Double.doubleToLongBits(other.m31)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m32) != Double.doubleToLongBits(other.m32)) {
            return false;
        }
        if (Double.doubleToLongBits(this.m33) != Double.doubleToLongBits(other.m33)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the product of this matrix and the specified value.
     *
     * @param a the value
     *
     *  Note:  this matrix is not modified.
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
     *  Note:  the vector is not modified.
     *
     * @return the product of this matrix and the specified vector
     */
    public Vector3d times(Vector3d a) {
        return new Vector3d(
                m11 * a.x + m12 * a.y + m13 * a.z,
                m21 * a.x + m22 * a.y + m23 * a.z,
                m31 * a.x + m32 * a.y + m33 * a.z);
    }

}
