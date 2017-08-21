package jmri.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Invokes complete set of tests in the jmri.swing tree
 *
 * @author	Bob Jacobsen Copyright 2014
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    EditableListTest.class,
    JmriJTablePersistenceManagerTest.class,
    ConnectionLabelTest.class,
    AboutDialogTest.class,
    DefaultEditableListModelTest.class,
    RowSorterUtilTest.class,
    JTitledSeparatorTest.class,
    DefaultCellEditorTest.class
})
public class PackageTest {
}
