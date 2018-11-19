package io.confluent.kgrafa.model;

public class TextValue {
    String text = "text";
    int value = 1;

    public TextValue() {

    }

    public TextValue(String text) {
        setText(text.replace("_", "-"));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
