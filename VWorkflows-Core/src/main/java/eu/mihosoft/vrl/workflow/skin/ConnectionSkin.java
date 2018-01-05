/*
 * Copyright 2012-2016 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * Please cite the following publication(s):
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vrl.workflow.skin;

import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import javafx.beans.property.ObjectProperty;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Connection skin.
 *
 * @param <T> Connection model type
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public interface ConnectionSkin<T extends Connection> extends Skin<Connection> {
    /**
     * Returns the sender connector.
     *
     * @return sender connector
     */
    Connector getSender();

    /**
     * Defines the sender connector.
     *
     * @param c sender connector to set
     */
    void setSender(Connector c);

    /**
     * Returns the sender property.
     *
     * @return sender property
     */
    ObjectProperty<Connector> senderProperty();

    /**
     * Returns the receiver connector.
     *
     * @return receiver connector
     */
    Connector getReceiver();

    /**
     * Defines the receiver connector.
     *
     * @param c receiver connector to set
     */
    void setReceiver(Connector c);

    /**
     * Returns the receiver property.
     *
     * @return receiver property
     */
    ObjectProperty<Connector> receiverProperty();

    /**
     * Moves the receiverUI element to the front.
     */
    void receiverToFront();

    void addBreakpoint(double x,double y);
    ArrayList<Circle> getBreakpoints();

    void addPoints(ArrayList<Pair> pointList);

    ArrayList<Pair> getPoints();
}
