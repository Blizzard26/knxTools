package org.knx.xml;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class BaseClass  {
	
	@XmlTransient
    protected BaseClass parent;

    public BaseClass getParent(){
        return parent;
    }
    
    public void setParent(BaseClass parent)
    {
    	this.parent = parent;
    }
}
