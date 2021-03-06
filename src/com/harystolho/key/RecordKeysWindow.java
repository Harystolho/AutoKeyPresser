package com.harystolho.key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.harystolho.AutoPresserGUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RecordKeysWindow {

	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;

	private AutoPresserGUI gui;

	private Stage window;

	private HashMap<KeyCode, Boolean> lockedKeys;

	private ObservableList<String> pressedKeysList;

	private List<KeyEvent> keyEventList = new ArrayList<>();

	private boolean recording;

	private long lastKeyTime;

	private CheckBox recordDelay;
	private TextField defaultDelay;
	private ListView<String> keysList;
	private Button recordButton;
	private Button saveButton;

	public RecordKeysWindow(AutoPresserGUI gui) {
		this.gui = gui;
	}

	public void display() {
		window = new Stage();
		window.setTitle("Record Key Window");
		window.setWidth(WIDTH);
		window.setHeight(HEIGHT);

		lockedKeys = new HashMap<>();
		pressedKeysList = FXCollections.observableArrayList();
		recording = false;

		lastKeyTime = 0;

		//
		VBox contents = new VBox();
		contents.setAlignment(Pos.TOP_CENTER);

		recordDelay = new CheckBox("Record Delay ?");
		recordDelay.setSelected(true);
		recordDelay.setTranslateY(9);
		defaultDelay = new TextField();
		defaultDelay.setDisable(true);
		defaultDelay.setPromptText("Default delay (ms)");
		defaultDelay.setTranslateY(5);
		defaultDelay.setTranslateX(15);
		keysList = new ListView<>();
		keysList.setItems(pressedKeysList);
		keysList.setMaxWidth(WIDTH * 0.5);
		keysList.setMaxHeight(HEIGHT * 0.7);
		keysList.setTranslateY(20);
		recordButton = new Button("RECORD");
		recordButton.setTranslateY(30);
		saveButton = new Button("Save");
		saveButton.setTranslateY(35);

		HBox delayBox = new HBox();
		delayBox.setAlignment(Pos.TOP_CENTER);
		delayBox.getChildren().addAll(recordDelay, defaultDelay);

		contents.getChildren().addAll(delayBox, keysList, recordButton, saveButton);
		//

		loadEvents();

		Scene scene = new Scene(contents);

		keyMouseListeners(scene);

		window.setScene(scene);
		window.show();
	}

	private void loadEvents() {
		recordDelay.setOnAction((e) -> {
			if (recordDelay.isSelected()) {
				defaultDelay.setDisable(true);
			} else {
				defaultDelay.setDisable(false);
			}
		});

		recordButton.setOnAction((e) -> {
			if (recording) {
				recording = false;
				recordButton.setStyle("");
			} else {
				recording = true;
				recordButton.setStyle("-fx-background-color: red; -fx-border-radius: 4px");
			}
		});

		saveButton.setOnAction((e) -> {
			if (gui.getCurrentProfile() == null) {
				Alert alert = new Alert(AlertType.ERROR, "Please select a profile", ButtonType.OK);
				alert.show();
			} else {
				for (KeyEvent key : keyEventList) {
					gui.getCurrentProfile().addKey(key);
					window.close();
				}
			}
		});

	}

	private void keyMouseListeners(Scene scene) {
		scene.setOnKeyPressed((e) -> {
			if (recording) {
				if (lockedKeys.containsKey(e.getCode())) {
					if (!lockedKeys.get(e.getCode())) {
						lockedKeys.put(e.getCode(), true);
						if (lastKeyTime == 0) {
							KeyEvent key = new KeyEvent(AddKeyWindow.getLetterKeyCode(e.getCode()), 0, true);
							setLastTimeKey();
							keyEventList.add(key);
						} else {
							KeyEvent key = new KeyEvent(AddKeyWindow.getLetterKeyCode(e.getCode()),
									(int) (System.currentTimeMillis() - lastKeyTime), true);
							setLastTimeKey();
							keyEventList.add(key);
						}

						pressedKeysList.add(KeyEvent.getKeyName(AddKeyWindow.getLetterKeyCode(e.getCode())) + " DOWN");
					}
				} else {
					lockedKeys.put(e.getCode(), true);

					if (lastKeyTime == 0) {
						KeyEvent key = new KeyEvent(AddKeyWindow.getLetterKeyCode(e.getCode()), 0, true);
						setLastTimeKey();
						keyEventList.add(key);
					} else {
						KeyEvent key = new KeyEvent(AddKeyWindow.getLetterKeyCode(e.getCode()),
								(int) (System.currentTimeMillis() - lastKeyTime), true);
						setLastTimeKey();
						keyEventList.add(key);
					}

					pressedKeysList.add(KeyEvent.getKeyName(AddKeyWindow.getLetterKeyCode(e.getCode())) + " DOWN");
				}

			}
		});

		scene.setOnKeyReleased((e) -> {
			if (recording) {
				if (lockedKeys.containsKey(e.getCode())) {
					lockedKeys.put(e.getCode(), false);

					try {
						KeyEvent key = new KeyEvent(AddKeyWindow.getLetterKeyCode(e.getCode()),
								(int) (System.currentTimeMillis() - lastKeyTime), false);
						setLastTimeKey();
						keyEventList.add(key);
					} catch (IllegalArgumentException exc) {
						KeyEvent key = new KeyEvent(
								AddKeyWindow
										.getKeyCode(KeyEvent.getKeyName(AddKeyWindow.getLetterKeyCode(e.getCode()))),
								(int) (System.currentTimeMillis() - lastKeyTime), false);
						setLastTimeKey();
						keyEventList.add(key);

					}

					pressedKeysList.add(KeyEvent.getKeyName(AddKeyWindow.getLetterKeyCode(e.getCode())) + " UP");
				}
			}
		});

		scene.setOnMousePressed((e) -> {
			if (lastKeyTime == 0) {
				setLastTimeKey();
			}
			if (recording) {
				KeyEvent key;
				switch (e.getButton()) {
				case PRIMARY:
					pressedKeysList.add(KeyEvent.getKeyName(1024) + " DOWN");
					key = new KeyEvent(1024, (int) (System.currentTimeMillis() - lastKeyTime), true);
					setLastTimeKey();
					keyEventList.add(key);
					break;
				case SECONDARY:
					pressedKeysList.add(KeyEvent.getKeyName(2048) + " DOWN");
					key = new KeyEvent(2048, (int) (System.currentTimeMillis() - lastKeyTime), true);
					setLastTimeKey();
					keyEventList.add(key);
					break;
				case MIDDLE:
					pressedKeysList.add(KeyEvent.getKeyName(4096) + " DOWN");
					key = new KeyEvent(4096, (int) (System.currentTimeMillis() - lastKeyTime), true);
					setLastTimeKey();
					keyEventList.add(key);
					break;
				default:
					break;
				}

			}
		});

		scene.setOnMouseReleased((e) -> {
			if (recording) {
				KeyEvent key;
				switch (e.getButton()) {
				case PRIMARY:
					pressedKeysList.add(KeyEvent.getKeyName(1024) + " UP");
					key = new KeyEvent(1024, (int) (System.currentTimeMillis() - lastKeyTime), true);
					setLastTimeKey();
					keyEventList.add(key);
					break;
				case SECONDARY:
					pressedKeysList.add(KeyEvent.getKeyName(2048) + " UP");
					key = new KeyEvent(2048, (int) (System.currentTimeMillis() - lastKeyTime), true);
					setLastTimeKey();
					keyEventList.add(key);
					break;
				case MIDDLE:
					pressedKeysList.add(KeyEvent.getKeyName(4096) + " UP");
					key = new KeyEvent(4096, (int) (System.currentTimeMillis() - lastKeyTime), true);
					setLastTimeKey();
					keyEventList.add(key);
					break;
				default:
					break;
				}

			}
		});
	}

	public void setLastTimeKey() {
		lastKeyTime = System.currentTimeMillis();
	}

}
