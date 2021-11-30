package io.castled.forms.dtos;


import io.castled.forms.FormFieldType;

public class JsonFileProps extends DisplayFieldProps {

    public JsonFileProps(String title, String description) {
        super(FormFieldType.JSON_FILE, title, description);
    }
}
