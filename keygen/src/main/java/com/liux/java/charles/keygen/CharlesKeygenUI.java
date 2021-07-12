package com.liux.java.charles.keygen;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CharlesKeygenUI extends JFrame implements DocumentListener {
    private final JTextField textFieldName;
    private final JTextField textFieldKey;

    public CharlesKeygenUI() {
        setSize(310, 120);
        setTitle("Charles Keygen");
        setResizable(false);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getSize().width / 2, screenSize.height / 2 - getSize().height / 2);

        JLabel labelName = new JLabel("Name: ", JLabel.TRAILING);
        labelName.setBounds(10, 10, 50, 30);
        JLabel labelKey = new JLabel("Key: ", JLabel.TRAILING);
        labelKey.setBounds(10, 50, 50, 30);

        textFieldName = new JTextField("Cracked", 20);
        textFieldName.setBounds(60, 10, 240, 30);
        textFieldName.getDocument().addDocumentListener(this);
        labelName.setLabelFor(textFieldName);

        textFieldKey = new JTextField(20);
        textFieldKey.setEditable(false);
        textFieldKey.setSize(300, 40);
        textFieldKey.setBounds(60, 50, 240, 30);
        int fontSize = textFieldKey.getFont().getSize();
        textFieldKey.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        labelKey.setLabelFor(textFieldKey);

        setLayout(null);
        Container contentPane = getContentPane();
        contentPane.add(labelName);
        contentPane.add(textFieldName);
        contentPane.add(labelKey);
        contentPane.add(textFieldKey);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        updateKey();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateKey();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateKey();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateKey();
    }

    private void updateKey() {
        String text = textFieldName.getText();
        if (text.isEmpty()) {
            textFieldKey.setText("");
        } else {
            textFieldKey.setText(CharlesKeygen.keygen(text));
        }
    }

    public static void main(String[] arguments) {
        if (arguments.length > 0) {
            CharlesKeygen.main(arguments);
        } else {
            new CharlesKeygenUI().setVisible(true);
        }
    }

}