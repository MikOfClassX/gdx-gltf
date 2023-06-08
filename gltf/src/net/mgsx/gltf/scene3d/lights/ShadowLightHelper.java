/*
 * Created on Jun 6, 2023
 */

package net.mgsx.gltf.scene3d.lights;

import static com.badlogic.gdx.math.Matrix4.*;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;

/** Helper class used to fit the ortho matrix of the shadow light camera to the scene camera frustum
 * 
 * @author dar */
public class ShadowLightHelper {

	private final Matrix4 frustum = new Matrix4();
	private final Matrix4 projViewMat = new Matrix4();

	private final Matrix4 lightViewMat = new Matrix4();
	private final Matrix4 lightProjMat = new Matrix4();

	private final Vector3 cuboidCenter = new Vector3();

	private final Vector3 lightDirection = new Vector3();

	private final Vector3 lookAtPosition = new Vector3();
	private final Vector3 lookAtTarget = new Vector3();
	private final Vector3 lookAtUp = new Vector3(0, 1, 0);

	private final Vector3 tmpVec = new Vector3();

	private final Vector3 transformedVector = new Vector3();

	private final Vector3[] frustumCorners = new Vector3[8];

	/** */
	public ShadowLightHelper () {
		for (int i = 0; i < frustumCorners.length; i++) {
			frustumCorners[i] = new Vector3();
		}
	}

	/** Fit the light ortho matrix to the given scene camera frustum. Reference: https://learnopengl.com/Guest-Articles/2021/CSM
	 * 
	 * @param sceneCam the perspective scene camera where to get the frustum
	 * @param dirLight the directional light to update
	 * @param near the near value (i.e. sceneCam#near)
	 * @param far the far value (i.e. sceneCam#far)
	 * @param zMult extension of frustum (i.e. 25f) */
	public void fitLightToCameraFrustum (PerspectiveCamera sceneCam, DirectionalShadowLight dirLight, float near, float far,
		float zMult) {
		// compute frustum following near/far values
		frustum.setToProjection(near, far, sceneCam.fieldOfView, sceneCam.viewportWidth / sceneCam.viewportHeight);

		// compute cuboid corners
		final Vector3[] corners = getFrustumCornersWorldSpace(frustum, sceneCam.view);

		// reset center
		cuboidCenter.scl(0);

		// compute cuboid center (avarage)
		for (int i = 0; i < corners.length; i++) {
			cuboidCenter.add(corners[i]);
		}
		cuboidCenter.scl(1f / corners.length);

		// store light direction
		lightDirection.set(dirLight.direction);

		// compute look-at position
		lookAtPosition.set(lightDirection).add(cuboidCenter);

		// compute look-at target
		lightDirection.nor();
		lookAtTarget.set(lookAtPosition).add(lightDirection);

		// comptue look-at up
		tmpVec.set(lightDirection).crs(lookAtUp);
		lookAtUp.set(tmpVec).crs(lightDirection).nor();

		// compute light view matrix
		lightViewMat.setToLookAt(lookAtPosition, lookAtTarget, lookAtUp);

		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxZ = Float.MIN_VALUE;

		// find minumum/maxumum of the ortho projection
		for (int i = 0; i < corners.length; i++) {
			Vector3 v = corners[i];

			float[] vec4 = {v.x, v.y, v.z, 1f};
			multVec4f(lightViewMat, vec4);

			transformedVector.set(vec4);
			minX = Math.min(minX, transformedVector.x);
			maxX = Math.max(maxX, transformedVector.x);
			minY = Math.min(minY, transformedVector.y);
			maxY = Math.max(maxY, transformedVector.y);
			minZ = Math.min(minZ, transformedVector.z);
			maxZ = Math.max(maxZ, transformedVector.z);
		}

		// avoid division by zero
		if (zMult == 0) zMult = 1.0f;

		// respect the zMult overlap
		if (minZ < 0) {
			minZ *= zMult;
		} else {
			minZ /= zMult;
		}
		if (maxZ < 0) {
			maxZ /= zMult;
		} else {
			maxZ *= zMult;
		}

		// compute light projection matrix
		lightProjMat.setToOrtho(minX, maxX, minY, maxY, minZ, maxZ);

		// update light camera matrices manually
		final Camera lightCam = dirLight.getCamera();

		lightCam.projection.set(lightProjMat);
		lightCam.view.set(lightViewMat);

		lightCam.combined.set(lightProjMat);
		Matrix4.mul(lightCam.combined.val, lightCam.view.val);

		// update frustum
		lightCam.invProjectionView.set(lightCam.combined);
		Matrix4.inv(lightCam.invProjectionView.val);
		lightCam.frustum.update(lightCam.invProjectionView);
	}

	/** Multiply the given matrix with the given vector4
	 * 
	 * @param mat4 the matrix
	 * @param vec4 vector4 array
	 * @return */
	private float[] multVec4f (final Matrix4 mat4, final float[] vec4) {
		final float[] val = mat4.val;
		final float x = vec4[0], y = vec4[1], z = vec4[2], w = vec4[3];

		vec4[0] = x * val[M00] + y * val[M01] + z * val[M02] + w * val[M03];
		vec4[1] = x * val[M10] + y * val[M11] + z * val[M12] + w * val[M13];
		vec4[2] = x * val[M20] + y * val[M21] + z * val[M22] + w * val[M23];
		vec4[3] = x * val[M30] + y * val[M31] + z * val[M32] + w * val[M33];

		return vec4;
	}

	/** compute frustum corners in world space following the given projection/view matrix
	 * 
	 * @param proj the camera projection matrix
	 * @param view the camera view matrix
	 * @return the resulting corners */
	private Vector3[] getFrustumCornersWorldSpace (final Matrix4 proj, final Matrix4 view) {
		// inverse projection/view matrix
		projViewMat.set(proj).mul(view);
		projViewMat.inv();

		// compute frustum corners in world space
		int idx = 0;
		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					// NDC cube
					float[] vec4 = {2.0f * x - 1.0f, 2.0f * y - 1.0f, 2.0f * z - 1.0f, 1f};
					multVec4f(projViewMat, vec4);

					// compute frustum corner
					frustumCorners[idx].set(vec4);
					frustumCorners[idx].scl(1f / vec4[3]);

					idx++;
				}
			}
		}

		return frustumCorners;
	}
}
