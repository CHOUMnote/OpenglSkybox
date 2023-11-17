#version 300 es
precision mediump float;

in vec3 Normal;
in vec3 Position;

uniform vec3 eyePos;
uniform samplerCube cubeMap;

out vec4 fragColor;

void main(void){
    vec3 I = normalize(Position - eyePos);
    vec3 R = reflect(I, normalize(Normal));
    fragColor = vec4(texture(cubeMap, R).rgb, 1.0);
}

