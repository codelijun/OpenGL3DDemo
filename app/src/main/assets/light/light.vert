attribute vec4 aPosition;
attribute vec2 aTexcoord;
attribute vec3 aNormal;
varying vec2 vTexcoord;
varying vec4 vDiffuseColor;
uniform vec3 uLightColor;
uniform float uDiffuseStrength;
uniform vec3 uLightPosition;
uniform mat4 uMVPMatrix;

//漫反射的计算
vec4 diffuseColor(){
    //模型变换后的位置
    vec3 fragPos=(uMVPMatrix*aPosition).xyz;
    //光照方向
    vec3 direction=normalize(uLightPosition-fragPos);
    //模型变换后的法线向量
    vec3 normal=normalize(mat3(uMVPMatrix)*aNormal);
    //max(cos(入射角)，0)
    float diff = max(dot(normal,direction), 0.0);
    //材质的漫反射系数*max(cos(入射角)，0)*光照颜色
    vec3 diffuse=uDiffuseStrength * diff * uLightColor;
    return vec4(diffuse,1.0);
}

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vDiffuseColor=diffuseColor();
    vTexcoord= aTexcoord;
}