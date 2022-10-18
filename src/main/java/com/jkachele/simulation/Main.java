/******************************************
 *Project-------OpenGL-Compute_Shaders
 *File----------Main.java
 *Author--------Justin Kachele
 *Date----------10/18/2022
 *License-------GNU GENERAL PUBLIC LICENSE
 ******************************************/
package com.jkachele.simulation;

import com.jkachele.simulation.engine.Engine;
import com.jkachele.simulation.util.Color;

public class Main {
    public static void main(String[] args) {
        final int DEFAULT_WIDTH = 1920;
        final int DEFAULT_HEIGHT = (DEFAULT_WIDTH / 16) * 9;    // 16 x 9 aspect ratio
        final String DEFAULT_TITLE = "Java Game";
        final Color backgroundColor = Color.BLACK;

        Engine engine = new Engine(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE, backgroundColor);
        engine.start();
    }
}
