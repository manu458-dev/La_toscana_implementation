package com.latoscana.branchnode.financial.domain.events;

import com.latoscana.branchnode.financial.domain.Ticket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SaleClosedEvent extends ApplicationEvent {
    private final Ticket ticket;

    public SaleClosedEvent(Object source, Ticket ticket) {
        super(source);
        this.ticket = ticket;
    }
}
