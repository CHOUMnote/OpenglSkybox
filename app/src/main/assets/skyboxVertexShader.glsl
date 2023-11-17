#version 300 es

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

layout(location=12) in vec4 vPosition;

out vec3 fTexCoord;

void main(void){
    mat4 view = mat4(mat3(viewMatrix));
    gl_Position = projectionMatrix * view * vPosition;
    fTexCoord = vec3(vPosition);
}
