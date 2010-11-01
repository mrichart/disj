/*******************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     DisJ Development Group
 *******************************************************************************/

package distributed.plugin.core;

/**
 * @author Me
 * 
 * A parent exception of this project
 */
public class DisJException extends Exception {

    public static final String[] ERROR_CODE;

    static final long serialVersionUID = IConstants.SERIALIZE_VERSION;

    static {
        ERROR_CODE = new String[30];
        ERROR_CODE[IConstants.ERROR_0] = "Node not found";
        ERROR_CODE[IConstants.ERROR_1] = "Edge not found";
        ERROR_CODE[IConstants.ERROR_2] = "Edge already existed";
        ERROR_CODE[IConstants.ERROR_3] = "Node already existed";
        ERROR_CODE[IConstants.ERROR_4] = "Graph already existed";
        ERROR_CODE[IConstants.ERROR_5] = "Graph not found";
        ERROR_CODE[IConstants.ERROR_6] = "Entity already assigned";
        ERROR_CODE[IConstants.ERROR_7] = "Cannot invoke DisJ operation when entity has not yet been created";
        ERROR_CODE[IConstants.ERROR_8] = "Cannot create user Object";
        ERROR_CODE[IConstants.ERROR_9] = "Node not found for this edge";
        ERROR_CODE[IConstants.ERROR_10] = "Start and End node cannot be the same";
        ERROR_CODE[IConstants.ERROR_11] = "Logger could not find the log owner";
        ERROR_CODE[IConstants.ERROR_12] = "Illegal alarm time value";
        ERROR_CODE[IConstants.ERROR_13] = "There is no event in a queue";
        ERROR_CODE[IConstants.ERROR_14] = "Port has not been blocked";

        ERROR_CODE[IConstants.ERROR_15] = "Logger could not find the log owner";
        ERROR_CODE[IConstants.ERROR_16] = "Illegal alarm time value";
        ERROR_CODE[IConstants.ERROR_17] = "There is no event in a queue";

        ERROR_CODE[IConstants.ERROR_18] = "Graph origin copy is null";
        ERROR_CODE[IConstants.ERROR_19] = "Process not found";

        ERROR_CODE[IConstants.ERROR_20] = "Destination ports cannot be null";
        ERROR_CODE[IConstants.ERROR_21] = "The function has been called at a wrong state";
        ERROR_CODE[IConstants.ERROR_22] = "Parameter cannot be null";
        ERROR_CODE[IConstants.ERROR_23] = "Does not met the condition to execute";
        
    }

    private short errorCode;

    public DisJException(short errorCode) {
        super(ERROR_CODE[errorCode]);
        this.errorCode = errorCode;
    }

    public DisJException(short errorCode, String extraInfo) {
        super(ERROR_CODE[errorCode] + " : " + extraInfo);
        this.errorCode = errorCode;
    }

    /**
     * This constructor use for throwing a java base exception
     * 
     * @param exception
     */
    public DisJException(Throwable exception) {
        super(exception.toString());
        this.errorCode = IConstants.JAVA_BASE_ERROR;
    }

}
