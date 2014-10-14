package jmini3d.material;

import jmini3d.Color4;
import jmini3d.Texture;

/**
 * A material with PHONG lighting
 */
public class PhongMaterial extends Material {

	// alpha is intensity
	public Color4 ambient;
	public Color4 diffuse;
	public Color4 specular;
	public float shininess = 8;

	public PhongMaterial(Color4 ambient, Color4 diffuse, Color4 specular) {
		super(new Color4(255, 255, 255, 255));
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}

	public PhongMaterial(Texture texture, Color4 ambient, Color4 diffuse, Color4 specular) {
		super(texture);
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}

	public PhongMaterial(Texture texture, Color4 ambient, Color4 diffuse, Color4 specular, float shininess) {
		super(texture);
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
	}
}
