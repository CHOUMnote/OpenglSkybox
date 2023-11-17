#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 worldMat;

layout(location=12) in vec4 vPosition;
layout (location = 13) in vec3 vNormal;

out vec3 Normal;
out vec3 Position;


void main(void){
    Normal = normalize(transpose(inverse(mat3(worldMat))) * vNormal);
    Position = vec3(worldMat * vPosition);
    gl_Position = uMVPMatrix * vPosition;
}
