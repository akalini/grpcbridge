package grpcbridge.swagger.model;

import grpcbridge.swagger.model.Property.Type;

class RepeatedQueryParameter extends Parameter {
    private final CollectionFormat collectionFormat;
    private final SimpleProperty items;

    private RepeatedQueryParameter(
        String name,
        SimpleProperty items,
        CollectionFormat collectionFormat
    ) {
        super(Type.ARRAY, null, name, Location.QUERY, false);
        this.items = items;
        this.collectionFormat = collectionFormat;
    }

    static RepeatedQueryParameter create(String name, SimpleProperty nestedType) {
        return new RepeatedQueryParameter(name, nestedType, CollectionFormat.MULTI);
    }

    enum CollectionFormat { CSV, SSV, TSV, PIPES, MULTI }
}
