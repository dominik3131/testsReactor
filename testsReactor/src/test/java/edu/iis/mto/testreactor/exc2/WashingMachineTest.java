package edu.iis.mto.testreactor.exc2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(WashingMachine.MAX_WEIGTH_KG)
                                   .withType(Material.COTTON)
                                   .build();
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.MEDIUM)
                                                   .withSpin(false)
                                                   .build();
    }

    @Test
    public void itCompiles() {
        assertThat(true, Matchers.equalTo(true));
    }

    @Test
    public void shouldSayThatLaundryBatchIsTooHeavy() {
        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(WashingMachine.MAX_WEIGTH_KG + 1)
                                   .withType(Material.COTTON)
                                   .build();

        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.FAILURE)));
        assertThat(laundryStatus.getErrorCode(), is(equalTo(ErrorCode.TOO_HEAVY)));
    }

    @Test
    public void shouldSayThatLaundryIsFinishedAndThatProperProgramWasUsed() {
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.SUCCESS)));
        assertThat(laundryStatus.getRunnedProgram(), is(equalTo(programConfiguration.getProgram())));
    }

    @Test
    public void shouldSayThatEngineSpinIsNotUsedWithDelicateMaterialAndSpinOptionSetToTrue() {
        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(WashingMachine.MAX_WEIGTH_KG)
                                   .withType(Material.DELICATE)
                                   .build();
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.MEDIUM)
                                                   .withSpin(true)
                                                   .build();
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.SUCCESS)));
        verify(engineMock, never()).spin();
    }

    @Test
    public void shouldUseLongProgramWhenDirtIsMoreThan_AVERAGE_DEGREE_AndAutoDetectIsUsed() {
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.AUTODETECT)
                                                   .withSpin(false)
                                                   .build();
        when(dirtDetectorMock.detectDirtDegree(laundryBatch)).thenReturn(
                new Percentage(WashingMachine.AVERAGE_DEGREE.getDoubleValue() + 1));
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.SUCCESS)));
        assertThat(laundryStatus.getRunnedProgram(), is(equalTo(Program.LONG)));
    }

    @Test
    public void shouldUseMediumProgramWhenDirtIsLowerThan_AVERAGE_DEGREE_AndAutoDetectIsUsed() {
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.AUTODETECT)
                                                   .withSpin(false)
                                                   .build();
        when(dirtDetectorMock.detectDirtDegree(laundryBatch)).thenReturn(
                new Percentage(WashingMachine.AVERAGE_DEGREE.getDoubleValue() - 1));
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.SUCCESS)));
        assertThat(laundryStatus.getRunnedProgram(), is(equalTo(Program.MEDIUM)));
    }

    @Test
    public void engineShouldRunForProperAmountOfTime() {
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.SUCCESS)));
        verify(engineMock, times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
    }

    @Test
    public void waterPumpPourAndReleaseMethodsShouldBeCalledOnce() {
        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertThat(laundryStatus.getResult(), is(equalTo(Result.SUCCESS)));
        verify(waterPumpMock, times(1)).pour(laundryBatch.getWeightKg());
        verify(waterPumpMock, times(1)).release();
    }
}
