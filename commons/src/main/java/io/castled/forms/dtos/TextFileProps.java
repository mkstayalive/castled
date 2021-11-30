package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;

public class TextFileProps extends DisplayFieldProps {

    public TextFileProps(String title, String description) {
        super(FormFieldType.TEXT_FILE, title, description);
    }
}
