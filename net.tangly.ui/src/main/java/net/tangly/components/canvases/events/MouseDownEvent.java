package net.tangly.components.canvases.events;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import net.tangly.components.canvases.Canvas;

@DomEvent("mousedown")
public class MouseDownEvent extends MouseEvent {
    public MouseDownEvent(Canvas source, boolean fromClient, @EventData("event.offsetX") int offsetX, @EventData("event.offsetY") int offsetY,
                          @EventData("event.button") int button) {
        super(source, fromClient, offsetX, offsetY, button);
    }
}
