/******************************************
 *Project-------OpenGL-Compute-Shaders
 *File----------Compute.java
 *Author--------Justin Kachele
 *Date----------10/25/2022
 *License-------GNU GENERAL PUBLIC LICENSE
 ******************************************/
package com.jkachele.simulation.computeShader;

import org.joml.Vector2i;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL30.GL_R32F;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class Compute {
    private int textureID;
    private Vector2i workSize;
    private Shader shader;

    public Compute(String shaderSource, Vector2i workSize) {
        this.workSize = workSize;
        shader = new Shader(shaderSource);
        init();
    }

    private void init() {
        shader.compile();

        // generate texture
        textureID = glGenTextures();
        glActiveTexture( GL_TEXTURE0 );
        glBindTexture( GL_TEXTURE_2D, textureID);

        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );

        // create empty texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, workSize.x, workSize.y, 0, GL_RED, GL_FLOAT, 0);
        glBindImageTexture(0, textureID, 0, false, 0, GL_READ_WRITE, GL_R32F);

        glActiveTexture( GL_TEXTURE1 );
        glBindTexture( GL_TEXTURE_2D, textureID);

        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );

        // create empty texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, workSize.x, workSize.y, 0, GL_RED, GL_FLOAT, 0);
        glBindImageTexture(0, textureID, 0, false, 0, GL_READ_WRITE, GL_R32F);
    }

    public void setValues(float[] values) {
        glActiveTexture( GL_TEXTURE0 );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, workSize.x, workSize.y, 0, GL_RED, GL_FLOAT, values);
    }

    public void setValues(float[] values1, float[] values2) {
        glActiveTexture( GL_TEXTURE0 );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, workSize.x, workSize.y, 0, GL_RED, GL_FLOAT, values1);
        glActiveTexture( GL_TEXTURE1 );
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, workSize.x, workSize.y, 0, GL_RED, GL_FLOAT, values2);
    }

    public float[] getValues() {
        int totalWorkSize = workSize.x * workSize.y;
        float[] values = new float[totalWorkSize];
        glActiveTexture( GL_TEXTURE0 );
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RED, GL_FLOAT, values);

        return values;
    }

    public void use() {
        shader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void dispose() {
        shader.detach();
    }

    public void dispatch() {
        // just keep it simple, 2d work group
        glDispatchCompute(workSize.x, workSize.y, 1);
    }

    public void waitForCompute() {
        glMemoryBarrier(GL_ALL_BARRIER_BITS);
    }
}
