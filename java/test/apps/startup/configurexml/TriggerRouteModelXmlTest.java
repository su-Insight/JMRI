package apps.startup.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TriggerRouteModelXmlTest.java
 *
 * Test for the TriggerRouteModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TriggerRouteModelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TriggerRouteModelXml constructor",new TriggerRouteModelXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

