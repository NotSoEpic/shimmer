#version 110

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec4 CameraRotation;
uniform float STime;
uniform float Strength;

uniform vec2 ScreenSize;
uniform float _FOV;

varying vec2 texCoord;

float linearize_depth(float d,float zNear,float zFar) {
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}
float getDistStrength(float dist, float strength) {
    return 1. - clamp(sqrt(dist) * strength / 3., 0., 1.);
}
float getDarkStrength(vec3 col, float strength) {
    float darkness = max(col.r, max(col.g, col.b));
    return clamp(darkness + 1. - strength * 2., 0., 1.);
}

void main() {
    gl_FragColor = texture2D(DiffuseSampler, texCoord);
    if (Strength > 0.) {
        vec2 oneTexel = vec2(1.) / ScreenSize;
        float depth = linearize_depth(texture2D(DepthSampler, texCoord).x, .1, 1000.);
        // https://github.com/onnowhere/depth_shaders/blob/master/Depth%20Shader%20Test%20(Distance)/assets/minecraft/shaders/program/distance.fsh
        float distance = length(vec3(1., (2.*texCoord - 1.) * vec2(ScreenSize.x/ScreenSize.y, 1.) * tan(radians(_FOV / 2.))) * depth);
        vec3 incol = texture2D(DiffuseSampler, texCoord).rgb;
        
        // todo: completely rework the starfield effect
        // turn texCoord into vector out of camera
        // transform using CameraAngle quaternion into world relative vector
        // use vector to render stuff?
        // ray marching?
        // https://www.youtube.com/watch?v=svLzmFuSBhk
        vec3 starcol = vec3(0.);
        if (rand(texCoord + oneTexel * vec2(0., floor(-STime * 60.) * 2.)) <= .01) {
            starcol = vec3(1.);
        }

        float distVal = getDistStrength(distance, Strength);
        float darkVal = getDarkStrength(incol, Strength);
        float mixing = max(distVal, darkVal);

        vec3 col = mix(starcol, incol, distVal);

        gl_FragColor = vec4(col, 1.);
    } else {
        gl_FragColor = texture2D(DiffuseSampler, texCoord);
    }
}
