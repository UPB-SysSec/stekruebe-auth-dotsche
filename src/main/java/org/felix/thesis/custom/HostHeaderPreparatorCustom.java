package org.felix.thesis.custom;

import de.rub.nds.tlsattacker.core.http.header.HostHeader;
import de.rub.nds.tlsattacker.core.http.header.preparator.HostHeaderPreparator;
import de.rub.nds.tlsattacker.core.workflow.chooser.Chooser;

public class HostHeaderPreparatorCustom extends HostHeaderPreparator {
    public HostHeaderPreparatorCustom(Chooser chooser, HostHeader header) {
        super(chooser, header);
    }

    public void prepare() {
        //yeet
    }
}
