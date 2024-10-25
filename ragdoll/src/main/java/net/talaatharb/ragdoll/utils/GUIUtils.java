package net.talaatharb.ragdoll.utils;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIUtils {

	public static final void setAnchorAll(Node node, double padding) {
		AnchorPane.setBottomAnchor(node, padding);
		AnchorPane.setLeftAnchor(node, padding);
		AnchorPane.setRightAnchor(node, padding);
		AnchorPane.setTopAnchor(node, padding);
	}
	
	public static final void setAnchorNoPadding(Node node) {
		setAnchorAll(node, 0.0);
	}
}
