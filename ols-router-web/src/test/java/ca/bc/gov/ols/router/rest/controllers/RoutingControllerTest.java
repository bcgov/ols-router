package ca.bc.gov.ols.router.rest.controllers;

import ca.bc.gov.ols.router.Router;
import ca.bc.gov.ols.router.RouterFactory;
import ca.bc.gov.ols.router.api.RouterDistanceBetweenPairsResponse;
import ca.bc.gov.ols.router.api.RoutingParameters;
import ca.bc.gov.ols.router.data.enums.DistanceUnit;
import ca.bc.gov.ols.router.data.enums.RoutingCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingControllerTest {
    private Router router;

    @Mock
    BindingResult res;

    @InjectMocks
    private RoutingController ctrlr;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        RouterFactory factory = new RouterFactory();
        factory.setUnitTestMode("TRUE");
        router = factory.getRouter();
        setPrivateField(ctrlr, "router", router);
    }

    @Tag("Prod")
    @Test
    void pingTest() {
        ResponseEntity<String> resp = ctrlr.ping();
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Tag("Prod")
    @Test
    void betweenPairsTest() {
        RoutingParameters params = new RoutingParameters();
        double fromPts[] = new double[] {100, 100};
        double toPts[] = new double[] {200, 200, 1000, 1000};
        params.setToPoints(toPts);
        params.setFromPoints(fromPts);
        params.setOutputSRS(3005);
        params.setCriteria(RoutingCriteria.SHORTEST);
        params.setDistanceUnit(DistanceUnit.KILOMETRE);
        params.setCorrectSide(false);
        params.setDisable("sc,tf,ev,td");
        params.setRouteDescription("Routing_results");
        RouterDistanceBetweenPairsResponse resp = ctrlr.distanceBetweenPairsGet(params, res);
        assertEquals(resp.getDistanceStr(0), "0.141");
        assertEquals(resp.getDistanceStr(1), "-1");
        assertEquals(resp.getError(0), null);
        assertEquals(resp.getError(1), "No Route Found.");
    }

    public static void setPrivateField(Object target, String fieldName, Object value){
        try {
            Field privateField = target.getClass().getDeclaredField(fieldName);
            privateField.setAccessible(true);
            privateField.set(target, value);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
