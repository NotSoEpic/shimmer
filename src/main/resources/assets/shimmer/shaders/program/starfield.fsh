#version 110

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec4 CamQuart;
uniform vec3 CamPos;
uniform float STime;
uniform float Strength;

uniform vec2 ScreenSize;
uniform float _FOV;

varying vec2 texCoord;

float linearize_depth(float d,float zNear,float zFar) {
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}
float getDistStrength(float dist, float strength) {
    return 1. - clamp(sqrt(dist) * strength / 3., 0., 1.);
}
float getDarkStrength(vec3 col, float strength) {
    float darkness = max(col.r, max(col.g, col.b));
    return clamp(darkness + 1. - strength * 2., 0., 1.);
}
float rand(vec3 r)
{
    r = fract(r * vec3(123.34, 456.21, 92.76));
    r += dot(r, r+45.32);
    return fract(r.x * r.y * r.z);
}
vec3 randCol(vec3 r) {
    r = fract(r * vec3(123.34, 456.21, 92.76));
    r += dot(r, r+45.32);
    return fract(r);
}

//	Classic Perlin 3D Noise 
//	by Stefan Gustavson
//
vec4 permute(vec4 x){return mod(((x*34.0)+1.0)*x, 289.0);}
vec4 taylorInvSqrt(vec4 r){return 1.79284291400159 - 0.85373472095314 * r;}
vec3 fade(vec3 t) {return t*t*t*(t*(t*6.0-15.0)+10.0);}

float cnoise(vec3 P){
    vec3 Pi0 = floor(P); // Integer part for indexing
    vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
    Pi0 = mod(Pi0, 289.0);
    Pi1 = mod(Pi1, 289.0);
    vec3 Pf0 = fract(P); // Fractional part for interpolation
    vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
    vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
    vec4 iy = vec4(Pi0.yy, Pi1.yy);
    vec4 iz0 = Pi0.zzzz;
    vec4 iz1 = Pi1.zzzz;

    vec4 ixy = permute(permute(ix) + iy);
    vec4 ixy0 = permute(ixy + iz0);
    vec4 ixy1 = permute(ixy + iz1);

    vec4 gx0 = ixy0 / 7.0;
    vec4 gy0 = fract(floor(gx0) / 7.0) - 0.5;
    gx0 = fract(gx0);
    vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
    vec4 sz0 = step(gz0, vec4(0.0));
    gx0 -= sz0 * (step(0.0, gx0) - 0.5);
    gy0 -= sz0 * (step(0.0, gy0) - 0.5);

    vec4 gx1 = ixy1 / 7.0;
    vec4 gy1 = fract(floor(gx1) / 7.0) - 0.5;
    gx1 = fract(gx1);
    vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
    vec4 sz1 = step(gz1, vec4(0.0));
    gx1 -= sz1 * (step(0.0, gx1) - 0.5);
    gy1 -= sz1 * (step(0.0, gy1) - 0.5);

    vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
    vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
    vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
    vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
    vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
    vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
    vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
    vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

    vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
    g000 *= norm0.x;
    g010 *= norm0.y;
    g100 *= norm0.z;
    g110 *= norm0.w;
    vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
    g001 *= norm1.x;
    g011 *= norm1.y;
    g101 *= norm1.z;
    g111 *= norm1.w;

    float n000 = dot(g000, Pf0);
    float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
    float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
    float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
    float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
    float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
    float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
    float n111 = dot(g111, Pf1);

    vec3 fade_xyz = fade(Pf0);
    vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
    vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
    float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x);
    return 2.2 * n_xyz;
}

// looks cool but it isnt the effect i wanted
/*vec3 starLayer(vec3 look, vec3 pos, int iter) {
    vec3 col = vec3(0.);
    vec3 march = pos;
    float minde = 1000.;
    float scale = 8.;
    for (int i = 0; i < iter; i++) {
        vec3 seed = floor(march / scale);
        float size = 1.;//.2 + rand(seed) * 1.8;
        vec3 center = vec3(.5 * scale);// + (vec3(.5) - vec3(rand(seed + 1.), rand(seed + 2.), rand(seed - 3.))) * (1. - size);
        vec3 starCol = randCol(seed);
        float de = length(mod(march, scale) - center) - size;
        if (de < .01) {
            return starCol;
        }
        march += look * de;
        minde = min(minde, de);
        col = starCol;
    }
    return vec3(0.);
}*/

float maxabs3(vec3 v) {
    return max(max(abs(v.x), abs(v.y)), abs(v.z));
}


vec3 starLayer(vec3 look, vec3 pos, int stars) {
    float scale = 5.;
    for (int i = 0; i < stars; i++) {
        vec3 starpos = (randCol(vec3(i)) - vec3(.5)) * scale;
        
        vec3 starrel = mod(starpos - pos, scale) - vec3(scale) * .5;
        vec3 cell = floor((starrel + pos) / scale);
        float dist = length(starrel);
        float dim = clamp((scale * .5 - dist) * 2., 0., 1.);
        float neardim = clamp((dist - .5) * 2., 0., 1.);
        float colmul = min(dim, neardim);
        if (colmul > 0.) {
            float seed = fract(float(i) * 23.121);
            float stardot = dot(starrel, look);
            // 2 * (1-x) ~= acos(x)^2 as x -> 1
            // do not care about accuracy when x is far from 1
            if (2. * (dist - stardot) < seed * .0004 * dist) {
                return vec3(colmul);
            }
        }
    }
    return vec3(0.);
}

void main() {
    if (Strength > 0.) {
        vec4 q = vec4(-CamQuart.x, CamQuart.yz, -CamQuart.w);
        // i only barely understand how this works
        vec3 pixVector = vec3((2.*texCoord - 1.) * vec2(ScreenSize.x/ScreenSize.y, 1.) * tan(radians(_FOV / 2.)), 1.);
        // this truly escapes me though
        vec3 temp = cross(q.xyz, pixVector) + q.w * pixVector;
        vec3 rotated = normalize(pixVector + 2.*cross(q.xyz, temp)) * vec3(-1., 1., 1.);
        // rotated is a normalized vector representing the direction a raycast from the camera would take to reach that pixel
        // or something
        
        float depth = linearize_depth(texture2D(DepthSampler, texCoord).x, .1, 1000.);
        // https://github.com/onnowhere/depth_shaders/blob/master/Depth%20Shader%20Test%20(Distance)/assets/minecraft/shaders/program/distance.fsh
        //float distance = length(vec3(1., (2.*texCoord - 1.) * vec2(ScreenSize.x/ScreenSize.y, 1.) * tan(radians(_FOV / 2.))) * depth);
        float distance = length(pixVector) * depth;
        
        // do cool star stuff here!
        // this creates quite visible poor performance :(
        //vec3 starcol = starLayer(rotated, CamPos, 40);
        //gl_FragColor = vec4(starcol, 1.);
        vec3 starcol = vec3(0.);
        // stop doing cool star stuff here :/
        
        vec3 incol = texture2D(DiffuseSampler, texCoord).rgb;
        float distVal = getDistStrength(distance, Strength);
        float darkVal = getDarkStrength(incol, Strength);
        float mixing = max(distVal, darkVal);

        vec3 col = mix(starcol, incol, distVal);

        gl_FragColor = vec4(col, 1.);//*/
    } else {
        gl_FragColor = texture2D(DiffuseSampler, texCoord);
    }
}
