#version 300 es
precision mediump float;

in vec3 fTexCoord;
out vec4 fragColor;

uniform samplerCube cubeMap;
uniform samplerCube cubeMap2;
uniform float blendFactor;

uniform vec3 fogColor;

const float lowerLimit = -5.0;
const float upperLimit = 2.0;

void main(void){
//    fragColor = texture(cubeMap2, fTexCoord);

    vec4 texture1 = texture(cubeMap, fTexCoord);
    vec4 texture2 = texture(cubeMap2, fTexCoord);
    vec4 color = mix(texture1, texture2, blendFactor);

    float factor = (fTexCoord.y - lowerLimit) / (upperLimit - lowerLimit);
    factor = clamp(factor, 0.0, 1.0);
    fragColor = mix(vec4(fogColor, 1.0), color, factor);
}

