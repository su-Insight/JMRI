package jmri.jmrit.logixng.logixemulator.implementation;

import java.util.Arrays;
import java.util.Set;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.logixng.LogixEmulatorActionManager;
import jmri.jmrit.logixng.logixemulator.actions.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the usual default implementations for the
 * {@link jmri.InstanceManager}.
 * <P>
 * Not all {@link jmri.InstanceManager} related classes are provided by this
 * class. See the discussion in {@link jmri.InstanceManager} of initialization
 * methods.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2014
 * @since 2.9.4
 */
@ServiceProvider(service = InstanceInitializer.class)
public class LogixNG_LogixEmulatorInstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) {
        
        // In order for getDefault() to be called for a particular manager,
        // the manager also needs to be added to the method getInitalizes()
        // below.
        
        InternalSystemConnectionMemo memo =
                InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        
        if (type == LogixEmulatorActionManager.class) {
            return new DefaultLogixEmulatorActionManager(memo);
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(
                LogixEmulatorActionManager.class
        ));
        return set;
    }

}
