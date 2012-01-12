package com.vaadin.graph;

import com.vaadin.Application;
import com.vaadin.ui.*;

public class GraphExplorerApplication extends Application {
	@Override
	public void init() {
		Window mainWindow = new Window("GraphExplorerApplication");
		Label label = new Label("Hello Vaadin user");
		mainWindow.addComponent(label);
		setMainWindow(mainWindow);
	}

}
