#line 1

#include <compat.fs.glsl>
#include <functions.glsl>
#include <material.glsl>
#include <env.glsl>
#include <lights.glsl>
#include <shadows.glsl>

void main() {
#ifdef shadowMapFlag
	out_FragColor = vec4(0.0, 0.0, 0.0, getBaseColor().a * (1.0 - getShadow()));
#endif
	applyClippingPlane();
}