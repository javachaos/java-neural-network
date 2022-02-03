/******************************************************************************
* Copyright (c) 2014 Fred .
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the GNU Public License v3.0
* which accompanies this distribution, and is available at
* http://www.gnu.org/licenses/gpl.html
*
* Contributors:
*     Fred  - initial API and implementation
******************************************************************************/
package com.neuralnetwork.shared.exceptions;

import java.io.Serial;

public class NeuronLinkException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8321102153473257324L;

    public NeuronLinkException(final String msg) {
        super(msg);
    }

}
