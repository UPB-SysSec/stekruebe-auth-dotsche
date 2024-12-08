package org.felix.thesis.custom;

import de.rub.nds.tlsattacker.core.http.header.HostHeader;
import de.rub.nds.tlsattacker.core.http.header.preparator.HostHeaderPreparator;
import de.rub.nds.tlsattacker.core.layer.context.HttpContext;

public class HostHeaderCustom extends HostHeader {
    public HostHeaderCustom(String hostname) {
        super();
        this.setHeaderName("Host");
        this.setHeaderValue(hostname);
    }
    public HostHeaderPreparator getPreparator(HttpContext httpContext) {
        return new HostHeaderPreparatorCustom(httpContext.getChooser(), this);
    }
}
