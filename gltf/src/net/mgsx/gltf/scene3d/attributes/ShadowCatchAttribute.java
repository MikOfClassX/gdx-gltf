/*
 * Created on May 25, 2023
 */

package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.*;

public class ShadowCatchAttribute extends Attribute 
{
	public static final String TypeAlias = "ShadowCatch";
	public static final long Type = register(TypeAlias);

	public ShadowCatchAttribute () {
		super(Type);
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (Attribute o) {
		return (int)(type - o.type);
	}

	/*
	 * @see com.badlogic.gdx.graphics.g3d.Attribute#copy()
	 */
	@Override
	public Attribute copy () {
		return new ShadowCatchAttribute();
	}
}
