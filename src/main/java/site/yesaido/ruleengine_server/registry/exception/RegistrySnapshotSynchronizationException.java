package site.yesaido.ruleengine_server.registry.exception;

public class RegistrySnapshotSynchronizationException extends RuntimeException {

    public RegistrySnapshotSynchronizationException(String message) {
        super(message);
    }

    public RegistrySnapshotSynchronizationException(String message, Throwable cause) {
        super(message, cause);
    }
}