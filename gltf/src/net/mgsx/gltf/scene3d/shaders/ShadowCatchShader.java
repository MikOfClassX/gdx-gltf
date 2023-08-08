/*
 * Created on Aug 2, 2023
 */

package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.shaders.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;

import net.mgsx.gltf.scene3d.attributes.*;
import net.mgsx.gltf.scene3d.lights.*;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

/** @author dar */
public class ShadowCatchShader extends DefaultShader {
	private int u_csmSamplers;
	private int u_csmPCFClip;
	private int u_csmTransforms;

	public ShadowCatchShader (Renderable renderable, Config config, String prefix) {
		super(renderable, config, prefix);
	}

	/*
	 * @see com.badlogic.gdx.graphics.g3d.shaders.BaseShader#init(com.badlogic.gdx.graphics.glutils.ShaderProgram,
	 * com.badlogic.gdx.graphics.g3d.Renderable)
	 */
	@Override
	public void init (ShaderProgram program, Renderable renderable) {
		super.init(program, renderable);

		u_csmSamplers = program.fetchUniformLocation("u_csmSamplers", false);
		u_csmPCFClip = program.fetchUniformLocation("u_csmPCFClip", false);
		u_csmTransforms = program.fetchUniformLocation("u_csmTransforms", false);
	}

	/*
	 * @see com.badlogic.gdx.graphics.g3d.shaders.DefaultShader#bindLights(com.badlogic.gdx.graphics.g3d.Renderable,
	 * com.badlogic.gdx.graphics.g3d.Attributes)
	 */
	@Override
	protected void bindLights (Renderable renderable, Attributes attributes) {
		super.bindLights(renderable, attributes);

		// XXX update color (to apply intensity) before default binding
		DirectionalLightsAttribute dla = attributes.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
		if (dla != null) {
			for (DirectionalLight light : dla.lights) {
				if (light instanceof DirectionalLightEx) {
					((DirectionalLightEx)light).updateColor();
				}
			}
		}

		super.bindLights(renderable, attributes);

		CascadeShadowMapAttribute csmAttrib = attributes.get(CascadeShadowMapAttribute.class, CascadeShadowMapAttribute.Type);
		if (csmAttrib != null && u_csmSamplers >= 0) {
			Array<DirectionalShadowLight> lights = csmAttrib.cascadeShadowMap.lights;
			for (int i = 0; i < lights.size; i++) {
				DirectionalShadowLight light = lights.get(i);
				float mapSize = light.getDepthMap().texture.getWidth();
				float pcf = 1.f / (2 * mapSize);
				float clip = 3.f / (2 * mapSize);

				int unit = context.textureBinder.bind(light.getDepthMap());
				program.setUniformi(u_csmSamplers + i, unit);
				program.setUniformMatrix(u_csmTransforms + i, light.getProjViewTrans());
				program.setUniformf(u_csmPCFClip + i, pcf, clip);
			}
		}
	}
}
