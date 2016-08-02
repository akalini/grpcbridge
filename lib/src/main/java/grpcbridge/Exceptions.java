package grpcbridge;

/**
 * Exceptions thrown by the {@link Bridge}.
 */
public final class Exceptions {
    /**
     * Base class for all the exceptions thrown by the {@link Bridge}.
     */
    public static class BridgeException extends RuntimeException {
        public BridgeException(String message) {
            super(message);
        }

        public BridgeException(String message, Throwable nested) {
            super(message, nested);
        }
    }

    /**
     * Thrown if {@link Bridge} mis configuration is detected.
     */
    public final static class ConfigurationException extends BridgeException {
        public ConfigurationException(String message) {
            super(message);
        }
    }

    /**
     * Thrown if no route matches the supplied HTTP request.
     */
    public final static class RouteNotFoundException extends BridgeException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Thrown if a protobuf or HTTP message parsing fails.
     */
    public final static class ParsingException extends BridgeException {
        public ParsingException(String message, Throwable nested) {
            super(message, nested);
        }
    }
}
