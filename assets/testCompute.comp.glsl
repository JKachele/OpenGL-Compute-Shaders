#version 460 core

layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;
layout(r32f, binding = 0) uniform image2D out_tex1;
layout(r32f, binding = 1) uniform image2D out_tex2;

void main() {
    // get position to read/write data from
    ivec2 pos = ivec2( gl_GlobalInvocationID.xy );

    // get value stored in the image
    float in_val1 = imageLoad( out_tex1, pos ).r;
    float in_val2 = imageLoad( out_tex2, pos).r;

    vec4 pixel = vec4(in_val1 + 1, 0, 0, 0);

    // store new value in image
    imageStore( out_tex1, pos, pixel );
}