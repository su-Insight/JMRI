package jmri.jmrit.logixng.string.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.string.expressions.Formula;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class FormulaXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public FormulaXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Formula p = (Formula) o;

        Element element = new Element("formula");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

//        NamedBeanHandle memory = p.getMemory();
//        if (memory != null) {
//            element.addContent(new Element("memory").addContent(memory.getName()));
//        }
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        Formula h;
        h = new Formula(sys, uname);

        loadCommon(h, shared);

//        Element memoryName = shared.getChild("memory");
//        if (memoryName != null) {
//            Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName.getTextTrim());
//           if (m != null) h.setMemory(m);
//            else h.removeMemory();
//        }

        // this.checkedNamedBeanReference()
        // <T extends NamedBean> T checkedNamedBeanReference(String name, @Nonnull T type, @Nonnull Manager<T> m) {

        InstanceManager.getDefault(StringExpressionManager.class).registerExpression(h);
        return true;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionMemoryXml.class);
}
