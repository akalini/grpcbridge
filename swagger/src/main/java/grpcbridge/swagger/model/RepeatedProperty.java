package grpcbridge.swagger.model;

class RepeatedProperty extends Property {
    private final Property items;

    RepeatedProperty(Property items) {
        super(Type.ARRAY, null, null);
        this.items = items;
    }
}
