package com.elvarg.game.model.areas.impl;

import java.util.Arrays;

import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.impl.PrivateArea;

public class ZulrahArea extends PrivateArea {

    public static final Boundary BOUNDARY = new Boundary(2900, 3250, 3024, 3472);

    public ZulrahArea() {
        super(Arrays.asList(BOUNDARY));
    }
}