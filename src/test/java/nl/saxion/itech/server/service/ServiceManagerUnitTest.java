package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.thread.ServiceListener;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class ServiceManagerUnitTest {
    private static final int PORT_1 = 6666;
    private static final int PORT_2 = 6667;
    private ServiceManager underTest;

    @BeforeEach
    void setUp() {
        this.underTest = new ServiceManager();
    }

    @Test
    @DisplayName("Add service on free port")
    void addServiceOnFreePort() {
        var serviceListener1 = addService(PORT_1);
        assertNotNull(serviceListener1);
        assertTrue(this.underTest.getRunningService().contains(serviceListener1));
    }

    @Test
    @DisplayName("Add 2 services on the same port")
    void addServicesOnSamePort() {
        var serviceListener1 = addService(PORT_1);
        assertTrue(this.underTest.getRunningService().contains(serviceListener1));
        assertThrows(
                IllegalArgumentException.class,
                () -> addService(PORT_1),
                "Port " + PORT_1 + " already in use."
        );
    }

    @Test
    @DisplayName("Add 2 services on different ports")
    void addServicesOnDifferentPorts() {
        var serviceListener1 = addService(PORT_1);
        assertTrue(this.underTest.getRunningService().contains(serviceListener1));
        var serviceListener2 = addService(PORT_2);
        assertTrue(this.underTest.getRunningService().contains(serviceListener2));
    }

    private ServiceListener addService(int port) {
        try {
            return this.underTest.addService(new MessageService(new DataObject()), port);
        } catch (IOException e) {
            return null;
        }
    }
}