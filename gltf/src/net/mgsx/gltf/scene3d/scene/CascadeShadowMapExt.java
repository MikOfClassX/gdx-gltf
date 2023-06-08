/*
 * Created on Jun 8, 2023
 */
package net.mgsx.gltf.scene3d.scene;

import net.mgsx.gltf.scene3d.lights.*;

/**
 * @author dar
 */
public class CascadeShadowMapExt extends CascadeShadowMap {
	// heuristic: based on visual experience
	private final static float[]	splits			= { 0.0f, 0.05f, .2f, .4f, .6f, .8f, 1f };

	/** the max n. of cacades */
	public final static int			MAX_CASCADES	= splits.length - 1;

	private class CascadeSplit
	{
		private float	near;
		private float	far;
	}

	private final CascadeSplit[]					cascadeSplits;

	/**
	 * @param cascadeCount how many cascade (at least 1)
	 */
	public CascadeShadowMapExt(int cascadeCount)
	{
		super(Math.min(cascadeCount, MAX_CASCADES));

		// create cascade split
		cascadeSplits = new CascadeSplit[cascadeCount];
		for (int i = 0; i < cascadeSplits.length; i++)
		{
			cascadeSplits[i] = new CascadeSplit();
		}
	}
	
	/**
	 * @param base
	 */
	public void setCascade (DirectionalShadowLightExt base) {
		setCascade(base, 0);
	}

	/*
	 * @see net.mgsx.gltf.scene3d.scene.CascadeShadowMap#setCascade(net.mgsx.gltf.scene3d.lights.DirectionalShadowLight, float)
	 */
	@Override
	public void setCascade (DirectionalShadowLight base, float downscale) {
			int w = base.getFrameBuffer().getWidth();
			int h = base.getFrameBuffer().getHeight();
			for (int i = 0; i < cascadeCount; i++)
			{
				if (i < lights.size)
				{
					DirectionalShadowLight light = lights.get(i);
					if (light.getFrameBuffer().getWidth() != w ||
							light.getFrameBuffer().getHeight() != h)
					{
						light.dispose();
						lights.set(i, createLight(w, h));
					}
				}
				else
				{
					lights.add(createLight(w, h));
				}
			}

			DirectionalShadowLightExt baseExt = (DirectionalShadowLightExt) base;
			// compute cascade split around base light near/far
			computeCascadeSplit(baseExt.getShadowNear(), baseExt.getShadowFar());

			// reverse order : first is the max LOD.
			for (int i = lights.size - 1; i >= 0; i--)
			{
				DirectionalShadowLightExt light = (DirectionalShadowLightExt) lights.get(i);
				light.baseColor.set(base.baseColor);
				light.color.set(base.baseColor);
				light.direction.set(base.direction);

				final CascadeSplit cascadeSplit = cascadeSplits[i];
				light.setShadowNear(cascadeSplit.near);
				light.setShadowFar(cascadeSplit.far);
				light.setZFrustumScale(baseExt.getZFrustumScale());
			}
	}

	/**
	 * Compute the cascade split around near far planes
	 * 
	 * @param near
	 * @param far
	 */
	private void computeCascadeSplit(final float near, float far)
	{
		float delta = (far - near);

		for (int i = 0; i < cascadeSplits.length; i++)
		{
			float y0 = splits[i];
			float y1 = splits[i + 1];

			cascadeSplits[i].near = near + y0 * delta;
			cascadeSplits[i].far = near + y1 * delta;
		}
	}

	/**
	 * Allow subclass to use their own shadow light implementation.
	 * 
	 * @param width
	 * @param height
	 * @return a new directional shadow light.
	 */
	protected DirectionalShadowLight createLight(int width, int height)
	{
		return new DirectionalShadowLightExt(width, height);
	}
}
