package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RadioGroupProps extends MultiOptionProps {

    public RadioGroupProps(List<FormFieldOption> selectOptions, String title, String description) {
        super(FormFieldType.RADIO_GROUP, selectOptions, title, description);
    }

    public RadioGroupProps(String optionsReference, String title, String description) {
        super(FormFieldType.RADIO_GROUP, optionsReference, title, description);

    }
}
