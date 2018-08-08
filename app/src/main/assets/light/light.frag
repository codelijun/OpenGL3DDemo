precision mediump float;

uniform sampler2D uTexture;
varying vec2 vTexcoord;
varying vec4 vDiffuseColor;

void main(){
    gl_FragColor=vDiffuseColor * texture2D(uTexture, vTexcoord);
}