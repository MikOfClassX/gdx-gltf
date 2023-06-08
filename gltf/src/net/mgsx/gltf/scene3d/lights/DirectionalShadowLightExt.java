/*
 * Created on Jun 7, 2023
 */

package net.mgsx.gltf.scene3d.lights;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.*;

/** Extension of {@link DirectionalShadowLight} that auto computes the best ortho matrix for the given frustum
 * @author dar */
public class DirectionalShadowLightExt extends DirectionalShadowLight {

	private final ShadowLightHelper shadowLightHelper = new ShadowLightHelper();

	private float shadowNear = 0;
	private float shadowFar = 0;
	private float zFrustumScale = 1;

	/** default constructor */
	public DirectionalShadowLightExt (int shadowMapWidth, int shadowMapHeight) {
		super(shadowMapWidth, shadowMapHeight);
	}

	/** @param shadowNear the shadowNear to set */
	public void setShadowNear (float shadowNear) {
		this.shadowNear = shadowNear;
	}

	/** @param shadowFar the shadowFar to set */
	public void setShadowFar (float shadowFar) {
		this.shadowFar = shadowFar;
	}

	/** @return the shadowNear */
	public float getShadowNear () {
		return shadowNear;
	}

	/** @return the shadowFar */
	public float getShadowFar () {
		return shadowFar;
	}

	/** @param zFrustumScale the zFrustumScale to set */
	public void setZFrustumScale (float zFrustumScale) {
		this.zFrustumScale = zFrustumScale;
	}

	/** @return the zFrustumScale */
	public float getZFrustumScale () {
		return zFrustumScale;
	}

	/*
	 * @see net.mgsx.gltf.scene3d.lights.DirectionalShadowLight#setViewport(float, float, float, float)
	 */
	@Override
	public DirectionalShadowLight setViewport (float shadowViewportWidth, float shadowViewportHeight, float shadowNear,
		float shadowFar) {
		return this;
	}

	/*
	 * @see net.mgsx.gltf.scene3d.lights.DirectionalShadowLight#setBounds(com.badlogic.gdx.math.collision.BoundingBox)
	 */
	@Override
	public DirectionalShadowLight setBounds (BoundingBox box) {
		return this;
	}

	/*
	 * @see net.mgsx.gltf.scene3d.lights.DirectionalShadowLight#setCenter(com.badlogic.gdx.math.Vector3)
	 */
	@Override
	public DirectionalShadowLight setCenter (Vector3 center) {
		return this;
	}

	/*
	 * @see net.mgsx.gltf.scene3d.lights.DirectionalShadowLight#validate(com.badlogic.gdx.graphics.Camera)
	 */
	@Override
	protected void validate (Camera sceneCamera) {
		// safety check the camera instance, this method only works with perspective camera
		if (sceneCamera instanceof PerspectiveCamera) {
			// note: cannot use pattern matching in instanceof due to "sourceCompatibility = 1.8"
			shadowLightHelper.fitLightToCameraFrustum((PerspectiveCamera)sceneCamera, this, shadowNear, shadowFar, zFrustumScale);
		}
	}
}
