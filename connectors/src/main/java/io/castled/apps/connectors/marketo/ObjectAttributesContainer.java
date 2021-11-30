package io.castled.apps.connectors.marketo;

import com.google.common.collect.Maps;
import io.castled.apps.connectors.marketo.dtos.GenericAttribute;
import io.castled.apps.connectors.marketo.dtos.GenericAttributesWrapper;
import io.castled.commons.models.AppSyncMode;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ObjectAttributesContainer {

    private final List<GenericAttribute> attributes;
    // API name of dedupe keys
    private final List<String> dedupeKeys;
    private final String idKey;
    private final MarketoObject object;

    public List<GenericAttribute> getAttributes(AppSyncMode syncMode) {
        if (syncMode == AppSyncMode.UPDATE) {
            return this.attributes;
        } else {
            // Id key not relevant for upsert or insert
            Predicate<GenericAttribute> isValidAttr = (attr) -> {
                return (object == MarketoObject.LEADS && !attr.getRest().getName().equals(this.idKey)) ||
                        (object != MarketoObject.LEADS && !attr.getName().equals(this.idKey));
            };
            return attributes.stream().filter(isValidAttr).collect(Collectors.toList());
        }
    }

    public List<String> getPkEligibles(AppSyncMode syncMode) {
        // Maps api name -> display name
        Map<String, String> nameMap = Maps.newHashMap();
        for (GenericAttribute attr : attributes) {
            if (object == MarketoObject.LEADS) {
                nameMap.put(attr.getRest().getName(), attr.getDisplayName());
            } else {
                nameMap.put(attr.getName(), attr.getDisplayName());
            }
        }
        List<String> dedupeKeyDisplayNames = dedupeKeys.stream().map(nameMap::get).collect(Collectors.toList());
        String idDisplayName = nameMap.get(idKey);

        // Id key is app generated, so makes sense only during update
        if (syncMode == AppSyncMode.UPDATE) {
            dedupeKeyDisplayNames.add(idDisplayName);
        }
        return dedupeKeyDisplayNames;
    }

    // Gets the field names of dedupe attrs
    public Map<String, String> getDedupeAttrFieldMap() {
        // attr display name -> field name
        Map<String, String> attrFieldMap = Maps.newHashMap();

        attrFieldMap.put(idKey, GenericAttributesWrapper.ID_FIELD);
        dedupeKeys.stream().forEach(dedupeKey -> attrFieldMap.put(dedupeKey, GenericAttributesWrapper.DEDUPE_FIELD));
        return attrFieldMap;
    }
}
