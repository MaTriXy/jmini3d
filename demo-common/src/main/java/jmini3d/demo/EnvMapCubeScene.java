package jmini3d.demo;

import jmini3d.CubeMapTexture;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;

public class EnvMapCubeScene extends Scene {

	public EnvMapCubeScene() {
		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

		Material material1 = new Material(null, envMap, 0);
		material1.useEnvMapAsMap = true;

		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);

		addChild(o3d);
	}
}
