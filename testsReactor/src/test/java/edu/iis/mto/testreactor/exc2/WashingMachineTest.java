package edu.iis.mto.testreactor.exc2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class WashingMachineTest {

    private WashingMachine washingMachine;
    private LaundryStatus laundryStatus;
    private ProgramConfiguration programConfiguration;
    private LaundryBatch laundryBatch;

    private DirtDetector dirtDetectorMock;

    private Engine engineMock;

    private WaterPump waterPumpMock;

    @Before
    public void setUp() {
        dirtDetectorMock = mock(DirtDetector.class);
        engineMock = mock(Engine.class);
        waterPumpMock = mock(WaterPump.class);
        washingMachine = new WashingMachine(dirtDetectorMock, engineMock, waterPumpMock);
    }

    @Test
    public void itCompiles() {
        assertThat(true, Matchers.equalTo(true));
    }

    @Test
    public void shouldSaidThatLaundryBatchIsTooHeavy() {

        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(WashingMachine.MAX_WEIGTH_KG + 1)
                                   .withType(Material.COTTON)
                                   .build();
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.AUTODETECT)
                                                   .withSpin(true)
                                                   .build();
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.FAILURE)));
        assertThat(laundryStatus.getErrorCode(), is(equalTo(ErrorCode.TOO_HEAVY)));
    }
}
