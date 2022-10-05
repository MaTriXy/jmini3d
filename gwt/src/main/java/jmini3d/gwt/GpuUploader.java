package jmini3d.gwt;

import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.client.JsUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;

import java.util.ArrayList;
import java.util.HashMap;

import jmini3d.CubeMapTexture;
import jmini3d.GpuObjectStatus;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.VertexColors;
import jmini3d.geometry.Geometry;
import jmini3d.material.Material;
import jmini3d.shader.ShaderKey;

public class GpuUploader {

	WebGLRenderingContext gl;
	ResourceLoader resourceLoader;

	HashMap<Geometry, GeometryBuffers> geometryBuffers = new HashMap<>();
	HashMap<VertexColors, Integer> vertexColorsBuffers = new HashMap<>();
	HashMap<Texture, Integer> textures = new HashMap<>();
	HashMap<Texture, ImageElement> textureImages = new HashMap<>();
	HashMap<CubeMapTexture, Integer> cubeMapTextures = new HashMap<>();
	HashMap<CubeMapTexture, ImageElement[]> cubeMapImages = new HashMap<>();
	ArrayList<Program> shaderPrograms = new ArrayList<>();

	GpuUploaderListener gpuUploaderListener;

	public GpuUploader(WebGLRenderingContext gl, ResourceLoader resourceLoader, GpuUploaderListener gpuUploaderListener) {
		this.gl = gl;
		this.resourceLoader = resourceLoader;
		this.gpuUploaderListener = gpuUploaderListener;
	}

	public Program getProgram(Scene scene, Material material) {
		if (scene.shaderKey == -1) {
			scene.shaderKey = ShaderKey.getSceneKey(scene);
		}
		if (material.shaderKey == -1) {
			material.shaderKey = ShaderKey.getMaterialKey(material);
		}
		int key = scene.shaderKey & material.shaderKey;
		Program program = null;
		// Use ArrayList instead HashMap to avoid Integer creation
		for (int i = 0; i < shaderPrograms.size(); i++) {
			if (key == shaderPrograms.get(i).shaderKey) {
				program = shaderPrograms.get(i);
			}
		}
		if (program == null) {
			program = new Program(gl);
			program.shaderKey = key;
			program.init(scene, material, resourceLoader, gpuUploaderListener);
			shaderPrograms.add(program);
		}
		return program;
	}

	public GeometryBuffers upload(Geometry geometry3d) {
		GeometryBuffers buffers = geometryBuffers.get(geometry3d);
		if (buffers == null) {
			buffers = new GeometryBuffers();
			geometryBuffers.put(geometry3d, buffers);
		}

		if ((geometry3d.status & GpuObjectStatus.VERTICES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.VERTICES_UPLOADED;
			float[] vertex = geometry3d.vertex();
			if (vertex != null) {
				if (buffers.vertexBufferId == null) {
					buffers.vertexBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.vertexBufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, JsUtils.createFloat32Array(JsArrayUtils.readOnlyJsArray(vertex)), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
			float[] normals = geometry3d.normals();
			if (normals != null) {
				if (buffers.normalsBufferId == null) {
					buffers.normalsBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.normalsBufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, JsUtils.createFloat32Array(JsArrayUtils.readOnlyJsArray(normals)), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
			float[] uvs = geometry3d.uvs();
			if (uvs != null) {
				if (buffers.uvsBufferId == null) {
					buffers.uvsBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.uvsBufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, JsUtils.createFloat32Array(JsArrayUtils.readOnlyJsArray(uvs)), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
			short[] faces = geometry3d.faces();
			if (faces != null) {
				geometry3d.facesLength = faces.length;
				if (buffers.facesBufferId == null) {
					buffers.facesBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
				gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, JsUtils.createInt16Array(JsArrayUtils.readOnlyJsArray(faces)), WebGLRenderingContext.STATIC_DRAW);
			}
		}
		return buffers;
	}

	public Integer upload(VertexColors vertexColors) {
		if (vertexColors == null) {
			return null;
		}

		Integer bufferId = vertexColorsBuffers.get(vertexColors);
		if ((vertexColors.status & GpuObjectStatus.VERTEX_COLORS_UPLOADED) == 0) {
			vertexColors.status |= GpuObjectStatus.VERTEX_COLORS_UPLOADED;

			float[] colors = vertexColors.getVertexColors();
			if (colors != null) {
				if (bufferId == null) {
					bufferId = gl.createBuffer();
					vertexColorsBuffers.put(vertexColors, bufferId);
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, bufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, JsUtils.createFloat32Array(JsArrayUtils.readOnlyJsArray(colors)), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		return bufferId;
	}


	public void upload(final Renderer3d renderer3d, final Texture texture, final int activeTexture) {
		if ((texture.status & GpuObjectStatus.TEXTURE_UPLOADING) == 0) {
			texture.status |= GpuObjectStatus.TEXTURE_UPLOADING;

			Integer Integer = gl.createTexture();
			textures.put(texture, Integer);

			ImageElement textureImage = resourceLoader.getImage(texture.image);
			if (textureImage == null) {
				Window.alert("Texture image not found in resources: " + texture.image);
			} else {
				textureImages.put(texture, textureImage);

				Event.setEventListener(textureImage, new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						textureLoaded(renderer3d, texture, activeTexture);
					}
				});
				Event.sinkEvents(textureImage, Event.ONLOAD);
			}
		}
	}

	public void upload(final Renderer3d renderer3d, final CubeMapTexture cubeMapTexture, final int activeTexture) {
		if ((cubeMapTexture.status & GpuObjectStatus.TEXTURE_UPLOADING) == 0) {
			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADING;

			Integer texture = gl.createTexture();
			cubeMapTextures.put(cubeMapTexture, texture);

			ImageElement[] textureImages = new ImageElement[6];
			cubeMapImages.put(cubeMapTexture, textureImages);

			for (int i = 0; i < 6; i++) {
				textureImages[i] = resourceLoader.getImage(cubeMapTexture.images[i]);

				Event.setEventListener(textureImages[i], new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						cubeTextureLoaded(renderer3d, cubeMapTexture, activeTexture);
					}
				});
				Event.sinkEvents(textureImages[i], Event.ONLOAD);
			}
		}
	}

	public void textureLoaded(Renderer3d renderer3d, Texture texture, int activeTexture) {
		Integer mapTextureId = textures.get(texture);

		if (renderer3d.activeTexture != activeTexture) {
			gl.activeTexture(activeTexture);
			renderer3d.activeTexture = activeTexture;
		}
		gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, mapTextureId);
		renderer3d.mapTextureId = mapTextureId;

		gl.texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE,
				textureImages.get(texture));
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);

		texture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

		if (gpuUploaderListener != null) {
			gpuUploaderListener.onGpuUploadFinish();
		}
	}

	public void cubeTextureLoaded(Renderer3d renderer3d, CubeMapTexture cubeMapTexture, int activeTexture) {
		cubeMapTexture.loadCount++;
		if (cubeMapTexture.loadCount == 6) {
			Integer envMapTextureId = cubeMapTextures.get(cubeMapTexture);

			if (renderer3d.activeTexture != activeTexture) {
				gl.activeTexture(activeTexture);
				renderer3d.activeTexture = activeTexture;
			}
			gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, envMapTextureId);
			renderer3d.envMapTextureId = envMapTextureId;

			for (int i = 0; i < 6; i++) {
				gl.texImage2D(Program.CUBE_MAP_SIDES[i], 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE,
						cubeMapImages.get(cubeMapTexture)[i]);
			}

			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);

			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

			if (gpuUploaderListener != null) {
				gpuUploaderListener.onGpuUploadFinish();
			}
		}
	}

	public void unload(Object o) {
		if (o instanceof Geometry) {
			GeometryBuffers buffers = geometryBuffers.remove(o);
			if (buffers != null) {
				((Geometry) o).status = 0;
				if (buffers.vertexBufferId != null) {
					gl.deleteBuffer(buffers.vertexBufferId);
				}
				if (buffers.normalsBufferId != null) {
					gl.deleteBuffer(buffers.normalsBufferId);
				}
				if (buffers.uvsBufferId != null) {
					gl.deleteBuffer(buffers.uvsBufferId);
				}
				if (buffers.facesBufferId != null) {
					gl.deleteBuffer(buffers.facesBufferId);
				}
			}
		} else if (o instanceof Texture) {
			Integer texture = textures.remove(o);
			if (texture != null) {
				((Texture) o).status = 0;
				gl.deleteTexture(texture);
			}
		} else if (o instanceof CubeMapTexture) {
			Integer texture = cubeMapTextures.remove(o);
			if (texture != null) {
				((CubeMapTexture) o).status = 0;
				gl.deleteTexture(texture);
			}
		} else if (o instanceof VertexColors) {
			Integer bufferId = vertexColorsBuffers.remove(o);
			if (bufferId != null) {
				gl.deleteBuffer(bufferId);
			}
		}
	}

	public void reset() {
		// Now force re-upload of all objects
		for (Geometry geometry : geometryBuffers.keySet()) {
			geometry.status = 0;
		}
		for (Texture texture : textures.keySet()) {
			texture.status = 0;
		}
		for (CubeMapTexture texture : cubeMapTextures.keySet()) {
			texture.status = 0;
		}
		for (VertexColors vertexColors : vertexColorsBuffers.keySet()) {
			vertexColors.status = 0;
		}
		geometryBuffers.clear();
		vertexColorsBuffers.clear();
		textures.clear();
		cubeMapTextures.clear();
		shaderPrograms.clear();
	}
}