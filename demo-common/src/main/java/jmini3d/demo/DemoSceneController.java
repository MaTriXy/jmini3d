package jmini3d.demo;

import java.util.HashMap;

import jmini3d.Scene;
import jmini3d.SceneController;
import jmini3d.Vector3;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class DemoSceneController implements SceneController, TouchListener {
	float cameraAngle;
	long initialTime;

	int sceneIndex = 0;
	Scene scenes[] = {new TeapotScene(), new CubeScene(), new EnvMapCubeScene(), new CubesScene(), new NormalMapScene()};
	int cameraModes[] = {0, 0, 0, 0, 1};

	public DemoSceneController() {
		initialTime = System.currentTimeMillis();
	}

	@Override
	public Scene getScene(int width, int height) {
		scenes[sceneIndex].setViewPort(width, height);

		// Rotate camera...
		cameraAngle = 0.0005f * (System.currentTimeMillis() - initialTime);

		float d = 5;
		Vector3 target = scenes[sceneIndex].getCamera().getTarget();
		scenes[sceneIndex].getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
				(float) (target.y - d * Math.sin(cameraAngle)), //
				target.z + (cameraModes[sceneIndex] == 0 ? (float) (d * Math.sin(cameraAngle)) : d)//
		);

		return scenes[sceneIndex];
	}

	@Override
	public boolean onTouch(HashMap<Integer, TouchPointer> pointers) {
		for (Integer key : pointers.keySet()) {
			TouchPointer pointer = pointers.get(key);
			if (pointer.status == TouchPointer.TOUCH_DOWN) {
				if (pointer.x > scenes[sceneIndex].getCamera().getWidth() / 2) {
					if (sceneIndex >= scenes.length - 1) {
						sceneIndex = 0;
					} else {
						sceneIndex++;
					}
				} else {
					if (sceneIndex <= 0) {
						sceneIndex = scenes.length - 1;
					} else {
						sceneIndex--;
					}
				}
			}
		}
		return true;
	}
}
