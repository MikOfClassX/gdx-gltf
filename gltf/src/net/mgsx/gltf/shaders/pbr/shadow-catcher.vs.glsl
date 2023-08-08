#line 1

#include <compat.vs.glsl>

varying vec3 v_position;

attribute vec3 a_position;
uniform mat4 u_projViewTrans;

uniform mat4 u_worldTrans;

#ifdef shadowMapFlag
uniform mat4 u_shadowMapProjViewTrans;
varying vec3 v_shadowMapUv;
#ifdef numCSM
uniform mat4 u_csmTransforms[numCSM];
varying vec3 v_csmUVs[numCSM];
#endif
#endif //shadowMapFlag

void main() {
	vec3 morph_pos = a_position;	
	vec4 pos = u_worldTrans * vec4(morph_pos, 1.0);
	
	v_position = vec3(pos.xyz) / pos.w;
	gl_Position = u_projViewTrans * pos;
	
	#ifdef shadowMapFlag
		vec4 spos = u_shadowMapProjViewTrans * pos;
		v_shadowMapUv.xyz = (spos.xyz / spos.w) * 0.5 + 0.5;
		v_shadowMapUv.z = min(v_shadowMapUv.z, 0.998);
		#ifdef numCSM
		for(int i=0 ; i<numCSM ; i++){
			vec4 csmPos = u_csmTransforms[i] * pos;
			v_csmUVs[i].xyz = (csmPos.xyz / csmPos.w) * 0.5 + 0.5;
		}
		#endif
	#endif //shadowMapFlag	
}
