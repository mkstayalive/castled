package io.castled.apps.connectors.salesforce.client;

public enum SFDCSoapType {
    STRING("string"),
    DOUBLE("double"),
    DATETIME("dateTime"),
    DATE("date"),
    TIME("time"),
    BOOLEAN("boolean"),
    ID("ID"),
    INTEGER("int");

    private final String name;

    SFDCSoapType(String name) {
        this.name = name;
    }

    public static SFDCSoapType fromName(String name) {
        for (SFDCSoapType sfdcSoapType : SFDCSoapType.values()) {
            if (sfdcSoapType.name.equals(name)) {
                return sfdcSoapType;
            }
        }
        return null;
    }

}
