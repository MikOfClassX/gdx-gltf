//#line 1

#include <compat.fs.glsl>
#include <functions.glsl>
#include <material.glsl>
#include <env.glsl>
#include <lights.glsl>
#include <shadows.glsl>

varying vec3 var_position;
//varying vec3 v_normal;

uniform mat4 u_viewTrans;

#ifdef POSITION_LOCATION
layout(location = POSITION_LOCATION) out vec3 out_position;
#endif

#ifdef NORMAL_LOCATION
layout(location = NORMAL_LOCATION) out vec3 out_normals;
#endif

void main() {

#ifdef POSITION_LOCATION
    out_position = var_position;
#endif

#ifdef NORMAL_LOCATION
	vec3 n = getNormal();
	out_normals = normalize((u_viewTrans * vec4(n.xyz, 0.0)).xyz);
#endif

#ifdef shadowMapFlag
	out_FragColor = vec4(0.0, 0.0, 0.0, getBaseColor().a * (1.0 - getShadow()));
#endif
	applyClippingPlane();
	
}