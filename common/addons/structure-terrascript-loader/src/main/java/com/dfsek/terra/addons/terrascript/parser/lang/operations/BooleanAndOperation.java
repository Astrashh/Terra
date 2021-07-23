package com.dfsek.terra.addons.terrascript.parser.lang.operations;

import com.dfsek.terra.addons.terrascript.parser.lang.Returnable;
import com.dfsek.terra.addons.terrascript.tokenizer.Position;

public class BooleanAndOperation extends BinaryOperation<Boolean, Boolean> {
    public BooleanAndOperation(Returnable<Boolean> left, Returnable<Boolean> right, Position start) {
        super(left, right, start);
    }

    @Override
    public Boolean apply(Boolean left, Boolean right) {
        return left && right;
    }

    @Override
    public ReturnType returnType() {
        return ReturnType.BOOLEAN;
    }
}