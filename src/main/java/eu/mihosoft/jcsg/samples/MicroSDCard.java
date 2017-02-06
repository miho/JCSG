/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples;

import eu.mihosoft.jcsg.Extrude;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.vvecmath.Vector3d;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author miho
 */
public class MicroSDCard {

    public CSG toCSG() {

        // data taken from
        // https://www.sparkfun.com/datasheets/Prototyping/microSD_Spec.pdf
        // total card width
        double A = 10.9;
        // front width
        double A1 = 9.6;

        double A8 = 0.6;

        // total card length
        double B = 14.9;

        double B1 = 6.3;

        // slit pos relative to front
        double B10 = 7.8;

        // slit thickness
        double B11 = 1.1;

        // total card thickness 
        double C1 = 0.6;

        double A_ = A - A1;
        double B_ = B - B1 + A_;

        return Extrude.points(Vector3d.xyz(0, 0, C1),
                Vector3d.xy(0, 0),
                Vector3d.xy(A, 0),
                Vector3d.xy(A, B),
                Vector3d.xy(A_, B),
                Vector3d.xy(A_, B_),
                Vector3d.xy(0, B - B1),
                Vector3d.xy(0, B - B10),
                Vector3d.xy(A8, B - B10),
                Vector3d.xy(A8, B - B10 - B11),
                Vector3d.xy(0, B - B10 - B11 - A8)
        );
    }

    public static void main(String[] args) throws IOException {

        FileUtil.write(Paths.get("mircosd.stl"), new MicroSDCard().toCSG().toStlString());

    }

}
