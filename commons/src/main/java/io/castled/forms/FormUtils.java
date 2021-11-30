package io.castled.forms;

import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.forms.dtos.*;
import io.castled.utils.FileUtils;
import io.castled.utils.ReflectionUtils;
import io.castled.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FormUtils {

    public static FormFieldsDTO getFormFields(Class<?> configClass) {

        Map<String, FormFieldDTO> formFields = getFormFields(Arrays.stream(FieldUtils.getAllFields(configClass)).collect(Collectors.toList()));

        Map<String, GroupActivatorDTO> groupActivators = Maps.newHashMap();
        for (GroupActivator groupActivator : ReflectionUtils.getAnnotationsFromType(configClass, GroupActivator.class)) {
            if (!groupActivators.containsKey(groupActivator.group())) {
                groupActivators.put(groupActivator.group(), new GroupActivatorDTO(StringUtils.nullIfEmpty(groupActivator.condition()),
                        Arrays.asList(groupActivator.dependencies().clone())));
            }
        }
        CodeBlock codeBlock = ReflectionUtils.getAnnotation(configClass, CodeBlock.class);
        return new FormFieldsDTO(formFields, toCodeBlockDTO(codeBlock), groupActivators);
    }

    private static CodeBlockDTO toCodeBlockDTO(CodeBlock codeBlock) {
        if (codeBlock == null) {
            return null;
        }
        List<CodeSnippetDTO> codeSnippets = Arrays.stream(codeBlock.snippets())
                .map(codeSnippet -> {
                    try {
                        return new CodeSnippetDTO(codeSnippet.title(),
                                FileUtils.getResourceFileAsString("code_snippets/" + codeSnippet.ref()));
                    } catch (Exception e) {
                        throw new CastledRuntimeException(e);
                    }
                }).collect(Collectors.toList());

        return new CodeBlockDTO(codeBlock.title(), Arrays.asList(codeBlock.dependencies()), codeSnippets);
    }

    private static Map<String, FormFieldDTO> getFormFields(List<Field> fields) {
        Map<String, FormFieldDTO> formFields = Maps.newLinkedHashMap();
        for (Field field : fields) {
            FormFieldDTO formFieldDTO = getFieldSchema(field);
            if (formFieldDTO != null) {
                formFields.put(field.getName(), formFieldDTO);
            }
        }
        return formFields;
    }

    private static FormFieldDTO getFieldSchema(Field field) {
        FormField formField = field.getAnnotation(FormField.class);
        if (formField == null) {
            return null;
        }

        return FormFieldDTO.builder()
                .validations(getFieldValidations(field)).fieldProps(getFieldMeta(formField))
                .group(formField.group()).build();
    }

    private static FieldValidations getFieldValidations(Field field) {
        FormField formField = field.getAnnotation(FormField.class);
        return new FieldValidations(formField.required());
    }

    private static FormFieldProps getFieldMeta(FormField formField) {
        switch (formField.type()) {
            case TEXT_BOX:
                return new TextBoxProps(formField.placeholder(), formField.title(),
                        formField.description(), formField.optionsRef().value());
            case DROP_DOWN:
                return getDropDownTypeFields(formField);
            case RADIO_GROUP:
                return getRadioGroupTypeFields(formField);
            case MAPPING:
                return new MappingProps();
            case CHECK_BOX:
                return new CheckBoxProps(formField.title(), formField.description());
            case HIDDEN:
                return new HiddenProps(formField.optionsRef().value());
            case JSON_FILE:
                return new JsonFileProps(formField.title(), formField.description());
            case TEXT_FILE:
                return new TextFileProps(formField.title(), formField.description());
            default:
                return null;
        }
    }

    private static DropDownProps getDropDownTypeFields(FormField formField) {
        OptionsRef optionsRef = formField.optionsRef();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(optionsRef.value())) {
            if (optionsRef.type() == OptionsRefType.STATIC) {
                List<FormFieldOption> formFieldOptions = ObjectRegistry.getInstance(StaticOptionsFetcherFactory.class)
                        .getOptions(optionsRef.value());
                return new DropDownProps(formFieldOptions, StringUtils.nullIfEmpty(formField.title()),
                        StringUtils.nullIfEmpty(formField.description()));

            }
            return new DropDownProps(optionsRef.value(), StringUtils.nullIfEmpty(formField.title()),
                    StringUtils.nullIfEmpty(formField.description()));
        }
        return null;
    }

    private static RadioGroupProps getRadioGroupTypeFields(FormField formField) {
        OptionsRef optionsRef = formField.optionsRef();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(optionsRef.value())) {
            if (optionsRef.type() == OptionsRefType.STATIC) {
                List<FormFieldOption> formFieldOptions = ObjectRegistry.getInstance(StaticOptionsFetcherFactory.class)
                        .getOptions(optionsRef.value());
                return new RadioGroupProps(formFieldOptions, StringUtils.nullIfEmpty(formField.title()),
                        StringUtils.nullIfEmpty(formField.description()));
            }
            return new RadioGroupProps(optionsRef.value(), StringUtils.nullIfEmpty(formField.title()), StringUtils.nullIfEmpty(formField.description()));
        }
        return null;
    }

    private static Object formatSelectOption(String allowedValue, FormFieldSchema fieldSchema) {
        try {
            if (fieldSchema == FormFieldSchema.NUMBER) {
                return NumberFormat.getInstance().parse(allowedValue);
            }
            return allowedValue;
        } catch (ParseException e) {
            log.error(String.format("Failed to parse value %s for schema %s", allowedValue, fieldSchema));
            throw new CastledRuntimeException(e);
        }
    }

}
