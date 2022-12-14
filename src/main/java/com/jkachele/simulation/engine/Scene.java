/******************************************
 *Project-------OpenGL-Compute-Shaders
 *File----------Scene.java
 *Author--------Justin Kachele
 *Date----------10/20/2022
 *License-------Mozilla Public License Version 2.0
 ******************************************/
package com.jkachele.simulation.engine;

import com.jkachele.simulation.computeShader.Compute;
import com.jkachele.simulation.renderer.ShaderParser;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glGetIntegeri_v;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.stb.STBImage.*;

public class Scene {
    private static int vaoID;
    private static int vboID;
    private static int eboID;
    private static int textureID;

    private static ShaderParser defaultShader;
    private static Compute computeShader;

    private static float[] vertexArray = {
            -1.0f, -1.0f, 0.0f,           0.0f, 0.0f,     1.0f, 0.0f, 0.0f, 1.0f,     // 0: Bottom Left
            -1.0f,  1.0f, 0.0f,           0.0f, 1.0f,     0.0f, 1.0f, 0.0f, 1.0f,     // 1: Top Left
             1.0f,  1.0f, 0.0f,           1.0f, 1.0f,     0.0f, 0.0f, 1.0f, 1.0f,     // 2: Top Right
             1.0f, -1.0f, 0.0f,           1.0f, 0.0f,     1.0f, 1.0f, 1.0f, 1.0f      // 3: Bottom Right
    };

    private static int[] elementArray = {
            0, 2, 1,
            0, 3, 2
    };

    public static void init() {
        defaultShader = new ShaderParser("assets/default.glsl");
        defaultShader.compile();

        computeShader = new Compute("assets/testCompute.comp.glsl", new Vector2i(10, 1));
        computeShader.use();
        float[] valuesA = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ByteBuffer vbb = ByteBuffer.allocate(valuesA.length * Float.BYTES);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer values = vbb.asFloatBuffer();
        values.put(valuesA);
        values.position(0);
        computeShader.setValues(valuesA);

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
        int colorSize = 4;
        int vertexSizeBytes = (positionsSize + uvSize + colorSize) * Float.BYTES;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, uvSize, GL_FLOAT, false, vertexSizeBytes,
                (positionsSize) * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, colorSize, GL_FLOAT, false, vertexSizeBytes,
                (positionsSize + uvSize) * Float.BYTES);
        glEnableVertexAttribArray(2);

        String texturePath = "assets/testImage.png";
        texture(texturePath);

//        getWorkGroupSizes();

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load("assets/blendImageG.png", width, height, channels, 0);
        if (image != null) {
//            FloatBuffer imageFB = image.asFloatBuffer();
            System.out.println(image);
        }
    }

    public static void update(float dt) {
        defaultShader.use();

        // Upload texture to shader
        defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);

        glBindVertexArray(vaoID);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        glBindVertexArray(0);

        defaultShader.detach();

        computeShader.use();
        computeShader.dispatch();
        computeShader.waitForCompute();
        FloatBuffer outValues = computeShader.getValuesBuffer();

        computeShader.dispose();

//        System.out.println(Arrays.toString(outValues.array()));
    }

    private static void texture(String filePath) {
        // Generate the texture on GPU
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Set the texture parameters
        // Repeat image in both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);   // Wrap in x direction
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);   // Wrap in y direction

        // When stretching and shrinking the image, pixelate
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);  // Stretching
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);  // Shrinking

        // Load the image
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load(filePath, width, height, channels, 0);

        float[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        // Upload the image to the GPU
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, 10, 1, 0, GL_RED, GL_FLOAT, data);
        if (image != null) {

            if (channels.get(0) == 3) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0),
                        0, GL_RGB, GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                        0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "Error: (Texture) Unknown number of color channels: " + channels.get(0);
            }
        } else {
            assert false : "Error: (Texture) Could not load image from file: " + filePath;
        }

        // Clean up to prevent memory leaks
        stbi_image_free(image);
    }

    public static void getWorkGroupSizes() {
        int[][] workGroupCount = new int[3][2];
        int[][] workGroupSize = new int[3][2];
        int[] workGroupInv = new int[2];

        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, workGroupCount[0]);
        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, workGroupCount[1]);
        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, workGroupCount[2]);
        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, workGroupSize[0]);
        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, workGroupSize[1]);
        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, workGroupSize[2]);
        glGetIntegerv(GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, workGroupInv);

        System.out.println(Arrays.deepToString(workGroupCount));
        System.out.println(Arrays.deepToString(workGroupSize));
        System.out.println(Arrays.toString(workGroupInv));
    }
}
