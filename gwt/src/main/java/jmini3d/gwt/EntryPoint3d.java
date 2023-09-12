package jmini3d.gwt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;

public class EntryPoint3d implements EntryPoint {

	public Canvas3d canvas3d;
	public String resourceDir = "./resources/";
	public String shaderDir = "./shaders/";

	public void onModuleLoad() {
		canvas3d = new Canvas3d(resourceDir, shaderDir);
		canvas3d.setScale(canvas3d.getDevicePixelRatio());
		canvas3d.setSize(Window.getClientWidth(), Window.getClientHeight());
		Window.addResizeHandler(event -> canvas3d.setSize(event.getWidth(), event.getHeight()));

		onCreateSetContentView();

		canvas3d.onResume();
	}

	protected void onCreateSetContentView() {
		appendToBody(canvas3d.getElement());
	}

	public static native void appendToBody(Element element) /*-{
		$doc.body.appendChild(element);
	}-*/;
}
