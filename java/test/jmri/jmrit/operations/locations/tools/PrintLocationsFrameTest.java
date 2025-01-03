package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintLocationsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintLocationsAction t = new PrintLocationsAction(true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        JUnitOperationsUtil.createSchedules(); // increase coverage by adding a schedule
        Location location = JUnitOperationsUtil.createOneNormalLocation("TEST_LOCATION"); // increase coverage
        Track interchange1 = location.getTrackByName("TEST_LOCATION Interchange 1", null);
        Assert.assertNotNull("confirm track exists", interchange1);
        Track interchange2 = location.getTrackByName("TEST_LOCATION Interchange 2", null);
        Assert.assertNotNull("confirm track exists", interchange2);
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        
        interchange1.setDropOption(Track.TRAINS);
        interchange1.addDropId(train.getId());
        interchange1.setPickupOption(Track.EXCLUDE_TRAINS);
        interchange1.addPickupId(train.getId());
        
        interchange1.setRoadOption(Track.INCLUDE_ROADS);
        interchange1.addRoadName("SP");
        
        interchange1.setLoadOption(Track.INCLUDE_LOADS);
        interchange1.addLoadName("Bolts");
        
        interchange1.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        interchange1.addDestination(location);
        
        interchange1.setServiceOrder(Track.LIFO);
        
        interchange2.setDropOption(Track.ROUTES);
        interchange2.addDropId(train.getRoute().getId());
        interchange2.setPickupOption(Track.EXCLUDE_ROUTES);
        interchange2.addPickupId(train.getRoute().getId());
        
        interchange2.setRoadOption(Track.EXCLUDE_ROADS);
        interchange2.addRoadName("SP");
        
        interchange2.setLoadOption(Track.EXCLUDE_LOADS);
        interchange2.addLoadName("Bolts");
        
        interchange2.setDestinationOption(Track.EXCLUDE_DESTINATIONS);
        interchange2.addDestination(location);
        
        interchange2.setServiceOrder(Track.FIFO);
        
        // staging options
        Location staging = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Track stagingTrack = staging.getTrackByName("North End 1", null);
        Assert.assertNotNull("exists", stagingTrack);
        
        stagingTrack.setAddCustomLoadsAnySpurEnabled(true);
        stagingTrack.setAddCustomLoadsAnyStagingTrackEnabled(true);
        stagingTrack.setAddCustomLoadsEnabled(true);
        stagingTrack.setBlockCarsEnabled(true);
        stagingTrack.setRemoveCustomLoadsEnabled(true);    
        stagingTrack.setLoadSwapEnabled(true);
        stagingTrack.setLoadEmptyEnabled(true);
        
        stagingTrack.setShipLoadOption(Track.EXCLUDE_LOADS);
        stagingTrack.addShipLoadName("Screws");

        PrintLocationsFrame plf = new PrintLocationsFrame(true, null);
        Assert.assertNotNull("exists", plf);

        // select all options
        plf.printLocations.setSelected(true);
        plf.printSchedules.setSelected(true);
        plf.printComments.setSelected(true);
        plf.printDetails.setSelected(true);
        plf.printAnalysis.setSelected(true);
        plf.printErrorAnalysis.setSelected(true);

        JemmyUtil.enterClickAndLeave(plf.okayButton); // closes window
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + "Locations");
        
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
    }

    @Test
    public void testPrintOptionsFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        PrintLocationsFrame f = new PrintLocationsFrame(true, null);
        Assert.assertNotNull("exists", f);
        JemmyUtil.enterClickAndLeave(f.okayButton); // closes window

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + Bundle.getMessage("TitleLocationsTable"));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(f);
        JUnitUtil.dispose(printPreviewFrame);
    }
}
