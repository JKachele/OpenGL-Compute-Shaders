/******************************************
 *Project-------OpenGL-Compute-Shaders
 *File----------Scene.java
 *Author--------Justin Kachele
 *Date----------10/20/2022
 *License-------GNU GENERAL PUBLIC LICENSE
 ******************************************/
package com.jkachele.simulation.engine;

import com.jkachele.simulation.renderer.Shader;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Scene {
    private static int vaoID;
    private static int vboID;
    private static int eboID;

    private static float[] vertexArray = {
            0.0f, 0.0f, 0.0f,           0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,           0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,           1.0f, 1.0f,
            1.0f, 0.0f, 0.0f,           1.0f, 0.0f
    };

    private static int[] elementArray = {
            2, 1, 0,
            0, 1, 3
    };

    public static void init() {
        Shader shader = new Shader("assets/default.glsl");
        shader.compile();

        // ============================================================
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ============================================================
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO upload the vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionsSize = 3;
        int uvSize = 2;
        int vertexSizeBytes = (positionsSize + uvSize) * Float.BYTES;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, uvSize, GL_FLOAT, false, vertexSizeBytes,
                (positionsSize) * Float.BYTES);
        glEnableVertexAttribArray(1);
    }

    public static void update() {
        
    }
}
